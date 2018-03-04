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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.UuidIntMapMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ContainerSequenceService;
import sh.isaac.model.DataStore;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.model.collections.SpinedNidIntMap;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L2)
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
   private transient DataStore                         store;
   private UuidIntMapMap                               uuidIntMapMap;
   
   private File uuidNidMapDirectory;

   private IdentifierProvider() {
      //Construct with HK2 only
   }
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
      LOG.info("Starting identifier provider for change to runlevel: {}", LookupService.getProceedingToRunLevel());
      this.store      = Get.service(DataStore.class);
      uuidNidMapDirectory = new File(store.getDataStorePath().toAbsolutePath().toFile(), "uuid-nid-map");

      this.uuidIntMapMap = UuidIntMapMap.create(uuidNidMapDirectory);
      
      //bootstrap our nids for core metadata concepts.  
      for (ConceptSpecification cs : TermAux.getAllSpecs()) {
         assignNid(cs.getUuids());
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      try {
         LOG.info("Stopping identifier provider for change to runlevel: " + LookupService.getProceedingToRunLevel());
         this.uuidIntMapMap.setShutdown(true);
         this.sync().get();
         this.store.sync().get();
         this.store = null;
         uuidIntMapMap = null;
      } catch (Throwable ex) {
         LOG.error("Unexpected error while stopping identifier provider", ex);
         throw new RuntimeException(ex);
      }
   }
   

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean setupNid(int nid, int assemblageNid, IsaacObjectType objectType, VersionType versionType) {
      if (versionType == VersionType.UNKNOWN) {
          throw new IllegalStateException("versionType may not be unknown. ");
      }
      int existingAssemblageNid = this.store.getNidToAssemblageNidMap().get(nid);
      if (existingAssemblageNid == Integer.MAX_VALUE) {
          this.store.getNidToAssemblageNidMap().put(nid, assemblageNid);
      }
      else if (existingAssemblageNid != assemblageNid) {
         throw new IllegalArgumentException("The nid " + nid + " is already assigned to assemblage " 
               + existingAssemblageNid + " and cannot be reassigned to " + assemblageNid);
      }
       
      IsaacObjectType oldObjectType = this.store.getAssemblageObjectTypeMap().computeIfAbsent(assemblageNid, (Integer t) -> objectType);
      if (oldObjectType != null && oldObjectType != objectType) {
         throw new IllegalStateException("Object types don't match: " +
                this.store.getAssemblageObjectTypeMap().get(assemblageNid) + " " +
                objectType
        );
      }

      VersionType oldVersionType = this.store.getAssemblageVersionTypeMap().computeIfAbsent(assemblageNid, (Integer t) -> versionType);
      if (oldVersionType != null && oldVersionType != versionType) {
         throw new IllegalStateException("Version types don't match: " +
               this.store.getAssemblageVersionTypeMap().get(assemblageNid) + " " + versionType);
      }
      
      return ((oldObjectType == null && oldVersionType == null));
   }
   
  private IsaacObjectType getObjectTypeForAssemblage(int assemblageNid) {
      return this.store.getAssemblageObjectTypeMap().getOrDefault(assemblageNid, IsaacObjectType.UNKNOWN);
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------
   @Override
   public IsaacObjectType getObjectTypeForComponent(int componentNid) {
      IsaacObjectType temp = getObjectTypeForAssemblage(getAssemblageNid(componentNid).getAsInt());
      if (temp == IsaacObjectType.UNKNOWN) {
         Optional<? extends Chronology> temp2 = Get.identifiedObjectService().getChronology(componentNid);
         if (temp2.isPresent()) {
            LOG.error("Object {} in store, but not in object type map?", componentNid);
            return temp2.get().getIsaacObjectType();
         }
      }
      return temp;
   }

   @Override
   public int getElementSequenceForNid(int nid) {
      int elementSequence = this.store.getNidToElementSequenceMap().get(nid);
      if (elementSequence != Integer.MAX_VALUE) {
         return elementSequence;
      }
      return getElementSequenceForNid(nid, getAssemblageNid(nid).getAsInt());
   }

   @Override
   public int getNidForElementSequence(int elementSequence, int assemblageNid) {
      return getElementSequenceToNidMap(assemblageNid).get(elementSequence);
   }

   @Override
   public int getNidForUuids(Collection<UUID> uuids) throws NoSuchElementException {
     return getNidForUuids(uuids.toArray(new UUID[uuids.size()]));
   }

   @Override
   public int[] getSemanticNidsForComponent(int componentNid) {
      return this.store.getComponentToSemanticNidsMap().get(componentNid);
   }

   @Override
   public int getNidForUuids(UUID... uuids) throws NoSuchElementException {

      for (final UUID uuid: uuids) {
         final int nid = this.uuidIntMapMap.get(uuid);

         if (nid != Integer.MAX_VALUE) {
            return nid;
         }
      }
      throw new NoSuchElementException("No nid found for " + Arrays.toString(uuids));
   }

   @Override
   public int assignNid(UUID... uuids) throws IllegalArgumentException {
      int lastFoundNid = Integer.MAX_VALUE;
      ArrayList<UUID> uuidsWithoutNid = new ArrayList<>(uuids.length);
      for (final UUID uuid: uuids) {
         final int nid =  this.uuidIntMapMap.get(uuid);

         if (nid != Integer.MAX_VALUE) {
            if (lastFoundNid != Integer.MAX_VALUE && lastFoundNid != nid) {
               LOG.trace("Two UUIDs are being merged onto a single nid!  Found " + lastFoundNid + " and " + nid);
               //I don't want to update lastFoundNid in this case, because the uuid -> nid mapping is for the previously checked UUID.
               //This UUID will need to be remaped to a new nid:
               uuidsWithoutNid.add(uuid);
            }
            else {
               lastFoundNid = nid;
            }
         }
         else {
            uuidsWithoutNid.add(uuid);
         }
      }
      
      if (lastFoundNid != Integer.MAX_VALUE) {
         for (UUID uuid : uuidsWithoutNid) {
            addUuidForNid(uuid, lastFoundNid);
         }
         return lastFoundNid;
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

      int assemblageForNid = this.store.getNidToAssemblageNidMap().get(nid);
      if (assemblageForNid == Integer.MAX_VALUE) {
         this.store.getNidToAssemblageNidMap().put(nid, assemblageNid);
      } else if (assemblageNid != assemblageForNid) {
          throw new IllegalStateException("Assemblage nids do not match: \n" +
                  Get.conceptDescriptionText(assemblageNid) + " and\n" +
                  Get.conceptDescriptionText(assemblageForNid));
      }

      AtomicInteger sequenceGenerator = this.store.getSequenceGeneratorMap().computeIfAbsent(
                                            assemblageNid,
                                                  (key) -> new AtomicInteger(1));
      int elementSequence = this.store.getNidToElementSequenceMap().getAndUpdate(
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
      return this.store.getNidToElementSequenceMap();
   }

   public SpinedIntIntMap getElementSequenceToNidMap(int assemblageNid) {
      return store.getAssemblageNid_ElementSequenceToNid_Map(assemblageNid);
   }

	@Override
   public boolean hasUuid(Collection<UUID> uuids) throws IllegalArgumentException {
      if (uuids == null || uuids.size() == 0) {
         throw new IllegalArgumentException("A UUID must be specified.");
      }
      return uuids.stream()
                  .anyMatch((uuid) -> (this.uuidIntMapMap.containsKey(uuid)));
   }

   @Override
   public boolean hasUuid(UUID... uuids) throws IllegalArgumentException{
     if (uuids == null || uuids.length == 0) {
         throw new IllegalArgumentException("A UUID must be specified.");
      }

      return Arrays.stream(uuids)
                   .anyMatch((uuid) -> (this.uuidIntMapMap.containsKey(uuid)));
   }

   @Override
   public UUID getUuidPrimordialForNid(int nid) throws NoSuchElementException {
      return getUuidsForNid(nid).get(0);
   }

   @Override
   public List<UUID> getUuidsForNid(int nid) throws NoSuchElementException {
      //This call is only faster if the cache has it, so test before doing the call.
      if (this.uuidIntMapMap.cacheContainsNid(nid)) {
         return Arrays.asList(this.uuidIntMapMap.getKeysForValue(nid));
      }

      //If the LRU cache doesn't have it, see if the identified object service knows about it (as that is a hashed lookup)
      final Optional<? extends Chronology> optionalObj = 
           Get.identifiedObjectService().getChronology(nid);

      if (optionalObj.isPresent()) {
         return optionalObj.get().getUuidList();
      }
      
      //Not in the datastore... do the scan lookup.
      final UUID[] uuids = this.uuidIntMapMap.getKeysForValue(nid);
      if (uuids.length > 0) {
         return Arrays.asList(uuids);
      }

      throw new NoSuchElementException("The nid " + nid + " is not assigned");   
   }

   @Override
   public OptionalInt getAssemblageNid(int componentNid) {
      int value = this.store.getNidToAssemblageNidMap().get(componentNid);
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
   public Optional<UUID> getDataStoreId() {
      return store.getDataStoreId();
   }

   @Override
   public Path getDataStorePath() {
      return store.getDataStorePath();
   }

   @Override
   public DataStoreStartState getDataStoreStartState() {
      return store.getDataStoreStartState();
   }

   @Override
   public IntStream getNidStreamOfType(IsaacObjectType objectType) {
      int maxNid = this.uuidIntMapMap.getMaxNid();
      NidSet allowedAssemblages = new NidSet();
      this.store.getAssemblageObjectTypeMap().forEach((nid, type) -> {
         if (type == objectType) {
            allowedAssemblages.add(nid);
         }
      });

      return IntStream.rangeClosed(Integer.MIN_VALUE + 1, maxNid)
              .filter((value) -> {
                 return allowedAssemblages.contains(this.store.getNidToAssemblageNidMap().get(value)); 
              });
   }

   @Override
   public int getMaxSequenceForAssemblage(int assemblageNid) {
      return store.getSequenceGeneratorMap().get(assemblageNid).get() - 1;
   }   

   @Override
   public Future<?> sync() {
      return Get.executor().submit(() -> {
         try {
            LOG.info("writing uuid-nid-map.");
            this.uuidIntMapMap.write();
            this.store.sync().get();
         } catch (IOException | InterruptedException | ExecutionException ex) {
            LOG.error("error syncing identifier provider", ex);
         }
      });
   }

    @Override
    public long getMemoryInUse() {
        long sizeInBytes = this.store.getNidToAssemblageNidMap().sizeInBytes();
        sizeInBytes += this.store.getNidToElementSequenceMap().sizeInBytes();
        sizeInBytes += uuidIntMapMap.getMemoryInUse();
        return sizeInBytes;
    }

    @Override
    public long getSizeOnDisk() {
        long sizeInBytes = this.store.getNidToAssemblageNidMap().sizeInBytes();
        sizeInBytes += this.store.getNidToElementSequenceMap().sizeInBytes();
        sizeInBytes += uuidIntMapMap.getDiskSpaceUsed();
        return sizeInBytes;
    }
}

