package gov.vha.isaac.ochre.commit.manager;


import gov.vha.isaac.ochre.api.DataSerializer;
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by kec on 12/18/14.
 *
 * @param <T>
 */
public class ConcurrentSequenceSerializedObjectMap<T> {

    private static final boolean storeObjects = false;
    private static final int SEGMENT_SIZE = 12800;
    ReentrantLock lock = new ReentrantLock();

    DataSerializer<T> serializer;
    byte[][][] objectByteList = new byte[1][][];

    CopyOnWriteArrayList<Object[]> objectListList = new CopyOnWriteArrayList<>();
    boolean[] changed = new boolean[1];
    AtomicInteger maxSequence = new AtomicInteger(-1);
    
    Path dbFolderPath;
    String folder;
    String suffix;

    public ConcurrentSequenceSerializedObjectMap(DataSerializer<T> serializer, 
            Path dbFolderPath, String folder, String suffix) {
        this.serializer = serializer;
        this.dbFolderPath = dbFolderPath;
        this.folder = folder;
        this.suffix = suffix;
        objectByteList[0] = new byte[SEGMENT_SIZE][];
        objectListList.add(new Object[SEGMENT_SIZE]);
        changed[0] = false;
    }
    
    public void readSegment(Path dbFolderPath, String folder, String suffix) {
        
    }
    
    public void writeSegment(Path dbFolderPath, String folder, String suffix) {
        
    }

    public void read() {
        int segment = 0;
        int segments = 1;

        while (segment < segments) {
            File segmentFile = new File(dbFolderPath.toFile(), folder + segment + suffix);
            try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(segmentFile)))) {
                segments = input.readInt();
                int segmentIndex = input.readInt();
                int segmentArrayLength = input.readInt();
                int offset = segmentIndex * segmentArrayLength;

                for (int i = 0; i < segmentArrayLength; i++) {
                    int byteArrayLength = input.readInt();
                    if (byteArrayLength > 0) {
                        byte[] bytes = new byte[byteArrayLength];
                        input.read(bytes);
                        put(offset + i, bytes);
                    }
                }
                
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            segment++;
        }

    }

    public void write() {
        int segments = objectByteList.length;
        for (int segmentIndex = 0; segmentIndex < segments; segmentIndex++) {
            File segmentFile = new File(dbFolderPath.toFile(), folder + segmentIndex + suffix);
            segmentFile.getParentFile().mkdirs();
            byte[][] segmentArray = objectByteList[segmentIndex];
            try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(segmentFile)))) {
                output.writeInt(segments);
                output.writeInt(segmentIndex);
                output.writeInt(segmentArray.length);
                for (byte[] indexValue : segmentArray) {
                    if (indexValue == null) {
                        output.writeInt(-1);
                    } else {
                        output.writeInt(indexValue.length);
                        output.write(indexValue);
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public Stream<T> getStream(IntPredicate sequenceFilter) {
        IntStream sequences = IntStream.rangeClosed(0, maxSequence.get());
        return sequences.filter(sequenceFilter).filter(sequence -> containsKey(sequence)).mapToObj(sequence -> getQuick(sequence));
    }

    public Stream<T> getStream() {
        IntStream sequences = IntStream.rangeClosed(0, maxSequence.get());
        return sequences.filter(sequence -> containsKey(sequence)).mapToObj(sequence -> getQuick(sequence));
    }

    public Stream<T> getParallelStream() {
        IntStream sequences = IntStream.rangeClosed(0, maxSequence.get()).parallel();
        return sequences.filter(sequence -> containsKey(sequence)).mapToObj(sequence -> getQuick(sequence));
    }
    
    public Stream<T> getParallelStream(IntPredicate sequenceFilter) {
        IntStream sequences = IntStream.rangeClosed(0, maxSequence.get()).parallel();
        return sequences.filter(sequenceFilter).filter(sequence -> containsKey(sequence)).mapToObj(sequence -> getQuick(sequence));
    }

    /**
     * Provides no range or null checking. For use with a stream that already
     * filters out null values and out of range sequences.
     *
     * @param sequence
     * @return
     */
    public T getQuick(int sequence) {
        int segmentIndex = sequence / SEGMENT_SIZE;
        int indexInSegment = sequence % SEGMENT_SIZE;
        if (storeObjects) {
            return (T) objectListList.get(segmentIndex)[indexInSegment];
        }
        if (segmentIndex >= objectByteList.length) {
            return null;
        }
        byte[] objectBytes = objectByteList[segmentIndex][indexInSegment];
        if (objectBytes == null) {
            return null;
        }
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(objectBytes))) {
            return serializer.deserialize(dis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getSize() {
        return maxSequence.get() + 1; // sequences start at zero, so size = max + 1...
    }

    public boolean containsKey(int sequence) {
        int segmentIndex = sequence / SEGMENT_SIZE;
        int indexInSegment = sequence % SEGMENT_SIZE;
        if (segmentIndex >= objectByteList.length) {
            return false;
        }
        return objectByteList[segmentIndex][indexInSegment] != null;
    }

    public Optional<T> get(int sequence) {

        int segmentIndex = sequence / SEGMENT_SIZE;
        if (segmentIndex >= objectByteList.length) {
            return Optional.empty();
        }
        int indexInSegment = sequence % SEGMENT_SIZE;
        if (storeObjects) {
            return Optional.ofNullable((T) objectListList.get(segmentIndex)[indexInSegment]);
        }
        byte[] objectBytes = objectByteList[segmentIndex][indexInSegment];
        if (objectBytes != null) {
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(objectBytes))) {

                return Optional.of(serializer.deserialize(dis));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    private boolean put(int sequence, byte[] value) {
        maxSequence.set(Math.max(sequence, maxSequence.get()));
        int segmentIndex = sequence / SEGMENT_SIZE;

        expandMap(segmentIndex);
        int indexInSegment = sequence % SEGMENT_SIZE;

        objectByteList[segmentIndex][indexInSegment] = value;
        return true;
    }

    public boolean put(int sequence, T value) {
        maxSequence.set(Math.max(sequence, maxSequence.get()));
        int segmentIndex = sequence / SEGMENT_SIZE;

        expandMap(segmentIndex);
        int indexInSegment = sequence % SEGMENT_SIZE;
        if (storeObjects) {
            objectListList.get(segmentIndex)[indexInSegment] = value;
            return true;
        } else {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                serializer.serialize(new DataOutputStream(byteArrayOutputStream), value);
                objectByteList[segmentIndex][indexInSegment] = byteArrayOutputStream.toByteArray();
                changed[segmentIndex] = true;
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void expandMap(int segmentIndex) {
        if (segmentIndex >= objectByteList.length) {
            lock.lock();
            try {
                while (segmentIndex >= objectByteList.length) {
                    changed = Arrays.copyOf(changed, objectByteList.length + 1);
                    changed[objectByteList.length] = false;
                    byte[][][] tempObjByteList = Arrays.copyOf(objectByteList, objectByteList.length + 1);
                    tempObjByteList[tempObjByteList.length - 1] = new byte[SEGMENT_SIZE][];
                    objectListList.add(new Object[SEGMENT_SIZE]);
                    objectByteList = tempObjByteList;
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
