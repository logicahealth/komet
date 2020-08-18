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



package sh.isaac.model.observable.version;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;

/**
 * The Class ObservableDescriptionVersionImpl.
 *
 * @author kec
 */
public class ObservableDescriptionVersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements ObservableDescriptionVersion {
   /**
    * The case significance concept nid property.
    */
   IntegerProperty caseSignificanceConceptNidProperty;

   /**
    * The language concept nid property.
    */
   IntegerProperty languageConceptNidProperty;

   /**
    * The text property.
    */
   StringProperty textProperty;

   /**
    * The description type concept nid property.
    */
   IntegerProperty descriptionTypeConceptNidProperty;

   /**
    * A constructor for de novo creation. For example when creating a new component prior to being committed for 
    * the first time. 
     * @param primordialUuid
     * @param referencedComponentUuid
     * @param assemblageNid
    */
   public ObservableDescriptionVersionImpl(UUID primordialUuid, UUID referencedComponentUuid, int assemblageNid) {
       super(VersionType.DESCRIPTION, primordialUuid, referencedComponentUuid, assemblageNid);
   }

    /**
     * Instantiates a new observable description.
     *
     * @param stampedVersion the stamped version
     * @param chronology the chronology
     */
    public ObservableDescriptionVersionImpl(DescriptionVersion stampedVersion, ObservableSemanticChronology chronology) {
        super(stampedVersion, chronology);
    }

   public ObservableDescriptionVersionImpl(ObservableDescriptionVersion versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setCaseSignificanceConceptNid(versionToClone.getCaseSignificanceConceptNid());
      setLanguageConceptNid(versionToClone.getLanguageConceptNid());
      setText(versionToClone.getText());
      setDescriptionTypeConceptNid(versionToClone.getDescriptionTypeConceptNid());
   }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        ObservableDescriptionVersionImpl analog = new ObservableDescriptionVersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(ec.getModuleNid());
        analog.setAuthorNid(ec.getAuthorNid());
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }

   @Override
   public IntegerProperty caseSignificanceConceptNidProperty() {
      if (this.stampedVersionProperty == null && caseSignificanceConceptNidProperty == null) {
         this.caseSignificanceConceptNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION.toExternalString(),
                 0);
      }
      if (this.caseSignificanceConceptNidProperty == null) {
         this.caseSignificanceConceptNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION.toExternalString(),
             getCaseSignificanceConceptNid());
         this.caseSignificanceConceptNidProperty.addListener(
             (observable, oldValue, newValue) -> {
                ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setCaseSignificanceConceptNid(newValue.intValue());
             });
      }

      return this.caseSignificanceConceptNidProperty;
   }

   @Override
   public IntegerProperty descriptionTypeConceptNidProperty() {
      if (this.stampedVersionProperty == null && descriptionTypeConceptNidProperty == null) {
         this.descriptionTypeConceptNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.DESCRIPTION_TYPE_FOR_DESCRIPTION.toExternalString(),
                 0);
      }
      if (this.descriptionTypeConceptNidProperty == null) {
         this.descriptionTypeConceptNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.DESCRIPTION_TYPE_FOR_DESCRIPTION.toExternalString(),
             getDescriptionTypeConceptNid());
         this.descriptionTypeConceptNidProperty.addListener(
             (observable, oldValue, newValue) -> {
                ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setDescriptionTypeConceptNid(newValue.intValue());
             });
      }

      return this.descriptionTypeConceptNidProperty;
   }

   @Override
   public IntegerProperty languageConceptNidProperty() {
      if (this.stampedVersionProperty == null && languageConceptNidProperty == null) {
         this.languageConceptNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION.toExternalString(),
                 0);
      }
      if (this.languageConceptNidProperty == null) {
         this.languageConceptNidProperty = new CommitAwareIntegerProperty(
             this,
             ObservableFields.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION.toExternalString(),
             getLanguageConceptNid());
         this.languageConceptNidProperty.addListener(
             (observable, oldValue, newValue) -> {
                ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setLanguageConceptNid(newValue.intValue());
             });
      }

      return this.languageConceptNidProperty;
   }

   @Override
   public final void setLanguageConceptNid(int languageConceptNid) {
       if (this.stampedVersionProperty == null) {
           this.languageConceptNidProperty();
       }
      if (this.languageConceptNidProperty != null) {
         this.languageConceptNidProperty.set(languageConceptNid);
      }

      if (this.stampedVersionProperty != null) {
        ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setLanguageConceptNid(languageConceptNid);
      }
   }

   @Override
   public int getLanguageConceptNid() {
      if (this.languageConceptNidProperty != null) {
         return this.languageConceptNidProperty.get();
      }

      return ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getLanguageConceptNid();
   }

   @SuppressWarnings("unchecked")
   @Override
   public <V extends Version> V makeAnalog(int stampSequence) {
      DescriptionVersion newVersion = this.stampedVersionProperty.get().makeAnalog(stampSequence);
      ObservableDescriptionVersionImpl newObservableVersion = new ObservableDescriptionVersionImpl(
                                                                  newVersion,
                                                                        (ObservableSemanticChronology) chronology);
        ((ObservableChronologyImpl) chronology).getVersionList()
                .add(newObservableVersion);
        return (V) newObservableVersion;
    }

    /**
    * Text property.
    *
    * @return the string property
    */
   @Override
   public StringProperty textProperty() {
      if (this.stampedVersionProperty == null && this.textProperty == null) {
         this.textProperty = new CommitAwareStringProperty(
             this,
             ObservableFields.TEXT_FOR_DESCRIPTION.toExternalString(),
                 "");
      }
      if (this.textProperty == null) {
         this.textProperty = new CommitAwareStringProperty(
             this,
             ObservableFields.TEXT_FOR_DESCRIPTION.toExternalString(),
             getText());
         this.textProperty.addListener(
             (observable, oldValue, newValue) -> {
                ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setText(newValue);
             });
      }

      return this.textProperty;
   }

   @Override
   public String toString() {
      return "ObservableDescriptionImpl{text:" + getText() + ", case: " + Get.conceptDescriptionText(
          getCaseSignificanceConceptNid()) + ", language:" + Get.conceptDescriptionText(
              getLanguageConceptNid()) + ", type: " + Get.conceptDescriptionText(
                  getDescriptionTypeConceptNid()) + " " + Get.stampService().describeStampSequence(
                      getStampSequence()) + '}';
   }

   @Override
   protected void updateVersion() {
      if (this.textProperty != null && 
              !this.textProperty.get().equals(((DescriptionVersionImpl) this.stampedVersionProperty.get()).getText())) {
         this.textProperty.set(((DescriptionVersionImpl) this.stampedVersionProperty.get()).getText());
      }

      if (this.caseSignificanceConceptNidProperty != null && 
              this.caseSignificanceConceptNidProperty.get() != ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getCaseSignificanceConceptNid()) {
         this.caseSignificanceConceptNidProperty.set(
             ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getCaseSignificanceConceptNid());
      }

      if (this.languageConceptNidProperty != null &&
              this.languageConceptNidProperty.get() != ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getLanguageConceptNid()) {
         this.languageConceptNidProperty.set(
             ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getLanguageConceptNid());
      }

      if (this.descriptionTypeConceptNidProperty != null &&
              this.descriptionTypeConceptNidProperty.get() != ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getDescriptionTypeConceptNid()) {
         this.descriptionTypeConceptNidProperty.set(
             ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getDescriptionTypeConceptNid());
      }
   }

   @Override
   public int getCaseSignificanceConceptNid() {
      if (this.caseSignificanceConceptNidProperty != null) {
         return this.caseSignificanceConceptNidProperty.get();
      }

      return ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getCaseSignificanceConceptNid();
   }

   @Override
   public final void setCaseSignificanceConceptNid(int caseSignificanceConceptSequence) {
       if (this.stampedVersionProperty == null) {
           this.caseSignificanceConceptNidProperty();
       }
      if (this.caseSignificanceConceptNidProperty != null) {
         this.caseSignificanceConceptNidProperty.set(caseSignificanceConceptSequence);
      }
      if (this.stampedVersionProperty != null) {

      ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setCaseSignificanceConceptNid(
          caseSignificanceConceptSequence);
      }
   }

   @Override
   public int getDescriptionTypeConceptNid() {
      if (this.descriptionTypeConceptNidProperty != null) {
         return this.descriptionTypeConceptNidProperty.get();
      }

      return ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getDescriptionTypeConceptNid();
   }

   @Override
   public final void setDescriptionTypeConceptNid(int descriptionTypeConceptNid) {
       if (this.stampedVersionProperty == null) {
           this.descriptionTypeConceptNidProperty();
       }
      if (this.descriptionTypeConceptNidProperty != null) {
         this.descriptionTypeConceptNidProperty.set(descriptionTypeConceptNid);
      }

      if (this.stampedVersionProperty != null) {
        ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setDescriptionTypeConceptNid(descriptionTypeConceptNid);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(textProperty());
      properties.add(languageConceptNidProperty());
      properties.add(descriptionTypeConceptNidProperty());
      properties.add(caseSignificanceConceptNidProperty());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(textProperty());
      properties.add(languageConceptNidProperty());
      properties.add(descriptionTypeConceptNidProperty());
      properties.add(caseSignificanceConceptNidProperty());
      return properties;
    }

   @Override
   public String getText() {
      if (this.textProperty != null) {
         return this.textProperty.get();
      }

      return ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getText();
   }

   @Override
   public final void setText(String text) {
       if (this.stampedVersionProperty == null) {
           this.textProperty();
       }
      if (this.textProperty != null) {
         this.textProperty.set(text);
      }

      if (this.stampedVersionProperty != null) {
        ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setText(text);
      }
   }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof ObservableDescriptionVersionImpl) {
            ObservableDescriptionVersionImpl observableAnalog = (ObservableDescriptionVersionImpl) analog;
            observableAnalog.setCaseSignificanceConceptNid(this.getCaseSignificanceConceptNid());
            observableAnalog.setDescriptionTypeConceptNid(this.getDescriptionTypeConceptNid());
            observableAnalog.setLanguageConceptNid(this.getLanguageConceptNid());
            observableAnalog.setText(this.getText());
        } else if (analog instanceof DescriptionVersionImpl) {
            DescriptionVersionImpl simpleAnalog = (DescriptionVersionImpl) analog;
            simpleAnalog.setCaseSignificanceConceptNid(this.getCaseSignificanceConceptNid());
            simpleAnalog.setDescriptionTypeConceptNid(this.getDescriptionTypeConceptNid());
            simpleAnalog.setLanguageConceptNid(this.getLanguageConceptNid());
            simpleAnalog.setText(this.getText());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        DescriptionVersionImpl newVersion = new DescriptionVersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }
}
