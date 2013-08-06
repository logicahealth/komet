package org.ihtsdo.otf.tcc.model.cc.lucene;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.description.Description;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexNotFoundException;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.thread.NamedThreadFactory;

public abstract class LuceneManager {

    protected static final Logger logger = Logger.getLogger(ConceptChronicle.class.getName());
    private static NativeIdSetBI uncommittedDescNids = new ConcurrentBitSet();
    protected static DescriptionIndexGenerator descIndexer = null;
    public final static Version version = Version.LUCENE_40;
    private static Semaphore initSemaphore = new Semaphore(1);
    private static Semaphore luceneWriterPermit = new Semaphore(50);
    private static ExecutorService luceneWriterService =
            Executors.newFixedThreadPool(1, new NamedThreadFactory(new ThreadGroup("Lucene group"), "Lucene writer"));
    public static Directory descLuceneMutableDir;
    public static Directory descLuceneReadOnlyDir;
    protected static IndexReader descReadOnlyReader;
    protected static IndexWriter descWriter;
    protected static DirectoryReader mutableSearcher;

    public static void commitDescriptionsToLucene() throws InterruptedException {
        luceneWriterPermit.acquire();

        NativeIdSetBI descNidsToCommit = new ConcurrentBitSet(uncommittedDescNids);

        uncommittedDescNids.clear();
        luceneWriterService.execute(new DescLuceneWriter(descNidsToCommit));
    }

    public static void commitDescriptionsToLucene(ConceptChronicle c) throws InterruptedException, IOException {
        luceneWriterPermit.acquire();

        NativeIdSetBI descNidsToCommit = new ConcurrentBitSet();

        for (int dnid : c.getDescriptionNids()) {
            descNidsToCommit.setMember(dnid);
            uncommittedDescNids.setNotMember(dnid);
        }

        luceneWriterService.execute(new DescLuceneWriter(descNidsToCommit));
    }

    public static void addUncommittedDescNid(int dNid) {
        uncommittedDescNids.setMember(dNid);
    }

    public static void close() {
        IndexWriter writer;

        writer = descWriter;

        if (writer != null) {
            try {
                writer.commit();
                writer.close(true);
                writer = null;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Exception during lucene writer close", e);
            }
        }

        descWriter = writer;
        logger.info("Shutting down luceneWriterService.");
        luceneWriterService.shutdown();
        logger.info("Awaiting termination of luceneWriterService.");

        try {
            luceneWriterService.awaitTermination(90, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public static void createLuceneIndex() throws Exception {
        createLuceneIndex(null);
    }

    public static void createLuceneIndex(ViewCoordinate viewCoord) throws Exception {
        IndexWriter writer;

        init();
        writer = descWriter;

        if (writer == null) {
            DescriptionLuceneManager.descLuceneMutableDirFile.mkdirs();
            descLuceneMutableDir = setupWriter(DescriptionLuceneManager.descLuceneMutableDirFile, descLuceneMutableDir);
        }

        descIndexer = new DescriptionIndexGenerator(writer);
        P.s.iterateConceptDataInSequence(descIndexer);
        writer.commit();
    }

    public static boolean indexExists() {
        return DescriptionLuceneManager.descLuceneMutableDirFile.exists();
    }

    public static void init() throws IOException {

        // Only do if not first time
        if (descLuceneReadOnlyDir == null) {
            initSemaphore.acquireUninterruptibly();

            try {
                if (descLuceneReadOnlyDir == null) {
                    descLuceneReadOnlyDir = initDirectory(DescriptionLuceneManager.descLuceneReadOnlyDirFile, false);
                    if (DirectoryReader.indexExists(descLuceneReadOnlyDir)) {
                            descReadOnlyReader = DirectoryReader.open(descLuceneReadOnlyDir);
                     }
 
                }
            } catch (IndexNotFoundException ex) {
                System.out.println(ex.toString());
                descReadOnlyReader = null;
            } finally {
                initSemaphore.release();
            }
        }

        if (descLuceneMutableDir == null) {
            initSemaphore.acquireUninterruptibly();

            try {
                if (descLuceneMutableDir == null) {
                    descLuceneMutableDir = initDirectory(DescriptionLuceneManager.descLuceneMutableDirFile, true);
                }
            } finally {
                initSemaphore.release();
            }
        }
    }

    private static Directory initDirectory(File luceneDirFile, boolean mutable)
            throws IOException, CorruptIndexException, LockObtainFailedException {
        Directory luceneDir;

        if (luceneDirFile.exists()) {
            luceneDir = new SimpleFSDirectory(luceneDirFile);

            if (mutable) {
                setupWriter(luceneDirFile, luceneDir);
            }
        } else {
            luceneDirFile.mkdirs();
            luceneDir = new SimpleFSDirectory(luceneDirFile);

            if (mutable) {
                setupWriter(luceneDirFile, luceneDir);
            }
        }

        return luceneDir;
    }

    public static SearchResult search(Query q) throws CorruptIndexException, IOException {
        IndexSearcher searcher;
         
        init();

        int matchLimit = getMatchLimit();


        TtkMultiReader mr = null;


        if (descReadOnlyReader != null) {
            DirectoryReader newMutableSearcher = 
                        DirectoryReader.openIfChanged(mutableSearcher, descWriter, true);
            if (newMutableSearcher != null) {
                mutableSearcher.close();
                mutableSearcher = newMutableSearcher;
            }
            mr = new TtkMultiReader(descReadOnlyReader, mutableSearcher);
            searcher = new IndexSearcher(mr);
            searcher.setSimilarity(new ShortTextSimilarity());
        } else {
            DirectoryReader newMutableSearcher = 
                        DirectoryReader.openIfChanged(mutableSearcher, descWriter, true);
            if (newMutableSearcher != null) {
                mutableSearcher.close();
                mutableSearcher = newMutableSearcher;
            }
            searcher = new IndexSearcher(mutableSearcher);
            searcher.setSimilarity(new ShortTextSimilarity());
        }


        TopDocs topDocs = searcher.search(q, null, matchLimit);

        // Suppress duplicates in the read-only index
        List<ScoreDoc> newDocs = new ArrayList<>(topDocs.scoreDocs.length);
        HashSet<Integer> ids = new HashSet<>(topDocs.scoreDocs.length);
        String searchTerm = "dnid";


        if (mr != null) {
            for (ScoreDoc sd : topDocs.scoreDocs) {
                if (!mr.isFirstIndex(sd.doc)) {
                    newDocs.add(sd);

                    Document d = searcher.doc(sd.doc);
                    int nid = Integer.parseInt(d.get(searchTerm));

                    ids.add(nid);
                }
            }
        }

        for (ScoreDoc sd : topDocs.scoreDocs) {
            if ((mr == null) || mr.isFirstIndex(sd.doc)) {
                Document d = searcher.doc(sd.doc);
                int nid = Integer.parseInt(d.get(searchTerm));

                if (!ids.contains(nid)) {
                    newDocs.add(sd);
                }
            }
        }

        // Lucene match explainer code, useful to tweak the lucene score, uncomment for debug purposes only
        boolean explainMatch = false;

        if (explainMatch) {
            for (ScoreDoc sd : newDocs) {
                Document d = searcher.doc(sd.doc);
                DescriptionChronicleBI desc = null;
                try {
                    desc = (DescriptionChronicleBI) Ts.get().getComponent(Integer.valueOf(d.get("dnid")));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (desc != null) {
                    System.out.println("-------------------------" + desc.getPrimordialVersion().getText());
                } else {
                    System.out.println("------------------------- Null");
                }
                System.out.println(searcher.explain(q, sd.doc).toString());
            }
        }
        topDocs.scoreDocs = newDocs.toArray(new ScoreDoc[newDocs.size()]);
        topDocs.totalHits = topDocs.scoreDocs.length;

        return new SearchResult(topDocs, searcher);

    }

    protected static Directory setupWriter(File luceneDirFile, Directory luceneDir)
            throws IOException, CorruptIndexException, LockObtainFailedException {
        if (luceneDir == null) {
            luceneDir = new SimpleFSDirectory(luceneDirFile);
        }

        luceneDir.clearLock("write.lock");

        IndexWriter writer;
        IndexWriterConfig config = new IndexWriterConfig(version, new StandardAnalyzer(version));
        MergePolicy mergePolicy = new LogByteSizeMergePolicy();

        config.setMergePolicy(mergePolicy);
        config.setSimilarity(new ShortTextSimilarity());

        if (new File(luceneDirFile, "segments.gen").exists()) {
            writer = new IndexWriter(luceneDir, config);
        } else {
            writer = new IndexWriter(luceneDir, config);
        }

        descWriter = writer;
        mutableSearcher = DirectoryReader.open(writer, true);

        return luceneDir;
    }

    public static void writeToLucene(Collection items) throws IOException {
        writeToLucene(items, null);
    }

    public static synchronized void writeToLucene(Collection items, ViewCoordinate viewCoord) throws IOException {
        init();

        try {

            DescriptionLuceneManager.writeToLuceneNoLock(items);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static int getMatchLimit() {
        return DescriptionLuceneManager.matchLimit;
    }

    public static void setLuceneRootDir(File root) throws IOException {
        IndexWriter writer;

        DescriptionLuceneManager.descLuceneMutableDirFile = new File(root,
                DescriptionLuceneManager.descMutableDirectorySuffix);
        DescriptionLuceneManager.descLuceneReadOnlyDirFile = new File(root,
                DescriptionLuceneManager.descReadOnlyDirectorySuffix);
        writer = descWriter;

        if (writer != null) {
            try {
                writer.close(true);
            } catch (CorruptIndexException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        descLuceneMutableDir = null;
        descLuceneReadOnlyDir = null;
    }

    public static void setMatchLimit(int limit) {
        DescriptionLuceneManager.matchLimit = limit;
    }

    private static class DescLuceneWriter implements Runnable {

        private int batchSize = 200;
        private NativeIdSetBI descNidsToWrite;

        public DescLuceneWriter(NativeIdSetBI descNidsToCommit) {
            super();
            this.descNidsToWrite = descNidsToCommit;
        }

        @Override
        public void run() {
            try {
                ArrayList<Description> toIndex = new ArrayList<>(batchSize + 1);
                NativeIdSetItrBI idItr = descNidsToWrite.getIterator();
                int count = 0;

                while (idItr.next()) {
                    count++;

                    Description d = (Description) P.s.getComponent(idItr.nid());

                    toIndex.add(d);

                    if (count > batchSize) {
                        count = 0;
                        LuceneManager.writeToLucene(toIndex);
                        toIndex = new ArrayList<>(batchSize + 1);
                    }
                }

                LuceneManager.writeToLucene(toIndex);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }

            luceneWriterPermit.release();
        }
    }
    
    public static void commit() throws IOException {
        if (descWriter != null) {
            descWriter.commit();
        }
        
    }

    protected static class ShortTextSimilarity extends DefaultSimilarity {

        public ShortTextSimilarity() {
        }

        @Override
        public float coord(int overlap, int maxOverlap) {
            return 1.0f;
        }

        @Override
        public float tf(float freq) {
            return 1.0f;
        }

        @Override
        public float tf(int freq) {
            return 1.0f;
        }
    }
}
