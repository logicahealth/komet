package gov.vha.isaac.ochre.integration.tests;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
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

    @Test (groups = {"load"})
    public void testLoad() {
        LOG.info("Testing load");
        try {
            BinaryDataReaderService reader = Get.binaryDataReader(Paths.get("target", "data", "IsaacMetadataAuxiliary.ibdf"));
            reader.getStream().forEach((object) -> LOG.info("Read: " + object));
        } catch (FileNotFoundException e) {
            Assert.fail("File not found", e);
        }
    }
}
