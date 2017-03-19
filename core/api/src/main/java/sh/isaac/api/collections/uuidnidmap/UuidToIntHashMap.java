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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.function.DoubleProcedure;
import org.apache.mahout.math.list.ByteArrayList;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.HashFunctions;
import org.apache.mahout.math.map.PrimeFinder;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class UuidToIntHashMap
        extends AbstractUuidToIntHashMap
         implements UuidToIntMap {
   /**
    *    
    */
   private static final long   serialVersionUID = -3621266181773866347L;
   protected static final byte FREE             = 0;
   protected static final byte FULL             = 1;
   protected static final byte REMOVED          = 2;

   //~--- fields --------------------------------------------------------------

   /**
    * The hash table keys.
    *
    * @serial
    */
   protected long table[];

   /**
    * The hash table values.
    *
    * @serial
    */
   protected int values[];

   /**
    * The state of each hash table entry (FREE, FULL, REMOVED).
    *
    * @serial
    */
   protected byte state[];

   /**
    * The number of table entries in state==FREE.
    *
    * @serial
    */
   protected int freeEntries;

   //~--- constructors --------------------------------------------------------

   /**
    * Constructs an empty map with default capacity and default load factors.
    */
   public UuidToIntHashMap() {
      this(defaultCapacity);
   }

   /**
    * Constructs an empty map with the specified initial capacity and default load factors.
    *
    * @param initialCapacity the initial capacity of the map.
    * @throws IllegalArgumentException if the initial capacity is less than zero.
    */
   public UuidToIntHashMap(int initialCapacity) {
      this(initialCapacity, defaultMinLoadFactor, defaultMaxLoadFactor);
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
   public UuidToIntHashMap(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
      setUp(initialCapacity, minLoadFactor, maxLoadFactor);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Removes all (key,value) associations from the receiver. Implicitly calls {@code trimToSize()}.
    */
   @Override
   public void clear() {
      new ByteArrayList(this.state).fillFromToWith(0, this.state.length - 1, FREE);

      // new UuidArrayList(values).fillFromToWith(0, state.length-1, 0); //
      // delta
      this.distinct    = 0;
      this.freeEntries = this.state.length;  // delta
      trimToSize();
   }

   /**
    * Returns a deep copy of the receiver.
    *
    * @return a deep copy of the receiver.
    */
   @Override
   public Object clone() {
      final UuidToIntHashMap copy = (UuidToIntHashMap) super.clone();

      copy.table  = copy.table.clone();
      copy.values = copy.values.clone();
      copy.state  = copy.state.clone();
      return copy;
   }

   /**
    * Returns {@code true} if the receiver contains the specified key.
    *
    * @param key
    * @return {@code true} if the receiver contains the specified key.
    */
   @Override
   public boolean containsKey(long[] key) {
      return indexOfKey(key) >= 0;
   }

   @Override
   public boolean containsKey(UUID key) {
      return indexOfKey(key) >= 0;
   }

   /**
    * Returns {@code true} if the receiver contains the specified value.
    *
    * @param value
    * @return {@code true} if the receiver contains the specified value.
    */
   @Override
   public boolean containsValue(int value) {
      return indexOfValue(value) >= 0;
   }

   /**
    * Ensures that the receiver can hold at least the specified number of associations without needing to
    * allocate new internal memory. If necessary, allocates new internal memory and increases the capacity of
    * the receiver. <p> This method never need be called; it is for performance tuning only. Calling this
    * method before {@code put()}ing a large number of associations boosts performance, because the receiver
    * will grow only once instead of potentially many times and hash collisions get less probable.
    *
    * @param minCapacity the desired minimum capacity.
    */
   @Override
   public void ensureCapacity(int minCapacity) {
      if (this.state.length < minCapacity) {
         final int newCapacity = nextPrime(minCapacity);

         rehash(newCapacity);
      }
   }

   @Override
   public boolean forEachKey(UuidProcedure procedure) {
      for (int i = this.state.length; i-- > 0; ) {
         if (this.state[i] == FULL) {
            final long[] key = new long[2];

            key[0] = this.table[i * 2];
            key[1] = this.table[i * 2 + 1];

            if (!procedure.apply(key)) {
               ;
            }

            return false;
         }
      }

      return true;
   }

   /**
    * Applies a procedure to each (key,value) pair of the receiver, if any. Iteration order is guaranteed to
    * be <i>identical</i> to the order used by method {@link #forEachKey(DoubleProcedure)}.
    *
    * @param procedure the procedure to be applied. Stops iteration if the procedure returns {@code false},
    * otherwise continues.
    * @return {@code false} if the procedure stopped before all keys where iterated over, {@code true}
    * otherwise.
    */
   @Override
   public boolean forEachPair(final UuidIntProcedure procedure) {
      for (int i = this.state.length; i-- > 0; ) {
         if (this.state[i] == FULL) {
            final long[] key = new long[2];

            key[0] = this.table[i * 2];
            key[1] = this.table[i * 2 + 1];

            if (!procedure.apply(key, this.values[i])) {
               return false;
            }
         }
      }

      return true;
   }

   /**
    * Returns the first key the given value is associated with. It is often a good idea to first check with
    * {@link #containsValue(int)} whether there exists an association from a key to this value. Search order
    * is guaranteed to be <i>identical</i> to the order used by method {@link #forEachKey(DoubleProcedure)}.
    *
    * @param value the value to search for.
    * @return the first key for which holds {@code get(key) == value}; returns {@code Double.NaN} if no
    * such key exists.
    */
   @Override
   public long[] keyOf(int value) {
      // returns the first key found; there may be more matching keys,
      // however.
      final int i = indexOfValue(value);

      if (i < 0) {
         return null;
      }

      final long[] uuid = new long[2];
      final int    msb  = i * 2;
      final int    lsb  = msb + 1;

      uuid[0] = this.table[msb];
      uuid[1] = this.table[lsb];
      return uuid;
   }

   /**
    * Fills all keys contained in the receiver into the specified list. Fills the list, starting at index 0.
    * After this call returns the specified list has a new size that equals {@code this.size()}. Iteration
    * order is guaranteed to be <i>identical</i> to the order used by method
    * {@link #forEachKey(DoubleProcedure)}. <p> This method can be used to iterate over the keys of the
    * receiver.
    *
    * @param list the list to be filled, can have any size.
    */
   @Override
   public void keys(UuidArrayList list) {
      list.setSize(this.distinct);

      final long[] elements = list.elements();
      final long[] tab      = this.table;
      final byte[] stat     = this.state;
      int    j        = 0;

      for (int i = stat.length; i-- > 0; ) {
         if (stat[i] == FULL) {
            final int iMsb = i * 2;
            final int iLsb = iMsb + 1;
            final int jMsb = j * 2;
            final int jLsb = jMsb + 1;

            elements[jMsb] = tab[iMsb];
            elements[jLsb] = tab[iLsb];
            j++;
         }
      }
   }

   public List<UUID> keysOf(int value) {
      final List<Integer> indexes = indexesOfValue(value);
      final List<UUID>    keys    = new ArrayList<>(indexes.size());

      indexes.stream()
             .map((index) -> index * 2)
             .forEach((msb) -> {
                         final int lsb = msb + 1;

                         keys.add(new UUID(this.table[msb], this.table[lsb]));
                      });
      return keys;
   }

   /**
    * Associates the given key with the given value. Replaces any old {@code (key,someOtherValue)}
    * association, if existing.
    *
    * @param key the key the value shall be associated with.
    * @param value the value to be associated.
    * @return {@code true} if the receiver did not already contain such a key; {@code false} if the
    * receiver did already contain such a key - the new value has now replaced the formerly associated value.
    */
   @Override
   public boolean put(long[] key, int value) {
      return privatePut(key, value);
   }

   /**
    * Removes the given key with its associated element from the receiver, if present.
    *
    * @param key the key to be removed from the receiver.
    * @return {@code true} if the receiver contained the specified key, {@code false} otherwise.
    */
   @Override
   public boolean removeKey(long[] key) {
      final int i = indexOfKey(key);

      if (i < 0) {
         return false;  // key not contained
      }

      this.state[i] = REMOVED;

      // this.values[i]=0; // delta
      this.distinct--;

      if (this.distinct < this.lowWaterMark) {
         final int newCapacity = chooseShrinkCapacity(this.distinct, this.minLoadFactor, this.maxLoadFactor);

         /*
          * if (state.length != newCapacity) {
          * System.out.print("shrink rehashing ");
          * System.out.println("at distinct="
          * +distinct+", capacity="+state.length
          * +" to newCapacity="+newCapacity+" ..."); }
          */
         rehash(newCapacity);
      }

      return true;
   }

   /**
    * Trims the capacity of the receiver to be the receiver's current size. Releases any superfluous internal
    * memory. An application can use this operation to minimize the storage of the receiver.
    */
   @Override
   public void trimToSize() {
      // * 1.2 because open addressing's performance exponentially degrades
      // beyond that point
      // so that even rehashing the table can take very long
      final int newCapacity = nextPrime((int) (1 + 1.2 * size()));

      if (this.state.length > newCapacity) {
         rehash(newCapacity);
      }
   }

   /**
    * Fills all values contained in the receiver into the specified list. Fills the list, starting at index
    * 0. After this call returns the specified list has a new size that equals {@code this.size()}.
    * Iteration order is guaranteed to be <i>identical</i> to the order used by method
    * {@link #forEachKey(org.ihtsdo.otf.uuidnidmap.UuidProcedure)}. <p> This method can be used to iterate over the values of the
    * receiver.
    *
    * @param list the list to be filled, can have any size.
    */
   @Override
   public void values(IntArrayList list) {
      list.setSize(this.distinct);

      final int[]  elements = list.elements();
      final int[]  val      = this.values;
      final byte[] stat     = this.state;
      int    j        = 0;

      for (int i = stat.length; i-- > 0; ) {
         if (stat[i] == FULL) {
            elements[j++] = val[i];
         }
      }
   }

   /**
    * @param key the key to be added to the receiver.
    * @return the index where the key would need to be inserted, if it is not already contained. Returns
    * -index-1 if the key is already contained at slot index. Therefore, if the returned index < 0, then it
    * is already contained at slot -index-1. If the returned index >= 0, then it is NOT already contained and
    * should be inserted at slot index.
    */
   protected int indexOfInsertion(long[] key) {
      final long tab[]     = this.table;
      final byte stat[]    = this.state;
      final int  length    = this.state.length;
      final int  hash      = HashFunctions.hash(key[0] + key[1]) & 0x7FFFFFFF;
      int        i         = hash % length;
      int        decrement = hash % (length - 2);  // double hashing, see

      // http://www.eece.unm.edu/faculty/heileman/hash/node4.html
      // int decrement = (hash / length) % length;
      if (decrement == 0) {
         decrement = 1;
      }

      // stop if we find a removed or free slot, or if we find the key itself
      // do NOT skip over removed slots (yes, open addressing is like that...)
      while ((stat[i] == FULL) && ((tab[i * 2] != key[0]) || (tab[i * 2 + 1] != key[1]))) {
         i -= decrement;

         // hashCollisions++;
         if (i < 0) {
            i += length;
         }
      }

      if (stat[i] == REMOVED) {
         // stop if we find a free slot, or if we find the key itself.
         // do skip over removed slots (yes, open addressing is like that...)
         // assertion: there is at least one FREE slot.
         final int j = i;

         while ((stat[i] != FREE) && ((stat[i] == REMOVED) || ((tab[i * 2] != key[0]) || (tab[i * 2 + 1] != key[1])))) {
            i -= decrement;

            // hashCollisions++;
            if (i < 0) {
               i += length;
            }
         }

         if (stat[i] == FREE) {
            i = j;
         }
      }

      if (stat[i] == FULL) {
         // key already contained at slot i.
         // return a negative number identifying the slot.
         return -i - 1;
      }

      // not already contained, should be inserted at slot i.
      // return a number >= 0 identifying the slot.
      return i;
   }

   protected int indexOfInsertionForRehash(long[] key, long[] tab, byte[] stat) {
      final int length    = stat.length;
      final int hash      = HashFunctions.hash(key[0] + key[1]) & 0x7FFFFFFF;
      int       i         = hash % length;
      int       decrement = hash % (length - 2);  // double hashing, see

      // http://www.eece.unm.edu/faculty/heileman/hash/node4.html
      // int decrement = (hash / length) % length;
      if (decrement == 0) {
         decrement = 1;
      }

      // stop if we find a removed or free slot, or if we find the key itself
      // do NOT skip over removed slots (yes, open addressing is like that...)
      while ((stat[i] == FULL) && ((tab[i * 2] != key[0]) || (tab[i * 2 + 1] != key[1]))) {
         i -= decrement;

         // hashCollisions++;
         if (i < 0) {
            i += length;
         }
      }

      if (stat[i] == REMOVED) {
         // stop if we find a free slot, or if we find the key itself.
         // do skip over removed slots (yes, open addressing is like that...)
         // assertion: there is at least one FREE slot.
         final int j = i;

         while ((stat[i] != FREE) && ((stat[i] == REMOVED) || ((tab[i * 2] != key[0]) || (tab[i * 2 + 1] != key[1])))) {
            i -= decrement;

            // hashCollisions++;
            if (i < 0) {
               i += length;
            }
         }

         if (stat[i] == FREE) {
            i = j;
         }
      }

      if (stat[i] == FULL) {
         // key already contained at slot i.
         // return a negative number identifying the slot.
         return -i - 1;
      }

      // not already contained, should be inserted at slot i.
      // return a number >= 0 identifying the slot.
      return i;
   }

   /**
    * @param key the key to be searched in the receiver.
    * @return the index where the key is contained in the receiver, returns -1 if the key was not found.
    */
   protected int indexOfKey(long[] key) {
      final long tab[]     = this.table;
      final byte stat[]    = this.state;
      final int  length    = stat.length;
      final int  hash      = HashFunctions.hash(key[0] + key[1]) & 0x7FFFFFFF;
      int        i         = hash % length;
      int        decrement = hash % (length - 2);  // double hashing, see

      // http://www.eece.unm.edu/faculty/heileman/hash/node4.html
      // int decrement = (hash / length) % length;
      if (decrement == 0) {
         decrement = 1;
      }

      // stop if we find a free slot, or if we find the key itself.
      // do skip over removed slots (yes, open addressing is like that...)
      while ((stat[i] != FREE) && ((stat[i] == REMOVED) || ((tab[i * 2] != key[0]) || (tab[i * 2 + 1] != key[1])))) {
         i -= decrement;

         // hashCollisions++;
         if (i < 0) {
            i += length;
         }
      }

      if (stat[i] == FREE) {
         return -1;  // not found
      }

      return i;  // found, return index where key is contained
   }

   protected int indexOfKey(UUID key) {
      return indexOfKey(new long[] { key.getMostSignificantBits(), key.getLeastSignificantBits() });
   }

   /**
    * @param value the value to be searched in the receiver.
    * @return the index where the value is contained in the receiver, returns -1 if the value was not found.
    */
   protected int indexOfValue(int value) {
      final int  val[]  = this.values;
      final byte stat[] = this.state;

      for (int i = stat.length; --i >= 0; ) {
         if ((stat[i] == FULL) && (val[i] == value)) {
            return i;
         }
      }

      return -1;  // not found
   }

   protected List<Integer> indexesOfValue(int value) {
      final List<Integer> indexes = new ArrayList<>();
      final int     val[]   = this.values;
      final byte    stat[]  = this.state;

      for (int i = stat.length; --i >= 0; ) {
         if ((stat[i] == FULL) && (val[i] == value)) {
            indexes.add(i);
         }
      }

      return indexes;  // not found
   }

   protected boolean privatePut(long[] key, int value) {
      int i = indexOfInsertion(key);

      if (i < 0) {  // already contained
         i              = -i - 1;
         this.values[i] = value;
         return false;
      }

      if (this.distinct > this.highWaterMark) {
         final int newCapacity = chooseGrowCapacity(this.distinct + 1, this.minLoadFactor, this.maxLoadFactor);

         rehash(newCapacity);
         return privatePut(key, value);
      }

      final int msb = i * 2;

      this.table[msb]     = key[0];
      this.table[msb + 1] = key[1];
      this.values[i]      = value;

      if (this.state[i] == FREE) {
         this.freeEntries--;
      }

      this.state[i] = FULL;
      this.distinct++;

      if (this.freeEntries < 1) {  // delta
         final int newCapacity = chooseGrowCapacity(this.distinct + 1, this.minLoadFactor, this.maxLoadFactor);

         rehash(newCapacity);
      }

      return true;
   }

   /**
    * Rehashes the contents of the receiver into a new table with a smaller or larger capacity. This method
    * is called automatically when the number of keys in the receiver exceeds the high water mark or falls
    * below the low water mark.
    * @param newCapacity
    */
   protected void rehash(int newCapacity) {
      final int oldCapacity = this.state.length;

      if (oldCapacity == newCapacity) {
         return;
      }

      final long oldTable[]  = this.table;
      final int  oldValues[] = this.values;
      final byte oldState[]  = this.state;
      final long newTable[]  = new long[newCapacity * 2 + 1];
      final int  newValues[] = new int[newCapacity];
      final byte newState[]  = new byte[newCapacity];

      for (int i = oldCapacity; i-- > 0; ) {
         final long[] element = new long[2];

         if (oldState[i] == FULL) {
            element[0] = oldTable[i * 2];
            element[1] = oldTable[i * 2 + 1];

            final int index = indexOfInsertionForRehash(element, newTable, newState);

            newTable[index * 2]     = element[0];
            newTable[index * 2 + 1] = element[1];
            newValues[index]        = oldValues[i];
            newState[index]         = FULL;
         }
      }

      updateAfterRehash(newCapacity, newTable, newValues, newState);
   }

   protected void updateAfterRehash(int newCapacity, long[] newTable, int[] newValues, byte[] newState) {
      this.lowWaterMark  = chooseLowWaterMark(newCapacity, this.minLoadFactor);
      this.highWaterMark = chooseHighWaterMark(newCapacity, this.maxLoadFactor);
      this.table         = newTable;
      this.values        = newValues;
      this.state         = newState;
      this.freeEntries   = newCapacity - this.distinct;  // delta
   }

   //~--- get methods ---------------------------------------------------------

   public int getFreeEntries() {
      return this.freeEntries;
   }

   //~--- set methods ---------------------------------------------------------

   public void setFreeEntries(int freeEntries) {
      this.freeEntries = freeEntries;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Returns the value associated with the specified key. It is often a good idea to first check with
    * {@link #containsKey(long[])} whether the given key has a value associated or not, i.e. whether there
    * exists an association for the given key or not.
    *
    * @param key the key to be searched for.
    * @return the value associated with the specified key; {@code 0} if no such key is present.
    */
   @Override
   public int get(long[] key) {
      final int i = indexOfKey(key);

      if (i < 0) {
         return Integer.MAX_VALUE;  // not contained
      }

      return this.values[i];
   }

   @Override
   public int get(UUID key) {
      final int i = indexOfKey(key);

      if (i < 0) {
         return Integer.MAX_VALUE;  // not contained
      }

      return this.values[i];
   }

   public byte[] getState() {
      return this.state;
   }

   public long[] getTable() {
      return this.table;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Initializes the receiver.
    *
    * @param initialCapacity the initial capacity of the receiver.
    * @param minLoadFactor the minLoadFactor of the receiver.
    * @param maxLoadFactor the maxLoadFactor of the receiver.
    * @throws IllegalArgumentException if
    *
    * {@code initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) || (maxLoadFactor <= 0.0 ||
    * maxLoadFactor >= 1.0) || (minLoadFactor >= maxLoadFactor)} .
    */
   @Override
   protected final void setUp(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
      int capacity = initialCapacity;

      super.setUp(capacity, minLoadFactor, maxLoadFactor);
      capacity = nextPrime(capacity);

      if (capacity == 0) {
         capacity = 1;  // open addressing needs at least one FREE slot at any time.
      }

      this.table  = new long[capacity * 2 + 1];
      this.values = new int[capacity];
      this.state  = new byte[capacity];

      // memory will be exhausted long before this pathological case happens,
      // anyway.
      this.minLoadFactor = minLoadFactor;

      if (capacity == PrimeFinder.largestPrime) {
         this.maxLoadFactor = 1.0;
      } else {
         this.maxLoadFactor = maxLoadFactor;
      }

      this.distinct    = 0;
      this.freeEntries = capacity;  // delta

      // lowWaterMark will be established upon first expansion.
      // establishing it now (upon instance construction) would immediately
      // make the table shrink upon first put(...).
      // After all the idea of an "initialCapacity" implies violating
      // lowWaterMarks when an object is young.
      // See ensureCapacity(...)
      this.lowWaterMark  = 0;
      this.highWaterMark = chooseHighWaterMark(capacity, this.maxLoadFactor);
   }

   //~--- get methods ---------------------------------------------------------

   public int[] getValues() {
      return this.values;
   }
}

