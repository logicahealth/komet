package sh.isaac.integration.tests.suite1;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.Assert;
import org.testng.annotations.Test;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.model.semantic.DynamicUsageDescriptionImpl;

@HK2("integration")
@Test(suiteName = "suite1")
public class StaticMetadataTests
{
	private static final Logger LOG = LogManager.getLogger();

	@Test(groups = { "staticMetadata" }, dependsOnGroups = { "load" })
	public void testChildren()
	{
		LOG.info("Testing Static Metadata Structure");

		AtomicInteger testCount = new AtomicInteger();
		Get.assemblageService().getSemanticChronologyStream(TermAux.SEMANTIC_TYPE.getNid(), true).forEach(semanticC -> {
			// We don't change the state / care about the state on the semantic. We update the state on the concept.
			LatestVersion<SemanticVersion> latest = semanticC.getLatestVersion(Coordinates.Filter.DevelopmentLatestActiveOnly());
			if (latest.isPresent())
			{
				ConceptChronology cc = Get.conceptService().getConceptChronology(latest.get().getReferencedComponentNid());
				LatestVersion<ConceptVersion> cv = cc.getLatestVersion(Coordinates.Filter.DevelopmentLatestActiveOnly());
				if (cv.isPresent())
				{
					DynamicUsageDescription dud = DynamicUsageDescriptionImpl.mockOrRead(cv.get().getNid());
					testCount.getAndIncrement();
					//Spot check some random ones... but the important part of this test is that the line above doesn't fail....
					if (dud.getDynamicUsageDescriptorNid() == MetaData.CODE____SOLOR.getNid())
					{
						Assert.assertEquals(dud.getColumnInfo().length, 1);
						Assert.assertEquals(dud.getColumnInfo()[0].getColumnDataType(), DynamicDataType.STRING);
					}
					else if (dud.getDynamicUsageDescriptorNid() == MetaData.US_ENGLISH_DIALECT____SOLOR.getNid())
					{
						Assert.assertEquals(dud.getColumnInfo().length, 1);
						Assert.assertEquals(dud.getColumnInfo()[0].getColumnDataType(), DynamicDataType.NID);
					}
					else if (dud.getDynamicUsageDescriptorNid() == MetaData.ENGLISH_LANGUAGE____SOLOR.getNid())
					{
						Assert.assertEquals(dud.getColumnInfo().length, 4);
						Assert.assertEquals(dud.getColumnInfo()[0].getColumnDataType(), DynamicDataType.STRING);
						Assert.assertEquals(dud.getColumnInfo()[1].getColumnDataType(), DynamicDataType.NID);
						Assert.assertEquals(dud.getColumnInfo()[2].getColumnDataType(), DynamicDataType.NID);
						Assert.assertEquals(dud.getColumnInfo()[3].getColumnDataType(), DynamicDataType.NID);
					}
				}
			}
		});

		Assert.assertTrue(testCount.get() > 3);
	}
}
