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

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableDescriptionVersionImpl.
 *
 * @author kec
 */
public class ObservableDescriptionVersionImpl
        extends ObservableSemanticVersionImpl
         implements ObservableDescriptionVersion {
   /**
    * The case significance concept sequence property.
    */
   IntegerProperty caseSignificanceConceptNidProperty;

   /**
    * The language concept sequence property.
    */
   IntegerProperty languageConceptNidProperty;

   /**
    * The text property.
    */
   StringProperty textProperty;

   /**
    * The description type concept sequence property.
    */
   IntegerProperty descriptionTypeConceptNidProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable description impl.
    *
    * @param stampedVersion the stamped version
    * @param chronology the chronology
    */
   public ObservableDescriptionVersionImpl(DescriptionVersion stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Case significance concept sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty caseSignificanceConceptNidProperty() {
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

   /**
    * Description type concept sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty descriptionTypeConceptNidProperty() {
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

   /**
    * Language concept sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty languageConceptNidProperty() {
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

   /**
    * Sets the language concept sequence.
    *
    * @param languageConceptNid the new language concept sequence
    */
   @Override
   public void setLanguageConceptNid(int languageConceptNid) {
      if (this.languageConceptNidProperty != null) {
         this.languageConceptNidProperty.set(languageConceptNid);
      }

      ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setLanguageConceptNid(languageConceptNid);
   }

   /**
    * Gets the language concept sequence.
    *
    * @return the language concept sequence
    */
   @Override
   public int getLanguageConceptNid() {
      if (this.languageConceptNidProperty != null) {
         return this.languageConceptNidProperty.get();
      }

      return ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getLanguageConceptNid();
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      DescriptionVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
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
      super.updateVersion();
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

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the case significance concept sequence.
    *
    * @return the case significance concept sequence
    */
   @Override
   public int getCaseSignificanceConceptNid() {
      if (this.caseSignificanceConceptNidProperty != null) {
         return this.caseSignificanceConceptNidProperty.get();
      }

      return ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getCaseSignificanceConceptNid();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the case significance concept sequence.
    *
    * @param caseSignificanceConceptSequence the new case significance concept sequence
    */
   @Override
   public void setCaseSignificanceConceptSequence(int caseSignificanceConceptSequence) {
      if (this.caseSignificanceConceptNidProperty != null) {
         this.caseSignificanceConceptNidProperty.set(caseSignificanceConceptSequence);
      }

      ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setCaseSignificanceConceptNid(
          caseSignificanceConceptSequence);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description type concept sequence.
    *
    * @return the description type concept sequence
    */
   @Override
   public int getDescriptionTypeConceptNid() {
      if (this.descriptionTypeConceptNidProperty != null) {
         return this.descriptionTypeConceptNidProperty.get();
      }

      return ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getDescriptionTypeConceptNid();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the description type concept sequence.
    *
    * @param descriptionTypeConceptSequence the new description type concept sequence
    */
   @Override
   public void setDescriptionTypeConceptSequence(int descriptionTypeConceptSequence) {
      if (this.descriptionTypeConceptNidProperty != null) {
         this.descriptionTypeConceptNidProperty.set(descriptionTypeConceptSequence);
      }

      ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setDescriptionTypeConceptNid(descriptionTypeConceptSequence);
   }

   //~--- get methods ---------------------------------------------------------

   //~--- set methods ---------------------------------------------------------

   //~--- get methods ---------------------------------------------------------

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(textProperty());
      properties.add(languageConceptNidProperty());
      properties.add(descriptionTypeConceptNidProperty());
      properties.add(caseSignificanceConceptNidProperty());
      return properties;
   }

   /**
    * Gets the text.
    *
    * @return the text
    */
   @Override
   public String getText() {
      if (this.textProperty != null) {
         return this.textProperty.get();
      }

      return ((DescriptionVersionImpl) this.stampedVersionProperty.get()).getText();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the text.
    *
    * @param text the new text
    */
   @Override
   public void setText(String text) {
      if (this.textProperty != null) {
         this.textProperty.set(text);
      }

      ((DescriptionVersionImpl) this.stampedVersionProperty.get()).setText(text);
   }
}

