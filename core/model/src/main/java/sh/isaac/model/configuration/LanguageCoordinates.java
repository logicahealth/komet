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
import java.util.HashSet;

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.ConceptProxy;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.StampFilter;

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
         return TermAux.ENGLISH_LANGUAGE.getNid();

      case "es":
         return TermAux.SPANISH_LANGUAGE.getNid();

      case "fr":
         return TermAux.FRENCH_LANGUAGE.getNid();

      case "da":
         return TermAux.DANISH_LANGUAGE.getNid();

      case "pl":
         return TermAux.POLISH_LANGUAGE.getNid();

      case "nl":
         return TermAux.DUTCH_LANGUAGE.getNid();

      case "lt":
         return TermAux.LITHUANIAN_LANGUAGE.getNid();

      case "zh":
         return TermAux.CHINESE_LANGUAGE.getNid();

      case "ja":
         return TermAux.JAPANESE_LANGUAGE.getNid();

      case "sv":
         return TermAux.SWEDISH_LANGUAGE.getNid();

      default:
         throw new UnsupportedOperationException("Can't handle: " + iso639text);
      }
   }

   /**
    * Take in a list of the description type prefs, such as {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
    * and include any non-core description types that are linked to these core types, in the right order, so that the LanguageCoordinates can include the 
    * non-core description types in the appropriate places when looking for descriptions.
    * @param descriptionTypePreferenceList the starting list - should only consist of core description types -
    * {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}, {@link TermAux#DEFINITION_DESCRIPTION_TYPE}
    * @param stampFilter - optional - if not provided, uses {@link Coordinates.Filter.getDevelopmentLatestActiveOnly()}
    * @return the initial list, plus any equivalent non-core types in the appropriate order.  See {@link DynamicConstants#DYNAMIC_DESCRIPTION_CORE_TYPE}
    */
   public static ConceptSpecification[] expandDescriptionTypePreferenceList(ConceptSpecification[] descriptionTypePreferenceList, StampFilter stampFilter) {
      long time = System.currentTimeMillis();
      StampFilter filter = stampFilter == null ? Coordinates.Filter.DevelopmentLatestActiveOnly() : stampFilter;
      HashMap<ConceptSpecification, HashSet<ConceptSpecification>> equivalentTypes = new HashMap<>();
      
      //Collect the mappings from core types -> non core types
      Get.assemblageService().getSemanticChronologyStream(DynamicConstants.get().DYNAMIC_DESCRIPTION_CORE_TYPE.getNid()).forEach(sc -> 
      {
         DynamicVersion dv = (DynamicVersion)sc.getLatestVersion(filter).get();
         ConceptProxy coreType = new ConceptProxy(Get.identifierService().getNidForUuids(((DynamicUUID)dv.getData(0)).getDataUUID()));
         HashSet<ConceptSpecification> mapped = equivalentTypes.get(coreType);
         if (mapped == null) {
            mapped = new HashSet<>();
            equivalentTypes.put(coreType, mapped);
         }
         mapped.add(new ConceptProxy(sc.getReferencedComponentNid()));
      });
      
      if (equivalentTypes.isEmpty()) {
         //this method is a noop
         LOG.trace("Expanded description types call is a noop in {}ms", System.currentTimeMillis() - time);
         return descriptionTypePreferenceList;
      }
      
      ArrayList<ConceptSpecification> result = new ArrayList<>();
      ArrayList<Integer> startNids = new ArrayList<>();
      ArrayList<Integer> endNids = new ArrayList<>();
      for (ConceptSpecification coreType : descriptionTypePreferenceList) {
         startNids.add(coreType.getNid());
         if (!result.contains(coreType)) {
            result.add(coreType);
         }
         HashSet<ConceptSpecification> nonCoreTypes = equivalentTypes.get(coreType);
         if (nonCoreTypes != null) {
            for (ConceptSpecification type: nonCoreTypes) {
               if (!result.contains(type)) {
                  result.add(type);
                  endNids.add(type.getNid());
               }
            }
         }
      }
      LOG.info("Expanded language type list from {} to {} in {}ms", startNids, endNids, System.currentTimeMillis() - time);
      return result.toArray(new ConceptSpecification[result.size()]);
   }
}
