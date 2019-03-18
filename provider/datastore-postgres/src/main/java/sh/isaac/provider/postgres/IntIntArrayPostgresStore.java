package sh.isaac.provider.postgres;

import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.store.IntIntArrayStore;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class IntIntArrayPostgresStore extends SpinedArrayPostgresStore implements IntIntArrayStore {

    public IntIntArrayPostgresStore(int assembalgeNid) {
        super(assembalgeNid);
    }

    @Override
    public Optional<AtomicReferenceArray<int[]>> get(int spineIndex) {
        AtomicReferenceArray<int[]> data = new AtomicReferenceArray<>(DEFAULT_ELEMENTS_PER_SPINE);
        return Optional.of(data);
    }

    @Override
    public void put(int spineIndex, AtomicReferenceArray<int[]> spine) {
        // Noop we don't persist spines, the database persists the individual items.
    }

}
