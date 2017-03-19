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

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.sememe.SememeBuilderService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.model.concept.ConceptChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ConceptBuilderOchreImpl
        extends ComponentBuilder<ConceptChronology<?>>
         implements ConceptBuilder {
   private final List<DescriptionBuilder<?, ?>> descriptionBuilders         = new ArrayList<>();
   private final List<LogicalExpressionBuilder> logicalExpressionBuilders   = new ArrayList<>();
   private final List<LogicalExpression>        logicalExpressions          = new ArrayList<>();
   private transient DescriptionBuilder<?, ?>   fsnDescriptionBuilder       = null;
   private transient DescriptionBuilder<?, ?>   preferredDescriptionBuilder = null;
   private final String                         conceptName;
   private final String                         semanticTag;
   private final ConceptSpecification           defaultLanguageForDescriptions;
   private final ConceptSpecification           defaultDialectAssemblageForDescriptions;
   private final LogicCoordinate                defaultLogicCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * @param conceptName - Optional - if specified, a FSN will be created using this value (but see additional information on semanticTag)
    * @param semanticTag - Optional - if specified, conceptName must be specified, and two descriptions will be created using the following forms:
    *   FSN: -     "conceptName (semanticTag)"
    *   Preferred: "conceptName"
    * If not specified:
    *   If the specified FSN contains a semantic tag, the FSN will be created using that value.  A preferred term will be created by stripping the
    *   supplied semantic tag.
    *   If the specified FSN does not contain a semantic tag, no preferred term will be created.
    * @param logicalExpression - Optional
    * @param defaultLanguageForDescriptions - Optional - used as the language for the created FSN and preferred term
    * @param defaultDialectAssemblageForDescriptions - Optional - used as the language for the created FSN and preferred term
    * @param defaultLogicCoordinate - Optional - used during the creation of the logical expression, if either a logicalExpression
    * is passed, or if @link {@link #addLogicalExpression(LogicalExpression)} or {@link #addLogicalExpressionBuilder(LogicalExpressionBuilder)} are
    * used later.
    */
   public ConceptBuilderOchreImpl(String conceptName,
                                  String semanticTag,
                                  LogicalExpression logicalExpression,
                                  ConceptSpecification defaultLanguageForDescriptions,
                                  ConceptSpecification defaultDialectAssemblageForDescriptions,
                                  LogicCoordinate defaultLogicCoordinate) {
      this.conceptName                             = conceptName;
      this.semanticTag                             = semanticTag;
      this.defaultLanguageForDescriptions          = defaultLanguageForDescriptions;
      this.defaultDialectAssemblageForDescriptions = defaultDialectAssemblageForDescriptions;
      this.defaultLogicCoordinate                  = defaultLogicCoordinate;

      if (logicalExpression != null) {
         this.logicalExpressions.add(logicalExpression);
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public ConceptBuilder addDescription(DescriptionBuilder<?, ?> descriptionBuilder) {
      this.descriptionBuilders.add(descriptionBuilder);
      return this;
   }

   @Override
   public ConceptBuilder addDescription(String value, ConceptSpecification descriptionType) {
      if ((this.defaultLanguageForDescriptions == null) || (this.defaultDialectAssemblageForDescriptions == null)) {
         throw new IllegalStateException("language and dialect are required if a concept name is provided");
      }

      if (!this.conceptName.equals(value)) {
         this.descriptionBuilders.add(LookupService.getService(DescriptionBuilderService.class)
               .getDescriptionBuilder(value, this, descriptionType, this.defaultLanguageForDescriptions)
               .addAcceptableInDialectAssemblage(this.defaultDialectAssemblageForDescriptions));
      }

      return this;
   }

   @Override
   public ConceptBuilder addLogicalExpression(LogicalExpression logicalExpression) {
      this.logicalExpressions.add(logicalExpression);
      return this;
   }

   @Override
   public ConceptBuilder addLogicalExpressionBuilder(LogicalExpressionBuilder logicalExpressionBuilder) {
      this.logicalExpressionBuilders.add(logicalExpressionBuilder);
      return this;
   }

   @Override
   public ConceptChronology build(int stampCoordinate,
                                  List<ObjectChronology<? extends StampedVersion>> builtObjects)
            throws IllegalStateException {
      final ConceptChronologyImpl conceptChronology = (ConceptChronologyImpl) Get.conceptService()
                                                                           .getConcept(getUuids());

      conceptChronology.createMutableVersion(stampCoordinate);
      builtObjects.add(conceptChronology);

      if (getFullySpecifiedDescriptionBuilder() != null) {
         this.descriptionBuilders.add(getFullySpecifiedDescriptionBuilder());
      }

      if (getSynonymPreferredDescriptionBuilder() != null) {
         this.descriptionBuilders.add(getSynonymPreferredDescriptionBuilder());
      }

      this.descriptionBuilders.forEach((builder) -> {
                                     builder.build(stampCoordinate, builtObjects);
                                  });

      if ((this.defaultLogicCoordinate == null) &&
            ((this.logicalExpressions.size() > 0) || (this.logicalExpressionBuilders.size() > 0))) {
         throw new IllegalStateException("A logic coordinate is required when a logical expression is passed");
      }

      final SememeBuilderService builderService = LookupService.getService(SememeBuilderService.class);

      for (final LogicalExpression logicalExpression: this.logicalExpressions) {
         this.sememeBuilders.add(builderService.getLogicalExpressionSememeBuilder(logicalExpression,
               this,
               this.defaultLogicCoordinate.getStatedAssemblageSequence()));
      }

      for (final LogicalExpressionBuilder builder: this.logicalExpressionBuilders) {
         this.sememeBuilders.add(builderService.getLogicalExpressionSememeBuilder(builder.build(),
               this,
               this.defaultLogicCoordinate.getStatedAssemblageSequence()));
      }

      this.sememeBuilders.forEach((builder) -> builder.build(stampCoordinate, builtObjects));
      return conceptChronology;
   }

   @Override
   public OptionalWaitTask<ConceptChronology<?>> build(EditCoordinate editCoordinate,
         ChangeCheckerMode changeCheckerMode,
         List<ObjectChronology<? extends StampedVersion>> builtObjects)
            throws IllegalStateException {
      final ArrayList<OptionalWaitTask<?>> nestedBuilders = new ArrayList<>();
      final ConceptChronologyImpl conceptChronology = (ConceptChronologyImpl) Get.conceptService()
                                                                           .getConcept(getUuids());

      conceptChronology.createMutableVersion(this.state, editCoordinate);
      builtObjects.add(conceptChronology);

      if (getFullySpecifiedDescriptionBuilder() != null) {
         this.descriptionBuilders.add(getFullySpecifiedDescriptionBuilder());
      }

      if (getSynonymPreferredDescriptionBuilder() != null) {
         this.descriptionBuilders.add(getSynonymPreferredDescriptionBuilder());
      }

      this.descriptionBuilders.forEach((builder) -> {
                                     nestedBuilders.add(builder.build(editCoordinate, changeCheckerMode, builtObjects));
                                  });

      if ((this.defaultLogicCoordinate == null) &&
            ((this.logicalExpressions.size() > 0) || (this.logicalExpressionBuilders.size() > 0))) {
         throw new IllegalStateException("A logic coordinate is required when a logical expression is passed");
      }

      final SememeBuilderService builderService = LookupService.getService(SememeBuilderService.class);

      for (final LogicalExpression logicalExpression: this.logicalExpressions) {
         this.sememeBuilders.add(builderService.getLogicalExpressionSememeBuilder(logicalExpression,
               this,
               this.defaultLogicCoordinate.getStatedAssemblageSequence()));
      }

      for (final LogicalExpressionBuilder builder: this.logicalExpressionBuilders) {
         this.sememeBuilders.add(builderService.getLogicalExpressionSememeBuilder(builder.build(),
               this,
               this.defaultLogicCoordinate.getStatedAssemblageSequence()));
      }

      this.sememeBuilders.forEach((builder) -> nestedBuilders.add(builder.build(editCoordinate,
            changeCheckerMode,
            builtObjects)));

      Task<Void> primaryNested;

      if (changeCheckerMode == ChangeCheckerMode.ACTIVE) {
         primaryNested = Get.commitService()
                            .addUncommitted(conceptChronology);
      } else {
         primaryNested = Get.commitService()
                            .addUncommittedNoChecks(conceptChronology);
      }

      return new OptionalWaitTask<ConceptChronology<?>>(primaryNested, conceptChronology, nestedBuilders);
   }

   @Override
   public ConceptBuilder mergeFromSpec(ConceptSpecification conceptSpec) {
      setPrimordialUuid(conceptSpec.getPrimordialUuid());
      addUuids(conceptSpec.getUuids());

      if (!this.conceptName.equals(conceptSpec.getConceptDescriptionText())) {
         addDescription(conceptSpec.getConceptDescriptionText(), TermAux.SYNONYM_DESCRIPTION_TYPE);
      }

      return this;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getConceptDescriptionText() {
      return this.conceptName;
   }

   @Override
   public DescriptionBuilder<?, ?> getFullySpecifiedDescriptionBuilder() {
      synchronized (this) {
         if ((this.fsnDescriptionBuilder == null) && StringUtils.isNotBlank(this.conceptName)) {
            final StringBuilder descriptionTextBuilder = new StringBuilder();

            descriptionTextBuilder.append(this.conceptName);

            if (StringUtils.isNotBlank(this.semanticTag)) {
               if ((this.conceptName.lastIndexOf('(') > 0) && (this.conceptName.lastIndexOf(')') == this.conceptName.length() - 1)) {
                  throw new IllegalArgumentException(
                      "A semantic tag was passed, but this fsn description already appears to contain a semantic tag");
               }

               descriptionTextBuilder.append(" (");
               descriptionTextBuilder.append(this.semanticTag);
               descriptionTextBuilder.append(")");
            }

            if ((this.defaultLanguageForDescriptions == null) || (this.defaultDialectAssemblageForDescriptions == null)) {
               throw new IllegalStateException("language and dialect are required if a concept name is provided");
            }

            this.fsnDescriptionBuilder = LookupService.getService(DescriptionBuilderService.class)
                  .getDescriptionBuilder(descriptionTextBuilder.toString(),
                                         this,
                                         TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE,
                                         this.defaultLanguageForDescriptions)
                  .addPreferredInDialectAssemblage(this.defaultDialectAssemblageForDescriptions);
         }
      }

      return this.fsnDescriptionBuilder;
   }

   @Override
   public DescriptionBuilder<?, ?> getSynonymPreferredDescriptionBuilder() {
      synchronized (this) {
         if (this.preferredDescriptionBuilder == null) {
            if ((this.defaultLanguageForDescriptions == null) || (this.defaultDialectAssemblageForDescriptions == null)) {
               throw new IllegalStateException("language and dialect are required if a concept name is provided");
            }

            String prefName = null;

            if (StringUtils.isNotBlank(this.semanticTag)) {
               prefName = this.conceptName;
            } else if ((this.conceptName.lastIndexOf('(') > 0) && (this.conceptName.lastIndexOf(')') == this.conceptName.length())) {
               // they didn't provide a stand-alone semantic tag.  If they included a semantic tag in what they provided, strip it.
               // If not, don't create a preferred term, as it would just be identical to the FSN.
               prefName = this.conceptName.substring(0, this.conceptName.lastIndexOf('('))
                                     .trim();
            }

            if (prefName != null) {
               this.preferredDescriptionBuilder = LookupService.getService(DescriptionBuilderService.class)
                     .getDescriptionBuilder(prefName,
                                            this,
                                            TermAux.SYNONYM_DESCRIPTION_TYPE,
                                            this.defaultLanguageForDescriptions)
                     .addPreferredInDialectAssemblage(this.defaultDialectAssemblageForDescriptions);
            }
         }
      }

      return this.preferredDescriptionBuilder;
   }
}

