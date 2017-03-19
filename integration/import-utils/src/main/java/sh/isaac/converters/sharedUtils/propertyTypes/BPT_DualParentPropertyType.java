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

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;

//~--- classes ----------------------------------------------------------------

/**
 * Certain types of properties we 'dual home' placing them in two different places in the metadata hierarchy.
 *
 *
 * @author Daniel Armbrust
 *
 */
public abstract class BPT_DualParentPropertyType
        extends PropertyType {
   private String secondParentName_;  // Typically "Term-name Refsets" under "Project Refsets" or "Term-name Descriptions" under "Descriptions in source terminology, etc
   private UUID secondParentId_;  // calculated from above, but may have semantic tag stuff added.

   //~--- constructors --------------------------------------------------------

   /**
    * @param propertyTypeDescription - The name used for the property category within the terminology specific hierarchy -typically something like
    * "Attribute Types" or "Association Types".  This text is also used to construct the UUID for this property type grouping.
    * @param descriptionWithCodeSystemName - a second name used to create a concept in a more generic hierarcy - should be a value such as "{Term-name} Descriptions"
    * Usually this is "Term-Name + propertyTypeDescription")
    * @param createAsDynamicRefex - true to mark as a dynamic refex, false otherwise.
    * @param defaultDynamicRefexColumnType - If the property is specified without further column instructions, and createAsDynamicRefex is true,
    */
   protected BPT_DualParentPropertyType(String propertyTypeDescription,
         String descriptionWithCodeSystemName,
         boolean createAsDynamicRefex,
         DynamicSememeDataType defaultDynamicRefexColumnType) {
      super(propertyTypeDescription, createAsDynamicRefex, defaultDynamicRefexColumnType);
      this.secondParentName_ = descriptionWithCodeSystemName;
   }

   //~--- set methods ---------------------------------------------------------

   public void setSecondParentId(UUID secondParentId) {
      secondParentId_ = secondParentId;
   }

   //~--- get methods ---------------------------------------------------------

   public String getSecondParentName() {
      return secondParentName_;
   }

   public UUID getSecondParentUUID() {
      if (secondParentId_ == null) {
         throw new RuntimeException("Second parent ID not yet calculated!");
      }

      return secondParentId_;
   }
}

