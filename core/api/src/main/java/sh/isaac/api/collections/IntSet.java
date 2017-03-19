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
import java.util.function.IntFunction;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

import org.roaringbitmap.IntIterator;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
public interface IntSet {
   /**
    *
    * @param item to add to set.
    */
   void add(int item);

   void addAll(IntStream intStream);

   IntSet and(IntSet otherSet);

   IntSet andNot(IntSet otherSet);

   int[] asArray();

   void clear();

   /**
    *
    * @param item to test for containment in set.
    * @return true if item is contained in set.
    */
   boolean contains(int item);

   OptionalInt findFirst();

   IntSet or(IntSet otherSet);

   /**
    *
    * @return the set members as an {@code IntStream}
    */
   IntStream parallelStream();

   /**
    *
    * @param item to remove from set.
    */
   void remove(int item);

   /**
    *
    * @return the number of elements in this set.
    */
   int size();

   /**
    *
    * @return the set members as an {@code IntStream}
    */
   IntStream stream();

   IntSet xor(IntSet otherSet);

   //~--- get methods ---------------------------------------------------------

   /**
    *
    * @return true if the set is empty.
    */
   boolean isEmpty();

   PrimitiveIterator.OfInt getIntIterator();

   PrimitiveIterator.OfInt getReverseIntIterator();
}

