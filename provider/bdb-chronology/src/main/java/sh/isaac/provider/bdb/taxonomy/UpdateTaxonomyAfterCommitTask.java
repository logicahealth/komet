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



package sh.isaac.provider.bdb.taxonomy;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class UpdateTaxonomyAfterCommitTask.
 *
 * @author kec
 */
public class UpdateTaxonomyAfterCommitTask
        extends TimedTask<Void> {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The work done. */
   int workDone = 0;

   /** The total work. */
   int totalWork = 0;

   /** The taxonomy service. */
   TaxonomyService taxonomyService;

   /** The commit record. */
   CommitRecord commitRecord;

   /** The semantic identifiers for unhandled changes. */
   ConcurrentSkipListSet<Integer> semanticNidsForUnhandledChanges;

   /** The lock. */
   Semaphore permit;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new update taxonomy after commit task.
    *
    * @param taxonomyService the taxonomy service
    * @param commitRecord the commit record
    * @param semanticNidsForUnhandledChanges the sememe sequences for unhandled changes
    * @param permit the lock
    */
   private UpdateTaxonomyAfterCommitTask(TaxonomyService taxonomyService,
         CommitRecord commitRecord,
         ConcurrentSkipListSet<Integer> semanticNidsForUnhandledChanges,
           Semaphore permit) {
      this.commitRecord                       = commitRecord;
      this.semanticNidsForUnhandledChanges = semanticNidsForUnhandledChanges;
      this.permit                               = permit;
      this.taxonomyService                    = taxonomyService;
      this.totalWork                          = semanticNidsForUnhandledChanges.size();
      this.updateTitle("Update taxonomy after commit");
      this.updateProgress(this.workDone, this.totalWork);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Call.
    *
    * @return the void
    * @throws Exception the exception
    */
   @Override
   protected Void call()
            throws Exception {

      try {
         final AtomicBoolean atLeastOneFailed = new AtomicBoolean(false);

         this.semanticNidsForUnhandledChanges.stream().forEach((semanticSequence) -> {
                           try {
                              this.workDone++;
                              this.updateProgress(this.workDone, this.totalWork);

                              if (this.commitRecord.getSemanticNidsInCommit()
                                    .contains(semanticSequence)) {
                                 this.updateMessage("Updating taxonomy for: " + semanticSequence);
                                 this.taxonomyService.updateTaxonomy((SemanticChronology) Get.assemblageService()
                                           .getSemanticChronology(semanticSequence));
                                 this.semanticNidsForUnhandledChanges.remove(semanticSequence);
                              }
                           } catch (final Exception e) {
                              LOG.error("Error handling update taxonomy after commit on semantic " + semanticSequence, e);
                              atLeastOneFailed.set(true);
                           }
                        });

         if (atLeastOneFailed.get()) {
            throw new RuntimeException("There were errors during taxonomy update after commit");
         }

         this.updateMessage("complete");
         return null;
      } finally {
         this.permit.release();
         Get.activeTasks()
            .remove(this);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Get an executing task that will update the taxonomy.
    * @param taxonomyService the service to update
    * @param commitRecord the commitRecord to process
    * @param unhandledChanges the changes to look for
    * @param permit permit for the update
    * @return a task, submitted to an executor, and added to the active task set.
    *
    */
   public static UpdateTaxonomyAfterCommitTask get(TaxonomyService taxonomyService,
         CommitRecord commitRecord,
         ConcurrentSkipListSet<Integer> unhandledChanges,
         Semaphore permit) {
      final UpdateTaxonomyAfterCommitTask task = new UpdateTaxonomyAfterCommitTask(taxonomyService,
                                                                                   commitRecord,
                                                                                   unhandledChanges,
                                                                                   permit);
      Get.activeTasks()
         .add(task);
      Get.workExecutors()
         .getExecutor()
         .execute(task);
      return task;
   }
}

