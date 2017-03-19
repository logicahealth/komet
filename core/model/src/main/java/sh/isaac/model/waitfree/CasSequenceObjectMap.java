/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.model.waitfree;

//~--- JDK imports ------------------------------------------------------------

import java.io.*;

import java.nio.file.Path;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.DataSerializer;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.memory.DiskSemaphore;
import sh.isaac.api.memory.HoldInMemoryCache;
import sh.isaac.api.memory.MemoryManagedReference;
import sh.isaac.api.memory.WriteToDiskCache;
import sh.isaac.model.ObjectChronologyImpl;
import sh.isaac.model.WaitFreeComparable;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <T>
 */
public class CasSequenceObjectMap<T extends WaitFreeComparable> {
   private static final Logger             log             = LogManager.getLogger();
   private static final int                SEGMENT_SIZE    = 1280;
   private static final int                WRITE_SEQUENCES = 64;
   private static final AtomicIntegerArray writeSequences  = new AtomicIntegerArray(WRITE_SEQUENCES);

   //~--- fields --------------------------------------------------------------

   ReentrantLock expandLock = new ReentrantLock();
   CasSequenceMapSerializer segmentSerializer = new CasSequenceMapSerializer();
   CopyOnWriteArrayList<MemoryManagedReference<SerializedAtomicReferenceArray>> objectByteList =
      new CopyOnWriteArrayList<>();
   private final String       filePrefix;
   private final String       fileSuffix;
   private final Path         dbFolderPath;
   WaitFreeMergeSerializer<T> elementSerializer;

   //~--- constructors --------------------------------------------------------

   public CasSequenceObjectMap(WaitFreeMergeSerializer<T> elementSerializer,
                               Path dbFolderPath,
                               String filePrefix,
                               String fileSuffix) {
      this.elementSerializer = elementSerializer;
      this.dbFolderPath      = dbFolderPath;
      this.filePrefix        = filePrefix;
      this.fileSuffix        = fileSuffix;
   }

   //~--- methods -------------------------------------------------------------

   public boolean containsKey(int sequence) {
      int segmentIndex   = sequence / SEGMENT_SIZE;
      int indexInSegment = sequence % SEGMENT_SIZE;

      if (segmentIndex >= objectByteList.size()) {
         return false;
      }

      return getSegment(segmentIndex).get(indexInSegment) != null;
   }

   /**
    * Read from disk.
    *
    * As part of initialization, ensure that all map files are found. Calculation made by ensuring that the number of files with the appropriate
    * fileSuffix are sequentially found.
    */
   public boolean initialize() {
      objectByteList.clear();

      int  segmentIndex     = 0;
      File segmentDirectory = new File(dbFolderPath.toString());

      // Identify number of files with fileSuffix
      int numberOfSegmentFiles = segmentDirectory.list(new FilenameFilter() {
               @Override
               public boolean accept(File dir, String name) {
                  return (name.endsWith(fileSuffix.toString()));
               }
            }).length;

      // While initializing, if cannot find expected *.fileSuffix file sequentially, database is corrupt.
      while (segmentIndex < numberOfSegmentFiles) {
         File segmentFile = new File(dbFolderPath.toFile(), filePrefix + segmentIndex + fileSuffix);

         if (!segmentFile.exists()) {
            throw new RuntimeException("Missing database file: " + segmentFile.getName());
         }

         MemoryManagedReference<SerializedAtomicReferenceArray> reference = new MemoryManagedReference<>(null,
                                                                                                         segmentFile,
                                                                                                         segmentSerializer);

         objectByteList.add(segmentIndex, reference);
         segmentIndex++;
      }

      // Inform calling method if map directory is populated
      return numberOfSegmentFiles > 0;
   }

   public boolean put(int sequence, @NotNull T value) {
      T   originalValue = value;
      int segmentIndex  = sequence / SEGMENT_SIZE;

      if (segmentIndex >= objectByteList.size()) {
         expandLock.lock();

         try {
            int currentMaxSegment = objectByteList.size() - 1;

            while (segmentIndex > currentMaxSegment) {
               int newSegment = currentMaxSegment + 1;
               File segmentFile = new File(dbFolderPath.toFile(), filePrefix + newSegment + fileSuffix);
               MemoryManagedReference<SerializedAtomicReferenceArray> reference =
                  new MemoryManagedReference<>(new SerializedAtomicReferenceArray(SEGMENT_SIZE,
                                                                                  elementSerializer,
                                                                                  newSegment),
                                               segmentFile,
                                               segmentSerializer);

               objectByteList.add(newSegment, reference);
               currentMaxSegment = objectByteList.size() - 1;
            }
         } finally {
            expandLock.unlock();
         }
      }

      int                            indexInSegment = sequence % SEGMENT_SIZE;
      SerializedAtomicReferenceArray segment        = getSegment(segmentIndex);

      //
      int    oldWriteSequence = value.getWriteSequence();
      int    oldDataSize      = 0;
      byte[] oldData          = segment.get(indexInSegment);

      if (oldData != null) {
         oldWriteSequence = getWriteSequence(oldData);
         oldDataSize      = oldData.length;
      }

      while (true) {
         if (oldWriteSequence != value.getWriteSequence()) {
            // need to merge.
            ByteArrayDataBuffer oldDataBuffer = new ByteArrayDataBuffer(oldData);
            T                   oldObject     = elementSerializer.deserialize(oldDataBuffer);

            value = elementSerializer.merge(value, oldObject, oldWriteSequence);
         }

         value.setWriteSequence(getWriteSequence(sequence));

         ByteArrayDataBuffer newDataBuffer = new ByteArrayDataBuffer(oldDataSize + 512);

         elementSerializer.serialize(newDataBuffer, value);
         newDataBuffer.trimToSize();

         if (segment.compareAndSet(indexInSegment, oldData, newDataBuffer.getData())) {
            objectByteList.get(segmentIndex)
                          .elementUpdated();

            if ((originalValue != value) && (value instanceof ObjectChronologyImpl)) {
               ObjectChronologyImpl objc = (ObjectChronologyImpl) originalValue;

               objc.setWrittenData(newDataBuffer.getData());
               objc.setWriteSequence(value.getWriteSequence());
            }

            return true;
         }

         // Try again.
         oldData          = segment.get(indexInSegment);
         oldWriteSequence = getWriteSequence(oldData);
      }
   }

   public void write() {
      objectByteList.stream()
                    .forEach((segment) -> segment.write());
   }

   protected SerializedAtomicReferenceArray readSegmentFromDisk(int segmentIndex) {
      File segmentFile = new File(dbFolderPath.toFile(), filePrefix + segmentIndex + fileSuffix);

      DiskSemaphore.acquire();

      try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(segmentFile)))) {
         SerializedAtomicReferenceArray segmentArray = segmentSerializer.deserialize(in);
         MemoryManagedReference<SerializedAtomicReferenceArray> reference = new MemoryManagedReference<>(segmentArray,
                                                                                                         segmentFile,
                                                                                                         segmentSerializer);

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

   //~--- get methods ---------------------------------------------------------

   public boolean hasData(int sequence) {
      int segmentIndex   = sequence / SEGMENT_SIZE;
      int indexInSegment = sequence % SEGMENT_SIZE;

      return getSegment(segmentIndex).get(indexInSegment) != null;
   }

   public Optional<T> get(int sequence) {
      int segmentIndex   = sequence / SEGMENT_SIZE;
      int indexInSegment = sequence % SEGMENT_SIZE;

      if (segmentIndex >= objectByteList.size()) {
         log.warn("Tried to access segment that does not exist. Sequence: " + sequence + " segment: " + segmentIndex +
                  " index: " + indexInSegment);
         return Optional.empty();
      }

      byte[] objectBytes = getSegment(segmentIndex).get(indexInSegment);

      if (objectBytes != null) {
         ByteArrayDataBuffer buf = new ByteArrayDataBuffer(objectBytes);

         return Optional.of(elementSerializer.deserialize(buf));
      }

      return Optional.empty();
   }

   public IntStream getKeyParallelStream() {
      IntStream sequences = IntStream.range(0, objectByteList.size() * SEGMENT_SIZE)
                                     .parallel();

      return sequences.filter(sequence -> containsKey(sequence));
   }

   public IntStream getKeyStream() {
      IntStream sequences = IntStream.range(0, objectByteList.size() * SEGMENT_SIZE);

      return sequences.filter(sequence -> containsKey(sequence));
   }

   public Stream<T> getParallelStream() {
      IntStream sequences = IntStream.range(0, objectByteList.size() * SEGMENT_SIZE)
                                     .parallel();

      return sequences.filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }

   /**
    * Provides no range or null checking. For use with a stream that already
    * filters out null values and out of range sequences.
    *
    * @param sequence
    * @return
    */
   public T getQuick(int sequence) {
      int                 segmentIndex   = sequence / SEGMENT_SIZE;
      int                 indexInSegment = sequence % SEGMENT_SIZE;
      ByteArrayDataBuffer buff           = new ByteArrayDataBuffer(getSegment(segmentIndex).get(indexInSegment));

      return elementSerializer.deserialize(buff);
   }

   protected SerializedAtomicReferenceArray getSegment(int segmentIndex) {
      SerializedAtomicReferenceArray referenceArray = objectByteList.get(segmentIndex)
                                                                    .get();

      if (referenceArray == null) {
         referenceArray = readSegmentFromDisk(segmentIndex);
      }

      objectByteList.get(segmentIndex)
                    .elementRead();
      return referenceArray;
   }

   public int getSize() {
      // TODO determine if this is the best way / if this method is necessary.
      // Calculating this is taking on the order of seconds, on the SOLOR-ALL db.
      return (int) getParallelStream().count();
   }

   public Stream<T> getStream() {
      IntStream sequences = IntStream.range(0, objectByteList.size() * SEGMENT_SIZE);

      return sequences.filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }

   public int getWriteSequence(byte[] data) {
      return (((data[0]) << 24) | ((data[1] & 0xff) << 16) | ((data[2] & 0xff) << 8) | ((data[3] & 0xff)));
   }

   private static int getWriteSequence(int componentSequence) {
      return writeSequences.incrementAndGet(componentSequence % WRITE_SEQUENCES);
   }

   //~--- inner classes -------------------------------------------------------

   private class CasSequenceMapSerializer
            implements DataSerializer<SerializedAtomicReferenceArray> {
      @Override
      public SerializedAtomicReferenceArray deserialize(DataInput in) {
         try {
            int segment = in.readInt();
            SerializedAtomicReferenceArray referenceArray = new SerializedAtomicReferenceArray(SEGMENT_SIZE,
                                                                                               elementSerializer,
                                                                                               segment);

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
   }
}

