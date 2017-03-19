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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.set.OpenIntHashSet;

import au.csiro.ontology.Factory;
import au.csiro.ontology.model.Axiom;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.Role;

import sh.isaac.model.logic.LogicalExpressionOchreImpl;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/16/14.
 */

//TODO move to CSIRO specific module
public class AxiomCollector
         implements Collector<LogicalExpressionOchreImpl, Set<Axiom>, Set<Axiom>> {
   BitSet                     conceptSequences;
   Concept[]                  concepts;
   OpenIntObjectHashMap<Role> roles;
   OpenIntHashSet             neverGroupRoleSequences;
   int                        roleGroupConceptSequence;

   //~--- constructors --------------------------------------------------------

   public AxiomCollector(BitSet conceptSequences,
                         OpenIntHashSet roleSequences,
                         OpenIntHashSet neverGroupRoleSequences,
                         int roleGroupConceptSequence) {
      this.conceptSequences = conceptSequences;
      this.concepts         = new Concept[conceptSequences.length()];
      Arrays.parallelSetAll(this.concepts,
                            conceptSequence -> Factory.createNamedConcept(Integer.toString(conceptSequence)));
      this.roles = new OpenIntObjectHashMap<>(roleSequences.size());
      roleSequences.forEachKey(roleSequence -> {
                                  this.roles.put(roleSequence, Factory.createNamedRole(Integer.toString(roleSequence)));
                                  return true;
                               });
      this.neverGroupRoleSequences  = neverGroupRoleSequences;
      this.roleGroupConceptSequence = roleGroupConceptSequence;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public BiConsumer<Set<Axiom>, LogicalExpressionOchreImpl> accumulator() {
      return new AxiomAccumulator(this.concepts, this.conceptSequences, this.roles, this.neverGroupRoleSequences, this.roleGroupConceptSequence);
   }

   @Override
   public Set<Characteristics> characteristics() {
      return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
            Characteristics.IDENTITY_FINISH));
   }

   @Override
   public BinaryOperator<Set<Axiom>> combiner() {
      return (list1, list2) -> {
                list1.addAll(list2);
                return list1;
             };
   }

   @Override
   public Function<Set<Axiom>, Set<Axiom>> finisher() {
      return Function.identity();
   }

   @Override
   public Supplier<Set<Axiom>> supplier() {
      return HashSet::new;
   }
}

