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



package sh.isaac.provider.commit;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- classes ----------------------------------------------------------------

/**
 * This class executes the jobs to write to disk, and also has a single thread running to call .get() on
 * the future for each job submitted, to ensure that any errors are logged / warned (if the caller didn't bother
 * to call get)
 *
 * Previous versions of this class used the ExecuterCompletionService stuff... but that turns out to be fundamentally
 * broken in combination with JavaFX Task objects - you end up with a Task and a Future, and the Task never gets completion
 * notification.
 *
 * @author kec
 * @author darmbrust
 */
public class WriteCompletionService
         implements Runnable {
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private boolean                         run = false;
   private ExecutorService                 writeConceptCompletionServiceThread;
   private ExecutorCompletionService<Void> conversionService;
   private ExecutorService                 workerPool;

   //~--- methods -------------------------------------------------------------

   @Override
   public void run() {
      log.info("WriteCompletionService starting");

      while (run) {
         try {
            conversionService.take()
                             .get();
         } catch (InterruptedException ex) {
            if (run) {
               // Only warn if we were not asked to shutdown
               log.warn(ex.getLocalizedMessage(), ex);
            }
         } catch (ExecutionException ex) {
            log.error(ex.getLocalizedMessage(), ex);
         }
      }

      conversionService                   = null;
      writeConceptCompletionServiceThread = null;
      workerPool                          = null;
      log.info("WriteCompletionService closed");
   }

   public void start() {
      log.info("Starting WriteCompletionService");
      run        = true;
      workerPool = Executors.newFixedThreadPool(4,
            (Runnable r) -> {
               return new Thread(r, "writeCommitDataPool");
            });
      conversionService                   = new ExecutorCompletionService<>(workerPool);
      writeConceptCompletionServiceThread = Executors.newSingleThreadExecutor((Runnable r) -> {
               return new Thread(r, "writeCompletionService");
            });
      writeConceptCompletionServiceThread.submit(this);
   }

   public void stop() {
      log.info("Stopping WriteCompletionService");
      run = false;
      writeConceptCompletionServiceThread.shutdown();
      workerPool.shutdown();
   }

   protected Future<Void> submit(Task<Void> task) {
      return conversionService.submit(task, null);
   }
}

