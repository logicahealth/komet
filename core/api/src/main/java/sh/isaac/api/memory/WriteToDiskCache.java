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



package sh.isaac.api.memory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 4/10/15.
 */
public class WriteToDiskCache {
   private static final int                             WRITE_INTERVAL_IN_MS = 15000;
   static ConcurrentSkipListSet<MemoryManagedReference> cacheSet             = new ConcurrentSkipListSet<>();
   static final Thread                                  writerThread;

   //~--- static initializers -------------------------------------------------

   static {
      writerThread = new Thread(new WriteToDiskRunnable(), "WriteToDiskCache thread");
      writerThread.setDaemon(true);
   }

   //~--- methods -------------------------------------------------------------

   public static void addToCache(MemoryManagedReference newRef) {
      cacheSet.add(newRef);
   }

   public static void flushAndClearCache() {
      cacheSet.stream().forEach((memoryManagedReference) -> {
                          cacheSet.remove(memoryManagedReference);

                          while (memoryManagedReference.hasUnwrittenUpdate()) {
                             memoryManagedReference.clear();
                             memoryManagedReference.write();
                          }
                       });
   }

   //~--- inner classes -------------------------------------------------------

   public static class WriteToDiskRunnable
            implements Runnable {
      @Override
      public void run() {
         while (true) {
            Optional<MemoryManagedReference> optionalReference = cacheSet.stream()
                                                                         .filter((memoryManagedReference) -> {
                     if (memoryManagedReference.get() == null) {
                        cacheSet.remove(memoryManagedReference);
                        return false;
                     }

                     return memoryManagedReference.hasUnwrittenUpdate();
                  })
                                                                         .max((o1, o2) -> {
                     if (o1.msSinceLastUnwrittenUpdate() > o2.msSinceLastUnwrittenUpdate()) {
                        return 1;
                     }

                     if (o1.msSinceLastUnwrittenUpdate() < o2.msSinceLastUnwrittenUpdate()) {
                        return -1;
                     }

                     return 0;
                  });
            boolean written = false;

            if (optionalReference.isPresent()) {
               written = true;

               MemoryManagedReference ref = (MemoryManagedReference) optionalReference.get();

               if (ref.msSinceLastUnwrittenUpdate() > WRITE_INTERVAL_IN_MS) {
                  ref.write();
               }
            }

            if (!written) {
               try {
                  writerThread.wait(WRITE_INTERVAL_IN_MS);
               } catch (InterruptedException e) {
                  // continue work
               }
            }
         }
      }
   }
}

