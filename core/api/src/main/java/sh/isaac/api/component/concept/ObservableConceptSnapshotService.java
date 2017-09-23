/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.component.concept;

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.observable.sememe.version.ObservableDescriptionVersion;

/**
 *
 * @author kec
 */
public interface ObservableConceptSnapshotService extends SharedConceptSnapshotService {

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
   //~--- get methods ---------------------------------------------------------
   /**
    * Checks if concept active.
    *
    * @param conceptId nid or sequence of the concept to determine if it is active
    * according to the {@code StampCoordinate} of this snapshot service
    * @return true, if concept active
    */
      /**
    * Gets the concept snapshot.
    *
    * @param conceptId nid or sequence of the concept to get the {@code ConceptSnapshot} for
    * @return a concept that internally uses the {@code StampCoordinate}
    * and {@code LanguageCoordinate} for
    */
   ConceptSnapshot getConceptSnapshot(int conceptId);

   /**
    * Gets the concept snapshot.
    *
    * @param conceptSpecification specification of the concept to get the {@code ConceptSnapshot} for
    * @return a concept that internally uses the {@code StampCoordinate}
    * and {@code LanguageCoordinate} for
    */
   ConceptSnapshot getConceptSnapshot(ConceptSpecification conceptSpecification);

   /**
    * This method will try to return description types according to the type preferences
    * of the language coordinate, finally any description if there is no
    * preferred or fully specified description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    * @param conceptId nid or sequence of the concept to get the description for
    * @return a Optional description for this concept.
    */
   LatestVersion<ObservableDescriptionVersion> getDescriptionOptional(int conceptId);

   /**
    * Gets the fully specified description.
    *
    * @param conceptId nid or sequence of the concept to get the description for
    * @return The fully specified description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<ObservableDescriptionVersion> getFullySpecifiedDescription(int conceptId);

   /**
    * Gets the preferred description.
    *
    * @param conceptId nid or sequence of the concept to get the description for
    * @return The preferred description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<ObservableDescriptionVersion> getPreferredDescription(int conceptId);
}
