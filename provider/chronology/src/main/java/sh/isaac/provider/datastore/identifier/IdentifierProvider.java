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



package sh.isaac.provider.datastore.identifier;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.api.collections.UuidIntMapMap;
import sh.isaac.api.collections.uuidnidmap.ConcurrentUuidToIntHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.util.UUIDUtil;
import sh.isaac.model.ContainerSequenceService;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.model.DataStore;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class IdentifierProvider
         implements IdentifierService, ContainerSequenceService {
   private static final Logger LOG = LogManager.getLogger();
   //~--- fields --------------------------------------------------------------
/*
   nid -> assemblage nid
   nid -> entry sequence
   nid -> uuid[] (store as single byte array?)
   entry sequence + assemblage nid -> nid
   uuid -> nid with generation...
   */
   private SpinedNidIntMap                       nid_AssemblageNid_Map;
   private SpinedNidIntMap                       nid_ElementSequence_Map;
   private ConcurrentMap<Integer, AtomicInteger> assemblageNid_SequenceGenerator_Map;
   private DataStore                           store;
   private UuidIntMapMap uuidIntMapMap;
   private ConcurrentHashMap<Integer, IsaacObjectType> assemblageNid_ObjectType_Map =
      new ConcurrentHashMap<>();
   private ConcurrentHashMap<Integer, VersionType> assemblageNid_VersionType_Map =
      new ConcurrentHashMap<>();
   
   ConcurrentUuidToIntHashMap proxyUuidNidMapCache;
   
   private File uuidNidMapProxyCacheFile;
   private File uuidNidMapDirectory;

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
      LOG.info("Starting identifier provider at runlevel: " + LookupService.getCurrentRunLevel());
      this.store      = Get.service(DataStore.class);
      uuidNidMapDirectory = new File(store.getDatabaseFolder().toAbsolutePath().toFile(), "uuid-nid-map");
      uuidNidMapProxyCacheFile = new File(store.getDatabaseFolder().toAbsolutePath().toFile(), "uuid-nid-map-proxy-cache");

      this.uuidIntMapMap = UuidIntMapMap.create(uuidNidMapDirectory);
      
      if (uuidNidMapProxyCacheFile.exists() && uuidNidMapProxyCacheFile.length() > 0) {
          try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(uuidNidMapProxyCacheFile)))) {
              this.proxyUuidNidMapCache = ConcurrentUuidToIntHashMap.deserialize(in);
          } catch (IOException ex) {
             LOG.error(ex);
             throw new RuntimeException(ex);
          }
          
      } else {
          this.proxyUuidNidMapCache = new ConcurrentUuidToIntHashMap();
      }
      
      
      this.nid_AssemblageNid_Map = this.store.getNidToAssemblageNidMap();
      this.nid_ElementSequence_Map = this.store.getNidToElementSequenceMap();
       
      this.assemblageNid_SequenceGenerator_Map = store.getSequenceGeneratorMap();
      this.assemblageNid_ObjectType_Map = store.getAssemblageObjectTypeMap();
      this.assemblageNid_VersionType_Map = store.getAssemblageVersionTypeMap();
 
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      try {
         LOG.info("Stopping identifier provider at runlevel: " + LookupService.getCurrentRunLevel());
         this.uuidIntMapMap.setShutdown(true);
         this.sync().get();
      } catch (Throwable ex) {
         LOG.error(ex);
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
   }
   

   @Override
   public void setupNid(int nid, int assemblageNid, 
           IsaacObjectType objectType, VersionType versionType) {
      if (versionType == VersionType.UNKNOWN) {
          throw new IllegalStateException("versionType may not be unknown. ");
      }
      this.assemblageNid_ObjectType_Map.computeIfAbsent(assemblageNid, (Integer t) -> objectType);
      this.nid_AssemblageNid_Map.put(nid, assemblageNid);
      this.assemblageNid_VersionType_Map.computeIfAbsent(assemblageNid, (Integer t) -> versionType);
      
      if (this.assemblageNid_ObjectType_Map.get(assemblageNid) != objectType) {
          throw new IllegalStateException("Object types don't match: " +
                  this.assemblageNid_ObjectType_Map.get(assemblageNid) + " " +
                  objectType
          );
      }
      if (this.assemblageNid_VersionType_Map.get(assemblageNid) != versionType) {
          throw new IllegalStateException("Version types don't match: " +
                  this.assemblageNid_VersionType_Map.get(assemblageNid) + " " +
                  versionType
          );
      }
   }
  private IsaacObjectType getObjectTypeForAssemblage(int assemblageNid) {
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
   public int getCachedNidForProxy(ConceptSpecification conceptProxy) {
       for (UUID uuid: conceptProxy.getUuids()) {
        if (this.proxyUuidNidMapCache.containsKey(uuid)) {
           return this.proxyUuidNidMapCache.get(uuid);
        }
       }
       int nid = getNidForUuids(conceptProxy.getUuidList());
       for (UUID uuid: conceptProxy.getUuids()) {
           final long stamp = proxyUuidNidMapCache.getStampedLock()
                .writeLock();
           try {
                this.proxyUuidNidMapCache.put(UUIDUtil.convert(uuid), nid, stamp);
           } finally {
               proxyUuidNidMapCache.getStampedLock()
                    .unlockWrite(stamp);
           }
       }
      return nid;
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
      this.store.getComponentToSemanticNidsMap().add(nid, referencingSemanticNid);
   }

   @Override
   public int[] getSemanticNidsForComponent(int componentNid) {
      return this.store.getComponentToSemanticNidsMap().get(componentNid);
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
      if (uuids[0].equals(UUID.fromString("624b125c-220e-3a0d-8285-10a4eeec019f"))) {
          LOG.info("Found uuid watch: " + uuids[0]);
      }

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

      int assemblageForNid = nid_AssemblageNid_Map.get(nid);
      if (assemblageForNid == Integer.MAX_VALUE) {
          nid_AssemblageNid_Map.put(nid, assemblageNid);
      } else if (assemblageNid != assemblageForNid) {
          throw new IllegalStateException("Assemblage nids do not match: \n" +
                  Get.conceptDescriptionText(assemblageNid) + " and\n" +
                  Get.conceptDescriptionText(assemblageForNid));
      }
      
      

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
      return store.getAssemblageNid_ElementSequenceToNid_Map(assemblageNid);
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
         OptionalInt optionalAssemblageNid = getAssemblageNid(nid);
         if (optionalAssemblageNid.isPresent()) {
            final Optional<? extends Chronology> optionalObj =
               Get.identifiedObjectService()
                  .getIdentifiedObjectChronology(
                   nid);

            if (optionalObj.isPresent()) {
               return Optional.of(optionalObj.get()
                                          .getPrimordialUuid());
            }
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
      OptionalInt optionalAssemblageNid = getAssemblageNid(nid);
      if (optionalAssemblageNid.isPresent()) {
         final Optional<? extends Chronology> optionalObj = 
              Get.identifiedObjectService().getIdentifiedObjectChronology(nid);

         if (optionalObj.isPresent()) {
            return optionalObj.get()
                           .getUuidList();
         }
      }

      final UUID[] uuids = this.uuidIntMapMap.getKeysForValue(nid);

      LOG.warn("[3] No object for nid: " + nid + ". No assemblage found for nid. Found uuids: " + Arrays.asList(uuids));
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
      return store.getAssemblageConceptNids();
   }

   @Override
   public IntStream getNidsForAssemblage(int assemblageNid) {
      return getElementSequenceToNidMap(assemblageNid).valueStream();
   }

   @Override
   public UUID getDataStoreId() {
      return store.getDataStoreId();
   }

   @Override
   public Path getDatabaseFolder() {
      return store.getDatabaseFolder();
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return store.getDatabaseValidityStatus();
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
       return Get.executor().submit(() -> {
         try {
             LOG.info("writing uuid-nid-map.");
             this.uuidIntMapMap.write();
             try (DataOutputStream out =
                     new DataOutputStream(new BufferedOutputStream(new FileOutputStream(uuidNidMapProxyCacheFile)))) {
                 this.proxyUuidNidMapCache.serialize(out);
                 this.store.sync().get();
             }
         }   catch (IOException | InterruptedException | ExecutionException ex) {
               LOG.error(ex);
           }
       });
   }

    @Override
    public long getMemoryInUse() {
        long sizeInBytes = nid_AssemblageNid_Map.sizeInBytes();
        sizeInBytes += nid_ElementSequence_Map.sizeInBytes();
        sizeInBytes += uuidIntMapMap.getMemoryInUse();
        return sizeInBytes;
    }

    @Override
    public long getSizeOnDisk() {
        long sizeInBytes = nid_AssemblageNid_Map.sizeInBytes();
        sizeInBytes += nid_ElementSequence_Map.sizeInBytes();
        sizeInBytes += uuidIntMapMap.getDiskSpaceUsed();
        return sizeInBytes;
    }
   
   
}

