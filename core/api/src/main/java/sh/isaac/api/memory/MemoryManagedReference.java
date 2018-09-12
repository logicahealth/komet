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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
 *
 * @param <T> the generic type
 */
public class MemoryManagedReference<T extends DataSerializer<T>>
        extends SoftReference<T>
         implements Comparable<MemoryManagedReference<T>> {
   /** The Constant objectIdSupplier. */
   private static final AtomicInteger OBJECT_ID_SUPPLIER = new AtomicInteger();

   /** The Constant referenceSequenceSupplier. */
   private static final AtomicInteger REFERENCE_SEQUENCE_SUPPLIER = new AtomicInteger(Integer.MIN_VALUE + 1);

   //~--- fields --------------------------------------------------------------

   /** The object id. */
   private final int objectId = OBJECT_ID_SUPPLIER.getAndIncrement();

   /** The last write to disk sequence. */
   private int lastWriteToDiskSequence = REFERENCE_SEQUENCE_SUPPLIER.getAndIncrement();

   /** The last write to disk time. */
   private long lastWriteToDiskTime = System.currentTimeMillis();

   /** The last element update sequence. */
   private int lastElementUpdateSequence = Integer.MIN_VALUE;

   /** The last element update time. */
   private long lastElementUpdateTime = System.currentTimeMillis();

   /** The last element read time. */
   private long lastElementReadTime = Long.MIN_VALUE;

   /** The strong reference for update. */
   private final AtomicReference<T> strongReferenceForUpdate = new AtomicReference<>();

   /** The strong reference for cache. */
   private final AtomicReference<T> strongReferenceForCache = new AtomicReference<>();

   /** The hits. */
   private final LongAdder hits = new LongAdder();

   /** The cache count. */
   private final AtomicInteger cacheCount = new AtomicInteger();

   /** The disk location. */
   private final File diskLocation;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new memory managed reference.
    *
    * @param referent the referent
    * @param diskLocation the disk location
    */
   public MemoryManagedReference(T referent, File diskLocation) {
      super(referent);
      this.diskLocation = diskLocation;
   }

   /**
    * Instantiates a new memory managed reference.
    *
    * @param referent the referent
    * @param q the q
    * @param diskLocation the disk location
    */
   public MemoryManagedReference(T referent,
                                 ReferenceQueue<? super T> q,
                                 File diskLocation) {
      super(referent, q);
      this.diskLocation = diskLocation;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Cache entry.
    */
   public void cacheEntry() {
      final int count = this.cacheCount.incrementAndGet();

      if (count == 1) {
         this.strongReferenceForCache.set(this.get());
      }
   }

   /**
    * Cache exit.
    */
   public void cacheExit() {
      final int count = this.cacheCount.decrementAndGet();

      if (count == 0) {
         this.strongReferenceForCache.set(null);
      }
   }

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   @Override
   public int compareTo(MemoryManagedReference<T> o) {
      return this.objectId - o.objectId;
   }

   /**
    * Element read.
    */
   public void elementRead() {
      this.hits.increment();
      this.lastElementReadTime = System.currentTimeMillis();
   }

   /**
    * Element updated.
    */
   public void elementUpdated() {
      this.strongReferenceForUpdate.set(this.get());
      this.lastElementUpdateSequence = REFERENCE_SEQUENCE_SUPPLIER.getAndIncrement();
      this.lastElementUpdateTime     = System.currentTimeMillis();
   }

   /**
    * Equals.
    *
    * @param o the o
    * @return true, if successful
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      final MemoryManagedReference<?> that = (MemoryManagedReference<?>) o;

      return this.objectId == that.objectId;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return HashFunctions.hash(this.objectId);
   }

   /**
    * Ms since last unwritten update.
    *
    * @return the long
    */
   public long msSinceLastUnwrittenUpdate() {
      return this.lastElementUpdateTime - this.lastWriteToDiskTime;
   }

   /**
    * Time since last read.
    *
    * @return the duration
    */
   public Duration timeSinceLastRead() {
      return Duration.ofMillis(System.currentTimeMillis() - this.lastElementReadTime);
   }

   /**
    * Write.
    */
   public void write() {
      final T objectToWrite = this.strongReferenceForUpdate.get();

      if (objectToWrite != null) {
         this.strongReferenceForUpdate.set(null);
         DiskSemaphore.acquire();
         this.lastWriteToDiskSequence = REFERENCE_SEQUENCE_SUPPLIER.getAndIncrement();
         this.lastWriteToDiskTime     = System.currentTimeMillis();
         this.diskLocation.getParentFile()
                          .mkdirs();

         try (DataOutputStream out =
               new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.diskLocation)))) {
            objectToWrite.serialize(out);
         } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
         } catch (final IOException e) {
            throw new RuntimeException(e);
         } finally {
            DiskSemaphore.release();
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the last write to disk time.
    *
    * @return the last write to disk time
    */
   public long getLastWriteToDiskTime() {
      return this.lastWriteToDiskTime;
   }

   /**
    * Checks for unwritten update.
    *
    * @return true, if successful
    */
   public boolean hasUnwrittenUpdate() {
      return this.lastWriteToDiskSequence < this.lastElementUpdateSequence;
   }
}

