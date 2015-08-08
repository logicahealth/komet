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

import gov.vha.isaac.ochre.api.chronicle.IdentifiedObjectLocal;

/**
 *
 * @author kec
 */
public interface SememeObject extends IdentifiedObjectLocal {
    
    /**
     * 
     * @return unique sequential identifier >= 0 for this sememe.  
     */
    int getSememeSequence();
    
    /**
     * 
     * @return concept sequence for the concept that identifies this assemblage.  
     */
    int getAssemblageSequence();
    
    /**
     * 
     * @return nid for the component referenced by this sememe. Since the referenced component can either
     * be a concept or another sememe, nids are used instead of concept sequences or sememe sequences, since the 
     * concept and sememe namespaces overlap. 
     */
    int getReferencedComponentNid();
    
}
