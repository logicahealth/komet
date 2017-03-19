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

import java.util.EnumSet;
import java.util.function.ObjIntConsumer;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.coordinate.StampCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LatestPrimitiveStampCollector.
 *
 * @author kec
 */
public class LatestPrimitiveStampCollector
         implements ObjIntConsumer<StampSequenceSet> {
   /** The computer. */
   private final RelativePositionCalculator computer;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new latest primitive stamp collector.
    *
    * @param stampCoordinate the stamp coordinate
    */
   public LatestPrimitiveStampCollector(StampCoordinate stampCoordinate) {
      this.computer = RelativePositionCalculator.getCalculator(stampCoordinate);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Accept.
    *
    * @param latestResult the latest result
    * @param possibleNewLatestStamp the possible new latest stamp
    */
   @Override
   public void accept(StampSequenceSet latestResult, int possibleNewLatestStamp) {
      final StampSequenceSet oldResult = StampSequenceSet.of(latestResult.stream());

      latestResult.clear();

      if (oldResult.isEmpty()) {
         // Simple case, no results yet, just add the possible stamp...
         latestResult.add(possibleNewLatestStamp);
      } else if (oldResult.size() == 1) {
         // Only a single existing result (no contradiction identified), so see which is
         // latest, or if a contradiction exists.
         final int oldStampSequence = oldResult.getIntIterator()
                                               .next();
         final RelativePosition relativePosition = this.computer.relativePosition(oldStampSequence,
                                                                                  possibleNewLatestStamp);

         switch (relativePosition) {
         case AFTER:
            latestResult.add(oldStampSequence);
            break;

         case BEFORE:
            latestResult.add(possibleNewLatestStamp);
            break;

         case CONTRADICTION:
         case EQUAL:
            latestResult.add(oldStampSequence);

            if (oldStampSequence != possibleNewLatestStamp) {
               latestResult.add(possibleNewLatestStamp);
            }

            break;

         case UNREACHABLE:
            latestResult.or(oldResult);

            if (this.computer.onRoute(possibleNewLatestStamp)) {
               latestResult.add(possibleNewLatestStamp);
            }

            break;

         default:
            throw new UnsupportedOperationException("Can't handle: " + relativePosition);
         }
      } else {
         // Complicated case, current results contain at least one contradiction (size > 1)
         final EnumSet<RelativePosition> relativePositions = EnumSet.noneOf(RelativePosition.class);

         oldResult.stream().forEach((oldResultStamp) -> {
                              relativePositions.add(
                                  this.computer.relativePosition(possibleNewLatestStamp, oldResultStamp));
                           });

         if (relativePositions.size() == 1) {
            switch ((RelativePosition) relativePositions.toArray()[0]) {
            case AFTER:
               latestResult.add(possibleNewLatestStamp);
               break;

            case BEFORE:
            case UNREACHABLE:
               oldResult.stream().forEach((oldResultStamp) -> {
                                    latestResult.add(oldResultStamp);
                                 });
               break;

            case CONTRADICTION:
            case EQUAL:
               latestResult.add(possibleNewLatestStamp);
               oldResult.stream().forEach((oldResultStamp) -> {
                                    latestResult.add(oldResultStamp);
                                 });
               break;

            default:
               throw new UnsupportedOperationException("Can't handle: " + relativePositions.toArray()[0]);
            }
         } else {
            oldResult.add(possibleNewLatestStamp);

//          String stampInfo = Stamp.stampArrayToString(oldResult.stream().toArray());
//          System.out.println(stampInfo);
            throw new UnsupportedOperationException("Can't compute latest stamp for: " + possibleNewLatestStamp);
         }
      }
   }
}

