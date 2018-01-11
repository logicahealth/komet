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

import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.DynamicConstants;

//~--- classes ----------------------------------------------------------------

/**
 * The Class PropertyAssociation.
 */
public class PropertyAssociation
        extends Property {
   /** The association inverse name. */
   private final String associationInverseName;

   /** The association component type restriction. */
   private final ObjectChronologyType associationComponentTypeRestriction;

   /** The association component type sub restriction. */
   private final VersionType associationComponentTypeSubRestriction;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new property association.
    *
    * @param owner the owner
    * @param sourcePropertyNameFQN the source property name fully qualified name
    * @param sourcePropertyAltName the source property alt name
    * @param associationInverseName the association inverse name
    * @param associationDescription the association description
    * @param disabled the disabled
    */
   public PropertyAssociation(PropertyType owner,
                              String sourcePropertyNameFQN,
                              String sourcePropertyAltName,
                              String associationInverseName,
                              String associationDescription,
                              boolean disabled) {
      this(owner,
           sourcePropertyNameFQN,
           sourcePropertyAltName,
           associationInverseName,
           associationDescription,
           disabled,
           null,
           null);
   }

   /**
    * Instantiates a new property association.
    *
    * @param owner the owner
    * @param sourcePropertyNameFQN the source property name FQN
    * @param sourcePropertyAltName the source property alt name
    * @param associationInverseName the association inverse name
    * @param associationDescription the association description
    * @param disabled the disabled
    * @param associationComponentTypeRestriction the association component type restriction
    * @param associationComponentTypeSubRestriction the association component type sub restriction
    */
   public PropertyAssociation(PropertyType owner,
                              String sourcePropertyNameFQN,
                              String sourcePropertyAltName,
                              String associationInverseName,
                              String associationDescription,
                              boolean disabled,
                              ObjectChronologyType associationComponentTypeRestriction,
                              VersionType associationComponentTypeSubRestriction) {
      super(owner,
            sourcePropertyNameFQN,
            sourcePropertyAltName,
            associationDescription,
            disabled,
            Integer.MAX_VALUE,
            null);

      if (associationDescription == null) {
         throw new RuntimeException("association description is required");
      }

      this.associationInverseName                 = associationInverseName;
      this.associationComponentTypeRestriction    = associationComponentTypeRestriction;
      this.associationComponentTypeSubRestriction = associationComponentTypeSubRestriction;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the association component type restriction.
    *
    * @return the association component type restriction
    */
   public ObjectChronologyType getAssociationComponentTypeRestriction() {
      return this.associationComponentTypeRestriction;
   }

   /**
    * Gets the association component type sub restriction.
    *
    * @return the association component type sub restriction
    */
   public VersionType getAssociationComponentTypeSubRestriction() {
      return this.associationComponentTypeSubRestriction;
   }

   /**
    * Gets the association inverse name.
    *
    * @return the association inverse name
    */
   public String getAssociationInverseName() {
      return this.associationInverseName;
   }

   /**
    * Gets the data columns for dynamic refex.
    *
    * @return the data columns for dynamic refex
    */
   @Override
   public DynamicColumnInfo[] getDataColumnsForDynamicRefex() {
      final DynamicColumnInfo[] columns = new DynamicColumnInfo[] {
            new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid(), 
                  DynamicDataType.UUID, null, false, true) };

      return columns;
   }
}

