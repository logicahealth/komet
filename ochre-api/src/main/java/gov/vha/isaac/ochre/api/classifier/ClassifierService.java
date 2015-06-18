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
package gov.vha.isaac.ochre.api.classifier;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.util.WorkExecutors;
import javafx.concurrent.Task;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface ClassifierService {

    /**
     * @param logicCoordinate
     * @return Return the (unstarted) task which will initialize the classifier
     */
    Task<Void> initialize(LogicCoordinate logicCoordinate);

    /**
     * Starts the {@link #initialize(LogicCoordinate)} in a background thread, return the task handle 
     * @param logicCoordinate
     * @return Return the started task which performs initialize
     */
    default Task<Void> startInitialize(LogicCoordinate logicCoordinate)
    {
        Task<Void> t = initialize(logicCoordinate);
        LookupService.getService(WorkExecutors.class).getExecutor().execute(t);
        return t;
    }
    
    /**
     * @param stampCoordinate
     * @param logicCoordinate
     * @param editCoordinate
     * @return Return the (unstarted) task which will classify
     */
    Task<ClassifierResults> fullClassification(
            StampCoordinate stampCoordinate, 
            LogicCoordinate logicCoordinate, 
            EditCoordinate editCoordinate);
    
    /**
     * Starts the {@link #fullClassification(StampCoordinate, LogicCoordinate, EditCoordinate)} in a background thread, return the task handle 
     * @param stampCoordinate
     * @param logicCoordinate
     * @param editCoordinate
     * @return Return the started task which performs classify
     */
    default Task<ClassifierResults> startFullClassification(
            StampCoordinate stampCoordinate, 
            LogicCoordinate logicCoordinate, 
            EditCoordinate editCoordinate) {
        Task<ClassifierResults> t = fullClassification(stampCoordinate, logicCoordinate, editCoordinate);
        LookupService.getService(WorkExecutors.class).getExecutor().execute(t);
        return t;
    }
    
    ClassifierResults incrementalClassification(StampCoordinate stampCoordinate, 
            LogicCoordinate logicCoordinate, 
            EditCoordinate editCoordinate, 
            ConceptSequenceSet newConcepts);
}
