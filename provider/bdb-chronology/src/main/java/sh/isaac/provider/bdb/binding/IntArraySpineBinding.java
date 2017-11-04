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



package sh.isaac.provider.bdb.binding;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.atomic.AtomicReferenceArray;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class IntArraySpineBinding
        extends TupleBinding<AtomicReferenceArray<int[]>> {
   @Override
   public AtomicReferenceArray<int[]> entryToObject(TupleInput input) {
      int     spineSize = input.readInt();
      int[][] spineData = new int[spineSize][];

      for (int spineIndex = 0; spineIndex < spineSize; spineIndex++) {
         int   size = input.readInt();
         int[] data = new int[size];

         for (int i = 0; i < size; i++) {
            data[i] = input.readInt();
         }

         spineData[spineIndex] = data;
      }

      return new AtomicReferenceArray(spineData);
   }

   @Override
   public void objectToEntry(AtomicReferenceArray<int[]> spine, TupleOutput output) {
      int spineSize = spine.length();
      output.writeInt(spineSize);
      for (int spineIndex = 0; spineIndex < spineSize; spineIndex++) {
         int[] spineElement = spine.get(spineIndex);
         if (spineElement == null) {
            output.writeInt(0);
         } else {
            output.writeInt(spineElement.length);
            for (int i = 0; i < spineElement.length; i++) {
               output.writeInt(spineElement[i]);
            }
         }
      }
   }
}

