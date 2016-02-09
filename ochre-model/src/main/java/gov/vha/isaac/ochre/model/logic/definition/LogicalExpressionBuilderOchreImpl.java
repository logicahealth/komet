/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.model.logic.definition;

import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.api.logic.assertions.AllRole;
import gov.vha.isaac.ochre.api.logic.assertions.Assertion;
import gov.vha.isaac.ochre.api.logic.assertions.ConceptAssertion;
import gov.vha.isaac.ochre.api.logic.assertions.Feature;
import gov.vha.isaac.ochre.api.logic.assertions.LogicalSet;
import gov.vha.isaac.ochre.api.logic.assertions.NecessarySet;
import gov.vha.isaac.ochre.api.logic.assertions.SomeRole;
import gov.vha.isaac.ochre.api.logic.assertions.SufficientSet;
import gov.vha.isaac.ochre.api.logic.assertions.Template;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.And;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.Connector;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.DisjointWith;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.Or;
import gov.vha.isaac.ochre.api.logic.assertions.literal.BooleanLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.FloatLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.InstantLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.IntegerLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.LiteralAssertion;
import gov.vha.isaac.ochre.api.logic.assertions.literal.StringLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.BooleanSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.ConceptSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.FloatSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.InstantSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.IntegerSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.StringSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.model.logic.node.*;
import gov.vha.isaac.ochre.model.logic.node.AbstractLogicNode;
import gov.vha.isaac.ochre.model.logic.node.internal.ConceptNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.FeatureNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeAllWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeSomeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.TemplateNodeWithSequences;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.mahout.math.map.OpenShortObjectHashMap;

/**
 *
 * @author kec
 */
public class LogicalExpressionBuilderOchreImpl implements LogicalExpressionBuilder {

    private boolean built = false;
    private short nextAxiomId = 0;
    private final Set<GenericAxiom> rootSets = new HashSet<>();
    private final HashMap<GenericAxiom, List<GenericAxiom>> definitionTree = new HashMap<>(20);
    private final OpenShortObjectHashMap<Object> axiomParameters = new OpenShortObjectHashMap<>(20);

    public LogicalExpressionBuilderOchreImpl() {
    }

    public short getNextAxiomIndex() {
        return nextAxiomId++;
    }

    private List<GenericAxiom> asList(Assertion... assertions) {
        ArrayList<GenericAxiom> list = new ArrayList<>(assertions.length);
        Arrays.stream(assertions).forEach((assertion) -> list.add((GenericAxiom) assertion));
        return list;
    }
    private List<? extends Assertion> makeAssertionsFromNodeDescendants(LogicNode logicNode) {
        return logicNode.getChildStream().map((childNode) ->
                makeAssertionFromNode(childNode)).collect(Collectors.toList());
    }

    private Assertion makeAssertionFromNode(LogicNode logicNode) {
        switch (logicNode.getNodeSemantic()) {
            case DEFINITION_ROOT:
                break;
            case NECESSARY_SET:
                return necessarySet(makeAssertionsFromNodeDescendants(logicNode).toArray(new Connector[0]));
            case SUFFICIENT_SET:
                return sufficientSet(makeAssertionsFromNodeDescendants(logicNode).toArray(new Connector[0]));
            case AND:
                return and(makeAssertionsFromNodeDescendants(logicNode).toArray(new Assertion[0]));
            case OR:
                return or(makeAssertionsFromNodeDescendants(logicNode).toArray(new Assertion[0]));
            case DISJOINT_WITH:
                break;
            case ROLE_ALL:
                RoleNodeAllWithSequences allRoleNode = (RoleNodeAllWithSequences) logicNode;
                return allRole(allRoleNode.getTypeConceptSequence(), makeAssertionFromNode(allRoleNode.getOnlyChild()));
            case ROLE_SOME:
                RoleNodeSomeWithSequences someRoleNode = (RoleNodeSomeWithSequences) logicNode;
                return someRole(someRoleNode.getTypeConceptSequence(), makeAssertionFromNode(someRoleNode.getOnlyChild()));
            case CONCEPT:
                ConceptNodeWithSequences conceptNode = (ConceptNodeWithSequences) logicNode;
                return conceptAssertion(conceptNode.getConceptSequence());
            case FEATURE:
                FeatureNodeWithSequences featureNode = (FeatureNodeWithSequences) logicNode;
                return feature(featureNode.getTypeConceptSequence(), 
                        (LiteralAssertion) makeAssertionFromNode(featureNode.getOnlyChild()));
            case LITERAL_BOOLEAN:
                LiteralNodeBoolean literalNodeBoolean = (LiteralNodeBoolean) logicNode;
                return booleanLiteral(literalNodeBoolean.getLiteralValue());
            case LITERAL_FLOAT:
                LiteralNodeFloat literalNodeFloat = (LiteralNodeFloat) logicNode;
                return floatLiteral(literalNodeFloat.getLiteralValue());
            case LITERAL_INSTANT:
                LiteralNodeInstant literalNodeInstant = (LiteralNodeInstant) logicNode;
                return instantLiteral(literalNodeInstant.getLiteralValue());
            case LITERAL_INTEGER:
                LiteralNodeInteger literalNodeInteger = (LiteralNodeInteger) logicNode;
                return integerLiteral(literalNodeInteger.getLiteralValue());
            case LITERAL_STRING:
                LiteralNodeString literalNodeString = (LiteralNodeString) logicNode;
                return stringLiteral(literalNodeString.getLiteralValue());

            case TEMPLATE:
                TemplateNodeWithSequences templateNode = (TemplateNodeWithSequences) logicNode;
                return template(templateNode.getTemplateConceptNid(), 
                        templateNode.getAssemblageConceptNid());
            case SUBSTITUTION_CONCEPT:
                SubstitutionNodeConcept substitutionNodeConcept = (SubstitutionNodeConcept) logicNode;
                return conceptSubstitution(substitutionNodeConcept.getSubstitutionFieldSpecification());
            case SUBSTITUTION_BOOLEAN:
                SubstitutionNodeBoolean substitutionNodeBoolean = (SubstitutionNodeBoolean) logicNode;
                return booleanSubstitution(substitutionNodeBoolean.getSubstitutionFieldSpecification());
            case SUBSTITUTION_FLOAT:
                SubstitutionNodeFloat substitutionNodeFloat = (SubstitutionNodeFloat) logicNode;
                return floatSubstitution(substitutionNodeFloat.getSubstitutionFieldSpecification());
            case SUBSTITUTION_INSTANT:
                SubstitutionNodeInstant substitutionNodeInstant = (SubstitutionNodeInstant) logicNode;
                return instantSubstitution(substitutionNodeInstant.getSubstitutionFieldSpecification());
            case SUBSTITUTION_INTEGER:
                SubstitutionNodeInteger substitutionNodeInteger = (SubstitutionNodeInteger) logicNode;
                return integerSubstitution(substitutionNodeInteger.getSubstitutionFieldSpecification());
            case SUBSTITUTION_STRING:
                SubstitutionNodeString substitutionNodeString = (SubstitutionNodeString) logicNode;
                return stringSubstitution(substitutionNodeString.getSubstitutionFieldSpecification());
        }
        throw new UnsupportedOperationException("Can't handle: " + logicNode.getNodeSemantic());
    }

    @Override
    public void addToRoot(LogicalSet logicalSet) {
        checkNotBuilt();
        
        GenericAxiom axiom;
        if (logicalSet instanceof NecessarySet) {
           axiom = new GenericAxiom(NodeSemantic.NECESSARY_SET, this);
        } else {
           axiom = new GenericAxiom(NodeSemantic.SUFFICIENT_SET, this);
        }
        rootSets.add(axiom);
        addToDefinitionTree(axiom, logicalSet);
    }

    @Override
    public Assertion cloneSubTree(LogicNode subTreeRoot) {
        return makeAssertionFromNode(subTreeRoot);
    }

    @Override
    public NecessarySet necessarySet(Connector... connector) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.NECESSARY_SET, this);
        rootSets.add(axiom);
        addToDefinitionTree(axiom, connector);
        return axiom;
    }

    protected void addToDefinitionTree(GenericAxiom axiom, Assertion... connectors) {
        definitionTree.put(axiom, asList(connectors));
    }

    @Override
    public SufficientSet sufficientSet(Connector... connector) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.SUFFICIENT_SET, this);
        rootSets.add(axiom);
        addToDefinitionTree(axiom, connector);
        return axiom;
    }

    @Override
    public DisjointWith disjointWith(ConceptChronology<?> conceptChronology) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.DISJOINT_WITH, this);
        axiomParameters.put(axiom.getIndex(), conceptChronology);
        return axiom;
    }


    @Override
    public And and(Assertion... assertions) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.AND, this);
        addToDefinitionTree(axiom, assertions);
        return axiom;
    }

    @Override
    public ConceptAssertion conceptAssertion(ConceptChronology<?> conceptChronology) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.CONCEPT, this);
        axiomParameters.put(axiom.getIndex(), conceptChronology);
        return axiom;
    }

    @Override
    public AllRole allRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.ROLE_ALL, this);
        addToDefinitionTree(axiom, roleRestriction);
        axiomParameters.put(axiom.getIndex(), roleTypeChronology);
        return axiom;
    }

    @Override
    public Feature feature(ConceptChronology<?> featureTypeChronology, LiteralAssertion literal) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.FEATURE, this);
        addToDefinitionTree(axiom, literal);
        axiomParameters.put(axiom.getIndex(), featureTypeChronology);
        return axiom;
    }

    @Override
    public SomeRole someRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.ROLE_SOME, this);
        addToDefinitionTree(axiom, roleRestriction);
        axiomParameters.put(axiom.getIndex(), roleTypeChronology);
        return axiom;
    }

    @Override
    public Template template(ConceptChronology<?> templateChronology, ConceptChronology<?> assemblageToPopulateTemplateConcept) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.TEMPLATE, this);
        axiomParameters.put(axiom.getIndex(), new Object[]{templateChronology, assemblageToPopulateTemplateConcept});
        return axiom;
    }
    private Template template(Integer templateChronologyNid, Integer assemblageToPopulateTemplateConceptNid) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.TEMPLATE, this);
        axiomParameters.put(axiom.getIndex(), new Object[]{templateChronologyNid, assemblageToPopulateTemplateConceptNid});
        return axiom;
    }

    @Override
    public Or or(Assertion... assertions) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.OR, this);
        addToDefinitionTree(axiom, assertions);
        return axiom;
    }

    @Override
    public BooleanLiteral booleanLiteral(boolean booleanLiteral) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.LITERAL_BOOLEAN, this);
        axiomParameters.put(axiom.getIndex(), booleanLiteral);
        return axiom;
    }

    @Override
    public FloatLiteral floatLiteral(float floatLiteral) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.LITERAL_FLOAT, this);
        axiomParameters.put(axiom.getIndex(), floatLiteral);
        return axiom;
    }

    @Override
    public InstantLiteral instantLiteral(Instant literalValue) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.LITERAL_INSTANT, this);
        axiomParameters.put(axiom.getIndex(), literalValue);
        return axiom;
    }

    @Override
    public IntegerLiteral integerLiteral(int literalValue) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.LITERAL_INTEGER, this);
        axiomParameters.put(axiom.getIndex(), literalValue);
        return axiom;
    }

    @Override
    public StringLiteral stringLiteral(String literalValue) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.LITERAL_STRING, this);
        axiomParameters.put(axiom.getIndex(), literalValue);
        return axiom;
    }

    @Override
    public BooleanSubstitution booleanSubstitution(SubstitutionFieldSpecification fieldSpecification) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.SUBSTITUTION_BOOLEAN, this);
        axiomParameters.put(axiom.getIndex(), fieldSpecification);
        return axiom;
    }

    @Override
    public ConceptSubstitution conceptSubstitution(SubstitutionFieldSpecification fieldSpecification) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.SUBSTITUTION_CONCEPT, this);
        axiomParameters.put(axiom.getIndex(), fieldSpecification);
        return axiom;
    }

    @Override
    public FloatSubstitution floatSubstitution(SubstitutionFieldSpecification fieldSpecification) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.SUBSTITUTION_FLOAT, this);
        axiomParameters.put(axiom.getIndex(), fieldSpecification);
        return axiom;
    }

    @Override
    public InstantSubstitution instantSubstitution(SubstitutionFieldSpecification fieldSpecification) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.SUBSTITUTION_INSTANT, this);
        axiomParameters.put(axiom.getIndex(), fieldSpecification);
        return axiom;
    }

    @Override
    public IntegerSubstitution integerSubstitution(SubstitutionFieldSpecification fieldSpecification) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.SUBSTITUTION_INTEGER, this);
        axiomParameters.put(axiom.getIndex(), fieldSpecification);
        return axiom;
    }

    @Override
    public StringSubstitution stringSubstitution(SubstitutionFieldSpecification fieldSpecification) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.SUBSTITUTION_STRING, this);
        axiomParameters.put(axiom.getIndex(), fieldSpecification);
        return axiom;
    }

    @Override
    public LogicalExpression build() throws IllegalStateException {
        checkNotBuilt();
        LogicalExpressionOchreImpl definition = new LogicalExpressionOchreImpl();
        definition.Root();

        rootSets.forEach((axiom) -> addToDefinition(axiom, definition));

        definition.sort();
        built = true;
        return definition;
    }

    private void checkNotBuilt() throws IllegalStateException {
        if (built) {
            throw new IllegalStateException("Builder has already built. Builders cannot be reused.");
        }
    }

    private AbstractLogicNode addToDefinition(GenericAxiom axiom, LogicalExpressionOchreImpl definition)
            throws IllegalStateException {

        AbstractLogicNode newNode;
        switch (axiom.getSemantic()) {
            case NECESSARY_SET:
                newNode = definition.NecessarySet(getChildren(axiom, definition));
                definition.getRoot().addChildren(newNode);
                return newNode;
            case SUFFICIENT_SET:
                newNode = definition.SufficientSet(getChildren(axiom, definition));
                definition.getRoot().addChildren(newNode);
                return newNode;
            case AND:
                return definition.And(getChildren(axiom, definition));
            case OR:
                return definition.Or(getChildren(axiom, definition));
            case FEATURE:
                if (axiomParameters.get(axiom.getIndex()) instanceof Integer) {
                    return definition.Feature((Integer) axiomParameters.get(axiom.getIndex()),
                        addToDefinition(definitionTree.get(axiom).get(0), definition));
                }
                if (axiomParameters.get(axiom.getIndex()) instanceof ConceptSpecification) {
                    return definition.Feature(((ConceptSpecification) axiomParameters.get(axiom.getIndex())).getNid(),
                            addToDefinition(definitionTree.get(axiom).get(0), definition));
                }
                ConceptChronology<?> featureTypeSpecification = (ConceptChronology<?>) axiomParameters.get(axiom.getIndex());
                return definition.Feature(featureTypeSpecification.getNid(),
                        addToDefinition(definitionTree.get(axiom).get(0), definition));
            case CONCEPT:
                if (axiomParameters.get(axiom.getIndex()) instanceof Integer) {
                    return definition.Concept(((Integer) axiomParameters.get(axiom.getIndex())));
                }
                if (axiomParameters.get(axiom.getIndex()) instanceof ConceptSpecification) {
                    return definition.Concept(((ConceptSpecification) axiomParameters.get(axiom.getIndex())).getConceptSequence());
                }
                ConceptChronology<?> conceptSpecification = (ConceptChronology<?>) axiomParameters.get(axiom.getIndex());
                return definition.Concept(conceptSpecification.getConceptSequence());
            case ROLE_ALL:
                if (axiomParameters.get(axiom.getIndex()) instanceof Integer) {
                    return definition.AllRole(((Integer) axiomParameters.get(axiom.getIndex())),
                            addToDefinition(definitionTree.get(axiom).get(0), definition));
                }
                if (axiomParameters.get(axiom.getIndex()) instanceof ConceptSpecification) {
                    return definition.AllRole(((ConceptSpecification) axiomParameters.get(axiom.getIndex())).getNid(),
                            addToDefinition(definitionTree.get(axiom).get(0), definition));
                }
                ConceptChronology<?> roleTypeSpecification = (ConceptChronology<?>) axiomParameters.get(axiom.getIndex());
                return definition.AllRole(roleTypeSpecification.getNid(),
                        addToDefinition(definitionTree.get(axiom).get(0), definition));
            case ROLE_SOME:
                if (axiomParameters.get(axiom.getIndex()) instanceof Integer) {
                    return definition.SomeRole(((Integer) axiomParameters.get(axiom.getIndex())),
                            addToDefinition(definitionTree.get(axiom).get(0), definition));
                }
                if (axiomParameters.get(axiom.getIndex()) instanceof ConceptSpecification) {
                    return definition.SomeRole(((ConceptSpecification) axiomParameters.get(axiom.getIndex())).getNid(),
                            addToDefinition(definitionTree.get(axiom).get(0), definition));
                }
                roleTypeSpecification = (ConceptChronology<?>) axiomParameters.get(axiom.getIndex());
                return definition.SomeRole(roleTypeSpecification.getNid(),
                        addToDefinition(definitionTree.get(axiom).get(0), definition));
            case TEMPLATE:
                Object[] params = (Object[]) axiomParameters.get(axiom.getIndex());
                if (params[0] instanceof Integer) {
                   return definition.Template((Integer) params[0],
                            (Integer) params[1]);
                }
                if (params[0] instanceof ConceptSpecification) {
                    ConceptSpecification templateConceptSpecification = (ConceptSpecification) params[0];
                    ConceptSpecification assemblageToPopulateTemplateConceptSpecification = (ConceptSpecification) params[1];
                    return definition.Template(templateConceptSpecification.getConceptSequence(),
                            assemblageToPopulateTemplateConceptSpecification.getConceptSequence());
                }
                ConceptChronology<?> templateConceptSpecification = (ConceptChronology<?>) params[0];
                ConceptChronology<?> assemblageToPopulateTemplateConceptSpecification = (ConceptChronology<?>) params[1];
                return definition.Template(templateConceptSpecification.getConceptSequence(),
                        assemblageToPopulateTemplateConceptSpecification.getConceptSequence());
            case DISJOINT_WITH:
                if (axiomParameters.get(axiom.getIndex()) instanceof Integer) {
                    return definition.DisjointWith(definition.Concept(((Integer) axiomParameters.get(axiom.getIndex()))));
                }
                if (axiomParameters.get(axiom.getIndex()) instanceof ConceptSpecification) {
                    return definition.DisjointWith(definition.Concept(((ConceptSpecification) axiomParameters.get(axiom.getIndex())).getConceptSequence()));
                }
                ConceptChronology<?> disjointConceptSpecification = (ConceptChronology<?>) axiomParameters.get(axiom.getIndex());
                return definition.DisjointWith(definition.Concept(disjointConceptSpecification.getConceptSequence()));
            case LITERAL_BOOLEAN:
                boolean booleanLiteral = (Boolean) axiomParameters.get(axiom.getIndex());
                return definition.BooleanLiteral(booleanLiteral);
            case LITERAL_FLOAT:
                float floatLiteral = (Float) axiomParameters.get(axiom.getIndex());
                return definition.FloatLiteral(floatLiteral);
            case LITERAL_INSTANT:
                Instant instantLiteral = (Instant) axiomParameters.get(axiom.getIndex());
                return definition.InstantLiteral(instantLiteral);
            case LITERAL_INTEGER:
                int integerLiteral = (Integer) axiomParameters.get(axiom.getIndex());
                return definition.IntegerLiteral(integerLiteral);
            case LITERAL_STRING:
                String stringLiteral = (String) axiomParameters.get(axiom.getIndex());
                return definition.StringLiteral(stringLiteral);
            case SUBSTITUTION_BOOLEAN:
                SubstitutionFieldSpecification fieldSpecification
                        = (SubstitutionFieldSpecification) axiomParameters.get(axiom.getIndex());
                return definition.BooleanSubstitution(fieldSpecification);
            case SUBSTITUTION_CONCEPT:
                fieldSpecification
                        = (SubstitutionFieldSpecification) axiomParameters.get(axiom.getIndex());
                return definition.ConceptSubstitution(fieldSpecification);
            case SUBSTITUTION_FLOAT:
                fieldSpecification
                        = (SubstitutionFieldSpecification) axiomParameters.get(axiom.getIndex());
                return definition.FloatSubstitution(fieldSpecification);
            case SUBSTITUTION_INSTANT:
                fieldSpecification
                        = (SubstitutionFieldSpecification) axiomParameters.get(axiom.getIndex());
                return definition.InstantSubstitution(fieldSpecification);
            case SUBSTITUTION_INTEGER:
                fieldSpecification
                        = (SubstitutionFieldSpecification) axiomParameters.get(axiom.getIndex());
                return definition.IntegerSubstitution(fieldSpecification);
            case SUBSTITUTION_STRING:
                fieldSpecification
                        = (SubstitutionFieldSpecification) axiomParameters.get(axiom.getIndex());
                return definition.StringSubstitution(fieldSpecification);
            default:
                throw new UnsupportedOperationException("Can't handle: " + axiom.getSemantic());

        }
    }

    protected AbstractLogicNode[] getChildren(GenericAxiom axiom, LogicalExpressionOchreImpl definition) {
        List<GenericAxiom> childrenAxioms = definitionTree.get(axiom);
        List<AbstractLogicNode> children = new ArrayList<>(childrenAxioms.size());
        childrenAxioms.forEach((childAxiom) -> children.add(addToDefinition(childAxiom, definition)));
        return children.toArray(new AbstractLogicNode[children.size()]);
    }

    @Override
    public DisjointWith disjointWith(ConceptSpecification conceptSpecification) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.DISJOINT_WITH, this);
        axiomParameters.put(axiom.getIndex(), conceptSpecification);
        return axiom;
    }

    @Override
    public ConceptAssertion conceptAssertion(ConceptSpecification conceptSpecification) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.CONCEPT, this);
        axiomParameters.put(axiom.getIndex(), conceptSpecification);
        return axiom;
    }

    @Override
     public ConceptAssertion conceptAssertion(Integer conceptNid) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.CONCEPT, this);
        axiomParameters.put(axiom.getIndex(), conceptNid);
        return axiom;
    }

    @Override
    public AllRole allRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.ROLE_ALL, this);
        addToDefinitionTree(axiom, roleRestriction);
        axiomParameters.put(axiom.getIndex(), roleTypeSpecification);
        return axiom;
    }

    private AllRole allRole(Integer roleTypeNid, Assertion roleRestriction) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.ROLE_ALL, this);
        addToDefinitionTree(axiom, roleRestriction);
        axiomParameters.put(axiom.getIndex(), roleTypeNid);
        return axiom;
    }

    @Override
    public Feature feature(ConceptSpecification featureTypeSpecification, LiteralAssertion literal) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.FEATURE, this);
        addToDefinitionTree(axiom, literal);
        axiomParameters.put(axiom.getIndex(), featureTypeSpecification);
        return axiom;
    }
    private Feature feature(Integer featureTypeNid, LiteralAssertion literal) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.FEATURE, this);
        addToDefinitionTree(axiom, literal);
        axiomParameters.put(axiom.getIndex(), featureTypeNid);
        return axiom;
    }

    @Override
    public SomeRole someRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.ROLE_SOME, this);
        addToDefinitionTree(axiom, roleRestriction);
        axiomParameters.put(axiom.getIndex(), roleTypeSpecification);
        return axiom;
    }

    @Override
    public SomeRole someRole(Integer roleTypeConceptNid, Assertion roleRestriction) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.ROLE_SOME, this);
        addToDefinitionTree(axiom, roleRestriction);
        axiomParameters.put(axiom.getIndex(), roleTypeConceptNid);
        return axiom;
    }

    @Override
    public Template template(ConceptSpecification templateSpecification, ConceptSpecification assemblageToPopulateTemplateSpecification) {
        checkNotBuilt();
        GenericAxiom axiom = new GenericAxiom(NodeSemantic.TEMPLATE, this);
        axiomParameters.put(axiom.getIndex(), new Object[]{templateSpecification, assemblageToPopulateTemplateSpecification});
        return axiom;
    }

}
