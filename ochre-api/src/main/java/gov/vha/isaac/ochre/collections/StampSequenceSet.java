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
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 */
public class StampSequenceSet extends SequenceSet {
    
    
    public static StampSequenceSet of(int... members) {
        return new StampSequenceSet(members);
    }

    public static StampSequenceSet of(OpenIntHashSet members) {
        return new StampSequenceSet(members);
    }
    
    public static StampSequenceSet of(Collection<Integer> members) {
        return new StampSequenceSet(members.stream().mapToInt(i -> i));
    }

    public static StampSequenceSet of(IntStream memberStream) {
        return new StampSequenceSet(memberStream);
    }

    public StampSequenceSet() {
    }
    

    protected StampSequenceSet(IntStream memberStream) {
        super(memberStream);
    }

    protected StampSequenceSet(int[] members) {
        super(members);
    }

    protected StampSequenceSet(OpenIntHashSet members) {
        super(members);
    }
}