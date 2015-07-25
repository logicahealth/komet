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

import gov.vha.isaac.ochre.api.coordinate.*;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class TaxonomyCoordinateImpl implements TaxonomyCoordinate {

    PremiseType taxonomyType;
    StampCoordinate stampCoordinate;
    LanguageCoordinate languageCoordinate;
    LogicCoordinate logicCoordinate;
    UUID uuid;

    public TaxonomyCoordinateImpl(PremiseType taxonomyType, StampCoordinate stampCoordinate,
                                  LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        this.taxonomyType = taxonomyType;
        this.stampCoordinate = stampCoordinate;
        this.languageCoordinate = languageCoordinate;
        uuid = UuidT5Generator.get(UuidT5Generator.TAXONOMY_COORDINATE_NAMESPACE,
        this.taxonomyType + stampCoordinate.toString() + languageCoordinate.toString());
    }
    
    
    
    
    @Override
    public PremiseType getTaxonomyType() {
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

    @Override
    public LogicCoordinate getLogicCoordinate() {
        return logicCoordinate;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.taxonomyType);
        hash = 53 * hash + Objects.hashCode(this.stampCoordinate);
        hash = 53 * hash + Objects.hashCode(this.languageCoordinate);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaxonomyCoordinateImpl other = (TaxonomyCoordinateImpl) obj;
        if (this.taxonomyType != other.taxonomyType) {
            return false;
        }
        if (!Objects.equals(this.stampCoordinate, other.stampCoordinate)) {
            return false;
        }
        return Objects.equals(this.languageCoordinate, other.languageCoordinate);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }
    
}
