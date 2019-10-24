package sh.isaac.solor.direct;

import sh.isaac.MetaData;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.brittle.Rf2Relationship;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sh.isaac.api.logic.LogicalExpressionBuilder.*;

public class OwlTransformerAndWriter extends TimedTaskWithProgressTracker<Void> {
    /**
     * The never role group set.
     */
    private final NidSet neverRoleGroupSet = new NidSet();

    private final NidSet definingCharacteristicSet = new NidSet();

    private final int isaNid = TermAux.IS_A.getNid();

    private final int legacyImplicationAssemblageNid = TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getNid();

    private final int sufficientDefinition = TermAux.SUFFICIENT_CONCEPT_DEFINITION.getNid();

    private final int primitiveDefinition = TermAux.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION.getNid();
    private final int solorOverlayModuleNid = TermAux.SOLOR_OVERLAY_MODULE.getNid();
    private final int statedAssemblageNid = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid();
    private final int inferredAssemblageNid = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid();
    private final int authorNid = TermAux.USER.getNid();
    private final int developmentPathNid = TermAux.DEVELOPMENT_PATH.getNid();
    private final TaxonomyService taxonomyService;

    {
        this.neverRoleGroupSet.add(TermAux.PART_OF.getNid());
        this.neverRoleGroupSet.add(TermAux.LATERALITY.getNid());
        this.neverRoleGroupSet.add(TermAux.HAS_ACTIVE_INGREDIENT.getNid());
        this.neverRoleGroupSet.add(TermAux.HAS_DOSE_FORM.getNid());

        this.definingCharacteristicSet.add(MetaData.INFERRED_PREMISE_TYPE____SOLOR.getNid());
        this.definingCharacteristicSet.add(MetaData.STATED_PREMISE_TYPE____SOLOR.getNid());
    }

    private final Semaphore writeSemaphore;
    private final List<TransformationGroup> transformationRecords;
    private final List<IndexBuilderService> indexers;
    private final ImportType importType;
    private final Instant commitTime;

    private static final AtomicInteger foundWatchCount = new AtomicInteger(0);

    public OwlTransformerAndWriter(List<TransformationGroup> transformationRecords,
                                   Semaphore writeSemaphore, ImportType importType, Instant commitTime) {
        this.transformationRecords = transformationRecords;
        this.writeSemaphore = writeSemaphore;
        this.importType = importType;
        this.commitTime = commitTime;
        this.writeSemaphore.acquireUninterruptibly();
        this.taxonomyService = Get.taxonomyService();
        indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle("EL++ OWL transformation");
        updateMessage("");
        addToTotalWork(transformationRecords.size());
        Get.activeTasks().add(this);
    }

    private void index(Chronology chronicle) {
        if (chronicle instanceof SemanticChronology) {
            this.taxonomyService.updateTaxonomy((SemanticChronology) chronicle);
        }
        for (IndexBuilderService indexer : indexers) {
            indexer.indexNow(chronicle);
        }
    }

    @Override
    protected Void call() throws Exception {
        try {
            Transaction transaction = Get.commitService().newTransaction(ChangeCheckerMode.INACTIVE);
            int count = 0;


            for (TransformationGroup transformationGroup : transformationRecords) {

                try {
                    transformOwlExpressions(transaction, transformationGroup.conceptNid, transformationGroup.semanticNids, transformationGroup.getPremiseType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (count % 1000 == 0) {
                    updateMessage("Processing concept: " + Get.conceptDescriptionText(transformationGroup.conceptNid));
                }
                count++;
                completedUnitOfWork();
            }
            transaction.commit("OWL transformer");
            return null;
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }
    }

    /**
     * Transform relationships.
     *
     * @param premiseType the stated
     */
    private void transformOwlExpressions(Transaction transaction, int conceptNid, int[] owlNids, PremiseType premiseType) throws IOException {
        updateMessage("Converting " + premiseType + " Owl expressions");

        List<SemanticChronology> owlChronologiesForConcept = new ArrayList<>();
        TreeSet<StampPositionImpl> stampPositions = new TreeSet<>();

        for (int owlNid: owlNids) {
            SemanticChronology owlChronology = Get.assemblageService().getSemanticChronology(owlNid);
            owlChronologiesForConcept.add(owlChronology);
            for (int stampSequence: owlChronology.getVersionStampSequences()) {
                stampPositions.add(new StampPositionImpl(Get.stampService().getTimeForStamp(stampSequence),
                        Get.stampService().getPathNidForStamp(stampSequence)));
            }
        }


        for (StampPositionImpl stampPosition: stampPositions) {
            StampCoordinate stampCoordinateForPosition = new StampCoordinateImpl(StampPrecedence.PATH,
                    stampPosition,
                    new HashSet(), new ArrayList(),
                    Status.makeActiveOnlySet());
            List<String> owlExpressionsToProcess = new ArrayList<>();

            for (SemanticChronology owlChronology: owlChronologiesForConcept) {
                LatestVersion<StringVersion> latestVersion = owlChronology.getLatestVersion(stampCoordinateForPosition);
                if (latestVersion.isPresent()) {
                    StringVersion sv = latestVersion.get();
                    if (sv.getStatus() == Status.ACTIVE) {
                        owlExpressionsToProcess.add(sv.getString());
                    }
                }
            }

            StringBuilder propertyBuilder = new StringBuilder();
            StringBuilder classBuilder = new StringBuilder();

            for (String owlExpression: owlExpressionsToProcess) {
                if (owlExpression.toLowerCase().contains("property")) {
                    propertyBuilder.append(" ").append(owlExpression);
                    if (!owlExpression.toLowerCase().contains("objectpropertychain")) {
                        //TODO ask Michael Lawley if this is ok...
                        String tempExpression = owlExpression.toLowerCase().replace("subobjectpropertyof", " subclassof");
                        classBuilder.append(" ").append(tempExpression);
                    }
                } else {
                    classBuilder.append(" ").append(owlExpression);
                }

            }
            String owlClassExpressionsToProcess = classBuilder.toString();
            String owlPropertyExpressionsToProcess = propertyBuilder.toString();


            LogicalExpression expression = SctOwlUtilities.sctToLogicalExpression(conceptNid, owlClassExpressionsToProcess,
                    owlPropertyExpressionsToProcess);

            addLogicGraph(transaction, conceptNid,
                    expression,
                    PremiseType.STATED,
                    stampPosition.getTime(),
                    TermAux.SOLOR_OVERLAY_MODULE.getNid(), stampCoordinateForPosition);
        }

    }

    /**
     * Adds the relationship graph.
     *
     * @param conceptNid        the conceptNid
     * @param logicalExpression the logical expression
     * @param premiseType       the premise type
     * @param time              the time
     * @param moduleNid         the module
     * @param stampCoordinate   for determining current version if a graph already
     *                          exists.
     */
    public void addLogicGraph(Transaction transaction, int conceptNid,
                              LogicalExpression logicalExpression,
                              PremiseType premiseType,
                              long time,
                              int moduleNid, StampCoordinate stampCoordinate) {
        if (time == Long.MAX_VALUE) {
            time = commitTime.toEpochMilli();
        }
        int graphAssemblageNid = statedAssemblageNid;
        if (premiseType == PremiseType.INFERRED) {
            graphAssemblageNid = inferredAssemblageNid;
        }

        final SemanticBuilder sb = Get.semanticBuilderService().getLogicalExpressionBuilder(logicalExpression,
                conceptNid,
                graphAssemblageNid);

        UUID nameSpace = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getPrimordialUuid();
        if (premiseType == PremiseType.INFERRED) {
            nameSpace = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getPrimordialUuid();
        }

        // See if a semantic already exists in this assemblage referencing this concept...
        NidSet graphNidsForComponent = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(conceptNid, graphAssemblageNid);
        if (!graphNidsForComponent.isEmpty()) {
//            LOG.info("Existing graph found for: " + Get.conceptDescriptionText(conceptNid));
            if (graphNidsForComponent.size() != 1) {
                throw new IllegalStateException("To many graphs for component: " + Get.conceptDescriptionText(conceptNid));
            }
            OptionalInt optionalGraphNid = graphNidsForComponent.findFirst();
            SemanticChronology existingGraph = Get.assemblageService().getSemanticChronology(optionalGraphNid.getAsInt());
            LatestVersion<LogicGraphVersionImpl> latest = existingGraph.getLatestVersion(stampCoordinate);
            if (latest.isPresent()) {
                LogicGraphVersionImpl logicGraphLatest = latest.get();
                LogicalExpression latestExpression = logicGraphLatest.getLogicalExpression();
                IsomorphicResults isomorphicResults = logicalExpression.findIsomorphisms(latestExpression);
                if (!isomorphicResults.equivalent()) {
                    int stamp = Get.stampService().getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, developmentPathNid);
                    final MutableLogicGraphVersion newVersion
                            = existingGraph.createMutableVersion(stamp);

                    newVersion.setGraphData(logicalExpression.getData(DataTarget.INTERNAL));
                    index(existingGraph);
                    Get.assemblageService().writeSemanticChronology(existingGraph);
                }
//                LOG.info("Isomorphic results: " + isomorphicResults);
            }
        } else {

// Create UUID from seed and assign SemanticBuilder the value
            final UUID generatedGraphPrimordialUuid = UuidT5Generator.get(nameSpace, Get.concept(conceptNid).getPrimordialUuid().toString());

            sb.setPrimordialUuid(generatedGraphPrimordialUuid);

            final ArrayList<IsaacExternalizable> builtObjects = new ArrayList<>();
            int stamp = Get.stampService().getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, developmentPathNid);
            final SemanticChronology sci = (SemanticChronology) sb.build(transaction, stamp,
                    builtObjects);
            // There should be no other build objects, so ignore the builtObjects list...

            if (builtObjects.size() != 1) {
                throw new IllegalStateException("More than one build object: " + builtObjects);
            }
            index(sci);
            Get.assemblageService().writeSemanticChronology(sci);
        }
    }
}
