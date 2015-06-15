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

import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableStampPosition;
import gov.vha.isaac.ochre.observable.model.ObservableFields;
import java.time.Instant;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 *
 * @author kec
 */
public class ObservableStampPositionImpl implements ObservableStampPosition {
    
    LongProperty timeProperty;
    IntegerProperty stampPathSequenceProperty;

    StampPosition stampPosition;

    public ObservableStampPositionImpl(StampPosition stampPosition) {
        this.stampPosition = stampPosition;
    }

    @Override
    public LongProperty timeProperty() {
        if (timeProperty == null) {
            timeProperty = new SimpleLongProperty(this, 
                    ObservableFields.TIME_FOR_STAMP_POSITION.toExternalString(), 
                    getTime());
        }
        return timeProperty;
    }

    @Override
    public IntegerProperty stampPathSequenceProperty() {
        if (stampPathSequenceProperty == null) {
            stampPathSequenceProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.PATH_SEQUENCE_FOR_STAMP_POSITION.toExternalString(), 
                    getStampPathSequence());
        }
        return stampPathSequenceProperty;
    }

    @Override
    public long getTime() {
        if (timeProperty != null) {
            return timeProperty.get();
        }
        return stampPosition.getTime();
    }

    @Override
    public int getStampPathSequence() {
        if (stampPathSequenceProperty != null) {
            return stampPathSequenceProperty.get();
        }
        return stampPosition.getStampPathSequence();
    }

    @Override
    public Instant getTimeAsInstant() {
        if (timeProperty != null) {
            return Instant.ofEpochMilli(timeProperty.get());
        }
        return stampPosition.getTimeAsInstant();
    }

    @Override
    public StampPath getStampPath() {
        return stampPosition.getStampPath();
    }
    
}
