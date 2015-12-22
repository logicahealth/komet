package gov.vha.isaac.ochre.api.collections;


import gov.vha.isaac.ochre.api.DataSerializer;
import gov.vha.isaac.ochre.api.collections.uuidnidmap.ConcurrentUuidToIntHashMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by kec on 7/20/14.
 */
public class ConcurrentUuidIntMapSerializer implements DataSerializer<ConcurrentUuidToIntHashMap>, Serializable {

    @Override
    public void serialize(DataOutput out, ConcurrentUuidToIntHashMap map) {
        try {
            out.writeInt(map.size());
            map.forEachPair((long[] uuid, int nid) -> {
                try {
                    out.writeLong(uuid[0]);
                    out.writeLong(uuid[1]);
                    out.writeInt(nid);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return true;
            });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public ConcurrentUuidToIntHashMap deserialize(DataInput input)  {
        try {
            int size = input.readInt();
            
            ConcurrentUuidToIntHashMap map = new ConcurrentUuidToIntHashMap(size);
            long stamp = map.getStampedLock().writeLock();
            try {
            long[] uuidData = new long[2];
            for (int i = 0; i < size; i++) {
                uuidData[0] = input.readLong();
                uuidData[1] = input.readLong();
                int nid = input.readInt();
                map.put(uuidData, nid, stamp);
            }
            } finally {
                map.getStampedLock().unlockWrite(stamp);
            }
            
            return map;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
