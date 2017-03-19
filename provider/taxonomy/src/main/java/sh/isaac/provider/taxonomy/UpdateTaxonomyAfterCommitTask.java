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



package sh.isaac.provider.taxonomy;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.task.TimedTask;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class UpdateTaxonomyAfterCommitTask
        extends TimedTask<Void> {
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   int                            workDone  = 0;
   int                            totalWork = 0;
   TaxonomyService                taxonomyService;
   CommitRecord                   commitRecord;
   ConcurrentSkipListSet<Integer> sememeSequencesForUnhandledChanges;
   StampedLock                    lock;

   //~--- constructors --------------------------------------------------------

   private UpdateTaxonomyAfterCommitTask(TaxonomyService taxonomyService,
         CommitRecord commitRecord,
         ConcurrentSkipListSet<Integer> sememeSequencesForUnhandledChanges,
         StampedLock lock) {
      this.commitRecord                       = commitRecord;
      this.sememeSequencesForUnhandledChanges = sememeSequencesForUnhandledChanges;
      this.lock                               = lock;
      this.taxonomyService                    = taxonomyService;
      this.totalWork                          = sememeSequencesForUnhandledChanges.size();
      this.updateTitle("Update taxonomy after commit");
      this.updateProgress(workDone, totalWork);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected Void call()
            throws Exception {
      long stamp = lock.writeLock();

      try {
         AtomicBoolean atLeastOneFailed = new AtomicBoolean(false);

         sememeSequencesForUnhandledChanges.stream().forEach((sememeSequence) -> {
                  try {
                     workDone++;
                     this.updateProgress(workDone, totalWork);

                     if (commitRecord.getSememesInCommit()
                                     .contains(sememeSequence)) {
                        this.updateMessage("Updating taxonomy for: " + sememeSequence);
                        taxonomyService.updateTaxonomy((SememeChronology<LogicGraphSememe<?>>) Get.sememeService()
                              .getSememe(sememeSequence));
                        sememeSequencesForUnhandledChanges.remove(sememeSequence);
                     }
                  } catch (Exception e) {
                     log.error("Error handling update taxonomy after commit on sememe " + sememeSequence, e);
                     atLeastOneFailed.set(true);
                  }
               });

         if (atLeastOneFailed.get()) {
            throw new RuntimeException("There were errors during taxonomy update after commit");
         }

         this.updateMessage("complete");
         return null;
      } finally {
         lock.unlockWrite(stamp);
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
    * @param lock write lock for the update
    * @return a task, submitted to an executor, and added to the active task set.
    *
    */
   public static UpdateTaxonomyAfterCommitTask get(TaxonomyService taxonomyService,
         CommitRecord commitRecord,
         ConcurrentSkipListSet<Integer> unhandledChanges,
         StampedLock lock) {
      UpdateTaxonomyAfterCommitTask task = new UpdateTaxonomyAfterCommitTask(taxonomyService,
                                                                             commitRecord,
                                                                             unhandledChanges,
                                                                             lock);

      Get.activeTasks()
         .add(task);
      Get.workExecutors()
         .getExecutor()
         .execute(task);
      return task;
   }
}

