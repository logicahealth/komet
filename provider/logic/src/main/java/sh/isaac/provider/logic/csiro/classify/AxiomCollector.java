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



package sh.isaac.provider.logic.csiro.classify;

//~--- JDK imports ------------------------------------------------------------

import au.csiro.ontology.Factory;
import au.csiro.ontology.model.Axiom;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.Role;
import org.apache.mahout.math.set.OpenIntHashSet;
import sh.isaac.api.collections.IntObjectHashMap;
import sh.isaac.model.logic.LogicalExpressionImpl;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/16/14.
 */

//TODO move to CSIRO specific module
public class AxiomCollector
         implements Collector<LogicalExpressionImpl, Set<Axiom>, Set<Axiom>> {
   /** The concept nids. */
   BitSet conceptSequences;

   /** The concepts. */
   Concept[] concepts;

   /** The roles. */
   IntObjectHashMap<Role> roles;

   /** The never group role sequences. */
   OpenIntHashSet neverGroupRoleSequences;

   /** The role group concept nid. */
   int roleGroupConceptSequence;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new axiom collector.
    *
    * @param conceptSequences the concept nids
    * @param roleSequences the role sequences
    * @param neverGroupRoleSequences the never group role sequences
    * @param roleGroupConceptSequence the role group concept nid
    */
   public AxiomCollector(BitSet conceptSequences,
                         OpenIntHashSet roleSequences,
                         OpenIntHashSet neverGroupRoleSequences,
                         int roleGroupConceptSequence) {
      this.conceptSequences = conceptSequences;
      this.concepts         = new Concept[conceptSequences.length()];
      Arrays.parallelSetAll(this.concepts,
                            conceptSequence -> Factory.createNamedConcept(Integer.toString(conceptSequence)));
      this.roles = new IntObjectHashMap<>(roleSequences.size());
      roleSequences.forEachKey(roleSequence -> {
                                  this.roles.put(roleSequence, Factory.createNamedRole(Integer.toString(roleSequence)));
                                  return true;
                               });
      this.neverGroupRoleSequences  = neverGroupRoleSequences;
      this.roleGroupConceptSequence = roleGroupConceptSequence;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Accumulator.
    *
    * @return the bi consumer
    */
   @Override
   public BiConsumer<Set<Axiom>, LogicalExpressionImpl> accumulator() {
      return new AxiomAccumulator(this.concepts,
                                  this.conceptSequences,
                                  this.roles,
                                  this.neverGroupRoleSequences,
                                  this.roleGroupConceptSequence);
   }

   /**
    * Characteristics.
    *
    * @return the set
    */
   @Override
   public Set<Characteristics> characteristics() {
      return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
            Characteristics.IDENTITY_FINISH));
   }

   /**
    * Combiner.
    *
    * @return the binary operator
    */
   @Override
   public BinaryOperator<Set<Axiom>> combiner() {
      return (list1, list2) -> {
                list1.addAll(list2);
                return list1;
             };
   }

   /**
    * Finisher.
    *
    * @return the function
    */
   @Override
   public Function<Set<Axiom>, Set<Axiom>> finisher() {
      return Function.identity();
   }

   /**
    * Supplier.
    *
    * @return the supplier
    */
   @Override
   public Supplier<Set<Axiom>> supplier() {
      return HashSet::new;
   }
}

