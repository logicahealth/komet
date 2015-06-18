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
package gov.vha.isaac.ochre.api.component.concept;

import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import java.util.UUID;
import java.util.stream.Stream;

/**
 *
 * @author kec
 */
//Normally, this would be a contract... but we only want one in the system (and we have two, that we don't want running at the same time)
//So, force the users to get one via the ConceptServiceManagerI
//Alternatively, maybe we could do something with:  https://hk2.java.net/custom-resolver-example.html
public interface ConceptService {
    
    ConceptChronology<? extends ConceptVersion> getConcept(int conceptSequence);
    
    ConceptChronology<? extends ConceptVersion> getConcept(UUID... conceptUuids);
    
    void writeConcept(ConceptChronology<? extends ConceptVersion> concept);

    boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate);
    
    ConceptSnapshotService getSnapshot(StampCoordinate stampCoordinate);
    
    int getConceptCount();
    
    Stream<ConceptChronology<? extends ConceptVersion>> getConceptChronologyStream();
    Stream<ConceptChronology<? extends ConceptVersion>> getParallelConceptChronologyStream();

    Stream<ConceptChronology<? extends ConceptVersion>> getConceptChronologyStream(ConceptSequenceSet conceptSequences);
    Stream<ConceptChronology<? extends ConceptVersion>> getParallelConceptChronologyStream(ConceptSequenceSet conceptSequences);
}
