package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by kec on 12/12/14.
 */
public abstract class SubstitutionNodeLiteral extends SubstitutionNode {

    public SubstitutionNodeLiteral(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
    }


    public SubstitutionNodeLiteral(LogicalExpressionOchreImpl logicGraphVersion, SubstitutionFieldSpecification substitutionFieldSpecification) {
        super(logicGraphVersion, substitutionFieldSpecification);
    }
}
