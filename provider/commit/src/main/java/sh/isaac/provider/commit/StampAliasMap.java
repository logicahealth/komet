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
 * The Class StampAliasMap.
 *
 * @author kec
 */
public class StampAliasMap {
   
   /** The rwl. */
   private final ReentrantReadWriteLock rwl           = new ReentrantReadWriteLock();
   
   /** The read. */
   private final Lock                   read          = this.rwl.readLock();
   
   /** The write. */
   private final Lock                   write         = this.rwl.writeLock();
   
   /** The stamp alias map. */
   NativeIntIntHashMap                  stampAliasMap = new NativeIntIntHashMap();
   
   /** The alias stamp map. */
   NativeIntIntHashMap                  aliasStampMap = new NativeIntIntHashMap();

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the alias.
    *
    * @param stamp the stamp
    * @param alias the alias
    */
   public void addAlias(int stamp, int alias) {
      try {
         this.write.lock();

         if (!this.stampAliasMap.containsKey(stamp)) {
            this.stampAliasMap.put(stamp, alias);
            this.aliasStampMap.put(alias, stamp);
         } else if (this.stampAliasMap.get(stamp) == alias) {
            // already added...
         } else {
            // add an additional alias
            this.aliasStampMap.put(alias, stamp);
         }
      } finally {
         if (this.write != null) {
            this.write.unlock();
         }
      }
   }

   /**
    * Read.
    *
    * @param mapFile the map file
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void read(File mapFile)
            throws IOException {
      try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)))) {
         int size = input.readInt();

         this.stampAliasMap.ensureCapacity(size);

         for (int i = 0; i < size; i++) {
            this.stampAliasMap.put(input.readInt(), input.readInt());
         }

         size = input.readInt();
         this.aliasStampMap.ensureCapacity(size);

         for (int i = 0; i < size; i++) {
            this.aliasStampMap.put(input.readInt(), input.readInt());
         }
      }
   }

   /**
    * Write.
    *
    * @param mapFile the map file
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write(File mapFile)
            throws IOException {
      try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mapFile)))) {
         output.writeInt(this.stampAliasMap.size());
         this.stampAliasMap.forEachPair((int stampSequence,
                                    int aliasSequence) -> {
                                      try {
                                         output.writeInt(stampSequence);
                                         output.writeInt(aliasSequence);
                                         return true;
                                      } catch (final IOException ex) {
                                         throw new RuntimeException(ex);
                                      }
                                   });
         output.writeInt(this.aliasStampMap.size());
         this.aliasStampMap.forEachPair((int aliasSequence,
                                    int stampSequence) -> {
                                      try {
                                         output.writeInt(aliasSequence);
                                         output.writeInt(stampSequence);
                                         return true;
                                      } catch (final IOException ex) {
                                         throw new RuntimeException(ex);
                                      }
                                   });
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the aliases.
    *
    * @param stamp the stamp
    * @return array of unique aliases, which do not include the stamp itself.
    */
   public int[] getAliases(int stamp) {
      try {
         this.read.lock();

         final IntStream.Builder builder = IntStream.builder();

         getAliasesForward(stamp, builder);
         getAliasesReverse(stamp, builder);
         return builder.build()
                       .distinct()
                       .toArray();
      } finally {
         if (this.read != null) {
            this.read.unlock();
         }
      }
   }

   /**
    * Gets the aliases forward.
    *
    * @param stamp the stamp
    * @param builder the builder
    * @return the aliases forward
    */
   private void getAliasesForward(int stamp, IntStream.Builder builder) {
      if (this.stampAliasMap.containsKey(stamp)) {
         final int alias = this.stampAliasMap.get(stamp);

         builder.add(alias);
         getAliasesForward(alias, builder);
      }
   }

   /**
    * Gets the aliases reverse.
    *
    * @param stamp the stamp
    * @param builder the builder
    * @return the aliases reverse
    */
   private void getAliasesReverse(int stamp, IntStream.Builder builder) {
      if (this.aliasStampMap.containsKey(stamp)) {
         final int alias = this.aliasStampMap.get(stamp);

         builder.add(alias);
         getAliasesReverse(alias, builder);
      }
   }

   /**
    * Gets the size.
    *
    * @return the size
    */
   public int getSize() {
      assert this.stampAliasMap.size() == this.aliasStampMap.size():
             "stampAliasMap.size() = " + this.stampAliasMap.size() + " aliasStampMap.size() = " + this.aliasStampMap.size();
      return this.aliasStampMap.size();
   }

   /**
    * Gets the stamp alias stream.
    *
    * @return the stamp alias stream
    */
   public Stream<StampAlias> getStampAliasStream() {
      return StreamSupport.stream(new StampAliasSpliterator(), false);
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class StampAliasSpliterator.
    */
   private class StampAliasSpliterator
           extends IndexedStampSequenceSpliterator<StampAlias> {
      
      /**
       * Instantiates a new stamp alias spliterator.
       */
      public StampAliasSpliterator() {
         super(StampAliasMap.this.aliasStampMap.keys());
      }

      //~--- methods ----------------------------------------------------------

      /**
       * Try advance.
       *
       * @param action the action
       * @return true, if successful
       */
      @Override
      public boolean tryAdvance(Consumer<? super StampAlias> action) {
         if (getIterator().hasNext()) {
            final int        alias      = getIterator().nextInt();
            final StampAlias stampAlias = new StampAlias(StampAliasMap.this.aliasStampMap.get(alias), alias);

            action.accept(stampAlias);
            return true;
         }

         return false;
      }
   }
}

