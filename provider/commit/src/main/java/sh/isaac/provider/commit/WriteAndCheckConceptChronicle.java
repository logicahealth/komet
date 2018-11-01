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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.CheckAndWriteTask;
import sh.isaac.api.commit.CheckPhase;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.progress.ActiveTasks;

//~--- classes ----------------------------------------------------------------

/**
 * The Class WriteAndCheckConceptChronicle.
 *
 * @author kec
 */
public class WriteAndCheckConceptChronicle
        extends CheckAndWriteTask {
   /** The cc. */
   private ConceptChronology cc;

   /** The checkers. */
   private final ConcurrentSkipListSet<ChangeChecker> checkers;

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
    * @param writeSemaphore the write semaphore
    * @param changeListeners the change listeners
    * @param uncommittedTracking A handle to call back to the caller to notify it that the concept has been
    * written to the ConceptService.  Parameter 1 is the Concept, Parameter two is true to indicate that the
    * change checker is active for this implementation.
    */
   public WriteAndCheckConceptChronicle(ConceptChronology cc,
         ConcurrentSkipListSet<ChangeChecker> checkers,
         Semaphore writeSemaphore,
         ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners,
         BiConsumer<Chronology, Boolean> uncommittedTracking) {
      this.cc                  = cc;
      this.checkers            = checkers;
      this.writeSemaphore      = writeSemaphore;
      this.changeListeners     = changeListeners;
      this.uncommittedTracking = uncommittedTracking;
      updateTitle("Write and check concept");

      updateMessage("writing " + cc.getNid());  //It is NOT safe to try to print a concept description here, as the semantic might be 
      //in the middle of being written on another thread, and some of the data store providers don't cleanly handle the read back of a partially 
      //written item.
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
         
         updateProgress(1, 4);
         updateMessage("checking: " + this.cc.getNid() + " against " + checkers.size() + " change checkers");

         if (this.cc.getCommitState() == CommitStates.UNCOMMITTED) {
            AtomicBoolean fail = new AtomicBoolean(false);
            StringBuilder sb = new StringBuilder();
            this.checkers.stream().forEach((check) -> {
               AlertObject ao = check.check(this.cc, CheckPhase.ADD_UNCOMMITTED);
               if (ao.getAlertType() == AlertType.ERROR || ao.getAlertType() == AlertType.WARNING) {
                  sb.append(System.lineSeparator());
                     sb.append(ao.toString());
                     if (ao.getAlertType().preventsCheckerPass()) {
                        fail.set(true);
                     }
                  }
            });
            
            if (fail.get()) {
               throw new RuntimeException(sb.toString());
            }
            else if (sb.length() > 0) {
               LOG.warn("Alerts during WriteAndCheck: " + sb.toString());
            }
         }
         
         updateProgress(2, 4);
         updateMessage("writing: " + " " + this.cc.getNid());
         
         Get.conceptService()
            .writeConcept(this.cc);
         // get any updates that may have occured during merge write...
         this.cc = Get.conceptService().getConceptChronology(this.cc.getNid());
         this.uncommittedTracking.accept(this.cc, true);

         updateProgress(3, 4);
         updateMessage("notifying: " + cc.getNid());  
         this.changeListeners.forEach((listenerRef) -> {
            final ChronologyChangeListener listener = listenerRef.get();

            if (listener == null) {
               this.changeListeners.remove(listenerRef);
            } else {
               listener.handleChange(this.cc);
            }
         });
         updateProgress(4, 4);

         updateMessage("Write and check complete: " + cc.getNid()); 
         return null;
      } finally {
         this.writeSemaphore.release();
         LookupService.getService(ActiveTasks.class)
                      .get()
                      .remove(this);
      }
   }
}

