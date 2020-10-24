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
import sh.isaac.api.Get;
import sh.isaac.api.task.AggregateTaskInput;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.provider.logic.csiro.classify.ClassifierData;

/**
 * The Class LoadAxioms.
 *
 * @author kec
 */
public class LoadAxioms
        extends TimedTaskWithProgressTracker<ClassifierData> implements AggregateTaskInput {

   ClassifierData inputData;
   Logger log = LogManager.getLogger();

   /**
    * Instantiates a new load axioms.
    */
   public LoadAxioms() {
      updateTitle("Load axioms");
   }

   /**
    * Must pass in a {@link ClassifierData} prior to executing this task 
    * @see sh.isaac.api.task.AggregateTaskInput#setInput(java.lang.Object)
    */
   @Override
   public void setInput(Object inputData)  {
      if (!(inputData instanceof ClassifierData)) {
         throw new RuntimeException("Input data to LoadAxioms must be " + ClassifierData.class.getName());
      }
      this.inputData = (ClassifierData)inputData;
   }

   @Override
   protected ClassifierData call()
            throws Exception {
      Get.activeTasks().add(this);
      setStartTime();
      try {
         log.info("Loading Axioms");
         if (inputData == null) {
            throw new RuntimeException("Input data to LoadAxioms must be specified by calling setInput prior to executing");
         }
         inputData.loadAxioms();
         return inputData;
      }
      finally {
         Get.activeTasks().remove(this);
         log.info("Load axioms complete");
      }
   }
}