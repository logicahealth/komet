/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.provider.postgres.provider;

import sh.isaac.api.IdentifierService;
import sh.isaac.model.DataStoreSubService;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.provider.datastore.cache.CacheBootstrap;
import sh.isaac.provider.datastore.cache.DatastoreAndIdentiferService;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.datastore.ChronologySerializeable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.semantic.version.SemanticVersionImpl;
import sh.isaac.provider.datastore.cache.CacheProvider;
import sh.isaac.provider.postgres.PostgresProvider;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L1)
@Rank(value = 500)
public class PostgresDatastore implements DatastoreAndIdentiferService, CacheBootstrap {

    DatastoreAndIdentiferService backingStore;
    PostgresProvider postgresProvider;

    public PostgresDatastore() {
    }

    @Override
    @PreDestroy
    public void shutdown() {
        this.backingStore.shutdown(); 
    }

    @Override
    @PostConstruct
    public void startup() {
        this.postgresProvider = new PostgresProvider();
        this.backingStore = new CacheProvider(this.postgresProvider, this.postgresProvider);
        this.postgresProvider.startup();
        this.backingStore.startup();
    }

    @Override
    public int getMaxNid() {
        return backingStore.getMaxNid();
    }

    @Override
    public void optimizeForOutOfOrderLoading() {
        this.backingStore.optimizeForOutOfOrderLoading(); 
    }

    @Override
    public void setupNid(int nid, int assemblageNid, IsaacObjectType objectType, VersionType versionType) throws IllegalStateException {
        this.backingStore.setupNid(nid, assemblageNid, objectType, versionType); 
    }

    @Override
    public long getSizeOnDisk() {
        return this.backingStore.getSizeOnDisk(); 
    }

    @Override
    public long getMemoryInUse() {
        return this.backingStore.getMemoryInUse(); 
    }

    @Override
    public List<UUID> getUuidsForNid(int nid) throws NoSuchElementException {
        return this.backingStore.getUuidsForNid(nid); 
    }

    @Override
    public UUID getUuidPrimordialForNid(int nid) throws NoSuchElementException {
        return this.backingStore.getUuidPrimordialForNid(nid); 
    }

    @Override
    public boolean hasUuid(UUID... uuids) throws IllegalArgumentException {
        return this.backingStore.hasUuid(uuids); 
    }

    @Override
    public boolean hasUuid(Collection<UUID> uuids) throws IllegalArgumentException {
        return this.backingStore.hasUuid(uuids); 
    }

    @Override
    public int getNidForUuids(UUID... uuids) throws NoSuchElementException {
        return this.backingStore.getNidForUuids(uuids); 
    }

    @Override
    public int getNidForUuids(Collection<UUID> uuids) throws NoSuchElementException {
        return this.backingStore.getNidForUuids(uuids); 
    }

    @Override
    public IsaacObjectType getObjectTypeForComponent(int componentNid) {
        return this.backingStore.getObjectTypeForComponent(componentNid); 
    }

    @Override
    public IntStream getNidStreamOfType(IsaacObjectType objectType) {
        return this.backingStore.getNidStreamOfType(objectType); 
    }

    @Override
    public int assignNid(UUID... uuids) throws IllegalArgumentException {
        return this.backingStore.assignNid(uuids); 
    }

    @Override
    public int[] getAssemblageNids() {
        return this.backingStore.getAssemblageNids(); 
    }

    @Override
    public OptionalInt getAssemblageNid(int componentNid) {
        return this.backingStore.getAssemblageNid(componentNid); 
    }

    @Override
    public void addUuidForNid(UUID uuid, int nid) {
        this.backingStore.addUuidForNid(uuid, nid); 
    }

    @Override
    public Future<?> sync() {
        return this.backingStore.sync(); 
    }

    @Override
    public Optional<UUID> getDataStoreId() {
        return this.backingStore.getDataStoreId(); 
    }

    @Override
    public DataStoreStartState getDataStoreStartState() {
        return this.backingStore.getDataStoreStartState(); 
    }

    @Override
    public Path getDataStorePath() {
        return this.backingStore.getDataStorePath(); 
    }

    @Override
    public boolean implementsSequenceStore() {
        return this.backingStore.implementsSequenceStore(); 
    }

    @Override
    public IntStream getNidsForAssemblage(int assemblageNid) {
        return this.backingStore.getNidsForAssemblage(assemblageNid); 
    }

    @Override
    public void unregisterDataWriteListener(DataWriteListener dataWriteListener) {
        this.backingStore.unregisterDataWriteListener(dataWriteListener); 
    }

    @Override
    public void registerDataWriteListener(DataWriteListener dataWriteListener) {
        this.backingStore.registerDataWriteListener(dataWriteListener); 
    }

    @Override
    public boolean hasChronologyData(int nid, IsaacObjectType ofType) {
        return this.backingStore.hasChronologyData(nid, ofType); 
    }

    @Override
    public int getAssemblageSizeOnDisk(int assemblageNid) {
        return this.backingStore.getAssemblageSizeOnDisk(assemblageNid); 
    }

    @Override
    public int getAssemblageMemoryInUse(int assemblageNid) {
        return this.backingStore.getAssemblageMemoryInUse(assemblageNid); 
    }

    @Override
    public void putAssemblageVersionType(int assemblageNid, VersionType type) throws IllegalStateException {
        this.backingStore.putAssemblageVersionType(assemblageNid, type); 
    }

    @Override
    public VersionType getVersionTypeForAssemblageNid(int assemblageNid) {
        return this.backingStore.getVersionTypeForAssemblageNid(assemblageNid); 
    }

    @Override
    public int[] accumulateAndGetTaxonomyData(int assemblageNid, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction) {
        return this.backingStore.accumulateAndGetTaxonomyData(assemblageNid, conceptNid, newData, accumulatorFunction); 
    }

    @Override
    public int[] getTaxonomyData(int assemblageNid, int conceptNid) {
        return this.backingStore.getTaxonomyData(assemblageNid, conceptNid); 
    }

    @Override
    public void setAssemblageForNid(int nid, int assemblage) throws IllegalArgumentException {
        this.backingStore.setAssemblageForNid(nid, assemblage); 
    }

    @Override
    public OptionalInt getAssemblageOfNid(int nid) {
        return this.backingStore.getAssemblageOfNid(nid); 
    }

    @Override
    public int[] getSemanticNidsForComponent(int componentNid) {
        return this.backingStore.getSemanticNidsForComponent(componentNid); 
    }

    @Override
    public Optional<ByteArrayDataBuffer> getChronologyVersionData(int nid) {
        return this.backingStore.getChronologyVersionData(nid); 
    }

    @Override
    public void putAssemblageIsaacObjectType(int assemblageNid, IsaacObjectType type) throws IllegalStateException {
        this.backingStore.putAssemblageIsaacObjectType(assemblageNid, type); 
    }

    @Override
    public NidSet getAssemblageNidsForType(IsaacObjectType type) {
        return this.backingStore.getAssemblageNidsForType(type); 
    }

    @Override
    public IsaacObjectType getIsaacObjectTypeForAssemblageNid(int assemblageNid) {
        return this.backingStore.getIsaacObjectTypeForAssemblageNid(assemblageNid); 
    }

    @Override
    public int[] getAssemblageConceptNids() {
        return this.backingStore.getAssemblageConceptNids(); 
    }

    @Override
    public void putChronologyData(ChronologySerializeable chronology) {
        this.backingStore.putChronologyData(chronology); 
    }

    @Override
    public String getUuidPrimordialStringForNid(int nid) {
        return this.backingStore.getUuidPrimordialStringForNid(nid); 
    }

    @Override
    public UUID[] getUuidArrayForNid(int nid) throws NoSuchElementException {
        return this.backingStore.getUuidArrayForNid(nid); 
    }

    @Override
    public IntStream getNidsForAssemblage(ConceptSpecification assemblageSpecification) {
        return this.backingStore.getNidsForAssemblage(assemblageSpecification); 
    }

    @Override
    public boolean implementsExtendedStoreAPI() {
        return this.backingStore.implementsExtendedStoreAPI(); 
    }

    @Override
    public void loadAssemblageOfNid(SpinedNidIntMap nidToAssemblageNidMap) {
        this.postgresProvider.loadAssemblageOfNid(nidToAssemblageNidMap);
    }
}
