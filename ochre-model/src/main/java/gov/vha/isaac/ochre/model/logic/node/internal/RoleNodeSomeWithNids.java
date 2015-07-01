package gov.vha.isaac.ochre.model.logic.node.internal;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.logic.node.AbstractNode;
import gov.vha.isaac.ochre.model.logic.node.external.RoleNodeSomeWithUuids;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.util.UuidT5Generator;

/**
 * Created by kec on 12/10/14.
 */
public final class RoleNodeSomeWithNids extends TypedNodeWithNids {

    public RoleNodeSomeWithNids(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    public RoleNodeSomeWithNids(LogicalExpressionOchreImpl logicGraphVersion, int typeConceptNid, AbstractNode child) {
        super(logicGraphVersion, typeConceptNid, child);
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
                Get.identifierService().getUuidPrimordialForNid(typeConceptNid).toString());
    }

    @Override
    public String toString() {
        return "RoleNodeSome[" + getNodeIndex() + "]:" + super.toString();
    }
}
