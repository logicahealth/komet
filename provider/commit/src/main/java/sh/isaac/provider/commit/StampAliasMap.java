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
package sh.isaac.provider.commit;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Spliterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.SIZED;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.list.IntArrayList;

import sh.isaac.api.collections.NativeIntIntHashMap;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class StampAliasMap {
   private final ReentrantReadWriteLock rwl           = new ReentrantReadWriteLock();
   private final Lock                   read          = rwl.readLock();
   private final Lock                   write         = rwl.writeLock();
   NativeIntIntHashMap                  stampAliasMap = new NativeIntIntHashMap();
   NativeIntIntHashMap                  aliasStampMap = new NativeIntIntHashMap();

   //~--- methods -------------------------------------------------------------

   public void addAlias(int stamp, int alias) {
      try {
         write.lock();

         if (!stampAliasMap.containsKey(stamp)) {
            stampAliasMap.put(stamp, alias);
            aliasStampMap.put(alias, stamp);
         } else if (stampAliasMap.get(stamp) == alias) {
            // already added...
         } else {
            // add an additional alias
            aliasStampMap.put(alias, stamp);
         }
      } finally {
         if (write != null) {
            write.unlock();
         }
      }
   }

   public void read(File mapFile)
            throws IOException {
      try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)))) {
         int size = input.readInt();

         stampAliasMap.ensureCapacity(size);

         for (int i = 0; i < size; i++) {
            stampAliasMap.put(input.readInt(), input.readInt());
         }

         size = input.readInt();
         aliasStampMap.ensureCapacity(size);

         for (int i = 0; i < size; i++) {
            aliasStampMap.put(input.readInt(), input.readInt());
         }
      }
   }

   public void write(File mapFile)
            throws IOException {
      try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mapFile)))) {
         output.writeInt(stampAliasMap.size());
         stampAliasMap.forEachPair((int stampSequence,
                                    int aliasSequence) -> {
                                      try {
                                         output.writeInt(stampSequence);
                                         output.writeInt(aliasSequence);
                                         return true;
                                      } catch (IOException ex) {
                                         throw new RuntimeException(ex);
                                      }
                                   });
         output.writeInt(aliasStampMap.size());
         aliasStampMap.forEachPair((int aliasSequence,
                                    int stampSequence) -> {
                                      try {
                                         output.writeInt(aliasSequence);
                                         output.writeInt(stampSequence);
                                         return true;
                                      } catch (IOException ex) {
                                         throw new RuntimeException(ex);
                                      }
                                   });
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    *
    * @param stamp
    * @return array of unique aliases, which do not include the stamp itself.
    */
   public int[] getAliases(int stamp) {
      try {
         read.lock();

         IntStream.Builder builder = IntStream.builder();

         getAliasesForward(stamp, builder);
         getAliasesReverse(stamp, builder);
         return builder.build()
                       .distinct()
                       .toArray();
      } finally {
         if (read != null) {
            read.unlock();
         }
      }
   }

   private void getAliasesForward(int stamp, IntStream.Builder builder) {
      if (stampAliasMap.containsKey(stamp)) {
         int alias = stampAliasMap.get(stamp);

         builder.add(alias);
         getAliasesForward(alias, builder);
      }
   }

   private void getAliasesReverse(int stamp, IntStream.Builder builder) {
      if (aliasStampMap.containsKey(stamp)) {
         int alias = aliasStampMap.get(stamp);

         builder.add(alias);
         getAliasesReverse(alias, builder);
      }
   }

   public int getSize() {
      assert stampAliasMap.size() == aliasStampMap.size():
             "stampAliasMap.size() = " + stampAliasMap.size() + " aliasStampMap.size() = " + aliasStampMap.size();
      return aliasStampMap.size();
   }

   public Stream<StampAlias> getStampAliasStream() {
      return StreamSupport.stream(new StampAliasSpliterator(), false);
   }

   //~--- inner classes -------------------------------------------------------

   private class StampAliasSpliterator
           extends IndexedStampSequenceSpliterator<StampAlias> {
      public StampAliasSpliterator() {
         super(aliasStampMap.keys());
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean tryAdvance(Consumer<? super StampAlias> action) {
         if (getIterator().hasNext()) {
            int        alias      = getIterator().nextInt();
            StampAlias stampAlias = new StampAlias(aliasStampMap.get(alias), alias);

            action.accept(stampAlias);
            return true;
         }

         return false;
      }
   }
}

