package sh.komet.fx.stage.spreadsheet;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidFactory;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SpreadsheetImporter extends TimedTaskWithProgressTracker<Void> {
    protected final String topConceptFQN;
    protected final String topConceptPreferredName;
    protected final File loadFile;
    protected final UUID sourceUuid;
    protected final long commitTime;
    protected final ConceptSpecification author;
    protected final ConceptSpecification module;
    protected final ConceptSpecification path;
    protected final UUID topConceptUuid;

    public SpreadsheetImporter(String topConceptFQN, String topConceptPreferredName, File loadFile, UUID sourceUuid, long commitTime,
                               ConceptSpecification author, ConceptSpecification module, ConceptSpecification path) {
        this.topConceptFQN = topConceptFQN;
        this.topConceptPreferredName = topConceptPreferredName;
        this.loadFile = loadFile;
        this.sourceUuid = sourceUuid;
        this.commitTime = commitTime;
        this.author = author;
        this.module = module;
        this.path = path;
        this.topConceptUuid = UuidT5Generator.get(sourceUuid, topConceptFQN);

        updateTitle("Importing " + topConceptFQN);
        Get.activeTasks().add(this);
    }
    protected abstract void addModelData(int stampSequence, Transaction transaction);

    private void load() throws Exception {

        try (CSVReader reader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(new FileInputStream(loadFile))))
                .withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build()) {
            AtomicInteger count = new AtomicInteger();
            reader.forEach(strings -> {
                count.incrementAndGet();
            });
            addToTotalWork(count.get());
        }

        Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.INACTIVE);
        int stampSequence = Get.stampService().getStampSequence(Status.ACTIVE, commitTime, author.getNid(), module.getNid(), path.getNid());

        addTopConcept(transaction, stampSequence);

        try (CSVReader reader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(new FileInputStream(loadFile))))
                .withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build()) {
            AtomicInteger count = new AtomicInteger();
            reader.forEach(strings -> {

                if (count.getAndIncrement() == 0) {
                    //skip first line...
                    completedUnitOfWork();
                } else {
                    boolean hasData = false;
                    for (String string: strings) {
                        if (string != null &! string.isBlank()) {
                            hasData = true;
                            break;
                        }
                    }
                    if (hasData) {
                        processLine(strings, stampSequence, transaction);
                    }
                    completedUnitOfWork();
                }
            });
        }

        transaction.commit("Spreadsheet importer", DateTimeUtil.epochToInstant(commitTime));
        ManifoldCoordinateImmutable manifold = Coordinates.Manifold.DevelopmentStatedRegularNameSort();
        ObservableManifoldCoordinate observableManifold = new ObservableManifoldCoordinateImpl(manifold);
        observableManifold.getEditCoordinate().authorForChangesProperty().setValue(author);
        observableManifold.getEditCoordinate().defaultModuleProperty().setValue(this.module);
        observableManifold.setManifoldPath(this.path);
        observableManifold.getViewStampFilter().timeProperty().setValue(this.commitTime);
        Get.logicService().getClassifierService(observableManifold.toManifoldCoordinateImmutable()).classify().get();
    }

    protected void addTopConcept(Transaction transaction, int stampSequence) {
        addConcept(topConceptUuid, stampSequence);
        addParents(topConceptUuid, stampSequence, transaction, TermAux.SOLOR_ROOT.getPrimordialUuid());
        addFullyQualifiedName(this.topConceptFQN, topConceptUuid, stampSequence);
        addPreferredName(this.topConceptPreferredName, topConceptUuid, stampSequence);
        addModelData(stampSequence, transaction);
    }

    abstract void processLine(String[] fields, int stampSequence, Transaction transaction);

    protected void addStringSemantic(UUID collectionUuid, UUID referencedComponentUuid, String semanticValue, int stampSequence) {

        // add to loinc identifier assemblage
        UUID loincIdentifierUuid = UuidFactory.getUuidForStringSemantic(MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getPrimordialUuid(),
                collectionUuid, referencedComponentUuid, semanticValue, null);

        SemanticChronologyImpl stringChronologyToWrite = new SemanticChronologyImpl(VersionType.STRING,
                loincIdentifierUuid,
                Get.identifierService().assignNid(collectionUuid),
                Get.identifierService().assignNid(referencedComponentUuid));

        StringVersionImpl loincIdVersion = stringChronologyToWrite.createMutableVersion(stampSequence);
        loincIdVersion.setString(semanticValue);
        index(stringChronologyToWrite);
        Get.assemblageService().writeSemanticChronology(stringChronologyToWrite);

    }
    protected void addParents(UUID conceptUuid, int stampSequence, Transaction transaction, UUID... parentUuids) {
        LogicalExpressionBuilder builder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        Assertion[] parents = new Assertion[parentUuids.length];

        for (int i = 0; i < parents.length; i++) {
            UUID parentUuid = parentUuids[i];
            parents[i] = builder.conceptAssertion(Get.identifierService().assignNid(parentUuid));
        }
        builder.necessarySet(builder.and(parents));

        int graphAssemblageNid = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid();

        final SemanticBuilder<? extends SemanticChronology> sb = Get.semanticBuilderService().getLogicalExpressionBuilder(builder.build(),
                Get.identifierService().getNidForUuids(conceptUuid),
                graphAssemblageNid);

        UUID nameSpace = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getPrimordialUuid();

        // Create UUID from seed and assign SemanticBuilder the value
        //Dan is leaving this one as it, rather than using the UuidFactory, in case some other code is trying to align logic graph creation for
        //loinc.
        final UUID generatedGraphPrimordialUuid = UuidT5Generator.get(nameSpace, conceptUuid.toString());

        sb.setPrimordialUuid(generatedGraphPrimordialUuid);

        final ArrayList<Chronology> builtObjects = new ArrayList<>();

        final SemanticChronology sci = (SemanticChronology) sb.build(transaction, stampSequence,
                builtObjects);
        // There should be no other build objects, so ignore the builtObjects list...

        if (builtObjects.size() != 1) {
            throw new IllegalStateException("More than one build object: " + builtObjects);
        }
        index(sci);
        Get.assemblageService().writeSemanticChronology(sci);

    }


    @Override
    protected Void call() throws Exception {
        try {
            load();
        } finally {
            Get.activeTasks().remove(this);
        }
        return null;
    }

    protected void addConcept(UUID conceptUuid, int stampSequence) {
        ConceptChronologyImpl conceptToWrite
                = new ConceptChronologyImpl(conceptUuid, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
        conceptToWrite.createMutableVersion(stampSequence);
        Get.conceptService().writeConcept(conceptToWrite);
        index(conceptToWrite);

    }
    protected void addDefinition(String definitionString, UUID conceptUuid, int stampSequence) {
        addDescription(definitionString,
                TermAux.DEFINITION_DESCRIPTION_TYPE, conceptUuid, stampSequence, true);
    }

    protected void addPreferredName(String preferredString, UUID conceptUuid, int stampSequence) {
        addDescription(preferredString,
                TermAux.REGULAR_NAME_DESCRIPTION_TYPE, conceptUuid, stampSequence, true);
    }


    protected void addFullyQualifiedName(String fqnString, UUID conceptUuid, int stampSequence) {
        addDescription(fqnString,
                TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, conceptUuid, stampSequence, true);
    }

    private void addDescription(String description, ConceptSpecification descriptionType,
                                UUID conceptUuid, int recordStamp, boolean preferredInDialect) {

        UUID descriptionUuid = UuidFactory.getUuidForDescriptionSemantic(MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(),
                conceptUuid, MetaData.DESCRIPTION_INITIAL_CHARACTER_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), descriptionType.getPrimordialUuid(),
                MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(), description, null);

        int descriptionTypeNid = descriptionType.getNid();
        int conceptNid = Get.identifierService().assignNid(conceptUuid);

        SemanticChronologyImpl descriptionToWrite
                = new SemanticChronologyImpl(VersionType.DESCRIPTION, descriptionUuid,
                MetaData.ENGLISH_LANGUAGE____SOLOR.getNid(), conceptNid);
        DescriptionVersionImpl descriptionVersion = descriptionToWrite.createMutableVersion(recordStamp);
        descriptionVersion.setCaseSignificanceConceptNid(
                MetaData.DESCRIPTION_INITIAL_CHARACTER_CASE_SENSITIVE____SOLOR.getNid());
        descriptionVersion.setDescriptionTypeConceptNid(descriptionTypeNid);
        descriptionVersion.setLanguageConceptNid(TermAux.ENGLISH_LANGUAGE.getNid());
        descriptionVersion.setText(description);

        index(descriptionToWrite);
        Get.assemblageService().writeSemanticChronology(descriptionToWrite);

        UUID acceptabilityUuid = UuidFactory.getUuidForComponentNidSemantic(TermAux.US_DIALECT_ASSEMBLAGE.getPrimordialUuid(), TermAux.US_DIALECT_ASSEMBLAGE.getPrimordialUuid(),
                descriptionUuid, preferredInDialect ? TermAux.PREFERRED.getPrimordialUuid() : TermAux.ACCEPTABLE.getPrimordialUuid(), null);

        SemanticChronologyImpl dialectToWrite = new SemanticChronologyImpl(
                VersionType.COMPONENT_NID,
                acceptabilityUuid,
                TermAux.US_DIALECT_ASSEMBLAGE.getNid(),
                descriptionToWrite.getNid());
        ComponentNidVersionImpl dialectVersion = dialectToWrite.createMutableVersion(recordStamp);
        if (preferredInDialect) {
            dialectVersion.setComponentNid(TermAux.PREFERRED.getNid());
        } else {
            dialectVersion.setComponentNid(TermAux.ACCEPTABLE.getNid());
        }

        index(dialectToWrite);
        Get.assemblageService().writeSemanticChronology(dialectToWrite);
    }

    protected void index(Chronology chronicle) {
        if (chronicle.getVersionType() == VersionType.LOGIC_GRAPH) {
            Get.taxonomyService().updateTaxonomy((SemanticChronology) chronicle);
        }
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            indexer.indexNow(chronicle);
        }
    }

}
