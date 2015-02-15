package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_float;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidFloatSerializer extends RefexSerializer<NidFloatMember, NidFloatRevision> {
    private static NidFloatSerializer singleton;
    public static NidFloatSerializer get() {
        if (singleton == null) {
            singleton = new NidFloatSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, NidFloatMember cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeFloat(cc.floatValue);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidFloatMember cc) throws IOException {
        cc.c1Nid      = input.readInt();
        cc.floatValue = input.readFloat();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidFloatRevision nidFloatRevision) throws IOException {
        output.writeInt(nidFloatRevision.c1Nid);
        output.writeFloat(nidFloatRevision.floatValue);

    }

    @Override
    public NidFloatRevision newRevision() {
        return new NidFloatRevision();
    }

    @Override
    public NidFloatMember newComponent() {
        return new NidFloatMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidFloatRevision nidFloatRevision) throws IOException {
        nidFloatRevision.c1Nid      = input.readInt();
        nidFloatRevision.floatValue = input.readFloat();

    }
}
