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
package gov.vha.isaac.ochre.model.coordinate;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;

/**
 *
 * @author kec
 */
public class StampPositionImpl implements StampPosition, Comparable<StampPosition> {
    
    long time;
    int stampPathSequence;

    public StampPositionImpl(long time, int stampPathSequence) {
        this.time = time;
        this.stampPathSequence = Get.identifierService().getConceptSequence(stampPathSequence);
    }

    @Override
    public StampPath getStampPath() {
        return new StampPathImpl(stampPathSequence);
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public int getStampPathSequence() {
        return stampPathSequence;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (int) (this.time ^ (this.time >>> 32));
        hash = 83 * hash + this.stampPathSequence;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StampPositionImpl other = (StampPositionImpl) obj;
        if (this.time != other.time) {
            return false;
        }
        return this.stampPathSequence == other.stampPathSequence;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StampPosition:{time=");
        if (time == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (time == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(getTimeAsInstant());
        }
        sb.append(", stampPathSequence=").append(stampPathSequence).append(" ").append(Get.getIdentifiedObjectService().informAboutObject(stampPathSequence)).append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(StampPosition o) {
        if (this.stampPathSequence != o.getStampPathSequence()) {
            return Integer.compare(stampPathSequence, o.getStampPathSequence());
        }
        return Long.compare(time, o.getTime());
    }

}
