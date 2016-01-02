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
package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import org.jvnet.hk2.annotations.Contract;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;

/**
 * {@link DynamicSememeExternalValidator}
 *
 * To support validators that are not implemented within the API here - we support external validators.  It is assumed that at runtime
 * the validator implementation will be fetchable via HK2.  
 * 
 * Currently, this is used for Drools validators.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface DynamicSememeExternalValidator
{
	/**
	 * @param userData - The user entry to validate.
	 * @param validatorDefinitionData - The string used to locate the validator implementation is stored in the first position of the array.
	 * see {@link DynamicSememeValidatorType} for details) and any other data that was stored with the validator assignment (any additional items
	 * in the array)
	 * @param sc - the stamp coordinate that was passed in to the validate call.
	 * @param c - the taxonomy coordinate that was passed in to the validate call.
	 * @return - true if valid, exception otherwise.
	 * @throws RuntimeException - if it fails the validator, this exception should contain a user-friendly reason why.
	 */
	public boolean validate(DynamicSememeData userData, DynamicSememeArray<DynamicSememeString> validatorDefinitionData, StampCoordinate sc,
							TaxonomyCoordinate tc) throws RuntimeException;
	
	/**
	 * Return true or false, depending on whether this validator implementation supports the specified data type.
	 * @param validatorDefinitionData - The string used to locate the validator implementation is stored in the first position of the array.
	 * see {@link DynamicSememeValidatorType} for details) and any other data that was stored with the validator assignment (any additional items
	 * in the array)
	 * @param dataType - The datatype to inquire about
	 */
	public boolean validatorSupportsType(DynamicSememeArray<DynamicSememeString> validatorDefinitionData, DynamicSememeDataType dataType);
}
