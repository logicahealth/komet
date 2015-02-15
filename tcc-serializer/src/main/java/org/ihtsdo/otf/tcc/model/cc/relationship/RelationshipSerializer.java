package org.ihtsdo.otf.tcc.model.cc.relationship;

import org.ihtsdo.otf.tcc.model.cc.component.AbstractSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class RelationshipSerializer extends AbstractSerializer<Relationship, RelationshipRevision> {
    private static RelationshipSerializer singleton;
    public static RelationshipSerializer get() {
        if (singleton == null) {
            singleton = new RelationshipSerializer();
        }
        return singleton;
    }
    @Override
    protected void serializePrimordial(DataOutput output, Relationship r) throws IOException {
        // Start writing
        // c1Nid is the enclosing concept, does not need to be written.
        output.writeInt(r.c2Nid);
        output.writeInt(r.characteristicNid);
        output.writeInt(r.group);
        output.writeInt(r.refinabilityNid);
        output.writeInt(r.typeNid);
    }

    @Override
    protected void serializeRevision(DataOutput output, RelationshipRevision relationshipRevision) throws IOException {
        output.writeInt(relationshipRevision.characteristicNid);
        output.writeInt(relationshipRevision.group);
        output.writeInt(relationshipRevision.refinabilityNid);
        output.writeInt(relationshipRevision.typeNid);

    }

    @Override
    public void deserializePrimordial(DataInput input, Relationship cc) throws IOException {
        cc.c2Nid             = input.readInt();
        cc.characteristicNid = input.readInt();
        cc.group             = input.readInt();
        cc.refinabilityNid   = input.readInt();
        cc.typeNid           = input.readInt();

    }

    @Override
    public RelationshipRevision newRevision() {
        return new RelationshipRevision();
    }

    @Override
    public Relationship newComponent() {
        return new Relationship();
    }

    @Override
    protected void deserializeRevision(DataInput input, RelationshipRevision relationshipRevision) throws IOException {
        relationshipRevision.characteristicNid = input.readInt();
        relationshipRevision.group = input.readInt();
        relationshipRevision.refinabilityNid = input.readInt();
        relationshipRevision.typeNid = input.readInt();
    }
}
