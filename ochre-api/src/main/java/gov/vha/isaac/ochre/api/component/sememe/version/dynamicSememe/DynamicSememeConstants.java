package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import java.util.UUID;
import gov.vha.isaac.ochre.api.MetadataConceptConstant;
import gov.vha.isaac.ochre.api.MetadataConceptConstantGroup;
import gov.vha.isaac.ochre.api.MetadataDynamicSememeConstant;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;


public class DynamicSememeConstants 
{
	public static final UUID UNKNOWN_CONCEPT = UUID.fromString("00000000-0000-0000-C000-000000000046");
	
	//Set up all of the data type columns
	
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_NID = new MetadataConceptConstant("nid", UUID.fromString("d1a17272-9785-51aa-8bde-cc556ab32ebb")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_BOOLEAN = new MetadataConceptConstant("boolean", UUID.fromString("08f2fb74-980d-5157-b92c-4ff1eac6a506")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_LONG = new MetadataConceptConstant("long", UUID.fromString("dea8cdf1-de75-5991-9791-79714e4a964d")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_BYTE_ARRAY = new MetadataConceptConstant("byte array", UUID.fromString("9a84fecf-708d-5de4-9c5f-e17973229e0f")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_FLOAT = new MetadataConceptConstant("float", UUID.fromString("fb591801-7b37-525d-980d-98a1c63ceee0")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_DOUBLE = new MetadataConceptConstant("double", UUID.fromString("7172e6ac-a05a-5a34-8275-aef430b18207")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_POLYMORPHIC = new MetadataConceptConstant("polymorphic", UUID.fromString("3d634fd6-1498-5e8b-b914-e75b42018397")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_ARRAY = new MetadataConceptConstant("array", UUID.fromString("318622e6-dd7a-5651-851d-2d5c2af85767")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_SEQUENCE = new MetadataConceptConstant("sequence", UUID.fromString("5bfd7cfb-ca7e-584d-8672-e089dbb4e912")) {};

	//The following data types already exist, but I'm also adding them to our hierarchy for clarity
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_STRING = new MetadataConceptConstant("String (foundation metadata concept)",
			"String",
			UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_INTEGER = new MetadataConceptConstant("Signed integer (foundation metadata concept)",
			"Signed integer",
			UUID.fromString("1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c")) {};
	public static final MetadataConceptConstant DYNAMIC_SEMEME_DT_UUID = new MetadataConceptConstant("Universally Unique Identifier (foundation metadata concept)",
			"Universally Unique Identifier",
			UUID.fromString("845274b5-9644-3799-94c6-e0ea37e7d1a4")) {};
	
	
	//Place them all under this organization concept
	public static final MetadataConceptConstantGroup COLUMN_DATA_TYPES = new MetadataConceptConstantGroup("dynamic sememe column data types",
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
	
	//Set up other metadata
	
	//used as salt for generating other UUIDs
	public static final MetadataConceptConstant DYNAMIC_SEMEME_NAMESPACE = new MetadataConceptConstant ("dynamic sememe namespace", 
			UUID.fromString("eb0c13ff-74fd-5987-88a0-6f5d75269e9d")) {};
			
	
	//The seven column types we need for describing column types
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_ORDER = new MetadataConceptConstant("column order", 
			UUID.fromString("8c501747-846a-5cea-8fd6-c9dd3dfc674f"),
			"Stores the column order of this column within a Dynamic Sememe Definition") {};
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_NAME = new MetadataConceptConstant("column name", 
			UUID.fromString("89c0ded2-fd69-5654-a386-ded850d258a1"),
			"Stores the concept reference to the concept that defines the name of this column within a Dynamic Sememe Definition") {};
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_TYPE = new MetadataConceptConstant("column type", 
			UUID.fromString("dbfd9bd2-b84f-574a-ab9e-64ba3bb94793"),
			"Stores the data type of this column within a Dynamic Sememe Definition") {};
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE = new MetadataConceptConstant("column default value", 
			UUID.fromString("4d3e79aa-ab74-5858-beb3-15e0888986cb"),
			"Stores the (optional) default value of this column within a Dynamic Sememe Definition") {};
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_REQUIRED = new MetadataConceptConstant("column required", 
			UUID.fromString("8a89ef19-bd5a-5e25-aa57-1172fbb437b6"),
			"Stores the (optional) flag to specify that this column is manditory within a Dynamic Sememe Definition") {};
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_VALIDATOR = new MetadataConceptConstant("column validator", 
			UUID.fromString("f295c3ba-d416-563d-8427-8b5d3e324192"),
			"Stores the (optional) validator type which will be applied to user supplied data of this column within a Dynamic Sememe Definition") {};
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_VALIDATOR_DATA = new MetadataConceptConstant("column validator data", 
			UUID.fromString("50ea8378-8355-5a5d-bae2-ce7c10e92636"),
			"Stores the (optional) validator data which will be used by the validator to check the user input of this column within a Dynamic Sememe Definition") {};
	
	//used for index config
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_COLUMNS_TO_INDEX = new MetadataConceptConstant("columns to index", 
			UUID.fromString("cede7677-3759-5dce-b28b-20a40fddf5d6"),
			"Contains a String which has a comma seperated list of the column positions within the referenced sememe assemblage which should have their values indexed.") {};
	
	//Used for referenced component type restrictions
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_TYPE = new MetadataConceptConstant("referenced component type restriction", 
			UUID.fromString("902f97b6-2ef4-59d7-b6f9-01278a00061c"),
			"Stores the (optional) referenced component type restriction selection which will be used by the validator to check the user input for the "
					+ "referenced component when creating an instance of a dynamic sememe") {};
	
	//Convenience column type for refex instances that just wish to attach a single column of data, and don't want to create another concept
	//to represent the column name.  Typically only used when defining refexes where there is a single column of attached data (typically - attaching an attribute,
	//the column represents the value, while the type of the attribute is represented by the refex type itself - so the column name isn't really necessary)
	public static MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_VALUE = new MetadataConceptConstant("value", 
			UUID.fromString("d94e271f-0e9b-5159-8691-6c29c7689ffb"),
			"The attached value of the sememe") {};
			
	//parent concept for all of the column info
	//An organizational concept which serves as a parent concept for any column types that are defined
	//within the system.
	public static MetadataConceptConstantGroup DYNAMIC_SEMEME_COLUMNS = new MetadataConceptConstantGroup("dynamic sememe columns", 
			UUID.fromString("46ddb9a2-0e10-586a-8b54-8e66333e9b77")) 
	{
		{
			addChild(DYNAMIC_SEMEME_COLUMN_ORDER);
			addChild(DYNAMIC_SEMEME_COLUMN_NAME);
			addChild(DYNAMIC_SEMEME_COLUMN_TYPE);
			addChild(DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE);
			addChild(DYNAMIC_SEMEME_COLUMN_REQUIRED);
			addChild(DYNAMIC_SEMEME_COLUMN_VALIDATOR);
			addChild(DYNAMIC_SEMEME_COLUMN_VALIDATOR_DATA);
			addChild(DYNAMIC_SEMEME_COLUMN_COLUMNS_TO_INDEX);
			addChild(DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_TYPE);
			addChild(DYNAMIC_SEMEME_COLUMN_VALUE);
		}
	};
	
	//This is the assemblage type that is optionally attached to an assemblage itself, to declare type restrictions on the referenced component
	//of the refex
	//TODO (artf231825) - [refex] it would have been useful to be able to declare a regexp restriction on the values of this column (and probably others) but can't do it, 
	//because I don't have any of the implementations on the classpath here.  something else to rethink?
	public static MetadataDynamicSememeConstant DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION = new MetadataDynamicSememeConstant("dynamic sememe referenced component restriction", 
			UUID.fromString("0d94ceeb-e24f-5f1a-84b2-1ac35f671db5"),
			"This concept is used as an assemblage for defining new Sememe extensions.  It annotates other sememe extensions to restrict the usage of a "
				+ " sememe to a particular Component Type (Concept, Description, etc).  The attached data column specifies the allowed Component Type",
			new DynamicSememeColumnInfo[] {
				new DynamicSememeColumnInfo(0, DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_TYPE.getUUID(), DynamicSememeDataType.STRING, null, true, null, null)},
			new Integer[] {0}) {};
	
	//an organizational concept for all of the metadata concepts being added for dynamic sememe 
	public static MetadataConceptConstantGroup DYNAMIC_SEMEME_METADATA = new MetadataConceptConstantGroup("dynamic sememe metadata", 
			UUID.fromString("9769773c-7b70-523d-8fc5-b16621ffa57c"))
	{
		{
			addChild(DYNAMIC_SEMEME_NAMESPACE);
			addChild(DYNAMIC_SEMEME_COLUMNS);
			addChild(DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION);
		}
	};
	
	
	
	
	

	
	
	//Set up the Dynamic Sememes that we require for Dynamic Sememes themselves.
	
	//This is the assemblage type that is usually present on a concept when it is used as an assemblage itself to describe the attached data - the attached
	//refex using this for an assemblage will describe a data column that is to be attached with the refex.  This assemblage type wouldn't be used if there was 
	//no data to attach.
	public static MetadataDynamicSememeConstant DYNAMIC_SEMEME_EXTENSION_DEFINITION = new MetadataDynamicSememeConstant("dynamic sememe extension definition", 
			UUID.fromString("406e872b-2e19-5f5e-a71d-e4e4b2c68fe5"),
			"This concept is used as an assemblage for defining new Sememe extensions.  "
				+ "The attached data columns describe what columns are required to define a new Sememe.",
			new DynamicSememeColumnInfo[] {
				new DynamicSememeColumnInfo(0, DYNAMIC_SEMEME_COLUMN_ORDER.getUUID(), DynamicSememeDataType.INTEGER, null, true, null, null),
				new DynamicSememeColumnInfo(1, DYNAMIC_SEMEME_COLUMN_NAME.getUUID(), DynamicSememeDataType.UUID, null, true, null, null),
				new DynamicSememeColumnInfo(2, DYNAMIC_SEMEME_COLUMN_TYPE.getUUID(), DynamicSememeDataType.STRING, null, true, null, null),
				new DynamicSememeColumnInfo(3, DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getUUID(), DynamicSememeDataType.POLYMORPHIC, null, false, null, null),
				new DynamicSememeColumnInfo(4, DYNAMIC_SEMEME_COLUMN_REQUIRED.getUUID(), DynamicSememeDataType.BOOLEAN, null, false, null, null),
				new DynamicSememeColumnInfo(5, DYNAMIC_SEMEME_COLUMN_VALIDATOR.getUUID(), DynamicSememeDataType.STRING, null, false, null, null),
				new DynamicSememeColumnInfo(6, DYNAMIC_SEMEME_COLUMN_VALIDATOR_DATA.getUUID(), DynamicSememeDataType.POLYMORPHIC, null, false, null, null)
				},
			null) {};
	
			
	//This is the extended description type that must be attached to a description within a concept to make the concept valid for use as an 
	//assemblage concept for DynamicSememe refexes.  The description annotated with this type describes the intent of 
	//using the concept containing the description as an assemblage concept.
	public static MetadataDynamicSememeConstant DYNAMIC_SEMEME_DEFINITION_DESCRIPTION = new MetadataDynamicSememeConstant("dynamic sememe definition description", 
			null,
			UUID.fromString("b0372953-4f20-58b8-ad04-20c2239c7d4e"),
			"This is the extended description type that must be attached to a description within a concept to make the concept valid for use as an "
				+ "assemblage concept for a Dynamic Sememe.  The description annotated with this type describes the intent of "
				+ "using the concept containing the description as an assemblage concept.", 
			new DynamicSememeColumnInfo[0],
			null,
			null,
			ObjectChronologyType.SEMEME,
			SememeType.DESCRIPTION,
			null){};
			
	//This is the assemblage type that is used to record the current configuration of the Indexer for Dynamic Sememes..
	//this is ALSO the concept used as the referenced component dynamic sememe instances (of assemblage type itself) which define which other 
	//dynamic sememes should be indexed within the system. 
	public static MetadataDynamicSememeConstant DYNAMIC_SEMEME_INDEX_CONFIGURATION = new MetadataDynamicSememeConstant("dynamic sememe index configuration", 
			UUID.fromString("a5d187a7-3d95-5694-b2eb-a48d94cb0698"),
			"A Dynamic Sememe which contains the indexer configuration for Dynamic Sememes within ISAAC.  "
				+ "The referenced component ID will be the assemblage being configured for indexing.", 
			new DynamicSememeColumnInfo[] {
				new DynamicSememeColumnInfo(0, DYNAMIC_SEMEME_COLUMN_COLUMNS_TO_INDEX.getUUID(), DynamicSememeDataType.STRING, null, false, null, null)},
			null) {};
	
	
	//An organizational concept which serves as a parent concept for dynamic refexes defined in the system
	//(unless they choose to put them some where else, this isn't required, is only for convenience)
	public static MetadataConceptConstantGroup DYNAMIC_SEMEME_ASSEMBLAGES = new MetadataConceptConstantGroup("dynamic sememe assemblages", 
			UUID.fromString("e18265b7-5406-52b6-baf0-4cfb867829b4"))
		{
			{
				addChild(DYNAMIC_SEMEME_EXTENSION_DEFINITION);
				addChild(DYNAMIC_SEMEME_DEFINITION_DESCRIPTION);
				addChild(DYNAMIC_SEMEME_INDEX_CONFIGURATION);
			}
		};
	
	
}
