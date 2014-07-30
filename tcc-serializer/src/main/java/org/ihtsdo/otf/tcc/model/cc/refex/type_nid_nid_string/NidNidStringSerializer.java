package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_string;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidNidStringSerializer extends RefexSerializer<NidNidStringMember, NidNidStringRevision> {
    private static NidNidStringSerializer singleton;
    public static NidNidStringSerializer get() {
        if (singleton == null) {
            singleton = new NidNidStringSerializer();
        }
        return singleton;
    }


    @Override
    protected void serializePrimordialFields(DataOutput output, NidNidStringMember cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeInt(cc.c2Nid);
        output.writeUTF(cc.string1);
    }
    @Override
    public void deserializePrimordialFields(DataInput input, NidNidStringMember cc) throws IOException {
        cc.c1Nid    = input.readInt();
        cc.c2Nid    = input.readInt();
        cc.string1 = input.readUTF();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidNidStringRevision cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeInt(cc.c2Nid);
        output.writeUTF(cc.string1);
    }

    @Override
    public NidNidStringRevision newRevision() {
        return new NidNidStringRevision();
    }

    @Override
    public NidNidStringMember newComponent() {
        return new NidNidStringMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidNidStringRevision cc) throws IOException {
        cc.c1Nid    = input.readInt();
        cc.c2Nid    = input.readInt();
        cc.string1 = input.readUTF();
    }
}
