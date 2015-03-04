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

import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.roaringbitmap.IntIterator;

/**
 *
 * @author kec
 */
public class SequenceSet extends IntSet {


    protected SequenceSet(IntStream memberStream) {
        super(memberStream);
    }

    protected SequenceSet(int... members) {
        super(members);
    }

    protected SequenceSet(OpenIntHashSet members) {
        super(members);
    }

    public SequenceSet() {
    }

    @Override
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
            return SequenceSet.this.size();
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
}
