package sh.isaac.api.collections;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.collections.uuidnidmap.ConcurrentUuidToIntHashMap;
import sh.isaac.api.collections.uuidnidmap.UuidToIntMap;
import sh.isaac.api.util.UUIDUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public abstract class UuidIntMapMap implements UuidToIntMap {
    /**
     * The Constant LOG.
     */
    protected static final Logger LOG = LogManager.getLogger();
    /**
     * The Constant DEFAULT_TOTAL_MAP_SIZE.
     */
    private static final int DEFAULT_TOTAL_MAP_SIZE = 15000000;
    /**
     * The Constant NUMBER_OF_MAPS.
     */
    protected static final int NUMBER_OF_MAPS = 256;
    /**
     * The Constant DEFAULT_MAP_SIZE.
     */
    protected static final int DEFAULT_MAP_SIZE = DEFAULT_TOTAL_MAP_SIZE / NUMBER_OF_MAPS;
    /**
     * The Constant MIN_LOAD_FACTOR.
     */
    protected static final double MIN_LOAD_FACTOR = 0.75;
    /**
     * The Constant MAX_LOAD_FACTOR.
     */
    protected static final double MAX_LOAD_FACTOR = 0.9;
    /**
     * The Constant NEXT_NID_PROVIDER.
     */
    protected final AtomicInteger NEXT_NID_PROVIDER = new AtomicInteger(Integer.MIN_VALUE);
    /**
     * The lock.
     */
    ReentrantLock lock = new ReentrantLock();
    /**
     * The nid to primordial cache.
     */
    private Cache<Integer, UUID[]> nidToPrimordialCache = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cacheContainsNid(int nid) {
        if (this.nidToPrimordialCache != null) {
            return this.nidToPrimordialCache.getIfPresent(nid) != null;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(UUID key) {
        return getMap(key).containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(int value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean put(UUID uuidKey, int value) {
        final int mapIndex = getMapIndex(uuidKey);
        final long[] keyAsArray = UUIDUtil.convert(uuidKey);
        final ConcurrentUuidToIntHashMap map = getMap(mapIndex);
        final long stamp = map.getStampedLock()
                .writeLock();

        try {
            final boolean returnValue = map.put(keyAsArray, value, stamp);

            this.mapElementUpdated(mapIndex);
            if (returnValue) {
               updateCache(value, uuidKey);
            }
            return returnValue;
        } finally {
            map.getStampedLock()
                    .unlockWrite(stamp);
        }
    }

    /**
     * Report stats.
     *
     * @param log the log
     */
    public void reportStats(Logger log) {
        for (int i = 0; i < NUMBER_OF_MAPS; i++) {
            log.info("UUID map: " + i + " " + getMap(i).getStats());
        }
    }

    /**
     * The number of UUIDs mapped to nids.
     *
     * @return the int
     */
    public int size() {
        int size = 0;

        for (int i = 0; i < NUMBER_OF_MAPS; i++) {
            size += getMap(i).size();
        }

        return size;
    }


    /**
     * Update cache.
     *
     * @param nid the nid
     * @param uuidKey the uuid key
     */
    private void updateCache(int nid, UUID uuidKey) {
        if (this.nidToPrimordialCache != null) {
            synchronized (nidToPrimordialCache) {
                final UUID[] temp = this.nidToPrimordialCache.getIfPresent(nid);
                UUID[] temp1;

                if (temp == null) {
                    temp1 = new UUID[]{uuidKey};
                } else {
                    temp1 = Arrays.copyOf(temp, temp.length + 1);
                    temp1[temp.length] = uuidKey;
                }

                this.nidToPrimordialCache.put(nid, temp1);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionalInt get(UUID key) {
        return getMap(key).get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID[] getKeysForValue(int nid) {
        if (this.nidToPrimordialCache != null) {
            final UUID[] cacheHit = this.nidToPrimordialCache.getIfPresent(nid);

            if ((cacheHit != null) && (cacheHit.length > 0)) {
                return cacheHit;
            }
        }

        final ArrayList<UUID> uuids = new ArrayList<>();

        for (int index = 0; index < NUMBER_OF_MAPS; index++) {
            getMap(index).keysOf(nid)
                    .stream()
                    .forEach(uuid -> {
                        uuids.add(uuid);
                    });
        }

        final UUID[] temp = uuids.toArray(new UUID[uuids.size()]);

        if ((this.nidToPrimordialCache != null) && (temp.length > 0)) {
            this.nidToPrimordialCache.put(nid, temp);
        }

        return temp;
    }
    /**
     * Gets the map.
     *
     * @param index the index
     * @return the map
     * @throws RuntimeException the runtime exception
     */
    protected abstract ConcurrentUuidToIntHashMap getMap(int index);

    /**
     * Gets the map.
     *
     * @param key the key
     * @return the map
     */
    private ConcurrentUuidToIntHashMap getMap(UUID key) {
        if (key == null) {
            throw new IllegalStateException("UUIDs cannot be null. ");
        }

        final int index = getMapIndex(key);

        return getMap(index);
    }

    /**
     * Gets the map index.
     *
     * @param key the key
     * @return the map index
     */
    private int getMapIndex(UUID key) {
        return (((byte) key.hashCode())) - Byte.MIN_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxNid() {
      return NEXT_NID_PROVIDER.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWithGeneration(UUID uuidKey) {
        final long[] keyAsArray = UUIDUtil.convert(uuidKey);
        final int mapIndex = getMapIndex(uuidKey);
        OptionalInt nid = getMap(mapIndex).get(keyAsArray);

        if (nid.isPresent()) {
            return nid.getAsInt();
        }

        final ConcurrentUuidToIntHashMap map = getMap(mapIndex);
        final long stamp = map.getStampedLock()
                .writeLock();

        try {
            nid = map.get(keyAsArray, stamp);

            if (nid.isPresent()) {
                return nid.getAsInt();
            }

            int intNid = NEXT_NID_PROVIDER.incrementAndGet();

            this.mapElementUpdated(mapIndex);
            map.put(keyAsArray, intNid, stamp);
            updateCache(intNid, uuidKey);
            return intNid;
        } finally {
            map.getStampedLock()
                    .unlockWrite(stamp);
        }
    }

    protected abstract void mapElementUpdated(int mapIndex);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean inverseCacheEnabled() {
        return nidToPrimordialCache != null;
    }

    /**
     * @see UuidToIntMap#enableInverseCache()
     */
    @Override
    public void enableInverseCache()
    {
        if (this.nidToPrimordialCache == null) {
            this.nidToPrimordialCache = Caffeine.newBuilder().build();
        }
    }
}
