/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.collections;

import java.util.OptionalInt;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

/**
 *
 * @author kec
 * @param <T>
 */
public abstract class IntSet<T extends IntSet<T>> implements Comparable<T> {

    RoaringBitmap rbmp;

    protected IntSet() {
        rbmp = new RoaringBitmap();
    }

    protected IntSet(int... members) {
        rbmp = RoaringBitmap.bitmapOf(members);
    }

    protected IntSet(OpenIntHashSet members) {
        rbmp = new RoaringBitmap();
        members.forEachKey((int element) -> {
            rbmp.add(element);
            return true;
        });
    }

    protected IntSet(IntStream memberStream) {
        rbmp = new RoaringBitmap();
        memberStream.forEach((member) -> rbmp.add(member));
    }

    @Override
    public int compareTo(T o) {
        int comparison = Integer.compare(rbmp.getCardinality(), rbmp.getCardinality());
        if (comparison != 0) {
            return comparison;
        }
        IntIterator thisIterator = rbmp.getIntIterator();
        IntIterator otherIterator = o.rbmp.getIntIterator();
        while (thisIterator.hasNext()) {
            comparison = Integer.compare(thisIterator.next(), otherIterator.next());
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    public void clear() {
        rbmp.clear();
    }

    public T or(T otherSet) {
        rbmp.or(otherSet.rbmp);
        return (T) this;
    }

    public T and(T otherSet) {
        rbmp.and(otherSet.rbmp);
        return (T) this;
    }

    public T andNot(T otherSet) {
        rbmp.andNot(otherSet.rbmp);
        return (T) this;
    }

    public T xor(T otherSet) {
        rbmp.xor(otherSet.rbmp);
        return (T) this;
    }

    /**
     *
     * @return the number of elements in this set.
     */
    public int size() {
        return rbmp.getCardinality();
    }

    /**
     *
     * @return true if the set is empty.
     */
    public boolean isEmpty() {
        return rbmp.isEmpty();
    }

    /**
     *
     * @param item to add to set.
     */
    public void add(int item) {
        rbmp.add(item);
    }

    public void addAll(IntStream intStream) {
        intStream.forEach((anInt) -> rbmp.add(anInt));
    }

    /**
     *
     * @param item to remove from set.
     */
    public void remove(int item) {
        rbmp.remove(item);
    }

    /**
     *
     * @param item to test for containment in set.
     * @return true if item is contained in set.
     */
    public boolean contains(int item) {
        return rbmp.contains(item);
    }

    /**
     *
     * @return the set members as an {@code IntStream}
     */
    public IntStream stream() {
        if (rbmp.isEmpty()) {
            return IntStream.empty();
        }
        Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();
        return StreamSupport.intStream(streamSupplier,
                streamSupplier.get().characteristics(),
                false);
    }

    public OptionalInt findFirst() {
        return stream().findFirst();
    }

    /**
     *
     * @return the set members as an {@code IntStream}
     */
    public IntStream parallelStream() {
        if (rbmp.isEmpty()) {
            return IntStream.empty();
        }
        Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();
        return StreamSupport.intStream(streamSupplier,
                streamSupplier.get().characteristics(),
                true);
    }

    public int[] asArray() {
        return stream().toArray();
    }

    public OpenIntHashSet asOpenIntHashSet() {
        OpenIntHashSet set = new OpenIntHashSet();
        stream().forEach((sequence) -> set.add(sequence));
        return set;
    }

    protected Supplier<? extends Spliterator.OfInt> get() {
        return new SpliteratorSupplier();
    }

    private class SpliteratorSupplier implements Supplier<Spliterator.OfInt> {

        @Override
        public Spliterator.OfInt get() {
            return new BitSetSpliterator();
        }

    }

    private class BitSetSpliterator implements Spliterator.OfInt {

        IntIterator intIterator = rbmp.getIntIterator();

        @Override
        public Spliterator.OfInt trySplit() {
            return null;
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            action.accept(intIterator.next());
            return intIterator.hasNext();
        }

        @Override
        public long estimateSize() {
            return IntSet.this.size();
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT
                    + Spliterator.IMMUTABLE
                    + Spliterator.NONNULL
                    + Spliterator.ORDERED
                    + Spliterator.SIZED
                    + Spliterator.SORTED;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + " size: " + size() + " elements: " + rbmp;
    }

    public IntIterator getIntIterator() {
        return rbmp.getIntIterator();
    }

    public IntIterator getReverseIntIterator() {
        return rbmp.getReverseIntIterator();
    }

}
