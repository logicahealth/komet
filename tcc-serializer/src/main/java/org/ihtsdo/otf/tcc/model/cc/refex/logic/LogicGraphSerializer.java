/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.model.cc.refex.logic;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.logic.LogicGraphMember;
import org.ihtsdo.otf.tcc.model.cc.refex.logic.LogicGraphRevision;

/**
 *
 * @author kec
 */
public class LogicGraphSerializer extends RefexSerializer<LogicGraphMember, LogicGraphRevision> {
    private static LogicGraphSerializer singleton;
    public static LogicGraphSerializer get() {
        if (singleton == null) {
            singleton = new LogicGraphSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializeRevision(DataOutput output, LogicGraphRevision r) throws IOException {
        output.writeShort(r.getLogicGraphBytes().length);
        for (byte[] bytes: r.getLogicGraphBytes()) {
            output.writeInt(bytes.length);
            output.write(bytes);
        }
    }

    @Override
    public LogicGraphRevision newRevision() {
        return new LogicGraphRevision();
    }

    @Override
    public LogicGraphMember newComponent() {
        return new LogicGraphMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, LogicGraphRevision r) throws IOException {
        int arrayLength = input.readShort();
        r.logicGraphBytes = new byte[arrayLength][];
        for (int i = 0; i < arrayLength; i++) {
            int byteArrayLength = input.readInt();
            r.logicGraphBytes[i] = new byte[byteArrayLength];
            input.readFully(r.logicGraphBytes[i], 0, byteArrayLength);
        }
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, LogicGraphMember cc) throws IOException {
        output.writeShort(cc.logicGraphBytes.length);
        for (byte[] bytes: cc.logicGraphBytes) {
            output.writeInt(bytes.length);
            output.write(bytes);
        }
    }

    @Override
    public void deserializePrimordialFields(DataInput input, LogicGraphMember cc) throws IOException {
        int arrayLength = input.readShort();
        cc.logicGraphBytes = new byte[arrayLength][];
        for (int i = 0; i < arrayLength; i++) {
            int byteArrayLength = input.readInt();
            cc.logicGraphBytes[i] = new byte[byteArrayLength];
            input.readFully(cc.logicGraphBytes[i], 0, byteArrayLength);
        }
    }
}
