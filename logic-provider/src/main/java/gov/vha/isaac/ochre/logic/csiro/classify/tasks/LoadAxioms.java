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
package gov.vha.isaac.ochre.logic.csiro.classify.tasks;

import gov.vha.isaac.ochre.logic.csiro.classify.ClassifierData;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.memory.MemoryConfigurations;
import gov.vha.isaac.ochre.api.memory.MemoryManagementService;
import gov.vha.isaac.ochre.api.task.TimedTaskWithProgressTracker;

/**
 *
 * @author kec
 */
public class LoadAxioms extends TimedTaskWithProgressTracker<Void> {

    StampCoordinate stampCoordinate;
    LogicCoordinate logicCoordinate;

    public LoadAxioms(StampCoordinate stampCoordinate,
                         LogicCoordinate logicCoordinate) {
        this.stampCoordinate = stampCoordinate;
        this.logicCoordinate = logicCoordinate;
        updateTitle("Load axioms");
        
    }

    @Override
    protected Void call() throws Exception {
        LookupService.getService(MemoryManagementService.class).setMemoryConfiguration(MemoryConfigurations.CLASSIFY);
        ClassifierData cd = ClassifierData.get(stampCoordinate, logicCoordinate);
        cd.loadAxioms();
        return null;
    }
    
}
