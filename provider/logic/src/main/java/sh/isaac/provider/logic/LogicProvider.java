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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.DataSource;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.logic.ClassifierResultsImpl;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.provider.logic.csiro.classify.ClassifierProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class LogicProvider.
 *
 * @author kec
 */
@Service(name = "logic provider")
@RunLevel(value = LookupService.SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class LogicProvider
        implements LogicService {

    private static final Logger LOG = LogManager.getLogger();

    private static final Map<ClassifierServiceKey, ClassifierService> classifierServiceMap = new ConcurrentHashMap<>();

    private final Set<Task<?>> pendingLogicTasks = ConcurrentHashMap.newKeySet();

    private final ConcurrentHashMap<Instant, ClassifierResults[]> classifierResultMap = new ConcurrentHashMap<>();

    private final ObservableList<Instant> classifierInstants = FXCollections.observableArrayList();

    private File classifierResultsFile;

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
        classifierServiceMap.clear();
        pendingLogicTasks.clear();
        // read from disk...
        DataStore store = Get.service(DataStore.class);
        //TODO DAN[1], once again, an ad-hoc store that completely ignores the fact that we have backing providers.  This should be going to 
        //MVStore, Or File, or Postgres, depending on the configuration.  Completely broken/unusable.
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
        for (Task<?> updateTask : pendingLogicTasks) {
            try {
                LOG.info("Waiting for completion of: " + updateTask.getTitle());
                updateTask.get();
                LOG.info("Completed: " + updateTask.getTitle());
            } catch (Throwable ex) {
                LOG.error(ex);
            }
        }
        this.classifierServiceMap.clear();
        this.pendingLogicTasks.clear();
        //TODO Dan[1] fragile mess of only saving on shutdown needs to be fixed too....
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
        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(this.classifierResultsFile))) {
            buff.write(output);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

     /**
     * Gets the classifier service.
     *
     * @param stampFilter the stamp coordinate
     * @param logicCoordinate the logic coordinate
     * @param editCoordinate  the edit coordinate
     * @return the classifier service
     */
    @Override
    public ClassifierService getClassifierService(StampFilter stampFilter,
                                                  LogicCoordinate logicCoordinate,
                                                  EditCoordinate editCoordinate) {
        StampFilter stampFilterAnalog;
        if (stampFilter.getStampPosition().getTime() == Long.MAX_VALUE) {
            LOG.info("changing classify coordinate time to now, rather that latest");
            stampFilterAnalog = stampFilter.makeCoordinateAnalog(System.currentTimeMillis());
        }
        else {
            stampFilterAnalog = stampFilter;
        }
        EditCoordinate ec;
        if (editCoordinate.getAuthorNid() != MetaData.IHTSDO_CLASSIFIER____SOLOR.getNid()){
           ec = EditCoordinateImmutable.make(MetaData.IHTSDO_CLASSIFIER____SOLOR.getNid(), editCoordinate.getModuleNid(), editCoordinate.getPathNid());
        }
        else {
           ec = editCoordinate;
        }
        
        final ClassifierServiceKey key = new ClassifierServiceKey(stampFilterAnalog, logicCoordinate, ec);
         

        if (!classifierServiceMap.containsKey(key)) {
            classifierServiceMap.putIfAbsent(key,
                    new ClassifierProvider(stampFilterAnalog, logicCoordinate, ec));
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

    /**
     * The Class ClassifierServiceKey.
     */
    private static class ClassifierServiceKey {

        StampFilter stampCoordinate;
        LogicCoordinate logicCoordinate;
        EditCoordinate editCoordinate;

        /**
         * Instantiates a new classifier service key.
         *  @param stampFilter the stamp coordinate
         * @param logicCoordinate the logic coordinate
         * @param editCoordinate  the edit coordinate
         */
        public ClassifierServiceKey(StampFilter stampFilter,
                                    LogicCoordinate logicCoordinate,
                                    EditCoordinate editCoordinate) {
            this.stampCoordinate = stampFilter;
            this.logicCoordinate = logicCoordinate;
            this.editCoordinate = editCoordinate;
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

            if (!Objects.equals(this.stampCoordinate, other.stampCoordinate)) {
                return false;
            }

            if (!Objects.equals(this.logicCoordinate, other.logicCoordinate)) {
                return false;
            }

            return Objects.equals(this.editCoordinate, other.editCoordinate);
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            int hash = 3;

            hash = 59 * hash + Objects.hashCode(this.logicCoordinate);
            hash = 59 * hash + Objects.hashCode(this.stampCoordinate);
            hash = 59 * hash + Objects.hashCode(this.editCoordinate);
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
        Instant classifierTime = classifierResults.getStampFilter().getStampPosition().getTimeAsInstant();
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
