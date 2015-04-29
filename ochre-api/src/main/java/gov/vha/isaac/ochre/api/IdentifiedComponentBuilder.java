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
     * @param uuid
     * @return the builder for chaining of operations in a fluent pattern. 
     */
    IdentifiedComponentBuilder<T> setPrimordialUuid(UUID uuid);
    
    IdentifiedComponentBuilder<T> addUuids(UUID... uuids);
    
    UUID[] getUuids();
    
    IdentifiedComponentBuilder<T> setIdentifierForAuthority(String identifier, 
            ConceptProxy identifierAuthority);
    
    T build(EditCoordinate editCoordinate, 
            ChangeCheckerMode changeCheckerMode) throws IllegalStateException;
    
    T build(EditCoordinate editCoordinate, 
            ChangeCheckerMode changeCheckerMode,
            List builtObjects) throws IllegalStateException;
    
}
