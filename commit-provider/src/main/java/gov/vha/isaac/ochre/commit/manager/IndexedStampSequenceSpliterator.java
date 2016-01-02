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

import java.util.Spliterator;

import gov.vha.isaac.ochre.api.commit.CommitService;
import org.apache.mahout.math.list.IntArrayList;

/**
 *
 * @author kec
 */
public abstract class IndexedStampSequenceSpliterator<T> implements Spliterator<T> {
    
    private final IntArrayList keys;
    private int index = CommitService.FIRST_STAMP_SEQUENCE;

    public IndexedStampSequenceSpliterator(IntArrayList keys) {
        this.keys = keys;
    }

    @Override
    public final Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public final long estimateSize() {
        return getKeys().size();
    }

    @Override
    public final int characteristics() {
        return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.SIZED;
    }

    /**
     * @return the index
     */
    public final int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public final void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the keys
     */
    public final IntArrayList getKeys() {
        return keys;
    }
    
}
