package sh.isaac.provider.elk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.elk.owl.interfaces.*;
import org.semanticweb.elk.owl.iris.ElkFullIri;
import org.semanticweb.elk.owl.managers.ElkObjectEntityRecyclingFactory;
import sh.isaac.api.DataSource;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.*;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.FeatureNodeWithNids;
import sh.isaac.model.logic.node.internal.PropertyPatternImplicationWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class GraphToElkTranslator {
    private static final Logger LOG = LogManager.getLogger();
    public static final String NOT_SUPPORTED_BY_EL = "Not supported by EL++.";
    public static final String SUPPORTED_BUT_NOT_YET_IMPLEMENTED = "Supported, but not yet implemented.";

    private final ElkObjectEntityRecyclingFactory elkObjectFactory = new ElkObjectEntityRecyclingFactory();

    public List<ElkAxiom> translate(LogicGraphVersion logicGraphVersion) {
        if (logicGraphVersion.getReferencedComponentNid() >= 0) {
            throw new IllegalStateException("Referenced component nid must be negative: " + logicGraphVersion.getReferencedComponentNid());
        }
        final LogicalExpressionImpl logicalExpression = new LogicalExpressionImpl(logicGraphVersion.getGraphData(),
                DataSource.INTERNAL);
        logicalExpression.setConceptBeingDefinedNid(logicGraphVersion.getReferencedComponentNid());
        List<ElkAxiom> results = new ArrayList<>();
        generateAxioms(logicalExpression.getRoot(), logicGraphVersion.getReferencedComponentNid(), logicalExpression, results);
        return results;
    }


    /**
     * Generate axioms.
     *
     * @param logicNode the logic node
     * @param conceptNid the concept nid
     * @param logicGraph the logic graph
     * @return the optional
     */
    private Optional<? extends ElkObject> generateAxioms(LogicNode logicNode,
                                             int conceptNid,
                                             LogicalExpressionImpl logicGraph,
                                             List<ElkAxiom> results) {
        switch (logicNode.getNodeSemantic()) {
            case AND:
                return processAnd((AndNode) logicNode, conceptNid, logicGraph, results);

            case CONCEPT:
                // A concept is not an axiom, but it needs to be allocated by the factory,
                // that stores weak references to it.
                final ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) logicNode;
                return Optional.of(getConcept(conceptNode.getConceptNid()));

            case DEFINITION_ROOT:
                processRoot(logicNode, conceptNid, logicGraph, results);
                return Optional.empty();

            case FEATURE:
                throw new UnsupportedOperationException("Feature nodes not supported.");
                //return processFeatureNode((FeatureNodeWithNids) logicNode, conceptNid, logicGraph);

            case NECESSARY_SET:
                processNecessarySet((NecessarySetNode) logicNode, conceptNid, logicGraph, results);
                return Optional.empty();

            case PROPERTY_SET:
                return processPropertySet((PropertySetNode) logicNode, conceptNid, logicGraph, results);

            case ROLE_SOME:
                return processRoleNodeSome((RoleNodeSomeWithNids) logicNode, conceptNid, logicGraph, results);

            case SUFFICIENT_SET:
                processSufficientSet((SufficientSetNode) logicNode, conceptNid, logicGraph, results);
                return Optional.empty();

            case DISJOINT_WITH:
            case OR:
            case ROLE_ALL:
                throw new UnsupportedOperationException(NOT_SUPPORTED_BY_EL);

            case SUBSTITUTION_BOOLEAN:
            case SUBSTITUTION_CONCEPT:
            case SUBSTITUTION_FLOAT:
            case SUBSTITUTION_INSTANT:
            case SUBSTITUTION_INTEGER:
            case SUBSTITUTION_STRING:
            case TEMPLATE:
                throw new UnsupportedOperationException(SUPPORTED_BUT_NOT_YET_IMPLEMENTED);

            case LITERAL_BOOLEAN:
            case LITERAL_DOUBLE:
            case LITERAL_INSTANT:
            case LITERAL_INTEGER:
            case LITERAL_STRING:
                throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: " + logicNode +
                        " Concept: " + conceptNid + " graph: " + logicGraph);

            default:
                throw new UnsupportedOperationException("ar Can't handle: " + logicNode.getNodeSemantic());
        }
    }

    /**
     * Generate literals.
     *
     * @param logicNode the logic node
     * @param c the c
     * @param logicGraph the logic graph
     * @return the optional
     */
    @SuppressWarnings("deprecation")
    private Optional<ElkLiteral> generateLiterals(LogicNode logicNode, ElkClass c, LogicalExpressionImpl logicGraph) {
        switch (logicNode.getNodeSemantic()) {
            case LITERAL_BOOLEAN:
                final LiteralNodeBoolean literalNodeBoolean = (LiteralNodeBoolean) logicNode;
                //return Optional.of(Factory.createBooleanLiteral(literalNodeBoolean.getLiteralValue()));
                return Optional.of(elkObjectFactory.getLiteral(Boolean.toString(literalNodeBoolean.getLiteralValue()),
                        elkObjectFactory.getXsdString()));//TODO elk does not seem to have a boolean data type?


            case LITERAL_DOUBLE:
                final LiteralNodeDouble literalNodeFloat = (LiteralNodeDouble) logicNode;
                //return Optional.of(Factory.createFloatLiteral((float) literalNodeFloat.getLiteralValue()));
                return Optional.of(elkObjectFactory.getLiteral(Double.toString(literalNodeFloat.getLiteralValue()),
                        elkObjectFactory.getXsdDouble()));

            case LITERAL_INSTANT:
                final LiteralNodeInstant literalNodeInstant = (LiteralNodeInstant) logicNode;
                final Calendar calendar           = Calendar.getInstance();

                calendar.setTimeInMillis(literalNodeInstant.getLiteralValue()
                        .toEpochMilli());
                //return Optional.of(Factory.createDateLiteral(calendar));
                return Optional.of(elkObjectFactory.getLiteral(calendar.toString(),
                        elkObjectFactory.getXsdDateTimeStamp())); //TODO string representation is probably not right...

            case LITERAL_INTEGER:
                final LiteralNodeInteger literalNodeInteger = (LiteralNodeInteger) logicNode;
                //return Optional.of(Factory.createIntegerLiteral(literalNodeInteger.getLiteralValue()));
                return Optional.of(elkObjectFactory.getLiteral(Integer.toString(literalNodeInteger.getLiteralValue()),
                        elkObjectFactory.getXsdLong()));

            case LITERAL_STRING:
                final LiteralNodeString literalNodeString = (LiteralNodeString) logicNode;

                //return Optional.of(Factory.createStringLiteral(literalNodeString.getLiteralValue()));
                return Optional.of(elkObjectFactory.getLiteral(literalNodeString.getLiteralValue(),
                        elkObjectFactory.getXsdString()));//TODO elk does not seem to have a boolean data type?

            default:
                throw new UnsupportedOperationException("Expected literal logicNode, found: " + logicNode + " Concept: " + c +
                        " graph: " + logicGraph);
        }
    }

    /**
     * Process and.
     *
     * @param andNode the and node
     * @param conceptNid the concept nid
     * @param logicGraph the logic graph
     * @return the optional
     */
    private Optional<ElkObjectIntersectionOf> processAnd(AndNode andNode, int conceptNid,
                                                         LogicalExpressionImpl logicGraph, List<ElkAxiom> results) {
        final LogicNode[] childrenLogicNodes  = andNode.getChildren();
        final List<ElkClassExpression>   conjunctionConcepts = new ArrayList<>(childrenLogicNodes.length);

        for (int i = 0; i < childrenLogicNodes.length; i++) {
            Optional<? extends ElkObject> optionalClassExpression = generateAxioms(childrenLogicNodes[i], conceptNid, logicGraph, results);
            if (optionalClassExpression.isPresent()) {
                conjunctionConcepts.add((ElkClassExpression) optionalClassExpression.get());
            } else {
                throw new IllegalStateException("ElkClassExpression expected...");
            }
        }
        ElkObjectIntersectionOf elkIntersection = elkObjectFactory.getObjectIntersectionOf(conjunctionConcepts);
        return Optional.of(elkIntersection);
    }

    /**
     * Process feature node.
     *
     * @param featureNode the feature node
     * @param conceptNid the concept nid
     * @param logicGraph the logic graph
     * @return the optional
     */
    private Optional<ElkClass> processFeatureNode(FeatureNodeWithNids featureNode,
                                                 int conceptNid,
                                                 LogicalExpressionImpl logicGraph) {
        throw new UnsupportedOperationException("Feature node unsupported for Elk");
        /*
        final Feature     theFeature = getFeature(featureNode.getTypeConceptNid());
        final LogicNode[] children   = featureNode.getChildren();

        if (children.length != 1) {
            throw new IllegalStateException("FeatureNode can only have one child. Concept: " + conceptNid + " graph: " +
                    logicGraph);
        }

        final Optional<Literal> optionalLiteral = generateLiterals(children[0], getConcept(conceptNid), logicGraph);

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

         */
    }

    /**
     * Process necessary set.
     *
     * @param necessarySetNode the necessary set node
     * @param conceptNid the concept nid
     * @param logicGraph the logic graph
     */
    private void processNecessarySet(NecessarySetNode necessarySetNode,
                                     int conceptNid,
                                     LogicalExpressionImpl logicGraph,
                                     List<ElkAxiom> results) {
        final LogicNode[] children = necessarySetNode.getChildren();

        if (children.length != 1) {
            throw new IllegalStateException("necessarySetNode can only have one child. Concept: " + conceptNid +
                    " graph: " + logicGraph);
        }

        if (!(children[0] instanceof AndNode)) {
            throw new IllegalStateException("necessarySetNode can only have AND for a child. Concept: " + conceptNid +
                    " graph: " + logicGraph);
        }

        final Optional<? extends ElkObject> optionalConjunctionConcept = generateAxioms(children[0], conceptNid, logicGraph, results);

        if (optionalConjunctionConcept.isPresent()) {
            ElkSubClassOfAxiom subClassOfAxiom = elkObjectFactory.getSubClassOfAxiom((ElkClassExpression) optionalConjunctionConcept.get(),
                    getConcept(conceptNid));
            results.add(subClassOfAxiom);
        } else {
            throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                    " graph: " + logicGraph);
        }
    }

    private Optional<ElkClass> processPropertySet(PropertySetNode propertySetNode,
                                    int conceptNid,
                                    LogicalExpressionImpl logicGraph,
                                    List<ElkAxiom> results) {
        final LogicNode[] children = propertySetNode.getChildren();

        if (children.length != 1) {
            throw new IllegalStateException("PropertySetNode can only have one child. Concept: " + conceptNid +
                    " graph: " + logicGraph);
        }

        if (!(children[0] instanceof AndNode)) {
            throw new IllegalStateException("PropertySetNode can only have AND for a child. Concept: " + conceptNid +
                    " graph: " + logicGraph);
        }

        for (LogicNode node: children[0].getChildren()) {
            switch (node.getNodeSemantic()) {
                case CONCEPT: {
                    final ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) node;
                    ElkSubObjectPropertyExpression subProperty = elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(conceptNid)));
                    ElkObjectPropertyExpression superProperty = elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(conceptNode.getConceptNid())));
                    ElkSubObjectPropertyOfAxiom subObjectPropertyOfAxiom = elkObjectFactory.getSubObjectPropertyOfAxiom(subProperty, superProperty);
                    results.add(subObjectPropertyOfAxiom);
                    break;
                }


                case PROPERTY_PATTERN_IMPLICATION: {
                    final PropertyPatternImplicationWithNids propertyPatternImplicationNode = (PropertyPatternImplicationWithNids) node;
                    ElkObjectProperty subProperty = elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(conceptNid)));

                    List<ElkObjectPropertyExpression> propertyChainList = new ArrayList<>(propertyPatternImplicationNode.getPropertyPattern().length);
                    for (int nid: propertyPatternImplicationNode.getPropertyPattern()) {
                        propertyChainList.add(elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(nid))));
                    }
                    ElkObjectPropertyChain propertyChain = elkObjectFactory.getObjectPropertyChain(propertyChainList);
                    ElkSubObjectPropertyOfAxiom subObjectPropertyOfAxiom = elkObjectFactory.getSubObjectPropertyOfAxiom(propertyChain, subProperty);
                    results.add(subObjectPropertyOfAxiom);
                    break;
                }

                default:
                    throw new UnsupportedOperationException("Can't handle: " + node + " in: " + logicGraph);
            }
        }
        return Optional.empty();
    }

    /**
     * Process role node some.
     *
     * @param roleNodeSome the role node some
     * @param conceptNid the concept nid
     * @param logicGraph the logic graph
     * @return the optional
     */
    private Optional<ElkObject> processRoleNodeSome(RoleNodeSomeWithNids roleNodeSome,
                                                  int conceptNid,
                                                  LogicalExpressionImpl logicGraph,
                                                   List<ElkAxiom> results) {
        final LogicNode[] children = roleNodeSome.getChildren();

        if (children.length != 1) {
            throw new IllegalStateException("RoleNodeSome can only have one child. Concept: " + conceptNid + " graph: " +
                    logicGraph);
        }

        Optional<? extends ElkObject> optionalRestriction = generateAxioms(children[0], conceptNid, logicGraph, results);

        if (optionalRestriction.isPresent()) {
            ElkObjectProperty x = elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(conceptNid)));
            ElkClassExpression y = (ElkClassExpression) optionalRestriction.get();
            ElkObjectSomeValuesFrom elkObjectSomeValuesFrom = elkObjectFactory.getObjectSomeValuesFrom(x, y);
            return Optional.of(elkObjectSomeValuesFrom);
        }
        throw new UnsupportedOperationException("Child of role node can not return null concept. Concept: " +
                conceptNid + " graph: " + logicGraph);
    }

    /**
     * Process root.
     *
     * @param logicNode the logic node
     * @param conceptNid the concept nid
     * @param logicGraph the logic graph
     * @throws IllegalStateException the illegal state exception
     */
    private void processRoot(LogicNode logicNode,
                             int conceptNid,
                             LogicalExpressionImpl logicGraph,
                             List<ElkAxiom> results)
            throws IllegalStateException {
        final RootNode rootNode = (RootNode) logicNode;

        boolean propertySet = false;

        for (final LogicNode child: rootNode.getChildren()) {
            if (child instanceof PropertySetNode) {
                propertySet = true;
            }
        }

        for (final LogicNode child: rootNode.getChildren()) {

            if (propertySet) {
                if (child instanceof PropertySetNode) {
                    final Optional<? extends ElkObject> axiom = generateAxioms(child, conceptNid, logicGraph, results);
                    if (axiom.isPresent()) {
                        throw new IllegalStateException("Children of root logicNode should not return axioms. Concept: " +
                                conceptNid + " graph: " + logicGraph);
                    }
                }
            } else {
                final Optional<? extends ElkObject> axiom = generateAxioms(child, conceptNid, logicGraph, results);
                if (axiom.isPresent()) {
                    throw new IllegalStateException("Children of root logicNode should not return axioms. Concept: " +
                            conceptNid + " graph: " + logicGraph);
                }
            }
        }
    }

    /**
     * Process sufficient set.
     *
     * @param sufficientSetNode the sufficient set node
     * @param conceptNid the concept nid
     * @param logicGraph the logic graph
     */
    private void processSufficientSet(SufficientSetNode sufficientSetNode,
                                      int conceptNid,
                                      LogicalExpressionImpl logicGraph,
                                      List<ElkAxiom> results) {
        final LogicNode[] children = sufficientSetNode.getChildren();

        if (children.length != 1) {
            throw new IllegalStateException("SufficientSetNode can only have one child. Concept: " + conceptNid +
                    " graph: " + logicGraph);
        }

        if (!(children[0] instanceof AndNode)) {
            throw new IllegalStateException("SufficientSetNode can only have AND for a child. Concept: " + conceptNid +
                    " graph: " + logicGraph);
        }

        final Optional<? extends ElkObject> optionalConjunctionConcept = generateAxioms(children[0], conceptNid, logicGraph, results);

        if (optionalConjunctionConcept.isPresent()) {
            ElkClassExpression conjunctionConcept = (ElkClassExpression) optionalConjunctionConcept.get();
            ElkSubClassOfAxiom axAsubB = elkObjectFactory.getSubClassOfAxiom(getConcept(conceptNid), conjunctionConcept);
            ElkSubClassOfAxiom axBsubA = elkObjectFactory.getSubClassOfAxiom(conjunctionConcept, getConcept(conceptNid));

            results.add(axAsubB);
            results.add(axBsubA);
        } else {
            throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                    " graph: " + logicGraph);
        }
    }

    //~--- get methods ---------------------------------------------------------

    ElkClass getConcept(int conceptNid) {
        return elkObjectFactory.getClass(new ElkFullIri(Integer.toString(conceptNid)));
    }

}
