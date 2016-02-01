package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.*;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;


/**
 * 
 * {@link DynamicSememeDataType}
 * 
 * Most types are fairly straight forward.  NIDs, SEQUQENCES and INTEGERS are identical internally.
 * Polymorphic is used when the data type for a dynamic sememe isn't known at dynamic sememe creation time.  In this case, a user of the API
 * will have to examine type types of the actual {@link DynamicSememeData} objects returned, to look at the type.
 * 
 * For all other types, the data type reported within the Refex Definition should exactly match the data type returned with 
 * a {@link DynamicSememeData}.
 * 
 * {@link DynamicSememeData} will never return a {@link POLYMORPHIC} type.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum DynamicSememeDataType {
	
	NID(101, DynamicSememeNid.class, "Component Nid"),
	STRING(102, DynamicSememeString.class, "String"),
	INTEGER(103, DynamicSememeInteger.class, "Integer"),
	BOOLEAN(104, DynamicSememeBoolean.class, "Boolean"),
	LONG(105, DynamicSememeLong.class, "Long"),
	BYTEARRAY(106, DynamicSememeByteArray.class, "Arbitrary Data"),
	FLOAT(107, DynamicSememeFloat.class, "Float"),
	DOUBLE(108, DynamicSememeDouble.class, "Double"),
	UUID(109, DynamicSememeUUID.class, "UUID"),
	POLYMORPHIC(110, DynamicSememePolymorphic.class, "Unspecified"),
	ARRAY(111, DynamicSememeArray.class, "Array"),
	SEQUENCE(112, DynamicSememeSequence.class, "Component Sequence"),
	UNKNOWN(Byte.MAX_VALUE, null, "Unknown");

	private int externalizedToken_;
	private Class<? extends DynamicSememeData> dataClass_;
	private String displayName_;

	public static DynamicSememeDataType getFromToken(int type) throws UnsupportedOperationException {
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
			case 111:
				return ARRAY;
			case 112:
				return SEQUENCE;
			default:
				return UNKNOWN;
		}
	}
	
	private DynamicSememeDataType(int externalizedToken, Class<? extends DynamicSememeData> dataClass, String displayName)
	{
		externalizedToken_ = externalizedToken;
		dataClass_ = dataClass;
		displayName_ = displayName;
	}

	public int getTypeToken()
	{
		return this.externalizedToken_;
	}

	public Class<? extends DynamicSememeData> getDynamicSememeMemberClass()
	{
		return dataClass_;
	}
	
	public UUID getDataTypeConcept()
	{
		/*
		 * Implementation note - these used to be defined in the constructor, and stored in a local variable - but
		 * that lead to a circular loop between the references of static elements in this class and DynamicSememe, 
		 * specifically in the constructors - which would throw maven / surefire for a loop - resulting in a 
		 * class not found exception... which was a PITA to track down.  So, don't do that....
		 */
		switch (this)
		{
			case BOOLEAN: return DynamicSememeConstants.get().get().DYNAMIC_SEMEME_DT_BOOLEAN.getUUID();
			case BYTEARRAY: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_BYTE_ARRAY.getUUID();
			case DOUBLE: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_DOUBLE.getUUID();
			case FLOAT: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_FLOAT.getUUID();
			case INTEGER: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_INTEGER.getUUID();
			case LONG: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_LONG.getUUID();
			case NID: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_NID.getUUID();
			case POLYMORPHIC: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_POLYMORPHIC.getUUID();
			case STRING: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_STRING.getUUID();
			case UNKNOWN: return DynamicSememeConstants.get().UNKNOWN_CONCEPT;
			case UUID: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_UUID.getUUID();
			case ARRAY: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_ARRAY.getUUID();
			case SEQUENCE: return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_SEQUENCE.getUUID();

			default: throw new RuntimeException("Implementation error");
		}
	}
	
	public String getDisplayName()
	{
		return displayName_;
	}

	public static DynamicSememeDataType classToType(Class<?> c) 
	{
		if (DynamicSememeNid.class.isAssignableFrom(c)) {
			return NID;
		}
		if (DynamicSememeString.class.isAssignableFrom(c)) {
			return STRING;
		}
		if (DynamicSememeInteger.class.isAssignableFrom(c)) {
			return INTEGER;
		}
		if (DynamicSememeBoolean.class.isAssignableFrom(c)) {
			return BOOLEAN;
		}
		if (DynamicSememeLong.class.isAssignableFrom(c)) {
			return LONG;
		}
		if (DynamicSememeByteArray.class.isAssignableFrom(c)) {
			return BYTEARRAY;
		}
		if (DynamicSememeFloat.class.isAssignableFrom(c)) {
			return FLOAT;
		}
		if (DynamicSememeDouble.class.isAssignableFrom(c)) {
			return DOUBLE;
		}
		if (DynamicSememeUUID.class.isAssignableFrom(c)) {
			return UUID;
		}
		if (DynamicSememePolymorphic.class.isAssignableFrom(c)) {
			return POLYMORPHIC;
		}
		if (DynamicSememeArray.class.isAssignableFrom(c)) {
			return ARRAY;
		}
		if (DynamicSememeSequence.class.isAssignableFrom(c)) {
			return SEQUENCE;
		}
		LogManager.getLogger().warn("Couldn't map class {} to type!", c);
		return UNKNOWN;
	}
}
