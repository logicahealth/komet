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
import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
@Contract
public interface LanguageCoordinateService {
   int caseSignificanceToConceptSequence(boolean initialCaseSignificant);

   boolean conceptIdToCaseSignificance(int id);

   /**
    *
    * @param id either a concept nid or concept sequence
    * @return ISO 639 language code
    */
   String conceptIdToIso639(int id);

   int iso639toConceptNid(String iso639text);

   int iso639toConceptSequence(String iso639text);

   //~--- get methods ---------------------------------------------------------

   int getAcceptableConceptSequence();

   int getFullySpecifiedConceptSequence();

   LanguageCoordinate getGbEnglishLanguagePreferredTermCoordinate();

   int getPreferredConceptSequence();

   /**
    *
    * @param stampCoordinate used to determine which versions of descriptions and dialect annotations are current.
    * @param descriptionList List of descriptions to consider.
    * @param languageCoordinate Used to determine ranking of candidate matches.
    * @return
    */
   Optional<LatestVersion<DescriptionSememe<?>>> getSpecifiedDescription(StampCoordinate stampCoordinate,
         List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList,
         LanguageCoordinate languageCoordinate);

   /**
    * @param stampCoordinate used to determine which versions of descriptions and dialect annotations are current.
    * @param descriptionList List of descriptions to consider.
    * @param typeSequence The specific type to match.
    * @param languageCoordinate Used to determine ranking of candidate matches.
    * @return
    */
   Optional<LatestVersion<DescriptionSememe<?>>> getSpecifiedDescription(StampCoordinate stampCoordinate,
         List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList,
         int typeSequence,
         LanguageCoordinate languageCoordinate);

   int getSynonymConceptSequence();

   LanguageCoordinate getUsEnglishLanguageFullySpecifiedNameCoordinate();

   LanguageCoordinate getUsEnglishLanguagePreferredTermCoordinate();
}

