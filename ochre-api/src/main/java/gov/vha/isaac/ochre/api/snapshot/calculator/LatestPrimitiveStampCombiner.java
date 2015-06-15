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

import gov.vha.isaac.ochre.collections.StampSequenceSet;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 *
 * @author kec
 */
public class LatestPrimitiveStampCombiner implements BiConsumer<StampSequenceSet, StampSequenceSet> {

    private final RelativePositionCalculator computer;

    public LatestPrimitiveStampCombiner(RelativePositionCalculator computer) {
        this.computer = computer;
    }

    @Override
    public void accept(StampSequenceSet t, StampSequenceSet uStampSet) {
        StampSequenceSet tStampSet = StampSequenceSet.of(t.stream());
        t.clear();
        if (tStampSet.size() == 1 && uStampSet.size() == 1) {

            int stamp1 = tStampSet.getIntIterator().next();
            int stamp2 = uStampSet.getIntIterator().next();
            RelativePosition relativePosition = computer.relativePosition(stamp1, stamp2);
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
                    if (computer.onRoute(stamp2)) {
                        t.add(stamp1);
                        t.add(stamp2);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Can't handle: " + relativePosition);

            }

        } else {
            tStampSet.or(uStampSet);
            t.clear();
            Arrays.stream(computer.getLatestStamps(tStampSet.stream())).forEach((stamp) -> t.add(stamp));
        }
    }

}
