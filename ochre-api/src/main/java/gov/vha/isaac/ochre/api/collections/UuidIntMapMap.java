package gov.vha.isaac.ochre.api.collections;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.ochre.api.collections.uuidnidmap.ConcurrentUuidToIntHashMap;
import gov.vha.isaac.ochre.api.collections.uuidnidmap.UuidToIntMap;
import gov.vha.isaac.ochre.api.memory.DiskSemaphore;
import gov.vha.isaac.ochre.api.memory.HoldInMemoryCache;
import gov.vha.isaac.ochre.api.memory.MemoryManagedReference;
import gov.vha.isaac.ochre.api.memory.WriteToDiskCache;
import gov.vha.isaac.ochre.api.util.UUIDUtil;

/**
 * Created by kec on 7/27/14.
 */
public class UuidIntMapMap implements UuidToIntMap {

    private static final Logger LOG = LogManager.getLogger();
    private static final int DEFAULT_TOTAL_MAP_SIZE = 15000000;
    public static final int NUMBER_OF_MAPS = 256;
    
    public static int NID_TO_UUID_CACHE_SIZE = 0;  //defaults to disabled / not normally used.
    //Loader utility code sets this to a much larger value, as there is no alternate cache to get from nid back to UUID
    //when the data isn't being written to the DB.
    
    private static final int DEFAULT_MAP_SIZE = DEFAULT_TOTAL_MAP_SIZE / NUMBER_OF_MAPS;
    private static final double MIN_LOAD_FACTOR = 0.75;
    private static final double MAX_LOAD_FACTOR = 0.9;
    
    private static final AtomicInteger NEXT_NID_PROVIDER = new AtomicInteger(Integer.MIN_VALUE);

    public static AtomicInteger getNextNidProvider() {
        return NEXT_NID_PROVIDER;
    }

    public boolean shutdown = false;

    private static final ConcurrentUuidIntMapSerializer SERIALIZER = new ConcurrentUuidIntMapSerializer();

    private final MemoryManagedReference<ConcurrentUuidToIntHashMap>[] maps = new MemoryManagedReference[NUMBER_OF_MAPS];
    private final File folder;
    
    private LruCache<Integer, UUID[]> nidToPrimoridialCache = null;
    
    private UuidIntMapMap(File folder) {
        folder.mkdirs();
        this.folder = folder;
        for (int i = 0; i < maps.length; i++) {
            maps[i] = new MemoryManagedReference<>(
                    null,
                    new File(folder, i + "-uuid-nid.map"), SERIALIZER);
            WriteToDiskCache.addToCache(maps[i]);
        }
        if (NID_TO_UUID_CACHE_SIZE > 0) {
            nidToPrimoridialCache = new LruCache<>(NID_TO_UUID_CACHE_SIZE);
        }
        LOG.debug("Created UuidIntMapMap: " + this);
    }
    
    public static UuidIntMapMap create(File folder) {
        return new UuidIntMapMap(folder);
    }
    
    public void write() throws IOException {
        for (int i = 0; i < NUMBER_OF_MAPS; i++) {
            ConcurrentUuidToIntHashMap map = maps[i].get();
            if (map != null && maps[i].hasUnwrittenUpdate()) {
                maps[i].write();
            }
        }
    }


    ReentrantLock lock = new ReentrantLock();

    protected void readMapFromDisk(int i) throws IOException {
        lock.lock();
        try {
            if (maps[i].get() == null) {
                File mapFile = new File(folder, i + "-uuid-nid.map");
                if (mapFile.exists()) {
                    DiskSemaphore.acquire();
                    try (DataInputStream in = new DataInputStream(new BufferedInputStream(
                            new FileInputStream(mapFile)))) {
                        maps[i] = new MemoryManagedReference<>(SERIALIZER.deserialize(in),
                                mapFile, SERIALIZER);
                        WriteToDiskCache.addToCache(maps[i]);
                        LOG.debug("UuidIntMapMap restored: " + i + " from: " + this + " file: " + mapFile.getAbsolutePath());
                    } finally {
                        DiskSemaphore.release();
                    }
                } else {
                    maps[i] = new MemoryManagedReference<>(
                            new ConcurrentUuidToIntHashMap(DEFAULT_MAP_SIZE, MIN_LOAD_FACTOR, MAX_LOAD_FACTOR),
                            new File(folder, i + "-uuid-nid.map"), SERIALIZER);
                    WriteToDiskCache.addToCache(maps[i]);

                }
            }
        } finally {
            lock.unlock();
        }
    }

    private ConcurrentUuidToIntHashMap getMap(UUID key) {
        if (key == null) {
            throw new IllegalStateException("UUIDs cannot be null. ");
        }
        int index = getMapIndex(key);
        return getMap(index);
    }

    protected ConcurrentUuidToIntHashMap getMap(int index) throws RuntimeException {
        ConcurrentUuidToIntHashMap result = maps[index].get();
        while (result == null) {
            try {
                readMapFromDisk(index);
                result = maps[index].get();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        maps[index].elementRead();
        HoldInMemoryCache.addToCache(maps[index]);
        return result;
    }

    @Override
    public boolean containsKey(UUID key) {
        return getMap(key).containsKey(key);
    }

    @Override
    public boolean containsValue(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int get(UUID key) {
        return getMap(key).get(key);
    }

    public int getWithGeneration(UUID uuidKey) {
         
        long[] keyAsArray = UUIDUtil.convert(uuidKey);
    
        int mapIndex = getMapIndex(uuidKey);
        int nid = getMap(mapIndex).get(keyAsArray);
        if (nid != Integer.MAX_VALUE) {
            return nid;
        }
        ConcurrentUuidToIntHashMap map = getMap(mapIndex);
        long stamp = map.getStampedLock().writeLock();
        try {
            nid = map.get(keyAsArray, stamp);
            if (nid != Integer.MAX_VALUE) {
                return nid;
            }
            nid = NEXT_NID_PROVIDER.incrementAndGet();
//            if (nid == -2147483637) {
//                System.out.println(nid + "->" + key);
//            }
            maps[mapIndex].elementUpdated();
            map.put(keyAsArray, nid, stamp);
            updateCache(nid, uuidKey);
            return nid;
        } finally {
            map.getStampedLock().unlockWrite(stamp);
        }
    }

    private void updateCache(int nid, UUID uuidKey) {
        if (nidToPrimoridialCache != null) {
            UUID[] temp = nidToPrimoridialCache.get(nid);
            UUID[] temp1;
            if (temp == null) {
                temp1 = new UUID[] {uuidKey};
            }
            else {
                temp1 = Arrays.copyOf(temp, temp.length + 1);
                temp1[temp.length] = uuidKey;
            }
            nidToPrimoridialCache.put(nid, temp1);
        }
    }

    @Override
    public boolean put(UUID uuidKey, int value) {
        updateCache(value, uuidKey);
        int mapIndex = getMapIndex(uuidKey);
        long[] keyAsArray = UUIDUtil.convert(uuidKey);
        ConcurrentUuidToIntHashMap map = getMap(mapIndex);
        long stamp = map.getStampedLock().writeLock();
        try {
            boolean returnValue = map.put(keyAsArray, value, stamp);
            maps[mapIndex].elementUpdated();
            return returnValue;
        } finally {
            map.getStampedLock().unlockWrite(stamp);
        }
    }

    public int size() {
        int size = 0;
        for (int i = 0; i < maps.length; i++) {
            size += getMap(i).size();
        }
        return size;
    }

    private int getMapIndex(UUID key) {
        return ((int) ((byte) key.hashCode())) - Byte.MIN_VALUE;
    }


    public UUID[] getKeysForValue(int value) {
        if (nidToPrimoridialCache != null) {
            UUID[] cacheHit = nidToPrimoridialCache.get(value);
            if (cacheHit != null) {
                return cacheHit;
            }
        }
        ArrayList<UUID> uuids = new ArrayList<>();
        for (int index = 0; index < maps.length; index++) {
            getMap(index).keysOf(value).stream().forEach(uuid -> {
                uuids.add(uuid);
            });
        }
        UUID[] temp = uuids.toArray(new UUID[uuids.size()]);
        if (nidToPrimoridialCache != null) {
            nidToPrimoridialCache.put(value, temp);
        }
        return temp;
    }
    
    public void reportStats(Logger log) {
        for (int i = 0; i < NUMBER_OF_MAPS; i++) {
            log.info("UUID map: " + i + " " + getMap(i).getStats());
        }
    }
    
    /**
     * This method is an optimization for loader patterns, where it can be faster to read the nid to UUID from this cache, 
     * but only if the cache actually as the value.
     * @param nid
     * @return
     */
    public boolean cacheContainsNid(int nid) {
        if (nidToPrimoridialCache != null) {
            return nidToPrimoridialCache.containsKey(nid);
        }
        return false;
    }


    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
