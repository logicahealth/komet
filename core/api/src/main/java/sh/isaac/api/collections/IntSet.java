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

import org.eclipse.collections.api.set.primitive.ImmutableIntSet;

import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface IntSet.
 *
 * @author kec
 */
public interface IntSet {
   /**
    * Adds the.
    *
    * @param item to add to set.
    */
   void add(int item);

   /**
    * Adds the all.
    *
    * @param intStream the int stream
    */
   void addAll(IntStream intStream);

   default void addAll(int[] values) {
      for (int item: values) {
         add(item);
      }
   }

   /**
    * And.
    *
    * @param otherSet the other set
    * @return the int set
    */
   IntSet and(IntSet otherSet);

   /**
    * And not.
    *
    * @param otherSet the other set
    * @return the int set
    */
   IntSet andNot(IntSet otherSet);

   /**
    * As array.
    *
    * @return the int[]
    */
   int[] asArray();

   /**
    * Clear.
    */
   void clear();

   /**
    * Contains.
    *
    * @param item to test for containment in set.
    * @return true if item is contained in set.
    */
   boolean contains(int item);

   /**
    * Find first.
    *
    * @return the optional int
    */
   OptionalInt findFirst();

   /**
    * Or.
    *
    * @param otherSet the other set
    * @return the int set
    */
   IntSet or(IntSet otherSet);

   IntSet or(ImmutableIntSet otherSet);

   /**
    * Parallel stream.
    *
    * @return the set members as an {@code IntStream}
    */
   IntStream parallelStream();

   /**
    * Removes the.
    *
    * @param item to remove from set.
    */
   void remove(int item);

   /**
    * Size.
    *
    * @return the number of elements in this set.
    */
   int size();

   /**
    * Stream.
    *
    * @return the set members as an {@code IntStream}
    */
   IntStream stream();

   /**
    * Xor.
    *
    * @param otherSet the other set
    * @return the int set
    */
   IntSet xor(IntSet otherSet);

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if empty.
    *
    * @return true if the set is empty.
    */
   boolean isEmpty();

   /**
    * Gets the int iterator.
    *
    * @return the int iterator
    */
   PrimitiveIterator.OfInt getIntIterator();

   /**
    * Gets the reverse int iterator.
    *
    * @return the reverse int iterator
    */
   PrimitiveIterator.OfInt getReverseIntIterator();
}

