package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArrayBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeBooleanBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeByteArrayBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDoubleBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloatBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeIntegerBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLongBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNidBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememePolymorphicBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeSequenceBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeStringBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUIDBI;
import gov.vha.isaac.ochre.api.constants.IsaacMetadataConstantsBase;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;


/**
 * 
 * {@link DynamicSememeDataType}
 * 
 * Most types are fairly straight forward.  NIDs, SEQUQENCES and INTEGERS are identical internally.
 * Polymorphic is used when the data type for a dynamic sememe isn't known at dynamic sememe creation time.  In this case, a user of the API
 * will have to examine type types of the actual {@link DynamicSememeDataBI} objects returned, to look at the type.
 * 
 * For all other types, the data type reported within the Refex Definition should exactly match the data type returned with 
 * a {@link DynamicSememeDataBI}.
 * 
 * {@link DynamicSememeDataBI} will never return a {@link POLYMORPHIC} type.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum DynamicSememeDataType {
	
	NID(101, DynamicSememeNidBI.class, "Component Nid"),
	STRING(102, DynamicSememeStringBI.class, "String"),
	INTEGER(103, DynamicSememeIntegerBI.class, "Integer"),
	BOOLEAN(104, DynamicSememeBooleanBI.class, "Boolean"),
	LONG(105, DynamicSememeLongBI.class, "Long"),
	BYTEARRAY(106, DynamicSememeByteArrayBI.class, "Arbitrary Data"),
	FLOAT(107, DynamicSememeFloatBI.class, "Float"),
	DOUBLE(108, DynamicSememeDoubleBI.class, "Double"),
	UUID(109, DynamicSememeUUIDBI.class, "UUID"),
	POLYMORPHIC(110, DynamicSememePolymorphicBI.class, "Unspecified"),
	ARRAY(111, DynamicSememeArrayBI.class, "Array"),
	SEQUENCE(112, DynamicSememeSequenceBI.class, "Component Sequence"),
	UNKNOWN(Byte.MAX_VALUE, null, "Unknown");

	private int externalizedToken_;
	private Class<? extends DynamicSememeDataBI> dataClass_;
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
	
	private DynamicSememeDataType(int externalizedToken, Class<? extends DynamicSememeDataBI> dataClass, String displayName)
	{
		externalizedToken_ = externalizedToken;
		dataClass_ = dataClass;
		displayName_ = displayName;
	}

	public int getTypeToken()
	{
		return this.externalizedToken_;
	}

	public Class<? extends DynamicSememeDataBI> getDynamicSememeMemberClass()
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
			case BOOLEAN: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_BOOLEAN.getUUID();
			case BYTEARRAY: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_BYTE_ARRAY.getUUID();
			case DOUBLE: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_DOUBLE.getUUID();
			case FLOAT: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_FLOAT.getUUID();
			case INTEGER: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_INTEGER.getUUID();
			case LONG: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_LONG.getUUID();
			case NID: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_NID.getUUID();
			case POLYMORPHIC: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_POLYMORPHIC.getUUID();
			case STRING: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_STRING.getUUID();
			case UNKNOWN: return IsaacMetadataConstantsBase.UNKNOWN_CONCEPT;
			case UUID: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_UUID.getUUID();
			case ARRAY: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_ARRAY.getUUID();
			case SEQUENCE: return IsaacMetadataConstantsBase.DYNAMIC_SEMEME_DT_SEQUENCE.getUUID();

			default: throw new RuntimeException("Implementation error");
		}
	}
	
	public String getDisplayName()
	{
		return displayName_;
	}

	public static DynamicSememeDataType classToType(Class<?> c) 
	{
		if (DynamicSememeNidBI.class.isAssignableFrom(c)) {
			return NID;
		}
		if (DynamicSememeStringBI.class.isAssignableFrom(c)) {
			return STRING;
		}
		if (DynamicSememeIntegerBI.class.isAssignableFrom(c)) {
			return INTEGER;
		}
		if (DynamicSememeBooleanBI.class.isAssignableFrom(c)) {
			return BOOLEAN;
		}
		if (DynamicSememeLongBI.class.isAssignableFrom(c)) {
			return LONG;
		}
		if (DynamicSememeByteArrayBI.class.isAssignableFrom(c)) {
			return BYTEARRAY;
		}
		if (DynamicSememeFloatBI.class.isAssignableFrom(c)) {
			return FLOAT;
		}
		if (DynamicSememeDoubleBI.class.isAssignableFrom(c)) {
			return DOUBLE;
		}
		if (DynamicSememeUUIDBI.class.isAssignableFrom(c)) {
			return UUID;
		}
		if (DynamicSememePolymorphicBI.class.isAssignableFrom(c)) {
			return POLYMORPHIC;
		}
		if (DynamicSememeArrayBI.class.isAssignableFrom(c)) {
			return ARRAY;
		}
		if (DynamicSememeSequenceBI.class.isAssignableFrom(c)) {
			return SEQUENCE;
		}
		LogManager.getLogger().warn("Couldn't map class {} to type!", c);
		return UNKNOWN;
	}
}
