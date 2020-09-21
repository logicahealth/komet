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

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.collections.api.set.primitive.MutableIntSet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;


//~--- classes ----------------------------------------------------------------

/**
 * The Class NidSet.
 *
 * @author kec
 */
public class NidSet extends AbstractIntSet<NidSet> {

   /**
    * Instantiates a new nid set.
    */
   public NidSet() {}

   /**
    * Instantiates a new nid set.
    *
    * @param members the members
    */
   private NidSet(int[] members) {
      super(members);
   }

   /**
    * Instantiates a new nid set.
    * Dan notes, this implementation appears to be NOT THREAD SAFE.  No Parallel streams!
    *
    * @param memberStream the member stream
    */
   private NidSet(IntStream memberStream) {
      super(memberStream);
   }

   /**
    * Instantiates a new nid set.
    *
    * @param members the members
    */
   private NidSet(MutableIntSet members) {
      super(members);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Of.
    *
    * @param members the members
    * @return the nid set
    */
   public static NidSet of(Collection<ConceptSpecification> members) {
      return new NidSet(members.stream().mapToInt(i -> i.getNid()));
   }
   public static NidSet of(Stream<Integer> memberStream) {
      return new NidSet(memberStream.mapToInt(i -> i));
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the nid set
    */
   public static NidSet of(int... members) {
      return new NidSet(members);
   }
   
   public static NidSet of(Integer... members) {
      return new NidSet(Arrays.stream(members).mapToInt(i -> i));
   }
   
   public static NidSet of(ConceptSpecification... members) {
      return new NidSet(Arrays.stream(members).mapToInt(i -> i.getNid()));
   }

   /**
    * Dan notes, this implementation appears to be NOT THREAD SAFE.  No Parallel streams!
    *
    * @param memberStream the member stream
    * @return the nid set
    */
   public static NidSet of(IntStream memberStream) {
      return new NidSet(memberStream);
   }

   /**
    * Of.
    *
    * @param another the other NidSet
    * @return the nid set
    */
   public static NidSet of(NidSet another) {
      return new NidSet(another.stream());
   }

   /**
    * Of.
    *
    * @param members the members
    * @return the nid set
    */
   public static NidSet of(MutableIntSet members) {
      return new NidSet(members);
   }

   /**
    * Adds the all.
    *
    * @param otherSet the set to add
    */
   public void addAll(NidSet otherSet) {
      if (this.readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }
      this.or(otherSet);
   }

   public void addAll(int... values) {
      if (this.readOnly) {
         throw new UnsupportedOperationException("Read only set");
      }
      for (int value: values) {
          this.add(value);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      return toString((nid) -> Integer.toString(nid));
   }

   public Collection<ConceptSpecification> toConceptSpec() {
      ArrayList<ConceptSpecification> result = new ArrayList<>(this.size());
      this.stream().forEach(item -> result.add(new ConceptProxy(item)));
      return result;
   }


   public static NidSet of(DataInput input) throws IOException {
      final int size = input.readInt();
      NidSet nidSet = new NidSet();
      for (int i = 0; i < size; i++) {
         nidSet.add(input.readInt());
      }
      return nidSet;
   }

}

