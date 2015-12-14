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
package gov.vha.isaac.ochre.model.builder;


import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service
public class ConceptBuilderProvider implements ConceptBuilderService {
    
    private ConceptSpecification defaultLanguageForDescriptions = TermAux.getConceptSpecificationForLanguageSequence(
            Get.configurationService().getDefaultLanguageCoordinate().getLanguageConceptSequence());
    
    private ConceptSpecification defaultDialectAssemblageForDescriptions = TermAux.US_DIALECT_ASSEMBLAGE;
    
    private LogicCoordinate defaultLogicCoordinate = Get.configurationService().getDefaultLogicCoordinate();

    @Override
    public ConceptBuilder getDefaultConceptBuilder(String conceptName, String semanticTag, LogicalExpression logicalExpression) {
        return new ConceptBuilderOchreImpl(conceptName, semanticTag, logicalExpression, 
               defaultLanguageForDescriptions, 
               defaultDialectAssemblageForDescriptions, 
               defaultLogicCoordinate);
    }

    @Override
    public ConceptBuilderProvider setDefaultLanguageForDescriptions(ConceptSpecification defaultLanguageForDescriptions) {
        this.defaultLanguageForDescriptions = defaultLanguageForDescriptions;
        return this; 
   }

    @Override
    public ConceptSpecification getDefaultLanguageForDescriptions() {
        return defaultLanguageForDescriptions;
    }

    @Override
    public ConceptBuilderProvider setDefaultDialectAssemblageForDescriptions(ConceptSpecification defaultDialectAssemblageForDescriptions) {
        this.defaultDialectAssemblageForDescriptions = defaultDialectAssemblageForDescriptions;
        return this;
    }

    @Override
    public ConceptSpecification getDefaultDialectForDescriptions() {
        return defaultDialectAssemblageForDescriptions;
    }

    @Override
    public ConceptBuilderProvider setDefaultLogicCoordinate(LogicCoordinate defaultLogicCoordinate) {
        this.defaultLogicCoordinate = defaultLogicCoordinate;
        return this;
    }

    @Override
    public LogicCoordinate getDefaultLogicCoordinate() {
        return defaultLogicCoordinate;
    }

    @Override
    public ConceptBuilder getConceptBuilder(String conceptName, 
            String semanticTag, 
            LogicalExpression logicalExpression, 
            ConceptSpecification languageForDescriptions, 
            ConceptSpecification dialectAssemblageForDescriptions, 
            LogicCoordinate logicCoordinate) {

        
       return new ConceptBuilderOchreImpl(conceptName, semanticTag, logicalExpression, 
               languageForDescriptions, 
               dialectAssemblageForDescriptions, 
               logicCoordinate);
        
    }
    
}
