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
package gov.vha.isaac.ochre.model.coordinate;

import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyType;

/**
 *
 * @author kec
 */
public class TaxonomyCoordinateImpl implements TaxonomyCoordinate {

    TaxonomyType taxonomyType;
    StampCoordinate stampCoordinate;
    LanguageCoordinate languageCoordinate;

    public TaxonomyCoordinateImpl(TaxonomyType taxonomyType, StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
        this.taxonomyType = taxonomyType;
        this.stampCoordinate = stampCoordinate;
        this.languageCoordinate = languageCoordinate;
    }
    
    @Override
    public TaxonomyType getTaxonomyType() {
        return taxonomyType;
    }

    @Override
    public StampCoordinate getStampCoordinate() {
       return stampCoordinate;
    }

    @Override
    public LanguageCoordinate getLanguageCoordinate() {
        return languageCoordinate;
    }
    
}
