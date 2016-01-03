package gov.vha.isaac.ochre.api.collections;

import org.apache.mahout.math.function.IntObjectProcedure;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import gov.vha.isaac.ochre.api.DataSerializer;

/**
 * Created by kec on 12/18/14.
 */
public class ConcurrentIntObjectMap<T> {
    ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    OpenIntObjectHashMap<byte[]> map = new OpenIntObjectHashMap<>();

    DataSerializer<T> serializer;

    private boolean changed = false;

    public ConcurrentIntObjectMap(DataSerializer<T> serializer) {
        this.serializer = serializer;
    }

    public boolean containsKey(int key) {
        rwl.readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            rwl.readLock().unlock();
        }
    }

    public Optional<T> get(int key) {
        byte[] data;
        rwl.readLock().lock();
        try {
            data = map.get(key);
        } finally {
            rwl.readLock().unlock();
        }
        if (data == null) {
            return Optional.empty();
        }
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            return Optional.of(serializer.deserialize(dis));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean put(int key, T value) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            serializer.serialize(new DataOutputStream(baos), value);
            changed = true;
            rwl.writeLock().lock();
            try {
                return map.put(key, baos.toByteArray());
            } finally {
                rwl.writeLock().unlock();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int size() {
        return map.size();
    }

    public boolean forEachPair(IntObjectProcedure<T> procedure) {

        map.forEachPair((int first, byte[] data) -> {
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
                return procedure.apply(first, serializer.deserialize(dis));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return true;
    }

}
