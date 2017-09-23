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

//~--- JDK imports ------------------------------------------------------------


//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.sememe.version.DescriptionVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ConceptSnapshotService.
 *
 * @author kec
 */
public interface ConceptSnapshotService extends SharedConceptSnapshotService {

   //~--- get methods ---------------------------------------------------------

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
   LatestVersion<DescriptionVersion> getDescriptionOptional(int conceptId);

   /**
    * Gets the fully specified description.
    *
    * @param conceptId nid or sequence of the concept to get the description for
    * @return The fully specified description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<DescriptionVersion> getFullySpecifiedDescription(int conceptId);

   /**
    * Gets the preferred description.
    *
    * @param conceptId nid or sequence of the concept to get the description for
    * @return The preferred description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<DescriptionVersion> getPreferredDescription(int conceptId);
}
//~--- JDK imports ------------------------------------------------------------
