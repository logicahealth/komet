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

//~--- JDK imports ------------------------------------------------------------

import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.IntStream;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ConcurrentSkipListIntegerSet.
 *
 * @author kec
 */
public class ConcurrentSkipListIntegerSet
         implements IntSet {
   /** The set. */
   ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concurrent skip list integer set.
    */
   protected ConcurrentSkipListIntegerSet() {}

   /**
    * Instantiates a new concurrent skip list integer set.
    *
    * @param members the members
    */
   protected ConcurrentSkipListIntegerSet(int... members) {
      for (final int member: members) {
         this.set.add(member);
      }
   }

   /**
    * Instantiates a new concurrent skip list integer set.
    *
    * @param memberStream the member stream
    */
   protected ConcurrentSkipListIntegerSet(IntStream memberStream) {
      memberStream.forEach((member) -> this.set.add(member));
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the.
    *
    * @param item the item
    */
   @Override
   public void add(int item) {
      this.set.add(item);
   }

   /**
    * Adds the all.
    *
    * @param intStream the int stream
    */
   @Override
   public void addAll(IntStream intStream) {
      intStream.forEach((item) -> add(item));
   }

   /**
    * And.
    *
    * @param otherSet the other set
    * @return the int set
    */

   //
   @Override
   public IntSet and(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * And not.
    *
    * @param otherSet the other set
    * @return the int set
    */
   @Override
   public IntSet andNot(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * As array.
    *
    * @return the int[]
    */
   @Override
   public int[] asArray() {
      return stream().toArray();
   }

   /**
    * Clear.
    */
   @Override
   public void clear() {
      this.set.clear();
   }

   /**
    * Contains.
    *
    * @param item the item
    * @return true, if successful
    */
   @Override
   public boolean contains(int item) {
      return this.set.contains(item);
   }

   /**
    * Find first.
    *
    * @return the optional int
    */
   @Override
   public OptionalInt findFirst() {
      return stream().findFirst();
   }

   /**
    * Or.
    *
    * @param otherSet the other set
    * @return the int set
    */
   @Override
   public IntSet or(IntSet otherSet) {
      this.addAll(otherSet.stream());
      return this;
   }

   /**
    * Parallel stream.
    *
    * @return the int stream
    */
   @Override
   public IntStream parallelStream() {
      return stream().parallel();
   }

   /**
    * Removes the.
    *
    * @param item the item
    */
   @Override
   public void remove(int item) {
      this.set.remove(item);
   }

   /**
    * Size.
    *
    * @return the int
    */
   @Override
   public int size() {
      return this.set.size();
   }

   /**
    * Stream.
    *
    * @return the int stream
    */
   @Override
   public IntStream stream() {
      return this.set.stream()
                     .mapToInt(item -> (int) item);
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return this.set.toString();
   }

   /**
    * Xor.
    *
    * @param otherSet the other set
    * @return the int set
    */
   @Override
   public IntSet xor(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if empty.
    *
    * @return true, if empty
    */
   @Override
   public boolean isEmpty() {
      return this.set.isEmpty();
   }

   /**
    * Gets the int iterator.
    *
    * @return the int iterator
    */
   @Override
   public PrimitiveIterator.OfInt getIntIterator() {
      return stream().iterator();
   }

   /**
    * Gets the reverse int iterator.
    *
    * @return the reverse int iterator
    */
   @Override
   public PrimitiveIterator.OfInt getReverseIntIterator() {
      return this.set.descendingSet()
                     .stream()
                     .mapToInt(item -> (int) item)
                     .iterator();
   }
}

