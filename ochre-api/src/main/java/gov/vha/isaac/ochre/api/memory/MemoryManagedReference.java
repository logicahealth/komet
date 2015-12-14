package gov.vha.isaac.ochre.api.memory;

import org.apache.mahout.math.map.HashFunctions;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import gov.vha.isaac.ochre.api.DataSerializer;

/**
 * Created by kec on 4/10/15.
 * @param <T>
 */
public class MemoryManagedReference<T extends Object> extends SoftReference<T> implements
        Comparable<MemoryManagedReference> {

    private static final AtomicInteger objectIdSupplier = new AtomicInteger();
    private static final AtomicInteger referenceSequenceSupplier = new AtomicInteger(Integer.MIN_VALUE + 1);

    private final int objectId = objectIdSupplier.getAndIncrement();

    private int lastWriteToDiskSequence = referenceSequenceSupplier.getAndIncrement();
    private long lastWriteToDiskTime = System.currentTimeMillis();
    
    private int  lastElementUpdateSequence = Integer.MIN_VALUE;
    private long lastElementUpdateTime = System.currentTimeMillis();
    
    private long lastElementReadTime = Long.MIN_VALUE;

    private final AtomicReference<T> strongReferenceForUpdate = new AtomicReference<>();
    private final AtomicReference<T> strongReferenceForCache= new AtomicReference<>();

    private final LongAdder hits = new LongAdder();
    private final AtomicInteger cacheCount = new AtomicInteger();

    private final File diskLocation;
    private final DataSerializer<T> serializer;

    public MemoryManagedReference(T referent, File diskLocation, DataSerializer<T> serializer) {
        super(referent);
        this.diskLocation = diskLocation;
        this.serializer = serializer;
    }

    public MemoryManagedReference(T referent, ReferenceQueue<? super T> q, File diskLocation,
                                  DataSerializer<T> serializer) {
        super(referent, q);
        this.diskLocation = diskLocation;
        this.serializer = serializer;
    }

    public void elementUpdated() {
        this.strongReferenceForUpdate.set(this.get());
        lastElementUpdateSequence = referenceSequenceSupplier.getAndIncrement();
        lastElementUpdateTime = System.currentTimeMillis();
    }

    public void elementRead() {
        hits.increment();
        this.lastElementReadTime = System.currentTimeMillis();
    }

    public Duration timeSinceLastRead() {
        return Duration.ofMillis(System.currentTimeMillis() - lastElementReadTime);
    }

    public void cacheEntry() {
        int count = cacheCount.incrementAndGet();
        if (count == 1) {
            strongReferenceForCache.set(this.get());
        }
    }

    public void cacheExit() {
        int count = cacheCount.decrementAndGet();
        if (count == 0) {
            strongReferenceForCache.set(null);
        }
    }

    public void write() {
        T objectToWrite = strongReferenceForUpdate.get();
        if (objectToWrite != null) {
            strongReferenceForUpdate.set(null);
            DiskSemaphore.acquire();
            lastWriteToDiskSequence = referenceSequenceSupplier.getAndIncrement();
            lastWriteToDiskTime = System.currentTimeMillis();
            diskLocation.getParentFile().mkdirs();
            try(DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(diskLocation)))) {
                serializer.serialize(out, objectToWrite);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                DiskSemaphore.release();
            }
        }
    }

    public boolean hasUnwrittenUpdate() {
        return lastWriteToDiskSequence < lastElementUpdateSequence;
    }
    
    public long msSinceLastUnwrittenUpdate() {
        return lastElementUpdateTime - lastWriteToDiskTime;
    }

    public long getLastWriteToDiskTime() {
        return lastWriteToDiskTime;
    }

    @Override
    public int compareTo(MemoryManagedReference o) {
        return this.objectId - o.objectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemoryManagedReference<?> that = (MemoryManagedReference<?>) o;

        return objectId == that.objectId;
    }

    @Override
    public int hashCode() {
        return HashFunctions.hash(objectId);
    }
}
