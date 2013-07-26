package org.ihtsdo.otf.tcc.api.refex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_string.RefexNidNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_boolean.RefexNidBooleanVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_float.RefexNidNidNidFloatVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_int.RefexNidNidNidIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long.RefexNidNidNidLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_string.RefexNidNidNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;

public enum RefexType {

    /**
     * CID = Component IDentifier
     * 
     * @author kec
     * 
     */
    MEMBER(1, RefexVersionBI.class),
    CID(2, RefexNidVersionBI.class),
    CID_CID(3, RefexNidNidVersionBI.class),
    CID_CID_CID(4, RefexNidNidNidVersionBI.class),
    CID_CID_STR(5, RefexNidNidStringVersionBI.class),
    STR(6, RefexStringVersionBI.class),
    INT(7, RefexIntVersionBI.class),
    CID_INT(8, RefexNidIntVersionBI.class),
    BOOLEAN(9, RefexBooleanVersionBI.class),
    CID_STR(10, RefexNidStringVersionBI.class),
    CID_FLOAT(11, RefexNidFloatVersionBI.class),
    CID_LONG(12, RefexNidLongVersionBI.class),
    LONG(13, RefexLongVersionBI.class),
    ARRAY_BYTEARRAY(14, RefexArrayOfBytearrayVersionBI.class),
    CID_CID_CID_FLOAT(15, RefexNidNidNidFloatVersionBI.class),
    CID_CID_CID_INT(16, RefexNidNidNidIntVersionBI.class),
    CID_CID_CID_LONG(17, RefexNidNidNidLongVersionBI.class),
    CID_CID_CID_STRING(18, RefexNidNidNidStringVersionBI.class),
   /**
     * A refex type that can annotate a referenced component with a component identifier
     * and a boolean value. 
     */
    CID_BOOLEAN(19, RefexNidBooleanVersionBI.class),
    
    UNKNOWN(Byte.MAX_VALUE, null);

    public static RefexType getFromToken(int type) throws UnsupportedOperationException {
        switch (type) {
            case 1:
                return MEMBER;
            case 2:
                return CID;
            case 3:
                return CID_CID;
            case 4:
                return CID_CID_CID;
            case 5:
                return CID_CID_STR;
            case 6:
                return STR;
            case 7:
                return INT;
            case 8:
                return CID_INT;
            case 9:
                return BOOLEAN;
            case 10:
                return CID_STR;
            case 11:
                return CID_FLOAT;
            case 12:
                return CID_LONG;
            case 13:
                return LONG;
            case 14:
                return ARRAY_BYTEARRAY;
            case 15:
                return CID_CID_CID_FLOAT;
            case 16:
                return CID_CID_CID_INT;
            case 17:
                return CID_CID_CID_LONG;
            case 18:
                return CID_CID_CID_STRING;
            case 19:
                return CID_BOOLEAN;
        }
        try {
            throw new UnsupportedOperationException("Can't handle type: " + type + " " +
                            Ts.get().getConceptForNid(type).toLongString());
        } catch (IOException ex) {
            Logger.getLogger(RefexType.class.getName()).log(Level.SEVERE, null, ex);
            throw new UnsupportedOperationException("Can't handle type: " + type);
        }
    }
    private int externalizedToken;
    private Class<? extends RefexVersionBI> rxc;

    RefexType(int externalizedToken, Class<? extends RefexVersionBI> rxc) {
        this.externalizedToken = externalizedToken;
        this.rxc = rxc;
    }
    
    public int getTypeToken() {
        return this.externalizedToken;
    }

    public Class<? extends RefexVersionBI> getRefexClass() {
        return rxc;
    }

    public void writeType(DataOutput output) throws IOException {
        output.writeByte(externalizedToken);
    }

    public static RefexType classToType(Class<?> c) {
        if (RefexNidNidNidFloatVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_CID_FLOAT;
        }
        if (RefexNidNidNidIntVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_CID_INT;
        }
        if (RefexNidNidNidLongVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_CID_LONG;
        }
        if (RefexNidNidNidStringVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_CID_STRING;
        }
        if (RefexNidNidNidVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_CID;
        }
        if (RefexNidNidStringVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_STR;
        }
        if (RefexNidNidVersionBI.class.isAssignableFrom(c)) {
            return CID_CID;
        }
        if (RefexNidIntVersionBI.class.isAssignableFrom(c)) {
            return CID_INT;
        }
        if (RefexNidStringVersionBI.class.isAssignableFrom(c)) {
            return CID_STR;
        }
        if (RefexNidFloatVersionBI.class.isAssignableFrom(c)) {
            return CID_FLOAT;
        }
        if (RefexNidLongVersionBI.class.isAssignableFrom(c)) {
            return CID_LONG;
        }
        if (RefexNidBooleanVersionBI.class.isAssignableFrom(c)) {
            return CID_BOOLEAN;
        }
        if (RefexNidVersionBI.class.isAssignableFrom(c)) {
            return CID;
        }
        if (RefexStringVersionBI.class.isAssignableFrom(c)) {
            return STR;
        }
        if (RefexIntVersionBI.class.isAssignableFrom(c)) {
            return INT;
        }
        if (RefexLongVersionBI.class.isAssignableFrom(c)) {
            return LONG;
        }
        if (RefexVersionBI.class.isAssignableFrom(c)) {
            return MEMBER;
        }
        if (RefexArrayOfBytearrayVersionBI.class.isAssignableFrom(c)) {
            return ARRAY_BYTEARRAY;
        }
        if (RefexBooleanVersionBI.class.isAssignableFrom(c)) {
            return BOOLEAN;
        }
        return UNKNOWN;
    }

    public static RefexType readType(DataInput input) throws IOException {
        int type = input.readByte();
        return getFromToken(type);
    }
}
