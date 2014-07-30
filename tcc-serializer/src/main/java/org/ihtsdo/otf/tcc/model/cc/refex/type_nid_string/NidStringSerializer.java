package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidStringSerializer extends RefexSerializer<NidStringMember, NidStringRevision> {
    private static NidStringSerializer singleton;
    public static NidStringSerializer get() {
        if (singleton == null) {
            singleton = new NidStringSerializer();
        }
        return singleton;
    }


    @Override
    protected void serializePrimordialFields(DataOutput output, NidStringMember cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeUTF(cc.string1);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidStringMember cc) throws IOException {
        cc.c1Nid    = input.readInt();
        cc.string1 = input.readUTF();

    }

    @Override
    protected void serializeRevision(DataOutput output, NidStringRevision cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeUTF(cc.string1);
    }

    @Override
    public NidStringRevision newRevision() {
        return new NidStringRevision();
    }

    @Override
    public NidStringMember newComponent() {
        return new NidStringMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidStringRevision cc) throws IOException {
        cc.c1Nid    = input.readInt();
        cc.string1 = input.readUTF();

    }
}
