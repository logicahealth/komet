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
 * The Class SememeSequenceSet.
 *
 * @author kec
 */
public class SememeSequenceSet
        extends SequenceSet<SememeSequenceSet> {
   
   /**
    * Instantiates a new sememe sequence set.
    */
   public SememeSequenceSet() {}

   /**
    * Instantiates a new sememe sequence set.
    *
    * @param concurrency the concurrency
    */
   protected SememeSequenceSet(Concurrency concurrency) {
      super(concurrency);
   }

   /**
    * Instantiates a new sememe sequence set.
    *
    * @param members the members
    */
   protected SememeSequenceSet(int[] members) {
      super(members);
   }

   /**
    * Instantiates a new sememe sequence set.
    *
    * @param memberStream the member stream
    */
   protected SememeSequenceSet(IntStream memberStream) {
      super(memberStream);
   }

   /**
    * Instantiates a new sememe sequence set.
    *
    * @param members the members
    */
   protected SememeSequenceSet(OpenIntHashSet members) {
      super(members);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the.
    *
    * @param sememeUuid the sememe uuid
    */
   public void add(UUID sememeUuid) {
      super.add(Get.identifierService()
                   .getSememeSequenceForUuids(sememeUuid));
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
                                          .getSememeSequence(item);
                                 }));
   }

   /**
    * Concurrent.
    *
    * @return the sememe sequence set
    */
   public static SememeSequenceSet concurrent() {
      return new SememeSequenceSet(Concurrency.THREAD_SAFE);
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
                               .getSememeSequence(item));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the sememe sequence set
    */
   public static SememeSequenceSet of(Collection<Integer> members) {
      return new SememeSequenceSet(members.stream().mapToInt((i) -> i));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the sememe sequence set
    */
   public static SememeSequenceSet of(int... members) {
      return new SememeSequenceSet(members);
   }

   /**
    * Of.
    *
    * @param sememeSquenceStream the sememe squence stream
    * @return the sememe sequence set
    */
   public static SememeSequenceSet of(IntStream sememeSquenceStream) {
      return new SememeSequenceSet(sememeSquenceStream);
   }

   /**
    * Of.
    *
    * @param sememeNidSet the sememe nid set
    * @return the sememe sequence set
    */
   public static SememeSequenceSet of(NidSet sememeNidSet) {
      final IdentifierService sp = Get.identifierService();

      return new SememeSequenceSet(sememeNidSet.stream().map((nid) -> sp.getSememeSequence(nid)));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the sememe sequence set
    */
   public static SememeSequenceSet of(OpenIntHashSet members) {
      return new SememeSequenceSet(members);
   }

   /**
    * Of.
    *
    * @param sememeSquenceSet the sememe squence set
    * @return the sememe sequence set
    */
   public static SememeSequenceSet of(SememeSequenceSet sememeSquenceSet) {
      return new SememeSequenceSet(sememeSquenceSet.stream());
   }

   /**
    * Of all sememe sequences.
    *
    * @return the sememe sequence set
    */
   public static SememeSequenceSet ofAllSememeSequences() {
      return new SememeSequenceSet(Get.identifierService().getSememeSequenceStream());
   }

   /**
    * Removes the.
    *
    * @param item the item
    */
   @Override
   public void remove(int item) {
      super.remove(Get.identifierService()
                      .getSememeSequence(item));
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return toString((sememeSequence) -> Integer.toString(sememeSequence));
   }
}

