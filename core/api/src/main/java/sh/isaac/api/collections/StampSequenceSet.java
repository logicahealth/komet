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

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class StampSequenceSet
        extends SequenceSet<StampSequenceSet> {
   public StampSequenceSet() {}

   protected StampSequenceSet(Concurrency concurrency) {
      super(concurrency);
   }

   protected StampSequenceSet(int[] members) {
      super(members);
   }

   protected StampSequenceSet(IntStream memberStream) {
      super(memberStream);
   }

   protected StampSequenceSet(OpenIntHashSet members) {
      super(members);
   }

   //~--- methods -------------------------------------------------------------

   public static StampSequenceSet concurrent() {
      return new StampSequenceSet(Concurrency.THREAD_SAFE);
   }

   public static StampSequenceSet of(Collection<Integer> members) {
      return new StampSequenceSet(members.stream().mapToInt(i -> i));
   }

   public static StampSequenceSet of(int... members) {
      return new StampSequenceSet(members);
   }

   public static StampSequenceSet of(IntStream memberStream) {
      return new StampSequenceSet(memberStream);
   }

   public static StampSequenceSet of(OpenIntHashSet members) {
      return new StampSequenceSet(members);
   }

   public static StampSequenceSet of(StampSequenceSet other) {
      return new StampSequenceSet(other.stream());
   }

   @Override
   public String toString() {
      return toString((stampSequence) -> Get.stampService()
            .describeStampSequence(stampSequence));
   }
}

