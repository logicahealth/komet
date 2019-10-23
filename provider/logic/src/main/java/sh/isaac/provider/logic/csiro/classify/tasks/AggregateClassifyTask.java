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

import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * The Class AggregateClassifyTask.
 *
 * @author kec
 */
public class AggregateClassifyTask
        extends SequentialAggregateTask<ClassifierResults> implements PersistTaskResult {

   private CycleCheck cc = null;
   private static Logger log = LogManager.getLogger();
   
   //Classification takes a tremendous amount of memory.  Don't allow it to run in parallel.
   private static Semaphore concurrentRunPrevent = new Semaphore(1);
   
   /**
    * Instantiates a new aggregate classify task.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    */
   private AggregateClassifyTask(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate, boolean cycleCheckFirst) {
      super("Classify",
            new Task[] { new ExtractAxioms(stampCoordinate,logicCoordinate), new LoadAxioms(), new ClassifyAxioms(), new ProcessClassificationResults()});
      if (cycleCheckFirst) {
         cc = new CycleCheck(stampCoordinate, logicCoordinate);
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
            log.debug("Running cycle check");
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
         concurrentRunPrevent.release();
         log.debug("Released classifier lock");
         if (LookupService.hasService(MemoryManagementService.class)) {
            LookupService.getService(MemoryManagementService.class)
                    .removeState(ApplicationStates.CLASSIFYING);
         }
         Get.service(LogicProvider.class).getPendingLogicTasks().remove(this);
      }
   }

    /**
     * When this method returns, the task is already executing, or will be shortly, if another classifier execution is already running.  
     * You do not need to execute the task.
     *
     * @param stampCoordinate the stamp coordinate
     * @param logicCoordinate the logic coordinate
     * @param cycleCheckFirst true, to do a cycle check on the stated taxonomy prior to classify.  Will abort classify if a cycle is detected.
     * @return an {@code AggregateClassifyTask} already submitted to an executor.
     */
    public static AggregateClassifyTask get(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate, boolean cycleCheckFirst) {
        final AggregateClassifyTask classifyTask = new AggregateClassifyTask(stampCoordinate, logicCoordinate, cycleCheckFirst);
        //The execution of this classify operation may block, if another classification is already running.
        //Don't want to (potentially) lose all of the work executor slots to blocked classifications - so spawn a new thread here, if necessary, 
        //to wait.
        Runnable r = (()-> {
            //If the classifier is currently running, this may block.  
            try {
                log.debug("Acquiring classifier execution slot");
                concurrentRunPrevent.acquire();
                log.debug("Acquired slot, executing classifier");
                Get.workExecutors().getExecutor().execute(classifyTask);
                Get.service(LogicProvider.class).getPendingLogicTasks().add(classifyTask);
            }
            catch (InterruptedException e) {
                log.error("Unexpected interrupt waiting for classify slot.  Not executing");
            }
        });
        
        if (concurrentRunPrevent.availablePermits() > 0) {
            //Since we just checked, its highly unlikely this will block.  Run the task submit directly on this thread.
            r.run();
        }
        else {
            log.debug("Spawning a thread to wait for classification slot");
            Thread t = new Thread(r, "classification run queue");
            t.setDaemon(true);
            t.start();
        }
        return classifyTask;
    }
}