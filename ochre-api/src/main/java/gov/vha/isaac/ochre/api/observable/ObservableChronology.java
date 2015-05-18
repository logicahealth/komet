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
package gov.vha.isaac.ochre.api.observable;

import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.commit.ChronologyChangeListener;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableSememeVersion;
import java.util.List;
import java.util.UUID;
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
    extends ObjectChronology<V>, ChronologyChangeListener {
        
    ListProperty<? extends V> versionListProperty();
    
    @Override
    ObservableList<? extends V> getVersionList();
    
    IntegerProperty nidProperty();
    
    ObjectProperty<UUID> primordialUuidProperty();
    
    ListProperty<UUID> uuidListProperty();
    
    ObjectProperty<CommitStates> commitStateProperty();

    ListProperty<? extends ObservableSememeChronology<? extends ObservableSememeVersion>> sememeListProperty();
    
    @Override
    ObservableList<? extends ObservableSememeChronology<? extends ObservableSememeVersion>> getSememeList();
    
    @Deprecated
    @Override
    default public ObservableList<? extends V> getVersions() {
        return getVersionList();
    }
    
}
