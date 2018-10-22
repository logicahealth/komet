/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api;

import java.util.List;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.stream.VersionStream;

/**
 *
 * @author kec
 * @param <V>
 */
public interface SingleAssemblageSnapshot<V extends SemanticVersion> {

    /**
     * 
     * @return the nid for the concept that identifies the assemblage this snapshot wraps.
     */
    int getAssemblageNid();
   /**
    * Gets the latest semantic version.
    *
    * @param semanticNid the semantic nid
    * @return the latest semantic version
    */
   LatestVersion<V> getLatestSemanticVersion(int semanticNid);

   /**
    * Gets the latest semantic versions for component from assemblage.
    *
    * @param componentNid the component nid
    * @return the latest semantic versions for component from assemblage
    */
   List<LatestVersion<V>> getLatestSemanticVersionsForComponentFromAssemblage(int componentNid);

   default List<LatestVersion<V>> getLatestSemanticVersionsForComponentFromAssemblage(ConceptSpecification concept) {
       return getLatestSemanticVersionsForComponentFromAssemblage(concept.getNid());
   }


   /**
    * Gets the latest semantic versions from assemblage.
    *
    * @param progressTrackers For each {@code progressTracker}, the addToTotalWork() will be
    * updated with the total number of semantics to be processed, and each time a semantic is
    * processed, {@code completedUnitOfWork()} will be called.
    * @return {@code Stream} of the {@code LatestVersion<V>} for each semantic according to the
    * criterion of this snapshot service.
    */
   VersionStream<V> getLatestSemanticVersionsFromAssemblage(ProgressTracker... progressTrackers);
   
   SingleAssemblageSnapshot<V> makeAnalog(StampCoordinate stampCoordinate);
}

