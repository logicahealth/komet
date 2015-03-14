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
import java.util.Collection;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 */
public class SememeSequenceSet extends SequenceSet {
    
    public static SememeSequenceSet of(int... members) {
        return new SememeSequenceSet(members);
    }
    
    public static SememeSequenceSet of(OpenIntHashSet members) {
        return new SememeSequenceSet(members);
    }
    
    public static SememeSequenceSet of(Collection<Integer> members) {
        return new SememeSequenceSet(members.stream().mapToInt((i) -> i));
    }
    
    public static SememeSequenceSet of(NidSet sememeNidSet) {
        SequenceProvider sp = getSequenceProvider();
        return new SememeSequenceSet(sememeNidSet.stream().map((nid) -> sp.getSememeSequence(nid)));
    }
    
    public SememeSequenceSet() {
    }
    
    protected SememeSequenceSet(IntStream memberStream) {
        super(memberStream);
    }
    
    protected SememeSequenceSet(int[] members) {
        super(members);
    }
    
    protected SememeSequenceSet(OpenIntHashSet members) {
        super(members);
    }    
}
