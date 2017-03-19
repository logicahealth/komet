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

import java.util.Objects;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.relationship.RelationshipAdaptorChronicleKey;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class RelationshipAdaptorChronicleKeyImpl
         implements RelationshipAdaptorChronicleKey {
   int         originSequence;
   int         destinationSequence;
   int         typeSequence;
   int         group;
   PremiseType premiseType;
   short       nodeSequence;

   //~--- constructors --------------------------------------------------------

   public RelationshipAdaptorChronicleKeyImpl(int originSequence,
         int destinationSequence,
         int typeSequence,
         int group,
         PremiseType premiseType,
         short nodeSequence) {
      this.originSequence      = originSequence;
      this.destinationSequence = destinationSequence;
      this.typeSequence        = typeSequence;
      this.group               = group;
      this.premiseType         = premiseType;
      this.nodeSequence        = nodeSequence;
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
   public String toString() {
      return "RelAdaptorKey{" + "originSequence=" + this.originSequence + ", destinationSequence=" + this.destinationSequence +
             ", typeSequence=" + this.typeSequence + ", group=" + this.group + ", premiseType=" + this.premiseType +
             ", nodeSequence=" + this.nodeSequence + '}';
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getDestinationSequence() {
      return this.destinationSequence;
   }

   @Override
   public int getGroup() {
      return this.group;
   }

   @Override
   public short getNodeSequence() {
      return this.nodeSequence;
   }

   @Override
   public int getOriginSequence() {
      return this.originSequence;
   }

   @Override
   public PremiseType getPremiseType() {
      return this.premiseType;
   }

   @Override
   public int getTypeSequence() {
      return this.typeSequence;
   }
}

