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

import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link Property}
 *
 * The converters common code uses this property abstraction system to handle converting different property
 * types in the WB, while maintaining consistency in how properties are represented.  Also handles advanced
 * cases where we do things like map a property to an existing WB property type, and then annotate the property
 * instance with the terminology specific property info.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Property {
   private boolean isDisabled_        = false;
   private boolean isFromConceptSpec_ = false;
   private int propertySubType_ =
      Integer.MAX_VALUE;  // Used for subtypes of descriptions, at the moment - FSN, synonym, etc.
   private UUID                      propertyUUID                = null;
   private UUID                      useWBPropertyTypeInstead    = null;  // see comments in setter
   private DynamicSememeColumnInfo[] dataColumnsForDynamicRefex_ = null;
   private String                    sourcePropertyNameFSN_;
   private String                    sourcePropertyAltName_;
   private String                    sourcePropertyDefinition_;
   private PropertyType              owner_;

   //~--- constructors --------------------------------------------------------

   public Property(PropertyType owner, ConceptSpecification cs) {
      this(owner, cs.getConceptDescriptionText(), null, null, false, Integer.MAX_VALUE, null);
      propertyUUID = cs.getPrimordialUuid();
      ConverterUUID.addMapping(cs.getConceptDescriptionText(), cs.getPrimordialUuid());
      isFromConceptSpec_ = true;
   }

   public Property(PropertyType owner, String sourcePropertyNameFSN) {
      this(owner, sourcePropertyNameFSN, null, null, false, Integer.MAX_VALUE, null);
   }

   /**
    * owner must be set via the set method after using this constructor!
    */
   public Property(String sourcePropertyNameFSN,
                   String sourcePropertyAltName,
                   String sourcePropertyDefinition,
                   UUID wbRelType) {
      this(null,
           sourcePropertyNameFSN,
           sourcePropertyAltName,
           sourcePropertyDefinition,
           false,
           Integer.MAX_VALUE,
           null);
      setWBPropertyType(wbRelType);
   }

   /**
    * @param dataTypesForDynamicRefex - if null - will use the default information for the parent {@link PropertyType} - otherwise,
    * uses as provided here (even if empty)
    */
   public Property(PropertyType owner,
                   String sourcePropertyNameFSN,
                   String sourcePropertyAltName,
                   String sourcePropertyDefinition,
                   boolean disabled,
                   int propertySubType,
                   DynamicSememeColumnInfo[] columnInforForDynamicRefex) {
      this.owner_                    = owner;
      this.sourcePropertyNameFSN_    = sourcePropertyNameFSN;
      this.sourcePropertyAltName_    = sourcePropertyAltName;
      this.sourcePropertyDefinition_ = sourcePropertyDefinition;
      this.isDisabled_               = disabled;
      this.propertySubType_          = propertySubType;

      // if owner is null, have to delay this until the setOwner call
      // leave the assemblageConceptUUID null for now - it should be set to "getUUID()" but that isn't always ready
      // at the time this code runs.  We make sure it is set down below, in the getter.
      if ((columnInforForDynamicRefex == null) && (owner != null) && (owner_.getDefaultColumnInfo() != null)) {
         // Create a single required column, with the column name just set to 'value'
         dataColumnsForDynamicRefex_ = new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(null,
               0,
               DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_VALUE.getUUID(),
               owner_.getDefaultColumnInfo(),
               null,
               true,
               null,
               null,
               true) };
      } else {
         dataColumnsForDynamicRefex_ = columnInforForDynamicRefex;
      }

      if ((dataColumnsForDynamicRefex_ != null) && (owner_ != null) &&!owner_.createAsDynamicRefex()) {
         throw new RuntimeException("Tried to attach dynamic sememe data where it isn't allowed.");
      }
   }

   //~--- get methods ---------------------------------------------------------

   public DynamicSememeColumnInfo[] getDataColumnsForDynamicRefex() {
      if ((dataColumnsForDynamicRefex_ != null) &&
            (dataColumnsForDynamicRefex_.length == 1) &&
            (dataColumnsForDynamicRefex_[0].getAssemblageConcept() == null)) {
         dataColumnsForDynamicRefex_[0].setAssemblageConcept(getUUID());
      }

      return dataColumnsForDynamicRefex_;
   }

   public boolean isDisabled() {
      return isDisabled_;
   }

   public boolean isFromConceptSpec() {
      return isFromConceptSpec_;
   }

   //~--- set methods ---------------------------------------------------------

   protected void setOwner(PropertyType owner) {
      this.owner_ = owner;

      if ((dataColumnsForDynamicRefex_ == null) && (owner_.getDefaultColumnInfo() != null)) {
         // Create a single required column, with the column name concept tied back to the assemblage concept itself.
         // leave the assemblageConceptUUID null for now - it should be set to "getUUID()" but that isn't always ready
         // at the time this code runs.  We make sure it is set down below, in the getter.
         dataColumnsForDynamicRefex_ = new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(null,
               0,
               getUUID(),
               owner_.getDefaultColumnInfo(),
               null,
               true,
               null,
               null,
               true) };
      }

      if ((dataColumnsForDynamicRefex_ != null) &&!owner_.createAsDynamicRefex()) {
         throw new RuntimeException("Tried to attach dynamic sememe data where it isn't allowed.");
      }
   }

   //~--- get methods ---------------------------------------------------------

   public int getPropertySubType() {
      return propertySubType_;
   }

   //~--- set methods ---------------------------------------------------------

   public void setPropertySubType(int value) {
      this.propertySubType_ = value;
   }

   //~--- get methods ---------------------------------------------------------

   public PropertyType getPropertyType() {
      return owner_;
   }

   public String getSourcePropertyAltName() {
      return sourcePropertyAltName_;
   }

   public String getSourcePropertyDefinition() {
      return sourcePropertyDefinition_;
   }

   public String getSourcePropertyNameFSN() {
      return sourcePropertyNameFSN_;
   }

   public UUID getUUID() {
      if (propertyUUID == null) {
         propertyUUID = owner_.getPropertyUUID(this.sourcePropertyNameFSN_);
      }

      return propertyUUID;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Normally, we just create the relation names as specified.  However, some, we map to
    * other existing WB relationships, and put the source rel name on as an extension - for example
    * To enable the map case, set this (and use the appropriate addRelationship method)
    */
   public void setWBPropertyType(UUID wbRelType) {
      this.useWBPropertyTypeInstead = wbRelType;
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getWBTypeUUID() {
      return useWBPropertyTypeInstead;
   }
}

