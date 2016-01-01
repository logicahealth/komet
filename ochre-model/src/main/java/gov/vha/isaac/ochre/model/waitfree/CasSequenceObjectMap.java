/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.waitfree;

import gov.vha.isaac.ochre.api.memory.DiskSemaphore;
import gov.vha.isaac.ochre.api.memory.HoldInMemoryCache;
import gov.vha.isaac.ochre.api.memory.MemoryManagedReference;
import gov.vha.isaac.ochre.api.memory.WriteToDiskCache;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.model.WaitFreeComparable;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.ochre.api.DataSerializer;

/**
 *
 * @author kec
 * @param <T>
 */
public class CasSequenceObjectMap<T extends WaitFreeComparable> {

    private static final Logger log = LogManager.getLogger();
    private static final int SEGMENT_SIZE = 1280;
    private static final int WRITE_SEQUENCES = 64;

    private static final AtomicIntegerArray writeSequences = new AtomicIntegerArray(WRITE_SEQUENCES);

    private static int getWriteSequence(int componentSequence) {
        return writeSequences.incrementAndGet(componentSequence % WRITE_SEQUENCES);
    }

    ReentrantLock expandLock = new ReentrantLock();

    private final String filePrefix;
    private final String fileSuffix;
    private final Path dbFolderPath;
    WaitFreeMergeSerializer<T> elementSerializer;
    CasSequenceMapSerializer segmentSerializer = new CasSequenceMapSerializer();

    CopyOnWriteArrayList<MemoryManagedReference<SerializedAtomicReferenceArray>> objectByteList = new CopyOnWriteArrayList<>();


    public CasSequenceObjectMap(WaitFreeMergeSerializer<T> elementSerializer, Path dbFolderPath,
                                String filePrefix, String fileSuffix) {
        this.elementSerializer = elementSerializer;
        this.dbFolderPath = dbFolderPath;
        this.filePrefix = filePrefix;
        this.fileSuffix = fileSuffix;

    }

    /**
     * Read from disk
     *
     */
    public void initialize() {
        objectByteList.clear();
        int segmentIndex = 0;
        File segmentFile = new File(dbFolderPath.toFile(), filePrefix + segmentIndex + fileSuffix);

        while (segmentFile.exists()) {
            MemoryManagedReference<SerializedAtomicReferenceArray> reference =
                    new MemoryManagedReference<>(null, segmentFile, segmentSerializer);
            objectByteList.add(segmentIndex, reference);
            segmentIndex++;
            segmentFile = new File(dbFolderPath.toFile(), filePrefix + segmentIndex + fileSuffix);
        }
    }

    private class CasSequenceMapSerializer implements DataSerializer<SerializedAtomicReferenceArray> {

        @Override
        public void serialize(DataOutput out, SerializedAtomicReferenceArray segmentArray) {
            try {
                out.writeInt(segmentArray.getSegment());
                for (int indexValue = 0; indexValue < SEGMENT_SIZE; indexValue++) {
                    byte[] value = segmentArray.get(indexValue);
                    if (value == null) {
                        out.writeInt(-1);
                    } else {
                        out.writeInt(value.length);
                        out.write(value);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public SerializedAtomicReferenceArray deserialize(DataInput in) {
            try {
                int segment = in.readInt();
                SerializedAtomicReferenceArray referenceArray =
                        new SerializedAtomicReferenceArray(SEGMENT_SIZE, elementSerializer, segment);

                for (int i = 0; i < SEGMENT_SIZE; i++) {
                    int byteArrayLength = in.readInt();
                    if (byteArrayLength > 0) {
                        byte[] bytes = new byte[byteArrayLength];
                        in.readFully(bytes);
                        referenceArray.set(i, bytes);
                    }
                }
                return referenceArray;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void write() {
        objectByteList.stream().forEach((segment) -> segment.write());
    }

    public Stream<T> getStream() {
        IntStream sequences = IntStream.range(0, objectByteList.size() * SEGMENT_SIZE);
        return sequences.filter(sequence -> containsKey(sequence)).mapToObj(sequence -> getQuick(sequence));
    }

    public Stream<T> getParallelStream() {
        IntStream sequences = IntStream.range(0, objectByteList.size() * SEGMENT_SIZE).parallel();
        return sequences.filter(sequence -> containsKey(sequence)).mapToObj(sequence -> getQuick(sequence));
    }

    protected SerializedAtomicReferenceArray readSegmentFromDisk(int segmentIndex) {
        File segmentFile = new File(dbFolderPath.toFile(), filePrefix + segmentIndex + fileSuffix);
        DiskSemaphore.acquire();
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(segmentFile)))) {
            SerializedAtomicReferenceArray segmentArray = segmentSerializer.deserialize(in);
            MemoryManagedReference<SerializedAtomicReferenceArray> reference =
                    new MemoryManagedReference<>(segmentArray, segmentFile, segmentSerializer);
            if (objectByteList.size() > segmentArray.getSegment()) {
                objectByteList.set(segmentArray.getSegment(), reference);
            } else {
                objectByteList.add(segmentArray.getSegment(), reference);
            }

            HoldInMemoryCache.addToCache(reference);
            WriteToDiskCache.addToCache(reference);

            return segmentArray;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            DiskSemaphore.release();
        }

    }

    protected SerializedAtomicReferenceArray getSegment(int segmentIndex) {
        SerializedAtomicReferenceArray referenceArray = objectByteList.get(segmentIndex).get();
        if (referenceArray == null) {
            referenceArray =
                    readSegmentFromDisk(segmentIndex);

        }
        objectByteList.get(segmentIndex).elementRead();
        return referenceArray;
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

        ByteArrayDataBuffer buff = new ByteArrayDataBuffer(getSegment(segmentIndex).get(indexInSegment));
        return elementSerializer.deserialize(buff);
    }
    
    public Optional<T> getOptional(int sequence) {
        int segmentIndex = sequence / SEGMENT_SIZE;
        int indexInSegment = sequence % SEGMENT_SIZE;
        if (segmentIndex >= objectByteList.size()) {
            log.warn("Tried to access segment that does not exist. Sequence: " +
                    sequence + " segment: " + segmentIndex +
                    " index: " + indexInSegment);
            return Optional.empty();
        }
        byte[] data = getSegment(segmentIndex).get(indexInSegment);
        if (data == null) {
            return Optional.empty();
        }

        ByteArrayDataBuffer buff = new ByteArrayDataBuffer(data);
        return Optional.of(elementSerializer.deserialize(buff));
    }
    
    

    public int getSize() {
        // TODO determine if this is the best way / if this method is necessary.  
        // Calculating this is taking on the order of seconds, on the SOLOR-ALL db. 
        return (int) getParallelStream().count();
    }

    public boolean containsKey(int sequence) {
        int segmentIndex = sequence / SEGMENT_SIZE;
        int indexInSegment = sequence % SEGMENT_SIZE;
        if (segmentIndex >= objectByteList.size()) {
            return false;
        }
        return getSegment(segmentIndex).get(indexInSegment) != null;
    }

    public Optional<T> get(int sequence) {

        int segmentIndex = sequence / SEGMENT_SIZE;
        if (segmentIndex >= objectByteList.size()) {
            return Optional.empty();
        }
        int indexInSegment = sequence % SEGMENT_SIZE;

        byte[] objectBytes = getSegment(segmentIndex).get(indexInSegment);
        if (objectBytes != null) {
            ByteArrayDataBuffer buf = new ByteArrayDataBuffer(objectBytes);
            return Optional.of(elementSerializer.deserialize(buf));
        }
        return Optional.empty();
    }

    public boolean put(int sequence, T value) {

        int segmentIndex = sequence / SEGMENT_SIZE;

        if (segmentIndex >= objectByteList.size()) {
            expandLock.lock();
            try {
                int currentMaxSegment = objectByteList.size() -1;
                while (segmentIndex > currentMaxSegment) {
                    int newSegment = currentMaxSegment + 1;
                    File segmentFile = new File(dbFolderPath.toFile(), filePrefix + newSegment + fileSuffix);
                    MemoryManagedReference<SerializedAtomicReferenceArray> reference =
                            new MemoryManagedReference<>(
                                    new SerializedAtomicReferenceArray(SEGMENT_SIZE, elementSerializer, newSegment),
                                    segmentFile, segmentSerializer);
                    objectByteList.add(newSegment, reference);
                    currentMaxSegment = objectByteList.size() -1;
                }
            } finally {
                expandLock.unlock();
            }
        }
        int indexInSegment = sequence % SEGMENT_SIZE;
        SerializedAtomicReferenceArray segment = getSegment(segmentIndex);
        //
        int oldWriteSequence = value.getWriteSequence();
        int oldDataSize = 0;
        byte[] oldData = segment.get(indexInSegment);
        if (oldData != null) {
            oldWriteSequence = getWriteSequence(oldData);
            oldDataSize = oldData.length;
        }

        while (true) {
            if (oldWriteSequence != value.getWriteSequence()) {
                // need to merge.
                ByteArrayDataBuffer oldDataBuffer = new ByteArrayDataBuffer(oldData);
                T oldObject = elementSerializer.deserialize(oldDataBuffer);
                value = elementSerializer.merge(value, oldObject, oldWriteSequence);
            }
            value.setWriteSequence(getWriteSequence(sequence));
            
            ByteArrayDataBuffer newDataBuffer = new ByteArrayDataBuffer(oldDataSize + 512);
            elementSerializer.serialize(newDataBuffer, value);
            newDataBuffer.trimToSize();
            if (segment.compareAndSet(indexInSegment, oldData, newDataBuffer.getData())) {
                objectByteList.get(segmentIndex).elementUpdated();
                return true;
            }

            // Try again.
            oldData = segment.get(indexInSegment);
            oldWriteSequence = getWriteSequence(oldData);
        }

    }

    public int getWriteSequence(byte[] data) {
        return (((data[0]) << 24)
                | ((data[1] & 0xff) << 16)
                | ((data[2] & 0xff) << 8)
                | ((data[3] & 0xff)));
    }
}
