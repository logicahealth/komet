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
package gov.vha.isaac.ochre.api.observable.concept;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.observable.ObservableChronology;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableDescriptionSememe;
import java.util.Optional;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author kec
 * @param <V>
 */
public interface ObservableConceptChronology<V extends ObservableConceptVersion>
        extends ObservableChronology<V> {
    
        /**
     * 
     * @return the sequence of this concept. A contiguously assigned identifier for
     * concepts >= 0;
     */
    int getConceptSequence();
    
    IntegerProperty conceptSequenceProperty();
    
    /**
     * Create a mutable version with Long.MAX_VALUE as the time, indicating
     * the version is uncommitted. It is the responsibility of the caller to
     * add the mutable version to the commit manager when changes are complete
     * prior to committing the component. 
     * @param state state of the created mutable version
     * @param ec edit coordinate to provide the author, module, and path for the mutable version
     * @return the mutable version
     */
    V createMutableVersion(State state, EditCoordinate ec);
    
    /**
     * Create a mutable version the specified stampSequence. It is the responsibility of the caller to
     * add persist the chronicle when changes to the mutable version are complete . 
     * @param stampSequence stampSequence that specifies the status, time, author, module, and path of this version.
     * @return the mutable version
     */
     V createMutableVersion(int stampSequence);
    
    /**
     * A test for validating that a concept contains a description. Used
     * to validate concept proxies or concept specs at runtime.
     * @param descriptionText text to match against. 
     * @return true if any version of a description matches this text. 
     */
    boolean containsDescription(String descriptionText);

    /**
     * A test for validating that a concept contains an active description. Used
     * to validate concept proxies or concept specs at runtime.
     * @param descriptionText text to match against. 
     * @param stampCoordinate coordinate to determine if description is active. 
     * @return true if any version of a description matches this text. 
     */
    boolean containsActiveDescription(String descriptionText, StampCoordinate stampCoordinate);
        
    <T extends ObservableDescriptionSememe<T>> ObservableList<? extends ObservableSememeChronology<T>> getConceptDescriptionList();
    
    <T extends ObservableDescriptionSememe<T>> ListProperty<ObservableSememeChronology<T>>
        conceptDescriptionListProperty();
    
    Optional<LatestVersion<ObservableDescriptionSememe<?>>> 
        getFullySpecifiedDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate);
    
    <T extends ObservableDescriptionSememe<T>> Optional<LatestVersion<T>> 
        getPreferredDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate);
    
    
}
