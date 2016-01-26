package gov.vha.isaac.ochre.integration.tests;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.classifier.ClassifierResults;
import gov.vha.isaac.ochre.api.classifier.ClassifierService;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataWriterService;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kec on 1/2/16.
 */
@HK2("integration")
public class ImportExportTest {
    private static final Logger LOG = LogManager.getLogger();
    private static final int MAX_VERBOSE_COUNT = 10;
    private static final boolean VERBOSE = false;
    private int verboseCount = -1;
    private OchreExternalizableObjectType lastType = null;
    private int testLoadCount = 0;

    @Test (groups = {"load"})
    public void testLoad() {
        LOG.info("Testing load");
        try {
            BinaryDataReaderService reader = Get.binaryDataReader(Paths.get("target", "data", "IsaacMetadataAuxiliary.ibdf"));
            CommitService commitService = Get.commitService();
            OchreExternalizableStatsTestFilter importStats = new OchreExternalizableStatsTestFilter();
            reader.getStream().filter(importStats).forEach((object) -> {
                testLoadCount++;
                if (object.getOchreObjectType() != lastType) {
                    verboseCount = -1;
                    lastType = object.getOchreObjectType();
                }
                verboseCount++;
                if (VERBOSE && verboseCount < MAX_VERBOSE_COUNT) {
                    LOG.info("Read " + verboseCount + ": \n" + object);
                }
                commitService.importNoChecks(object);
            });
            LOG.info("Loaded components: " + testLoadCount + " " + importStats);
        } catch (FileNotFoundException e) {
            Assert.fail("File not found", e);
        }
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
            Assert.assertEquals(exportCount.get(), testLoadCount);
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


}
