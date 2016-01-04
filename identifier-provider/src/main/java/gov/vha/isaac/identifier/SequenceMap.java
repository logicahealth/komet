/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.identifier;

import gov.vha.isaac.ochre.api.collections.NativeIntIntHashMap;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.OptionalInt;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

/**
 * Sequences start at 1.
 * @author kec
 */
public class SequenceMap {

    public static final int FIRST_SEQUENCE = 1;
    private static final double MINIMUM_LOAD_FACTOR = 0.75;
    private static final double MAXIMUM_LOAD_FACTOR = 0.9;

    StampedLock sl = new StampedLock();

    int nextSequence = FIRST_SEQUENCE;

    final NativeIntIntHashMap nidSequenceMap;
    final NativeIntIntHashMap sequenceNidMap;

    public SequenceMap(int defaultCapacity) {
        nidSequenceMap = new NativeIntIntHashMap(defaultCapacity, MINIMUM_LOAD_FACTOR, MAXIMUM_LOAD_FACTOR);
        sequenceNidMap = new NativeIntIntHashMap(defaultCapacity, MINIMUM_LOAD_FACTOR, MAXIMUM_LOAD_FACTOR);
    }

    public int getNextSequence() {
        return nextSequence;
    }

    public int getSize() {
        assert nidSequenceMap.size() == sequenceNidMap.size() : "nidSequenceMap.size() = "
                + nidSequenceMap.size() + " sequenceNidMap.size() = " + sequenceNidMap.size();
        return sequenceNidMap.size();
    }

    public boolean containsNid(int nid) {
        long stamp = sl.tryOptimisticRead();
        boolean value = nidSequenceMap.containsKey(nid);
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                value = nidSequenceMap.containsKey(nid);
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return value;
    }

    public int getSequenceFast(int nid) {
        long stamp = sl.tryOptimisticRead();
        int value = nidSequenceMap.get(nid);
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                value = nidSequenceMap.get(nid);
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return value;
    }

    public OptionalInt getSequence(int nid) {
        if (containsNid(nid)) {
            long stamp = sl.tryOptimisticRead();
            int value = nidSequenceMap.get(nid);
            if (!sl.validate(stamp)) {
                stamp = sl.readLock();
                try {
                    value = nidSequenceMap.get(nid);
                } finally {
                    sl.unlockRead(stamp);
                }
            }
            return OptionalInt.of(value);
        }
        return OptionalInt.empty();
    }

    public OptionalInt getNid(int sequence) {
        long stamp = sl.tryOptimisticRead();
        int value = sequenceNidMap.get(sequence);
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                value = sequenceNidMap.get(sequence);
            } finally {
                sl.unlockRead(stamp);
            }
        }
        if (value == 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(value);
    }

    public int getNidFast(int sequence) {
        long stamp = sl.tryOptimisticRead();
        int value = sequenceNidMap.get(sequence);
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                value = sequenceNidMap.get(sequence);
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return value;
    }

    public int addNid(int nid) {
        long stamp = sl.writeLock();
        try {
            if (!nidSequenceMap.containsKey(nid)) {
                int sequence = nextSequence++;
                nidSequenceMap.put(nid, sequence);
                sequenceNidMap.put(sequence, nid);
                return sequence;
            }
            return nidSequenceMap.get(nid);
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    public int addNidIfMissing(int nid) {
        long stamp = sl.tryOptimisticRead();
        boolean containsKey = nidSequenceMap.containsKey(nid);
        int value = nidSequenceMap.get(nid);
        if (sl.validate(stamp) && containsKey) {
            return value;
        }
        stamp = sl.writeLock();
        try {
            if (nidSequenceMap.containsKey(nid)) {
                return nidSequenceMap.get(nid);
            }
                value = nextSequence++;
                nidSequenceMap.put(nid, value);
                sequenceNidMap.put(value, nid);
                return value;
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    public IntStream getSequenceStream() {
        return IntStream.of(sequenceNidMap.keys().elements());
    }

    public IntStream getConceptNidStream() {
        return IntStream.of(nidSequenceMap.keys().elements());
    }

    public void write(File mapFile) throws IOException {
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mapFile)))) {
            output.writeInt(nidSequenceMap.size());
            output.writeInt(nextSequence);
            nidSequenceMap.forEachPair((int nid, int sequence) -> {
                try {
                    output.writeInt(nid);
                    output.writeInt(sequence);
                    return true;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    public void read(File mapFile) throws IOException {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)))) {
            int size = input.readInt();
            nextSequence = input.readInt();
            nidSequenceMap.ensureCapacity(size);
            sequenceNidMap.ensureCapacity(size);
            for (int i = 0; i < size; i++) {
                int nid = input.readInt();
                int sequence = input.readInt();
                nidSequenceMap.put(nid, sequence);
                sequenceNidMap.put(sequence, nid);
            }
        }
    }
}
