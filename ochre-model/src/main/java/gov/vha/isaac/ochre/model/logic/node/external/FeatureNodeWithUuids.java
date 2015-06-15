/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.logic.node.external;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.ConcreteDomainOperators;
import gov.vha.isaac.ochre.model.logic.LogicExpressionOchreImpl;
import gov.vha.isaac.ochre.model.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.logic.node.AbstractNode;
import gov.vha.isaac.ochre.model.logic.node.internal.FeatureNodeWithNids;
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
public class FeatureNodeWithUuids extends TypedNodeWithUuids {

    static ConcreteDomainOperators[] concreteDomainOperators = ConcreteDomainOperators.values();

    ConcreteDomainOperators operator;

    UUID unitsConceptUuid;

    public FeatureNodeWithUuids(LogicExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        operator = concreteDomainOperators[dataInputStream.readByte()];
        unitsConceptUuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
    }
    public FeatureNodeWithUuids(LogicExpressionOchreImpl logicGraphVersion, UUID typeConceptUuid, AbstractNode child) {
        super(logicGraphVersion, typeConceptUuid, child);
    }
    public FeatureNodeWithUuids(FeatureNodeWithNids internalNode) throws IOException {
        super(internalNode);
        operator = internalNode.getOperator();
        unitsConceptUuid = getIdentifierService().getUuidPrimordialForNid(internalNode.getUnitsConceptNid()).get();
    }

    @Override
    public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
        switch (dataTarget) {
            case EXTERNAL:
                dataOutput.writeByte(operator.ordinal());
                dataOutput.writeLong(unitsConceptUuid.getMostSignificantBits());
                dataOutput.writeLong(unitsConceptUuid.getLeastSignificantBits());
                break;
            case INTERNAL:
                FeatureNodeWithNids internalForm =  new FeatureNodeWithNids(this);
                internalForm.writeNodeData(dataOutput, dataTarget);
                break;
            default: throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
        }
    }


    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.FEATURE;
    }
    
    @Override
    protected UUID initNodeUuid() {
            try {
                return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(), 
                        typeConceptUuid.toString() +
                                operator + 
                                unitsConceptUuid.toString());
            } catch (IOException| NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            } 
     }
        

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FeatureNodeWithUuids that = (FeatureNodeWithUuids) o;

        if (!unitsConceptUuid.equals(that.unitsConceptUuid)) return false;
        return operator == that.operator;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + operator.hashCode();
        result = 31 * result + unitsConceptUuid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FeatureNode[" + getNodeIndex() + "]: " +
                 operator +
                ", units:" + getConceptService().getConcept(unitsConceptUuid).toUserString() +
                super.toString();
    }
    
    public ConcreteDomainOperators getOperator() {
        return operator;
    }
    
    public UUID getUnitsConceptUuid() {
        return unitsConceptUuid;
    }

}
