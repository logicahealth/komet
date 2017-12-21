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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Use with circumstances where mapping all nids, not just a subset. 
 * @author kec
 */
public class SpinedNidIntMap {

   private static final int DEFAULT_ELEMENTS_PER_SPINE = 1024;
   private final int elementsPerSpine;
   private final ConcurrentMap<Integer, AtomicIntegerArray> spines = new ConcurrentHashMap<>();
   private final int INITIALIZATION_VALUE = Integer.MAX_VALUE;
   ConcurrentHashMap<Integer, AtomicLong> lastWrite = new ConcurrentHashMap<>();
   ConcurrentHashMap<Integer, AtomicLong> lastUpdate = new ConcurrentHashMap<>();


   public SpinedNidIntMap() {
      this.elementsPerSpine = DEFAULT_ELEMENTS_PER_SPINE;
   }

   public void read(File directory) {
      File[] files = directory.listFiles((pathname) -> {
         return pathname.getName().startsWith("spine-");
      });
      for (File spineFile : files) {
         int spine = Integer.parseInt(spineFile.getName().substring("spine-".length()));
         try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
            int arraySize = dis.readInt();
            int[] spineArray = new int[arraySize];
            for (int i = 0; i < arraySize; i++) {
               spineArray[i] = dis.readInt();
            }
            spines.put(spine, new AtomicIntegerArray(spineArray));
         } catch (IOException ex) {
            Logger.getLogger(SpinedByteArrayArrayMap.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   public boolean write(File directory) {
      directory.mkdirs();
      AtomicBoolean wroteAny = new AtomicBoolean(false);
      spines.forEach((Integer key, AtomicIntegerArray spine) -> {
         String spineKey = "spine-" + key;
         AtomicLong lastWriteSequence = lastWrite.computeIfAbsent(key, (t) -> new AtomicLong());
         AtomicLong lastUpdateSequence = lastUpdate.computeIfAbsent(key, (t) -> new AtomicLong());
         if (lastWriteSequence.get() < lastUpdateSequence.get()) {
            wroteAny.set(true);
            lastWriteSequence.set(lastUpdateSequence.get());
            File spineFile = new File(directory, spineKey);
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
               dos.writeInt(spine.length());
               for (int i = 0; i < spine.length(); i++) {
                  dos.writeInt(spine.get(i));
               }
            } catch (FileNotFoundException ex) {
               Logger.getLogger(SpinedByteArrayArrayMap.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
               Logger.getLogger(SpinedByteArrayArrayMap.class.getName()).log(Level.SEVERE, null, ex);
            }
         }

      });
      return wroteAny.get();
   }

      private int getSpineCount() {
      int spineCount = 0;
      for (Integer spineKey:  spines.keySet()) {
         spineCount = Math.max(spineCount, spineKey + 1);
      }
      return spineCount; 
   }

   public int sizeInBytes() {
      int sizeInBytes = 0;

      sizeInBytes = sizeInBytes + ((elementsPerSpine * 4) * getSpineCount());  // 4 bytes = bytes of 32 bit integer
      return sizeInBytes;
   }
   

   private AtomicIntegerArray newSpine(Integer spineKey) {
      int[] spine = new int[elementsPerSpine];
      Arrays.fill(spine, INITIALIZATION_VALUE);
      return new AtomicIntegerArray(spine);
   }
   
   public ConcurrentMap<Integer, AtomicIntegerArray> getSpines() {
      return spines;
   }

   public void put(int index, int element) {
      if (index < 0) {
         index = Integer.MAX_VALUE + index;
      }
      int spineIndex = index / elementsPerSpine;
      int indexInSpine = index % elementsPerSpine;
      if (spineIndex > this.spines.size() + 2) {
         throw new IllegalStateException("Trying to add spine: " + spineIndex + " for: " + index);
      }
      this.lastUpdate.computeIfAbsent(spineIndex, (t) -> new AtomicLong()).incrementAndGet();
      this.spines.computeIfAbsent(spineIndex, this::newSpine).set(indexInSpine, element);
   }

   public int get(int index) {
      if (index < 0) {
         index = Integer.MAX_VALUE + index;
      }
      int spineIndex = index / elementsPerSpine;
      int indexInSpine = index % elementsPerSpine;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine);
   }

   public int getAndUpdate(int index, IntUnaryOperator generator) {
       if (index < 0) {
         index = Integer.MAX_VALUE + index;
      }
     int spineIndex = index / elementsPerSpine;
      int indexInSpine = index % elementsPerSpine;
      this.lastUpdate.computeIfAbsent(spineIndex, (t) -> new AtomicLong()).incrementAndGet();
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).updateAndGet(indexInSpine, generator);
   }

   public boolean containsKey(int index) {
      if (index < 0) {
         index = Integer.MAX_VALUE + index;
      }
      int spineIndex = index / elementsPerSpine;
      int indexInSpine = index % elementsPerSpine;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine) != INITIALIZATION_VALUE;
   }

   public void forEach(Processor processor) {
      int currentSpineCount = getSpineCount();
      int key = 0;
      for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
         AtomicIntegerArray spine = this.spines.computeIfAbsent(spineIndex, this::newSpine);
         for (int indexInSpine = 0; indexInSpine < elementsPerSpine; indexInSpine++) {
            int value = spine.get(indexInSpine);
            if (value != INITIALIZATION_VALUE) {
               processor.process(key, value);
            }
         }
         key++;
      }
   }
   
   public IntStream keyStream() {
      final Supplier<? extends Spliterator.OfInt> streamSupplier = this.getKeySpliterator();

      return StreamSupport.intStream(streamSupplier, streamSupplier.get()
              .characteristics(), false);
   }

   public IntStream valueStream() {
      final Supplier<? extends Spliterator.OfInt> streamSupplier = this.getValueSpliterator();

      return StreamSupport.intStream(streamSupplier, streamSupplier.get()
              .characteristics(), false);
   }

   public void addSpine(int spineKey, AtomicIntegerArray spineData) {
      spines.put(spineKey, spineData);
   }

   public interface Processor {

      public void process(int key, int value);
   }

   /**
    * Gets the value spliterator.
    *
    * @return the supplier<? extends spliterator. of int>
    */
   protected Supplier<? extends Spliterator.OfInt> getValueSpliterator() {
      return new ValueSpliteratorSupplier();
   }
  /**
    * Gets the value spliterator.
    *
    * @return the supplier<? extends spliterator. of int>
    */
   protected Supplier<? extends Spliterator.OfInt> getKeySpliterator() {
      return new KeySpliteratorSupplier();
   }

   /**
    * The Class KeySpliteratorSupplier.
    */
   private class KeySpliteratorSupplier
           implements Supplier<Spliterator.OfInt> {

      /**
       * Gets the.
       *
       * @return the spliterator of int
       */
      @Override
      public Spliterator.OfInt get() {
         return new SpinedKeySpliterator();
      }
   }

   /**
    * The Class ValueSpliteratorSupplier.
    */
   private class ValueSpliteratorSupplier
           implements Supplier<Spliterator.OfInt> {

      /**
       * Gets the.
       *
       * @return the spliterator of int
       */
      @Override
      public Spliterator.OfInt get() {
         return new SpinedValueSpliterator();
      }
   }

   private class SpinedValueSpliterator implements Spliterator.OfInt {
      int end;
      int currentPosition;

      public SpinedValueSpliterator() {
         this.end = DEFAULT_ELEMENTS_PER_SPINE * getSpineCount();
         this.currentPosition = 0;
      }
      
      public SpinedValueSpliterator(int start, int end) {
         this.currentPosition = start;
         this.end = end;
      }
      
      @Override
      public Spliterator.OfInt trySplit() {
         int splitEnd = end;
         int split = end - currentPosition;
         int half = split / 2;
         this.end = currentPosition + half;
         return new SpinedValueSpliterator(currentPosition + half + 1, splitEnd);
      }

      @Override
      public boolean tryAdvance(IntConsumer action) {
         while (currentPosition < end) {
            int value = get(currentPosition++);
            if (value != INITIALIZATION_VALUE) {
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
   
   

   private class SpinedKeySpliterator implements Spliterator.OfInt {
      int end;
      int currentPosition;

      public SpinedKeySpliterator() {
         this.end = DEFAULT_ELEMENTS_PER_SPINE * getSpineCount();
         this.currentPosition = 0;
      }
      
      public SpinedKeySpliterator(int start, int end) {
         this.currentPosition = start;
         this.end = end;
      }
      
      @Override
      public Spliterator.OfInt trySplit() {
         int splitEnd = end;
         int split = end - currentPosition;
         int half = split / 2;
         this.end = currentPosition + half;
         return new SpinedValueSpliterator(currentPosition + half + 1, splitEnd);
      }

      @Override
      public boolean tryAdvance(IntConsumer action) {
         while (currentPosition < end) {
            int key = currentPosition++;
            int value = get(key);
            if (value != INITIALIZATION_VALUE) {
               action.accept(key);
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
                | Spliterator.SIZED | Spliterator.SORTED;
     }

   }   
}
