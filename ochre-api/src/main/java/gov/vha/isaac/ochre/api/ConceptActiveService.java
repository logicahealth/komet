/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface ConceptActiveService {
    
    boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate);
    
    /**
     * Update the service with the status values in this ConceptChronology. This method will be
     * called by the concept provider (based on a single service found using lookup) 
     * when concepts are written, so developers do not have to update the
     * ConceptActiveService themselves, unless developing an alternative 
     * implementation.
     * @param conceptChronology 
     */
    void updateStatus(ConceptChronology<?> conceptChronology);

}
