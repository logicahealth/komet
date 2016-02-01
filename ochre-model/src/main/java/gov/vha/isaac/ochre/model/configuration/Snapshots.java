/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.model.configuration;

import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.snapshot.Snapshot;

/**
 *
 * @author kec
 */
public class Snapshots {
    public static Snapshot getDefaultSolorInferredSnapshot() {
        LanguageCoordinate language = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
        StampCoordinate stampCoordinate = StampCoordinates.getDevelopmentLatest();
        return new Snapshot(language, 
                LogicCoordinates.getStandardElProfile(), 
                stampCoordinate, 
                TaxonomyCoordinates.getInferredTaxonomyCoordinate(stampCoordinate, language));
    }
    public static Snapshot getDefaultSolorStatedSnapshot() {
        LanguageCoordinate language = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
        StampCoordinate stampCoordinate = StampCoordinates.getDevelopmentLatest();
        return new Snapshot(language, 
                LogicCoordinates.getStandardElProfile(), 
                stampCoordinate, 
                TaxonomyCoordinates.getStatedTaxonomyCoordinate(stampCoordinate, language));
    }
}
