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
import sh.isaac.api.logic.LogicalExpression;

/**
 * The Result measures the actual state of the 
 * clinical statement topic during the timing specified
 * by the performance circumstance. 
 * @author kec
 */
public interface ObservationResult extends Result {
    /**
     * 
     * @return an indicator as to the possibility of an immediate 
     * health risk to the individual that may require immediate action. 
     */
    Optional<LogicalExpression> getHealthRisk();

    /**
     * 
     * @return the normal range for this measure. 
     */
    Optional<Interval> getNormalRange();
    
}
