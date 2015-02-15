package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_int;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidIntSerializer extends RefexSerializer<NidIntMember, NidIntRevision> {
    private static NidIntSerializer singleton;
    public static NidIntSerializer get() {
        if (singleton == null) {
            singleton = new NidIntSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, NidIntMember cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeInt(cc.intValue);

    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidIntMember cc) throws IOException {
        cc.c1Nid    = input.readInt();
        cc.intValue = input.readInt();

    }

    @Override
    protected void serializeRevision(DataOutput output, NidIntRevision nidIntRevision) throws IOException {
        output.writeInt(nidIntRevision.c1Nid);
        output.writeInt(nidIntRevision.intValue);
    }

    @Override
    public NidIntRevision newRevision() {
        return new NidIntRevision();
    }

    @Override
    public NidIntMember newComponent() {
        return new NidIntMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidIntRevision cc) throws IOException {
        cc.c1Nid    = input.readInt();
        cc.intValue = input.readInt();
    }
}
