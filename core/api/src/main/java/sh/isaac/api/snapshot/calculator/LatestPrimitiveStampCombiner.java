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



package sh.isaac.api.snapshot.calculator;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.function.BiConsumer;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.StampSequenceSet;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LatestPrimitiveStampCombiner.
 *
 * @author kec
 */
public class LatestPrimitiveStampCombiner
         implements BiConsumer<StampSequenceSet, StampSequenceSet> {
   /** The computer. */
   private final RelativePositionCalculator computer;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new latest primitive stamp combiner.
    *
    * @param computer the computer
    */
   public LatestPrimitiveStampCombiner(RelativePositionCalculator computer) {
      this.computer = computer;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Accept.
    *
    * @param t the t
    * @param uStampSet the u stamp set
    */
   @Override
   public void accept(StampSequenceSet t, StampSequenceSet uStampSet) {
      final StampSequenceSet tStampSet = StampSequenceSet.of(t.stream());

      t.clear();

      if ((tStampSet.size() == 1) && (uStampSet.size() == 1)) {
         final int              stamp1           = tStampSet.getIntIterator()
                                                            .next();
         final int              stamp2           = uStampSet.getIntIterator()
                                                            .next();
         final RelativePosition relativePosition = this.computer.relativePosition(stamp1, stamp2);

         switch (relativePosition) {
         case AFTER:
            break;

         case BEFORE:
            t.add(stamp2);
            break;

         case CONTRADICTION:
         case EQUAL:
            if (stamp1 != stamp2) {
               t.add(stamp1);
               t.add(stamp2);
            }

            break;

         case UNREACHABLE:
            if (this.computer.onRoute(stamp2)) {
               t.add(stamp1);
               t.add(stamp2);
            }

            break;

         default:
            throw new UnsupportedOperationException("m Can't handle: " + relativePosition);
         }
      } else {
         tStampSet.or(uStampSet);
         t.clear();
         Arrays.stream(this.computer.getLatestStampSequencesAsArray(tStampSet.stream()))
               .forEach((stamp) -> t.add(stamp));
      }
   }
}

