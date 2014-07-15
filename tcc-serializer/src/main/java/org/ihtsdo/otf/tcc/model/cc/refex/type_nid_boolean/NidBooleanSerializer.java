package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_boolean;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidBooleanSerializer extends RefexSerializer<NidBooleanMember, NidBooleanRevision> {
    private static NidBooleanSerializer singleton;
    public static NidBooleanSerializer get() {
        if (singleton == null) {
            singleton = new NidBooleanSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, NidBooleanMember cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeBoolean(cc.boolean1);

    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidBooleanMember cc) throws IOException {
        cc.c1Nid      = input.readInt();
        cc.boolean1 = input.readBoolean();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidBooleanRevision nidBooleanRevision) throws IOException {
        output.writeInt(nidBooleanRevision.nid1);
        output.writeBoolean(nidBooleanRevision.boolean1);
    }

    @Override
    public NidBooleanRevision newRevision() {
        return new NidBooleanRevision();
    }

    @Override
    public NidBooleanMember newComponent() {
        return new NidBooleanMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidBooleanRevision nidBooleanRevision) throws IOException {
        nidBooleanRevision.nid1      = input.readInt();
        nidBooleanRevision.boolean1 = input.readBoolean();

    }
}
