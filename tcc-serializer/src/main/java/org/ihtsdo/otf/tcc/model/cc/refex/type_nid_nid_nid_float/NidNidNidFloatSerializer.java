package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_float;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidNidNidFloatSerializer extends RefexSerializer<NidNidNidFloatMember, NidNidNidFloatRevision> {
    private static NidNidNidFloatSerializer singleton;
    public static NidNidNidFloatSerializer get() {
        if (singleton == null) {
            singleton = new NidNidNidFloatSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, NidNidNidFloatMember cc) throws IOException {
        output.writeInt(cc.nid1);
        output.writeInt(cc.nid2);
        output.writeInt(cc.nid3);
        output.writeFloat(cc.float1);

    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidNidNidFloatMember cc) throws IOException {
        cc.nid1 = input.readInt();
        cc.nid2 = input.readInt();
        cc.nid3 = input.readInt();
        cc.float1 = input.readFloat();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidNidNidFloatRevision cc) throws IOException {
        output.writeInt(cc.nid1);
        output.writeInt(cc.nid2);
        output.writeInt(cc.nid3);
        output.writeFloat(cc.float1);
    }

    @Override
    public NidNidNidFloatRevision newRevision() {
        return new NidNidNidFloatRevision();
    }

    @Override
    public NidNidNidFloatMember newComponent() {
        return new NidNidNidFloatMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidNidNidFloatRevision cc) throws IOException {
        cc.nid1 = input.readInt();
        cc.nid2 = input.readInt();
        cc.nid3 = input.readInt();
        cc.float1 = input.readFloat();
    }
}
