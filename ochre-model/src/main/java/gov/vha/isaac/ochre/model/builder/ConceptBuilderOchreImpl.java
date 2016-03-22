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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY_STATE_SET KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.model.builder;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilder;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.model.concept.ConceptChronologyImpl;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kec
 */
public class ConceptBuilderOchreImpl extends ComponentBuilder<ConceptChronology<?>> implements ConceptBuilder {

    private final String conceptName;
    private final String semanticTag;
    private final ConceptSpecification defaultLanguageForDescriptions;
    private final ConceptSpecification defaultDialectAssemblageForDescriptions;
    private final LogicCoordinate defaultLogicCoordinate;
    private final List<DescriptionBuilder> descriptionBuilders = new ArrayList<>();
    private final List<LogicalExpressionBuilder> logicalExpressionBuilders = new ArrayList<>();
    private final List<LogicalExpression> logicalExpressions = new ArrayList<>();

    public ConceptBuilderOchreImpl(String conceptName,
            String semanticTag,
            LogicalExpression logicalExpression,
            ConceptSpecification defaultLanguageForDescriptions,
            ConceptSpecification defaultDialectAssemblageForDescriptions,
            LogicCoordinate defaultLogicCoordinate) {
        this.conceptName = conceptName;
        this.semanticTag = semanticTag;
        this.defaultLanguageForDescriptions = defaultLanguageForDescriptions;
        this.defaultDialectAssemblageForDescriptions = defaultDialectAssemblageForDescriptions;
        this.defaultLogicCoordinate = defaultLogicCoordinate;
        if (logicalExpression != null) {
            this.logicalExpressions.add(logicalExpression);
        }
    }

    public ConceptBuilderOchreImpl(String conceptName,
            String semanticTag,
            ConceptSpecification defaultLanguageForDescriptions,
            ConceptSpecification defaultDialectAssemblageForDescriptions,
            LogicCoordinate defaultLogicCoordinate) {
        this(conceptName,
                semanticTag,
                (LogicalExpressionBuilder) null,
                defaultLanguageForDescriptions,
                defaultDialectAssemblageForDescriptions,
                defaultLogicCoordinate);

    }

    public ConceptBuilderOchreImpl(String conceptName,
            String semanticTag,
            LogicalExpressionBuilder logicalExpressionBuilder,
            ConceptSpecification defaultLanguageForDescriptions,
            ConceptSpecification defaultDialectAssemblageForDescriptions,
            LogicCoordinate defaultLogicCoordinate) {
        this.conceptName = conceptName;
        this.semanticTag = semanticTag;
        this.defaultLanguageForDescriptions = defaultLanguageForDescriptions;
        this.defaultDialectAssemblageForDescriptions = defaultDialectAssemblageForDescriptions;
        this.defaultLogicCoordinate = defaultLogicCoordinate;
        if (logicalExpressionBuilder != null) {
            this.logicalExpressionBuilders.add(logicalExpressionBuilder);
        }

    }

    @Override
    public DescriptionBuilder getFullySpecifiedDescriptionBuilder() {
        StringBuilder descriptionTextBuilder = new StringBuilder();
        descriptionTextBuilder.append(conceptName);
        if (semanticTag != null && semanticTag.length() > 0) {
            descriptionTextBuilder.append(" (");
            descriptionTextBuilder.append(semanticTag);
            descriptionTextBuilder.append(")");
        }
        return LookupService.getService(DescriptionBuilderService.class).
                getDescriptionBuilder(descriptionTextBuilder.toString(), this,
                        TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE,
                        defaultLanguageForDescriptions).
                setPreferredInDialectAssemblage(defaultDialectAssemblageForDescriptions);
    }

    @Override
    public DescriptionBuilder getSynonymPreferredDescriptionBuilder() {
        return LookupService.getService(DescriptionBuilderService.class).
                getDescriptionBuilder(conceptName, this,
                        TermAux.SYNONYM_DESCRIPTION_TYPE,
                        defaultLanguageForDescriptions).
                setPreferredInDialectAssemblage(defaultDialectAssemblageForDescriptions);
    }

    @Override
    public ConceptBuilder addDescription(DescriptionBuilder descriptionBuilder) {
        descriptionBuilders.add(descriptionBuilder);
        return this;
    }

    @Override
    public ConceptBuilder addLogicalExpressionBuilder(LogicalExpressionBuilder logicalExpressionBuilder) {
        this.logicalExpressionBuilders.add(logicalExpressionBuilder);
        return this;
    }

    @Override
    public ConceptBuilder addLogicalExpression(LogicalExpression logicalExpression) {
        this.logicalExpressions.add(logicalExpression);
        return this;
    }

    @Override
    public ConceptChronology build(EditCoordinate editCoordinate, ChangeCheckerMode changeCheckerMode,
            List builtObjects) throws IllegalStateException {

        ConceptChronologyImpl conceptChronology
                = (ConceptChronologyImpl) Get.conceptService().getConcept(getUuids());
        conceptChronology.createMutableVersion(State.ACTIVE, editCoordinate);
        builtObjects.add(conceptChronology);
        descriptionBuilders.add(getFullySpecifiedDescriptionBuilder());
        descriptionBuilders.add(getSynonymPreferredDescriptionBuilder());
        descriptionBuilders.forEach((builder) -> {
            builder.build(editCoordinate, changeCheckerMode, builtObjects);
        });
        SememeBuilderService builderService = LookupService.getService(SememeBuilderService.class);
        for (LogicalExpression logicalExpression : logicalExpressions) {
            sememeBuilders.add(builderService.
                    getLogicalExpressionSememeBuilder(logicalExpression, this, defaultLogicCoordinate.getStatedAssemblageSequence()));
        }
        for (LogicalExpressionBuilder builder : logicalExpressionBuilders) {
            sememeBuilders.add(builderService.
                    getLogicalExpressionSememeBuilder(builder.build(), this, defaultLogicCoordinate.getStatedAssemblageSequence()));
        }
        sememeBuilders.forEach((builder) -> builder.build(editCoordinate, changeCheckerMode, builtObjects));
        if (changeCheckerMode == ChangeCheckerMode.ACTIVE) {
            Get.commitService().addUncommitted(conceptChronology);
        } else {
            Get.commitService().addUncommittedNoChecks(conceptChronology);
        }
        return conceptChronology;
    }

    @Override
    public ConceptChronology build(int stampCoordinate,
            List builtObjects) throws IllegalStateException {

        ConceptChronologyImpl conceptChronology
                = (ConceptChronologyImpl) Get.conceptService().getConcept(getUuids());
        conceptChronology.createMutableVersion(stampCoordinate);
        builtObjects.add(conceptChronology);
        descriptionBuilders.add(getFullySpecifiedDescriptionBuilder());
        descriptionBuilders.add(getSynonymPreferredDescriptionBuilder());
        descriptionBuilders.forEach((builder) -> {
            builder.build(stampCoordinate, builtObjects);
        });
        SememeBuilderService builderService = LookupService.getService(SememeBuilderService.class);
        for (LogicalExpression logicalExpression : logicalExpressions) {
            sememeBuilders.add(builderService.
                    getLogicalExpressionSememeBuilder(logicalExpression, this, defaultLogicCoordinate.getStatedAssemblageSequence()));
        }
        for (LogicalExpressionBuilder builder : logicalExpressionBuilders) {
            sememeBuilders.add(builderService.
                    getLogicalExpressionSememeBuilder(builder.build(), this, defaultLogicCoordinate.getStatedAssemblageSequence()));
        }
        sememeBuilders.forEach((builder) -> builder.build(stampCoordinate, builtObjects));
        return conceptChronology;
    }

    @Override
    public String getConceptDescriptionText() {
        return conceptName;
    }
}
