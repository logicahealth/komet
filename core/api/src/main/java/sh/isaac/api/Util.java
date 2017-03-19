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

//~--- JDK imports ------------------------------------------------------------

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.concurrent.ExecutionException;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import sh.isaac.api.progress.ActiveTasks;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Util {
   public static <T> T addToTaskSetAndWaitTillDone(Task<T> task)
            throws InterruptedException, ExecutionException {
      LookupService.get()
                   .getService(ActiveTasks.class)
                   .get()
                   .add(task);

      try {
         final T returnValue = task.get();

         return returnValue;
      } finally {
         LookupService.get()
                      .getService(ActiveTasks.class)
                      .get()
                      .remove(task);
      }
   }

   public static Path[] stringArrayToPathArray(String... strings) {
      final Path[] paths = new Path[strings.length];

      for (int i = 0; i < paths.length; i++) {
         paths[i] = Paths.get(strings[i]);
      }

      return paths;
   }
}

