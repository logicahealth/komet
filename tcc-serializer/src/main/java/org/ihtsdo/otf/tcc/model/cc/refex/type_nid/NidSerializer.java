package org.ihtsdo.otf.tcc.model.cc.refex.type_nid;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidSerializer extends RefexSerializer<NidMember, NidRevision> {
    private static NidSerializer singleton;
    public static NidSerializer get() {
        if (singleton == null) {
            singleton = new NidSerializer();
        }
        return singleton;
    }
    @Override
    protected void serializePrimordialFields(DataOutput output, NidMember cc) throws IOException {
        output.writeInt(cc.c1Nid);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidMember cc) throws IOException {
        cc.c1Nid = input.readInt();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidRevision nidRevision) throws IOException {
           output.writeInt(nidRevision.nid1);
    }

    @Override
    public NidRevision newRevision() {
        return new NidRevision();
    }

    @Override
    public NidMember newComponent() {
        return new NidMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidRevision nidRevision) throws IOException {
        nidRevision.nid1 = input.readInt();
    }
}
