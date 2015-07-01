package gov.vha.isaac.ochre.model.logic.node.internal;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.logic.node.AbstractNode;
import gov.vha.isaac.ochre.model.logic.node.external.ConceptNodeWithUuids;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import java.util.Optional;

/**
 * Created by kec on 12/10/14.
 */
public final class ConceptNodeWithNids extends AbstractNode {

    int conceptNid;

    public ConceptNodeWithNids(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        conceptNid = dataInputStream.readInt();
    }

    public ConceptNodeWithNids(LogicalExpressionOchreImpl logicGraphVersion, int conceptNid) {
        super(logicGraphVersion);
        this.conceptNid = conceptNid;

    }

    public ConceptNodeWithNids(ConceptNodeWithUuids externalForm) {
        super(externalForm);
        this.conceptNid = Get.identifierService().getNidForUuids(externalForm.getConceptUuid());

    }

    public int getConceptNid() {
        return conceptNid;
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
                dataOutput.writeInt(conceptNid);
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
                Get.identifierService().getUuidPrimordialForNid(conceptNid).toString());

    }

    @Override
    public AbstractNode[] getChildren() {
        return new AbstractNode[0];
    }

    @Override
    public final void addChildren(Node... children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        Optional<? extends ConceptChronology<? extends ConceptVersion>> concept = 
                Get.conceptService().getOptionalConcept(conceptNid);
        if (concept.isPresent()) {
        return "ConceptNode[" + getNodeIndex() + "]: " + concept.get().toUserString() + "<"
                + Get.identifierService().getConceptSequence(conceptNid)
                + ">" + super.toString();
        }
        return "ConceptNode[" + getNodeIndex() + "]: " + conceptNid + "<not found>" + super.toString();
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

        ConceptNodeWithNids that = (ConceptNodeWithNids) o;

        return conceptNid == that.conceptNid;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + conceptNid;
        return result;
    }

    @Override
    protected int compareFields(Node o) {
        return conceptNid - ((ConceptNodeWithNids) o).getConceptNid();
    }

}
