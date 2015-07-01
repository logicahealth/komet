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
package gov.vha.isaac.ochre.api.relationship;

import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;

/**
 * A transient component derived from a logical expression. Attempts to persist 
 * this component will result in runtime errors. 
 * The native identifier and UUID for this component is synthetic, and guaranteed unique, 
 * within the life span of the ConceptChronology object from which it is derived. 
 * The native identifier will not participate in UUID to nid maps or other aspects 
 * of the identifier service. This object is not allowed to have associated sememes
 * because it is transient. 
 * <br/>
 * The RelationshipVersionAdaptor objects cannot be retrieved from the 
 * {@code IdentifiedObjectService} at this time, since they are transient. They can
 * only be retrieved by calling getRelationshipListWithConceptAsDestination or 
 * getRelationshipListOriginatingFromConcept on the ConceptChronology objects. 
 * <br/>
 * Components that use relationships should transition to using logic graphs directly. 
 * 
 * @author kec
 */
public interface RelationshipVersionAdaptor 
    extends SememeVersion {

    int getOriginSequence();
    
    int getDestinationSequence();
    
    int getTypeSequence();
    
    int getGroup();
    
    PremiseType getPremiseType();
    
    /**
     * 
     * @return sequence of the node in the logical expression 
     * from which this adaptor originated.  
     */
    short getNodeSequence();
    
    RelationshipAdaptorChronicleKey getChronicleKey();
    
}
