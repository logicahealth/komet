package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_int;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class NidNidNidIntSerializer extends RefexSerializer<NidNidNidIntMember, NidNidNidIntRevision>{
    private static NidNidNidIntSerializer singleton;
    public static NidNidNidIntSerializer get() {
        if (singleton == null) {
            singleton = new NidNidNidIntSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, NidNidNidIntMember cc) throws IOException {
        output.writeInt(cc.nid1);
        output.writeInt(cc.nid2);
        output.writeInt(cc.nid3);
        output.writeInt(cc.int1);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, NidNidNidIntMember cc) throws IOException {
        cc.nid1 = input.readInt();
        cc.nid2 = input.readInt();
        cc.nid3 = input.readInt();
        cc.int1 = input.readInt();

    }

    @Override
    protected void serializeRevision(DataOutput output, NidNidNidIntRevision cc) throws IOException {
        output.writeInt(cc.nid1);
        output.writeInt(cc.nid2);
        output.writeInt(cc.nid3);
        output.writeInt(cc.int1);

    }

    @Override
    public NidNidNidIntRevision newRevision() {
        return new NidNidNidIntRevision();
    }

    @Override
    public NidNidNidIntMember newComponent() {
        return new NidNidNidIntMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, NidNidNidIntRevision cc) throws IOException {
        cc.nid1 = input.readInt();
        cc.nid2 = input.readInt();
        cc.nid3 = input.readInt();
        cc.int1 = input.readInt();
    }
}
