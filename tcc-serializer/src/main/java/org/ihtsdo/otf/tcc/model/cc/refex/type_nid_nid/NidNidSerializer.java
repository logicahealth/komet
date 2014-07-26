package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidNidSerializer extends RefexSerializer<NidNidMember, NidNidRevision> {
    private static NidNidSerializer singleton;
    public static NidNidSerializer get() {
        if (singleton == null) {
            singleton = new NidNidSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, NidNidMember cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeInt(cc.c2Nid);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidNidMember cc) throws IOException {
        cc.c1Nid = input.readInt();
        cc.c2Nid = input.readInt();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidNidRevision cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeInt(cc.c2Nid);
    }

    @Override
    public NidNidRevision newRevision() {
        return new NidNidRevision();
    }

    @Override
    public NidNidMember newComponent() {
        return new NidNidMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidNidRevision cc) throws IOException {
        cc.c1Nid = input.readInt();
        cc.c2Nid = input.readInt();
    }
}
