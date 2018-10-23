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



package sh.isaac.model.configuration;

import java.util.ArrayList;
import java.util.HashMap;

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.LanguageCoordinateService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.model.coordinate.LanguageCoordinateImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LanguageCoordinates.
 *
 * @author kec
 */
public class LanguageCoordinates {
   
   private static final Logger LOG = LogManager.getLogger();
   
   /**
    * Case significance to concept nid.
    *
    * @param initialCaseSignificant the initial case significant
    * @return the int
    */
   public static int caseSignificanceToConceptSequence(boolean initialCaseSignificant) {
      return TermAux.caseSignificanceToConceptNid(initialCaseSignificant);
   }

   /**
    * Concept id to case significance.
    *
    * @param id the id
    * @return true, if successful
    */
   public static boolean conceptIdToCaseSignificance(int id) {
      return TermAux.conceptIdToCaseSignificance(id);
   }

   /**
    * Concept nid to iso 639.
    *
    * @param nid the nid
    * @return the string
    */
   public static String conceptNidToIso639(int nid) {
      if (nid >= 0) {
         throw new IllegalStateException("Nids must be negative: " + nid);
      }

      if (TermAux.ENGLISH_LANGUAGE.getNid() == nid) {
         return "en";
      }

      if (TermAux.SPANISH_LANGUAGE.getNid() == nid) {
         return "es";
      }

      if (TermAux.FRENCH_LANGUAGE.getNid() == nid) {
         return "fr";
      }

      if (TermAux.DANISH_LANGUAGE.getNid() == nid) {
         return "da";
      }

      if (TermAux.POLISH_LANGUAGE.getNid() == nid) {
         return "pl";
      }

      if (TermAux.DUTCH_LANGUAGE.getNid() == nid) {
         return "nl";
      }

      if (TermAux.LITHUANIAN_LANGUAGE.getNid() == nid) {
         return "lt";
      }

      if (TermAux.CHINESE_LANGUAGE.getNid() == nid) {
         return "zh";
      }

      if (TermAux.JAPANESE_LANGUAGE.getNid() == nid) {
         return "ja";
      }

      if (TermAux.SWEDISH_LANGUAGE.getNid() == nid) {
         return "sv";
      }

      throw new UnsupportedOperationException("r Can't handle: " + nid);
   }

   /**
    * Iso 639 to concept nid.
    *
    * @param iso639text the iso 639 text
    * @return the int
    */
   public static int iso639toConceptNid(String iso639text) {
     //TODO we should really get rid of all of this hard-coded stuff and replace it with putting proper language codes 
     //directly into the metadata concept definitions, where they should be, so this can just be a query.... 
     //SeeAlso LanguageMap, for yet another implementation of all of this stuff...
      switch (iso639text.toLowerCase(Locale.ENGLISH)) {
      case "en":
         return Get.identifierService()
                   .getNidForUuids(TermAux.ENGLISH_LANGUAGE.getUuids());

      case "es":
         return Get.identifierService()
                   .getNidForUuids(TermAux.SPANISH_LANGUAGE.getUuids());

      case "fr":
         return Get.identifierService()
                   .getNidForUuids(TermAux.FRENCH_LANGUAGE.getUuids());

      case "da":
         return Get.identifierService()
                   .getNidForUuids(TermAux.DANISH_LANGUAGE.getUuids());

      case "pl":
         return Get.identifierService()
                   .getNidForUuids(TermAux.POLISH_LANGUAGE.getUuids());

      case "nl":
         return Get.identifierService()
                   .getNidForUuids(TermAux.DUTCH_LANGUAGE.getUuids());

      case "lt":
         return Get.identifierService()
                   .getNidForUuids(TermAux.LITHUANIAN_LANGUAGE.getUuids());

      case "zh":
         return Get.identifierService()
                   .getNidForUuids(TermAux.CHINESE_LANGUAGE.getUuids());

      case "ja":
         return Get.identifierService()
                   .getNidForUuids(TermAux.JAPANESE_LANGUAGE.getUuids());

      case "sv":
         return Get.identifierService()
                   .getNidForUuids(TermAux.SWEDISH_LANGUAGE.getUuids());

      default:
         throw new UnsupportedOperationException("s Can't handle: " + iso639text);
      }
   }

   /**
    * Iso 639 to assemblage nid.
    *
    * @param iso639text the iso 639 text
    * @return the int
    */
   public static int iso639toDescriptionAssemblageNid(String iso639text) {
      switch (iso639text.toLowerCase(Locale.ENGLISH)) {
      case "en":
         return Get.identifierService()
                   .getNidForUuids(TermAux.ENGLISH_LANGUAGE.getUuids());

      case "es":
         return Get.identifierService()
                   .getNidForUuids(TermAux.SPANISH_LANGUAGE.getUuids());

      case "fr":
         return Get.identifierService()
                   .getNidForUuids(TermAux.FRENCH_LANGUAGE.getUuids());

      case "da":
         return Get.identifierService()
                   .getNidForUuids(TermAux.DANISH_LANGUAGE.getUuids());

      case "pl":
         return Get.identifierService()
                   .getNidForUuids(TermAux.POLISH_LANGUAGE.getUuids());

      case "nl":
         return Get.identifierService()
                   .getNidForUuids(TermAux.DUTCH_LANGUAGE.getUuids());

      case "lt":
         return Get.identifierService()
                   .getNidForUuids(TermAux.LITHUANIAN_LANGUAGE.getUuids());

      case "zh":
         return Get.identifierService()
                   .getNidForUuids(TermAux.CHINESE_LANGUAGE.getUuids());

      case "ja":
         return Get.identifierService()
                   .getNidForUuids(TermAux.JAPANESE_LANGUAGE.getUuids());

      case "sv":
         return Get.identifierService()
                   .getNidForUuids(TermAux.SWEDISH_LANGUAGE.getUuids());

      default:
         throw new UnsupportedOperationException("Can't handle: " + iso639text);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the gb english language fully specified name coordinate.
    *
    * @return the gb english language fully specified name coordinate
    */
   public static LanguageCoordinate getGbEnglishLanguageFullySpecifiedNameCoordinate() {
      final int languageSequence = TermAux.ENGLISH_LANGUAGE.getNid();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.GB_DIALECT_ASSEMBLAGE.getNid(),
                                                                TermAux.US_DIALECT_ASSEMBLAGE.getNid() };
      final int[] descriptionTypePreferenceList = expandDescriptionTypePreferenceList(
              new int[] {TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()}, null);

      final int[] modulePreferenceList = new int[] { TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()};
      return new LanguageCoordinateImpl(TermAux.ENGLISH_LANGUAGE,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList, 
                                        modulePreferenceList);
   }

   /**
    * Gets the gb english language preferred term coordinate.
    *
    * @return the gb english language preferred term coordinate
    */
   public static LanguageCoordinate getGbEnglishLanguagePreferredTermCoordinate() {
      final int languageSequence = TermAux.ENGLISH_LANGUAGE.getNid();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.GB_DIALECT_ASSEMBLAGE.getNid(),
                                                                TermAux.US_DIALECT_ASSEMBLAGE.getNid() };
      final int[] descriptionTypePreferenceList = expandDescriptionTypePreferenceList(
              new int[] {TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(), TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()}, null);

      final int[] modulePreferenceList = new int[] { TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()};
      return new LanguageCoordinateImpl(TermAux.ENGLISH_LANGUAGE,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList, 
                                        modulePreferenceList);
   }

   /**
    * Gets the us english language fully specified name coordinate.
    *
    * @return the us english language fully specified name coordinate
    */
   public static LanguageCoordinate getUsEnglishLanguageFullySpecifiedNameCoordinate() {
      final int languageSequence = TermAux.ENGLISH_LANGUAGE.getNid();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.US_DIALECT_ASSEMBLAGE.getNid(),
                                                                TermAux.GB_DIALECT_ASSEMBLAGE.getNid() };
      final int[] descriptionTypePreferenceList = expandDescriptionTypePreferenceList(
            new int[] {TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()}, null);

      final int[] modulePreferenceList = new int[] { TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()};
      LanguageCoordinateImpl coordinate = new LanguageCoordinateImpl(TermAux.ENGLISH_LANGUAGE,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList, 
                                        modulePreferenceList);
      
      coordinate.setNextProrityLanguageCoordinate(getSpanishLanguageFullySpecifiedNameCoordinate());
      
      return coordinate;
   }

   /**
    * Gets the us english language preferred term coordinate.
    *
    * @return the us english language preferred term coordinate
    */
   public static LanguageCoordinate getUsEnglishLanguagePreferredTermCoordinate() {
      final int languageSequence = TermAux.ENGLISH_LANGUAGE.getNid();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.US_DIALECT_ASSEMBLAGE.getNid(),
                                                                TermAux.GB_DIALECT_ASSEMBLAGE.getNid() };
      final int[] descriptionTypePreferenceList = expandDescriptionTypePreferenceList(
              new int[] {TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(), TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()}, null);

      final int[] modulePreferenceList = new int[] { TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()};
      LanguageCoordinateImpl coordinate = new LanguageCoordinateImpl(TermAux.ENGLISH_LANGUAGE,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList, 
                                        modulePreferenceList);
      coordinate.setNextProrityLanguageCoordinate(getSpanishLanguagePreferredTermCoordinate());
      
      return coordinate;
   }
   public static LanguageCoordinate getSpanishLanguageFullySpecifiedNameCoordinate() {
      final int languageSequence = TermAux.SPANISH_LANGUAGE.getNid();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE.getNid() };
      final int[] descriptionTypePreferenceList = expandDescriptionTypePreferenceList(
              new int[] {TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()}, null);

      final int[] modulePreferenceList = new int[] { TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()};
      LanguageCoordinateImpl coordinate = new LanguageCoordinateImpl(TermAux.SPANISH_LANGUAGE,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList, 
                                        modulePreferenceList);
      coordinate.setNextProrityLanguageCoordinate(getFullyQualifiedCoordinate());
      
      return coordinate;
   }
   /**
    * Gets the us english language preferred term coordinate.
    *
    * @return the us english language preferred term coordinate
    */
   public static LanguageCoordinate getSpanishLanguagePreferredTermCoordinate() {
      final int languageSequence = TermAux.SPANISH_LANGUAGE.getNid();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE.getNid() };
      final int[] descriptionTypePreferenceList = expandDescriptionTypePreferenceList(
              new int[] {TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(), TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()}, null);

      final int[] modulePreferenceList = new int[] { TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()};
      LanguageCoordinateImpl coordinate = new LanguageCoordinateImpl(TermAux.SPANISH_LANGUAGE,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList, 
                                        modulePreferenceList);
      coordinate.setNextProrityLanguageCoordinate(getRegularNameCoordinate());
      
      return coordinate;
   }
   
   /**
    * A coordinate that completely ignores language - descriptions ranked by this coordinate will only be ranked by
    * description type and module preference.  This coordinate is primarily useful as a fallback coordinate for the final 
    * {@link LanguageCoordinate#getNextProrityLanguageCoordinate()} in a chain
    * 
    * See {@link LanguageCoordinateService#getSpecifiedDescription(StampCoordinate, java.util.List, LanguageCoordinate)}
    *
    * @return a coordinate that prefers regular names, of arbitrary language, but will return descriptions of any description
    * type
    */
   public static LanguageCoordinate getRegularNameCoordinate() {
      final int languageSequence = TermAux.LANGUAGE.getNid();
      final int[] descriptionTypePreferenceList = expandDescriptionTypePreferenceList(
              new int[] {TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(), 
                    TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(), 
                    TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()}, 
              null);

      final int[] modulePreferenceList = new int[] { TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()};
      LanguageCoordinateImpl coordinate = new LanguageCoordinateImpl(TermAux.LANGUAGE,
                                        new int[] {},
                                        descriptionTypePreferenceList, 
                                        modulePreferenceList);
      return coordinate;
   }
   
   /**
    * A coordinate that completely ignores language - descriptions ranked by this coordinate will only be ranked by
    * description type and module preference.  This coordinate is primarily useful as a fallback coordinate for the final 
    * {@link LanguageCoordinate#getNextProrityLanguageCoordinate()} in a chain
    * 
    * See {@link LanguageCoordinateService#getSpecifiedDescription(StampCoordinate, java.util.List, LanguageCoordinate)}
    *
    * @return a coordinate that prefers fully qualified names, of arbitrary language  but will return descriptions of any description
    * type
    */
   public static LanguageCoordinate getFullyQualifiedCoordinate() {
      final int languageSequence = TermAux.LANGUAGE.getNid();
      final int[] descriptionTypePreferenceList = expandDescriptionTypePreferenceList(
              new int[] {TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                    TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(), 
                    TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()}, 
              null);

      final int[] modulePreferenceList = new int[] { TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()};
      LanguageCoordinateImpl coordinate = new LanguageCoordinateImpl(TermAux.LANGUAGE,
                                        new int[] {},
                                        descriptionTypePreferenceList, 
                                        modulePreferenceList);
      return coordinate;
   }
   
   /**
    * Take in a list of the description type prefs, such as {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
    * and include any non-core description types that are linked to these core types, in the right order, so that the LanguageCoordinates can include the 
    * non-core description types in the appropriate places when looking for descriptions.
    * @param descriptionTypePreferenceList the starting list - should only consist of core description types - 
    * {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}, {@link TermAux#DEFINITION_DESCRIPTION_TYPE} 
    * @param stampCoordinate - optional - if not provided, uses {@link StampCoordinates#getDevelopmentLatestActiveOnly()}
    * @return the initial list, plus any equivalent non-core types in the appropriate order.  See {@link DynamicConstants#DYNAMIC_DESCRIPTION_CORE_TYPE}
    */
   public static int[] expandDescriptionTypePreferenceList(int[] descriptionTypePreferenceList, StampCoordinate stampCoordinate) {
      long time = System.currentTimeMillis();
      StampCoordinate stamp = stampCoordinate == null ? StampCoordinates.getDevelopmentLatestActiveOnly() : stampCoordinate;
      HashMap<Integer, NidSet> equivalentTypes = new HashMap<>();
      
      //Collect the mappings from core types -> non core types
      Get.assemblageService().getSemanticChronologyStream(DynamicConstants.get().DYNAMIC_DESCRIPTION_CORE_TYPE.getNid()).forEach(sc -> 
      {
         @SuppressWarnings("unchecked")
         DynamicVersion<? extends Version> dv = (DynamicVersion<? extends Version>)sc.getLatestVersion(stamp).get();
         int coreTypeNid = Get.identifierService().getNidForUuids(((DynamicUUID)dv.getData(0)).getDataUUID());
         NidSet mapped = equivalentTypes.get(coreTypeNid);
         if (mapped == null) {
            mapped = new NidSet();
            equivalentTypes.put(coreTypeNid, mapped);
         }
         mapped.add(sc.getReferencedComponentNid());
      });
      
      if (equivalentTypes.isEmpty()) {
         //this method is a noop
         LOG.debug("Expanded description types call is a noop in {}ms", System.currentTimeMillis() - time);
         return descriptionTypePreferenceList;
      }
      
      ArrayList<Integer> result = new ArrayList<>();
      for (int coreTypeNid : descriptionTypePreferenceList) {
         if (!result.contains(coreTypeNid)) {
            result.add(coreTypeNid);
         }
         NidSet nonCoreTypes = equivalentTypes.get(coreTypeNid);
         if (nonCoreTypes != null) {
            for (int type: nonCoreTypes.asArray()) {
               if (!result.contains(type)) {
                  result.add(type);
               }
            }
         }
      }
      int[] finalResult = new int[result.size()];
      int i = 0;
      for (int r : result) {
         finalResult[i++] = r;  
      }
      LOG.info("Expanded language type list from {} to {} in {}ms", descriptionTypePreferenceList, finalResult, System.currentTimeMillis() - time);
      return finalResult;
   }
}
