package gov.vha.isaac.ochre.model.logic;

import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.api.DataSource;
import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.logic.IsomorphicResults;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import gov.vha.isaac.ochre.api.tree.TreeNodeVisitData;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.model.logic.node.*;
import gov.vha.isaac.ochre.model.logic.node.AbstractLogicNode;
import gov.vha.isaac.ochre.model.logic.node.external.ConceptNodeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.external.FeatureNodeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.external.RoleNodeAllWithUuids;
import gov.vha.isaac.ochre.model.logic.node.external.RoleNodeSomeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.external.TemplateNodeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.internal.ConceptNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.FeatureNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeAllWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeSomeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.TemplateNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.TypedNodeWithSequences;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.apache.mahout.math.list.IntArrayList;

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
public class LogicalExpressionOchreImpl implements LogicalExpression {

    private static final NodeSemantic[] NODE_SEMANTICS = NodeSemantic.values();

    private static final EnumSet<NodeSemantic> meaningfulNodeSemantics
            = EnumSet.of(NodeSemantic.CONCEPT, NodeSemantic.SUBSTITUTION_CONCEPT);

    protected static int isaNid = 0;

    transient int conceptSequence = -1;

    ArrayList<LogicNode> logicNodes = new ArrayList<>();
    int rootNode = 0;

    public LogicalExpressionOchreImpl() {
    }

    public LogicalExpressionOchreImpl(LogicalExpressionOchreImpl another, int[] solution) {
         addNodes(another, solution, another.rootNode);
        logicNodes.trimToSize();
    }

    private LogicNode[] addNodes(LogicalExpressionOchreImpl another, int[] solution, int... oldIds) {

        AbstractLogicNode[] results = new AbstractLogicNode[oldIds.length];
        for (int i = 0; i < oldIds.length; i++) {
            LogicNode oldLogicNode = another.getNode(oldIds[i]);
            switch (oldLogicNode.getNodeSemantic()) {
                case DEFINITION_ROOT:
                    results[i] = Root(Arrays.stream(addNodes(another, solution, oldLogicNode.getChildStream().filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0).mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray())).map((LogicNode t) -> (ConnectorNode) t).toArray(ConnectorNode[]::new));
                    rootNode =  results[i].getNodeIndex();
                    break;
                case NECESSARY_SET:
                    results[i] = NecessarySet((AbstractLogicNode[]) addNodes(another, solution, oldLogicNode.getChildStream().filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0).mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray()));
                    break;
                case SUFFICIENT_SET:
                    results[i] = SufficientSet((AbstractLogicNode[]) addNodes(another, solution, oldLogicNode.getChildStream().filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0).mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray()));
                    break;
                case AND:
                    results[i] = And((AbstractLogicNode[]) addNodes(another, solution, oldLogicNode.getChildStream().filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0).mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray()));
                    break;
                case OR:
                    results[i] = Or((AbstractLogicNode[]) addNodes(another, solution, oldLogicNode.getChildStream().filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0).mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray()));
                    break;
                case DISJOINT_WITH:
                    results[i] = DisjointWith((AbstractLogicNode[]) addNodes(another, solution, oldLogicNode.getChildStream().filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0).mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray()));
                    break;
                case ROLE_ALL:
                    results[i] = AllRole(((TypedNodeWithSequences) oldLogicNode).getTypeConceptSequence(), (AbstractLogicNode) addNodes(another, solution, oldLogicNode.getChildStream().filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0).mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray())[0]);
                    break;
                case ROLE_SOME:
                    results[i] = SomeRole(((TypedNodeWithSequences) oldLogicNode).getTypeConceptSequence(), (AbstractLogicNode) addNodes(another, solution, oldLogicNode.getChildStream().filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0).mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray())[0]);
                    break;
                case FEATURE:
                    results[i] = Feature(((TypedNodeWithSequences) oldLogicNode).getTypeConceptSequence(), (AbstractLogicNode) addNodes(another, solution, oldLogicNode.getChildStream().filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0).mapToInt((oldChildNode) -> oldChildNode.getNodeIndex()).toArray())[0]);
                    break;
                case LITERAL_BOOLEAN:
                    results[i] = BooleanLiteral(((LiteralNodeBoolean) oldLogicNode).getLiteralValue());
                    break;
                case LITERAL_FLOAT:
                    results[i] = FloatLiteral(((LiteralNodeFloat) oldLogicNode).getLiteralValue());
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
                    results[i] = Concept(((ConceptNodeWithSequences) oldLogicNode).getConceptSequence());
                    break;
                case TEMPLATE:
                    results[i] = Template(((TemplateNodeWithSequences) oldLogicNode).getTemplateConceptSequence(),
                            ((TemplateNodeWithSequences) oldLogicNode).getAssemblageConceptSequence());
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
                default:
                    throw new UnsupportedOperationException("Can't handle: " + oldLogicNode.getNodeSemantic());
            }
        }
        return results;
    }

    /**
     *
     * @param nodeDataArray
     * @param dataSource
     * @param conceptId Either a nid or sequence of a concept is acceptable.
     */
    public LogicalExpressionOchreImpl(byte[][] nodeDataArray, DataSource dataSource, int conceptId) {
        this(nodeDataArray, dataSource);
        if (conceptId < 0) {
            conceptId = Get.identifierService().getConceptSequence(conceptId);
        }
        this.conceptSequence = conceptId;
    }

    public LogicalExpressionOchreImpl(byte[][] nodeDataArray, DataSource dataSource) {
        try {
            logicNodes = new ArrayList<>(nodeDataArray.length);
            for (byte[] nodeDataArray1 : nodeDataArray) {
                DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(nodeDataArray1));
                byte nodeSemanticIndex = dataInputStream.readByte();
                NodeSemantic nodeSemantic = NODE_SEMANTICS[nodeSemanticIndex];
                switch (nodeSemantic) {
                    case DEFINITION_ROOT:
                        Root(dataInputStream);
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
                                throw new UnsupportedOperationException("Can't handle: " + dataSource);
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
                                throw new UnsupportedOperationException("Can't handle: " + dataSource);
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
                                throw new UnsupportedOperationException("Can't handle: " + dataSource);
                        }

                        break;
                    case LITERAL_BOOLEAN:
                        BooleanLiteral(dataInputStream);
                        break;
                    case LITERAL_FLOAT:
                        FloatLiteral(dataInputStream);
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
                                throw new UnsupportedOperationException("Can't handle: " + dataSource);
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
                                throw new UnsupportedOperationException("Can't handle: " + dataSource);
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
                    default:
                        throw new UnsupportedOperationException("Can't handle: " + nodeSemantic);
                }
            }
            logicNodes.trimToSize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean contains(NodeSemantic semantic) {
        return logicNodes.stream().anyMatch((node) -> (node.getNodeSemantic() == semantic));
    }

    @Override
    public Stream<LogicNode> getNodesOfType(NodeSemantic semantic) {
        return logicNodes.stream().filter((node) -> (node.getNodeSemantic() == semantic));
    }

    @Override
    public boolean isMeaningful() {
        return logicNodes.stream().anyMatch((node) -> (meaningfulNodeSemantics.contains(node.getNodeSemantic())));
    }

    @Override
    public int getConceptSequence() {
        return conceptSequence;
    }

    @Override
    public int getNodeCount() {
        return logicNodes.size();
    }

    @Override
    public final RootNode getRoot() {
        if (logicNodes.isEmpty()) {
            return Root();
        }
        return (RootNode) logicNodes.get(rootNode);
    }

    @Override
    public LogicNode getNode(int nodeIndex) {
        return logicNodes.get(nodeIndex);
    }

    @Override
    public byte[][] getData(DataTarget dataTarget) {
        init();
        byte[][] byteArrayArray = new byte[logicNodes.size()][];
        for (int index = 0; index < byteArrayArray.length; index++) {
            byteArrayArray[index] = logicNodes.get(index).getBytes(dataTarget);
        }
        return byteArrayArray;
    }

    protected void init() {
        logicNodes.trimToSize();
    }

    public void addNode(LogicNode logicNode) {
        logicNode.setNodeIndex((short) logicNodes.size());
        logicNodes.add(logicNode);
    }

    @Override
    public void processDepthFirst(BiConsumer<LogicNode, TreeNodeVisitData> consumer) {
        processDepthFirst(getRoot(), consumer);
    }

    /**
     * Process the fragment starting at root in a depth first manner.
     *
     * @param fragmentRoot
     * @param consumer
     */
    @Override
    public void processDepthFirst(LogicNode fragmentRoot, BiConsumer<LogicNode, TreeNodeVisitData> consumer) {
        init();
        TreeNodeVisitData graphVisitData = new TreeNodeVisitData(logicNodes.size());
        depthFirstVisit(consumer, fragmentRoot, graphVisitData, 0);
    }

    protected void depthFirstVisit(BiConsumer<LogicNode, TreeNodeVisitData> consumer, LogicNode logicNode,
                                   TreeNodeVisitData graphVisitData, int depth) {

        if (depth > 100) {
            // toString depends on this method, so we can't include this.toString() in the exception...
            throw new RuntimeException("Depth limit exceeded for logicNode: " + logicNode); // + " in graph: " + this);
        }

        graphVisitData.startNodeVisit(logicNode.getNodeIndex(), depth);
        ConceptSequenceSet conceptsAtNodeOrAbove = new ConceptSequenceSet();
        logicNode.addConceptsReferencedByNode(conceptsAtNodeOrAbove);
        graphVisitData.getConceptsReferencedAtNodeOrAbove(logicNode.getNodeIndex());

        logicNode.addConceptsReferencedByNode(ConceptSequenceSet.of(graphVisitData.getConceptsReferencedAtNodeOrAbove(logicNode.getNodeIndex())));
        conceptsAtNodeOrAbove.addAll(graphVisitData.getConceptsReferencedAtNodeOrAbove(graphVisitData.getPredecessorSequence(logicNode.getNodeIndex())));
        graphVisitData.setConceptsReferencedAtNodeOrAbove(logicNode.getNodeIndex(), conceptsAtNodeOrAbove);

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
                    siblingGroupSequence = logicNode.getNodeIndex();
                    break;
                default:
                    siblingGroupSequence =
                            graphVisitData.getSiblingGroupForSequence(logicNode.getNodeIndex());
            }

            for (LogicNode child : logicNode.getChildren()) {
                graphVisitData.setSiblingGroupForSequence(child.getNodeIndex(), siblingGroupSequence);
                graphVisitData.setPredecessorSequence(child.getNodeIndex(), logicNode.getNodeIndex());
                     depthFirstVisit(consumer, child, graphVisitData, depth + 1);
            }
        }
        graphVisitData.endNodeVisit(logicNode.getNodeIndex());
    }

    public final NecessarySetNode NecessarySet(AbstractLogicNode... children) {
        return new NecessarySetNode(this, children);
    }

    public final NecessarySetNode NecessarySet(DataInputStream dataInputStream) throws IOException {
        return new NecessarySetNode(this, dataInputStream);
    }

    public final SufficientSetNode SufficientSet(AbstractLogicNode... children) {
        return new SufficientSetNode(this, children);
    }

    public final SufficientSetNode SufficientSet(DataInputStream dataInputStream) throws IOException {
        return new SufficientSetNode(this, dataInputStream);
    }

    public final AndNode And(AbstractLogicNode... children) {
        return new AndNode(this, children);
    }

    public final AndNode And(DataInputStream dataInputStream) throws IOException {
        return new AndNode(this, dataInputStream);
    }

    public OrNode Or(AbstractLogicNode... children) {
        return new OrNode(this, children);
    }

    public final OrNode Or(DataInputStream dataInputStream) throws IOException {
        return new OrNode(this, dataInputStream);
    }

    public RootNode Root(ConnectorNode... children) {
        return new RootNode(this, children);
    }

    public final RootNode Root(DataInputStream dataInputStream) throws IOException {
        return new RootNode(this, dataInputStream);
    }

    public DisjointWithNode DisjointWith(AbstractLogicNode... children) {
        return new DisjointWithNode(this, children);
    }

    public final DisjointWithNode DisjointWith(DataInputStream dataInputStream) throws IOException {
        return new DisjointWithNode(this, dataInputStream);
    }

    public RoleNodeAllWithSequences AllRole(int typeNid, AbstractLogicNode restriction) {
        return new RoleNodeAllWithSequences(this, typeNid, restriction);
    }

    public final RoleNodeAllWithSequences AllRole(DataInputStream dataInputStream) throws IOException {
        return new RoleNodeAllWithSequences(this, dataInputStream);
    }

    public final RoleNodeAllWithUuids AllRoleWithUuids(DataInputStream dataInputStream) throws IOException {
        return new RoleNodeAllWithUuids(this, dataInputStream);
    }

    public final RoleNodeSomeWithSequences SomeRole(int typeNid, AbstractLogicNode restriction) {
        return new RoleNodeSomeWithSequences(this, typeNid, restriction);
    }

    public final RoleNodeSomeWithSequences SomeRole(DataInputStream dataInputStream) throws IOException {
        return new RoleNodeSomeWithSequences(this, dataInputStream);
    }

    public final RoleNodeSomeWithUuids SomeRoleWithUuids(DataInputStream dataInputStream) throws IOException {
        return new RoleNodeSomeWithUuids(this, dataInputStream);
    }

    public FeatureNodeWithSequences Feature(int typeNid, AbstractLogicNode literal) {
        // check for LiteralNode or SubstitutionNodeLiteral
        if ((literal instanceof LiteralNode) || (literal instanceof SubstitutionNodeLiteral)) {
            return new FeatureNodeWithSequences(this, typeNid, literal);
        }
        throw new IllegalStateException(
                "LogicNode must be of type LiteralNode or SubstitutionNodeLiteral. Found: "
                + literal);
    }

    public final FeatureNodeWithSequences Feature(DataInputStream dataInputStream) throws IOException {
        return new FeatureNodeWithSequences(this, dataInputStream);
    }

    public final FeatureNodeWithUuids FeatureWithUuids(DataInputStream dataInputStream) throws IOException {
        return new FeatureNodeWithUuids(this, dataInputStream);
    }

    public LiteralNodeBoolean BooleanLiteral(boolean literalValue) {
        return new LiteralNodeBoolean(this, literalValue);
    }

    public final LiteralNodeBoolean BooleanLiteral(DataInputStream dataInputStream) throws IOException {
        return new LiteralNodeBoolean(this, dataInputStream);
    }

    public LiteralNodeFloat FloatLiteral(float literalValue) {
        return new LiteralNodeFloat(this, literalValue);
    }

    public final LiteralNodeFloat FloatLiteral(DataInputStream dataInputStream) throws IOException {
        return new LiteralNodeFloat(this, dataInputStream);
    }

    public LiteralNodeInstant InstantLiteral(Instant literalValue) {
        return new LiteralNodeInstant(this, literalValue);
    }

    public final LiteralNodeInstant InstantLiteral(DataInputStream dataInputStream) throws IOException {
        return new LiteralNodeInstant(this, dataInputStream);
    }

    public LiteralNodeInteger IntegerLiteral(int literalValue) {
        return new LiteralNodeInteger(this, literalValue);
    }

    public final LiteralNodeInteger IntegerLiteral(DataInputStream dataInputStream) throws IOException {
        return new LiteralNodeInteger(this, dataInputStream);
    }

    public LiteralNodeString StringLiteral(String literalValue) {
        return new LiteralNodeString(this, literalValue);
    }

    public final LiteralNodeString StringLiteral(DataInputStream dataInputStream) throws IOException {
        return new LiteralNodeString(this, dataInputStream);
    }

    public final ConceptNodeWithSequences Concept(int conceptSequence) {
        return new ConceptNodeWithSequences(this, conceptSequence);
    }

    public final ConceptNodeWithSequences Concept(DataInputStream dataInputStream) throws IOException {
        return new ConceptNodeWithSequences(this, dataInputStream);
    }

    public final ConceptNodeWithUuids ConceptWithUuids(DataInputStream dataInputStream) throws IOException {
        return new ConceptNodeWithUuids(this, dataInputStream);
    }

    public TemplateNodeWithSequences Template(int templateConceptId, int assemblageConceptId) {
        return new TemplateNodeWithSequences(this, templateConceptId, assemblageConceptId);
    }

    public final TemplateNodeWithSequences Template(DataInputStream dataInputStream) throws IOException {
        return new TemplateNodeWithSequences(this, dataInputStream);
    }

    public final TemplateNodeWithUuids TemplateWithUuids(DataInputStream dataInputStream) throws IOException {
        return new TemplateNodeWithUuids(this, dataInputStream);
    }

    public SubstitutionNodeBoolean BooleanSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        return new SubstitutionNodeBoolean(this, substitutionFieldSpecification);
    }

    public final SubstitutionNodeBoolean BooleanSubstitution(DataInputStream dataInputStream) throws IOException {
        return new SubstitutionNodeBoolean(this, dataInputStream);
    }

    public SubstitutionNodeConcept ConceptSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        return new SubstitutionNodeConcept(this, substitutionFieldSpecification);
    }

    public final SubstitutionNodeConcept ConceptSubstitution(DataInputStream dataInputStream) throws IOException {
        return new SubstitutionNodeConcept(this, dataInputStream);
    }

    public SubstitutionNodeFloat FloatSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        return new SubstitutionNodeFloat(this, substitutionFieldSpecification);
    }

    public final SubstitutionNodeFloat FloatSubstitution(DataInputStream dataInputStream) throws IOException {
        return new SubstitutionNodeFloat(this, dataInputStream);
    }

    public SubstitutionNodeInstant InstantSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        return new SubstitutionNodeInstant(this, substitutionFieldSpecification);
    }

    public final SubstitutionNodeInstant InstantSubstitution(DataInputStream dataInputStream) throws IOException {
        return new SubstitutionNodeInstant(this, dataInputStream);
    }

    public SubstitutionNodeInteger IntegerSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        return new SubstitutionNodeInteger(this, substitutionFieldSpecification);
    }

    public final SubstitutionNodeInteger IntegerSubstitution(DataInputStream dataInputStream) throws IOException {
        return new SubstitutionNodeInteger(this, dataInputStream);
    }

    public SubstitutionNodeString StringSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
        return new SubstitutionNodeString(this, substitutionFieldSpecification);
    }

    public final SubstitutionNodeString StringSubstitution(DataInputStream dataInputStream) throws IOException {
        return new SubstitutionNodeString(this, dataInputStream);
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String nodeIdSuffix) {
        StringBuilder builder = new StringBuilder();
        processDepthFirst((LogicNode logicNode, TreeNodeVisitData graphVisitData) -> {
            for (int i = 0; i < graphVisitData.getDistance(logicNode.getNodeIndex()); i++) {
                builder.append("    ");
            }
            builder.append(logicNode.toString(nodeIdSuffix));
            builder.append("\n");
        });
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LogicalExpressionOchreImpl other = (LogicalExpressionOchreImpl) obj;
        if (this.logicNodes == other.logicNodes) {
            return true;
        }

        if (this.logicNodes != null) {
            if (this.logicNodes.size() != other.logicNodes.size()) {
                return false;
            }
            TreeNodeVisitData graphVisitData = new TreeNodeVisitData(logicNodes.size());
            depthFirstVisit(null, getRoot(), graphVisitData, 0);

            return graphsEqual(this.getRoot(), other.getRoot(), 0, graphVisitData.getMaxDepth());
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.conceptSequence;
        return hash;
    }

    private boolean graphsEqual(AbstractLogicNode g1, AbstractLogicNode g2, int depth, int maxDepth) {
        if (g1.equals(g2)) {
            AbstractLogicNode[] g1children = g1.getChildren();
            AbstractLogicNode[] g2children = g2.getChildren();
            if (g1children.length != g2children.length) {
                return false;
            }
            if (g1children.length == 0) {
                return true;
            }

            HashMap<Set<UUID>, IntArrayList> uuidSetNodeListMap = new HashMap<>();
            int depthToTest = 0;
            while (uuidSetNodeListMap.size() < g1children.length && depthToTest < maxDepth - depth) {
                depthToTest++;
                uuidSetNodeListMap.clear();
                for (AbstractLogicNode child : g1children) {
                    Set<UUID> nodeUuidSetForDepth = child.getNodeUuidSetForDepth(depthToTest);
                    if (!uuidSetNodeListMap.containsKey(nodeUuidSetForDepth)) {
                        IntArrayList nodeList = new IntArrayList();
                        nodeList.add(child.getNodeIndex());
                        uuidSetNodeListMap.put(nodeUuidSetForDepth, nodeList);
                    } else {
                        uuidSetNodeListMap.get(nodeUuidSetForDepth).add(child.getNodeIndex());
                    }

                }
            }
            // need to try all combinations
            for (AbstractLogicNode g2Child : g2children) {
                Set<UUID> nodeUuidSetForDepth = g2Child.getNodeUuidSetForDepth(depthToTest);
                IntArrayList possibleMatches = uuidSetNodeListMap.get(nodeUuidSetForDepth);
                if (possibleMatches == null) {
                    return false;
                }
                int match = -1;
                for (int possibleMatchIndex : possibleMatches.elements()) {
                    if (graphsEqual((AbstractLogicNode) this.logicNodes.get(possibleMatchIndex), g2Child, depth + 1, maxDepth)) {
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

    public void sort() {
        logicNodes.forEach((node) -> node.sort());
    }

    @Override
    public IsomorphicResults findIsomorphisms(LogicalExpression another) {
        return new IsomorphicResultsBottomUp(this, another);
    }

}
