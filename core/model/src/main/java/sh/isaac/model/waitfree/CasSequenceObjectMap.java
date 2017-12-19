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

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.WaitFreeComparable;

//~--- classes ----------------------------------------------------------------

/**
 * The Class CasSequenceObjectMap.
 * @deprecated use the accumulate and get spines instead. 
 * @author kec
 * @param <T> the generic type
 */
@Deprecated
public class CasSequenceObjectMap<T extends WaitFreeComparable> {
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   /** The Constant SEGMENT_SIZE. */
   private static final int SEGMENT_SIZE = 1280;

   /** The Constant WRITE_SEQUENCES. */
   private static final int WRITE_SEQUENCES = 64;

   /** The Constant writeSequences. */
   private static final AtomicIntegerArray writeSequences = new AtomicIntegerArray(WRITE_SEQUENCES);

   //~--- fields --------------------------------------------------------------

   /** The expand lock. */
   ReentrantLock expandLock = new ReentrantLock();

   /** The segment serializer. */
   CasSequenceMapSerializer segmentSerializer = new CasSequenceMapSerializer();

   /** The object byte list. */
   CopyOnWriteArrayList<MemoryManagedReference<SerializedAtomicReferenceArray>> objectByteList =
      new CopyOnWriteArrayList<>();

   /** The file prefix. */
   private final String filePrefix;

   /** The file suffix. */
   private final String fileSuffix;

   /** The db folder path. */
   private final Path dbFolderPath;

   /** The element serializer. */
   WaitFreeMergeSerializer<T> elementSerializer;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new cas sequence object map.
    *
    * @param elementSerializer the element serializer
    * @param dbFolderPath the db folder path
    * @param filePrefix the file prefix
    * @param fileSuffix the file suffix
    */
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

   /**
    * Contains key.
    *
    * @param sequence the sequence
    * @return true, if successful
    */
   public boolean containsKey(int sequence) {
      final int segmentIndex   = sequence / SEGMENT_SIZE;
      final int indexInSegment = sequence % SEGMENT_SIZE;

      if (segmentIndex >= this.objectByteList.size()) {
         return false;
      }

      return getSegment(segmentIndex).get(indexInSegment) != null;
   }

   /**
    * Read from disk.
    *
    * As part of initialization, ensure that all map files are found. Calculation made by ensuring that the number of files with the appropriate
    * fileSuffix are sequentially found.
    *
    * @return true, if successful
    */
   public boolean initialize() {
      this.objectByteList.clear();

      int        segmentIndex     = 0;
      final File segmentDirectory = new File(this.dbFolderPath.toString());

      // Identify number of files with fileSuffix
      final int numberOfSegmentFiles =
         segmentDirectory.list((dir, name) -> (name.endsWith(CasSequenceObjectMap.this.fileSuffix))).length;

      // While initializing, if cannot find expected *.fileSuffix file sequentially, database is corrupt.
      while (segmentIndex < numberOfSegmentFiles) {
         final File segmentFile = new File(this.dbFolderPath.toFile(),
                                           this.filePrefix + segmentIndex + this.fileSuffix);

         if (!segmentFile.exists()) {
            throw new RuntimeException("Missing database file: " + segmentFile.getName());
         }

         final MemoryManagedReference<SerializedAtomicReferenceArray> reference = new MemoryManagedReference<>(null,
                                                                                                               segmentFile,
                                                                                                               this.segmentSerializer);

         this.objectByteList.add(segmentIndex, reference);
         segmentIndex++;
      }

      // Inform calling method if map directory is populated
      return numberOfSegmentFiles > 0;
   }

   /**
    * Put.
    *
    * @param sequence the sequence
    * @param value the value
    * @return true, if successful
    */
   public boolean put(int sequence, @NotNull T value) {
      final T   originalValue = value;
      final int segmentIndex  = sequence / SEGMENT_SIZE;

      if (segmentIndex >= this.objectByteList.size()) {
         this.expandLock.lock();

         try {
            int currentMaxSegment = this.objectByteList.size() - 1;

            while (segmentIndex > currentMaxSegment) {
               final int newSegment = currentMaxSegment + 1;
               final File segmentFile = new File(this.dbFolderPath.toFile(),
                                                 this.filePrefix + newSegment + this.fileSuffix);
               final MemoryManagedReference<SerializedAtomicReferenceArray> reference =
                  new MemoryManagedReference<>(new SerializedAtomicReferenceArray(SEGMENT_SIZE,
                                                                                  this.elementSerializer,
                                                                                  newSegment),
                                               segmentFile,
                                               this.segmentSerializer);

               this.objectByteList.add(newSegment, reference);
               currentMaxSegment = this.objectByteList.size() - 1;
            }
         } finally {
            this.expandLock.unlock();
         }
      }

      final int                            indexInSegment = sequence % SEGMENT_SIZE;
      final SerializedAtomicReferenceArray segment        = getSegment(segmentIndex);

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
            final ByteArrayDataBuffer oldDataBuffer = new ByteArrayDataBuffer(oldData);
            final T                   oldObject     = this.elementSerializer.deserialize(oldDataBuffer);

            value = this.elementSerializer.merge(value, oldObject, oldWriteSequence);
         }

         value.setWriteSequence(getWriteSequence(sequence));

         final ByteArrayDataBuffer newDataBuffer = new ByteArrayDataBuffer(oldDataSize + 512);  //TODO add version

         this.elementSerializer.serialize(newDataBuffer, value);
         newDataBuffer.trimToSize();

         if (segment.compareAndSet(indexInSegment, oldData, newDataBuffer.getData())) {
            this.objectByteList.get(segmentIndex)
                               .elementUpdated();

            if ((originalValue != value) && (value instanceof ChronologyImpl)) {
               final ChronologyImpl objc = (ChronologyImpl) originalValue;

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

   /**
    * Write.
    */
   public void write() {
      this.objectByteList.stream()
                         .forEach((segment) -> segment.write());
   }

   /**
    * Read segment from disk.
    *
    * @param segmentIndex the segment index
    * @return the serialized atomic reference array
    */
   protected SerializedAtomicReferenceArray readSegmentFromDisk(int segmentIndex) {
      final File segmentFile = new File(this.dbFolderPath.toFile(), this.filePrefix + segmentIndex + this.fileSuffix);

      DiskSemaphore.acquire();

      try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(segmentFile)))) {
         final SerializedAtomicReferenceArray segmentArray = this.segmentSerializer.deserialize(in);
         final MemoryManagedReference<SerializedAtomicReferenceArray> reference =
            new MemoryManagedReference<>(segmentArray,
                                         segmentFile,
                                         this.segmentSerializer);

         if (this.objectByteList.size() > segmentArray.getSegment()) {
            this.objectByteList.set(segmentArray.getSegment(), reference);
         } else {
            this.objectByteList.add(segmentArray.getSegment(), reference);
         }

         HoldInMemoryCache.addToCache(reference);
         WriteToDiskCache.addToCache(reference);
         return segmentArray;
      } catch (final IOException e) {
         throw new RuntimeException(e);
      } finally {
         DiskSemaphore.release();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks for data.
    *
    * @param sequence the sequence
    * @return true, if successful
    */
   public boolean hasData(int sequence) {
      final int segmentIndex   = sequence / SEGMENT_SIZE;
      final int indexInSegment = sequence % SEGMENT_SIZE;

      return getSegment(segmentIndex).get(indexInSegment) != null;
   }

   /**
    * Gets the.
    *
    * @param sequence the sequence
    * @return the optional
    */
   public Optional<T> get(int sequence) {
      final int segmentIndex   = sequence / SEGMENT_SIZE;
      final int indexInSegment = sequence % SEGMENT_SIZE;

      if (segmentIndex >= this.objectByteList.size()) {
         log.warn("Tried to access segment that does not exist. Sequence: " + sequence + " segment: " + segmentIndex +
                  " index: " + indexInSegment);
         return Optional.empty();
      }

      final byte[] objectBytes = getSegment(segmentIndex).get(indexInSegment);

      if (objectBytes != null) {
         final ByteArrayDataBuffer buf = new ByteArrayDataBuffer(objectBytes);

         return Optional.of(this.elementSerializer.deserialize(buf));
      }

      return Optional.empty();
   }

   /**
    * Gets the key parallel stream.
    *
    * @return the key parallel stream
    */
   public IntStream getKeyParallelStream() {
      final IntStream sequences = IntStream.range(0, this.objectByteList.size() * SEGMENT_SIZE)
                                           .parallel();

      return sequences.filter(sequence -> containsKey(sequence));
   }

   /**
    * Gets the key stream.
    *
    * @return the key stream
    */
   public IntStream getKeyStream() {
      final IntStream sequences = IntStream.range(0, this.objectByteList.size() * SEGMENT_SIZE);

      return sequences.filter(sequence -> containsKey(sequence));
   }

   /**
    * Gets the parallel stream.
    *
    * @return the parallel stream
    */
   public Stream<T> getParallelStream() {
      final IntStream sequences = IntStream.range(0, this.objectByteList.size() * SEGMENT_SIZE)
                                           .parallel();

      return sequences.filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }

   /**
    * Provides no range or null checking. For use with a stream that already
    * filters out null values and out of range sequences.
    *
    * @param sequence the sequence
    * @return the quick
    */
   public T getQuick(int sequence) {
      final int                 segmentIndex   = sequence / SEGMENT_SIZE;
      final int                 indexInSegment = sequence % SEGMENT_SIZE;
      final ByteArrayDataBuffer buff           = new ByteArrayDataBuffer(getSegment(segmentIndex).get(indexInSegment));

      return this.elementSerializer.deserialize(buff);
   }

   /**
    * Gets the segment.
    *
    * @param segmentIndex the segment index
    * @return the segment
    */
   protected SerializedAtomicReferenceArray getSegment(int segmentIndex) {
      
      SerializedAtomicReferenceArray referenceArray = this.objectByteList.get(segmentIndex)
                                                                         .get();

      if (referenceArray == null) {
         referenceArray = readSegmentFromDisk(segmentIndex);
      }

      this.objectByteList.get(segmentIndex)
                         .elementRead();
      return referenceArray;
   }

   /**
    * Gets the size.
    *
    * @return the size
    */
   public int getSize() {
      // TODO determine if this is the best way / if this method is necessary.
      // Calculating this is taking on the order of seconds, on the SOLOR-ALL db.
      return (int) getParallelStream().count();
   }

   /**
    * Gets the stream.
    *
    * @return the stream
    */
   public Stream<T> getStream() {
      final IntStream sequences = IntStream.range(0, this.objectByteList.size() * SEGMENT_SIZE);

      return sequences.filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }

   /**
    * Gets the write sequence.
    *
    * @param data the data
    * @return the write sequence
    */
   public static int getWriteSequence(byte[] data) {
      return (((data[2]) << 24) | ((data[3] & 0xff) << 16) | ((data[4] & 0xff) << 8) | ((data[5] & 0xff)));
   }

   /**
    * Gets the write sequence.
    *
    * @param componentSequence the component sequence
    * @return the write sequence
    */
   private static int getWriteSequence(int componentSequence) {
      int writeSequence = writeSequences.incrementAndGet(componentSequence % WRITE_SEQUENCES);
      if (writeSequence > 10240) {
         writeSequences.set(componentSequence % WRITE_SEQUENCES, 0);
         return getWriteSequence(componentSequence);
      }
      return writeSequence;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class CasSequenceMapSerializer.
    */
   private class CasSequenceMapSerializer
            implements DataSerializer<SerializedAtomicReferenceArray> {
      /**
       * Deserialize.
       *
       * @param in the in
       * @return the serialized atomic reference array
       */
      @Override
      public SerializedAtomicReferenceArray deserialize(DataInput in) {
         try {
            final int segment = in.readInt();
            final SerializedAtomicReferenceArray referenceArray = new SerializedAtomicReferenceArray(SEGMENT_SIZE,
                                                                                                     CasSequenceObjectMap.this.elementSerializer,
                                                                                                     segment);

            for (int i = 0; i < SEGMENT_SIZE; i++) {
               final int byteArrayLength = in.readInt();

               if (byteArrayLength > 0) {
                  final byte[] bytes = new byte[byteArrayLength];

                  in.readFully(bytes);
                  referenceArray.set(i, bytes);
               }
            }

            return referenceArray;
         } catch (final IOException e) {
            throw new RuntimeException(e);
         }
      }

      /**
       * Serialize.
       *
       * @param out the out
       * @param segmentArray the segment array
       */
      @Override
      public void serialize(DataOutput out, SerializedAtomicReferenceArray segmentArray) {
         try {
            out.writeInt(segmentArray.getSegment());

            for (int indexValue = 0; indexValue < SEGMENT_SIZE; indexValue++) {
               final byte[] value = segmentArray.get(indexValue);

               if (value == null) {
                  out.writeInt(-1);
               } else {
                  out.writeInt(value.length);
                  out.write(value);
               }
            }
         } catch (final IOException e) {
            throw new RuntimeException(e);
         }
      }
   }
}

