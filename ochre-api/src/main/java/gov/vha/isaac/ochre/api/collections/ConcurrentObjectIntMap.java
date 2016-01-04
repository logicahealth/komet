package gov.vha.isaac.ochre.api.collections;

import org.apache.mahout.math.map.OpenObjectIntHashMap;

import java.util.OptionalInt;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ObjIntConsumer;
import org.apache.mahout.math.function.ObjectIntProcedure;

/**
 * Created by kec on 12/18/14.
 * @param <T> Type of object in map. 
 */
public class ConcurrentObjectIntMap<T> {
    ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    OpenObjectIntHashMap<T> backingMap = new OpenObjectIntHashMap<>();

    public void forEachPair(ObjIntConsumer<T> consumer) {
        backingMap.forEachPair((T first, int second) -> {
            consumer.accept(first, second);
            return true;
        });
        
    }

    public boolean containsKey(T key) {
        rwl.readLock().lock();
        try {
            return backingMap.containsKey(key);
        } finally {
            rwl.readLock().unlock();
        }
    }

    public OptionalInt get(T key) {
        int value;
        rwl.readLock().lock();
        try {
            if (backingMap.containsKey(key)) {
                return OptionalInt.of(backingMap.get(key));
            }
            return OptionalInt.empty();
        } finally {
            rwl.readLock().unlock();
        }
    }

    public boolean put(T key, int value) {

        rwl.writeLock().lock();
        try {
            return backingMap.put(key, value);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public int size() {
        return backingMap.size();
    }

}
