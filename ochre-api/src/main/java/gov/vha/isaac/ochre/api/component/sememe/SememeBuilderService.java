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
package gov.vha.isaac.ochre.api.component.sememe;

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.IdentifiedComponentBuilder;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 * @param <C>
 */
@Contract
public interface SememeBuilderService<C extends SememeChronology<? extends SememeVersion>> {
    
    SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    
    SememeBuilder<C> getConceptSememeBuilder(ConceptProxy memeConceptProxy,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder<C> getConceptSememeBuilder(ConceptProxy memeConceptProxy,
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    
    SememeBuilder<C> getConceptTimeSememeBuilder(ConceptProxy memeConceptProxy,
            long memeTime,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder<C> getConceptTimeSememeBuilder(ConceptProxy memeConceptProxy,
            long memeTime,
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    
    SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    SememeBuilder<C> getMembershipSememeBuilder(
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder<C> getMembershipSememeBuilder(
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    
    SememeBuilder<C> getStringSememeBuilder(String memeString,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder<C> getStringSememeBuilder(String memeString,
            int referencedComponentNid, 
            int assemblageConceptSequence);
}
