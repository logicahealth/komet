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
import sh.isaac.api.component.concept.ConceptChronology;

/**
 *
 * @author kec
 */
public interface Measure {

    /**
     *
     * @return the resolution of this measurement.
     */
    Optional<Float> getResolution();

    /**
     *
     * @return the lower bound for this measurement
     */
    float getLowerBound();

    /**
     *
     * @return the upper bound for this measurement
     */
    float getUpperBound();

    /**
     *
     * @return true if the lower bound is part of the interval.
     */
    boolean includeLowerBound();

    /**
     *
     * @return true if the upper bound is part of the interval.
     */
    boolean includeUpperBound();

    /**
     * In most cases, the semantics of the measurement are the units of measure.
     * @return the semantics for this measurement.
     */
    ConceptChronology getMeasureSemantic();
}
