package gov.vha.isaac.identifier;

import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.NidSet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;


/**
 * Created by kec on 12/18/14.
 */
public class ConcurrentSequenceIntMap {

    private static final int SEGMENT_SIZE = 128000;
    ReentrantLock lock = new ReentrantLock();
    
    CopyOnWriteArrayList<int[]> sequenceIntList = new CopyOnWriteArrayList<>();
    AtomicInteger size = new AtomicInteger(0);
    
    public ConcurrentSequenceIntMap() {
        int[] segmentArray = new int[SEGMENT_SIZE];
        Arrays.fill(segmentArray, -1);
        sequenceIntList.add(segmentArray);
    }
    
    public void read(File folder) throws IOException {
        sequenceIntList = null;
        int segments = 1;
        for (int segment = 0; segment < segments; segment++) {
            try (DataInputStream in = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(new File(folder, segment + ".sequence-int.map"))))) {                
                int segmentSize = in.readInt();
                segments = in.readInt();
                if (sequenceIntList == null) {
                    sequenceIntList = new CopyOnWriteArrayList<>();
                }
                int[] segmentArray = new int[segmentSize];
                Arrays.fill(segmentArray, -1);
                sequenceIntList.add(segmentArray);
                
                for (int indexInSegment = 0; indexInSegment < segmentSize; indexInSegment++) {
                    sequenceIntList.get(segment)[indexInSegment] = in.readInt();
                }
                size.set(in.readInt());
            }
        }
    }
    
    public void write(File folder) throws IOException {
        folder.mkdirs();
        int segments = sequenceIntList.size();
        for (int segment = 0; segment < segments; segment++) {
            try (DataOutputStream out = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(new File(folder, segment + ".sequence-int.map"))))) {                
                out.writeInt(SEGMENT_SIZE);
                out.writeInt(segments);
                int[] segmentArray = sequenceIntList.get(segment);
                for (int indexInSegment = 0; indexInSegment < SEGMENT_SIZE; indexInSegment++) {
                    out.writeInt(segmentArray[indexInSegment]);
                }
                out.writeInt(size.get());
            }
        }
    }
    
    public int getSize() {
        return size.get();
    }
    
    public boolean containsKey(int sequence) {
        if (sequence < 0) {
            sequence = sequence - Integer.MIN_VALUE;
        }   
        int segmentIndex = sequence / SEGMENT_SIZE;
        int indexInSegment = sequence % SEGMENT_SIZE;
        if (segmentIndex >= sequenceIntList.size()) {
            return false;
        }
        return sequenceIntList.get(segmentIndex)[indexInSegment] != 0;
    }
    
    public OptionalInt get(int sequence) {
        if (sequence < 0) {
            sequence = sequence - Integer.MIN_VALUE;
        }        
        int segmentIndex = sequence / SEGMENT_SIZE;
        if (segmentIndex >= sequenceIntList.size()) {
            return OptionalInt.empty();
        }
        
        int indexInSegment = sequence % SEGMENT_SIZE;
        int returnValue = sequenceIntList.get(segmentIndex)[indexInSegment];
        if (returnValue == -1) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(returnValue);
    }
    
    public boolean put(int sequence, int value) {
        if (sequence < 0) {
            sequence = sequence - Integer.MIN_VALUE;
        }
        size.set(Math.max(sequence, size.get()));
        int segmentIndex = sequence / SEGMENT_SIZE;
        
        if (segmentIndex >= sequenceIntList.size()) {
            lock.lock();
            try {
                while (segmentIndex >= sequenceIntList.size()) {
                    int[] segmentArray = new int[SEGMENT_SIZE];
                    Arrays.fill(segmentArray, -1);
                    sequenceIntList.add(segmentArray);
                }
            } finally {
                lock.unlock();
            }
        }
        int indexInSegment = sequence % SEGMENT_SIZE;
        sequenceIntList.get(segmentIndex)[indexInSegment] = value;
        return true;
    }
    
    public NidSet getComponentNidsForConceptNids(ConceptSequenceSet conceptSequenceSet) {
        NidSet conceptNids = NidSet.of(conceptSequenceSet);
        NidSet results = new NidSet();
        int componentSize = size.get();
        for (int i = 0; i < componentSize; i++) {
            int segmentIndex = i / SEGMENT_SIZE;
            int indexInSegment = i % SEGMENT_SIZE;
            if (sequenceIntList.get(segmentIndex)[indexInSegment] != 0) {
                if (conceptNids.contains(sequenceIntList.get(segmentIndex)[indexInSegment])) {
                    results.add(i + Integer.MIN_VALUE);
                }
            }
        }
        return results;
    }

    public IntStream getComponentNidStream() {
        int componentSize = size.get();
        return IntStream.of(Integer.MIN_VALUE, 
                componentSize + Integer.MIN_VALUE).filter((nid) -> {
                    int i = nid - Integer.MIN_VALUE;
                    int segmentIndex = i / SEGMENT_SIZE;
                    int indexInSegment = i % SEGMENT_SIZE;
                    return sequenceIntList.get(segmentIndex)[indexInSegment] != 0;
                });
    }

    public IntStream getComponentsNotSet() {
        int componentSize = size.get();
        return IntStream.of(Integer.MIN_VALUE, 
                componentSize + Integer.MIN_VALUE).filter((nid) -> {
                    int i = nid - Integer.MIN_VALUE;
                    int segmentIndex = i / SEGMENT_SIZE;
                    int indexInSegment = i % SEGMENT_SIZE;
                    return sequenceIntList.get(segmentIndex)[indexInSegment] == 0;
                });
    }
}
