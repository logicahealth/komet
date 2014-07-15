package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_string;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidNidNidStringSerializer extends RefexSerializer<NidNidNidStringMember,NidNidNidStringRevision>{
    private static NidNidNidStringSerializer singleton;
    public static NidNidNidStringSerializer get() {
        if (singleton == null) {
            singleton = new NidNidNidStringSerializer();
        }
        return singleton;
    }


    @Override
    protected void serializePrimordialFields(DataOutput output, NidNidNidStringMember cc) throws IOException {
        output.writeInt(cc.nid1);
        output.writeInt(cc.nid2);
        output.writeInt(cc.nid3);
        output.writeUTF(cc.string1);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidNidNidStringMember cc) throws IOException {
        cc.nid1 = input.readInt();
        cc.nid2 = input.readInt();
        cc.nid3 = input.readInt();
        cc.string1 = input.readUTF();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidNidNidStringRevision cc) throws IOException {
        output.writeInt(cc.nid1);
        output.writeInt(cc.nid2);
        output.writeInt(cc.nid3);
        output.writeUTF(cc.string1);
    }

    @Override
    public NidNidNidStringRevision newRevision() {
        return new NidNidNidStringRevision();
    }

    @Override
    public NidNidNidStringMember newComponent() {
        return new NidNidNidStringMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidNidNidStringRevision cc) throws IOException {
        cc.nid1 = input.readInt();
        cc.nid2 = input.readInt();
        cc.nid3 = input.readInt();
        cc.string1 = input.readUTF();
    }
}
