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

import gov.vha.isaac.ochre.api.commit.IdentifiedStampedVersion;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import java.util.Optional;
import java.util.Set;

/**
 * An object that identifies a concept, and has a specific {@code StampCoordinate}
 * and {@code LanguageCoordinate} which determine which versions of which components
 * will be returned in response to method calls such as {@code getFullySpecifiedDescription()}.
 * @author kec
 */
public interface ConceptSnapshot extends IdentifiedStampedVersion, ConceptSpecification {
    
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
     * @return the {@code LanguageCoordinate} that defines the latest
     * version used by this snapshot. 
     */
    LanguageCoordinate getLanguageCoordinate();

    /**
     * A test for validating that a concept contains an active description. Used
     * to validate concept proxies or concept specs at runtime.
     * @param descriptionText text to match against. 
     * @return true if any active version of a description matches this text. 
     */
    boolean containsActiveDescription(String descriptionText);
    
// TODO put these methods in after removing OTF libraries. 
//    /**
//     * 
//     * @return The fully specified description for this concept. Optional in case
//     * there is not description that satisfies the {@code StampCoordinate} and the
//     * {@code LanguageCoordinate} of this snapshot.
//     */
//    Optional<LatestVersion<DescriptionSememe>> getFullySpecifiedDescription();
//    
//    /**
//     * 
//     * @return The preferred description for this concept. Optional in case
//     * there is not description that satisfies the {@code StampCoordinate} and the
//     * {@code LanguageCoordinate} of this snapshot.
//     */
//    Optional<LatestVersion<DescriptionSememe>> getPreferredDescription();
//    
//    /**
//     * This method will try first to return the fully specified description, 
//     * next the preferred description, finally any description if there is no 
//     * preferred or fully specified description that satisfies the {@code StampCoordinate} and the
//     * {@code LanguageCoordinate} of this snapshot. 
//     * @return a description for this concept. 
//     */
//    DescriptionSememe getDescription();
    
}
