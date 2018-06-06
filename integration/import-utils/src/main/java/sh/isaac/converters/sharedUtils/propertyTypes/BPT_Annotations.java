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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;

//~--- classes ----------------------------------------------------------------

/**
 * Fields to treat as annotations (attributes).
 *
 * @author Daniel Armbrust
 */
public class BPT_Annotations
        extends PropertyType {
   
      /**
       * Instantiates a new BP T annotations.
       *
       * @param fqnPrefix the fqn prefix
       */
      public BPT_Annotations(String fqnPrefix) {
         this(fqnPrefix, null);
      }

   /**
    * Instantiates a new BP T annotations.
    *
    * @param fqnPrefix the fqn prefix
    * @param converterUUID optional converter to use
    */
   public BPT_Annotations(String fqnPrefix, ConverterUUID converterUUID) {
      super(fqnPrefix + " Attribute Types", true, DynamicDataType.STRING, converterUUID);
   }
}

