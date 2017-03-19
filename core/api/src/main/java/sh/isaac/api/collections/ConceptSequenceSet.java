/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.collections;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ConceptSequenceSet
        extends SequenceSet<ConceptSequenceSet> {
   public final static ConceptSequenceSet EMPTY = new ConceptSequenceSet(true);

   //~--- constructors --------------------------------------------------------

   public ConceptSequenceSet() {}

   private ConceptSequenceSet(boolean readOnly) {
      super(readOnly);
   }

   protected ConceptSequenceSet(Concurrency concurrency) {
      super(concurrency);
   }

   public ConceptSequenceSet(int[] members) {
      super(members);
   }

   protected ConceptSequenceSet(IntStream memberStream) {
      super(memberStream);
   }

   protected ConceptSequenceSet(OpenIntHashSet members) {
      super(members);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void add(int item) {
      super.add(Get.identifierService()
                   .getConceptSequence(item));
   }

   public void add(UUID conceptUuid) {
      super.add(Get.identifierService()
                   .getConceptSequenceForUuids(conceptUuid));
   }

   @Override
   public void addAll(IntStream intStream) {
      final IdentifierService sp = Get.identifierService();

      super.addAll(intStream.map((item) -> {
                                    return sp.getConceptSequence(item);
                                 }));
   }

   public void addAll(OpenIntHashSet conceptsReferencedAtNodeOrAbove) {
      final IdentifierService ids = Get.identifierService();

      conceptsReferencedAtNodeOrAbove.forEachKey((id) -> {
               super.add(ids.getConceptSequence(id));
               return true;
            });
   }

   public static ConceptSequenceSet concurrent() {
      return new ConceptSequenceSet(Concurrency.THREAD_SAFE);
   }

   @Override
   public boolean contains(int item) {
      return super.contains(Get.identifierService()
                               .getConceptSequence(item));
   }

   public static ConceptSequenceSet of(Collection<Integer> members) {
      final IdentifierService sp = Get.identifierService();

      return new ConceptSequenceSet(members.stream().mapToInt((id) -> sp.getConceptSequence(id)));
   }

   public static ConceptSequenceSet of(ConceptSequenceSet another) {
      return new ConceptSequenceSet(another.stream());
   }

   public static ConceptSequenceSet of(int... members) {
      final IdentifierService sp = Get.identifierService();

      return new ConceptSequenceSet(Arrays.stream(members).map((id) -> sp.getConceptSequence(id)));
   }

   public static ConceptSequenceSet of(IntStream memberStream) {
      return new ConceptSequenceSet(memberStream);
   }

   public static ConceptSequenceSet of(NidSet nidSet) {
      final IdentifierService ids = Get.identifierService();

      return new ConceptSequenceSet(nidSet.stream().map((nid) -> ids.getConceptSequence(nid)));
   }

   public static ConceptSequenceSet of(OpenIntHashSet members) {
      return new ConceptSequenceSet(members.keys().elements());
   }

   public static ConceptSequenceSet ofAllConceptSequences() {
      return new ConceptSequenceSet(Get.identifierService().getConceptSequenceStream());
   }

   @Override
   public void remove(int item) {
      super.remove(Get.identifierService()
                      .getConceptSequence(item));
   }

   public List<ConceptSpecification> toConceptSpecificationList() {
      return stream().mapToObj((int conceptSequence) -> new ConceptProxy(Get.conceptDescriptionText(conceptSequence),
            Get.identifierService().getUuidPrimordialFromConceptId(conceptSequence).get()))
                     .collect(Collectors.toList());
   }

   @Override
   public String toString() {
      return toString((conceptSequence) -> Get.conceptDescriptionText(conceptSequence));
   }
}

