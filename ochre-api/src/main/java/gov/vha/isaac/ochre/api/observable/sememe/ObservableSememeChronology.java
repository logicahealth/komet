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
package gov.vha.isaac.ochre.api.observable.sememe;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.observable.ObservableChronology;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableSememeVersion;
import javafx.beans.property.IntegerProperty;

/**
 *
 * @author kec
 * @param <V>
 */
public interface ObservableSememeChronology<V extends ObservableSememeVersion> 
    extends ObservableChronology<V>, SememeChronology<V> {
    
    IntegerProperty sememeSequenceProperty();
    
    IntegerProperty assemblageSequenceProperty();
    
    IntegerProperty referencedComponentNidProperty();

    @Override
    <M extends V> M createMutableUncommittedVersion(Class<M> type, State status, EditCoordinate ec);
    
    @Override
    <M extends V> M createMutableStampedVersion(Class<M> type, int stampSequence);
    
    
}
