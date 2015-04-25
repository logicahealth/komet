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
package gov.vha.isaac.ochre.api.sememe;

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.IdentifiedComponentBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface SememeBuilderService {
    
    SememeBuilder getComponentSememeBuilder(int memeComponentNid,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder getComponentSememeBuilder(int memeComponentNid,
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    
    SememeBuilder getConceptSememeBuilder(ConceptProxy memeConceptProxy,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder getConceptSememeBuilder(ConceptProxy memeConceptProxy,
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    
    SememeBuilder getConceptTimeSememeBuilder(ConceptProxy memeConceptProxy,
            long memeTime,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder getConceptTimeSememeBuilder(ConceptProxy memeConceptProxy,
            long memeTime,
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    
    SememeBuilder getLogicalExpressionSememeBuilder(LogicalExpression expression,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder getLogicalExpressionSememeBuilder(LogicalExpression expression,
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    SememeBuilder getMembershipSememeBuilder(
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder getMembershipSememeBuilder(
            int referencedComponentNid, 
            int assemblageConceptSequence);
    
    
    SememeBuilder getStringSememeBuilder(String memeString,
            IdentifiedComponentBuilder referencedComponent, 
            int assemblageConceptSequence);
    
    SememeBuilder getStringSememeBuilder(String memeString,
            int referencedComponentNid, 
            int assemblageConceptSequence);
}
