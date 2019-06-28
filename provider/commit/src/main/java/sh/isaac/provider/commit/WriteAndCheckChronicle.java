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
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import sh.isaac.api.Get;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.CheckAndWriteTask;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.transaction.Transaction;

//~--- classes ----------------------------------------------------------------

/**
 * The Class WriteAndCheckChronicle.
 *
 * @author kec
 */
public class WriteAndCheckChronicle
        extends CheckAndWriteTask {
   /** The chronology. */
   private Chronology chronology;

   /** The checkers. */
   private final ConcurrentSkipListSet<ChangeChecker> checkers;

   /** The write semaphore. */
   private final Semaphore writeSemaphore;

   /** The change listeners. */
   private final ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners;

   /** The uncommitted tracking. */
   private final BiConsumer<Chronology, Boolean> uncommittedTracking;

   private final Transaction transaction;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new write and check concept chronicle.
    *
    * @param chronology the chronology
    * @param checkers the checkers
    * @param writeSemaphore the write semaphore
    * @param changeListeners the change listeners
    * @param uncommittedTracking A handle to call back to the caller to notify it that the concept has been
    * written to the ConceptService.  Parameter 1 is the Concept, Parameter two is true to indicate that the
    * change checker is active for this implementation.
    */
   public WriteAndCheckChronicle(Transaction transaction,
                                 Chronology chronology,
                                 ConcurrentSkipListSet<ChangeChecker> checkers,
                                 Semaphore writeSemaphore,
                                 ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners,
                                 BiConsumer<Chronology, Boolean> uncommittedTracking) {
      this.transaction = transaction;
      this.chronology = chronology;
      this.checkers            = checkers;
      this.writeSemaphore      = writeSemaphore;
      this.changeListeners     = changeListeners;
      this.uncommittedTracking = uncommittedTracking;
      updateTitle("Check");

      updateMessage("writing " + chronology.getNid());  //It is NOT safe to try to print a concept description here, as the semantic might be
      //in the middle of being written on another thread, and some of the data store providers don't cleanly handle the read back of a partially 
      //written item.
      updateProgress(-1, Long.MAX_VALUE);           // Indeterminate progress
      Get.activeTasks().add(this);
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
         updateMessage("checking: " + this.chronology.getNid() + " against " + checkers.size() + " change checkers");

         if (this.chronology.getCommitState() == CommitStates.UNCOMMITTED) {
            AtomicBoolean fail = new AtomicBoolean(false);
            StringBuilder sb = new StringBuilder();
            this.checkers.stream().forEach((check) -> {
               for (Version v: chronology.getVersionList()) {
                  if (v.isUncommitted() && transaction.containsTransactionId(Get.stampService().getTransactionIdForStamp(v.getStampSequence()))) {
                     Optional<AlertObject> optionalAlertObject = check.check(v, transaction);
                     if (optionalAlertObject.isPresent() &&
                             (optionalAlertObject.get().getAlertType() == AlertType.ERROR || optionalAlertObject.get().getAlertType() == AlertType.WARNING)) {
                        AlertObject alertObject = optionalAlertObject.get();
                        sb.append(System.lineSeparator());
                        sb.append(alertObject.toString());
                        if (alertObject.failCommit()) {
                           fail.set(true);
                        }
                     }
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
         updateMessage("writing: " + " " + this.chronology.getNid());

         Get.identifiedObjectService().putChronologyData(this.chronology);
         // get any updates that may have occured during merge write...
         this.chronology = Get.identifiedObjectService().getChronology(this.chronology.getNid()).get();
         this.uncommittedTracking.accept(this.chronology, true);

         updateProgress(3, 4);
         updateMessage("notifying: " + chronology.getNid());
         this.changeListeners.forEach((listenerRef) -> {
            final ChronologyChangeListener listener = listenerRef.get();

            if (listener == null) {
               this.changeListeners.remove(listenerRef);
            } else {
               listener.handleChange(this.chronology);
            }
         });
         updateProgress(4, 4);

         updateMessage("Write and check complete: " + chronology.getNid());
         return null;
      } finally {
         this.writeSemaphore.release();
         Get.activeTasks().remove(this);
      }
   }
}

