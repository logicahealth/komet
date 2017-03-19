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
 *
 * @author kec
 */
@Contract
public interface ConceptBuilderService {
   /**
    * @param conceptName - Optional - if specified, a FSN will be created using this value (but see additional information on semanticTag)
    * @param semanticTag - Optional - if specified, conceptName must be specified, and two descriptions will be created using the following forms:
    * - FSN: "conceptName (semanticTag)"
    * - Preferred: "conceptName"
    * If not specified:
    *    - If the specified FSN contains a semantic tag, the FSN will be created using that value. A preferred term will be created by stripping the
    * supplied semantic tag.
    *   - If the specified FSN does not contain a semantic tag, no preferred term will be created.
    * @param logicalExpression - Optional
    * @param languageForDescriptions - Optional - used as the language for the created FSN and preferred term
    * @param dialectForDescriptions - Optional - used as the language for the created FSN and preferred term
    * @param logicCoordinate - Optional - used during the creation of the logical expression, if any are passed for creation.
    */
   ConceptBuilder getConceptBuilder(String conceptName,
                                    String semanticTag,
                                    LogicalExpression logicalExpression,
                                    ConceptSpecification languageForDescriptions,
                                    ConceptSpecification dialectForDescriptions,
                                    LogicCoordinate logicCoordinate);

   /**
    * @param conceptName - Optional - if specified, a FSN will be created using this value (but see additional information on semanticTag)
    * @param semanticTag - Optional - if specified, conceptName must be specified, and two descriptions will be created using the following forms:
    * - FSN: "conceptName (semanticTag)"
    * - Preferred: "conceptName"
    * If not specified:
    *    - If the specified FSN contains a semantic tag, the FSN will be created using that value. A preferred term will be created by stripping the
    * supplied semantic tag.
    *   - If the specified FSN does not contain a semantic tag, no preferred term will be created.
    * @param logicalExpression - Optional
    */
   ConceptBuilder getDefaultConceptBuilder(String conceptName, String semanticTag, LogicalExpression logicalExpression);

   //~--- set methods ---------------------------------------------------------

   ConceptBuilderService setDefaultDialectAssemblageForDescriptions(ConceptSpecification dialectForDescriptions);

   //~--- get methods ---------------------------------------------------------

   ConceptSpecification getDefaultDialectForDescriptions();

   ConceptSpecification getDefaultLanguageForDescriptions();

   //~--- set methods ---------------------------------------------------------

   ConceptBuilderService setDefaultLanguageForDescriptions(ConceptSpecification languageForDescriptions);

   //~--- get methods ---------------------------------------------------------

   LogicCoordinate getDefaultLogicCoordinate();

   //~--- set methods ---------------------------------------------------------

   ConceptBuilderService setDefaultLogicCoordinate(LogicCoordinate logicCoordinate);
}

