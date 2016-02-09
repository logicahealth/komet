/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.logic.node.external;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.ConcreteDomainOperators;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.logic.node.AbstractLogicNode;
import gov.vha.isaac.ochre.model.logic.node.internal.FeatureNodeWithSequences;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 *
 * @author kec
 */
public class FeatureNodeWithUuids extends TypedNodeWithUuids {

    static ConcreteDomainOperators[] concreteDomainOperators = ConcreteDomainOperators.values();

    ConcreteDomainOperators operator;

//    UUID unitsConceptUuid;

    public FeatureNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        operator = concreteDomainOperators[dataInputStream.readByte()];
//        unitsConceptUuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
    }
    public FeatureNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion, UUID typeConceptUuid, AbstractLogicNode child) {
        super(logicGraphVersion, typeConceptUuid, child);
    }
    public FeatureNodeWithUuids(FeatureNodeWithSequences internalNode) throws IOException {
        super(internalNode);
        operator = internalNode.getOperator();
//        unitsConceptUuid = Get.identifierService().getUuidPrimordialForNid(internalNode.getUnitsConceptSequence()).get();
    }

    @Override
    public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeNodeData(dataOutput, dataTarget);
        switch (dataTarget) {
            case EXTERNAL:
                dataOutput.writeByte(operator.ordinal());
//                dataOutput.writeLong(unitsConceptUuid.getMostSignificantBits());
//                dataOutput.writeLong(unitsConceptUuid.getLeastSignificantBits());
                break;
            case INTERNAL:
                FeatureNodeWithSequences internalForm =  new FeatureNodeWithSequences(this);
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
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                typeConceptUuid.toString() +
                        operator
                        // + unitsConceptUuid.toString()
                        ); 
     }
        

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FeatureNodeWithUuids that = (FeatureNodeWithUuids) o;

        if (!typeConceptUuid.equals(that.typeConceptUuid)) return false;
        return operator == that.operator;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + operator.hashCode();
//        result = 31 * result + unitsConceptUuid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return toString("");
    }
    
    @Override
    public String toString(String nodeIdSuffix) {
        return "FeatureNode[" + getNodeIndex() + nodeIdSuffix + "] " +
                 operator +
                ", units:" 
                 //+ Get.conceptService().getConcept(unitsConceptUuid).toUserString()
                + super.toString(nodeIdSuffix);
    }
    
    public ConcreteDomainOperators getOperator() {
        return operator;
    }
    
//    public UUID getUnitsConceptUuid() {
//        return unitsConceptUuid;
//    }
    @Override
    protected int compareTypedNodeFields(LogicNode o) {
        // node semantic already determined equals. 
        FeatureNodeWithUuids other = (FeatureNodeWithUuids) o;
        if (!typeConceptUuid.equals(other.typeConceptUuid)) {
            return typeConceptUuid.compareTo(other.typeConceptUuid);
        }
        return operator.compareTo(other.operator);
    }

}
