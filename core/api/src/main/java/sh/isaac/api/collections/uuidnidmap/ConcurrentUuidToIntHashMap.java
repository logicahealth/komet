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



package sh.isaac.api.collections.uuidnidmap;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import java.util.concurrent.locks.StampedLock;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 7/25/14.
 */
public class ConcurrentUuidToIntHashMap
        extends UuidToIntHashMap {
   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = -6525403154660005459L;

   //~--- fields --------------------------------------------------------------

   /** The sl. */
   StampedLock sl = new StampedLock();

   //~--- constructors --------------------------------------------------------

   /**
    * Constructs an empty map with default capacity and default load factors.
    */
   public ConcurrentUuidToIntHashMap() {
      super(defaultCapacity);
   }

   /**
    * Constructs an empty map with the specified initial capacity and default load factors.
    *
    * @param initialCapacity the initial capacity of the map.
    * @throws IllegalArgumentException if the initial capacity is less than zero.
    */
   public ConcurrentUuidToIntHashMap(int initialCapacity) {
      super(initialCapacity, defaultMinLoadFactor, defaultMaxLoadFactor);
   }

   /**
    * Constructs an empty map with the specified initial capacity and the specified minimum and maximum load
    * factor.
    *
    * @param initialCapacity the initial capacity.
    * @param minLoadFactor the minimum load factor.
    * @param maxLoadFactor the maximum load factor.
    * @throws IllegalArgumentException if
    *
    * {@code initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) || (maxLoadFactor <= 0.0 ||
    * maxLoadFactor >= 1.0) || (minLoadFactor >= maxLoadFactor)} .
    */
   public ConcurrentUuidToIntHashMap(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
      super(initialCapacity, minLoadFactor, maxLoadFactor);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Contains key.
    *
    * @param key the key
    * @return true, if successful
    */
   @Override
   public boolean containsKey(long[] key) {
      long    stamp       = this.sl.tryOptimisticRead();
      boolean containsKey = indexOfKey(key) >= 0;

      if (!this.sl.validate(stamp)) {
         stamp = this.sl.readLock();

         try {
            containsKey = indexOfKey(key) >= 0;
         } finally {
            this.sl.unlockRead(stamp);
         }
      }

      return containsKey;
   }

   /**
    * Contains key.
    *
    * @param key the key
    * @return true, if successful
    */
   @Override
   public boolean containsKey(UUID key) {
      return this.containsKey(new long[] { key.getMostSignificantBits(), key.getLeastSignificantBits() });
   }

   /**
    * Put.
    *
    * @param key the key
    * @param value the value
    * @return true, if successful
    */
   @Override
   public boolean put(long[] key, int value) {
      throw new UnsupportedOperationException("Use put(long[] key, int value, long stamp) instead.");
   }

   /**
    * Put.
    *
    * @param key the key
    * @param value the value
    * @param stamp the stamp
    * @return true, if successful
    */
   public boolean put(long[] key, int value, long stamp) {
      this.sl.validate(stamp);
      return super.put(key, value);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the distinct.
    *
    * @return the distinct
    */
   public int getDistinct() {
      return this.distinct;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the distinct.
    *
    * @param distinct the new distinct
    */
   public void setDistinct(int distinct) {
      this.distinct = distinct;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the.
    *
    * @param key the key
    * @return the int
    */
   @Override
   public int get(long[] key) {
      long stamp = this.sl.tryOptimisticRead();
      int  value = super.get(key);

      if (!this.sl.validate(stamp)) {
         stamp = this.sl.readLock();

         try {
            value = super.get(key);
         } finally {
            this.sl.unlockRead(stamp);
         }
      }

      return value;
   }

   /**
    * Gets the.
    *
    * @param key the key
    * @return the int
    */
   @Override
   public int get(UUID key) {
      return this.get(new long[] { key.getMostSignificantBits(), key.getLeastSignificantBits() });
   }

   /**
    * Gets the.
    *
    * @param key the key
    * @param stampLong the stamp long
    * @return the int
    */
   public int get(long[] key, long stampLong) {
      return super.get(key);
   }

   /**
    * Gets the.
    *
    * @param key the key
    * @param stampSequence the stamp sequence
    * @return the int
    */
   public int get(UUID key, long stampSequence) {
      return this.get(new long[] { key.getMostSignificantBits(), key.getLeastSignificantBits() }, stampSequence);
   }

   /**
    * Gets the stamped lock.
    *
    * @return the stamped lock
    */
   public StampedLock getStampedLock() {
      return this.sl;
   }

   /**
    * Gets the stats.
    *
    * @return the stats
    */
   public String getStats() {
      return "distinct: " + getDistinct() + " free: " + getFreeEntries() + " utilization: " +
             getDistinct() * 100 / (getDistinct() + getFreeEntries());
   }
}

