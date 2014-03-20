package org.ihtsdo.otf.tcc.api.refex2.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexBooleanBI;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexByteArrayBI;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexCompressedStringBI;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexDoubleBI;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexFloatBI;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexIntegerBI;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexLongBI;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexNidBI;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexStringBI;
import org.ihtsdo.otf.tcc.api.refex2.types.data.RefexUUIDBI;


public enum RefexDataType {

	/**
	 * @author kec
	 */
	NID(101, RefexNidBI.class),
	STRING(102, RefexStringBI.class),
	COMPRESSED_STRING(103, RefexCompressedStringBI.class),
	INTEGER(104, RefexIntegerBI.class),
	BOOLEAN(105, RefexBooleanBI.class),
	LONG(106, RefexLongBI.class),
	BYTEARRAY(107, RefexByteArrayBI.class),
	FLOAT(108, RefexFloatBI.class),
	DOUBLE(109, RefexDoubleBI.class),
	UUID(110, RefexUUIDBI.class), 
	UNKNOWN(Byte.MAX_VALUE, null);


	public static RefexDataType getFromToken(int type) throws UnsupportedOperationException {
		switch (type) {
			case 101:
				return NID;
			case 102:
				return STRING;
			case 103:
				return COMPRESSED_STRING;
			case 104:
				return INTEGER;
			case 105:
				return BOOLEAN;
			case 106:
				return LONG;
			case 107:
				return BYTEARRAY;
			case 108:
				return FLOAT;
			case 109:
				return DOUBLE;
			case 110:
				return UUID;
		}
		throw new UnsupportedOperationException("Can't handle type: " + type);
	}
	private int externalizedToken_;
	private Class<? extends RefexDataBI> dataClass_;

	RefexDataType(int externalizedToken, Class<? extends RefexDataBI> dataClass)
	{
		externalizedToken_ = externalizedToken;
		dataClass_ = dataClass;
	}

	public int getTypeToken()
	{
		return this.externalizedToken_;
	}

	public Class<? extends RefexDataBI> getRefexMemberClass()
	{
		return dataClass_;
	}

	public void writeType(DataOutput output) throws IOException
	{
		output.writeByte(externalizedToken_);
	}

	public static RefexDataType classToType(Class<?> c) 
	{

		if (RefexNidBI.class.isAssignableFrom(c)) {
			return NID;
		}
		if (RefexStringBI.class.isAssignableFrom(c)) {
			return STRING;
		}
		if (RefexIntegerBI.class.isAssignableFrom(c)) {
			return INTEGER;
		}
		if (RefexLongBI.class.isAssignableFrom(c)) {
			return LONG;
		}
		if (RefexByteArrayBI.class.isAssignableFrom(c)) {
			return BYTEARRAY;
		}
		if (RefexBooleanBI.class.isAssignableFrom(c)) {
			return BOOLEAN;
		}
		//TODO missing types
		return UNKNOWN;
	}

	public static RefexDataType readType(DataInput input) throws IOException
	{
		int type = input.readByte();
		return getFromToken(type);
	}
}
