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

import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableStampPath;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableStampPosition;
import gov.vha.isaac.ochre.model.coordinate.StampPathImpl;
import gov.vha.isaac.ochre.model.observable.ObservableFields;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;

/**
 *
 * @author kec
 */
public class ObservableStampPathImpl extends ObservableCoordinateImpl implements ObservableStampPath {
    StampPathImpl stampPath;

   
    ReadOnlyIntegerProperty pathConceptSequenceProperty;
    ReadOnlyListWrapper<ObservableStampPosition> pathOriginsProperty;
    
    public ObservableStampPathImpl(StampPath stampPath) {
        this.stampPath = (StampPathImpl) stampPath;
    }

    @Override
    public ReadOnlyIntegerProperty pathConceptSequenceProperty() {
        if (pathConceptSequenceProperty == null) {
            pathConceptSequenceProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.PATH_SEQUENCE_FOR_STAMP_PATH.toExternalString(), 
                    getPathConceptSequence());
        }
        return pathConceptSequenceProperty;
    }

    @Override
    public ReadOnlyListProperty<ObservableStampPosition> pathOriginsProperty() {
        if (pathOriginsProperty == null) {
            pathOriginsProperty = new ReadOnlyListWrapper<>(this,
                    ObservableFields.PATH_ORIGIN_LIST_FOR_STAMP_PATH.toExternalString(), 
                    FXCollections.<ObservableStampPosition>observableList(getPathOrigins()));
        }
        return pathOriginsProperty;
    }

    @Override
    public int getPathConceptSequence() {
        if (pathConceptSequenceProperty != null) {
            return pathConceptSequenceProperty.get();
        }
        return stampPath.getPathConceptSequence();
    }

    @Override
    public List<ObservableStampPosition> getPathOrigins() {
        if (pathOriginsProperty != null) {
            return pathOriginsProperty.get();
        }
        return stampPath.getPathOrigins().stream()
                .map((origin) -> new ObservableStampPositionImpl(origin))
                .collect(Collectors.toList());
    }

    @Override
    public int compareTo(StampPath o) {
        return stampPath.compareTo(o);
    }

    @Override
    public String toString() {
        return "ObservableStampPathImpl{" +
                    stampPath +
                '}';
    }
}
