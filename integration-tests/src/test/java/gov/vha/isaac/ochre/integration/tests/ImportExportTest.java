package gov.vha.isaac.ochre.integration.tests;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.classifier.ClassifierResults;
import gov.vha.isaac.ochre.api.classifier.ClassifierService;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.coordinate.*;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataWriterService;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.api.tree.TreeNodeVisitData;
import gov.vha.isaac.ochre.model.logic.LogicByteArrayConverterService;
import gov.vha.isaac.ochre.model.logic.definition.LogicalExpressionBuilderOchreProvider;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.Assert;
import org.testng.annotations.Test;

import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.Feature;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.FloatLiteral;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.SomeRole;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.SufficientSet;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by kec on 1/2/16.
 */
@HK2("integration")
public class ImportExportTest {
    private static final Logger LOG = LogManager.getLogger();
    OchreExternalizableStatsTestFilter importStats;
	LogicalExpressionBuilderOchreProvider builderProvider = new LogicalExpressionBuilderOchreProvider();

    @Test (groups = {"load"})
    public void testLoad() {
        LOG.info("Testing load");
        try {
            BinaryDataReaderService reader = Get.binaryDataReader(Paths.get("target", "data", "IsaacMetadataAuxiliary.ibdf"));
            CommitService commitService = Get.commitService();
            importStats = new OchreExternalizableStatsTestFilter();
            reader.getStream().filter(importStats).forEach((object) -> {
                commitService.importNoChecks(object);
            });
            LOG.info("Loaded components: " + importStats);
        } catch (FileNotFoundException e) {
            Assert.fail("File not found", e);
        }
    }

    @Test (groups = {"load"}, dependsOnMethods = {"testLoad"})
    public void testStatedTaxonomy(){
        LOG.info("Testing stated taxonomy");
        TaxonomyCoordinate taxonomyCoordinate = Get.configurationService().getDefaultTaxonomyCoordinate().makeAnalog(PremiseType.STATED);
        int[] roots = Get.taxonomyService().getRoots(taxonomyCoordinate).toArray();
        Assert.assertEquals(roots.length, 1);

        Tree taxonomyTree = Get.taxonomyService().getTaxonomyTree(taxonomyCoordinate);
        AtomicInteger taxonomyCount = new AtomicInteger(0);
        taxonomyTree.depthFirstProcess(roots[0], (TreeNodeVisitData t, int conceptSequence) -> {
            taxonomyCount.incrementAndGet();
        });
        logTree(roots[0], taxonomyTree);
        Assert.assertEquals(taxonomyCount.get(), importStats.concepts.get());
    }

    @Test (groups = {"load"}, dependsOnMethods = {"testLoad"})
    public void testExportImport() {
        LOG.info("Testing exportImport");
        try {
            AtomicInteger exportCount = new AtomicInteger(0);
            AtomicInteger importCount = new AtomicInteger(0);
            OchreExternalizableStatsTestFilter exportStats = new OchreExternalizableStatsTestFilter();
            BinaryDataWriterService writer = Get.binaryDataWriter(Paths.get("target", "data", "IsaacMetadataAuxiliary.export.ibdf"));
            Get.ochreExternalizableStream().filter(exportStats).forEach((ochreExternalizable) -> {
                writer.put(ochreExternalizable);
                exportCount.incrementAndGet();
            });
            LOG.info("exported components: " + exportStats);
            Assert.assertEquals(exportStats, importStats);
            BinaryDataReaderService reader = Get.binaryDataReader(Paths.get("target", "data", "IsaacMetadataAuxiliary.export.ibdf"));
            OchreExternalizableStatsTestFilter importStats = new OchreExternalizableStatsTestFilter();
            CommitService commitService = Get.commitService();
            reader.getStream().filter(importStats).forEach((object) -> {
                importCount.incrementAndGet();
                commitService.importNoChecks(object);
            });
            LOG.info("imported components: " + importStats);

            Assert.assertEquals(exportCount.get(), importCount.get());
            Assert.assertEquals(exportStats, importStats);

        } catch (FileNotFoundException e) {
            Assert.fail("File not found", e);
        }
    }

    @Test (groups = {"load"}, dependsOnMethods = {"testExportImport"})
    public void testClassify() {
        LOG.info("Classifying");
        StampCoordinate stampCoordinate = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
        LogicCoordinate logicCoordinate = Get.coordinateFactory().createStandardElProfileLogicCoordinate();
        EditCoordinate editCoordinate = Get.coordinateFactory().createClassifierSolorOverlayEditCoordinate();
        ClassifierService logicService = Get.logicService().getClassifierService(stampCoordinate,
                logicCoordinate, editCoordinate);
        Task<ClassifierResults> classifyTask = logicService.classify();
        try {
            ClassifierResults classifierResults = classifyTask.get();
            LOG.info("Classify results: " + classifierResults);
        } catch (InterruptedException | ExecutionException e) {
            Assert.fail("Classify failed.", e);
        }

    }


    @Test (groups = {"load"}, dependsOnMethods = {"testClassify"})
    public void testInferredTaxonomy(){
        LOG.info("Testing inferred taxonomy");
        TaxonomyCoordinate taxonomyCoordinate = Get.configurationService().getDefaultTaxonomyCoordinate().makeAnalog(PremiseType.INFERRED);
        int[] roots = Get.taxonomyService().getRoots(taxonomyCoordinate).toArray();
        Assert.assertEquals(roots.length, 1);

        Tree taxonomyTree = Get.taxonomyService().getTaxonomyTree(taxonomyCoordinate);
        AtomicInteger taxonomyCount = new AtomicInteger(0);
        taxonomyTree.depthFirstProcess(roots[0], (TreeNodeVisitData t, int conceptSequence) -> {
            taxonomyCount.incrementAndGet();
        });
        Assert.assertEquals(taxonomyCount.get(), importStats.concepts.get());
        logTree(roots[0], taxonomyTree);
    }

    private void logTree(int root, Tree taxonomyTree) {
        taxonomyTree.depthFirstProcess(root, (TreeNodeVisitData t, int conceptSequence) -> {
            int paddingSize = t.getDistance(conceptSequence) * 2;
            char[] padding = new char[paddingSize];
            Arrays.fill(padding, ' ');
            LOG.info(new String(padding) + Get.conceptDescriptionText(conceptSequence));
        });
    }

    @Test (groups = {"load"}, dependsOnMethods = {"testClassify"})
    public void testExportAfterClassify() {
        LOG.info("Testing export after classify");
        try {
            OchreExternalizableStatsTestFilter exportStats = new OchreExternalizableStatsTestFilter();
            BinaryDataWriterService writer = Get.binaryDataWriter(Paths.get("target", "data", "IsaacMetadataAuxiliary.export.ibdf"));
            Get.ochreExternalizableStream().filter(exportStats).forEach((ochreExternalizable) -> {
                writer.put(ochreExternalizable);
                if (ochreExternalizable.getOchreObjectType() == OchreExternalizableObjectType.STAMP_ALIAS) {
                    LOG.info(ochreExternalizable);
                }
            });
            LOG.info("exported components: " + exportStats);
            if (exportStats.concepts.get() != importStats.concepts.get()) {
                Get.conceptService().getConceptChronologyStream().forEach((conceptChronology) -> LOG.info(conceptChronology));
            }
            Assert.assertEquals(exportStats.concepts.get(), importStats.concepts.get());
            // One new sememe for every concept except the root concept from classification...
            Assert.assertEquals(exportStats.sememes.get(), importStats.sememes.get() + exportStats.concepts.get() - 1);
            // One new stamp comment for the classify writeback
            Assert.assertEquals(exportStats.stampComments.get(), importStats.stampComments.get() + 1);
            Assert.assertEquals(exportStats.stampAliases.get(), importStats.stampAliases.get());


        } catch (FileNotFoundException e) {
            Assert.fail("File not found", e);
        }
    }

    @Test
    public void testConvertLogicGraphForm() throws Exception {
        LogicalExpressionBuilder defBuilder = builderProvider.getLogicalExpressionBuilder();

        SufficientSet(
                And(
                    SomeRole(MetaData.ROLE_GROUP,
                        And(
                            Feature(MetaData.HAS_STRENGTH, FloatLiteral(1.2345F, defBuilder)),
                            ConceptAssertion(TermAux.MASTER_PATH, defBuilder)))));
        LogicalExpression logicGraphDef = defBuilder.build();

        LogicByteArrayConverterService converter = new LogicByteArrayConverterService();

        byte[][] internalizedData = logicGraphDef.getData(DataTarget.INTERNAL);
        byte[][] externalizedData = converter.convertLogicGraphForm(internalizedData, DataTarget.EXTERNAL);
        byte[][] reinternalizedData = converter.convertLogicGraphForm(externalizedData, DataTarget.INTERNAL);

        if (! Arrays.deepEquals(internalizedData, reinternalizedData)) {
            Assert.fail("convertLogicGraphForm() FAILED: Reinternalized LogicGraph LogicalExpression does not match original internalized version");
        }
    }
}
