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



package sh.isaac.api;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.concurrent.Task;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.externalizable.IsaacObjectType;


/**
 * The Class Util.
 *
 * @author kec
 */
public class Util {
	
	private static final Logger LOG = LogManager.getLogger(Util.class);
   /**
    * Adds the to task set and wait till done.
    *
    * @param <T> the generic type
    * @param task the task
    * @return the t
    * @throws InterruptedException the interrupted exception
    * @throws ExecutionException the execution exception
    */
   public static <T> T addToTaskSetAndWaitTillDone(Task<T> task)
            throws InterruptedException, ExecutionException {
      Get.activeTasks().add(task);

      try {
         final T returnValue = task.get();

         return returnValue;
      } finally {
         Get.activeTasks().remove(task);
      }
   }

   /**
    * String array to path array.
    *
    * @param strings the strings
    * @return the path[]
    */
   public static Path[] stringArrayToPathArray(String... strings) {
      final Path[] paths = new Path[strings.length];

      for (int i = 0; i < paths.length; i++) {
         paths[i] = Paths.get(strings[i]);
      }

      return paths;
   }
   
   /**
    * Convenience method to find the nearest concept related to a semantic.  Recursively walks referenced components until it finds a concept.
    * @param nid 
    * @return the nearest concept nid, or empty, if no concept can be found.
    */
   public static Optional<Integer> getNearestConcept(int nid) {
      Optional<? extends Chronology> c = Get.identifiedObjectService().getChronology(nid);
      
      if (c.isPresent()) {
         if (c.get().getIsaacObjectType() == IsaacObjectType.SEMANTIC) {
            return getNearestConcept(((SemanticChronology)c.get()).getReferencedComponentNid());
         }
         else if (c.get().getIsaacObjectType() == IsaacObjectType.CONCEPT) {
            return Optional.of(((ConceptChronology)c.get()).getNid());
         }
         else {
            LOG.warn("Unexpected object type: " + c.get().getIsaacObjectType());
         }
      }
      return Optional.empty();
   }
}