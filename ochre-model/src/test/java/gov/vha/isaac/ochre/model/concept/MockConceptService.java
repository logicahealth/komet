package gov.vha.isaac.ochre.model.concept;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptSnapshotService;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import org.jvnet.hk2.annotations.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Created by kec on 1/3/16.
 */
@Service
public class MockConceptService implements ConceptService {

    ConcurrentHashMap<Integer, ConceptChronology<? extends ConceptVersion<?>>> conceptsMap = new ConcurrentHashMap<>();
    @Override
    public ConceptChronology<? extends ConceptVersion<?>> getConcept(int conceptId) {
        return conceptsMap.get(Get.identifierService().getConceptSequence(conceptId));
    }

    @Override
    public ConceptChronology<? extends ConceptVersion<?>> getConcept(UUID... conceptUuids) {
        int conceptNid = Get.identifierService().getNidForUuids(conceptUuids);
        int conceptSequence = Get.identifierService().getConceptSequence(conceptNid);
        if (conceptsMap.containsKey(conceptSequence)) {
            return conceptsMap.get(Get.identifierService().getConceptSequenceForUuids(conceptUuids));
        }

        ConceptChronologyImpl concept = new ConceptChronologyImpl(conceptUuids[0], conceptNid, conceptSequence);
        if (conceptUuids.length > 1) {
            concept.setAdditionalUuids(Arrays.asList(Arrays.copyOfRange(conceptUuids, 1, conceptUuids.length)));
        }
        conceptsMap.put(conceptSequence, concept);
        return concept;

    }

    @Override
    public Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(int conceptId) {
        return Optional.ofNullable(getConcept(conceptId));
    }

    @Override
    public boolean hasConcept(int conceptId) {
        return conceptsMap.containsKey(Get.identifierService().getConceptSequence(conceptId));
    }

	@Override
    public Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(UUID... conceptUuids) {
        return Optional.ofNullable(getConcept(conceptUuids));
    }

    @Override
    public void writeConcept(ConceptChronology<? extends ConceptVersion<?>> concept) {
        conceptsMap.put(concept.getConceptSequence(), concept);
    }

    @Override
    public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
        return false;
    }

    @Override
    public ConceptSnapshotService getSnapshot(StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getConceptCount() {
        return conceptsMap.size();
    }

    @Override
    public Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream() {
        return conceptsMap.values().stream();
    }

    @Override
    public Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream() {
        return conceptsMap.values().parallelStream();
    }

    @Override
    public Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream(ConceptSequenceSet conceptSequences) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream(ConceptSequenceSet conceptSequences) {
        throw new UnsupportedOperationException();
    }
}
