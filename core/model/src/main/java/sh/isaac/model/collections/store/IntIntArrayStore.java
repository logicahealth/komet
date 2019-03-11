package sh.isaac.model.collections.store;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceArray;

public interface IntIntArrayStore {
    Optional<AtomicReferenceArray<int[]>> get(int spineIndex);

    void put(int spineIndex, AtomicReferenceArray<int[]> spine);

    int sizeOnDisk();

    int getSpineCount();

    void writeSpineCount(int spineCount);
}
