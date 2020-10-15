package sh.isaac.provider.postgres;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.model.collections.store.ByteArrayArrayStore;
import sh.isaac.model.collections.store.ByteArrayArrayStoreProvider;

import jakarta.inject.Singleton;

@Service
@Singleton
public class ByteArrayArrayPostgresStoreProvider implements ByteArrayArrayStoreProvider {
    @Override
    public ByteArrayArrayStore get(int assemblageNid) {
        return new ByteArrayArrayPostgresStore(assemblageNid);
    }

}
