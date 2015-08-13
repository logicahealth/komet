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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.UUID;
import gov.vha.isaac.ochre.api.MetadataConceptConstant;


/**
 * Constants that need to be defined within the API level packages for dependency reasons
 * @author darmbrust
 */
public class IsaacMetadataConstantsBase
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

}
