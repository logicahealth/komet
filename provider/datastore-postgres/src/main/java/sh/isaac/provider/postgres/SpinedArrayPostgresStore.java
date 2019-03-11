package sh.isaac.provider.postgres;

import sh.isaac.api.IsaacCache;
import sh.isaac.model.collections.SpinedByteArrayArrayMap;
import sh.isaac.model.collections.SpinedIntIntArrayMap;

import java.util.concurrent.ConcurrentHashMap;

public class SpinedArrayPostgresStore  {
    protected static final int DEFAULT_ELEMENTS_PER_SPINE = 1024;

    protected final int assembalgeNid;

    public SpinedArrayPostgresStore(int assembalgeNid) {
        this.assembalgeNid = assembalgeNid;
    }

    public final int sizeOnDisk() {
        return 0;
    }

    public final int getSpineCount() {
        return 0;
    }

    public final void writeSpineCount(int spineCount) {
        // Noop... We don't persist spines
    }
}
