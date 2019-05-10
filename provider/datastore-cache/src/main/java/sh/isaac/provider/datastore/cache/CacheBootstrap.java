package sh.isaac.provider.datastore.cache;

import sh.isaac.model.collections.SpinedNidIntMap;

public interface CacheBootstrap {
    void loadAssemblageOfNid(SpinedNidIntMap nidToAssemblageNidMap);
}
