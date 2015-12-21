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
package gov.vha.isaac.ochre.logic.provider;

import au.csiro.ontology.Ontology;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.classifier.ClassifierResults;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.progress.ActiveTasks;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
public class UpdateInferredFormsTask extends Task<Integer> {

    ClassifierResults classifierResults;
    Ontology classifiedModel;
    LogicCoordinate logicCoordinate;
    StampCoordinate stampCoordinate;
    AtomicInteger processedCount = new AtomicInteger();
    int conceptsToProcess;

    public static UpdateInferredFormsTask create(ClassifierResults classifierResults,
            Ontology classifiedModel, LogicCoordinate logicCoordinate,
            StampCoordinate stampCoordinate) {
        UpdateInferredFormsTask task = new UpdateInferredFormsTask(classifierResults,
                classifiedModel, logicCoordinate, stampCoordinate);
        LookupService.getService(ActiveTasks.class).get().add(task);
        return task;

    }

    private UpdateInferredFormsTask(ClassifierResults classifierResults,
            Ontology classifiedModel, LogicCoordinate logicCoordinate,
            StampCoordinate stampCoordinate) {
        this.classifierResults = classifierResults;
        this.classifiedModel = classifiedModel;
        this.logicCoordinate = logicCoordinate;
        this.stampCoordinate = stampCoordinate;
        conceptsToProcess = classifierResults.getAffectedConcepts().size();
        updateProgress(-1, conceptsToProcess); // Indeterminate progress
        updateValue(0); // no concepts processed
        updateTitle("Updating inferred taxonomy and forms ");
    }

    @Override
    protected Integer call() throws Exception {
        try {
            SememeSnapshotService<LogicGraphSememe> sememeSnapshot = 
                    Get.sememeService().getSnapshot(LogicGraphSememe.class, stampCoordinate);
            
            classifierResults.getAffectedConcepts().stream().parallel().forEach((conceptSequence) -> {
                if (processedCount.incrementAndGet() % 10 == 0) {
                    updateProgress(processedCount.get(), conceptsToProcess);
                    ConceptChronology concept = Get.conceptService().getConcept(conceptSequence);
                    updateMessage("Updating concept: " + concept.toUserString());
                    updateValue(processedCount.get());
                    
                    sememeSnapshot.getLatestSememeVersionsForComponentFromAssemblage(conceptSequence, 
                            logicCoordinate.getInferredAssemblageSequence())
                            .forEach((LatestVersion<LogicGraphSememe> latestLogicGraph) -> {
                                processLogicGraphSememe(latestLogicGraph);
                    });
                }

            });
        } finally {
            LookupService.getService(ActiveTasks.class).get().remove(this);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private void processLogicGraphSememe(LatestVersion<LogicGraphSememe> latest) {
        
    }
    
}
