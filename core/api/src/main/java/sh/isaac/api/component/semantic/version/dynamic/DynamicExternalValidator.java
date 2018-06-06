/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.component.semantic.version.dynamic;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;

//~--- interfaces -------------------------------------------------------------

/**
 * {@link DynamicExternalValidator}
 *
 * To support validators that are not implemented within the API here - we support external validators.  It is assumed that at runtime
 * the validator implementation will be fetchable via HK2.
 *
 * Currently, this is used for Drools validators.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface DynamicExternalValidator {
   /**
    * Validate.
    *
    * @param userData - The user entry to validate.
    * @param validatorDefinitionData - The string used to locate the validator implementation is stored in the first position of the array.
    * see {@link DynamicValidatorType} for details) and any other data that was stored with the validator assignment (any additional items
    * in the array)
    * @param stampSequence - the stamp sequence where this data will live
    * @return - true if valid, exception otherwise.
    * @throws RuntimeException - if it fails the validator, this exception should contain a user-friendly reason why.
    */
   public boolean validate(DynamicData userData,
                           DynamicArray<DynamicString> validatorDefinitionData,
                           int stampSequence)
            throws RuntimeException;

   /**
    * Return true or false, depending on whether this validator implementation supports the specified data type.
    *
    * @param validatorDefinitionData - The string used to locate the validator implementation is stored in the first position of the array.
    * see {@link DynamicValidatorType} for details) and any other data that was stored with the validator assignment (any additional items
    * in the array)
    * @param dataType - The datatype to inquire about
    * @return true, if successful
    */
   public boolean validatorSupportsType(DynamicArray<DynamicString> validatorDefinitionData,
         DynamicDataType dataType);
}

