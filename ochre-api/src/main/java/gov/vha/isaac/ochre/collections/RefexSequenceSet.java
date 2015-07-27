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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.IdentifierService;
import java.util.Collection;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 * @deprecated migrate to Sememes...
 */
@Deprecated
public class RefexSequenceSet extends SequenceSet<RefexSequenceSet> {
    
    public static RefexSequenceSet of(int... members) {
        return new RefexSequenceSet(members);
    }
    
    public static RefexSequenceSet of(OpenIntHashSet members) {
        return new RefexSequenceSet(members);
    }
    
    public static RefexSequenceSet of(Collection<Integer> members) {
        return new RefexSequenceSet(members.stream().mapToInt((i) -> i));
    }
    
    public static RefexSequenceSet of(NidSet refexNidSet) {
        IdentifierService sp = Get.identifierService();
        return new RefexSequenceSet(refexNidSet.stream().map((nid) -> sp.getRefexSequence(nid)));
    }
    
    public static RefexSequenceSet of(IntStream refexSquenceStream) {
        return new RefexSequenceSet(refexSquenceStream);
    }
    
    public RefexSequenceSet() {
    }
    
    protected RefexSequenceSet(IntStream memberStream) {
        super(memberStream);
    }
    
    protected RefexSequenceSet(int[] members) {
        super(members);
    }
    
    protected RefexSequenceSet(OpenIntHashSet members) {
        super(members);
    }    
}