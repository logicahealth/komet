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
   protected static int srcVersion_ = 1;

   //~--- fields --------------------------------------------------------------

   private UUID propertyTypeUUID = null;
   private boolean createAsDynamicRefex_ =
      false;  // It could make sense to set this at the individual Property level... but in general, everything of the same type
   private Map<String, String> altNamePropertyMap_ = null;
   protected List<String>      skipList_           = null;
   private String              propertyTypeDescription_;

   // will be handled in the same way - relationships are not dynamic sememes, assoications are, for example.
   private DynamicSememeDataType defaultDataColumn_;  // If the property is specified without further column instructions, and createAsDynamicRefex is true,

   // use this information to configure the (single) data column.

   private Map<String, Property> properties_;

   //~--- constructors --------------------------------------------------------

   /**
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

   public Property addProperty(Property property) {
      if (skipList_ != null) {
         for (String s: skipList_) {
            if (property.getSourcePropertyNameFSN()
                        .equals(s)) {
               ConsoleUtil.println("Skipping property '" + s + "' because of skip list configuration");
               return property;
            }
         }
      }

      property.setOwner(this);

      Property old = properties_.put(property.getSourcePropertyNameFSN(), property);

      if (old != null) {
         throw new RuntimeException("Duplicate property name: " + property.getSourcePropertyNameFSN());
      }

      if ((altNamePropertyMap_ != null) && StringUtils.isNotEmpty(property.getSourcePropertyAltName())) {
         String s = altNamePropertyMap_.put(property.getSourcePropertyAltName(), property.getSourcePropertyNameFSN());

         if (s != null) {
            throw new RuntimeException("Alt Indexing Error - duplicate!");
         }
      }

      return property;
   }

   public Property addProperty(String propertyNameFSN) {
      return addProperty(propertyNameFSN, -1);
   }

   public Property addProperty(String propertyNameFSN, int propertySubType) {
      return addProperty(propertyNameFSN, null, null, false, propertySubType, null);
   }

   /**
    * Only adds the property if the version of the data file falls between min and max, inclusive.
    * pass 0 in min or max to specify no min or no max, respectively
    */
   public Property addProperty(String propertyNameFSN, int minVersion, int maxVersion) {
      return addProperty(propertyNameFSN, null, null, minVersion, maxVersion, false, -1);
   }

   public Property addProperty(String sourcePropertyNameFSN,
                               String sourcePropertyAltName,
                               String sourcePropertyDefinition) {
      return addProperty(sourcePropertyNameFSN, sourcePropertyAltName, sourcePropertyDefinition, false, -1, null);
   }

   /**
    * Only adds the property if the version of the data file falls between min and max, inclusive.
    * pass 0 in min or max to specify no min or no max, respectively
    */
   public Property addProperty(String propertyNameFSN, int minVersion, int maxVersion, boolean disabled) {
      return addProperty(propertyNameFSN, null, null, minVersion, maxVersion, disabled, -1);
   }

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

   public boolean containsProperty(String propertyName) {
      boolean result = properties_.containsKey(propertyName);

      if (!result && (altNamePropertyMap_ != null)) {
         String altKey = altNamePropertyMap_.get(propertyName);

         if (altKey != null) {
            result = properties_.containsKey(altKey);
         }
      }

      return result;
   }

   public boolean createAsDynamicRefex() {
      return createAsDynamicRefex_;
   }

   /**
    * Enable index and lookup of properties by their altName field
    */
   public void indexByAltNames() {
      if (altNamePropertyMap_ == null) {
         altNamePropertyMap_ = new HashMap<>();
      }
   }

   //~--- get methods ---------------------------------------------------------

   protected DynamicSememeDataType getDefaultColumnInfo() {
      return defaultDataColumn_;
   }

   public Collection<Property> getProperties() {
      return properties_.values();
   }

   public Property getProperty(String propertyName) {
      Property p = properties_.get(propertyName);

      if ((p == null) && (altNamePropertyMap_ != null)) {
         String altKey = altNamePropertyMap_.get(propertyName);

         if (altKey != null) {
            p = properties_.get(altKey);
         }
      }

      return p;
   }

   public Set<String> getPropertyNames() {
      return properties_.keySet();
   }

   public String getPropertyTypeDescription() {
      return propertyTypeDescription_;
   }

   public UUID getPropertyTypeUUID() {
      if (propertyTypeUUID == null) {
         propertyTypeUUID = ConverterUUID.createNamespaceUUIDFromString(propertyTypeDescription_);
      }

      return propertyTypeUUID;
   }

   protected UUID getPropertyUUID(String propertyName) {
      return ConverterUUID.createNamespaceUUIDFromString(propertyTypeDescription_ + ":" + propertyName);
   }

   //~--- set methods ---------------------------------------------------------

   public static void setSourceVersion(int version) {
      srcVersion_ = version;
   }
}

