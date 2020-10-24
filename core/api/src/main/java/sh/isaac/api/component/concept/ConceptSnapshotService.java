/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.component.concept;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;

import java.util.NoSuchElementException;

/**
 * The Interface ConceptSnapshotService.
 *
 * @author kec
 */
public interface ConceptSnapshotService {

   /**
    * Simple method for getting text of the description of a concept.
    * This method will return a description type according to the constraints of
    * the {@code StampCoordinate} and the default {@code LanguageCoordinate}.
    * @param conceptNid of the concept to get the description for
    * @return a description for this concept. If no description can be found,
    * {@code "No desc for: " + UUID;} will be returned.
    */
   default String conceptDescriptionText(int conceptNid) {
      try {
         final LatestVersion<DescriptionVersion> descriptionOptional = getDescriptionOptional(conceptNid);

         if (descriptionOptional.isPresent()) {
             return descriptionOptional.get().getText();
         }

         return "No desc for: " + Get.identifierService().getUuidPrimordialStringForNid(conceptNid);
      } catch (NoSuchElementException e) {
        return "No desc for: " + e.getLocalizedMessage();
      }
   }

   /**
    * Gets the manifold coordinate.
    *
    * @return the {@code ManifoldCoordinate} associated with this snapshot.
    */
   ManifoldCoordinate getManifoldCoordinate();

   /**
    * Checks if concept active.
    *
    * @param conceptNid of the concept to determine if it is active
    * according to the {@code StampCoordinate} of this snapshot service
    * @return true, if concept active
    */
   boolean isConceptActive(int conceptNid);

   /**
    * Gets the concept snapshot.
    *
    * @param conceptNid of the concept to get the {@code ConceptSnapshot} for
    * @return a concept that internally uses the {@code StampCoordinate}
    * and {@code LanguageCoordinate} for
    */
   ConceptSnapshot getConceptSnapshot(int conceptNid);

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
    * regular or fully specified description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    * @param conceptNid of the concept to get the description for
    * @return a Optional description for this concept.
    */
   LatestVersion<DescriptionVersion> getDescriptionOptional(int conceptNid);

   /**
    * Gets the fully specified description.
    *
    * @param conceptNid of the concept to get the description for
    * @return The fully specified description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   default LatestVersion<DescriptionVersion> getFullyQualifiedDescription(int conceptNid) {
      return getManifoldCoordinate().getFullyQualifiedDescription(conceptNid);
   }

   /**
    * Gets the preferred description. The preferred description is a regular name that is
    * preferred within the rules of the language coordinate (dialects, etc).
    *
    * @param conceptNid of the concept to get the description for
    * @return The preferred description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   default LatestVersion<DescriptionVersion> getPreferredDescription(int conceptNid) {
      return getManifoldCoordinate().getPreferredDescription(conceptNid);
   }
}