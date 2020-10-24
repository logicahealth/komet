package sh.isaac.model.logic.node;

import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.model.logic.LogicalExpressionImpl;

public abstract class SingleChildConnectorNode extends ConnectorNode {
    public SingleChildConnectorNode(AbstractLogicNode another) {
        super(another);
    }

    public SingleChildConnectorNode(LogicalExpressionImpl logicGraphVersion, AbstractLogicNode... children) {
        super(logicGraphVersion, children);
        if (children.length > 1) {
            throw new IllegalStateException(this.getClass().getName() + " can only have 1 child. Trying to add: " + children);
        }
    }

    public SingleChildConnectorNode(LogicalExpressionImpl logicGraphVersion, ByteArrayDataBuffer dataInputStream) {
        super(logicGraphVersion, dataInputStream);
        if (childIndices.size() > 1) {
            throw new IllegalStateException(this.getClass().getName() + " can only have 1 child. Trying to add: " + childIndices);
        }
    }

    @Override
    public void addChildren(LogicNode... children) {
        if (children.length > 1 || this.childIndices.size() + children.length > 1) {
            throw new IllegalStateException(this.getClass().getName() + " can only have 1 child. Trying to add: " + children);
        }
        for (final LogicNode child: children) {
            ((AbstractLogicNode) child).setParentIndex(this.getNodeIndex());
            this.childIndices.add(child.getNodeIndex());
        }

        sort();
    }
}
