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
package sh.isaac.api.collections;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <E> Type of {@code Object} that are the values of the map.
 */
public class ConcurrentSequenceObjectMap<E> {
   private static final int SEGMENT_SIZE = 1280;

   //~--- fields --------------------------------------------------------------

   ReentrantLock                                 lock           = new ReentrantLock();
   CopyOnWriteArrayList<AtomicReferenceArray<E>> objectListList = new CopyOnWriteArrayList<>();
   AtomicInteger                                 maxSequence    = new AtomicInteger(0);

   //~--- constructors --------------------------------------------------------

   public ConcurrentSequenceObjectMap() {
      this.objectListList.add(new AtomicReferenceArray<>(SEGMENT_SIZE));
   }

   //~--- methods -------------------------------------------------------------

   public void clear() {
      this.objectListList.clear();
      this.objectListList.add(new AtomicReferenceArray<>(SEGMENT_SIZE));
      this.maxSequence.set(0);
   }

   public boolean containsKey(int sequence) {
      final int segmentIndex   = sequence / SEGMENT_SIZE;
      final int indexInSegment = sequence % SEGMENT_SIZE;

      if (segmentIndex >= this.objectListList.size()) {
         return false;
      }

      return this.objectListList.get(segmentIndex)
                           .get(indexInSegment) != null;
   }

   public E put(int sequence, E value) {
      this.maxSequence.set(Math.max(sequence, this.maxSequence.get()));

      final int segmentIndex = sequence / SEGMENT_SIZE;

      if (segmentIndex >= this.objectListList.size()) {
         this.lock.lock();

         try {
            while (segmentIndex >= this.objectListList.size()) {
               this.objectListList.add(new AtomicReferenceArray<>(SEGMENT_SIZE));
            }
         } finally {
            this.lock.unlock();
         }
      }

      final int indexInSegment = sequence % SEGMENT_SIZE;

      if (this.objectListList.get(segmentIndex)
                        .compareAndSet(indexInSegment, (E) null, value)) {
         return value;
      }

      return this.objectListList.get(segmentIndex)
                           .get(indexInSegment);
   }

   //~--- get methods ---------------------------------------------------------

   public Optional<E> get(int sequence) {
      final int segmentIndex = sequence / SEGMENT_SIZE;

      if (segmentIndex >= this.objectListList.size()) {
         return Optional.empty();
      }

      final int indexInSegment = sequence % SEGMENT_SIZE;

      return Optional.ofNullable(this.objectListList.get(segmentIndex)
            .get(indexInSegment));
   }

   /**
    * Provides no range or null checking. For use with a stream that already
    * filters out null values and out of range sequences.
    *
    * @param sequence
    * @return
    */
   private E getQuick(int sequence) {
      final int segmentIndex   = sequence / SEGMENT_SIZE;
      final int indexInSegment = sequence % SEGMENT_SIZE;

      return this.objectListList.get(segmentIndex)
                               .get(indexInSegment);
   }

   public IntStream getSequences() {
      final int               maxSize = this.maxSequence.get();
      final IntStream.Builder builder = IntStream.builder();

      for (int i = 0; i < maxSize; i++) {
         final int segmentIndex   = i / SEGMENT_SIZE;
         final int indexInSegment = i % SEGMENT_SIZE;

         if (this.objectListList.get(segmentIndex)
                           .get(indexInSegment) != null) {
            builder.accept(i);
         }
      }

      return builder.build();
   }
}

