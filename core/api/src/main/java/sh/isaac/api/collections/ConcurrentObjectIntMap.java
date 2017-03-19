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
   
   /** The rwl. */
   private final ReentrantReadWriteLock rwl        = new ReentrantReadWriteLock();
   
   /** The read. */
   private final Lock                   read       = this.rwl.readLock();
   
   /** The write. */
   private final Lock                   write      = this.rwl.writeLock();
   
   /** The backing map. */
   OpenObjectIntHashMap<T>              backingMap = new OpenObjectIntHashMap<>();

   //~--- methods -------------------------------------------------------------

   /**
    * Contains key.
    *
    * @param key the key
    * @return true, if successful
    */
   public boolean containsKey(T key) {
      try {
         this.read.lock();
         return this.backingMap.containsKey(key);
      } finally {
         if (this.read != null) {
            this.read.unlock();
         }
      }
   }

   /**
    * For each pair.
    *
    * @param consumer the consumer
    */
   public void forEachPair(ObjIntConsumer<T> consumer) {
      this.backingMap.forEachPair((T first,
                              int second) -> {
                                consumer.accept(first, second);
                                return true;
                             });
   }

   /**
    * Put.
    *
    * @param key the key
    * @param value the value
    * @return true, if successful
    */
   public boolean put(T key, int value) {
      try {
         this.write.lock();
         return this.backingMap.put(key, value);
      } finally {
         if (this.write != null) {
            this.write.unlock();
         }
      }
   }

   /**
    * Size.
    *
    * @return the int
    */
   public int size() {
      return this.backingMap.size();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the.
    *
    * @param key the key
    * @return the optional int
    */
   public OptionalInt get(T key) {
      try {
         this.read.lock();

         if (this.backingMap.containsKey(key)) {
            return OptionalInt.of(this.backingMap.get(key));
         }

         return OptionalInt.empty();
      } finally {
         if (this.read != null) {
            this.read.unlock();
         }
      }
   }
}

