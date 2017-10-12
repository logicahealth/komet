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



package sh.isaac.api.observable;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;


import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

//~--- interfaces -------------------------------------------------------------

/**
 * Note the property constructor should take a concept id, and use a
 * language coordinate, and a stamp coordinate to determine the name of
 * the property.
 *
 * Should the property name be the primordial UUID of of the concept identifying
 * the property?
 *
 * ObservableChronologies are singletons.
 *
 * @author kec
 */
public interface ObservableChronology
        extends ChronologyChangeListener, Chronology {
   /**
    * Commit state property.
    *
    * @return the object property
    */
   ObjectProperty<CommitStates> commitStateProperty();

   /**
    * Nid property.
    *
    * @return the integer property
    */
   IntegerProperty nidProperty();

   /**
    * Primordial uuid property.
    *
    * @return the object property
    */
   ObjectProperty<UUID> primordialUuidProperty();

   /**
    * Sememe list property.
    *
    * @return the list property<? extends observable sememe chronology<? extends observable sememe version<?>>>
    */
   ListProperty<? extends ObservableSemanticChronology> sememeListProperty();

   /**
    * Uuid list property.
    *
    * @return the list property
    */
   ListProperty<UUID> uuidListProperty();

   /**
    * Version list property.
    *
    * @return the list property<? extends v>
    */
   ListProperty<ObservableVersion> versionListProperty();

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sememe list.
    *
    * @return a list of SememeChronology objects, where this object is the referenced component.
    */
   ObservableList<ObservableSemanticChronology> getObservableSememeList();

   /**
    * Gets the sememe list from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememe list from assemblage
    */
   ObservableList<ObservableSemanticChronology> getObservableSememeListFromAssemblage(int assemblageSequence);

   /**
    * Gets the sememe list from assemblage of type.
    *
    * @param assemblageSequence the assemblage sequence
    * @param type the type
    * @return the sememe list from assemblage of type
    */
   ObservableList<ObservableSemanticChronology> getObservableSememeListFromAssemblageOfType(
           int assemblageSequence,
           VersionType type);


   /**
    * Gets the latest version.
    *
    * @param type the type
    * @param coordinate the coordinate
    * @return the latest version
    */
   LatestVersion<? extends ObservableVersion> getLatestVersion(Class<? extends StampedVersion> type,
                                                            StampCoordinate coordinate);

}

