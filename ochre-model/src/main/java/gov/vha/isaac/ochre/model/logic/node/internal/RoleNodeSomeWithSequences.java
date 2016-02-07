package gov.vha.isaac.ochre.model.logic.node.internal;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.logic.node.AbstractLogicNode;
import gov.vha.isaac.ochre.model.logic.node.external.RoleNodeSomeWithUuids;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 * Created by kec on 12/10/14.
 */
public final class RoleNodeSomeWithSequences extends TypedNodeWithSequences {

    public RoleNodeSomeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    public RoleNodeSomeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, int typeConceptId, AbstractLogicNode child) {
        super(logicGraphVersion, typeConceptId, child);
    }
    
    public RoleNodeSomeWithSequences(RoleNodeSomeWithUuids externalForm) {
        super(externalForm);
    }

    @Override
    public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        switch (dataTarget) {
            case EXTERNAL:
                RoleNodeSomeWithUuids externalForm = new RoleNodeSomeWithUuids(this);
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
        return NodeSemantic.ROLE_SOME;
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                Get.identifierService().getUuidPrimordialForNid(typeConceptSequence).toString());
    }

    @Override
    public String toString() {
        return toString("");
    }
    
    @Override
    public String toString(String nodeIdSuffix) {
        return "Some[" + getNodeIndex() + nodeIdSuffix + "]" + super.toString(nodeIdSuffix);
    }
    
    @Override
    protected int compareTypedNodeFields(LogicNode o) {
        // node semantic already determined equals. 
        return 0;
    }
}
