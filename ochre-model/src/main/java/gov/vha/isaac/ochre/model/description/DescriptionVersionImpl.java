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
package gov.vha.isaac.ochre.model.description;

import gov.vha.isaac.ochre.api.component.concept.description.MutableConceptDescription;
import gov.vha.isaac.ochre.model.ObjectChronicleImpl;
import gov.vha.isaac.ochre.model.ObjectVersionImpl;

/**
 *
 * @author kec
 */
public class DescriptionVersionImpl 
    extends ObjectVersionImpl<ObjectChronicleImpl<DescriptionVersionImpl>, DescriptionVersionImpl>
    implements MutableConceptDescription {

    protected int caseSignificanceConceptSequence;
    protected int languageConceptSequence;
    protected String text;
    protected int descriptionTypeConceptSequence;

    public DescriptionVersionImpl(ObjectChronicleImpl<DescriptionVersionImpl> chronicle, 
            int stampSequence, short versionSequence) {
        super(chronicle, stampSequence, versionSequence);
    }

    @Override
    public int getCaseSignificanceConceptSequence() {
        return caseSignificanceConceptSequence;
    }

    @Override
    public void setCaseSignificanceConceptSequence(int caseSignificanceConceptSequence) {
        this.caseSignificanceConceptSequence = caseSignificanceConceptSequence;
    }

    @Override
    public int getLanguageConceptSequence() {
        return languageConceptSequence;
    }

    @Override
    public void setLanguageConceptSequence(int languageConceptSequence) {
        this.languageConceptSequence = languageConceptSequence;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int getDescriptionTypeConceptSequence() {
        return descriptionTypeConceptSequence;
    }

    @Override
    public void setDescriptionTypeConceptSequence(int descriptionTypeConceptSequence) {
        this.descriptionTypeConceptSequence = descriptionTypeConceptSequence;
    }
    
}
