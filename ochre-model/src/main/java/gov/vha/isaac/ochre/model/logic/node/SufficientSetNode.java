package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by kec on 12/10/14.
 */
public class SufficientSetNode extends ConnectorNode {

    public SufficientSetNode(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }
    public SufficientSetNode(LogicalExpressionOchreImpl logicGraphVersion, AbstractNode... children) {
        super(logicGraphVersion, children);
    }

    @Override
    protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        writeData(dataOutput, dataTarget);
    }

    @Override
    protected UUID initNodeUuid() {
        return getNodeSemantic().getSemanticUuid();
    }
    
    @Override
    public String toString() {
        return "SufficientSetNode[" + getNodeIndex() + "]:" + super.toString();
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.SUFFICIENT_SET;
    }

}