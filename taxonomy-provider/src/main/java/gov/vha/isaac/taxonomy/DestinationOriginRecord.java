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
package gov.vha.isaac.taxonomy;

import gov.vha.isaac.ochre.api.Get;

/**
 *
 * @author kec
 */
public class DestinationOriginRecord implements Comparable<DestinationOriginRecord> {
    private final int originSequence;
    private final int destinationSequence;

    public DestinationOriginRecord(int destinationSequence, int originSequence) {
        if (originSequence < 0 && originSequence != Integer.MIN_VALUE) {
            originSequence = Get.identifierService().getConceptSequence(originSequence);
        }
        if (destinationSequence < 0 && destinationSequence != Integer.MIN_VALUE) {
            destinationSequence = Get.identifierService().getConceptSequence(destinationSequence);
        }
        this.originSequence = originSequence;
        this.destinationSequence = destinationSequence;
    }

    public int getOriginSequence() {
        return originSequence;
    }

    public int getDestinationSequence() {
        return destinationSequence;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.originSequence;
        hash = 97 * hash + this.destinationSequence;
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
        final DestinationOriginRecord other = (DestinationOriginRecord) obj;
        if (this.originSequence != other.originSequence) {
            return false;
        }
        return this.destinationSequence == other.destinationSequence;
    }

    @Override
    public int compareTo(DestinationOriginRecord o) {
        if (destinationSequence > o.destinationSequence) {
            return 1;
        }
        if (destinationSequence < o.destinationSequence) {
            return -1;
        }
        if (originSequence > o.originSequence) {
            return 1;
        }
        if (originSequence < o.originSequence) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return Get.conceptDescriptionText(originSequence) +  "<" + originSequence + ">➞" + Get.conceptDescriptionText(destinationSequence) + "<" + destinationSequence + ">";
    }
    
}
