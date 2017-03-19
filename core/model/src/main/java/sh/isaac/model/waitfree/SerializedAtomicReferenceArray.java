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
package sh.isaac.model.waitfree;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.atomic.AtomicReferenceArray;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class SerializedAtomicReferenceArray
        extends AtomicReferenceArray<byte[]> {
   WaitFreeMergeSerializer isaacSerializer;
   int                     segment;

   //~--- constructors --------------------------------------------------------

   public SerializedAtomicReferenceArray(int length, WaitFreeMergeSerializer isaacSerializer, int segment) {
      super(length);
      this.isaacSerializer = isaacSerializer;
      this.segment         = segment;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Returns the String representation of the current values of array.
    *
    * @return the String representation of the current values of array
    */
   @Override
   public String toString() {
      int iMax = length() - 1;

      if (iMax == -1) {
         return "{Bytes≤≥B}";
      }

      StringBuilder b = new StringBuilder();

      for (int i = 0; ; i++) {
         b.append("{Bytes≤");

         int sequence = segment * length() + i;

         b.append(sequence);
         b.append(": ");

         // TODO is this just for concepts?
         b.append(Get.conceptDescriptionText(sequence));
         b.append(" ");

         byte[] byteData = get(i);

         if (byteData != null) {
            ByteArrayDataBuffer db = new ByteArrayDataBuffer(byteData);

            b.append(isaacSerializer.deserialize(db));
         } else {
            b.append("null");
         }

         if (i == iMax) {
            return b.append("≥B}")
                    .toString();
         }

         b.append("≥B} ");
      }
   }

   //~--- get methods ---------------------------------------------------------

   public int getSegment() {
      return segment;
   }
}

