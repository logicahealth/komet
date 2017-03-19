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
 * The Class RelationshipVersionAdaptorImpl.
 *
 * @author kec
 */
public class RelationshipVersionAdaptorImpl
         implements RelationshipVersionAdaptor<RelationshipVersionAdaptorImpl> {
   /** The relationship adaptor chronicle key. */
   RelationshipAdaptorChronicleKeyImpl relationshipAdaptorChronicleKey;

   /** The chronology. */
   RelationshipAdaptorChronologyImpl chronology;

   /** The stamp sequence. */
   int stampSequence;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new relationship version adaptor impl.
    *
    * @param relationshipAdaptorChronicleKey the relationship adaptor chronicle key
    * @param stampSequence the stamp sequence
    */
   public RelationshipVersionAdaptorImpl(RelationshipAdaptorChronicleKeyImpl relationshipAdaptorChronicleKey,
         int stampSequence) {
      this.relationshipAdaptorChronicleKey = relationshipAdaptorChronicleKey;
      this.stampSequence                   = stampSequence;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
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

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 5;

      hash = 71 * hash + this.stampSequence;
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "{[" + Get.conceptDescriptionText(this.relationshipAdaptorChronicleKey.originSequence) + "]➞(" +
             Get.conceptDescriptionText(this.relationshipAdaptorChronicleKey.typeSequence) + ")➞[" +
             Get.conceptDescriptionText(this.relationshipAdaptorChronicleKey.destinationSequence) + "]" + " " +
             Get.stampService().describeStampSequence(this.stampSequence) + "}";
   }

   /**
    * To user string.
    *
    * @return the string
    */
   @Override
   public String toUserString() {
      return toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the assemblage sequence.
    *
    * @return the assemblage sequence
    */
   @Override
   public int getAssemblageSequence() {
      return this.chronology.getAssemblageSequence();
   }

   /**
    * Gets the author sequence.
    *
    * @return the author sequence
    */
   @Override
   public int getAuthorSequence() {
      return Get.stampService()
                .getAuthorSequenceForStamp(this.stampSequence);
   }

   /**
    * Gets the chronicle key.
    *
    * @return the chronicle key
    */
   @Override
   public RelationshipAdaptorChronicleKey getChronicleKey() {
      return this.relationshipAdaptorChronicleKey;
   }

   /**
    * Gets the chronology.
    *
    * @return the chronology
    */
   @Override
   public RelationshipAdaptorChronologyImpl getChronology() {
      return this.chronology;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the chronology.
    *
    * @param chronology the new chronology
    */
   public void setChronology(RelationshipAdaptorChronologyImpl chronology) {
      this.chronology = chronology;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the commit state.
    *
    * @return the commit state
    */
   @Override
   public CommitStates getCommitState() {
      return CommitStates.COMMITTED;
   }

   /**
    * Gets the destination sequence.
    *
    * @return the destination sequence
    */
   @Override
   public int getDestinationSequence() {
      return this.relationshipAdaptorChronicleKey.destinationSequence;
   }

   /**
    * Gets the group.
    *
    * @return the group
    */
   @Override
   public int getGroup() {
      return this.relationshipAdaptorChronicleKey.group;
   }

   /**
    * Gets the module sequence.
    *
    * @return the module sequence
    */
   @Override
   public int getModuleSequence() {
      return Get.stampService()
                .getModuleSequenceForStamp(this.stampSequence);
   }

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() {
      return this.chronology.getNid();
   }

   /**
    * Gets the node sequence.
    *
    * @return the node sequence
    */
   @Override
   public short getNodeSequence() {
      return this.relationshipAdaptorChronicleKey.getNodeSequence();
   }

   /**
    * Gets the origin sequence.
    *
    * @return the origin sequence
    */
   @Override
   public int getOriginSequence() {
      return this.relationshipAdaptorChronicleKey.originSequence;
   }

   /**
    * Gets the path sequence.
    *
    * @return the path sequence
    */
   @Override
   public int getPathSequence() {
      return Get.stampService()
                .getPathSequenceForStamp(this.stampSequence);
   }

   /**
    * Gets the premise type.
    *
    * @return the premise type
    */
   @Override
   public PremiseType getPremiseType() {
      return this.relationshipAdaptorChronicleKey.premiseType;
   }

   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   @Override
   public UUID getPrimordialUuid() {
      return this.chronology.getPrimordialUuid();
   }

   /**
    * Gets the referenced component nid.
    *
    * @return the referenced component nid
    */
   @Override
   public int getReferencedComponentNid() {
      return this.chronology.getReferencedComponentNid();
   }

   /**
    * Gets the sememe sequence.
    *
    * @return the sememe sequence
    */
   @Override
   public int getSememeSequence() {
      return this.chronology.getSememeSequence();
   }

   /**
    * Gets the stamp sequence.
    *
    * @return the stamp sequence
    */
   @Override
   public int getStampSequence() {
      return this.stampSequence;
   }

   /**
    * Gets the state.
    *
    * @return the state
    */
   @Override
   public State getState() {
      return Get.stampService()
                .getStatusForStamp(this.stampSequence);
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   @Override
   public long getTime() {
      return Get.stampService()
                .getTimeForStamp(this.stampSequence);
   }

   /**
    * Gets the type sequence.
    *
    * @return the type sequence
    */
   @Override
   public int getTypeSequence() {
      return this.relationshipAdaptorChronicleKey.typeSequence;
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() {
      return this.chronology.getUuidList();
   }
}

