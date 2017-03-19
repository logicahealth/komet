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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.provider.logic.csiro.axioms;

//~--- JDK imports ------------------------------------------------------------

import java.util.Calendar;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

//~--- non-JDK imports --------------------------------------------------------

//TODO move to CSIRO specific module
import au.csiro.ontology.Factory;
import au.csiro.ontology.model.Axiom;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.ConceptInclusion;
import au.csiro.ontology.model.Feature;
import au.csiro.ontology.model.Literal;
import au.csiro.ontology.model.Operator;
import au.csiro.ontology.model.Role;

import sh.isaac.api.DataSource;
import sh.isaac.api.Get;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.ConcurrentSequenceObjectMap;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.LiteralNodeBoolean;
import sh.isaac.model.logic.node.LiteralNodeFloat;
import sh.isaac.model.logic.node.LiteralNodeInstant;
import sh.isaac.model.logic.node.LiteralNodeInteger;
import sh.isaac.model.logic.node.LiteralNodeString;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.RootNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithSequences;
import sh.isaac.model.logic.node.internal.FeatureNodeWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithSequences;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class GraphToAxiomTranslator {
   Set<Axiom>                           axioms                  = new ConcurrentSkipListSet<>();
   ConcurrentSequenceObjectMap<Concept> sequenceLogicConceptMap = new ConcurrentSequenceObjectMap<>();
   ConcurrentHashMap<Integer, Role>     sequenceLogicRoleMap    = new ConcurrentHashMap<>();
   ConcurrentHashMap<Integer, Feature>  sequenceLogicFeatureMap = new ConcurrentHashMap<>();
   ConcurrentSkipListSet<Integer>       loadedConcepts          = new ConcurrentSkipListSet<>();
   Factory                              f                       = new Factory();

   //~--- methods -------------------------------------------------------------

   public void clear() {
      axioms.clear();
      sequenceLogicRoleMap.clear();
      sequenceLogicFeatureMap.clear();
      sequenceLogicConceptMap.clear();
      loadedConcepts.clear();
   }

   /**
    * Translates the logicGraphSememe into a set of axioms, and adds those axioms
    * to the internal set of axioms.
    * @param logicGraphSememe
    */
   public void convertToAxiomsAndAdd(LogicGraphSememe logicGraphSememe) {
      loadedConcepts.add(logicGraphSememe.getReferencedComponentNid());

      LogicalExpressionOchreImpl logicGraph = new LogicalExpressionOchreImpl(logicGraphSememe.getGraphData(),
                                                                             DataSource.INTERNAL);

      generateAxioms(logicGraph.getRoot(), logicGraphSememe.getReferencedComponentNid(), logicGraph);
   }

   @Override
   public String toString() {
      return "GraphToAxiomTranslator{" + "axioms=" + axioms.size() + ", sequenceLogicConceptMap=" +
             sequenceLogicConceptMap.getSequences().count() + ", sequenceLogicRoleMap=" + sequenceLogicRoleMap.size() +
             ", sequenceLogicFeatureMap=" + sequenceLogicFeatureMap.size() + '}';
   }

   private Optional<Concept> generateAxioms(LogicNode logicNode,
         int conceptNid,
         LogicalExpressionOchreImpl logicGraph) {
      switch (logicNode.getNodeSemantic()) {
      case AND:
         return processAnd((AndNode) logicNode, conceptNid, logicGraph);

      case CONCEPT:
         ConceptNodeWithSequences conceptNode = (ConceptNodeWithSequences) logicNode;

         return Optional.of(getConcept(conceptNode.getConceptSequence()));

      case DEFINITION_ROOT:
         processRoot(logicNode, conceptNid, logicGraph);
         break;

      case DISJOINT_WITH:
         throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");

      case FEATURE:
         return processFeatureNode((FeatureNodeWithSequences) logicNode, conceptNid, logicGraph);

      case NECESSARY_SET:
         processNecessarySet((NecessarySetNode) logicNode, conceptNid, logicGraph);
         break;

      case OR:
         throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");

      case ROLE_ALL:
         throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");

      case ROLE_SOME:
         return processRoleNodeSome((RoleNodeSomeWithSequences) logicNode, conceptNid, logicGraph);

      case SUBSTITUTION_BOOLEAN:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_CONCEPT:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_FLOAT:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_INSTANT:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_INTEGER:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_STRING:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUFFICIENT_SET:
         processSufficientSet((SufficientSetNode) logicNode, conceptNid, logicGraph);
         break;

      case TEMPLATE:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case LITERAL_BOOLEAN:
      case LITERAL_FLOAT:
      case LITERAL_INSTANT:
      case LITERAL_INTEGER:
      case LITERAL_STRING:
         throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: " + logicNode +
               " Concept: " + conceptNid + " graph: " + logicGraph);

      default:
         throw new UnsupportedOperationException("Can't handle: " + logicNode.getNodeSemantic());
      }

      return Optional.empty();
   }

   private Optional<Literal> generateLiterals(LogicNode logicNode, Concept c, LogicalExpressionOchreImpl logicGraph) {
      switch (logicNode.getNodeSemantic()) {
      case LITERAL_BOOLEAN:
         LiteralNodeBoolean literalNodeBoolean = (LiteralNodeBoolean) logicNode;

         return Optional.of(Factory.createBooleanLiteral(literalNodeBoolean.getLiteralValue()));

      case LITERAL_FLOAT:
         LiteralNodeFloat literalNodeFloat = (LiteralNodeFloat) logicNode;

         return Optional.of(Factory.createFloatLiteral(literalNodeFloat.getLiteralValue()));

      case LITERAL_INSTANT:
         LiteralNodeInstant literalNodeInstant = (LiteralNodeInstant) logicNode;
         Calendar           calendar           = Calendar.getInstance();

         calendar.setTimeInMillis(literalNodeInstant.getLiteralValue()
               .toEpochMilli());
         return Optional.of(Factory.createDateLiteral(calendar));

      case LITERAL_INTEGER:
         LiteralNodeInteger literalNodeInteger = (LiteralNodeInteger) logicNode;

         return Optional.of(Factory.createIntegerLiteral(literalNodeInteger.getLiteralValue()));

      case LITERAL_STRING:
         LiteralNodeString literalNodeString = (LiteralNodeString) logicNode;

         return Optional.of(Factory.createStringLiteral(literalNodeString.getLiteralValue()));

      default:
         throw new UnsupportedOperationException("Expected literal logicNode, found: " + logicNode + " Concept: " + c +
               " graph: " + logicGraph);
      }
   }

   private Optional<Concept> processAnd(AndNode andNode, int conceptNid, LogicalExpressionOchreImpl logicGraph) {
      LogicNode[] childrenLogicNodes  = andNode.getChildren();
      Concept[]   conjunctionConcepts = new Concept[childrenLogicNodes.length];

      for (int i = 0; i < childrenLogicNodes.length; i++) {
         conjunctionConcepts[i] = generateAxioms(childrenLogicNodes[i], conceptNid, logicGraph).get();
      }

      return Optional.of(Factory.createConjunction(conjunctionConcepts));
   }

   private Optional<Concept> processFeatureNode(FeatureNodeWithSequences featureNode,
         int conceptNid,
         LogicalExpressionOchreImpl logicGraph) {
      Feature     theFeature = getFeature(featureNode.getTypeConceptSequence());
      LogicNode[] children   = featureNode.getChildren();

      if (children.length != 1) {
         throw new IllegalStateException("FeatureNode can only have one child. Concept: " + conceptNid + " graph: " +
                                         logicGraph);
      }

      Optional<Literal> optionalLiteral = generateLiterals(children[0], getConcept(conceptNid), logicGraph);

      if (optionalLiteral.isPresent()) {
         switch (featureNode.getOperator()) {
         case EQUALS:
            return Optional.of(Factory.createDatatype(theFeature, Operator.EQUALS, optionalLiteral.get()));

         case GREATER_THAN:
            return Optional.of(Factory.createDatatype(theFeature, Operator.GREATER_THAN, optionalLiteral.get()));

         case GREATER_THAN_EQUALS:
            return Optional.of(Factory.createDatatype(theFeature, Operator.GREATER_THAN_EQUALS, optionalLiteral.get()));

         case LESS_THAN:
            return Optional.of(Factory.createDatatype(theFeature, Operator.LESS_THAN, optionalLiteral.get()));

         case LESS_THAN_EQUALS:
            return Optional.of(Factory.createDatatype(theFeature, Operator.LESS_THAN_EQUALS, optionalLiteral.get()));

         default:
            throw new UnsupportedOperationException(featureNode.getOperator().toString());
         }
      }

      throw new UnsupportedOperationException("Child of FeatureNode node cannot return null concept. Concept: " +
            conceptNid + " graph: " + logicGraph);
   }

   private void processNecessarySet(NecessarySetNode necessarySetNode,
                                    int conceptNid,
                                    LogicalExpressionOchreImpl logicGraph) {
      LogicNode[] children = necessarySetNode.getChildren();

      if (children.length != 1) {
         throw new IllegalStateException("necessarySetNode can only have one child. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }

      if (!(children[0] instanceof AndNode)) {
         throw new IllegalStateException("necessarySetNode can only have AND for a child. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }

      Optional<Concept> conjunctionConcept = generateAxioms(children[0], conceptNid, logicGraph);

      if (conjunctionConcept.isPresent()) {
         axioms.add(new ConceptInclusion(getConcept(conceptNid), conjunctionConcept.get()));
      } else {
         throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }
   }

   private Optional<Concept> processRoleNodeSome(RoleNodeSomeWithSequences roleNodeSome,
         int conceptNid,
         LogicalExpressionOchreImpl logicGraph) {
      Role        theRole  = getRole(roleNodeSome.getTypeConceptSequence());
      LogicNode[] children = roleNodeSome.getChildren();

      if (children.length != 1) {
         throw new IllegalStateException("RoleNodeSome can only have one child. Concept: " + conceptNid + " graph: " +
                                         logicGraph);
      }

      Optional<Concept> restrictionConcept = generateAxioms(children[0], conceptNid, logicGraph);

      if (restrictionConcept.isPresent()) {
         return Optional.of(Factory.createExistential(theRole, restrictionConcept.get()));
      }

      throw new UnsupportedOperationException("Child of role node can not return null concept. Concept: " +
            conceptNid + " graph: " + logicGraph);
   }

   private void processRoot(LogicNode logicNode,
                            int conceptNid,
                            LogicalExpressionOchreImpl logicGraph)
            throws IllegalStateException {
      RootNode rootNode = (RootNode) logicNode;

      for (LogicNode child: rootNode.getChildren()) {
         Optional<Concept> axiom = generateAxioms(child, conceptNid, logicGraph);

         if (axiom.isPresent()) {
            throw new IllegalStateException("Children of root logicNode should not return axioms. Concept: " +
                                            conceptNid + " graph: " + logicGraph);
         }
      }
   }

   private void processSufficientSet(SufficientSetNode sufficientSetNode,
                                     int conceptNid,
                                     LogicalExpressionOchreImpl logicGraph) {
      LogicNode[] children = sufficientSetNode.getChildren();

      if (children.length != 1) {
         throw new IllegalStateException("SufficientSetNode can only have one child. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }

      if (!(children[0] instanceof AndNode)) {
         throw new IllegalStateException("SufficientSetNode can only have AND for a child. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }

      Optional<Concept> conjunctionConcept = generateAxioms(children[0], conceptNid, logicGraph);

      if (conjunctionConcept.isPresent()) {
         axioms.add(new ConceptInclusion(getConcept(conceptNid), conjunctionConcept.get()));
         axioms.add(new ConceptInclusion(conjunctionConcept.get(), getConcept(conceptNid)));
      } else {
         throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public Set<Axiom> getAxioms() {
      return axioms;
   }

   private Concept getConcept(int name) {
      if (name < 0) {
         name = Get.identifierService()
                   .getConceptSequence(name);
      }

      Optional<Concept> optionalConcept = sequenceLogicConceptMap.get(name);

      if (optionalConcept.isPresent()) {
         return optionalConcept.get();
      }

      return sequenceLogicConceptMap.put(name, Factory.createNamedConcept(Integer.toString(name)));
   }

   public Optional<Concept> getConceptFromSequence(int sequence) {
      return sequenceLogicConceptMap.get(sequence);
   }

   private Feature getFeature(int name) {
      if (name < 0) {
         name = Get.identifierService()
                   .getConceptSequence(name);
      }

      Feature feature = sequenceLogicFeatureMap.get(name);

      if (feature != null) {
         return feature;
      }

      sequenceLogicFeatureMap.putIfAbsent(name, Factory.createNamedFeature(Integer.toString(name)));
      return sequenceLogicFeatureMap.get(name);
   }

   public ConceptSequenceSet getLoadedConcepts() {
      return ConceptSequenceSet.of(loadedConcepts);
   }

   private Role getRole(int name) {
      if (name < 0) {
         name = Get.identifierService()
                   .getConceptSequence(name);
      }

      Role role = sequenceLogicRoleMap.get(name);

      if (role != null) {
         return role;
      }

      sequenceLogicRoleMap.putIfAbsent(name, Factory.createNamedRole(Integer.toString(name)));
      return sequenceLogicRoleMap.get(name);
   }
}

