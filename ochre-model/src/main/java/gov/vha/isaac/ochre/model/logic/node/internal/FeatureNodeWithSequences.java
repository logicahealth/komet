package gov.vha.isaac.ochre.model.logic.node.internal;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.model.logic.ConcreteDomainOperators;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
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
public final class FeatureNodeWithSequences extends TypedNodeWithSequences {

    static ConcreteDomainOperators[] concreteDomainOperators = ConcreteDomainOperators.values();

    ConcreteDomainOperators operator;

    int unitsConceptSequence;

    public FeatureNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        operator = concreteDomainOperators[dataInputStream.readByte()];
        unitsConceptSequence = dataInputStream.readInt();
    }

    public FeatureNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, int typeConceptId, AbstractNode child) {
        super(logicGraphVersion, typeConceptId, child);
    }

    public FeatureNodeWithSequences(FeatureNodeWithUuids externalForm) {
        super(externalForm);
        operator = externalForm.getOperator();
        unitsConceptSequence = Get.identifierService().getConceptSequenceForUuids(externalForm.getUnitsConceptUuid());
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
                dataOutput.writeInt(unitsConceptSequence);
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
                Get.identifierService().getUuidPrimordialForNid(typeConceptSequence).toString()
                        + operator
                        + Get.identifierService().getUuidPrimordialForNid(unitsConceptSequence).toString());

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

        FeatureNodeWithSequences that = (FeatureNodeWithSequences) o;

        if (unitsConceptSequence != that.unitsConceptSequence) {
            return false;
        }
        return operator == that.operator;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + operator.hashCode();
        result = 31 * result + unitsConceptSequence;
        return result;
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return "Feature[" + getNodeIndex() + nodeIdSuffix +"] "
                + operator
                + ", units:" + Get.conceptDescriptionText(unitsConceptSequence)
                + super.toString(nodeIdSuffix);
    }

    public ConcreteDomainOperators getOperator() {
        return operator;
    }

    public int getUnitsConceptSequence() {
        return unitsConceptSequence;
    }
    @Override

    protected int compareTypedNodeFields(Node o) {
        // node semantic already determined equals. 
        FeatureNodeWithSequences other = (FeatureNodeWithSequences) o;
        if (unitsConceptSequence != other.unitsConceptSequence) {
            return Integer.compare(unitsConceptSequence, other.unitsConceptSequence);
        }
        if (operator != other.operator) {
            return operator.compareTo(other.operator);
        }
        return Integer.compare(unitsConceptSequence, other.unitsConceptSequence);
    }

    @Override
    public void addConceptsReferencedByNode(ConceptSequenceSet conceptSequenceSet) {
        super.addConceptsReferencedByNode(conceptSequenceSet);
        conceptSequenceSet.add(unitsConceptSequence);
    }

}
