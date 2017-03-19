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

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableDescriptionSememe;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.sememe.version.DescriptionSememeImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableDescriptionImpl
        extends ObservableSememeVersionImpl<ObservableDescriptionImpl>
         implements ObservableDescriptionSememe<ObservableDescriptionImpl> {
   IntegerProperty caseSignificanceConceptSequenceProperty;
   IntegerProperty languageConceptSequenceProperty;
   StringProperty  textProperty;
   IntegerProperty descriptionTypeConceptSequenceProperty;

   //~--- constructors --------------------------------------------------------

   public ObservableDescriptionImpl(DescriptionSememeImpl stampedVersion,
                                    ObservableSememeChronology<ObservableDescriptionImpl> chronology) {
      super(stampedVersion, chronology);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public IntegerProperty caseSignificanceConceptSequenceProperty() {
      if (caseSignificanceConceptSequenceProperty == null) {
         caseSignificanceConceptSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.CASE_SIGNIFICANCE_CONCEPT_SEQUENCE_FOR_DESCRIPTION.toExternalString(),
               getCaseSignificanceConceptSequence());
      }

      return caseSignificanceConceptSequenceProperty;
   }

   @Override
   public IntegerProperty descriptionTypeConceptSequenceProperty() {
      if (descriptionTypeConceptSequenceProperty == null) {
         descriptionTypeConceptSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.DESCRIPTION_TYPE_FOR_DESCRIPTION.toExternalString(),
               getDescriptionTypeConceptSequence());
      }

      return descriptionTypeConceptSequenceProperty;
   }

   @Override
   public IntegerProperty languageConceptSequenceProperty() {
      if (languageConceptSequenceProperty == null) {
         languageConceptSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.LANGUAGE_CONCEPT_SEQUENCE_FOR_DESCRIPTION.toExternalString(),
               getLanguageConceptSequence());
      }

      return languageConceptSequenceProperty;
   }

   @Override
   public StringProperty textProperty() {
      if (textProperty == null) {
         textProperty = new CommitAwareStringProperty(this,
               ObservableFields.TEXT_FOR_DESCRIPTION.toExternalString(),
               getText());
      }

      return textProperty;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getCaseSignificanceConceptSequence() {
      if (caseSignificanceConceptSequenceProperty != null) {
         return caseSignificanceConceptSequenceProperty.get();
      }

      return ((DescriptionSememeImpl) stampedVersion).getCaseSignificanceConceptSequence();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setCaseSignificanceConceptSequence(int caseSignificanceConceptSequence) {
      if (caseSignificanceConceptSequenceProperty != null) {
         caseSignificanceConceptSequenceProperty.set(caseSignificanceConceptSequence);
      } else {
         ((DescriptionSememeImpl) stampedVersion).setCaseSignificanceConceptSequence(caseSignificanceConceptSequence);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getDescriptionTypeConceptSequence() {
      if (descriptionTypeConceptSequenceProperty != null) {
         return descriptionTypeConceptSequenceProperty.get();
      }

      return ((DescriptionSememeImpl) stampedVersion).getDescriptionTypeConceptSequence();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDescriptionTypeConceptSequence(int descriptionTypeConceptSequence) {
      if (descriptionTypeConceptSequenceProperty != null) {
         descriptionTypeConceptSequenceProperty.set(descriptionTypeConceptSequence);
      }

      ((DescriptionSememeImpl) stampedVersion).setDescriptionTypeConceptSequence(descriptionTypeConceptSequence);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getLanguageConceptSequence() {
      if (languageConceptSequenceProperty != null) {
         return languageConceptSequenceProperty.get();
      }

      return ((DescriptionSememeImpl) stampedVersion).getLanguageConceptSequence();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLanguageConceptSequence(int languageConceptSequence) {
      if (languageConceptSequenceProperty != null) {
         languageConceptSequenceProperty.set(languageConceptSequence);
      } else {
         ((DescriptionSememeImpl) stampedVersion).setLanguageConceptSequence(languageConceptSequence);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getText() {
      if (textProperty != null) {
         return textProperty.get();
      }

      return ((DescriptionSememeImpl) stampedVersion).getText();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setText(String text) {
      if (textProperty != null) {
         textProperty.set(text);
      }

      ((DescriptionSememeImpl) stampedVersion).setText(text);
   }
}

