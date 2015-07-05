/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.component.sememe;

import gov.vha.isaac.ochre.api.ProgressTracker;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * @author kec
 * @param <V> type of SememeVersions provided by this snapshot service. 
 */
public interface SememeSnapshotService<V extends SememeVersion> {
    
    Optional<LatestVersion<V>> getLatestSememeVersion(int sememeSequence);
    
    /**
     * 
     * @param assemblageSequence The sequence identifier of the assemblage to select 
     * sememes from. 
     * @param progressTrackers For each {@code progressTracker}, the addToTotalWork() will be
     * updated with the total number of sememes to be processed, and each time a sememe is
     * processed, {@code completedUnitOfWork()} will be called. 
     * @return {@code Stream} of the {@code LatestVersion<V>} for each sememe according to the 
     * criterion of this snapshot service. 
     */
    Stream<LatestVersion<V>> getLatestSememeVersionsFromAssemblage(int assemblageSequence, ProgressTracker... progressTrackers);
    
    Stream<LatestVersion<V>> getLatestSememeVersionsForComponent(int componentNid);
    
    Stream<LatestVersion<V>> getLatestSememeVersionsForComponentFromAssemblage(int componentNid, int assemblageSequence);

    Stream<LatestVersion<V>> getLatestDescriptionVersionsForComponent(int componentNid);
    
}
