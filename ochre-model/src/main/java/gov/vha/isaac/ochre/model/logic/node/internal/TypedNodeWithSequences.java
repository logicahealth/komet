package gov.vha.isaac.ochre.model.logic.node.internal;


import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.model.logic.node.AbstractNode;
import gov.vha.isaac.ochre.model.logic.node.ConnectorNode;
import gov.vha.isaac.ochre.model.logic.node.external.TypedNodeWithUuids;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by kec on 12/9/14.
 */
public abstract class TypedNodeWithSequences extends ConnectorNode {

    int typeConceptSequence;

    public TypedNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        this.typeConceptSequence = dataInputStream.readInt();
    }

    public TypedNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, int typeConceptId, AbstractNode child) {
        super(logicGraphVersion, child);
        this.typeConceptSequence = Get.identifierService().getConceptSequence(typeConceptId);
    }

    public TypedNodeWithSequences(TypedNodeWithUuids externalForm) {
        super(externalForm);
        this.typeConceptSequence = Get.identifierService().getConceptSequenceForUuids(externalForm.getTypeConceptUuid());
    }

    public int getTypeConceptSequence() {
        return typeConceptSequence;
    }

    @Override
    public void addConceptsReferencedByNode(ConceptSequenceSet conceptSequenceSet) {
        super.addConceptsReferencedByNode(conceptSequenceSet); 
        conceptSequenceSet.add(typeConceptSequence);
    }

    @Override
    public String toString() {
        return toString("");
        
    }
   @Override
    public String toString(String nodeIdSuffix) {
        return " " + Get.conceptDescriptionText(typeConceptSequence) +" <"
                + Get.identifierService().getConceptSequence(typeConceptSequence)
                + ">"+ super.toString(nodeIdSuffix);
        
    }

        @Override
    protected void writeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
        dataOutput.writeInt(typeConceptSequence);
    }
        
    public Node getOnlyChild() {
        Node[] children = getChildren();
        if (children.length == 1) {
            return children[0];
        }
        throw new IllegalStateException("Typed nodes can have only one child. Found: " + Arrays.toString(children));
    }
    
    @Override
    protected final int compareNodeFields(Node o) {
        // node semantic already determined equals. 
        TypedNodeWithSequences other = (TypedNodeWithSequences) o;
        if (typeConceptSequence != other.typeConceptSequence) {
            return Integer.compare(typeConceptSequence, other.typeConceptSequence);
        }
        return compareTypedNodeFields(o);
    }
    protected abstract int compareTypedNodeFields(Node o);

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.typeConceptSequence;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypedNodeWithSequences other = (TypedNodeWithSequences) obj;
        if (this.typeConceptSequence != other.typeConceptSequence) {
            return false;
        }
        return super.equals(obj);
    }

}
