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

import gov.vha.isaac.ochre.api.SequenceProvider;
import static gov.vha.isaac.ochre.collections.IntSet.getSequenceProvider;
import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.roaringbitmap.IntIterator;

/**
 *
 * @author kec
 */
public class NidSet extends IntSet {
    
    public static NidSet of(int... members) {
        return new NidSet(members);
    }

    public static NidSet of(OpenIntHashSet members) {
        return new NidSet(members);
    }
    
    public static NidSet of(Collection<Integer> members) {
        return new NidSet(members.stream().mapToInt(i -> i));
    }
    public static NidSet of(ConceptSequenceSet conceptSequenceSet) {
        SequenceProvider sp = getSequenceProvider();
        return new NidSet(conceptSequenceSet.stream()
                .map((sequence) -> sp.getConceptNid(sequence)));
    }

    public static NidSet of(SememeSequenceSet sememeSequenceSet) {
        SequenceProvider sp = getSequenceProvider();
        return new NidSet(sememeSequenceSet.stream()
                .map((sequence) -> sp.getSememeNid(sequence)));
    }

    private NidSet(IntStream memberStream) {
        super(memberStream);
    }

    private NidSet(int[] members) {
        super(members);
    }

    private NidSet(OpenIntHashSet members) {
        super(members);
    }

    public NidSet() {
    }
    

    /**
     * 
     * @param item to add to set.
     */
    @Override
    public void add(int item) {
        rbmp.add(item + Integer.MIN_VALUE);
    }
    
    /**
     * 
     * @param item to remove from set.
     */
    @Override
    public void remove(int item) {
        rbmp.remove(item + Integer.MIN_VALUE);
    }
    
    /**
     * 
     * @param item to test for containment in set.
     * @return true if item is contained in set.
     */
    @Override
    public boolean contains(int item) {
        return rbmp.contains(item + Integer.MIN_VALUE);
    }
    
    @Override
    public IntStream stream() {
        return super.stream().map((int item) -> item + Integer.MIN_VALUE);
    }

    @Override
    protected Supplier<? extends Spliterator.OfInt> get() {
        return new SpliteratorSupplier();
    }
    
    private class SpliteratorSupplier implements Supplier<Spliterator.OfInt> {

        @Override
        public OfInt get() {
            return new BitSetSpliterator();
        }
        
    }
    
    private class BitSetSpliterator implements OfInt {
        
        IntIterator intIterator = rbmp.getIntIterator();
        
        @Override
        public OfInt trySplit() {
           return null;
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            action.accept(intIterator.next() + Integer.MIN_VALUE);
            return intIterator.hasNext();
        }

        @Override
        public long estimateSize() {
            return NidSet.this.size();
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT + 
                    Spliterator.IMMUTABLE + 
                    Spliterator.NONNULL + 
                    Spliterator.ORDERED + 
                    Spliterator.SIZED + 
                    Spliterator.SORTED;
        }
    }
}
