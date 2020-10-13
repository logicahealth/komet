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

//~--- JDK imports ------------------------------------------------------------

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.DataSource;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicService;
import sh.isaac.api.logic.LogicServiceSnoRocket;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.logic.ClassifierResultsImpl;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.provider.logic.csiro.classify.ClassifierProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class LogicProvider.
 *
 * @author kec
 */
@Service(name = "logic provider")
@RunLevel(value = LookupService.SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class LogicProvider
        implements LogicServiceSnoRocket {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * The Constant classifierServiceMap.
     */
    private static final Map<ClassifierServiceKey, ClassifierService> classifierServiceMap = new ConcurrentHashMap<>();

    private final Set<Task<?>> pendingLogicTasks = ConcurrentHashMap.newKeySet();

    private final ConcurrentHashMap<Instant, ClassifierResults[]> classifierResultMap = new ConcurrentHashMap<>();

    private final ObservableList<Instant> classifierInstants = FXCollections.observableArrayList();

    private File classifierResultsFile;

    //~--- constructors --------------------------------------------------------

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

    //~--- methods -------------------------------------------------------------

    /**
     * Start me.
     */
    @PostConstruct
    private void startMe() {
        LOG.info("Starting LogicProvider for change to runlevel: " + LookupService.getProceedingToRunLevel());
        classifierServiceMap.clear();
        pendingLogicTasks.clear();
        // read from disk...
        DataStore store = Get.service(DataStore.class);
        File logicProviderDir = new File(store.getDataStorePath().toAbsolutePath().toFile(), "logic-provider");
        logicProviderDir.mkdirs();
        this.classifierResultsFile = new File(logicProviderDir, "classification-results");
        if (this.classifierResultsFile.exists()) {
            // Open and load...
            try (DataInputStream input = new DataInputStream(new FileInputStream(this.classifierResultsFile))) {
                ByteArrayDataBuffer buff = ByteArrayDataBuffer.make(input);
                int instantCount = buff.getInt();
                for (int i = 0; i < instantCount; i++) {
                  Instant instant = Instant.ofEpochMilli(buff.getLong());
                  LOG.info("Reading classifier results for: " + instant);
                  ClassifierResultsImpl[] resultsForInstant = new ClassifierResultsImpl[buff.getInt()];
                  for (int j = 0; j < resultsForInstant.length; j++) {
                     resultsForInstant[j] = ClassifierResultsImpl.make(buff);
                  }
                  this.classifierResultMap.put(instant, resultsForInstant);
                    if (Platform.isFxApplicationThread()) {
                        classifierInstants.add(instant);
                    } else {
                        Platform.runLater(() -> { classifierInstants.add(instant); });
                    }

                }
            } catch (IOException e) {
               LOG.error(e);
            }
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
                LogicProvider.this.classifierServiceMap.clear();
                LogicProvider.this.pendingLogicTasks.clear();
                ByteArrayDataBuffer buff = new ByteArrayDataBuffer();
                Set<Map.Entry<Instant, ClassifierResults[]>> classifierResultsEntrySet = classifierResultMap.entrySet();
                buff.putInt(classifierResultsEntrySet.size());
                for (Map.Entry<Instant, ClassifierResults[]> entry: classifierResultsEntrySet) {
                    buff.putLong(entry.getKey().toEpochMilli());
                    LOG.info("Writing classifier results for: " + entry.getKey());
                    buff.putInt(entry.getValue().length);
                    for (ClassifierResults results: entry.getValue()) {
                        ((ClassifierResultsImpl) results).putExternal(buff);
                    }
                }

                // write to disk...
                try (DataOutputStream output = new DataOutputStream(new FileOutputStream(LogicProvider.this.classifierResultsFile))) {
                    buff.write(output);
                } catch (IOException e) {
                    LOG.error(e);
                }
                completedUnitOfWork();
                return null;
            } finally {
                Get.activeTasks().remove(this);
            }
         }
    }

    //~--- get methods ---------------------------------------------------------

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

        if (!classifierServiceMap.containsKey(key)) {
            classifierServiceMap.putIfAbsent(key,
                    new ClassifierProvider(manifoldCoordinateImmutable));
        }

        return classifierServiceMap.get(key);
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

    //~--- inner classes -------------------------------------------------------

    /**
     * The Class ClassifierServiceKey.
     */
    private static class ClassifierServiceKey {

        /**
         * The stamp coordinate.
         */
        ManifoldCoordinateImmutable manifoldCoordinateImmutable;

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new classifier service key.
         * @param manifoldCoordinate the stamp coordinate
         *
         */
        public ClassifierServiceKey(ManifoldCoordinateImmutable manifoldCoordinate) {
            this.manifoldCoordinateImmutable = manifoldCoordinate;
        }

        //~--- methods ----------------------------------------------------------

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
        return Optional.ofNullable(classifierResultMap.get(instant));
    }

    @Override
    public void addClassifierResults(ClassifierResults classifierResults) {
        Instant classifierTime = classifierResults.getManifoldCoordinate().getViewStampFilter().getTimeAsInstant();
        if (Platform.isFxApplicationThread()) {
            classifierInstants.add(classifierTime);
        } else {
            Platform.runLater(() -> { classifierInstants.add(classifierTime); });
        }
        classifierResultMap.merge(classifierTime,
                new ClassifierResults[]{classifierResults}, (classifierResults1, classifierResults2) -> {
                    ArrayList<ClassifierResults> newResultList = new ArrayList<>();
                    newResultList.addAll(Arrays.asList(classifierResults1));
                    newResultList.addAll(Arrays.asList(classifierResults2));
                    return newResultList.toArray(new ClassifierResults[newResultList.size()]);
                });
    }
}
