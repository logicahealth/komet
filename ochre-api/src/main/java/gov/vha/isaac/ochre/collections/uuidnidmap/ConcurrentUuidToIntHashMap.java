package gov.vha.isaac.ochre.collections.uuidnidmap;

import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by kec on 7/25/14.
 */
public class ConcurrentUuidToIntHashMap extends UuidToIntHashMap {
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public Lock getWriteLock() {
        return w;
    }


    /**
     * Constructs an empty map with default capacity and default load factors.
     */
    public ConcurrentUuidToIntHashMap() {
        super(defaultCapacity);
    }

    /**
     * Constructs an empty map with the specified initial capacity and default load factors.
     *
     * @param initialCapacity the initial capacity of the map.
     * @throws IllegalArgumentException if the initial capacity is less than zero.
     */
    public ConcurrentUuidToIntHashMap(int initialCapacity) {
        super(initialCapacity, defaultMinLoadFactor, defaultMaxLoadFactor);
    }

    /**
     * Constructs an empty map with the specified initial capacity and the specified minimum and maximum load
     * factor.
     *
     * @param initialCapacity the initial capacity.
     * @param minLoadFactor the minimum load factor.
     * @param maxLoadFactor the maximum load factor.
     * @throws IllegalArgumentException if
     *
     * <tt>initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) || (maxLoadFactor <= 0.0 ||
     * maxLoadFactor >= 1.0) || (minLoadFactor >= maxLoadFactor)</tt> .
     */
    public ConcurrentUuidToIntHashMap(int initialCapacity, double minLoadFactor,
                            double maxLoadFactor) {
        super(initialCapacity, minLoadFactor, maxLoadFactor);
    }
    @Override
    public boolean containsKey(long[] key) {
        r.lock();
        try {
            return indexOfKey(key) >= 0;
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean containsKey(UUID key) {
        r.lock();
        try {
            return indexOfKey(key) >= 0;
        } finally {
            r.unlock();
        }
    }

    public int getDistinct() {
        return distinct;
    }

    public void setDistinct(int distinct) {
        this.distinct = distinct;
    }
    @Override
    public int get(long[] key) {
        r.lock();
        try {
            return super.get(key);
        } finally {
            r.unlock();
        }
    }

    @Override
    public int get(UUID key) {
        r.lock();
        try {
            return super.get(key);
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean put(long[] key, int value) {
        w.lock();
        try {
            return super.put(key, value);

        } finally {
            w.unlock();
        }
    }
}
