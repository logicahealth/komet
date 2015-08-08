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

import gov.vha.isaac.ochre.api.observable.concept.ObservableConceptChronology;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface ObservableChronologyService {
    /**
     * 
     * @param id either a nid or a concept sequence
     * @return the ObservableConceptChronology with the provided id
     */
    ObservableConceptChronology<?> getObservableConceptChronology(int id);
    /**
     * 
     * @param id either a nid or a sememe sequence
     * @return the ObservableSememeChronology with the provided id
     */
    ObservableSememeChronology<?> getObservableSememeChronology(int id);
    
}
