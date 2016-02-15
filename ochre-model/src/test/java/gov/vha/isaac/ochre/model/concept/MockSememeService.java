package gov.vha.isaac.ochre.model.concept;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.collections.NidSet;
import gov.vha.isaac.ochre.api.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeConstraints;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.SememeServiceTyped;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;

/**
 * Created by kec on 1/3/16.
 */
@Service
@Rank(value=-50)
public class MockSememeService implements SememeService {

    ConcurrentHashMap<Integer, SememeSequenceSet> componentSememeMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, SememeChronology<? extends SememeVersion<?>>> sememeMap = new ConcurrentHashMap<>();

    @Override
    public <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType, StampCoordinate stampCoordinate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V extends SememeVersion> SememeServiceTyped<V> ofType(Class<V> versionType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SememeChronology<? extends SememeVersion<?>> getSememe(int sememeId) {
        return sememeMap.get(Get.identifierService().getSememeSequence(sememeId));
    }

    @Override
    public Optional<? extends SememeChronology<? extends SememeVersion<?>>> getOptionalSememe(int sememeId) {
        return Optional.ofNullable(getSememe(sememeId));
    }
    
    

    



	@Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesFromAssemblage(int assemblageConceptSequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageConceptSequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet, int assemblageConceptSequence, StampPosition position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponent(int componentNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SememeSequenceSet getSememeSequencesForComponent(int componentNid) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblages(int componentNid, Set<Integer> allowedAssemblageSequences) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public SememeSequenceSet getSememeSequencesForComponentFromAssemblages(int componentNid, Set<Integer> allowedAssemblageSequences) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet, int assemblageConceptSequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeSememe(SememeChronology<?> sememeChronicle, SememeConstraints... constraints) {
        if (componentSememeMap.containsKey(sememeChronicle.getReferencedComponentNid())) {
            componentSememeMap.get(sememeChronicle.getReferencedComponentNid()).add(sememeChronicle.getSememeSequence());
        } else {
            SememeSequenceSet set = SememeSequenceSet.of(sememeChronicle.getSememeSequence());
            componentSememeMap.put(sememeChronicle.getReferencedComponentNid(), set);
        }
        sememeMap.put(sememeChronicle.getSememeSequence(), (SememeChronology<? extends SememeVersion<?>>) sememeChronicle);
    }

    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getSememeChronologyStream() {
        return sememeMap.values().stream();
    }

    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getParallelSememeStream() {
        return sememeMap.values().parallelStream();
    }

    @Override
    public Stream<SememeChronology<? extends DescriptionSememe<?>>> getDescriptionsForComponent(int componentNid) {
        SememeSequenceSet set = componentSememeMap.get(componentNid);
        Stream.Builder<SememeChronology<? extends DescriptionSememe<?>>> builder = Stream.builder();
        if (set != null) {
            set.stream().forEach((sememeSequence) -> {
                SememeChronology sememeChronology = sememeMap.get(sememeSequence);
                if (sememeChronology.getSememeType() == SememeType.DESCRIPTION) {
                    builder.accept(sememeChronology);
                }
            });
        }
        return builder.build();
    }

    @Override
    public Stream<Integer> getAssemblageTypes() {
        throw new UnsupportedOperationException();
    }
}
