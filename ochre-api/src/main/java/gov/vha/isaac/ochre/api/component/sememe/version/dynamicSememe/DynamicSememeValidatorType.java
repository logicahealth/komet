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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDoubleBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloatBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeIntegerBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLongBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNidBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeStringBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUIDBI;
import java.util.logging.Logger;


/**
 * {@link DynamicSememeValidatorType}
 * 
 * The acceptable validatorDefinitionData object type(s) for the following fields:
 * {@link DynamicSememeValidatorType#LESS_THAN}
 * {@link DynamicSememeValidatorType#GREATER_THAN}
 * {@link DynamicSememeValidatorType#LESS_THAN_OR_EQUAL}
 * {@link DynamicSememeValidatorType#GREATER_THAN_OR_EQUAL}
 * 
 * are one of ( {@link DynamicSememeIntegerBI}, {@link DynamicSememeLongBI}, {@link DynamicSememeFloatBI}, {@link DynamicSememeDoubleBI})
 * 
 * {@link DynamicSememeValidatorType#INTERVAL} - Should be a {@link DynamicSememeStringBI} with valid interval notation - such as "[4,6)"
 * 
 * {@link DynamicSememeValidatorType#REGEXP} - Should be a {@link DynamicSememeStringBI} with valid regular expression, per 
 * http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
 * 
 * And for the following two:
 * {@link DynamicSememeValidatorType#IS_CHILD_OF}
 * {@link DynamicSememeValidatorType#IS_KIND_OF}
 * The validatorDefinitionData should be either an {@link DynamicSememeNidBI} or a {@link DynamicSememeUUIDBI}.
 * 
 * For {@link DynamicSememeValidatorType#EXTERNAL} the validatorDefinitionData should be a {@link DynamicSememeArrayBI<DynamicSememeStringBI>} 
 * which contains (in the first position of the array) the name of an HK2 named service which implements {@link ExternalValidatorBI} 
 * the name that you provide should be the value of the '@Name' annotation within the class which implements the ExternalValidatorBI class.  
 * This code will request that implementation (by name) and pass the validation call to it.
 * 
 * Optionally, the validatorDefinitionData more that one {@link DynamicSememeStringBI} in the array - only the first position of the array 
 * will be considered as the '@Name' to be used for the HK2 lookup.  All following data is ignored, and may be used by the external validator 
 * implementation to store other data.  For example, if the validatorDefinitionData {@link DynamicSememeArrayBI<DynamicSememeStringBI>}
 * contains an array of strings such as new String[]{"mySuperRefexValidator", "somespecialmappingdata", "some other mapping data"} 
 * then the following HK2 call will be made to locate the validator implementation (and validate):
 * <pre>
 *   ExternalValidatorBI validator = Hk2Looker.get().getService(ExternalValidatorBI.class, "mySuperRefexValidator");
 *   return validator.validate(userData, validatorDefinitionData, viewCoordinate);
 * </pre>
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum DynamicSememeValidatorType
{
	LESS_THAN("<"), GREATER_THAN(">"), LESS_THAN_OR_EQUAL("<="), GREATER_THAN_OR_EQUAL(">="),  //Standard math stuff 
	INTERVAL("Interval"), //math interval notation - such as [5,10)
	REGEXP("Regular Expression"),  //http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
	EXTERNAL("External"), //see class docs above - implemented by an ExternalValidatorBI
	IS_CHILD_OF("Is Child Of"), //OTF is child of - which only includes immediate (not recursive) children on the 'Is A' relationship. 
	IS_KIND_OF("Is Kind Of"), //OTF kind of - which is child of - but recursive, and self (heart disease is a kind-of heart disease);
	COMPONENT_TYPE("Component Type Restriction"),  //specify which type of nid can be put into a UUID or nid column
	UNKNOWN("Unknown");  //Not a real validator, only exists to allow GUI convenience, or potentially store other validator data that we don't support in OTF
	//but we may need to store / retreive
	
	private String displayName_;
	private static Logger logger = Logger.getLogger(DynamicSememeValidatorType.class.getName());
	
	private DynamicSememeValidatorType(String displayName)
	{
		displayName_ = displayName;
	}
	
	public String getDisplayName()
	{
		return displayName_;
	}
	
	public boolean validatorSupportsType(DynamicSememeDataType type)
	{
		//These are supported by all types - external specifies itself, what it supports, and we always include UNKNOWN.
		if (this == UNKNOWN || this == EXTERNAL)
		{
			return true;
		}
		
		switch (type)
		{
			case BOOLEAN: case POLYMORPHIC: 
			{
				//technically, regexp would work here... but makes no sense.
				return false;
			}	
			case DOUBLE: case FLOAT: case INTEGER: case LONG:
			{
				if (this == GREATER_THAN || this == GREATER_THAN_OR_EQUAL || 
						this == LESS_THAN || this == LESS_THAN_OR_EQUAL || 
						this == INTERVAL || this == REGEXP)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			case NID: case UUID:
			{
				if (this == IS_CHILD_OF || this == IS_KIND_OF || this == REGEXP || this == COMPONENT_TYPE)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			case STRING: case BYTEARRAY:
			{
				if (this == REGEXP)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			default:
			{
				logger.warning("Unexpected case!");
				return false;
			}
		}
	}
}
