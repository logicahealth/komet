package sh.isaac.provider.postgres;

import sh.isaac.model.collections.store.ByteArrayArrayStore;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ByteArrayArrayPostgresStore extends SpinedArrayPostgresStore implements ByteArrayArrayStore {

    public ByteArrayArrayPostgresStore(int assembalgeNid) {
        super(assembalgeNid);
    }

    @Override
    public Optional<AtomicReferenceArray<byte[][]>> get(int spineIndex) {
        AtomicReferenceArray<byte[][]> data = new AtomicReferenceArray<>(DEFAULT_ELEMENTS_PER_SPINE);
        return Optional.of(data);
    }

    @Override
    public void put(int spineIndex, AtomicReferenceArray<byte[][]> spine) {
        // Noop we don't persist spines, the database persists the individual items.
    }
}
