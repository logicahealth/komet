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
package gov.vha.isaac.ochre.sememe.provider;


import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.SystemStatusService;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeConstraints;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.SememeServiceTyped;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.collections.NidSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.waitfree.CasSequenceObjectMap;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 0)
public class SememeProvider implements SememeService {

    private static final Logger LOG = LogManager.getLogger();

    final CasSequenceObjectMap<SememeChronologyImpl<?>> sememeMap;
    final ConcurrentSkipListSet<AssemblageSememeKey> assemblageSequenceSememeSequenceMap = new ConcurrentSkipListSet<>();
    final ConcurrentSkipListSet<ReferencedNidAssemblageSequenceSememeSequenceKey> referencedNidAssemblageSequenceSememeSequenceMap = new ConcurrentSkipListSet<>();
    final Path sememePath;
    private transient HashSet<Integer> inUseAssemblages = new HashSet<>();
    private AtomicBoolean loadRequired = new AtomicBoolean();

    //For HK2
    private SememeProvider() throws IOException {
        try {
            sememePath = LookupService.getService(ConfigurationService.class)
                    .getChronicleFolderPath().resolve("sememe");
            loadRequired.set(!Files.exists(sememePath));
            Files.createDirectories(sememePath);
            LOG.info("Setting up sememe provider at " + sememePath.toAbsolutePath().toString());

            sememeMap = new CasSequenceObjectMap<>(new SememeSerializer(), sememePath, "seg.", ".sememe.map");
        } catch (Exception e) {
            LookupService.getService(SystemStatusService.class).notifyServiceConfigurationFailure("Cradle Commit Manager", e);
            throw e;
        }
    }

    @PostConstruct
    private void startMe() throws IOException {
        try {
            LOG.info("Loading sememeMap.");
            if (!loadRequired.get()) {
                LOG.info("Reading existing sememeMap.");
                sememeMap.initialize();

                LOG.info("Reading existing SememeKeys.");

                try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(sememePath.toFile(), "assemblage-sememe.keys"))))) {
                    int size = in.readInt();
                    for (int i = 0; i < size; i++) {
                        int assemblageSequence = in.readInt();
                        int sequence = in.readInt();
                        assemblageSequenceSememeSequenceMap.add(new AssemblageSememeKey(assemblageSequence, sequence));
                        inUseAssemblages.add(assemblageSequence);
                    }
                }
                try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(sememePath.toFile(), "component-sememe.keys"))))) {
                    int size = in.readInt();
                    for (int i = 0; i < size; i++) {
                        int referencedNid = in.readInt();
                        int assemblageSequence = in.readInt();
                        int sequence = in.readInt();
                        referencedNidAssemblageSequenceSememeSequenceMap.add(new ReferencedNidAssemblageSequenceSememeSequenceKey(referencedNid, assemblageSequence, sequence));
                    }
                }
            }

            SememeSequenceSet statedGraphSequences = getSememeSequencesFromAssemblage(Get.identifierService().getConceptSequence(Get.identifierService().getNidForUuids(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getUuids())));
            LOG.info("Stated logic graphs: " + statedGraphSequences.size());

            SememeSequenceSet inferedGraphSequences = getSememeSequencesFromAssemblage(Get.identifierService().getConceptSequence(Get.identifierService().getNidForUuids(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getUuids())));

            LOG.info("Inferred logic graphs: " + inferedGraphSequences.size());
            LOG.info("Finished SememeProvider load.");
        } catch (Exception e) {
            LookupService.getService(SystemStatusService.class).notifyServiceConfigurationFailure("Cradle Commit Manager", e);
            throw e;
        }
    }

    @PreDestroy
    private void stopMe() throws IOException {
        LOG.info("Stopping SememeProvider pre-destroy. ");

        //Dan commented out this LOG statement because it is really slow...
        //log.info("sememeMap size: {}", sememeMap.getSize());
        LOG.info("writing sememe-map.");
        sememeMap.write();

        LOG.info("writing SememeKeys.");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(sememePath.toFile(), "assemblage-sememe.keys"))))) {
            out.writeInt(assemblageSequenceSememeSequenceMap.size());
            for (AssemblageSememeKey key : assemblageSequenceSememeSequenceMap) {
                out.writeInt(key.assemblageSequence);
                out.writeInt(key.sememeSequence);
            }
        }
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(sememePath.toFile(), "component-sememe.keys"))))) {
            out.writeInt(referencedNidAssemblageSequenceSememeSequenceMap.size());
            for (ReferencedNidAssemblageSequenceSememeSequenceKey key : referencedNidAssemblageSequenceSememeSequenceMap) {
                out.writeInt(key.referencedNid);
                out.writeInt(key.assemblageSequence);
                out.writeInt(key.sememeSequence);
            }
        }
        LOG.info("Finished SememeProvider stop.");
    }

    @Override
    public <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType, StampCoordinate stampCoordinate) {
        return new SememeSnapshotProvider<>(versionType, stampCoordinate, this);
    }

    @Override
    public SememeChronology<? extends SememeVersion<?>> getSememe(int sememeId) {
        sememeId = Get.identifierService().getSememeSequence(sememeId);
        return sememeMap.getQuick(sememeId);
    }

    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesFromAssemblage(int assemblageConceptSequence) {
        SememeSequenceSet sememeSequences = getSememeSequencesFromAssemblage(assemblageConceptSequence);
        return sememeSequences.stream().mapToObj((int sememeSequence) -> getSememe(sememeSequence));
    }

    @Override
    public SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageConceptSequence) {
        assemblageConceptSequence = Get.identifierService().getConceptSequence(assemblageConceptSequence);
        AssemblageSememeKey rangeStart = new AssemblageSememeKey(assemblageConceptSequence, Integer.MIN_VALUE); // yes
        AssemblageSememeKey rangeEnd = new AssemblageSememeKey(assemblageConceptSequence, Integer.MAX_VALUE); // no
        NavigableSet<AssemblageSememeKey> assemblageSememeKeys
                = assemblageSequenceSememeSequenceMap.subSet(rangeStart, true,
                        rangeEnd, true
                );
        return SememeSequenceSet.of(assemblageSememeKeys.stream().mapToInt((AssemblageSememeKey key) -> key.sememeSequence));
    }

    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponent(int componentNid) {
        SememeSequenceSet sememeSequences = getSememeSequencesForComponent(componentNid);
        return sememeSequences.stream().mapToObj((int sememeSequence) -> getSememe(sememeSequence));
    }

    @Override
    public SememeSequenceSet getSememeSequencesForComponent(int componentNid) {
        if (componentNid >= 0) {
            throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
        }
        NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> assemblageSememeKeys
                = referencedNidAssemblageSequenceSememeSequenceMap.subSet(
                        new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid, Integer.MIN_VALUE, Integer.MIN_VALUE), true,
                        new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid, Integer.MAX_VALUE, Integer.MAX_VALUE), true
                );
        return SememeSequenceSet.of(assemblageSememeKeys.stream().mapToInt((ReferencedNidAssemblageSequenceSememeSequenceKey key) -> key.sememeSequence));
    }

    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblage(int componentNid, int mblageConceptSequence) {
        if (componentNid >= 0) {
            componentNid = Get.identifierService().getConceptNid(componentNid);
        }
        if (mblageConceptSequence < 0) {
            mblageConceptSequence = Get.identifierService().getConceptSequence(mblageConceptSequence);
        }
        SememeSequenceSet sememeSequences = getSememeSequencesForComponentFromAssemblage(componentNid, mblageConceptSequence);
        return sememeSequences.stream().mapToObj((int sememeSequence) -> getSememe(sememeSequence));
    }

    @Override
    public SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence) {
        if (componentNid >= 0) {
            throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
        }
        assemblageConceptSequence = Get.identifierService().getConceptSequence(assemblageConceptSequence);
        ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeStart = new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid, assemblageConceptSequence, Integer.MIN_VALUE); // yes
        ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeEnd = new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid, assemblageConceptSequence, Integer.MAX_VALUE); // no
        NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> referencedComponentRefexKeys
                = referencedNidAssemblageSequenceSememeSequenceMap.subSet(rcRangeStart, true,
                        rcRangeEnd, true
                );

        SememeSequenceSet referencedComponentSet
                = SememeSequenceSet.of(referencedComponentRefexKeys.stream()
                        .mapToInt((ReferencedNidAssemblageSequenceSememeSequenceKey key) -> key.sememeSequence));

        return referencedComponentSet;
    }

    @Override
    public SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet, final int assemblageConceptSequence) {
        if (assemblageConceptSequence < 0) {
            throw new IndexOutOfBoundsException("assemblageSequence must be >= 0. Found: " + assemblageConceptSequence);
        }
        SememeSequenceSet resultSet = new SememeSequenceSet();
        componentNidSet.stream().forEach((componentNid) -> {
            ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeStart = new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid, assemblageConceptSequence, Integer.MIN_VALUE); // yes
            ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeEnd = new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid, assemblageConceptSequence, Integer.MAX_VALUE); // no
            NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> referencedComponentRefexKeys
                    = referencedNidAssemblageSequenceSememeSequenceMap.subSet(rcRangeStart, true,
                            rcRangeEnd, true
                    );
            referencedComponentRefexKeys.stream().forEach((key) -> {
                resultSet.add(key.sememeSequence);
            });

        });

        return resultSet;
    }

    @Override
    public void writeSememe(SememeChronology<?> sememeChronicle, SememeConstraints... constraints) {
        Arrays.stream(constraints).forEach((constraint) -> {
            switch (constraint) {
                case ONE_SEMEME_PER_COMPONENT:
                    ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeStart = new ReferencedNidAssemblageSequenceSememeSequenceKey(sememeChronicle.getReferencedComponentNid(), sememeChronicle.getAssemblageSequence(), Integer.MIN_VALUE); // yes
                    ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeEnd = new ReferencedNidAssemblageSequenceSememeSequenceKey(sememeChronicle.getReferencedComponentNid(), sememeChronicle.getAssemblageSequence(), Integer.MAX_VALUE); // no
                    NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> subset = referencedNidAssemblageSequenceSememeSequenceMap.subSet(rcRangeStart, rcRangeEnd);
                    if (!subset.isEmpty()) {
                        if (!subset.stream().allMatch((value) -> value.sememeSequence == sememeChronicle.getSememeSequence())) {
                            throw new IllegalStateException("Attempt to add a second sememe for component, where assemblage has a ONE_SEMEME_PER_COMPONENT constraint."
                                    + "\n New sememe: " + sememeChronicle + "\n Existing in index: " + subset);
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Can't handle " + constraint);
            }
        });
        assemblageSequenceSememeSequenceMap.add(
                new AssemblageSememeKey(sememeChronicle.getAssemblageSequence(),
                        sememeChronicle.getSememeSequence()));
        inUseAssemblages.add(sememeChronicle.getAssemblageSequence());
        referencedNidAssemblageSequenceSememeSequenceMap.add(
                new ReferencedNidAssemblageSequenceSememeSequenceKey(sememeChronicle.getReferencedComponentNid(),
                        sememeChronicle.getAssemblageSequence(),
                        sememeChronicle.getSememeSequence()));
        sememeMap.put(sememeChronicle.getSememeSequence(),
                (SememeChronologyImpl<?>) sememeChronicle);
    }

    @Override
    public SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(
            NidSet componentNidSet, int assemblageConceptSequence, StampPosition position) {
        SememeSequenceSet sequencesToTest
                = getSememeSequencesForComponentsFromAssemblage(componentNidSet, assemblageConceptSequence);
        SememeSequenceSet sequencesThatPassedTest = new SememeSequenceSet();
        sequencesToTest.stream().forEach((sememeSequence) -> {
            SememeChronologyImpl<?> chronicle = (SememeChronologyImpl<?>) getSememe(sememeSequence);
            if (chronicle.getVersionStampSequences().anyMatch((stampSequence) -> {
                return (Get.commitService().getTimeForStamp(stampSequence) > position.getTime()
                        && (position.getStampPathSequence() == Get.commitService().getPathSequenceForStamp(stampSequence)));
            })) {
                sequencesThatPassedTest.add(sememeSequence);
            }
        });
        return sequencesThatPassedTest;
    }

    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getSememeStream() {
        return Get.identifierService().getSememeSequenceStream().mapToObj((int sememeSequence) -> getSememe(sememeSequence));
    }

    @Override
    public Stream<SememeChronology<? extends SememeVersion<?>>> getParallelSememeStream() {
        return Get.identifierService().getSememeSequenceStream().parallel().mapToObj((int sememeSequence) -> {
            try {
                //TODO Keith - this is DEBUG code that should be removed - it isn't proper to inject a null into the return stream.  However, something _ELSE_
                //is broken at the moment, and the sememeSequenceStream is returning invalid sememe identifiers... eek.
                return getSememe(sememeSequence);
            } catch (Exception e) {
                LOG.error("sememe sequence " + sememeSequence + " could not be resolved!", e);
                return null;
            }
        });
    }
    int descriptionAssemblageSequence = Integer.MIN_VALUE;

    @Override
    public Stream<SememeChronology<? extends DescriptionSememe<?>>> getDescriptionsForComponent(int componentNid) {
        if (descriptionAssemblageSequence == Integer.MIN_VALUE) {
            //TODO support descriptions assemblage for languages other than english. 
            descriptionAssemblageSequence = Get.identifierService().getConceptSequenceForUuids(TermAux.ENGLISH_DESCRIPTION_ASSEMBLAGE.getUuids());
        }
        SememeSequenceSet sequences = getSememeSequencesForComponentFromAssemblage(componentNid, descriptionAssemblageSequence);
        IntFunction<SememeChronology<? extends DescriptionSememe<?>>> mapper = (int sememeSequence) -> 
            (SememeChronology<? extends DescriptionSememe<?>>)getSememe(sememeSequence);
        return sequences.stream().filter((int sememeSequence) -> getOptionalSememe(sememeSequence).isPresent()).mapToObj(mapper);
    }

    @Override
    public <V extends SememeVersion> SememeServiceTyped<V> ofType(Class<V> versionType) {
        return new SememeTypeProvider<>(versionType, this);
    }

    @Override
    public Optional<? extends SememeChronology<? extends SememeVersion<?>>> getOptionalSememe(int sememeSequence) {
        sememeSequence = Get.identifierService().getSememeSequence(sememeSequence);
        return sememeMap.getOptional(sememeSequence);
    }

	@Override
	public Stream<Integer> getAssemblageTypes()
	{
		return inUseAssemblages.stream();
	}
}
