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
 * The Class RoaringIntSet.
 *
 * @author kec
 */
public class RoaringIntSet
         implements IntSet {
   /** The rbmp. */
   RoaringBitmap rbmp;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new roaring int set.
    */
   protected RoaringIntSet() {
      this.rbmp = new RoaringBitmap();
   }

   /**
    * Instantiates a new roaring int set.
    *
    * @param members the members
    */
   protected RoaringIntSet(int... members) {
      this.rbmp = RoaringBitmap.bitmapOf(members);
   }

   /**
    * Instantiates a new roaring int set.
    *
    * @param memberStream the member stream
    */
   protected RoaringIntSet(IntStream memberStream) {
      this.rbmp = new RoaringBitmap();
      memberStream.forEach((member) -> this.rbmp.add(member));
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the.
    *
    * @param item to add to set.
    */
   @Override
   public void add(int item) {
      this.rbmp.add(item);
   }

   /**
    * Adds the all.
    *
    * @param intStream the int stream
    */
   @Override
   public void addAll(IntStream intStream) {
      intStream.forEach((anInt) -> this.rbmp.add(anInt));
   }

   /**
    * And.
    *
    * @param otherSet the other set
    * @return the int set
    */
   @Override
   public IntSet and(IntSet otherSet) {
      this.rbmp.and(getRoaringSet(otherSet));
      return this;
   }

   /**
    * And not.
    *
    * @param otherSet the other set
    * @return the int set
    */
   @Override
   public IntSet andNot(IntSet otherSet) {
      this.rbmp.andNot(getRoaringSet(otherSet));
      return this;
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
      this.rbmp.clear();
   }

   /**
    * Contains.
    *
    * @param item to test for containment in set.
    * @return true if item is contained in set.
    */
   @Override
   public boolean contains(int item) {
      return this.rbmp.contains(item);
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
      this.rbmp.or(getRoaringSet(otherSet));
      return this;
   }

   /**
    * Parallel stream.
    *
    * @return the set members as an {@code IntStream}
    */
   @Override
   public IntStream parallelStream() {
      if (this.rbmp.isEmpty()) {
         return IntStream.empty();
      }

      final Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();

      return StreamSupport.intStream(streamSupplier, streamSupplier.get()
            .characteristics(), true);
   }

   /**
    * Removes the.
    *
    * @param item to remove from set.
    */
   @Override
   public void remove(int item) {
      this.rbmp.remove(item);
   }

   /**
    * Size.
    *
    * @return the number of elements in this set.
    */
   @Override
   public int size() {
      return this.rbmp.getCardinality();
   }

   /**
    * Stream.
    *
    * @return the set members as an {@code IntStream}
    */
   @Override
   public IntStream stream() {
      if (this.rbmp.isEmpty()) {
         return IntStream.empty();
      }

      final Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();

      return StreamSupport.intStream(streamSupplier, streamSupplier.get()
            .characteristics(), false);
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return this.getClass()
                 .getSimpleName() + " size: " + size() + " elements: " + this.rbmp;
   }

   /**
    * Xor.
    *
    * @param otherSet the other set
    * @return the int set
    */
   @Override
   public IntSet xor(IntSet otherSet) {
      this.rbmp.xor(getRoaringSet(otherSet));
      return this;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if empty.
    *
    * @return true if the set is empty.
    */
   @Override
   public boolean isEmpty() {
      return this.rbmp.isEmpty();
   }

   /**
    * Gets the.
    *
    * @return the supplier<? extends spliterator. of int>
    */
   protected Supplier<? extends Spliterator.OfInt> get() {
      return new SpliteratorSupplier();
   }

   /**
    * Gets the int iterator.
    *
    * @return the int iterator
    */
   @Override
   public PrimitiveIterator.OfInt getIntIterator() {
      return new OfIntWrapper(this.rbmp.getIntIterator());
   }

   /**
    * Gets the reverse int iterator.
    *
    * @return the reverse int iterator
    */
   @Override
   public PrimitiveIterator.OfInt getReverseIntIterator() {
      return new OfIntWrapper(this.rbmp.getReverseIntIterator());
   }

   /**
    * Gets the roaring set.
    *
    * @param set the set
    * @return the roaring set
    */
   private RoaringBitmap getRoaringSet(IntSet set) {
      if (set instanceof RoaringIntSet) {
         return ((RoaringIntSet) set).rbmp;
      }

      final RoaringBitmap roaringSet = new RoaringBitmap();

      set.stream()
         .forEach((member) -> roaringSet.add(member));
      return roaringSet;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class BitSetSpliterator.
    */
   private class BitSetSpliterator
            implements Spliterator.OfInt {
      /** The int iterator. */
      IntIterator intIterator = RoaringIntSet.this.rbmp.getIntIterator();

      //~--- methods ----------------------------------------------------------

      /**
       * Characteristics.
       *
       * @return the int
       */
      @Override
      public int characteristics() {
         return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                | Spliterator.SIZED | Spliterator.SORTED;
      }

      /**
       * Estimate size.
       *
       * @return the long
       */
      @Override
      public long estimateSize() {
         return RoaringIntSet.this.size();
      }

      /**
       * Try advance.
       *
       * @param action the action
       * @return true, if successful
       */
      @Override
      public boolean tryAdvance(IntConsumer action) {
         action.accept(this.intIterator.next());
         return this.intIterator.hasNext();
      }

      /**
       * Try split.
       *
       * @return the spliterator. of int
       */
      @Override
      public Spliterator.OfInt trySplit() {
         return null;
      }
   }


   /**
    * The Class OfIntWrapper.
    */
   private static class OfIntWrapper
            implements PrimitiveIterator.OfInt {
      /** The int iterator. */
      IntIterator intIterator;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new of int wrapper.
       *
       * @param intIterator the int iterator
       */
      public OfIntWrapper(IntIterator intIterator) {
         this.intIterator = intIterator;
      }

      //~--- methods ----------------------------------------------------------

      /**
       * Next int.
       *
       * @return the int
       */
      @Override
      public int nextInt() {
         return this.intIterator.next();
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Checks for next.
       *
       * @return true, if successful
       */
      @Override
      public boolean hasNext() {
         return this.intIterator.hasNext();
      }
   }


   /**
    * The Class SpliteratorSupplier.
    */
   private class SpliteratorSupplier
            implements Supplier<Spliterator.OfInt> {
      /**
       * Gets the.
       *
       * @return the spliterator. of int
       */
      @Override
      public Spliterator.OfInt get() {
         return new BitSetSpliterator();
      }
   }
}

