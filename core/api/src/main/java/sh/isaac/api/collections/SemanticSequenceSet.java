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

import java.util.Collection;
import java.util.UUID;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;

//~--- classes ----------------------------------------------------------------

/**
 * The Class SemanticSequenceSet.
 *
 * @author kec
 */
public class SemanticSequenceSet
        extends SequenceSet<SemanticSequenceSet> {
   /**
    * Instantiates a new semantic sequence set.
    */
   public SemanticSequenceSet() {}

   /**
    * Instantiates a new semantic sequence set.
    *
    * @param concurrency the concurrency
    */
   protected SemanticSequenceSet(Concurrency concurrency) {
      super(concurrency);
   }

   /**
    * Instantiates a new semantic sequence set.
    *
    * @param members the members
    */
   protected SemanticSequenceSet(int[] members) {
      super(members);
   }

   /**
    * Instantiates a new semantic sequence set.
    *
    * @param memberStream the member stream
    */
   protected SemanticSequenceSet(IntStream memberStream) {
      super(memberStream);
   }

   /**
    * Instantiates a new semantic sequence set.
    *
    * @param members the members
    */
   protected SemanticSequenceSet(OpenIntHashSet members) {
      super(members);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void add(int item) {
      super.add(Get.identifierService()
              .getSemanticSequence(item)); 
   }

   /**
    * Adds the.
    *
    * @param semanticUuid the semantic uuid
    */
   public void add(UUID semanticUuid) {
      super.add(Get.identifierService()
              .getSemanticSequenceForUuids(semanticUuid));
   }

   /**
    * Adds the all.
    *
    * @param intStream the int stream
    */
   @Override
   public void addAll(IntStream intStream) {
      super.addAll(intStream.map((item) -> {
                                    return Get.identifierService()
                                          .getSemanticSequence(item);
                                 }));
   }

   /**
    * Concurrent.
    *
    * @return the semantic sequence set
    */
   public static SemanticSequenceSet concurrent() {
      return new SemanticSequenceSet(Concurrency.THREAD_SAFE);
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
                               .getSemanticSequence(item));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the semantic sequence set
    */
   public static SemanticSequenceSet of(Collection<Integer> members) {
      return new SemanticSequenceSet(members.stream().mapToInt((i) -> i));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the semantic sequence set
    */
   public static SemanticSequenceSet of(int... members) {
      return new SemanticSequenceSet(members);
   }

   /**
    * Of.
    *
    * @param semanticSquenceStream the semantic sequence stream
    * @return the semantic sequence set
    */
   public static SemanticSequenceSet of(IntStream semanticSquenceStream) {
      return new SemanticSequenceSet(semanticSquenceStream);
   }

   /**
    * Of.
    *
    * @param semanticNidSet the semantic nid set
    * @return the semantic sequence set
    */
   public static SemanticSequenceSet of(NidSet semanticNidSet) {
      final IdentifierService sp = Get.identifierService();

      return new SemanticSequenceSet(semanticNidSet.stream().map((nid) -> sp.getSemanticSequence(nid)));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the semantic sequence set
    */
   public static SemanticSequenceSet of(OpenIntHashSet members) {
      return new SemanticSequenceSet(members);
   }

   /**
    * Of.
    *
    * @param semanticSquenceSet the semantic squence set
    * @return the semantic sequence set
    */
   public static SemanticSequenceSet of(SemanticSequenceSet semanticSquenceSet) {
      return new SemanticSequenceSet(semanticSquenceSet.stream());
   }

   /**
    * Of all semantic sequences.
    *
    * @return the semantic sequence set
    */
   public static SemanticSequenceSet ofAllSemanticSequences() {
      return new SemanticSequenceSet(Get.identifierService().getSemanticSequenceStream());
   }

   /**
    * Removes the.
    *
    * @param item the item
    */
   @Override
   public void remove(int item) {
      super.remove(Get.identifierService()
                      .getSemanticSequence(item));
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return toString((semanticSequence) -> Integer.toString(semanticSequence));
   }
}

