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
   
   /** The concept sequences. */
   BitSet                     conceptSequences;
   
   /** The concepts. */
   Concept[]                  concepts;
   
   /** The roles. */
   OpenIntObjectHashMap<Role> roles;
   
   /** The never group role sequences. */
   OpenIntHashSet             neverGroupRoleSequences;
   
   /** The role group concept sequence. */
   int                        roleGroupConceptSequence;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new axiom accumulator.
    *
    * @param concepts the concepts
    * @param conceptSequences the concept sequences
    * @param roles the roles
    * @param neverGroupRoleSequences the never group role sequences
    * @param roleGroupConceptSequence the role group concept sequence
    */
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

   /**
    * Accept.
    *
    * @param axioms the axioms
    * @param logicGraphVersion the logic graph version
    */
   @Override
   public void accept(Set<Axiom> axioms, LogicalExpressionOchreImpl logicGraphVersion) {
      if (this.conceptSequences.get(logicGraphVersion.getConceptSequence())) {
         axioms.addAll(generateAxioms(logicGraphVersion));
      }
   }

   /**
    * Generate axioms.
    *
    * @param logicGraphVersion the logic graph version
    * @return the set
    */
   public Set<Axiom> generateAxioms(LogicalExpressionOchreImpl logicGraphVersion) {
      final Concept    thisConcept = this.concepts[logicGraphVersion.getConceptSequence()];
      final Set<Axiom> axioms      = new HashSet<>();

      for (final LogicNode setLogicNode: logicGraphVersion.getRoot()
            .getChildren()) {
         final AndNode            andNode    = (AndNode) setLogicNode.getChildren()[0];
         final ArrayList<Concept> definition = new ArrayList<>();

         for (final LogicNode child: andNode.getChildren()) {
            switch (child.getNodeSemantic()) {
            case CONCEPT:
               final ConceptNodeWithSequences conceptNode = (ConceptNodeWithSequences) child;

               definition.add(this.concepts[conceptNode.getConceptSequence()]);
               break;

            case ROLE_SOME:
               final RoleNodeSomeWithSequences roleNodeSome = (RoleNodeSomeWithSequences) child;

               definition.add(processRole(roleNodeSome,
                                          this.concepts,
                                          this.roles,
                                          this.neverGroupRoleSequences,
                                          this.roleGroupConceptSequence));
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

   /**
    * Process role.
    *
    * @param roleNodeSome the role node some
    * @param concepts the concepts
    * @param roles the roles
    * @param neverGroupRoleSequences the never group role sequences
    * @param roleGroupConceptSequence the role group concept sequence
    * @return the concept
    */
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

   /**
    * Gets the concept.
    *
    * @param logicNode the logic node
    * @param concepts the concepts
    * @param roles the roles
    * @param neverGroupRoleSequences the never group role sequences
    * @param roleGroupConceptSequence the role group concept sequence
    * @return the concept
    */
   private Concept getConcept(LogicNode logicNode,
                              Concept[] concepts,
                              OpenIntObjectHashMap<Role> roles,
                              OpenIntHashSet neverGroupRoleSequences,
                              int roleGroupConceptSequence) {
      switch (logicNode.getNodeSemantic()) {
      case ROLE_SOME:
         final RoleNodeSomeWithSequences roleNodeSome = (RoleNodeSomeWithSequences) logicNode;

         return Factory.createExistential(roles.get(roleNodeSome.getTypeConceptSequence()),
                                          getConcept(roleNodeSome.getOnlyChild(),
                                                concepts,
                                                roles,
                                                neverGroupRoleSequences,
                                                roleGroupConceptSequence));

      case CONCEPT:
         final ConceptNodeWithSequences conceptNode = (ConceptNodeWithSequences) logicNode;

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

   /**
    * Gets the concepts.
    *
    * @param logicNodes the logic nodes
    * @param concepts the concepts
    * @param roles the roles
    * @param neverGroupRoleSequences the never group role sequences
    * @param roleGroupConceptSequence the role group concept sequence
    * @return the concepts
    */
   private Concept[] getConcepts(LogicNode[] logicNodes,
                                 Concept[] concepts,
                                 OpenIntObjectHashMap<Role> roles,
                                 OpenIntHashSet neverGroupRoleSequences,
                                 int roleGroupConceptSequence) {
      final Concept[] returnValues = new Concept[concepts.length];

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

