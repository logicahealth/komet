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
package sh.isaac.provider.logic;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import sh.isaac.api.DataSource;
import sh.isaac.api.DatastoreServices;
import sh.isaac.api.DatastoreServices.DataStoreStartState;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.datastore.ExtendedStoreData;
import sh.isaac.api.datastore.extendedStore.ExtendedStoreStandAlone;
import sh.isaac.api.logic.LogicService;
import sh.isaac.api.logic.LogicServiceSnoRocket;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.provider.logic.csiro.classify.ClassifierProvider;

/**
 * The Class LogicProvider.
 *
 * @author kec
 */
@Service(name = "logic provider")
@RunLevel(value = LookupService.SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class LogicProvider
        implements LogicServiceSnoRocket {

    private static final Logger LOG = LogManager.getLogger();

    private static final String STORAGE_NAME = "logic-provider";
    
    private static final Map<ClassifierServiceKey, ClassifierService> CLASSIFIER_SERVICE_MAP = new ConcurrentHashMap<>();

    private transient final Set<Task<?>> pendingLogicTasks = ConcurrentHashMap.newKeySet();

    private transient final ObservableList<Instant> classifierInstants = FXCollections.observableArrayList();
    
    private ExtendedStoreData<Instant, List<ClassifierResults>> classifierResultMap;

    private transient DataStore store;

    /**
     * Instantiates a new logic provider.
     */
    private LogicProvider() {
        // For HK2
        LOG.info("logic provider constructed");
    }

    public Set<Task<?>> getPendingLogicTasks() {
        return pendingLogicTasks;
    }

    /**
     * Start me.
     */
    @PostConstruct
    private void startMe() {
        LOG.info("Starting LogicProvider for change to runlevel: " + LookupService.getProceedingToRunLevel());
        CLASSIFIER_SERVICE_MAP.clear();
        pendingLogicTasks.clear();
        // read from disk...
        store = Get.service(DataStore.class);
        
        if (!store.implementsExtendedStoreAPI()) {
            LOG.debug("LogicProvider uses local file for storage");
            File logicProviderDir = new File(store.getDataStorePath().toAbsolutePath().toFile(), STORAGE_NAME);
            logicProviderDir.mkdirs();
            File classifierResultsFile = new File(logicProviderDir, "classification-results");
            
            classifierResultMap = new ExtendedStoreStandAlone<Instant, byte[], List<ClassifierResults>>(classifierResultsFile, 
                classifierResultInput -> MarshalUtil.toBytes(classifierResultInput), 
                byteArrayInput -> MarshalUtil.fromBytes(byteArrayInput));
        }
        else {
            LOG.debug("LogicProvider uses extended store API for storage");
            //Instant isn't a great type to be letting MVStore serialize - but it should work, and we won't have a ton of these, so the extra overhead
            //of having it use java serialization should be ok.  Alternatively, we could make the key a byte[], and do the instant / byte[] translation in 
            //this class, but I don't think its worth the effort.
            classifierResultMap = ((ExtendedStore)store).<Instant, byte[], List<ClassifierResults>>getStore(STORAGE_NAME, 
                    classifierResultInput -> MarshalUtil.toBytes(classifierResultInput), 
                    byteArrayInput -> MarshalUtil.fromBytes(byteArrayInput));
        }
        
        if (Platform.isFxApplicationThread()) {
            classifierInstants.addAll(classifierResultMap.keySet());
        } else {
            Platform.runLater(() -> { classifierInstants.addAll(classifierResultMap.keySet()); });
        }
    }

    /**
     * Stop me.
     */
    @PreDestroy
    private void stopMe() {
        LOG.info("Stopping LogicProvider for change to runlevel: " + LookupService.getProceedingToRunLevel());
        StopMeTask stopMeTask = new StopMeTask();
        try {
            stopMeTask.call();
        } catch (Exception e) {
            LOG.error(e);
        }

        LOG.info("Stopped LogicProvider  ");
    }

    private class StopMeTask extends TimedTaskWithProgressTracker {

        public StopMeTask() {
            updateTitle("Stopping logic provider");
            addToTotalWork(2);
            Get.activeTasks().add(this);
        }
        @Override
        protected Object call() throws Exception {
            try {
                updateMessage("Completing pending logic tasks");
                for (Task<?> updateTask : pendingLogicTasks) {
                    try {
                        LOG.info("Waiting for completion of: " + updateTask.getTitle());
                        updateTask.get();
                        LOG.info("Completed: " + updateTask.getTitle());
                    } catch (Throwable ex) {
                        LOG.error(ex);
                    }
                }
                completedUnitOfWork();
                
                updateMessage("Writing classifier results");
                sync().get();
                if (store.implementsExtendedStoreAPI()) {
                    ((ExtendedStore)store).closeStore(STORAGE_NAME);
                }
                
                CLASSIFIER_SERVICE_MAP.clear();
                LogicProvider.this.pendingLogicTasks.clear();
                LogicProvider.this.classifierResultMap = null;
                LogicProvider.this.store = null;
                completedUnitOfWork();
                return null;
            } finally {
                Get.activeTasks().remove(this);
            }
         }
    }

     /**
     * Gets the classifier service.
     *
     * @param manifoldCoordinate the stamp coordinate
     * @return the classifier service
     */
    @Override
    public ClassifierService getClassifierService(ManifoldCoordinateImmutable manifoldCoordinate) {
        ObservableManifoldCoordinateImpl manifoldAnalog = new ObservableManifoldCoordinateImpl(manifoldCoordinate);
        if (manifoldCoordinate.getViewStampFilter().getTime() == Long.MAX_VALUE) {
            LOG.info("changing classify coordinate time to now, rather that latest");
            manifoldAnalog.getViewStampFilter().timeProperty().setValue(System.currentTimeMillis());
         }

        if (manifoldAnalog.getEditCoordinate().getAuthorForChanges().getNid() != TermAux.SNOROCKET_CLASSIFIER.getNid()) {
            LOG.info("changing classify coordinate author to SNOROCKET, rather than " + Get.conceptDescriptionText(manifoldAnalog.getEditCoordinate().getAuthorNidForChanges()));
            manifoldAnalog.getEditCoordinate().authorForChangesProperty().setValue(TermAux.SNOROCKET_CLASSIFIER);
        }

        ManifoldCoordinateImmutable manifoldCoordinateImmutable = manifoldAnalog.toManifoldCoordinateImmutable();
        final ClassifierServiceKey key = new ClassifierServiceKey(manifoldCoordinateImmutable);

        if (!CLASSIFIER_SERVICE_MAP.containsKey(key)) {
            CLASSIFIER_SERVICE_MAP.putIfAbsent(key,
                    new ClassifierProvider(manifoldCoordinateImmutable));
        }

        return CLASSIFIER_SERVICE_MAP.get(key);
    }

    /**
     * Gets the logical expression.
     *
     * @param conceptId         the concept id
     * @param logicAssemblageId the logic assemblage id
     * @param stampFilter   the stamp coordinate
     * @return the logical expression
     */
    @Override
    public LatestVersion<? extends LogicalExpression> getLogicalExpression(int conceptId,
                                                                           int logicAssemblageId,
                                                                           StampFilter stampFilter) {
        final SemanticSnapshotService<LogicGraphVersionImpl> ssp = Get.assemblageService()
                .getSnapshot(LogicGraphVersionImpl.class,
                        stampFilter);

        List<LatestVersion<LogicalExpression>> latestExpressions = new ArrayList<>();
        final List<LatestVersion<LogicGraphVersionImpl>> latestVersions
                = ssp.getLatestSemanticVersionsForComponentFromAssemblage(conceptId,
                logicAssemblageId);
        for (LatestVersion<LogicGraphVersionImpl> lgs : latestVersions) {
            final LogicalExpression expressionValue
                    = new LogicalExpressionImpl(lgs.get().getGraphData(),
                    DataSource.INTERNAL,
                    lgs.get().getReferencedComponentNid());

            final LatestVersion<LogicalExpression> latestExpressionValue
                    = new LatestVersion<>(expressionValue);

            lgs.contradictions().forEach((LogicGraphVersionImpl contradiction) -> {
                final LogicalExpressionImpl contradictionValue
                        = new LogicalExpressionImpl(contradiction.getGraphData(),
                        DataSource.INTERNAL,
                        contradiction.getReferencedComponentNid());

                latestExpressionValue.addLatest(contradictionValue);
            });

            latestExpressions.add(latestExpressionValue);
        }
        if (latestExpressions.isEmpty()) {
            LOG.warn("No logical expression for: " + Get.conceptDescriptionText(conceptId) + " in: "
                    + Get.conceptDescriptionText(logicAssemblageId) + "\n\n"
                    + Get.conceptService().getConceptChronology(conceptId).toString());
            return new LatestVersion<>();
        } else if (latestExpressions.size() > 1) {
            throw new IllegalStateException("More than one logical expression for concept in assemblage: "
                    + latestVersions);
        }

        return latestExpressions.get(0);
    }

    /**
     * The Class ClassifierServiceKey.
     */
    private static class ClassifierServiceKey {

        /**
         * The stamp coordinate.
         */
        ManifoldCoordinateImmutable manifoldCoordinateImmutable;

        /**
         * Instantiates a new classifier service key.
         * @param manifoldCoordinate the stamp coordinate
         *
         */
        public ClassifierServiceKey(ManifoldCoordinateImmutable manifoldCoordinate) {
            this.manifoldCoordinateImmutable = manifoldCoordinate;
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            final ClassifierServiceKey other = (ClassifierServiceKey) obj;

            return Objects.equals(this.manifoldCoordinateImmutable, other.manifoldCoordinateImmutable);
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + Objects.hashCode(this.manifoldCoordinateImmutable);
            return hash;
        }
    }

    @Override
    public ObservableList<Instant> getClassificationInstants() {
        return classifierInstants;
    }

    @Override
    public Optional<ClassifierResults[]> getClassificationResultsForInstant(Instant instant) {
        List<ClassifierResults> result = classifierResultMap.get(instant);
        return result == null ? Optional.empty() : Optional.of(result.toArray(new ClassifierResults[result.size()]));
    }

    @Override
    public void addClassifierResults(ClassifierResults classifierResults) {
        Instant classifierTime = classifierResults.getManifoldCoordinate().getViewStampFilter().getTimeAsInstant();
        if (Platform.isFxApplicationThread()) {
            classifierInstants.add(classifierTime);
        } else {
            Platform.runLater(() -> { classifierInstants.add(classifierTime); });
        }
        classifierResultMap.accumulateAndGet(classifierTime,
                Arrays.asList(new ClassifierResults[]{classifierResults}), (classifierResults1, classifierResults2) -> {
                    ArrayList<ClassifierResults> newResultList = new ArrayList<>();
                    newResultList.addAll(classifierResults1);
                    newResultList.addAll(classifierResults2);
                    return newResultList;
                });
    }

    public Path getDataStorePath() {
        return store.getDataStorePath();
    }

     public DataStoreStartState getDataStoreStartState() {
        return store.getDataStoreStartState();
    }

    public Optional<UUID> getDataStoreId() {
        return store.getDataStoreId();
    }

    @SuppressWarnings("rawtypes")
    public Future<?> sync() {
        return Get.executor().submit(() -> {
            if (classifierResultMap instanceof ExtendedStoreStandAlone) {
                ((ExtendedStoreStandAlone)classifierResultMap).sync().get();
            }  //If its not the stand along implementation, its not our problem to sync, the underlying data store will sync when necessary
            return null;
        });
    }
}
