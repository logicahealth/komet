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
package gov.vha.isaac.ochre.api.coordinate;

import java.util.UUID;

/**
 *
 * @author kec
 * @param <T>
 * TODO can remove generic type on TaxonomyCoordinate once ViewCoordinate is eliminated. 
 */
public interface TaxonomyCoordinate<T extends TaxonomyCoordinate<T>> extends TimeBasedAnalogMaker<T>, StateBasedAnalogMaker<T> {

    /**
     *
     * @return a UUID that uniquely identifies this taxonomy coordinate.
     */
    UUID getUuid();

    /**
     *
     * @return PremiseType.STATED if taxonomy operations should be based on stated definitions, or
     * PremiseType.INFERRED if taxonomy operations should be based on inferred definitions.
     */
    PremiseType getTaxonomyType();

    /**
     *
     * @return a StampCoordinate that specifies the retrieval and display of
     * object chronicle versions by indicating the current position on a path, and allowed modules.
     */
    StampCoordinate<?> getStampCoordinate();

    /**
     *
     * @return a LanguageCoordinate that specifies how to manage the retrieval and display of language.
     * and dialect information.
     */
    LanguageCoordinate getLanguageCoordinate();

    /**
     *
     * @return a LogicCoordinate that specifies how to manage the retrieval and display of logic information.
     */
    LogicCoordinate getLogicCoordinate();
    
    /**
     * 
     * @param taxonomyType the {@code PremiseType} for the analog 
     * @return a new taxonomyCoordinate with the specified taxonomy type. 
     */
    
    TaxonomyCoordinate<? extends T> makeAnalog(PremiseType taxonomyType);
}
