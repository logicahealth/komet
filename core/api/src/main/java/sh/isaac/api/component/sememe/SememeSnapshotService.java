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



package sh.isaac.api.component.sememe;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.ProgressTracker;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.sememe.version.SememeVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface SememeSnapshotService.
 *
 * @author kec
 * @param <V> type of SememeVersions provided by this snapshot service.
 */
public interface SememeSnapshotService<V extends SememeVersion> {
   /**
    * Gets the latest description versions for component.
    *
    * @param componentNid the component nid
    * @return the latest description versions for component
    */
   Stream<LatestVersion<V>> getLatestDescriptionVersionsForComponent(int componentNid);

   /**
    * Gets the latest sememe version.
    *
    * @param sememeSequenceOrNid the sememe sequence or nid
    * @return the latest sememe version
    */
   Optional<LatestVersion<V>> getLatestSememeVersion(int sememeSequenceOrNid);

   /**
    * Gets the latest sememe versions for component.
    *
    * @param componentNid the component nid
    * @return the latest sememe versions for component
    */
   Stream<LatestVersion<V>> getLatestSememeVersionsForComponent(int componentNid);

   /**
    * Gets the latest sememe versions for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the latest sememe versions for component from assemblage
    */
   Stream<LatestVersion<V>> getLatestSememeVersionsForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence);

   /**
    * Gets the latest sememe versions from assemblage.
    *
    * @param assemblageConceptSequence The sequence identifier of the assemblage to select
    * sememes from.
    * @param progressTrackers For each {@code progressTracker}, the addToTotalWork() will be
    * updated with the total number of sememes to be processed, and each time a sememe is
    * processed, {@code completedUnitOfWork()} will be called.
    * @return {@code Stream} of the {@code LatestVersion<V>} for each sememe according to the
    * criterion of this snapshot service.
    */
   Stream<LatestVersion<V>> getLatestSememeVersionsFromAssemblage(int assemblageConceptSequence,
         ProgressTracker... progressTrackers);
}

