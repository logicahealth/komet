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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.model.coordinate.TaxonomyCoordinateImpl;

/**
 *
 * @author kec
 */
public class TaxonomyCoordinates {
    /**
     * Uses the default logic coordinate. 
     * @param stampCoordinate
     * @param languageCoordinate
     * @return 
     */
    public static TaxonomyCoordinate getInferredTaxonomyCoordinate(
            StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
        return new TaxonomyCoordinateImpl(PremiseType.INFERRED, 
                stampCoordinate, languageCoordinate, Get.configurationService().getDefaultLogicCoordinate());
    }
    /**
     * Uses the default logic coordinate.
     * @param stampCoordinate
     * @param languageCoordinate
     * @return 
     */
    public static TaxonomyCoordinate getStatedTaxonomyCoordinate(
            StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
        return new TaxonomyCoordinateImpl(PremiseType.STATED, 
                stampCoordinate, languageCoordinate, Get.configurationService().getDefaultLogicCoordinate());
    }
    public static TaxonomyCoordinate getInferredTaxonomyCoordinate(
            StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate,
            LogicCoordinate logicCoordinate) {
        return new TaxonomyCoordinateImpl(PremiseType.INFERRED, 
                stampCoordinate, languageCoordinate, logicCoordinate);
    }
    public static TaxonomyCoordinate getStatedTaxonomyCoordinate(
            StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate,
            LogicCoordinate logicCoordinate) {
        return new TaxonomyCoordinateImpl(PremiseType.STATED, 
                stampCoordinate, languageCoordinate, logicCoordinate);
    }
}
