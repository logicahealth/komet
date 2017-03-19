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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.set.OpenIntHashSet;

import au.csiro.ontology.Factory;
import au.csiro.ontology.model.Axiom;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.ConceptInclusion;
import au.csiro.ontology.model.Role;

import sh.isaac.api.logic.LogicNode;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithSequences;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/16/14.
 */

//TODO move to CSIRO specific module
public class AxiomAccumulator
         implements BiConsumer<Set<Axiom>, LogicalExpressionOchreImpl> {
   BitSet                     conceptSequences;
   Concept[]                  concepts;
   OpenIntObjectHashMap<Role> roles;
   OpenIntHashSet             neverGroupRoleSequences;
   int                        roleGroupConceptSequence;

   //~--- constructors --------------------------------------------------------

   public AxiomAccumulator(Concept[] concepts,
                           BitSet conceptSequences,
                           OpenIntObjectHashMap<Role> roles,
                           OpenIntHashSet neverGroupRoleSequences,
                           int roleGroupConceptSequence) {
      this.concepts                 = concepts;
      this.conceptSequences         = conceptSequences;
      this.roles                    = roles;
      this.neverGroupRoleSequences  = neverGroupRoleSequences;
      this.roleGroupConceptSequence = roleGroupConceptSequence;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void accept(Set<Axiom> axioms, LogicalExpressionOchreImpl logicGraphVersion) {
      if (conceptSequences.get(logicGraphVersion.getConceptSequence())) {
         axioms.addAll(generateAxioms(logicGraphVersion));
      }
   }

   public Set<Axiom> generateAxioms(LogicalExpressionOchreImpl logicGraphVersion) {
      Concept    thisConcept = concepts[logicGraphVersion.getConceptSequence()];
      Set<Axiom> axioms      = new HashSet<>();

      for (LogicNode setLogicNode: logicGraphVersion.getRoot()
            .getChildren()) {
         AndNode            andNode    = (AndNode) setLogicNode.getChildren()[0];
         ArrayList<Concept> definition = new ArrayList<>();

         for (LogicNode child: andNode.getChildren()) {
            switch (child.getNodeSemantic()) {
            case CONCEPT:
               ConceptNodeWithSequences conceptNode = (ConceptNodeWithSequences) child;

               definition.add(concepts[conceptNode.getConceptSequence()]);
               break;

            case ROLE_SOME:
               RoleNodeSomeWithSequences roleNodeSome = (RoleNodeSomeWithSequences) child;

               definition.add(processRole(roleNodeSome,
                                          concepts,
                                          roles,
                                          neverGroupRoleSequences,
                                          roleGroupConceptSequence));
               break;

            default:
               throw new UnsupportedOperationException("Can't handle " + child + " as child of AND");
            }
         }

         switch (setLogicNode.getNodeSemantic()) {
         case SUFFICIENT_SET:

            // if sufficient set, create a concept inclusion from the axioms to the concept
            axioms.add(
                new ConceptInclusion(Factory.createConjunction(definition.toArray(new Concept[definition.size()])),
                                     thisConcept));

         // No break; here, for sufficient set, need to add the reverse necessary set...
         case NECESSARY_SET:

            // if necessary set create a concept inclusion from the concept to the axioms
            axioms.add(new ConceptInclusion(thisConcept,
                                            Factory.createConjunction(
                                            definition.toArray(new Concept[definition.size()]))));
            break;

         default:
            throw new UnsupportedOperationException("Can't handle " + setLogicNode + " as child of root");
         }
      }

      return axioms;
   }

   private Concept processRole(RoleNodeSomeWithSequences roleNodeSome,
                               Concept[] concepts,
                               OpenIntObjectHashMap<Role> roles,
                               OpenIntHashSet neverGroupRoleSequences,
                               int roleGroupConceptSequence) {
      // need to handle grouped, and never grouped...
      if (neverGroupRoleSequences.contains(roleNodeSome.getTypeConceptSequence())) {
         return Factory.createExistential(roles.get(roleNodeSome.getTypeConceptSequence()),
                                          getConcept(roleNodeSome.getOnlyChild(),
                                                concepts,
                                                roles,
                                                neverGroupRoleSequences,
                                                roleGroupConceptSequence));
      }

      return Factory.createExistential(roles.get(roleGroupConceptSequence),
                                       getConcept(roleNodeSome,
                                             concepts,
                                             roles,
                                             neverGroupRoleSequences,
                                             roleGroupConceptSequence));
   }

   //~--- get methods ---------------------------------------------------------

   private Concept getConcept(LogicNode logicNode,
                              Concept[] concepts,
                              OpenIntObjectHashMap<Role> roles,
                              OpenIntHashSet neverGroupRoleSequences,
                              int roleGroupConceptSequence) {
      switch (logicNode.getNodeSemantic()) {
      case ROLE_SOME:
         RoleNodeSomeWithSequences roleNodeSome = (RoleNodeSomeWithSequences) logicNode;

         return Factory.createExistential(roles.get(roleNodeSome.getTypeConceptSequence()),
                                          getConcept(roleNodeSome.getOnlyChild(),
                                                concepts,
                                                roles,
                                                neverGroupRoleSequences,
                                                roleGroupConceptSequence));

      case CONCEPT:
         ConceptNodeWithSequences conceptNode = (ConceptNodeWithSequences) logicNode;

         return concepts[conceptNode.getConceptSequence()];

      case AND:
         return Factory.createConjunction(getConcepts(logicNode.getChildren(),
               concepts,
               roles,
               neverGroupRoleSequences,
               roleGroupConceptSequence));
      }

      throw new UnsupportedOperationException("Can't handle " + logicNode + " as child of ROLE_SOME.");
   }

   private Concept[] getConcepts(LogicNode[] logicNodes,
                                 Concept[] concepts,
                                 OpenIntObjectHashMap<Role> roles,
                                 OpenIntHashSet neverGroupRoleSequences,
                                 int roleGroupConceptSequence) {
      Concept[] returnValues = new Concept[concepts.length];

      for (int i = 0; i < concepts.length; i++) {
         returnValues[i] = getConcept(logicNodes[i],
                                      concepts,
                                      roles,
                                      neverGroupRoleSequences,
                                      roleGroupConceptSequence);
      }

      return returnValues;
   }
}

