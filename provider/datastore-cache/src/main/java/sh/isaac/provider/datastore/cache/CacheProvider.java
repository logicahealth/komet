package sh.isaac.provider.datastore.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.UuidIntMapMapMemoryBased;
import sh.isaac.api.collections.uuidnidmap.UuidToIntMap;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.datastore.ChronologySerializeable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.util.time.DurationUtil;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.DataStoreSubService;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.*;
import sh.isaac.model.collections.store.ByteArrayArrayStoreProvider;
import sh.isaac.model.collections.store.IntIntArrayStoreProvider;
import sh.isaac.model.semantic.SemanticChronologyImpl;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

public class CacheProvider
        implements DatastoreAndIdentiferService {
    private static final Logger LOG = LogManager.getLogger();


    DataStoreSubService datastoreService;
    IdentifierService identifierService;

    private final ArrayList<DataWriteListener> writeListeners = new ArrayList<>();

    private UuidToIntMap uuidIntMapMap;
    private ConcurrentHashMap<Integer, IsaacObjectType> assemblageToObjectType_Map;
    private ConcurrentHashMap<Integer, VersionType> assemblageToVersionType_Map;
    private SpinedNidIntMap nidToAssemblageNidMap;
    private int[] assemblageNids;
    private final SpinedNidNidSetMap componentToSemanticNidsMap = new SpinedNidNidSetMap();

    private final ConcurrentHashMap<Integer, SpinedByteArrayArrayMap> spinedChronologyMapMap
            = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, SpinedIntIntArrayMap> spinedTaxonomyMapMap
            = new ConcurrentHashMap<>();

    public CacheProvider(DataStoreSubService datastoreService,
            IdentifierService identifierService) {
        this.datastoreService = datastoreService;
        this.identifierService = identifierService;
        this.uuidIntMapMap = new UuidIntMapMapMemoryBased();
        this.uuidIntMapMap.enableInverseCache();
        this.assemblageToObjectType_Map = new ConcurrentHashMap<>();
        this.assemblageToVersionType_Map = new ConcurrentHashMap<>();
        this.nidToAssemblageNidMap = new SpinedNidIntMap();
    }

    @Override
    public void startup() {
        this.assemblageNids = this.identifierService.getAssemblageNids();
        for (int assemblageNid: this.assemblageNids) {
            this.assemblageToObjectType_Map.put(assemblageNid, this.datastoreService.getIsaacObjectTypeForAssemblageNid(assemblageNid));
            this.assemblageToVersionType_Map.put(assemblageNid, this.datastoreService.getVersionTypeForAssemblageNid(assemblageNid));
        }

    }

    @Override
    public void shutdown() {
        this.datastoreService = null;
        this.identifierService = null;
        this.uuidIntMapMap = null;
        this.assemblageToObjectType_Map = null;
        this.assemblageToVersionType_Map = null;
        this.nidToAssemblageNidMap = null;
        this.assemblageNids = null;
        this.writeListeners.clear();
        this.componentToSemanticNidsMap.clear();
        this.spinedChronologyMapMap.clear();
        this.spinedTaxonomyMapMap.clear();

    }

    @Override
    public int getMaxNid() {
        return this.identifierService.getMaxNid();
    }

    @Override
    public void addUuidForNid(UUID uuid, int nid) {
        OptionalInt old = this.uuidIntMapMap.get(uuid);
        if (old.isPresent() && old.getAsInt() != nid) {
            throw new RuntimeException("Reassignment of nid for " + uuid + " from " + old + " to " + nid);
        }
        this.uuidIntMapMap.put(uuid, nid);
        this.identifierService.addUuidForNid(uuid, nid);
    }

    @Override
    public IsaacObjectType getObjectTypeForComponent(int componentNid) {
        OptionalInt assemblageNid = getAssemblageNid(componentNid);
        if (assemblageNid.isPresent()) {
            return this.assemblageToObjectType_Map.computeIfAbsent(assemblageNid.getAsInt(),
                    key -> this.identifierService.getObjectTypeForComponent(componentNid));
        }
        return IsaacObjectType.UNKNOWN;
    }

    @Override
    public VersionType getVersionTypeForAssemblageNid(int assemblageNid) {
        return this.assemblageToVersionType_Map.computeIfAbsent(assemblageNid,
                key -> this.datastoreService.getVersionTypeForAssemblageNid(key));
    }

    @Override
    public OptionalInt getAssemblageNid(int componentNid) {
        int value = nidToAssemblageNidMap.get(componentNid);
        if (value != Integer.MAX_VALUE) {
            return OptionalInt.of(value);
        }
        OptionalInt optionalValue = this.identifierService.getAssemblageNid(componentNid);
        if (optionalValue.isPresent()) {
            nidToAssemblageNidMap.put(componentNid, optionalValue.getAsInt());
            return optionalValue;
        }
        return OptionalInt.empty();
    }

    @Override
    public int[] getAssemblageNids() {
        // TODO [future] websocket notification of new semantics...
        return this.assemblageNids;
    }

    @Override
    public int assignNid(UUID... uuids) throws IllegalArgumentException {
        boolean foundAll = true;
        int nid = Integer.MAX_VALUE;
        for (UUID uuid : uuids) {
            OptionalInt old = this.uuidIntMapMap.get(uuid);
            if (old.isPresent()) {
                nid = old.getAsInt();
            } else {
                foundAll = false;
            }
        }
        if (foundAll) {
            return nid;
        }
        nid = this.identifierService.assignNid(uuids);
        for (UUID uuid : uuids) {
            this.uuidIntMapMap.put(uuid, nid);
        }
        return nid;
    }

    @Override
    public IntStream getNidStreamOfType(IsaacObjectType objectType) {
        int maxNid = this.identifierService.getMaxNid();
        NidSet allowedAssemblages = this.getAssemblageNidsForType(objectType);

        return IntStream.rangeClosed(Integer.MIN_VALUE + 1, maxNid)
                .filter((value) -> {
                    return allowedAssemblages.contains(this.getAssemblageOfNid(value).orElseGet(() -> Integer.MAX_VALUE));
                });
    }

    @Override
    public int getNidForUuids(Collection<UUID> uuids) throws NoSuchElementException {
        OptionalInt optionalNid = this.uuidIntMapMap.get(uuids.iterator().next());
        if (optionalNid.isPresent()) {
            return optionalNid.getAsInt();
        }
        int nid = this.identifierService.getNidForUuids(uuids);
        for (UUID uuid : uuids) {
            this.uuidIntMapMap.put(uuid, nid);
        }
        return nid;
    }

    @Override
    public int getNidForUuids(UUID... uuids) throws NoSuchElementException {
        OptionalInt optionalNid = this.uuidIntMapMap.get(uuids[0]);
        if (optionalNid.isPresent()) {
            return optionalNid.getAsInt();
        }
        int nid = this.identifierService.getNidForUuids(uuids);

        for (UUID uuid : uuids) {
            this.uuidIntMapMap.put(uuid, nid);
        }
        return nid;
    }

    @Override
    public boolean hasUuid(Collection<UUID> uuids) throws IllegalArgumentException {
        OptionalInt optionalNid = this.uuidIntMapMap.get(uuids.iterator().next());
        if (optionalNid.isPresent()) {
            return true;
        }
        if (this.identifierService.hasUuid(uuids)) {
            getNidForUuids(uuids);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasUuid(UUID... uuids) throws IllegalArgumentException {
        OptionalInt optionalNid = this.uuidIntMapMap.get(uuids[0]);
        if (optionalNid.isPresent()) {
            return true;
        }
        if (this.identifierService.hasUuid(uuids)) {
            getNidForUuids(uuids);
            return true;
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

        List<UUID> uuids;
        Optional<? extends Chronology> optionalChronology = Get.identifiedObjectService().getChronology(nid);
        if (optionalChronology.isPresent()) {
            uuids = optionalChronology.get().getUuidList();
        } else {
            uuids = this.identifierService.getUuidsForNid(nid);
        }
        for (UUID uuid : uuids) {
            this.uuidIntMapMap.put(uuid, nid);
        }
        return uuids;
    }

    @Override
    public long getMemoryInUse() {
        //TODO
        return -1;
    }

    @Override
    public long getSizeOnDisk() {
        //TODO
        return -1;
    }

    @Override
    public void setupNid(int nid, int assemblageNid, IsaacObjectType objectType, VersionType versionType) throws IllegalStateException {
        this.identifierService.setupNid(nid, assemblageNid, objectType, versionType);
        this.assemblageToObjectType_Map.put(assemblageNid, objectType);
        this.assemblageToVersionType_Map.put(assemblageNid, versionType);
        this.nidToAssemblageNidMap.put(nid, assemblageNid);

    }

    @Override
    public void optimizeForOutOfOrderLoading() {

    }


    @Override
    public IntStream getNidsForAssemblage(int assemblageNid) {
        return this.identifierService.getNidsForAssemblage(assemblageNid);
    }

    @Override
    public IntStream getNidStream() {
        return this.identifierService.getNidStream();
    }

    @Override
    public Path getDataStorePath() {
        return datastoreService.getDataStorePath();
    }

    @Override
    public DataStoreStartState getDataStoreStartState() {
        return datastoreService.getDataStoreStartState();
    }

    @Override
    public Optional<UUID> getDataStoreId() {
        return datastoreService.getDataStoreId();
    }

    @Override
    public Future<?> sync() {
        Future<?> idServiceFuture = identifierService.sync();
        Future<?> datastoreFuture = datastoreService.sync();
        return new Future<Object>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return idServiceFuture.isCancelled() && datastoreFuture.isCancelled();
            }

            @Override
            public boolean isDone() {
                return idServiceFuture.isDone() && datastoreFuture.isDone();
            }

            @Override
            public Object get() throws InterruptedException, ExecutionException {
                idServiceFuture.get();
                datastoreFuture.get();
                return null;
            }

            @Override
            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                idServiceFuture.get(timeout, unit);
                datastoreFuture.get(timeout, unit);
                return null;
            }
        };
    }

    @Override
    public void putChronologyData(ChronologySerializeable chronology) {
        try {
            int assemblageNid = chronology.getAssemblageNid();

            if (chronology instanceof SemanticChronologyImpl) {
                SemanticChronologyImpl semanticChronology = (SemanticChronologyImpl) chronology;
                int referencedComponentNid = semanticChronology.getReferencedComponentNid();

                //We could optionally check and see if this chronology is already listed for this nid, but its likely cheaper to just let it merge internally
                componentToSemanticNidsMap.add(referencedComponentNid, semanticChronology.getNid());
            }

            SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNid);

            spinedByteArrayArrayMap.put(chronology.getNid(), ChronologyImpl.getDataList(chronology));

            for (DataWriteListener dwl : writeListeners) {
                dwl.writeData(chronology);
            }

        } catch (Throwable e) {
            LOG.error("Unexpected error putting chronology data!", e);
            throw e;
        }
    }

    @Override
    public int[] getAssemblageConceptNids() {
        if (assemblageNids == null) {
            this.assemblageNids = this.identifierService.getAssemblageNids();
        }
        return assemblageNids;
    }

    @Override
    public IsaacObjectType getIsaacObjectTypeForAssemblageNid(int assemblageNid) {
        if (this.assemblageToObjectType_Map.containsKey(assemblageNid)) {
            return this.assemblageToObjectType_Map.get(assemblageNid);
        }
        IsaacObjectType objectType = this.datastoreService.getIsaacObjectTypeForAssemblageNid(assemblageNid);
        this.assemblageToObjectType_Map.put(assemblageNid, objectType);
        return objectType;
    }

    @Override
    public NidSet getAssemblageNidsForType(IsaacObjectType searchType) {
        final NidSet results = new NidSet();
        assemblageToObjectType_Map.forEach((nid, type) -> {
            if (searchType == type) {
                results.add(nid);
            }
        });
        return results;
    }

    @Override
    public void putAssemblageIsaacObjectType(int assemblageNid, IsaacObjectType type) throws IllegalStateException {
        IsaacObjectType oldValue = assemblageToObjectType_Map.get(assemblageNid);
        if (oldValue == null || oldValue == IsaacObjectType.UNKNOWN) {
            this.datastoreService.putAssemblageIsaacObjectType(assemblageNid, type);
            this.assemblageToObjectType_Map.put(assemblageNid, type);
        } else if (oldValue != type) {
            throw new IllegalStateException("Tried to change the isaac object type of " + assemblageNid + " from " + oldValue + " to " + type);
        }
    }

    @Override
    public Optional<ByteArrayDataBuffer> getChronologyVersionData(int nid) {
        OptionalInt assemblageNidOptional = getAssemblageNid(nid);
        if (!assemblageNidOptional.isPresent()) {
            return Optional.empty();
        }
        SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNidOptional.getAsInt());
        byte[][] data = spinedByteArrayArrayMap.get(nid);
        if (data == null) {
            Optional<ByteArrayDataBuffer> optionalByteBuffer = this.datastoreService.getChronologyVersionData(nid);
            if (optionalByteBuffer.isPresent()) {
                data = optionalByteBuffer.get().toDataArray();
                spinedByteArrayArrayMap.put(nid, data);
            } else {
                return Optional.empty();
            }
            
        }
        return Optional.of(ByteArrayDataBuffer.dataArrayToBuffer(data)); 
    }

    @Override
    public int[] getSemanticNidsForComponent(int componentNid) {
        if (this.componentToSemanticNidsMap.containsKey(componentNid)) {
            return this.componentToSemanticNidsMap.get(componentNid);
        }
        int[] semanticNids = this.datastoreService.getSemanticNidsForComponent(componentNid);
        this.componentToSemanticNidsMap.put(componentNid, semanticNids);
        return semanticNids;
    }

    AtomicBoolean startGetAssemblageForNids = new AtomicBoolean(true);
    private class GetAssemblageForNids implements Runnable {

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            CacheBootstrap cacheBootstrap = (CacheBootstrap) CacheProvider.this.datastoreService;
            cacheBootstrap.loadAssemblageOfNid(nidToAssemblageNidMap);
            LOG.info("Loaded nidToAssemblageNidMap in " + DurationUtil.msTo8601(System.currentTimeMillis() - startTime));
        }
    }

    @Override
    public OptionalInt getAssemblageOfNid(int nid) {
        if (startGetAssemblageForNids.get()) {
            if (startGetAssemblageForNids.getAndSet(false)) {
                Get.executor().execute(new GetAssemblageForNids());
            }
        }

        int value = nidToAssemblageNidMap.get(nid);
        if (value != Integer.MAX_VALUE) {
            return OptionalInt.of(value);
        }
        OptionalInt optionalValue = this.datastoreService.getAssemblageOfNid(nid);
        if (optionalValue.isPresent()) {
            nidToAssemblageNidMap.put(nid, optionalValue.getAsInt());
        }
        return optionalValue;
    }

    @Override
    public void setAssemblageForNid(int nid, int assemblage) throws IllegalArgumentException {
        OptionalInt current = getAssemblageOfNid(nid);
        if (current.isPresent() && current.getAsInt() != assemblage) {
            throw new IllegalArgumentException("The nid " + nid + " is already assigned to assemblage "
                    + current + " and cannot be reassigned to " + nid);
        } else {
            nidToAssemblageNidMap.put(nid, assemblage);
            this.datastoreService.setAssemblageForNid(nid, assemblage);
        }
    }

    private SpinedIntIntArrayMap getTaxonomyMap(int assemblageNid) {
        SpinedIntIntArrayMap spinedMap = spinedTaxonomyMapMap.computeIfAbsent(
                assemblageNid,
                (dbKey) -> {
                    SpinedIntIntArrayMap spinedIntIntArrayMap = new SpinedIntIntArrayMap(Get.service(IntIntArrayStoreProvider.class).get(assemblageNid));
                    int spinesRead = spinedIntIntArrayMap.read();
                    if (spinesRead > 0) {
                        LOG.info("Read  " + spinesRead + " taxonomy spines for assemblage: "
                                + " " + assemblageNid + " " + Integer.toUnsignedString(assemblageNid));
                    }
                    return spinedIntIntArrayMap;
                });

        return spinedMap;
    }
    AtomicBoolean startGetTaxonomyData = new AtomicBoolean(true);
    private class GetTaxonomyData implements Runnable {

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            CacheBootstrap cacheBootstrap = (CacheBootstrap) CacheProvider.this.datastoreService;
            int assemblageForTaxonomyNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getAssemblageNid();
            cacheBootstrap.loadTaxonomyData(assemblageForTaxonomyNid, getTaxonomyMap(assemblageForTaxonomyNid));
            LOG.info("Loaded Taxonomy Data in " + DurationUtil.msTo8601(System.currentTimeMillis() - startTime));
        }
    }

    @Override
    public int[] getTaxonomyData(int assemblageNid, int conceptNid) {
        if (startGetTaxonomyData.get()) {
            if (startGetTaxonomyData.getAndSet(false)) {
                Get.executor().execute(new GetTaxonomyData());
            }
        }
        int[] data = getTaxonomyMap(assemblageNid).get(conceptNid);
        if (data == null) {
            data = this.datastoreService.getTaxonomyData(assemblageNid, conceptNid);
            this.spinedTaxonomyMapMap.get(assemblageNid).put(conceptNid, data);
        }
        return data;
    }
    @Override
    public int[] accumulateAndGetTaxonomyData(int assemblageNid, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction) {
        int[] accumulatedData = getTaxonomyMap(assemblageNid).accumulateAndGet(conceptNid, newData, accumulatorFunction);
        int[] accumulatedDatastoreData = this.datastoreService.accumulateAndGetTaxonomyData(assemblageNid, conceptNid, accumulatedData, accumulatorFunction);
        newData = accumulatedDatastoreData;
        while (!Arrays.equals(accumulatedData, accumulatedDatastoreData)) {
            accumulatedData = getTaxonomyMap(assemblageNid).accumulateAndGet(conceptNid, accumulatedDatastoreData, accumulatorFunction);
            accumulatedDatastoreData = this.datastoreService.accumulateAndGetTaxonomyData(assemblageNid, conceptNid, accumulatedData, accumulatorFunction);
        }
        return accumulatedDatastoreData;
    }

    private void printArray(String prefix, int[] array) {
        StringBuilder b = new StringBuilder(prefix);
        b.append("\n");
        for (int i = 0; i < array.length; i++) {
            b.append(i).append(": ");
            b.append(array[i]).append("\n");
        }
        b.append("\n");
        System.out.println(b);
    }

    @Override
    public void putAssemblageVersionType(int assemblageNid, VersionType type) throws IllegalStateException {
        VersionType oldValue = assemblageToVersionType_Map.computeIfAbsent(assemblageNid, (i -> type));
        if (oldValue != type && oldValue != VersionType.UNKNOWN) {
            throw new IllegalStateException("Tried to change the version type of " + assemblageNid + " from " + oldValue + " to " + type);
        }
        assemblageToVersionType_Map.put(assemblageNid, type);
        if (oldValue != type) {
            this.datastoreService.putAssemblageVersionType(assemblageNid, type);
        }
    }

    @Override
    public int getAssemblageMemoryInUse(int assemblageNid) {
        if (spinedChronologyMapMap.containsKey(assemblageNid)) {
            return getChronologySpinedMap(assemblageNid).memoryInUse();
        }
        return 0;
    }

    @Override
    public int getAssemblageSizeOnDisk(int assemblageNid) {
        return this.datastoreService.getAssemblageSizeOnDisk(assemblageNid);
    }

    @Override
    public boolean hasChronologyData(int nid, IsaacObjectType ofType) {
        OptionalInt assemblageNid = ModelGet.identifierService().getAssemblageNid(nid);
        if (assemblageNid.isPresent()) {
            if (ofType != assemblageToObjectType_Map.get(assemblageNid.getAsInt())) {
                return false;
            }
            SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNid.getAsInt());
            byte[][] data = spinedByteArrayArrayMap.get(nid);
            return data != null;
        }
        return this.datastoreService.hasChronologyData(nid, ofType);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDataWriteListener(DataWriteListener dataWriteListener) {
        writeListeners.add(dataWriteListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterDataWriteListener(DataWriteListener dataWriteListener) {
        writeListeners.remove(dataWriteListener);
    }

    @Override
    public boolean implementsSequenceStore() {
        return false;
    }

    private SpinedByteArrayArrayMap getChronologySpinedMap(int assemblageNid) {
        SpinedByteArrayArrayMap spinedMap = spinedChronologyMapMap.computeIfAbsent(
                assemblageNid,
                (dbKey) -> {
                    SpinedByteArrayArrayMap spinedByteArrayArrayMap = new SpinedByteArrayArrayMap(Get.service(ByteArrayArrayStoreProvider.class).get(assemblageNid));
                    return spinedByteArrayArrayMap;
                });

        return spinedMap;
    }

    @Override
    public DatabaseImplementation getDataStoreType()
    {
        throw new RuntimeException("Was never properly integrated");
    }
    
    
}
