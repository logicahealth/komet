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

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.function.IntObjectProcedure;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import sh.isaac.api.DataSerializer;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/18/14.
 *
 * @param <T> the generic type
 */
public class ConcurrentIntObjectMap<T> {
   /** The rwl. */
   private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

   /** The read. */
   private final Lock read = this.rwl.readLock();

   /** The write. */
   private final Lock write = this.rwl.writeLock();

   /** The map. */
   OpenIntObjectHashMap<byte[]> map = new OpenIntObjectHashMap<>();

   /** The serializer. */
   DataSerializer<T> serializer;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concurrent int object map.
    *
    * @param serializer the serializer
    */
   public ConcurrentIntObjectMap(DataSerializer<T> serializer) {
      this.serializer = serializer;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Contains key.
    *
    * @param key the key
    * @return true, if successful
    */
   public boolean containsKey(int key) {
      try {
         this.read.lock();
         return this.map.containsKey(key);
      } finally {
         if (this.read != null) {
            this.read.unlock();
         }
      }
   }

   /**
    * For each pair.
    *
    * @param procedure the procedure
    * @return true, if successful
    */
   public boolean forEachPair(IntObjectProcedure<T> procedure) {
      this.map.forEachPair((int first,
                            byte[] data) -> {
                              try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
                                 return procedure.apply(first, this.serializer.deserialize(dis));
                              } catch (final IOException e) {
                                 throw new RuntimeException(e);
                              }
                           });
      return true;
   }

   /**
    * Put.
    *
    * @param key the key
    * @param value the value
    * @return true, if successful
    */
   public boolean put(int key, T value) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
         this.serializer.serialize(new DataOutputStream(baos), value);

         try {
            this.write.lock();
            return this.map.put(key, baos.toByteArray());
         } finally {
            if (this.write != null) {
               this.write.unlock();
            }
         }
      } catch (final IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Size.
    *
    * @return the int
    */
   public int size() {
      return this.map.size();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the.
    *
    * @param key the key
    * @return the optional
    */
   public Optional<T> get(int key) {
      byte[] data;

      try {
         this.read.lock();
         data = this.map.get(key);
      } finally {
         if (this.read != null) {
            this.read.unlock();
         }
      }

      if (data == null) {
         return Optional.empty();
      }

      try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
         return Optional.of(this.serializer.deserialize(dis));
      } catch (final IOException e) {
         throw new RuntimeException(e);
      }
   }
}

