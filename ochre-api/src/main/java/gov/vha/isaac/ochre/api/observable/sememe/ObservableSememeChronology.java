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
import gov.vha.isaac.ochre.api.component.sememe.SememeObject;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
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
    extends ObservableChronology<V>, SememeObject {
    
    IntegerProperty sememeSequenceProperty();
    
    IntegerProperty assemblageSequenceProperty();
    
    IntegerProperty referencedComponentNidProperty();


    /**
     * Create a mutable version with Long.MAX_VALUE as the time, indicating
     * the version is uncommitted. It is the responsibility of the caller to
     * add the mutable version to the commit manager when changes are complete
     * prior to committing the component. 
     * @param <M>
     * @param type SememeVersion type
     * @param state state of the created mutable version 
     * @param ec edit coordinate to provide the author, module, and path for the mutable version
     * @return the mutable version
     */
    <M extends V> M createMutableVersion(Class<M> type, State state, EditCoordinate ec);
    
    /**
     * Create a mutable version the specified stampSequence. It is the responsibility of the caller to
     * add persist the chronicle when changes to the mutable version are complete . 
     * @param <M>
     * @param type SememeVersion type
     * @param stampSequence stampSequence that specifies the status, time, author, module, and path of this version. 
     * @return the mutable version
     */
    <M extends V> M createMutableVersion(Class<M> type, int stampSequence);
    
    SememeType getSememeType();
    
}
