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
 */
public class ConcurrentIntObjectMap<T> {
   private final ReentrantReadWriteLock rwl     = new ReentrantReadWriteLock();
   private final Lock                   read    = rwl.readLock();
   private final Lock                   write   = rwl.writeLock();
   OpenIntObjectHashMap<byte[]>         map     = new OpenIntObjectHashMap<>();
   private boolean                      changed = false;
   DataSerializer<T>                    serializer;

   //~--- constructors --------------------------------------------------------

   public ConcurrentIntObjectMap(DataSerializer<T> serializer) {
      this.serializer = serializer;
   }

   //~--- methods -------------------------------------------------------------

   public boolean containsKey(int key) {
      try {
         read.lock();
         return map.containsKey(key);
      } finally {
         if (read != null) {
            read.unlock();
         }
      }
   }

   public boolean forEachPair(IntObjectProcedure<T> procedure) {
      map.forEachPair((int first,
                       byte[] data) -> {
                         try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
                            return procedure.apply(first, serializer.deserialize(dis));
                         } catch (IOException e) {
                            throw new RuntimeException(e);
                         }
                      });
      return true;
   }

   public boolean put(int key, T value) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
         serializer.serialize(new DataOutputStream(baos), value);
         changed = true;

         try {
            write.lock();
            return map.put(key, baos.toByteArray());
         } finally {
            if (write != null) {
               write.unlock();
            }
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public int size() {
      return map.size();
   }

   //~--- get methods ---------------------------------------------------------

   public Optional<T> get(int key) {
      byte[] data;

      try {
         read.lock();
         data = map.get(key);
      } finally {
         if (read != null) {
            read.unlock();
         }
      }

      if (data == null) {
         return Optional.empty();
      }

      try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
         return Optional.of(serializer.deserialize(dis));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}

