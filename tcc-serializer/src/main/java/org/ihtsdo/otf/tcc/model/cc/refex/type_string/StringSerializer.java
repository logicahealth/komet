package org.ihtsdo.otf.tcc.model.cc.refex.type_string;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class StringSerializer extends RefexSerializer<StringMember, StringRevision> {
    private static StringSerializer singleton;
    public static StringSerializer get() {
        if (singleton == null) {
            singleton = new StringSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, StringMember cc) throws IOException {
        output.writeUTF(cc.stringValue);
    }

    @Override
    public void deserializePrimordialFields(DataInput input, StringMember cc) throws IOException {
        cc.stringValue = input.readUTF();

    }

    @Override
    protected void serializeRevision(DataOutput output, StringRevision cc) throws IOException {
        output.writeUTF(cc.stringValue);
    }

    @Override
    public StringRevision newRevision() {
        return new StringRevision();
    }

    @Override
    public StringMember newComponent() {
        return new StringMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, StringRevision cc) throws IOException {
        cc.stringValue = input.readUTF();

    }
}
