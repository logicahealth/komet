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



package sh.isaac.converters.sharedUtils.stats;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;

//~--- classes ----------------------------------------------------------------

/**
 * A utility class for generating UUIDs which keeps track of what was used to generate the UUIDs - which
 * can then be dumped to disk (or looked up by UUID)
 *
 * The in-memory map can be disabled by setting the static flag here - or - with loaders that extend {@link ConverterBaseMojo}
 * by setting the system property skipUUIDDebug to true - or in maven speak - '-DskipUUIDDebug' on the command line.
 *
 * @author darmbrust
 * TODO: [KEC] evaluate the utility of this class.
 */
public class ConverterUUID {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

 //TODO [DAN 2] this needs to be made non static / non global, now that is is being pulled into some runtime use cases...
   /** The disable UUID map flag. */
   public static boolean disableUUIDMap = false;  // Some loaders need to disable this due to memory constraints

   /** The master UUID map. */
   private static final ConcurrentHashMap<UUID, String> masterUUIDMap = new ConcurrentHashMap<UUID, String>();

   /** The namespace. */
   private static UUID namespace = null;

   private static ConceptSpecification[] CONSTANTS;
   static {
      List<ConceptSpecification> constantsList = new ArrayList<>();
      try {
         // Add MetaData static class member concepts
         for (Field field : MetaData.class.getDeclaredFields()) {
            if (ConceptSpecification.class.isAssignableFrom(field.getType())) {
               constantsList.add((ConceptSpecification)field.get(MetaData.class));
            }
         }
         // Add DynamicSememeConstants instance member concepts
         for (Field field : DynamicConstants.class.getDeclaredFields()) {
            if (ConceptSpecification.class.isAssignableFrom(field.getType())) {
               constantsList.add((ConceptSpecification)field.get(DynamicConstants.get()));
            }
         }
         
         CONSTANTS = constantsList.toArray(new ConceptSpecification[constantsList.size()]);
      } catch (Exception e) {
         // This is just a fall back in case the initialization by reflection fails
         CONSTANTS = new ConceptSpecification[] { 
               MetaData.IS_A____SOLOR, MetaData.REGULAR_NAME____SOLOR,
               MetaData.FULLY_QUALIFIED_NAME____SOLOR, MetaData.DEFINITION____SOLOR,
               MetaData.US_ENGLISH_DIALECT____SOLOR, MetaData.GB_ENGLISH_DIALECT____SOLOR,
               MetaData.CONVERTED_IBDF_ARTIFACT_CLASSIFIER____SOLOR,
               MetaData.CONVERTED_IBDF_ARTIFACT_VERSION____SOLOR, MetaData.CONVERTER_VERSION____SOLOR,
               MetaData.SOURCE_ARTIFACT_VERSION____SOLOR, MetaData.SOURCE_RELEASE_DATE____SOLOR,
               MetaData.SCTID____SOLOR, MetaData.IDENTIFIER_SOURCE____SOLOR, DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION,
               DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION,
               DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION,
               DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION, DynamicConstants.get().DYNAMIC_ASSOCIATION };
      }
   }

  //~--- methods -------------------------------------------------------------

   /**
    * Allow this map to be updated with UUIDs that were not generated via this utility class.
    *
    * @param value the value
    * @param uuid the uuid
    */
   public static void addMapping(String value, UUID uuid) {
      if (!disableUUIDMap) {
         final String putResult = masterUUIDMap.put(uuid, value);

         if (putResult != null) {
            throw new RuntimeException("Just made a duplicate UUID! '" + value + "' -> " + uuid);
         }
      }
   }

   /**
    * Clear cache.
    */
   public static void clearCache() {
      masterUUIDMap.clear();
   }

   /**
    * Configure namespace.
    *
    * @param namespace the namespace
    */
   public static void configureNamespace(UUID namespace) {
      if (namespace != null)
      {
         ConsoleUtil.println("Reconfiguring Namespace!");
      }
      ConverterUUID.namespace = namespace;
   }

   /**
    * Create a new Type5 UUID using the provided name as the seed in the configured namespace.
    *
    * Throws a runtime exception if the namespace has not been configured.
    *
    * @param name the name
    * @return the uuid
    */
   public static UUID createNamespaceUUIDFromString(String name) {
      return createNamespaceUUIDFromString(name, false);
   }

   /**
    * Create a new Type5 UUID using the provided namespace, and provided name as the seed.
    *
    * @param namespace the namespace
    * @param name the name
    * @return the uuid
    */
   public static UUID createNamespaceUUIDFromString(UUID namespace, String name) {
      return createNamespaceUUIDFromString(namespace, name, false);
   }

   /**
    * Create a new Type5 UUID using the provided name as the seed in the configured namespace.
    *
    * Throws a runtime exception if the namespace has not been configured.
    *
    * @param name the name
    * @param skipDupeCheck can be used to bypass the duplicate checking function - useful in cases where you know
    * you are creating the same UUID more than once.  Normally, this method throws a runtime exception
    * if the same UUID is generated more than once.
    * @return the uuid
    */
   public static UUID createNamespaceUUIDFromString(String name, boolean skipDupeCheck) {
      initCheck();
      return createNamespaceUUIDFromString(namespace, name, skipDupeCheck);
   }

   /**
    * Create a new Type5 UUID using the provided namespace, and provided name as the seed.
    *
    * @param namespace the namespace
    * @param name the name
    * @param skipDupeCheck can be used to bypass the duplicate checking function - useful in cases where you know
    * you are creating the same UUID more than once.  Normally, this method throws a runtime exception
    * if the same UUID is generated more than once.
    * @return the uuid
    */
   public static UUID createNamespaceUUIDFromString(UUID namespace, String name, boolean skipDupeCheck) {
      UUID uuid;

      try {
         uuid = UuidT5Generator.get(namespace, name);
      } catch (final Exception e) {
         throw new RuntimeException("Unexpected error configuring UUID generator");
      }

      if (!disableUUIDMap) {
         final String putResult = masterUUIDMap.put(uuid, name);

         if (!skipDupeCheck && (putResult != null)) {
            throw new RuntimeException("Just made a duplicate UUID! '" + name + "' -> " + uuid);
         }
      }

      return uuid;
   }

   /**
    * Create a new Type5 UUID using the provided name as the seed in the configured namespace.
    *
    * Throws a runtime exception if the namespace has not been configured.
    *
    * @param values the values
    * @return the uuid
    */
   public static UUID createNamespaceUUIDFromStrings(String... values) {
      final StringBuilder uuidKey = new StringBuilder();

      for (final String s: values) {
         if (s != null) {
            uuidKey.append(s);
            uuidKey.append("|");
         }
      }

      if (uuidKey.length() > 1) {
         uuidKey.setLength(uuidKey.length() - 1);
      } else {
         throw new RuntimeException("No string provided!");
      }

      return createNamespaceUUIDFromString(uuidKey.toString());
   }

   /**
    * Write out a debug file with all of the UUID - String mappings.
    *
    * @param outputDirectory the output directory
    * @param prefix the prefix
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void dump(File outputDirectory, String prefix)
            throws IOException {
      try (BufferedWriter br = new BufferedWriter(new FileWriter(new File(outputDirectory,
                                                                          prefix + "DebugMap.txt")));) {
         if (disableUUIDMap) {
            ConsoleUtil.println("UUID Debug map was disabled");
            br.write("Note - the UUID debug feature was disabled, this file is incomplete" +
                     System.getProperty("line.separator"));
         }

         for (final Map.Entry<UUID, String> entry: masterUUIDMap.entrySet()) {
            br.write(entry.getKey() + " - " + entry.getValue() + System.getProperty("line.separator"));
         }
      }
   }

   /**
    * In some scenarios, it isn't desirable to cache every creation string - allow the removal in these cases.
    *
    * @param uuid the uuid
    */
   public static void removeMapping(UUID uuid) {
      masterUUIDMap.remove(uuid);
   }

   /**
    * Inits the check.
    */
   private static void initCheck() {
      if (namespace == null) {
         throw new RuntimeException("Namespace UUID has not yet been initialized");
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the namespace.
    *
    * @return the namespace
    */
   public static UUID getNamespace() {
      return namespace;
   }

   /**
    * Return the string that was used to generate this UUID (if available - null if not).
    *
    * @param uuid the uuid
    * @return the UUID creation string
    */
   public static String getUUIDCreationString(UUID uuid) {
      if (uuid == null) {
         return null;
      }

      final String found = masterUUIDMap.get(uuid);

      if (found == null) {
         for (final ConceptSpecification cs: CONSTANTS) {
            if (uuid.equals(cs.getPrimordialUuid())) {
               return cs.getFullyQualifiedName();
            }
         }
      }

      return found;
   }
}

