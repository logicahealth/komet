/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.model.observable.version;

import gov.vha.isaac.ochre.model.observable.CommitAwareIntegerProperty;
import gov.vha.isaac.ochre.model.observable.CommitAwareStringProperty;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableDescriptionSememe;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.model.observable.ObservableFields;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author kec
 */
public class ObservableDescriptionImpl 
    extends ObservableSememeVersionImpl<ObservableDescriptionImpl> 
    implements ObservableDescriptionSememe<ObservableDescriptionImpl> {
    
    IntegerProperty caseSignificanceConceptSequenceProperty;
    IntegerProperty languageConceptSequenceProperty;
    StringProperty textProperty;
    IntegerProperty descriptionTypeConceptSequenceProperty;
    

    public ObservableDescriptionImpl(DescriptionSememeImpl stampedVersion, 
            ObservableSememeChronology<ObservableDescriptionImpl> chronology) {
        super(stampedVersion, chronology);
    }
    
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
    public int getCaseSignificanceConceptSequence() {
        if (caseSignificanceConceptSequenceProperty != null) {
            return caseSignificanceConceptSequenceProperty.get();
        }
        return ((DescriptionSememeImpl) stampedVersion).getCaseSignificanceConceptSequence();
    }
    @Override
    public void setCaseSignificanceConceptSequence(int caseSignificanceConceptSequence) {
        if (caseSignificanceConceptSequenceProperty != null) {
            caseSignificanceConceptSequenceProperty.set(caseSignificanceConceptSequence);
        } else {
            ((DescriptionSememeImpl) stampedVersion).setCaseSignificanceConceptSequence(caseSignificanceConceptSequence);
        }
    }

    @Override
    public int getLanguageConceptSequence() {
        if (languageConceptSequenceProperty != null) {
            return languageConceptSequenceProperty.get();
        }
        return ((DescriptionSememeImpl) stampedVersion).getLanguageConceptSequence();
    }
    @Override
    public void setLanguageConceptSequence(int languageConceptSequence) {
        if (languageConceptSequenceProperty != null) {
            languageConceptSequenceProperty.set(languageConceptSequence);
        } else {
            ((DescriptionSememeImpl) stampedVersion).setLanguageConceptSequence(languageConceptSequence);
        }
    }


    @Override
    public String getText() {
        if (textProperty != null) {
            return textProperty.get();
        }
        return ((DescriptionSememeImpl) stampedVersion).getText();
    }

    @Override
    public void setText(String text) {
        if (textProperty != null) {
            textProperty.set(text);
        }
        ((DescriptionSememeImpl) stampedVersion).setText(text);
    }
    
    @Override
    public int getDescriptionTypeConceptSequence() {
        if (descriptionTypeConceptSequenceProperty != null) {
            return descriptionTypeConceptSequenceProperty.get();
        }
        return ((DescriptionSememeImpl) stampedVersion).getDescriptionTypeConceptSequence();
    }

    @Override
    public void setDescriptionTypeConceptSequence(int descriptionTypeConceptSequence) {
        if (descriptionTypeConceptSequenceProperty != null) {
            descriptionTypeConceptSequenceProperty.set(descriptionTypeConceptSequence);
        }
        ((DescriptionSememeImpl) stampedVersion).setDescriptionTypeConceptSequence(descriptionTypeConceptSequence);
    }
    
}
