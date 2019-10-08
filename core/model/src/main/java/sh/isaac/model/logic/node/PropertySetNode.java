package sh.isaac.model.logic.node;

import sh.isaac.api.DataTarget;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.model.logic.LogicalExpressionImpl;

import java.util.UUID;

public class PropertySetNode extends SingleChildConnectorNode {
    /**
     * Instantiates a new necessary set node.
     *
     * @param logicGraphVersion the logic graph version
     * @param child the {@link AndNode} or {@link OrNode} node
     */
    public PropertySetNode(LogicalExpressionImpl logicGraphVersion, ConnectorNode child) {
        super(logicGraphVersion, child);
        if (!(child instanceof AndNode || child instanceof OrNode)) {
            throw new RuntimeException("Illegal construction");
        }
    }

    /**
     * Instantiates a new necessary set node.
     *
     * @param logicGraphVersion the logic graph version
     * @param dataInputStream the data input stream
     */
    public PropertySetNode(LogicalExpressionImpl logicGraphVersion,
                            ByteArrayDataBuffer dataInputStream) {
        super(logicGraphVersion, dataInputStream);
    }

    //~--- methods -------------------------------------------------------------

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return toString("");
    }

    /**
     * To string.
     *
     * @param nodeIdSuffix the node id suffix
     * @return the string
     */
    @Override
    public String toString(String nodeIdSuffix) {
        return "PropertySet[" + getNodeIndex() + nodeIdSuffix + "]" + super.toString(nodeIdSuffix);
    }
    @Override
    public String toSimpleString() {
        return "Property set " +  super.toSimpleString();
    }

    @Override
    public void addToBuilder(StringBuilder builder) {
        for (AbstractLogicNode child: getChildren()) {
            builder.append("\n       PropertySet(");
            child.addToBuilder(builder);
            builder.append("\n          )\n");
        }
    }

    /**
     * Compare node fields.
     *
     * @param o the o
     * @return the int
     */
    @Override
    protected int compareNodeFields(LogicNode o) {
        // no fields to compare, node semantic already determined equals.
        return 0;
    }

    /**
     * Inits the node uuid.
     *
     * @return the uuid
     */
    @Override
    protected UUID initNodeUuid() {
        return getNodeSemantic().getSemanticUuid();
    }

    /**
     * Write node data.
     *
     * @param dataOutput the data output
     * @param dataTarget the data target
     */
    @Override
    protected void writeNodeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget) {
        writeData(dataOutput, dataTarget);
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * Gets the node semantic.
     *
     * @return the node semantic
     */
    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.PROPERTY_SET;
    }
}

