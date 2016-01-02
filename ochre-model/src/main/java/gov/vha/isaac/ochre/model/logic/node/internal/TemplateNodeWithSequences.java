package gov.vha.isaac.ochre.model.logic.node.internal;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.model.logic.node.AbstractNode;
import gov.vha.isaac.ochre.model.logic.node.external.TemplateNodeWithUuids;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 * A node that specifies a template to be substituted in place of this node, and
 * the assemblage concept that will be used to fill template substitution
 * values. Created by kec on 12/10/14.
 */
public final class TemplateNodeWithSequences extends AbstractNode {

    /**
     * Sequence of the concept that defines the template
     */
    int templateConceptSequence;

    /**
     * Sequence of the assemblage concept that provides the substitution values
     * for the template.
     */
    int assemblageConceptSequence;

    public TemplateNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        templateConceptSequence = dataInputStream.readInt();
        assemblageConceptSequence = dataInputStream.readInt();
    }

    public TemplateNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, int templateConceptNid, int assemblageConceptNid) {
        super(logicGraphVersion);
        this.templateConceptSequence = Get.identifierService().getConceptSequence(templateConceptNid);
        this.assemblageConceptSequence = Get.identifierService().getConceptSequence(assemblageConceptNid);
    }

    public TemplateNodeWithSequences(TemplateNodeWithUuids externalForm) {
        super(externalForm);
        this.templateConceptSequence = Get.identifierService().getConceptSequenceForUuids(externalForm.getTemplateConceptUuid());
        this.assemblageConceptSequence = Get.identifierService().getConceptSequenceForUuids(externalForm.getAssemblageConceptUuid());
    }

    @Override
    public void addConceptsReferencedByNode(ConceptSequenceSet conceptSequenceSet) {
        super.addConceptsReferencedByNode(conceptSequenceSet); 
        conceptSequenceSet.add(templateConceptSequence);
        conceptSequenceSet.add(assemblageConceptSequence);
    }

    @Override
    public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        switch (dataTarget) {
            case EXTERNAL:
                TemplateNodeWithUuids externalForm = new TemplateNodeWithUuids(this);
                externalForm.writeNodeData(dataOutput, dataTarget);
                break;
            case INTERNAL:
                super.writeData(dataOutput, dataTarget);
                dataOutput.writeInt(templateConceptSequence);
                dataOutput.writeInt(assemblageConceptSequence);
                break;
            default:
                throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
        }
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.TEMPLATE;
    }

    @Override
    public final AbstractNode[] getChildren() {
        return new AbstractNode[0];
    }

    @Override
    public final void addChildren(Node... children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return "Template[" + getNodeIndex() + nodeIdSuffix + "] "
                + "assemblage: " + Get.conceptDescriptionText(assemblageConceptSequence)
                + ", template: " + Get.conceptDescriptionText(templateConceptSequence)
                + super.toString(nodeIdSuffix);
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

        TemplateNodeWithSequences that = (TemplateNodeWithSequences) o;

        if (assemblageConceptSequence != that.assemblageConceptSequence) {
            return false;
        }
        return templateConceptSequence == that.templateConceptSequence;
    }

    @Override
    protected int compareFields(Node o) {
        TemplateNodeWithSequences that = (TemplateNodeWithSequences) o;
        if (assemblageConceptSequence != that.assemblageConceptSequence) {
            return Integer.compare(this.assemblageConceptSequence, that.assemblageConceptSequence);
        }

        return this.templateConceptSequence - that.templateConceptSequence;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + templateConceptSequence;
        result = 31 * result + assemblageConceptSequence;
        return result;
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                Get.identifierService().getUuidPrimordialForNid(assemblageConceptSequence).toString()
                        + Get.identifierService().getUuidPrimordialForNid(templateConceptSequence).toString());

    }

    public int getTemplateConceptNid() {
        return templateConceptSequence;
    }

    public int getAssemblageConceptNid() {
        return assemblageConceptSequence;
    }

}
