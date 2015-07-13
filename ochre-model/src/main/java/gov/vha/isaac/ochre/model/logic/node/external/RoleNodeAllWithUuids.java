/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.logic.node.external;


import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.logic.node.AbstractNode;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeAllWithNids;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import gov.vha.isaac.ochre.util.UuidT5Generator;

/**
 *
 * @author kec
 */
public class RoleNodeAllWithUuids extends TypedNodeWithUuids {

    public RoleNodeAllWithUuids(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    public RoleNodeAllWithUuids(LogicalExpressionOchreImpl logicGraphVersion, UUID typeConceptUuid, AbstractNode child) {
        super(logicGraphVersion, typeConceptUuid, child);
    }

    public RoleNodeAllWithUuids(RoleNodeAllWithNids internalFrom) {
        super(internalFrom);
    }

    @Override
    public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
    }


    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.ROLE_ALL;
    }
    
    @Override
    protected UUID initNodeUuid() {
        
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                typeConceptUuid.toString()); 
        
     }

    @Override
    public String toString() {
        return "RoleNodeAll[" + getNodeIndex() + "]:" + super.toString();
    }
    
    @Override
    protected int compareTypedNodeFields(Node o) {
        // node semantic already determined equals. 
        return 0;
    }
    
}
