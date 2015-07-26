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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.dag.Graph;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePosition;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import gov.vha.isaac.ochre.collections.StampSequenceSet;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 * @param <V>
 */
public abstract class ObjectChronologyImpl<V extends ObjectVersionImpl>
        implements ObjectChronology<V>, WaitFreeComparable {

    private static final StampedLock[] stampedLocks = new StampedLock[256];

    static {
        for (int i = 0; i < stampedLocks.length; i++) {
            stampedLocks[i] = new StampedLock();
        }
    }

    protected static StampedLock getLock(int key) {
        return stampedLocks[((int) ((byte) key)) - Byte.MIN_VALUE];
    }

    /**
     * The write sequence is incremented each time data is written, and provides
     * a check to see if this chronicle has had any changes written since the
     * data for this chronicle was read. If the write sequence does not match
     * the write sequences in the persistence storage, the data needs to be
     * merged prior to writing, according to the principles of a
     * {@code WaitFreeComparable} object.
     */
    private int writeSequence;
    /**
     * Primordial uuid most significant bits for this component
     */
    private final long primordialUuidMsb;
    /**
     * Primoridal uuid least significant bits for this component
     */
    private final long primordialUuidLsb;
    /**
     * additional uuid most and least significant bits for this component
     */
    protected long[] additionalUuidParts;
    /**
     * Native identifier of this component
     */
    private final int nid;
    /**
     * Concept sequence if a concept or description, Sememe sequence if a
     * sememe.
     */
    private final int containerSequence;

    /**
     * Counter to give versions unique sequences within the chronicle.
     */
    private short versionSequence = 0;
    /**
     * Position in the data where chronicle data ends, and version data starts
     */
    private int versionStartPosition;

    /**
     * Data previously persisted. Used for lazy instantiation of versions and
     * objects that are part of this chronicle.
     */
    private byte[] writtenData;

    /**
     * Data that has not yet been persisted. This data will need to be merged
     * with the written data when the chronicle is next serialized.
     */
    private ConcurrentSkipListMap<Integer, V> unwrittenData;

    /**
     * Version data is stored in a soft reference after lazy instantiation, to
     * minimize unnecessary memory utilization.
     */
    private SoftReference<ArrayList<V>> versionListReference;

    /**
     * For constructing an object for the first time.
     *
     * @param primoridalUuid A unique external identifier for this chronicle
     * @param nid A unique internal identifier, that is only valid within this
     * database
     * @param containerSequence Either a concept sequence or a sememe sequence
     * depending on the ofType of the underlying object.
     */
    protected ObjectChronologyImpl(UUID primoridalUuid, int nid,
            int containerSequence) {
        this.writeSequence = Integer.MIN_VALUE;
        this.primordialUuidMsb = primoridalUuid.getMostSignificantBits();
        this.primordialUuidLsb = primoridalUuid.getLeastSignificantBits();
        this.nid = nid;
        this.containerSequence = containerSequence;
    }

    /**
     * For reconstituting an object previously serialized.
     *
     * @param data The data from which to reconstitute this chronicle.
     */
    protected ObjectChronologyImpl(DataBuffer data) {
        this.writeSequence = data.getInt();
        this.writtenData = data.getData();
        this.primordialUuidMsb = data.getLong();
        this.primordialUuidLsb = data.getLong();
        int additionalUuidPartsSize = data.getInt();
        if (additionalUuidPartsSize > 0) {
            additionalUuidParts = new long[additionalUuidPartsSize];
            for (int i = 0; i < additionalUuidPartsSize; i++) {
                additionalUuidParts[i] = data.getLong();
            }
        }

        this.nid = data.getInt();
        this.containerSequence = data.getInt();
        this.versionSequence = data.getShort();
        constructorEnd(data);
    }

    protected void writeChronicleData(DataBuffer data) {
        data.putInt(writeSequence);
        data.putLong(primordialUuidMsb);
        data.putLong(primordialUuidLsb);
        if (additionalUuidParts == null) {
            data.putInt(0);
        } else {
            data.putInt(additionalUuidParts.length);
            LongStream.of(additionalUuidParts).forEach(
                    (uuidPart) -> data.putLong(uuidPart));
        }
        data.putInt(nid);
        data.putInt(containerSequence);
        data.putShort(versionSequence);
    }

    protected short nextVersionSequence() {
        return versionSequence++;
    }

    /**
     * Stores the location where the chronicle data ends, and the version data
     * starts.
     *
     * @param data the buffer from which to derive the location data.
     */
    protected final void constructorEnd(DataBuffer data) {
        versionStartPosition = data.getPosition();
    }

    /**
     * Overwrites existing versions. Use to remove duplicates, etc. Deliberately
     * not advertised in standard API, as this call may lose audit data.
     *
     * @param versions
     */
    public void setVersions(Collection<V> versions) {
        if (unwrittenData != null) {
            unwrittenData.clear();
        }
        // reset written data
        writtenData = null;
        versions.forEach((V version) -> addVersion(version));
    }

    /**
     * Use to add a new version to the chronicle.
     *
     * @param version the version to add
     */
    protected void addVersion(V version) {
        if (unwrittenData == null) {
            long lockStamp = getLock(nid).writeLock();
            try {
                unwrittenData = new ConcurrentSkipListMap<>();
            } finally {
                getLock(nid).unlockWrite(lockStamp);
            }
        }
        unwrittenData.put(version.getStampSequence(), version);
        // invalidate the version reference list, it will be reconstructed with the new version
        // added if requested via a call to versionStream();
        versionListReference = null;
    }

    /**
     * Get data to write to datastore, use the writeSequence as it was
     * originally read from the database.
     *
     * @return the data to write
     */
    public byte[] getDataToWrite() {
        return getDataToWrite(this.writeSequence);
    }

    /**
     * Get data to write to datastore. Set the write sequence to the specified
     * value
     *
     * @param writeSequence the write sequence to prepend to the data
     * @return the data to write
     */
    public byte[] getDataToWrite(int writeSequence) {
        setWriteSequence(writeSequence);
        if (unwrittenData == null) {
            // no changes, so nothing to merge. 
            if (writtenData != null) {
                DataBuffer db = new DataBuffer(writtenData);
                db.putInt(writeSequence);
                return db.getData();
            }
            // creating a brand new object
            DataBuffer db = new DataBuffer(10);
            writeChronicleData(db);
            db.putInt(0);
            db.trimToSize();
            return db.getData();
        }
        DataBuffer db = new DataBuffer(512);
        writeChronicleData(db);
        // add versions..
        unwrittenData.values().forEach((version) -> {
            int stampSequenceForVersion = version.getStampSequence();
            if (Get.commitService().isNotCanceled(stampSequenceForVersion)) {
                int startWritePosition = db.getPosition();
                db.putInt(0); // placeholder for length
                version.writeVersionData(db);
                int versionLength = db.getPosition() - startWritePosition;
                db.setPosition(startWritePosition);
                db.putInt(versionLength);
                db.setPosition(db.getLimit());
            }
        });

        db.putInt(0); // last data is a zero length version record
        db.trimToSize();
        return db.getData();
    }

    /**
     * Merge this data, with data from another source to integrate into a single
     * data sequence
     *
     * @param writeSequence the write sequence to use for the merged data
     * @param dataToMerge data from another source to integrate with this data
     * @return the merged data
     */
    public byte[] mergeData(int writeSequence, byte[] dataToMerge) {
        setWriteSequence(writeSequence);
        DataBuffer db = new DataBuffer(512);
        writeChronicleData(db);
        OpenIntHashSet writtenStamps = new OpenIntHashSet(11);
        if (unwrittenData != null) {
            unwrittenData.values().forEach((version) -> {
                int stampSequenceForVersion = version.getStampSequence();
                if (Get.commitService().isNotCanceled(stampSequenceForVersion)) {
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
        }

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

    protected void mergeData(byte[] dataToMerge,
            OpenIntHashSet writtenStamps, DataBuffer db) {
        DataBuffer writtenBuffer = new DataBuffer(dataToMerge);
        int nextPosition = versionStartPosition;
        while (nextPosition < writtenBuffer.getLimit()) {
            writtenBuffer.setPosition(nextPosition);
            int versionLength = writtenBuffer.getInt();
            if (versionLength > 0) {
                int stampSequenceForVersion = writtenBuffer.getInt();
                if ((!writtenStamps.contains(stampSequenceForVersion))
                        && Get.commitService().isNotCanceled(stampSequenceForVersion)) {
                    writtenStamps.add(stampSequenceForVersion);
                    db.append(writtenBuffer, nextPosition, versionLength);
                }
                nextPosition = nextPosition + versionLength;
            } else {
                nextPosition = writtenBuffer.getLimit();
            }
        }
    }

    /**
     *
     * @return a list of all versions contained in this chronicle.
     */
    @Override
    public List<? extends V> getVersionList() {
        ArrayList<V> results = null;
        if (versionListReference != null) {
            results = versionListReference.get();
        }
        while (results == null) {
            results = new ArrayList<>();
            if (writtenData != null) {
                DataBuffer bb = new DataBuffer(writtenData);
                bb.setPosition(versionStartPosition);
                makeVersions(bb, results);
            }
            if (unwrittenData != null) {
                results.addAll(unwrittenData.values());
            }
            versionListReference = new SoftReference<>(results);
        }
        return results;
    }

    /**
     * Used to retrieve a single version, without creating all version objects
     * and storing them in a version list.
     *
     * @param stampSequence the stamp sequence that specifies a particular
     * version
     * @return the version with the corresponding stamp sequence
     */
    public Optional<V> getVersionForStamp(int stampSequence) {
        if (versionListReference != null) {
            List<V> versions = versionListReference.get();
            if (versions != null) {
                for (V v : versions) {
                    if (v.getStampSequence() == stampSequence) {
                        return Optional.of(v);
                    }
                }
            }
        }
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
            bb.setPosition(nextPosition);
        }
        return Optional.empty();
    }

    /**
     * Reconstitutes version objects previously serialized.
     *
     * @param bb the byte buffer containing previously written data
     * @param results list of the reconstituted version objects
     */
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
            } else {
                nextPosition = Integer.MAX_VALUE;
            }
        }
    }

    /**
     * Call to subclass to read data from the data buffer, and create the
     * corresponding version object. The subclass is not responsible to add the
     * version to the version list, that task is performed by the calling method
     * ({@code maveVersions}).
     *
     * @param stampSequence the stamp sequence for this version
     * @param bb the data buffer
     * @return the version object
     */
    protected abstract V makeVersion(int stampSequence, DataBuffer bb);

    /**
     *
     * @return a stream of the stampSequences for each version of this
     * chronology.
     */
    @Override
    public IntStream getVersionStampSequences() {
        IntStream.Builder builder = IntStream.builder();
        List<V> versions = null;
        if (versionListReference != null) {
            versions = versionListReference.get();
        }
        if (versions != null) {
            versions.forEach((version) -> builder.accept(version.getStampSequence()));
        } else if (writtenData != null) {
            DataBuffer bb = new DataBuffer(writtenData);
            getVersionStampSequences(versionStartPosition, bb, builder);
        }
        if (unwrittenData != null) {
            unwrittenData.keySet().forEach((stamp) -> builder.accept(stamp));
        }
        return builder.build();
    }

    @Override
    public CommitStates getCommitState() {
        if (getVersionStampSequences().anyMatch((stampSequence)
                -> Get.commitService().isUncommitted(stampSequence))) {
            return CommitStates.UNCOMMITTED;
        }
        return CommitStates.COMMITTED;
    }

    protected void getVersionStampSequences(int index, DataBuffer bb,
            IntStream.Builder builder) {
        int limit = bb.getLimit();
        while (index < limit) {
            bb.setPosition(index);
            int versionLength = bb.getInt();
            if (versionLength > 0) {
                int stampSequence = bb.getInt();
                builder.accept(stampSequence);
                index = index + versionLength;
            } else {
                index = Integer.MAX_VALUE;
            }
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
    public List<UUID> getUuidList() {
        List<UUID> uuids = new ArrayList();
        uuids.add(getPrimordialUuid());
        if (additionalUuidParts != null) {
            for (int i = 0; i < additionalUuidParts.length; i = i + 2) {
                uuids.add(
                        new UUID(additionalUuidParts[i], additionalUuidParts[i + 1]));
            }
        }
        return uuids;
    }

    public void setAdditionalUuids(List<UUID> uuids) {
        additionalUuidParts = new long[uuids.size() * 2];
        for (int i = 0; i < uuids.size(); i++) {
            UUID uuid = uuids.get(i);
            additionalUuidParts[2 * i] = uuid.getMostSignificantBits();
            additionalUuidParts[2 * i + 1] = uuid.getLeastSignificantBits();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("{");
        toString(builder);
        builder.append('}');
        return builder.toString();
    }

    public void toString(StringBuilder builder) {
        builder//.append("write:").append(writeSequence)
                .append("uuid:").append(new UUID(primordialUuidMsb, primordialUuidLsb))
                .append(",\n nid:").append(nid)
                .append("\n container:").append(containerSequence)
                //.append(", versionStartPosition:").append(versionStartPosition)
                .append(",\n versions[");
        getVersionList().forEach((version) -> {
            builder.append("\n");
            builder.append(version);
            builder.append(",");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");

    }

    @Override
    public String toUserString() {
        return toString();
    }

    @Override
    public List<SememeChronology<? extends SememeVersion<?>>> getSememeList() {
        return Get.sememeService().getSememesForComponent(nid).collect(Collectors.toList());
    }

    @Override
    public List<SememeChronology<? extends SememeVersion<?>>> getSememeListFromAssemblage(int assemblageSequence) {
        return Get.sememeService().
                getSememesForComponentFromAssemblage(nid, assemblageSequence).collect(Collectors.toList());
    }

    @Override
    public <SV extends SememeVersion> List<SememeChronology<SV>>
            getSememeListFromAssemblageOfType(int assemblageSequence, Class<SV> type) {
        List<SememeChronology<SV>> results = Get.sememeService().ofType(type).
                getSememesForComponentFromAssemblage(nid, assemblageSequence)
                .collect(Collectors.toList());
        return results;
    }

    private List<V> getVersionsForStamps(StampSequenceSet stampSequences) {
        List<V> versions = new ArrayList<>(stampSequences.size());
        stampSequences.stream().forEach((stampSequence) -> versions.add(getVersionForStamp(stampSequence).get()));
        return versions;
    }

    @Override
    public Optional<LatestVersion<V>> getLatestVersion(Class<V> type, StampCoordinate coordinate) {
        RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);
        if (versionListReference != null) {
            ArrayList<V> versions = versionListReference.get();
            if (versions != null) {
                return calc.getLatestVersion(this);
            }
        }
        StampSequenceSet latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());
        if (latestStampSequences.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new LatestVersion<>((List<V>) getVersionsForStamps(latestStampSequences)));
    }

    @Override
    public boolean isLatestVersionActive(StampCoordinate coordinate) {
        RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);
        StampSequenceSet latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());
        return !latestStampSequences.isEmpty();
    }
    
    

    @Override
    public List<Graph<? extends V>> getVersionGraphList() {

        HashMap<StampPath, TreeSet<V>> versionMap = new HashMap<>();
        getVersionList().forEach((version) -> {
            StampPath path = Get.pathService().getStampPath(version.getPathSequence());

            TreeSet<V> versionSet = versionMap.get(path);
            if (versionSet == null) {
                versionSet = new TreeSet<>((V v1, V v2) -> {
                    int comparison = Long.compare(v1.getTime(), v2.getTime());
                    if (comparison != 0) {
                        return comparison;
                    }
                    return Integer.compare(v1.getStampSequence(), v2.getStampSequence());
                });
                 versionMap.put(path, versionSet);
            }
            versionSet.add(version);
        });

        if (versionMap.size() == 1) {
            // easy case...
            List<Graph<? extends V>> results = new ArrayList();
            Graph<V> graph = new Graph<>();
            results.add(graph);
            versionMap.entrySet().forEach((entry) -> {
                entry.getValue().forEach((version) -> {
                    if (graph.getRoot() == null) {
                        graph.createRoot(version);
                    } else {
                        graph.getLastAddedNode().addChild(version);
                    }
                });

            });
            return results;
        }
        // TODO support for more than one path...
        throw new UnsupportedOperationException("TODO: Implement version graph for more than one path...");
    }

    @Override
    public List<? extends V> getVisibleOrderedVersionList(StampCoordinate stampCoordinate) {
        RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(stampCoordinate);
        SortedSet<V> sortedLogicGraphs = new TreeSet<>((V graph1, V graph2) -> {
            RelativePosition relativePosition = calc.fastRelativePosition(graph1, graph2, stampCoordinate.getStampPrecedence());
            switch (relativePosition) {
                case BEFORE:
                    return -1;
                case EQUAL:
                    return 0;
                case AFTER:
                    return 1;
                case UNREACHABLE:
                case CONTRADICTION:
                default:
                    throw new UnsupportedOperationException("Can't handle: " + relativePosition);
            }
        });

        sortedLogicGraphs.addAll(getVersionList());

        return sortedLogicGraphs.stream().collect(Collectors.toList());
    }

}
