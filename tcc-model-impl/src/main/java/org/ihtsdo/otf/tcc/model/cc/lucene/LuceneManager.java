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
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.model.cc.description.Description;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.thread.NamedThreadFactory;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.termstore.SearchType;

public abstract class LuceneManager {

    protected static final Logger logger = Logger.getLogger(ConceptChronicleBI.class.getName());
    private static NativeIdSetBI uncommittedDescNids = new ConcurrentBitSet();
    protected static DescriptionIndexGenerator descIndexer = null;
    public final static Version version = Version.LUCENE_40;
    private static Semaphore initSemaphore = new Semaphore(1);
    private static Semaphore luceneWriterPermit = new Semaphore(50);
    private static Semaphore refsetInitSemaphore = new Semaphore(1);
    private static Semaphore refsetLuceneWriterPermit = new Semaphore(50);
    private static ExecutorService luceneWriterService =
            Executors.newFixedThreadPool(1, new NamedThreadFactory(new ThreadGroup("Lucene group"), "Lucene writer"));
    public static Directory descLuceneMutableDir;
    public static Directory descLuceneReadOnlyDir;
    protected static IndexReader descReadOnlyReader;
    public static IndexWriter descWriter;
    protected static DirectoryReader mutableSearcher;
    protected static DirectoryReader mutableSearcher2;
    protected static IndexReader refsetReadOnlyReader;
    public static IndexWriter refsetWriter;
    public static Directory refsetLuceneMutableDir;
    public static Directory refsetLuceneReadOnlyDir;
    private static NativeIdSetBI uncommittedRefsetNids = new ConcurrentBitSet();
    protected static RefsetIndexGenerator refsetIndexer = null;

    public static void commitDescriptionsToLucene() throws InterruptedException {
        luceneWriterPermit.acquire();

        NativeIdSetBI descNidsToCommit = new ConcurrentBitSet(uncommittedDescNids);

        uncommittedDescNids.clear();
        luceneWriterService.execute(new DescLuceneWriter(descNidsToCommit));
    }

    public static void commitRefsetsToLucene() throws InterruptedException {
        refsetLuceneWriterPermit.acquire();

        NativeIdSetBI refsetNidsToCommit = new ConcurrentBitSet(uncommittedRefsetNids);
        uncommittedRefsetNids.clear();
        luceneWriterService.execute(new RefsetLuceneWriter(refsetNidsToCommit));
    }

  /*  public static void commitRefsetsToLucene(ConceptChronicleBI c) throws InterruptedException, IOException {
        refsetLuceneWriterPermit.acquire();
        NativeIdSetBI descNidsToCommit = new ConcurrentBitSet();

        for (DescriptionChronicleBI dnid : c.getRefsetMemberForComponent(c.getNid())) {
            descNidsToCommit.setMember(dnid.getNid());
            uncommittedDescNids.setNotMember(dnid.getNid());
        }

        luceneWriterService.execute(new DescLuceneWriter(descNidsToCommit));
    }*/

    public static void commitDescriptionsToLucene(ConceptChronicleBI c) throws InterruptedException, IOException {
        luceneWriterPermit.acquire();

        NativeIdSetBI descNidsToCommit = new ConcurrentBitSet();

        for (DescriptionChronicleBI dnid : c.getDescriptions()) {
            descNidsToCommit.setMember(dnid.getNid());
            uncommittedDescNids.setNotMember(dnid.getNid());
        }

        luceneWriterService.execute(new DescLuceneWriter(descNidsToCommit));
    }

    public static void addUncommittedDescNid(int dNid) {
        uncommittedDescNids.setMember(dNid);
    }

    public static void closeRefset() {
        IndexWriter writer;

        writer = refsetWriter;
        if (writer != null) {
            try {
                writer.commit();
                writer.close(true);
                writer = null;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Exception during lucene writer close", e);
            }
        }

        refsetWriter = writer;
        logger.info("Shutting down luceneWriterService.");
        luceneWriterService.shutdown();
        logger.info("Awaiting termination of luceneWriterService.");

        try {
            luceneWriterService.awaitTermination(90, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
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
            descLuceneMutableDir = setupWriter(DescriptionLuceneManager.descLuceneMutableDirFile, descLuceneMutableDir, SearchType.DESCRIPTION);
        }

        descIndexer = new DescriptionIndexGenerator(writer);
        P.s.iterateConceptDataInSequence(descIndexer);
        writer.commit();
    }

    public static void createRefsetLuceneIndex() throws Exception {
        createRefsetLuceneIndex(null);
    }

    public static void createRefsetLuceneIndex(ViewCoordinate viewCoord) throws Exception {
        IndexWriter writer;
        init();
        writer = refsetWriter;

        if (writer == null) {
            RefsetLuceneManager.refsetLuceneMutableDirFile.mkdirs();
            refsetLuceneMutableDir = setupRefsetWriter(RefsetLuceneManager.refsetLuceneMutableDirFile, refsetLuceneMutableDir);

        }

        refsetIndexer = new RefsetIndexGenerator(writer);
        P.s.iterateConceptDataInSequence(refsetIndexer);
        writer.commit();

    }

    public static boolean indexExists() {
        return DescriptionLuceneManager.descLuceneMutableDirFile.exists();
    }

    public static void initRefset() throws IOException {
        if (refsetLuceneReadOnlyDir == null) {
            refsetInitSemaphore.acquireUninterruptibly();

            try {
                if (refsetLuceneReadOnlyDir == null) {
                    refsetLuceneReadOnlyDir = initDirectory(RefsetLuceneManager.refsetLuceneReadOnlyDirFile, false, SearchType.REFSET);
                    refsetReadOnlyReader = DirectoryReader.open(refsetLuceneReadOnlyDir);
                }
            } catch (IndexNotFoundException ex) {
                System.out.println(ex.toString());
                refsetReadOnlyReader = null;
            } finally {
                refsetInitSemaphore.release();
            }
        }


        if (refsetLuceneMutableDir == null) {
            refsetInitSemaphore.acquireUninterruptibly();

            try {
                if (refsetLuceneMutableDir == null) {
                    refsetLuceneMutableDir = initDirectory(RefsetLuceneManager.refsetLuceneMutableDirFile, true, SearchType.REFSET);
                }
            } finally {
                refsetInitSemaphore.release();
            }
        }

    }

    public static void init() throws IOException {

        // Only do if not first time
        if (descLuceneReadOnlyDir == null) {
            initSemaphore.acquireUninterruptibly();

            try {
                if (descLuceneReadOnlyDir == null) {
                    descLuceneReadOnlyDir = initDirectory(DescriptionLuceneManager.descLuceneReadOnlyDirFile, false, SearchType.DESCRIPTION);
                    descReadOnlyReader = DirectoryReader.open(descLuceneReadOnlyDir);

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
                    descLuceneMutableDir = initDirectory(DescriptionLuceneManager.descLuceneMutableDirFile, true, SearchType.DESCRIPTION);
                }
            } finally {
                initSemaphore.release();
            }
        }

        /*       if (refsetLuceneReadOnlyDir == null) {
         initSemaphore.acquireUninterruptibly();

         try {
         if (refsetLuceneReadOnlyDir == null) {
         refsetLuceneReadOnlyDir = initDirectory(RefsetLuceneManager.refsetLuceneReadOnlyDirFile, false);
         refsetReadOnlyReader = DirectoryReader.open(refsetLuceneReadOnlyDir);
         }
         } catch (IndexNotFoundException ex) {
         System.out.println(ex.toString());
         refsetReadOnlyReader = null;
         } finally {
         initSemaphore.release();
         }
         }


         if (refsetLuceneMutableDir == null) {
         initSemaphore.acquireUninterruptibly();

         try {
         if (refsetLuceneMutableDir == null) {
         refsetLuceneMutableDir = initDirectory(RefsetLuceneManager.refsetLuceneMutableDirFile, true);
         }
         } finally {
         initSemaphore.release();
         }
         }*/
    }

    private static Directory initDirectory(File luceneDirFile, boolean mutable, SearchType type)
            throws IOException, CorruptIndexException, LockObtainFailedException {
        Directory luceneDir;

        if (luceneDirFile.exists()) {
            luceneDir = new SimpleFSDirectory(luceneDirFile);

            if (mutable) {
                setupWriter(luceneDirFile, luceneDir, type);
            }
        } else {
            luceneDirFile.mkdirs();
            luceneDir = new SimpleFSDirectory(luceneDirFile);

            if (mutable) {
                setupWriter(luceneDirFile, luceneDir, type);
            }
        }

        return luceneDir;
    }

    /*public SearchResult descriptionLuceneSearch(String input) throws CorruptIndexException, IOException{
     Term t = new Term(input);
     Query query = new TermQuery(t);
        
     return search(query);    
       
        
     }*/
    public static SearchResult search(Query q, SearchType type) throws CorruptIndexException, IOException {
        IndexSearcher searcher;
        init();

        int matchLimit = getMatchLimit();


        TtkMultiReader mr = null;

        switch (type) {
            case DESCRIPTION:
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

            case REFSET:
                if (refsetReadOnlyReader != null) {
                    DirectoryReader newMutableSearcher =
                            DirectoryReader.openIfChanged(mutableSearcher, refsetWriter, true);
                    if (newMutableSearcher != null) {
                        mutableSearcher.close();
                        mutableSearcher = newMutableSearcher;
                    }
                    mr = new TtkMultiReader(refsetReadOnlyReader, mutableSearcher);
                    searcher = new IndexSearcher(mr);
                    searcher.setSimilarity(new ShortTextSimilarity());
                } else {
                    DirectoryReader newMutableSearcher =
                            DirectoryReader.openIfChanged(mutableSearcher, refsetWriter, true);
                    if (newMutableSearcher != null) {
                        mutableSearcher.close();
                        mutableSearcher = newMutableSearcher;
                    }
                    searcher = new IndexSearcher(mutableSearcher);
                    searcher.setSimilarity(new ShortTextSimilarity());
                }


                TopDocs topDocs2 = searcher.search(q, null, matchLimit);

                // Suppress duplicates in the read-only index
                List<ScoreDoc> newDocs2 = new ArrayList<>(topDocs2.scoreDocs.length);
                HashSet<Integer> ids2 = new HashSet<>(topDocs2.scoreDocs.length);
                String searchTerm2 = "rnid";

                if (mr != null) {
                    for (ScoreDoc sd : topDocs2.scoreDocs) {
                        if (!mr.isFirstIndex(sd.doc)) {
                            newDocs2.add(sd);

                            Document d = searcher.doc(sd.doc);
                            int nid = Integer.parseInt(d.get(searchTerm2));

                            ids2.add(nid);
                        }
                    }
                }

                for (ScoreDoc sd : topDocs2.scoreDocs) {
                    if ((mr == null) || mr.isFirstIndex(sd.doc)) {
                        Document d = searcher.doc(sd.doc);
                        int nid = Integer.parseInt(d.get(searchTerm2));

                        if (!ids2.contains(nid)) {
                            newDocs2.add(sd);
                        }
                    }
                }

                // Lucene match explainer code, useful to tweak the lucene score, uncomment for debug purposes only
                boolean explainMatch2 = false;

                if (explainMatch2) {
                    for (ScoreDoc sd : newDocs2) {
                        Document d = searcher.doc(sd.doc);
                        RefexChronicleBI refset = null;
                        try {
                            refset = (RefexChronicleBI) Ts.get().getComponent(Integer.valueOf(d.get("rnid")));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        if (refset != null) {
                            System.out.println("-------------------------" + refset.getPrimordialVersion().toString());
                        } else {
                            System.out.println("------------------------- Null");
                        }
                        System.out.println(searcher.explain(q, sd.doc).toString());
                    }
                }
                topDocs2.scoreDocs = newDocs2.toArray(new ScoreDoc[newDocs2.size()]);
                topDocs2.totalHits = topDocs2.scoreDocs.length;

                return new SearchResult(topDocs2, searcher);
            default:
                throw new UnsupportedOperationException("This search type isn't supported yet");
        }






    }

    public static SearchResult refsetSearch(Query q) throws IOException {
        IndexSearcher searcher;

        initRefset();

        int matchLimit = getMatchLimit();


        TtkMultiReader mr = null;



        if (refsetReadOnlyReader != null) {
            DirectoryReader newMutableSearcher =
                    DirectoryReader.openIfChanged(mutableSearcher, refsetWriter, true);
            if (newMutableSearcher != null) {
                mutableSearcher.close();
                mutableSearcher = newMutableSearcher;
            }
            mr = new TtkMultiReader(refsetReadOnlyReader, mutableSearcher);
            searcher = new IndexSearcher(mr);
            searcher.setSimilarity(new ShortTextSimilarity());
        } else {
            DirectoryReader newMutableSearcher =
                    DirectoryReader.openIfChanged(mutableSearcher, refsetWriter, true);
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
        String searchTerm = "rnid";

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
                RefexChronicleBI refset = null;
                try {
                    refset = (RefexChronicleBI) Ts.get().getComponent(Integer.valueOf(d.get("rnid")));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (refset != null) {
                    System.out.println("-------------------------" + refset.getPrimordialVersion().toString());
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

    protected static Directory setupWriter(File luceneDirFile, Directory luceneDir, SearchType type)
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

        switch (type) {
            case DESCRIPTION:
                descWriter = writer;
            case REFSET:
                refsetWriter = writer;


        }

        mutableSearcher = DirectoryReader.open(writer, true);

        return luceneDir;
    }

    protected static Directory setupRefsetWriter(File luceneDirFile, Directory luceneDir) throws IOException {
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

        refsetWriter = writer;
        mutableSearcher = DirectoryReader.open(writer, true);

        return luceneDir;

    }

    public static void writeToLucene(Collection items) throws IOException {
        writeToLucene(items, null);
    }

    public static void refsetWriteToLucene(Collection items) throws IOException {
        refsetWriteToLucene(items, null);
    }

    public static synchronized void refsetWriteToLucene(Collection items, ViewCoordinate viewCoord) throws IOException {
        initRefset();
        try {

            RefsetLuceneManager.writeToLuceneNoLock(items);
        } catch (IOException e) {
            throw new IOException(e);
        }

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

    public static void setRefsetLuceneRootDir(File root) throws IOException {
        IndexWriter writer;

        RefsetLuceneManager.refsetLuceneMutableDirFile = new File(root,
                RefsetLuceneManager.refsetMutableDirectorySuffix);
        RefsetLuceneManager.refsetLuceneReadOnlyDirFile = new File(root,
                RefsetLuceneManager.refsetReadOnlyDirectorySuffix);
        writer = refsetWriter;

        if (writer != null) {
            try {
                writer.close(true);
            } catch (CorruptIndexException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        refsetLuceneMutableDir = null;
        refsetLuceneReadOnlyDir = null;



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

    private static class RefsetLuceneWriter implements Runnable {

        private int batchSize = 200;
        private NativeIdSetBI refsetNidsToWrite;

        public RefsetLuceneWriter(NativeIdSetBI refsetNidsToCommit) {
            super();
            this.refsetNidsToWrite = refsetNidsToCommit;
        }

        @Override
        public void run() {
            try {
                ArrayList<RefexMember> toIndex = new ArrayList<>(batchSize + 1);
                NativeIdSetItrBI iter = refsetNidsToWrite.getIterator();
                int count = 0;

                while (iter.next()) {
                    count++;

                    RefexMember r = (RefexMember) P.s.getComponent(iter.nid());
                    toIndex.add(r);

                    if (count > batchSize) {
                        count = 0;
                        LuceneManager.refsetWriteToLucene(toIndex);
                        toIndex = new ArrayList<>(batchSize + 1);
                    }
                }
                LuceneManager.refsetWriteToLucene(toIndex);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            luceneWriterPermit.release();
        }

        public static void commit() throws IOException {
            if (refsetWriter != null) {
                refsetWriter.commit();
            }

        }
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
