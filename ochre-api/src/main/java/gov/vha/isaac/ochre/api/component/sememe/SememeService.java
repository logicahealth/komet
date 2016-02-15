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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.jvnet.hk2.annotations.Contract;
import gov.vha.isaac.ochre.api.collections.NidSet;
import gov.vha.isaac.ochre.api.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;

/**
 *
 * @author kec
 */
@Contract
public interface SememeService {
    <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType, 
            StampCoordinate stampCoordinate);
    
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
    
    /**
     * @param componentNid The component nid that the sememes must reference
     * @param allowedAssemblageSequences The (optional) set of assemblage types to limit the return to.  If empty or null, no assemblage filter is applied.
     */
    Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblages(int componentNid, Set<Integer> allowedAssemblageSequences);
    /**
     * @param componentNid The component nid that the sememes must reference
     * @param allowedAssemblageSequences The (optional) set of assemblage types to limit the return to.  If empty or null, no assemblage filter is applied.
     */
    SememeSequenceSet getSememeSequencesForComponentFromAssemblages(int componentNid, Set<Integer> allowedAssemblageSequences);
    
    Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence);
    SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence);
    SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet, int assemblageConceptSequence);

    /**
     * Write a sememe to the sememe service. Will not overwrite a sememe if one already exists, rather it will
     * merge the written sememe with the provided sememe.
     *
     *
     * The persistence of the concept is dependent on the persistence
     * of the underlying service.
     * @param sememe to be written.
     */
    void writeSememe(SememeChronology<?> sememeChronicle, SememeConstraints... constraints);
    
    Stream<SememeChronology<? extends SememeVersion<?>>> getSememeChronologyStream();
    
    Stream<SememeChronology<? extends SememeVersion<?>>> getParallelSememeStream();
    
    Stream<SememeChronology<? extends DescriptionSememe<?>>> getDescriptionsForComponent(int componentNid);
    
    /**
     * @return the sequence identifiers of all assemblage concepts that are actually in use by a sememe
     */
    Stream<Integer> getAssemblageTypes();

}
