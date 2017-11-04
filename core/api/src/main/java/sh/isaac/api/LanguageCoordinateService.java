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
    * Case significance to concept sequence.
    *
    * @param initialCaseSignificant the initial case significant
    * @return the int
    */
   int caseSignificanceToConceptSequence(boolean initialCaseSignificant);

   /**
    * Concept id to case significance.
    *
    * @param id the id
    * @return true, if successful
    */
   boolean conceptIdToCaseSignificance(int id);

   /**
    * Concept id to iso 639.
    *
    * @param id either a concept nid or concept sequence
    * @return ISO 639 language code
    */
   String conceptIdToIso639(int id);

   /**
    * Iso 639 to concept nid.
    *
    * @param iso639text the iso 639 text
    * @return the int
    */
   int iso639toConceptNid(String iso639text);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the acceptable concept sequence.
    *
    * @return the acceptable concept sequence
    */
   int getAcceptableConceptNid();

   /**
    * Gets the fully specified concept sequence.
    *
    * @return the fully specified concept sequence
    */
   int getFullySpecifiedConceptNid();

   /**
    * Gets the gb english language preferred term coordinate.
    *
    * @return the gb english language preferred term coordinate
    */
   LanguageCoordinate getGbEnglishLanguagePreferredTermCoordinate();

   /**
    * Gets the preferred concept sequence.
    *
    * @return the preferred concept sequence
    */
   int getPreferredConceptNid();

   /**
    * Gets the specified description.
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
    * Gets the specified description.
    *
    * @param stampCoordinate used to determine which versions of descriptions and dialect annotations are current.
    * @param descriptionList List of descriptions to consider.
    * @param typeSequence The specific type to match.
    * @param languageCoordinate Used to determine ranking of candidate matches.
    * @return the specified description
    */
   LatestVersion<DescriptionVersion> getSpecifiedDescription(StampCoordinate stampCoordinate,
         List<SemanticChronology> descriptionList,
         int typeSequence,
         LanguageCoordinate languageCoordinate);

   /**
    * Gets the synonym concept sequence.
    *
    * @return the synonym concept sequence
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

