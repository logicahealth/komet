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

import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import sh.isaac.api.externalizable.StampComment;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class StampCommentMap {
   private final ReentrantReadWriteLock rwl             = new ReentrantReadWriteLock();
   private final Lock                   read            = this.rwl.readLock();
   private final Lock                   write           = this.rwl.writeLock();
   OpenIntObjectHashMap<String>         stampCommentMap = new OpenIntObjectHashMap();

   //~--- methods -------------------------------------------------------------

   public void addComment(int stamp, String comment) {
      try {
         this.write.lock();

         if (comment != null) {
            this.stampCommentMap.put(stamp, comment);
         } else {
            this.stampCommentMap.removeKey(stamp);
         }
      } finally {
         if (this.write != null) {
            this.write.unlock();
         }
      }
   }

   public void read(File mapFile)
            throws IOException {
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

   public void write(File mapFile)
            throws IOException {
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
    *
    * @param stamp
    * @return Comment associated with the stamp.
    */
   public Optional<String> getComment(int stamp) {
      try {
         this.read.lock();
         return Optional.ofNullable(this.stampCommentMap.get(stamp));
      } finally {
         if (this.read != null) {
            this.read.unlock();
         }
      }
   }

   public int getSize() {
      return this.stampCommentMap.size();
   }

   public Stream<StampComment> getStampCommentStream() {
      return StreamSupport.stream(new StampCommentSpliterator(), false);
   }

   //~--- inner classes -------------------------------------------------------

   private class StampCommentSpliterator
           extends IndexedStampSequenceSpliterator<StampComment> {
      public StampCommentSpliterator() {
         super(StampCommentMap.this.stampCommentMap.keys());
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean tryAdvance(Consumer<? super StampComment> action) {
         if (getIterator().hasNext()) {
            final int          mapIndex     = getIterator().nextInt();
            final StampComment stampComment = new StampComment(StampCommentMap.this.stampCommentMap.get(mapIndex), mapIndex);

            action.accept(stampComment);
            return true;
         }

         return false;
      }
   }
}

