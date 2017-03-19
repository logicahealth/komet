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
 *
 * @author kec
 */
public class ConcurrentSkipListIntegerSet
         implements IntSet {
   ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>();

   //~--- constructors --------------------------------------------------------

   protected ConcurrentSkipListIntegerSet() {}

   protected ConcurrentSkipListIntegerSet(int... members) {
      for (int member: members) {
         set.add(member);
      }
   }

   protected ConcurrentSkipListIntegerSet(IntStream memberStream) {
      memberStream.forEach((member) -> set.add(member));
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void add(int item) {
      set.add(item);
   }

   @Override
   public void addAll(IntStream intStream) {
      intStream.forEach((item) -> add(item));
   }

   //
   @Override
   public IntSet and(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public IntSet andNot(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public int[] asArray() {
      return stream().toArray();
   }

   @Override
   public void clear() {
      set.clear();
   }

   @Override
   public boolean contains(int item) {
      return set.contains(item);
   }

   @Override
   public OptionalInt findFirst() {
      return stream().findFirst();
   }

   @Override
   public IntSet or(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public IntStream parallelStream() {
      return stream().parallel();
   }

   @Override
   public void remove(int item) {
      set.remove(item);
   }

   @Override
   public int size() {
      return set.size();
   }

   @Override
   public IntStream stream() {
      return set.stream()
                .mapToInt(item -> (int) item);
   }

   @Override
   public String toString() {
      return set.toString();
   }

   @Override
   public IntSet xor(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public boolean isEmpty() {
      return set.isEmpty();
   }

   @Override
   public PrimitiveIterator.OfInt getIntIterator() {
      return stream().iterator();
   }

   @Override
   public PrimitiveIterator.OfInt getReverseIntIterator() {
      return set.descendingSet()
                .stream()
                .mapToInt(item -> (int) item)
                .iterator();
   }
}

