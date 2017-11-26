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



package sh.isaac.provider.bdb.identifier;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.api.collections.UuidIntMapMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ContainerSequenceService;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.provider.bdb.chronology.BdbProvider;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class BdbIdentifierProvider
         implements ContainerSequenceService {
   private static final Logger LOG = LogManager.getLogger();

   public static final String NID_ASSEMBLAGENID_MAP_KEY = "nid_AssemblageNid_Map";
   public static final String NID_ELEMENT_SEQUENCE_MAP_KEY = "nid_ElementSequence_Map";
   public static final String ASSEMBLAGENID_SEQUENCE_MAP_PREFIX = "assemblageNid_ElementSequence";
   //~--- fields --------------------------------------------------------------
/*
   nid -> assemblage nid
   nid -> entry sequence
   nid -> uuid[] (store as single byte array?)
   entry sequence + assemblage nid -> nid
   uuid -> nid with generation...
   */
   private final ConcurrentHashMap<Integer, SpinedIntIntMap> assemblageNid_ElementSequenceToNid_Map =
      new ConcurrentHashMap<>();
   private SpinedNidIntMap                       nid_AssemblageNid_Map;
   private SpinedNidIntMap                       nid_ElementSequence_Map;
   private ConcurrentMap<Integer, AtomicInteger> assemblageNid_SequenceGenerator_Map;
   private BdbProvider                           bdb;
   private UuidIntMapMap uuidIntMapMap;
   private ConcurrentHashMap<Integer, IsaacObjectType> assemblageNid_ObjectType_Map =
      new ConcurrentHashMap<>();

   //~--- methods -------------------------------------------------------------

   @Override
   public void addUuidForNid(UUID uuid, int nid) {
      this.uuidIntMapMap.put(uuid, nid);
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Starting bdb identifier provider.");
      this.bdb      = Get.service(BdbProvider.class);
      this.uuidIntMapMap = UuidIntMapMap.create(new File(bdb.getDatabaseFolder().getParent().toAbsolutePath().toFile(), "uuid-nid-map"));
      this.nid_AssemblageNid_Map = this.bdb.getSpinedNidIntMap(NID_ASSEMBLAGENID_MAP_KEY);
      this.nid_ElementSequence_Map = this.bdb.getSpinedNidIntMap(NID_ELEMENT_SEQUENCE_MAP_KEY);
      for (int assemblageNid: this.bdb.getAssemblageNids()) {
         assemblageNid_ElementSequenceToNid_Map.put(assemblageNid, bdb.getSpinedIntIntMap(ASSEMBLAGENID_SEQUENCE_MAP_PREFIX + assemblageNid));
      }
      
      this.assemblageNid_SequenceGenerator_Map = bdb.getSequenceGeneratorMap();

      this.assemblageNid_ObjectType_Map = bdb.getAssemblageTypeMap();
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      try {
         LOG.info("Stopping bdb identifier provider.");
         this.uuidIntMapMap.setShutdown(true);
         LOG.info("writing uuid-nid-map.");
         this.uuidIntMapMap.write();
         
         this.bdb.putSpinedNidIntMap(NID_ASSEMBLAGENID_MAP_KEY, nid_AssemblageNid_Map);
         this.bdb.putSpinedNidIntMap(NID_ELEMENT_SEQUENCE_MAP_KEY, nid_ElementSequence_Map);
         
         assemblageNid_ElementSequenceToNid_Map.forEach((assemblageNid, map) -> {
            this.bdb.putSpinedIntIntMap(ASSEMBLAGENID_SEQUENCE_MAP_PREFIX + assemblageNid, map);
         });
         
         this.bdb.putSequenceGeneratorMap(assemblageNid_SequenceGenerator_Map);
         
         this.bdb.putAssemblageTypeMap(assemblageNid_ObjectType_Map);
         
      } catch (Throwable ex) {
         LOG.error(ex);
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
   }
   

   @Override
   public void setupNid(int nid, int assemblageNid, IsaacObjectType objectType) {
      assemblageNid_ObjectType_Map.computeIfAbsent(assemblageNid, (Integer t) -> objectType);
      nid_AssemblageNid_Map.put(nid, assemblageNid);
   }

   @Override
   public IsaacObjectType getObjectTypeForAssemblage(int assemblageNid) {
      return assemblageNid_ObjectType_Map.getOrDefault(assemblageNid, IsaacObjectType.UNKNOWN);
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------
   @Override
   public IsaacObjectType getObjectTypeForComponent(int componentNid) {
      return getObjectTypeForAssemblage(getAssemblageNidForNid(componentNid));
   }

   @Override
   public int getAssemblageNidForNid(int nid) {
      if (nid >= 0) {
         throw new IllegalStateException("Nids must be negative. Found: " + nid);
      }
      return nid_AssemblageNid_Map.get(nid);
   }

   @Override
   public int getElementSequenceForNid(int nid) {
      int elementSequence = nid_ElementSequence_Map.get(nid);
      if (elementSequence != Integer.MAX_VALUE) {
         return elementSequence;
      }
      return getElementSequenceForNid(nid, getAssemblageNidForNid(nid));
   }

   @Override
   public ObjectChronologyType getOldChronologyTypeForNid(int nid) {
      Optional<? extends Chronology> optionalChronology = Get.identifiedObjectService().getIdentifiedObjectChronology(nid);  
      if (optionalChronology.isPresent()) {
         Chronology chronology = optionalChronology.get();
         if (chronology.getIsaacObjectType() == IsaacObjectType.CONCEPT) {
            return ObjectChronologyType.CONCEPT;
         }
         return ObjectChronologyType.SEMANTIC;
      }
      return ObjectChronologyType.UNKNOWN_NID;
   }

   @Override
   public Optional<String> getIdentifierForAuthority(int nid,
         UUID identifierAuthorityUuid,
         StampCoordinate stampCoordinate) {
      if (nid >= 0) {
         throw new IllegalStateException("Not a nid: " + nid);
      }

      final int authorityAssemblageNid = getNidForUuids(identifierAuthorityUuid);
      final SemanticSnapshotService<StringVersion> snapshot = Get.assemblageService()
                                                              .getSnapshot(StringVersion.class, stampCoordinate);
      
      for (LatestVersion<StringVersion> stringVersion: snapshot.getLatestSemanticVersionsForComponentFromAssemblage(nid, authorityAssemblageNid)) {
         if (stringVersion.isPresent()) {
            return Optional.of(stringVersion.get().getString());
         }
      }

      return Optional.empty();
   }

   @Override
   public int getNidForProxy(ConceptSpecification conceptProxy) {
      return getNidForUuids(conceptProxy.getUuidList());
   }

   @Override
   public int getNidForElementSequence(int elementSequence, int assemblageNid) {
      return getElementSequenceToNidMap(assemblageNid).get(elementSequence);
   }

   @Override
   public int getNidForUuids(Collection<UUID> uuids) {
     return getNidForUuids(uuids.toArray(new UUID[uuids.size()]));
   }

   @Override
   public void addToSemanticIndex(int nid, int referencingSemanticNid) {
      this.bdb.getComponentToSemanticNidsMap().add(nid, referencingSemanticNid);
   }

   @Override
   public int[] getSemanticNidsForComponent(int componentNid) {
      return this.bdb.getComponentToSemanticNidsMap().get(componentNid);
   }

   @Override
   public int getNidForUuids(UUID... uuids) {

      for (final UUID uuid: uuids) {
         final int nid = this.uuidIntMapMap.get(uuid);

         if (nid != Integer.MAX_VALUE) {
            return nid;
         }
      }

      final int nid = this.uuidIntMapMap.getWithGeneration(uuids[0]);

      for (int i = 1; i < uuids.length; i++) {
         this.uuidIntMapMap.put(uuids[i], nid);
      }

      return nid;
   }

   @Override
   public int getElementSequenceForNid(int nid, int assemblageNid) {
      if (nid >= 0) {
         throw new IllegalStateException("Nids must be negative. Found: " + nid);
      }
      if (assemblageNid >= 0) {
         throw new IllegalStateException("assemblageNid must be negative. Found: " + assemblageNid);
      }

      nid_AssemblageNid_Map.put(nid, assemblageNid);

      AtomicInteger sequenceGenerator = assemblageNid_SequenceGenerator_Map.computeIfAbsent(
                                            assemblageNid,
                                                  (key) -> new AtomicInteger(1));
      int elementSequence = nid_ElementSequence_Map.getAndUpdate(
          nid,
              (currentValue) -> {
                 if (currentValue == Integer.MAX_VALUE) {
                    return sequenceGenerator.getAndIncrement();
                 }
                 return currentValue;
              });
      
      SpinedIntIntMap elementSequenceToNidMap = getElementSequenceToNidMap(assemblageNid);
      elementSequenceToNidMap.put(elementSequence, nid);
      return elementSequence;
   }

   public SpinedNidIntMap getNid_ElementSequence_Map() {
      return nid_ElementSequence_Map;
   }

   public SpinedIntIntMap getElementSequenceToNidMap(int assemblageNid) {
      if (assemblageNid >= 0) {
         throw new IllegalStateException("assemblageNid must be negative. Found: " + assemblageNid);
      }

      return this.assemblageNid_ElementSequenceToNid_Map.computeIfAbsent(assemblageNid, (key) -> new SpinedIntIntMap());
   }

   @Override
   public boolean hasUuid(Collection<UUID> uuids) {
      if (uuids == null) {
         throw new IllegalArgumentException("A UUID must be specified.");
      }
      return uuids.stream()
                  .anyMatch((uuid) -> (this.uuidIntMapMap.containsKey(uuid)));
   }

   @Override
   public boolean hasUuid(UUID... uuids) {
     if (uuids == null) {
         throw new IllegalArgumentException("A UUID must be specified.");
      }

      return Arrays.stream(uuids)
                   .anyMatch((uuid) -> (this.uuidIntMapMap.containsKey(uuid)));
   }

   @Override
   public Optional<UUID> getUuidPrimordialForNid(int nid) {
      if (nid > 0) {
         throw new RuntimeException("Sequence passed to a function that expects a nid!");
      }

      // If we have a cache in uuidIntMapMap, read from there, it is faster.
      // If we don't have a cache, then uuidIntMapMap will be extremely slow, so try this first.
      if (!this.uuidIntMapMap.cacheContainsNid(nid)) {
         final Optional<? extends Chronology> optionalObj =
            Get.identifiedObjectService()
               .getIdentifiedObjectChronology(
                   nid);

         if (optionalObj.isPresent()) {
            return Optional.of(optionalObj.get()
                                          .getPrimordialUuid());
         }
      }

      final UUID[] uuids = this.uuidIntMapMap.getKeysForValue(nid);

      // In the use case of directly writing files (converting terminology) this is a normal occurrence
      // LOG.debug("[1] No object for nid: " + nid + " Found uuids: " + Arrays.asList(uuids));

      if (uuids.length > 0) {
         return Optional.of(uuids[0]);
      }

      return Optional.empty();   }

   @Override
   public List<UUID> getUuidsForNid(int nid) {
     if (nid > 0) {
         throw new RuntimeException("Method expected nid!");
      }

      final Optional<? extends Chronology> optionalObj = Get.identifiedObjectService()
                                                                                            .getIdentifiedObjectChronology(
                                                                                                  nid);

      if (optionalObj.isPresent()) {
         return optionalObj.get()
                           .getUuidList();
      }

      final UUID[] uuids = this.uuidIntMapMap.getKeysForValue(nid);

      LOG.warn("[3] No object for nid: " + nid + " Found uuids: " + Arrays.asList(uuids));
      return Arrays.asList(uuids);
   }

   @Override
   public OptionalInt getAssemblageNid(int componentNid) {
      int value = nid_AssemblageNid_Map.get(componentNid);
      if (value != Integer.MAX_VALUE) {
         return OptionalInt.of(value);
      }
      return OptionalInt.empty();
   }

   @Override
   public int[] getAssemblageNids() {
      return NidSet.of(bdb.getAssemblageNids()).asArray();
   }

   @Override
   public IntStream getNidsForAssemblage(int assemblageNid) {
      return getElementSequenceToNidMap(assemblageNid).valueStream();
   }

   @Override
   public UUID getDataStoreId() {
      return bdb.getDataStoreId();
   }

   @Override
   public void clearDatabaseValidityValue() {
      bdb.clearDatabaseValidityValue();
   }

   @Override
   public Path getDatabaseFolder() {
      return bdb.getDatabaseFolder();
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return bdb.getDatabaseValidityStatus();
   }

   @Override
   public IntStream getNidStreamOfType(IsaacObjectType objectType) {
      int maxNid = UuidIntMapMap.getNextNidProvider().get();
      NidSet allowedAssemblages = new NidSet();
      assemblageNid_ObjectType_Map.forEach((nid, type) -> {
         if (type == objectType) {
            allowedAssemblages.add(nid);
         }
      });

      return IntStream.rangeClosed(Integer.MIN_VALUE + 1, maxNid)
              .filter((value) -> {
                 return allowedAssemblages.contains(nid_AssemblageNid_Map.get(value)); 
              });
   }

   @Override
   public int getMaxSequenceForAssemblage(int assemblageNid) {
      return assemblageNid_SequenceGenerator_Map.get(assemblageNid).get() - 1;
   }   

   @Override
   public Future<?> sync() {
     return this.bdb.sync();
   }
}

