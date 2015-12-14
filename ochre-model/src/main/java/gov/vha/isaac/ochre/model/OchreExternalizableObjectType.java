/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author kec
 */
public enum OchreExternalizableObjectType {

    CONCEPT((byte) 1), 
    SEMEME((byte) 2),
    COMMIT_RECORD((byte) 3);

    private final byte token;

    private OchreExternalizableObjectType(byte token) {
        this.token = token;
    }

    public byte getToken() {
        return token;
    }

    public static OchreExternalizableObjectType fromDataStream(DataInput input) throws IOException {
        byte token = input.readByte();
        switch (token) {
            case 1:
                return CONCEPT;
            case 2:
                return SEMEME;
            case 3:
                return COMMIT_RECORD;
            default:
                throw new UnsupportedOperationException("Can't handle: " + token);
        }
    }

    public void toDataStream(DataOutput out) throws IOException {
        out.writeByte(token);
    }
}
