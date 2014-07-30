package org.ihtsdo.otf.tcc.model.cc.refex.type_boolean;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class BooleanSerializer extends RefexSerializer<BooleanMember, BooleanRevision> {
    private static BooleanSerializer singleton;
    public static BooleanSerializer get() {
        if (singleton == null) {
            singleton = new BooleanSerializer();
        }
        return singleton;
    }


    @Override
    protected void serializeRevision(DataOutput output, BooleanRevision booleanRevision) throws IOException {
        output.writeBoolean(booleanRevision.booleanValue);
    }

    @Override
    public BooleanRevision newRevision() {
        return new BooleanRevision();
    }

    @Override
    public BooleanMember newComponent() {
        return new BooleanMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, BooleanRevision booleanRevision) throws IOException {
        booleanRevision.booleanValue = input.readBoolean();
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, BooleanMember cc) throws IOException {
        output.writeBoolean(cc.booleanValue);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, BooleanMember cc) throws IOException {
        cc.booleanValue = input.readBoolean();
    }
}
