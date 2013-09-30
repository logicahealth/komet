package org.ihtsdo.otf.tcc.datastore.id;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import org.ihtsdo.otf.tcc.datastore.Bdb;
import org.ihtsdo.otf.tcc.datastore.ComponentBdb;
import org.ihtsdo.otf.tcc.datastore.temp.AceLog;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.api.concurrency.ConcurrentReentrantLocks;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.model.version.RelativePositionComputer;
import org.ihtsdo.otf.tcc.model.version.RelativePositionComputerBI;

/**
 * Caches a variety of relationships of a component to other components as
 * defined by nids in arrays kept in memory for performance.
 * <h2>Implementation Details</h2> The
 * <code>nid</code> is the
 * <code>(nid - Integer.MIN_VALUE)</code> index into an
 * <code>int[][]</code> which stores sequentially increasing arrays of cNid.
 * <br> <br>This array approach is taken because Java stores multidimensional
 * arrays as arrays of arrays, rather than a contiguous block of arrays. Each
 * array has an overhead of 96 bits (above and beyond its data), which doubles
 * the memory size, and also increases the burden on the garbage collector.
 *
 *
 * @author kec
 *
 */
public class MemoryCacheBdb extends ComponentBdb {

    private static final int NID_CNID_MAP_SIZE = 12800;
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private ConcurrentReentrantLocks locks = new ConcurrentReentrantLocks();
    private AtomicReference<int[][][]> indexCacheRecords;
    private boolean[] mapChanged;
    private AtomicReference<int[][]> nidCNidMaps;
    private int readOnlyRecords;

    public MemoryCacheBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
        super(readOnlyBdbEnv, mutableBdbEnv);
    }

    public void addNidPairForRefex(int nid, NidPairForRefex pair) throws IOException {
        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int nidIndexInMap = ((nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        assert (mapIndex >= 0) && (nidIndexInMap >= 0) :
                "mapIndex: " + mapIndex + " indexInMap: " + nidIndexInMap + " nid: " + nid;

        if (mapIndex >= nidCNidMaps.get().length) {
            ensureCapacity(nid);
        }

        IndexCacheRecord record = new IndexCacheRecord(indexCacheRecords.get()[mapIndex][nidIndexInMap]);

        if (!record.isRefexMemberAlreadyThere(pair.getMemberNid())) {
            locks.lock(nid);
            try {
                record = new IndexCacheRecord(indexCacheRecords.get()[mapIndex][nidIndexInMap]);
                record.addNidPairForRefex(pair.getRefexNid(), pair.getMemberNid());
                indexCacheRecords.get()[mapIndex][nidIndexInMap] = record.getData();
                mapChanged[mapIndex] = true;
            } finally {
                locks.unlock(nid);
            }
        }
    }

    public void addRelOrigin(int destinationCNid, int originCNid) throws IOException {
        int mapIndex = (destinationCNid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int nidIndexInMap = ((destinationCNid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        assert (mapIndex >= 0) && (nidIndexInMap >= 0) :
                "mapIndex: " + mapIndex + " indexInMap: " + nidIndexInMap
                + " destinationCNid: " + destinationCNid;

        if (mapIndex >= nidCNidMaps.get().length) {
            ensureCapacity(destinationCNid);
        }

        IndexCacheRecord record = new IndexCacheRecord(indexCacheRecords.get()[mapIndex][nidIndexInMap]);

        if (!record.isDestinationRelOriginAlreadyThere(originCNid)) {
            locks.lock(destinationCNid);

            try {
                record = new IndexCacheRecord(indexCacheRecords.get()[mapIndex][nidIndexInMap]);
                record.addDestinationOriginNid(originCNid);
                indexCacheRecords.get()[mapIndex][nidIndexInMap] = record.getData();
                mapChanged[mapIndex] = true;
            } finally {
                locks.unlock(destinationCNid);
            }
        }
    }

    @Override
    public void close() {
        try {
            sync();
        } catch (IOException e) {
            AceLog.getAppLog().severe(e.getLocalizedMessage(), e);
        }

        super.close();
    }

    private void ensureCapacity(int nextId) throws IOException {
        long next = nextId;
        long numIds = (next - Integer.MIN_VALUE);
        long maps = numIds / NID_CNID_MAP_SIZE;
        int nidCidMapCount = (int) maps + 1;

        rwl.readLock().lock();

        try {
            if (nidCidMapCount > nidCNidMaps.get().length) {
                rwl.readLock().unlock();
                rwl.writeLock().lock();

                if (nidCidMapCount > nidCNidMaps.get().length) {
                    try {
                        expandCapacity(nidCidMapCount);
                    } finally {
                        rwl.readLock().lock();
                        rwl.writeLock().unlock();
                    }
                } else {
                    rwl.readLock().lock();
                    rwl.writeLock().unlock();
                }
            }
        } finally {
            rwl.readLock().unlock();
        }
    }

    private void expandCapacity(int nidCidMapCount) throws IOException {
        int oldCount = nidCNidMaps.get().length;
        int[][] newNidCidMaps = new int[nidCidMapCount][];
        int[][][] newIndexCacheRecords = new int[nidCidMapCount][][];
        boolean[] newMapChanged = new boolean[nidCidMapCount];

        for (int i = 0; i < oldCount; i++) {
            newIndexCacheRecords[i] = indexCacheRecords.get()[i];
            newNidCidMaps[i] = nidCNidMaps.get()[i];
            newMapChanged[i] = mapChanged[i];
        }

        for (int i = oldCount; i < nidCidMapCount; i++) {
            newIndexCacheRecords[i] = new int[NID_CNID_MAP_SIZE][];
            newNidCidMaps[i] = new int[NID_CNID_MAP_SIZE];
            newMapChanged[i] = true;
            Arrays.fill(newNidCidMaps[i], Integer.MAX_VALUE);
        }

        indexCacheRecords.set(newIndexCacheRecords);
        nidCNidMaps.set(newNidCidMaps);
        mapChanged = newMapChanged;
    }

    public void forgetNidPairForRefex(int nid, NidPairForRefex pair) {
        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int nidIndexInMap = ((nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        assert (mapIndex >= 0) && (nidIndexInMap >= 0) :
                "mapIndex: " + mapIndex + " indexInMap: " + nidIndexInMap + " nid: " + nid;

        if (mapIndex >= nidCNidMaps.get().length) {
            throw new NoSuchElementException("nid: " + nid);
        }

        locks.lock(nid);

        try {
            IndexCacheRecord record = new IndexCacheRecord(indexCacheRecords.get()[mapIndex][nidIndexInMap]);

            record.forgetNidPairForRefex(pair.getRefexNid(), pair.getMemberNid());
            indexCacheRecords.get()[mapIndex][nidIndexInMap] = record.getData();
            mapChanged[mapIndex] = true;
        } finally {
            locks.unlock(nid);
        }
    }

    @Override
    protected void init() throws IOException {
        preloadBoth();

        int maxId = Bdb.getUuidsToNidMap().getCurrentMaxNid();

        readOnlyRecords = (int) readOnly.count();

        int mutableRecords = (int) mutable.count();

        AceLog.getAppLog().info("NidCidMap readOnlyRecords: " + readOnlyRecords);
        AceLog.getAppLog().info("NidCidMap mutableRecords: " + mutableRecords);

        int nidCidMapCount = ((maxId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;

        nidCNidMaps = new AtomicReference<>(new int[nidCidMapCount][]);
        indexCacheRecords = new AtomicReference<>(new int[nidCidMapCount][][]);
        mapChanged = new boolean[nidCidMapCount];
        Arrays.fill(mapChanged, false);

        for (int index = 0; index < nidCidMapCount; index++) {
            nidCNidMaps.get()[index] = new int[NID_CNID_MAP_SIZE];
            indexCacheRecords.get()[index] = new int[NID_CNID_MAP_SIZE][];
            Arrays.fill(nidCNidMaps.get()[index], Integer.MAX_VALUE);
        }

        readMaps(readOnly);
        closeReadOnly();
        readMaps(mutable);

        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            printKeys("Read only keys: ", readOnly);
            printKeys("Mutable keys: ", mutable);
        }

        rwl = new ReentrantReadWriteLock();
    }

    private void printKeys(String prefix, Database db) {
        int size = (int) db.count();
        OpenIntIntHashMap nidMap = new OpenIntIntHashMap(size + 2);
        CursorConfig cursorConfig = new CursorConfig();

        cursorConfig.setReadUncommitted(true);

        try (Cursor cursor = db.openCursor(null, cursorConfig)) {
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();

            foundData.setPartial(true);
            foundData.setPartial(0, 0, true);

            int max = Integer.MIN_VALUE;

            while (cursor.getNext(foundKey, foundData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
                int cNid = IntegerBinding.entryToInt(foundKey);

                nidMap.put(cNid, cNid);
                max = Math.max(max, cNid);
            }

            cursor.close();
            AceLog.getAppLog().fine(prefix + nidMap.keys().toList().toString());
        }
    }

    private void readMaps(Database db) {
        CursorConfig cursorConfig = new CursorConfig();

        cursorConfig.setReadUncommitted(true);

        try (Cursor cursor = db.openCursor(null, cursorConfig)) {
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();

            while (cursor.getNext(foundKey, foundData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
                int index = IntegerBinding.entryToInt(foundKey);
                TupleInput ti = new TupleInput(foundData.getData());
                int j = 0;

                while (ti.available() > 0) {
                    nidCNidMaps.get()[index][j] = ti.readInt();

                    int length = ti.readSortedPackedInt();

                    if (length != 0) {
                        int[] cacheData = new int[length];

                        for (int i = 0; i < length; i++) {
                            cacheData[i] = ti.readInt();
                        }

                        indexCacheRecords.get()[index][j] = cacheData;
                    }

                    j++;
                }
            }
        }
    }

    public void resetCNidForNid(int cNid, int nid) throws IOException {
        assert cNid != Integer.MAX_VALUE;

        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;

        assert mapIndex >= 0 : "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex;

        int cNidIndexInMap = (nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;

        assert cNidIndexInMap < NID_CNID_MAP_SIZE :
                "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex + " cNidIndexInMap: " + cNidIndexInMap;
        ensureCapacity(nid);

        if ((nidCNidMaps.get() != null) && (nidCNidMaps.get()[mapIndex] != null)) {
            if (nidCNidMaps.get()[mapIndex][cNidIndexInMap] != cNid) {
                if (cNid < 0) {
                    nidCNidMaps.get()[mapIndex][cNidIndexInMap] = cNid * -1;
                } else {
                    nidCNidMaps.get()[mapIndex][cNidIndexInMap] = cNid;
                }
                mapChanged[mapIndex] = true;
            }
        } else {
            if (nidCNidMaps.get() == null) {
                throw new IOException("Null nidCidMap: ");
            }

            throw new IOException("nidCidMap[" + mapIndex + "] " + "is null. cNid: " + cNid + " nid: " + nid);
        }
    }

    @Override
    public void sync() throws IOException {
        writeChangedMaps();
        super.sync();
    }

    public void updateOutgoingRelationshipData(ConceptChronicle concept) throws IOException {
        int cNid = concept.getNid();
        int mapIndex = (cNid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int nidIndexInMap = ((cNid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        assert (mapIndex >= 0) && (nidIndexInMap >= 0) :
                "mapIndex: " + mapIndex + " indexInMap: " + nidIndexInMap + " cNid: " + cNid;

        if (mapIndex >= nidCNidMaps.get().length) {
            ensureCapacity(cNid);
        }

        locks.lock(concept.getNid());

        try {
            IndexCacheRecord record =
                    new IndexCacheRecord(indexCacheRecords.get()[mapIndex][nidIndexInMap]);
            int[] indexCacheRecordRelationshipArray = RelationshipIndexRecordFactory.make(concept);

            record.updateData(indexCacheRecordRelationshipArray, record.getDestinationOriginNids(),
                    record.getRefexIndexArray());
            indexCacheRecords.get()[mapIndex][nidIndexInMap] = record.getData();
            mapChanged[mapIndex] = true;
        } finally {
            locks.unlock(concept.getNid());
        }
    }

    private void writeChangedMaps() throws IOException {
        rwl.writeLock().lock();

        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            int mapCount = nidCNidMaps.get().length;

            found:
            for (int key = 0; key < mapCount; key++) {
                if (mapChanged[key]) {
                    IntegerBinding.intToEntry(key, keyEntry);

                    // NID_CNID_MAP_SIZE * 4 * 2 = 4 bytes per integer, 2 integers per nid
                    TupleOutput output = new TupleOutput(new byte[NID_CNID_MAP_SIZE * 4 * 2]);

                    for (int i = 0; i < NID_CNID_MAP_SIZE; i++) {
                        output.writeInt(nidCNidMaps.get()[key][i]);

                        int[] cacheRecord = indexCacheRecords.get()[key][i];

                        if (cacheRecord == null) {
                            output.writeSortedPackedInt(0);
                        } else {
                            output.writeSortedPackedInt(cacheRecord.length);

                            for (int j = 0; j < cacheRecord.length; j++) {
                                output.writeInt(cacheRecord[j]);
                            }
                        }
                    }

                    DatabaseEntry valueEntry = new DatabaseEntry(output.toByteArray());
                    OperationStatus status = mutable.put(null, keyEntry, valueEntry);

                    if (status != OperationStatus.SUCCESS) {
                        throw new IOException("Unsuccessful operation: " + status);
                    }

                    mapChanged[key] = false;
                }
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void setIndexed(int nid, boolean indexed) {
        assert nid != Integer.MAX_VALUE;

        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int nidIndexInMap = ((nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        assert (mapIndex >= 0) && (nidIndexInMap >= 0) :
                "mapIndex: " + mapIndex + " indexInMap: " + nidIndexInMap + " nid: " + nid;

        if (mapIndex >= nidCNidMaps.get().length) {
            return;
        }
        int cNid = nidCNidMaps.get()[mapIndex][nidIndexInMap];

        if ((indexed && cNid > 0)
                || (!indexed && cNid < 0)) {
            nidCNidMaps.get()[mapIndex][nidIndexInMap] = cNid * -1;
            mapChanged[mapIndex] = true;
        }
    }

    public boolean isIndexed(int nid) {
        assert nid != Integer.MAX_VALUE;

        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int nidIndexInMap = ((nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        assert (mapIndex >= 0) && (nidIndexInMap >= 0) :
                "mapIndex: " + mapIndex + " indexInMap: " + nidIndexInMap + " nid: " + nid;

        if (mapIndex >= nidCNidMaps.get().length) {
            return false;
        }
        if (nidCNidMaps.get()[mapIndex][nidIndexInMap] < 0) {
            return true;
        }
        return false;

    }

    public int getCNid(int nid) {
        assert nid != Integer.MAX_VALUE;

        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int nidIndexInMap = ((nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        assert (mapIndex >= 0) && (nidIndexInMap >= 0) :
                "mapIndex: " + mapIndex + " indexInMap: " + nidIndexInMap + " nid: " + nid;

        if (mapIndex >= nidCNidMaps.get().length) {
            return Integer.MAX_VALUE;
        }
        int cNid = nidCNidMaps.get()[mapIndex][nidIndexInMap];
        if (cNid > 0) {
            return cNid * -1;
        }
        return cNid;
    }

    @Override
    protected String getDbName() {
        return "MemoryCache";
    }

    public int[] getDestRelNids(int cNid) throws IOException {
        return getIndexCacheRecord(cNid).getDestRelNids(cNid);
    }

    /**
     *
     * @param cNid
     * @param relTypes
     * @return
     * @throws IOException
     */
    public NativeIdSetBI getDestRelNids(int cNid, NativeIdSetBI relTypes, ViewCoordinate vc) throws IOException, ContradictionException {
        return getIndexCacheRecord(cNid).getDestRelNidsSet(cNid, relTypes, vc);
    }

    public NativeIdSetBI getOutgoingRelNids(int cNid, NativeIdSetBI relTypes) throws IOException {
        return getIndexCacheRecord(cNid).getOutgoingRelNidSet(cNid, relTypes);
    }

    public int[] getDestRelNids(int cNid, NidSetBI relTypes) throws IOException {
        IndexCacheRecord record = getIndexCacheRecord(cNid);

        return record.getDestRelNids(cNid, relTypes);
    }

    public int[] getDestRelNids(int cNid, ViewCoordinate vc) throws IOException {
        return getIndexCacheRecord(cNid).getDestRelNids(cNid, vc);
    }

    public Collection<Relationship> getDestRels(int cNid) throws IOException {
        return getIndexCacheRecord(cNid).getDestRels(cNid);
    }

    protected IndexCacheRecord getIndexCacheRecord(int cNid) throws NoSuchElementException {
        int mapIndex = (cNid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int nidIndexInMap = ((cNid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        assert (mapIndex >= 0) && (nidIndexInMap >= 0) :
                "mapIndex: " + mapIndex + " indexInMap: " + nidIndexInMap + " nid: " + cNid;

        if (mapIndex >= nidCNidMaps.get().length) {
            throw new NoSuchElementException("nid: " + cNid);
        }

        locks.lock(cNid);

        try {
            return new IndexCacheRecord(indexCacheRecords.get()[mapIndex][nidIndexInMap]);
        } finally {
            locks.unlock(cNid);
        }
    }

    public NidPairForRefex[] getRefsetPairs(int nid) {
        return getIndexCacheRecord(nid).getNidPairsForRefsets();
    }

    /**
     * Retrieves the components nids enclosed by the input conceptNids.
     *
     * @param conceptNativeIds the <code>NativeIdSetBI</code> for which the
     * components nids will be retrieved
     */
    public NativeIdSetBI getComponentNidsForConceptNids(NativeIdSetBI conceptNids) throws IOException {
        NativeIdSetBI componentNids = new ConcurrentBitSet();
        int maxNid = Bdb.getUuidsToNidMap().getCurrentMaxNid();
        for (int i = Integer.MIN_VALUE; i <= maxNid; i++) {
            int mapIndex = (i - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
            int cNidIndexInMap = ((i - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);
            int nid = nidCNidMaps.get()[mapIndex][cNidIndexInMap];
            if (nid > 0) {
                nid = nid * -1;
            }
            if (conceptNids.contains(nid)) {
                componentNids.add(i);
            }
        }
        return componentNids;

    }

    public boolean hasConcept(int cNid) {
        assert cNid > Integer.MIN_VALUE : "Invalid cNid == Integer.MIN_VALUE: " + cNid;
        assert cNid <= Bdb.getUuidsToNidMap().getCurrentMaxNid() :
                "Invalid cNid: " + cNid + " currentMax: " + Bdb.getUuidsToNidMap().getCurrentMaxNid();

        int mapIndex = (cNid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int cNidIndexInMap = ((cNid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        if ((mapIndex < 0) || (mapIndex >= nidCNidMaps.get().length)) {
            return false;
        }

        if ((cNidIndexInMap < 0) || (cNidIndexInMap >= NID_CNID_MAP_SIZE)) {
            return false;
        }

        int cNid2 = nidCNidMaps.get()[mapIndex][cNidIndexInMap];

        if (cNid2 == cNid) {
            return true;
        }
        if (cNid2 == cNid * -1) {
            return true;
        }

        return false;
    }

    public boolean hasMap(int nid) {
        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int nidIndexInMap = ((nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        if ((mapIndex < nidCNidMaps.get().length) && (nidIndexInMap < NID_CNID_MAP_SIZE)) {
            if (nidCNidMaps.get()[mapIndex][nidIndexInMap] < Integer.MAX_VALUE) {
                return true;
            }
        }

        return false;
    }

    public boolean isKindOf(int childNid, int parentNid, ViewCoordinate vc) throws IOException, ContradictionException {
        if (childNid == parentNid) {
            return true;
        }

        IndexCacheRecord indexCacheRecord = getIndexCacheRecord(childNid);

        RelativePositionComputerBI computer = RelativePositionComputer.getComputer(vc.getViewPosition());

        return indexCacheRecord.isKindOf(parentNid, vc, computer);
    }

    public void setCNidForNid(int cNid, int nid) throws IOException {
        assert cNid != Integer.MAX_VALUE : "cNid: " + cNid + " nid: " + nid;

        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;

        assert mapIndex >= 0 : "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex;

        int nidIndexInMap = ((nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE);

        assert nidIndexInMap < NID_CNID_MAP_SIZE :
                "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex + " nidIndexInMap: " + nidIndexInMap;
        assert (cNid == nid) || hasConcept(cNid) : cNid + " is not a concept nid. nid: " + nid;
        ensureCapacity(nid);

        int[][] nidCNidMapArrays = nidCNidMaps.get();

        assert (nidCNidMapArrays[mapIndex][nidIndexInMap] == Integer.MAX_VALUE)
                || ((int) (nidCNidMapArrays[mapIndex][nidIndexInMap]) == cNid
                || ((int) (nidCNidMapArrays[mapIndex][nidIndexInMap]) == cNid * -1)) :
                "processing cNid: " + cNid + " " + Bdb.getUuidsToNidMap().getUuidsForNid(cNid) + " nid: " + nid
                + " found existing cNid: " + ((int) nidCNidMapArrays[mapIndex][nidIndexInMap]) + " "
                + Bdb.getUuidsToNidMap().getUuidsForNid((int) nidCNidMapArrays[mapIndex][nidIndexInMap]) + "\n    "
                + cNid + " maps to: " + getCNid(cNid) + "\n    " + ((int) nidCNidMapArrays[mapIndex][nidIndexInMap])
                + " maps to: " + getCNid((int) nidCNidMapArrays[mapIndex][nidIndexInMap]);

        if ((nidCNidMapArrays != null) && (nidCNidMapArrays[mapIndex] != null)) {
            if (nidCNidMapArrays[mapIndex][nidIndexInMap] != cNid) {
                if (cNid < 0) {
                    nidCNidMapArrays[mapIndex][nidIndexInMap] = cNid * -1;
                } else {
                    nidCNidMapArrays[mapIndex][nidIndexInMap] = cNid;
                }

                mapChanged[mapIndex] = true;
            }
        } else {
            if (nidCNidMapArrays == null) {
                throw new IOException("Null nidCidMap: ");
            }

            throw new IOException("nidCidMap[" + mapIndex + "] " + "is null. cNid: " + cNid + " nid: " + nid);
        }
    }

    public boolean isChildOf(int childNid, int parentNid, ViewCoordinate vc)
            throws IOException, ContradictionException {
        if (childNid == parentNid) {
            return false;
        }

        IndexCacheRecord indexCacheRecord = getIndexCacheRecord(childNid);

        RelativePositionComputerBI computer = RelativePositionComputer.getComputer(vc.getViewPosition());

        return indexCacheRecord.isChildOf(parentNid, vc, computer);
    }

    /**
     * Obtains concept nids that are a kind of conceptNid.
     *
     * @author dylangrald
     * @param conceptNid the <code>int</code> nid of the input concept
     * @param vc the desired <code>ViewCoordinate</code>
     * @return <code>NativeIdSetBI</code> set of components that are a kind of
     * conceptNid
     */
    public NativeIdSetBI getKindOfNids(int conceptNid, ViewCoordinate vc) throws IOException, ContradictionException {
        NativeIdSetBI kindOfSet = Bdb.getConceptDb().getEmptyIdSet();
        HashSet<Long> testedSet = new HashSet<>();

        kindOfSet.setMember(conceptNid);
        getKindOfNids(conceptNid, vc, kindOfSet, testedSet);

        return kindOfSet;
    }

    public void getChildOfNids(int conceptNid, ViewCoordinate vc, NativeIdSetBI childOfSet, HashSet<Long> testedSet) throws IOException, ContradictionException {
        for (int cNid : getIndexCacheRecord(conceptNid).getDestinationOriginNids()) {
            if (isChildOf(cNid, conceptNid, vc)) {
                childOfSet.setMember(cNid);
            }
        }
    }

    private void getKindOfNids(int conceptNid, ViewCoordinate vc, NativeIdSetBI kindOfSet, HashSet<Long> testedSet)
            throws IOException, ContradictionException {
        for (int cNid : getIndexCacheRecord(conceptNid).getDestinationOriginNids()) {
            long testedKey = conceptNid;

            testedKey = testedKey & 0x00000000FFFFFFFFL;

            long nid1Long = cNid;

            nid1Long = nid1Long & 0x00000000FFFFFFFFL;
            testedKey = testedKey << 32;
            testedKey = testedKey | nid1Long;

            if (!testedSet.contains(testedKey)) {
                testedSet.add(testedKey);

                if (isChildOf(cNid, conceptNid, vc)) {
                    kindOfSet.setMember(cNid);
                    getKindOfNids(cNid, vc, kindOfSet, testedSet);
                }
            }
        }
    }

    public Set<Integer> getAncestorNids(int childNid, ViewCoordinate vc) throws IOException, ContradictionException {
        Set<Integer> ancestorSet = new HashSet<>();
        Set<Long> testedSet = new HashSet<>();
        RelativePositionComputerBI computer = RelativePositionComputer.getComputer(vc.getViewPosition());

        getAncestorNids(childNid, vc, ancestorSet, testedSet, computer);

        return ancestorSet;
    }

    private void getAncestorNids(int childNid, ViewCoordinate vc, Set<Integer> ancestorSet, Set<Long> testedSet,
            RelativePositionComputerBI computer)
            throws IOException, ContradictionException {
        for (RelationshipIndexRecord r : getIndexCacheRecord(childNid).getRelationshipsRecord()) {
            if (!ancestorSet.contains(r.getDestinationNid())) {
                long testedKey = childNid;

                testedKey = testedKey & 0x00000000FFFFFFFFL;

                long nid1Long = r.getDestinationNid();

                nid1Long = nid1Long & 0x00000000FFFFFFFFL;
                testedKey = testedKey << 32;
                testedKey = testedKey | nid1Long;

                if (!testedSet.contains(testedKey)) {
                    testedSet.add(testedKey);

                    if (r.isActiveTaxonomyRelationship(vc, computer)) {
                        ancestorSet.add(r.getDestinationNid());
                        getAncestorNids(r.getDestinationNid(), vc);
                    }
                }
            }
        }
    }

    public boolean hasExtension(int refsetNid, int componentNid) {
        for (NidPairForRefex npr : getRefsetPairs(componentNid)) {
            if (npr.getRefexNid() == refsetNid) {
                return true;
            }
        }

        return false;
    }

    /**
     * Computes the concepts that are a kind of parentNid and returns the set as
     * a
     * <code>NativeIdSetBI</code>.
     *
     * @param parentNid
     * @param vc the desired <code>ViewCoordinate</code>
     * @return a <code>NativeIdSetBI</code> of concepts that are a kind of
     * parentNid
     * @throws IOException
     * @throws ContradictionException
     */
    public NativeIdSetBI isKindOfSet(int parentNid, ViewCoordinate vc) throws IOException, ContradictionException {

        return getKindOfNids(parentNid, vc);
    }

    /**
     * Computes the set of concept nids that are children of given concept nid.
     *
     * @author dylangrald
     * @param conceptNid the parent <code>int</code> nid
     * @param vc the given <code>ViewCoordinate</code>
     * @return the <code>NativeIdSetBI</code> that contains the children of the
     * conceptNid
     */
    public NativeIdSetBI isChildOfSet(int conceptNid, ViewCoordinate vc) throws IOException, ContradictionException {
        NativeIdSetBI childOfSet = Bdb.getConceptDb().getEmptyIdSet();
        HashSet<Long> testedSet = new HashSet<>();

        getChildOfNids(conceptNid, vc, childOfSet, testedSet);
        return childOfSet;
    }
}
