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
package gov.vha.isaac.ochre.model.coordinate;

import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;

/**
 *
 * @author kec
 */
public class LogicCoordinateImpl implements LogicCoordinate {
    
    int statedAssemblageSequence;
    int inferredAssemblageSequence;
    int descriptionLogicProfileSequence;
    int classifierSequence;

    public LogicCoordinateImpl(int statedAssemblageSequence, int inferredAssemblageSequnce, int descriptionLogicProfileSequence, int classifierSequence) {
        this.statedAssemblageSequence = statedAssemblageSequence;
        this.inferredAssemblageSequence = inferredAssemblageSequnce;
        this.descriptionLogicProfileSequence = descriptionLogicProfileSequence;
        this.classifierSequence = classifierSequence;
    }

    
    @Override
    public int getStatedAssemblageSequence() {
        return statedAssemblageSequence;
    }

    @Override
    public int getInferredAssemblageSequence() {
        return inferredAssemblageSequence;
    }

    @Override
    public int getDescriptionLogicProfileSequence() {
        return descriptionLogicProfileSequence;
    }

    @Override
    public int getClassifierSequence() {
        return classifierSequence;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.statedAssemblageSequence;
        hash = 29 * hash + this.inferredAssemblageSequence;
        hash = 29 * hash + this.descriptionLogicProfileSequence;
        hash = 29 * hash + this.classifierSequence;
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
        final LogicCoordinateImpl other = (LogicCoordinateImpl) obj;
        if (this.statedAssemblageSequence != other.statedAssemblageSequence) {
            return false;
        }
        if (this.inferredAssemblageSequence != other.inferredAssemblageSequence) {
            return false;
        }
        if (this.descriptionLogicProfileSequence != other.descriptionLogicProfileSequence) {
            return false;
        }
        return this.classifierSequence == other.classifierSequence;
    }
    
}
