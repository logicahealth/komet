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



package sh.isaac.api.component.sememe.version.dynamicSememe;

//~--- JDK imports ------------------------------------------------------------

import java.security.InvalidParameterException;

import java.util.Locale;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.*;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import sh.isaac.api.constants.DynamicSememeConstants;

//~--- enums ------------------------------------------------------------------

/**
 *
 * {@link DynamicSememeDataType}
 *
 * Most types are fairly straight forward.  NIDs, SEQUQENCES and INTEGERS are identical internally.
 * Polymorphic is used when the data type for a dynamic sememe isn't known at dynamic sememe creation time.  In this case, a user of the API
 * will have to examine type types of the actual {@link DynamicSememeData} objects returned, to look at the type.
 *
 * For all other types, the data type reported within the Refex Definition should exactly match the data type returned with
 * a {@link DynamicSememeData}.
 *
 * {@link DynamicSememeData} will never return a {@link POLYMORPHIC} type.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum DynamicSememeDataType {
   /** The nid. */
   NID(101, DynamicSememeNid.class, "Component Nid"),

   /** The string. */
   STRING(102, DynamicSememeString.class, "String"),

   /** The integer. */
   INTEGER(103, DynamicSememeInteger.class, "Integer"),

   /** The boolean. */
   BOOLEAN(104, DynamicSememeBoolean.class, "Boolean"),

   /** The long. */
   LONG(105, DynamicSememeLong.class, "Long"),

   /** The bytearray. */
   BYTEARRAY(106, DynamicSememeByteArray.class, "Arbitrary Data"),

   /** The float. */
   FLOAT(107, DynamicSememeFloat.class, "Float"),

   /** The double. */
   DOUBLE(108, DynamicSememeDouble.class, "Double"),

   /** The uuid. */
   UUID(109, DynamicSememeUUID.class, "UUID"),

   /** The polymorphic. */
   POLYMORPHIC(110, DynamicSememePolymorphic.class, "Unspecified"),

   /** The array. */
   ARRAY(111, DynamicSememeArray.class, "Array"),

   /** The sequence. */
   SEQUENCE(112, DynamicSememeSequence.class, "Component Sequence"),

   /** The unknown. */
   UNKNOWN(Byte.MAX_VALUE, null, "Unknown");

   /** The externalized token. */
   private int externalizedToken_;

   /** The data class. */
   private Class<? extends DynamicSememeData> dataClass_;

   /** The display name. */
   private String displayName_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic sememe data type.
    *
    * @param externalizedToken the externalized token
    * @param dataClass the data class
    * @param displayName the display name
    */
   private DynamicSememeDataType(int externalizedToken,
                                 Class<? extends DynamicSememeData> dataClass,
                                 String displayName) {
      this.externalizedToken_ = externalizedToken;
      this.dataClass_         = dataClass;
      this.displayName_       = displayName;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Class to type.
    *
    * @param c the c
    * @return the dynamic sememe data type
    */
   public static DynamicSememeDataType classToType(Class<?> c) {
      if (DynamicSememeNid.class.isAssignableFrom(c)) {
         return NID;
      }

      if (DynamicSememeString.class.isAssignableFrom(c)) {
         return STRING;
      }

      if (DynamicSememeInteger.class.isAssignableFrom(c)) {
         return INTEGER;
      }

      if (DynamicSememeBoolean.class.isAssignableFrom(c)) {
         return BOOLEAN;
      }

      if (DynamicSememeLong.class.isAssignableFrom(c)) {
         return LONG;
      }

      if (DynamicSememeByteArray.class.isAssignableFrom(c)) {
         return BYTEARRAY;
      }

      if (DynamicSememeFloat.class.isAssignableFrom(c)) {
         return FLOAT;
      }

      if (DynamicSememeDouble.class.isAssignableFrom(c)) {
         return DOUBLE;
      }

      if (DynamicSememeUUID.class.isAssignableFrom(c)) {
         return UUID;
      }

      if (DynamicSememePolymorphic.class.isAssignableFrom(c)) {
         return POLYMORPHIC;
      }

      if (DynamicSememeArray.class.isAssignableFrom(c)) {
         return ARRAY;
      }

      if (DynamicSememeSequence.class.isAssignableFrom(c)) {
         return SEQUENCE;
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
    * @return the dynamic sememe data type
    */
   public static DynamicSememeDataType parse(String nameOrTokenOrEnumId, boolean exceptionOnParseFail) {
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
            return DynamicSememeDataType.values()[i];
         }
      } catch (final NumberFormatException e) {
         for (final DynamicSememeDataType x: DynamicSememeDataType.values()) {
            if (x.displayName_.equalsIgnoreCase(clean) || x.name().toLowerCase().equals(clean)) {
               return x;
            }
         }
      }

      if (exceptionOnParseFail) {
         throw new InvalidParameterException("Could not determine DynamicSememeDataType from " + nameOrTokenOrEnumId);
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
       * that lead to a circular loop between the references of static elements in this class and DynamicSememe,
       * specifically in the constructors - which would throw maven / surefire for a loop - resulting in a
       * class not found exception... which was a PITA to track down.  So, don't do that....
       */
      switch (this) {
      case BOOLEAN:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_BOOLEAN
                                      .getUUID();

      case BYTEARRAY:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_BYTE_ARRAY
                                      .getUUID();

      case DOUBLE:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_DOUBLE
                                      .getUUID();

      case FLOAT:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_FLOAT
                                      .getUUID();

      case INTEGER:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_INTEGER
                                      .getUUID();

      case LONG:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_LONG
                                      .getUUID();

      case NID:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_NID
                                      .getUUID();

      case POLYMORPHIC:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_POLYMORPHIC
                                      .getUUID();

      case STRING:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_STRING
                                      .getUUID();

      case UNKNOWN:
         return DynamicSememeConstants.get().UNKNOWN_CONCEPT;

      case UUID:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_UUID
                                      .getUUID();

      case ARRAY:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_ARRAY
                                      .getUUID();

      case SEQUENCE:
         return DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_SEQUENCE
                                      .getUUID();

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
      return this.displayName_;
   }

   /**
    * Gets the dynamic sememe member class.
    *
    * @return the dynamic sememe member class
    */
   public Class<? extends DynamicSememeData> getDynamicSememeMemberClass() {
      return this.dataClass_;
   }

   /**
    * Gets the from token.
    *
    * @param type the type
    * @return the from token
    * @throws UnsupportedOperationException the unsupported operation exception
    */
   public static DynamicSememeDataType getFromToken(int type)
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

      case 112:
         return SEQUENCE;

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
      return this.externalizedToken_;
   }
}

