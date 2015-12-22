package gov.vha.isaac.ochre.api.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by kec on 5/29/15.
 *
 * @param <K> key type for this cache
 * @param <V> key value for this cache
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {

    /**
     *
     */
    private static final long serialVersionUID = -2584554176457193968L;

    private final int capacity; // Maximum number of items in the cache.

    public LruCache(int capacity) {
        super(capacity + 1, 1.0f, true); // Pass 'true' for accessOrder.
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
        return (size() > this.capacity);
    }
}
