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

import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.collections.NidSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import java.util.Optional;
import java.util.stream.Stream;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface SememeService {
    <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType, 
            StampCoordinate<? extends StampCoordinate<?>> stampCoordinate);
    
    <V extends SememeVersion> SememeServiceTyped<V> ofType(Class<V> versionType);
    
    /**
     * 
     * @param sememeId sequence or nid for a sememe
     * @return the identified {@code SememeChronology}
     */
    SememeChronology<? extends SememeVersion<?>> getSememe(int sememeId);
    /**
     * 
     * @param sememeId sequence or nid for a sememe
     * @return the identified {@code SememeChronology}
     */
    Optional<? extends SememeChronology<? extends SememeVersion<?>>> getOptionalSememe(int sememeId);
    
    Stream<SememeChronology<? extends SememeVersion<?>>> getSememesFromAssemblage(int assemblageConceptSequence);
    SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageConceptSequence);
    SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet, int assemblageConceptSequence, StampPosition position);
    
    Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponent(int componentNid);
    SememeSequenceSet getSememeSequencesForComponent(int componentNid);
    
    Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence);
    SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence);
    SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet, int assemblageConceptSequence);
    
    void writeSememe(SememeChronology<?> sememeChronicle, SememeConstraints... constraints);
    
    Stream<SememeChronology<? extends SememeVersion<?>>> getSememeStream();
    
    Stream<SememeChronology<? extends SememeVersion<?>>> getParallelSememeStream();
    
    <T extends DescriptionSememe<T>> Stream<SememeChronology<T>> getDescriptionsForComponent(int componentNid);
    
    /**
     * @return the sequence identifiers of all assemblage concepts that are actually in use by a sememe
     */
    Stream<Integer> getAssemblageTypes();

}
