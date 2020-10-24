package sh.isaac.integration.tests.suite1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.Assert;
import org.testng.annotations.Test;

import sh.isaac.MetaData;
import sh.isaac.utility.Frills;


/**
 * 
 * {@link FrillsTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@HK2("integration")
@Test(suiteName="suite1")
public class FrillsTest {
	private static final Logger LOG = LogManager.getLogger();


	@Test(groups = { "frills" }, dependsOnGroups = { "load" })
	public void testChildren() {
		LOG.info("Testing Child methods");
		
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.CONTENT_METADATA____SOLOR.getNid(), true, true, null).size(), 7);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.CONTENT_METADATA____SOLOR.getNid(), false, true, null).size(), 7);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.CONTENT_METADATA____SOLOR.getNid(), true, false, null).size(), 7);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.CONTENT_METADATA____SOLOR.getNid(), false, false, null).size(), 7);
		
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.FEATURE____SOLOR.getNid(), false, false, null).size(), 1);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.FEATURE____SOLOR.getNid(), true, false, null).size(), 1);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.FEATURE____SOLOR.getNid(), false, true, null).size(), 1);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.FEATURE____SOLOR.getNid(), true, true, null).size(), 1);
		
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.VERSION_PROPERTIES____SOLOR.getNid(), false, false, null).size(), 8);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.VERSION_PROPERTIES____SOLOR.getNid(), true, false, null).size(), 12);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.VERSION_PROPERTIES____SOLOR.getNid(), false, true, null).size(), 7);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.VERSION_PROPERTIES____SOLOR.getNid(), true, true, null).size(), 11);
		
		Assert.assertTrue(Frills.definesIdentifierSemantic(MetaData.SCTID____SOLOR.getNid()));
		Assert.assertTrue(Frills.definesIdentifierSemantic(MetaData.SCTID____SOLOR.getNid()));
		
		Assert.assertTrue(Frills.definesIdentifierSemantic(MetaData.VUID____SOLOR.getNid()));
		Assert.assertTrue(Frills.definesIdentifierSemantic(MetaData.CODE____SOLOR.getNid()));
		Assert.assertTrue(Frills.definesIdentifierSemantic(MetaData.UUID____SOLOR.getNid()));
		
		Assert.assertFalse(Frills.definesIdentifierSemantic(MetaData.ACCEPTABLE____SOLOR.getNid()));
	}

}
