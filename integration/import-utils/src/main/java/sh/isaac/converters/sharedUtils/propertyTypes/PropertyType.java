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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
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
	
	private static final Logger LOG = LogManager.getLogger();
	
   /** The src version. */
   protected static int srcVersion = 1;

   //~--- fields --------------------------------------------------------------

   /** The property type UUID. */
   private UUID propertyTypeUUID = null;

   /** The create as dynamic refex. */
   private boolean createAsDynamicRefex =
      false;  // It could make sense to set this at the individual Property level... but in general, everything of the same type

   /** The alt name property map. */
   private Map<String, String> altNamePropertyMap = null;

   /** The skip list. */
   protected List<String> skipList = null;

   /** The property type description. */
   private final String propertyTypeDescription;

   /** The default data column. */

   // will be handled in the same way - relationships are not dynamic, assoications are, for example.
   private final DynamicDataType defaultDataColumn;  // If the property is specified without further column instructions, and createAsDynamicRefex is true,

   // use this information to configure the (single) data column.

   /** The properties. */
   private final Map<String, Property> properties;

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
                          DynamicDataType defaultDynamicRefexColumnType) {
      this.properties              = new HashMap<>();
      this.propertyTypeDescription = propertyTypeDescription;
      this.createAsDynamicRefex    = createAsDynamicRefex;
      this.defaultDataColumn       = defaultDynamicRefexColumnType;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the property.
    *
    * @param property the property
    * @return the property
    */
   public Property addProperty(Property property) {
      if (this.skipList != null) {
         for (final String s: this.skipList) {
            if (property.getSourcePropertyNameFQN().equals(s) || property.getSourcePropertyAltName().equals(s)) {
               ConsoleUtil.println("Skipping property '" + s + "' because of skip list configuration");
               return property;
            }
         }
      }

      property.setOwner(this);

      final Property old = this.properties.put(property.getSourcePropertyNameFQN(), property);

      if (old != null) {
         throw new RuntimeException("Duplicate property name: " + property.getSourcePropertyNameFQN());
      }

      if ((this.altNamePropertyMap != null) && StringUtils.isNotEmpty(property.getSourcePropertyAltName())) {
         final String s = this.altNamePropertyMap.put(property.getSourcePropertyAltName(),
                                                       property.getSourcePropertyNameFQN());

         if (s != null) {
            throw new RuntimeException("Alt Indexing Error - duplicate!");
         }
      }

      return property;
   }
   
   /**
    * Add an additional alt name that can be used to look up a particular property.  Property needs to have already been added.
    * @param property
    * @param altName
    */
   public void addPropertyAltName(Property property, String altName) {
      final String s = this.altNamePropertyMap.put(altName, property.getSourcePropertyNameFQN());
      if (s != null) {
         throw new RuntimeException("Alt Indexing Error - duplicate!");
      }
   }

   /**
    * Adds the property.
    *
    * @param propertyNameFQN the property name FQN
    * @return the property
    */
   public Property addProperty(String propertyNameFQN) {
      return addProperty(propertyNameFQN, -1, false);
   }
   
   /**
    * Adds the property.
    *
    * @param propertyNameFQN the property name FQN
    * @return the property
    */
   public Property addProperty(String propertyNameFQN, boolean isIdentifier) {
      return addProperty(propertyNameFQN, -1, isIdentifier);
   }
   
   public Property addProperty(PropertyType owner, ConceptSpecification cs, boolean isIdentifier) {
     return addProperty(new Property(owner, cs, isIdentifier));
     }
   
   public Property addProperty(ConceptSpecification cs, boolean isIdentifier) {
      return addProperty(new Property((PropertyType)null, cs, isIdentifier));
   }
   
   /**
    * @param propertyNameFQN
    * @param propertySubType
    * @return
    */
   public Property addProperty(String propertyNameFQN, int propertySubType) {
      return addProperty(propertyNameFQN, propertySubType, false);
   }

   /**
    * Adds the property.
    *
    * @param propertyNameFQN the property name FQN
    * @param propertySubType the property sub type
    * @return the property
    */
   public Property addProperty(String propertyNameFQN, int propertySubType, boolean isIdentifier) {
      return addProperty(propertyNameFQN, null, null, false, isIdentifier, propertySubType, null);
   }

   /**
    * Only adds the property if the version of the data file falls between min and max, inclusive.
    * pass 0 in min or max to specify no min or no max, respectively
    *
    * @param propertyNameFQN the property name FQN
    * @param minVersion the min version
    * @param maxVersion the max version
    * @return the property
    */
   public Property addProperty(String propertyNameFQN, int minVersion, int maxVersion) {
      return addProperty(propertyNameFQN, null, null, minVersion, maxVersion, false, -1);
   }

   /**
    * Adds the property.
    *
    * @param sourcePropertyNameFQN the source property name FQN
    * @param sourcePropertyAltName the source property alt name
    * @param sourcePropertyDefinition the source property definition
    * @return the property
    */
   public Property addProperty(String sourcePropertyNameFQN,
                               String sourcePropertyAltName,
                               String sourcePropertyDefinition) {
      return addProperty(sourcePropertyNameFQN, sourcePropertyAltName, sourcePropertyDefinition, false, -1, null);
   }

   /**
    * Only adds the property if the version of the data file falls between min and max, inclusive.
    * pass 0 in min or max to specify no min or no max, respectively
    *
    * @param propertyNameFQN the property name FQN
    * @param minVersion the min version
    * @param maxVersion the max version
    * @param disabled the disabled
    * @return the property
    */
   public Property addProperty(String propertyNameFQN, int minVersion, int maxVersion, boolean disabled) {
      return addProperty(propertyNameFQN, null, null, minVersion, maxVersion, disabled, -1);
   }

   /**
    * Adds the property.
    *
    * @param sourcePropertyNameFQN the source property name FQN
    * @param sourcePropertyAltName the source property alt name
    * @param sourcePropertyDefinition the source property definition
    * @param disabled the disabled
    * @param propertySubType the property sub type
    * @param dataColumnForDynamicRefex the data column for dynamic refex
    * @return the property
    */
   public Property addProperty(String sourcePropertyNameFQN,
                               String sourcePropertyAltName,
                               String sourcePropertyDefinition,
                               boolean disabled,
                               int propertySubType,
                               DynamicColumnInfo[] dataColumnForDynamicRefex) {
      return addProperty(sourcePropertyNameFQN, sourcePropertyAltName, sourcePropertyDefinition, disabled, false, propertySubType, dataColumnForDynamicRefex);
   }
   
   public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyAltName, String sourcePropertyDefinition, boolean isIdentifier) {
      return addProperty(new Property(this, sourcePropertyNameFSN, sourcePropertyAltName, sourcePropertyDefinition, isIdentifier));
   }
   
   /**
    * Adds the property.
    *
    * @param sourcePropertyNameFQN the source property name FQN
    * @param sourcePropertyAltName the source property alt name
    * @param sourcePropertyDefinition the source property definition
    * @param disabled the disabled
    * @param propertySubType the property sub type
    * @param dataColumnForDynamicRefex the data column for dynamic refex
    * @return the property
    */
   public Property addProperty(String sourcePropertyNameFQN,
                               String sourcePropertyAltName,
                               String sourcePropertyDefinition,
                               boolean disabled,
                               boolean isIdentifier,
                               int propertySubType,
                               DynamicColumnInfo[] dataColumnForDynamicRefex) {
      return addProperty(new Property(this,
                                      sourcePropertyNameFQN,
                                      sourcePropertyAltName,
                                      sourcePropertyDefinition,
                                      disabled,
                                      isIdentifier,
                                      propertySubType,
                                      dataColumnForDynamicRefex));
   }

   /**
    * Only adds the property if the version of the data file falls between min and max, inclusive.
    * pass 0 in min or max to specify no min or no max, respectively
    *
    * @param sourcePropertyNameFQN the source property name FQN
    * @param altName the alt name
    * @param sourcePropertyDefinition the source property definition
    * @param minVersion the min version
    * @param maxVersion the max version
    * @param disabled the disabled
    * @param propertySubType the property sub type
    * @return the property
    */
   public Property addProperty(String sourcePropertyNameFQN,
                               String altName,
                               String sourcePropertyDefinition,
                               int minVersion,
                               int maxVersion,
                               boolean disabled,
                               int propertySubType) {
      if (((minVersion != 0) && (srcVersion < minVersion)) || ((maxVersion != 0) && (srcVersion > maxVersion))) {
         return null;
      }

      return addProperty(sourcePropertyNameFQN, altName, sourcePropertyDefinition, disabled, propertySubType, null);
   }

   /**
    * Contains property.
    *
    * @param propertyName the property name
    * @return true, if successful
    */
   public boolean containsProperty(String propertyName) {
      boolean result = this.properties.containsKey(propertyName);

      if (!result && (this.altNamePropertyMap != null)) {
         final String altKey = this.altNamePropertyMap.get(propertyName);

         if (altKey != null) {
            result = this.properties.containsKey(altKey);
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
      return this.createAsDynamicRefex;
   }

   /**
    * Enable index and lookup of properties by their altName field.
    */
   public void indexByAltNames() {
      if (this.altNamePropertyMap == null) {
         this.altNamePropertyMap = new HashMap<>();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default column info.
    *
    * @return the default column info
    */
   protected DynamicDataType getDefaultColumnInfo() {
      return this.defaultDataColumn;
   }

   /**
    * Gets the properties.
    *
    * @return the properties
    */
   public Collection<Property> getProperties() {
      return this.properties.values();
   }

	/**
	 * Gets the property.
	 *
	 * @param propertyName the property name
	 * @return the property
	 */
	public Property getProperty(String propertyName) {
		Optional<Property> p = getPropertyOptional(propertyName);
		if (!p.isPresent()) {
			LOG.warn("Failed to find property for {} in {}", propertyName, this.getPropertyTypeDescription());
		}
		return p.orElse(null);
	}
   
   
   /**
    * Gets the property.
    *
    * @param propertyName the property name
    * @return the property
    */
   public Optional<Property> getPropertyOptional(String propertyName) {
      Property p = this.properties.get(propertyName);

      if ((p == null) && (this.altNamePropertyMap != null)) {
         final String altKey = this.altNamePropertyMap.get(propertyName);

         if (altKey != null) {
            p = this.properties.get(altKey);
         }
      }
      return Optional.ofNullable(p);
   }

   /**
    * Gets the property names.
    *
    * @return the property names
    */
   public Set<String> getPropertyNames() {
      return this.properties.keySet();
   }

   /**
    * Gets the property type description.
    *
    * @return the property type description
    */
   public String getPropertyTypeDescription() {
      return this.propertyTypeDescription;
   }

   /**
    * Gets the property type UUID.
    *
    * @return the property type UUID
    */
   public UUID getPropertyTypeUUID() {
      if (this.propertyTypeUUID == null) {
         this.propertyTypeUUID = ConverterUUID.createNamespaceUUIDFromString(this.propertyTypeDescription);
         Get.identifierService().assignNid(this.propertyTypeUUID);
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
      return ConverterUUID.createNamespaceUUIDFromString(this.propertyTypeDescription + ":" + propertyName);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the source version.
    *
    * @param version the new source version
    */
   public static void setSourceVersion(int version) {
      srcVersion = version;
   }
}

