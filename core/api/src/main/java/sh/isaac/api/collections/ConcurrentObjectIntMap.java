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



package sh.isaac.api.collections;

//~--- JDK imports ------------------------------------------------------------

import java.util.OptionalInt;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ObjIntConsumer;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/18/14.
 * @param <T> Type of object in map.
 */
public class ConcurrentObjectIntMap<T> {
   private final ReentrantReadWriteLock rwl        = new ReentrantReadWriteLock();
   private final Lock                   read       = rwl.readLock();
   private final Lock                   write      = rwl.writeLock();
   OpenObjectIntHashMap<T>              backingMap = new OpenObjectIntHashMap<>();

   //~--- methods -------------------------------------------------------------

   public boolean containsKey(T key) {
      try {
         read.lock();
         return backingMap.containsKey(key);
      } finally {
         if (read != null) {
            read.unlock();
         }
      }
   }

   public void forEachPair(ObjIntConsumer<T> consumer) {
      backingMap.forEachPair((T first,
                              int second) -> {
                                consumer.accept(first, second);
                                return true;
                             });
   }

   public boolean put(T key, int value) {
      try {
         write.lock();
         return backingMap.put(key, value);
      } finally {
         if (write != null) {
            write.unlock();
         }
      }
   }

   public int size() {
      return backingMap.size();
   }

   //~--- get methods ---------------------------------------------------------

   public OptionalInt get(T key) {
      try {
         read.lock();

         if (backingMap.containsKey(key)) {
            return OptionalInt.of(backingMap.get(key));
         }

         return OptionalInt.empty();
      } finally {
         if (read != null) {
            read.unlock();
         }
      }
   }
}

