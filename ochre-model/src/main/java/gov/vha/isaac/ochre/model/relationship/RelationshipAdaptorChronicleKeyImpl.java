/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.model.relationship;

import gov.vha.isaac.ochre.api.relationship.RelationshipAdaptorChronicleKey;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import java.util.Objects;

/**
 *
 * @author kec
 */
public class RelationshipAdaptorChronicleKeyImpl implements RelationshipAdaptorChronicleKey {
    int originSequence;
    int destinationSequence;
    int typeSequence;
    int group;
    PremiseType premiseType;
    short nodeSequence;

    public RelationshipAdaptorChronicleKeyImpl(int originSequence, int destinationSequence, 
            int typeSequence, int group, PremiseType premiseType, short nodeSequence) {
        this.originSequence = originSequence;
        this.destinationSequence = destinationSequence;
        this.typeSequence = typeSequence;
        this.group = group;
        this.premiseType = premiseType;
        this.nodeSequence = nodeSequence;
    }

    @Override
    public short getNodeSequence() {
        return nodeSequence;
    }

    @Override
    public int getOriginSequence() {
        return originSequence;
    }

    @Override
    public int getDestinationSequence() {
        return destinationSequence;
    }

    @Override
    public int getTypeSequence() {
        return typeSequence;
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public PremiseType getPremiseType() {
        return premiseType;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.originSequence;
        hash = 83 * hash + this.destinationSequence;
        hash = 83 * hash + this.typeSequence;
        hash = 83 * hash + this.group;
        hash = 83 * hash + Objects.hashCode(this.premiseType);
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
        final RelationshipAdaptorChronicleKeyImpl other = (RelationshipAdaptorChronicleKeyImpl) obj;
        if (this.originSequence != other.originSequence) {
            return false;
        }
        if (this.destinationSequence != other.destinationSequence) {
            return false;
        }
        if (this.typeSequence != other.typeSequence) {
            return false;
        }
        if (this.group != other.group) {
            return false;
        }

        return this.premiseType == other.premiseType;
    }
    
}
