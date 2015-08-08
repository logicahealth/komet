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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.relationship.RelationshipAdaptorChronicleKey;
import gov.vha.isaac.ochre.api.relationship.RelationshipVersionAdaptor;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class RelationshipVersionAdaptorImpl implements RelationshipVersionAdaptor<RelationshipVersionAdaptorImpl> {
    
    RelationshipAdaptorChronicleKeyImpl relationshipAdaptorChronicleKey;
    RelationshipAdaptorChronologyImpl chronology;
    int stampSequence;

    public RelationshipVersionAdaptorImpl(RelationshipAdaptorChronicleKeyImpl relationshipAdaptorChronicleKey, int stampSequence) {
        this.relationshipAdaptorChronicleKey = relationshipAdaptorChronicleKey;
        this.stampSequence = stampSequence;
    }

    @Override
    public short getNodeSequence() {
        return relationshipAdaptorChronicleKey.getNodeSequence();
    }
    
    @Override
    public int getOriginSequence() {
        return relationshipAdaptorChronicleKey.originSequence;
    }

    @Override
    public int getDestinationSequence() {
        return relationshipAdaptorChronicleKey.destinationSequence;
    }

    @Override
    public int getTypeSequence() {
        return relationshipAdaptorChronicleKey.typeSequence;
    }

    @Override
    public int getGroup() {
        return relationshipAdaptorChronicleKey.group;
    }

    @Override
    public PremiseType getPremiseType() {
        return relationshipAdaptorChronicleKey.premiseType;
    }

    @Override
    public RelationshipAdaptorChronologyImpl getChronology() {
        return chronology;
    }

    public void setChronology(RelationshipAdaptorChronologyImpl chronology) {
        this.chronology = chronology;
    }

    @Override
    public int getStampSequence() {
        return stampSequence;
    }

    @Override
    public State getState() {
        return Get.commitService().getStatusForStamp(stampSequence);
    }

    @Override
    public long getTime() {
        return Get.commitService().getTimeForStamp(stampSequence);
    }

    @Override
    public int getAuthorSequence() {
        return Get.commitService().getAuthorSequenceForStamp(stampSequence);
    }

    @Override
    public int getModuleSequence() {
       return Get.commitService().getModuleSequenceForStamp(stampSequence);
    }

    @Override
    public int getPathSequence() {
        return Get.commitService().getPathSequenceForStamp(stampSequence);
    }

    @Override
    public int getSememeSequence() {
        return chronology.getSememeSequence();
    }

    @Override
    public int getAssemblageSequence() {
        return chronology.getAssemblageSequence();
    }

    @Override
    public int getReferencedComponentNid() {
        return chronology.getReferencedComponentNid();
    }

    @Override
    public int getNid() {
        return chronology.getNid();
    }

    @Override
    public String toUserString() {
        return toString();
    }

    @Override
    public UUID getPrimordialUuid() {
        return chronology.getPrimordialUuid();
    }

    @Override
    public List<UUID> getUuidList() {
        return chronology.getUuidList();
    }

    @Override
    public CommitStates getCommitState() {
        return CommitStates.COMMITTED;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + this.stampSequence;
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
        final RelationshipVersionAdaptorImpl other = (RelationshipVersionAdaptorImpl) obj;
        if (!Objects.equals(this.relationshipAdaptorChronicleKey, other.relationshipAdaptorChronicleKey)) {
            return false;
        }
        if (this.chronology.getNid() != other.chronology.getNid()) {
            return false;
        }
        return this.stampSequence == other.stampSequence;
    }

    @Override
    public RelationshipAdaptorChronicleKey getChronicleKey() {
        return relationshipAdaptorChronicleKey;
    }

    @Override
    public String toString() {
        return "{[" + Get.conceptDescriptionText(relationshipAdaptorChronicleKey.originSequence) + "]➞(" +
                Get.conceptDescriptionText(relationshipAdaptorChronicleKey.typeSequence) + ")➞[" +
                Get.conceptDescriptionText(relationshipAdaptorChronicleKey.destinationSequence) + "]"
                + " " + Get.commitService().describeStampSequence(stampSequence) + "}";
    }
    
}
