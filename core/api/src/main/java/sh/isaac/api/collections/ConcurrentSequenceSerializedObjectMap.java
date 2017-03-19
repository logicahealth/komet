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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataSerializer;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/18/14.
 *
 * @param <T>
 */
public class ConcurrentSequenceSerializedObjectMap<T> {
   private static final boolean storeObjects = false;
   private static final int     SEGMENT_SIZE = 12800;

   //~--- fields --------------------------------------------------------------

   ReentrantLock                  lock           = new ReentrantLock();
   byte[][][]                     objectByteList = new byte[1][][];
   CopyOnWriteArrayList<Object[]> objectListList = new CopyOnWriteArrayList<>();
   boolean[]                      changed        = new boolean[1];
   AtomicInteger                  maxSequence    = new AtomicInteger(-1);
   DataSerializer<T>              serializer;
   Path                           dbFolderPath;
   String                         folder;
   String                         suffix;

   //~--- constructors --------------------------------------------------------

   public ConcurrentSequenceSerializedObjectMap(DataSerializer<T> serializer,
         Path dbFolderPath,
         String folder,
         String suffix) {
      this.serializer   = serializer;
      this.dbFolderPath = dbFolderPath;
      this.folder       = folder;
      this.suffix       = suffix;
      objectByteList[0] = new byte[SEGMENT_SIZE][];
      objectListList.add(new Object[SEGMENT_SIZE]);
      changed[0] = false;
   }

   //~--- methods -------------------------------------------------------------

   public boolean containsKey(int sequence) {
      int segmentIndex   = sequence / SEGMENT_SIZE;
      int indexInSegment = sequence % SEGMENT_SIZE;

      if (segmentIndex >= objectByteList.length) {
         return false;
      }

      return objectByteList[segmentIndex][indexInSegment] != null;
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
            changed[segmentIndex]                        = true;
            return true;
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
   }

   public void read() {
      int segment  = 0;
      int segments = 1;

      while (segment < segments) {
         File segmentFile = new File(dbFolderPath.toFile(), folder + segment + suffix);

         try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(segmentFile)))) {
            segments = input.readInt();

            int segmentIndex       = input.readInt();
            int segmentArrayLength = input.readInt();
            int offset             = segmentIndex * segmentArrayLength;

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

   public void readSegment(Path dbFolderPath, String folder, String suffix) {}

   public void write() {
      int segments = objectByteList.length;

      for (int segmentIndex = 0; segmentIndex < segments; segmentIndex++) {
         File segmentFile = new File(dbFolderPath.toFile(), folder + segmentIndex + suffix);

         segmentFile.getParentFile()
                    .mkdirs();

         byte[][] segmentArray = objectByteList[segmentIndex];

         try (DataOutputStream output =
               new DataOutputStream(new BufferedOutputStream(new FileOutputStream(segmentFile)))) {
            output.writeInt(segments);
            output.writeInt(segmentIndex);
            output.writeInt(segmentArray.length);

            for (byte[] indexValue: segmentArray) {
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

   public void writeSegment(Path dbFolderPath, String folder, String suffix) {}

   private void expandMap(int segmentIndex) {
      if (segmentIndex >= objectByteList.length) {
         lock.lock();

         try {
            while (segmentIndex >= objectByteList.length) {
               changed                        = Arrays.copyOf(changed, objectByteList.length + 1);
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

   private boolean put(int sequence, byte[] value) {
      maxSequence.set(Math.max(sequence, maxSequence.get()));

      int segmentIndex = sequence / SEGMENT_SIZE;

      expandMap(segmentIndex);

      int indexInSegment = sequence % SEGMENT_SIZE;

      objectByteList[segmentIndex][indexInSegment] = value;
      return true;
   }

   //~--- get methods ---------------------------------------------------------

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

   public Stream<T> getParallelStream() {
      IntStream sequences = IntStream.rangeClosed(0, maxSequence.get())
                                     .parallel();

      return sequences.filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }

   public Stream<T> getParallelStream(IntPredicate sequenceFilter) {
      IntStream sequences = IntStream.rangeClosed(0, maxSequence.get())
                                     .parallel();

      return sequences.filter(sequenceFilter)
                      .filter(sequence -> containsKey(sequence))
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
      int segmentIndex   = sequence / SEGMENT_SIZE;
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
      return maxSequence.get() + 1;  // sequences start at zero, so size = max + 1...
   }

   public Stream<T> getStream() {
      IntStream sequences = IntStream.rangeClosed(0, maxSequence.get());

      return sequences.filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }

   public Stream<T> getStream(IntPredicate sequenceFilter) {
      IntStream sequences = IntStream.rangeClosed(0, maxSequence.get());

      return sequences.filter(sequenceFilter)
                      .filter(sequence -> containsKey(sequence))
                      .mapToObj(sequence -> getQuick(sequence));
   }
}

