/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.logic.csiro.classify;


import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.DataSource;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.progress.ActiveTasks;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import gov.vha.isaac.ochre.util.WorkExecutors;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
public class GetConceptSequenceForExpressionTask extends Task<Integer> {

    LogicalExpression expression;
    ClassifierProvider classifierProvider;
    StampCoordinate stampCoordinate;
    LogicCoordinate logicCoordinate;
    EditCoordinate statedEditCoordinate;

    private GetConceptSequenceForExpressionTask(LogicalExpression expression, ClassifierProvider classifierProvider,
            EditCoordinate statedEditCoordinate) {
        this.expression = expression;
        this.classifierProvider = classifierProvider;
        this.stampCoordinate = classifierProvider.stampCoordinate;
        this.logicCoordinate = classifierProvider.logicCoordinate;
        this.statedEditCoordinate = statedEditCoordinate;
        updateTitle("Get ID for Expression");
        updateProgress(-1, Integer.MAX_VALUE);
    }

    public static GetConceptSequenceForExpressionTask create(LogicalExpression expression,
            ClassifierProvider classifierProvider, EditCoordinate statedEditCoordinate) {
        GetConceptSequenceForExpressionTask task = new GetConceptSequenceForExpressionTask(expression, classifierProvider, statedEditCoordinate);
        LookupService.getService(ActiveTasks.class).get().add(task);
        LookupService.getService(WorkExecutors.class).getForkJoinPoolExecutor().execute(task);
        return task;
    }

    @Override
    protected Integer call() throws Exception {
        try {
            SememeSnapshotService<LogicGraphSememeImpl> sememeSnapshot = Get.sememeService().getSnapshot(LogicGraphSememeImpl.class, stampCoordinate);
            updateMessage("Searching existing definitions...");
            Optional<LatestVersion<LogicGraphSememeImpl>> match = sememeSnapshot.
                    getLatestSememeVersionsFromAssemblage(
                            logicCoordinate.getStatedAssemblageSequence()).
                    filter((LatestVersion<LogicGraphSememeImpl> t) -> {
                        LogicGraphSememeImpl lgs = t.value();
                        LogicalExpressionOchreImpl existingGraph = new LogicalExpressionOchreImpl(lgs.getGraphData(), DataSource.INTERNAL);
                        updateMessage("found existing definition");
                        return existingGraph.equals(expression);
                    }).findFirst();

            if (match.isPresent()) {
                LogicGraphSememeImpl lgs = match.get().value();

                return Get.identifierService().getConceptSequence(lgs.getReferencedComponentNid());
            }

            updateMessage("Building new concept...");
            UUID uuidForNewConcept = UUID.randomUUID();
            ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);
            conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANUGAGE);
            conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT);
            conceptBuilderService.setDefaultLogicCoordinate(logicCoordinate);
            ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(
                    uuidForNewConcept.toString(), "expression", expression);

            ConceptChronology concept = builder.build(statedEditCoordinate, ChangeCheckerMode.INACTIVE);
            updateMessage("Commiting new expression...");
            try {
                Get.commitService().commit("Expression commit.").get();
                updateMessage("Classifying new concept...");
                classifierProvider.classify().get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            
            return concept.getConceptSequence();
        } finally {
            updateProgress(-1, Integer.MAX_VALUE);
            LookupService.getService(ActiveTasks.class).get().remove(this);
        }
    }

}
