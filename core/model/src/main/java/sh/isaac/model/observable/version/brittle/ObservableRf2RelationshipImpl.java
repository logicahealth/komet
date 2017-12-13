/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.observable.version.brittle;

import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.component.semantic.version.brittle.Rf2Relationship;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.ObservableRf2Relationship;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableSemanticVersionImpl;
import sh.isaac.model.semantic.version.brittle.Rf2RelationshipImpl;

/**
 *
 * @author kec
 */
public class ObservableRf2RelationshipImpl 
        extends ObservableSemanticVersionImpl
         implements ObservableRf2Relationship {
   IntegerProperty typeNidProperty;
   IntegerProperty destinationNidProperty;
   IntegerProperty relationshipGroupProperty;
   IntegerProperty characteristicNidProperty;
   IntegerProperty modifierNidProperty;

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(typeNidProperty());
      properties.add(destinationNidProperty());
      properties.add(relationshipGroupProperty());
      properties.add(characteristicNidProperty());
      properties.add(modifierNidProperty());
      return properties;
   }
   

   @Override
   protected void updateVersion() {
      super.updateVersion();
      if (this.typeNidProperty != null && 
              this.typeNidProperty.get() != getRf2RelationshipImpl().getTypeNid()) {
         this.typeNidProperty.set(getRf2RelationshipImpl().getTypeNid());
      }

      if (this.destinationNidProperty != null && 
              this.destinationNidProperty.get() != getRf2RelationshipImpl().getDestinationNid()) {
         this.destinationNidProperty.set(
             getRf2RelationshipImpl().getDestinationNid());
      }

      if (this.relationshipGroupProperty != null &&
              this.relationshipGroupProperty.get() != getRf2RelationshipImpl().getRelationshipGroup()) {
         this.relationshipGroupProperty.set(
             getRf2RelationshipImpl().getRelationshipGroup());
      }

      if (this.characteristicNidProperty != null &&
              this.characteristicNidProperty.get() != getRf2RelationshipImpl().getCharacteristicNid()) {
         this.characteristicNidProperty.set(
             getRf2RelationshipImpl().getCharacteristicNid());
      }

      if (this.modifierNidProperty != null &&
              this.modifierNidProperty.get() != getRf2RelationshipImpl().getModifierNid()) {
         this.modifierNidProperty.set(
             getRf2RelationshipImpl().getModifierNid());
      }
   }
   
   public ObservableRf2RelationshipImpl(Rf2Relationship stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   private Rf2RelationshipImpl getRf2RelationshipImpl() {
      return (Rf2RelationshipImpl) this.stampedVersionProperty.get();
   }
   @Override
   public IntegerProperty typeNidProperty() {
      if (this.typeNidProperty == null) {
         this.typeNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.TYPE_NID_FOR_RF2_REL.toExternalString(),
             getTypeNid());
         this.typeNidProperty.addListener(
             (observable, oldValue, newValue) -> {
                getRf2RelationshipImpl().setTypeNid(newValue.intValue());
             });
      }

      return this.typeNidProperty;
   }
   @Override
   public void setTypeNid(int nid) {
      if (this.typeNidProperty != null) {
         this.typeNidProperty.set(nid);
      }
      getRf2RelationshipImpl().setTypeNid(nid);
   } 
   @Override
   public int getTypeNid() {
      if (this.typeNidProperty != null) {
         return this.typeNidProperty.get();
      }
      return getRf2RelationshipImpl().getTypeNid();
   }
   

   @Override
   public IntegerProperty destinationNidProperty() {
     if (this.destinationNidProperty == null) {
         this.destinationNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.DESTINATION_NID_FOR_RF2_REL.toExternalString(),
             getDestinationNid());
         this.destinationNidProperty.addListener(
             (observable, oldValue, newValue) -> {
                getRf2RelationshipImpl().setDestinationNid(newValue.intValue());
             });
      }

      return this.destinationNidProperty;
   }
   @Override
   public void setDestinationNid(int nid) {
      if (this.destinationNidProperty != null) {
         this.destinationNidProperty.set(nid);
      }
      getRf2RelationshipImpl().setDestinationNid(nid);
   }
   @Override
   public int getDestinationNid() {
      if (this.destinationNidProperty != null) {
         return this.destinationNidProperty.get();
      }
      return getRf2RelationshipImpl().getDestinationNid();
   }

   
   @Override
   public IntegerProperty relationshipGroupProperty() {
     if (this.relationshipGroupProperty == null) {
         this.relationshipGroupProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.REL_GROUP_FOR_RF2_REL.toExternalString(),
             getRelationshipGroup());
         this.relationshipGroupProperty.addListener(
             (observable, oldValue, newValue) -> {
                getRf2RelationshipImpl().setRelationshipGroup(newValue.intValue());
             });
      }

      return this.relationshipGroupProperty;
   }
   @Override
   public void setRelationshipGroup(int group) {
      if (this.relationshipGroupProperty != null) {
         this.relationshipGroupProperty.set(group);
      }
      getRf2RelationshipImpl().setRelationshipGroup(group);
   }
   @Override
   public int getRelationshipGroup() {
       if (this.relationshipGroupProperty != null) {
         return this.relationshipGroupProperty.get();
      }
      return getRf2RelationshipImpl().getRelationshipGroup();
  }

   
   @Override
   public IntegerProperty characteristicNidProperty() {
     if (this.characteristicNidProperty == null) {
         this.characteristicNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.CHARACTERISTIC_NID_FOR_RF2_REL.toExternalString(),
             getCharacteristicNid());
         this.characteristicNidProperty.addListener(
             (observable, oldValue, newValue) -> {
                getRf2RelationshipImpl().setCharacteristicNid(newValue.intValue());
             });
      }

      return this.characteristicNidProperty;
   }
   @Override
   public void setCharacteristicNid(int nid) {
      if (this.characteristicNidProperty != null) {
         this.characteristicNidProperty.set(nid);
      }
      getRf2RelationshipImpl().setCharacteristicNid(nid);
   }
   @Override
   public int getCharacteristicNid() {
      if (this.characteristicNidProperty != null) {
         return this.characteristicNidProperty.get();
      }
      return getRf2RelationshipImpl().getCharacteristicNid();
   }

   
   @Override
   public IntegerProperty modifierNidProperty() {
     if (this.modifierNidProperty == null) {
         this.modifierNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.MODIFIER_NID_FOR_RF2_REL.toExternalString(),
             getModifierNid());
         this.modifierNidProperty.addListener(
             (observable, oldValue, newValue) -> {
                getRf2RelationshipImpl().setModifierNid(newValue.intValue());
             });
      }

      return this.modifierNidProperty;
   }
   @Override
   public void setModifierNid(int nid) {
       if (this.modifierNidProperty != null) {
         this.modifierNidProperty.set(nid);
      }
      getRf2RelationshipImpl().setModifierNid(nid);
   }
   @Override
   public int getModifierNid() {
      if (this.modifierNidProperty != null) {
         return this.modifierNidProperty.get();
      }
      return getRf2RelationshipImpl().getModifierNid();
   }
   
}
