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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api.collections;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.HashFunctions;
import org.apache.mahout.math.map.OpenIntIntHashMap;

//~--- classes ----------------------------------------------------------------

/**
 * The Class NativeIntIntHashMap.
 *
 * @author kec
 */
public class NativeIntIntHashMap
        extends OpenIntIntHashMap {
   
   /** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

/**
 * Instantiates a new native int int hash map.
 */
public NativeIntIntHashMap() {}

   /**
    * Instantiates a new native int int hash map.
    *
    * @param initialCapacity the initial capacity
    */
   public NativeIntIntHashMap(int initialCapacity) {
      super(initialCapacity);
   }

   /**
    * Instantiates a new native int int hash map.
    *
    * @param initialCapacity the initial capacity
    * @param minLoadFactor the min load factor
    * @param maxLoadFactor the max load factor
    */
   public NativeIntIntHashMap(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
      super(initialCapacity, minLoadFactor, maxLoadFactor);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Index of insertion.
    *
    * @param key the key
    * @return the int
    */
   @Override
   protected int indexOfInsertion(int key) {
      return indexOfInsertion(key, this.table, this.state);
   }

   /**
    * Index of insertion.
    *
    * @param key the key
    * @param newTable the new table
    * @param newState the new state
    * @return the int
    */
   protected int indexOfInsertion(int key, int[] newTable, byte[] newState) {
      final int length = newTable.length;
      final int hash   = HashFunctions.hash(key) & 0x7FFFFFFF;
      int       i      = hash % length;
      int decrement = hash %
                      (length - 2);  // double hashing, see http://www.eece.unm.edu/faculty/heileman/hash/node4.html

      // int decrement = (hash / length) % length;
      if (decrement == 0) {
         decrement = 1;
      }

      // stop if we find a removed or free slot, or if we find the key itself
      // do NOT skip over removed slots (yes, open addressing is like that...)
      while ((newState[i] == FULL) && (newTable[i] != key)) {
         i -= decrement;

         // hashCollisions++;
         if (i < 0) {
            i += length;
         }
      }

      if (newState[i] == REMOVED) {
         // stop if we find a free slot, or if we find the key itself.
         // do skip over removed slots (yes, open addressing is like that...)
         // assertion: there is at least one FREE slot.
         final int j = i;

         while ((newState[i] != FREE) && ((newState[i] == REMOVED) || (newTable[i] != key))) {
            i -= decrement;

            // hashCollisions++;
            if (i < 0) {
               i += length;
            }
         }

         if (newState[i] == FREE) {
            i = j;
         }
      }

      if (newState[i] == FULL) {
         // key already contained at slot i.
         // return a negative number identifying the slot.
         return -i - 1;
      }

      // not already contained, should be inserted at slot i.
      // return a number >= 0 identifying the slot.
      return i;
   }

   /**
    * Reimplement rehash so that tables are not exposed until after copying is
    * complete.
    *
    * @param newCapacity the new capacity
    */
   @Override
   protected void rehash(int newCapacity) {
      final int oldCapacity = this.table.length;

      // if (oldCapacity == newCapacity) return;
      final int[]  oldTable  = this.table;
      final int[]  oldValues = this.values;
      final byte[] oldState  = this.state;
      final int[]  newTable  = new int[newCapacity];
      final int[]  newValues = new int[newCapacity];
      final byte[] newState  = new byte[newCapacity];

      for (int i = oldCapacity; i-- > 0; ) {
         if (oldState[i] == FULL) {
            final int element = oldTable[i];
            final int index   = indexOfInsertion(element, newTable, newState);

            newTable[index]  = element;
            newValues[index] = oldValues[i];
            newState[index]  = FULL;
         }
      }

      updateData(newTable, newValues, newState, newCapacity);
   }

   /**
    * Update data.
    *
    * @param newTable the new table
    * @param newValues the new values
    * @param newState the new state
    * @param newCapacity the new capacity
    */
   protected void updateData(int[] newTable, int[] newValues, byte[] newState, int newCapacity) {
      this.values        = newValues;
      this.state         = newState;
      this.table         = newTable;
      this.lowWaterMark  = chooseLowWaterMark(newCapacity, this.minLoadFactor);
      this.highWaterMark = chooseHighWaterMark(newCapacity, this.maxLoadFactor);
      this.freeEntries   = newCapacity - this.distinct;  // delta
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
    * Gets the free entries.
    *
    * @return the free entries
    */
   public int getFreeEntries() {
      return this.freeEntries;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the free entries.
    *
    * @param freeEntries the new free entries
    */
   public void setFreeEntries(int freeEntries) {
      this.freeEntries = freeEntries;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the state.
    *
    * @return the state
    */
   public byte[] getState() {
      return this.state;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the state.
    *
    * @param state the new state
    */
   public void setState(byte[] state) {
      this.state = state;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the table.
    *
    * @return the table
    */
   public int[] getTable() {
      return this.table;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the table.
    *
    * @param table the new table
    */
   public void setTable(int[] table) {
      this.table = table;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the values.
    *
    * @return the values
    */
   public int[] getValues() {
      return this.values;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the values.
    *
    * @param values the new values
    */
   public void setValues(int[] values) {
      this.values = values;
   }
}

