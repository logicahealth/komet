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

import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableEditCoordinate;
import gov.vha.isaac.ochre.observable.model.ObservableFields;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author kec
 */
public class ObservableEditCoordinateImpl implements ObservableEditCoordinate {
    //TODO finish the property listeners for the other Observable coordinates. 

    private IntegerProperty authorSequenceProperty = null;
    private IntegerProperty moduleSequenceProperty = null;
    private IntegerProperty pathSequenceProperty = null;
    
    private final EditCoordinate editCoordinate;

    public ObservableEditCoordinateImpl(EditCoordinate editCoordinate) {
        this.editCoordinate = editCoordinate;
    }
    
    @Override
    public IntegerProperty authorSequenceProperty() {
        if (authorSequenceProperty == null) {
            authorSequenceProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.AUTHOR_SEQUENCE_FOR_EDIT_COORDINATE.toExternalString(), 
                    getAuthorSequence());
        }
        return authorSequenceProperty;
    }

    @Override
    public IntegerProperty moduleSequenceProperty() {
        if (moduleSequenceProperty == null) {
            moduleSequenceProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.MODULE_SEQUENCE_FOR_EDIT_COORDINATE.toExternalString(), 
                    getAuthorSequence());
        }
        return moduleSequenceProperty;
    }

    @Override
    public IntegerProperty pathSequenceProperty() {
        if (pathSequenceProperty == null) {
            pathSequenceProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.PATH_SEQUENCE_FOR_EDIT_CORDINATE.toExternalString(), 
                    getAuthorSequence());
        }
        return pathSequenceProperty;
    }
    
    @Override
    public int getAuthorSequence() {
        if (authorSequenceProperty != null) {
            return authorSequenceProperty.get();
        }
        return editCoordinate.getAuthorSequence();
    }

    @Override
    public int getModuleSequence() {
        if (moduleSequenceProperty != null) {
            return moduleSequenceProperty.get();
        }
        return editCoordinate.getModuleSequence();
    }

    @Override
    public int getPathSequence() {
        if (pathSequenceProperty != null) {
            return pathSequenceProperty.get();
        }
        return editCoordinate.getPathSequence();
    }
}
