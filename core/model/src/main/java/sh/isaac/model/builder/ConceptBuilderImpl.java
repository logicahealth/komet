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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.concurrent.Task;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.coordinate.WriteCoordinateImpl;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.SemanticTags;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.concept.ConceptChronologyImpl;

/**
 * The Class ConceptBuilderImpl.
 *
 * @author kec
 */
public class ConceptBuilderImpl
        extends ComponentBuilder<ConceptChronology>
        implements ConceptBuilder {
    private static final Logger LOG = LogManager.getLogger();

    /**
     * The description builders.
     */
    private final List<DescriptionBuilder<?, ?>> descriptionBuilders = new ArrayList<>();

    /**
     * The logical expression builders.
     */
    private final List<LogicalExpressionBuilder> logicalExpressionBuilders = new ArrayList<>();

    /**
     * The logical expressions.
     */
    private final List<LogicalExpression> logicalExpressions = new ArrayList<>();

    /**
     * The FQN description builder.
     */
    private transient DescriptionBuilder<?, ?> fqnDescriptionBuilder = null;

    /**
     * The preferred description builder.
     */
    private transient DescriptionBuilder<?, ?> preferredDescriptionBuilder = null;

    private final transient HashMap<LogicalExpressionBuilder, SemanticBuilder<?>> builtLogicalExpressionBuilders = new HashMap<>();
    private final transient HashMap<LogicalExpression, SemanticBuilder<?>> builtLogicalExpressions = new HashMap<>();

    /**
     * The concept name.
     */
    private final String conceptName;

    /**
     * The semantic tag.
     */
    private final String semanticTag;

    /**
     * The default language for descriptions.
     */
    private final ConceptSpecification defaultLanguageForDescriptions;

    /**
     * The default dialect assemblage for descriptions.
     */
    private final ConceptSpecification defaultDialectAssemblageForDescriptions;

    /**
     * The default logic coordinate.
     */
    private final LogicCoordinate defaultLogicCoordinate;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new concept builder ochre impl.
     *
     * @param conceptName                             - Optional - if specified, a FQN will be created using this value (but see additional
     *                                                information on semanticTag)
     * @param semanticTag                             - Optional - if specified, conceptName must be specified, and two descriptions will be created
     *                                                using the following forms:
     *                                                FQN: - "conceptName (semanticTag)"
     *                                                Regular Name: "conceptName"
     *                                                If the specified conceptName already contains a semantic tag, this tag will override the semanticTag parameter (and the semanticTag
     *                                                parameter will be ignored)
     *                                                <p>
     *                                                If the semantic tag is not specified:
     *                                                - If the specified FQN contains a semantic tag, the FQN will be created using that value.  A regular name will be created by stripping the
     *                                                supplied semantic tag.
     *                                                - If the specified FQN does not contain a semantic tag, no regular term will be created (and the FQN will be created WITHOUT a semantic tag)
     * @param logicalExpression                       - Optional
     * @param defaultLanguageForDescriptions          - Optional - used as the language for the created FQN and preferred term
     * @param defaultDialectAssemblageForDescriptions - Optional - used as the language for the created FQN and preferred
     *                                                term
     * @param defaultLogicCoordinate                  - Optional - used during the creation of the logical expression, if either a
     *                                                logicalExpression is passed, or if @link {@link #addLogicalExpression(LogicalExpression)} or
     *                                                {@link #addLogicalExpressionBuilder(LogicalExpressionBuilder)} are used later.
     * @param assemblageId                            the assemblage ID to create this concept in
     */
    public ConceptBuilderImpl(String conceptName,
                              String semanticTag,
                              LogicalExpression logicalExpression,
                              ConceptSpecification defaultLanguageForDescriptions,
                              ConceptSpecification defaultDialectAssemblageForDescriptions,
                              LogicCoordinate defaultLogicCoordinate,
                              int assemblageId) {
        super(assemblageId);
        this.conceptName = SemanticTags.stripSemanticTagIfPresent(conceptName);
        this.semanticTag = SemanticTags.findSemanticTagIfPresent(conceptName).orElse(semanticTag);
        this.defaultLanguageForDescriptions = defaultLanguageForDescriptions;
        this.defaultDialectAssemblageForDescriptions = defaultDialectAssemblageForDescriptions;
        this.defaultLogicCoordinate = defaultLogicCoordinate;

        if (this.defaultLogicCoordinate.getStatedAssemblageNid() != TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid()) {
            throw new IllegalStateException("Incorrect stated assemblage: " + Get.conceptDescriptionText(this.defaultLogicCoordinate.getStatedAssemblageNid()));
        }

        if (logicalExpression != null) {
            this.logicalExpressions.add(logicalExpression);
        }
    }

    /**
     * Adds the description.
     *
     * @param descriptionBuilder the description builder
     * @return the concept builder
     */
    @Override
    public ConceptBuilder addDescription(DescriptionBuilder<?, ?> descriptionBuilder) {
        this.descriptionBuilders.add(descriptionBuilder);
        return this;
    }

    /**
     * Adds the description.
     *
     * @param value           the value
     * @param descriptionType the description type
     * @return the concept builder
     */
    @Override
    public ConceptBuilder addDescription(String value, ConceptSpecification descriptionType) {
        if ((this.defaultLanguageForDescriptions == null) || (this.defaultDialectAssemblageForDescriptions == null)) {
            throw new IllegalStateException("language and dialect are required if a concept name is provided");
        }

        if (!value.equals(this.conceptName)) {
            this.descriptionBuilders.add(LookupService.getService(DescriptionBuilderService.class)
                    .getDescriptionBuilder(value, this, descriptionType, this.defaultLanguageForDescriptions)
                    .addAcceptableInDialectAssemblage(this.defaultDialectAssemblageForDescriptions));
        }

        return this;
    }

    /**
     * Adds the logical expression.
     *
     * @param logicalExpression the logical expression
     * @return the concept builder
     */
    @Override
    public ConceptBuilder addLogicalExpression(LogicalExpression logicalExpression) {
        this.logicalExpressions.add(logicalExpression);
        return this;
    }

    /**
     * Adds the logical expression builder.
     *
     * @param logicalExpressionBuilder the logical expression builder
     * @return the concept builder
     */
    @Override
    public ConceptBuilder addLogicalExpressionBuilder(LogicalExpressionBuilder logicalExpressionBuilder) {
        this.logicalExpressionBuilders.add(logicalExpressionBuilder);
        return this;
    }

    /**
     * Sets the logical expression. This method erases any previous logical expressions.
     *
     * @param logicalExpression the logical expression
     * @return the concept builder
     */
    @Override
    public ConceptBuilder setLogicalExpression(LogicalExpression logicalExpression) {
        this.logicalExpressions.clear();
        this.logicalExpressions.add(logicalExpression);
        return this;
    }

    /**
     * Sets the logical expression builder. This method erases previous logical expression builders.
     *
     * @param logicalExpressionBuilder the logical expression builder
     * @return the concept builder
     */
    @Override
    public ConceptBuilder setLogicalExpressionBuilder(LogicalExpressionBuilder logicalExpressionBuilder) {
        this.logicalExpressionBuilders.clear();
        this.logicalExpressionBuilders.add(logicalExpressionBuilder);
        return this;
    }

    /*
     * Note, this does NOT fire the sub builders, if writeSubs is false
     */
    private ConceptChronology buildInternal(WriteCoordinate writeCoordinate, List<Chronology> builtObjects, boolean buildSubs) 
            throws IllegalStateException {
        UUID[] uuids = getUuids();
        final ConceptChronologyImpl conceptChronology = new ConceptChronologyImpl(uuids[0], this.assemblageId);
        for (int i = 1; i < uuids.length; i++) {
            conceptChronology.addAdditionalUuids(uuids[i]);
        }

        conceptChronology.createMutableVersion(status == null ? writeCoordinate : new WriteCoordinateImpl(writeCoordinate, status));
        builtObjects.add(conceptChronology);
        if (buildSubs) {
            getDescriptionBuilders().forEach((builder) -> builder.build(writeCoordinate, builtObjects));
            getSemanticBuilders().forEach((builder) -> builder.build(writeCoordinate, builtObjects));
        }
        return conceptChronology;
    }

    @Override
    public ConceptChronology build(Transaction transaction, int stampSequence, List<Chronology> builtObjects) throws IllegalStateException {
        return buildInternal(adjustForModule(new WriteCoordinateImpl(transaction, stampSequence)), builtObjects, true);
    }

    @Override
    public OptionalWaitTask<ConceptChronology> buildAndWrite(WriteCoordinate writeCoordinate, List<Chronology> builtObjects) throws IllegalStateException {
        WriteCoordinate forWrite = adjustForModule(writeCoordinate);
        ConceptChronology cc = buildInternal(forWrite, builtObjects, false);
        final ArrayList<OptionalWaitTask<?>> nestedBuilders = new ArrayList<>();

        getDescriptionBuilders().forEach((builder) -> nestedBuilders.add(builder.buildAndWrite(forWrite, builtObjects)));
        getSemanticBuilders().forEach((builder) -> nestedBuilders.add(builder.buildAndWrite(forWrite, builtObjects)));

        Task<Void> primaryNested = Get.commitService().addUncommitted(forWrite.getTransaction().get(), cc);
        return new OptionalWaitTask<>(primaryNested, cc, nestedBuilders);
    }

    @Override
    public ConceptBuilder mergeFromSpec(ConceptSpecification conceptSpec) {
        setPrimordialUuid(conceptSpec.getPrimordialUuid());
        addUuids(conceptSpec.getUuids());

        String specSemTag = SemanticTags.findSemanticTagIfPresent(conceptSpec.getFullyQualifiedName()).orElse(null);

        //Not sure if adding two FQN is ideal, but not sure the proper merge otherwise.
        if (!this.conceptName.equals(SemanticTags.stripSemanticTagIfPresent(conceptSpec.getFullyQualifiedName()))
                || StringUtils.isNotBlank(this.semanticTag) && StringUtils.isNotBlank(specSemTag) && !this.semanticTag.equals(specSemTag)) {
            addDescription(SemanticTags.addSemanticTagIfAbsent(conceptSpec.getFullyQualifiedName(), specSemTag), TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
        }


        Optional<String> temp = conceptSpec.getRegularName();

        if (temp.isPresent() && !temp.get().equals(this.conceptName)) {
            addDescription(temp.get(), TermAux.REGULAR_NAME_DESCRIPTION_TYPE);
        }

        return this;
    }

    @Override
    public String getFullyQualifiedName() {
        return getFullySpecifiedDescriptionBuilder().getDescriptionText();
    }


    @Override
    public DescriptionBuilder<?, ?> getFullySpecifiedDescriptionBuilder() {
        synchronized (this) {
            if ((this.fqnDescriptionBuilder == null) && StringUtils.isNotBlank(this.conceptName)) {

                if ((this.defaultLanguageForDescriptions == null)
                        || (this.defaultDialectAssemblageForDescriptions == null)) {
                    throw new IllegalStateException("language and dialect are required if a concept name is provided");
                }

                this.fqnDescriptionBuilder = LookupService.getService(DescriptionBuilderService.class)
                        .getDescriptionBuilder(
                                StringUtils.isNotBlank(this.semanticTag) ? SemanticTags.addSemanticTagIfAbsent(this.conceptName, this.semanticTag) : this.conceptName,
                                this,
                                TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE,
                                this.defaultLanguageForDescriptions)
                        .addPreferredInDialectAssemblage(this.defaultDialectAssemblageForDescriptions);
            }
        }

        return this.fqnDescriptionBuilder;
    }

    /**
     * Gets the synonym preferred description builder.
     *
     * @return the synonym preferred description builder
     */
    @Override
    public DescriptionBuilder<?, ?> getPreferredDescriptionBuilder() {
        synchronized (this) {
            if (this.preferredDescriptionBuilder == null && StringUtils.isNotBlank(this.conceptName)) {
                if ((this.defaultLanguageForDescriptions == null)
                        || (this.defaultDialectAssemblageForDescriptions == null)) {
                    throw new IllegalStateException("language and dialect are required if a concept name is provided");
                }

                String prefName = null;

                if (StringUtils.isNotBlank(this.semanticTag)) {
                    prefName = this.conceptName;  //We have all ready stripped semantic tags from this
                } else {
                    // they didn't provide a stand-alone semantic tag.  don't create a preferred term, as it would just be identical to the FSN.
                }

                if (prefName != null) {
                    this.preferredDescriptionBuilder = LookupService.getService(DescriptionBuilderService.class)
                            .getDescriptionBuilder(prefName,
                                    this,
                                    TermAux.REGULAR_NAME_DESCRIPTION_TYPE,
                                    this.defaultLanguageForDescriptions)
                            .addPreferredInDialectAssemblage(this.defaultDialectAssemblageForDescriptions);
                }
            }
        }

        return this.preferredDescriptionBuilder;
    }

    @Override
    public Optional<String> getRegularName() {
        DescriptionBuilder<?, ?> descriptionBuilder = getPreferredDescriptionBuilder();
        return Optional.of(descriptionBuilder.getDescriptionText());
    }

    @Override
    public String toString() {
        if (this.isPrimordialUuidSet()) {
            return "ConceptBuilderImpl{" + conceptName + (StringUtils.isNotBlank(semanticTag) ? " (" + semanticTag + ") " : " ") + this.getPrimordialUuid() + '}';
        }
        return "ConceptBuilderImpl{" + conceptName + (StringUtils.isNotBlank(semanticTag) ? " (" + semanticTag + ") " : " ") + "Primordial UUID not set" + '}';
    }

    @Override
    public List<DescriptionBuilder<?, ?>> getDescriptionBuilders() {
        List<DescriptionBuilder<?, ?>> temp = new ArrayList<>(descriptionBuilders.size() + 2);
        temp.addAll(descriptionBuilders);
        if (getFullySpecifiedDescriptionBuilder() != null) {
            temp.add(getFullySpecifiedDescriptionBuilder());
        }
        if (getPreferredDescriptionBuilder() != null) {
            temp.add(getPreferredDescriptionBuilder());
        }
        return temp;
    }

    @Override
    public IdentifiedComponentBuilder<ConceptChronology> setT5Uuid(UUID namespace, BiConsumer<String, UUID> consumer) {
        if (isPrimordialUuidSet() && getPrimordialUuid().version() == 4) {
            throw new RuntimeException("Attempting to set Type 5 UUID where the UUID was previously set to random on - " + getFullyQualifiedName());
        }
        if (!isPrimordialUuidSet()) {
            setPrimordialUuid(UuidT5Generator.get(namespace == null ? UuidT5Generator.PATH_ID_FROM_FS_DESC : namespace, getFullyQualifiedName(), consumer));
        }
        return this;
    }

    @Override
    public IdentifiedComponentBuilder<ConceptChronology> setT5UuidNested(UUID namespace) {
        setT5Uuid(namespace, null);
        for (DescriptionBuilder<?, ?> db : getDescriptionBuilders()) {
            db.setT5UuidNested(namespace);
        }

        for (SemanticBuilder<?> sb : getSemanticBuilders()) {
            sb.setT5UuidNested(namespace);
        }
        return this;
    }

    @Override
    public List<SemanticBuilder<?>> getSemanticBuilders() {
        List<SemanticBuilder<?>> temp = new ArrayList<>(super.getSemanticBuilders().size() + logicalExpressionBuilders.size() + logicalExpressions.size());
        temp.addAll(super.getSemanticBuilders());

        if (defaultLogicCoordinate == null && (logicalExpressions.size() > 0 || logicalExpressionBuilders.size() > 0)) {
            throw new IllegalStateException("A logic coordinate is required when a logical expression is passed");
        }

        SemanticBuilderService<?> builderService = LookupService.getService(SemanticBuilderService.class);
        for (LogicalExpression logicalExpression : logicalExpressions) {
            if (!builtLogicalExpressions.containsKey(logicalExpression)) {
                builtLogicalExpressions.put(logicalExpression,
                        builderService.getLogicalExpressionBuilder(logicalExpression, this, defaultLogicCoordinate.getStatedAssemblageNid()));
            }
            temp.add(builtLogicalExpressions.get(logicalExpression));
        }
        for (LogicalExpressionBuilder builder : logicalExpressionBuilders) {
            if (!builtLogicalExpressionBuilders.containsKey(builder)) {
                builtLogicalExpressionBuilders.put(builder,
                        builderService.getLogicalExpressionBuilder(builder.build(), this, defaultLogicCoordinate.getStatedAssemblageNid()));
            }
            temp.add(builtLogicalExpressionBuilders.get(builder));
        }

        return temp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VersionType getVersionType() {
        return VersionType.CONCEPT;
    }

    @Override
   public IdentifiedComponentBuilder<? extends SemanticChronology> createAddComponentIntSemantic(ConceptSpecification component, int fieldIndex, ConceptSpecification assemblage) {
      SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService()
            .getComponentIntSemanticBuilder(component.getNid(), fieldIndex, this, assemblage.getNid());
      addSemantic(builder);
      return builder;
   }

   @Override
   public ConceptBuilder addComponentIntSemantic(ConceptSpecification component, int fieldIndex, ConceptSpecification assemblage) {
      createAddComponentIntSemantic(component, fieldIndex, assemblage);
        return this;
    }

    @Override
   public IdentifiedComponentBuilder<? extends SemanticChronology> createAddComponentSemantic(ConceptSpecification component, ConceptSpecification assemblage) {
      SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService()
               .getComponentSemanticBuilder(component.getNid(), this, assemblage.getNid());
      addSemantic(builder);
      return builder;
    }

    @Override
   public ConceptBuilder addComponentSemantic(ConceptSpecification component, ConceptSpecification assemblage) {
      createAddComponentSemantic(component, assemblage);
        return this;
    }

    @Override
    public IdentifiedComponentBuilder<? extends SemanticChronology> createAddStringSemantic(String strValue, ConceptSpecification assemblage) {
        SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService()
                .getStringSemanticBuilder(strValue, this, assemblage.getNid());
        addSemantic(builder);
        return builder;
    }

    @Override
    public ConceptBuilder addStringSemantic(String strValue, ConceptSpecification assemblage) {
        createAddStringSemantic(strValue, assemblage);
        return this;
    }

    @Override
    public IdentifiedComponentBuilder<? extends SemanticChronology> createAddFieldSemanticConcept(String fieldName, int fieldIndex) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public IdentifiedComponentBuilder<? extends SemanticChronology> createAddFieldSemanticConcept(UUID conceptUuid, int fieldIndex) {
        SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService()
                .getComponentIntSemanticBuilder(Get.nidForUuids(conceptUuid), fieldIndex, this, TermAux.SEMANTIC_TYPE.getNid());
        addSemantic(builder);
        return builder;
     }
}
