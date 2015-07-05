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

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;

/**
 *
 * @author kec
 */
public class StampCoordinateImpl implements StampCoordinate {

    StampPrecedence stampPrecedence;
    StampPosition stampPosition;
    int[] moduleSequences;
    EnumSet<State> allowedStates;

    public StampCoordinateImpl(StampPrecedence stampPrecedence, 
            StampPosition stampPosition, 
            int[] moduleSequences, EnumSet<State> allowedStates) {
        this.stampPrecedence = stampPrecedence;
        this.stampPosition = stampPosition;
        this.moduleSequences = moduleSequences;
        this.allowedStates = allowedStates;
    }

    @Override
    public StampCoordinate makeAnalog(long stampPositionTime) {
        StampPosition anotherStampPosition = new StampPositionImpl(stampPositionTime, stampPosition.getStampPathSequence());
       return new StampCoordinateImpl(stampPrecedence, anotherStampPosition, moduleSequences, allowedStates);
    }
    

    @Override
    public EnumSet<State> getAllowedStates() {
        return allowedStates;
    }
    
    @Override
    public StampPrecedence getStampPrecedence() {
        return stampPrecedence;
    }

    @Override
    public StampPosition getStampPosition() {
        return stampPosition;
    }

    @Override
    public int[] getModuleSequences() {
        return moduleSequences;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.stampPrecedence);
        hash = 11 * hash + Objects.hashCode(this.stampPosition);
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
        final StampCoordinateImpl other = (StampCoordinateImpl) obj;
        if (this.stampPrecedence != other.stampPrecedence) {
            return false;
        }
        if (!Objects.equals(this.stampPosition, other.stampPosition)) {
            return false;
        }
        if (!this.allowedStates.equals(other.allowedStates)) {
            return false;
        }
        
        return Arrays.equals(this.moduleSequences, other.moduleSequences);
    }

    @Override
    public String toString() {
        return "StampCoordinateImpl{" + "stampPrecedence=" + stampPrecedence + 
                ", stampPosition=" + stampPosition + 
                ", moduleSequences=" + Arrays.toString(moduleSequences) + 
                ", allowedStates=" + allowedStates + '}';
    }
    
}
