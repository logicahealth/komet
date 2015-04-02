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
package gov.vha.isaac.ochre.api.sememe;

import gov.vha.isaac.ochre.api.sememe.version.SememeVersion;
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
    Optional<LatestVersion<V>> getLatestSememeVersionIfActive(int sememeSequence);
    
    Stream<LatestVersion<V>> getLatestSememeVersionsFromAssemblage(int assemblageSequence);
    Stream<LatestVersion<V>> getLatestActiveSememeVersionsFromAssemblage(int assemblageSequence);
    
    Stream<LatestVersion<V>> getLatestSememeVersionsForComponent(int componentNid);
    Stream<LatestVersion<V>> getLatestActiveSememeVersionsForComponent(int componentNid);
    
    Stream<LatestVersion<V>> getLatestSememeVersionsForComponentFromAssemblage(int componentNid, int assemblageSequence);
    Stream<LatestVersion<V>> getLatestActiveSememeVersionsForComponentFromAssemblage(int componentNid, int assemblageSequence);
    
}
