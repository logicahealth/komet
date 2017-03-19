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



package sh.isaac.api.progress;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.ticker.Ticker;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 4/9/15.
 */
public class ActiveTasksTicker {
   
   /** The Constant log. */
   private static final Logger log    = LogManager.getLogger();
   
   /** The Constant ticker. */
   private static final Ticker ticker = new Ticker();

   //~--- methods -------------------------------------------------------------

   /**
    * Start.
    *
    * @param intervalInSeconds the interval in seconds
    */
   public static void start(int intervalInSeconds) {
      ticker.start(intervalInSeconds,
                   (tick) -> {
                      final Set<Task<?>> taskSet = Get.activeTasks()
                                                .get();

                      taskSet.stream().forEach((task) -> {
                                         double percentProgress = task.getProgress() * 100;

                                         if (percentProgress < 0) {
                                            percentProgress = 0;
                                         }

                                         log.printf(org.apache.logging.log4j.Level.INFO,
                                               "%n    %s%n    %s%n    %.1f%% complete",
                                               task.getTitle(),
                                               task.getMessage(),
                                               percentProgress);
                                      });
                   });
   }

   /**
    * Stop.
    */
   public static void stop() {
      ticker.stop();
   }
}

