package gov.vha.isaac.ochre.integration.tests;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

/**
 * Created by kec on 1/2/16.
 */
@HK2("integration")
public class ImportExportTest {
    private static final Logger LOG = LogManager.getLogger();
    private static final int MAX_VERBOSE_COUNT = 10;
    private static final boolean VERBOSE = true;
    private int verboseCount = -1;
    private OchreExternalizableObjectType lastType = null;

    @Test (groups = {"load"})
    public void testLoad() {
        LOG.info("Testing load");
        try {
            BinaryDataReaderService reader = Get.binaryDataReader(Paths.get("target", "data", "IsaacMetadataAuxiliary.ibdf"));
            reader.getStream().forEach((object) -> {
                if (object.getOchreObjectType() != lastType) {
                    verboseCount = -1;
                    lastType = object.getOchreObjectType();
                }
                verboseCount++;
                if (VERBOSE && verboseCount < MAX_VERBOSE_COUNT) {
                    LOG.info("Read " + verboseCount + ": \n" + object);
                }
            });
        } catch (FileNotFoundException e) {
            Assert.fail("File not found", e);
        }
    }
}
