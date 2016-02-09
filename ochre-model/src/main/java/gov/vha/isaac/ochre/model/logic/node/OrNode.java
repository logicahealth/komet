package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by kec on 12/10/14.
 */
public class OrNode extends ConnectorNode {

    public OrNode(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    public OrNode(LogicalExpressionOchreImpl logicGraphVersion, AbstractLogicNode... children) {
        super(logicGraphVersion, children);
    }

    @Override
    protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        writeData(dataOutput, dataTarget);
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.OR;
    }

    @Override
    protected UUID initNodeUuid() {
        return getNodeSemantic().getSemanticUuid();
    }

    @Override
    public String toString() {
        return toString("");
    }
    
    @Override
    public String toString(String nodeIdSuffix) {
        return "Or[" + getNodeIndex() + nodeIdSuffix + "]" + super.toString(nodeIdSuffix);
    }
    
    @Override
    protected int compareNodeFields(LogicNode o) {
        // no fields to compare, node semantic already determined equals. 
        return 0;
    }

}