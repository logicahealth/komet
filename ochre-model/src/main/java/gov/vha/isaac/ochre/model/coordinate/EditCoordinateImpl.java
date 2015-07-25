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

import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;


/**
 *
 * @author kec
 */
public class EditCoordinateImpl implements EditCoordinate {
    int authorSequence;
    int moduleSequence;
    int pathSequence;

    public EditCoordinateImpl(int authorSequence, int moduleSequence, int pathSequence) {
        this.authorSequence = authorSequence;
        this.moduleSequence = moduleSequence;
        this.pathSequence = pathSequence;
    }


    public ChangeListener<Number> setAuthorSequenceProperty(IntegerProperty authorSequenceProperty) {
        ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                           Number oldValue,
                                           Number newValue) -> {
            authorSequence = newValue.intValue();
        };
        authorSequenceProperty.addListener(new WeakChangeListener<>(listener));
        return listener;
    }

    public ChangeListener<Number> setModuleSequenceProperty(IntegerProperty moduleSequenceProperty) {
        ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                           Number oldValue,
                                           Number newValue) -> {
            moduleSequence = newValue.intValue();
        };
        moduleSequenceProperty.addListener(new WeakChangeListener<>(listener));
        return listener;
    }

    public ChangeListener<Number> setPathSequenceProperty(IntegerProperty pathSequenceProperty) {
        ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                           Number oldValue,
                                           Number newValue) -> {
            pathSequence = newValue.intValue();
        };
        pathSequenceProperty.addListener(new WeakChangeListener<>(listener));
        return listener;
    }


    @Override
    public int getAuthorSequence() {
        return authorSequence;
    }

    @Override
    public int getModuleSequence() {
        return moduleSequence;
    }

    @Override
    public int getPathSequence() {
        return pathSequence;
    }

    public void setAuthorSequence(int authorSequence) {
        this.authorSequence = authorSequence;
    }

    public void setModuleSequence(int moduleSequence) {
        this.moduleSequence = moduleSequence;
    }

    public void setPathSequence(int pathSequence) {
        this.pathSequence = pathSequence;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.authorSequence;
        hash = 97 * hash + this.moduleSequence;
        hash = 97 * hash + this.pathSequence;
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
        final EditCoordinateImpl other = (EditCoordinateImpl) obj;
        if (this.authorSequence != other.authorSequence) {
            return false;
        }
        if (this.moduleSequence != other.moduleSequence) {
            return false;
        }
        if (this.pathSequence != other.pathSequence) {
            return false;
        }
        return true;
    }
    
}
