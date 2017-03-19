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
 *
 * @author kec
 * @param <T>
 */
public abstract class AbstractIntSet<T extends AbstractIntSet<T>> {
   boolean readOnly = false;
   IntSet  intSet;

   //~--- constant enums ------------------------------------------------------

   protected enum Concurrency { THREAD_SAFE }

   ;

   //~--- constructors --------------------------------------------------------

   protected AbstractIntSet() {
      intSet = new RoaringIntSet();
   }

   /**
    *
    * @param readOnly true if the set is read only.
    */
   protected AbstractIntSet(boolean readOnly) {
      intSet        = new RoaringIntSet();
      this.readOnly = readOnly;
   }

   protected AbstractIntSet(Concurrency concurrency) {
      if (concurrency == Concurrency.THREAD_SAFE) {
         intSet = new ConcurrentSkipListIntegerSet();
      } else {
         intSet = new RoaringIntSet();
      }
   }

   protected AbstractIntSet(int... members) {
      intSet = new RoaringIntSet(members);
   }

   protected AbstractIntSet(IntStream memberStream) {
      intSet = new RoaringIntSet(memberStream);
   }

   protected AbstractIntSet(OpenIntHashSet members) {
      intSet = new RoaringIntSet();
      members.forEachKey((int element) -> {
                            intSet.add(element);
                            return true;
                         });
   }

   //~--- methods -------------------------------------------------------------

   /**
    *
    * @param item to add to set.
    */
   public void add(int item) {
      if (readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      intSet.add(item);
   }

   public void addAll(IntStream intStream) {
      if (readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      intStream.forEach((anInt) -> intSet.add(anInt));
   }

   public T and(T otherSet) {
      if (readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      intSet.and(otherSet.intSet);
      return (T) this;
   }

   public T andNot(T otherSet) {
      if (readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      intSet.andNot(otherSet.intSet);
      return (T) this;
   }

   public int[] asArray() {
      return stream().toArray();
   }

   public OpenIntHashSet asOpenIntHashSet() {
      OpenIntHashSet set = new OpenIntHashSet();

      stream().forEach((sequence) -> set.add(sequence));
      return set;
   }

   public void clear() {
      intSet.clear();
   }

   public int compareTo(T o) {
      int comparison = Integer.compare(intSet.size(), o.intSet.size());

      if (comparison != 0) {
         return comparison;
      }

      PrimitiveIterator.OfInt thisIterator  = intSet.getIntIterator();
      PrimitiveIterator.OfInt otherIterator = o.intSet.getIntIterator();

      while (thisIterator.hasNext()) {
         comparison = Integer.compare(thisIterator.next(), otherIterator.next());

         if (comparison != 0) {
            return comparison;
         }
      }

      return 0;
   }

   /**
    *
    * @param item to test for containment in set.
    * @return true if item is contained in set.
    */
   public boolean contains(int item) {
      return intSet.contains(item);
   }

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

      PrimitiveIterator.OfInt itr1 = this.intSet.getIntIterator();
      PrimitiveIterator.OfInt itr2 = other.intSet.getIntIterator();

      while ((itr1.hasNext() == itr2.hasNext()) && (itr1.hasNext() == true)) {
         if (itr1.nextInt() != itr2.nextInt()) {
            return false;
         }
      }

      return !(itr1.hasNext() || itr2.hasNext());
   }

   public OptionalInt findFirst() {
      return stream().findFirst();
   }

   @Override
   public int hashCode() {
      int                     result = 1;
      PrimitiveIterator.OfInt itr    = intSet.getIntIterator();

      while (itr.hasNext()) {
         result = 31 * result + itr.next();
      }

      return result;
   }

   public T or(T otherSet) {
      if (readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      intSet.or(otherSet.intSet);
      return (T) this;
   }

   /**
    *
    * @return the set members as an {@code IntStream}
    */
   public IntStream parallelStream() {
      if (intSet.isEmpty()) {
         return IntStream.empty();
      }

      Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();

      return StreamSupport.intStream(streamSupplier, streamSupplier.get()
            .characteristics(), true);
   }

   /**
    * Reads a size, then each of the members from DataInput.
    *
    * @param input
    * @throws IOException
    */
   public void read(DataInput input)
            throws IOException {
      int size = input.readInt();

      for (int i = 0; i < size; i++) {
         add(input.readInt());
      }
   }

   /**
    *
    * @param item to remove from set.
    */
   public void remove(int item) {
      if (readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      intSet.remove(item);
   }

   /**
    *
    * @return the number of elements in this set.
    */
   public int size() {
      return intSet.size();
   }

   /**
    *
    * @return the set members as an {@code IntStream}
    */
   public IntStream stream() {
      if (intSet.isEmpty()) {
         return IntStream.empty();
      }

      Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();

      return StreamSupport.intStream(streamSupplier, streamSupplier.get()
            .characteristics(), false);
   }

   @Override
   public String toString() {
      return this.getClass()
                 .getSimpleName() + " size: " + size() + " elements: " + intSet;
   }

   public String toString(IntFunction<String> function) {
      StringBuilder sb = new StringBuilder();

      sb.append("[");

      int limit = 20;

      stream().limit(limit).forEach((element) -> {
                          sb.append(function.apply(element));
                          sb.append("<");
                          sb.append(element);
                          sb.append(">");
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
    * @param output
    * @throws IOException
    */
   public void write(DataOutput output)
            throws IOException {
      output.writeInt(size());
      stream().forEach((member) -> {
                          try {
                             output.writeInt(member);
                          } catch (IOException ex) {
                             throw new RuntimeException(ex);
                          }
                       });
   }

   public T xor(T otherSet) {
      if (readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }

      intSet.xor(otherSet.intSet);
      return (T) this;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    *
    * @return true if the set is empty.
    */
   public boolean isEmpty() {
      return intSet.isEmpty();
   }

   protected Supplier<? extends Spliterator.OfInt> get() {
      return new SpliteratorSupplier();
   }

   public PrimitiveIterator.OfInt getIntIterator() {
      return intSet.getIntIterator();
   }

   //~--- set methods ---------------------------------------------------------

   public void setReadOnly() {
      this.readOnly = true;
   }

   //~--- get methods ---------------------------------------------------------

   public PrimitiveIterator.OfInt getReverseIntIterator() {
      return intSet.getReverseIntIterator();
   }

   //~--- inner classes -------------------------------------------------------

   private class BitSetSpliterator
            implements Spliterator.OfInt {
      PrimitiveIterator.OfInt intIterator = intSet.getIntIterator();

      //~--- methods ----------------------------------------------------------

      @Override
      public int characteristics() {
         return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                | Spliterator.SIZED | Spliterator.SORTED;
      }

      @Override
      public long estimateSize() {
         return AbstractIntSet.this.size();
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


   private class SpliteratorSupplier
            implements Supplier<Spliterator.OfInt> {
      @Override
      public Spliterator.OfInt get() {
         return new BitSetSpliterator();
      }
   }
}

