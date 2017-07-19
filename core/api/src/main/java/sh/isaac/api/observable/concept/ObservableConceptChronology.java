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



package sh.isaac.api.observable.concept;

//~--- JDK imports ------------------------------------------------------------


//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;

import javafx.collections.ObservableList;

import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableDescriptionVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableConceptChronology.
 *
 * @author kec
 */
public interface ObservableConceptChronology
        extends ObservableChronology<ObservableConceptVersion> {
   /**
    * Concept description list property.
    *
    * @return the list property
    */
   ListProperty<ObservableSememeChronology<ObservableDescriptionVersion>> conceptDescriptionListProperty();

   /**
    * Concept sequence property.
    *
    * @return the integer property
    */
   IntegerProperty conceptSequenceProperty();

   /**
    * A test for validating that a concept contains an active description. Used
    * to validate concept proxies or concept specs at runtime.
    * @param descriptionText text to match against.
    * @param stampCoordinate coordinate to determine if description is active.
    * @return true if any version of a description matches this text.
    */
   boolean containsActiveDescription(String descriptionText, StampCoordinate stampCoordinate);

   /**
    * A test for validating that a concept contains a description. Used
    * to validate concept proxies or concept specs at runtime.
    * @param descriptionText text to match against.
    * @return true if any version of a description matches this text.
    */
   boolean containsDescription(String descriptionText);

   /**
    * Create a mutable version the specified stampSequence. It is the responsibility of the caller to
    * add persist the chronicle when changes to the mutable version are complete .
    * @param stampSequence stampSequence that specifies the status, time, author, module, and path of this version.
    * @return the mutable version
    */
   ObservableConceptVersion createMutableVersion(int stampSequence);

   /**
    * Create a mutable version with Long.MAX_VALUE as the time, indicating
    * the version is uncommitted. It is the responsibility of the caller to
    * add the mutable version to the commit manager when changes are complete
    * prior to committing the component.
    * @param state state of the created mutable version
    * @param ec edit coordinate to provide the author, module, and path for the mutable version
    * @return the mutable version
    */
   ObservableConceptVersion createMutableVersion(State state, EditCoordinate ec);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept description list.
    *
    * @return the concept description list
    */
   ObservableList<ObservableSememeChronology<ObservableDescriptionVersion>> getConceptDescriptionList();

   /**
    * Gets the concept sequence.
    *
    * @return the sequence of this concept. A contiguously assigned identifier for
    * concepts >= 0;
    */
   int getConceptSequence();

   /**
    * Gets the fully specified description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified description
    */
   LatestVersion<ObservableDescriptionVersion> getFullySpecifiedDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate);

   /**
    * Gets the preferred description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampCoordinate the stamp coordinate
    * @return the preferred description
    */
   LatestVersion<ObservableDescriptionVersion> getPreferredDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate);
}
//~--- JDK imports ------------------------------------------------------------
