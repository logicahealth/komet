package sh.isaac.model.collections.store;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceArray;


public interface ByteArrayArrayStore {

    Optional<AtomicReferenceArray<byte[][]>> get(int spineIndex);

    void put(int spineIndex, AtomicReferenceArray<byte[][]> spine);

    int sizeOnDisk();

    int getSpineCount();

    void writeSpineCount(int spineCount);

}
