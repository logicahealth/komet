/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.ddo;

/**
 * Enumeration to represent the state of a logical definition for a component. 
 * @author kec
 */
public enum DefinitionalState {
    /**
     * The concept has only necessary conditions defined. 
     */
    NECESSARY, 
    
    /**
     * The concept has necessary and sufficient conditions. 
     */
    NECESSARY_AND_SUFFICIENT, 
    /**
     * It is undetermined if the component is a concept, or if it is a concept, 
     * it is undetermined if the concept has necessary or sufficient conditions 
     * defined. 
     */
    UNDETERMINED, 
    /**
     * The component is not a concept, and therefore is not capable of being 
     * logically defined. 
     */
    NOT_A_DEFINED_COMPONENT;
}
