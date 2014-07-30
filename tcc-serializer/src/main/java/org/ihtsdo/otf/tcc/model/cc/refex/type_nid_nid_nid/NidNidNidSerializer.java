package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidNidNidSerializer extends RefexSerializer<NidNidNidMember, NidNidNidRevision> {
    private static NidNidNidSerializer singleton;
    public static NidNidNidSerializer get() {
        if (singleton == null) {
            singleton = new NidNidNidSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, NidNidNidMember cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeInt(cc.c2Nid);
        output.writeInt(cc.c3Nid);

    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidNidNidMember cc) throws IOException {
        cc.c1Nid = input.readInt();
        cc.c2Nid = input.readInt();
        cc.c3Nid = input.readInt();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidNidNidRevision cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeInt(cc.c2Nid);
        output.writeInt(cc.c3Nid);
    }

    @Override
    public NidNidNidRevision newRevision() {
        return new NidNidNidRevision();
    }

    @Override
    public NidNidNidMember newComponent() {
        return new NidNidNidMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidNidNidRevision cc) throws IOException {
        cc.c1Nid = input.readInt();
        cc.c2Nid = input.readInt();
        cc.c3Nid = input.readInt();
    }
}
