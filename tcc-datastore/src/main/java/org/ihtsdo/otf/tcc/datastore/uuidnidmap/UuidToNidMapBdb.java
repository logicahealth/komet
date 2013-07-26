/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.ihtsdo.otf.tcc.datastore.uuidnidmap;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.datastore.Bdb;
import org.ihtsdo.otf.tcc.datastore.ComponentBdb;
import org.ihtsdo.otf.tcc.datastore.temp.PrimordialId;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author kec
 */
public class UuidToNidMapBdb extends ComponentBdb {

    private static final String ID_NEXT = "org.ihtsdo.ID_NEXT";
    private IdSequence idSequence;
    ConcurrentHashMap<Byte, UuidToNidSoftMapSegment> readOnlyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Byte, UuidToNidSoftMapSegment> mutableMap = new ConcurrentHashMap<>();
    ReentrantLock generateLock = new ReentrantLock();

    //~--- constructors --------------------------------------------------------
    public UuidToNidMapBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
        super(readOnlyBdbEnv, mutableBdbEnv);
        idSequence = new IdSequence();
        for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
            readOnlyMap.put((byte) b, new UuidToNidSoftMapSegment(readOnly, (byte) b));
            mutableMap.put((byte) b, new UuidToNidSoftMapSegment(mutable, (byte) b));
        }
    }

    //~--- methods -------------------------------------------------------------
    private void addToDb(UUID key, int nid) {
        mutableMap.get((byte) key.hashCode()).put(key, nid);
    }

    private int generate(UUID key) {
        generateLock.lock();
        try {
            int nid = getNoGen(key);

            // if can't find, then generate new...
            if (nid == Integer.MIN_VALUE) {
                nid = idSequence.getAndIncrement();

                if (nid == 0) {
                    nid = idSequence.getAndIncrement();
                }

                addToDb(key, nid);
            }

            return nid;
        } finally {
            generateLock.unlock();
        }
    }

    public void put(UUID key, int nid) {
        mutableMap.get((byte) key.hashCode()).put(key, nid);
    }

    @Override
    public void sync() throws IOException {
        Bdb.setProperty(ID_NEXT, Integer.toString(idSequence.sequence.get()));

        for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
            readOnlyMap.get((byte) b).write();
            mutableMap.get((byte) b).write();
        }
        super.sync();
    }

    public int uuidToNid(UUID uuid) {
        return get(uuid);
    }

    public int uuidsToNid(Collection<UUID> uuids) {
        Collection<UUID> uuidsToAdd = new ArrayList<>(uuids.size());
        int nid = Integer.MIN_VALUE;
        for (UUID uuid : uuids) {
            int tempNid = getNoGen(uuid);

            if (tempNid == Integer.MIN_VALUE) {
                uuidsToAdd.add(uuid);
            } else {
                nid = tempNid;
            }
        }

        for (UUID uuid : uuidsToAdd) {
            if (nid == Integer.MIN_VALUE) {
                nid = generate(uuid);
            } else {
                addToDb(uuid, nid);
            }
        }

        return nid;
    }

    public int uuidsToNid(UUID[] uuids) {
        return uuidsToNid(Arrays.asList(uuids));
    }

    //~--- get methods ---------------------------------------------------------
    public int get(UUID key) {
        int nid = getNoGen(key);

        if (nid != Integer.MIN_VALUE) {
            return nid;
        }

        nid = generate(key);

        return nid;
    }

    public int getCurrentMaxNid() {
        return idSequence.sequence.get() - 1;
    }

    @Override
    protected String getDbName() {
        return "Uuid2NidBdb";
    }

    private int getNoGen(UUID key) {
        int nid = readOnlyMap.get((byte) key.hashCode()).getNid(key);
        if (nid != Integer.MAX_VALUE) {
            return nid;
        }

        nid = mutableMap.get((byte) key.hashCode()).getNid(key);
        if (nid != Integer.MAX_VALUE) {
            return nid;
        }

        return Integer.MIN_VALUE;
    }

    public List<UUID> getUuidsForNid(int nid) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean hasUuid(UUID uuid) {
        return getNoGen(uuid) != Integer.MIN_VALUE;
    }

    @Override
    protected void init() throws IOException {
        // nothing to do, using lazy initilization
    }

    //~--- inner classes -------------------------------------------------------
    private class IdSequence {

        private AtomicInteger sequence = new AtomicInteger(Integer.MIN_VALUE);

        //~--- constructors -----------------------------------------------------
        public IdSequence() throws IOException {
            super();

            String nextIdStr = Bdb.getProperty(ID_NEXT);

            if (nextIdStr == null) {
                int max = Integer.MIN_VALUE;

                for (PrimordialId primoridal : PrimordialId.values()) {
                    max = Math.max(max, primoridal.getNativeId());
                    sequence.set(max + 1);
                }

                Bdb.setProperty(ID_NEXT, Integer.toString(sequence.get()));
            } else {
                sequence = new AtomicInteger(Integer.decode(nextIdStr));
            }
        }

        public IdSequence(int nextId) throws IOException {
            super();
            sequence = new AtomicInteger(nextId);
            Bdb.setProperty(ID_NEXT, Integer.toString(sequence.get()));
        }

        //~--- get methods ------------------------------------------------------
        public final int getAndIncrement() {
            int next = sequence.getAndIncrement();

            return next;
        }
    }
}
