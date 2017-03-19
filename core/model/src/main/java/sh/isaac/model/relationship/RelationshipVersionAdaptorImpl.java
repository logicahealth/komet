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
      return "{[" + Get.conceptDescriptionText(this.relationshipAdaptorChronicleKey.originSequence) + "]➞(" +
             Get.conceptDescriptionText(this.relationshipAdaptorChronicleKey.typeSequence) + ")➞[" +
             Get.conceptDescriptionText(this.relationshipAdaptorChronicleKey.destinationSequence) + "]" + " " +
             Get.stampService().describeStampSequence(this.stampSequence) + "}";
   }

   @Override
   public String toUserString() {
      return toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAssemblageSequence() {
      return this.chronology.getAssemblageSequence();
   }

   @Override
   public int getAuthorSequence() {
      return Get.stampService()
                .getAuthorSequenceForStamp(this.stampSequence);
   }

   @Override
   public RelationshipAdaptorChronicleKey getChronicleKey() {
      return this.relationshipAdaptorChronicleKey;
   }

   @Override
   public RelationshipAdaptorChronologyImpl getChronology() {
      return this.chronology;
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
      return this.relationshipAdaptorChronicleKey.destinationSequence;
   }

   @Override
   public int getGroup() {
      return this.relationshipAdaptorChronicleKey.group;
   }

   @Override
   public int getModuleSequence() {
      return Get.stampService()
                .getModuleSequenceForStamp(this.stampSequence);
   }

   @Override
   public int getNid() {
      return this.chronology.getNid();
   }

   @Override
   public short getNodeSequence() {
      return this.relationshipAdaptorChronicleKey.getNodeSequence();
   }

   @Override
   public int getOriginSequence() {
      return this.relationshipAdaptorChronicleKey.originSequence;
   }

   @Override
   public int getPathSequence() {
      return Get.stampService()
                .getPathSequenceForStamp(this.stampSequence);
   }

   @Override
   public PremiseType getPremiseType() {
      return this.relationshipAdaptorChronicleKey.premiseType;
   }

   @Override
   public UUID getPrimordialUuid() {
      return this.chronology.getPrimordialUuid();
   }

   @Override
   public int getReferencedComponentNid() {
      return this.chronology.getReferencedComponentNid();
   }

   @Override
   public int getSememeSequence() {
      return this.chronology.getSememeSequence();
   }

   @Override
   public int getStampSequence() {
      return this.stampSequence;
   }

   @Override
   public State getState() {
      return Get.stampService()
                .getStatusForStamp(this.stampSequence);
   }

   @Override
   public long getTime() {
      return Get.stampService()
                .getTimeForStamp(this.stampSequence);
   }

   @Override
   public int getTypeSequence() {
      return this.relationshipAdaptorChronicleKey.typeSequence;
   }

   @Override
   public List<UUID> getUuidList() {
      return this.chronology.getUuidList();
   }
}

