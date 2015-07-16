package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import gov.vha.isaac.ochre.util.UuidT5Generator;

/**
 * Created by kec on 12/9/14.
 */
public class LiteralNodeInstant extends LiteralNode {

    Instant literalValue;

    public LiteralNodeInstant(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        literalValue = Instant.ofEpochSecond(dataInputStream.readLong());
    }

    public LiteralNodeInstant(LogicalExpressionOchreImpl logicGraphVersion, Instant literalValue) {
        super(logicGraphVersion);
        this.literalValue = literalValue;
    }

    @Override
    protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
        dataOutput.writeLong(literalValue.getEpochSecond());
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return "Instant literal[" + getNodeIndex() + nodeIdSuffix + "]" + literalValue + super.toString(nodeIdSuffix);
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

        LiteralNodeInstant that = (LiteralNodeInstant) o;

        return literalValue.equals(that.literalValue);
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                literalValue.toString());
    }

    @Override
    protected int compareFields(Node o) {
        LiteralNodeInstant that = (LiteralNodeInstant) o;
        return this.literalValue.compareTo(that.literalValue);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + literalValue.hashCode();
        return result;
    }

    public Instant getLiteralValue() {
        return literalValue;
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.LITERAL_INSTANT;
    }
}
