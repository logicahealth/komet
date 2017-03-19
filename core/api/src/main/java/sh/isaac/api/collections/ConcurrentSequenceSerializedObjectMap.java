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



package sh.isaac.api.collections;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Path;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataSerializer;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/18/14.
 *
 * @param <T> the generic type
 */
public class ConcurrentSequenceSerializedObjectMap<T> {
   /** The Constant storeObjects. */
   private static final boolean storeObjects = false;

   /** The Constant SEGMENT_SIZE. */
   private static final int SEGMENT_SIZE = 12800;

   //~--- fields --------------------------------------------------------------

   /** The lock. */
   ReentrantLock lock = new ReentrantLock();

   /** The object byte list. */
   byte[][][] objectByteList = new byte[1][][];

   /** The object list list. */
   CopyOnWriteArrayList<Object[]> objectListList = new CopyOnWriteArrayList<>();

   /** The changed. */
   boolean[] changed = new boolean[1];

   /** The max sequence. */
   AtomicInteger maxSequence = new AtomicInteger(-1);

   /** The serializer. */
   DataSerializer<T> serializer;

   /** The db folder path. */
   Path dbFolderPath;

   /** The folder. */
   String folder;

   /** The suffix. */
   String suffix;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concurrent sequence serialized object map.
    *
    * @param serializer the serializer
    * @param dbFolderPath the db folder path
    * @param folder the folder
    * @param suffix the suffix
    */
   public ConcurrentSequenceSerializedObjectMap(DataSerializer<T> serializer,
         Path dbFolderPath,
         String folder,
         String suffix) {
      this.serializer        = serializer;
      this.dbFolderPath      = dbFolderPath;
      this.folder            = folder;
      this.suffix            = suffix;
      this.objectByteList[0] = new byte[SEGMENT_SIZE][];
      this.objectListList.add(new Object[SEGMENT_SIZE]);
      this.changed[0] = false;
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

      if (segmentIndex >= this.objectByteList.length) {
         return false;
      }

      return this.objectByteList[segmentIndex][indexInSegment] != null;
   }

   /**
    * Put.
    *
    * @param sequence the sequence
    * @param value the value
    * @return true, if successful
    */
   public boolean put(int sequence, T value) {
      this.maxSequence.set(Math.max(sequence, this.maxSequence.get()));

      final int segmentIndex = sequence / SEGMENT_SIZE;

      expandMap(segmentIndex);

      final int indexInSegment = sequence % SEGMENT_SIZE;

      if (storeObjects) {
         this.objectListList.get(segmentIndex)[indexInSegment] = value;
         return true;
      } else {
         try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            this.serializer.serialize(new DataOutputStream(byteArrayOutputStream), value);
            this.objectByteList[segmentIndex][indexInSegment] = byteArrayOutputStream.toByteArray();
            this.changed[segmentIndex]                        = true;
            return true;
         } catch (final IOException e) {
            throw new RuntimeException(e);
         }
      }
   }

   /**
    * Read.
    */
   public void read() {
      int segment  = 0;
      int segments = 1;

      while (segment < segments) {
         final File segmentFile = new File(this.dbFolderPath.toFile(), this.folder + segment + this.suffix);

         try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(segmentFile)))) {
            segments = input.readInt();

            final int segmentIndex       = input.readInt();
            final int segmentArrayLength = input.readInt();
            final int offset             = segmentIndex * segmentArrayLength;

            for (int i = 0; i < segmentArrayLength; i++) {
               final int byteArrayLength = input.readInt();

               if (byteArrayLength > 0) {
                  final byte[] bytes = new byte[byteArrayLength];

                  input.read(bytes);
                  put(offset + i, bytes);
               }
            }
         } catch (final IOException ex) {
            throw new RuntimeException(ex);
         }

         segment++;
      }
   }

   /**
    * Read segment.
    *
    * @param dbFolderPath the db folder path
    * @param folder the folder
    * @param suffix the suffix
    */
   public void readSegment(Path dbFolderPath, String folder, String suffix) {}

   /**
    * Write.
    */
   public void write() {
      final int segments = this.objectByteList.length;

      for (int segmentIndex = 0; segmentIndex < segments; segmentIndex++) {
         final File segmentFile = new File(this.dbFolderPath.toFile(), this.folder + segmentIndex + this.suffix);

         segmentFile.getParentFile()
                    .mkdirs();

         final byte[][] segmentArray = this.objectByteList[segmentIndex];

         try (DataOutputStream output =
               new DataOutputStream(new BufferedOutputStream(new FileOutputStream(segmentFile)))) {
            output.writeInt(segments);
            output.writeInt(segmentIndex);
            output.writeInt(segmentArray.length);

            for (final byte[] indexValue: segmentArray) {
               if (indexValue == null) {
                  output.writeInt(-1);
               } else {
                  output.writeInt(indexValue.length);
                  output.write(indexValue);
               }
            }
         } catch (final IOException ex) {
            throw new RuntimeException(ex);
         }
      }
   }

   /**
    * Write segment.
    *
    * @param dbFolderPath the db folder path
    * @param folder the folder
    * @param suffix the suffix
    */
   public void writeSegment(Path dbFolderPath, String folder, String suffix) {}

   /**
    * Expand map.
    *
    * @param segmentIndex the segment index
    */
   private void expandMap(int segmentIndex) {
      if (segmentIndex >= this.objectByteList.length) {
         this.lock.lock();

         try {
            while (segmentIndex >= this.objectByteList.length) {
               this.changed                             = Arrays.copyOf(this.changed, this.objectByteList.length + 1);
               this.changed[this.objectByteList.length] = false;

               final byte[][][] tempObjByteList = Arrays.copyOf(this.objectByteList, this.objectByteList.length + 1);

               tempObjByteList[tempObjByteList.length - 1] = new byte[SEGMENT_SIZE][];
               this.objectListList.add(new Object[SEGMENT_SIZE]);
               this.objectByteList = tempObjByteList;
            }
         } finally {
            this.lock.unlock();
         }
      }
   }

   /**
    * Put.
    *
    * @param sequence the sequence
    * @param value the value
    * @return true, if successful
    */
   private boolean put(int sequence, byte[] value) {
      this.maxSequence.set(Math.max(sequence, this.maxSequence.get()));

      final int segmentIndex = sequence / SEGMENT_SIZE;

      expandMap(segmentIndex);

      final int indexInSegment = sequence % SEGMENT_SIZE;

      this.objectByteList[segmentIndex][indexInSegment] = value;
      return true;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the.
    *
    * @param sequence the sequence
    * @return the optional
    */
   public Optional<T> get(int sequence) {
      final int segmentIndex = sequence / SEGMENT_SIZE;

      if (segmentIndex >= this.objectByteList.length) {
         return Optional.empty();
      }

      final int indexInSegment = sequence % SEGMENT_SIZE;

      if (storeObjects) {
         return Optional.ofNullable((T) this.objectListList.get(segmentIndex)[indexInSegment]);
      }

      final byte[] objectBytes = this.objectByteList[segmentIndex][indexInSegment];

      if (objectBytes != null) {
         try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(objectBytes))) {
            return Optional.of(this.serializer.deserialize(dis));
         } catch (final IOException e) {
            throw new RuntimeException(e);
         }
      }

      return Optional.empty();
   }

   /**
    * Gets the parallel stream.
    *
    * @return the parallel stream
    */
   public Stream<T> getParallelStream() {
      final IntStream sequences = IntStream.rangeClosed(0, this.maxSequence.get())
                                           .parallel();

      return sequences.filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }

   /**
    * Gets the parallel stream.
    *
    * @param sequenceFilter the sequence filter
    * @return the parallel stream
    */
   public Stream<T> getParallelStream(IntPredicate sequenceFilter) {
      final IntStream sequences = IntStream.rangeClosed(0, this.maxSequence.get())
                                           .parallel();

      return sequences.filter(sequenceFilter)
                      .filter(sequence -> containsKey(sequence))
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
      final int segmentIndex   = sequence / SEGMENT_SIZE;
      final int indexInSegment = sequence % SEGMENT_SIZE;

      if (storeObjects) {
         return (T) this.objectListList.get(segmentIndex)[indexInSegment];
      }

      if (segmentIndex >= this.objectByteList.length) {
         return null;
      }

      final byte[] objectBytes = this.objectByteList[segmentIndex][indexInSegment];

      if (objectBytes == null) {
         return null;
      }

      try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(objectBytes))) {
         return this.serializer.deserialize(dis);
      } catch (final IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Gets the size.
    *
    * @return the size
    */
   public int getSize() {
      return this.maxSequence.get() + 1;  // sequences start at zero, so size = max + 1...
   }

   /**
    * Gets the stream.
    *
    * @return the stream
    */
   public Stream<T> getStream() {
      final IntStream sequences = IntStream.rangeClosed(0, this.maxSequence.get());

      return sequences.filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }

   /**
    * Gets the stream.
    *
    * @param sequenceFilter the sequence filter
    * @return the stream
    */
   public Stream<T> getStream(IntPredicate sequenceFilter) {
      final IntStream sequences = IntStream.rangeClosed(0, this.maxSequence.get());

      return sequences.filter(sequenceFilter)
                      .filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }
}

