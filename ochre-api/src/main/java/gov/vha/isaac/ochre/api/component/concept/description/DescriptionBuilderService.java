/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.component.concept.description;

import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDescriptionSememe;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface DescriptionBuilderService {
    
    DescriptionBuilder<? extends SememeChronology<?>, ? extends MutableDescriptionSememe<?>>  getDescriptionBuilder(String descriptionText, 
            int conceptSequence,
            ConceptSpecification descriptionType, 
            ConceptSpecification languageForDescription);
    
    DescriptionBuilder<? extends SememeChronology<?>, ? extends MutableDescriptionSememe<?>>  getDescriptionBuilder(String descriptionText, 
            ConceptBuilder conceptBuilder,
            ConceptSpecification descriptionType, 
            ConceptSpecification languageForDescription);
}
