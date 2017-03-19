/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.provider.identifier;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FilenameFilter;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.DatabaseServices.DatabaseValidity;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedObjectService;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.LruCache;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.collections.UuidIntMapMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.version.StringSememe;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.identity.StampedVersion;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 0)
public class IdentifierProvider
         implements IdentifierService, IdentifiedObjectService {
   private static final Logger LOG = LogManager.getLogger();

   /**
    * For debugging...
    */
   private static HashSet<UUID> watchSet = new HashSet<>();

// {
//     watchSet.add(UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9"));
//     watchSet.add(UUID.fromString("4459d8cf-5a6f-3952-9458-6d64324b27b7"));
// }
   private static ThreadLocal<LinkedHashMap<UUID, Integer>> THREAD_LOCAL_CACHE = new ThreadLocal() {
      @Override
      protected LruCache<UUID, Integer> initialValue() {
         return new LruCache<>(50);
      }
   };

   //~--- fields --------------------------------------------------------------

   private final AtomicBoolean loadRequired     = new AtomicBoolean();
   private DatabaseValidity    databaseValidity = DatabaseValidity.NOT_SET;
   private final Path          folderPath;
   private final UuidIntMapMap uuidIntMapMap;
   private final SequenceMap   conceptSequenceMap;
   private final SequenceMap   sememeSequenceMap;

   //~--- constructors --------------------------------------------------------

   private IdentifierProvider()
            throws IOException {
      // for HK2
      LOG.info("IdentifierProvider constructed");
      folderPath = LookupService.getService(ConfigurationService.class)
                                .getChronicleFolderPath()
                                .resolve("identifier-provider");

      if (!Files.exists(folderPath)) {
         databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
      }

      loadRequired.set(!Files.exists(folderPath));
      Files.createDirectories(folderPath);
      uuidIntMapMap      = UuidIntMapMap.create(new File(folderPath.toAbsolutePath().toFile(), "uuid-nid-map"));
      conceptSequenceMap = new SequenceMap(450000);
      sememeSequenceMap  = new SequenceMap(3000000);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void addUuidForNid(UUID uuid, int nid) {
      uuidIntMapMap.put(uuid, nid);
   }

   @Override
   public void clearDatabaseValidityValue() {
      // Reset to enforce analysis
      databaseValidity = DatabaseValidity.NOT_SET;
   }

   /**
    * A method to remove refs to sememe or concept sequences that never had data stored.
    * This should not be necessary in normal operation.  This supports patterns where objects are
    * being deserialized from an ibdf file (causing refs to be stored here) but then not loaded into the DB.
    */
   @Override
   public void clearUnusedIds() {
      AtomicInteger cleaned = new AtomicInteger();

      conceptSequenceMap.getSequenceStream().parallel().forEach((conceptSequence) -> {
                                    if (!Get.conceptService()
                                            .hasConcept(conceptSequence)) {
                                       int nid = conceptSequenceMap.getNid(conceptSequence)
                                                                   .getAsInt();

                                       conceptSequenceMap.removeNid(nid);
                                       cleaned.incrementAndGet();
                                    }
                                 });
      LOG.info("Removed " + cleaned.get() + " unused concept references");
      cleaned.set(0);
      sememeSequenceMap.getSequenceStream().parallel().forEach((sememeSequence) -> {
                                   if (!Get.sememeService()
                                           .hasSememe(sememeSequence)) {
                                      int nid = sememeSequenceMap.getNid(sememeSequence)
                                                                 .getAsInt();

                                      sememeSequenceMap.removeNid(nid);
                                      cleaned.incrementAndGet();
                                   }
                                });
      LOG.info("Removed " + cleaned.get() + " unused sememe references");

      // We could also clear refs from the uuid map here... but that would take longer /
      // provide minimal gain
   }

   protected static void reset() {
      THREAD_LOCAL_CACHE = new ThreadLocal() {
         @Override
         protected LruCache<UUID, Integer> initialValue() {
            return new LruCache<>(50);
         }
      };
   }

   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting IdentifierProvider post-construct - reading from " + folderPath);

         if (!loadRequired.get()) {
            final String conceptSequenceMapBaseName = "concept-sequence.map";

            LOG.info("Loading {} from dir {}.",
                     conceptSequenceMapBaseName,
                     folderPath.toAbsolutePath()
                               .normalize()
                               .toString());
            conceptSequenceMap.read(new File(folderPath.toFile(), conceptSequenceMapBaseName));

            final String sememeSequenceMapBaseName = "sememe-sequence.map";

            LOG.info("Loading {} from dir {}.",
                     sememeSequenceMapBaseName,
                     folderPath.toAbsolutePath()
                               .normalize()
                               .toString());
            sememeSequenceMap.read(new File(folderPath.toFile(), sememeSequenceMapBaseName));

            // uuid-nid-map can do dynamic load, no need to read all at the beginning.
            // LOG.info("Loading uuid-nid-map.");
            // uuidIntMapMap.read();
            if (isPopulated()) {
               databaseValidity = DatabaseValidity.POPULATED_DIRECTORY;
            }
         }
      } catch (Exception e) {
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Identifier Provider", e);
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
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   //~--- get methods ---------------------------------------------------------

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
   public Optional<LatestVersion<String>> getConceptIdentifierForAuthority(int conceptId,
         UUID identifierAuthorityUuid,
         StampCoordinate stampCoordinate) {
      conceptId = getConceptNid(conceptId);
      return getIdentifierForAuthority(conceptId, identifierAuthorityUuid, stampCoordinate);
   }

   @Override
   public int getConceptNid(int conceptSequence) {
      if (conceptSequence < 0) {
         return conceptSequence;
      }

      int conceptNid = conceptSequenceMap.getNidFast(conceptSequence);

      if ((conceptSequence != 0) && (conceptNid == 0)) {
         LOG.warn("retrieved nid=" + conceptNid + " for sequence=" + conceptSequence);
      }

      return conceptNid;
   }

   @Override
   public IntStream getConceptNidsForConceptSequences(IntStream conceptSequences) {
      return conceptSequences.map((sequence) -> {
                                     return getConceptNid(sequence);
                                  });
   }

   @Override
   public int getConceptSequence(int nid) {
      if (nid >= 0) {
         return nid;
      }

      return conceptSequenceMap.addNidIfMissing(nid);
   }

   @Override
   public int getConceptSequenceForProxy(ConceptSpecification conceptProxy) {
      return getConceptSequence(getNidForProxy(conceptProxy));
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
   public IntStream getConceptSequenceStream() {
      return conceptSequenceMap.getSequenceStream();
   }

   @Override
   public ConceptSequenceSet getConceptSequencesForConceptNids(int[] conceptNidArray) {
      ConceptSequenceSet sequences = new ConceptSequenceSet();

      IntStream.of(conceptNidArray)
               .forEach((nid) -> sequences.add(conceptSequenceMap.getSequenceFast(nid)));
      return sequences;
   }

   @Override
   public ConceptSequenceSet getConceptSequencesForConceptNids(NidSet conceptNidSet) {
      ConceptSequenceSet sequences = new ConceptSequenceSet();

      conceptNidSet.stream()
                   .forEach((nid) -> sequences.add(conceptSequenceMap.getSequenceFast(nid)));
      return sequences;
   }

   @Override
   public Path getDatabaseFolder() {
      return folderPath;
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return databaseValidity;
   }

   @Override
   public Optional<? extends ObjectChronology<? extends StampedVersion>> getIdentifiedObjectChronology(int nid) {
      switch (getChronologyTypeForNid(nid)) {
      case CONCEPT:
         return Get.conceptService()
                   .getOptionalConcept(nid);

      case SEMEME:
         return Get.sememeService()
                   .getOptionalSememe(nid);

      case UNKNOWN_NID:
         return Optional.empty();
      }

      throw new UnsupportedOperationException("Unknown chronology type: " + getChronologyTypeForNid(nid));
   }

   @Override
   public Optional<LatestVersion<String>> getIdentifierForAuthority(int nid,
         UUID identifierAuthorityUuid,
         StampCoordinate stampCoordinate) {
      if (nid >= 0) {
         throw new IllegalStateException("Not a nid: " + nid);
      }

      int authoritySequence = getConceptSequenceForUuids(identifierAuthorityUuid);
      SememeSnapshotService<StringSememe> snapshot = Get.sememeService()
                                                        .getSnapshot(StringSememe.class, stampCoordinate);

      return snapshot.getLatestSememeVersionsForComponentFromAssemblage(nid, authoritySequence)
                     .findAny()
                     .map((LatestVersion<StringSememe> latestSememe) -> {
                             LatestVersion<String> latestString = new LatestVersion<>(latestSememe.value().getString());

                             if (latestSememe.contradictions()
                                   .isPresent()) {
                                for (StringSememe version: latestSememe.contradictions()
                                      .get()) {
                                   latestString.addLatest(version.getString());
                                }
                             }

                             return latestString;
                          });
   }

   @Override
   public int getMaxNid() {
      return uuidIntMapMap.getNextNidProvider()
                          .get();
   }

   @Override
   public int getNidForProxy(ConceptSpecification conceptProxy) {
      return getNidForUuids(conceptProxy.getUuids());
   }

   @Override
   public int getNidForUuids(Collection<UUID> uuids) {
      return getNidForUuids(uuids.toArray(new UUID[uuids.size()]));
   }

   @Override
   public int getNidForUuids(UUID... uuids) {
      LinkedHashMap<UUID, Integer> cacheMap = THREAD_LOCAL_CACHE.get();
      Integer                      cacheNid = cacheMap.get(uuids[0]);

      if (cacheNid != null) {
         return cacheNid;
      }

      for (UUID uuid: uuids) {
//       if (watchSet.contains(uuid)) {
//          System.out.println("Found watch: " + Arrays.asList(uuids));
//          watchSet.remove(uuid);
//       }
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
   public IntStream getParallelConceptSequenceStream() {
      return conceptSequenceMap.getSequenceStream()
                               .parallel();
   }

   @Override
   public IntStream getParallelSememeSequenceStream() {
      return sememeSequenceMap.getSequenceStream()
                              .parallel();
   }

   /*
    * Investigate if "uuid-nid-map" directory is populated with at least one *.map file.
    */
   private boolean isPopulated() {
      File segmentDirectory     = new File(folderPath.toAbsolutePath().toFile(), "uuid-nid-map");
      int  numberOfSegmentFiles = segmentDirectory.list(new FilenameFilter() {
               @Override
               public boolean accept(File segmentDirectory, String name) {
                  return (name.endsWith("map"));
               }
            }).length;

      return numberOfSegmentFiles > 0;
   }

   @Override
   public int getSememeNid(int sememeId) {
      if (sememeId < 0) {
         return sememeId;
      }

      return sememeSequenceMap.getNidFast(sememeId);
   }

   @Override
   public IntStream getSememeNidsForSememeSequences(IntStream sememSequences) {
      return sememSequences.map((sequence) -> {
                                   return getSememeNid(sequence);
                                });
   }

   @Override
   public int getSememeSequence(int sememeId) {
      if (sememeId >= 0) {
         return sememeId;
      }

      return sememeSequenceMap.addNidIfMissing(sememeId);
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
   public IntStream getSememeSequenceStream() {
      return sememeSequenceMap.getSequenceStream();
   }

   @Override
   public SememeSequenceSet getSememeSequencesForSememeNids(int[] sememeNidArray) {
      SememeSequenceSet sequences = new SememeSequenceSet();

      IntStream.of(sememeNidArray)
               .forEach((nid) -> sequences.add(sememeSequenceMap.getSequenceFast(nid)));
      return sequences;
   }

   @Override
   public boolean hasUuid(Collection<UUID> uuids) {
      if (uuids == null) {
         throw new IllegalArgumentException("A UUID must be specified.");
      }

      final LinkedHashMap<UUID, Integer> cacheMap = THREAD_LOCAL_CACHE.get();

      // Check the cache to (hopefully) avoid a potential disk read
      boolean cacheHit = uuids.stream()
                              .anyMatch((uuid) -> (cacheMap.get(uuid) != null));

      if (cacheHit) {
         return true;
      }

      return uuids.stream()
                  .anyMatch((uuid) -> (uuidIntMapMap.containsKey(uuid)));
   }

   @Override
   public boolean hasUuid(UUID... uuids) {
      if (uuids == null) {
         throw new IllegalArgumentException("A UUID must be specified.");
      }

      return Arrays.stream(uuids)
                   .anyMatch((uuid) -> (uuidIntMapMap.containsKey(uuid)));
   }

   @Override
   public Optional<UUID> getUuidPrimordialForNid(int nid) {
      if (nid > 0) {
         throw new RuntimeException("Sequence passed to a function that expects a nid!");
      }

      // If we have a cache in uuidIntMapMap, read from there, it is faster.
      // If we don't have a cache, then uuidIntMapMap will be extremely slow, so try this first.
      if (!uuidIntMapMap.cacheContainsNid(nid)) {
         Optional<? extends ObjectChronology<? extends StampedVersion>> optionalObj = Get.identifiedObjectService()
                                                                                         .getIdentifiedObjectChronology(
                                                                                            nid);

         if (optionalObj.isPresent()) {
            return Optional.of(optionalObj.get()
                                          .getPrimordialUuid());
         }
      }

      UUID[] uuids = uuidIntMapMap.getKeysForValue(nid);

      // In the use case of directly writing files (converting terminology) this is a normal occurrence
      LOG.debug("[1] No object for nid: " + nid + " Found uuids: " + Arrays.asList(uuids));

      if (uuids.length > 0) {
         return Optional.of(uuids[0]);
      }

      return Optional.empty();
   }

   @Override
   public Optional<UUID> getUuidPrimordialFromConceptId(int conceptId) {
      return getUuidPrimordialForNid(getConceptNid(conceptId));
   }

   @Override
   public Optional<UUID> getUuidPrimordialFromSememeId(int sememeId) {
      return getUuidPrimordialForNid(getSememeNid(sememeId));
   }

   /**
    * @param nid
    * @return A list of uuids corresponding with a nid.
    */
   @Override
   public List<UUID> getUuidsForNid(int nid) {
      if (nid > 0) {
         throw new RuntimeException("Method expected nid!");
      }

      Optional<? extends ObjectChronology<? extends StampedVersion>> optionalObj = Get.identifiedObjectService()
                                                                                      .getIdentifiedObjectChronology(
                                                                                         nid);

      if (optionalObj.isPresent()) {
         return optionalObj.get()
                           .getUuidList();
      }

      UUID[] uuids = uuidIntMapMap.getKeysForValue(nid);

      LOG.warn("[3] No object for nid: " + nid + " Found uuids: " + Arrays.asList(uuids));
      return Arrays.asList(uuids);
   }
}

