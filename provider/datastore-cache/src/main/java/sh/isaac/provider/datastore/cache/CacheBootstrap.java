package sh.isaac.provider.datastore.cache;

import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.SpinedNidIntMap;

public interface CacheBootstrap {
    void loadAssemblageOfNid(SpinedNidIntMap nidToAssemblageNidMap);

    /**
     *
     * @param assemblageNid the assemblage the taxonomy is for
     * @param taxonomyDataMap the taxonomy data for the assemblage
     */
    void loadTaxonomyData(int assemblageNid, SpinedIntIntArrayMap taxonomyDataMap);
}
