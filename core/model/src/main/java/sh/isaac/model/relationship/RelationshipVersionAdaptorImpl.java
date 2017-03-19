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



package sh.isaac.model.relationship;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Objects;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.relationship.RelationshipAdaptorChronicleKey;
import sh.isaac.api.relationship.RelationshipVersionAdaptor;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class RelationshipVersionAdaptorImpl
         implements RelationshipVersionAdaptor<RelationshipVersionAdaptorImpl> {
   RelationshipAdaptorChronicleKeyImpl relationshipAdaptorChronicleKey;
   RelationshipAdaptorChronologyImpl   chronology;
   int                                 stampSequence;

   //~--- constructors --------------------------------------------------------

   public RelationshipVersionAdaptorImpl(RelationshipAdaptorChronicleKeyImpl relationshipAdaptorChronicleKey,
         int stampSequence) {
      this.relationshipAdaptorChronicleKey = relationshipAdaptorChronicleKey;
      this.stampSequence                   = stampSequence;
   }

   //~--- methods -------------------------------------------------------------

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
   public int hashCode() {
      int hash = 5;

      hash = 71 * hash + this.stampSequence;
      return hash;
   }

   @Override
   public String toString() {
      return "{[" + Get.conceptDescriptionText(relationshipAdaptorChronicleKey.originSequence) + "]➞(" +
             Get.conceptDescriptionText(relationshipAdaptorChronicleKey.typeSequence) + ")➞[" +
             Get.conceptDescriptionText(relationshipAdaptorChronicleKey.destinationSequence) + "]" + " " +
             Get.stampService().describeStampSequence(stampSequence) + "}";
   }

   @Override
   public String toUserString() {
      return toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAssemblageSequence() {
      return chronology.getAssemblageSequence();
   }

   @Override
   public int getAuthorSequence() {
      return Get.stampService()
                .getAuthorSequenceForStamp(stampSequence);
   }

   @Override
   public RelationshipAdaptorChronicleKey getChronicleKey() {
      return relationshipAdaptorChronicleKey;
   }

   @Override
   public RelationshipAdaptorChronologyImpl getChronology() {
      return chronology;
   }

   //~--- set methods ---------------------------------------------------------

   public void setChronology(RelationshipAdaptorChronologyImpl chronology) {
      this.chronology = chronology;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public CommitStates getCommitState() {
      return CommitStates.COMMITTED;
   }

   @Override
   public int getDestinationSequence() {
      return relationshipAdaptorChronicleKey.destinationSequence;
   }

   @Override
   public int getGroup() {
      return relationshipAdaptorChronicleKey.group;
   }

   @Override
   public int getModuleSequence() {
      return Get.stampService()
                .getModuleSequenceForStamp(stampSequence);
   }

   @Override
   public int getNid() {
      return chronology.getNid();
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
   public int getPathSequence() {
      return Get.stampService()
                .getPathSequenceForStamp(stampSequence);
   }

   @Override
   public PremiseType getPremiseType() {
      return relationshipAdaptorChronicleKey.premiseType;
   }

   @Override
   public UUID getPrimordialUuid() {
      return chronology.getPrimordialUuid();
   }

   @Override
   public int getReferencedComponentNid() {
      return chronology.getReferencedComponentNid();
   }

   @Override
   public int getSememeSequence() {
      return chronology.getSememeSequence();
   }

   @Override
   public int getStampSequence() {
      return stampSequence;
   }

   @Override
   public State getState() {
      return Get.stampService()
                .getStatusForStamp(stampSequence);
   }

   @Override
   public long getTime() {
      return Get.stampService()
                .getTimeForStamp(stampSequence);
   }

   @Override
   public int getTypeSequence() {
      return relationshipAdaptorChronicleKey.typeSequence;
   }

   @Override
   public List<UUID> getUuidList() {
      return chronology.getUuidList();
   }
}

