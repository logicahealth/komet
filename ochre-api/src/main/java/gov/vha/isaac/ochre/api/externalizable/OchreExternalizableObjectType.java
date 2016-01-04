/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.externalizable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author kec
 */
public enum OchreExternalizableObjectType {

    /**
     * An external representation of a concept. An identifier with status. Descriptions and definitions of concepts
     * are provided as SEMEMEs. 
     */
    CONCEPT((byte) 1), 
    /**
     * An external representation of a semantic unit of meaning, associated with a concept or another SEMEME. 
     */
    SEMEME((byte) 2),

    /**
     * An external representation of a stamp comment. 
     */
    STAMP_COMMENT((byte) 4),
    /**
     * An external representation of a stamp alias. 
     */
    STAMP_ALIAS((byte) 5);

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
                throw new UnsupportedOperationException("Commit record deprecated: " + token);
            case 4:
                return STAMP_COMMENT;
            case 5:
                return STAMP_ALIAS;
            default:
                throw new UnsupportedOperationException("Can't handle: " + token);
        }
    }

    public void toDataStream(DataOutput out) throws IOException {
        out.writeByte(token);
    }
}
