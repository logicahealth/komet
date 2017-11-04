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
import java.util.function.Supplier;
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
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DiskOrderedCursor;
import com.sleepycat.je.DiskOrderedCursorConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import sh.isaac.api.ConceptActiveService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.model.collections.SpinedIntObjectMap;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.task.TaskWrapper;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.provider.bdb.chronology.BdbProvider;
import sh.isaac.provider.bdb.chronology.ChronologyUpdate;
import sh.isaac.provider.bdb.binding.IntArrayBinding;
import sh.isaac.provider.bdb.identifier.BdbIdentifierProvider;

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
   private final ConcurrentSkipListSet<Integer> semanticNidsForUnhandledChanges = new ConcurrentSkipListSet<>();

   /**
    * The tree cache.
    */
   private final ConcurrentHashMap<Integer, Task<Tree>> snapshotCache = new ConcurrentHashMap<>(5);
   private final ConcurrentHashMap<Integer, SpinedIntObjectMap<int[]>> conceptAssemblageNid_originDestinationTaxonomyRecordMap_map =
      new ConcurrentHashMap<>();
   private final UUID listenerUUID = UUID.randomUUID();

   /**
    * The identifier service.
    */
   private BdbIdentifierProvider identifierService;
   private BdbProvider           bdb;
   private int                   inferredAssemblageNid;
   private int                   isaNid;
   private int                   roleGroupNid;

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
         this.semanticNidsForUnhandledChanges.add(sc.getNid());
      }
   }

   @Override
   public void handleCommit(CommitRecord commitRecord) {
      // If a logic graph changed, clear our cache.
      if (this.semanticNidsForUnhandledChanges.size() > 0) {
         this.snapshotCache.clear();
      }

      UpdateTaxonomyAfterCommitTask.get(this, commitRecord, this.semanticNidsForUnhandledChanges, this.stampedLock);
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

   private SpinedIntObjectMap<int[]> loadFromDatabase(int assemblageKey)
            throws DatabaseException {
      IntArrayBinding           binding                              = new IntArrayBinding();
      DiskOrderedCursorConfig   docc                                 = new DiskOrderedCursorConfig();
      DatabaseEntry             foundKey                             = new DatabaseEntry();
      DatabaseEntry             foundData                            = new DatabaseEntry();
      SpinedIntObjectMap<int[]> origin_DestinationTaxonomyRecord_Map = new SpinedIntObjectMap<>();

      origin_DestinationTaxonomyRecord_Map.setElementStringConverter(
          (int[] records) -> {
             return new TaxonomyRecord(records).toString();
          });

      Database database = bdb.getTaxonomyDatabase(assemblageKey);

      try (DiskOrderedCursor cursor = database.openCursor(docc)) {
         while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            origin_DestinationTaxonomyRecord_Map.put(
                IntegerBinding.entryToInt(foundKey),
                binding.entryToObject(foundData));
         }
      }

      LOG.info("Taxonomy count at open for " + database.getDatabaseName() + " is " + database.count());
      return origin_DestinationTaxonomyRecord_Map;
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting BdbTaxonomyProvider post-construct");
         this.inferredAssemblageNid = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid();
         this.isaNid                = TermAux.IS_A.getNid();
         this.roleGroupNid          = TermAux.ROLE_GROUP.getNid();
         this.bdb                   = Get.service(BdbProvider.class);
         Get.commitService()
            .addChangeListener(this);
         this.identifierService = Get.service(BdbIdentifierProvider.class);
         
         for (int assemblageNid: bdb.getAssemblageNids()) {
            SpinedIntObjectMap<int[]> map = loadFromDatabase(assemblageNid);
            conceptAssemblageNid_originDestinationTaxonomyRecordMap_map.put(assemblageNid, map);
         }
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
      ConcurrentHashMap.KeySetView<Integer, SpinedIntObjectMap<int[]>> keys =
         conceptAssemblageNid_originDestinationTaxonomyRecordMap_map.keySet();

      for (int conceptAssemblageKey: keys) {
         SpinedIntObjectMap<int[]> map = conceptAssemblageNid_originDestinationTaxonomyRecordMap_map.get(
                                             conceptAssemblageKey);
         Database database = bdb.getTaxonomyDatabase(conceptAssemblageKey);

         map.forEach(
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
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------

   @Override
   public IntStream getAllRelationshipOriginNidsOfType(int destinationId, IntSet typeSequenceSet) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean isConceptActive(int conceptNid, StampCoordinate stampCoordinate) {
      long stamp = this.stampedLock.tryOptimisticRead();
      int  assemblageNid = identifierService.getAssemblageNidForNid(conceptNid);
      int  conceptSequence = identifierService.getElementSequenceForNid(conceptNid, assemblageNid);
      SpinedIntObjectMap<int[]> origin_DestinationTaxonomyRecord_Map =
         conceptAssemblageNid_originDestinationTaxonomyRecordMap_map.get(
             assemblageNid);
      int[] taxonomyData = origin_DestinationTaxonomyRecord_Map.get(conceptSequence);

      if (taxonomyData == null) {
         return false;
      }

      TaxonomyRecordPrimitive taxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);

      if (this.stampedLock.validate(stamp)) {
         return taxonomyRecord.isConceptActive(conceptNid, stampCoordinate);
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyData   = origin_DestinationTaxonomyRecord_Map.get(conceptSequence);
         taxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);
         return taxonomyRecord.isConceptActive(conceptNid, stampCoordinate);
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

   public SpinedIntObjectMap<int[]> getTaxonomyRecordMap(int conceptAssemblageNid) {
      return conceptAssemblageNid_originDestinationTaxonomyRecordMap_map.computeIfAbsent(
          conceptAssemblageNid,
          (key) -> new SpinedIntObjectMap<>());
   }

   public Task<Tree> getTaxonomyTree(ManifoldCoordinate tc) {
      // TODO determine if the returned tree is thread safe for multiple accesses in parallel, if not, may need a pool of these.
      final Task<Tree> treeTask = this.snapshotCache.get(tc.hashCode());

      if (treeTask != null) {
         return treeTask;
      }

      SpinedIntObjectMap<int[]> origin_DestinationTaxonomyRecord_Map =
         conceptAssemblageNid_originDestinationTaxonomyRecordMap_map.get(
             tc.getLogicCoordinate()
               .getConceptAssemblageNid());
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

   public SpinedIntObjectMap<int[]> getOrigin_DestinationTaxonomyRecord_Map(int conceptAssemblageNid) {
      return conceptAssemblageNid_originDestinationTaxonomyRecordMap_map.get(conceptAssemblageNid);
   }
   @Override
   public Supplier<TreeNodeVisitData> getTreeNodeVisitDataSupplier(int conceptAssemblageNid) {
      SpinedIntIntMap sequenceInAssemblage_nid_map = identifierService.getElementSequenceToNidMap(conceptAssemblageNid);
      SpinedNidIntMap nid_sequenceInAssemblage_map = identifierService.getNid_ElementSequence_Map();

      return () -> new TreeNodeVisitDataBdbImpl(
          (int) sequenceInAssemblage_nid_map.valueStream().count(),
          conceptAssemblageNid,
          nid_sequenceInAssemblage_map,
          sequenceInAssemblage_nid_map);
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

      //~--- getValueSpliterator methods ------------------------------------------------------

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
      public NidSet getKindOfSequenceSet(int rootId) {
         NidSet kindOfSet = this.treeSnapshot.getDescendentNidSet(rootId);

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
         return treeSnapshot.getRootNids();
      }

      /**
       * Gets the taxonomy child sequences.
       *
       * @param parentId the parent id
       * @return the taxonomy child sequences
       */
      @Override
      public int[] getTaxonomyChildNids(int parentId) {
         return this.treeSnapshot.getChildNidStream(parentId)
                                 .toArray();
      }

      /**
       * Gets the taxonomy parent sequences.
       *
       * @param childId the child id
       * @return the taxonomy parent sequences
       */
      @Override
      public int[] getTaxonomyParentNids(int childId) {
         return this.treeSnapshot.getParentNids(childId);
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

