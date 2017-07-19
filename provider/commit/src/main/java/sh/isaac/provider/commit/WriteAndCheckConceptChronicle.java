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

import java.lang.ref.WeakReference;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.commit.Alert;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.CheckPhase;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.chronicle.Chronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class WriteAndCheckConceptChronicle.
 *
 * @author kec
 */
public class WriteAndCheckConceptChronicle
        extends Task<Void> {
   /** The cc. */
   private final ConceptChronology cc;

   /** The checkers. */
   private final ConcurrentSkipListSet<ChangeChecker> checkers;

   /** The alert collection. */
   private final ConcurrentSkipListSet<Alert> alertCollection;

   /** The write semaphore. */
   private final Semaphore writeSemaphore;

   /** The change listeners. */
   private final ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners;

   /** The uncommitted tracking. */
   private final BiConsumer<Chronology, Boolean> uncommittedTracking;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new write and check concept chronicle.
    *
    * @param cc the cc
    * @param checkers the checkers
    * @param alertCollection the alert collection
    * @param writeSemaphore the write semaphore
    * @param changeListeners the change listeners
    * @param uncommittedTracking A handle to call back to the caller to notify it that the concept has been
    * written to the ConceptService.  Parameter 1 is the Concept, Parameter two is true to indicate that the
    * change checker is active for this implementation.
    */
   public WriteAndCheckConceptChronicle(ConceptChronology cc,
         ConcurrentSkipListSet<ChangeChecker> checkers,
         ConcurrentSkipListSet<Alert> alertCollection,
         Semaphore writeSemaphore,
         ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners,
         BiConsumer<Chronology, Boolean> uncommittedTracking) {
      this.cc                  = cc;
      this.checkers            = checkers;
      this.alertCollection     = alertCollection;
      this.writeSemaphore      = writeSemaphore;
      this.changeListeners     = changeListeners;
      this.uncommittedTracking = uncommittedTracking;
      updateTitle("Write and check concept");

      // TODO dan disabled this, cause it keeps causing a timing based (randomly occurring) null pointer exception when it tries to read the descriptions
      // for this new concept.  see https://slack-files.com/T04QD7FHW-F0B2PQL87-4d6e82e985
      updateMessage("writing nid " + cc.getNid());  // Get.conceptDescriptionText(cc.getConceptSequence()));
      updateProgress(-1, Long.MAX_VALUE);           // Indeterminate progress
      LookupService.getService(ActiveTasks.class)
                   .get()
                   .add(this);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Call.
    *
    * @return the void
    * @throws Exception the exception
    */
   @Override
   public Void call()
            throws Exception {
      try {
         Get.conceptService()
            .writeConcept(this.cc);
         this.uncommittedTracking.accept(this.cc, true);
         updateProgress(1, 3);

         // TODO dan disabled for the same reason as above.
         updateMessage("checking nid: " + this.cc.getNid());  // Get.conceptDescriptionText(cc.getConceptSequence()));

         if (this.cc.isUncommitted()) {
            this.checkers.stream().forEach((check) -> {
                                     check.check(this.cc, this.alertCollection, CheckPhase.ADD_UNCOMMITTED);
                                  });
         }

         updateProgress(2, 3);

         // TODO dan disabled for the same reason as above.
         updateMessage("notifying nid: " + this.cc.getNid());  // Get.conceptDescriptionText(cc.getConceptSequence()));
         this.changeListeners.forEach((listenerRef) -> {
                                         final ChronologyChangeListener listener = listenerRef.get();

                                         if (listener == null) {
                                            this.changeListeners.remove(listenerRef);
                                         } else {
                                            listener.handleChange(this.cc);
                                         }
                                      });
         updateProgress(3, 3);

         // TODO dan disabled for the same reason as above.
         updateMessage("complete nid: " + this.cc.getNid());  // Get.conceptDescriptionText(cc.getConceptSequence()));
         return null;
      } finally {
         this.writeSemaphore.release();
         LookupService.getService(ActiveTasks.class)
                      .get()
                      .remove(this);
      }
   }
}

