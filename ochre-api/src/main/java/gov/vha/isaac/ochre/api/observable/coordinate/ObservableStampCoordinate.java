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
package gov.vha.isaac.ochre.api.observable.coordinate;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.collections.ObservableIntegerArray;

/**
 *
 * @author kec
 */
public interface ObservableStampCoordinate extends StampCoordinate {
    
    SetProperty<State> allowedStatesProperty();
    
    ObjectProperty<StampPrecedence> stampPrecedenceProperty();
    
    ObjectProperty<ObservableStampPosition> stampPositionProperty();

    ObjectProperty<ObservableIntegerArray> moduleSequencesProperty();
    
}
