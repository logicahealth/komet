package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_long;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidNidNidLongSerializer extends RefexSerializer<NidNidNidLongMember, NidNidNidLongRevision>{
    private static NidNidNidLongSerializer singleton;
    public static NidNidNidLongSerializer get() {
        if (singleton == null) {
            singleton = new NidNidNidLongSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, NidNidNidLongMember cc) throws IOException {
        output.writeInt(cc.nid1);
        output.writeInt(cc.nid2);
        output.writeInt(cc.nid3);
        output.writeLong(cc.long1);

    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidNidNidLongMember cc) throws IOException {
        cc.nid1 = input.readInt();
        cc.nid2 = input.readInt();
        cc.nid3 = input.readInt();
        cc.long1 = input.readLong();
    }

    @Override
    protected void serializeRevision(DataOutput output, NidNidNidLongRevision cc) throws IOException {
        output.writeInt(cc.nid1);
        output.writeInt(cc.nid2);
        output.writeInt(cc.nid3);
        output.writeLong(cc.long1);

    }

    @Override
    public NidNidNidLongRevision newRevision() {
        return new NidNidNidLongRevision();
    }

    @Override
    public NidNidNidLongMember newComponent() {
        return new NidNidNidLongMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidNidNidLongRevision cc) throws IOException {
        cc.nid1 = input.readInt();
        cc.nid2 = input.readInt();
        cc.nid3 = input.readInt();
        cc.long1 = input.readLong();

    }
}
