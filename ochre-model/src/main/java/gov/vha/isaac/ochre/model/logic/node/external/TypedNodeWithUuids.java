/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.logic.node.external;


import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.model.logic.node.AbstractLogicNode;
import gov.vha.isaac.ochre.model.logic.node.ConnectorNode;
import gov.vha.isaac.ochre.model.logic.node.internal.FeatureNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeAllWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeSomeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.TypedNodeWithSequences;

/**
 *
 * @author kec
 */
public abstract class TypedNodeWithUuids extends ConnectorNode {

    UUID typeConceptUuid;

    public TypedNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        this.typeConceptUuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
    }

    public TypedNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion, UUID typeConceptUuid, AbstractLogicNode child) {
        super(logicGraphVersion, child);
        this.typeConceptUuid = typeConceptUuid;
    }

    public TypedNodeWithUuids(TypedNodeWithSequences internalForm) {
        super(internalForm);
        this.typeConceptUuid = Get.identifierService().getUuidPrimordialFromConceptSequence(internalForm.getTypeConceptSequence()).get();
    }
    
    @Override
    public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        switch (dataTarget) {
            case EXTERNAL:
                super.writeData(dataOutput, dataTarget);
                dataOutput.writeLong(typeConceptUuid.getMostSignificantBits());
                dataOutput.writeLong(typeConceptUuid.getLeastSignificantBits());
                break;
            case INTERNAL:
                TypedNodeWithSequences internalForm = null;
                if (this instanceof FeatureNodeWithUuids) {
                    internalForm = new FeatureNodeWithSequences((FeatureNodeWithUuids)this);
                    ((FeatureNodeWithSequences)internalForm).writeNodeData(dataOutput, dataTarget);
                }
                else if (this instanceof RoleNodeAllWithUuids) {
                    internalForm = new RoleNodeAllWithSequences((RoleNodeAllWithUuids)this);
                    ((RoleNodeAllWithSequences)internalForm).writeNodeData(dataOutput, dataTarget);
                }
                else if (this instanceof RoleNodeSomeWithUuids) {
                    internalForm = new RoleNodeSomeWithSequences((RoleNodeSomeWithUuids)this);
                    ((RoleNodeSomeWithSequences)internalForm).writeNodeData(dataOutput, dataTarget);
                }
                else {
                    throw new RuntimeException("Can't write internal form!");
                }
                break;
            default: throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
        }
    }

    public UUID getTypeConceptUuid() {
        return typeConceptUuid;
    }

    @Override
    public String toString() {
        return toString("");
    }
    
    @Override
    public String toString(String nodeIdSuffix) {
        return " " + Get.conceptService().getConcept(typeConceptUuid).toUserString();
    }

    public LogicNode getOnlyChild() {
        LogicNode[] children = getChildren();
        if (children.length == 1) {
            return children[0];
        }
        throw new IllegalStateException("Typed nodes can have only one child. Found: " + Arrays.toString(children));
    }
    
    @Override
    protected final int compareNodeFields(LogicNode o) {
        // node semantic already determined equals. 
        TypedNodeWithUuids other = (TypedNodeWithUuids) o;
        if (!typeConceptUuid.equals(other.typeConceptUuid)) {
            return typeConceptUuid.compareTo(other.typeConceptUuid);
        }
        return compareTypedNodeFields(o);
    }
    protected abstract int compareTypedNodeFields(LogicNode o);
    
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + this.typeConceptUuid.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypedNodeWithUuids other = (TypedNodeWithUuids) obj;
        if (!this.typeConceptUuid.equals(other.typeConceptUuid)) {
            return false;
        }
        return super.equals(obj);
    }
}