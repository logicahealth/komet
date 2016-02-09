package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.LogicNode;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 12/12/14.
 */
public abstract class LiteralNode extends AbstractLogicNode {

    public LiteralNode(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    public LiteralNode(LogicalExpressionOchreImpl logicGraphVersion) {
        super(logicGraphVersion);
    }

    @Override
    protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
    }

    @Override
    public final AbstractLogicNode[] getChildren() {
        return new AbstractLogicNode[0];
    }

    @Override
    public final void addChildren(LogicNode... children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return toString(""); 
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return super.toString(nodeIdSuffix);
    }
}
