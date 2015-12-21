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
package gov.vha.isaac.ochre.model.observable.coordinate;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableLanguageCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableLogicCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableStampCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableTaxonomyCoordinate;
import gov.vha.isaac.ochre.model.coordinate.TaxonomyCoordinateImpl;
import gov.vha.isaac.ochre.model.observable.ObservableFields;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author kec
 */
public class ObservableTaxonomyCoordinateImpl extends ObservableCoordinateImpl implements ObservableTaxonomyCoordinate {
    TaxonomyCoordinateImpl taxonomyCoordinate;

   
    ObjectProperty<PremiseType> taxonomyTypeProperty;
    ObjectProperty<ObservableStampCoordinate> stampCoordinateProperty;
    ObjectProperty<ObservableLanguageCoordinate> languageCoordinateProperty;
    ObjectProperty<ObservableLogicCoordinate> logicCoordinateProperty;
    ObjectProperty<UUID> uuidProperty;
    
    public ObservableTaxonomyCoordinateImpl(TaxonomyCoordinate taxonomyCoordinate) {
        this.taxonomyCoordinate = (TaxonomyCoordinateImpl) taxonomyCoordinate;
    }

    @Override
    public ObjectProperty<PremiseType> premiseTypeProperty() {
        if (taxonomyTypeProperty == null) {
            taxonomyTypeProperty = new SimpleObjectProperty<>(this,
                    ObservableFields.PREMISE_TYPE_FOR_TAXONOMY_COORDINATE.toExternalString(),
                    taxonomyCoordinate.getTaxonomyType());
        }
        return taxonomyTypeProperty;
    }

    @Override
    public ObjectProperty<ObservableStampCoordinate> stampCoordinateProperty() {
        if (stampCoordinateProperty == null) {
            stampCoordinateProperty = new SimpleObjectProperty<>(this,
                    ObservableFields.STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(), 
                    new ObservableStampCoordinateImpl(taxonomyCoordinate.getStampCoordinate()));
        }
        return stampCoordinateProperty;
    }

    @Override
    public ObjectProperty<ObservableLanguageCoordinate> languageCoordinateProperty() {
        if (languageCoordinateProperty == null) {
            languageCoordinateProperty = new SimpleObjectProperty<>(this,
                    ObservableFields.LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(), 
                    new ObservableLanguageCoordinateImpl(taxonomyCoordinate.getLanguageCoordinate()));
        }
        return languageCoordinateProperty;
    }

    @Override
    public ObjectProperty<ObservableLogicCoordinate> logicCoordinateProperty() {
        if (logicCoordinateProperty == null) {
            logicCoordinateProperty = new SimpleObjectProperty<>(this,
                    ObservableFields.LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(),
                    new ObservableLogicCoordinateImpl(taxonomyCoordinate.getLogicCoordinate()));
        }
        return logicCoordinateProperty;
    }

    @Override
    public ObjectProperty<UUID> uuidProperty() {
        if (uuidProperty == null) {
            uuidProperty = new SimpleObjectProperty<>(this,
                    ObservableFields.UUID_FOR_TAXONOMY_COORDINATE.toExternalString(),
                    taxonomyCoordinate.getUuid());
        }
        return uuidProperty;
    }

    @Override
    public LogicCoordinate getLogicCoordinate() {
        return logicCoordinateProperty().get();
    }

    @Override
    public PremiseType getTaxonomyType() {
        return premiseTypeProperty().get();
    }

    @Override
    public ObservableStampCoordinate getStampCoordinate() {
        return stampCoordinateProperty().get();
    }

    @Override
    public ObservableLanguageCoordinate getLanguageCoordinate() {
        return languageCoordinateProperty().get();
    }
    @Override
    public UUID getUuid() {
        return uuidProperty().get();
    }

    @Override
    public ObservableTaxonomyCoordinate makeAnalog(long stampPositionTime) {
        return new ObservableTaxonomyCoordinateImpl(taxonomyCoordinate.makeAnalog(stampPositionTime));
    }

    @Override
    public ObservableTaxonomyCoordinate makeAnalog(State... state) {
        return new ObservableTaxonomyCoordinateImpl(taxonomyCoordinate.makeAnalog(state));
    }

    @Override
    public TaxonomyCoordinate makeAnalog(PremiseType taxonomyType) {
        return new ObservableTaxonomyCoordinateImpl(taxonomyCoordinate.makeAnalog(taxonomyType));
    }

    @Override
    public int getIsaConceptSequence() {
        return taxonomyCoordinate.getIsaConceptSequence();
    }
    
}
