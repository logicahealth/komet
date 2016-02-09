package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 12/10/14.
 */
public abstract class SubstitutionNode extends AbstractLogicNode {

    SubstitutionFieldSpecification substitutionFieldSpecification;

    public SubstitutionNode(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        int length = dataInputStream.readInt();
        byte[] bytes = new byte[length];
        dataInputStream.read(bytes, 0, length);
        throw new UnsupportedOperationException(
                "deserializer for substitution field specification not implemented");
    }

    public SubstitutionNode(LogicalExpressionOchreImpl logicGraphVersion, 
            SubstitutionFieldSpecification substitutionFieldSpecification) {
        super(logicGraphVersion);
        this.substitutionFieldSpecification = substitutionFieldSpecification;
    }

    public SubstitutionFieldSpecification getSubstitutionFieldSpecification() {
        return substitutionFieldSpecification;
    }

    @Override
    protected final void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
        byte[] bytes = substitutionFieldSpecification.getBytes();
        dataOutput.writeInt(bytes.length);
        dataOutput.write(bytes);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SubstitutionNode that = (SubstitutionNode) o;

        return substitutionFieldSpecification.equals(that.substitutionFieldSpecification);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + substitutionFieldSpecification.hashCode();
        return result;
    }
    @Override
    protected int compareFields(LogicNode o) {
        SubstitutionNode that = (SubstitutionNode) o;
        return this.substitutionFieldSpecification.compareTo(that.substitutionFieldSpecification);
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return " Substitution: '" + substitutionFieldSpecification + '\''  + super.toString(nodeIdSuffix);
    }
}