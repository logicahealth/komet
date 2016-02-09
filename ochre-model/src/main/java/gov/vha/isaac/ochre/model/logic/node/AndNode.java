package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import java.io.*;
import java.util.UUID;
/**
 * Created by kec on 12/10/14.
 */
public class AndNode extends ConnectorNode {

    public AndNode(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }
    public AndNode(LogicalExpressionOchreImpl logicGraphVersion, AbstractLogicNode... children) {
        super(logicGraphVersion, children);
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.AND;
    }

    @Override
    protected UUID initNodeUuid() {
        return getNodeSemantic().getSemanticUuid();
    }

    @Override
    protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        writeData(dataOutput, dataTarget);
    }

    @Override
    public String toString() {
        return toString("");
    }    

    @Override
    public String toString(String nodeIdSuffix) {
        return "And[" + getNodeIndex() + nodeIdSuffix + "]" + super.toString(nodeIdSuffix);
    }    

    @Override
    protected int compareNodeFields(LogicNode o) {
        // no fields to compare, node semantic already determined equals. 
        return 0;
    }
    
}