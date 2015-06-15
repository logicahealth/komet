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

    String conceptNidToIso639(int nid);

    int iso639toConceptNid(String iso639text);

    int iso639toConceptSequence(String iso639text);

    int getFullySpecifiedConceptSequence();
    
    int getSynonymConceptSequence();
    
    int getPreferredConceptSequence();
    
    int getAcceptableConceptSequence();
    
    <V extends DescriptionSememe, C extends SememeChronology<V>> Optional<LatestVersion<V>> getSpecifiedDescription(StampCoordinate stampCoordinate, 
            List<C> descriptionList, 
            int typeSequence, LanguageCoordinate languageCoordinate);
}
