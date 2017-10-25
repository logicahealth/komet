/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.provider.bdb.taxonomy;

//~--- JDK imports ------------------------------------------------------------

import java.nio.file.Path;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

//~--- JDK imports ------------------------------------------------------------

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DiskOrderedCursor;
import com.sleepycat.je.DiskOrderedCursorConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import sh.isaac.api.ConceptActiveService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.SpinedIntObjectMap;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.task.TaskWrapper;
import sh.isaac.api.tree.Tree;
import sh.isaac.provider.bdb.chronology.BdbProvider;
import sh.isaac.provider.bdb.disruptor.ChronologyUpdate;
import sh.isaac.provider.bdb.disruptor.UpdateAction;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class BdbTaxonomyProvider
         implements TaxonomyService, ConceptActiveService, ChronologyChangeListener {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /**
    * The stamped lock.
    */
   private final StampedLock stampedLock = new StampedLock();

   /**
    * The semantic sequences for unhandled changes.
    */
   private final ConcurrentSkipListSet<Integer> semanticSequencesForUnhandledChanges = new ConcurrentSkipListSet<>();

   /**
    * The tree cache.
    */
   private final ConcurrentHashMap<Integer, Task<Tree>> snapshotCache = new ConcurrentHashMap<>(5);

   /**
    * The {@code taxonomyMap} associates concept sequence keys with a primitive taxonomy record, which represents the
    * destination, stamp, and taxonomy flags for parent and child concepts.
    */
   private final SpinedIntObjectMap<int[]> origin_DestinationTaxonomyRecord_Map = new SpinedIntObjectMap<>();
   private final UUID                      listenerUUID                       = UUID.randomUUID();

   /**
    * The identifier service.
    */
   private IdentifierService identifierService;
   private BdbProvider       bdb;
   private Database          database;
   private int               inferredAssemblageSequence;
   private int               isaSequence;
   private int               roleGroupSequence;

   //~--- constructors --------------------------------------------------------

   public BdbTaxonomyProvider() {}

   //~--- methods -------------------------------------------------------------

   @Override
   public void clearDatabaseValidityValue() {
      this.bdb.clearDatabaseValidityValue();
   }

   @Override
   public void handleChange(ConceptChronology cc) {
      // not processing concept changes
      // is this call redundant/better than update status call above?
   }

   @Override
   public void handleChange(SemanticChronology sc) {
      if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
         this.semanticSequencesForUnhandledChanges.add(sc.getSemanticSequence());
      }
   }

   @Override
   public void handleCommit(CommitRecord commitRecord) {
      // If a logic graph changed, clear our cache.
      if (this.semanticSequencesForUnhandledChanges.size() > 0) {
         this.snapshotCache.clear();
      }

      UpdateTaxonomyAfterCommitTask.get(
          this,
          commitRecord,
          this.semanticSequencesForUnhandledChanges,
          this.stampedLock);
   }

   @Override
   public void updateStatus(ConceptChronology conceptChronology) {
      ChronologyUpdate.handleStatusUpdate(conceptChronology);
    }

   @Override
   public void updateTaxonomy(SemanticChronology logicGraphChronology) {
      ChronologyUpdate.handleTaxonomyUpdate(logicGraphChronology);
   }

   @Override
   public boolean wasEverKindOf(int childId, int parentId) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting BdbTaxonomyProvider post-construct");
         this.inferredAssemblageSequence = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getConceptSequence();
         this.isaSequence                = TermAux.IS_A.getConceptSequence();
         this.roleGroupSequence          = TermAux.ROLE_GROUP.getConceptSequence();
         bdb                             = Get.service(BdbProvider.class);
         database                        = bdb.getTaxonomyDatabase();
         Get.commitService()
            .addChangeListener(this);
         this.identifierService = Get.identifierService();

         IntArrayBinding binding = new IntArrayBinding();
         DiskOrderedCursorConfig docc      = new DiskOrderedCursorConfig();
         DatabaseEntry           foundKey  = new DatabaseEntry();
         DatabaseEntry           foundData = new DatabaseEntry();
         
         this.origin_DestinationTaxonomyRecord_Map.setElementStringConverter((int[] records) -> {
            return new TaxonomyRecord(records).toString(); 
         });

         try (DiskOrderedCursor cursor = database.openCursor(docc)) {
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
               this.origin_DestinationTaxonomyRecord_Map.put(IntegerBinding.entryToInt(foundKey), binding.entryToObject(foundData));
            }
         }
         ChronologyUpdate.setOrigin_DestinationTaxonomyRecord_Map(this.origin_DestinationTaxonomyRecord_Map);
      } catch (final Exception e) {
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Bdb Taxonomy Provider", e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Writing taxonomy.");

      IntArrayBinding binding = new IntArrayBinding();

      this.origin_DestinationTaxonomyRecord_Map.forEach(
          (int key,
           int[] value) -> {
             DatabaseEntry keyEntry = new DatabaseEntry();

             IntegerBinding.intToEntry(key, keyEntry);

             DatabaseEntry valueEntry = new DatabaseEntry();

             binding.objectToEntry(value, valueEntry);

             OperationStatus result = database.put(null, keyEntry, valueEntry);

             if (result != OperationStatus.SUCCESS) {
                throw new RuntimeException("taxonomy data did not write: " + result);
             }
          });
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      long  stamp        = this.stampedLock.tryOptimisticRead();
      int[] taxonomyData = this.origin_DestinationTaxonomyRecord_Map.get(conceptSequence);

      if (taxonomyData == null) {
         return false;
      }

      TaxonomyRecordPrimitive taxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);

      if (this.stampedLock.validate(stamp)) {
         return taxonomyRecord.isConceptActive(conceptSequence, stampCoordinate);
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyData   = this.origin_DestinationTaxonomyRecord_Map.get(conceptSequence);
         taxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);
         return taxonomyRecord.isConceptActive(conceptSequence, stampCoordinate);
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   @Override
   public UUID getDataStoreId() {
      return this.bdb.getDataStoreId();
   }

   @Override
   public Path getDatabaseFolder() {
      return this.bdb.getDatabaseFolder();
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return this.bdb.getDatabaseValidityStatus();
   }

   @Override
   public UUID getListenerUuid() {
      return listenerUUID;
   }

   @Override
   public Task<TaxonomySnapshotService> getSnapshot(ManifoldCoordinate tc) {
      Task<Tree>                    treeTask        = getTaxonomyTree(tc);
      Task<TaxonomySnapshotService> getSnapshotTask = new TaskWrapper<>(
                                                          treeTask,
                                                                (t) -> {
               return new TaxonomySnapshotProvider(tc, t);
            },
                                                                "Generating taxonomy snapshot");

      Get.executor()
         .execute(getSnapshotTask);
      return getSnapshotTask;
   }

   @Override
   public IntStream getTaxonomyChildSequences(int parentId) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getTaxonomyParentSequences(int childId) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   public Task<Tree> getTaxonomyTree(ManifoldCoordinate tc) {
      // TODO determine if the returned tree is thread safe for multiple accesses in parallel, if not, may need a pool of these.
      final Task<Tree> treeTask = this.snapshotCache.get(tc.hashCode());

      if (treeTask != null) {
         return treeTask;
      }

      TreeBuilderTask treeBuilderTask = new TreeBuilderTask(origin_DestinationTaxonomyRecord_Map, tc, stampedLock);

//    Task<Tree>      previousTask    = this.snapshotCache.putIfAbsent(tc.hashCode(), treeBuilderTask);
//
//    if (previousTask != null) {
//       return previousTask;
//    }
      Get.executor()
         .execute(treeBuilderTask);
      return treeBuilderTask;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class TaxonomySnapshotProvider.
    */
   private class TaxonomySnapshotProvider
            implements TaxonomySnapshotService {
      /**
       * The tc.
       */
      final ManifoldCoordinate tc;
      final Tree               treeSnapshot;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new taxonomy snapshot provider.
       *
       * @param tc the tc
       */
      public TaxonomySnapshotProvider(ManifoldCoordinate tc, Tree treeSnapshot) {
         this.tc           = tc;
         this.treeSnapshot = treeSnapshot;
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Checks if child of.
       *
       * @param childId the child id
       * @param parentId the parent id
       * @return true, if child of
       */
      @Override
      public boolean isChildOf(int childId, int parentId) {
         return this.treeSnapshot.isChildOf(childId, parentId);
      }

      /**
       * Checks if kind of.
       *
       * @param childId the child id
       * @param parentId the parent id
       * @return true, if kind of
       */
      @Override
      public boolean isKindOf(int childId, int parentId) {
         return this.treeSnapshot.isDescendentOf(childId, parentId);
      }

      /**
       * Gets the kind of sequence set.
       *
       * @param rootId the root id
       * @return the kind of sequence set
       */
      @Override
      public ConceptSequenceSet getKindOfSequenceSet(int rootId) {
         ConceptSequenceSet kindOfSet = this.treeSnapshot.getDescendentSequenceSet(rootId);

         kindOfSet.add(rootId);
         return kindOfSet;
      }

      @Override
      public ManifoldCoordinate getManifoldCoordinate() {
         return this.tc;
      }

      /**
       * Gets the roots.
       *
       * @return the roots
       */
      @Override
      public int[] getRoots() {
         return treeSnapshot.getRootSequences();
      }

      /**
       * Gets the taxonomy child sequences.
       *
       * @param parentId the parent id
       * @return the taxonomy child sequences
       */
      @Override
      public int[] getTaxonomyChildSequences(int parentId) {
         return this.treeSnapshot.getChildrenSequenceStream(parentId)
                                 .toArray();
      }

      /**
       * Gets the taxonomy parent sequences.
       *
       * @param childId the child id
       * @return the taxonomy parent sequences
       */
      @Override
      public int[] getTaxonomyParentSequences(int childId) {
         return this.treeSnapshot.getParentSequences(childId);
      }

      /**
       * Gets the taxonomy tree.
       *
       * @return the taxonomy tree
       */
      @Override
      public Tree getTaxonomyTree() {
         return this.treeSnapshot;
      }
   }
}

