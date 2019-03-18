package sh.isaac.provider.postgres;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.IsaacCache;
import sh.isaac.model.collections.store.IntIntArrayStore;
import sh.isaac.model.collections.store.IntIntArrayStoreProvider;

import javax.inject.Singleton;

@Service
@Singleton
public class IntIntArrayPostgresStoreProvider implements IntIntArrayStoreProvider {
    @Override
    public IntIntArrayStore get(int assemblageNid) {
        return new IntIntArrayPostgresStore(assemblageNid);
    }

}
