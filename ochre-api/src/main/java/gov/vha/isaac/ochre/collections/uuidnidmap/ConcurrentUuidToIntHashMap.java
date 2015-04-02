package gov.vha.isaac.ochre.collections.uuidnidmap;

import java.util.UUID;
import java.util.concurrent.locks.StampedLock;

/**
 * Created by kec on 7/25/14.
 */
public class ConcurrentUuidToIntHashMap extends UuidToIntHashMap {
    
    StampedLock sl = new StampedLock();

    public StampedLock getStampedLock() {
        return sl;
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
     * {@code initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) || (maxLoadFactor <= 0.0 ||
     * maxLoadFactor >= 1.0) || (minLoadFactor >= maxLoadFactor)} .
     */
    public ConcurrentUuidToIntHashMap(int initialCapacity, double minLoadFactor,
                            double maxLoadFactor) {
        super(initialCapacity, minLoadFactor, maxLoadFactor);
    }
    @Override
    public boolean containsKey(long[] key) {
        long stamp = sl.tryOptimisticRead();
        boolean containsKey = indexOfKey(key) >= 0;
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                containsKey = indexOfKey(key) >= 0;
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return containsKey;
    }

    @Override
    public boolean containsKey(UUID key) {
        return this.containsKey(new long[] {key.getMostSignificantBits(),
                                      key.getLeastSignificantBits()});
    }

    public int getDistinct() {
        return distinct;
    }

    public void setDistinct(int distinct) {
        this.distinct = distinct;
    }
    public int get(long[] key, long stampLong) {
        return super.get(key);
    }
     
    @Override
    public int get(long[] key) {
        long stamp = sl.tryOptimisticRead();
        int value = super.get(key);
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                value = super.get(key);
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return value;
    }

    @Override
    public int get(UUID key) {
        return this.get(new long[] {key.getMostSignificantBits(),
                                    key.getLeastSignificantBits()});
    }
    
    public int get(UUID key, long stampSequence) {
        return this.get(new long[] {key.getMostSignificantBits(),
                                    key.getLeastSignificantBits()},
                stampSequence);
    }
    
    @Override
    public boolean put(long[] key, int value) {
        throw new UnsupportedOperationException("Use put(long[] key, int value, long stamp) instead.");
    }

    public boolean put(long[] key, int value, long stamp) {
        sl.validate(stamp);
        return super.put(key, value);
    }

    public String getStats() {
        return "distinct: " + getDistinct() + " free: " + getFreeEntries()
                + " utilization: " + getDistinct() * 100 /(getDistinct() + getFreeEntries());
    }
}
