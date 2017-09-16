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
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.chronicle.Chronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class WriteSememeChronicle.
 *
 * @author kec
 */
public class WriteSememeChronicle
        extends Task<Void> {
   /** The sc. */
   private final SememeChronology sc;

   /** The write semaphore. */
   private final Semaphore writeSemaphore;

   /** The change listeners. */
   private final ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners;

   /** The uncommitted tracking. */
   private final BiConsumer<Chronology, Boolean> uncommittedTracking;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new write sememe chronicle.
    *
    * @param sc the sc
    * @param writeSemaphore the write semaphore
    * @param changeListeners the change listeners
    * @param uncommittedTracking A handle to call back to the caller to notify it that the sememe has been
 written to the AssemblageService.  Parameter 1 is the Sememe, Parameter two is true to indicate that the
 change checker is active for this implementation.
    */
   public WriteSememeChronicle(SememeChronology sc,
                               Semaphore writeSemaphore,
                               ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners,
                               BiConsumer<Chronology, Boolean> uncommittedTracking) {
      this.sc                  = sc;
      this.writeSemaphore      = writeSemaphore;
      this.changeListeners     = changeListeners;
      this.uncommittedTracking = uncommittedTracking;
      updateTitle("Write and notify sememe change");
      updateMessage("write: " + sc.getSememeType() + " " + sc.getSememeSequence());
      updateProgress(-1, Long.MAX_VALUE);  // Indeterminate progress
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
         Get.assemblageService()
            .writeSememe(this.sc);
         this.uncommittedTracking.accept(this.sc, false);
         updateProgress(1, 2);
         updateMessage("notifying: " + this.sc.getAssemblageSequence());
         this.changeListeners.forEach((listenerRef) -> {
            try {
               final ChronologyChangeListener listener = listenerRef.get();
               
               if (listener == null) {
                  this.changeListeners.remove(listenerRef);
               } else {
                  listener.handleChange(this.sc);
               }
            } catch (Throwable e) {
               e.printStackTrace();
            }
                                      });
         updateProgress(2, 2);
         updateMessage("complete: " + this.sc.getSememeType() + " " + this.sc.getSememeSequence());
         return null;
      } finally {
         this.writeSemaphore.release();
         LookupService.getService(ActiveTasks.class)
                      .get()
                      .remove(this);
      }
   }
}

