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
 */
public class NidSet extends AbstractIntSet<NidSet> {
    
    public static NidSet of(int... members) {
        return new NidSet(members);
    }

    public static NidSet of(OpenIntHashSet members) {
        return new NidSet(members);
    }
    public static NidSet of(IntStream memberStream) {
        return new NidSet(memberStream);
    }
    public static NidSet ofAllComponentNids() {
        return new NidSet(Get.identifierService().getComponentNidStream());
    }
    
    public static NidSet of(Collection<Integer> members) {
        return new NidSet(members.stream().mapToInt(i -> i));
    }
    public static NidSet of(ConceptSequenceSet conceptSequenceSet) {
        IdentifierService sp = Get.identifierService();
        return new NidSet(conceptSequenceSet.stream()
                .map((sequence) -> sp.getConceptNid(sequence)));
    }

    public static NidSet of(SememeSequenceSet sememeSequenceSet) {
        IdentifierService sp = Get.identifierService();
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
	 
	@Override
	public String toString() {
		return toString((nid) -> Integer.toString(nid));
	}
	 
}
