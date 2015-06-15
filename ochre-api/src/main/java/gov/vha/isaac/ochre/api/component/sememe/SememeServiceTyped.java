/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.collections.NidSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import java.util.stream.Stream;

/**
 *
 * @author kec
 * @param <SV>
 */
public interface SememeServiceTyped<SV extends SememeVersion> {
    SememeChronology<SV> getSememe(int sememeSequence);
    
    Stream<SememeChronology<SV>> getSememesFromAssemblage(int assemblageSequence);
    SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageSequence);
    SememeSequenceSet getSememeSequencesFromAssemblageModifiedAfterPosition(int assemblageSequence, StampPosition position);
    SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet, int assemblageSequence, StampPosition position);
    
    Stream<SememeChronology<SV>> getSememesForComponent(int componentNid);
    SememeSequenceSet getSememeSequencesForComponent(int componentNid);
    
    Stream<SememeChronology<SV>> getSememesForComponentFromAssemblage(int componentNid, int assemblageSequence);
    SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageSequence);
    SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet, int assemblageSequence);
    
    void writeSememe(SememeChronology sememeChronicle);
    
    Stream<SememeChronology<SV>> getSememeStream();
    
    Stream<SememeChronology<SV>> getParallelSememeStream();
}
