package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 * Created by kec on 12/10/14.
 */
public class SubstitutionNodeString extends SubstitutionNodeLiteral {

    public SubstitutionNodeString(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    public SubstitutionNodeString(LogicalExpressionOchreImpl logicGraphVersion,
            SubstitutionFieldSpecification substitutionFieldSpecification) {
        super(logicGraphVersion, substitutionFieldSpecification);
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return "String substitution[" + getNodeIndex() + nodeIdSuffix + "]" + super.toString(nodeIdSuffix);
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.SUBSTITUTION_STRING;
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                substitutionFieldSpecification.toString());
    }
}
