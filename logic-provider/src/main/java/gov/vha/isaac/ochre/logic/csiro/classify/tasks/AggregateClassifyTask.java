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
package gov.vha.isaac.ochre.logic.csiro.classify.tasks;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.classifier.ClassifierResults;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.task.SequentialAggregateTask;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
public class AggregateClassifyTask extends SequentialAggregateTask<ClassifierResults> {

    private AggregateClassifyTask(StampCoordinate stampCoordinate,
    LogicCoordinate logicCoordinate) {
        super("Classify", new Task[] {
            new ExtractAxioms(stampCoordinate, logicCoordinate),
            new LoadAxioms(stampCoordinate, logicCoordinate),
            new ClassifyAxioms(stampCoordinate, logicCoordinate),
            new ProcessClassificationResults(stampCoordinate, logicCoordinate),
        });
    }
    
    /**
     * 
     * @param stampCoordinate
     * @param logicCoordinate
     * @return an {@code AggregateClassifyTask} already submitted to an executor.
     */
    public static AggregateClassifyTask get(StampCoordinate stampCoordinate,
        LogicCoordinate logicCoordinate) {
        AggregateClassifyTask classifyTask = new AggregateClassifyTask(stampCoordinate, 
                logicCoordinate);
        Get.activeTasks().add(classifyTask);
        Get.workExecutors().getExecutor().execute(classifyTask);
        return classifyTask;
    }
    
    
}
