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

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.NidSet;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/18/14.
 */
public class ConcurrentSequenceIntMap {
   private static final int SEGMENT_SIZE = 128000;

   //~--- fields --------------------------------------------------------------

   ReentrantLock               lock            = new ReentrantLock();
   CopyOnWriteArrayList<int[]> sequenceIntList = new CopyOnWriteArrayList<>();
   AtomicInteger               size            = new AtomicInteger(0);

   //~--- constructors --------------------------------------------------------

   public ConcurrentSequenceIntMap() {
      final int[] segmentArray = new int[SEGMENT_SIZE];

      Arrays.fill(segmentArray, -1);
      this.sequenceIntList.add(segmentArray);
   }

   //~--- methods -------------------------------------------------------------

   public boolean containsKey(int sequence) {
      if (sequence < 0) {
         sequence = sequence - Integer.MIN_VALUE;
      }

      final int segmentIndex   = sequence / SEGMENT_SIZE;
      final int indexInSegment = sequence % SEGMENT_SIZE;

      if (segmentIndex >= this.sequenceIntList.size()) {
         return false;
      }

      return this.sequenceIntList.get(segmentIndex)[indexInSegment] != 0;
   }

   public boolean put(int sequence, int value) {
      if (sequence < 0) {
         sequence = sequence - Integer.MIN_VALUE;
      }

      this.size.set(Math.max(sequence, this.size.get()));

      final int segmentIndex = sequence / SEGMENT_SIZE;

      if (segmentIndex >= this.sequenceIntList.size()) {
         this.lock.lock();

         try {
            while (segmentIndex >= this.sequenceIntList.size()) {
               final int[] segmentArray = new int[SEGMENT_SIZE];

               Arrays.fill(segmentArray, -1);
               this.sequenceIntList.add(segmentArray);
            }
         } finally {
            this.lock.unlock();
         }
      }

      final int indexInSegment = sequence % SEGMENT_SIZE;

      this.sequenceIntList.get(segmentIndex)[indexInSegment] = value;
      return true;
   }

   public void read(File folder)
            throws IOException {
      this.sequenceIntList = null;

      int segments = 1;

      for (int segment = 0; segment < segments; segment++) {
         try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(folder,
                                                                                                            segment +
                                                                                                            ".sequence-int.map"))))) {
            final int segmentSize = in.readInt();

            segments = in.readInt();

            if (this.sequenceIntList == null) {
               this.sequenceIntList = new CopyOnWriteArrayList<>();
            }

            final int[] segmentArray = new int[segmentSize];

            Arrays.fill(segmentArray, -1);
            this.sequenceIntList.add(segmentArray);

            for (int indexInSegment = 0; indexInSegment < segmentSize; indexInSegment++) {
               this.sequenceIntList.get(segment)[indexInSegment] = in.readInt();
            }

            this.size.set(in.readInt());
         }
      }
   }

   public void write(File folder)
            throws IOException {
      folder.mkdirs();

      final int segments = this.sequenceIntList.size();

      for (int segment = 0; segment < segments; segment++) {
         try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(folder,
                                                                                                                 segment +
                                                                                                                 ".sequence-int.map"))))) {
            out.writeInt(SEGMENT_SIZE);
            out.writeInt(segments);

            final int[] segmentArray = this.sequenceIntList.get(segment);

            for (int indexInSegment = 0; indexInSegment < SEGMENT_SIZE; indexInSegment++) {
               out.writeInt(segmentArray[indexInSegment]);
            }

            out.writeInt(this.size.get());
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public IntStream getComponentNidStream() {
      final int componentSize = this.size.get();

      return IntStream.of(Integer.MIN_VALUE, componentSize + Integer.MIN_VALUE)
                      .filter((nid) -> {
                                 final int i              = nid - Integer.MIN_VALUE;
                                 final int segmentIndex   = i / SEGMENT_SIZE;
                                 final int indexInSegment = i % SEGMENT_SIZE;

                                 return this.sequenceIntList.get(segmentIndex)[indexInSegment] != 0;
                              });
   }

   public NidSet getComponentNidsForConceptNids(ConceptSequenceSet conceptSequenceSet) {
      final NidSet conceptNids   = NidSet.of(conceptSequenceSet);
      final NidSet results       = new NidSet();
      final int    componentSize = this.size.get();

      for (int i = 0; i < componentSize; i++) {
         final int segmentIndex   = i / SEGMENT_SIZE;
         final int indexInSegment = i % SEGMENT_SIZE;

         if (this.sequenceIntList.get(segmentIndex)[indexInSegment] != 0) {
            if (conceptNids.contains(this.sequenceIntList.get(segmentIndex)[indexInSegment])) {
               results.add(i + Integer.MIN_VALUE);
            }
         }
      }

      return results;
   }

   public IntStream getComponentsNotSet() {
      final int componentSize = this.size.get();

      return IntStream.of(Integer.MIN_VALUE, componentSize + Integer.MIN_VALUE)
                      .filter((nid) -> {
                                 final int i              = nid - Integer.MIN_VALUE;
                                 final int segmentIndex   = i / SEGMENT_SIZE;
                                 final int indexInSegment = i % SEGMENT_SIZE;

                                 return this.sequenceIntList.get(segmentIndex)[indexInSegment] == 0;
                              });
   }

   public OptionalInt get(int sequence) {
      if (sequence < 0) {
         sequence = sequence - Integer.MIN_VALUE;
      }

      final int segmentIndex = sequence / SEGMENT_SIZE;

      if (segmentIndex >= this.sequenceIntList.size()) {
         return OptionalInt.empty();
      }

      final int indexInSegment = sequence % SEGMENT_SIZE;
      final int returnValue    = this.sequenceIntList.get(segmentIndex)[indexInSegment];

      if (returnValue == -1) {
         return OptionalInt.empty();
      }

      return OptionalInt.of(returnValue);
   }

   public int getSize() {
      return this.size.get();
   }
}

