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



package sh.isaac.model.observable.version.brittle;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.SemanticVersion;

import sh.isaac.api.component.semantic.version.brittle.Rf2Relationship;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.ObservableRf2Relationship;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Rf2RelationshipImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableRf2RelationshipImpl
        extends ObservableAbstractSemanticVersionImpl
         implements ObservableRf2Relationship {
   IntegerProperty typeNidProperty;
   IntegerProperty destinationNidProperty;
   IntegerProperty relationshipGroupProperty;
   IntegerProperty characteristicNidProperty;
   IntegerProperty modifierNidProperty;

   //~--- constructors --------------------------------------------------------

   public ObservableRf2RelationshipImpl(Rf2Relationship stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   public ObservableRf2RelationshipImpl(ObservableRf2RelationshipImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setTypeNid(versionToClone.getTypeNid());
      setDestinationNid(versionToClone.getDestinationNid());
      setRelationshipGroup(versionToClone.getRelationshipGroup());
      setCharacteristicNid(versionToClone.getCharacteristicNid());
      setModifierNid(versionToClone.getModifierNid());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        ObservableRf2RelationshipImpl analog = new ObservableRf2RelationshipImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(mc.getModuleNidForAnalog(this));
        analog.setAuthorNid(mc.getAuthorNidForChanges());
        analog.setPathNid(mc.getPathNidForAnalog());
        return (V) analog;
    }

   //~--- methods -------------------------------------------------------------

   @Override
   public IntegerProperty characteristicNidProperty() {
      if (this.stampedVersionProperty == null  && this.characteristicNidProperty == null) {
         this.characteristicNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.CHARACTERISTIC_NID_FOR_RF2_REL.toExternalString(),
             0);
      }
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
   public IntegerProperty destinationNidProperty() {
      if (this.stampedVersionProperty == null  && this.destinationNidProperty == null) {
         this.destinationNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.DESTINATION_NID_FOR_RF2_REL.toExternalString(),
             0);
      }
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
   public IntegerProperty modifierNidProperty() {
      if (this.stampedVersionProperty == null  && this.modifierNidProperty == null) {
         this.modifierNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.MODIFIER_NID_FOR_RF2_REL.toExternalString(),
             0);
      }
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
   public IntegerProperty relationshipGroupProperty() {
      if (this.stampedVersionProperty == null  && this.relationshipGroupProperty == null) {
         this.relationshipGroupProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.REL_GROUP_FOR_RF2_REL.toExternalString(),
             0);
      }
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
   public IntegerProperty typeNidProperty() {
      if (this.stampedVersionProperty == null  && this.typeNidProperty == null) {
         this.typeNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.TYPE_NID_FOR_RF2_REL.toExternalString(),
             0);
      }
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
   protected void updateVersion() {

      if ((this.typeNidProperty != null) && (this.typeNidProperty.get() != getRf2RelationshipImpl().getTypeNid())) {
         this.typeNidProperty.set(getRf2RelationshipImpl().getTypeNid());
      }

      if ((this.destinationNidProperty != null) &&
            (this.destinationNidProperty.get() != getRf2RelationshipImpl().getDestinationNid())) {
         this.destinationNidProperty.set(getRf2RelationshipImpl().getDestinationNid());
      }

      if ((this.relationshipGroupProperty != null) &&
            (this.relationshipGroupProperty.get() != getRf2RelationshipImpl().getRelationshipGroup())) {
         this.relationshipGroupProperty.set(getRf2RelationshipImpl().getRelationshipGroup());
      }

      if ((this.characteristicNidProperty != null) &&
            (this.characteristicNidProperty.get() != getRf2RelationshipImpl().getCharacteristicNid())) {
         this.characteristicNidProperty.set(getRf2RelationshipImpl().getCharacteristicNid());
      }

      if ((this.modifierNidProperty != null) &&
            (this.modifierNidProperty.get() != getRf2RelationshipImpl().getModifierNid())) {
         this.modifierNidProperty.set(getRf2RelationshipImpl().getModifierNid());
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getCharacteristicNid() {
      if (this.characteristicNidProperty != null) {
         return this.characteristicNidProperty.get();
      }

      return getRf2RelationshipImpl().getCharacteristicNid();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setCharacteristicNid(int nid) {
       if (this.stampedVersionProperty == null) {
           this.characteristicNidProperty();
       }
      if (this.characteristicNidProperty != null) {
         this.characteristicNidProperty.set(nid);
      }

      if (this.stampedVersionProperty != null) {
        getRf2RelationshipImpl().setCharacteristicNid(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getDestinationNid() {
      if (this.destinationNidProperty != null) {
         return this.destinationNidProperty.get();
      }

      return getRf2RelationshipImpl().getDestinationNid();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setDestinationNid(int nid) {
       if (this.stampedVersionProperty == null) {
           this.destinationNidProperty();
       }
      if (this.destinationNidProperty != null) {
         this.destinationNidProperty.set(nid);
      }

      if (this.stampedVersionProperty != null) {
        getRf2RelationshipImpl().setDestinationNid(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getModifierNid() {
      if (this.modifierNidProperty != null) {
         return this.modifierNidProperty.get();
      }

      return getRf2RelationshipImpl().getModifierNid();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setModifierNid(int nid) {
       if (this.stampedVersionProperty == null) {
           this.modifierNidProperty();
       }
      if (this.modifierNidProperty != null) {
         this.modifierNidProperty.set(nid);
      }

      if (this.stampedVersionProperty != null) {
        getRf2RelationshipImpl().setModifierNid(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

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
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(typeNidProperty());
      properties.add(destinationNidProperty());
      properties.add(relationshipGroupProperty());
      properties.add(characteristicNidProperty());
      properties.add(modifierNidProperty());
      return properties;
    }

   @Override
   public int getRelationshipGroup() {
      if (this.relationshipGroupProperty != null) {
         return this.relationshipGroupProperty.get();
      }

      return getRf2RelationshipImpl().getRelationshipGroup();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setRelationshipGroup(int group) {
       if (this.stampedVersionProperty == null) {
           this.relationshipGroupProperty();
       }
      if (this.relationshipGroupProperty != null) {
         this.relationshipGroupProperty.set(group);
      }

      if (this.stampedVersionProperty != null) {
        getRf2RelationshipImpl().setRelationshipGroup(group);
      }
   }

   //~--- get methods ---------------------------------------------------------

   private Rf2RelationshipImpl getRf2RelationshipImpl() {
      return (Rf2RelationshipImpl) this.stampedVersionProperty.get();
   }

   @Override
   public int getTypeNid() {
      if (this.typeNidProperty != null) {
         return this.typeNidProperty.get();
      }

      return getRf2RelationshipImpl().getTypeNid();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setTypeNid(int nid) {
       if (this.stampedVersionProperty == null) {
           this.typeNidProperty();
       }
      if (this.typeNidProperty != null) {
         this.typeNidProperty.set(nid);
      }

      if (this.stampedVersionProperty != null) {
        getRf2RelationshipImpl().setTypeNid(nid);
      }
   }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof ObservableRf2RelationshipImpl) {
            ObservableRf2RelationshipImpl observableAnalog = (ObservableRf2RelationshipImpl) analog;
            observableAnalog.setCharacteristicNid(this.getCharacteristicNid());
            observableAnalog.setDestinationNid(this.getDestinationNid());
            observableAnalog.setModifierNid(this.getModifierNid());
            observableAnalog.setRelationshipGroup(this.getRelationshipGroup());
            observableAnalog.setTypeNid(this.getTypeNid());
        } else if (analog instanceof Rf2RelationshipImpl) {
            Rf2RelationshipImpl simpleAnalog = (Rf2RelationshipImpl) analog;
            simpleAnalog.setCharacteristicNid(this.getCharacteristicNid());
            simpleAnalog.setDestinationNid(this.getDestinationNid());
            simpleAnalog.setModifierNid(this.getModifierNid());
            simpleAnalog.setRelationshipGroup(this.getRelationshipGroup());
            simpleAnalog.setTypeNid(this.getTypeNid());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Rf2RelationshipImpl newVersion = new Rf2RelationshipImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    public <V extends Version> V setupAnalog(int stampSequence) {
        Rf2RelationshipImpl newVersion = getStampedVersion().setupAnalog(stampSequence);
        ObservableRf2RelationshipImpl newObservableVersion = new ObservableRf2RelationshipImpl(
                newVersion,
                getChronology());
        chronology.getVersionList()
                .add(newObservableVersion);
        return (V) newObservableVersion;
    }
}

