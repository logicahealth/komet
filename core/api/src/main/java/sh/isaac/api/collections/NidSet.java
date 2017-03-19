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
public class NidSet
        extends AbstractIntSet<NidSet> {
   public NidSet() {}

   private NidSet(int[] members) {
      super(members);
   }

   private NidSet(IntStream memberStream) {
      super(memberStream);
   }

   private NidSet(OpenIntHashSet members) {
      super(members);
   }

   //~--- methods -------------------------------------------------------------

   public static NidSet of(Collection<Integer> members) {
      return new NidSet(members.stream().mapToInt(i -> i));
   }

   public static NidSet of(ConceptSequenceSet conceptSequenceSet) {
      IdentifierService sp = Get.identifierService();

      return new NidSet(conceptSequenceSet.stream().map((sequence) -> sp.getConceptNid(sequence)));
   }

   public static NidSet of(int... members) {
      return new NidSet(members);
   }

   public static NidSet of(IntStream memberStream) {
      return new NidSet(memberStream);
   }

   public static NidSet of(OpenIntHashSet members) {
      return new NidSet(members);
   }

   public static NidSet of(SememeSequenceSet sememeSequenceSet) {
      IdentifierService sp = Get.identifierService();

      return new NidSet(sememeSequenceSet.stream().map((sequence) -> sp.getSememeNid(sequence)));
   }

   public static NidSet ofAllComponentNids() {
      return new NidSet(IntStream.rangeClosed(IdentifierService.FIRST_NID, Get.identifierService().getMaxNid()));
   }

   ;

   //~--- methods -------------------------------------------------------------

   @Override
   public String toString() {
      return toString((nid) -> Integer.toString(nid));
   }
}

