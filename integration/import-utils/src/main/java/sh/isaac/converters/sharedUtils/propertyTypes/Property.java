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
   /** The is disabled. */
   private boolean isDisabled = false;

   /** The is from concept spec. */
   private boolean isFromConceptSpec = false;

   /** The property sub type. */
   private int propertySubType =
      Integer.MAX_VALUE;  // Used for subtypes of descriptions, at the moment - FSN, synonym, etc.

   /** The property UUID. */
   private UUID propertyUUID = null;

   /** The use WB property type instead. */
   private UUID useWBPropertyTypeInstead = null;  // see comments in setter

   /** The data columns for dynamic refex. */
   private DynamicSememeColumnInfo[] dataColumnsForDynamicRefex = null;

   /** The source property name FS N. */
   private final String sourcePropertyNameFSN;

   /** The source property alt name. */
   private final String sourcePropertyAltName;

   /** The source property definition. */
   private final String sourcePropertyDefinition;

   /** The owner. */
   private PropertyType owner;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new property.
    *
    * @param owner the owner
    * @param cs the cs
    */
   public Property(PropertyType owner, ConceptSpecification cs) {
      this(owner, cs.getConceptDescriptionText(), null, null, false, Integer.MAX_VALUE, null);
      this.propertyUUID = cs.getPrimordialUuid();
      ConverterUUID.addMapping(cs.getConceptDescriptionText(), cs.getPrimordialUuid());
      this.isFromConceptSpec = true;
   }

   /**
    * Instantiates a new property.
    *
    * @param owner the owner
    * @param sourcePropertyNameFSN the source property name FSN
    */
   public Property(PropertyType owner, String sourcePropertyNameFSN) {
      this(owner, sourcePropertyNameFSN, null, null, false, Integer.MAX_VALUE, null);
   }

   /**
    * owner must be set via the set method after using this constructor!.
    *
    * @param sourcePropertyNameFSN the source property name FSN
    * @param sourcePropertyAltName the source property alt name
    * @param sourcePropertyDefinition the source property definition
    * @param wbRelType the wb rel type
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
    * Instantiates a new property.
    *
    * @param owner the owner
    * @param sourcePropertyNameFSN the source property name FSN
    * @param sourcePropertyAltName the source property alt name
    * @param sourcePropertyDefinition the source property definition
    * @param disabled the disabled
    * @param propertySubType the property sub type
    * @param columnInforForDynamicRefex the column infor for dynamic refex
    */
   public Property(PropertyType owner,
                   String sourcePropertyNameFSN,
                   String sourcePropertyAltName,
                   String sourcePropertyDefinition,
                   boolean disabled,
                   int propertySubType,
                   DynamicSememeColumnInfo[] columnInforForDynamicRefex) {
      this.owner                    = owner;
      this.sourcePropertyNameFSN    = sourcePropertyNameFSN;
      this.sourcePropertyAltName    = sourcePropertyAltName;
      this.sourcePropertyDefinition = sourcePropertyDefinition;
      this.isDisabled               = disabled;
      this.propertySubType          = propertySubType;

      // if owner is null, have to delay this until the setOwner call
      // leave the assemblageConceptUUID null for now - it should be set to "getUUID()" but that isn't always ready
      // at the time this code runs.  We make sure it is set down below, in the getter.
      if ((columnInforForDynamicRefex == null) && (owner != null) && (this.owner.getDefaultColumnInfo() != null)) {
         // Create a single required column, with the column name just set to 'value'
         this.dataColumnsForDynamicRefex = new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(null,
               0,
               DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_VALUE.getUUID(),
               this.owner.getDefaultColumnInfo(),
               null,
               true,
               null,
               null,
               true) };
      } else {
         this.dataColumnsForDynamicRefex = columnInforForDynamicRefex;
      }

      if ((this.dataColumnsForDynamicRefex != null) && (this.owner != null) &&!this.owner.createAsDynamicRefex()) {
         throw new RuntimeException("Tried to attach dynamic sememe data where it isn't allowed.");
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data columns for dynamic refex.
    *
    * @return the data columns for dynamic refex
    */
   public DynamicSememeColumnInfo[] getDataColumnsForDynamicRefex() {
      if ((this.dataColumnsForDynamicRefex != null) &&
            (this.dataColumnsForDynamicRefex.length == 1) &&
            (this.dataColumnsForDynamicRefex[0].getAssemblageConcept() == null)) {
         this.dataColumnsForDynamicRefex[0].setAssemblageConcept(getUUID());
      }

      return this.dataColumnsForDynamicRefex;
   }

   /**
    * Checks if disabled.
    *
    * @return true, if disabled
    */
   public boolean isDisabled() {
      return this.isDisabled;
   }

   /**
    * Checks if from concept spec.
    *
    * @return true, if from concept spec
    */
   public boolean isFromConceptSpec() {
      return this.isFromConceptSpec;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the owner.
    *
    * @param owner the new owner
    */
   protected void setOwner(PropertyType owner) {
      this.owner = owner;

      if ((this.dataColumnsForDynamicRefex == null) && (this.owner.getDefaultColumnInfo() != null)) {
         // Create a single required column, with the column name concept tied back to the assemblage concept itself.
         // leave the assemblageConceptUUID null for now - it should be set to "getUUID()" but that isn't always ready
         // at the time this code runs.  We make sure it is set down below, in the getter.
         this.dataColumnsForDynamicRefex = new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(null,
               0,
               getUUID(),
               this.owner.getDefaultColumnInfo(),
               null,
               true,
               null,
               null,
               true) };
      }

      if ((this.dataColumnsForDynamicRefex != null) &&!this.owner.createAsDynamicRefex()) {
         throw new RuntimeException("Tried to attach dynamic sememe data where it isn't allowed.");
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the property sub type.
    *
    * @return the property sub type
    */
   public int getPropertySubType() {
      return this.propertySubType;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the property sub type.
    *
    * @param value the new property sub type
    */
   public void setPropertySubType(int value) {
      this.propertySubType = value;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the property type.
    *
    * @return the property type
    */
   public PropertyType getPropertyType() {
      return this.owner;
   }

   /**
    * Gets the source property alt name.
    *
    * @return the source property alt name
    */
   public String getSourcePropertyAltName() {
      return this.sourcePropertyAltName;
   }

   /**
    * Gets the source property definition.
    *
    * @return the source property definition
    */
   public String getSourcePropertyDefinition() {
      return this.sourcePropertyDefinition;
   }

   /**
    * Gets the source property name FSN.
    *
    * @return the source property name FSN
    */
   public String getSourcePropertyNameFSN() {
      return this.sourcePropertyNameFSN;
   }

   /**
    * Gets the uuid.
    *
    * @return the uuid
    */
   public UUID getUUID() {
      if (this.propertyUUID == null) {
         this.propertyUUID = this.owner.getPropertyUUID(this.sourcePropertyNameFSN);
      }

      return this.propertyUUID;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Normally, we just create the relation names as specified.  However, some, we map to
    * other existing WB relationships, and put the source rel name on as an extension - for example
    * To enable the map case, set this (and use the appropriate addRelationship method)
    *
    * @param wbRelType the new WB property type
    */
   public void setWBPropertyType(UUID wbRelType) {
      this.useWBPropertyTypeInstead = wbRelType;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the WB type UUID.
    *
    * @return the WB type UUID
    */
   public UUID getWBTypeUUID() {
      return this.useWBPropertyTypeInstead;
   }
}

