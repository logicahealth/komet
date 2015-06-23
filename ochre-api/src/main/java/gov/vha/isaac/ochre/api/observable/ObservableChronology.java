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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY_STATE_SET KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.observable;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.commit.ChronologyChangeListener;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableSememeVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

/**
 * Note the property constructor should take a concept id, and use a 
 * language coordinate, and a stamp coordinate to determine the name of 
 * the property. 
 * 
 * Should the property name be the primordial UUID of of the concept identifying
 * the property?
 * 
 * ObservableChronologies are singletons. 
 *
 * @author kec
 * @param <V>
 */
public interface ObservableChronology<V extends ObservableVersion> 

    extends ChronologyChangeListener {
        

    Optional<LatestVersion<V>> 
        getLatestVersion(Class<V> type, StampCoordinate coordinate);
        
    /**
     * 
     * @return a list of all versions of this object chronology. 
     */
    List<? extends V> getVersionList();
    
    ListProperty<? extends V> versionListProperty();
    
    /**
     * 
     * @return the version stamps for all the versions of this object chronology. 
     */
    IntStream getVersionStampSequences();
    
    /**
     * 
     * @return a list of sememes, where this object is the referenced component. 
     */
    ObservableList<? extends ObservableSememeChronology<? extends ObservableSememeVersion>> 
        getSememeList();
    
    List<? extends ObservableSememeChronology<? extends SememeVersion>> 
        getSememeListFromAssemblage(int assemblageSequence);

    <SV extends ObservableSememeVersion> List<? extends ObservableSememeChronology<SV>> 
        getSememeListFromAssemblageOfType(int assemblageSequence, Class<SV> type);
        
    
   IntegerProperty nidProperty();
    
    ObjectProperty<UUID> primordialUuidProperty();
    
    ListProperty<UUID> uuidListProperty();
    
    ObjectProperty<CommitStates> commitStateProperty();

    ListProperty<? extends ObservableSememeChronology<? extends ObservableSememeVersion>> sememeListProperty();
        
}
