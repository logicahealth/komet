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
package gov.vha.isaac.ochre.api.component.concept;

import gov.vha.isaac.ochre.api.chronicle.IdentifiedStampedVersion;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author kec
 */
public interface ConceptSnapshot extends IdentifiedStampedVersion {
    
    /**
     * 
     * @return the {@code ConceptChronology} that backs this snapshot.
     */
    ConceptChronology<? extends StampedVersion> getChronology();
    
    /**
     * 
     * @return any contradictions that may exist for the given {@code StampCoordinate}. 
     */
    
    Optional<? extends Set<? extends StampedVersion>> getContradictions();
    /**
     * 
     * @return the {@code StampCoordinate} that defines the latest
     * version used by this snapshot. 
     */
    StampCoordinate getStampCoordinate();
    
    /**
     * 
     * @return the sequence of this concept. A contiguously assigned identifier for
     * concepts >= 0;
     */
    int getConceptSequence();

    /**
     * A test for validating that a concept contains an active description. Used
     * to validate concept proxies or concept specs at runtime.
     * @param descriptionText text to match against. 
     * @return true if any active version of a description matches this text. 
     */
    boolean containsActiveDescription(String descriptionText);
    
}
