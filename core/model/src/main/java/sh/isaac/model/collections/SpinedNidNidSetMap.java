/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.collections;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.mahout.math.list.IntArrayList;

/**
 *
 * @author kec
 */
public class SpinedNidNidSetMap {

   private static final int DEFAULT_SPINE_SIZE = 1024;
   protected final int spineSize;
   protected final ConcurrentMap<Integer, AtomicReferenceArray<int[]>> spines = new ConcurrentHashMap<>();
   private Function<int[], String> elementStringConverter;

   ConcurrentHashMap<String, AtomicLong> lastWrite = new ConcurrentHashMap<>();
   ConcurrentHashMap<String, AtomicLong> lastUpdate = new ConcurrentHashMap<>();

   public SpinedNidNidSetMap() {
      this.spineSize = DEFAULT_SPINE_SIZE;
   }

   public void read(File directory) {
      File[] files = directory.listFiles((pathname) -> {
         return pathname.getName().startsWith("spine-");
      });
      for (File spineFile : files) {
         int spine = Integer.parseInt(spineFile.getName().substring("spine-".length()));
         try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
            int arraySize = dis.readInt();
            int offset = arraySize * spine;
            for (int i = 0; i < arraySize; i++) {
               int valueSize = dis.readInt();
               if (valueSize != 0) {
                  int[] value = new int[valueSize];
                  put(offset + i, value);
                  for (int j = 0; j < valueSize; j++) {
                     value[j] = dis.readInt();
                  }
               }
            }
         } catch (IOException ex) {
            Logger.getLogger(SpinedByteArrayArrayMap.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   public void write(File directory) {
      spines.forEach((Integer key, AtomicReferenceArray<int[]> spine) -> {
         String spineKey = "spine-" + key;
         AtomicLong lastWriteSequence = lastWrite.computeIfAbsent(spineKey, (t) -> new AtomicLong());
         AtomicLong lastUpdateSequence = lastUpdate.computeIfAbsent(spineKey, (t) -> new AtomicLong());
         if (lastWriteSequence.get() < lastUpdateSequence.get()) {
            lastWriteSequence.set(lastUpdateSequence.get());
            File spineFile = new File(directory, spineKey);
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
               dos.writeInt(spine.length());
               for (int i = 0; i < spine.length(); i++) {
                  int[] value = spine.get(i);
                  if (value == null) {
                     dos.writeInt(0);
                  } else {
                     dos.writeInt(value.length);
                     for (int valueElement : value) {
                        dos.writeInt(valueElement);
                     }
                  }
               }
            } catch (FileNotFoundException ex) {
               Logger.getLogger(SpinedByteArrayArrayMap.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
               Logger.getLogger(SpinedByteArrayArrayMap.class.getName()).log(Level.SEVERE, null, ex);
            }
         }

      });
   }

   public synchronized void add(int index, int element) {
      if (index < 0) {
         index = Integer.MAX_VALUE + index;
      } else {
         throw new UnsupportedOperationException("index >= 0: " + index);
      }
      int spineIndex = index / spineSize;
      int indexInSpine = index % spineSize;
      this.lastUpdate.computeIfAbsent("spine-" + spineIndex, (t) -> new AtomicLong()).incrementAndGet();
      this.spines.computeIfAbsent(spineIndex, this::newSpine).accumulateAndGet(indexInSpine, new int[]{element}, this::merge);
   }

   private int[] merge(int[] currentArray, int[] updateArray) {
      if (currentArray == null || currentArray.length == 0) {
         return updateArray;
      }
      if (updateArray == null) {
         throw new IllegalStateException("Update value is null");
      }
      if (updateArray.length == 1) {
         int updateValue = updateArray[0];
         int searchResult = Arrays.binarySearch(currentArray, updateValue);
         if (searchResult >= 0) {
            return currentArray; // already there. 
         }
         int[] array2 = new int[currentArray.length + 1];
         int insertIndex = -searchResult - 1;
         System.arraycopy(currentArray, 0, array2, 0, insertIndex);
         System.arraycopy(currentArray, insertIndex, array2, insertIndex + 1, currentArray.length - insertIndex);
         array2[insertIndex] = updateValue;
         return array2;
      }
      Arrays.sort(updateArray);
      IntArrayList mergedValues = new IntArrayList(currentArray.length + updateArray.length);
      int updateIndex = 0;
      int currentIndex = 0;

      while (updateIndex < updateArray.length || currentIndex < currentArray.length) {
         int compare = Integer.compare(currentArray[currentIndex], updateArray[updateIndex]);
         if (compare == 0) {
            mergedValues.add(currentArray[currentIndex]);
            currentIndex++;
            updateIndex++;
            if (currentIndex == currentArray.length) {
               while (updateIndex < updateArray.length) {
                  mergedValues.add(updateArray[updateIndex++]);
               }
            }
            if (updateIndex == updateArray.length) {
               while (currentIndex < currentArray.length) {
                  mergedValues.add(currentArray[currentIndex++]);
               }
            }
         } else if (compare < 0) {
            mergedValues.add(currentArray[currentIndex]);
            currentIndex++;
            if (currentIndex == currentArray.length) {
               while (updateIndex < updateArray.length) {
                  mergedValues.add(updateArray[updateIndex++]);
               }
            }
         } else {
            mergedValues.add(updateArray[updateIndex]);
            updateIndex++;
            if (updateIndex == updateArray.length) {
               while (currentIndex < currentArray.length) {
                  mergedValues.add(currentArray[currentIndex++]);
               }
            }
         }
      }
      return mergedValues.toArray(new int[mergedValues.size()]);
   }

   private int getSpineCount() {
      int spineCount = 0;
      for (Integer spineKey : spines.keySet()) {
         spineCount = Math.max(spineCount, spineKey + 1);
      }
      return spineCount;
   }

   protected AtomicReferenceArray<int[]> newSpine(Integer spineKey) {
      AtomicReferenceArray<int[]> spine = new AtomicReferenceArray(spineSize);
      return spine;
   }

   private void put(int index, int[] element) {
      if (index < 0) {
         index = Integer.MAX_VALUE + index;
      }
      int spineIndex = index / spineSize;
      int indexInSpine = index % spineSize;
      this.lastUpdate.computeIfAbsent("spine-" + spineIndex, (t) -> new AtomicLong()).incrementAndGet();
      this.spines.computeIfAbsent(spineIndex, this::newSpine).accumulateAndGet(indexInSpine, element, this::merge);
   }

   public int[] get(int index) {
      if (index < 0) {
         index = Integer.MAX_VALUE + index;
      }
      int spineIndex = index / spineSize;
      int indexInSpine = index % spineSize;
      int[] result = this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine);
      if (result == null) {
         return new int[]{};
      }
      return result;
   }

   public boolean containsKey(int index) {
      if (index < 0) {
         index = Integer.MAX_VALUE + index;
      } else {
         throw new UnsupportedOperationException("index >= 0: " + index);
      }
      int spineIndex = index / spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine) != null;
   }

   public void forEach(Processor<int[]> processor) {
      int currentSpineCount = getSpineCount();
      int key = 0;
      for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
         AtomicReferenceArray<int[]> spine = this.spines.computeIfAbsent(spineIndex, this::newSpine);
         for (int indexInSpine = 0; indexInSpine < spineSize; indexInSpine++) {
            int[] element = spine.get(indexInSpine);
            if (element != null) {
               processor.process(key, element);
            }
            key++;
         }

      }
   }

   public interface Processor<E> {

      public void process(int key, E value);
   }

   public Stream<int[]> stream() {
      final Supplier<? extends Spliterator<int[]>> streamSupplier = this.get();

      return StreamSupport.stream(streamSupplier, streamSupplier.get()
              .characteristics(), false);
   }

   /**
    * Gets the.
    *
    * @return the supplier<? extends spliterator. of int>
    */
   protected Supplier<? extends Spliterator<int[]>> get() {
      return new SpliteratorSupplier();
   }

   /**
    * The Class SpliteratorSupplier.
    */
   private class SpliteratorSupplier
           implements Supplier<Spliterator<int[]>> {

      /**
       * Gets the.
       *
       * @return the spliterator
       */
      @Override
      public Spliterator<int[]> get() {
         return new SpinedValueSpliterator();
      }
   }

   private class SpinedValueSpliterator implements Spliterator<int[]> {

      int end;
      int currentPosition;

      public SpinedValueSpliterator() {
         this.end = DEFAULT_SPINE_SIZE * getSpineCount();
         this.currentPosition = 0;
      }

      public SpinedValueSpliterator(int start, int end) {
         this.currentPosition = start;
         this.end = end;
      }

      @Override
      public Spliterator<int[]> trySplit() {
         int splitEnd = end;
         int split = end - currentPosition;
         int half = split / 2;
         this.end = currentPosition + half;
         return new SpinedValueSpliterator(currentPosition + half + 1, splitEnd);
      }

      @Override
      public boolean tryAdvance(Consumer<? super int[]> action) {
         while (currentPosition < end) {
            int[] value = get(currentPosition++);
            if (value != null) {
               action.accept(value);
               return true;
            }
         }
         return false;
      }

      @Override
      public long estimateSize() {
         return end - currentPosition;
      }

      @Override
      public int characteristics() {
         return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                 | Spliterator.SIZED;
      }

   }
}
