/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.collections;

import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.store.IntIntArrayStore;

/**
 * @author kec
 */
public class SpinedIntIntArrayMap implements IntObjectMap<int[]> {

    private static final Logger LOG = LogManager.getLogger();

    private static final int DEFAULT_ELEMENTS_PER_SPINE = 1024;

    protected final IntIntArrayStore intIntArrayStore;
    protected final int elementsPerSpine;
    private final ConcurrentSpineList<AtomicReferenceArray<int[]>> spines = new ConcurrentSpineList<AtomicReferenceArray<int[]>>(16884, this::newSpine);
    protected final ConcurrentSkipListSet<Integer> changedSpineIndexes = new ConcurrentSkipListSet<>();

    public SpinedIntIntArrayMap(IntIntArrayStore intIntArrayStore) {
        this.elementsPerSpine = DEFAULT_ELEMENTS_PER_SPINE;
        this.intIntArrayStore = intIntArrayStore;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        int size = 0;
        int currentSpineCount = this.spines.getSpineCount();
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            AtomicReferenceArray<int[]> spine = this.spines.getSpine(spineIndex);
            for (int indexInSpine = 0; indexInSpine < elementsPerSpine; indexInSpine++) {
                int[] element = spine.get(indexInSpine);
                if (element != null) {
                    size++;
                }
            }
        }
        return size;
    }

    /**
     * @return the number of spines read.
     */
    public int read() {
        int spinesInStore = this.intIntArrayStore.getSpineCount();
       for (int i = 0; i < spinesInStore; i++) {
            Optional<AtomicReferenceArray<int[]>> optionalData = this.intIntArrayStore.get(i);
            if (optionalData.isPresent()) {
                this.spines.setSpine(i, optionalData.get());
            }
        }
        return spinesInStore;
    }

    public boolean write() {
        AtomicBoolean wroteAny = new AtomicBoolean(false);
        this.intIntArrayStore.writeSpineCount(spines.getSpineCount());
        int length = this.spines.getSpineCount();
        for (int key = 0; key < length; key++) {
            boolean spineChanged = changedSpineIndexes.contains(key);
            if (spineChanged) {
                wroteAny.set(true);
                changedSpineIndexes.remove(key);
                this.intIntArrayStore.put(key, spines.getSpine(key));
            }
        }
        return wroteAny.get();
    }

    public int getSpineCount() {
        return spines.getSpineCount();
    }

    private AtomicReferenceArray<int[]> newSpine() {
        AtomicReferenceArray<int[]> spine = new AtomicReferenceArray<>(elementsPerSpine);
        return spine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean put(int index, int[] element) {
        if (index < 0) {
            if (ModelGet.sequenceStore() != null) {
                index = ModelGet.sequenceStore().getElementSequenceForNid(index);
            } else {
                index = Integer.MAX_VALUE + index;
            }
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        this.changedSpineIndexes.add(spineIndex);
        return this.spines.getSpine(spineIndex).getAndSet(indexInSpine, element) == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] get(int index) {
        if (index < 0) {
            if (ModelGet.sequenceStore() != null) {
                index = ModelGet.sequenceStore().getElementSequenceForNid(index);
            } else {
                index = Integer.MAX_VALUE + index;
            }
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        return this.spines.getSpine(spineIndex).get(indexInSpine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<int[]> getOptional(int key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getAndSet(int index, int[] element) {
        if (index < 0) {
            if (ModelGet.sequenceStore() != null) {
                index = ModelGet.sequenceStore().getElementSequenceForNid(index);
            } else {
                index = Integer.MAX_VALUE + index;
            }
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        this.changedSpineIndexes.add(spineIndex);
        return this.spines.getSpine(spineIndex).getAndSet(indexInSpine, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.spines.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(int index) {
        if (index < 0) {
            if (ModelGet.sequenceStore() != null) {
                index = ModelGet.sequenceStore().getElementSequenceForNid(index);
            } else {
                index = Integer.MAX_VALUE + index;
            }
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        return this.spines.getSpine(spineIndex).get(indexInSpine) != null;
    }

    public void forEach(IntBiConsumer<int[]> consumer) {
        int currentSpineCount = getSpineCount();
        int key = 0;
        for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
            AtomicReferenceArray<int[]> spine = this.spines.getSpine(spineIndex);
            for (int indexInSpine = 0; indexInSpine < elementsPerSpine; indexInSpine++) {
                int[] element = spine.get(indexInSpine);
                if (element != null) {
                    consumer.accept(key, element);
                }
                key++;
            }

        }
    }

    @Override
    public int[] accumulateAndGet(int index, int[] x, BinaryOperator<int[]> accumulatorFunction) {
        if (index < 0) {
            if (ModelGet.sequenceStore() != null) {
                index = ModelGet.sequenceStore().getElementSequenceForNid(index);
            } else {
                index = Integer.MAX_VALUE + index;
            }
        }
        int spineIndex = index / elementsPerSpine;
        int indexInSpine = index % elementsPerSpine;
        this.changedSpineIndexes.add(spineIndex);
        return this.spines.getSpine(spineIndex)
                .accumulateAndGet(indexInSpine, x, accumulatorFunction);

    }

    public interface Processor<E> {

        public void process(int key, E value);
    }

    public Stream<int[]> stream() {
        final Supplier<? extends Spliterator<int[]>> streamSupplier = this.get();

        return StreamSupport.stream(streamSupplier, streamSupplier.get()
                .characteristics(), false);
    }

    /**
     * Gets the.
     *
     * @return the supplier<? extends spliterator. of int>
     */
    protected Supplier<? extends Spliterator<int[]>> get() {
        return new SpliteratorSupplier();
    }

    /**
     * The Class SpliteratorSupplier.
     */
    private class SpliteratorSupplier
            implements Supplier<Spliterator<int[]>> {

        /**
         * Gets the.
         *
         * @return the spliterator
         */
        @Override
        public Spliterator<int[]> get() {
            return new SpinedValueSpliterator();
        }
    }

    private class SpinedValueSpliterator implements Spliterator<int[]> {

        int end;
        int currentPosition;

        public SpinedValueSpliterator() {
            this.end = DEFAULT_ELEMENTS_PER_SPINE * getSpineCount();
            this.currentPosition = 0;
        }

        public SpinedValueSpliterator(int start, int end) {
            this.currentPosition = start;
            this.end = end;
        }

        @Override
        public Spliterator<int[]> trySplit() {
            int splitEnd = end;
            int split = end - currentPosition;
            int half = split / 2;
            this.end = currentPosition + half;
            return new SpinedValueSpliterator(currentPosition + half + 1, splitEnd);
        }

        @Override
        public boolean tryAdvance(Consumer<? super int[]> action) {
            while (currentPosition < end) {
                int[] value = get(currentPosition++);
                if (value != null) {
                    action.accept(value);
                    return true;
                }
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - currentPosition;
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                    | Spliterator.SIZED;
        }

    }
}
