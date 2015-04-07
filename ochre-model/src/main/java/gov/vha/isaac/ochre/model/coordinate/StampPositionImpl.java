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

import gov.vha.isaac.ochre.api.coordinate.StampPosition;

/**
 *
 * @author kec
 */
public class StampPositionImpl implements StampPosition {
    long time;
    int stampPathSequence;

    public StampPositionImpl(long time, int stampPathSequence) {
        this.time = time;
        this.stampPathSequence = stampPathSequence;
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
        if (this.stampPathSequence != other.stampPathSequence) {
            return false;
        }
        return true;
    }
    
}
