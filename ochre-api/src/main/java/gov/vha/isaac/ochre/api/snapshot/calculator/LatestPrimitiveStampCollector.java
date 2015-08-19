/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.snapshot.calculator;

import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.collections.StampSequenceSet;
import java.util.EnumSet;
import java.util.function.ObjIntConsumer;

/**
 *
 * @author kec
 */
public class LatestPrimitiveStampCollector implements ObjIntConsumer<StampSequenceSet> {

    private final RelativePositionCalculator computer;

    public LatestPrimitiveStampCollector(StampCoordinate<? extends StampCoordinate<?>> stampCoordinate) {
        this.computer = RelativePositionCalculator.getCalculator(stampCoordinate);
    }

    @Override
    public void accept(StampSequenceSet latestResult, int possibleNewLatestStamp) {
        StampSequenceSet oldResult = StampSequenceSet.of(latestResult.stream());
        latestResult.clear();
        if (oldResult.isEmpty()) {
            // Simple case, no results yet, just add the possible stamp...
            latestResult.add(possibleNewLatestStamp);
        } else if (oldResult.size() == 1) {
            // Only a single existing result (no contradiction identified), so see which is
            // latest, or if a contradiction exists. 
            int oldStampSequence = oldResult.getIntIterator().next();
            RelativePosition relativePosition = computer.relativePosition(oldStampSequence, possibleNewLatestStamp);
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
                    if (computer.onRoute(possibleNewLatestStamp)) {
                        latestResult.add(possibleNewLatestStamp);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Can't handle: " + relativePosition);

            }
        } else {
            // Complicated case, current results contain at least one contradiction (size > 1)

            EnumSet<RelativePosition> relativePositions = EnumSet.noneOf(RelativePosition.class);
            oldResult.stream().forEach((oldResultStamp) -> {
                relativePositions.add(computer.relativePosition(possibleNewLatestStamp, oldResultStamp));
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
//                String stampInfo = Stamp.stampArrayToString(oldResult.stream().toArray());
//                System.out.println(stampInfo);
                throw new UnsupportedOperationException("Can't compute latest stamp for: " + possibleNewLatestStamp);
            }
        }
    }

}
