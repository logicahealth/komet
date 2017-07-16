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



package sh.isaac.model.builder;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.logic.LogicalExpression;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ConceptBuilderProvider.
 *
 * @author kec
 */
@Service
public class ConceptBuilderProvider
         implements ConceptBuilderService {
   /** The default language for descriptions. */
   private ConceptSpecification defaultLanguageForDescriptions =
      TermAux.getConceptSpecificationForLanguageSequence(Get.configurationService()
                                                            .getDefaultLanguageCoordinate()
                                                            .getLanguageConceptSequence());

   /** The default dialect assemblage for descriptions. */
   private ConceptSpecification defaultDialectAssemblageForDescriptions = TermAux.US_DIALECT_ASSEMBLAGE;

   /** The default logic coordinate. */
   private LogicCoordinate defaultLogicCoordinate = Get.configurationService()
                                                       .getDefaultLogicCoordinate();

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept builder.
    *
    * @param conceptName the concept name
    * @param semanticTag the semantic tag
    * @param logicalExpression the logical expression
    * @param languageForDescriptions the language for descriptions
    * @param dialectAssemblageForDescriptions the dialect assemblage for descriptions
    * @param logicCoordinate the logic coordinate
    * @return the concept builder
    */
   @Override
   public ConceptBuilder getConceptBuilder(String conceptName,
         String semanticTag,
         LogicalExpression logicalExpression,
         ConceptSpecification languageForDescriptions,
         ConceptSpecification dialectAssemblageForDescriptions,
         LogicCoordinate logicCoordinate) {
      return new ConceptBuilderImpl(conceptName,
                                         semanticTag,
                                         logicalExpression,
                                         languageForDescriptions,
                                         dialectAssemblageForDescriptions,
                                         logicCoordinate);
   }

   /**
    * Gets the default concept builder.
    *
    * @param conceptName the concept name
    * @param semanticTag the semantic tag
    * @param logicalExpression the logical expression
    * @return the default concept builder
    */
   @Override
   public ConceptBuilder getDefaultConceptBuilder(String conceptName,
         String semanticTag,
         LogicalExpression logicalExpression) {
      return new ConceptBuilderImpl(conceptName,
                                         semanticTag,
                                         logicalExpression,
                                         this.defaultLanguageForDescriptions,
                                         this.defaultDialectAssemblageForDescriptions,
                                         this.defaultLogicCoordinate);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set default dialect assemblage for descriptions.
    *
    * @param defaultDialectAssemblageForDescriptions the default dialect assemblage for descriptions
    * @return the concept builder provider
    */
   @Override
   public ConceptBuilderProvider setDefaultDialectAssemblageForDescriptions(
           ConceptSpecification defaultDialectAssemblageForDescriptions) {
      this.defaultDialectAssemblageForDescriptions = defaultDialectAssemblageForDescriptions;
      return this;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default dialect for descriptions.
    *
    * @return the default dialect for descriptions
    */
   @Override
   public ConceptSpecification getDefaultDialectForDescriptions() {
      return this.defaultDialectAssemblageForDescriptions;
   }

   /**
    * Gets the default language for descriptions.
    *
    * @return the default language for descriptions
    */
   @Override
   public ConceptSpecification getDefaultLanguageForDescriptions() {
      return this.defaultLanguageForDescriptions;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set default language for descriptions.
    *
    * @param defaultLanguageForDescriptions the default language for descriptions
    * @return the concept builder provider
    */
   @Override
   public ConceptBuilderProvider setDefaultLanguageForDescriptions(
           ConceptSpecification defaultLanguageForDescriptions) {
      this.defaultLanguageForDescriptions = defaultLanguageForDescriptions;
      return this;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default logic coordinate.
    *
    * @return the default logic coordinate
    */
   @Override
   public LogicCoordinate getDefaultLogicCoordinate() {
      return this.defaultLogicCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set default logic coordinate.
    *
    * @param defaultLogicCoordinate the default logic coordinate
    * @return the concept builder provider
    */
   @Override
   public ConceptBuilderProvider setDefaultLogicCoordinate(LogicCoordinate defaultLogicCoordinate) {
      this.defaultLogicCoordinate = defaultLogicCoordinate;
      return this;
   }
}

