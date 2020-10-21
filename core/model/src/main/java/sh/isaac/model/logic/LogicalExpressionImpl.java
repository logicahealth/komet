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
package sh.isaac.model.logic;

//~--- JDK imports ------------------------------------------------------------
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.list.IntArrayList;
import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.DataSource;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.ConcreteDomainOperators;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.ConnectorNode;
import sh.isaac.model.logic.node.DisjointWithNode;
import sh.isaac.model.logic.node.LiteralNode;
import sh.isaac.model.logic.node.LiteralNodeBoolean;
import sh.isaac.model.logic.node.LiteralNodeDouble;
import sh.isaac.model.logic.node.LiteralNodeInstant;
import sh.isaac.model.logic.node.LiteralNodeInteger;
import sh.isaac.model.logic.node.LiteralNodeString;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.OrNode;
import sh.isaac.model.logic.node.PropertySetNode;
import sh.isaac.model.logic.node.RootNode;
import sh.isaac.model.logic.node.SubstitutionNode;
import sh.isaac.model.logic.node.SubstitutionNodeBoolean;
import sh.isaac.model.logic.node.SubstitutionNodeConcept;
import sh.isaac.model.logic.node.SubstitutionNodeFloat;
import sh.isaac.model.logic.node.SubstitutionNodeInstant;
import sh.isaac.model.logic.node.SubstitutionNodeInteger;
import sh.isaac.model.logic.node.SubstitutionNodeLiteral;
import sh.isaac.model.logic.node.SubstitutionNodeString;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.external.ConceptNodeWithUuids;
import sh.isaac.model.logic.node.external.FeatureNodeWithUuids;
import sh.isaac.model.logic.node.external.RoleNodeAllWithUuids;
import sh.isaac.model.logic.node.external.RoleNodeSomeWithUuids;
import sh.isaac.model.logic.node.external.TemplateNodeWithUuids;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.FeatureNodeWithNids;
import sh.isaac.model.logic.node.internal.PropertyPatternImplicationWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.model.logic.node.internal.TemplateNodeWithNids;
import sh.isaac.model.logic.node.internal.TypedNodeWithNids;
import sh.isaac.model.tree.TreeNodeVisitDataImpl;

//~--- classes ----------------------------------------------------------------
/**
 * Created by kec on 12/6/14.
 *
 * TODO need version of Pack that uses UUIDs for change sets
 *
 * TODO need unique way of identifying data columns for substitution: Use
 * enumerations for now
 *
 * TODO Standard refset for never grouped roles
 *
 * TODO Standard refset for right identities
 */
public class LogicalExpressionImpl
        implements LogicalExpression {

    private static final Logger LOG = LogManager.getLogger();
    
    private static final String CONCEPT_NIDS_AT_OR_ABOVE_NODE = "ConceptsReferencedAtNodeOrAbove";

    public static final byte SERIAL_FORMAT_VERSION = 1;
    private static final AtomicInteger terminationErrorWarningCount = new AtomicInteger();

    /**
     * The Constant NODE_SEMANTICS.
     */
    private static final NodeSemantic[] NODE_SEMANTICS = NodeSemantic.values();

    /**
     * The Constant MEANINGFUL_NODE_SEMANTICS.
     */
    private static final EnumSet<NodeSemantic> MEANINGFUL_NODE_SEMANTICS = EnumSet.of(NodeSemantic.CONCEPT,
            NodeSemantic.SUBSTITUTION_CONCEPT);

    /**
     * The isa nid.
     */
    protected static int isaNid = 0;

    //~--- fields --------------------------------------------------------------
    /**
     * The concept nid.
     */
    transient int conceptBeingDefinedNid = TermAux.UNINITIALIZED_COMPONENT_ID.getNid();

    /**
     * The logic nodes.
     */
    ArrayList<AbstractLogicNode> logicNodes = new ArrayList<>();

    /**
     * The root node index.
     */
    int rootNodeIndex = -1;

    transient SimpleObjectProperty<CommitStates> commitStateProperty = new SimpleObjectProperty<>(CommitStates.COMMITTED);

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new logical expression.
     */
    public LogicalExpressionImpl() {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
    }

    /**
     * Instantiates a new logical expression.
     *
     * @param nodeDataArray the node data array
     * @param dataSource the data source
     */
    public LogicalExpressionImpl(byte[][] nodeDataArray, DataSource dataSource) {
        commitStateProperty.set(CommitStates.COMMITTED);
        if (nodeDataArray == null) {
            this.logicNodes = new ArrayList<>(0);
        } else {
            this.logicNodes = new ArrayList<>(nodeDataArray.length);
            for (final byte[] nodeDataArray1 : nodeDataArray) {
                final ByteArrayDataBuffer dataInputStream = new ByteArrayDataBuffer(nodeDataArray1);
                dataInputStream.setObjectDataFormatVersion(dataInputStream.getByte());
                if (dataInputStream.getObjectDataFormatVersion() != SERIAL_FORMAT_VERSION) {
                    throw new IllegalStateException("Data format: " + dataInputStream.getObjectDataFormatVersion() + " does not equal SERIAL_FORMAT_VERSION");
                }
                final byte nodeSemanticIndex = dataInputStream.getByte();
                final NodeSemantic nodeSemantic = NODE_SEMANTICS[nodeSemanticIndex];

                switch (nodeSemantic) {
                    case DEFINITION_ROOT:
                        Root(dataInputStream);
                        break;

                    case PROPERTY_SET:
                        PropertySet(dataInputStream);
                        break;

                    case NECESSARY_SET:
                        NecessarySet(dataInputStream);
                        break;

                    case SUFFICIENT_SET:
                        SufficientSet(dataInputStream);
                        break;

                    case AND:
                        And(dataInputStream);
                        break;

                    case OR:
                        Or(dataInputStream);
                        break;

                    case DISJOINT_WITH:
                        DisjointWith(dataInputStream);
                        break;

                    case ROLE_ALL:
                        switch (dataSource) {
                            case EXTERNAL:
                                AllRoleWithUuids(dataInputStream);
                                break;

                            case INTERNAL:
                                AllRole(dataInputStream);
                                break;

                            default:
                                throw new UnsupportedOperationException("v Can't handle: " + dataSource);
                        }

                        break;

                    case ROLE_SOME:
                        switch (dataSource) {
                            case EXTERNAL:
                                SomeRoleWithUuids(dataInputStream);
                                break;

                            case INTERNAL:
                                SomeRole(dataInputStream);
                                break;

                            default:
                                throw new UnsupportedOperationException("w Can't handle: " + dataSource);
                        }

                        break;

                    case FEATURE:
                        switch (dataSource) {
                            case EXTERNAL:
                                FeatureWithUuids(dataInputStream);
                                break;

                            case INTERNAL:
                                Feature(dataInputStream);
                                break;

                            default:
                                throw new UnsupportedOperationException("x Can't handle: " + dataSource);
                        }

                        break;

                    case LITERAL_BOOLEAN:
                        BooleanLiteral(dataInputStream);
                        break;

                    case LITERAL_DOUBLE:
                        DoubleLiteral(dataInputStream);
                        break;

                    case LITERAL_INSTANT:
                        InstantLiteral(dataInputStream);
                        break;

                    case LITERAL_INTEGER:
                        IntegerLiteral(dataInputStream);
                        break;

                    case LITERAL_STRING:
                        StringLiteral(dataInputStream);
                        break;

                    case CONCEPT:
                        switch (dataSource) {
                            case EXTERNAL:
                                ConceptWithUuids(dataInputStream);
                                break;

                            case INTERNAL:
                                Concept(dataInputStream);
                                break;

                            default:
                                throw new UnsupportedOperationException("y Can't handle: " + dataSource);
                        }

                        break;

                    case TEMPLATE:
                        switch (dataSource) {
                            case EXTERNAL:
                                TemplateWithUuids(dataInputStream);
                                break;

                            case INTERNAL:
                                Template(dataInputStream);
                                break;

                            default:
                                throw new UnsupportedOperationException("z Can't handle: " + dataSource);
                        }

                        break;

                    case SUBSTITUTION_BOOLEAN:
                        BooleanSubstitution(dataInputStream);
                        break;

                    case SUBSTITUTION_CONCEPT:
                        ConceptSubstitution(dataInputStream);
                        break;

                    case SUBSTITUTION_FLOAT:
                        FloatSubstitution(dataInputStream);
                        break;

                    case SUBSTITUTION_INSTANT:
                        InstantSubstitution(dataInputStream);
                        break;

                    case SUBSTITUTION_INTEGER:
                        IntegerSubstitution(dataInputStream);
                        break;

                    case SUBSTITUTION_STRING:
                        StringSubstitution(dataInputStream);
                        break;

                    case PROPERTY_PATTERN_IMPLICATION:
                        PropertyPatternImplication(dataInputStream);
                        break;

                    default:
                        throw new UnsupportedOperationException("aa Can't handle: " + nodeSemantic);
                }
            }
            this.logicNodes.trimToSize();
        }

    }

    /**
     * Called to generate an isomorphicExpression and a mergedExpression.
     *
     * @param another the logical expression to add nodes from.
     * @param solution an array mapping from the nodeId in another to the nodeId
     * in this expression. If the value of the solution element == -1, that node
     * is not added to this logical expression, otherwise the value of the
     * solution element is used for the nodeId in this logical expression.
     */
    public LogicalExpressionImpl(LogicalExpressionImpl another, int[] solution) {
        this.conceptBeingDefinedNid = another.conceptBeingDefinedNid;
        addNodesWithMap(another, solution, new int[another.getNodeCount()], another.rootNodeIndex);
        this.logicNodes.trimToSize();
        commitStateProperty.set(CommitStates.UNCOMMITTED);
    }

    /**
     * Instantiates a new logical expression impl.
     *
     * @param nodeDataArray the node data array
     * @param dataSource the data source
     * @param conceptId The concept that this expression defines.
     */
    public LogicalExpressionImpl(byte[][] nodeDataArray, DataSource dataSource, int conceptId) {
        this(nodeDataArray, dataSource);

        this.conceptBeingDefinedNid = conceptId;
        commitStateProperty.set(CommitStates.COMMITTED);
    }

    /**
     * Called to generate an isomorphicExpression and a mergedExpression.
     *
     * @param another the logical expression to add nodes from.
     * @param solution an array mapping from the nodeId in another to the nodeId
     * in this expression. If the value of the solution element == -1, that node
     * is not added to this logical expression, otherwise the value of the
     * solution element is used for the nodeId in this logical expression.
     * @param anotherToThisNodeIdMap contains a mapping from nodeId in another
     * to nodeId in this constructed expression.
     */
    public LogicalExpressionImpl(LogicalExpressionImpl another, int[] solution, int[] anotherToThisNodeIdMap) {
        addNodesWithMap(another, solution, anotherToThisNodeIdMap, another.rootNodeIndex);
        this.logicNodes.trimToSize();
        commitStateProperty.set(CommitStates.UNCOMMITTED);
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public CommitStates getCommitState() {
        return commitStateProperty.get();
    }

    @Override
    public void setUncommitted() {
        this.setCommitState(CommitStates.UNCOMMITTED);
    }

    public void setCommitState(CommitStates commitState) {
        commitStateProperty.set(commitState);
    }

    public SimpleObjectProperty<CommitStates> commitStateProperty() {
        return commitStateProperty;
    }

    /**
     * All role.
     *
     * @param dataInputStream the data input stream
     * @return the role node all with sequences
     */
    public final RoleNodeAllWithNids AllRole(ByteArrayDataBuffer dataInputStream) {
        return new RoleNodeAllWithNids(this, dataInputStream);
    }

    /**
     * All role.
     *
     * @param typeNid the type nid
     * @param restriction the restriction
     * @return the role node all with sequences
     */
    public RoleNodeAllWithNids AllRole(int typeNid, AbstractLogicNode restriction) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new RoleNodeAllWithNids(this, typeNid, restriction);
    }

    /**
     * All role with uuids.
     *
     * @param dataInputStream the data input stream
     * @return the role node all with uuids
     */
    public final RoleNodeAllWithUuids AllRoleWithUuids(ByteArrayDataBuffer dataInputStream) {
        return new RoleNodeAllWithUuids(this, dataInputStream);
    }

    /**
     * And.
     *
     * @param children the children
     * @return the and node
     */
    public final AndNode And(AbstractLogicNode... children) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new AndNode(this, children);
    }

    /**
     * And.
     *
     * @param dataInputStream the data input stream
     * @return the and node
     */
    public final AndNode And(ByteArrayDataBuffer dataInputStream) {
        return new AndNode(this, dataInputStream);
    }

    /**
     * Boolean literal.
     *
     * @param literalValue the literal value
     * @return the literal node boolean
     */
    public LiteralNodeBoolean BooleanLiteral(boolean literalValue) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new LiteralNodeBoolean(this, literalValue);
    }

    /**
     * Boolean literal.
     *
     * @param dataInputStream the data input stream
     * @return the literal node boolean
     */
    public final LiteralNodeBoolean BooleanLiteral(ByteArrayDataBuffer dataInputStream) {
        return new LiteralNodeBoolean(this, dataInputStream);
    }

    /**
     * Boolean substitution.
     *
     * @param dataInputStream the data input stream
     * @return the substitution node boolean
     */
    public final SubstitutionNodeBoolean BooleanSubstitution(ByteArrayDataBuffer dataInputStream) {
        return new SubstitutionNodeBoolean(this, dataInputStream);
    }

    /**
     * Boolean substitution.
     *
     * @param substitutionFieldSpecification the substitution field
     * specification
     * @return the substitution node boolean
     */
    public SubstitutionNodeBoolean BooleanSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new SubstitutionNodeBoolean(this, substitutionFieldSpecification);
    }

    /**
     * Concept.
     *
     * @param dataInputStream the data input stream
     * @return the concept node with sequences
     */
    public final ConceptNodeWithNids Concept(ByteArrayDataBuffer dataInputStream) {
        return new ConceptNodeWithNids(this, dataInputStream);
    }

    public final PropertyPatternImplicationWithNids PropertyPatternImplication(ByteArrayDataBuffer dataInputStream) {
        return new PropertyPatternImplicationWithNids(this, dataInputStream);
    }

    public final PropertyPatternImplicationWithNids PropertyPatternImplication(int[] propertyPattern, int propertyImplication) {
        return new PropertyPatternImplicationWithNids(this, propertyPattern, propertyImplication);
    }

    /**
     * Concept.
     *
     * @param conceptNid the concept nid
     * @return the concept node with sequences
     */
    public final ConceptNodeWithNids Concept(int conceptNid) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new ConceptNodeWithNids(this, conceptNid);
    }

    public final ConceptNodeWithNids Concept(ConceptSpecification specification) {
        return Concept(specification.getNid());
    }

    /**
     * Concept substitution.
     *
     * @param dataInputStream the data input stream
     * @return the substitution node concept
     */
    public final SubstitutionNodeConcept ConceptSubstitution(ByteArrayDataBuffer dataInputStream) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new SubstitutionNodeConcept(this, dataInputStream);
    }

    /**
     * Concept substitution.
     *
     * @param substitutionFieldSpecification the substitution field
     * specification
     * @return the substitution node concept
     */
    public SubstitutionNodeConcept ConceptSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new SubstitutionNodeConcept(this, substitutionFieldSpecification);
    }

    /**
     * Concept with uuids.
     *
     * @param dataInputStream the data input stream
     * @return the concept node with uuids
     */
    public final ConceptNodeWithUuids ConceptWithUuids(ByteArrayDataBuffer dataInputStream) {
        return new ConceptNodeWithUuids(this, dataInputStream);
    }

    /**
     * Disjoint with.
     *
     * @param children the children
     * @return the disjoint with node
     */
    public DisjointWithNode DisjointWith(AbstractLogicNode... children) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new DisjointWithNode(this, children);
    }

    /**
     * Disjoint with.
     *
     * @param dataInputStream the data input stream
     * @return the disjoint with node
     */
    public final DisjointWithNode DisjointWith(ByteArrayDataBuffer dataInputStream) {
        return new DisjointWithNode(this, dataInputStream);
    }

    /**
     * Feature.
     *
     * @param dataInputStream the data input stream
     * @return the feature node with sequences
     */
    public final FeatureNodeWithNids Feature(ByteArrayDataBuffer dataInputStream) {
        return new FeatureNodeWithNids(this, dataInputStream);
    }

    /**
     * Feature.
     *
     * @param typeNid the type nid
     * @param measureSemanticNid
     * @param operator
     * @param literal the literal
     * @return the feature node with sequences
     */
    public FeatureNodeWithNids Feature(int typeNid, int measureSemanticNid, ConcreteDomainOperators operator, AbstractLogicNode literal) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        // check for LiteralNode or SubstitutionNodeLiteral
        if ((literal instanceof LiteralNode) || (literal instanceof SubstitutionNodeLiteral)) {
            return new FeatureNodeWithNids(this, typeNid, measureSemanticNid, operator, literal);
        }

        throw new IllegalStateException("LogicNode must be of type LiteralNode or SubstitutionNodeLiteral. Found: "
                + literal);
    }

    /**
     * Feature with uuids.
     *
     * @param dataInputStream the data input stream
     * @return the feature node with uuids
     */
    public final FeatureNodeWithUuids FeatureWithUuids(ByteArrayDataBuffer dataInputStream) {
        return new FeatureNodeWithUuids(this, dataInputStream);
    }

    /**
     * Float literal.
     *
     * @param dataInputStream the data input stream
     * @return the literal node float
     */
    public final LiteralNodeDouble DoubleLiteral(ByteArrayDataBuffer dataInputStream) {
        return new LiteralNodeDouble(this, dataInputStream);
    }

    /**
     * Float literal.
     *
     * @param literalValue the literal value
     * @return the literal node float
     */
    public LiteralNodeDouble DoubleLiteral(double literalValue) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new LiteralNodeDouble(this, literalValue);
    }

    /**
     * Float substitution.
     *
     * @param dataInputStream the data input stream
     * @return the substitution node float
     */
    public final SubstitutionNodeFloat FloatSubstitution(ByteArrayDataBuffer dataInputStream) {
        return new SubstitutionNodeFloat(this, dataInputStream);
    }

    /**
     * Float substitution.
     *
     * @param substitutionFieldSpecification the substitution field
     * specification
     * @return the substitution node float
     */
    public SubstitutionNodeFloat FloatSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new SubstitutionNodeFloat(this, substitutionFieldSpecification);
    }

    /**
     * Instant literal.
     *
     * @param dataInputStream the data input stream
     * @return the literal node instant
     */
    public final LiteralNodeInstant InstantLiteral(ByteArrayDataBuffer dataInputStream) {
        return new LiteralNodeInstant(this, dataInputStream);
    }

    /**
     * Instant literal.
     *
     * @param literalValue the literal value
     * @return the literal node instant
     */
    public LiteralNodeInstant InstantLiteral(Instant literalValue) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new LiteralNodeInstant(this, literalValue);
    }

    /**
     * Instant substitution.
     *
     * @param dataInputStream the data input stream
     * @return the substitution node instant
     */
    public final SubstitutionNodeInstant InstantSubstitution(ByteArrayDataBuffer dataInputStream) {
        return new SubstitutionNodeInstant(this, dataInputStream);
    }

    /**
     * Instant substitution.
     *
     * @param substitutionFieldSpecification the substitution field
     * specification
     * @return the substitution node instant
     */
    public SubstitutionNodeInstant InstantSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new SubstitutionNodeInstant(this, substitutionFieldSpecification);
    }

    /**
     * Integer literal.
     *
     * @param dataInputStream the data input stream
     * @return the literal node integer
     */
    public final LiteralNodeInteger IntegerLiteral(ByteArrayDataBuffer dataInputStream) {
        return new LiteralNodeInteger(this, dataInputStream);
    }

    /**
     * Integer literal.
     *
     * @param literalValue the literal value
     * @return the literal node integer
     */
    public LiteralNodeInteger IntegerLiteral(int literalValue) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new LiteralNodeInteger(this, literalValue);
    }

    /**
     * Integer substitution.
     *
     * @param dataInputStream the data input stream
     * @return the substitution node integer
     */
    public final SubstitutionNodeInteger IntegerSubstitution(ByteArrayDataBuffer dataInputStream) {
        return new SubstitutionNodeInteger(this, dataInputStream);
    }

    /**
     * Integer substitution.
     *
     * @param substitutionFieldSpecification the substitution field
     * specification
     * @return the substitution node integer
     */
    public SubstitutionNodeInteger IntegerSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new SubstitutionNodeInteger(this, substitutionFieldSpecification);
    }

    /**
     * Necessary set.
     *
     * @param child the {@link AndNode} or {@link OrNode} node
     * @return the necessary set node
     */
    public final NecessarySetNode NecessarySet(ConnectorNode child) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new NecessarySetNode(this, child);
    }

    /**
     * Necessary set.
     *
     * @param dataInputStream the data input stream
     * @return the necessary set node
     */
    public final NecessarySetNode NecessarySet(ByteArrayDataBuffer dataInputStream) {
        return new NecessarySetNode(this, dataInputStream);
    }

    /**
     * Property set.
     *
     * @param child the {@link AndNode} or {@link OrNode} node
     * @return the property set node
     */
    public final PropertySetNode PropertySet(ConnectorNode child) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new PropertySetNode(this, child);
    }

    /**
     * Property set.
     *
     * @param dataInputStream the data input stream
     * @return the property set node
     */
    public final PropertySetNode PropertySet(ByteArrayDataBuffer dataInputStream) {
        return new PropertySetNode(this, dataInputStream);
    }

    /**
     * Or.
     *
     * @param children the children
     * @return the or node
     */
    public OrNode Or(AbstractLogicNode... children) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new OrNode(this, children);
    }

    /**
     * Or.
     *
     * @param dataInputStream the data input stream
     * @return the or node
     */
    public final OrNode Or(ByteArrayDataBuffer dataInputStream) {
        return new OrNode(this, dataInputStream);
    }

    /**
     * Root.
     *
     * @param children the children
     * @return the root node
     */
    public RootNode Root(ConnectorNode... children) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        final RootNode rootNode = new RootNode(this, children);

        this.rootNodeIndex = rootNode.getNodeIndex();
        return rootNode;
    }

    /**
     * Root.
     *
     * @param dataInputStream the data input stream
     * @return the root node
     */
    public final RootNode Root(ByteArrayDataBuffer dataInputStream) {
        final RootNode rootNode = new RootNode(this, dataInputStream);

        this.rootNodeIndex = rootNode.getNodeIndex();
        return rootNode;
    }

    /**
     * Some role.
     *
     * @param dataInputStream the data input stream
     * @return the role node some with sequences
     */
    public final RoleNodeSomeWithNids SomeRole(ByteArrayDataBuffer dataInputStream) {
        return new RoleNodeSomeWithNids(this, dataInputStream);
    }

    /**
     * Some role.
     *
     * @param typeNid the type nid
     * @param restriction the restriction
     * @return the role node some with sequences
     */
    public final RoleNodeSomeWithNids SomeRole(int typeNid, AbstractLogicNode restriction) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new RoleNodeSomeWithNids(this, typeNid, restriction);
    }

    /**
     * Some role with uuids.
     *
     * @param dataInputStream the data input stream
     * @return the role node some with uuids
     */
    public final RoleNodeSomeWithUuids SomeRoleWithUuids(ByteArrayDataBuffer dataInputStream) {
        return new RoleNodeSomeWithUuids(this, dataInputStream);
    }

    /**
     * String literal.
     *
     * @param dataInputStream the data input stream
     * @return the literal node string
     */
    public final LiteralNodeString StringLiteral(ByteArrayDataBuffer dataInputStream) {
        return new LiteralNodeString(this, dataInputStream);
    }

    /**
     * String literal.
     *
     * @param literalValue the literal value
     * @return the literal node string
     */
    public LiteralNodeString StringLiteral(String literalValue) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new LiteralNodeString(this, literalValue);
    }

    /**
     * String substitution.
     *
     * @param dataInputStream the data input stream
     * @return the substitution node string
     */
    public final SubstitutionNodeString StringSubstitution(ByteArrayDataBuffer dataInputStream) {
        return new SubstitutionNodeString(this, dataInputStream);
    }

    /**
     * String substitution.
     *
     * @param substitutionFieldSpecification the substitution field
     * specification
     * @return the substitution node string
     */
    public SubstitutionNodeString StringSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new SubstitutionNodeString(this, substitutionFieldSpecification);
    }

    /**
     * Sufficient set.
     *
     * @param child the {@link AndNode} or {@link OrNode} node
     * @return the sufficient set node
     */
    public final SufficientSetNode SufficientSet(ConnectorNode child) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new SufficientSetNode(this, child);
    }

    /**
     * Sufficient set.
     *
     * @param dataInputStream the data input stream
     * @return the sufficient set node
     */
    public final SufficientSetNode SufficientSet(ByteArrayDataBuffer dataInputStream) {
        return new SufficientSetNode(this, dataInputStream);
    }

    /**
     * Template.
     *
     * @param dataInputStream the data input stream
     * @return the template node with sequences
     */
    public final TemplateNodeWithNids Template(ByteArrayDataBuffer dataInputStream) {
        return new TemplateNodeWithNids(this, dataInputStream);
    }

    /**
     * Template.
     *
     * @param templateConceptId the template concept id
     * @param assemblageConceptId the assemblage concept id
     * @return the template node with sequences
     */
    public TemplateNodeWithNids Template(int templateConceptId, int assemblageConceptId) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return new TemplateNodeWithNids(this, templateConceptId, assemblageConceptId);
    }

    /**
     * Template with uuids.
     *
     * @param dataInputStream the data input stream
     * @return the template node with uuids
     */
    public final TemplateNodeWithUuids TemplateWithUuids(ByteArrayDataBuffer dataInputStream) {
        return new TemplateNodeWithUuids(this, dataInputStream);
    }

    /**
     * Adds the node at the end of list, with no links to the node.
     *
     * @param logicNode the logic node
     */
    public void addNode(LogicNode logicNode) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        logicNode.setNodeIndex((short) this.logicNodes.size());
        this.logicNodes.add((AbstractLogicNode) logicNode);
    }

    @Override
    public LogicalExpressionImpl removeNode(int nodeIndex) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        processDepthFirst((LogicNode node, TreeNodeVisitData visitData) -> {
            LogicNode[] children = node.getChildren();
            for (LogicNode childNode : children) {
                if (childNode.getNodeIndex() == nodeIndex) {
                    AbstractLogicNode parentNode = (AbstractLogicNode) node;
                    parentNode.removeChild((short) nodeIndex);
                }
            }
        });
        AbstractLogicNode nodeToRemove = this.logicNodes.get(nodeIndex);
        for (LogicNode childToRemove : nodeToRemove.getChildren()) {
            removeAdditionalNodes(childToRemove.getNodeIndex());
        }
        logicNodes.set(nodeIndex, null);
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        int[] solution = new int[this.logicNodes.size()];
        int[] anotherToThisNodeIdMap = new int[solution.length];
        int newNodeId = 0;
        for (int i = 0; i < solution.length; i++) {
            AbstractLogicNode node = this.logicNodes.get(i);
            if (node == null) {
                solution[i] = -1;
                anotherToThisNodeIdMap[i] = -1;
            } else {
                solution[i] = node.getNodeIndex();
                anotherToThisNodeIdMap[i] = newNodeId++;
            }
        }

        return new LogicalExpressionImpl(this, solution, anotherToThisNodeIdMap);
    }

    public void removeAdditionalNodes(int nodeIndex) {
        AbstractLogicNode nodeToRemove = this.logicNodes.get(nodeIndex);
        for (LogicNode childToRemove : nodeToRemove.getChildren()) {
            removeAdditionalNodes(childToRemove.getNodeIndex());
        }
        logicNodes.set(nodeIndex, null);
    }

    /**
     * Adds the nodes.
     *
     * @param another the logical expression to add nodes from.
     * @param solution an array mapping from the nodeId in another to the nodeId
     * in this expression. If the value of the solution element == -1, that node
     * is not added to this logical expression, otherwise the value of the
     * solution element is used for the nodeId in this logical expression.
     * @param oldIds the list of nodeIds in the provided logical expression
     * (another) to add to this logical expression on this invocation. Note that
     * children of the nodes indicated by oldIds may be added by recursive calls
     * to this method, if the oldId index in the solution array is >= 0.
     * @return the LogicNode elements added as a result of this instance of the
     * call, not including any children LogicNode elements added by recursive
     * calls. Those children LogicNode elements can be retrieved by recursively
     * traversing the children of these returned LogicNode elements.
     */
    public final LogicNode[] addNodes(LogicalExpressionImpl another, int[] solution, int... oldIds) {
        commitStateProperty.set(CommitStates.UNCOMMITTED);
        return this.addNodesWithMap(another, solution, null, oldIds);
    }

    /**
     * Contains.
     *
     * @param semantic the semantic
     * @return true, if successful
     */
    @Override
    public boolean contains(NodeSemantic semantic) {
        for (LogicNode node : this.logicNodes) {
            if (node != null && node.getNodeSemantic() == semantic) {
                return true;
            }
        }
        return false;
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final LogicalExpressionImpl other = (LogicalExpressionImpl) obj;

        if (this.logicNodes == other.logicNodes) {
            return true;
        }

        if (this.logicNodes != null) {
            if (this.logicNodes.size() != other.logicNodes.size()) {
                return false;
            }

            final TreeNodeVisitData graphVisitData = new TreeNodeVisitDataImpl(this.logicNodes.size());

            depthFirstVisit(null, getRoot(), graphVisitData, 0);
            return graphsEqual(this.getRoot(), other.getRoot(), 0, graphVisitData.getMaxDepth());
        }

        return true;
    }

    /**
     * Find isomorphisms.
     *
     * @param another the another
     * @return the isomorphic results
     */
    @Override
    public IsomorphicResults findIsomorphisms(LogicalExpression another) {
        try {
            IsomorphicResultsFromPathHash isomorphicCallable = new IsomorphicResultsFromPathHash(this, another);
            return isomorphicCallable.call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = 29 * hash + this.conceptBeingDefinedNid;
        return hash;
    }

    /**
     * Process depth first. The consumer will be presented with the current
     * logic node, and with graph visit data that provides information about the
     * other nodes that have been encountered. To get the current id of the
     * visit, within the consumers
     * {@code accept(LogicNode logicNode, TreeNodeVisitDataImpl visitData)}
     * method, get the node id of the presented logic node.
     *
     * @param consumer the consumer
     */
    @Override
    public void processDepthFirst(BiConsumer<LogicNode, TreeNodeVisitData> consumer) {
        processDepthFirst(getRoot(), consumer);
    }

    /**
     * Process the fragment starting at root in a depth first manner.
     *
     * @param fragmentRoot the fragment root
     * @param consumer the consumer
     */
    @Override
    public void processDepthFirst(LogicNode fragmentRoot, BiConsumer<LogicNode, TreeNodeVisitData> consumer) {
        init();

        final TreeNodeVisitData graphVisitData = new TreeNodeVisitDataImpl(this.logicNodes.size());

        depthFirstVisit(consumer, fragmentRoot, graphVisitData, 0);
    }

    @Override
    public void processDepthFirst(BiConsumer<LogicNode, TreeNodeVisitData> consumer, TreeNodeVisitData treeNodeVisitData) {
        init();
        depthFirstVisit(consumer, getRoot(), treeNodeVisitData, 0);
    }

    /**
     * Sort.
     */
    public void sort() {
        this.logicNodes.forEach((node) -> node.sort());
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toSimpleString() {
        final StringBuilder builder = new StringBuilder();

        processDepthFirst((LogicNode logicNode,
                TreeNodeVisitData graphVisitData) -> {
            if (!(logicNode instanceof RootNode)) {
                for (int i = 1; i < graphVisitData.getDistance(logicNode.getNodeIndex()); i++) {
                    builder.append("    ");
                }

                builder.append(logicNode.toSimpleString());
                builder.append("\n");
            }
        });
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * To string.
     *
     * @param nodeIdSuffix the node id suffix
     * @return the string
     */
    @Override
    public String toString(String nodeIdSuffix) {
        final StringBuilder builder = new StringBuilder();

        processDepthFirst((LogicNode logicNode,
                TreeNodeVisitData graphVisitData) -> {
            for (int i = 0; i < graphVisitData.getDistance(logicNode.getNodeIndex()); i++) {
                builder.append("    ");
            }

            builder.append(logicNode.toString(nodeIdSuffix));
            builder.append("\n");
        });
        return builder.toString();
    }

    @Override
    public String toBuilder() {
        final StringBuilder builder = new StringBuilder();
        builder.append("import sh.isaac.api.Get;\n");
        builder.append("import sh.isaac.api.logic.LogicalExpression;\n");
        builder.append("import sh.isaac.model.logic.definition.LogicalExpressionBuilderImpl;\n\n");
        builder.append("import static sh.isaac.api.logic.LogicalExpressionBuilder.And;\n\n");
        builder.append("import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;\n\n");
        builder.append("import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;\n\n");
        builder.append("import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;\n\n");
        builder.append("public class ComparisonExpression1 {\n");
        builder.append("static LogicalExpression getExpression() {\n");
        builder.append("       LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();\n");

        getRoot().addToBuilder(builder);

        builder.append("        return leb.build();\n");
        builder.append("}\n");

        return builder.toString();
    }

    /**
     * Depth first visit. The consumer will be presented with the current logic
     * node, and with graph visit data that provides information about the other
     * nodes that have been encountered. To get the current id of the visit, get
     * the node id of the presented logic node.
     *
     * @param consumer the consumer
     * @param logicNode the logic node
     * @param graphVisitData the graph visit data
     * @param depth the depth
     */
    protected void depthFirstVisit(BiConsumer<LogicNode, TreeNodeVisitData> consumer,
            LogicNode logicNode,
            TreeNodeVisitData graphVisitData,
            int depth) {
        if (depth > 100) {
            // toString depends on this method, so we can't include this.toString() in the exception...
            throw new RuntimeException("Depth limit exceeded for logicNode: " + logicNode);  // + " in graph: " + this);
        }

        graphVisitData.startNodeVisit(logicNode.getNodeIndex(), depth);

        final RoaringBitmap conceptsReferencedByNode = new RoaringBitmap();

        logicNode.addConceptsReferencedByNode(conceptsReferencedByNode);

        graphVisitData.getUserNodeSet(CONCEPT_NIDS_AT_OR_ABOVE_NODE, logicNode.getNodeIndex());

        logicNode.addConceptsReferencedByNode(graphVisitData.getUserNodeSet(CONCEPT_NIDS_AT_OR_ABOVE_NODE, logicNode.getNodeIndex()));

        OptionalInt predecessorNid = graphVisitData.getPredecessorNid(logicNode.getNodeIndex());
        if (predecessorNid.isPresent()) {

            graphVisitData.getUserNodeSet(CONCEPT_NIDS_AT_OR_ABOVE_NODE, predecessorNid.getAsInt()).forEach((IntConsumer) node -> {
                conceptsReferencedByNode.add(node);
            });
            graphVisitData.setUserNodeSet(CONCEPT_NIDS_AT_OR_ABOVE_NODE, logicNode.getNodeIndex(), conceptsReferencedByNode);
        }

        if (consumer != null) {
            consumer.accept(logicNode, graphVisitData);
        }

        if (logicNode.getChildren().length == 0) {
            graphVisitData.setLeafNode(logicNode.getNodeIndex());
        } else {
            int siblingGroupSequence;

            switch (logicNode.getNodeSemantic()) {
                case AND:
                case OR:
                case SUFFICIENT_SET:
                case NECESSARY_SET:
                case DISJOINT_WITH:
                case DEFINITION_ROOT:
                case PROPERTY_SET:
                    siblingGroupSequence = logicNode.getNodeIndex();
                    break;

                default:
                    siblingGroupSequence = graphVisitData.getSiblingGroupForNid(logicNode.getNodeIndex());
            }

            for (final LogicNode child : logicNode.getChildren()) {
                graphVisitData.setSiblingGroupForNid(child.getNodeIndex(), siblingGroupSequence);
                graphVisitData.setPredecessorNid(child.getNodeIndex(), logicNode.getNodeIndex());
                depthFirstVisit(consumer, child, graphVisitData, depth + 1);
            }
        }

        graphVisitData.endNodeVisit(logicNode.getNodeIndex());
    }

    /**
     * Initializes the logic nodes.
     */
    protected void init() {
        this.logicNodes.trimToSize();
    }

    /**
     * Adds the nodes with map.
     *
     * @param another the logical expression to add nodes from.
     * @param solution an array mapping from the nodeId in another to the nodeId
     * in this expression. If the value of the solution element == -1, that node
     * is not added to this logical expression, otherwise the value of the
     * solution element is used for the nodeId in this logical expression.
     * @param anotherToThisNodeIdMap contains a mapping from nodeId in another
     * to nodeId in this constructed expression.
     * @param oldIds the list of nodeIds in the provided logical expression
     * (another) to add to this logical expression on this invocation. Note that
     * children of the nodes indicated by oldIds may be added by recursive calls
     * to this method, if the oldId index in the solution array is >= 0.
     * @return the LogicNode elements added as a result of this instance of the
     * call, not including any children LogicNode elements added by recursive
     * calls. Those children LogicNode elements can be retrieved by recursively
     * traversing the children of these returned LogicNode elements.
     */
    private LogicNode[] addNodesWithMap(LogicalExpressionImpl another,
            int[] solution,
            int[] anotherToThisNodeIdMap,
            int... oldIds) {
        this.conceptBeingDefinedNid = another.conceptBeingDefinedNid;
        commitStateProperty.set(CommitStates.UNCOMMITTED);

        final AbstractLogicNode[] results = new AbstractLogicNode[oldIds.length];

        for (int i = 0; i < oldIds.length; i++) {
            final LogicNode oldLogicNode = another.getNode(oldIds[i]);

            switch (oldLogicNode.getNodeSemantic()) {
                case DEFINITION_ROOT:

                    results[i] = Root(Arrays.stream(addNodesWithMap(another,
                            solution,
                            anotherToThisNodeIdMap,
                            oldLogicNode.getChildStream()
                                    .filter( // the int[] of oldIds to add to the expression
                                            (oldChildNode) -> solution[oldChildNode.getNodeIndex()]
                                            >= 0 // if the solution element == -1, filter out
                                    )
                                    .mapToInt(
                                            (oldChildNode) -> oldChildNode.getNodeIndex() // the nodeId in the original expression
                                    )
                                    .toArray() // create the oldIds passed into the addNodes recursive call
                    ) // end of addNodes parameters; returns LogicNode[]
                    )
                            .map((LogicNode t) -> (ConnectorNode) t)
                            .toArray(
                                    ConnectorNode[]::new) // convert LogicNode[] to ConnectorNode[] to pass into the Root method call.
                    );
                    this.rootNodeIndex = results[i].getNodeIndex();
                    break;

                case PROPERTY_SET:
                {
                    LogicNode[] nodes = addNodesWithMap(another,
                            solution,
                            anotherToThisNodeIdMap,
                            oldLogicNode.getChildStream()
                                    .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                    .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray());
                    if (nodes.length != 1) {
                        throw new RuntimeException("Illegal construction");
                    }
                    results[i] = PropertySet((ConnectorNode) nodes[0]);
                    break;
                }
                case NECESSARY_SET:
                {
                    LogicNode[] nodes = addNodesWithMap(another,
                            solution,
                            anotherToThisNodeIdMap,
                            oldLogicNode.getChildStream()
                                    .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                    .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray());
                    if (nodes.length != 1) {
                        throw new RuntimeException("Illegal construction");
                    }
                    results[i] = NecessarySet((ConnectorNode) nodes[0]);
                    break;
                }
                case SUFFICIENT_SET:
                {
                    LogicNode[] nodes = addNodesWithMap(another,
                         solution,
                         anotherToThisNodeIdMap,
                         oldLogicNode.getChildStream()
                                 .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                 .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray());
                    if (nodes.length != 1) {
                       throw new RuntimeException("Illegal construction: " + nodes.length);
                    }
                    results[i] = SufficientSet((ConnectorNode) nodes[0]);
                    break;
                }
                case AND:
                    results[i] = And((AbstractLogicNode[]) addNodesWithMap(another,
                            solution,
                            anotherToThisNodeIdMap,
                            oldLogicNode.getChildStream()
                                    .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                    .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                                    .toArray()));
                    break;

                case OR:
                    results[i] = Or((AbstractLogicNode[]) addNodesWithMap(another,
                            solution,
                            anotherToThisNodeIdMap,
                            oldLogicNode.getChildStream()
                                    .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                    .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                                    .toArray()));
                    break;

                case DISJOINT_WITH:
                    results[i] = DisjointWith((AbstractLogicNode[]) addNodesWithMap(another,
                            solution,
                            anotherToThisNodeIdMap,
                            oldLogicNode.getChildStream()
                                    .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                    .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                                    .toArray()));
                    break;

                case ROLE_ALL:
                    results[i] = AllRole(((TypedNodeWithNids) oldLogicNode).getTypeConceptNid(),
                            (AbstractLogicNode) addNodesWithMap(another,
                                    solution,
                                    anotherToThisNodeIdMap,
                                    oldLogicNode.getChildStream()
                                            .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                            .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                                            .toArray())[0]);
                    break;

                case ROLE_SOME:
                    LogicNode[] nodes = addNodesWithMap(another,
                                    solution,
                                    anotherToThisNodeIdMap,
                                    oldLogicNode.getChildStream()
                                            .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                            .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                                            .toArray());
                    if (nodes.length == 0) {
                        int count = terminationErrorWarningCount.getAndIncrement();
                        if (count == 100) {
                            LOG.warn("Further role termination errors only logged at TRACE level.");
                        }
                        if (getConceptBeingDefinedNid() == -1) {
                            LOG.log(count > 100 ? Level.TRACE : Level.INFO, "Role termination error for unspecified isomorphic concept. \n this: {}\n that: {}",
                                    this, another);
                        } else {
                            LOG.log(count > 100 ? Level.TRACE : Level.INFO, "Role termination error for isomorphic concept: '{}' [{}]\n this: {}\n that: {}",
                                    Get.conceptDescriptionText(getConceptBeingDefinedNid()),
                                    Get.identifierService().getUuidPrimordialForNid(getConceptBeingDefinedNid()),
                                    this, another);
                        }
                        
                        results[i] = SomeRole(((TypedNodeWithNids) oldLogicNode).getTypeConceptNid(),
                                StringLiteral("Role termination error..."));
                    } else {
                        results[i] = SomeRole(((TypedNodeWithNids) oldLogicNode).getTypeConceptNid(),
                            (AbstractLogicNode) nodes[0]);
                    }
                    break;

                case FEATURE: {
                    FeatureNodeWithNids featureNode = (FeatureNodeWithNids) oldLogicNode;
                    results[i] = Feature(featureNode.getTypeConceptNid(), featureNode.getMeasureSemanticNid(),
                            featureNode.getOperator(),
                            (AbstractLogicNode) addNodesWithMap(another,
                                    solution,
                                    anotherToThisNodeIdMap,
                                    oldLogicNode.getChildStream()
                                            .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                            .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                                            .toArray())[0]);
                    break;
                }

                case LITERAL_BOOLEAN:
                    results[i] = BooleanLiteral(((LiteralNodeBoolean) oldLogicNode).getLiteralValue());
                    break;

                case LITERAL_DOUBLE:
                    results[i] = DoubleLiteral(((LiteralNodeDouble) oldLogicNode).getLiteralValue());
                    break;

                case LITERAL_INSTANT:
                    results[i] = InstantLiteral(((LiteralNodeInstant) oldLogicNode).getLiteralValue());
                    break;

                case LITERAL_INTEGER:
                    results[i] = IntegerLiteral(((LiteralNodeInteger) oldLogicNode).getLiteralValue());
                    break;

                case LITERAL_STRING:
                    results[i] = StringLiteral(((LiteralNodeString) oldLogicNode).getLiteralValue());
                    break;

                case CONCEPT:
                    results[i] = Concept(((ConceptNodeWithNids) oldLogicNode).getConceptNid());
                    break;

                case TEMPLATE:
                    results[i] = Template(((TemplateNodeWithNids) oldLogicNode).getTemplateConceptNid(),
                            ((TemplateNodeWithNids) oldLogicNode).getAssemblageConceptNid());
                    break;

                case SUBSTITUTION_BOOLEAN:
                    results[i] = BooleanSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
                    break;

                case SUBSTITUTION_CONCEPT:
                    results[i] = ConceptSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
                    break;

                case SUBSTITUTION_FLOAT:
                    results[i] = FloatSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
                    break;

                case SUBSTITUTION_INSTANT:
                    results[i] = InstantSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
                    break;

                case SUBSTITUTION_INTEGER:
                    results[i] = IntegerSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
                    break;

                case SUBSTITUTION_STRING:
                    results[i] = StringSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
                    break;

                case PROPERTY_PATTERN_IMPLICATION:
                    results[i] = PropertyPatternImplication(((PropertyPatternImplicationWithNids) oldLogicNode).getPropertyPattern(),
                            ((PropertyPatternImplicationWithNids) oldLogicNode).getPropertyImplication());
                    break;
                default:
                    throw new UnsupportedOperationException("ab Can't handle: " + oldLogicNode.getNodeSemantic());
            }

            if (anotherToThisNodeIdMap != null) {
                anotherToThisNodeIdMap[oldLogicNode.getNodeIndex()] = results[i].getNodeIndex();
            }
        }

        return results;
    }

    /**
     * Graphs equal.
     *
     * @param g1 the g 1
     * @param g2 the g 2
     * @param depth the depth
     * @param maxDepth the max depth
     * @return true, if successful
     */
    private boolean graphsEqual(AbstractLogicNode g1, AbstractLogicNode g2, int depth, int maxDepth) {
        if (g1.equals(g2)) {
            final AbstractLogicNode[] g1children = g1.getChildren();
            final AbstractLogicNode[] g2children = g2.getChildren();

            if (g1children.length != g2children.length) {
                return false;
            }

            if (g1children.length == 0) {
                return true;
            }

            final HashMap<Set<UUID>, IntArrayList> uuidSetNodeListMap = new HashMap<>();
            int depthToTest = 0;

            while ((uuidSetNodeListMap.size() < g1children.length) && (depthToTest < maxDepth - depth)) {
                depthToTest++;
                uuidSetNodeListMap.clear();

                for (final AbstractLogicNode child : g1children) {
                    final Set<UUID> nodeUuidSetForDepth = child.getNodeUuidSetForDepth(depthToTest);

                    if (!uuidSetNodeListMap.containsKey(nodeUuidSetForDepth)) {
                        final IntArrayList nodeList = new IntArrayList();

                        nodeList.add(child.getNodeIndex());
                        uuidSetNodeListMap.put(nodeUuidSetForDepth, nodeList);
                    } else {
                        uuidSetNodeListMap.get(nodeUuidSetForDepth)
                                .add(child.getNodeIndex());
                    }
                }
            }

            // need to try all combinations
            for (final AbstractLogicNode g2Child : g2children) {
                final Set<UUID> nodeUuidSetForDepth = g2Child.getNodeUuidSetForDepth(depthToTest);
                final IntArrayList possibleMatches = uuidSetNodeListMap.get(nodeUuidSetForDepth);

                if (possibleMatches == null) {
                    return false;
                }

                int match = -1;
                possibleMatches.trimToSize();
                for (final int possibleMatchIndex : possibleMatches.elements()) {
                    if (graphsEqual((AbstractLogicNode) this.logicNodes.get(possibleMatchIndex),
                            g2Child,
                            depth + 1,
                            maxDepth)) {
                        match = possibleMatchIndex;
                        break;
                    }
                }

                if (match == -1) {
                    return false;
                }

                possibleMatches.delete(match);
            }

            return true;
        }

        return false;
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the concept nid.
     *
     * @return the concept nid
     */
    @Override
    public int getConceptBeingDefinedNid() {
        return this.conceptBeingDefinedNid;
    }

    @Override
    public void setConceptBeingDefinedNid(int conceptNid) {
        this.conceptBeingDefinedNid = conceptNid;
    }

    /**
     * Gets the data.
     *
     * @param dataTarget the data target
     * @return the data
     */
    @Override
    public byte[][] getData(DataTarget dataTarget) {
        init();

        boolean containsNull = false;
        for (AbstractLogicNode node: this.logicNodes) {
            if (node == null) {
                containsNull = true;
                break;
            }
        }

        if (containsNull) {
            int[] solution = new int[this.logicNodes.size()];

            int nextNode = 0;
            for (int index = 0; index < solution.length; index++) {
                if (this.logicNodes.get(index) == null) {
                    solution[index] = -1;
                } else {
                    solution[index] = nextNode++;
                }
            }

            // LogicalExpressionImpl another, int[] solution
            LogicalExpressionImpl expression = new LogicalExpressionImpl(this, solution);
            return expression.getData(dataTarget);

        } else {
            final byte[][] byteArrayArray = new byte[this.logicNodes.size()][];

            for (int index = 0; index < byteArrayArray.length; index++) {
                byteArrayArray[index] = this.logicNodes.get(index)
                        .getBytes(dataTarget);
            }

            return byteArrayArray;
        }

    }

    /**
     * Checks if meaningful.
     *
     * @return true, if meaningful
     */
    @Override
    public boolean isMeaningful() {
        return this.logicNodes.stream()
                .anyMatch((node) -> (MEANINGFUL_NODE_SEMANTICS.contains(node.getNodeSemantic())));
    }

    /**
     * Gets the node.
     *
     * @param nodeIndex the node index
     * @return the node
     */
    @Override
    public LogicNode getNode(int nodeIndex) {
        return this.logicNodes.get(nodeIndex);
    }

    /**
     * Gets the node count.
     *
     * @return the node count
     */
    @Override
    public int getNodeCount() {
        return this.logicNodes.size();
    }

    /**
     * Gets the nodes of type.
     *
     * @param semantic the semantic
     * @return the nodes of type
     */
    @Override
    public List<LogicNode> getNodesOfType(NodeSemantic semantic) {
        List<LogicNode> results = new ArrayList<>();
        for (LogicNode node : this.logicNodes) {
            if (node.getNodeSemantic() == semantic) {
                results.add(node);
            }
        }
        return results;
    }

    /**
     * Gets the root.
     *
     * @return the root
     */
    @Override
    public final RootNode getRoot() {
        if (this.logicNodes.isEmpty() || this.rootNodeIndex == -1) {
            return Root();
        }

        return (RootNode) this.logicNodes.get(this.rootNodeIndex);
    }

    public void setParentIds() {
        setParentIds(getRoot());
    }
    public void setParentIds(AbstractLogicNode parent) {
        for (AbstractLogicNode child: parent.getChildren()) {
            child.setParentIndex(parent.getNodeIndex());
            setParentIds(child);
        }
    }

    boolean inNecessarySet(int nodeId) {
        setParentIds();
        Optional<LogicNode> optionalParent = this.getNode(nodeId).getParent();
        while (optionalParent.isPresent()) {
            LogicNode parentNode = optionalParent.get();
            if (parentNode.getNodeSemantic() == NodeSemantic.NECESSARY_SET) {
                return true;
            }
            optionalParent = parentNode.getParent();
        }
        return false;
    }

    @Override
    public boolean containsConcept(ConceptSpecification conceptSpecification) {
        return containsConcept(conceptSpecification.getNid());
    }

    @Override
    public boolean containsConcept(int nid) {
        for (AbstractLogicNode logicNode: logicNodes) {
            switch (logicNode.getNodeSemantic()) {
                case CONCEPT:
                    if (((ConceptNodeWithNids) logicNode).getConceptNid() == nid) {
                        return true;
                    }
                    break;
                case FEATURE:
                    if (((FeatureNodeWithNids) logicNode).getMeasureSemanticNid() == nid) {
                        return true;
                    }
                    if (((FeatureNodeWithNids) logicNode).getTypeConceptNid() == nid) {
                        return true;
                    }
                    break;
                case PROPERTY_PATTERN_IMPLICATION:
                    if (((PropertyPatternImplicationWithNids) logicNode).getPropertyImplication() == nid) {
                        return true;
                    }
                    for (int patternNid: ((PropertyPatternImplicationWithNids) logicNode).getPropertyPattern()) {
                        if (patternNid == nid) {
                            return true;
                        }
                    }
                    break;
                case ROLE_ALL:
                    if (((RoleNodeAllWithNids) logicNode).getTypeConceptNid() == nid) {
                        return true;
                    }
                    break;
                case ROLE_SOME:
                    if (((RoleNodeSomeWithNids) logicNode).getTypeConceptNid() == nid) {
                        return true;
                    }
                    break;
                case TEMPLATE:
                    if (((TemplateNodeWithNids) logicNode).getTemplateConceptNid() == nid) {
                        return true;
                    }
                    if (((TemplateNodeWithNids) logicNode).getAssemblageConceptNid() == nid) {
                        return true;
                    }
                    break;


                case DISJOINT_WITH:
                case AND:
                case OR:
                case DEFINITION_ROOT:
                case LITERAL_BOOLEAN:
                case LITERAL_DOUBLE:
                case LITERAL_INSTANT:
                case LITERAL_INTEGER:
                case LITERAL_STRING:
                case NECESSARY_SET:
                case PROPERTY_SET:
                case SUBSTITUTION_BOOLEAN:
                case SUBSTITUTION_CONCEPT:
                case SUBSTITUTION_FLOAT:
                case SUBSTITUTION_INSTANT:
                case SUBSTITUTION_INTEGER:
                case SUBSTITUTION_STRING:
                case SUFFICIENT_SET:
                default:
            }
        }
        return false;
    }

    @Override
    public LogicalExpression replaceAllConceptOccurences(ConceptSpecification conceptToFind, ConceptSpecification replacementConcept) {
        int nidToFind = conceptToFind.getNid();
        int replacementNid = replacementConcept.getNid();
        LogicalExpressionImpl newExpression = this.deepClone();
        for (AbstractLogicNode logicNode: newExpression.logicNodes) {
            switch (logicNode.getNodeSemantic()) {
                case CONCEPT:
                    if (((ConceptNodeWithNids) logicNode).getConceptNid() == nidToFind) {
                        ((ConceptNodeWithNids) logicNode).setConceptNid(replacementNid);
                    }
                    break;
                case FEATURE:
                    if (((FeatureNodeWithNids) logicNode).getMeasureSemanticNid() == nidToFind) {
                        ((FeatureNodeWithNids) logicNode).setMeasureSemanticNid(replacementNid);
                    }
                    if (((FeatureNodeWithNids) logicNode).getTypeConceptNid() == nidToFind) {
                        ((FeatureNodeWithNids) logicNode).setTypeConceptNid(replacementNid);
                    }
                    break;
                case PROPERTY_PATTERN_IMPLICATION:
                    if (((PropertyPatternImplicationWithNids) logicNode).getPropertyImplication() == nidToFind) {
                        ((PropertyPatternImplicationWithNids) logicNode).setPropertyImplication(replacementNid);
                    }
                    int[] propertyPattern = ((PropertyPatternImplicationWithNids) logicNode).getPropertyPattern();
                    for (int i = 0; i < propertyPattern.length; i++) {
                        if (propertyPattern[i] == nidToFind)  {
                            propertyPattern[i] = replacementNid;
                        }
                    }
                    break;
                case ROLE_ALL:
                    if (((RoleNodeAllWithNids) logicNode).getTypeConceptNid() == nidToFind) {
                        ((RoleNodeAllWithNids) logicNode).setTypeConceptNid(replacementNid);
                    }
                    break;
                case ROLE_SOME:
                    if (((RoleNodeSomeWithNids) logicNode).getTypeConceptNid() == nidToFind) {
                        ((RoleNodeSomeWithNids) logicNode).setTypeConceptNid(replacementNid);
                    }
                    break;
                case TEMPLATE:
                    if (((TemplateNodeWithNids) logicNode).getTemplateConceptNid() == nidToFind) {
                        ((TemplateNodeWithNids) logicNode).setTemplateConceptNid(replacementNid);
                    }
                    if (((TemplateNodeWithNids) logicNode).getAssemblageConceptNid() == nidToFind) {
                        ((TemplateNodeWithNids) logicNode).setAssemblageConceptNid(replacementNid);
                    }
                    break;


                case DISJOINT_WITH:
                case AND:
                case OR:
                case DEFINITION_ROOT:
                case LITERAL_BOOLEAN:
                case LITERAL_DOUBLE:
                case LITERAL_INSTANT:
                case LITERAL_INTEGER:
                case LITERAL_STRING:
                case NECESSARY_SET:
                case PROPERTY_SET:
                case SUBSTITUTION_BOOLEAN:
                case SUBSTITUTION_CONCEPT:
                case SUBSTITUTION_FLOAT:
                case SUBSTITUTION_INSTANT:
                case SUBSTITUTION_INTEGER:
                case SUBSTITUTION_STRING:
                case SUFFICIENT_SET:
                default:
            }
        }
        return newExpression;
    }

    LogicalExpressionImpl deepClone() {
        return new LogicalExpressionImpl(this.getData(DataTarget.INTERNAL), DataSource.INTERNAL);
    }
}
