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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY_STATE_SET KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.model.configuration;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;

/**
 *
 * @author kec
 */
public class StampCoordinates {
    public static StampCoordinate getDevelopmentLatest() {
        StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE, 
                TermAux.DEVELOPMENT_PATH.getConceptSequence());
        return new StampCoordinateImpl(StampPrecedence.PATH, stampPosition, 
                ConceptSequenceSet.EMPTY, State.ANY_STATE_SET);
    }
    public static StampCoordinate getDevelopmentLatestActiveOnly() {
        StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE, 
                TermAux.DEVELOPMENT_PATH.getConceptSequence());
        return new StampCoordinateImpl(StampPrecedence.PATH, stampPosition, 
                ConceptSequenceSet.EMPTY, State.ACTIVE_ONLY_SET);
    }
    public static StampCoordinate getMasterLatest() {
        StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE, 
                TermAux.MASTER_PATH.getConceptSequence());
        return new StampCoordinateImpl(StampPrecedence.PATH, stampPosition, 
                ConceptSequenceSet.EMPTY, State.ANY_STATE_SET);
    }
    public static StampCoordinate getMasterLatestActiveOnly() {
        StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE, 
                TermAux.MASTER_PATH.getConceptSequence());
        return new StampCoordinateImpl(StampPrecedence.PATH, stampPosition, 
                ConceptSequenceSet.EMPTY, State.ACTIVE_ONLY_SET);
    }
}
