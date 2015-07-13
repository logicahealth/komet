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

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableStampCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableStampPosition;
import gov.vha.isaac.ochre.observable.model.ObservableFields;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.ObservableSet;

/**
 *
 * @author kec
 */
public class ObservableStampCoordinateImpl implements ObservableStampCoordinate {
    //TODO finish the property listeners for the other Observable coordinates. 
    
    ObjectProperty<StampPrecedence> stampPrecedenceProperty;
    ObjectProperty<ObservableStampPosition> stampPositionProperty;
    ObjectProperty<ObservableIntegerArray> moduleSequencesProperty;
    SetProperty<State> allowedStates;
    StampCoordinate stampCoordinate;

    public ObservableStampCoordinateImpl(StampCoordinate stampCoordinate) {
        this.stampCoordinate = stampCoordinate;
    }

    @Override
    public ObservableStampCoordinateImpl makeAnalog(long stampPositionTime) {
        StampCoordinate analog = stampCoordinate.makeAnalog(stampPositionTime);
        return new ObservableStampCoordinateImpl(analog);
    }

    @Override
    public SetProperty<State> allowedStatesProperty() {
        if (allowedStates == null) {
            allowedStates = new SimpleSetProperty(this, 
                    ObservableFields.ALLOWED_STATES_FOR_STAMP_COORDINATE.toExternalString(), 
                    FXCollections.observableSet(stampCoordinate.getAllowedStates()));
        }
        return allowedStates;
    }

    @Override
    public ObservableSet<State> getAllowedStates() {
        return allowedStatesProperty().getValue();
    }
    
    

    @Override
    public ObjectProperty<StampPrecedence> stampPrecedenceProperty() {
        if (stampPrecedenceProperty == null) {
            stampPrecedenceProperty = new SimpleObjectProperty(this, 
                    ObservableFields.STAMP_PRECEDENCE_FOR_STAMP_COORDINATE.toExternalString(), 
                    getStampPrecedence());
        }
        return stampPrecedenceProperty;
    }

    @Override
    public ObjectProperty<ObservableStampPosition> stampPositionProperty() {
        if (stampPositionProperty == null) {
            stampPositionProperty = new SimpleObjectProperty(this, 
                    ObservableFields.STAMP_POSITION_FOR_STAMP_COORDINATE.toExternalString(), 
                    getStampPosition());
        }
        return stampPositionProperty;
    }

    @Override
    public ObjectProperty<ObservableIntegerArray> moduleSequencesProperty() {
        if (moduleSequencesProperty == null) {
            moduleSequencesProperty = new SimpleObjectProperty(this, 
                    ObservableFields.MODULE_SEQUENCE_ARRAY_FOR_STAMP_COORDINATE.toExternalString(), 
                    getModuleSequences());
        }
        return moduleSequencesProperty;
    }

    @Override
    public StampPrecedence getStampPrecedence() {
        if (stampPrecedenceProperty != null) {
            return stampPrecedenceProperty.get();
        }
        return stampCoordinate.getStampPrecedence();
    }

    @Override
    public StampPosition getStampPosition() {
        if (stampPositionProperty != null) {
            return stampPositionProperty.get();
        }
        return stampCoordinate.getStampPosition();
    }

    @Override
    public int[] getModuleSequences() {
        if (moduleSequencesProperty != null) {
            return moduleSequencesProperty.get().toArray(new int[0]);
        }
        return stampCoordinate.getModuleSequences();
    }
    
}
