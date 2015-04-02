/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.model;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.ChronicledObjectLocal;
import gov.vha.isaac.ochre.api.commit.CommitManager;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 * @param <V>
 */
public abstract class ObjectChronicleImpl<V extends ObjectVersionImpl> implements ChronicledObjectLocal<V>,
        WaitFreeComparable {

    private static CommitManager commitManager;

    private static CommitManager getCommitManager() {
        if (commitManager == null) {
            commitManager = LookupService.getService(CommitManager.class);
        }
        return commitManager;
    } 

    private static final StampedLock[] stampedLocks = new StampedLock[256];

    static {
        for (int i = 0; i < stampedLocks.length; i++) {
            stampedLocks[i] = new StampedLock();
        }
    }

    protected static StampedLock getLock(int key) {
        return stampedLocks[((int) ((byte) key)) - Byte.MIN_VALUE];
    }

    private int writeSequence;
    private final long primordialUuidMsb;
    private final long primordialUuidLsb;
    private final int nid;
    private final int containerSequence;

    private int versionStartPosition;

    private byte[] writtenData;

    private ConcurrentSkipListMap<Integer, V> unwrittenData;

    private SoftReference<ArrayList<V>> versionListReference;

    public ObjectChronicleImpl(UUID primoridalUuid, int nid, int containerSequence) {
        this.writeSequence = Integer.MIN_VALUE;
        this.primordialUuidMsb = primoridalUuid.getMostSignificantBits();
        this.primordialUuidLsb = primoridalUuid.getLeastSignificantBits();
        this.nid = nid;
        this.containerSequence = containerSequence;
    }

    public ObjectChronicleImpl(DataBuffer data) {
        this.writeSequence = data.getInt();
        this.writtenData = data.getData();
        this.primordialUuidMsb = data.getLong();
        this.primordialUuidLsb = data.getLong();
        this.nid = data.getInt();
        this.containerSequence = data.getInt();
        constructorEnd(data);
    }

    protected void writeChronicleData(DataBuffer data) {
        data.putInt(writeSequence);
        data.putLong(primordialUuidMsb);
        data.putLong(primordialUuidLsb);
        data.putInt(nid);
        data.putInt(containerSequence);
    }

    protected final void constructorEnd(DataBuffer data) {
        versionStartPosition = data.getPosition();
    }

    public void addVersion(V version) {
        if (unwrittenData == null) {
            long lockStamp = getLock(nid).writeLock();
            try {
                unwrittenData = new ConcurrentSkipListMap<>();
            } finally {
                getLock(nid).unlockWrite(lockStamp);
            }
        }
        unwrittenData.put(version.getStampSequence(), version);
        versionListReference = null;
    }
    

    public byte[] getDataToWrite(int writeSequence) {
        return mergeData(writeSequence, null);
    }
    
    public byte[] mergeData(int writeSequence, byte[] dataToMerge) {
        setWriteSequence(writeSequence);
        if (unwrittenData == null) {
            if (writtenData != null) {
                DataBuffer db = new DataBuffer(writtenData);
                db.putInt(writeSequence);
                return db.getData();
            }
            DataBuffer db = new DataBuffer(10);
            writeChronicleData(db);
            db.putInt(0);
            db.trimToSize();
            return db.getData();
        }

        DataBuffer db = new DataBuffer(512);
        writeChronicleData(db);
        OpenIntHashSet writtenStamps = new OpenIntHashSet(11);
        unwrittenData.values().forEach((version) -> {
            int stampSequenceForVersion = version.getStampSequence();
            if (getCommitManager().isNotCanceled(stampSequenceForVersion)) {
                writtenStamps.add(stampSequenceForVersion);
                int startWritePosition = db.getPosition();
                db.putInt(0); // placeholder for length
                version.writeVersionData(db);
                int versionLength = db.getPosition() - startWritePosition;
                db.setPosition(startWritePosition);
                db.putInt(versionLength);
                db.setPosition(db.getLimit());
            }
        });
        if (writtenData != null) {
            mergeData(writtenData, writtenStamps, db);
        }
        if (dataToMerge != null) {
             mergeData(dataToMerge, writtenStamps, db);
        }
        db.putInt(0); // last data is a zero length version record
        db.trimToSize();
        return db.getData();
    }

    protected void mergeData(byte[] dataToMerge, OpenIntHashSet writtenStamps, DataBuffer db) {
        DataBuffer writtenBuffer = new DataBuffer(dataToMerge);
        int nextPosition = versionStartPosition;
        while (nextPosition < writtenBuffer.getLimit()) {
            writtenBuffer.setPosition(nextPosition);
            int versionLength = writtenBuffer.getInt();
            int stampSequenceForVersion = writtenBuffer.getInt();
            if ((!writtenStamps.contains(stampSequenceForVersion))
                    && getCommitManager().isNotCanceled(stampSequenceForVersion)) {
                writtenStamps.add(stampSequenceForVersion);
                db.append(writtenBuffer, nextPosition, versionLength);
            }
            nextPosition = nextPosition + versionLength;
        }
    }

    public Optional<V> getVersionForStamp(int stampSequence) {
        if (unwrittenData != null && unwrittenData.containsKey(stampSequence)) {
            return Optional.of(unwrittenData.get(stampSequence));
        }
        DataBuffer bb = new DataBuffer(writtenData);
        bb.setPosition(versionStartPosition);
        int nextPosition = bb.getPosition();
        while (nextPosition < bb.getLimit()) {
            int versionLength = bb.getInt();
            nextPosition = nextPosition + versionLength;
            int stampSequenceForVersion = bb.getInt();
            if (stampSequence == stampSequenceForVersion) {
                return Optional.of(makeVersion(stampSequence, bb));
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<V> getVersions() {
        ArrayList<V> results = null;
        if (versionListReference != null) {
            results = versionListReference.get();
        }
        while (results == null) {
            results = new ArrayList<>();
            DataBuffer bb = new DataBuffer(writtenData);
            bb.setPosition(versionStartPosition);
            makeVersions(bb, results);
            if (unwrittenData != null) {
                results.addAll(unwrittenData.values());
            }
            versionListReference = new SoftReference<>(results);
        }
        return results;
    }

    protected void makeVersions(DataBuffer bb, ArrayList<V> results) {
        int nextPosition = bb.getPosition();
        while (nextPosition < bb.getLimit()) {
            int versionLength = bb.getInt();
            if (versionLength > 0) {
                nextPosition = nextPosition + versionLength;
                int stampSequence = bb.getInt();
                if (stampSequence >= 0) {
                    results.add(makeVersion(stampSequence, bb));
                }
            }
        }
    }

    protected abstract V makeVersion(int stampSequence, DataBuffer bb);

    @Override
    public IntStream getVersionStampSequences() {
        IntStream.Builder builder = IntStream.builder();
        DataBuffer bb = new DataBuffer(writtenData);
        getVersionStampSequences(versionStartPosition, bb, builder);
        if (unwrittenData != null) {
            unwrittenData.keySet().forEach((stamp) -> builder.accept(stamp));
        }
        return builder.build();
    }

    protected void getVersionStampSequences(int index, DataBuffer bb, IntStream.Builder builder) {
        while (index < bb.getLimit()) {
            int versionLength = bb.getInt();
            int stampSequence = bb.getInt();
            builder.accept(stampSequence);
            index = index + versionLength;
        }
    }

    @Override
    public int getNid() {
        return nid;
    }

    @Override
    public int getWriteSequence() {
        return writeSequence;
    }

    @Override
    public void setWriteSequence(int writeSequence) {
        this.writeSequence = writeSequence;
    }

    public int getContainerSequence() {
        return containerSequence;
    }
    
    
    @Override
    public UUID getPrimordialUuid() {
        return new UUID(primordialUuidMsb, primordialUuidLsb);
    }

    @Override
    public List<UUID> getUUIDs() {
        return Arrays.asList(new UUID[] {getPrimordialUuid()});
    }

}
