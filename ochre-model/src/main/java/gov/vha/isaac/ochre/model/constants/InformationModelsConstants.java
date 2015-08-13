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
package gov.vha.isaac.ochre.model.constants;

import java.util.UUID;
import gov.vha.isaac.ochre.api.MetadataConceptConstant;
import gov.vha.isaac.ochre.api.MetadataConceptConstantGroup;
import gov.vha.isaac.ochre.api.MetadataDynamicSememeConstant;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;

/**
 * {@link InformationModelsConstants}
 *
 * InformationModel related constants for ISAAC in ConceptSpec form for reuse.
 * 
 * The DBBuilder mojo processes this class, and creates these concept / relationships as necessary during build.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class InformationModelsConstants
{
	
	//current information models
	public static final MetadataConceptConstant CEM_ENUMERATIONS = new MetadataConceptConstant("Clinical Element Model Enumerations", 
		UUID.fromString("ee5da47f-562f-555d-b7dd-e18697e11ece")) {};	

	public static final MetadataConceptConstantGroup CEM = new MetadataConceptConstantGroup("Clinical Element Model", 
		UUID.fromString("0a9c9ba5-410e-5a40-88f4-b0cdd17325e1")) 
		{
			{
				addChild(CEM_ENUMERATIONS);
			}
		};

	public static final MetadataConceptConstant FHIM_ENUMERATIONS = new MetadataConceptConstant("Federal Health Information Model Enumerations", 
			UUID.fromString("78e5feff-faf7-5666-a2e1-21bdfe688e13")) {}; 
	
			
	public static final MetadataConceptConstantGroup FHIM = new MetadataConceptConstantGroup("Federal Health Information Model", 
			UUID.fromString("9eddce80-784c-50a3-8ec6-e92278ac7691"))
	{
		{
			addChild(FHIM_ENUMERATIONS);
		}
	};

	public static final MetadataConceptConstant HED_ENUMERATIONS = new MetadataConceptConstant("Health eDecision Enumerations",  
			UUID.fromString("5f4cf488-38bd-54b0-8d08-809599d6db82")) {};

	public static final MetadataConceptConstantGroup HED = new MetadataConceptConstantGroup("Health eDecision", 
		UUID.fromString("1cdae521-c637-526a-bf88-134de474f824"))
	{
		{
			addChild(HED_ENUMERATIONS);
		}
	};
	
	public static final MetadataConceptConstantGroup INFORMATION_MODELS = new MetadataConceptConstantGroup("Information Models", 
		UUID.fromString("ab09b185-b93d-577b-a350-622be832e6c7"))
		{
			{
				addChild(CEM);
				addChild(FHIM);
				addChild(HED);
			}
		};
	
	//Required columns
	public static final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_INFO_MODEL_LABEL = new MetadataConceptConstant("info model property label", 
			UUID.fromString("7f1102be-2fe4-57e3-9b9d-80087d6ee054"),
			"Used to capture the label for a property as used by the native information model type, e.g. 'qual' in CEM"){};
	
	public static final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_INFO_MODEL_TYPE = new MetadataConceptConstant("info model property type", 
			UUID.fromString("302e90ab-c149-5a0f-b64e-b189de5e2292"),
			"Used to capture the property type as expressed in the model, e.g. 'MethodDevice' in CEM"){};
	
	public static final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_INFO_MODEL_NAME = new MetadataConceptConstant("info model property name", 
			UUID.fromString("2dc47ed8-9b53-57f0-a844-b15b2275e8e8"),
			"Used to capture the property name as expressed in the model"){};
	
	public static final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_INFO_MODEL_DEFAULT_VALUE = new MetadataConceptConstant("info model property default value", 
			UUID.fromString("a5e2412f-b27b-5dcf-aba0-f6a2869296b4"),
			"Used to capture any default value the property has in the model"){};
	
	public static final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_INFO_MODEL_VALUE = new MetadataConceptConstant("info model property value", 
			UUID.fromString("a856f12e-b1dc-5521-ae85-2c232aba79e4"),
			"Used to capture any actual value the property has (for demo purposes)"){};
	
	public static final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_INFO_MODEL_CARDINALITY_MIN = new MetadataConceptConstant("info model property cardinality min", 
			UUID.fromString("a6d7fda7-bd08-5712-a4e4-19cf49e2702e"),
			"Used to capture the cardinality lower limit in the model"){};
	
	public static final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_INFO_MODEL_CARDINALITY_MAX = new MetadataConceptConstant("info model property cardinality max", 
			UUID.fromString("a6f17770-1256-5d5b-b298-90a71858f391"),
			"Used to capture the cardinality upper limit in the model"){};
	
	public static final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_INFO_MODEL_VISIBILITY = new MetadataConceptConstant("info model property visibility", 
			UUID.fromString("ff3653ec-61f8-5382-9d6f-d12a26f425d4"),
			"Used to capture the property visibility in the model"){};
	
	
	public static final MetadataDynamicSememeConstant INFORMATION_MODEL_PROPERTIES = new MetadataDynamicSememeConstant("Information model property refset", 
			UUID.fromString("ef4a1189-8fe0-56c3-9ca9-334c40b78fc1"),
			"Used to capture information about information model properties", 
			new DynamicSememeColumnInfo[] {
				new DynamicSememeColumnInfo(0, DYNAMIC_SEMEME_COLUMN_INFO_MODEL_LABEL.getUUID(), DynamicSememeDataType.STRING, null, false, null, null),
				new DynamicSememeColumnInfo(1, DYNAMIC_SEMEME_COLUMN_INFO_MODEL_TYPE.getUUID(), DynamicSememeDataType.STRING, null, false, null, null),
				new DynamicSememeColumnInfo(2, DYNAMIC_SEMEME_COLUMN_INFO_MODEL_NAME.getUUID(), DynamicSememeDataType.STRING, null, false, null, null),
				new DynamicSememeColumnInfo(3, DYNAMIC_SEMEME_COLUMN_INFO_MODEL_DEFAULT_VALUE.getUUID(), DynamicSememeDataType.STRING, null, false, null, null),
				new DynamicSememeColumnInfo(4, DYNAMIC_SEMEME_COLUMN_INFO_MODEL_VALUE.getUUID(), DynamicSememeDataType.STRING, null, false, null, null),
				new DynamicSememeColumnInfo(5, DYNAMIC_SEMEME_COLUMN_INFO_MODEL_CARDINALITY_MIN.getUUID(), DynamicSememeDataType.STRING, null, false, null, null),
				new DynamicSememeColumnInfo(6, DYNAMIC_SEMEME_COLUMN_INFO_MODEL_CARDINALITY_MAX.getUUID(), DynamicSememeDataType.STRING, null, false, null, null),
				new DynamicSememeColumnInfo(7, DYNAMIC_SEMEME_COLUMN_INFO_MODEL_VISIBILITY.getUUID(), DynamicSememeDataType.STRING, null, false, null, null)},
				null);
	
	public static final MetadataConceptConstant HAS_TERMINOLOGY_CONCEPT = new MetadataConceptConstant("Has terminology concept", 
			UUID.fromString("890b36d9-655f-5acb-9339-dd8628dced65")) {}; 
}