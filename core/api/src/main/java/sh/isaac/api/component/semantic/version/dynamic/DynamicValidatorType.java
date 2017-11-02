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

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.Interval;
import sh.isaac.api.util.NumericUtils;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicSequence;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;

//~--- enums ------------------------------------------------------------------

/**
 * {@link DynamicValidatorType}
 *
 * The acceptable validatorDefinitionData object type(s) for the following fields:
 * {@link DynamicValidatorType#LESS_THAN}
 * {@link DynamicValidatorType#GREATER_THAN}
 * {@link DynamicValidatorType#LESS_THAN_OR_EQUAL}
 * {@link DynamicValidatorType#GREATER_THAN_OR_EQUAL}
 *
 * are one of ( {@link DynamicSememeInteger}, {@link DynamicSememeLong}, {@link DynamicSememeFloat}, {@link DynamicSememeDouble})
 *
 * {@link DynamicValidatorType#INTERVAL} - Should be a {@link DynamicString} with valid interval notation - such as "[4,6)"
 *
 * {@link DynamicValidatorType#REGEXP} - Should be a {@link DynamicString} with valid regular expression, per
 * http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
 *
 * And for the following two:
 * {@link DynamicValidatorType#IS_CHILD_OF}
 * {@link DynamicValidatorType#IS_KIND_OF}
 * The validatorDefinitionData should be either an {@link DynamicNid} or {@link DynamicSequence} or a {@link DynamicUUID}.
 *
 * For {@link DynamicValidatorType#COMPONENT_TYPE} the validator definition data should be a {@link DynamicArray <DynamicSememeString>}
 * where position 0 is a string constant parseable by {@link ObjectChronologyType#parse(String)}.  Postion 1 is optional, and is only applicable when
 * position 0 is {@link ObjectChronologyType#SEMANTIC} - in which case - the value should be parsable by {@link VersionType#parse(String)}
 *
 * For {@link DynamicValidatorType#EXTERNAL} the validatorDefinitionData should be a {@link DynamicArray <DynamicSememeString>}
 * which contains (in the first position of the array) the name of an HK2 named service which implements {@link DynamicExternalValidator}
 * the name that you provide should be the value of the '@Name' annotation within the class which implements the ExternalValidatorBI class.
 * This code will request that implementation (by name) and pass the validation call to it.
 *
 * Optionally, the validatorDefinitionData more that one {@link DynamicString} in the array - only the first position of the array
 * will be considered as the '@Name' to be used for the HK2 lookup.  All following data is ignored, and may be used by the external validator
 * implementation to store other data.  For example, if the validatorDefinitionData {@link DynamicArray <DynamicSememeString>}
 * contains an array of strings such as new String[]{"mySuperRefexValidator", "somespecialmappingdata", "some other mapping data"}
 * then the following HK2 call will be made to locate the validator implementation (and validate):
 * <pre>
 *   ExternalValidatorBI validator = LookupService.get().getService(ExternalValidatorBI.class, "mySuperRefexValidator");
 *   return validator.validate(userData, validatorDefinitionData, viewCoordinate);
 * </pre>
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum DynamicValidatorType {
   /** The less than. */
   LESS_THAN("<"),

   /** The greater than. */
   GREATER_THAN(">"),

   /** The less than or equal. */
   LESS_THAN_OR_EQUAL("<="),

   /** The greater than or equal. */
   GREATER_THAN_OR_EQUAL(">="),

   /** The interval. */

   // Standard math stuff
   INTERVAL("Interval"),

   /** The regexp. */

   // math interval notation - such as [5,10)
   REGEXP("Regular Expression"),

   /** The external. */

   // http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
   EXTERNAL("External"),

   /** The is child of. */

   // see class docs above - implemented by an ExternalValidatorBI
   IS_CHILD_OF("Is Child Of"),

   /** The is kind of. */

   // OTF is child of - which only includes immediate (not recursive) children on the 'Is A' relationship.
   IS_KIND_OF("Is Kind Of"),

   /** The component type. */

   // OTF kind of - which is child of - but recursive, and self (heart disease is a kind-of heart disease);
   COMPONENT_TYPE("Component Type Restriction"),

   /** The unknown. */

   // specify which type of nid can be put into a UUID or nid column
   UNKNOWN(
      "Unknown");  // Not a real validator, only exists to allow GUI convenience, or potentially store other validator data that we don't support in OTF// Not a real validator, only exists to allow GUI convenience, or potentially store other validator data that we don't support in OTF

   // but we may need to store / retreive

   /** The logger. */
   private static final Logger logger = Logger.getLogger(DynamicValidatorType.class.getName());

   //~--- fields --------------------------------------------------------------

   /** The display name. */
   private final String displayName;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic validator type.
    *
    * @param displayName the display name
    */
   private DynamicValidatorType(String displayName) {
      this.displayName = displayName;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Parses the.
    *
    * @param nameOrEnumId the name or enum id
    * @param exceptionOnParseFail the exception on parse fail
    * @return the dynamic validator type
    */
   public static DynamicValidatorType parse(String nameOrEnumId, boolean exceptionOnParseFail) {
      if (nameOrEnumId == null) {
         return null;
      }

      final String clean = nameOrEnumId.toLowerCase(Locale.ENGLISH)
                                       .trim();

      if (StringUtils.isBlank(clean)) {
         return null;
      }

      try {
         final int i = Integer.parseInt(clean);

         // enumId
         return DynamicValidatorType.values()[i];
      } catch (final NumberFormatException e) {
         for (final DynamicValidatorType x: DynamicValidatorType.values()) {
            if (x.displayName.equalsIgnoreCase(clean) || x.name().toLowerCase().equals(clean)) {
               return x;
            }
         }
      }

      if (exceptionOnParseFail) {
         throw new InvalidParameterException("The value " + nameOrEnumId +
               " could not be parsed as a DynamicSememeValidatorType");
      } else {
         return UNKNOWN;
      }
   }

   /**
    * Parses the.
    *
    * @param nameOrEnumId the name or enum id
    * @param exceptionOnParseFail the exception on parse fail
    * @return the dynamic validator type[]
    */
   public static DynamicValidatorType[] parse(String[] nameOrEnumId, boolean exceptionOnParseFail) {
      if (nameOrEnumId == null) {
         return null;
      }

      final DynamicValidatorType[] temp = new DynamicValidatorType[nameOrEnumId.length];

      {
         for (int i = 0; i < nameOrEnumId.length; i++) {
            temp[i] = parse(nameOrEnumId[i], exceptionOnParseFail);
         }
      }
      return temp;
   }

   /**
    * These are all defined from the perspective of the userData - so for passesValidator to return true -
    * userData must be LESS_THAN validatorDefinitionData, for example.
    *
    * @param userData the user data
    * @param validatorDefinitionData the validator definition data
    * @param sc The Stamp Coordinate - not needed for some types of validations. Null allowed when unneeded (for math based tests, for example)
    *   {@link IllegalArgumentException} will be thrown if the coordinate was required for the validator (but it wasn't supplied)
    * @param tc The Taxonomy Coordinate - not needed for some types of validations. Null allowed when unneeded (for math based tests, for example)
    *    {@link IllegalArgumentException} will be thrown if the coordinate was required for the validator (but it wasn't supplied)
    * @return true, if successful
    * @throws IllegalArgumentException the illegal argument exception
    */
   @SuppressWarnings("unchecked")
   public boolean passesValidator(DynamicData userData,
                                  DynamicData validatorDefinitionData,
                                  StampCoordinate sc,
                                  ManifoldCoordinate tc)
            throws IllegalArgumentException {
      if (validatorDefinitionData == null) {
         throw new RuntimeException("The validator definition data is required");
      }

      if (userData instanceof DynamicArray) {
         // If the user data is an array, unwrap, and validate each.
         for (final DynamicData userDataItem: ((DynamicArray<?>) userData).getDataArray()) {
            if (!passesValidator(userDataItem, validatorDefinitionData, sc, tc)) {
               return false;
            }
         }

         return true;
      }

      if (this == DynamicValidatorType.EXTERNAL) {
         DynamicExternalValidator          validator              = null;
         DynamicString[]                   valNameInfo            = null;
         DynamicArray<DynamicString> stringValidatorDefData = null;
         String                                  valName                = null;

         if (validatorDefinitionData != null) {
            stringValidatorDefData = (DynamicArray<DynamicString>) validatorDefinitionData;
            valNameInfo            = stringValidatorDefData.getDataArray();
         }

         if ((valNameInfo != null) && (valNameInfo.length > 0)) {
            valName = valNameInfo[0].getDataString();
            logger.fine("Looking for an ExternalValidatorBI with the name of '" + valName + "'");
            validator = LookupService.get()
                                     .getService(DynamicExternalValidator.class, valName);
         } else {
            logger.severe(
                "An external validator type was specified, but no DynamicSememeExternalValidatorBI 'name' was provided.  API misuse!");
         }

         if (validator == null) {
            throw new RuntimeException(
                "Could not locate an implementation of DynamicSememeExternalValidatorBI with the requested name of '" +
                valName + "'");
         }

         return validator.validate(userData, stringValidatorDefData, sc, tc);
      } else if (this == DynamicValidatorType.REGEXP) {
         try {
            if (userData == null) {
               return false;
            }

            return Pattern.matches(((DynamicString) validatorDefinitionData).getDataString(),
                                   userData.getDataObject()
                                           .toString());
         } catch (final Exception e) {
            throw new RuntimeException("The specified validator data object was not a valid regular expression: " +
                                       e.getMessage());
         }
      } else if ((this == DynamicValidatorType.IS_CHILD_OF) || (this == DynamicValidatorType.IS_KIND_OF)) {
         try {
            int childId;
            int parentId;

            if (userData instanceof DynamicUUID) {
               childId = Get.identifierService()
                            .getNidForUuids(((DynamicUUID) userData).getDataUUID());
            } else if (userData instanceof DynamicNid) {
               childId = ((DynamicNid) userData).getDataNid();
            } else if (userData instanceof DynamicSequence) {
               childId = ((DynamicSequence) userData).getDataSequence();
            } else {
               throw new RuntimeException("Userdata is invalid for a IS_CHILD_OF or IS_KIND_OF comparison");
            }

            if (validatorDefinitionData instanceof DynamicUUID) {
               parentId = Get.identifierService()
                             .getNidForUuids(((DynamicUUID) validatorDefinitionData).getDataUUID());
            } else if (validatorDefinitionData instanceof DynamicNid) {
               parentId = ((DynamicNid) validatorDefinitionData).getDataNid();
            } else if (userData instanceof DynamicSequence) {
               parentId = ((DynamicSequence) validatorDefinitionData).getDataSequence();
            } else {
               throw new RuntimeException(
                   "Validator DefinitionData is invalid for a IS_CHILD_OF or IS_KIND_OF comparison");
            }

            if (this == DynamicValidatorType.IS_CHILD_OF) {
               if (tc == null) {
                  throw new IllegalArgumentException("A taxonomy coordinate must be provided to evaluate IS_CHILD_OF");
               }

               return Get.taxonomyService().getSnapshot(tc).get()
                         .isChildOf(childId, parentId);
            } else {
               if (tc == null) {
                  return Get.taxonomyService()
                            .wasEverKindOf(childId, parentId);
               } else {
                  return Get.taxonomyService().getSnapshot(tc).get()
                            .isKindOf(childId, parentId);
               }
            }
         } catch (final IllegalArgumentException e) {
            throw e;
         } catch (final Exception e) {
            logger.log(Level.WARNING, "Failure executing validator", e);
            throw new RuntimeException("Failure executing validator", e);
         }
      } else if (this == DynamicValidatorType.COMPONENT_TYPE) {
         try {
            int nid;

            if (userData instanceof DynamicUUID) {
               final DynamicUUID uuid = (DynamicUUID) userData;

               if (!Get.identifierService()
                       .hasUuid(uuid.getDataUUID())) {
                  throw new RuntimeException(
                      "The specified UUID can not be found in the database, so the validator cannot execute");
               } else {
                  nid = Get.identifierService()
                           .getNidForUuids(uuid.getDataUUID());
               }
            } else if (userData instanceof DynamicNid) {
               nid = ((DynamicNid) userData).getDataNid();
            } else {
               throw new RuntimeException("Userdata is invalid for a COMPONENT_TYPE comparison");
            }

            // Position 0 tells us the ObjectChronologyType.  When the type is Sememe, position 2 tells us the (optional) VersionType of the assemblage restriction
            final DynamicString[] valData =
               ((DynamicArray<DynamicString>) validatorDefinitionData).getDataArray();
            final ObjectChronologyType expectedCT = ObjectChronologyType.parse(valData[0].getDataString(), false);
            final ObjectChronologyType component  = Get.identifierService()
                                                       .getOldChronologyTypeForNid(nid);

            if (expectedCT == ObjectChronologyType.UNKNOWN_NID) {
               throw new RuntimeException("Couldn't determine validator type from validator data '" + valData + "'");
            }

            if (component != expectedCT) {
               throw new RuntimeException("The specified component must be of type " + expectedCT.toString() +
                                          ", not " + component);
            }

            if ((expectedCT == ObjectChronologyType.SEMANTIC) && (valData.length == 2)) {
               // they specified a specific type.  Verify.
               final VersionType st = VersionType.parse(valData[1].getDataString(), false);
               final SemanticChronology semanticChronology = Get.assemblageService()
                                                                              .getSemanticChronology(nid);

               if (semanticChronology.getVersionType() != st) {
                  throw new RuntimeException("The specified component must be of type " + st.toString() + ", not " +
                                             semanticChronology.getVersionType().toString());
               }
            }

            return true;
         } catch (final RuntimeException e) {
            throw e;
         } catch (final Exception e) {
            logger.log(Level.WARNING, "Failure executing validator", e);
            throw new RuntimeException("Failure executing validator", e);
         }
      } else {
         final Number userDataNumber = NumericUtils.readNumber(userData);
         Number       validatorDefinitionDataNumber;

         if (this == DynamicValidatorType.INTERVAL) {
            final String   s        = validatorDefinitionData.getDataObject()
                                                             .toString()
                                                             .trim();
            final Interval interval = new Interval(s);

            if (interval.getLeft() != null) {
               final int compareLeft = NumericUtils.compare(userDataNumber, interval.getLeft());

               if ((!interval.isLeftInclusive() && (compareLeft == 0)) || (compareLeft < 0)) {
                  return false;
               }
            }

            if (interval.getRight() != null) {
               final int compareRight = NumericUtils.compare(userDataNumber, interval.getRight());

               if ((!interval.isRightInclusive() && (compareRight == 0)) || (compareRight > 0)) {
                  return false;
               }
            }

            return true;
         } else {
            validatorDefinitionDataNumber = NumericUtils.readNumber(validatorDefinitionData);

            final int compareResult = NumericUtils.compare(userDataNumber, validatorDefinitionDataNumber);

            switch (this) {
            case LESS_THAN:
               return compareResult < 0;

            case GREATER_THAN:
               return compareResult > 0;

            case GREATER_THAN_OR_EQUAL:
               return compareResult >= 0;

            case LESS_THAN_OR_EQUAL:
               return compareResult <= 0;

            default:
               throw new RuntimeException("oops");
            }
         }
      }
   }

   /**
    * A convenience wrapper of {@link #passesValidator(DynamicSememeDataBI, DynamicSememeDataBI, ViewCoordinate)} that just returns a string - never
    * throws an error
    *
    * These are all defined from the perspective of the userData - so for passesValidator to return true -
    * userData must be LESS_THAN validatorDefinitionData, for example.
    *
    * @param userData the user data
    * @param validatorDefinitionData the validator definition data
    * @param sc - The Stamp Coordinate - not needed for some types of validations. Null allowed when unneeded (for math based tests, for example)
    * @param tc - The Taxonomy Coordinate - not needed for some types of validations. Null allowed when unneeded (for math based tests, for example)
    * @return - empty string if valid, an error message otherwise.
    */
   public String passesValidatorStringReturn(DynamicData userData,
         DynamicData validatorDefinitionData,
         StampCoordinate sc,
         ManifoldCoordinate tc) {
      try {
         if (passesValidator(userData, validatorDefinitionData, sc, tc)) {
            return "";
         } else {
            return "The value does not pass the validator";
         }
      } catch (final Exception e) {
         return e.getMessage();
      }
   }

   /**
    * Validator supports type.
    *
    * @param type the type
    * @return true, if successful
    */
   public boolean validatorSupportsType(DynamicDataType type) {
      // These are supported by all types - external specifies itself, what it supports, and we always include UNKNOWN.
      if ((this == UNKNOWN) || (this == EXTERNAL)) {
         return true;
      }

      switch (type) {
      case BOOLEAN:
      case POLYMORPHIC: {
         // technically, regexp would work here... but makes no sense.
         return false;
      }

      case DOUBLE:
      case FLOAT:
      case INTEGER:
      case LONG: {
         if ((this == GREATER_THAN) ||
               (this == GREATER_THAN_OR_EQUAL) ||
               (this == LESS_THAN) ||
               (this == LESS_THAN_OR_EQUAL) ||
               (this == INTERVAL) ||
               (this == REGEXP)) {
            return true;
         } else {
            return false;
         }
      }

      case NID:
      case UUID: {
         if ((this == IS_CHILD_OF) || (this == IS_KIND_OF) || (this == REGEXP) || (this == COMPONENT_TYPE)) {
            return true;
         } else {
            return false;
         }
      }

      case SEQUENCE:  // can't support component type with sequence, because we don't know how to look it up
      {
         if ((this == IS_CHILD_OF) || (this == IS_KIND_OF) || (this == REGEXP)) {
            return true;
         } else {
            return false;
         }
      }

      case STRING:
      case BYTEARRAY: {
         if (this == REGEXP) {
            return true;
         } else {
            return false;
         }
      }

      default: {
         logger.warning("Unexpected case!");
         return false;
      }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the display name.
    *
    * @return the display name
    */
   public String getDisplayName() {
      return this.displayName;
   }
}

