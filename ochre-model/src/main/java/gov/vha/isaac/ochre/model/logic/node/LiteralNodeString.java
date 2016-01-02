package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 * Created by kec on 12/10/14.
 */
public class LiteralNodeString extends LiteralNode {

    String literalValue;

    public LiteralNodeString(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        literalValue = dataInputStream.readUTF();
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

        LiteralNodeString that = (LiteralNodeString) o;

        return literalValue.equals(that.literalValue);
    }

    @Override
    protected int compareFields(Node o) {
        LiteralNodeString that = (LiteralNodeString) o;
        return this.literalValue.compareTo(that.literalValue);
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                literalValue);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + literalValue.hashCode();
        return result;
    }

    public LiteralNodeString(LogicalExpressionOchreImpl logicGraphVersion, String literalValue) {
        super(logicGraphVersion);
        this.literalValue = literalValue;
    }

    @Override
    protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
        dataOutput.writeUTF(literalValue);
    }

    @Override
    public String toString() {
        return "String literal[" + getNodeIndex() + "]" + literalValue + super.toString();
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return "String literal[" + getNodeIndex() + nodeIdSuffix + "]" + literalValue + super.toString(nodeIdSuffix);
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.LITERAL_STRING;
    }

    public String getLiteralValue() {
        return literalValue;
    }

}
