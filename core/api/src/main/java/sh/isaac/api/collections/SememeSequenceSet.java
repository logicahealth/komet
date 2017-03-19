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
 *
 * @author kec
 */
public class SememeSequenceSet
        extends SequenceSet<SememeSequenceSet> {
   public SememeSequenceSet() {}

   protected SememeSequenceSet(Concurrency concurrency) {
      super(concurrency);
   }

   protected SememeSequenceSet(int[] members) {
      super(members);
   }

   protected SememeSequenceSet(IntStream memberStream) {
      super(memberStream);
   }

   protected SememeSequenceSet(OpenIntHashSet members) {
      super(members);
   }

   //~--- methods -------------------------------------------------------------

   public void add(UUID sememeUuid) {
      super.add(Get.identifierService()
                   .getSememeSequenceForUuids(sememeUuid));
   }

   @Override
   public void addAll(IntStream intStream) {
      super.addAll(intStream.map((item) -> {
                                    return Get.identifierService()
                                          .getSememeSequence(item);
                                 }));
   }

   public static SememeSequenceSet concurrent() {
      return new SememeSequenceSet(Concurrency.THREAD_SAFE);
   }

   @Override
   public boolean contains(int item) {
      return super.contains(Get.identifierService()
                               .getSememeSequence(item));
   }

   public static SememeSequenceSet of(Collection<Integer> members) {
      return new SememeSequenceSet(members.stream().mapToInt((i) -> i));
   }

   public static SememeSequenceSet of(int... members) {
      return new SememeSequenceSet(members);
   }

   public static SememeSequenceSet of(IntStream sememeSquenceStream) {
      return new SememeSequenceSet(sememeSquenceStream);
   }

   public static SememeSequenceSet of(NidSet sememeNidSet) {
      IdentifierService sp = Get.identifierService();

      return new SememeSequenceSet(sememeNidSet.stream().map((nid) -> sp.getSememeSequence(nid)));
   }

   public static SememeSequenceSet of(OpenIntHashSet members) {
      return new SememeSequenceSet(members);
   }

   public static SememeSequenceSet of(SememeSequenceSet sememeSquenceSet) {
      return new SememeSequenceSet(sememeSquenceSet.stream());
   }

   public static SememeSequenceSet ofAllSememeSequences() {
      return new SememeSequenceSet(Get.identifierService().getSememeSequenceStream());
   }

   @Override
   public void remove(int item) {
      super.remove(Get.identifierService()
                      .getSememeSequence(item));
   }

   @Override
   public String toString() {
      return toString((sememeSequence) -> Integer.toString(sememeSequence));
   }
}

