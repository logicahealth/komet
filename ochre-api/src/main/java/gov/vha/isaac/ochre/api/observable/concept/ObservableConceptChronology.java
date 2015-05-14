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

import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.description.ConceptDescription;
import gov.vha.isaac.ochre.api.component.concept.description.ConceptDescriptionChronology;
import gov.vha.isaac.ochre.api.observable.ObservableChronology;
import gov.vha.isaac.ochre.api.observable.ObservableVersion;
import gov.vha.isaac.ochre.api.observable.description.ObservableDescriptionChronology;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author kec
 * @param <V>
 */
public interface ObservableConceptChronology<V extends ObservableVersion> 
        extends ConceptChronology<V>, 
                ObservableChronology<V> {
    
    IntegerProperty conceptSequenceProperty();
    
    ListProperty<ObservableDescriptionChronology> conceptDescriptionListProperty();

    @Override
    ObservableList<? extends ConceptDescriptionChronology<? extends ConceptDescription>> getConceptDescriptionList();
    
}
