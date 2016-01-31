package gov.vha.isaac.ochre.api.constants;

/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.UUID;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;


/**
 * Constants relating to Dynamic Sememes
 * @author darmbrust
 * 
 * Unfortunately, due to the use of the LookupService within this class - and the class itself being provided by a LookupService, 
 * we cannot create these constants as static - it leads to recusion in the LookupService init which breaks things.
 */

@Service
@Singleton
public class DynamicSememeConstants implements ModuleProvidedConstants
{
	private DynamicSememeConstants()
	{
		//making this class impossible to construct outside of HK2
	}
	
	public static DynamicSememeConstants get()
	{
		return LookupService.getService(DynamicSememeConstants.class);
	}
	
	public final UUID UNKNOWN_CONCEPT = UUID.fromString("00000000-0000-0000-C000-000000000046");
	//Set up all of the data type columns
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_NID = new MetadataConceptConstant("nid", UUID.fromString("d1a17272-9785-51aa-8bde-cc556ab32ebb")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_BOOLEAN = new MetadataConceptConstant("boolean", UUID.fromString("08f2fb74-980d-5157-b92c-4ff1eac6a506")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_LONG = new MetadataConceptConstant("long", UUID.fromString("dea8cdf1-de75-5991-9791-79714e4a964d")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_BYTE_ARRAY = new MetadataConceptConstant("byte array", UUID.fromString("9a84fecf-708d-5de4-9c5f-e17973229e0f")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_FLOAT = new MetadataConceptConstant("float", UUID.fromString("fb591801-7b37-525d-980d-98a1c63ceee0")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_DOUBLE = new MetadataConceptConstant("double", UUID.fromString("7172e6ac-a05a-5a34-8275-aef430b18207")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_POLYMORPHIC = new MetadataConceptConstant("polymorphic", UUID.fromString("3d634fd6-1498-5e8b-b914-e75b42018397")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_ARRAY = new MetadataConceptConstant("array", UUID.fromString("318622e6-dd7a-5651-851d-2d5c2af85767")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_SEQUENCE = new MetadataConceptConstant("sequence", UUID.fromString("5bfd7cfb-ca7e-584d-8672-e089dbb4e912")) {};
	
	//The following data types already exist, but I'm also adding them to our hierarchy for clarity
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_STRING = new MetadataConceptConstant("String (foundation metadata concept)",
		"String",
		UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_INTEGER = new MetadataConceptConstant("Signed integer (foundation metadata concept)",
		"Signed integer",
		UUID.fromString("1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c")) {};
	public final MetadataConceptConstant DYNAMIC_SEMEME_DT_UUID = new MetadataConceptConstant("Universally Unique Identifier (foundation metadata concept)",
		"Universally Unique Identifier",
		UUID.fromString("845274b5-9644-3799-94c6-e0ea37e7d1a4")) {};
		
	public final MetadataConceptConstantGroup DYNAMIC_SEMEME_COLUMN_DATA_TYPES = new MetadataConceptConstantGroup("dynamic sememe column data types",
			UUID.fromString("61da7e50-f606-5ba0-a0df-83fd524951e7")) {
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
	//used as salt for generating other UUIDs.
	public final MetadataConceptConstant DYNAMIC_SEMEME_NAMESPACE = new MetadataConceptConstant("dynamic sememe namespace",
			UUID.fromString("eb0c13ff-74fd-5987-88a0-6f5d75269e9d")) {
	};

	//The seven column types we need for describing column types
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_ORDER = new MetadataConceptConstant("column order",
			UUID.fromString("8c501747-846a-5cea-8fd6-c9dd3dfc674f"),
			"Stores the column order of this column within a Dynamic Sememe Definition") {
	};
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_NAME = new MetadataConceptConstant("column name",
			UUID.fromString("89c0ded2-fd69-5654-a386-ded850d258a1"),
			"Stores the concept reference to the concept that defines the name of this column within a Dynamic Sememe Definition") {
	};
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_TYPE = new MetadataConceptConstant("column type",
			UUID.fromString("dbfd9bd2-b84f-574a-ab9e-64ba3bb94793"),
			"Stores the data type of this column within a Dynamic Sememe Definition") {
	};
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE = new MetadataConceptConstant("column default value",
			UUID.fromString("4d3e79aa-ab74-5858-beb3-15e0888986cb"),
			"Stores the (optional) default value of this column within a Dynamic Sememe Definition") {
	};
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_REQUIRED = new MetadataConceptConstant("column required",
			UUID.fromString("8a89ef19-bd5a-5e25-aa57-1172fbb437b6"),
			"Stores the (optional) flag to specify that this column is manditory within a Dynamic Sememe Definition") {
	};
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_VALIDATOR = new MetadataConceptConstant("column validator",
			UUID.fromString("f295c3ba-d416-563d-8427-8b5d3e324192"),
			"Stores the (optional) validator type which will be applied to user supplied data of this column within a Dynamic Sememe Definition") {
	};

	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_VALIDATOR_DATA = new MetadataConceptConstant("column validator data",
			UUID.fromString("50ea8378-8355-5a5d-bae2-ce7c10e92636"),
			"Stores the (optional) validator data which will be used by the validator to check the user input of this column within a Dynamic Sememe Definition") {
	};

	//used for index config
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_COLUMNS_TO_INDEX = new MetadataConceptConstant("columns to index",
			UUID.fromString("cede7677-3759-5dce-b28b-20a40fddf5d6"),
			"Contains an array of integers that denote the column positions within the referenced sememe assemblage which should have their values indexed.") {
	};

	//Used for referenced component type restrictions
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_TYPE = new MetadataConceptConstant("referenced component type restriction",
			UUID.fromString("902f97b6-2ef4-59d7-b6f9-01278a00061c"),
			"Stores the (optional) referenced component type restriction selection which will be used by the validator to check the user input for the "
			+ "referenced component when creating an instance of a dynamic sememe") {
	};

	//Used for referenced component sub-type restrictions
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_SUBTYPE = new MetadataConceptConstant("referenced component subtype restriction",
			UUID.fromString("8af1045e-1122-5072-9f29-ce7da9337915"),
			"Stores the (optional) referenced component type sub restriction selection which will be used by the validator to check the user input for the "
			+ "referenced component when creating an instance of a dynamic sememe.") {
	};

	//Convenience column type for refex instances that just wish to attach a single column of data, and don't want to create another concept
	//to represent the column name.  Typically only used when defining refexes where there is a single column of attached data (typically - attaching an attribute,
	//the column represents the value, while the type of the attribute is represented by the refex type itself - so the column name isn't really necessary)
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_VALUE = new MetadataConceptConstant("value",
			UUID.fromString("d94e271f-0e9b-5159-8691-6c29c7689ffb"),
			"The attached value of the sememe") {
	};

	//2 columns for a comments sememe
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_EDITOR_COMMENT = new MetadataConceptConstant("editor comment",
			UUID.fromString("2b38b1a9-ce6e-5be2-8885-65cd76f40929"),
			"Stores the comment created by the editor") {
	};

	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_EDITOR_COMMENT_CONTEXT = new MetadataConceptConstant("editor comment context",
			UUID.fromString("2e4187ca-ba45-5a87-8484-1f86801a331a"),
			"Stores an optional value that may be used to group comments, such as 'mapping comment' or 'assertion comment' which"
			+ " then would allow programmatic filtering of comments to be context specific.") {
	};

	//A column to store the target of an association within a sememe
	public final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT = new MetadataConceptConstant("target",
			UUID.fromString("e598e12f-3d39-56ac-be68-4e9fca98fb7a"),
			"Stores the (optional) target concept or component of an association or mapping") {
	};

	//parent concept for all of the column info
	//An organizational concept which serves as a parent concept for any column types that are defined
	//within the system.
	public final MetadataConceptConstantGroup DYNAMIC_SEMEME_COLUMNS = new MetadataConceptConstantGroup("dynamic sememe columns",
			UUID.fromString("46ddb9a2-0e10-586a-8b54-8e66333e9b77")) {
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
			addChild(DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_SUBTYPE);
			addChild(DYNAMIC_SEMEME_COLUMN_VALUE);
			addChild(DYNAMIC_SEMEME_COLUMN_EDITOR_COMMENT);
			addChild(DYNAMIC_SEMEME_COLUMN_EDITOR_COMMENT_CONTEXT);
			addChild(DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT);
		}
	};

	//This is the assemblage type that is optionally attached to an assemblage itself, to declare type restrictions on the referenced component
	//of the sememe
	public final MetadataDynamicSememeConstant DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION = new MetadataDynamicSememeConstant("dynamic sememe referenced component restriction",
			UUID.fromString("0d94ceeb-e24f-5f1a-84b2-1ac35f671db5"),
			"This concept is used as an assemblage for defining new Sememe extensions.  It annotates other sememe extensions to restrict the usage of a "
			+ " sememe to a particular Component Type (Concept, Description, etc).  The attached data column specifies the allowed Component Type",
			new DynamicSememeColumnInfo[]{
				new DynamicSememeColumnInfo(0, DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_TYPE.getUUID(), DynamicSememeDataType.STRING, null, true,
						new DynamicSememeValidatorType[]{DynamicSememeValidatorType.REGEXP},
						new DynamicSememeString[]{LookupService.getService(DynamicSememeUtility.class).createDynamicStringData(
								ObjectChronologyType.CONCEPT.name() + "|" + ObjectChronologyType.SEMEME)}, false),
				new DynamicSememeColumnInfo(1, DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_SUBTYPE.getUUID(), DynamicSememeDataType.STRING, null, false,
						new DynamicSememeValidatorType[]{DynamicSememeValidatorType.REGEXP},
						new DynamicSememeString[]{LookupService.getService(DynamicSememeUtility.class).createDynamicStringData(
									SememeType.COMPONENT_NID.name() + "|"
									+ SememeType.DESCRIPTION.name() + "|"
									+ SememeType.DYNAMIC.name() + "|"
									+ SememeType.LOGIC_GRAPH.name() + "|"
									+ SememeType.LONG.name() + "|"
									+ SememeType.MEMBER.name() + "|"
									+ SememeType.RELATIONSHIP_ADAPTOR.name() + "|"
									+ SememeType.STRING.name())}, false)}) {
	};

	//an organizational concept for all of the metadata concepts being added for dynamic sememe 
	public final MetadataConceptConstantGroup DYNAMIC_SEMEME_METADATA = new MetadataConceptConstantGroup("dynamic sememe metadata",
			UUID.fromString("9769773c-7b70-523d-8fc5-b16621ffa57c")) {
		{
			addChild(DYNAMIC_SEMEME_NAMESPACE);
			addChild(DYNAMIC_SEMEME_COLUMNS);
			addChild(DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION);
			addChild(DYNAMIC_SEMEME_COLUMN_DATA_TYPES);
			//no parent, this can attach to root
		}
	};

	//Set up the Dynamic Sememes that we require for Dynamic Sememes themselves.
	//This is the assemblage type that is usually present on a concept when it is used as an assemblage itself to describe the attached data - the attached
	//refex using this for an assemblage will describe a data column that is to be attached with the refex.  This assemblage type wouldn't be used if there was 
	//no data to attach.
	public final MetadataDynamicSememeConstant DYNAMIC_SEMEME_EXTENSION_DEFINITION = new MetadataDynamicSememeConstant("dynamic sememe extension definition",
			UUID.fromString("406e872b-2e19-5f5e-a71d-e4e4b2c68fe5"),
			"This concept is used as an assemblage for defining new Sememe extensions.  "
			+ "The attached data columns describe what columns are required to define a new Sememe.",
			new DynamicSememeColumnInfo[]{
				new DynamicSememeColumnInfo(0, DYNAMIC_SEMEME_COLUMN_ORDER.getUUID(), DynamicSememeDataType.INTEGER, null, true, false),
				new DynamicSememeColumnInfo(1, DYNAMIC_SEMEME_COLUMN_NAME.getUUID(), DynamicSememeDataType.UUID, null, true, false),
				new DynamicSememeColumnInfo(2, DYNAMIC_SEMEME_COLUMN_TYPE.getUUID(), DynamicSememeDataType.STRING, null, true, false),
				new DynamicSememeColumnInfo(3, DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getUUID(), DynamicSememeDataType.POLYMORPHIC, null, false, false),
				new DynamicSememeColumnInfo(4, DYNAMIC_SEMEME_COLUMN_REQUIRED.getUUID(), DynamicSememeDataType.BOOLEAN, null, false, false),
				new DynamicSememeColumnInfo(5, DYNAMIC_SEMEME_COLUMN_VALIDATOR.getUUID(), DynamicSememeDataType.ARRAY, null, false,
						DynamicSememeValidatorType.REGEXP,
						LookupService.getService(DynamicSememeUtility.class).createDynamicStringData(
								DynamicSememeValidatorType.COMPONENT_TYPE.name() + "|"
								+ DynamicSememeValidatorType.EXTERNAL.name() + "|"
								+ DynamicSememeValidatorType.GREATER_THAN.name() + "|"
								+ DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.name() + "|"
								+ DynamicSememeValidatorType.INTERVAL.name() + "|"
								+ DynamicSememeValidatorType.IS_CHILD_OF.name() + "|"
								+ DynamicSememeValidatorType.IS_KIND_OF.name() + "|"
								+ DynamicSememeValidatorType.LESS_THAN.name() + "|"
								+ DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.name() + "|"
								+ DynamicSememeValidatorType.REGEXP.name()), false),
				new DynamicSememeColumnInfo(6, DYNAMIC_SEMEME_COLUMN_VALIDATOR_DATA.getUUID(), DynamicSememeDataType.ARRAY, null, false, false)
			},
			null) {
	};

	//This is the extended description type that must be attached to a description within a concept to make the concept valid for use as an 
	//assemblage concept for DynamicSememe refexes.  The description annotated with this type describes the intent of 
	//using the concept containing the description as an assemblage concept.
	public final MetadataDynamicSememeConstant DYNAMIC_SEMEME_DEFINITION_DESCRIPTION = new MetadataDynamicSememeConstant("dynamic sememe definition description",
			null,
			UUID.fromString("b0372953-4f20-58b8-ad04-20c2239c7d4e"),
			"This is the extended description type that must be attached to a description within a concept to make the concept valid for use as an "
			+ "assemblage concept for a Dynamic Sememe.  The description annotated with this type describes the intent of "
			+ "using the concept containing the description as an assemblage concept.",
			new DynamicSememeColumnInfo[0],
			null,
			null,
			ObjectChronologyType.SEMEME,
			SememeType.DESCRIPTION) {
	};

	//This is the assemblage type that is used to record the current configuration of the Indexer for Dynamic Sememes..
	//this is ALSO the concept used as the referenced component dynamic sememe instances (of assemblage type itself) which define which other 
	//dynamic sememes should be indexed within the system. 
	public final MetadataDynamicSememeConstant DYNAMIC_SEMEME_INDEX_CONFIGURATION = new MetadataDynamicSememeConstant("dynamic sememe index configuration",
			UUID.fromString("a5d187a7-3d95-5694-b2eb-a48d94cb0698"),
			"A Dynamic Sememe which contains the indexer configuration for Dynamic Sememes within ISAAC.  "
			+ "The referenced component ID will be the assemblage being configured for indexing.",
			new DynamicSememeColumnInfo[]{
				new DynamicSememeColumnInfo(0, DYNAMIC_SEMEME_COLUMN_COLUMNS_TO_INDEX.getUUID(), DynamicSememeDataType.ARRAY, null, false, false)},
			null) {
	};

	public final MetadataDynamicSememeConstant DYNAMIC_SEMEME_COMMENT_ATTRIBUTE = new MetadataDynamicSememeConstant("Comment",
			UUID.fromString("147832d4-b9b8-5062-8891-19f9c4e4760a"),
			"A Sememe used to store comments on arbitrary items (concepts, relationships, sememes, etc)",
			new DynamicSememeColumnInfo[]{
				new DynamicSememeColumnInfo(0, DYNAMIC_SEMEME_COLUMN_EDITOR_COMMENT.getUUID(), DynamicSememeDataType.STRING, null, true, true),
				new DynamicSememeColumnInfo(1, DYNAMIC_SEMEME_COLUMN_EDITOR_COMMENT_CONTEXT.getUUID(), DynamicSememeDataType.STRING, null, false, true)}) {
	};  //Index the comments, and the columns

	public final MetadataDynamicSememeConstant DYNAMIC_SEMEME_ASSOCIATION_SEMEME = new MetadataDynamicSememeConstant("sememe represents association",
			UUID.fromString("5252bafb-1ba7-5a35-b1a2-48d7a65fa477"),
			"A Sememe used to annotate other sememes which define an association, which is defined as a sememe which contains "
			+ "a data column named 'target concept', among other criteria.",
			new DynamicSememeColumnInfo[]{},
			null) {
	};

	public final MetadataDynamicSememeConstant DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME = new MetadataDynamicSememeConstant("inverse name",
			null,
			UUID.fromString("c342d18a-ec1c-5583-bfe3-59e6324ae189"),
			"This is the extended description type that may be attached to a description within a concept that defines an Association Refex to signify that "
			+ "the referenced description is the inverse of the association name.",
			new DynamicSememeColumnInfo[0],
			new String[]{},
			new String[]{},
			ObjectChronologyType.SEMEME,
			SememeType.DESCRIPTION) {
	};

	//An organizational concept which serves as a parent concept for dynamic sememes defined in the system
	//(unless they choose to put them some where else, this isn't required, is only for convenience)
	public final MetadataConceptConstantGroup DYNAMIC_SEMEME_ASSEMBLAGES = new MetadataConceptConstantGroup("dynamic sememe assemblages",
			UUID.fromString("e18265b7-5406-52b6-baf0-4cfb867829b4")) {
		{
			addChild(DYNAMIC_SEMEME_EXTENSION_DEFINITION);
			addChild(DYNAMIC_SEMEME_DEFINITION_DESCRIPTION);
			addChild(DYNAMIC_SEMEME_INDEX_CONFIGURATION);
			addChild(DYNAMIC_SEMEME_COMMENT_ATTRIBUTE);
			addChild(DYNAMIC_SEMEME_ASSOCIATION_SEMEME);
			addChild(DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME);
			setParent(TermAux.ASSEMBLAGE);
		}
	};

	@Override
	public MetadataConceptConstant[] getConstantsToCreate() {
		return new MetadataConceptConstant[] {DYNAMIC_SEMEME_ASSEMBLAGES, DYNAMIC_SEMEME_METADATA};
	}
}
