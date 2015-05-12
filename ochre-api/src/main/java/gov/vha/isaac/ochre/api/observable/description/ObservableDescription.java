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
package gov.vha.isaac.ochre.api.observable.description;

import gov.vha.isaac.ochre.api.component.concept.description.ConceptDescription;
import gov.vha.isaac.ochre.api.observable.ObservableVersion;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author kec
 */
public interface ObservableDescription extends ObservableVersion, ConceptDescription {
    
    IntegerProperty caseSignificanceConceptSequenceProperty();
    
    IntegerProperty languageConceptSequenceProperty();
    
    StringProperty textProperty();
    
    IntegerProperty descriptionTypeConceptSequenceProperty();
    
}
