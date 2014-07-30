package org.ihtsdo.otf.tcc.model.cc.refex.type_long;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class LongSerializer extends RefexSerializer<LongMember, LongRevision> {
    private static LongSerializer singleton;
    public static LongSerializer get() {
        if (singleton == null) {
            singleton = new LongSerializer();
        }
        return singleton;
    }
    @Override
    protected void serializePrimordialFields(DataOutput output, LongMember cc) throws IOException {
        output.writeLong(cc.longValue);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, LongMember cc) throws IOException {
        cc.longValue = input.readLong();
    }

    @Override
    protected void serializeRevision(DataOutput output, LongRevision longRevision) throws IOException {
        output.writeLong(longRevision.longValue);
    }

    @Override
    public LongRevision newRevision() {
        return new LongRevision();
    }

    @Override
    public LongMember newComponent() {
        return new LongMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, LongRevision longRevision) throws IOException {
        longRevision.longValue = input.readLong();
    }
}
