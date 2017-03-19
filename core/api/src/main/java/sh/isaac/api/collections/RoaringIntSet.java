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
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

//~--- non-JDK imports --------------------------------------------------------

import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class RoaringIntSet
         implements IntSet {
   RoaringBitmap rbmp;

   //~--- constructors --------------------------------------------------------

   protected RoaringIntSet() {
      rbmp = new RoaringBitmap();
   }

   protected RoaringIntSet(int... members) {
      rbmp = RoaringBitmap.bitmapOf(members);
   }

   protected RoaringIntSet(IntStream memberStream) {
      rbmp = new RoaringBitmap();
      memberStream.forEach((member) -> rbmp.add(member));
   }

   //~--- methods -------------------------------------------------------------

   /**
    *
    * @param item to add to set.
    */
   @Override
   public void add(int item) {
      rbmp.add(item);
   }

   @Override
   public void addAll(IntStream intStream) {
      intStream.forEach((anInt) -> rbmp.add(anInt));
   }

   @Override
   public IntSet and(IntSet otherSet) {
      rbmp.and(getRoaringSet(otherSet));
      return this;
   }

   @Override
   public IntSet andNot(IntSet otherSet) {
      rbmp.andNot(getRoaringSet(otherSet));
      return this;
   }

   @Override
   public int[] asArray() {
      return stream().toArray();
   }

   @Override
   public void clear() {
      rbmp.clear();
   }

   /**
    *
    * @param item to test for containment in set.
    * @return true if item is contained in set.
    */
   @Override
   public boolean contains(int item) {
      return rbmp.contains(item);
   }

   @Override
   public OptionalInt findFirst() {
      return stream().findFirst();
   }

   @Override
   public IntSet or(IntSet otherSet) {
      rbmp.or(getRoaringSet(otherSet));
      return this;
   }

   /**
    *
    * @return the set members as an {@code IntStream}
    */
   @Override
   public IntStream parallelStream() {
      if (rbmp.isEmpty()) {
         return IntStream.empty();
      }

      Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();

      return StreamSupport.intStream(streamSupplier, streamSupplier.get()
            .characteristics(), true);
   }

   /**
    *
    * @param item to remove from set.
    */
   @Override
   public void remove(int item) {
      rbmp.remove(item);
   }

   /**
    *
    * @return the number of elements in this set.
    */
   @Override
   public int size() {
      return rbmp.getCardinality();
   }

   /**
    *
    * @return the set members as an {@code IntStream}
    */
   @Override
   public IntStream stream() {
      if (rbmp.isEmpty()) {
         return IntStream.empty();
      }

      Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();

      return StreamSupport.intStream(streamSupplier, streamSupplier.get()
            .characteristics(), false);
   }

   @Override
   public String toString() {
      return this.getClass()
                 .getSimpleName() + " size: " + size() + " elements: " + rbmp;
   }

   @Override
   public IntSet xor(IntSet otherSet) {
      rbmp.xor(getRoaringSet(otherSet));
      return this;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    *
    * @return true if the set is empty.
    */
   @Override
   public boolean isEmpty() {
      return rbmp.isEmpty();
   }

   protected Supplier<? extends Spliterator.OfInt> get() {
      return new SpliteratorSupplier();
   }

   @Override
   public PrimitiveIterator.OfInt getIntIterator() {
      return new OfIntWrapper(rbmp.getIntIterator());
   }

   @Override
   public PrimitiveIterator.OfInt getReverseIntIterator() {
      return new OfIntWrapper(rbmp.getReverseIntIterator());
   }

   private RoaringBitmap getRoaringSet(IntSet set) {
      if (set instanceof RoaringIntSet) {
         return ((RoaringIntSet) set).rbmp;
      }

      RoaringBitmap roaringSet = new RoaringBitmap();

      set.stream()
         .forEach((member) -> roaringSet.add(member));
      return roaringSet;
   }

   //~--- inner classes -------------------------------------------------------

   private class BitSetSpliterator
            implements Spliterator.OfInt {
      IntIterator intIterator = rbmp.getIntIterator();

      //~--- methods ----------------------------------------------------------

      @Override
      public int characteristics() {
         return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                | Spliterator.SIZED | Spliterator.SORTED;
      }

      @Override
      public long estimateSize() {
         return RoaringIntSet.this.size();
      }

      @Override
      public boolean tryAdvance(IntConsumer action) {
         action.accept(intIterator.next());
         return intIterator.hasNext();
      }

      @Override
      public Spliterator.OfInt trySplit() {
         return null;
      }
   }


   private static class OfIntWrapper
            implements PrimitiveIterator.OfInt {
      IntIterator intIterator;

      //~--- constructors -----------------------------------------------------

      public OfIntWrapper(IntIterator intIterator) {
         this.intIterator = intIterator;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int nextInt() {
         return intIterator.next();
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public boolean hasNext() {
         return intIterator.hasNext();
      }
   }


   private class SpliteratorSupplier
            implements Supplier<Spliterator.OfInt> {
      @Override
      public Spliterator.OfInt get() {
         return new BitSetSpliterator();
      }
   }
}

