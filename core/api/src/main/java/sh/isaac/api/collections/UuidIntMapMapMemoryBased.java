package sh.isaac.api.collections;

import sh.isaac.api.collections.uuidnidmap.ConcurrentUuidToIntHashMap;

public class UuidIntMapMapMemoryBased extends UuidIntMapMap {

    protected final ConcurrentUuidToIntHashMap[] maps = new ConcurrentUuidToIntHashMap[NUMBER_OF_MAPS];
    {
        for (int i = 0; i < NUMBER_OF_MAPS; i++) {
            maps[i] = new ConcurrentUuidToIntHashMap(DEFAULT_MAP_SIZE,
                    MIN_LOAD_FACTOR,
                    MAX_LOAD_FACTOR);
        }
    }


    protected ConcurrentUuidToIntHashMap getMap(int index)
            throws RuntimeException {
        return maps[index];
    }

    @Override
    public int getDiskSpaceUsed() {
        return 0;
    }

    @Override
    public int getMemoryInUse() {
        int memoryInUse = 0;
        for (ConcurrentUuidToIntHashMap map : maps) {
            if (map != null) {
                memoryInUse += map.getMemoryInUse();
            }
        }
        return memoryInUse;
    }

    protected void mapElementUpdated(int mapIndex) {
        // nothing to do...;
    }

}
