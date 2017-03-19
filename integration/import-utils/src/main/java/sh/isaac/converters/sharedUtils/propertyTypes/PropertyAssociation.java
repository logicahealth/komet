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
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.constants.DynamicSememeConstants;

//~--- classes ----------------------------------------------------------------

public class PropertyAssociation
        extends Property {
   private final String               associationInverseName_;
   private final ObjectChronologyType associationComponentTypeRestriction_;
   private final SememeType           associationComponentTypeSubRestriction_;

   //~--- constructors --------------------------------------------------------

   public PropertyAssociation(PropertyType owner,
                              String sourcePropertyNameFSN,
                              String sourcePropertyAltName,
                              String associationInverseName,
                              String associationDescription,
                              boolean disabled) {
      this(owner,
           sourcePropertyNameFSN,
           sourcePropertyAltName,
           associationInverseName,
           associationDescription,
           disabled,
           null,
           null);
   }

   public PropertyAssociation(PropertyType owner,
                              String sourcePropertyNameFSN,
                              String sourcePropertyAltName,
                              String associationInverseName,
                              String associationDescription,
                              boolean disabled,
                              ObjectChronologyType associationComponentTypeRestriction,
                              SememeType associationComponentTypeSubRestriction) {
      super(owner,
            sourcePropertyNameFSN,
            sourcePropertyAltName,
            associationDescription,
            disabled,
            Integer.MAX_VALUE,
            null);

      if (associationDescription == null) {
         throw new RuntimeException("association description is required");
      }

      this.associationInverseName_                 = associationInverseName;
      this.associationComponentTypeRestriction_    = associationComponentTypeRestriction;
      this.associationComponentTypeSubRestriction_ = associationComponentTypeSubRestriction;
   }

   //~--- get methods ---------------------------------------------------------

   public ObjectChronologyType getAssociationComponentTypeRestriction() {
      return this.associationComponentTypeRestriction_;
   }

   public SememeType getAssociationComponentTypeSubRestriction() {
      return this.associationComponentTypeSubRestriction_;
   }

   public String getAssociationInverseName() {
      return this.associationInverseName_;
   }

   @Override
   public DynamicSememeColumnInfo[] getDataColumnsForDynamicRefex() {
      final DynamicSememeColumnInfo[] columns = new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(0,
                                                                                                      DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(),
                                                                                                      DynamicSememeDataType.UUID,
                                                                                                      null,
                                                                                                      false,
                                                                                                      true) };

      return columns;
   }
}

