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



package sh.isaac.provider.logic.csiro.classify.tasks;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;
import sh.isaac.api.ApplicationStates;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.memory.MemoryManagementService;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.SequentialAggregateTask;
import sh.isaac.provider.logic.LogicProvider;

//~--- classes ----------------------------------------------------------------

/**
 * The Class AggregateClassifyTask.
 *
 * @author kec
 */
public class AggregateClassifyTask
        extends SequentialAggregateTask<ClassifierResults> implements PersistTaskResult {
   /**
    * Instantiates a new aggregate classify task.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    */
   private AggregateClassifyTask(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate) {
      super("Classify",
            new Task[] { new ExtractAxioms(stampCoordinate,
                  logicCoordinate), new LoadAxioms(stampCoordinate,
                        logicCoordinate), new ClassifyAxioms(stampCoordinate,
                              logicCoordinate), new ProcessClassificationResults(stampCoordinate, logicCoordinate), });
   }

   //~--- get methods ---------------------------------------------------------

    @Override
   protected ClassifierResults call() throws Exception {
      LookupService.getService(MemoryManagementService.class)
                   .addState(ApplicationStates.CLASSIFYING);
       try {
        return super.call(); 
       } finally {
           LookupService.getService(MemoryManagementService.class)
                   .removeState(ApplicationStates.CLASSIFYING);
           Get.service(LogicProvider.class).getPendingLogicTasks().remove(this);

       }
   }

    /**
     * When this method returns, the task is already executing.
     *
     * @param stampCoordinate the stamp coordinate
     * @param logicCoordinate the logic coordinate
     * @return an {@code AggregateClassifyTask} already submitted to an executor.
     */
    public static AggregateClassifyTask get(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate) {
        final AggregateClassifyTask classifyTask = new AggregateClassifyTask(stampCoordinate, logicCoordinate);
        
        Get.activeTasks()
                .add(classifyTask);
        Get.workExecutors()
                .getExecutor()
                .execute(classifyTask);
        Get.service(LogicProvider.class).getPendingLogicTasks().add(classifyTask);
        return classifyTask;
    }
}

