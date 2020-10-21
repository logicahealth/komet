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
package sh.isaac.provider.datastore.taxonomy;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import sh.isaac.api.ConceptActiveService;
import sh.isaac.api.Edge;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.RefreshListener;
import sh.isaac.api.Status;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.navigation.NavigationRecord;
import sh.isaac.api.navigation.NavigationService;
import sh.isaac.api.navigation.Navigator;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.isaac.api.task.TaskCountManager;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.tree.EdgeImpl;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.ModelGet;
import sh.isaac.model.TaxonomyDebugService;
import sh.isaac.model.taxonomy.TaxonomyRecord;
import sh.isaac.model.taxonomy.TaxonomyRecordPrimitive;
import sh.isaac.provider.datastore.chronology.ChronologyUpdate;
import sh.isaac.provider.datastore.navigator.NavigationAmalgam;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L4)
public class TaxonomyProvider
        implements TaxonomyDebugService, ConceptActiveService, ChronologyChangeListener, NavigationService {

    private static final Logger LOG = LogManager.getLogger();

    private final TaskCountManager taskCountManager = Get.taskCountManager();

    private final ConcurrentSkipListSet<Integer> semanticNidsForUnhandledChanges = new ConcurrentSkipListSet<>();
    private final Set<Task<?>> pendingUpdateTasks = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<SnapshotCacheKey, Task<Tree>> snapshotCache = new ConcurrentHashMap<>(5);
    private final ConcurrentHashMap<SnapshotCacheKey, TaxonomySnapshot> noTreeSnapshotCache = new ConcurrentHashMap<>(50);  //These have a low footprint, can cache many
    private final NidSet isANidSet = new NidSet();  //init in startup
    private final NidSet childOfTypeNidSet = new NidSet();  //init in startup
    private final UUID listenerUUID = UUID.randomUUID();

    ConcurrentSkipListSet<WeakReference<RefreshListener>> refreshListeners = new ConcurrentSkipListSet<>();

    private IdentifierService identifierService;
    private DataStore store;

    public TaxonomyProvider() {
    }

    @Override
    public void addTaxonomyRefreshListener(RefreshListener refreshListener) {
        refreshListeners.add(new WeakReferenceRefreshListener(refreshListener));
    }

    @Override
    public NavigationRecord getNavigationRecord(int conceptNid) {
        return getTaxonomyRecord(conceptNid).getTaxonomyRecordUnpacked();
    }

    @Override
    public String describeTaxonomyRecord(int nid) {
        return getTaxonomyRecord(nid).toString();
    }

    public Set<Task<?>> getPendingUpdateTasks() {
        return pendingUpdateTasks;
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
            LOG.debug("Clearing snapshot cache due to commit");
            this.snapshotCache.clear();
            this.noTreeSnapshotCache.clear();
        }

        try {
        this.taskCountManager.acquire();
        UpdateTaxonomyAfterCommitTask updateTask
                = UpdateTaxonomyAfterCommitTask.get(this, commitRecord, this.semanticNidsForUnhandledChanges, this.taskCountManager);
             //wait for completion
            updateTask.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Unexpected error waiting for taxonomy update after commit", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyTaxonomyListenersToRefresh() {
        LOG.debug("Clearing snapshot cache due notify request");
        snapshotCache.clear();
        this.noTreeSnapshotCache.clear();
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
        return Get.executor().submit(() -> {
            for (Task<?> updateTask : pendingUpdateTasks) {
                try {
                    LOG.debug("Waiting for completion of: {} ", updateTask);
                    updateTask.get();
                    LOG.debug("Completed: {}", updateTask);
                    Platform.runLater(() -> LOG.info("Completed: " + updateTask.getTitle()));
                } catch (Throwable ex) {
                    LOG.error(ex);
                }
            }
            // Not our job to sync the data store
            return null;
        });
    }

    @Override
    public void updateStatus(ConceptChronology conceptChronology) {
        ChronologyUpdate.handleStatusUpdate(conceptChronology);
    }

    @Override
    public void updateTaxonomy(SemanticChronology logicGraphChronology) {
        LOG.trace("Updating taxonomy for commit to {}", () -> logicGraphChronology.toString());
        try {
            ChronologyUpdate.handleTaxonomyUpdate(logicGraphChronology);
        } catch (Throwable e) {
            LOG.error("error processing taxonomy update", e);
            throw e;
        }
    }

    /**
     * Start me.
     */
    @PostConstruct
    private void startMe() {
        LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting chronology provider");
        Get.executor().execute(progressTask);
        try {
            LOG.info("Starting TaxonomyProvider post-construct");
            this.store = Get.service(DataStore.class);
            Get.commitService()
                    .addChangeListener(this);
            this.identifierService = Get.identifierService();
            this.semanticNidsForUnhandledChanges.clear();
            this.pendingUpdateTasks.clear();
            this.snapshotCache.clear();
            this.noTreeSnapshotCache.clear();
            this.refreshListeners.clear();
            this.isANidSet.clear();
            this.isANidSet.add(TermAux.IS_A.getNid());
            this.childOfTypeNidSet.clear();
            this.childOfTypeNidSet.add(TermAux.CHILD_OF.getNid());
        } catch (final Exception e) {
            LookupService.getService(SystemStatusService.class)
                    .notifyServiceConfigurationFailure("Taxonomy Provider", e);
            throw new RuntimeException(e);
        } finally {
            progressTask.finished();
        }
    }

    /**
     * Stop me.
     */
    @PreDestroy
    private void stopMe() {
        LOG.info("Stopping TaxonomyProvider");
        try {
            StopMeTask stopMeTask = new StopMeTask();
            stopMeTask.call();
        } catch (Exception ex) {
            LOG.error("Exception during service stop. ", ex);
        }
        LOG.info("Stopped TaxonomyProvider");
    }
    private class StopMeTask extends TimedTaskWithProgressTracker {

        public StopMeTask() {
            updateTitle("Stopping taxonomy provider");
            addToTotalWork(4);
            Get.activeTasks().add(this);
        }

        @Override
        protected Object call() throws Exception {
            try {
                // ensure all pending operations have completed.
                updateMessage("Waiting for pending taxonomy updates");
                for (Task<?> updateTask : TaxonomyProvider.this.pendingUpdateTasks) {
                    updateTask.get();
                }
                completedUnitOfWork();
                updateMessage("Waiting for taxonomy sync");
                TaxonomyProvider.this.sync().get();
                completedUnitOfWork();
                // make sure updates are done prior to allowing other services to stop.
                updateMessage("Waiting for task count manager");
                TaxonomyProvider.this.taskCountManager.waitForCompletion();
                completedUnitOfWork();
                updateMessage("Clearing cached data");
                TaxonomyProvider.this.semanticNidsForUnhandledChanges.clear();
                TaxonomyProvider.this.pendingUpdateTasks.clear();
                TaxonomyProvider.this.snapshotCache.clear();
                TaxonomyProvider.this.noTreeSnapshotCache.clear();
                TaxonomyProvider.this.refreshListeners.clear();
                TaxonomyProvider.this.identifierService = null;
                TaxonomyProvider.this.store = null;
                TaxonomyProvider.this.isANidSet.clear();
                TaxonomyProvider.this.childOfTypeNidSet.clear();
                Get.commitService().removeChangeListener(TaxonomyProvider.this);
                completedUnitOfWork();
                return null;
            } finally {
                Get.activeTasks().remove(this);
            }
        }
    }

    @Override
    public IntStream getAllRelationshipOriginNidsOfType(int destinationId, IntSet typeSequenceSet) {
        throw new UnsupportedOperationException(
                "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isConceptActive(int conceptNid, StampFilterImmutable stampFilter) {
        int assemblageNid = identifierService.getAssemblageNid(conceptNid).getAsInt();
        int[] taxonomyData = store.getTaxonomyData(assemblageNid, conceptNid);

        if (taxonomyData == null) {
            return false;
        }

        TaxonomyRecordPrimitive taxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);

        try {
            return taxonomyRecord.isConceptActive(conceptNid, stampFilter.getRelativePositionCalculator());
        } catch (NoSuchElementException ex) {
            StringBuilder builder = new StringBuilder();
            builder.append("Error determining if concept is active.");
            builder.append(Get.conceptSpecification(conceptNid));
            LOG.error(builder.toString(), ex);
            return false;
        }
    }

    @Override
    public EnumSet<Status> getConceptStates(int conceptNid, StampFilterImmutable stampFilter) {
        int assemblageNid = identifierService.getAssemblageNid(conceptNid).getAsInt();
        int[] taxonomyData = store.getTaxonomyData(assemblageNid, conceptNid);

        if (taxonomyData == null) {
            return EnumSet.noneOf(Status.class);
        }

        TaxonomyRecordPrimitive taxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);

        return taxonomyRecord.getConceptStates(conceptNid, stampFilter);
    }

    @Override
    public Optional<UUID> getDataStoreId() {
        return this.store.getDataStoreId();
    }

    @Override
    public Path getDataStorePath() {
        return this.store.getDataStorePath();
    }

    @Override
    public DataStoreStartState getDataStoreStartState() {
        return this.store.getDataStoreStartState();
    }

    @Override
    public UUID getListenerUuid() {
        return listenerUUID;
    }

    @Override
    public TaxonomySnapshot getStatedLatestSnapshot(int pathNid, Set<ConceptSpecification> modules, Set<Status> allowedStates, boolean computeTree) {

        ManifoldCoordinate statedManifold = ManifoldCoordinateImmutable.makeStated(StampFilterImmutable.make(StatusSet.of(allowedStates), pathNid, modules), null);

        return computeTree ?
                getSnapshot(statedManifold) :
                getSnapshotNoTree(statedManifold);
    }

    @Override
    public TaxonomySnapshot getSnapshot(ManifoldCoordinate mc) {
        Task<Tree> treeTask = getTaxonomyTree(mc);
        return new TaxonomySnapshotProvider(mc, treeTask);
    }
    

    @Override
    public TaxonomySnapshot getSnapshotNoTree(ManifoldCoordinate mc) {
        //The TaxonomySnapshotNoTree does keep a cache of child to parent items, so we cache the entire structure as well, per 
        //manifold coordinate
        return noTreeSnapshotCache.computeIfAbsent(new SnapshotCacheKey(mc), (key) -> new TaxonomySnapshotNoTree(mc));
    }

    private TaxonomyRecordPrimitive getTaxonomyRecord(int nid) {
        int conceptAssemblageNid = ModelGet.identifierService()
                .getAssemblageNid(nid).getAsInt();
        int[] record = store.getTaxonomyData(conceptAssemblageNid, nid);

        return new TaxonomyRecordPrimitive(record);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getTaxonomyData(int assemblageNid, int conceptNid) {
       return store.getTaxonomyData(assemblageNid, conceptNid);
    }
     /**
     * {@inheritDoc}
     */
    @Override
    public int[] accumulateAndGetTaxonomyData(int assemblageNid, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction) {
       return store.accumulateAndGetTaxonomyData(assemblageNid, conceptNid, newData, accumulatorFunction);
    }

    private class SnapshotCacheKey {

        final PremiseSet taxPremiseTypes;
        final UUID manifoldCoordinateUuid;
        final int customSortHash;

        public SnapshotCacheKey(ManifoldCoordinate mc) {
            this.taxPremiseTypes = mc.getPremiseTypes();
            this.manifoldCoordinateUuid = mc.getManifoldCoordinateUuid();
            this.customSortHash = mc.getVertexSort().hashCode();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.taxPremiseTypes);
            hash = 29 * hash + this.manifoldCoordinateUuid.hashCode();
            hash = 29 * hash + customSortHash;
            return hash;
        }

        @Override
        public String toString() {
            return "SnapshotCacheKey{" +
                    "taxPremiseType=" + taxPremiseTypes +
                    ", stampCoordinate=" + manifoldCoordinateUuid +
                    ", customSortHash=" + customSortHash +

                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SnapshotCacheKey other = (SnapshotCacheKey) obj;
            if (!this.taxPremiseTypes.equals(other.taxPremiseTypes)) {
                return false;
            }
            if (!Objects.equals(this.manifoldCoordinateUuid, other.manifoldCoordinateUuid)) {
                return false;
            }
            if (this.customSortHash != other.customSortHash) {
                return false;
            }
            return true;
        }
    }

    public Task<Tree> getTaxonomyTree(ManifoldCoordinate mc) {
        SnapshotCacheKey snapshotCacheKey = new SnapshotCacheKey(mc);
        final Task<Tree> treeTask = this.snapshotCache.get(snapshotCacheKey);

        if (treeTask != null) {
            return treeTask;
        }

        LOG.debug("Building tree for {}, cache key {}", () -> mc.getClass().getName() + "@" + Integer.toHexString(mc.hashCode()), () -> snapshotCacheKey.hashCode());
        IntFunction<int[]> taxonomyDataProvider = new IntFunction<int[]>() {
            final int assemblageNid = mc.getLogicCoordinate().getConceptAssemblageNid();
            @Override
            public int[] apply(int conceptNid) {
                try {
                    return store.getTaxonomyData(assemblageNid, conceptNid);
                } catch (IllegalStateException ex) {
                    LOG.error(ex.getLocalizedMessage() + " retrieving " + Get.conceptDescriptionText(conceptNid), ex);
                    return new int[0];
                }
            }
        };
        
        TreeBuilderTask treeBuilderTask = new TreeBuilderTask(taxonomyDataProvider, mc);

        Task<Tree> previousTask = this.snapshotCache.putIfAbsent(snapshotCacheKey, treeBuilderTask);

        if (previousTask != null) {
            Get.activeTasks().remove(treeBuilderTask);
            return previousTask;
        }

        LOG.debug("Executing: " + snapshotCacheKey + " -- cache count is: " + this.snapshotCache.size());
        Get.executor().execute(treeBuilderTask);

        return treeBuilderTask;
    }

    @Override
    public Supplier<TreeNodeVisitData> getTreeNodeVisitDataSupplier(int conceptAssemblageNid) {
        return () -> new TreeNodeVisitDataImpl(conceptAssemblageNid);
    }
    
    @Override
    public boolean wasEverKindOf(int childNid, int parentNid) {
        if (wasEverChildOf(childNid, parentNid)) {
            return true;
        }

        for (int directParentNid : getTaxonomyRecord(childNid).getTaxonomyRecordUnpacked().getDestinationConceptNidsOfType(isANidSet).toArray()) {
            if (directParentNid == childNid) {
                TaxonomyRecord record = getTaxonomyRecord(childNid).getTaxonomyRecordUnpacked();
                if (record.containsConceptNidViaType(directParentNid, NidSet.of(TermAux.IS_A),
                        new int[]{ TaxonomyFlag.INFERRED.bits,  TaxonomyFlag.STATED.bits })) {
                    LOG.warn(Get.conceptDescriptionText(childNid) + " has a taxonomy isA record that points to itself");
                }

                continue;
            }
            NidSet nidSet = new NidSet();
            nidSet.add(childNid);
            if (wasEverKindOf(directParentNid, parentNid, nidSet)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * recursive portion of wasEverKindOf, with a hashset of already examined nodes to prevent following a cycle.
     * This was previously implemented with a depth counter, but that is not effective, as when you look at all rels
     * over the full history of a terminology, without taking state into account, its easy to get cycles.
     * @param childNid
     * @param parentNid
     * @return
     */
    private boolean wasEverKindOf(int childNid, int parentNid, NidSet visited) {
        if (wasEverChildOf(childNid, parentNid)) {
            return true;
        }
        
        //This really shouldn't happen, but leaving it here as a sanity check to prevent infinite loops.
        if (visited.size() > 500) {
            LOG.error("Visited more than 500 nodes on path to parent - current child: " + Get.conceptDescriptionText(childNid) + " parent: " + Get.conceptDescriptionText(parentNid));
            LOG.error("Return false secondary to presumed bad data or implementation error. ");
            return false;
        }
        
        visited.add(childNid);

        for (int directParentNid : getTaxonomyRecord(childNid).getTaxonomyRecordUnpacked().getDestinationConceptNidsOfType(isANidSet).toArray()) {
            if (directParentNid == childNid) {
                LOG.warn(Get.conceptDescriptionText(childNid) + " has a taxonomy isA record that points to itself");
                continue;
            }
            if (visited.contains(directParentNid)) {
                continue;
            }
            if (wasEverKindOf(directParentNid, parentNid, visited)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean wasEverChildOf(int childNid, int parentNid) {
        for (int directParentNid : getTaxonomyRecord(childNid).getTaxonomyRecordUnpacked().getDestinationConceptNidsOfType(isANidSet).toArray()) {
            if (directParentNid == parentNid) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] getAllTaxonomyChildren(int parentNid) {
        return getTaxonomyRecord(parentNid).getTaxonomyRecordUnpacked().getDestinationConceptNidsOfType(childOfTypeNidSet).toArray();
    }

   /**
     * The Class TaxonomySnapshotProvider.
     */
    private class TaxonomySnapshotProvider
            implements TaxonomySnapshot {

        final ManifoldCoordinate manifoldCoordinate;
        Tree treeSnapshot;
        final Task<Tree> treeTask;

        public TaxonomySnapshotProvider(ManifoldCoordinate manifoldCoordinate, Task<Tree> treeTask) {
            this.manifoldCoordinate = manifoldCoordinate;
            this.treeTask = treeTask;

            if (!treeTask.isDone()) {
                if (Platform.isFxApplicationThread()) {
                    this.treeTask.stateProperty()
                            .addListener(this::succeeded);
                } else {
                    Platform.runLater(
                            () -> {
                                Task<Tree> theTask = treeTask;

                                if (!theTask.isDone()) {
                                    theTask.stateProperty()
                                            .addListener(this::succeeded);
                                } else {
                                    try {
                                        this.treeSnapshot = treeTask.get();
                                    } catch (InterruptedException | ExecutionException ex) {
                                        if (ex.getCause() instanceof CancellationException) {
                                            LOG.info(theTask.getTitle() + " canceled.");
                                        } else {
                                            LOG.error("Unexpected error constructing taxonomy snapshot provider", ex);
                                        }
                                    }
                                }

                            });
                }
            }

            if (treeTask.isDone()) {
                try {
                    this.treeSnapshot = treeTask.get();
                } catch (InterruptedException | ExecutionException ex) {
                    LOG.error("Unexpected error constructing taxonomy snapshot provider", ex);
                    throw new RuntimeException(ex);
                }
            }
        }

        @Override
        public ImmutableCollection<Edge> getTaxonomyParentLinks(int parentConceptNid) {
            int[] parentNids = getTaxonomyParentConceptNids(parentConceptNid);
            MutableList<Edge> links = Lists.mutable.ofInitialCapacity(parentNids.length);
            for (int parentNid: parentNids) {
                links.add(new EdgeImpl(TermAux.IS_A.getNid(), parentNid));
            }
            return links.toImmutable();
        }

        @Override
        public ImmutableCollection<Edge> getTaxonomyChildLinks(int childConceptNid) {
            int[] childNids = getTaxonomyChildConceptNids(childConceptNid);
            MutableList<Edge> links = Lists.mutable.ofInitialCapacity(childNids.length);
            for (int childNid: childNids) {
                links.add(new EdgeImpl(TermAux.IS_A.getNid(), childNid));
            }
            return links.toImmutable();
        }

        private void succeeded(ObservableValue<? extends State> observable, State oldValue, State newValue) {
            try {
                switch (newValue) {
                    case SUCCEEDED: {
                        this.treeSnapshot = treeTask.get();
                        break;
                    }
                    default :
                        //noop
                        break;
                }
            } catch (InterruptedException | ExecutionException ex) {
                LOG.error("Unexpected error in succeeded call", ex);
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean isChildOf(int childId, int parentId) {
            if (treeSnapshot != null) {
                return this.treeSnapshot.isChildOf(childId, parentId);
            }
            
            //filter out destinations that don't match the coordinate
            if (Get.conceptService().getConceptChronology(childId).getLatestVersion(manifoldCoordinate.getVertexStampFilter()).isAbsent()) {
                return false;
            }
            TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(childId);

            return taxonomyRecordPrimitive.containsNidViaType(parentId, TermAux.IS_A.getNid(), manifoldCoordinate);
        }

        @Override
        public boolean isKindOf(int childId, int kindofNid) {
            if (childId == kindofNid) {
                return true;
            }
            if (treeSnapshot != null) {
                return this.treeSnapshot.isDescendentOf(childId, kindofNid);
            }

            if (isChildOf(childId, kindofNid)) {
                return true;
            }

            for (int parentNid : getTaxonomyParentConceptNids(childId)) {
                if (isKindOf(parentNid, kindofNid, 0)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isDescendentOf(int descendantConceptNid, int ancestorConceptNid) {
            if (descendantConceptNid != ancestorConceptNid) {
                return isKindOf(descendantConceptNid, ancestorConceptNid);
            }
            return false;
        }


        private boolean isKindOf(int childId, int kindofNid, int depth) {
            if (depth > 40) {
                LOG.warn("Taxonomy depth > 40: " + depth + "; \n" + Get.conceptDescriptionText(childId) + " <? \n" + Get.conceptDescriptionText(kindofNid));
            }
            if (depth > 60) {
                LOG.error("Taxonomy depth > 60" + Get.conceptDescriptionText(childId) + " <? " + Get.conceptDescriptionText(kindofNid));
                LOG.error("Return false secondary to presumed cycle. ");
                // TODO raise alert to user via alert mechanism. 
                return false;
            }
            if (isChildOf(childId, kindofNid)) {
                return true;
            }

            for (int parentNid : getTaxonomyParentConceptNids(childId)) {
                if (isKindOf(parentNid, kindofNid, depth + 1)) {
                    return true;
                }
            }

            return false;
        }


        @Override
        public ImmutableIntSet getKindOfConcept(int rootId) {
            MutableIntSet kindNids = IntSets.mutable.empty();
            if (treeSnapshot != null) {
                kindNids.addAll(this.treeSnapshot.getDescendentNids(rootId));

                kindNids.add(rootId);
                return kindNids.toImmutable();
            }

            int[] childNids = getTaxonomyChildConceptNids(rootId);
            kindNids.addAll(getTaxonomyChildConceptNids(rootId));

            for (int childNid : childNids) {
                kindNids.addAll(getKindOfConcept(childNid));
            }

            return kindNids.toImmutable();
        }

        @Override
        public ManifoldCoordinate getManifoldCoordinate() {
            return this.manifoldCoordinate;
        }

        @Override
        public int[] getRootNids() {
            if (treeSnapshot != null) {
                return treeSnapshot.getRootNids();
            }

            return new int[]{TermAux.SOLOR_ROOT.getNid()};
        }

        @Override
        public int[] getTaxonomyChildConceptNids(int parentId) {
            if (treeSnapshot != null) {
                return this.treeSnapshot.getTaxonomyChildConceptNids(parentId);
            }

            TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(parentId);

            return taxonomyRecordPrimitive.getDestinationNidsOfType(childOfTypeNidSet, manifoldCoordinate);
        }

        @Override
        public boolean isLeaf(int conceptNid) {
            if (treeSnapshot != null) {
                return this.treeSnapshot.getTaxonomyChildConceptNids(conceptNid).length == 0;
            }
            TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(conceptNid);
            return !taxonomyRecordPrimitive.hasDestinationNidsOfType(childOfTypeNidSet, manifoldCoordinate);
        }

        @Override
        public int[] getTaxonomyParentConceptNids(int childId) {
            if (treeSnapshot != null) {
                return this.treeSnapshot.getTaxonomyParentConceptNids(childId);
            }

            TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(childId);

            return taxonomyRecordPrimitive.getDestinationNidsOfType(isANidSet, manifoldCoordinate);
        }

        @Override
        public Tree getTaxonomyTree() {
            try {
                if (treeSnapshot != null) {
                    return this.treeSnapshot;
                }

                return treeTask.get();
            } catch (InterruptedException | ExecutionException ex) {
                LOG.error("Unexpected error constructing taxonomy snapshot provider", ex);
                throw new RuntimeException(ex);
            }
        }
    }
    
    //An alternate implementation that doesn't compute a tree in the background....
    //TODO merge the code above with this somehow, maybe so the above code falls back to this code when the tree isn't available, rather than
    // copy and paste inheritance.
    private class TaxonomySnapshotNoTree implements TaxonomySnapshot {
        //These caches are for specific performance issues in some rest API usage patterns.  These help make up for not having a fully computed tree.
        ConcurrentHashMap<String, Boolean> childOfCache = new ConcurrentHashMap<>(250);
        ConcurrentHashMap<Integer, int[]> parentsCache = new ConcurrentHashMap<>(250);
        ConcurrentHashMap<Integer, int[]> childrenCache = new ConcurrentHashMap<>(250);

        final ManifoldCoordinate mc;

        public TaxonomySnapshotNoTree(ManifoldCoordinate mc) {
            LOG.debug("Building a new non-tree taxonomy snapshot for {}", mc);
            this.mc = mc;
        }

        @Override
        public boolean isChildOf(int childId, int parentId) {
            return childOfCache.computeIfAbsent(childId + ":" + parentId, (key) -> 
            {
                //TODO [KEITH] shouldn't IS_A come from manifold coord?
                TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(childId);
                return taxonomyRecordPrimitive.containsNidViaType(parentId, TermAux.IS_A.getNid(), mc);
            });
        }

        @Override
        public boolean isKindOf(int childId, int kindofNid) {
            if (isChildOf(childId, kindofNid)) {
                return true;
            }

            for (int parentNid : getTaxonomyParentConceptNids(childId)) {
                if (isKindOf(parentNid, kindofNid, 0)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isDescendentOf(int descendantConceptNid, int ancestorConceptNid) {
            if (descendantConceptNid != ancestorConceptNid) {
                return isKindOf(descendantConceptNid, ancestorConceptNid);
            }
            return false;
        }

        private boolean isKindOf(int childId, int kindofNid, int depth) {
            if (depth > 40) {
                LOG.warn("Taxonomy depth > 40: " + depth + "; \n" + Get.conceptDescriptionText(childId) + " <? \n" + Get.conceptDescriptionText(kindofNid));
            }
            if (depth > 60) {
                LOG.error("Taxonomy depth > 60" + Get.conceptDescriptionText(childId) + " <? " + Get.conceptDescriptionText(kindofNid));
                LOG.error("Return false secondary to presumed cycle. ");
                // TODO raise alert to user via alert mechanism. 
                return false;
            }
            if (isChildOf(childId, kindofNid)) {
                return true;
            }

            for (int parentNid : getTaxonomyParentConceptNids(childId)) {
                if (isKindOf(parentNid, kindofNid, depth + 1)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public ImmutableIntSet getKindOfConcept(int rootId) {
            int[] childNids = getTaxonomyChildConceptNids(rootId);
            MutableIntSet kindOfSet = IntSets.mutable.of(getTaxonomyChildConceptNids(rootId));

            for (int childNid : childNids) {
                kindOfSet.addAll(getKindOfConcept(childNid));
            }

            return kindOfSet.toImmutable();
        }

        @Override
        public ManifoldCoordinate getManifoldCoordinate() {
            return this.mc;
        }

        @Override
        public int[] getRootNids() {
            return new int[] { TermAux.SOLOR_ROOT.getNid() };
        }

        @Override
        public int[] getTaxonomyChildConceptNids(int parentId) {
            try {
                return childrenCache.computeIfAbsent(parentId, key -> {
                    TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(key);
                    return taxonomyRecordPrimitive.getDestinationNidsOfType(childOfTypeNidSet, mc);
                });
            } catch (IllegalStateException ex) {
                LOG.error(ex.getLocalizedMessage() + " retrieving " + Get.conceptDescriptionText(parentId), ex);
                return new int[0];
            }
        }

        @Override
        public boolean isLeaf(int conceptNid) {
            TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(conceptNid);
            return !taxonomyRecordPrimitive.hasDestinationNidsOfType(childOfTypeNidSet, mc);
        }

        @Override
        public int[] getTaxonomyParentConceptNids(int childId) {
            return parentsCache.computeIfAbsent(childId, childIdAgain -> {
                TaxonomyRecordPrimitive taxonomyRecordPrimitive = getTaxonomyRecord(childId);
                return taxonomyRecordPrimitive.getDestinationNidsOfType(isANidSet, mc);
            });
        }

        @Override
        public Tree getTaxonomyTree() {
            throw new UnsupportedOperationException("Need to call getSnapshot(), rather than getSnapshotNoTree()");
        }

        @Override
        public ImmutableCollection<Edge> getTaxonomyParentLinks(int parentConceptNid) {
            int[] parentNids = getTaxonomyParentConceptNids(parentConceptNid);
            MutableList<Edge> links = Lists.mutable.ofInitialCapacity(parentNids.length);
            for (int parentNid: parentNids) {
                links.add(new EdgeImpl(TermAux.IS_A.getNid(), parentNid));
            }
            return links.toImmutable();
        }
    
        @Override
        public ImmutableCollection<Edge> getTaxonomyChildLinks(int childConceptNid) {
            int[] childNids = getTaxonomyChildConceptNids(childConceptNid);
            MutableList<Edge> links = Lists.mutable.ofInitialCapacity(childNids.length);
            for (int childNid: childNids) {
                links.add(new EdgeImpl(TermAux.IS_A.getNid(), childNid));
            }
            return links.toImmutable();
        }
    }

    @Override
    public Navigator getNavigator(ManifoldCoordinate mc) {
        return new NavigationAmalgam(mc);
    }
}
