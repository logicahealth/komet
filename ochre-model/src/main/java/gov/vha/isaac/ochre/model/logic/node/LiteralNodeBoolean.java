package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.LogicExpressionOchreImpl;
import gov.vha.isaac.ochre.model.logic.Node;
import gov.vha.isaac.ochre.model.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import java.io.UnsupportedEncodingException;

/**
 * Created by kec on 12/10/14.
 */
public class LiteralNodeBoolean extends LiteralNode {

    boolean literalValue;

    public LiteralNodeBoolean(LogicExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        literalValue = dataInputStream.readBoolean();
    }

    public LiteralNodeBoolean(LogicExpressionOchreImpl logicGraphVersion, boolean literalValue) {
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
        try {
            return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                    Boolean.toString(literalValue));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
     }

    
    @Override
    protected int compareFields(Node o) {
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
        return "LiteralNodeBoolean[" + getNodeIndex() + "]:" + literalValue + super.toString();
    }
}