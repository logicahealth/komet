package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import gov.vha.isaac.ochre.util.UuidT5Generator;

/**
 * Created by kec on 12/10/14.
 */
public class SubstitutionNodeFloat extends SubstitutionNodeLiteral {

    public SubstitutionNodeFloat(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    public SubstitutionNodeFloat(LogicalExpressionOchreImpl logicGraphVersion, SubstitutionFieldSpecification substitutionFieldSpecification) {
        super(logicGraphVersion, substitutionFieldSpecification);
    }

    @Override
    public String toString() {
        return "SubstitutionNodeFloat[" + getNodeIndex() + "]:" + super.toString();
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.SUBSTITUTION_FLOAT;
    }

    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                substitutionFieldSpecification.toString());
    }
}
