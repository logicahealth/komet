package gov.vha.isaac.ochre.model.logic.node.internal;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.model.logic.node.AbstractLogicNode;
import gov.vha.isaac.ochre.model.logic.node.external.ConceptNodeWithUuids;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 * Created by kec on 12/10/14.
 */
public final class ConceptNodeWithSequences extends AbstractLogicNode {

    int conceptSequence;

    public ConceptNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        conceptSequence = dataInputStream.readInt();
    }

    public ConceptNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, int conceptId) {
        super(logicGraphVersion);
        this.conceptSequence = Get.identifierService().getConceptSequence(conceptId);

    }

    public ConceptNodeWithSequences(ConceptNodeWithUuids externalForm) {
        super(externalForm);
        this.conceptSequence = Get.identifierService().getConceptSequenceForUuids(externalForm.getConceptUuid());

    }

    @Override
    public void addConceptsReferencedByNode(ConceptSequenceSet conceptSequenceSet) {
        super.addConceptsReferencedByNode(conceptSequenceSet); 
        conceptSequenceSet.add(conceptSequence);
    }

    public int getConceptSequence() {
        return conceptSequence;
    }

    @Override
    public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        switch (dataTarget) {
            case EXTERNAL:
                ConceptNodeWithUuids externalForm = new ConceptNodeWithUuids(this);
                externalForm.writeNodeData(dataOutput, dataTarget);
                break;
            case INTERNAL:
                super.writeData(dataOutput, dataTarget);
                dataOutput.writeInt(conceptSequence);
                break;
            default:
                throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
        }
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.CONCEPT;
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                Get.identifierService().getUuidPrimordialForNid(conceptSequence).toString());
    }

    @Override
    public AbstractLogicNode[] getChildren() {
        return new AbstractLogicNode[0];
    }

    @Override
    public final void addChildren(LogicNode... children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return "Concept[" + getNodeIndex() + nodeIdSuffix + "] " + Get.conceptDescriptionText(conceptSequence) + " <"
                + Get.identifierService().getConceptSequence(conceptSequence)
                + ">" + super.toString(nodeIdSuffix);
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

        ConceptNodeWithSequences that = (ConceptNodeWithSequences) o;

        return conceptSequence == that.conceptSequence;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + conceptSequence;
        return result;
    }

    @Override
    protected int compareFields(LogicNode o) {
        return Integer.compare(conceptSequence, ((ConceptNodeWithSequences) o).getConceptSequence());
    }

}
