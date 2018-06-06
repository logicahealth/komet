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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api.chronicle;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Status;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePosition;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface Chronology.
 *
 * @author kec
 */
public interface Chronology
        extends IsaacExternalizable, CommittableComponent {
   /**
    * Create a mutable version the specified stampSequence. It is the responsibility of the caller to
    * add persist the chronicle when changes to the mutable version are complete .
    *
    * @param <V> the mutable version type
    * @param stampSequence stampSequence that specifies the status, time, author, module, and path of this version.
    * @return the mutable version
    */
   <V extends Version> V createMutableVersion(int stampSequence);
   
   /**
    * Create a mutable version with Long.MAX_VALUE as the time, indicating
    * the version is uncommitted. It is the responsibility of the caller to
    * add the mutable version to the commit manager when changes are complete
    * prior to committing the component.
    *
    * @param <V> the mutable version type
    * @param state state of the created mutable version
    * @param ec edit coordinate to provide the author, module, and path for the mutable version
    * @return the mutable version
    */
   <V extends Version> V createMutableVersion(Status state, EditCoordinate ec);

   /**
    * Gets the latest version.
    *
    * @param <V>
    * @param coordinate the coordinate
    * @return the latest version
    */
   <V extends Version> LatestVersion<V> getLatestVersion(StampCoordinate coordinate);
   
   /**
    * Exclude uncommitted versions from the latest version computation. 
    * @param <V>
    * @param coordinate
    * @return 
    */
   <V extends Version> LatestVersion<V> getLatestCommittedVersion(StampCoordinate coordinate);


   /**
    * Gets the categorized versions.
    *
    * @param <V>
    * @param coordinate the coordinate
    * @return the latest version
    */
   default <V extends Version> CategorizedVersions<V> getCategorizedVersions(StampCoordinate coordinate) {
      LatestVersion<V> latestVersion = getLatestCommittedVersion(coordinate);
      return new CategorizedVersions(latestVersion, this);
   }

   /**
    * Determine if the latest version is active, on a given stamp coordinate.  This method ignores the
 state attribute of the provided StampCoordinate - allowing all Status types -
 it returns true if the latest version is {@link Status#ACTIVE}
    *
    * @param coordinate the coordinate
    * @return true, if latest version active
    */
   boolean isLatestVersionActive(StampCoordinate coordinate);

   /**
    * Gets the semantic list.
    *
    * @param <V>
    * @return a list of SemanticChronology objects, where this object is the referenced component.
    */
   <V extends SemanticChronology> List<V> getSemanticChronologyList();

   /**
    * Gets the SemanticChronology list from assemblage.
    *
    * @param <V>
    * @param assemblageSequence the assemblage sequence
    * @return the SemanticChronology list from assemblage
    */
   <V extends SemanticChronology> List<V> getSemanticChronologyListFromAssemblage(int assemblageSequence);

   /**
    * Gets the unwritten version list.
    *
    * @param <V>
    * @return a list of all unwritten versions of this object chronology, with no order guarantee.
    */
   <V extends Version> List<V> getUnwrittenVersionList();

   /**
    * Gets the version graph list.
    *
    * @param <V>
    * @return Get a graph representation of the versions of this object chronology, where the root of the
    * graph is the original version of this component on a path, and the children are in sequential order, taking path
    * precedence into account. When a component version may have subsequent changes on more than one path,
    * which will result in more that one child node for that version.
    * If a chronology has disconnected versions on multiple paths, multiple graphs will be created and returned.
    * A version may be included in more than one graph if disconnected original versions are subsequently
    * merged onto commonly visible downstream paths.
    */
   default <V extends Version> List<Graph<V>> getVersionGraphList() {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the version list.
    *
    * @param <V>
    * @return a list of all versions of this object chronology, with no order guarantee. .
    */
   <V extends Version> List<V> getVersionList();

   /**
    * Gets the version stamp sequences.
    *
    * @return the version stamps for all the versions of this object chronology.
    */
   int[] getVersionStampSequences();

   /**
    * Gets the visible ordered version list.
    *
    * @param <V>
    * @param stampCoordinate used to determine visibility and order of versions
    * @return a list of all visible versions of this object chronology, sorted in
    * ascending order (oldest version first, newest version last).
    */
   default <V extends StampedVersion> List<V> getVisibleOrderedVersionList(StampCoordinate stampCoordinate) {
      final RelativePositionCalculator calc              = RelativePositionCalculator.getCalculator(stampCoordinate);
      final SortedSet<V>               sortedLogicGraphs = new TreeSet<>((StampedVersion graph1,
                                                                          StampedVersion graph2) -> {
               final RelativePosition relativePosition = calc.fastRelativePosition(graph1,
                                                                                   graph2,
                                                                                   stampCoordinate.getStampPrecedence());

               switch (relativePosition) {
               case BEFORE:
                  return -1;

               case EQUAL:
                  return 0;

               case AFTER:
                  return 1;

               case UNREACHABLE:
               case CONTRADICTION:
               default:
                  throw new UnsupportedOperationException("c Can't handle: " + relativePosition);
               }
            });

      sortedLogicGraphs.addAll(getVersionList());
      return sortedLogicGraphs.stream()
                              .collect(Collectors.toList());
   }
   
   VersionType getVersionType();
   
   NidSet getRecursiveSemanticNids();

}

