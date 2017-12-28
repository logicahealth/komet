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

import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import sh.isaac.model.ModelGet;

/**
 *
 * @author kec
 * @param <E> the generic type for the spined list. 
 */
 public class SpinedIntObjectMap<E>   {
   private static final int DEFAULT_SPINE_SIZE = 1024;
   protected final int spineSize;
   protected final ConcurrentMap<Integer, AtomicReferenceArray<E>> spines = new ConcurrentHashMap<>();
   private Function<E,String> elementStringConverter;

   public void setElementStringConverter(Function<E, String> elementStringConverter) {
      this.elementStringConverter = elementStringConverter;
   }
   
   public void clear() {
       spines.clear();
   }
   
   public void printToConsole() {
      if (elementStringConverter != null) {
         forEach((key, value) -> {
            System.out.println(key + ": " + elementStringConverter.apply(value));
         });
      } else {
         forEach((key, value) -> {
            System.out.println(key + ": " + value);
         });
      }
   }

   public SpinedIntObjectMap() {
      this.spineSize = DEFAULT_SPINE_SIZE;
   }
   
   public SpinedIntObjectMap(int size, Supplier<E> supplier) {
      this.spineSize = DEFAULT_SPINE_SIZE;
      for (int i = 0; i < size; i++) {
         put(i, supplier.get());
      }
   }
   private int getSpineCount() {
      int spineCount = 0;
      for (Integer spineKey:  spines.keySet()) {
         spineCount = Math.max(spineCount, spineKey + 1);
      }
      return spineCount; 
   }
   

   protected AtomicReferenceArray<E> newSpine(Integer spineKey) {
      AtomicReferenceArray<E> spine = new AtomicReferenceArray(spineSize);
      return spine;
   }

   public void put(int index, E element) {
      if (index < 0) {
         index = ModelGet.identifierService().getElementSequenceForNid(index);
      } 
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      this.spines.computeIfAbsent(spineIndex, this::newSpine).set(indexInSpine, element);
   }

   public E get(int index) {
      if (index < 0) {
         index = ModelGet.identifierService().getElementSequenceForNid(index);
      }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine);
   }

   public Optional<E> getOptional(int index) {
      if (index < 0) {
         index = ModelGet.identifierService().getElementSequenceForNid(index);
      }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return Optional.ofNullable(this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine));
   }

   public boolean containsKey(int index) {
      if (index < 0) {
         index = ModelGet.identifierService().getElementSequenceForNid(index);
      }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine) != null;
   }
   
   public int size() {
       int size = 0;
      int currentSpineCount = getSpineCount();
      for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
         AtomicReferenceArray<E> spine = this.spines.computeIfAbsent(spineIndex, this::newSpine);
         for (int indexInSpine = 0; indexInSpine < spineSize; indexInSpine++) {
            E element = spine.get(indexInSpine);
            if (element != null) {
               size++;
            }
         }       
      }
      return size;
   }
   
   public void forEach(Processor<E> processor) {
      int currentSpineCount = getSpineCount();
      int key = 0;
      for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
         AtomicReferenceArray<E> spine = this.spines.computeIfAbsent(spineIndex, this::newSpine);
         for (int indexInSpine = 0; indexInSpine < spineSize; indexInSpine++) {
            E element = spine.get(indexInSpine);
            if (element != null) {
               processor.process(key, (E) element);
            }
            key++;
         }
         
      }
   } 
   public E accumulateAndGet(int index, E x, BinaryOperator<E> accumulatorFunction) {
      if (index < 0) {
         index = ModelGet.identifierService().getElementSequenceForNid(index);
      }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine)
              .accumulateAndGet(indexInSpine, x, accumulatorFunction);
      
   }
   
   public interface Processor<E> {
      public void process(int key, E value);
   }

   public Stream<E> stream() {
      final Supplier<? extends Spliterator<E>> streamSupplier = this.get();

      return StreamSupport.stream(streamSupplier, streamSupplier.get()
              .characteristics(), false);
   }

   /**
    * Gets the.
    *
    * @return the supplier<? extends spliterator. of int>
    */
   protected Supplier<? extends Spliterator<E>> get() {
      return new SpliteratorSupplier();
   }

   /**
    * The Class SpliteratorSupplier.
    */
   private class SpliteratorSupplier
           implements Supplier<Spliterator<E>> {

      /**
       * Gets the.
       *
       * @return the spliterator
       */
      @Override
      public Spliterator<E> get() {
         return new SpinedValueSpliterator();
      }
   }

   private class SpinedValueSpliterator implements Spliterator<E> {
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
      public Spliterator<E> trySplit() {
         int splitEnd = end;
         int split = end - currentPosition;
         int half = split / 2;
         this.end = currentPosition + half;
         return new SpinedValueSpliterator(currentPosition + half + 1, splitEnd);
      }

      @Override
      public boolean tryAdvance(Consumer<? super E> action) {
         while (currentPosition < end) {
            E value = get(currentPosition++);
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
