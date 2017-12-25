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



package sh.isaac.converters.sharedUtils.propertyTypes;

import java.util.UUID;

import sh.isaac.MetaData;

/**
 * Fields to treat as descriptions.
 *
 * @author Daniel Armbrust
 */
public class BPT_Descriptions extends PropertyType implements BPT_HasAltMetaDataParent{
   // These values can be used as the starting point for establishing the hierarchy of synonym types.
   // Descriptions are typically sorted (ascending) by the propertySubType values.
   // The lowest number found will be used as the FQN.
   // The next higher number will be used as the 'preferred' synonym.
   // The next higher number will be used as the 'acceptable' synonym - continuing until the value is above the description threshold.

   /** The Constant FQN. */
   // Then, the first found description will be the 'preferred' description - the rest will be 'acceptable'.
   public static final int FULLY_QUALIFIED_NAME = 0;

   /** The Constant SYNONYM. */
   public static final int SYNONYM = 200;

   /** The Constant DEFINITION. */
   public static final int DEFINITION = 400;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new BP T descriptions.
    *
    * @param terminologyName the terminology name
    */
   public BPT_Descriptions(String terminologyName) {
      super(terminologyName + " Description Types", false, null);
   }
   
   public UUID getAltMetaDataParentUUID() {
	return MetaData.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getPrimordialUuid();
   }
}

