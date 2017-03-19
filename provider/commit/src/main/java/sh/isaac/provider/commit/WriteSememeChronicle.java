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
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.progress.ActiveTasks;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class WriteSememeChronicle
        extends Task<Void> {
   private final SememeChronology                                               sc;
   private final Semaphore                                                      writeSemaphore;
   private final ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners;
   private final BiConsumer<ObjectChronology, Boolean>                          uncommittedTracking;

   //~--- constructors --------------------------------------------------------

   /**
    *
    * @param sc
    * @param writeSemaphore
    * @param changeListeners
    * @param uncommittedTracking A handle to call back to the caller to notify it that the sememe has been
    * written to the SememeService.  Parameter 1 is the Sememe, Parameter two is true to indicate that the
    * change checker is active for this implementation.
    */
   public WriteSememeChronicle(SememeChronology sc,
                               Semaphore writeSemaphore,
                               ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners,
                               BiConsumer<ObjectChronology, Boolean> uncommittedTracking) {
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

   @Override
   public Void call()
            throws Exception {
      try {
         Get.sememeService()
            .writeSememe(sc);
         uncommittedTracking.accept(sc, false);
         updateProgress(1, 2);
         updateMessage("notifying: " + sc.getAssemblageSequence());
         changeListeners.forEach((listenerRef) -> {
                                    ChronologyChangeListener listener = listenerRef.get();

                                    if (listener == null) {
                                       changeListeners.remove(listenerRef);
                                    } else {
                                       listener.handleChange(sc);
                                    }
                                 });
         updateProgress(2, 2);
         updateMessage("complete: " + sc.getSememeType() + " " + sc.getSememeSequence());
         return null;
      } finally {
         writeSemaphore.release();
         LookupService.getService(ActiveTasks.class)
                      .get()
                      .remove(this);
      }
   }
}

