/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.component.concept;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.logic.LogicalExpression;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ConceptBuilderService.
 *
 * @author kec
 */
@Contract
public interface ConceptBuilderService {
   /**
    * Gets the concept builder.
    *
    * @param conceptName - Optional - if specified, a FQN will be created using this value (but see additional information on semanticTag)
    * @param semanticTag - Optional - if specified, conceptName must be specified, and two descriptions will be created using the following forms:
    * - FQN: "conceptName (semanticTag)"
    * - Preferred: "conceptName"
    * If not specified:
    *    - If the specified FQN contains a semantic tag, the FQN will be created using that value. A preferred term will be created by stripping the
    * supplied semantic tag.
    *   - If the specified FQN does not contain a semantic tag, no preferred term will be created.
    * @param logicalExpression - Optional
    * @param languageForDescriptions - Optional - used as the language for the created FQN and preferred term
    * @param dialectForDescriptions - Optional - used as the language for the created FQN and preferred term
    * @param logicCoordinate - Optional - used during the creation of the logical expression, if any are passed for creation.
    * @param assemblageId the assemblage to create the concept in
    * @return the concept builder
    */
   ConceptBuilder getConceptBuilder(String conceptName,
                                    String semanticTag,
                                    LogicalExpression logicalExpression,
                                    ConceptSpecification languageForDescriptions,
                                    ConceptSpecification dialectForDescriptions,
                                    LogicCoordinate logicCoordinate,
                                    int assemblageId);

   /**
    * Gets the default concept builder.
    *
    * @param conceptName - Optional - if specified, a FQN will be created using this value (but see additional information on semanticTag)
    * @param semanticTag - Optional - if specified, conceptName must be specified, and two descriptions will be created using the following forms:
    * - FQN: "conceptName (semanticTag)"
    * - Preferred: "conceptName"
    * If not specified:
    *    - If the specified FQN contains a semantic tag, the FQN will be created using that value. A preferred term will be created by stripping the
    * supplied semantic tag.
    *   - If the specified FQN does not contain a semantic tag, no preferred term will be created.
    * @param logicalExpression - Optional
    * @param assemblageId the assemblaged to create the conept in
    * @return the default concept builder
    */
   ConceptBuilder getDefaultConceptBuilder(String conceptName, String semanticTag, LogicalExpression logicalExpression, int assemblageId);

   //~--- set methods ---------------------------------------------------------

   /**
    * Set default dialect assemblage for descriptions.
    *
    * @param dialectForDescriptions the dialect for descriptions
    * @return the concept builder service
    */
   ConceptBuilderService setDefaultDialectAssemblageForDescriptions(ConceptSpecification dialectForDescriptions);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default dialect for descriptions.
    *
    * @return the default dialect for descriptions
    */
   ConceptSpecification getDefaultDialectForDescriptions();

   /**
    * Gets the default language for descriptions.
    *
    * @return the default language for descriptions
    */
   ConceptSpecification getDefaultLanguageForDescriptions();

   //~--- set methods ---------------------------------------------------------

   /**
    * Set default language for descriptions.
    *
    * @param languageForDescriptions the language for descriptions
    * @return the concept builder service
    */
   ConceptBuilderService setDefaultLanguageForDescriptions(ConceptSpecification languageForDescriptions);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default logic coordinate.
    *
    * @return the default logic coordinate
    */
   LogicCoordinate getDefaultLogicCoordinate();

   //~--- set methods ---------------------------------------------------------

   /**
    * Set default logic coordinate.
    *
    * @param logicCoordinate the logic coordinate
    * @return the concept builder service
    */
   ConceptBuilderService setDefaultLogicCoordinate(LogicCoordinate logicCoordinate);
}

