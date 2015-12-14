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
import java.util.UUID;
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

	public ConceptSequenceSet() {
	}

	protected ConceptSequenceSet(Concurrency concurrency) {
		super(concurrency);
	}

	public static ConceptSequenceSet concurrent() {
		return new ConceptSequenceSet(Concurrency.THREAD_SAFE);
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

	public static ConceptSequenceSet of(NidSet nidSet) {
		IdentifierService ids = Get.identifierService();
		return new ConceptSequenceSet(nidSet.stream()
				  .map((nid) -> ids.getConceptSequence(nid)));
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
		super.add(Get.identifierService().getConceptSequence(item));
	}
        
                 public void add(UUID conceptUuid) {
                     super.add(Get.identifierService().getConceptSequenceForUuids(conceptUuid));
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
		super.addAll(intStream.map((item) -> {
			return sp.getConceptSequence(item);
		}));
	}

	public List<ConceptSpecification> toConceptSpecificationList() {
		return stream().mapToObj((int conceptSequence) -> new ConceptProxy(
				  Get.conceptDescriptionText(conceptSequence),
				  Get.identifierService().getUuidPrimordialFromConceptSequence(conceptSequence).get())).collect(Collectors.toList());
	}

	public void addAll(OpenIntHashSet conceptsReferencedAtNodeOrAbove) {
		IdentifierService ids = Get.identifierService();
		conceptsReferencedAtNodeOrAbove.forEachKey((id) -> {
			super.add(ids.getConceptSequence(id));
			return true;
		});
	}

	@Override
	public String toString() {
		return toString((conceptSequence) -> Get.conceptDescriptionText(conceptSequence));
	}

}
