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
package gov.vha.isaac.ochre.observable.model.coordinate;

import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyType;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableLanguageCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableStampCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableTaxonomyCoordinate;
import gov.vha.isaac.ochre.observable.model.ObservableFields;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author kec
 */
public class ObservableTaxonomyCoordinateImpl implements ObservableTaxonomyCoordinate {
    
    ObjectProperty<TaxonomyType> taxonomyTypeProperty;
    ObjectProperty<ObservableStampCoordinate> stampCoordinateProperty;
    ObjectProperty<ObservableLanguageCoordinate> languageCoordinateProperty;
    
    TaxonomyCoordinate taxonomyCoordinate;

    public ObservableTaxonomyCoordinateImpl(TaxonomyCoordinate taxonomyCoordinate) {
        this.taxonomyCoordinate = taxonomyCoordinate;
    }

    @Override
    public ObjectProperty<TaxonomyType> taxonomyTypeProperty() {
        if (taxonomyTypeProperty == null) {
            taxonomyTypeProperty = new SimpleObjectProperty(this, 
                    ObservableFields.TAXONOMY_TYPE_FOR_TAXONOMY_COORDINATE.toExternalString(), 
                    getTaxonomyType());
        }
        return taxonomyTypeProperty;
    }

    @Override
    public ObjectProperty<ObservableStampCoordinate> stampCoordinateProperty() {
        if (stampCoordinateProperty == null) {
            stampCoordinateProperty = new SimpleObjectProperty(this, 
                    ObservableFields.STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(), 
                    getStampCoordinate());
        }
        return stampCoordinateProperty;
    }

    @Override
    public ObjectProperty<ObservableLanguageCoordinate> languageCoordinateProperty() {
        if (languageCoordinateProperty == null) {
            languageCoordinateProperty = new SimpleObjectProperty(this, 
                    ObservableFields.LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(), 
                    getLanguageCoordinate());
        }
        return languageCoordinateProperty;
    }

    @Override
    public TaxonomyType getTaxonomyType() {
        return taxonomyCoordinate.getTaxonomyType();
    }

    @Override
    public ObservableStampCoordinate getStampCoordinate() {
        return new ObservableStampCoordinateImpl(taxonomyCoordinate.getStampCoordinate());
    }

    @Override
    public ObservableLanguageCoordinate getLanguageCoordinate() {
        return new ObservableLanguageCoordinateImpl(taxonomyCoordinate.getLanguageCoordinate());
    }
}
