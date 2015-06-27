/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.logic.node.external;


import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.model.logic.node.AbstractNode;
import gov.vha.isaac.ochre.model.logic.node.ConnectorNode;
import gov.vha.isaac.ochre.model.logic.node.internal.TypedNodeWithNids;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 *
 * @author kec
 */
public abstract class TypedNodeWithUuids extends ConnectorNode {

    UUID typeConceptUuid;

    public TypedNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        this.typeConceptUuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
    }

    public TypedNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion, UUID typeConceptUuid, AbstractNode child) {
        super(logicGraphVersion, child);
        this.typeConceptUuid = typeConceptUuid;
    }

    public TypedNodeWithUuids(TypedNodeWithNids internalForm) {
        super(internalForm);
        this.typeConceptUuid = getIdentifierService().getUuidPrimordialForNid(internalForm.getTypeConceptNid()).get();
    }

    public UUID getTypeConceptUuid() {
        return typeConceptUuid;
    }

    @Override
    public String toString() {
        return " type: \"" + getConceptService().getConcept(typeConceptUuid).toUserString() +"\""+ super.toString();
    }

    public Node getOnlyChild() {
        Node[] children = getChildren();
        if (children.length == 1) {
            return children[0];
        }
        throw new IllegalStateException("Typed nodes can have only one child. Found: " + Arrays.toString(children));
    }
}

