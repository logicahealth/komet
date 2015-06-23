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

import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.collections.NidSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import java.util.stream.Stream;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface SememeService {
    <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType, 
            StampCoordinate stampCoordinate);
    
    <V extends SememeVersion> SememeServiceTyped<V> ofType(Class<V> versionType);
    
    SememeChronology<? extends SememeVersion> getSememe(int sememeSequence);
    
    Stream<SememeChronology<? extends SememeVersion>> getSememesFromAssemblage(int assemblageSequence);
    SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageSequence);
    SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet, int assemblageSequence, StampPosition position);
    
    Stream<SememeChronology<? extends SememeVersion>> getSememesForComponent(int componentNid);
    SememeSequenceSet getSememeSequencesForComponent(int componentNid);
    
    Stream<SememeChronology<? extends SememeVersion>> getSememesForComponentFromAssemblage(int componentNid, int assemblageSequence);
    SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageSequence);
    SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet, int assemblageSequence);
    
    void writeSememe(SememeChronology sememeChronicle);
    
    Stream<SememeChronology<? extends SememeVersion>> getSememeStream();
    
    Stream<SememeChronology<? extends SememeVersion>> getParallelSememeStream();
    
    Stream<SememeChronology<DescriptionSememe>> getDescriptionsForComponent(int componentNid);
    
}
