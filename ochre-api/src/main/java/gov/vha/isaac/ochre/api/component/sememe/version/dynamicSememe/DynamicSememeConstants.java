package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import java.util.UUID;

import gov.vha.isaac.ochre.api.ConceptConstant;
import gov.vha.isaac.ochre.api.ConceptConstantGroup;


public class DynamicSememeConstants 
{
	public static final UUID UNKNOWN_CONCEPT = UUID.fromString("00000000-0000-0000-C000-000000000046");
	
	public static final ConceptConstant DYNAMIC_SEMEME_DT_NID = new ConceptConstant("nid", UUID.fromString("d1a17272-9785-51aa-8bde-cc556ab32ebb")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_BOOLEAN = new ConceptConstant("boolean", UUID.fromString("08f2fb74-980d-5157-b92c-4ff1eac6a506")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_LONG = new ConceptConstant("long", UUID.fromString("dea8cdf1-de75-5991-9791-79714e4a964d")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_BYTE_ARRAY = new ConceptConstant("byte array", UUID.fromString("9a84fecf-708d-5de4-9c5f-e17973229e0f")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_FLOAT = new ConceptConstant("float", UUID.fromString("fb591801-7b37-525d-980d-98a1c63ceee0")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_DOUBLE = new ConceptConstant("double", UUID.fromString("7172e6ac-a05a-5a34-8275-aef430b18207")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_POLYMORPHIC = new ConceptConstant("polymorphic", UUID.fromString("3d634fd6-1498-5e8b-b914-e75b42018397")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_ARRAY = new ConceptConstant("array", UUID.fromString("318622e6-dd7a-5651-851d-2d5c2af85767")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_SEQUENCE = new ConceptConstant("sequence", UUID.fromString("5bfd7cfb-ca7e-584d-8672-e089dbb4e912")) {};

	//The following data types already exist, but I'm also adding them to our hierarchy for clarity
	public static final ConceptConstant DYNAMIC_SEMEME_DT_STRING = new ConceptConstant("String (foundation metadata concept)", 
			UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_INTEGER = new ConceptConstant("Signed integer (foundation metadata concept)", 
			UUID.fromString("1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c")) {};
	public static final ConceptConstant DYNAMIC_SEMEME_DT_UUID = new ConceptConstant("Universally Unique Identifier (foundation metadata concept)", 
			UUID.fromString("845274b5-9644-3799-94c6-e0ea37e7d1a4")) {};
	
	
	public static final ConceptConstantGroup COLUMN_DATA_TYPES = new ConceptConstantGroup("dynamic sememe column data types",
			UUID.fromString("61da7e50-f606-5ba0-a0df-83fd524951e7")) 
	{
		{
			addChild(DYNAMIC_SEMEME_DT_NID);
			addChild(DYNAMIC_SEMEME_DT_BOOLEAN);
			addChild(DYNAMIC_SEMEME_DT_LONG);
			addChild(DYNAMIC_SEMEME_DT_BYTE_ARRAY);
			addChild(DYNAMIC_SEMEME_DT_FLOAT);
			addChild(DYNAMIC_SEMEME_DT_DOUBLE);
			addChild(DYNAMIC_SEMEME_DT_POLYMORPHIC);
			addChild(DYNAMIC_SEMEME_DT_ARRAY);
			addChild(DYNAMIC_SEMEME_DT_STRING);
			addChild(DYNAMIC_SEMEME_DT_INTEGER);
			addChild(DYNAMIC_SEMEME_DT_UUID);
			addChild(DYNAMIC_SEMEME_DT_SEQUENCE);
		}
	};
}
