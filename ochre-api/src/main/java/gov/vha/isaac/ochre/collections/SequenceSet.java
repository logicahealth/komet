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

import java.util.Collection;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 * @param <T>
 */
public class SequenceSet<T extends SequenceSet<T>> extends IntSet<T> {

    protected SequenceSet(boolean readOnly) {
        super(readOnly);
    }


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
    
    public static SequenceSet<?> of(int... members) {
        return new SequenceSet<>(members);
    }

    public static SequenceSet<?> of(OpenIntHashSet members) {
        return new SequenceSet<>(members);
    }
    
    public static SequenceSet<?> of(Collection<Integer> members) {
        return new SequenceSet<>(members.stream().mapToInt(i -> i));
    }

    public static SequenceSet<?> of(IntStream memberStream) {
        return new SequenceSet<>(memberStream);
    }

    public static SequenceSet<?> of(StampSequenceSet other) {
        return new SequenceSet<>(other.stream());
    }
    
}
