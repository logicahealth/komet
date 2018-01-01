package sh.isaac.integration.tests;

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
public class FrillsTest {
	private static final Logger LOG = LogManager.getLogger();


	@Test(groups = { "frills" }, dependsOnGroups = { "load" })
	public void testChildren() {
		LOG.info("Testing Child methods");
		
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.DESCRIPTION_ASSEMBLAGE____SOLOR.getNid(), true, true).size(), 10);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.DESCRIPTION_ASSEMBLAGE____SOLOR.getNid(), false, true).size(), 10);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.DESCRIPTION_ASSEMBLAGE____SOLOR.getNid(), true, false).size(), 10);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.DESCRIPTION_ASSEMBLAGE____SOLOR.getNid(), false, false).size(), 10);
		
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.FEATURE____SOLOR.getNid(), false, false).size(), 0);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.FEATURE____SOLOR.getNid(), true, false).size(), 0);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.FEATURE____SOLOR.getNid(), false, true).size(), 0);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.FEATURE____SOLOR.getNid(), true, true).size(), 0);
		
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.VERSION_PROPERTIES____SOLOR.getNid(), false, false).size(), 8);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.VERSION_PROPERTIES____SOLOR.getNid(), true, false).size(), 12);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.VERSION_PROPERTIES____SOLOR.getNid(), false, true).size(), 7);
		Assert.assertEquals(Frills.getAllChildrenOfConcept(MetaData.VERSION_PROPERTIES____SOLOR.getNid(), true, true).size(), 11);
		
		Assert.assertTrue(Frills.definesIdentifierSememe(MetaData.SCTID____SOLOR.getNid()));
		Assert.assertTrue(Frills.definesIdentifierSememe(MetaData.SCTID____SOLOR.getNid()));
		
		Assert.assertTrue(Frills.definesIdentifierSememe(MetaData.VUID____SOLOR.getNid()));
		Assert.assertTrue(Frills.definesIdentifierSememe(MetaData.CODE____SOLOR.getNid()));
		Assert.assertTrue(Frills.definesIdentifierSememe(MetaData.UUID____SOLOR.getNid()));
		
		Assert.assertFalse(Frills.definesIdentifierSememe(MetaData.ACCEPTABLE____SOLOR.getNid()));
	}

}
