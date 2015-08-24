/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import java.util.List;
import java.util.Optional;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface LanguageCoordinateService {

    LanguageCoordinate getGbEnglishLanguagePreferredTermCoordinate();

    LanguageCoordinate getUsEnglishLanguageFullySpecifiedNameCoordinate();

    LanguageCoordinate getUsEnglishLanguagePreferredTermCoordinate();

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

    int getFullySpecifiedConceptSequence();
    
    int getSynonymConceptSequence();
    
    int getPreferredConceptSequence();
    
    int getAcceptableConceptSequence();
    
    /**
     * @param stampCoordinate used to determine which versions of descriptions and dialect annotations are current. 
     * @param descriptionList List of descriptions to consider. 
     * @param typeSequence The specific type to match. 
     * @param languageCoordinate Used to determine ranking of candidate matches. 
     * @return 
     */
    Optional<LatestVersion<DescriptionSememe<?>>> getSpecifiedDescription(StampCoordinate stampCoordinate, 
            List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList, 
            int typeSequence, LanguageCoordinate languageCoordinate);
    /**
     * 
     * @param stampCoordinate used to determine which versions of descriptions and dialect annotations are current. 
     * @param descriptionList List of descriptions to consider. 
     * @param languageCoordinate Used to determine ranking of candidate matches. 
     * @return 
     */
     Optional<LatestVersion<DescriptionSememe<?>>> getSpecifiedDescription(StampCoordinate stampCoordinate, 
            List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList, LanguageCoordinate languageCoordinate);
}
