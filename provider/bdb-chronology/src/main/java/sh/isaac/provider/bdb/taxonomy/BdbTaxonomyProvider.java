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
import java.lang.ref.WeakReference;

import java.nio.file.Path;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------
import javafx.application.Platform;

import javafx.beans.value.ObservableValue;

import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;

//~--- JDK imports ------------------------------------------------------------
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.ConceptActiveService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.RefreshListener;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.ModelGet;
import sh.isaac.model.TaxonomyDebugService;
import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.provider.bdb.chronology.BdbProvider;
import sh.isaac.provider.bdb.chronology.ChronologyUpdate;
import sh.isaac.provider.bdb.identifier.BdbIdentifierProvider;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 5)
public class BdbTaxonomyProvider
        implements TaxonomyDebugService, ConceptActiveService, ChronologyChangeListener {

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();
   private static final int MAX_AVAILABLE = Runtime.getRuntime()
           .availableProcessors() * 2;

   //~--- fields --------------------------------------------------------------
   private final Semaphore updatePermits = new Semaphore(MAX_AVAILABLE);

   /**
    * The semantic nids for unhandled changes.
    */
   private final ConcurrentSkipListSet<Integer> semanticNidsForUnhandledChanges = new ConcurrentSkipListSet<>();

   /**
    * The tree cache.
    */
   private final ConcurrentHashMap<Integer, Task<Tree>> snapshotCache = new ConcurrentHashMap<>(5);
   private final UUID listenerUUID = UUID.randomUUID();

   /**
    * The change listeners.
    */
   ConcurrentSkipListSet<WeakReference<RefreshListener>> refreshListeners = new ConcurrentSkipListSet<>();

   /**
    * The identifier service.
    */
   private BdbIdentifierProvider identifierService;
   private BdbProvider bdb;
   private int inferredAssemblageNid;
   private int isaNid;
   private int roleGroupNid;

   //~--- constructors --------------------------------------------------------
   public BdbTaxonomyProvider() {
   }

   //~--- methods -------------------------------------------------------------
   @Override
   public void addTaxonomyRefreshListener(RefreshListener refreshListener) {
      refreshListeners.add(new WeakReference<>(refreshListener));
   }

   @Override
   public String describeTaxonomyRecord(int nid) {
      return getTaxonomyRecord(nid).toString();
   }

   @Override
   public void handleChange(ConceptChronology cc) {
      // not processing concept changes
      // is this call redundant/better than updateStatus(ConceptChronology conceptChronology) call/method?
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

      this.updatePermits.acquireUninterruptibly();
      UpdateTaxonomyAfterCommitTask.get(this, commitRecord, this.semanticNidsForUnhandledChanges, this.updatePermits);
   }

   @Override
   public void notifyTaxonomyListenersToRefresh() {
      snapshotCache.clear();
      Platform.runLater(
              () -> {
                 for (WeakReference<RefreshListener> listenerReference : refreshListeners) {
                    RefreshListener listener = listenerReference.get();

                    if (listener != null) {
                       listener.refresh();
                    }
                 }
              });
   }

   @Override
   public Future<?> sync() {
      return this.bdb.sync();
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

   private SpinedIntIntArrayMap loadTaxonomyFromDatabase(int assemblageKey) {
      return bdb.getTaxonomyMap(assemblageKey);
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting BdbTaxonomyProvider post-construct");
         this.inferredAssemblageNid = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid();
         this.isaNid = TermAux.IS_A.getNid();
         this.roleGroupNid = TermAux.ROLE_GROUP.getNid();
         this.bdb = Get.service(BdbProvider.class);
         Get.commitService()
                 .addChangeListener(this);
         this.identifierService = Get.service(BdbIdentifierProvider.class);
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
      LOG.info("Stopping BdbTaxonomyProvider");

      // make sure updates are done prior to allowing other services to stop.
      this.updatePermits.acquireUninterruptibly(MAX_AVAILABLE);
      LOG.info("BdbTaxonomyProvider stopped");
   }

   //~--- get methods ---------------------------------------------------------
   @Override
   public IntStream getAllRelationshipOriginNidsOfType(int destinationId, IntSet typeSequenceSet) {
      throw new UnsupportedOperationException(
              "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean isConceptActive(int conceptNid, StampCoordinate stampCoordinate) {
      int assemblageNid = identifierService.getAssemblageNidForNid(conceptNid);
      SpinedIntIntArrayMap origin_DestinationTaxonomyRecord_Map = bdb.getTaxonomyMap(assemblageNid);
      int[] taxonomyData = origin_DestinationTaxonomyRecord_Map.get(conceptNid);

      if (taxonomyData == null) {
         return false;
      }

      TaxonomyRecordPrimitive taxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);

      return taxonomyRecord.isConceptActive(conceptNid, stampCoordinate);
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

   public SpinedIntIntArrayMap getOrigin_DestinationTaxonomyRecord_Map(int conceptAssemblageNid) {
      return bdb.getTaxonomyMap(conceptAssemblageNid);
   }

   @Override
   public TaxonomySnapshotService getSnapshot(ManifoldCoordinate tc) {
      Task<Tree> treeTask = getTaxonomyTree(tc);

      return new TaxonomySnapshotProvider(tc, treeTask);
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

   private TaxonomyRecordPrimitive getTaxonomyRecord(int nid) {
      int conceptAssemblageNid = ModelGet.identifierService()
              .getAssemblageNidForNid(nid);
      SpinedIntIntArrayMap map = getTaxonomyRecordMap(conceptAssemblageNid);
      int[] record = map.get(nid);

      return new TaxonomyRecordPrimitive(record);
   }

   @Override
   public SpinedIntIntArrayMap getTaxonomyRecordMap(int conceptAssemblageNid) {
      return bdb.getTaxonomyMap(conceptAssemblageNid);
   }

   public Task<Tree> getTaxonomyTree(ManifoldCoordinate tc) {
      final Task<Tree> treeTask = this.snapshotCache.get(tc.hashCode());

      if (treeTask != null) {
         return treeTask;
      }

      SpinedIntIntArrayMap origin_DestinationTaxonomyRecord_Map = bdb.getTaxonomyMap(
              tc.getLogicCoordinate()
                      .getConceptAssemblageNid());
      TreeBuilderTask treeBuilderTask = new TreeBuilderTask(origin_DestinationTaxonomyRecord_Map, tc);
      Task<Tree> previousTask = this.snapshotCache.putIfAbsent(tc.hashCode(), treeBuilderTask);

      if (previousTask != null) {
         return previousTask;
      }

      Get.executor()
              .execute(treeBuilderTask);
      return treeBuilderTask;
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

      int isaNid = TermAux.IS_A.getNid();
      int childOfNid = TermAux.CHILD_OF.getNid();
      NidSet childOfTypeNidSet = new NidSet();
      NidSet isaTypeNidSet = new NidSet();

      /**
       * The tc.
       */
      final ManifoldCoordinate tc;
      Tree treeSnapshot;
      final Task<Tree> treeTask;

      //~--- initializers -----------------------------------------------------
      {
         isaTypeNidSet.add(isaNid);
         childOfTypeNidSet.add(childOfNid);
      }

      //~--- constructors -----------------------------------------------------
      public TaxonomySnapshotProvider(ManifoldCoordinate tc, Task<Tree> treeTask) {
         this.tc = tc;
         this.treeTask = treeTask;

         if (Platform.isFxApplicationThread()) {
            this.treeTask.stateProperty()
                    .addListener(this::succeeded);
         } else {
            Platform.runLater(
                    () -> {
                       Task<Tree> theTask = treeTask;

                       if (theTask != null) {
                          if (!theTask.isDone()) {
                             theTask.stateProperty()
                                     .addListener(this::succeeded);
                          } else {
                             try {
                                this.treeSnapshot = treeTask.get();
                             } catch (InterruptedException | ExecutionException ex) {
                                LOG.error(ex);
                             }
                          }
                       }
                    });
         }

         if (treeTask.isDone()) {
            try {
               this.treeSnapshot = treeTask.get();
            } catch (InterruptedException | ExecutionException ex) {
               LOG.error(ex);
               throw new RuntimeException(ex);
            }
         }
      }

      //~--- methods ----------------------------------------------------------
      private void succeeded(ObservableValue<? extends State> observable, State oldValue, State newValue) {
         try {
            switch (newValue) {
               case SUCCEEDED: {
                  this.treeSnapshot = treeTask.get();
               }
            }
         } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex);
            throw new RuntimeException(ex);
         }
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
         if (treeSnapshot != null) {
            return this.treeSnapshot.isChildOf(childId, parentId);
         }

         TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(childId);

         return taxonomyRecordPrimitive.containsNidViaType(parentId, isaNid, tc);
      }

      /**
       * Checks if kind of.
       *
       * @param childId the child id
       * @param kindofNid the parent id
       * @return true, if kind of
       */
      @Override
      public boolean isKindOf(int childId, int kindofNid) {
         if (treeSnapshot != null) {
            return this.treeSnapshot.isDescendentOf(childId, kindofNid);
         }

         if (isChildOf(childId, kindofNid)) {
            return true;
         }

         for (int parentNid : getTaxonomyParentNids(childId)) {
            if (isKindOf(parentNid, kindofNid)) {
               return true;
            }
         }

         return false;
      }

      /**
       * Gets the kind of sequence set.
       *
       * @param rootId the root id
       * @return the kind of sequence set
       */
      @Override
      public NidSet getKindOfSequenceSet(int rootId) {
         if (treeSnapshot != null) {
            NidSet kindOfSet = this.treeSnapshot.getDescendentNidSet(rootId);

            kindOfSet.add(rootId);
            return kindOfSet;
         }

         int[] childNids = getTaxonomyChildNids(rootId);
         NidSet kindOfSet = NidSet.of(getTaxonomyChildNids(rootId));

         for (int childNid : childNids) {
            kindOfSet.addAll(getKindOfSequenceSet(childNid));
         }

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
         if (treeSnapshot != null) {
            return treeSnapshot.getRootNids();
         }

         return new int[]{TermAux.SOLOR_ROOT.getNid()};
      }

      /**
       * Gets the taxonomy child sequences.
       *
       * @param parentId the parent id
       * @return the taxonomy child sequences
       */
      @Override
      public int[] getTaxonomyChildNids(int parentId) {
         if (treeSnapshot != null) {
            return this.treeSnapshot.getChildNids(parentId);
         }

         TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(parentId);

         return taxonomyRecordPrimitive.getDestinationNidsOfType(childOfTypeNidSet, tc);
      }

      /**
       * Gets the taxonomy parent sequences.
       *
       * @param childId the child id
       * @return the taxonomy parent sequences
       */
      @Override
      public int[] getTaxonomyParentNids(int childId) {
         if (treeSnapshot != null) {
            return this.treeSnapshot.getParentNids(childId);
         }

         TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(childId);

         return taxonomyRecordPrimitive.getDestinationNidsOfType(isaTypeNidSet, tc);
      }

      /**
       * Gets the taxonomy tree.
       *
       * @return the taxonomy tree
       */
      @Override
      public Tree getTaxonomyTree() {
         try {
            if (treeSnapshot != null) {
               return this.treeSnapshot;
            }

            return treeTask.get();
         } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex);
            throw new RuntimeException(ex);
         }
      }
   }
}
