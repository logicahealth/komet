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



package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface LanguageCoordinateService.
 *
 * @author kec
 */
@Contract
public interface LanguageCoordinateService {
   /**
    * Case significance to concept nid.
    *
    * @param initialCaseSignificant the initial case significant
    * @return the int
    */
   int caseSignificanceToConceptNid(boolean initialCaseSignificant);

   /**
    * Concept id to case significance.
    *
    * @param id the id
    * @return true, if successful
    */
   boolean conceptIdToCaseSignificance(int id);

   /**
    * Concept conceptNid to iso 639.
    *
    * @param conceptNid concept nid 
    * @return ISO 639 language code
    */
   String conceptIdToIso639(int conceptNid);

   /**
    * Iso 639 to concept nid.
    *
    * @param iso639text the iso 639 text
    * @return the int
    */
   int iso639toConceptNid(String iso639text);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the acceptable concept nid.
    *
    * @return the acceptable concept nid
    */
   int getAcceptableConceptNid();

   /**
    * Gets the fully specified concept nid.
    *
    * @return the fully specified concept nid
    */
   int getFullySpecifiedConceptNid();

   /**
    * Gets the gb english language preferred term coordinate.
    *
    * @return the gb english language preferred term coordinate
    */
   LanguageCoordinate getGbEnglishLanguagePreferredTermCoordinate();

   /**
    * Gets the preferred concept nid.
    *
    * @return the preferred concept nid
    */
   int getPreferredConceptNid();

   /**
    * Gets the specified description(s).
    * 
    * Iterates over the list of supplied descriptions, finding the descriptions that match the highest ranked 
    * {@link LanguageCoordinate#getDescriptionTypePreferenceList()} (first item in the array) and the 
    * {@link LanguageCoordinate#getLanguageConceptNid()}.  If no descriptions match, the process is repeated 
    * with each subsequent item in {@link LanguageCoordinate#getDescriptionTypePreferenceList()}, walking 
    * through the array one by one.
    * 
    * For any given step, if multiple descriptions match the criteria, an ACTIVE description should have priority over 
    * an inactive one.
    * 
    * To be returned, a description MUST match one of the description types, and the specified language.
    * 
    * If the specified language {@link LanguageCoordinate#getLanguageConceptNid()} is {@link TermAux#LANGUAGE}, 
    * then language will be considered to always match, ignoring the actual value of the language in the description.
    * This allows this method to be used with a fallback behavior - where it will match a description of any language, 
    * but still rank by the requested type.   
    * 
    *  - If no descriptions match this criteria, then 
    *    - if a {@link LanguageCoordinate#getNextPriorityLanguageCoordinate()} is supplied, the method is re-evaluated with the next coordinate.
    *    - if there is no next priority coordinate, then an empty {@link LatestVersion} is returned.
    * 
    * For any descriptions that matched the criteria, they are then compared with the requested 
    * {@link LanguageCoordinate#getDialectAssemblagePreferenceList()}
    * The dialect preferences are evaluated in array order.  Each description that has a dialect annotation that matches
    * the dialect preference, with a type of {@link TermAux#PREFERRED}, it is advanced to the next ranking step (below)
    * 
    * If none of the descriptions has a dialect annotation of type {@link TermAux#PREFERRED} that matches a dialect 
    * in the {@link LanguageCoordinate#getDialectAssemblagePreferenceList()}, then all matching language / type matching
    * descriptions are advanced to the next ranking step (below).
    * 
    * The final ranking step, is to evaluate {@link LanguageCoordinate#getModulePreferenceListForLanguage()}
    * The module list is evaluated in order.  If a description matches the requested module, then it is placed
    * into the top position, so it is returned via {@link LatestVersion#get()}.  All other descriptions are still 
    * returned, but as part of the {@link LatestVersion#contradictions()}.
    * 
    * If none of the description match a specified module ranking, then the descriptions are returned in an arbitrary order, 
    * between {@link LatestVersion#get()} and {@link LatestVersion#contradictions()}.
    *
    * @param stampCoordinate used to determine which versions of descriptions and dialect annotations are current.
    * @param descriptionList List of descriptions to consider.
    * @param languageCoordinate Used to determine ranking of candidate matches.
    * @return the specified description
    */
   LatestVersion<DescriptionVersion> getSpecifiedDescription(StampCoordinate stampCoordinate,
         List<SemanticChronology> descriptionList,
         LanguageCoordinate languageCoordinate);

   /**
    * Gets the synonym concept nid.
    *
    * @return the synonym concept nid
    */
   int getSynonymConceptNid();

   /**
    * Gets the us english language fully specified name coordinate.
    *
    * @return the us english language fully specified name coordinate
    */
   LanguageCoordinate getUsEnglishLanguageFullySpecifiedNameCoordinate();

   /**
    * Gets the us english language preferred term coordinate.
    *
    * @return the us english language preferred term coordinate
    */
   LanguageCoordinate getUsEnglishLanguagePreferredTermCoordinate();
}

