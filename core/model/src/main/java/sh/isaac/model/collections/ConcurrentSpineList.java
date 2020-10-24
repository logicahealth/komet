package sh.isaac.model.collections;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConcurrentSpineList<E extends Object> {
    private static final Logger LOG = LogManager.getLogger();
    private final int incrementSize = 4096;
    private final AtomicReference<AtomicReferenceArray<E>> spineArrayReference = new AtomicReference<>();
    private final Supplier<E> supplier;

    public ConcurrentSpineList(int size, Supplier<E> supplier) {
        this.spineArrayReference.set(new AtomicReferenceArray<>(size));
        this.supplier = supplier;
    }

    public ConcurrentSpineList(E[] elements, Supplier<E> supplier) {
        this.spineArrayReference.set(new AtomicReferenceArray<>(elements));
        this.supplier = supplier;
    }

    public E getSpine(int spineIndex) {
        AtomicReferenceArray<E> spineArray = spineArrayReference.get();
        if (spineIndex < spineArray.length()) {
            E spine = spineArray.get(spineIndex);
            if (spine != null) {
                return spine;
            }
            spineArrayReference.get().compareAndExchange(spineIndex, null, supplier.get());
            return spineArrayReference.get().get(spineIndex);
        }
        // need to grow array
        // need to add spine to array
        growArray(spineIndex);
        return getSpine(spineIndex);
    }

    private void growArray(int spineIndex) {
        AtomicReferenceArray<E> spineArray = spineArrayReference.get();
        if (spineIndex >= spineArray.length()) {
            AtomicReferenceArray<E> newSpineArray = new AtomicReferenceArray<>(spineIndex + incrementSize);
            for (int i = 0; i < spineArray.length(); i++) {
                newSpineArray.set(i, spineArray.get(i));
            }
            spineArrayReference.compareAndSet(spineArray, newSpineArray);
        }
    }

    public void setSpine(int spineIndex, E spine) {
        AtomicReferenceArray<E> spineArray = spineArrayReference.get();
        if (spineIndex >= spineArray.length()) {
            LOG.debug("Growing for length: " + spineIndex);
            growArray(spineIndex);
            spineArray = spineArrayReference.get();
            LOG.debug("new length: " + spineArray.length());
        }
        spineArray.set(spineIndex, spine);
    }

    public int getSpineCount() {
        if (spineArrayReference.get() == null) {
            return 0;
        }
        return spineArrayReference.get().length();
    }

    public void clear() {
        spineArrayReference.set(new AtomicReferenceArray<>(0));
    }

    public AtomicReferenceArray<E> getSpines() {
        return spineArrayReference.get();
    }
}
