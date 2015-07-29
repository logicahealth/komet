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
package gov.vha.isaac.ochre.api.coordinate;

import gov.vha.isaac.ochre.api.LanguageCoordinateService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import java.time.temporal.TemporalAccessor;
import java.util.EnumSet;
import java.util.List;
import org.jvnet.hk2.annotations.Contract;

/**
 * A factory to obtain {@code StampCoordinate} objects. 
 * @author kec
 */
@Contract
public interface CoordinateFactory extends LanguageCoordinateService {
    /**
     * 
     * @param stampPath Specifies the concept that identifies the path. 
     * @param precedence Specifies how precedence of two compared {@code StampCoordinate} objects are handled. 
     * @param moduleSpecificationList List of allowed modules to include in version computations. 
     *          An empty list is a wildcard (will include all modules)
     * @param allowedStateSet allowed states to be included in version computations based on the returned {@code StampCoordinate}
     * @param year the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth the day-of-month to represent, from 1 to 31
     * @param hour the hour-of-day to represent, from 0 to 23
     * @param minute the minute-of-hour to represent, from 0 to 59
     * @param second the second-of-minute to represent, from 0 to 59
     * @return a new instance that implements {@code StampCoordinate} with the provided temporal information
     */
    StampCoordinate createStampCoordinate(ConceptSpecification stampPath, 
                               StampPrecedence precedence,
                               List<ConceptSpecification> moduleSpecificationList, 
                               EnumSet<State> allowedStateSet,
                               int year,
                               int month,
                               int dayOfMonth,
                               int hour,
                               int minute,
                               int second);
    /**
     * 
     * @param stampPath Specifies the concept that identifies the path. 
     * @param precedence Specifies how precedence of two compared {@code StampCoordinate} objects are handled. 
    * @param moduleSpecificationList List of allowed modules to include in version computations. 
     *          An empty list is a wildcard (will include all modules)
     * @param allowedStateSet allowed states to be included in version computations based on the returned {@code StampCoordinate}
    * @param temporal the temporal object to specify the time on a path for the returned  {@code StampCoordinate} 
     * @return a new instance that implements {@code StampCoordinate} with the provided temporal information
     */
    StampCoordinate createStampCoordinate(ConceptSpecification stampPath, 
                               StampPrecedence precedence,
                               List<ConceptSpecification> moduleSpecificationList, 
                               EnumSet<State> allowedStateSet,
                               TemporalAccessor temporal);
   /**
     * 
     * @param stampPath Specifies the concept that identifies the path. 
    * @param precedence Specifies how precedence of two compared {@code StampCoordinate} objects are handled. 
    * @param moduleSpecificationList List of allowed modules to include in version computations. 
     *          An empty list is a wildcard (will include all modules)
     * @param allowedStateSet allowed states to be included in version computations based on the returned {@code StampCoordinate}
     * @param dateTimeText the text to parse such as "2007-12-03T10:15:30", which is specified by the ISO-8601 extended offset date-time format. 
     * @return a new instance that implements {@code StampCoordinate} with the provided temporal information
     */
    StampCoordinate createStampCoordinate(ConceptSpecification stampPath, 
                               StampPrecedence precedence,
                               List<ConceptSpecification> moduleSpecificationList, 
                               EnumSet<State> allowedStateSet,
                               CharSequence dateTimeText);
    
    /**
     * 
     * @return a {@code StampCoordinate} representing the latest version on the development path
     * of components in all modules.
     */
    StampCoordinate<StampCoordinate> createDevelopmentLatestStampCoordinate();
    /**
     * 
     * @return a {@code StampCoordinate} representing the latest active version on the development path
     * of components in all modules. Inactive versions are not included in computed results. 
     */
    StampCoordinate<StampCoordinate> createDevelopmentLatestActiveOnlyStampCoordinate();
    /**
     * 
     * @return a {@code StampCoordinate} representing the latest version on the master path
     * of components in all modules.
     */
    StampCoordinate<StampCoordinate> createMasterLatestStampCoordinate();
    /**
     * 
     * @return a {@code StampCoordinate} representing the latest active version on the master path
     * of components in all modules. Inactive versions are not included in computed results. 
     */
    StampCoordinate<StampCoordinate> createMasterLatestActiveOnlyStampCoordinate();
    
    /**
     * 
     * @return the standard EL profile logic coordinate. 
     */
    LogicCoordinate createStandardElProfileLogicCoordinate();
    
   TaxonomyCoordinate<TaxonomyCoordinate> createInferredTaxonomyCoordinate(
            StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate,
            LogicCoordinate logicCoordinate);
   
    TaxonomyCoordinate<TaxonomyCoordinate> createStatedTaxonomyCoordinate(
            StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate,
            LogicCoordinate logicCoordinate);
 /**
  * active only
  * latest on dev path
  * @return 
  */
   TaxonomyCoordinate<TaxonomyCoordinate> createDefaultInferredTaxonomyCoordinate();
   
    TaxonomyCoordinate<TaxonomyCoordinate> createDefaultStatedTaxonomyCoordinate();


    EditCoordinate createDefaultUserSolorOverlayEditCoordinate();

    EditCoordinate createClassifierSolorOverlayEditCoordinate();

    EditCoordinate createDefaultUserVeteransAdministrationExtensionEditCoordinate();

    EditCoordinate createDefaultUserMetadataEditCoordinate();
}
