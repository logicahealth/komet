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
package sh.isaac.api.collections;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.LongUnaryOperator;

/**
 *
 * @author kec
 */
public class SpinedIntLongMap {
  private static final int DEFAULT_SPINE_SIZE = 1024;
   private final int spineSize;
   private final ConcurrentMap<Integer, AtomicLongArray> spines = new ConcurrentHashMap<>();
   private final long INITIAL_VALUE = Long.MAX_VALUE;
   private final AtomicInteger spineCount = new AtomicInteger();

   public SpinedIntLongMap() {
      this.spineSize = DEFAULT_SPINE_SIZE;
   }
   
   private AtomicLongArray newSpine(Integer spineKey) {
      spineCount.set(Math.max(spineKey + 1, spineCount.get()));
      long[] spine = new long[spineSize];
      Arrays.fill(spine, INITIAL_VALUE);
      return new AtomicLongArray(spine);
   }

   public void put(int index, int element) {
      if (index < 0) {
         throw new ArrayIndexOutOfBoundsException("Index: " + index);
      }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      this.spines.computeIfAbsent(spineIndex, this::newSpine).set(indexInSpine, element);
   }

   public long get(int index) {
      if (index < 0) {
         throw new ArrayIndexOutOfBoundsException("Index: " + index);
      }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine);
   }

   public boolean containsKey(int index) {
      if (index < 0) {
         throw new ArrayIndexOutOfBoundsException("Index: " + index);
      }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine) != INITIAL_VALUE;
   }
   
   public long getAndUpdate(int index, LongUnaryOperator generator) {
      if (index < 0) {
         throw new ArrayIndexOutOfBoundsException("Index: " + index);
      }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).updateAndGet(indexInSpine, generator);
   }
   
   public void forEach(Processor processor) {
      int currentSpineCount = spineCount.get();
      int key = 0;
      for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
         AtomicLongArray spine = this.spines.computeIfAbsent(spineIndex, this::newSpine);
         for (int indexInSpine = 0; indexInSpine < spineSize; indexInSpine++) {
            long value = spine.get(indexInSpine);
            if (value != INITIAL_VALUE) {
               processor.process(key, value);
            }
         }
         key++;
      }
   } 
   
   public interface Processor {
      public void process(int key, long value);
   }   
}
