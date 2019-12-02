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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.concurrent.Task;
import sh.isaac.api.ApplicationStates;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.memory.MemoryManagementService;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.SequentialAggregateTask;
import sh.isaac.provider.logic.LogicProvider;

import java.time.Instant;

/**
 * The Class AggregateClassifyTask.
 *
 * @author kec
 */
public class AggregateClassifyTask
        extends SequentialAggregateTask<ClassifierResults> implements PersistTaskResult {

   private CycleCheck cc = null;
   private Logger log = LogManager.getLogger();
   
   /**
    * Instantiates a new aggregate classify task.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    */
   private AggregateClassifyTask(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate, EditCoordinate editCoordinate, boolean cycleCheckFirst) {
      super("Classify",
            new Task[] { new ExtractAxioms(stampCoordinate,logicCoordinate), new LoadAxioms(), new ClassifyAxioms(),
                    new ProcessClassificationResults(stampCoordinate, logicCoordinate, editCoordinate)});
      if (cycleCheckFirst) {
         cc = new CycleCheck(stampCoordinate, logicCoordinate, editCoordinate);
      }
   }

   @Override
   protected ClassifierResults call() throws Exception {
      Get.activeTasks().add(this);
      //Logic service doesn't depend on the memory management service, so this isn't safe without checking....
      if (LookupService.hasService(MemoryManagementService.class)) {
         LookupService.getService(MemoryManagementService.class)
                  .addState(ApplicationStates.CLASSIFYING);
      }
      try {
         if (cc != null) {
            ClassifierResults cr = cc.call();
            if (cr != null) {
               // had a cycle.  Abort.
               log.info("At least one cycle detected, classification aborted - summary: {}", cr);
               return cr;
            }
         }
         log.debug("Starting classification aggregate tasks");
         ClassifierResults cr = super.call();
         if (cc != null) {
            cr.addOrphans(cc.getOrphans());
         }
         log.info("Classification task finished - summary: {}", cr.toString());
         return cr;
      } finally {
         if (LookupService.hasService(MemoryManagementService.class)) {
            LookupService.getService(MemoryManagementService.class)
                    .removeState(ApplicationStates.CLASSIFYING);
         }
         Get.service(LogicProvider.class).getPendingLogicTasks().remove(this);
      }
   }

    /**
     * When this method returns, the task is already executing.
     *
     * @param stampCoordinate the stamp coordinate
     * @param logicCoordinate the logic coordinate
     * @param cycleCheckFirst true, to do a cycle check on the stated taxonomy prior to classify.  Will abort classify if a cycle is detected.
     * @return an {@code AggregateClassifyTask} already submitted to an executor.
     */
    public static AggregateClassifyTask get(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate, EditCoordinate editCoordinate, boolean cycleCheckFirst) {
       Instant classifyCommitTime = Get.commitService().getTimeForCommit();
       stampCoordinate = stampCoordinate.makeCoordinateAnalog(classifyCommitTime.toEpochMilli());
        final AggregateClassifyTask classifyTask = new AggregateClassifyTask(stampCoordinate, logicCoordinate, editCoordinate, cycleCheckFirst);
        Get.workExecutors()
                .getExecutor()
                .execute(classifyTask);
        Get.service(LogicProvider.class).getPendingLogicTasks().add(classifyTask);
        return classifyTask;
    }
}