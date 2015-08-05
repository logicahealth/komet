/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpecWithDescriptions;
import org.ihtsdo.otf.tcc.api.spec.DynamicRefexConceptSpec;

import gov.vha.isaac.ochre.impl.sememe.RefexDynamicUsageDescription;

/**
 * 
 * {@link RefexDynamic}
 * 
 * If you want to understand how these various concepts work together, see  the class description for
 * {@link RefexDynamicUsageDescription}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamic
{
	/*
	 * !!!NOTE !!!
	 * Need to be careful about the order of definition of these elements - especially where they reference each other.
	 * Otherwise, you get null pointer exceptions in the class loader at runtime.
	 * 
	 * From top -down - make sure everything is defined before it is used elsewhere in this class.
	 */
	
	public static ConceptSpec UNKNOWN_CONCEPT = new ConceptSpec("unknown (null) concept", 
			UUID.fromString("00000000-0000-0000-C000-000000000046"));
	
	//From IsaacMetadataAuxiliaryBinding - which we can't yet depend on
	private static ConceptSpec ASSEMBLAGE = new ConceptSpec("assemblage", UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da"));
	
	//Above this point - these concepts already exist - and come from SCT, or elsewhere in the source code.
	//Below this point, all of these concepts are new (unless otherwise specified), and are being created to support the RefexDynamic code.
	
	//an organizational concept for all of the new concepts being added for dynamic refex 
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_METADATA = new ConceptSpecWithDescriptions("dynamic sememe metadata", 
			UUID.fromString("9769773c-7b70-523d-8fc5-b16621ffa57c"), 
			new String[] {"dynamic sememe metadata", "dynamic refex metadata"},
			new String[] {},
			ASSEMBLAGE);
	
	//used as salt for generating other UUIDs
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_NAMESPACE = new ConceptSpecWithDescriptions("dynamic sememe namespace", 
			UUID.fromString("eb0c13ff-74fd-5987-88a0-6f5d75269e9d"),
			new String[] {"dynamic sememe namespace", "dynamic refex namespace"},
			new String[] {},
			DYNAMIC_SEMEME_METADATA);

	//An organizational concept which serves as a parent concept for any column types that are defined
	//within the system.
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMNS = new ConceptSpecWithDescriptions("dynamic sememe columns", 
			UUID.fromString("46ddb9a2-0e10-586a-8b54-8e66333e9b77"),
			new String[] {"dynamic sememe columns", "dynamic refex columns"},
			new String[] {},
			DYNAMIC_SEMEME_METADATA);

	//The seven column types we need for describing column types
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_ORDER = new ConceptSpecWithDescriptions("column order", 
			UUID.fromString("8c501747-846a-5cea-8fd6-c9dd3dfc674f"),
			new String[] {"column order"},
			new String[] {"Stores the column order of this column within a Dynamic Sememe Definition"},
			DYNAMIC_SEMEME_COLUMNS);
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_NAME = new ConceptSpecWithDescriptions("column name", 
			UUID.fromString("89c0ded2-fd69-5654-a386-ded850d258a1"),
			new String[] {"column name"},
			new String[] {"Stores the concept reference to the concept that defines the name of this column within a Dynamic Sememe Definition"},
			DYNAMIC_SEMEME_COLUMNS);
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_TYPE = new ConceptSpecWithDescriptions("column type", 
			UUID.fromString("dbfd9bd2-b84f-574a-ab9e-64ba3bb94793"),
			new String[] {"column type"},
			new String[] {"Stores the data type of this column within a Dynamic Sememe Definition"},
			DYNAMIC_SEMEME_COLUMNS);
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE = new ConceptSpecWithDescriptions("column default value", 
			UUID.fromString("4d3e79aa-ab74-5858-beb3-15e0888986cb"),
			new String[] {"column default value"},
			new String[] {"Stores the (optional) default value of this column within a Dynamic Sememe Definition"},
			DYNAMIC_SEMEME_COLUMNS);
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_REQUIRED = new ConceptSpecWithDescriptions("column required", 
			UUID.fromString("8a89ef19-bd5a-5e25-aa57-1172fbb437b6"),
			new String[] {"column required"},
			new String[] {"Stores the (optional) flag to specify that this column is manditory within a Dynamic Sememe Definition"},
			DYNAMIC_SEMEME_COLUMNS);
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_VALIDATOR = new ConceptSpecWithDescriptions("column validator", 
			UUID.fromString("f295c3ba-d416-563d-8427-8b5d3e324192"),
			new String[] {"column validator"},
			new String[] {"Stores the (optional) validator type which will be applied to user supplied data of this column within a Dynamic Sememe Definition"},
			DYNAMIC_SEMEME_COLUMNS);
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_VALIDATOR_DATA = new ConceptSpecWithDescriptions("column validator data", 
			UUID.fromString("50ea8378-8355-5a5d-bae2-ce7c10e92636"),
			new String[] {"column validator data"},
			new String[] {"Stores the (optional) validator data which will be used by the validator to check the user input of this column within a Dynamic Sememe Definition"},
			DYNAMIC_SEMEME_COLUMNS);
	
	//used for index config
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_COLUMNS_TO_INDEX = new ConceptSpecWithDescriptions("columns to index", 
			UUID.fromString("cede7677-3759-5dce-b28b-20a40fddf5d6"),
			new String[] {"columns to index"},
			new String[] {"Contains a String which has a comma seperated list of the column positions within the referenced sememe assemblage which should have their values indexed."},
			DYNAMIC_SEMEME_COLUMNS);
	
	//Used for referenced component type restrictions
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_TYPE = new ConceptSpecWithDescriptions("referenced component type restriction", 
			UUID.fromString("902f97b6-2ef4-59d7-b6f9-01278a00061c"),
			new String[] {"referenced component type restriction"},
			new String[] {"Stores the (optional) referenced component type restriction selection which will be used by the validator to check the user input for the "
					+ "referenced component when creating an instance of a dynamic sememe"},
			DYNAMIC_SEMEME_COLUMNS);
	
	//Convenience column type for refex instances that just wish to attach a single column of data, and don't want to create another concept
	//to represent the column name.  Typically only used when defining refexes where there is a single column of attached data (typically - attaching an attribute,
	//the column represents the value, while the type of the attribute is represented by the refex type itself - so the column name isn't really necessary)
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_COLUMN_VALUE = new ConceptSpecWithDescriptions("value", 
			UUID.fromString("d94e271f-0e9b-5159-8691-6c29c7689ffb"),
			new String[] {"value"},
			new String[] {"The attached value of the sememe"},
			DYNAMIC_SEMEME_COLUMNS);
	
	//An organizational concept which serves as a parent concept for dynamic refexes defined in the system
	//(unless they choose to put them some where else, this isn't required, is only for convenience)
	public static ConceptSpecWithDescriptions DYNAMIC_SEMEME_ASSEMBLAGES = new ConceptSpecWithDescriptions("dynamic sememe assemblages", 
			UUID.fromString("e18265b7-5406-52b6-baf0-4cfb867829b4"),
			new String[] {"dynamic sememe assemblages", "dynamic refex assemblages"},
			new String[] {},
			ASSEMBLAGE);
	
	//This is the extended description type that must be attached to a description within a concept to make the concept valid for use as an 
	//assemblage concept for RefexDynamic refexes.  The description annotated with this type describes the intent of 
	//using the concept containing the description as an assemblage concept.
	public static DynamicRefexConceptSpec DYNAMIC_SEMEME_DEFINITION_DESCRIPTION = new DynamicRefexConceptSpec("dynamic sememe definition description", 
			UUID.fromString("b0372953-4f20-58b8-ad04-20c2239c7d4e"),
			new String[] {"dynamic sememe definition description", "dynamic refex definition description"},
			new String[0],
			true, 
			"This is the extended description type that must be attached to a description within a concept to make the concept valid for use as an "
				+ "assemblage concept for a Dynamic Sememe.  The description annotated with this type describes the intent of "
				+ "using the concept containing the description as an assemblage concept.", 
			new RefexDynamicColumnInfo[0],
			DYNAMIC_SEMEME_ASSEMBLAGES, 
			ComponentType.DESCRIPTION, 
			new Integer[] {});  //Require an index on this
	
	//This is the assemblage type that is usually present on a concept when it is used as an assemblage itself to describe the attached data - the attached
	//refex using this for an assemblage will describe a data column that is to be attached with the refex.  This assemblage type wouldn't be used if there was 
	//no data to attach.
	public static DynamicRefexConceptSpec DYNAMIC_SEMEME_EXTENSION_DEFINITION = new DynamicRefexConceptSpec("dynamic sememe extension definition", 
			UUID.fromString("406e872b-2e19-5f5e-a71d-e4e4b2c68fe5"),
			new String[] {"dynamic sememe definition", "dynamic refex definition"},
			new String[0],
			true, 
			"This concept is used as an assemblage for defining new Sememe extensions.  "
				+ "The attached data columns describe what columns are required to define a new Sememe.",
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, RefexDynamic.DYNAMIC_SEMEME_COLUMN_ORDER.getPrimodialUuid(), RefexDynamicDataType.INTEGER, null, true, null, null),
				new RefexDynamicColumnInfo(1, RefexDynamic.DYNAMIC_SEMEME_COLUMN_NAME.getPrimodialUuid(), RefexDynamicDataType.UUID, null, true, null, null),
				new RefexDynamicColumnInfo(2, RefexDynamic.DYNAMIC_SEMEME_COLUMN_TYPE.getPrimodialUuid(), RefexDynamicDataType.STRING, null, true, null, null),
				new RefexDynamicColumnInfo(3, RefexDynamic.DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimodialUuid(), RefexDynamicDataType.POLYMORPHIC, null, false, null, null),
				new RefexDynamicColumnInfo(4, RefexDynamic.DYNAMIC_SEMEME_COLUMN_REQUIRED.getPrimodialUuid(), RefexDynamicDataType.BOOLEAN, null, false, null, null),
				new RefexDynamicColumnInfo(5, RefexDynamic.DYNAMIC_SEMEME_COLUMN_VALIDATOR.getPrimodialUuid(), RefexDynamicDataType.STRING, null, false, null, null),
				new RefexDynamicColumnInfo(6, RefexDynamic.DYNAMIC_SEMEME_COLUMN_VALIDATOR_DATA.getPrimodialUuid(), RefexDynamicDataType.POLYMORPHIC, null, false, null, null)},
			DYNAMIC_SEMEME_ASSEMBLAGES);
	
	//This is the assemblage type that is optionally attached to an assemblage itself, to declare type restrictions on the referenced component
	//of the refex
	//TODO (artf231825) - [refex] it would have been useful to be able to declare a regexp restriction on the values of this column (and probably others) but can't do it, 
	//because I don't have any of the implementations on the classpath here.  something else to rethink?
	public static DynamicRefexConceptSpec DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION = new DynamicRefexConceptSpec("dynamic sememe referenced component restriction", 
			UUID.fromString("0d94ceeb-e24f-5f1a-84b2-1ac35f671db5"),
			new String[] {"dynamic sememe referenced component restriction", "dynamic refex referenced component restriction"},
			new String[0],
			true, 
			"This concept is used as an assemblage for defining new Sememe extensions.  It annotates other sememe extensions to restrict the usage of a "
				+ " sememe to a particular Component Type (Concept, Description, etc).  The attached data column specifies the allowed Component Type",
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, RefexDynamic.DYNAMIC_SEMEME_COLUMN_REFERENCED_COMPONENT_TYPE.getPrimodialUuid(), RefexDynamicDataType.STRING, null, true, null, null)},
			DYNAMIC_SEMEME_METADATA);
	
	//This is the assemblage type that is used to record the current configuration of the Indexer for Dynamic Refexes.
	//this is ALSO the concept that stores (as a member list) dynamic refex instances (of assemblage type itself) which define which other 
	//dynamic refexes should be indexed within the system. 
	public static DynamicRefexConceptSpec DYNAMIC_SEMEME_INDEX_CONFIGURATION = new DynamicRefexConceptSpec("dynamic sememe index configuration", 
			UUID.fromString("a5d187a7-3d95-5694-b2eb-a48d94cb0698"),
			new String[] {"dynamic sememe index configuration", "dynamic refex index configuration"},
			new String[0],
			false, 
			"A Dynamic Sememe which contains the indexer configuration for Dynamic Sememes within ISAAC.  "
				+ "The referenced component ID will be the assemblage being configured for indexing.", 
			new RefexDynamicColumnInfo[] {
				new RefexDynamicColumnInfo(0, RefexDynamic.DYNAMIC_SEMEME_COLUMN_COLUMNS_TO_INDEX.getPrimodialUuid(), RefexDynamicDataType.STRING, null, false, null, null)},
			DYNAMIC_SEMEME_METADATA);
	

}
