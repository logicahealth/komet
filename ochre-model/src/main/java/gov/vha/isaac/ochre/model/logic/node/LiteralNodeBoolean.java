package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 * Created by kec on 12/10/14.
 */
public class LiteralNodeBoolean extends LiteralNode {

    boolean literalValue;

    public LiteralNodeBoolean(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        literalValue = dataInputStream.readBoolean();
    }

    public LiteralNodeBoolean(LogicalExpressionOchreImpl logicGraphVersion, boolean literalValue) {
        super(logicGraphVersion);
        this.literalValue = literalValue;
    }


    public boolean getLiteralValue() {
        return literalValue;
    }
    @Override
    protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
        dataOutput.writeBoolean(literalValue);
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.LITERAL_BOOLEAN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LiteralNodeBoolean that = (LiteralNodeBoolean) o;

        return literalValue == that.literalValue;
    }

        
    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                Boolean.toString(literalValue));
     }

    
    @Override
    protected int compareFields(LogicNode o) {
        LiteralNodeBoolean that = (LiteralNodeBoolean) o;
        return Boolean.compare(this.literalValue, that.literalValue);
    }


    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (literalValue ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return "Boolean literal[" + getNodeIndex() + nodeIdSuffix + "]" + literalValue + super.toString(nodeIdSuffix);
    }
}