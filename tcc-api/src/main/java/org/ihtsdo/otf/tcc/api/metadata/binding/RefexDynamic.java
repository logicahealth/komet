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
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

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
	public static ConceptSpec UNKNOWN_CONCEPT = new ConceptSpec("unknown (null) concept", 
			UUID.fromString("00000000-0000-0000-C000-000000000046"));
	
	//Above this point - these concepts already exist - and come from SCT, or elsewhere in the source code.
	//Below this point, all of these concepts are new (unless otherwise specified), and are being created to support the RefexDynamic code.
	
	//This concept doesn't need to be in the taxonomy, it is just used as salt for generating other UUIDs
	public static ConceptSpec REFEX_DYNAMIC_NAMESPACE = new ConceptSpec("Refex Dynamic Namespace", 
			UUID.fromString("9c76af37-671c-59a3-93bf-dfe0c5c58bfa"));
	
	//an organizational concept for all of the new concepts being added to the Refset Auxiliary Concept tree
	public static ConceptSpec REFEX_DYNAMIC_TYPES = new ConceptSpec("dynamic refex types", 
			UUID.fromString("647b6283-7c5f-53ff-a5f7-a40c865b1ef0"), 
			Taxonomies.REFSET_AUX);
	
	//This is the extended description type that must be attached to a concept to make it valid for use as an 
	//assemblage concept for RefexDynamic refexes.  The description annotated with this type describes the intent of 
	//using the concept containing the description as an assemblage concept.
	public static ConceptSpec REFEX_DYNAMIC_DEFINITION_DESCRIPTION = new ConceptSpec("dynamic refex definition description", 
			UUID.fromString("21d300f2-b2d8-5586-916b-0e7ac88d5bea"),
			REFEX_DYNAMIC_TYPES);
	
	//This is the assemblage type that must be present on a concept for it to be used as an assemblage itself - this is the description
	//of the refex that carries the data type definition of the refex.
	public static ConceptSpec REFEX_DYNAMIC_DEFINITION = new ConceptSpec("dynamic refex definition", 
			UUID.fromString("a40fb48c-d755-5eaa-a725-4c4ebc9b9e6e"),
			REFEX_DYNAMIC_TYPES);
	
	//An organizational concept which serves as a parent concept for any column types that are defined
	//within the system.
	public static ConceptSpec REFEX_DYNAMIC_COLUMNS = new ConceptSpec("dynamic refex columns", 
			UUID.fromString("a6767545-14d4-50f7-9522-2ddc37c2f676"),
			REFEX_DYNAMIC_TYPES);
	
	//An organizational concept which serves as a parent concept for any column types that are defined
	//within the system.
	public static ConceptSpec REFEX_DATA_TYPES = new ConceptSpec("refex dynamic column data types", 
			UUID.fromString("0cb295ea-71c9-5137-8662-66373eecd0dc"),
			REFEX_DYNAMIC_TYPES);
	
	//An organizational concept which serves as a parent concept for dynamic refsets defined in the system
	//(unless they choose to put them some where else, this isn't required, is only for convenience)
	public static ConceptSpec REFEX_DYNAMIC_IDENTITY = new ConceptSpec("Dynamic Refsets", 
			UUID.fromString("297a9eb3-ab27-5d33-8a95-61e9c741be73"),
			TermAux.REFSET_IDENTITY);

	//New data types
	
	public static ConceptSpec REFEX_DT_NID = new ConceptSpec("nid", 
			UUID.fromString("d1a17272-9785-51aa-8bde-cc556ab32ebb"),
			REFEX_DATA_TYPES);
	public static ConceptSpec REFEX_DT_BOOLEAN = new ConceptSpec("boolean", 
			UUID.fromString("08f2fb74-980d-5157-b92c-4ff1eac6a506"),
			REFEX_DATA_TYPES);
	public static ConceptSpec REFEX_DT_LONG = new ConceptSpec("long", 
			UUID.fromString("dea8cdf1-de75-5991-9791-79714e4a964d"),
			REFEX_DATA_TYPES);
	public static ConceptSpec REFEX_DT_BYTE_ARRAY = new ConceptSpec("byte array", 
			UUID.fromString("9a84fecf-708d-5de4-9c5f-e17973229e0f"),
			REFEX_DATA_TYPES);
	public static ConceptSpec REFEX_DT_FLOAT = new ConceptSpec("float", 
			UUID.fromString("fb591801-7b37-525d-980d-98a1c63ceee0"),
			REFEX_DATA_TYPES);
	public static ConceptSpec REFEX_DT_DOUBLE = new ConceptSpec("double", 
			UUID.fromString("7172e6ac-a05a-5a34-8275-aef430b18207"),
			REFEX_DATA_TYPES);
	public static ConceptSpec REFEX_DT_POLYMORPHIC = new ConceptSpec("polymorphic", 
			UUID.fromString("3d634fd6-1498-5e8b-b914-e75b42018397"),
			REFEX_DATA_TYPES);
	
	//The following data types already exist, but I'm also adding them to our hierarchy for clarity
	//(assuming you generate concepts from this constants file and load them into the DB, anyway)
	public static ConceptSpec REFEX_DT_STRING = new ConceptSpec("String (foundation metadata concept)", 
			UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5"),
			REFEX_DATA_TYPES);
	public static ConceptSpec REFEX_DT_INTEGER = new ConceptSpec("Signed integer (foundation metadata concept)", 
			UUID.fromString("1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c"),
			REFEX_DATA_TYPES);
	public static ConceptSpec REFEX_DT_UUID = new ConceptSpec("Universally Unique Identifier (foundation metadata concept)", 
			UUID.fromString("845274b5-9644-3799-94c6-e0ea37e7d1a4"),
			REFEX_DATA_TYPES);
}
