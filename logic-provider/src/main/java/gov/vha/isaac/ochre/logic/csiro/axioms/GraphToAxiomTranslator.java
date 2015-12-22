/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.logic.csiro.axioms;

// TODO move to CSIRO specific module

import au.csiro.ontology.Factory;

import java.util.Set;
import au.csiro.ontology.model.Axiom;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.ConceptInclusion;
import au.csiro.ontology.model.Feature;
import au.csiro.ontology.model.Literal;
import au.csiro.ontology.model.Operator;
import au.csiro.ontology.model.Role;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.model.logic.node.AndNode;
import gov.vha.isaac.ochre.model.logic.node.internal.ConceptNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.FeatureNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeBoolean;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeFloat;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeInstant;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeInteger;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeString;
import gov.vha.isaac.ochre.model.logic.node.NecessarySetNode;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeSomeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.RootNode;
import gov.vha.isaac.ochre.model.logic.node.SufficientSetNode;
import gov.vha.isaac.ochre.api.DataSource;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.collections.ConcurrentSequenceObjectMap;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author kec
 */
public class GraphToAxiomTranslator {

    Set<Axiom> axioms = new ConcurrentSkipListSet<>();

    ConcurrentSequenceObjectMap<Concept> sequenceLogicConceptMap = new ConcurrentSequenceObjectMap<>();
    ConcurrentHashMap<Integer, Role> sequenceLogicRoleMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Feature> sequenceLogicFeatureMap = new ConcurrentHashMap<>();
    ConcurrentSkipListSet<Integer> loadedConcepts = new ConcurrentSkipListSet<>();
    Factory f = new Factory();
    

    public void clear() {
        axioms.clear();
        sequenceLogicRoleMap.clear();
        sequenceLogicFeatureMap.clear();
        sequenceLogicConceptMap.clear();
        loadedConcepts.clear();
    }
    
    public ConceptSequenceSet getLoadedConcepts() {
        return ConceptSequenceSet.of(loadedConcepts);
    }
    private Concept getConcept(int name) {
        if (name < 0) {
            name = Get.identifierService().getConceptSequence(name);
        }
        Optional<Concept> optionalConcept = sequenceLogicConceptMap.get(name);
        if (optionalConcept.isPresent()) {
            return optionalConcept.get();
        }
        return sequenceLogicConceptMap.put(name, Factory.createNamedConcept(Integer.toString(name)));
    }

    private Feature getFeature(int name) {
        if (name < 0) {
            name = Get.identifierService().getConceptSequence(name);
        }
        Feature feature = sequenceLogicFeatureMap.get(name);
        if (feature != null) {
            return feature;
        }
        sequenceLogicFeatureMap.putIfAbsent(name, Factory.createNamedFeature(Integer.toString(name)));
        return sequenceLogicFeatureMap.get(name);
    }

    private Role getRole(int name) {
        if (name < 0) {
            name = Get.identifierService().getConceptSequence(name);
        }
        Role role = sequenceLogicRoleMap.get(name);
        if (role != null) {
            return role;
        }
        sequenceLogicRoleMap.putIfAbsent(name, Factory.createNamedRole(Integer.toString(name)));
        return sequenceLogicRoleMap.get(name);
    }

    /**
     * Translates the logicGraphSememe into a set of axioms, and adds those axioms 
     * to the internal set of axioms. 
     * @param logicGraphSememe 
     */
    public void convertToAxiomsAndAdd(LogicGraphSememe logicGraphSememe) {
        loadedConcepts.add(logicGraphSememe.getReferencedComponentNid());
        LogicalExpressionOchreImpl logicGraph = new LogicalExpressionOchreImpl(logicGraphSememe.getGraphData(), DataSource.INTERNAL);
        generateAxioms(logicGraph.getRoot(), logicGraphSememe.getReferencedComponentNid(), logicGraph);
    }

    private Optional<Literal> generateLiterals(Node node, Concept c, LogicalExpressionOchreImpl logicGraph) {
        switch (node.getNodeSemantic()) {
            case LITERAL_BOOLEAN:
                LiteralNodeBoolean literalNodeBoolean = (LiteralNodeBoolean) node;
                return Optional.of(Factory.createBooleanLiteral(literalNodeBoolean.getLiteralValue()));
            case LITERAL_FLOAT:
                LiteralNodeFloat literalNodeFloat = (LiteralNodeFloat) node;
                return Optional.of(Factory.createFloatLiteral(literalNodeFloat.getLiteralValue()));
            case LITERAL_INSTANT:
                LiteralNodeInstant literalNodeInstant = (LiteralNodeInstant) node;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(literalNodeInstant.getLiteralValue().toEpochMilli());
                return Optional.of(Factory.createDateLiteral(calendar));
            case LITERAL_INTEGER:
                LiteralNodeInteger literalNodeInteger = (LiteralNodeInteger) node;
                return Optional.of(Factory.createIntegerLiteral(literalNodeInteger.getLiteralValue()));
            case LITERAL_STRING:
                LiteralNodeString literalNodeString = (LiteralNodeString) node;
                return Optional.of(Factory.createStringLiteral(literalNodeString.getLiteralValue()));
            default:
                throw new UnsupportedOperationException("Expected literal node, found: " + node
                        + " Concept: " + c + " graph: " + logicGraph);
        }
    }

    private Optional<Concept> generateAxioms(Node node, int conceptNid, LogicalExpressionOchreImpl logicGraph) {
        switch (node.getNodeSemantic()) {
            case AND:
                return processAnd((AndNode) node, conceptNid, logicGraph);
            case CONCEPT:
                ConceptNodeWithSequences conceptNode = (ConceptNodeWithSequences) node;
                return Optional.of(getConcept(conceptNode.getConceptSequence()));
            case DEFINITION_ROOT:
                processRoot(node, conceptNid, logicGraph);
                break;
            case DISJOINT_WITH:
                throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");
            case FEATURE:
                return processFeatureNode((FeatureNodeWithSequences) node, conceptNid, logicGraph);
            case NECESSARY_SET:
                processNecessarySet((NecessarySetNode) node, conceptNid, logicGraph);
                break;
            case OR:
                throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");
            case ROLE_ALL:
                throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");
            case ROLE_SOME:
                return processRoleNodeSome((RoleNodeSomeWithSequences) node, conceptNid, logicGraph);
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
                processSufficientSet((SufficientSetNode) node, conceptNid, logicGraph);
                break;
            case TEMPLATE:
                throw new UnsupportedOperationException("Supported, but not yet implemented.");
            case LITERAL_BOOLEAN:
            case LITERAL_FLOAT:
            case LITERAL_INSTANT:
            case LITERAL_INTEGER:
            case LITERAL_STRING:
                throw new UnsupportedOperationException("Expected concept node, found literal node: " + node
                        + " Concept: " + conceptNid + " graph: " + logicGraph);
            default:
                throw new UnsupportedOperationException("Can't handle: " + node.getNodeSemantic());
        }
        return Optional.empty();
    }

    private Optional<Concept> processAnd(AndNode andNode, int conceptNid, LogicalExpressionOchreImpl logicGraph) {
        Node[] childrenNodes = andNode.getChildren();
        Concept[] conjunctionConcepts = new Concept[childrenNodes.length];
        for (int i = 0; i < childrenNodes.length; i++) {
            conjunctionConcepts[i] = generateAxioms(childrenNodes[i], conceptNid, logicGraph).get();
        }
        return Optional.of(Factory.createConjunction(conjunctionConcepts));
    }

    private void processSufficientSet(SufficientSetNode sufficientSetNode, int conceptNid, LogicalExpressionOchreImpl logicGraph) {
        Node[] children = sufficientSetNode.getChildren();
        if (children.length != 1) {
            throw new IllegalStateException("SufficientSetNode can only have one child. Concept: " + conceptNid + " graph: " + logicGraph);
        }
        if (!(children[0] instanceof AndNode)) {
            throw new IllegalStateException("SufficientSetNode can only have AND for a child. Concept: " + conceptNid + " graph: " + logicGraph);
        }
        Optional<Concept> conjunctionConcept = generateAxioms(children[0], conceptNid, logicGraph);
        if (conjunctionConcept.isPresent()) {
            axioms.add(new ConceptInclusion(getConcept(conceptNid), conjunctionConcept.get()));
            axioms.add(new ConceptInclusion(conjunctionConcept.get(), getConcept(conceptNid)));
        } else {
            throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid + " graph: " + logicGraph);
        }
    }

    private void processNecessarySet(NecessarySetNode necessarySetNode, int conceptNid, LogicalExpressionOchreImpl logicGraph) {
        Node[] children = necessarySetNode.getChildren();
        if (children.length != 1) {
            throw new IllegalStateException("necessarySetNode can only have one child. Concept: " + conceptNid + " graph: " + logicGraph);
        }
        if (!(children[0] instanceof AndNode)) {
            throw new IllegalStateException("necessarySetNode can only have AND for a child. Concept: " + conceptNid + " graph: " + logicGraph);
        }
        Optional<Concept> conjunctionConcept = generateAxioms(children[0], conceptNid, logicGraph);
        if (conjunctionConcept.isPresent()) {
            axioms.add(new ConceptInclusion(getConcept(conceptNid), conjunctionConcept.get()));
        } else {
            throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid + " graph: " + logicGraph);
        }
    }

    private void processRoot(Node node, int conceptNid, LogicalExpressionOchreImpl logicGraph) throws IllegalStateException {
        RootNode rootNode = (RootNode) node;
        for (Node child : rootNode.getChildren()) {
            Optional<Concept> axiom = generateAxioms(child, conceptNid, logicGraph);
            if (axiom.isPresent()) {
                throw new IllegalStateException("Children of root node should not return axioms. Concept: " + conceptNid + " graph: " + logicGraph);
            }
        }
    }

    private Optional<Concept> processRoleNodeSome(RoleNodeSomeWithSequences roleNodeSome, int conceptNid, LogicalExpressionOchreImpl logicGraph) {
        Role theRole = getRole(roleNodeSome.getTypeConceptSequence());
        Node[] children = roleNodeSome.getChildren();
        if (children.length != 1) {
            throw new IllegalStateException("RoleNodeSome can only have one child. Concept: " + conceptNid + " graph: " + logicGraph);
        }
        Optional<Concept> restrictionConcept = generateAxioms(children[0], conceptNid, logicGraph);
        if (restrictionConcept.isPresent()) {
            return Optional.of(Factory.createExistential(theRole, restrictionConcept.get()));
        }
        throw new UnsupportedOperationException("Child of role node can not return null concept. Concept: " + conceptNid + " graph: " + logicGraph);
    }

    public Set<Axiom> getAxioms() {
        return axioms;
    }

    public Optional<Concept> getConceptFromSequence(int sequence) {
        return sequenceLogicConceptMap.get(sequence);
    }

    private Optional<Concept> processFeatureNode(FeatureNodeWithSequences featureNode, int conceptNid, LogicalExpressionOchreImpl logicGraph) {
        Feature theFeature = getFeature(featureNode.getTypeConceptSequence());
        Node[] children = featureNode.getChildren();
        if (children.length != 1) {
            throw new IllegalStateException("FeatureNode can only have one child. Concept: " + conceptNid + " graph: " + logicGraph);
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
        throw new UnsupportedOperationException("Child of FeatureNode node cannot return null concept. Concept: " + conceptNid + " graph: " + logicGraph);
    }

    @Override
    public String toString() {
        return "GraphToAxiomTranslator{" +
                "axioms=" + axioms.size() +
                ", sequenceLogicConceptMap=" + sequenceLogicConceptMap.getSequences().count() +
                ", sequenceLogicRoleMap=" + sequenceLogicRoleMap.size() +
                ", sequenceLogicFeatureMap=" + sequenceLogicFeatureMap.size() +
                '}';
    }
}
