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

import sh.isaac.api.collections.IntObjectHashMap;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.datastore.ExtendedStoreData;
import sh.isaac.api.externalizable.StampComment;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class StampCommentMap.
 *
 * @author kec
 */
public class StampCommentMap {
   /** The rwl. */
   private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

   /** The read. */
   private final Lock read = this.rwl.readLock();

   /** The write. */
   private final Lock write = this.rwl.writeLock();

   /** The stamp comment map for in-memory stores*/
   private IntObjectHashMap<String> stampCommentMap;
   
   /**
    * Storage for DataStore linked storage
    */
   private ExtendedStore dataStore;
   private ExtendedStoreData<Integer, String> stampToComment; 

   /**
    * Construct a default stamp comment map, which holds the comments in memory, and must be read / written to the file system.
    */
   public StampCommentMap() {
      stampCommentMap = new IntObjectHashMap<>();
   }
   
   /**
    * Construct a a StampAliasMap class, that is just a thin wrapper around a datastore.  Does not hold any data in memory.
    * @param dataStore the datastore to read/write from 
    */
   public StampCommentMap(ExtendedStore dataStore) {
      this.dataStore = dataStore;
      stampToComment = dataStore.<Integer, String>getStore("stampCommentMap");
   }

   /**
    * Adds the comment.
    *
    * @param stamp the stamp
    * @param comment the comment
    */
   public void addComment(int stamp, String comment) {
      try {
         this.write.lock();

         if (dataStore == null) {
            if (comment != null) {
               this.stampCommentMap.put(stamp, comment);
            } else {
               this.stampCommentMap.removeKey(stamp);
            }
         }
         else {
            if (comment != null) {
                this.stampToComment.put(stamp, comment);
             } else {
                this.stampToComment.remove(stamp);
             }
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
      if (dataStore != null) {
         throw new RuntimeException("Read shouldn't be called with datastore storage");
      }
      try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)))) {
         final int size = input.readInt();

         this.stampCommentMap.ensureCapacity(size);

         for (int i = 0; i < size; i++) {
            final int    stamp   = input.readInt();
            final String comment = input.readUTF();

            this.stampCommentMap.put(stamp, comment);
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
      if (dataStore != null) {
         throw new RuntimeException("Write shouldn't be called with datastore storage");
      }

      try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mapFile)))) {
         output.writeInt(this.stampCommentMap.size());
         this.stampCommentMap.forEachPair((int nid,
                                           String comment) -> {
                                             try {
                                                output.writeInt(nid);
                                                output.writeUTF(comment);
                                                return true;
                                             } catch (final IOException ex) {
                                                throw new RuntimeException(ex);
                                             }
                                          });
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the comment.
    *
    * @param stamp the stamp
    * @return Comment associated with the stamp.
    */
   public Optional<String> getComment(int stamp) {
      try {
         this.read.lock();
         if (dataStore == null) {
            return Optional.ofNullable(this.stampCommentMap.get(stamp));
         }
         else {
            return Optional.ofNullable(stampToComment.get(stamp));
         }
            
      } finally {
         if (this.read != null) {
            this.read.unlock();
         }
      }
   }

   /**
    * Gets the size.
    *
    * @return the size
    */
   public int getSize() {
      return dataStore == null ? this.stampCommentMap.size() : stampToComment.size();
   }

   /**
    * Gets the stamp comment stream.
    *
    * @return the stamp comment stream
    */
   public Stream<StampComment> getStampCommentStream() {
      return dataStore == null ? StreamSupport.stream(new StampCommentSpliterator(), false) : 
         stampToComment.getStream().map(entry -> new StampComment(entry.getValue(), entry.getKey()));
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class StampCommentSpliterator.
    */
   private class StampCommentSpliterator
           extends IndexedStampSequenceSpliterator<StampComment> {
      /**
       * Instantiates a new stamp comment spliterator.
       */
      public StampCommentSpliterator() {
         super(StampCommentMap.this.stampCommentMap.keys());
      }

      //~--- methods ----------------------------------------------------------

      /**
       * Try advance.
       *
       * @param action the action
       * @return true, if successful
       */
      @Override
      public boolean tryAdvance(Consumer<? super StampComment> action) {
         if (getIterator().hasNext()) {
            final int mapIndex = getIterator().nextInt();
            final StampComment stampComment = new StampComment(StampCommentMap.this.stampCommentMap.get(mapIndex),
                                                               mapIndex);

            action.accept(stampComment);
            return true;
         }

         return false;
      }
   }
}

