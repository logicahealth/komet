package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_long;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidLongSerializer extends RefexSerializer<NidLongMember, NidLongRevision> {
    private static NidLongSerializer singleton;
    public static NidLongSerializer get() {
        if (singleton == null) {
            singleton = new NidLongSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, NidLongMember cc) throws IOException {
        output.writeInt(cc.c1Nid);
        output.writeLong(cc.longValue);

    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidLongMember cc) throws IOException {
        cc.c1Nid     = input.readInt();
        cc.longValue = input.readLong();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidLongRevision nidLongRevision) throws IOException {
        output.writeInt(nidLongRevision.c1Nid);
        output.writeLong(nidLongRevision.longValue);

    }

    @Override
    public NidLongRevision newRevision() {
        return new NidLongRevision();
    }

    @Override
    public NidLongMember newComponent() {
        return new NidLongMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidLongRevision nidLongRevision) throws IOException {
        nidLongRevision.c1Nid     = input.readInt();
        nidLongRevision.longValue = input.readLong();

    }
}
