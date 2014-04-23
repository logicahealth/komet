package org.ihtsdo.otf.tcc.api.refexDynamic.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicBooleanBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicByteArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicDoubleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicFloatBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicLongBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicPolymorphicBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;


/**
 * 
 * {@link RefexDynamicDataType}
 * 
 * Most types are fairly straight forward.  NIDs and INTEGERS are identical internally, except NIDs identify concepts.
 * Polymorphic is used when the data type for a refex isn't known at refex creation time.  In this case, a user of the API
 * will have to examine type types of the actual {@link RefexDynamicDataBI} objects returned, to look at the type.
 * 
 * For all other types, the data type reported within the Refex Definition should exactly match the data type returned with 
 * a {@link RefexDynamicDataBI}.
 * 
 * {@link RefexDynamicDataBI} will never return a {@link POLYMORPHIC} type.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum RefexDynamicDataType {
	
	NID(101, RefexDynamicNidBI.class, RefexDynamic.REFEX_DT_NID.getUuids()[0], "Component Nid"),
	STRING(102, RefexDynamicStringBI.class, RefexDynamic.REFEX_DT_STRING.getUuids()[0], "String"),
	INTEGER(103, RefexDynamicIntegerBI.class, RefexDynamic.REFEX_DT_INTEGER.getUuids()[0], "Integer"),
	BOOLEAN(104, RefexDynamicBooleanBI.class, RefexDynamic.REFEX_DT_BOOLEAN.getUuids()[0], "Boolean"),
	LONG(105, RefexDynamicLongBI.class, RefexDynamic.REFEX_DT_LONG.getUuids()[0], "Long"),
	BYTEARRAY(106, RefexDynamicByteArrayBI.class, RefexDynamic.REFEX_DT_BYTE_ARRAY.getUuids()[0], "Arbitrary Data"),
	FLOAT(107, RefexDynamicFloatBI.class, RefexDynamic.REFEX_DT_FLOAT.getUuids()[0], "Float"),
	DOUBLE(108, RefexDynamicDoubleBI.class, RefexDynamic.REFEX_DT_DOUBLE.getUuids()[0], "Double"),
	UUID(109, RefexDynamicUUIDBI.class, RefexDynamic.REFEX_DT_UUID.getUuids()[0], "Component UUID"),
	POLYMORPHIC(110, RefexDynamicPolymorphicBI.class, RefexDynamic.REFEX_DT_POLYMORPHIC.getUuids()[0], "Unspecified"),
	UNKNOWN(Byte.MAX_VALUE, null, RefexDynamic.UNKNOWN_CONCEPT.getUuids()[0], "Unknown");

	private int externalizedToken_;
	private Class<? extends RefexDynamicDataBI> dataClass_;
	private UUID typeConcept_;
	private String displayName_;

	public static RefexDynamicDataType getFromToken(int type) throws UnsupportedOperationException {
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
	
	private RefexDynamicDataType(int externalizedToken, Class<? extends RefexDynamicDataBI> dataClass, UUID typeConcept, String displayName)
	{
		externalizedToken_ = externalizedToken;
		dataClass_ = dataClass;
		typeConcept_ = typeConcept;
		displayName_ = displayName;
	}

	public int getTypeToken()
	{
		return this.externalizedToken_;
	}

	public Class<? extends RefexDynamicDataBI> getRefexMemberClass()
	{
		return dataClass_;
	}
	
	public UUID getDataTypeConcept()
	{
		return typeConcept_;
	}
	
	public String getDisplayName()
	{
		return displayName_;
	}

	public void writeType(DataOutput output) throws IOException
	{
		output.writeByte(externalizedToken_);
	}

	public static RefexDynamicDataType classToType(Class<?> c) 
	{
		if (RefexDynamicNidBI.class.isAssignableFrom(c)) {
			return NID;
		}
		if (RefexDynamicStringBI.class.isAssignableFrom(c)) {
			return STRING;
		}
		if (RefexDynamicIntegerBI.class.isAssignableFrom(c)) {
			return INTEGER;
		}
		if (RefexDynamicBooleanBI.class.isAssignableFrom(c)) {
			return BOOLEAN;
		}
		if (RefexDynamicLongBI.class.isAssignableFrom(c)) {
			return LONG;
		}
		if (RefexDynamicByteArrayBI.class.isAssignableFrom(c)) {
			return BYTEARRAY;
		}
		if (RefexDynamicFloatBI.class.isAssignableFrom(c)) {
			return FLOAT;
		}
		if (RefexDynamicDoubleBI.class.isAssignableFrom(c)) {
			return DOUBLE;
		}
		if (RefexDynamicUUIDBI.class.isAssignableFrom(c)) {
			return UUID;
		}
		if (RefexDynamicPolymorphicBI.class.isAssignableFrom(c)) {
			return POLYMORPHIC;
		}
		return UNKNOWN;
	}

	public static RefexDynamicDataType readType(DataInput input) throws IOException
	{
		int type = input.readByte();
		return getFromToken(type);
	}
}
