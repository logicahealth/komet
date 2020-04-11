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


import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampFilter;
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
        extends ChronologyChangeListener, Chronology, Comparable<ObservableChronology> {
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
    * Semantic list property.
    *
    * @return the list property<? extends observable semantic chronology<? extends observable semantic version<?>>>
    */
   ListProperty<? extends ObservableSemanticChronology> semanticListProperty();

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
    * Gets the semantic list.
    *
    * @return a list of SemanticChronology objects, where this object is the referenced component.
    */
   ObservableList<ObservableSemanticChronology> getObservableSemanticList();

   /**
    * Gets the semantic list from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the semantic list from assemblage
    */
   ObservableList<ObservableSemanticChronology> getObservableSemanticListFromAssemblage(int assemblageSequence);

   /**
    * Gets the latest version.
    *
    * @param type the type
    * @param stampFilter the stamp filter
    * @return the latest version
    */
   LatestVersion<? extends ObservableVersion> getLatestVersion(Class<? extends StampedVersion> type,
                                                            StampFilter stampFilter);

   <V extends ObservableVersion> LatestVersion<V> getLatestObservableVersion(StampFilter stampFilter);

   /**
    * This creates an observable version that is not added to the version list,
    * and can be edited without creating a STAMP coordinate. This allows the version
    * to be edited and committed independently of other content. 
     * @param <T>
    * @param ec
    * @return an editable observable version
    */
   <T extends ObservableVersion> T createAutonomousMutableVersion(EditCoordinate ec);

   /**
    * Implement a consistent ordering of chronologies for presentation purposes.
    * @param o
    * @return
    */
   @Override
   default int compareTo(ObservableChronology o) {
      if (this.getVersionType() != o.getVersionType()) {
         if (this.getVersionType() == VersionType.CONCEPT) {
            return -1;
         }
         if (o.getVersionType() == VersionType.CONCEPT) {
            return 1;
         }
         if (this.getVersionType() == VersionType.DESCRIPTION) {
            return -1;
         }
         if (o.getVersionType() == VersionType.DESCRIPTION) {
            return 1;
         }
         if (this.getVersionType() == VersionType.LOGIC_GRAPH) {
            return -1;
         }
         if (o.getVersionType() == VersionType.LOGIC_GRAPH) {
            return 1;
         }
         return this.getVersionType().compareTo(o.getVersionType());

      }
      if (this.getVersionType() == VersionType.LOGIC_GRAPH) {
         if (this.getAssemblageNid() == o.getAssemblageNid()) {
            return 0;
         }
         if (this.getAssemblageNid() == TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid()) {
            return -1;
         } else {
            return 1;
         }
      }
      return Get.conceptDescriptionText(this.getAssemblageNid()).compareTo(Get.conceptDescriptionText(o.getAssemblageNid()));
   }
}

