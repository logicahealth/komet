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
public class NecessarySetNode extends ConnectorNode {

    public NecessarySetNode(LogicalExpressionOchreImpl logicGraphVersion, AbstractNode... children) {
        super(logicGraphVersion, children);
    }

    public NecessarySetNode(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    @Override
    protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        writeData(dataOutput, dataTarget);
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.NECESSARY_SET;
    }

    @Override
    protected UUID initNodeUuid() {
        return getNodeSemantic().getSemanticUuid();
    }

    @Override
    public String toString() {
        return "NecessarySetNode[" + getNodeIndex() + "]:" + super.toString();
    }
}