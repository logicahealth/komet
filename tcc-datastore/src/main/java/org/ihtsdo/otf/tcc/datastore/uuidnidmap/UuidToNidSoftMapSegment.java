/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.datastore.uuidnidmap;

import com.sleepycat.bind.tuple.ByteBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.lang.ref.SoftReference;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Consider adding a BloomFilter is use case warrants it...
 * http://code.google.com/p/guava-libraries/wiki/HashingExplained#BloomFilter
 *
 * @author kec
 */
public class UuidToNidSoftMapSegment {

    private static UuidToIntHashMap.UuidToIntHashMapBinder uuidIntMapBinder =
            UuidToIntHashMap.getUuidIntMapBinder();
    private SoftReference<UuidToIntHashMap> mapSoftReference = new SoftReference<>(null);
    private UuidToIntHashMap mapHardReference;
    private ReentrantLock lock = new ReentrantLock();
    private Database database;
    private byte segment;

    public UuidToNidSoftMapSegment(Database database, byte segment) {
        this.database = database;
        this.segment = segment;
    }

    public boolean hasUuid(UUID uuid) {
        return getMap().containsKey(uuid);
    }

    public int getNid(UUID uuid) {
        return getMap().get(uuid);
    }

    public void put(UUID uuid, int nid) {
        if (mapHardReference != null) {
            mapHardReference = mapSoftReference.get();
        }
        if (mapHardReference == null) {
            lock.lock();
            try {
                mapHardReference = mapSoftReference.get();
                if (mapHardReference == null) {
                    mapHardReference = read();
                    mapSoftReference = new SoftReference<>(mapHardReference);
                }
            } finally {
                lock.unlock();
            }
        }
        mapHardReference.put(UuidUtil.convert(uuid), nid);
    }

    private UuidToIntHashMap read() {
        DatabaseEntry theKey = new DatabaseEntry();
        ByteBinding.byteToEntry(segment, theKey);

        DatabaseEntry theData = new DatabaseEntry();
        if (database.get(null, theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
            return uuidIntMapBinder.entryToObject(theData);
        }
        return new UuidToIntHashMap();
    }

    protected void write() {
        if (mapHardReference != null) {
            lock.lock();
            try {
                DatabaseEntry valueEntry = new DatabaseEntry();
                uuidIntMapBinder.objectToEntry(mapHardReference, valueEntry);
                DatabaseEntry theKey = new DatabaseEntry();

                ByteBinding.byteToEntry(segment, theKey);
                database.put(null, theKey, valueEntry);
                //System.out.println("For segment: " + segment + " wrote: " + mapHardReference.size() + " uuid maps.");
                mapHardReference = null;
            } finally {
                lock.unlock();
            }
        }
    }

    private UuidToIntHashMap getMap() {
        UuidToIntHashMap map = mapSoftReference.get();
        if (map == null) {
            lock.lock();
            try {
                map = mapSoftReference.get();
                if (map == null) {
                    map = read();
                    mapSoftReference = new SoftReference<>(map);
                }
            } finally {
                lock.unlock();
            }
        }
        return map;
    }
}
