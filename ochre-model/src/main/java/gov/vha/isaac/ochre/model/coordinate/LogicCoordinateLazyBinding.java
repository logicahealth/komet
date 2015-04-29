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
package gov.vha.isaac.ochre.model.coordinate;

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;

/**
 *
 * @author kec
 */
public class LogicCoordinateLazyBinding implements LogicCoordinate {
    
    private final ConceptProxy statedAssemblageProxy;
    private final ConceptProxy inferredAssemblageProxy;
    private final ConceptProxy descriptionLogicProfileProxy;
    private final ConceptProxy classifierProxy;

    int statedAssemblageSequence = Integer.MAX_VALUE;
    int inferredAssemblageSequence = Integer.MAX_VALUE;
    int descriptionLogicProfileSequence = Integer.MAX_VALUE;
    int classifierSequence = Integer.MAX_VALUE;

    public LogicCoordinateLazyBinding(ConceptProxy statedAssemblageProxy, 
            ConceptProxy inferredAssemblageProxy, 
            ConceptProxy descriptionLogicProfileProxy, 
            ConceptProxy classifierProxy) {
        this.statedAssemblageProxy = statedAssemblageProxy;
        this.inferredAssemblageProxy = inferredAssemblageProxy;
        this.descriptionLogicProfileProxy = descriptionLogicProfileProxy;
        this.classifierProxy = classifierProxy;
    }
    
    

    @Override
    public int getStatedAssemblageSequence() {
        if (statedAssemblageSequence == Integer.MAX_VALUE) {
            statedAssemblageSequence = statedAssemblageProxy.getSequence();
        }
        return statedAssemblageSequence;
    }

    @Override
    public int getInferredAssemblageSequence() {
        if (inferredAssemblageSequence == Integer.MAX_VALUE) {
            inferredAssemblageSequence = inferredAssemblageProxy.getSequence();
        }
        return inferredAssemblageSequence;
    }

    @Override
    public int getDescriptionLogicProfileSequence() {
        if (descriptionLogicProfileSequence == Integer.MAX_VALUE) {
            descriptionLogicProfileSequence = descriptionLogicProfileProxy.getSequence();
        }
        return descriptionLogicProfileSequence;
    }

    @Override
    public int getClassifierSequence() {
        if (classifierSequence == Integer.MAX_VALUE) {
            classifierSequence = classifierProxy.getSequence();
        }
        return classifierSequence;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.getStatedAssemblageSequence();
        hash = 83 * hash + this.getInferredAssemblageSequence();
        hash = 83 * hash + this.getDescriptionLogicProfileSequence();
        hash = 83 * hash + this.getClassifierSequence();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LogicCoordinate other = (LogicCoordinate) obj;
        if (this.getStatedAssemblageSequence() != other.getStatedAssemblageSequence()) {
            return false;
        }
        if (this.getInferredAssemblageSequence() != other.getInferredAssemblageSequence()) {
            return false;
        }
        if (this.getDescriptionLogicProfileSequence() != other.getDescriptionLogicProfileSequence()) {
            return false;
        }
        return this.getClassifierSequence() == other.getClassifierSequence();
    }
    
}
