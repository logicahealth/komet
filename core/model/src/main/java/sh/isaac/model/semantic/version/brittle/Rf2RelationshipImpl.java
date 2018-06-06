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



package sh.isaac.model.semantic.version.brittle;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Rf2Relationship;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Rf2RelationshipImpl
        extends AbstractVersionImpl
         implements Rf2Relationship {
   int typeNid           = Integer.MAX_VALUE;
   int destinationNid    = Integer.MAX_VALUE;
   int relationshipGroup = Integer.MAX_VALUE;
   int characteristicNid = Integer.MAX_VALUE;
   int modifierNid       = Integer.MAX_VALUE;

   //~--- constructors --------------------------------------------------------

   public Rf2RelationshipImpl(Rf2RelationshipImpl another, int stampSequence) {
      super(another.getChronology(), stampSequence);
      this.typeNid           = another.typeNid;
      this.destinationNid    = another.destinationNid;
      this.relationshipGroup = another.relationshipGroup;
      this.characteristicNid = another.characteristicNid;
      this.modifierNid       = another.modifierNid;
   }

   public Rf2RelationshipImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   public Rf2RelationshipImpl(SemanticChronology container, int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.typeNid           = data.getNid();
      this.destinationNid    = data.getNid();
      this.relationshipGroup = data.getInt();
      this.characteristicNid = data.getNid();
      this.modifierNid       = data.getNid();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      final int stampSequence = Get.stampService()
                                   .getStampSequence(
                                       this.getStatus(),
                                       Long.MAX_VALUE,
                                       ec.getAuthorNid(),
                                       this.getModuleNid(),
                                       ec.getPathNid());
      SemanticChronologyImpl    chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final Rf2RelationshipImpl newVersion     = new Rf2RelationshipImpl(this, stampSequence);

      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("{Rel≤rc: ")
        .append(Get.conceptDescriptionText(getReferencedComponentNid()))
        .append(" <")
        .append(getReferencedComponentNid())
        .append(">, ")
        .append(Get.conceptDescriptionText(this.typeNid))
        .append(" <")
        .append(this.typeNid)
        .append(">, ")
        .append(Get.conceptDescriptionText(this.destinationNid))
        .append(" <")
        .append(this.destinationNid)
        .append(">, group: ")
        .append(this.relationshipGroup)
        .append(" characteristic: ")
        .append(Get.conceptDescriptionText(this.characteristicNid))
        .append(" <")
        .append(this.characteristicNid)
        .append(">, modifier: ")
        .append(Get.conceptDescriptionText(this.modifierNid))
        .append(" <")
        .append(this.modifierNid)
        .append(">");
      toString(sb);
      sb.append("≥}");
      return sb.toString();
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      if (!(other instanceof LogicGraphVersionImpl)) {
         return false;
      }

      Rf2RelationshipImpl otherImpl = (Rf2RelationshipImpl) other;

      if (this.typeNid != otherImpl.typeNid) {
         return false;
      }

      if (this.destinationNid != otherImpl.destinationNid) {
         return false;
      }

      if (this.relationshipGroup != otherImpl.relationshipGroup) {
         return false;
      }

      if (this.characteristicNid != otherImpl.characteristicNid) {
         return false;
      }

      return this.modifierNid == otherImpl.modifierNid;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      Rf2RelationshipImpl otherImpl = (Rf2RelationshipImpl) other;

      if (this.typeNid != otherImpl.typeNid) {
         editDistance++;
      }

      if (this.destinationNid != otherImpl.destinationNid) {
         editDistance++;
      }

      if (this.relationshipGroup != otherImpl.relationshipGroup) {
         editDistance++;
      }

      if (this.characteristicNid != otherImpl.characteristicNid) {
         editDistance++;
      }

      if (this.modifierNid != otherImpl.modifierNid) {
         editDistance++;
      }

      return editDistance;
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putNid(this.typeNid);
      data.putNid(this.destinationNid);
      data.putInt(this.relationshipGroup);
      data.putNid(this.characteristicNid);
      data.putNid(this.modifierNid);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getCharacteristicNid() {
      return characteristicNid;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setCharacteristicNid(int nid) {
      this.characteristicNid = nid;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getDestinationNid() {
      return destinationNid;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDestinationNid(int nid) {
      this.destinationNid = nid;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getModifierNid() {
      return modifierNid;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setModifierNid(int nid) {
      this.modifierNid = nid;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getRelationshipGroup() {
      return relationshipGroup;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setRelationshipGroup(int group) {
      this.relationshipGroup = group;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public VersionType getSemanticType() {
      return VersionType.RF2_RELATIONSHIP;
   }

   @Override
   public int getTypeNid() {
      return typeNid;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setTypeNid(int nid) {
      this.typeNid = nid;
   }
}

