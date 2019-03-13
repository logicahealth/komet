package sh.isaac.model.collections.store;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class IntIntArrayNoStore implements IntIntArrayStore {
    @Override
    public Optional<AtomicReferenceArray<int[]>> get(int spineIndex) {
        throw new UnsupportedOperationException("Persistence is not supported");
    }

    @Override
    public void put(int spineIndex, AtomicReferenceArray<int[]> spine) {
        throw new UnsupportedOperationException("Persistence is not supported");
    }

    @Override
    public int sizeOnDisk() {
        throw new UnsupportedOperationException("Persistence is not supported");
    }

    @Override
    public int getSpineCount() {
        throw new UnsupportedOperationException("Persistence is not supported");
    }

    @Override
    public void writeSpineCount(int spineCount) {
        throw new UnsupportedOperationException("Persistence is not supported");
    }
}
