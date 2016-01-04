package gov.vha.isaac.ochre.model.concept;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.memory.HeapUseTicker;
import gov.vha.isaac.ochre.api.progress.ActiveTasksTicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.nio.file.Paths;

import static gov.vha.isaac.ochre.api.constants.Constants.CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by kec on 1/2/16.
 */
@HK2("model")
public class ConceptSuite {
    private static final Logger LOG = LogManager.getLogger();

    @BeforeGroups(groups = {"services"})
    public void setUpSuite() throws Exception {
        LOG.info("ModelSuiteManagement setup");

        System.setProperty(CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY, "target/object-chronicles");

        java.nio.file.Path dbFolderPath = Paths.get(System.getProperty(CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY));
        LOG.info("termstore folder path exists: " + dbFolderPath.toFile().exists());

        LookupService.startupIsaac();
        ActiveTasksTicker.start(10);
        HeapUseTicker.start(10);
    }

    @AfterGroups(groups = {"services"})
    public void tearDownSuite() throws Exception {
        LOG.info("ModelSuiteManagement tear down");
        LookupService.shutdownIsaac();
        ActiveTasksTicker.stop();
        HeapUseTicker.stop();
    }
    
    @Test(groups = {"services"})
    public void testSerializationNoVersions() throws Exception {
        UUID primordialUuid = UUID.fromString("2b2b14cd-ea97-4bbc-a3e7-6f7f00e6eff1");
        int nid = Get.identifierService().getNidForUuids(primordialUuid);
        int containerSequence = 1;
        ConceptChronologyImpl conceptChronology = new ConceptChronologyImpl(primordialUuid, nid, containerSequence);

        byte[] data = conceptChronology.getDataToWrite();
        ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(data);

        ConceptChronologyImpl conceptChronology2 =  ConceptChronologyImpl.make(buffer);

        Assert.assertEquals(conceptChronology, conceptChronology2);


        ByteArrayDataBuffer externalBuffer = new ByteArrayDataBuffer(data);
        externalBuffer.setExternalData(true);
        conceptChronology.putExternal(externalBuffer);

        externalBuffer.reset();
        ConceptChronologyImpl conceptChronology3 =  ConceptChronologyImpl.make(externalBuffer);
        Assert.assertEquals(conceptChronology, conceptChronology3);

    }

}
