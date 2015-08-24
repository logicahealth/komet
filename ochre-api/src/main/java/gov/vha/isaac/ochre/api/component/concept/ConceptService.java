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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface ConceptService {
    
    /**
     * 
     * @param conceptId either a concept sequence or a concept nid. 
     * @return the concept chronology associated with the identifier. 
     */
    ConceptChronology<? extends ConceptVersion<?>> getConcept(int conceptId);
    
    /**
     * 
     * @param conceptUuids a UUID that identifies a concept.
     * @return the concept chronology associated with the identifier. 
     */
    ConceptChronology<? extends ConceptVersion<?>> getConcept(UUID... conceptUuids);
    
    /**
     * Use in circumstances when not all concepts may have been loaded. 
     * @param conceptId Either a nid or concept sequence
     * @return an Optional ConceptChronology.
     */
    Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(int conceptId);
    /**
     * Use in circumstances when not all concepts may have been loaded. 
     * @param conceptUuids uuids that identify the concept
     * 
     * This implementation should not have a side effect of adding the UUID to any indexes, if the UUID isn't yet present.
     * @return an Optional ConceptChronology.
     */
    Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(UUID... conceptUuids);
    
    void writeConcept(ConceptChronology<? extends ConceptVersion<?>> concept);

    boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate);
    
    ConceptSnapshotService getSnapshot(StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate);
    
    @Deprecated
    default ConceptSnapshotService getSnapshot(StampCoordinate stampCoordinate) {
        return getSnapshot(stampCoordinate, Get.configurationService().getDefaultLanguageCoordinate());
    }
    
    int getConceptCount();
    
    Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream();
    Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream();

    Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream(ConceptSequenceSet conceptSequences);
    Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream(ConceptSequenceSet conceptSequences);
    
    /**
     * For backward compatibility reasons only. 
     * @return 
     * @deprecated 
     */
    @Deprecated
    ConceptService getDelegate();
}
