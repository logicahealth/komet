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
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.chronicle.IdentifiedObjectLocal;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author kec
 * @param <T>
 */
public interface IdentifiedComponentBuilder<T extends IdentifiedObjectLocal> {
    
    /**
     * If not set, a randomly generated UUID will be automatically used. 
     * @param uuid the primordial uuid for the component to be built. 
     * @return the builder for chaining of operations in a fluent pattern. 
     */
    IdentifiedComponentBuilder<T> setPrimordialUuid(UUID uuid);
    
    /**
     * Add additional uuids as identifiers for this component. 
     * @param uuids the additional uuids to add as identifiers for this component
     * @return  the builder for chaining of operations in a fluent pattern.
     */
    IdentifiedComponentBuilder<T> addUuids(UUID... uuids);
    
    /**
     * 
     * @return the list of all UUID that identify this component. 
     */
    UUID[] getUuids();
    
    /**
     * 
     * @param identifier a string identifier such as a SNOMED CT id, or a LOINC id. 
     * @param identifierAuthority a concept that identifies the authority that assigns the identifier. 
     * @return the builder for chaining of operations in a fluent pattern.
     */
    IdentifiedComponentBuilder<T> setIdentifierForAuthority(String identifier, 
            ConceptProxy identifierAuthority);
    
    /**
     * Create a component with a state of ACTIVE. 
     * @param editCoordinate the edit coordinate that determines the author, module and path for the change
     * @param changeCheckerMode determines if added to the commit manager with or without checks. 
     * @return the constructed component after it has been added to the commit manager
     * @throws IllegalStateException 
     */
    T build(EditCoordinate editCoordinate, 
            ChangeCheckerMode changeCheckerMode) throws IllegalStateException;
    
    /**
     * Create a component with a state of ACTIVE. 
     * @param editCoordinate the edit coordinate that determines the author, module and path for the change
     * @param changeCheckerMode determines if added to the commit manager with or without checks. 
     * @param subordinateBuiltObjects a list of subordinate objects also build as a result of building this object.
     * @return the constructed component after it has been added to the commit manager
     * @throws IllegalStateException 
     */
    T build(EditCoordinate editCoordinate, 
            ChangeCheckerMode changeCheckerMode,
            List<?> subordinateBuiltObjects) throws IllegalStateException;

    /**
     * The caller is responsible to write the component to the proper store when 
     * all updates to the component are complete. 
     * @param stampSequence
     * @param builtObjects a list objects build as a result of call build. 
     * Includes top-level object being built. 
     * The caller is also responsible to write all build objects to the proper store. 
     * @return the constructed component, not yet written to the database. 
     * @throws IllegalStateException 
     */
    T build(int stampSequence, List<?> builtObjects) throws IllegalStateException;
    
}
