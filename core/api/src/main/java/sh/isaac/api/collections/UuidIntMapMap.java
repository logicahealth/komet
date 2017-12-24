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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.collections.uuidnidmap.ConcurrentUuidToIntHashMap;
import sh.isaac.api.collections.uuidnidmap.UuidToIntMap;
import sh.isaac.api.memory.DiskSemaphore;
import sh.isaac.api.memory.HoldInMemoryCache;
import sh.isaac.api.memory.MemoryManagedReference;
import sh.isaac.api.memory.WriteToDiskCache;
import sh.isaac.api.util.UUIDUtil;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 7/27/14.
 */
public class UuidIntMapMap
         implements UuidToIntMap {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /** The Constant DEFAULT_TOTAL_MAP_SIZE. */
   private static final int DEFAULT_TOTAL_MAP_SIZE = 15000000;

   /** The Constant NUMBER_OF_MAPS. */
   public static final int NUMBER_OF_MAPS = 256;

   /** The nid to uuid cache size. */
   public static int NID_TO_UUID_CACHE_SIZE = 0;  // defaults to disabled / not normally used.

   // Loader utility code sets this to a much larger value, as there is no alternate cache to get from nid back to UUID
   // when the data isn't being written to the DB.

   /** The Constant DEFAULT_MAP_SIZE. */
   private static final int DEFAULT_MAP_SIZE = DEFAULT_TOTAL_MAP_SIZE / NUMBER_OF_MAPS;

   /** The Constant MIN_LOAD_FACTOR. */
   private static final double MIN_LOAD_FACTOR = 0.75;

   /** The Constant MAX_LOAD_FACTOR. */
   private static final double MAX_LOAD_FACTOR = 0.9;

   /** The Constant NEXT_NID_PROVIDER. */
   private static final AtomicInteger NEXT_NID_PROVIDER = new AtomicInteger(Integer.MIN_VALUE);

   /** The Constant SERIALIZER. */
   private static final ConcurrentUuidIntMapSerializer SERIALIZER = new ConcurrentUuidIntMapSerializer();

   //~--- fields --------------------------------------------------------------

   /** The shutdown. */
   public boolean shutdown = false;

   /** The maps. */
   private final MemoryManagedReference<ConcurrentUuidToIntHashMap>[] maps = new MemoryManagedReference[NUMBER_OF_MAPS];

   /** The nid to primoridial cache. */
   private LruCache<Integer, UUID[]> nidToPrimoridialCache = null;

   /** The lock. */
   ReentrantLock lock = new ReentrantLock();

   /** The folder. */
   private final File folder;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new uuid int map map.
    *
    * @param folder the folder
    */
   private UuidIntMapMap(File folder) {
      folder.mkdirs();
      this.folder = folder;

      for (int i = 0; i < this.maps.length; i++) {
         this.maps[i] = new MemoryManagedReference<>(null, new File(folder, i + "-uuid-nid.map"), SERIALIZER);
         WriteToDiskCache.addToCache(this.maps[i]);
      }

      if (NID_TO_UUID_CACHE_SIZE > 0) {
         this.nidToPrimoridialCache = new LruCache<>(NID_TO_UUID_CACHE_SIZE);
      }

      LOG.debug("Created UuidIntMapMap: " + this);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * This method is an optimization for loader patterns, where it can be faster to read the nid to UUID from this cache,
    * but only if the cache actually as the value.
    *
    * @param nid the nid
    * @return true, if successful
    */
   public boolean cacheContainsNid(int nid) {
      if (this.nidToPrimoridialCache != null) {
         return this.nidToPrimoridialCache.containsKey(nid);
      }

      return false;
   }

   /**
    * Contains key.
    *
    * @param key the key
    * @return true, if successful
    */
   @Override
   public boolean containsKey(UUID key) {
      return getMap(key).containsKey(key);
   }

   /**
    * Contains value.
    *
    * @param value the value
    * @return true, if successful
    */
   @Override
   public boolean containsValue(int value) {
      throw new UnsupportedOperationException();
   }

   /**
    * Creates the.
    *
    * @param folder the folder
    * @return the uuid int map map
    */
   public static UuidIntMapMap create(File folder) {
      return new UuidIntMapMap(folder);
   }

   /**
    * Put.
    *
    * @param uuidKey the uuid key
    * @param value the value
    * @return true, if successful
    */
   @Override
   public boolean put(UUID uuidKey, int value) {
      updateCache(value, uuidKey);

      final int                        mapIndex   = getMapIndex(uuidKey);
      final long[]                     keyAsArray = UUIDUtil.convert(uuidKey);
      final ConcurrentUuidToIntHashMap map        = getMap(mapIndex);
      final long                       stamp      = map.getStampedLock()
                                                       .writeLock();

      try {
         final boolean returnValue = map.put(keyAsArray, value, stamp);

         this.maps[mapIndex].elementUpdated();
         return returnValue;
      } finally {
         map.getStampedLock()
            .unlockWrite(stamp);
      }
   }

   /**
    * Report stats.
    *
    * @param log the log
    */
   public void reportStats(Logger log) {
      for (int i = 0; i < NUMBER_OF_MAPS; i++) {
         log.info("UUID map: " + i + " " + getMap(i).getStats());
      }
   }

   /**
    * Size.
    *
    * @return the int
    */
   public int size() {
      int size = 0;

      for (int i = 0; i < this.maps.length; i++) {
         size += getMap(i).size();
      }

      return size;
   }

   /**
    * Write.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write()
            throws IOException {
      for (int i = 0; i < NUMBER_OF_MAPS; i++) {
         final ConcurrentUuidToIntHashMap map = this.maps[i].get();

         if ((map != null) && this.maps[i].hasUnwrittenUpdate()) {
            this.maps[i].write();
         }
      }
   }

   /**
    * Read map from disk.
    *
    * @param i the i
    * @throws IOException Signals that an I/O exception has occurred.
    */
   protected void readMapFromDisk(int i)
            throws IOException {
      this.lock.lock();

      try {
         if (this.maps[i].get() == null) {
            final File mapFile = new File(this.folder, i + "-uuid-nid.map");

            if (mapFile.exists()) {
               DiskSemaphore.acquire();

               try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)))) {
                  this.maps[i] = new MemoryManagedReference<>(SERIALIZER.deserialize(in), mapFile, SERIALIZER);
                  WriteToDiskCache.addToCache(this.maps[i]);
                  LOG.trace("UuidIntMapMap restored: " + i + " from: " + this + " file: " + mapFile.getAbsolutePath());
               } finally {
                  DiskSemaphore.release();
               }
            } else {
               this.maps[i] = new MemoryManagedReference<>(new ConcurrentUuidToIntHashMap(DEFAULT_MAP_SIZE,
                     MIN_LOAD_FACTOR,
                     MAX_LOAD_FACTOR),
                     new File(this.folder, i + "-uuid-nid.map"),
                     SERIALIZER);
               WriteToDiskCache.addToCache(this.maps[i]);
            }
         }
      } finally {
         this.lock.unlock();
      }
   }

   /**
    * Update cache.
    *
    * @param nid the nid
    * @param uuidKey the uuid key
    */
   private void updateCache(int nid, UUID uuidKey) {
      if (this.nidToPrimoridialCache != null) {
         final UUID[] temp = this.nidToPrimoridialCache.get(nid);
         UUID[]       temp1;

         if (temp == null) {
            temp1 = new UUID[] { uuidKey };
         } else {
            temp1              = Arrays.copyOf(temp, temp.length + 1);
            temp1[temp.length] = uuidKey;
         }

         this.nidToPrimoridialCache.put(nid, temp1);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the.
    *
    * @param key the key
    * @return the int
    */
   @Override
   public int get(UUID key) {
      return getMap(key).get(key);
   }

   /**
    * Gets the keys for value.
    *
    * @param value the value
    * @return the keys for value
    */
   public UUID[] getKeysForValue(int value) {
      if (this.nidToPrimoridialCache != null) {
         final UUID[] cacheHit = this.nidToPrimoridialCache.get(value);

         if ((cacheHit != null) && (cacheHit.length > 0)) {
            return cacheHit;
         }
      }

      final ArrayList<UUID> uuids = new ArrayList<>();

      for (int index = 0; index < this.maps.length; index++) {
         getMap(index).keysOf(value)
                      .stream()
                      .forEach(uuid -> {
                                  uuids.add(uuid);
                               });
      }

      final UUID[] temp = uuids.toArray(new UUID[uuids.size()]);

      if ((this.nidToPrimoridialCache != null) && (temp.length > 0)) {
         this.nidToPrimoridialCache.put(value, temp);
      }

      return temp;
   }

   /**
    * Gets the map.
    *
    * @param index the index
    * @return the map
    * @throws RuntimeException the runtime exception
    */
   protected ConcurrentUuidToIntHashMap getMap(int index)
            throws RuntimeException {
      ConcurrentUuidToIntHashMap result = this.maps[index].get();

      while (result == null) {
         try {
            readMapFromDisk(index);
            result = this.maps[index].get();
         } catch (final IOException ex) {
            throw new RuntimeException(ex);
         }
      }

      this.maps[index].elementRead();
      HoldInMemoryCache.addToCache(this.maps[index]);
      return result;
   }

   /**
    * Gets the map.
    *
    * @param key the key
    * @return the map
    */
   private ConcurrentUuidToIntHashMap getMap(UUID key) {
      if (key == null) {
         throw new IllegalStateException("UUIDs cannot be null. ");
      }

      final int index = getMapIndex(key);

      return getMap(index);
   }

   /**
    * Gets the map index.
    *
    * @param key the key
    * @return the map index
    */
   private int getMapIndex(UUID key) {
      return (((byte) key.hashCode())) - Byte.MIN_VALUE;
   }

   /**
    * Gets the next nid provider.
    *
    * @return the next nid provider
    */
   public static AtomicInteger getNextNidProvider() {
      return NEXT_NID_PROVIDER;
   }

   /**
    * Checks if shutdown.
    *
    * @return true, if shutdown
    */
   public boolean isShutdown() {
      return this.shutdown;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the shutdown.
    *
    * @param shutdown the new shutdown
    */
   public void setShutdown(boolean shutdown) {
      this.shutdown = shutdown;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the with generation.
    *
    * @param uuidKey the uuid key
    * @return the with generation
    */
   public int getWithGeneration(UUID uuidKey) {
      final long[] keyAsArray = UUIDUtil.convert(uuidKey);
      final int    mapIndex   = getMapIndex(uuidKey);
      int          nid        = getMap(mapIndex).get(keyAsArray);

      if (nid != Integer.MAX_VALUE) {
         return nid;
      }

      final ConcurrentUuidToIntHashMap map   = getMap(mapIndex);
      final long                       stamp = map.getStampedLock()
                                                  .writeLock();

      try {
         nid = map.get(keyAsArray, stamp);

         if (nid != Integer.MAX_VALUE) {
            return nid;
         }

         nid = NEXT_NID_PROVIDER.incrementAndGet();
         
//       if (nid == -2147483637) {
//           System.out.println(nid + "->" + key);
//       }
         this.maps[mapIndex].elementUpdated();
         map.put(keyAsArray, nid, stamp);
         updateCache(nid, uuidKey);
         return nid;
      } finally {
         map.getStampedLock()
            .unlockWrite(stamp);
      }
   }
}

