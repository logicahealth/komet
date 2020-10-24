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
import java.util.concurrent.Future;
import java.util.stream.IntStream;
//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.UuidIntMapMapFileBased;
import sh.isaac.api.collections.uuidnidmap.DataStoreUuidToIntMap;
import sh.isaac.api.collections.uuidnidmap.UuidToIntMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.isaac.model.DataStoreSubService;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L2)
public class IdentifierProvider
        implements IdentifierService {

    private static final Logger LOG = LogManager.getLogger();

    private transient DataStore store;
    private UuidToIntMap uuidIntMapMap;

    private File uuidNidMapDirectory;

    private IdentifierProvider() {
        //Construct with HK2 only
    }

    @Override
    public void addUuidForNid(UUID uuid, int nid) {
        OptionalInt old = this.uuidIntMapMap.get(uuid);
        if (old.isPresent() && old.getAsInt() != nid) {
            throw new IllegalArgumentException("Reassignment of nid for " + uuid + " from " + old + " to " + nid);
        }
        this.uuidIntMapMap.put(uuid, nid);
    }

    /**
     * Start me.
     */
    @PostConstruct
    private void startMe() {
        LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting Identifier provider");
        Get.executor().execute(progressTask);
        try {
            LOG.info("Starting identifier provider for change to runlevel: {}", LookupService.getProceedingToRunLevel());
            this.store = Get.service(DataStore.class);
            uuidNidMapDirectory = new File(store.getDataStorePath().toAbsolutePath().toFile(), "uuid-nid-map");

            //Continue using local implementation when the FileSystem store is in use, even though the filesystem store
            //now supports extended APIs for performance reasons
            if (this.store.implementsExtendedStoreAPI() && Get.dataStore().getDataStoreType() != DatabaseImplementation.FILESYSTEM) {
                uuidIntMapMap = new DataStoreUuidToIntMap((ExtendedStore) this.store);
            } else {
                this.uuidIntMapMap = UuidIntMapMapFileBased.create(uuidNidMapDirectory);
            }

            //bootstrap our nids for core metadata concepts.
            for (ConceptSpecification cs : TermAux.getAllSpecs()) {
                assignNid(cs.getUuids());
            }
        } finally {
            progressTask.finished();
        }
    }

    /**
     * Stop me.
     */
    @PreDestroy
    private void stopMe() {
        try {
            LOG.info("Stopping identifier provider for change to runlevel: " + LookupService.getProceedingToRunLevel());
            this.sync().get();
            uuidIntMapMap.shutdown();
            uuidIntMapMap = null;
            store = null;
        } catch (Throwable ex) {
            LOG.error("Unexpected error while stopping identifier provider", ex);
            throw new RuntimeException(ex);
        }
        LOG.info("Stopped identifier provider. ");
    }

    @Override
    public int getMaxNid() {
        return uuidIntMapMap.getMaxNid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupNid(int nid, int assemblageNid, IsaacObjectType objectType, VersionType versionType) {
        if (versionType == VersionType.UNKNOWN) {
            throw new IllegalStateException("versionType may not be unknown. ");
        }
        this.store.setAssemblageForNid(nid, assemblageNid);

        IsaacObjectType oldObjectType = this.store.getIsaacObjectTypeForAssemblageNid(assemblageNid);
        if (oldObjectType == IsaacObjectType.UNKNOWN) {
            this.store.putAssemblageIsaacObjectType(assemblageNid, objectType);
        }

        VersionType oldVersionType = this.store.getVersionTypeForAssemblageNid(assemblageNid);
        if (oldVersionType == VersionType.UNKNOWN) {
            this.store.putAssemblageVersionType(assemblageNid, versionType);
        }
    }

    @Override
    public IsaacObjectType getObjectTypeForComponent(int componentNid) {
        OptionalInt optionalAssemblageNid = getAssemblageNid(componentNid);
        if (optionalAssemblageNid.isPresent()) {
            IsaacObjectType temp = this.store.getIsaacObjectTypeForAssemblageNid(optionalAssemblageNid.getAsInt());
            if (temp == IsaacObjectType.UNKNOWN) {
                Optional<? extends Chronology> temp2 = Get.identifiedObjectService().getChronology(componentNid);
                if (temp2.isPresent()) {
                    LOG.error("Object {} in store, but not in object type map?", componentNid);
                    return temp2.get().getIsaacObjectType();
                }
            }
            return temp;
        }
        return IsaacObjectType.UNKNOWN;
    }

    @Override
    public int getNidForUuids(Collection<UUID> uuids) throws NoSuchElementException {
        return getNidForUuids(uuids.toArray(new UUID[uuids.size()]));
    }

    @Override
    public int getNidForUuids(UUID... uuids) throws NoSuchElementException {

        for (final UUID uuid : uuids) {
            final OptionalInt nid = this.uuidIntMapMap.get(uuid);

            if (nid.isPresent()) {
                return nid.getAsInt();
            }
        }
        throw new NoSuchElementException("No nid found for " + Arrays.toString(uuids));
    }
    @Override
    public int assignNid(UUID... uuids) throws IllegalArgumentException {
        int lastFoundNid = Integer.MAX_VALUE;
        ArrayList<UUID> uuidsWithoutNid = new ArrayList<>(uuids.length);
        for (final UUID uuid : uuids) {
             final OptionalInt nid = this.uuidIntMapMap.get(uuid);

            if (nid.isPresent()) {
                if (lastFoundNid != Integer.MAX_VALUE && lastFoundNid != nid.getAsInt()) {
                    LOG.trace("Two UUIDs are being merged onto a single nid!  Found " + lastFoundNid + " and " + nid);
                    //I don't want to update lastFoundNid in this case, because the uuid -> nid mapping is for the previously checked UUID.
                    //This UUID will need to be remaped to a new nid:
                    uuidsWithoutNid.add(uuid);
                } else {
                    lastFoundNid = nid.getAsInt();
                }
            } else {
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
    public boolean hasUuid(Collection<UUID> uuids) throws IllegalArgumentException {
        if (uuids == null || uuids.isEmpty()) {
            throw new IllegalArgumentException("A UUID must be specified.");
        }
        for (UUID uuid : uuids) {
            if (this.uuidIntMapMap.containsKey(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasUuid(UUID... uuids) throws IllegalArgumentException {
        if (uuids == null || uuids.length == 0) {
            throw new IllegalArgumentException("A UUID must be specified.");
        }

        for (UUID uuid : uuids) {
            if (this.uuidIntMapMap.containsKey(uuid)) {
                return true;
            }
        }
        return false;
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
        final Optional<? extends Chronology> optionalObj
                = Get.identifiedObjectService().getChronology(nid);

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
        return this.store.getAssemblageOfNid(componentNid);
    }

    @Override
    public int[] getAssemblageNids() {
        return store.getAssemblageConceptNids();
    }

    @Override
    public IntStream getNidsForAssemblage(int assemblageNid, boolean parallel) {
        return store.getNidsForAssemblage(assemblageNid, parallel);
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
    public IntStream getNidStreamOfType(IsaacObjectType objectType, boolean parallel) {
        int maxNid = this.uuidIntMapMap.getMaxNid();
        NidSet allowedAssemblages = this.store.getAssemblageNidsForType(objectType);

        IntStream is = IntStream.rangeClosed(IdentifierProvider.FIRST_NID, maxNid)
                .filter((value) -> {
                    return allowedAssemblages.contains(this.store.getAssemblageOfNid(value).orElseGet(() -> Integer.MAX_VALUE));
                });
        if (parallel) {
            is = is.parallel();
        }
        return is;
    }

    @Override
    public IntStream getNidStream(boolean parallel) {
        int maxNid = this.uuidIntMapMap.getMaxNid();
        IntStream is = IntStream.rangeClosed(IdentifierService.FIRST_NID, maxNid)
                .filter((value) -> {
                    return this.store.getAssemblageOfNid(value).isPresent();
                });
        if (parallel) {
            is = is.parallel();
        }
        return is;
    }

    @Override
    public Future<?> sync() {
        return Get.executor().submit(() -> {
            try {
                LOG.info("writing uuid-nid-map.");
                if (this.uuidIntMapMap instanceof UuidIntMapMapFileBased) {
                    ((UuidIntMapMapFileBased) this.uuidIntMapMap).write();
                }
                //Don't sync the data store here, and we don't need to do anything if it is an extended store based storage
            } catch (IOException ex) {
                LOG.error("error syncing identifier provider", ex);
            }
        });
    }

    //TODO [refactor] need to see if I'm reporting this as part of the datastore
    @Override
    public long getMemoryInUse() {
        return uuidIntMapMap.getMemoryInUse();
    }

    @Override
    public long getSizeOnDisk() {
        return uuidIntMapMap.getDiskSpaceUsed();
    }

    @Override
    public void optimizeForOutOfOrderLoading() {
        uuidIntMapMap.enableInverseCache();
    }
}
