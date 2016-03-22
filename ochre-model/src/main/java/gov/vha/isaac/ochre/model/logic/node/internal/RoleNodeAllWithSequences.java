package gov.vha.isaac.ochre.model.logic.node.internal;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.logic.node.AbstractLogicNode;
import gov.vha.isaac.ochre.model.logic.node.external.RoleNodeAllWithUuids;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 * Created by kec on 12/6/14.
 */
public final class RoleNodeAllWithSequences extends TypedNodeWithSequences {

    public RoleNodeAllWithSequences(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    public RoleNodeAllWithSequences(LogicalExpressionOchreImpl logicGraphVersion, int typeConceptId, AbstractLogicNode child) {
        super(logicGraphVersion, typeConceptId, child);
    }
    
    public RoleNodeAllWithSequences(RoleNodeAllWithUuids externalForm) {
        super(externalForm);
    }

    @Override
    public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        switch (dataTarget) {
            case EXTERNAL:
                RoleNodeAllWithUuids externalForm = new RoleNodeAllWithUuids(this);
                externalForm.writeNodeData(dataOutput, dataTarget);
                break;
            case INTERNAL:
                super.writeData(dataOutput, dataTarget);
                break;
            default:
                throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
        }
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.ROLE_ALL;
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                Get.identifierService().getUuidPrimordialFromConceptSequence(typeConceptSequence).get().toString());

    }

    @Override
    public String toString() {
        return "All[" + getNodeIndex() + "]" + super.toString();
    }
    
    @Override
    public String toString(String nodeIdSuffix) {
        return "All[" + getNodeIndex() + nodeIdSuffix + "]" + super.toString(nodeIdSuffix);
    }
    
    @Override
    protected int compareTypedNodeFields(LogicNode o) {
        // node semantic already determined equals. 
        return 0;
    }
    
}
