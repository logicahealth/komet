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
package gov.vha.isaac.identifier;

import gov.vha.isaac.ochre.api.*;
import gov.vha.isaac.ochre.api.collections.UuidIntMapMap;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.collections.LruCache;
import gov.vha.isaac.ochre.api.collections.NidSet;
import gov.vha.isaac.ochre.api.collections.SememeSequenceSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
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
public class IdentifierProvider implements IdentifierService, IdentifiedObjectService {

    private static final Logger LOG = LogManager.getLogger();
    /**
     * For debugging...
     */
    private static HashSet<UUID> watchSet = new HashSet<>();

//    {
//        watchSet.add(UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9"));
//        watchSet.add(UUID.fromString("4459d8cf-5a6f-3952-9458-6d64324b27b7"));
//    }
    private static ThreadLocal<LinkedHashMap<UUID, Integer>> THREAD_LOCAL_CACHE
            = new ThreadLocal() {
        @Override
        protected LruCache<UUID, Integer> initialValue() {
            return new LruCache<>(50);
        }
    };
    
    private final Path folderPath;
    private final UuidIntMapMap uuidIntMapMap;
    private final SequenceMap conceptSequenceMap;
    private final SequenceMap sememeSequenceMap;
     private final AtomicBoolean loadRequired = new AtomicBoolean();

    private IdentifierProvider() throws IOException {
        //for HK2
        LOG.info("IdentifierProvider constructed");
        folderPath = LookupService.getService(ConfigurationService.class).getChronicleFolderPath().resolve("identifier-provider");
        loadRequired.set(!Files.exists(folderPath));
        Files.createDirectories(folderPath);
        uuidIntMapMap = UuidIntMapMap.create(new File(folderPath.toAbsolutePath().toFile(), "uuid-nid-map"));
        conceptSequenceMap = new SequenceMap(450000);
        sememeSequenceMap = new SequenceMap(3000000);
    }

    @PostConstruct
    private void startMe() {
        try {
            LOG.info("Starting IdentifierProvider post-construct - reading from " + folderPath);
            if (!loadRequired.get()) {
                final String conceptSequenceMapBaseName = "concept-sequence.map";
                LOG.info("Loading {} from dir {}.", conceptSequenceMapBaseName, folderPath.toAbsolutePath().normalize().toString());
                conceptSequenceMap.read(new File(folderPath.toFile(), conceptSequenceMapBaseName));

                final String sememeSequenceMapBaseName = "sememe-sequence.map";
                LOG.info("Loading {} from dir {}.", sememeSequenceMapBaseName, folderPath.toAbsolutePath().normalize().toString());
                sememeSequenceMap.read(new File(folderPath.toFile(), sememeSequenceMapBaseName));

                // uuid-nid-map can do dynamic load, no need to read all at the beginning.
                // LOG.info("Loading uuid-nid-map.");
                // uuidIntMapMap.read();
            }
        } catch (Exception e) {
            LookupService.getService(SystemStatusService.class).notifyServiceConfigurationFailure("Identifier Provider", e);
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    private void stopMe() {
        try {
            uuidIntMapMap.setShutdown(true);
            LOG.info("conceptSequence: {}", conceptSequenceMap.getNextSequence());
            LOG.info("writing concept-sequence.map.");
            conceptSequenceMap.write(new File(folderPath.toFile(), "concept-sequence.map"));
            LOG.info("writing sememe-sequence.map.");
            sememeSequenceMap.write(new File(folderPath.toFile(), "sememe-sequence.map"));
            LOG.info("writing uuid-nid-map.");
            uuidIntMapMap.write();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getMaxNid() {
        return uuidIntMapMap.getNextNidProvider().get();
    }

    @Override
    public ObjectChronologyType getChronologyTypeForNid(int nid) {
        if (sememeSequenceMap.containsNid(nid)) {
            return ObjectChronologyType.SEMEME;
        }
        if (conceptSequenceMap.containsNid(nid)) {
            return ObjectChronologyType.CONCEPT;
        }
        return ObjectChronologyType.UNKNOWN_NID;
    }

    @Override
    public int getConceptSequence(int nid) {
        if (nid >= 0) {
            return nid;
        }
        return conceptSequenceMap.addNidIfMissing(nid);
    }

    @Override
    public int getConceptNid(int conceptSequence) {
        if (conceptSequence < 0) {
            return conceptSequence;
        }
        int conceptNid = conceptSequenceMap.getNidFast(conceptSequence);
        if (conceptSequence != 0 && conceptNid == 0) {
            LOG.warn("retrieved nid=" + conceptNid + " for sequence=" + conceptSequence);
        }
        return conceptNid;
    }

    @Override
    public int getSememeSequence(int sememeId) {
        if (sememeId >= 0) {
            return sememeId;
        }
        return sememeSequenceMap.addNidIfMissing(sememeId);
    }

    @Override
    public int getSememeNid(int sememeId) {
        if (sememeId < 0) {
            return sememeId;
        }
        return sememeSequenceMap.getNidFast(sememeId);
    }

    @Override
    public IntStream getConceptSequenceStream() {
        return conceptSequenceMap.getSequenceStream();
    }

    @Override
    public IntStream getParallelConceptSequenceStream() {
        return conceptSequenceMap.getSequenceStream().parallel();
    }

    @Override
    public IntStream getSememeSequenceStream() {
        return sememeSequenceMap.getSequenceStream();
    }

    @Override
    public IntStream getParallelSememeSequenceStream() {
        return sememeSequenceMap.getSequenceStream().parallel();
    }

    @Override
    public ConceptSequenceSet getConceptSequencesForConceptNids(int[] conceptNidArray) {
        ConceptSequenceSet sequences = new ConceptSequenceSet();
        IntStream.of(conceptNidArray).forEach((nid) -> sequences.add(conceptSequenceMap.getSequenceFast(nid)));
        return sequences;
    }

    @Override
    public ConceptSequenceSet getConceptSequencesForConceptNids(NidSet conceptNidSet) {
        ConceptSequenceSet sequences = new ConceptSequenceSet();
        conceptNidSet.stream().forEach((nid) -> sequences.add(conceptSequenceMap.getSequenceFast(nid)));
        return sequences;
    }

    @Override
    public SememeSequenceSet getSememeSequencesForSememeNids(int[] sememeNidArray) {
        SememeSequenceSet sequences = new SememeSequenceSet();
        IntStream.of(sememeNidArray).forEach((nid) -> sequences.add(sememeSequenceMap.getSequenceFast(nid)));
        return sequences;
    }

    @Override
    public IntStream getConceptNidsForConceptSequences(IntStream conceptSequences) {
        return conceptSequences.map((sequence) -> {
            return getConceptNid(sequence);
        });
    }

    @Override
    public IntStream getSememeNidsForSememeSequences(IntStream sememSequences) {
        return sememSequences.map((sequence) -> {
            return getSememeNid(sequence);
        });
    }

    @Override
    public int getNidForUuids(Collection<UUID> uuids) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getNidForUuids(UUID... uuids) {
        LinkedHashMap<UUID, Integer> cacheMap = THREAD_LOCAL_CACHE.get();
        Integer cacheNid = cacheMap.get(uuids[0]);
        if (cacheNid != null) {
            return cacheNid;
        }
        for (UUID uuid : uuids) {
//          if (watchSet.contains(uuid)) {
//             System.out.println("Found watch: " + Arrays.asList(uuids));
//             watchSet.remove(uuid);
//          }
            int nid = uuidIntMapMap.get(uuid);
            if (nid != Integer.MAX_VALUE) {
                cacheMap.put(uuids[0], nid);
                return nid;
            }
        }
        int nid = uuidIntMapMap.getWithGeneration(uuids[0]);
        cacheMap.put(uuids[0], nid);
        for (int i = 1; i < uuids.length; i++) {
            uuidIntMapMap.put(uuids[i], nid);
        }
        return nid;
    }

    @Override
    public Optional<UUID> getUuidPrimordialForNid(int nid) {
        //If we have a cache in uuidIntMapMap, read from there, it is faster.
        //If we don't have a cache, then uuidIntMapMap will be extremely slow, so try this first.
        if (!uuidIntMapMap.cacheContainsNid(nid)) {
            Optional<? extends ObjectChronology<? extends StampedVersion>> optionalObj
                    = Get.identifiedObjectService().getIdentifiedObjectChronology(nid);
            if (optionalObj.isPresent()) {
                return Optional.of(optionalObj.get().getPrimordialUuid());
            }
        }
        UUID[] uuids = uuidIntMapMap.getKeysForValue(nid);
        //In the use case of directly writing files (converting terminology) this is a normal occurrence
        LOG.debug("[1] No object for nid: " + nid + " Found uuids: " + Arrays.asList(uuids));

        if (uuids.length > 0) {
            return Optional.of(uuids[0]);
        }
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getUuidPrimordialFromConceptSequence(int conceptSequence) {
        return getUuidPrimordialForNid(getConceptNid(conceptSequence));
    }

    @Override
    public Optional<UUID> getUuidPrimordialFromSememeSequence(int sememeSequence) {
        return getUuidPrimordialForNid(getSememeNid(sememeSequence));
    }

    /**
     * @param nid
     * @return A list of uuids corresponding with a nid.
     */
    @Override
    public List<UUID> getUuidsForNid(int nid) {
        if (nid > 0) {
            nid = getConceptNid(nid);
        }
        Optional<? extends ObjectChronology<? extends StampedVersion>> optionalObj
                = Get.identifiedObjectService().getIdentifiedObjectChronology(nid);
        if (optionalObj.isPresent()) {
            return optionalObj.get().getUuidList();
        }

        UUID[] uuids = uuidIntMapMap.getKeysForValue(nid);
        LOG.warn("[3] No object for nid: " + nid + " Found uuids: " + Arrays.asList(uuids));
        return Arrays.asList(uuids);
    }

    @Override
    public boolean hasUuid(UUID... uuids) {
        if (uuids == null) {
            throw new IllegalArgumentException("A UUID must be specified.");
        }
        return Arrays.stream(uuids).anyMatch((uuid) -> (uuidIntMapMap.containsKey(uuid)));
    }

    @Override
    public boolean hasUuid(Collection<UUID> uuids) {
        if (uuids == null) {
            throw new IllegalArgumentException("A UUID must be specified.");
        }
        return uuids.stream().anyMatch((uuid) -> (uuidIntMapMap.containsKey(uuid)));
    }

    @Override
    public void addUuidForNid(UUID uuid, int nid) {
        uuidIntMapMap.put(uuid, nid);
    }



    @Override
    public int getConceptSequenceForUuids(Collection<UUID> uuids) {
        return getConceptSequenceForUuids(uuids.toArray(new UUID[uuids.size()]));
    }

    @Override
    public int getConceptSequenceForUuids(UUID... uuids) {
        return getConceptSequence(getNidForUuids(uuids));
    }


    @Override
    public int getNidForProxy(ConceptSpecification conceptProxy) {
        return getNidForUuids(conceptProxy.getUuids());
    }

    @Override
    public int getConceptSequenceForProxy(ConceptSpecification conceptProxy) {
        return getConceptSequence(getNidForProxy(conceptProxy));
    }

    @Override
    public int getSememeSequenceForUuids(Collection<UUID> uuids) {
        return getSememeSequence(getNidForUuids(uuids));
    }

    @Override
    public int getSememeSequenceForUuids(UUID... uuids) {
        return getSememeSequence(getNidForUuids(uuids));
    }

    @Override
    public Optional<LatestVersion<String>> getIdentifierForAuthority(int nid, UUID identifierAuthorityUuid, StampCoordinate stampCoordinate) {
        if (nid >= 0) {
            throw new IllegalStateException("Not a nid: " + nid);
        }
        int authoritySequence = getConceptSequenceForUuids(identifierAuthorityUuid);
        SememeSnapshotService<StringSememe> snapshot
                = Get.sememeService().getSnapshot(
                        StringSememe.class,
                        stampCoordinate);
        return snapshot.getLatestSememeVersionsForComponentFromAssemblage(nid, authoritySequence).findAny().map((LatestVersion<StringSememe> latestSememe) -> {
            LatestVersion<String> latestString = new LatestVersion<>(latestSememe.value().getString());
            if (latestSememe.contradictions().isPresent()) {
                for (StringSememe version : latestSememe.contradictions().get()) {
                    latestString.addLatest(version.getString());
                }
            }
            return latestString;
        });
    }

    @Override
    public Optional<LatestVersion<String>> getConceptIdentifierForAuthority(int conceptId, UUID identifierAuthorityUuid, StampCoordinate stampCoordinate) {
        conceptId = getConceptNid(conceptId);
        return getIdentifierForAuthority(conceptId, identifierAuthorityUuid, stampCoordinate);
    }

    protected static void reset() {
        THREAD_LOCAL_CACHE
                = new ThreadLocal() {
            @Override
            protected LruCache<UUID, Integer> initialValue() {
                return new LruCache<>(50);
            }
        };
    }

    @Override
    public Optional<? extends ObjectChronology<? extends StampedVersion>> getIdentifiedObjectChronology(int nid) {

        switch (getChronologyTypeForNid(nid)) {
            case CONCEPT:
                return Get.conceptService().getOptionalConcept(nid);
            case SEMEME:
                return Get.sememeService().getOptionalSememe(nid);
            case UNKNOWN_NID:
                return Optional.empty();
        }
        throw new UnsupportedOperationException("Unknown chronology type: " + getChronologyTypeForNid(nid));

    }
}
