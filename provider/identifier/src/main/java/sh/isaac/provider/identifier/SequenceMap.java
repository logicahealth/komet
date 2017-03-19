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
package sh.isaac.provider.identifier;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.OptionalInt;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NativeIntIntHashMap;

//~--- classes ----------------------------------------------------------------

/**
 * Sequences start at 1.
 * @author kec
 */
public class SequenceMap {
   
   /** The Constant FIRST_SEQUENCE. */
   public static final int     FIRST_SEQUENCE      = 1;
   
   /** The Constant MINIMUM_LOAD_FACTOR. */
   private static final double MINIMUM_LOAD_FACTOR = 0.75;
   
   /** The Constant MAXIMUM_LOAD_FACTOR. */
   private static final double MAXIMUM_LOAD_FACTOR = 0.9;

   //~--- fields --------------------------------------------------------------

   /** The sl. */
   StampedLock               sl           = new StampedLock();
   
   /** The next sequence. */
   int                       nextSequence = FIRST_SEQUENCE;
   
   /** The nid sequence map. */
   final NativeIntIntHashMap nidSequenceMap;
   
   /** The sequence nid map. */
   final NativeIntIntHashMap sequenceNidMap;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new sequence map.
    *
    * @param defaultCapacity the default capacity
    */
   public SequenceMap(int defaultCapacity) {
      this.nidSequenceMap = new NativeIntIntHashMap(defaultCapacity, MINIMUM_LOAD_FACTOR, MAXIMUM_LOAD_FACTOR);
      this.sequenceNidMap = new NativeIntIntHashMap(defaultCapacity, MINIMUM_LOAD_FACTOR, MAXIMUM_LOAD_FACTOR);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the nid.
    *
    * @param nid the nid
    * @return the int
    */
   public int addNid(int nid) {
      final long stamp = this.sl.writeLock();

      try {
         if (!this.nidSequenceMap.containsKey(nid)) {
            final int sequence = this.nextSequence++;

            this.nidSequenceMap.put(nid, sequence);
            this.sequenceNidMap.put(sequence, nid);
            return sequence;
         }

         return this.nidSequenceMap.get(nid);
      } finally {
         this.sl.unlockWrite(stamp);
      }
   }

   /**
    * Adds the nid if missing.
    *
    * @param nid the nid
    * @return the int
    */
   public int addNidIfMissing(int nid) {
      long    stamp       = this.sl.tryOptimisticRead();
      final boolean containsKey = this.nidSequenceMap.containsKey(nid);
      int     value       = this.nidSequenceMap.get(nid);

      if (this.sl.validate(stamp) && containsKey) {
         return value;
      }

      stamp = this.sl.writeLock();

      try {
         if (this.nidSequenceMap.containsKey(nid)) {
            return this.nidSequenceMap.get(nid);
         }

         value = this.nextSequence++;
         this.nidSequenceMap.put(nid, value);
         this.sequenceNidMap.put(value, nid);
         return value;
      } finally {
         this.sl.unlockWrite(stamp);
      }
   }

   /**
    * Contains nid.
    *
    * @param nid the nid
    * @return true, if successful
    */
   public boolean containsNid(int nid) {
      long    stamp = this.sl.tryOptimisticRead();
      boolean value = this.nidSequenceMap.containsKey(nid);

      if (!this.sl.validate(stamp)) {
         stamp = this.sl.readLock();

         try {
            value = this.nidSequenceMap.containsKey(nid);
         } finally {
            this.sl.unlockRead(stamp);
         }
      }

      return value;
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
         final int size = input.readInt();

         this.nextSequence = input.readInt();
         this.nidSequenceMap.ensureCapacity(size);
         this.sequenceNidMap.ensureCapacity(size);

         for (int i = 0; i < size; i++) {
            final int nid      = input.readInt();
            final int sequence = input.readInt();

            this.nidSequenceMap.put(nid, sequence);
            this.sequenceNidMap.put(sequence, nid);
         }
      }
   }

   /**
    * Removes the nid.
    *
    * @param nid the nid
    */
   public void removeNid(int nid) {
      final long stamp = this.sl.writeLock();

      try {
         if (this.nidSequenceMap.containsKey(nid)) {
            final int sequence = this.nidSequenceMap.get(nid);

            this.nidSequenceMap.removeKey(nid);
            this.sequenceNidMap.removeKey(sequence);
         }
      } finally {
         this.sl.unlockWrite(stamp);
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
         output.writeInt(this.nidSequenceMap.size());
         output.writeInt(this.nextSequence);
         this.nidSequenceMap.forEachPair((int nid,
                                     int sequence) -> {
                                       try {
                                          output.writeInt(nid);
                                          output.writeInt(sequence);
                                          return true;
                                       } catch (final IOException ex) {
                                          throw new RuntimeException(ex);
                                       }
                                    });
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept nid stream.
    *
    * @return the concept nid stream
    */
   public IntStream getConceptNidStream() {
      return IntStream.of(this.nidSequenceMap.keys()
                                        .elements());
   }

   /**
    * Gets the next sequence.
    *
    * @return the next sequence
    */
   public int getNextSequence() {
      return this.nextSequence;
   }

   /**
    * Gets the nid.
    *
    * @param sequence the sequence
    * @return the nid
    */
   public OptionalInt getNid(int sequence) {
      long stamp = this.sl.tryOptimisticRead();
      int  value = this.sequenceNidMap.get(sequence);

      if (!this.sl.validate(stamp)) {
         stamp = this.sl.readLock();

         try {
            value = this.sequenceNidMap.get(sequence);
         } finally {
            this.sl.unlockRead(stamp);
         }
      }

      if (value == 0) {
         return OptionalInt.empty();
      }

      return OptionalInt.of(value);
   }

   /**
    * Gets the nid fast.
    *
    * @param sequence the sequence
    * @return the nid fast
    */
   public int getNidFast(int sequence) {
      long stamp = this.sl.tryOptimisticRead();
      int  value = this.sequenceNidMap.get(sequence);

      if (!this.sl.validate(stamp)) {
         stamp = this.sl.readLock();

         try {
            value = this.sequenceNidMap.get(sequence);
         } finally {
            this.sl.unlockRead(stamp);
         }
      }

      return value;
   }

   /**
    * Gets the sequence.
    *
    * @param nid the nid
    * @return the sequence
    */
   public OptionalInt getSequence(int nid) {
      if (containsNid(nid)) {
         long stamp = this.sl.tryOptimisticRead();
         int  value = this.nidSequenceMap.get(nid);

         if (!this.sl.validate(stamp)) {
            stamp = this.sl.readLock();

            try {
               value = this.nidSequenceMap.get(nid);
            } finally {
               this.sl.unlockRead(stamp);
            }
         }

         return OptionalInt.of(value);
      }

      return OptionalInt.empty();
   }

   /**
    * Gets the sequence fast.
    *
    * @param nid the nid
    * @return the sequence fast
    */
   public int getSequenceFast(int nid) {
      long stamp = this.sl.tryOptimisticRead();
      int  value = this.nidSequenceMap.get(nid);

      if (!this.sl.validate(stamp)) {
         stamp = this.sl.readLock();

         try {
            value = this.nidSequenceMap.get(nid);
         } finally {
            this.sl.unlockRead(stamp);
         }
      }

      return value;
   }

   /**
    * Gets the sequence stream.
    *
    * @return the sequence stream
    */
   public IntStream getSequenceStream() {
      return IntStream.of(this.sequenceNidMap.keys()
                                        .elements());
   }

   /**
    * Gets the size.
    *
    * @return the size
    */
   public int getSize() {
      assert this.nidSequenceMap.size() == this.sequenceNidMap.size():
             "nidSequenceMap.size() = " + this.nidSequenceMap.size() + " sequenceNidMap.size() = " + this.sequenceNidMap.size();
      return this.sequenceNidMap.size();
   }
}

