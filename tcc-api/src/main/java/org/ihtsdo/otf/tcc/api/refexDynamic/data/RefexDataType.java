package org.ihtsdo.otf.tcc.api.refexDynamic.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexBooleanBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexByteArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDoubleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexFloatBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexLongBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexNidBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexPolymorphicBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexStringBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexUUIDBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;


/**
 * 
 * {@link RefexDataType}
 * 
 * Most types are fairly straight forward.  NIDs and INTEGERS are identical internally, except NIDs identify concepts.
 * Polymorphic is used when the data type for a refex isn't known at refex creation time.  In this case, a user of the API
 * will have to examine type types of the actual {@link RefexDataBI} objects returned, to look at the type.
 * 
 * For all other types, the data type reported within the Refex Definition should exactly match the data type returned with 
 * a {@link RefexDataBI}.
 * 
 * {@link RefexDataBI} will never return a {@link POLYMORPHIC} type.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum RefexDataType {
	
	NID(101, RefexNidBI.class, RefexDynamic.REFEX_NID_DT.getUuids()[0]),
	STRING(102, RefexStringBI.class, java.util.UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5")),  //String (foundation metadata concept)
	INTEGER(103, RefexIntegerBI.class, java.util.UUID.fromString("1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c")),  //Signed integer (foundation metadata concept)
	BOOLEAN(104, RefexBooleanBI.class, RefexDynamic.REFEX_BOOLEAN_DT.getUuids()[0]),
	LONG(105, RefexLongBI.class, RefexDynamic.REFEX_LONG_DT.getUuids()[0]),
	BYTEARRAY(106, RefexByteArrayBI.class, RefexDynamic.REFEX_BYTE_ARRAY_DT.getUuids()[0]),
	FLOAT(107, RefexFloatBI.class, RefexDynamic.REFEX_FLOAT_DT.getUuids()[0]),
	DOUBLE(108, RefexDoubleBI.class, RefexDynamic.REFEX_DOUBLE_DT.getUuids()[0]),
	UUID(109, RefexUUIDBI.class, java.util.UUID.fromString("845274b5-9644-3799-94c6-e0ea37e7d1a4")),  //Universally Unique Identifier (foundation metadata concept)
	POLYMORPHIC(110, RefexPolymorphicBI.class, RefexDynamic.REFEX_POLYMORPHIC_DT.getUuids()[0]),
	UNKNOWN(Byte.MAX_VALUE, null, RefexDynamic.UNKNOWN_CONCEPT.getUuids()[0]);

	private int externalizedToken_;
	private Class<? extends RefexDataBI> dataClass_;
	private UUID typeConcept_;

	public static RefexDataType getFromToken(int type) throws UnsupportedOperationException {
		switch (type) {
			case 101:
				return NID;
			case 102:
				return STRING;
			case 103:
				return INTEGER;
			case 104:
				return BOOLEAN;
			case 105:
				return LONG;
			case 106:
				return BYTEARRAY;
			case 107:
				return FLOAT;
			case 108:
				return DOUBLE;
			case 109:
				return UUID;
			case 110:
				return POLYMORPHIC;
			default:
				return UNKNOWN;
		}
	}
	
	RefexDataType(int externalizedToken, Class<? extends RefexDataBI> dataClass, UUID typeConcept)
	{
		externalizedToken_ = externalizedToken;
		dataClass_ = dataClass;
		typeConcept_ = typeConcept;
	}

	public int getTypeToken()
	{
		return this.externalizedToken_;
	}

	public Class<? extends RefexDataBI> getRefexMemberClass()
	{
		return dataClass_;
	}
	
	public UUID getDataTypeConcept()
	{
		return typeConcept_;
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
		if (RefexBooleanBI.class.isAssignableFrom(c)) {
			return BOOLEAN;
		}
		if (RefexLongBI.class.isAssignableFrom(c)) {
			return LONG;
		}
		if (RefexByteArrayBI.class.isAssignableFrom(c)) {
			return BYTEARRAY;
		}
		if (RefexFloatBI.class.isAssignableFrom(c)) {
			return FLOAT;
		}
		if (RefexDoubleBI.class.isAssignableFrom(c)) {
			return DOUBLE;
		}
		if (RefexUUIDBI.class.isAssignableFrom(c)) {
			return UUID;
		}
		if (RefexPolymorphicBI.class.isAssignableFrom(c)) {
			return POLYMORPHIC;
		}
		return UNKNOWN;
	}

	public static RefexDataType readType(DataInput input) throws IOException
	{
		int type = input.readByte();
		return getFromToken(type);
	}
}
