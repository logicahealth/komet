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



package sh.isaac.api.component.semantic.version.dynamic;

//~--- JDK imports ------------------------------------------------------------

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Rf2Relationship;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicBoolean;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicByteArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicDouble;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicFloat;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicInteger;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicLong;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicPolymorphic;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.UuidT3Generator;

//~--- enums ------------------------------------------------------------------

/**
 *
 * {@link DynamicDataType}
 *
 * Most types are fairly straight forward.  NIDs, SEQUENCES and INTEGERS are identical internally.
 * Polymorphic is used when the data type for a dynamic isn't known at dynamic creation time.  In this case, a user of the API
 * will have to examine type types of the actual {@link DynamicData} objects returned, to look at the type.
 *
 * For all other types, the data type reported within the Refex Definition should exactly match the data type returned with
 * a {@link DynamicData}.
 *
 * {@link DynamicData} will never return a {@link DynamicDataType#POLYMORPHIC} type.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum DynamicDataType {
   /** The nid. */
   NID(101, DynamicNid.class, "Component Nid"),

   /** The string. */
   STRING(102, DynamicString.class, "String"),

   /** The integer. */
   INTEGER(103, DynamicInteger.class, "Integer"),

   /** The boolean. */
   BOOLEAN(104, DynamicBoolean.class, "Boolean"),

   /** The long. */
   LONG(105, DynamicLong.class, "Long"),

   /** The bytearray. */
   BYTEARRAY(106, DynamicByteArray.class, "Arbitrary Data"),

   /** The float. */
   FLOAT(107, DynamicFloat.class, "Float"),

   /** The double. */
   DOUBLE(108, DynamicDouble.class, "Double"),

   /** The uuid. */
   UUID(109, DynamicUUID.class, "UUID"),

   /** The polymorphic. */
   POLYMORPHIC(110, DynamicPolymorphic.class, "Unspecified"),

   /** The array. */
   ARRAY(111, DynamicArray.class, "Array"),

   /** The unknown. */
   UNKNOWN(Byte.MAX_VALUE, null, "Unknown");

   /** The externalized token. */
   private int externalizedToken;

   /** The data class. */
   private Class<? extends DynamicData> dataClass;

   /** The display name. */
   private String displayName;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic data type.
    *
    * @param externalizedToken the externalized token
    * @param dataClass the data class
    * @param displayName the display name
    */
   private DynamicDataType(int externalizedToken,
                                 Class<? extends DynamicData> dataClass,
                                 String displayName) {
      this.externalizedToken = externalizedToken;
      this.dataClass         = dataClass;
      this.displayName       = displayName;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Class to type.
    *
    * @param c the c
    * @return the dynamic data type
    */
   public static DynamicDataType classToType(Class<?> c) {
      if (DynamicNid.class.isAssignableFrom(c)) {
         return NID;
      }

      if (DynamicString.class.isAssignableFrom(c)) {
         return STRING;
      }

      if (DynamicInteger.class.isAssignableFrom(c)) {
         return INTEGER;
      }

      if (DynamicBoolean.class.isAssignableFrom(c)) {
         return BOOLEAN;
      }

      if (DynamicLong.class.isAssignableFrom(c)) {
         return LONG;
      }

      if (DynamicByteArray.class.isAssignableFrom(c)) {
         return BYTEARRAY;
      }

      if (DynamicFloat.class.isAssignableFrom(c)) {
         return FLOAT;
      }

      if (DynamicDouble.class.isAssignableFrom(c)) {
         return DOUBLE;
      }

      if (DynamicUUID.class.isAssignableFrom(c)) {
         return UUID;
      }

      if (DynamicPolymorphic.class.isAssignableFrom(c)) {
         return POLYMORPHIC;
      }

      if (DynamicArray.class.isAssignableFrom(c)) {
         return ARRAY;
      }

      LogManager.getLogger()
                .warn("Couldn't map class {} to type!", c);
      return UNKNOWN;
   }

   /**
    * Parses the.
    *
    * @param nameOrTokenOrEnumId the name or token or enum id
    * @param exceptionOnParseFail the exception on parse fail
    * @return the dynamic data type
    */
   public static DynamicDataType parse(String nameOrTokenOrEnumId, boolean exceptionOnParseFail) {
      if (nameOrTokenOrEnumId == null) {
         return null;
      }

      final String clean = nameOrTokenOrEnumId.toLowerCase(Locale.ENGLISH)
                                              .trim();

      if (StringUtils.isBlank(clean)) {
         return null;
      }

      try {
         final int i = Integer.parseInt(clean);

         if (i > 100) {
            return getFromToken(i);
         } else {
            // enumId
            return DynamicDataType.values()[i];
         }
      } catch (final NumberFormatException e) {
         for (final DynamicDataType x: DynamicDataType.values()) {
            if (x.displayName.equalsIgnoreCase(clean) || x.name().toLowerCase().equals(clean)) {
               return x;
            }
         }
      }

      if (exceptionOnParseFail) {
         throw new InvalidParameterException("Could not determine DynamicSemanticDataType from " + nameOrTokenOrEnumId);
      } else {
         return UNKNOWN;
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data type concept.
    *
    * @return the data type concept
    */
   public UUID getDataTypeConcept() {
      /*
       * Implementation note - these used to be defined in the constructor, and stored in a local variable - but
       * that lead to a circular loop between the references of static elements in this class and DynamicSemantic,
       * specifically in the constructors - which would throw maven / surefire for a loop - resulting in a
       * class not found exception... which was a PITA to track down.  So, don't do that....
       */
      switch (this) {
      case BOOLEAN:
         return DynamicConstants.get().DYNAMIC_DT_BOOLEAN
                                      .getPrimordialUuid();

      case BYTEARRAY:
         return DynamicConstants.get().DYNAMIC_DT_BYTE_ARRAY
                                      .getPrimordialUuid();

      case DOUBLE:
         return DynamicConstants.get().DYNAMIC_DT_DOUBLE
                                      .getPrimordialUuid();

      case FLOAT:
         return DynamicConstants.get().DYNAMIC_DT_FLOAT
                                      .getPrimordialUuid();

      case INTEGER:
         return DynamicConstants.get().DYNAMIC_DT_INTEGER
                                      .getPrimordialUuid();

      case LONG:
         return DynamicConstants.get().DYNAMIC_DT_LONG
                                      .getPrimordialUuid();

      case NID:
         return DynamicConstants.get().DYNAMIC_DT_NID
                                      .getPrimordialUuid();

      case POLYMORPHIC:
         return DynamicConstants.get().DYNAMIC_DT_POLYMORPHIC
                                      .getPrimordialUuid();

      case STRING:
         return DynamicConstants.get().DYNAMIC_DT_STRING
                                      .getPrimordialUuid();

      case UNKNOWN:
         return DynamicConstants.get().UNKNOWN_CONCEPT;

      case UUID:
         return DynamicConstants.get().DYNAMIC_DT_UUID
                                      .getPrimordialUuid();

      case ARRAY:
         return DynamicConstants.get().DYNAMIC_DT_ARRAY
                                      .getPrimordialUuid();
      default:
         throw new RuntimeException("Implementation error");
      }
   }

   /**
    * Gets the display name.
    *
    * @return the display name
    */
   public String getDisplayName() {
      return this.displayName;
   }

   /**
    * Gets the dynamic member class.
    *
    * @return the dynamic member class
    */
   public Class<? extends DynamicData> getDynamicMemberClass() {
      return this.dataClass;
   }

   /**
    * Gets the from token.
    *
    * @param type the type
    * @return the from token
    * @throws UnsupportedOperationException the unsupported operation exception
    */
   public static DynamicDataType getFromToken(int type)
            throws UnsupportedOperationException {
      switch (type) {
      case 101:
         return NID;

      case 102:
         return STRING;

      case 103:
         return INTEGER;

      case 104:
         return BOOLEAN;

      case 105:
         return LONG;

      case 106:
         return BYTEARRAY;

      case 107:
         return FLOAT;

      case 108:
         return DOUBLE;

      case 109:
         return UUID;

      case 110:
         return POLYMORPHIC;

      case 111:
         return ARRAY;

      default:
         return UNKNOWN;
      }
   }

   /**
    * Gets the type token.
    *
    * @return the type token
    */
   public int getTypeToken() {
      return this.externalizedToken;
   }
   
   /**
    * A method to map SCT refset specification types into the corresponding brittle data types that column of data
    * should be stored as.  See code that uses this in Rf2DirectImport for more details on the usage.
    * @param sctidString
    * @return the corresponding BrittleDataType
    */
   public static DynamicDataType translateSCTIDMetadata(String sctidString) {
      long sctid = Long.parseLong(sctidString.trim());
      if (sctid == 900000000000461009l //Concept type component (foundation metadata concept) 78f69fb6-410c-3b5a-9120-53954592a80d
            || sctid == 900000000000460005l) {  //Component type (foundation metadata concept) a3d732bb-030d-3fba-b914-aeaaebc628c9
         return NID;
      }
      else if (sctid == 900000000000465000l   //String (SOLOR) a46aaf11-b37a-32d6-abdc-707f084ec8f5
            || sctid == 900000000000469006l  // Uniform resource locator (foundation metadata concept)
            || sctid == 762678002l  // OWL 2 language syntax (foundation metadata concept)
            || sctid == 707000009l  // SNOMED CT parsable string (foundation metadata concept)
            || sctid == 900000000000475002l) {  //Time (foundation metadata concept)  TODO should look and see what this actually is, if parseable, we could put in a long...
         return STRING;
      }
      else if (sctid == 900000000000476001l   //Integer (foundation metadata concept) 42d9f81e-27e9-3b73-9c19-9de4e2346b44
            || sctid == 900000000000477005l //Signed integer (SOLOR) 1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c
            || sctid == 900000000000478000l) { //Unsigned integer (foundation metadata concept) //TODO get UUID
         return INTEGER;
      }
      
      int typeConceptNid = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(sctid));

      //Component type (foundation metadata concept) a3d732bb-030d-3fba-b914-aeaaebc628c9
      if (isChildOf(typeConceptNid, Get.identifierService().getNidForUuids(java.util.UUID.fromString("a3d732bb-030d-3fba-b914-aeaaebc628c9")))) {
         return NID;
      }
      //String (SOLOR) a46aaf11-b37a-32d6-abdc-707f084ec8f5
      else if (isChildOf(typeConceptNid, Get.identifierService().getNidForUuids(java.util.UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5")))) {
         //This isn't as accurate as it probably should be, as other subtypes of string in snomed land are "time" and UUID".  If we ever encounter
         //those, we likely need to pick them out, and modify the loader to handle them more properly.
         return STRING;
      }
      
      throw new RuntimeException("Unmapped type " + sctidString + "!");
   }
   
   /**
    * Due to an oddity in how Keith set up the RF2 direct loader, the relationships that are loaded are useless in isaac at the time when we 
    * actually want to read them, because they haven't yet been processed into the taxonomy (even though they just as well could have been)
    * To avoid refactoring all of the code necessary to change the load order, just recursively dig through this refset to figure it out manually....
    * @param childConceptNid
    * @param parentConceptNid
    * @return
    */
   private static boolean isChildOf(int childConceptNid, int parentConceptNid)
   {
      StampCoordinate stamp = Get.defaultCoordinate().getStampCoordinate();
      
      ArrayList<SemanticChronology> chronologies = new ArrayList<>();
      
      Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(childConceptNid, 
            TermAux.RF2_STATED_RELATIONSHIP_ASSEMBLAGE.getNid()).forEach(chronology -> chronologies.add(chronology));
      
      ArrayList<Integer> destiniationNids = new ArrayList<>(chronologies.size());
      
      //check all of this level before recursing
      for (SemanticChronology sc : chronologies) {
         LatestVersion<Rf2Relationship> version = sc.getLatestVersion(stamp);
          if (version.isPresent()) {
             if (version.get().getDestinationNid() == parentConceptNid) {
                return true;
             }
             else {
                destiniationNids.add(version.get().getDestinationNid());
             }
          }
      }
      //recurse
      for (int nid : destiniationNids) {
         if (isChildOf(nid, parentConceptNid)) {
            return true;
         }
      }
      
      return false;
   }
}
