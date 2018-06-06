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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

//~--- classes ----------------------------------------------------------------

/**
 * The Class AbstractIntSet.
 *
 * @author kec
 * @param <T> the generic type
 */
public abstract class AbstractIntSet<T extends AbstractIntSet<T>> implements IntSet {
   /** The read only. */
   boolean readOnly = false;

   /** The int set. */
   IntSet intSet;

   //~--- constant enums ------------------------------------------------------

   /**
    * The Enum Concurrency.
    */
   protected enum Concurrency {
      /** The thread safe. */
      THREAD_SAFE
   }

   ;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new abstract int set.
    */
   protected AbstractIntSet() {
      this.intSet = new RoaringIntSet();
   }

   /**
    * Instantiates a new abstract int set.
    *
    * @param readOnly true if the set is read only.
    */
   protected AbstractIntSet(boolean readOnly) {
      this.intSet   = new RoaringIntSet();
      this.readOnly = readOnly;
   }

   /**
    * Instantiates a new abstract int set.
    *
    * @param concurrency the concurrency
    */
   protected AbstractIntSet(Concurrency concurrency) {
      if (concurrency == Concurrency.THREAD_SAFE) {
         this.intSet = new ConcurrentSkipListIntegerSet();
      } else {
         this.intSet = new RoaringIntSet();
      }
   }

   /**
    * Instantiates a new abstract int set.
    *
    * @param members the members
    */
   protected AbstractIntSet(int... members) {
      this.intSet = new RoaringIntSet(members);
   }

   /**
    * Instantiates a new abstract int set.
    *
    * @param memberStream the member stream
    */
   protected AbstractIntSet(IntStream memberStream) {
      this.intSet = new RoaringIntSet(memberStream);
   }

   /**
    * Instantiates a new abstract int set.
    *
    * @param members the members
    */
   protected AbstractIntSet(OpenIntHashSet members) {
      this.intSet = new RoaringIntSet();
      members.forEachKey((int element) -> {
                            this.intSet.add(element);
                            return true;
                         });
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the.
    *
    * @param item to add to set.
    */
   @Override
   public void add(int item) {
      if (this.readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      this.intSet.add(item);
   }

   /**
    * Adds the all.
    *
    * @param intStream the int stream
    */
   @Override
   public void addAll(IntStream intStream) {
      if (this.readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      intStream.forEach((anInt) -> this.intSet.add(anInt));
   }

   /**
    * And.
    *
    * @param otherSet the other set
    * @return the t
    */
   public T and(T otherSet) {
      if (this.readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      this.intSet.and(otherSet.intSet);
      return (T) this;
   }

   @Override
   public IntSet and(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntSet andNot(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntSet or(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntSet xor(IntSet otherSet) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   /**
    * And not.
    *
    * @param otherSet the other set
    * @return the t
    */
   public T andNot(T otherSet) {
      if (this.readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      this.intSet.andNot(otherSet.intSet);
      return (T) this;
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
    * As open int hash set.
    *
    * @return the open int hash set
    */
   public OpenIntHashSet asOpenIntHashSet() {
      final OpenIntHashSet set = new OpenIntHashSet();

      stream().forEach((sequence) -> set.add(sequence));
      return set;
   }

   /**
    * Clear.
    */
   @Override
   public void clear() {
      this.intSet.clear();
   }

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   public int compareTo(T o) {
      int comparison = Integer.compare(this.intSet.size(), o.intSet.size());

      if (comparison != 0) {
         return comparison;
      }

      final PrimitiveIterator.OfInt thisIterator  = this.intSet.getIntIterator();
      final PrimitiveIterator.OfInt otherIterator = o.intSet.getIntIterator();

      while (thisIterator.hasNext()) {
         comparison = Integer.compare(thisIterator.next(), otherIterator.next());

         if (comparison != 0) {
            return comparison;
         }
      }

      return 0;
   }

   /**
    * Contains.
    *
    * @param item to test for containment in set.
    * @return true if item is contained in set.
    */
   @Override
   public boolean contains(int item) {
      return this.intSet.contains(item);
   }

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final AbstractIntSet<?> other = (AbstractIntSet<?>) obj;

      if (this.size() != other.size()) {
         return false;
      }

      final PrimitiveIterator.OfInt itr1 = this.intSet.getIntIterator();
      final PrimitiveIterator.OfInt itr2 = other.intSet.getIntIterator();

      while ((itr1.hasNext() == itr2.hasNext()) && (itr1.hasNext() == true)) {
         if (itr1.nextInt() != itr2.nextInt()) {
            return false;
         }
      }

      return !(itr1.hasNext() || itr2.hasNext());
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
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int                           result = 1;
      final PrimitiveIterator.OfInt itr    = this.intSet.getIntIterator();

      while (itr.hasNext()) {
         result = 31 * result + itr.next();
      }

      return result;
   }

   /**
    * Or.
    *
    * @param otherSet the other set
    * @return the t
    */
   public T or(T otherSet) {
      if (this.readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      this.intSet.or(otherSet.intSet);
      return (T) this;
   }

   
   /**
    * Parallel stream.
    *
    * @return the set members as an {@code IntStream}
    */
   @Override
   public IntStream parallelStream() {
      if (this.intSet.isEmpty()) {
         return IntStream.empty();
      }

      final Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();

      return StreamSupport.intStream(streamSupplier, streamSupplier.get()
            .characteristics(), true);
   }

   /**
    * Reads a size, then each of the members from DataInput.
    *
    * @param input the input
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void read(DataInput input)
            throws IOException {
      final int size = input.readInt();

      for (int i = 0; i < size; i++) {
         add(input.readInt());
      }
   }

   /**
    * Removes the.
    *
    * @param item to remove from set.
    */
   @Override
   public void remove(int item) {
      if (this.readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      this.intSet.remove(item);
   }

   /**
    * Size.
    *
    * @return the number of elements in this set.
    */
   @Override
   public int size() {
      return this.intSet.size();
   }

   /**
    * Stream.
    *
    * @return the set members as an {@code IntStream}
    */
   @Override
   public IntStream stream() {
      if (this.intSet.isEmpty()) {
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
                 .getSimpleName() + " size: " + size() + " elements: " + this.intSet;
   }

   /**
    * To string.
    *
    * @param function the function
    * @return the string
    */
   public String toString(IntFunction<String> function) {
      final StringBuilder sb = new StringBuilder();

      sb.append("[");

      final int limit = 20;

      stream().limit(limit).forEach((element) -> {
         String temp = function.apply(element);
         sb.append(element); // only append the function output if it is different from just printing the int...
         if (!temp.equals(element + "")) {
            sb.append("<");
            sb.append(temp);
            sb.append(">");
         }
         sb.append(", ");
      });

      if (size() > limit) {
         sb.append("...");
      } else if (size() > 0) {
         sb.delete(sb.length() - 2, sb.length());
      }

      sb.append("]");
      return this.getClass()
                 .getSimpleName() + " size: " + size() + " elements: " + sb.toString();
   }

   /**
    * Writes a size then each of the members to the DataOutput.
    *
    * @param output the output
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write(DataOutput output)
            throws IOException {
      output.writeInt(size());
      stream().forEach((member) -> {
                          try {
                             output.writeInt(member);
                          } catch (final IOException ex) {
                             throw new RuntimeException(ex);
                          }
                       });
   }

   /**
    * Xor. The underlying set is modified. 
    *
    * @param otherSet the other set
    * @return the modified set that represents the result of the operation. 
    */
   public T xor(T otherSet) {
      if (this.readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      this.intSet.xor(otherSet.intSet);
      return (T) this;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if empty.
    *
    * @return true if the set is empty.
    */
   @Override
   public boolean isEmpty() {
      return this.intSet.isEmpty();
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
      return this.intSet.getIntIterator();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set read only.
    */
   public void setReadOnly() {
      this.readOnly = true;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the reverse int iterator.
    *
    * @return the reverse int iterator
    */
   @Override
   public PrimitiveIterator.OfInt getReverseIntIterator() {
      return this.intSet.getReverseIntIterator();
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class BitSetSpliterator.
    */
   private class BitSetSpliterator
            implements Spliterator.OfInt {
      /** The int iterator. */
      PrimitiveIterator.OfInt intIterator = AbstractIntSet.this.intSet.getIntIterator();

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
         return AbstractIntSet.this.size();
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

