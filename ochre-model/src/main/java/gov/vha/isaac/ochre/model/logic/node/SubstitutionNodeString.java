package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import gov.vha.isaac.ochre.model.logic.LogicExpressionOchreImpl;
import gov.vha.isaac.ochre.model.logic.NodeSemantic;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import gov.vha.isaac.ochre.util.UuidT5Generator;

/**
 * Created by kec on 12/10/14.
 */
public class SubstitutionNodeString extends SubstitutionNodeLiteral {

    public SubstitutionNodeString(LogicExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }

    public SubstitutionNodeString(LogicExpressionOchreImpl logicGraphVersion,
            SubstitutionFieldSpecification substitutionFieldSpecification) {
        super(logicGraphVersion, substitutionFieldSpecification);
    }

    @Override
    public String toString() {
        return "SubstitutionNodeString[" + getNodeIndex() + "]:" + super.toString();
    }

    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.SUBSTITUTION_STRING;
    }

    @Override
    protected UUID initNodeUuid() {
        try {
            return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                    substitutionFieldSpecification.toString());
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
