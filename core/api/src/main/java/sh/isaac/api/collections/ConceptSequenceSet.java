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
 * The Class ConceptSequenceSet.
 *
 * @author kec
 */
public class ConceptSequenceSet
        extends SequenceSet<ConceptSequenceSet> {
   /** The Constant EMPTY. */
   public final static ConceptSequenceSet EMPTY = new ConceptSequenceSet(true);

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concept sequence set.
    */
   public ConceptSequenceSet() {}

   /**
    * Instantiates a new concept sequence set.
    *
    * @param readOnly the read only
    */
   private ConceptSequenceSet(boolean readOnly) {
      super(readOnly);
   }

   /**
    * Instantiates a new concept sequence set.
    *
    * @param concurrency the concurrency
    */
   protected ConceptSequenceSet(Concurrency concurrency) {
      super(concurrency);
   }

   /**
    * Instantiates a new concept sequence set.
    *
    * @param members the members
    */
   public ConceptSequenceSet(int[] members) {
      super(members);
   }

   /**
    * Instantiates a new concept sequence set.
    *
    * @param memberStream the member stream
    */
   public ConceptSequenceSet(IntStream memberStream) {
      super(memberStream);
   }

   /**
    * Instantiates a new concept sequence set.
    *
    * @param members the members
    */
   protected ConceptSequenceSet(OpenIntHashSet members) {
      super(members);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the.
    *
    * @param item the item
    */
   @Override
   public void add(int item) {
      super.add(Get.identifierService()
                   .getConceptSequence(item));
   }

   /**
    * Adds the.
    *
    * @param conceptUuid the concept uuid
    */
   public void add(UUID conceptUuid) {
      super.add(Get.identifierService()
                   .getConceptSequenceForUuids(conceptUuid));
   }

   /**
    * Adds the all.
    *
    * @param intStream the int stream
    */
   @Override
   public void addAll(IntStream intStream) {
      final IdentifierService sp = Get.identifierService();

      super.addAll(intStream.map((item) -> {
                                    return sp.getConceptSequence(item);
                                 }));
   }

   /**
    * Adds the all.
    *
    * @param conceptsReferencedAtNodeOrAbove the concepts referenced at node or above
    */
   public void addAll(OpenIntHashSet conceptsReferencedAtNodeOrAbove) {
      final IdentifierService ids = Get.identifierService();

      conceptsReferencedAtNodeOrAbove.forEachKey((id) -> {
               super.add(ids.getConceptSequence(id));
               return true;
            });
   }

   /**
    * Concurrent.
    *
    * @return the concept sequence set
    */
   public static ConceptSequenceSet concurrent() {
      return new ConceptSequenceSet(Concurrency.THREAD_SAFE);
   }

   /**
    * Contains.
    *
    * @param item the item
    * @return true, if successful
    */
   @Override
   public boolean contains(int item) {
      return super.contains(Get.identifierService()
                               .getConceptSequence(item));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the concept sequence set
    */
   public static ConceptSequenceSet of(Collection<Integer> members) {
      final IdentifierService sp = Get.identifierService();

      return new ConceptSequenceSet(members.stream().mapToInt((id) -> sp.getConceptSequence(id)));
   }

   /**
    * Of.
    *
    * @param another the another
    * @return the concept sequence set
    */
   public static ConceptSequenceSet of(ConceptSequenceSet another) {
      return new ConceptSequenceSet(another.stream());
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the concept sequence set
    */
   public static ConceptSequenceSet of(int... members) {
      final IdentifierService sp = Get.identifierService();

      return new ConceptSequenceSet(Arrays.stream(members).map((id) -> sp.getConceptSequence(id)));
   }

   /**
    * Of.
    *
    * @param memberStream the member stream
    * @return the concept sequence set
    */
   public static ConceptSequenceSet of(IntStream memberStream) {
      return new ConceptSequenceSet(memberStream);
   }

   /**
    * Of.
    *
    * @param nidSet the nid set
    * @return the concept sequence set
    */
   public static ConceptSequenceSet of(NidSet nidSet) {
      final IdentifierService ids = Get.identifierService();

      return new ConceptSequenceSet(nidSet.stream().map((nid) -> ids.getConceptSequence(nid)));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the concept sequence set
    */
   public static ConceptSequenceSet of(OpenIntHashSet members) {
      return new ConceptSequenceSet(members.keys().elements());
   }

   /**
    * Of all concept sequences.
    *
    * @return the concept sequence set
    */
   public static ConceptSequenceSet ofAllConceptSequences() {
      return new ConceptSequenceSet(Get.identifierService().getConceptSequenceStream());
   }

   /**
    * Removes the.
    *
    * @param item the item
    */
   @Override
   public void remove(int item) {
      super.remove(Get.identifierService()
                      .getConceptSequence(item));
   }

   /**
    * To concept specification list.
    *
    * @return the list
    */
   public List<ConceptSpecification> toConceptSpecificationList() {
      return stream().mapToObj((int conceptSequence) -> new ConceptProxy(Get.conceptDescriptionText(conceptSequence),
            Get.identifierService().getUuidPrimordialFromConceptId(conceptSequence).get()))
                     .collect(Collectors.toList());
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return toString((conceptSequence) -> Get.conceptDescriptionText(conceptSequence));
   }
}

