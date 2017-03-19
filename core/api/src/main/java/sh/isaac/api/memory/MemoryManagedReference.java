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



package sh.isaac.api.memory;

//~--- JDK imports ------------------------------------------------------------

import java.io.*;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import java.time.Duration;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.HashFunctions;

import sh.isaac.api.DataSerializer;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 4/10/15.
 * @param <T>
 */
public class MemoryManagedReference<T extends Object>
        extends SoftReference<T>
         implements Comparable<MemoryManagedReference> {
   private static final AtomicInteger objectIdSupplier          = new AtomicInteger();
   private static final AtomicInteger referenceSequenceSupplier = new AtomicInteger(Integer.MIN_VALUE + 1);

   //~--- fields --------------------------------------------------------------

   private final int                objectId                  = objectIdSupplier.getAndIncrement();
   private int                      lastWriteToDiskSequence   = referenceSequenceSupplier.getAndIncrement();
   private long                     lastWriteToDiskTime       = System.currentTimeMillis();
   private int                      lastElementUpdateSequence = Integer.MIN_VALUE;
   private long                     lastElementUpdateTime     = System.currentTimeMillis();
   private long                     lastElementReadTime       = Long.MIN_VALUE;
   private final AtomicReference<T> strongReferenceForUpdate  = new AtomicReference<>();
   private final AtomicReference<T> strongReferenceForCache   = new AtomicReference<>();
   private final LongAdder          hits                      = new LongAdder();
   private final AtomicInteger      cacheCount                = new AtomicInteger();
   private final File               diskLocation;
   private final DataSerializer<T>  serializer;

   //~--- constructors --------------------------------------------------------

   public MemoryManagedReference(T referent, File diskLocation, DataSerializer<T> serializer) {
      super(referent);
      this.diskLocation = diskLocation;
      this.serializer   = serializer;
   }

   public MemoryManagedReference(T referent,
                                 ReferenceQueue<? super T> q,
                                 File diskLocation,
                                 DataSerializer<T> serializer) {
      super(referent, q);
      this.diskLocation = diskLocation;
      this.serializer   = serializer;
   }

   //~--- methods -------------------------------------------------------------

   public void cacheEntry() {
      int count = cacheCount.incrementAndGet();

      if (count == 1) {
         strongReferenceForCache.set(this.get());
      }
   }

   public void cacheExit() {
      int count = cacheCount.decrementAndGet();

      if (count == 0) {
         strongReferenceForCache.set(null);
      }
   }

   @Override
   public int compareTo(MemoryManagedReference o) {
      return this.objectId - o.objectId;
   }

   public void elementRead() {
      hits.increment();
      this.lastElementReadTime = System.currentTimeMillis();
   }

   public void elementUpdated() {
      this.strongReferenceForUpdate.set(this.get());
      lastElementUpdateSequence = referenceSequenceSupplier.getAndIncrement();
      lastElementUpdateTime     = System.currentTimeMillis();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      MemoryManagedReference<?> that = (MemoryManagedReference<?>) o;

      return objectId == that.objectId;
   }

   @Override
   public int hashCode() {
      return HashFunctions.hash(objectId);
   }

   public long msSinceLastUnwrittenUpdate() {
      return lastElementUpdateTime - lastWriteToDiskTime;
   }

   public Duration timeSinceLastRead() {
      return Duration.ofMillis(System.currentTimeMillis() - lastElementReadTime);
   }

   public void write() {
      T objectToWrite = strongReferenceForUpdate.get();

      if (objectToWrite != null) {
         strongReferenceForUpdate.set(null);
         DiskSemaphore.acquire();
         lastWriteToDiskSequence = referenceSequenceSupplier.getAndIncrement();
         lastWriteToDiskTime     = System.currentTimeMillis();
         diskLocation.getParentFile()
                     .mkdirs();

         try (DataOutputStream out =
               new DataOutputStream(new BufferedOutputStream(new FileOutputStream(diskLocation)))) {
            serializer.serialize(out, objectToWrite);
         } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
         } catch (IOException e) {
            throw new RuntimeException(e);
         } finally {
            DiskSemaphore.release();
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public long getLastWriteToDiskTime() {
      return lastWriteToDiskTime;
   }

   public boolean hasUnwrittenUpdate() {
      return lastWriteToDiskSequence < lastElementUpdateSequence;
   }
}

