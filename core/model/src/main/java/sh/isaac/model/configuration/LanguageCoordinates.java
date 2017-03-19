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

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.model.coordinate.LanguageCoordinateImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LanguageCoordinates.
 *
 * @author kec
 */
public class LanguageCoordinates {
   
   /**
    * Case significance to concept sequence.
    *
    * @param initialCaseSignificant the initial case significant
    * @return the int
    */
   public static int caseSignificanceToConceptSequence(boolean initialCaseSignificant) {
      return TermAux.caseSignificanceToConceptSequence(initialCaseSignificant);
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
         nid = Get.identifierService()
                  .getConceptNid(nid);
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

      throw new UnsupportedOperationException("Can't handle: " + nid);
   }

   /**
    * Iso 639 to concept nid.
    *
    * @param iso639text the iso 639 text
    * @return the int
    */
   public static int iso639toConceptNid(String iso639text) {
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

   /**
    * Iso 639 to concept sequence.
    *
    * @param iso639text the iso 639 text
    * @return the int
    */
   public static int iso639toConceptSequence(String iso639text) {
      switch (iso639text.toLowerCase(Locale.ENGLISH)) {
      case "en":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.ENGLISH_LANGUAGE.getUuids());

      case "es":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.SPANISH_LANGUAGE.getUuids());

      case "fr":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.FRENCH_LANGUAGE.getUuids());

      case "da":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.DANISH_LANGUAGE.getUuids());

      case "pl":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.POLISH_LANGUAGE.getUuids());

      case "nl":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.DUTCH_LANGUAGE.getUuids());

      case "lt":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.LITHUANIAN_LANGUAGE.getUuids());

      case "zh":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.CHINESE_LANGUAGE.getUuids());

      case "ja":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.JAPANESE_LANGUAGE.getUuids());

      case "sv":
         return Get.identifierService()
                   .getConceptSequenceForUuids(TermAux.SWEDISH_LANGUAGE.getUuids());

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
      final int languageSequence = TermAux.ENGLISH_LANGUAGE.getConceptSequence();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence(),
                                                          TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence() };
      final int[] descriptionTypePreferenceList = new int[] { TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence(),
                                                        TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence() };

      return new LanguageCoordinateImpl(languageSequence,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList);
   }

   /**
    * Gets the gb english language preferred term coordinate.
    *
    * @return the gb english language preferred term coordinate
    */
   public static LanguageCoordinate getGbEnglishLanguagePreferredTermCoordinate() {
      final int languageSequence = TermAux.ENGLISH_LANGUAGE.getConceptSequence();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence(),
                                                          TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence() };
      final int[] descriptionTypePreferenceList = new int[] { TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence(),
                                                        TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence() };

      return new LanguageCoordinateImpl(languageSequence,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList);
   }

   /**
    * Gets the us english language fully specified name coordinate.
    *
    * @return the us english language fully specified name coordinate
    */
   public static LanguageCoordinate getUsEnglishLanguageFullySpecifiedNameCoordinate() {
      final int languageSequence = TermAux.ENGLISH_LANGUAGE.getConceptSequence();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence(),
                                                          TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence() };
      final int[] descriptionTypePreferenceList = new int[] { TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence(),
                                                        TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence() };

      return new LanguageCoordinateImpl(languageSequence,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList);
   }

   /**
    * Gets the us english language preferred term coordinate.
    *
    * @return the us english language preferred term coordinate
    */
   public static LanguageCoordinate getUsEnglishLanguagePreferredTermCoordinate() {
      final int languageSequence = TermAux.ENGLISH_LANGUAGE.getConceptSequence();
      final int[] dialectAssemblagePreferenceList = new int[] { TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence(),
                                                          TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence() };
      final int[] descriptionTypePreferenceList = new int[] { TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence(),
                                                        TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence() };

      return new LanguageCoordinateImpl(languageSequence,
                                        dialectAssemblagePreferenceList,
                                        descriptionTypePreferenceList);
   }
}

