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



package sh.isaac.api.component.semantic;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.ProgressTracker;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.stream.VersionStream;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.StampCoordinate;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface SemanticSnapshotService.
 *
 * @author kec
 * @param <V> type of SemanticVersions provided by this snapshot service.
 */
public interface SemanticSnapshotService<V extends SemanticVersion> {
   /**
    * Gets the latest description versions for component.
    *
    * @param componentNid the component nid
    * @return the latest description versions for component
    */
   List<LatestVersion<V>> getLatestDescriptionVersionsForComponent(int componentNid);

   /**
    * Gets the latest semantic version.
    *
    * @param semanticNid the semantic nid
    * @return the latest semantic version
    */
   LatestVersion<V> getLatestSemanticVersion(int semanticNid);

   /**
    * Gets the latest semantic versions for component.
    *
    * @param componentNid the component nid
    * @return the latest semantic versions for component
    */
   List<LatestVersion<V>> getLatestSemanticVersionsForComponent(int componentNid);

   /**
    * Gets the latest semantic versions for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptNid the assemblage concept nid
    * @return the latest semantic versions for component from assemblage
    */
   List<LatestVersion<V>> getLatestSemanticVersionsForComponentFromAssemblage(int componentNid,
         int assemblageConceptNid);

   /**
    * Gets the latest semantic versions from assemblage.
    *
    * @param assemblageConceptNid The nid of the assemblage to select
    * semantics from.
    * @param progressTrackers For each {@code progressTracker}, the addToTotalWork() will be
    * updated with the total number of semantics to be processed, and each time a semantic is
    * processed, {@code completedUnitOfWork()} will be called.
    * @return {@code Stream} of the {@code LatestVersion<V>} for each semantic according to the
    * criterion of this snapshot service.
    */
   VersionStream<V> getLatestSemanticVersionsFromAssemblage(int assemblageConceptNid,
         ProgressTracker... progressTrackers);
   
   /**
    * 
    * @param stampCoordinate 
    * @return a new SemanticSnapshotService based on the provided stamp coordinate;
    */
   SemanticSnapshotService<V> makeAnalog(StampCoordinate stampCoordinate);
}

