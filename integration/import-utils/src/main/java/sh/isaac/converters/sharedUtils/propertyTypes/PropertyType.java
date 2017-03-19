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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;

//~--- classes ----------------------------------------------------------------

/**
 * Abstract base class to help in mapping code system property types into the workbench data model.
 *
 * The main purpose of this structure is to keep the UUID generation sane across the various
 * places where UUIDs are needed in the workbench.
 *
 * @author Daniel Armbrust
 */
public abstract class PropertyType {
   
   /** The src version. */
   protected static int srcVersion_ = 1;

   //~--- fields --------------------------------------------------------------

   /** The property type UUID. */
   private UUID propertyTypeUUID = null;
   
   /** The create as dynamic refex. */
   private boolean createAsDynamicRefex_ =
      false;  // It could make sense to set this at the individual Property level... but in general, everything of the same type
   
   /** The alt name property map. */
   private Map<String, String> altNamePropertyMap_ = null;
   
   /** The skip list. */
   protected List<String>      skipList_           = null;
   
   /** The property type description. */
   private final String              propertyTypeDescription_;

   /** The default data column. */
   // will be handled in the same way - relationships are not dynamic sememes, assoications are, for example.
   private final DynamicSememeDataType defaultDataColumn_;  // If the property is specified without further column instructions, and createAsDynamicRefex is true,

   // use this information to configure the (single) data column.

   /** The properties. */
   private final Map<String, Property> properties_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new property type.
    *
    * @param propertyTypeDescription - The name used for the property category within the terminology specific hierarchy -typically something like
    * "Attribute Types" or "Association Types".  This text is also used to construct the UUID for this property type grouping.
    * @param createAsDynamicRefex - true to mark as a dynamic refex, false otherwise.
    * @param defaultDynamicRefexColumnType - If the property is specified without further column instructions, and createAsDynamicRefex is true,
    * //use this information to configure the (single) data column.
    */
   protected PropertyType(String propertyTypeDescription,
                          boolean createAsDynamicRefex,
                          DynamicSememeDataType defaultDynamicRefexColumnType) {
      this.properties_              = new HashMap<String, Property>();
      this.propertyTypeDescription_ = propertyTypeDescription;
      this.createAsDynamicRefex_    = createAsDynamicRefex;
      this.defaultDataColumn_       = defaultDynamicRefexColumnType;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the property.
    *
    * @param property the property
    * @return the property
    */
   public Property addProperty(Property property) {
      if (this.skipList_ != null) {
         for (final String s: this.skipList_) {
            if (property.getSourcePropertyNameFSN()
                        .equals(s)) {
               ConsoleUtil.println("Skipping property '" + s + "' because of skip list configuration");
               return property;
            }
         }
      }

      property.setOwner(this);

      final Property old = this.properties_.put(property.getSourcePropertyNameFSN(), property);

      if (old != null) {
         throw new RuntimeException("Duplicate property name: " + property.getSourcePropertyNameFSN());
      }

      if ((this.altNamePropertyMap_ != null) && StringUtils.isNotEmpty(property.getSourcePropertyAltName())) {
         final String s = this.altNamePropertyMap_.put(property.getSourcePropertyAltName(), property.getSourcePropertyNameFSN());

         if (s != null) {
            throw new RuntimeException("Alt Indexing Error - duplicate!");
         }
      }

      return property;
   }

   /**
    * Adds the property.
    *
    * @param propertyNameFSN the property name FSN
    * @return the property
    */
   public Property addProperty(String propertyNameFSN) {
      return addProperty(propertyNameFSN, -1);
   }

   /**
    * Adds the property.
    *
    * @param propertyNameFSN the property name FSN
    * @param propertySubType the property sub type
    * @return the property
    */
   public Property addProperty(String propertyNameFSN, int propertySubType) {
      return addProperty(propertyNameFSN, null, null, false, propertySubType, null);
   }

   /**
    * Only adds the property if the version of the data file falls between min and max, inclusive.
    * pass 0 in min or max to specify no min or no max, respectively
    *
    * @param propertyNameFSN the property name FSN
    * @param minVersion the min version
    * @param maxVersion the max version
    * @return the property
    */
   public Property addProperty(String propertyNameFSN, int minVersion, int maxVersion) {
      return addProperty(propertyNameFSN, null, null, minVersion, maxVersion, false, -1);
   }

   /**
    * Adds the property.
    *
    * @param sourcePropertyNameFSN the source property name FSN
    * @param sourcePropertyAltName the source property alt name
    * @param sourcePropertyDefinition the source property definition
    * @return the property
    */
   public Property addProperty(String sourcePropertyNameFSN,
                               String sourcePropertyAltName,
                               String sourcePropertyDefinition) {
      return addProperty(sourcePropertyNameFSN, sourcePropertyAltName, sourcePropertyDefinition, false, -1, null);
   }

   /**
    * Only adds the property if the version of the data file falls between min and max, inclusive.
    * pass 0 in min or max to specify no min or no max, respectively
    *
    * @param propertyNameFSN the property name FSN
    * @param minVersion the min version
    * @param maxVersion the max version
    * @param disabled the disabled
    * @return the property
    */
   public Property addProperty(String propertyNameFSN, int minVersion, int maxVersion, boolean disabled) {
      return addProperty(propertyNameFSN, null, null, minVersion, maxVersion, disabled, -1);
   }

   /**
    * Adds the property.
    *
    * @param sourcePropertyNameFSN the source property name FSN
    * @param sourcePropertyAltName the source property alt name
    * @param sourcePropertyDefinition the source property definition
    * @param disabled the disabled
    * @param propertySubType the property sub type
    * @param dataColumnForDynamicRefex the data column for dynamic refex
    * @return the property
    */
   public Property addProperty(String sourcePropertyNameFSN,
                               String sourcePropertyAltName,
                               String sourcePropertyDefinition,
                               boolean disabled,
                               int propertySubType,
                               DynamicSememeColumnInfo[] dataColumnForDynamicRefex) {
      return addProperty(new Property(this,
                                      sourcePropertyNameFSN,
                                      sourcePropertyAltName,
                                      sourcePropertyDefinition,
                                      disabled,
                                      propertySubType,
                                      dataColumnForDynamicRefex));
   }

   /**
    * Only adds the property if the version of the data file falls between min and max, inclusive.
    * pass 0 in min or max to specify no min or no max, respectively
    *
    * @param sourcePropertyNameFSN the source property name FSN
    * @param altName the alt name
    * @param sourcePropertyDefinition the source property definition
    * @param minVersion the min version
    * @param maxVersion the max version
    * @param disabled the disabled
    * @param propertySubType the property sub type
    * @return the property
    */
   public Property addProperty(String sourcePropertyNameFSN,
                               String altName,
                               String sourcePropertyDefinition,
                               int minVersion,
                               int maxVersion,
                               boolean disabled,
                               int propertySubType) {
      if (((minVersion != 0) && (srcVersion_ < minVersion)) || ((maxVersion != 0) && (srcVersion_ > maxVersion))) {
         return null;
      }

      return addProperty(sourcePropertyNameFSN, altName, sourcePropertyDefinition, disabled, propertySubType, null);
   }

   /**
    * Contains property.
    *
    * @param propertyName the property name
    * @return true, if successful
    */
   public boolean containsProperty(String propertyName) {
      boolean result = this.properties_.containsKey(propertyName);

      if (!result && (this.altNamePropertyMap_ != null)) {
         final String altKey = this.altNamePropertyMap_.get(propertyName);

         if (altKey != null) {
            result = this.properties_.containsKey(altKey);
         }
      }

      return result;
   }

   /**
    * Creates the as dynamic refex.
    *
    * @return true, if successful
    */
   public boolean createAsDynamicRefex() {
      return this.createAsDynamicRefex_;
   }

   /**
    * Enable index and lookup of properties by their altName field.
    */
   public void indexByAltNames() {
      if (this.altNamePropertyMap_ == null) {
         this.altNamePropertyMap_ = new HashMap<>();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default column info.
    *
    * @return the default column info
    */
   protected DynamicSememeDataType getDefaultColumnInfo() {
      return this.defaultDataColumn_;
   }

   /**
    * Gets the properties.
    *
    * @return the properties
    */
   public Collection<Property> getProperties() {
      return this.properties_.values();
   }

   /**
    * Gets the property.
    *
    * @param propertyName the property name
    * @return the property
    */
   public Property getProperty(String propertyName) {
      Property p = this.properties_.get(propertyName);

      if ((p == null) && (this.altNamePropertyMap_ != null)) {
         final String altKey = this.altNamePropertyMap_.get(propertyName);

         if (altKey != null) {
            p = this.properties_.get(altKey);
         }
      }

      return p;
   }

   /**
    * Gets the property names.
    *
    * @return the property names
    */
   public Set<String> getPropertyNames() {
      return this.properties_.keySet();
   }

   /**
    * Gets the property type description.
    *
    * @return the property type description
    */
   public String getPropertyTypeDescription() {
      return this.propertyTypeDescription_;
   }

   /**
    * Gets the property type UUID.
    *
    * @return the property type UUID
    */
   public UUID getPropertyTypeUUID() {
      if (this.propertyTypeUUID == null) {
         this.propertyTypeUUID = ConverterUUID.createNamespaceUUIDFromString(this.propertyTypeDescription_);
      }

      return this.propertyTypeUUID;
   }

   /**
    * Gets the property UUID.
    *
    * @param propertyName the property name
    * @return the property UUID
    */
   protected UUID getPropertyUUID(String propertyName) {
      return ConverterUUID.createNamespaceUUIDFromString(this.propertyTypeDescription_ + ":" + propertyName);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the source version.
    *
    * @param version the new source version
    */
   public static void setSourceVersion(int version) {
      srcVersion_ = version;
   }
}

