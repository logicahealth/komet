/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.commit.manager;

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.stream.IntStream;

import gov.vha.isaac.ochre.api.commit.StampService;
import org.apache.mahout.math.list.IntArrayList;

/**
 *
 * @author kec
 */
public abstract class IndexedStampSequenceSpliterator<T> implements Spliterator<T> {

    final PrimitiveIterator.OfInt iterator;
    final int size;

    public IndexedStampSequenceSpliterator(IntArrayList keys) {
        size = keys.size();
        iterator = IntStream.of(keys.elements()).iterator();
    }

    @Override
    public final Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public final long estimateSize() {
        return size;
    }

    @Override
    public final int characteristics() {
        return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.SIZED;
    }

    public PrimitiveIterator.OfInt getIterator() {
        return iterator;
    }


}
