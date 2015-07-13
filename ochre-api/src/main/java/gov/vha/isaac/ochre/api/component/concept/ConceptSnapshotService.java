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
package gov.vha.isaac.ochre.api.component.concept;

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import java.util.Optional;

/**
 *
 * @author kec
 */
public interface ConceptSnapshotService {
    
    /**
     * 
     * @param conceptId nid or sequence of the concept to determine if it is active
     * according to the {@code StampCoordinate} of this snapshot service
     * @return 
     */
    boolean isConceptActive(int conceptId);

    /**
     * 
     * @param conceptId nid or sequence of the concept to get the {@code ConceptSnapshot} for
     * @return a concept that internally uses the {@code StampCoordinate} 
     * and {@code LanguageCoordinate} for 
     */
    ConceptSnapshot getConceptSnapshot(int conceptId);

    /**
     * 
     * @return the {@code StampCoordinate} associated with this snapshot. 
     */
    StampCoordinate getStampCoordinate();
    
   /**
     * 
     * @return the {@code LanguageCoordinate} associated with this snapshot. 
     */
    LanguageCoordinate getLanguageCoordinate();

    /**
     * 
     * @param conceptId nid or sequence of the concept to get the description for
     * @return The fully specified description for this concept. Optional in case
     * there is not description that satisfies the {@code StampCoordinate} and the
     * {@code LanguageCoordinate} of this snapshot.
     */
    Optional<LatestVersion<DescriptionSememe>> getFullySpecifiedDescription(int conceptId);
    
    /**
     * 
     * @param conceptId nid or sequence of the concept to get the description for
     * @return The preferred description for this concept. Optional in case
     * there is not description that satisfies the {@code StampCoordinate} and the
     * {@code LanguageCoordinate} of this snapshot.
     */
    Optional<LatestVersion<DescriptionSememe>> getPreferredDescription(int conceptId);
    
    /**
     * This method will try to return description types according to the type preferences
     * of the language coordinate, finally any description if there is no 
     * preferred or fully specified description that satisfies the {@code StampCoordinate} and the
     * {@code LanguageCoordinate} of this snapshot. 
     * @param conceptId nid or sequence of the concept to get the description for
     * @return a Optional description for this concept. 
     */
    Optional<LatestVersion<DescriptionSememe>> getDescriptionOptional(int conceptId);
    /**
     * Simple method for getting text of the description of a concept. 
     * This method will return a description type according to the constraints of
     * the 
     * {@code StampCoordinate} and the default
     * {@code LanguageCoordinate}. 
     * @param conceptId nid or sequence of the concept to get the description for
     * @return a description for this concept. If no description can be found, 
     * {@code "No desc for: " + conceptId;} will be returned. 
     */
    String conceptDescriptionText(int conceptId);
    
}
