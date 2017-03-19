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



package sh.isaac.provider.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class DestinationOriginRecord
         implements Comparable<DestinationOriginRecord> {
   private final int originSequence;
   private final int destinationSequence;

   //~--- constructors --------------------------------------------------------

   public DestinationOriginRecord(int destinationSequence, int originSequence) {
      if ((originSequence < 0) && (originSequence != Integer.MIN_VALUE)) {
         originSequence = Get.identifierService()
                             .getConceptSequence(originSequence);
      }

      if ((destinationSequence < 0) && (destinationSequence != Integer.MIN_VALUE)) {
         destinationSequence = Get.identifierService()
                                  .getConceptSequence(destinationSequence);
      }

      this.originSequence      = originSequence;
      this.destinationSequence = destinationSequence;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public int compareTo(DestinationOriginRecord o) {
      if (this.destinationSequence > o.destinationSequence) {
         return 1;
      }

      if (this.destinationSequence < o.destinationSequence) {
         return -1;
      }

      if (this.originSequence > o.originSequence) {
         return 1;
      }

      if (this.originSequence < o.originSequence) {
         return -1;
      }

      return 0;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final DestinationOriginRecord other = (DestinationOriginRecord) obj;

      if (this.originSequence != other.originSequence) {
         return false;
      }

      return this.destinationSequence == other.destinationSequence;
   }

   @Override
   public int hashCode() {
      int hash = 7;

      hash = 97 * hash + this.originSequence;
      hash = 97 * hash + this.destinationSequence;
      return hash;
   }

   @Override
   public String toString() {
      return Get.conceptDescriptionText(this.originSequence) + "<" + this.originSequence + ">➞" +
             Get.conceptDescriptionText(this.destinationSequence) + "<" + this.destinationSequence + ">";
   }

   //~--- get methods ---------------------------------------------------------

   public int getDestinationSequence() {
      return this.destinationSequence;
   }

   public int getOriginSequence() {
      return this.originSequence;
   }
}

