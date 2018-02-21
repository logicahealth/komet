/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.statement;

import java.util.Optional;

/**
    Day of week, MWF or TH. 
    
    Handle as multiple weekly repetitions. M every week; W every week; etc. 
    
    Put "with meals" as part of the topic, not the repetition. 

 * resource: https://www.hl7.org/fhir/datatypes.html#Timing
 * @author kec
 */
public interface Repetition {
    
    /**
     * 
     * @return a specific time period or day
     */
    Measure getPeriodStart();
    /**
     * 
     * 
     * @return the duration of one cycle in a repeating event
     */
    Measure getPeriodDuration();
    
    /**
     * 
     * @return number of times the event may occur in a period. 
     */
    Measure getEventFrequency();

    /**
     * 
     * @return The length of the event (e.g. exercise for 30 minutes)
     */
    Optional<Measure> getEventDuration();
    

}
