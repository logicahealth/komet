package gov.vha.isaac.ochre.model.logic.node.internal;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.ConcreteDomainOperators;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.logic.node.AbstractNode;
import gov.vha.isaac.ochre.model.logic.node.external.FeatureNodeWithUuids;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.util.UuidT5Generator;

/**
 * Created by kec on 12/9/14.
 */
public final class FeatureNodeWithNids extends TypedNodeWithNids {

    static ConcreteDomainOperators[] concreteDomainOperators = ConcreteDomainOperators.values();

    ConcreteDomainOperators operator;

    int unitsConceptNid;

    public FeatureNodeWithNids(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        operator = concreteDomainOperators[dataInputStream.readByte()];
        unitsConceptNid = dataInputStream.readInt();
    }

    public FeatureNodeWithNids(LogicalExpressionOchreImpl logicGraphVersion, int typeConceptNid, AbstractNode child) {
        super(logicGraphVersion, typeConceptNid, child);
    }

    public FeatureNodeWithNids(FeatureNodeWithUuids externalForm) {
        super(externalForm);
        operator = externalForm.getOperator();
        unitsConceptNid = getIdentifierService().getNidForUuids(externalForm.getUnitsConceptUuid());
    }

    @Override
    public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        switch (dataTarget) {
            case EXTERNAL:
                FeatureNodeWithUuids externalForm = new FeatureNodeWithUuids(this);
                externalForm.writeNodeData(dataOutput, dataTarget);
                break;
            case INTERNAL:
                super.writeData(dataOutput, dataTarget);
                dataOutput.writeByte(operator.ordinal());
                dataOutput.writeInt(unitsConceptNid);
                break;
            default:
                throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
        }
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.FEATURE;
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                getIdentifierService().getUuidPrimordialForNid(typeConceptNid).toString()
                        + operator
                        + getIdentifierService().getUuidPrimordialForNid(unitsConceptNid).toString());

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

        FeatureNodeWithNids that = (FeatureNodeWithNids) o;

        if (unitsConceptNid != that.unitsConceptNid) {
            return false;
        }
        return operator == that.operator;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + operator.hashCode();
        result = 31 * result + unitsConceptNid;
        return result;
    }

    @Override
    public String toString() {
        return "FeatureNode[" + getNodeIndex() + "]: "
                + operator
                + ", units:" + getConceptService().getConcept(unitsConceptNid).toUserString()
                + super.toString();
    }

    public ConcreteDomainOperators getOperator() {
        return operator;
    }

    public int getUnitsConceptNid() {
        return unitsConceptNid;
    }

}
