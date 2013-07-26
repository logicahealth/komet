package org.ihtsdo.otf.tcc.datastore.concept;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import javafx.application.Platform;

import javafx.concurrent.Worker;

import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.datastore.Bdb;
import org.ihtsdo.otf.tcc.datastore.ComponentBdb;
import org.ihtsdo.otf.tcc.datastore.id.NidCNidMapBdb;
import org.ihtsdo.otf.tcc.ddo.progress.AggregateProgressItem;
import org.ihtsdo.ttk.lookup.Looker;
import org.ihtsdo.ttk.lookup.TtkEnvironment;
import org.ihtsdo.ttk.lookup.WorkerPublisher;
import org.ihtsdo.ttk.lookup.properties.AllowItemCancel;
import org.ihtsdo.ttk.lookup.properties.ShowGlobalTaskProgress;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSetReadOnly;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.lucene.LuceneManager;
import org.ihtsdo.otf.tcc.chronicle.thread.NamedThreadFactory;

/**
 * Class description
 *
 *
 * @version        Enter version here..., 13/04/25
 * @author         Enter your name here...
 */
public class ConceptBdb extends ComponentBdb {

   /** Field description */
   private static final ThreadGroup conDbThreadGroup = new ThreadGroup("concept db threads");

   /** Field description */
   private static final int processors = Runtime.getRuntime().availableProcessors();

   /** Field description */
   private static final ExecutorService iteratorService =
      Executors.newCachedThreadPool(new NamedThreadFactory(conDbThreadGroup, "parallel iterator service"));

   /** Field description */
   private NativeIdSetBI conceptIdSet;

   /**
    * Constructs ...
    *
    *
    * @param readOnlyBdbEnv
    * @param mutableBdbEnv
    *
    * @throws IOException
    */
   public ConceptBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
      super(readOnlyBdbEnv, mutableBdbEnv);
      getReadOnlyConceptIdSet();
   }

   /**
    * Method description
    *
    *
    * @param c
    *
    * @throws IOException
    */
   public void forget(ConceptChronicle c) throws IOException {
      int cNid = c.getNid();

      try {
         DatabaseEntry key = new DatabaseEntry();

         IntegerBinding.intToEntry(cNid, key);

         // Perform the deletion. All records that use this key are
         // deleted.
         mutable.delete(null, key);
      } catch (DatabaseException | UnsupportedOperationException | IllegalArgumentException e) {
         throw new IOException(e);
      }

      conceptIdSet.setNotMember(cNid);
   }

   /**
    * Method description
    *
    *
    * @throws IOException
    */
   @Override
   protected void init() throws IOException {
      preloadBoth();
   }

   /**
    * Method description
    *
    *
    * @param processor
    * @param executors
    *
    * @throws ExecutionException
    * @throws IOException
    * @throws InterruptedException
    */
   private void iterateConceptData(final ProcessUnfetchedConceptDataBI processor, final int executors)
           throws IOException, InterruptedException, ExecutionException {

      // AceLog.getAppLog().info("Iterate in parallel. Executors: " + executors);
      NativeIdSetBI ids = processor.getNidSet();

      if (ids == null) {
         ids = getReadOnlyConceptIdSet();
      }

      boolean useFx                         = Looker.lookup(TtkEnvironment.class).useFxWorkers();
      int     cardinality                   = ids.size();
      int     idsPerParallelConceptIterator = cardinality / executors;

      // AceLog.getAppLog().info("Iterate in parallel. idsPerParallelConceptIterator: " + idsPerParallelConceptIterator);
      NativeIdSetItrBI  idsItr  = ids.getIterator();
      List<Future<?>> futures = new ArrayList<>(executors + 1);

      // TODO: add optimizations that make use of the PCI. Search in old workbench for examples.
      List<ParallelConceptIterator>     pcis            = new ArrayList<>();
      int                               sum             = 0;
      final List<ParallelConceptIteratorTask> progressWorkers = new ArrayList(executors + 1);
      int                               iteratorCount   = 0;

      while (idsItr.next()) {
         iteratorCount++;

         int first = idsItr.nid();
         int last  = first;
         int count = 1;

         while (idsItr.next()) {
            last = idsItr.nid();
            count++;

            if (count == idsPerParallelConceptIterator) {
               break;
            }
         }

         sum = sum + count;

         ParallelConceptIterator pci = new ParallelConceptIterator(first, last, count, processor, readOnly,
                                          mutable);

         pcis.add(pci);

         Future<?> f;

         if (useFx) {
            ParallelConceptIteratorTask task = new ParallelConceptIteratorTask(pci);

            task.updateTitle(processor.getTitle() + " iterator " + iteratorCount);
            task.updateMessage("Submitting to executor");
            task.updateProgress(0, count);
            progressWorkers.add(task);
            f = iteratorService.submit(task);
         } else {
            f = iteratorService.submit(pci);
         }

         futures.add(f);
      }

      if (useFx) {
         Platform.runLater(new Runnable() {
            @Override
            public void run() {
               AggregateProgressItem aggregateProgressItem =
                  new AggregateProgressItem(
                      processor.getTitle(), "Iterating over concepts with " + executors + " executors",
                      (Worker<?>[]) progressWorkers.toArray(
                         new ParallelConceptIteratorTask[progressWorkers.size()]));

               if (processor.allowCancel()) {
                  WorkerPublisher.publish(aggregateProgressItem, processor.getTitle(),
                                          Arrays.asList(new ShowGlobalTaskProgress(), new AllowItemCancel()));
               } else {
                  WorkerPublisher.publish(aggregateProgressItem, processor.getTitle(),
                                          Collections.singletonList(new ShowGlobalTaskProgress()));
               }
            }
         });
      }

      for (Future<?> f : futures) {
         f.get();
      }
   }

   /**
    * Method description
    *
    *
    * @param processor
    *
    * @throws Exception
    */
   public void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception {
      iterateConceptData(processor, processors);
   }

   /**
    * Method description
    *
    *
    * @param processor
    *
    * @throws Exception
    */
   public void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception {
      iterateConceptData(processor, 1);
   }

   /**
    * Method description
    *
    *
    * @param concept
    *
    * @throws IOException
    */
   public void writeConcept(ConceptChronicle concept) throws IOException {
      if (concept.isCanceled()) {
         return;
      }

      ConceptBinder binder = new ConceptBinder();
      DatabaseEntry key    = new DatabaseEntry();
      int           cNid   = concept.getNid();

      conceptIdSet.setMember(cNid);
      IntegerBinding.intToEntry(cNid, key);

      DatabaseEntry value = new DatabaseEntry();

      synchronized (concept) {
         long writeVersion = Bdb.gVersion.incrementAndGet();

         if (writeVersion < concept.getDataVersion()) {
            Bdb.gVersion.set(concept.getDataVersion() + 1);
            writeVersion = Bdb.gVersion.incrementAndGet();
         }

         binder.objectToEntry(concept, value);
         concept.resetNidData();
         mutable.put(null, key, value);
         concept.setLastWrite(writeVersion);
         Bdb.getNidCNidMap().updateOutgoingRelationshipData(concept);
      }

      Collection<Integer> nids      = concept.getAllNids();
      NidCNidMapBdb       nidCidMap = Bdb.getNidCNidMap();

      for (int nid : nids) {
         assert nid != 0 : "nid is 0: " + nids;
         nidCidMap.setCNidForNid(cNid, nid);
      }
   }

   /**
    * Method description
    *
    *
    * @param cNid
    *
    * @return
    *
    * @throws IOException
    */
   public ConceptChronicle getConcept(int cNid) throws IOException {
      assert cNid != Integer.MAX_VALUE;

      return ConceptChronicle.get(cNid);
   }

   /**
    *
    * @return a mutable bit set, with all concept identifiers set to true.
    * @throws IOException
    */
   public NativeIdSetBI getConceptNidSet() throws IOException {
      return new ConcurrentBitSet(conceptIdSet);
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws IOException
    */
   public int getCount() throws IOException {
      return (int) getReadOnlyConceptIdSet().cardinality();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   protected String getDbName() {
      return "conceptDb";
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws IOException
    */
   public NativeIdSetBI getEmptyIdSet() throws IOException {
      return new ConcurrentBitSet(getReadOnlyConceptIdSet().size());
   }

   /**
    *
    * @return a read-only bit set, with all concept identifiers set to true.
    * @throws IOException
    */
   public final ConcurrentBitSetReadOnly getReadOnlyConceptIdSet() throws IOException {
      if (conceptIdSet == null) {
         GetCNids readOnlyGetter = new GetCNids(readOnly);
         GetCNids mutableGetter  = new GetCNids(mutable);

         try {
            ConcurrentBitSet readOnlyMap = readOnlyGetter.call();
            ConcurrentBitSet mutableMap  = mutableGetter.call();

            readOnlyMap.or(mutableMap);
            conceptIdSet = new ConcurrentBitSet(readOnlyMap);
         } catch (Exception e) {
            throw new IOException(e);
         }
      }

      return new ConcurrentBitSetReadOnly(conceptIdSet);
   }

   /**
    * Method description
    *
    *
    * @param cNid
    *
    * @return
    *
    * @throws IOException
    */
   public List<UUID> getUuidsForConcept(int cNid) throws IOException {
      return getConcept(cNid).getUUIDs();
   }

   /**
    * Method description
    *
    *
    * @param cNid
    *
    * @return
    *
    * @throws IOException
    */
   public ConceptChronicle getWritableConcept(int cNid) throws IOException {
      return ConceptChronicle.get(cNid);
   }

   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/25
    * @author         Enter your name here...
    */
   private static class GetCNids implements Callable<ConcurrentBitSet> {

      /** Field description */
      private Database db;

      /**
       * Constructs ...
       *
       *
       * @param db
       */
      public GetCNids(Database db) {
         super();
         this.db = db;
      }

      /**
       * Method description
       *
       *
       * @return
       *
       * @throws Exception
       */
      @Override
      public ConcurrentBitSet call() throws Exception {
         int           size         = (int) db.count();
         ConcurrentBitSet nidMap       = new ConcurrentBitSet(size + 2);
         CursorConfig  cursorConfig = new CursorConfig();

         cursorConfig.setReadUncommitted(true);

         try (Cursor cursor = db.openCursor(null, cursorConfig)) {
            DatabaseEntry foundKey  = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();

            foundData.setPartial(true);
            foundData.setPartial(0, 0, true);

            while (cursor.getNext(foundKey, foundData, LockMode.READ_UNCOMMITTED)
                   == OperationStatus.SUCCESS) {
               int cNid = IntegerBinding.entryToInt(foundKey);

               nidMap.setMember(cNid);
            }

            cursor.close();

            return nidMap;
         }
      }
   }

    @Override
    public void sync() throws IOException {
        super.sync(); 
        LuceneManager.commit();
    }
}
