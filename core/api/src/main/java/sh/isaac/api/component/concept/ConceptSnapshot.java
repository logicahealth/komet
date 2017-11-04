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

import java.util.Set;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.IdentifiedStampedVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * An object that identifies a concept, and has a specific {@code StampCoordinate}
 * and {@code LanguageCoordinate} which determine which versions of which components
 * will be returned in response to method calls such as {@code getFullySpecifiedDescription()}.
 * @author kec
 */
public interface ConceptSnapshot
        extends IdentifiedStampedVersion, ConceptSpecification, ManifoldCoordinate {
   /**
    * A test for validating that a concept contains an active description. Used
    * to validate concept proxies or concept specs at runtime.
    * @param descriptionText text to match against.
    * @return true if any active version of a description matches this text.
    */
   boolean containsActiveDescription(String descriptionText);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the chronology.
    *
    * @return the {@code ConceptChronology} that backs this snapshot.
    */
   ConceptChronology getChronology();

   /**
    * Gets the contradictions.
    *
    * @return any contradictions that may exist for the given {@code StampCoordinate}.
    */
   Set<? extends StampedVersion> getContradictions();

   /**
    * This method will try first to return the fully specified description,
    * next the preferred description, finally any description if there is no
    * preferred or fully specified description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    * @return a description for this concept.
    */
   DescriptionVersion getDescription();

   /**
    * Gets the fully specified description.
    *
    * @return The fully specified description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<DescriptionVersion> getFullySpecifiedDescription();

   /**
    * Gets the preferred description.
    *
    * @return The preferred description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<DescriptionVersion> getPreferredDescription();
}

