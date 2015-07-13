package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.util.UuidT5Generator;

/**
 * Created by kec on 12/9/14.
 */
public class LiteralNodeInteger extends LiteralNode {

    int literalValue;

    public LiteralNodeInteger(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        literalValue = dataInputStream.readInt();
    }

    public LiteralNodeInteger(LogicalExpressionOchreImpl logicGraphVersion, int literalValue) {
        super(logicGraphVersion);
        this.literalValue = literalValue;
    }

    @Override
    public String toString() {
        return "Integer literal[" + getNodeIndex() + "]" + literalValue + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        LiteralNodeInteger that = (LiteralNodeInteger) o;

        return literalValue == that.literalValue;
    }

    @Override
    protected int compareFields(Node o) {
        LiteralNodeInteger that = (LiteralNodeInteger) o;
        return that.literalValue - this.literalValue;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + literalValue;
        return result;
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                Integer.toString(literalValue));
    }

    @Override
    protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
        dataOutput.writeInt(literalValue);
    }

    public int getLiteralValue() {
        return literalValue;
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.LITERAL_INTEGER;
    }
}
