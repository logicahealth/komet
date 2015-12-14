package gov.vha.isaac.ochre.api.memory;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by kec on 4/10/15.
 */
public class HoldInMemoryCache {

    private static final int CACHE_SIZE = 512;
    private static final int GENERATIONS = 3;
    private static final LinkedBlockingDeque<Set<MemoryManagedReference>> queue = new LinkedBlockingDeque();

    private static final AtomicReference<Set<MemoryManagedReference>> cacheRef = new AtomicReference<>(new ConcurrentSkipListSet<>());
    private static final AtomicInteger cacheCount = new AtomicInteger();

    public static void addToCache(MemoryManagedReference newRef) {

        Set<MemoryManagedReference> cache = cacheRef.get();
        if (!cache.contains(newRef)) {
            newRef.cacheEntry();
            cache.add(newRef);
            int count = cacheCount.incrementAndGet();
            if (count > CACHE_SIZE) {
                if (cacheRef.compareAndSet(cache, new ConcurrentSkipListSet<>())) {
                    queue.addFirst(cache);
                    while (queue.size() > GENERATIONS) {
                        Set<MemoryManagedReference> oldCache = queue.removeLast();
                        oldCache.stream().forEach(memoryManagedReference -> {
                            memoryManagedReference.cacheExit();
                        });
                    }
                }
            }
        }

    }

    public static void clearCache() {
        queue.stream().forEach((Set<MemoryManagedReference> referenceSet)-> {
            referenceSet.stream().forEach((MemoryManagedReference ref) -> {
                ref.cacheExit();
                referenceSet.remove(ref);
            });
        }); 
    }
}
