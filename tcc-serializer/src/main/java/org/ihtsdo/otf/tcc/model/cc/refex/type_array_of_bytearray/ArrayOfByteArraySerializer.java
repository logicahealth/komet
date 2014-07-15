package org.ihtsdo.otf.tcc.model.cc.refex.type_array_of_bytearray;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/13/14.
 */
public class ArrayOfByteArraySerializer  extends RefexSerializer<ArrayOfByteArrayMember, ArrayOfByteArrayRevision> {
    private static ArrayOfByteArraySerializer singleton;
    public static ArrayOfByteArraySerializer get() {
        if (singleton == null) {
            singleton = new ArrayOfByteArraySerializer();
        }
        return singleton;
    }

    @Override
    protected void serializeRevision(DataOutput output, ArrayOfByteArrayRevision r) throws IOException {
        output.writeShort(r.arrayOfByteArray.length);
        for (byte[] bytes: r.arrayOfByteArray) {
            output.writeInt(bytes.length);
            output.write(bytes);
        }
    }

    @Override
    public ArrayOfByteArrayRevision newRevision() {
        return new ArrayOfByteArrayRevision();
    }

    @Override
    public ArrayOfByteArrayMember newComponent() {
        return new ArrayOfByteArrayMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, ArrayOfByteArrayRevision r) throws IOException {
        int arrayLength = input.readShort();
        r.arrayOfByteArray = new byte[arrayLength][];
        for (int i = 0; i < arrayLength; i++) {
            int byteArrayLength = input.readInt();
            r.arrayOfByteArray[i] = new byte[byteArrayLength];
            input.readFully(r.arrayOfByteArray[i], 0, byteArrayLength);
        }
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, ArrayOfByteArrayMember cc) throws IOException {
        output.writeShort(cc.arrayOfByteArray.length);
        for (byte[] bytes: cc.arrayOfByteArray) {
            output.writeInt(bytes.length);
            output.write(bytes);
        }
    }

    @Override
    public void deserializePrimordialFields(DataInput input, ArrayOfByteArrayMember cc) throws IOException {
        int arrayLength = input.readShort();
        cc.arrayOfByteArray = new byte[arrayLength][];
        for (int i = 0; i < arrayLength; i++) {
            int byteArrayLength = input.readInt();
            cc.arrayOfByteArray[i] = new byte[byteArrayLength];
            input.readFully(cc.arrayOfByteArray[i], 0, byteArrayLength);
        }
    }
}
