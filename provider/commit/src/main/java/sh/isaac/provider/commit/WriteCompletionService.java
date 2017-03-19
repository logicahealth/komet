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
   
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The run. */
   private boolean                         run = false;
   
   /** The write concept completion service thread. */
   private ExecutorService                 writeConceptCompletionServiceThread;
   
   /** The conversion service. */
   private ExecutorCompletionService<Void> conversionService;
   
   /** The worker pool. */
   private ExecutorService                 workerPool;

   //~--- methods -------------------------------------------------------------

   /**
    * Run.
    */
   @Override
   public void run() {
      log.info("WriteCompletionService starting");

      while (this.run) {
         try {
            this.conversionService.take()
                             .get();
         } catch (final InterruptedException ex) {
            if (this.run) {
               // Only warn if we were not asked to shutdown
               log.warn(ex.getLocalizedMessage(), ex);
            }
         } catch (final ExecutionException ex) {
            log.error(ex.getLocalizedMessage(), ex);
         }
      }

      this.conversionService                   = null;
      this.writeConceptCompletionServiceThread = null;
      this.workerPool                          = null;
      log.info("WriteCompletionService closed");
   }

   /**
    * Start.
    */
   public void start() {
      log.info("Starting WriteCompletionService");
      this.run        = true;
      this.workerPool = Executors.newFixedThreadPool(4,
            (Runnable r) -> {
               return new Thread(r, "writeCommitDataPool");
            });
      this.conversionService                   = new ExecutorCompletionService<>(this.workerPool);
      this.writeConceptCompletionServiceThread = Executors.newSingleThreadExecutor((Runnable r) -> {
               return new Thread(r, "writeCompletionService");
            });
      this.writeConceptCompletionServiceThread.submit(this);
   }

   /**
    * Stop.
    */
   public void stop() {
      log.info("Stopping WriteCompletionService");
      this.run = false;
      this.writeConceptCompletionServiceThread.shutdown();
      this.workerPool.shutdown();
   }

   /**
    * Submit.
    *
    * @param task the task
    * @return the future
    */
   protected Future<Void> submit(Task<Void> task) {
      return this.conversionService.submit(task, null);
   }
}

