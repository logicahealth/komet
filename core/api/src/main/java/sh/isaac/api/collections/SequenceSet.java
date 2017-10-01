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

//~--- classes ----------------------------------------------------------------

/**
 * The Class SequenceSet.
 *
 * @author kec
 * @param <T> the generic type
 */
public class SequenceSet<T extends SequenceSet<T>>
        extends AbstractIntSet<T> {
   /**
    * Instantiates a new sequence set.
    */
   public SequenceSet() {
      super();
   }

   /**
    * Instantiates a new sequence set.
    *
    * @param readOnly the read only
    */
   protected SequenceSet(boolean readOnly) {
      super(readOnly);
   }

   /**
    * Instantiates a new sequence set.
    *
    * @param concurrency the concurrency
    */
   protected SequenceSet(Concurrency concurrency) {
      super(concurrency);
   }

   /**
    * Instantiates a new sequence set.
    *
    * @param members the members
    */
   protected SequenceSet(int... members) {
      super(members);
   }

   /**
    * Instantiates a new sequence set.
    *
    * @param memberStream the member stream
    */
   protected SequenceSet(IntStream memberStream) {
      super(memberStream);
   }

   /**
    * Instantiates a new sequence set.
    *
    * @param members the members
    */
   protected SequenceSet(OpenIntHashSet members) {
      super(members);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Concurrent.
    *
    * @return the sequence set
    */
   public static SequenceSet concurrent() {
      return new SequenceSet(Concurrency.THREAD_SAFE);
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the sequence set
    */
   public static SequenceSet<?> of(Collection<Integer> members) {
      return new SequenceSet<>(members.stream().mapToInt(i -> i));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the sequence set
    */
   public static SequenceSet<?> of(int... members) {
      return new SequenceSet<>(members);
   }

   /**
    * Of.
    *
    * @param memberStream the member stream
    * @return the sequence set
    */
   public static SequenceSet<?> of(IntStream memberStream) {
      return new SequenceSet<>(memberStream);
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the sequence set
    */
   public static SequenceSet<?> of(OpenIntHashSet members) {
      return new SequenceSet<>(members);
   }

   /**
    * Of.
    *
    * @param other the other
    * @return the sequence set
    */
   public static SequenceSet<?> of(StampSequenceSet other) {
      return new SequenceSet<>(other.stream());
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return toString((sequence) -> Integer.toString(sequence));
   }
}

