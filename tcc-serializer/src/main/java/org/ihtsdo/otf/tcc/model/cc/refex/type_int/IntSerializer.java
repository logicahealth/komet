package org.ihtsdo.otf.tcc.model.cc.refex.type_int;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class IntSerializer extends RefexSerializer<IntMember, IntRevision> {
    private static IntSerializer singleton;
    public static IntSerializer get() {
        if (singleton == null) {
            singleton = new IntSerializer();
        }
        return singleton;
    }
    @Override
    protected void serializePrimordialFields(DataOutput output, IntMember cc) throws IOException {
        output.writeInt(cc.int1);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, IntMember cc) throws IOException {
        cc.int1 = input.readInt();
    }

    @Override
    protected void serializeRevision(DataOutput output, IntRevision intRevision) throws IOException {
        output.writeInt(intRevision.intValue);
    }

    @Override
    public IntRevision newRevision() {
        return new IntRevision();
    }

    @Override
    public IntMember newComponent() {
        return new IntMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, IntRevision intRevision) throws IOException {
        intRevision.intValue = input.readInt();
    }
}
