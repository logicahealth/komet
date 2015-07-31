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

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 */
public class ConceptSequenceSet extends SequenceSet<ConceptSequenceSet> {
    
    public final static ConceptSequenceSet EMPTY = new ConceptSequenceSet(true);

    private ConceptSequenceSet(boolean readOnly) {
        super(readOnly);
    }
    
    public static ConceptSequenceSet of(int... members) {
        IdentifierService sp = Get.identifierService();
        return new ConceptSequenceSet(Arrays.stream(members).map((id) -> sp.getConceptSequence(id)));
    }

    public static ConceptSequenceSet of(OpenIntHashSet members) {
        return new ConceptSequenceSet(members.keys().elements());
    }
    
    public static ConceptSequenceSet of(Collection<Integer> members) {
        IdentifierService sp = Get.identifierService();
        return new ConceptSequenceSet(members.stream().mapToInt((id) -> sp.getConceptSequence(id)));
    }

    public static ConceptSequenceSet of(IntStream memberStream) {
        return new ConceptSequenceSet(memberStream);
    }

    public static ConceptSequenceSet of(ConceptSequenceSet another) {
        return new ConceptSequenceSet(another.stream());
    }

    public static ConceptSequenceSet ofAllConceptSequences() {
        return new ConceptSequenceSet(Get.identifierService().getConceptSequenceStream());
    }

    public ConceptSequenceSet() {
    }
    
    public static ConceptSequenceSet of(NidSet nidSet) {
        IdentifierService sp = Get.identifierService();
        return new ConceptSequenceSet(nidSet.stream()
                .map((nid) -> sp.getConceptSequence(nid)));
    }
    protected ConceptSequenceSet(IntStream memberStream) {
        super(memberStream);
    }

    public ConceptSequenceSet(int[] members) {
        super(members);
    }

    protected ConceptSequenceSet(OpenIntHashSet members) {
        super(members);
    }
    
    @Override
    public void add(int item) {
        rbmp.add(Get.identifierService().getConceptSequence(item));
    }

    @Override
    public boolean contains(int item) {
        return super.contains(Get.identifierService().getConceptSequence(item)); 
    }

    @Override
    public void remove(int item) {
        super.remove(Get.identifierService().getConceptSequence(item)); 
    }

    @Override
    public void addAll(IntStream intStream) {
        IdentifierService sp = Get.identifierService();
        super.addAll(intStream.map((item) -> { return sp.getConceptSequence(item);})); 
    }
    
    public List<ConceptSpecification> toConceptSpecificationList() {
       return stream().mapToObj((int conceptSequence) -> new ConceptProxy(
                Get.conceptDescriptionText(conceptSequence),
                Get.identifierService().getUuidPrimordialFromConceptSequence(conceptSequence).get())).collect(Collectors.toList()) ;
    }
    
    
}
