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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;

import javafx.collections.ObservableList;

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableSememeVersion;

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
 * @param <V>
 */
public interface ObservableChronology<V extends ObservableVersion>
        extends ChronologyChangeListener {
   ObjectProperty<CommitStates> commitStateProperty();

   IntegerProperty nidProperty();

   ObjectProperty<UUID> primordialUuidProperty();

   ListProperty<? extends ObservableSememeChronology<? extends ObservableSememeVersion<?>>> sememeListProperty();

   ListProperty<UUID> uuidListProperty();

   ListProperty<? extends V> versionListProperty();

   //~--- get methods ---------------------------------------------------------

   Optional<LatestVersion<V>> getLatestVersion(Class<V> type, StampCoordinate coordinate);

   /**
    *
    * @return a list of sememes, where this object is the referenced component.
    */
   ObservableList<? extends ObservableSememeChronology<? extends ObservableSememeVersion<?>>> getSememeList();

   List<? extends ObservableSememeChronology<? extends SememeVersion<?>>> getSememeListFromAssemblage(
           int assemblageSequence);

   <SV extends ObservableSememeVersion> List<? extends ObservableSememeChronology<SV>> getSememeListFromAssemblageOfType(
           int assemblageSequence,
           Class<SV> type);

   /**
    *
    * @return a list of all versions of this object chronology.
    */
   List<? extends V> getVersionList();

   /**
    *
    * @return the version stamps for all the versions of this object chronology.
    */
   IntStream getVersionStampSequences();
}

