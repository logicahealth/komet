/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.integration.tests.suite2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.index.AmpRestriction;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.mojo.LoadTermstore;
import sh.isaac.provider.query.lucene.indexers.DescriptionIndexer;
import sh.isaac.provider.query.lucene.indexers.SemanticIndexer;

@Test(suiteName = "suite2")
public class QueryProviderTest {

	DescriptionIndexer di = null;
	SemanticIndexer si = null;

	private static final Logger LOG = LogManager.getLogger();

	@BeforeClass
	public void configure() throws Exception {
		LOG.info("Suite 2 setup");
		File db = new File("target/suite2");
		RecursiveDelete.delete(db);
		db.mkdirs();
		System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, db.getCanonicalPath());
		LookupService.startupIsaac();
		LoadTermstore lt = new LoadTermstore();
		lt.setLog(new SystemStreamLog());
		lt.setibdfFilesFolder(new File("target/data/"));
		lt.execute();

		di = LookupService.get().getService(DescriptionIndexer.class);
		si = LookupService.get().getService(SemanticIndexer.class);
	}

	@AfterClass
	public void shutdown() {
		LOG.info("Suite 2 teardown");
		LookupService.shutdownSystem();
	}

	@Test
	public void testSizeLimits() {
		
		int expectedMaxHits = 15;  //May need to change this when tweaking metadata... until we come up with a proper "testing" terminology to use for things like this.
		
		Assert.assertEquals(di.query("h*").size(), expectedMaxHits);
		
		for (int i = 1; i <=1; i++)
		{
			Assert.assertEquals(di.query("h*", i).size(), i);
		}
		Assert.assertEquals(di.query("h*", 0).size(), 1);
		Assert.assertEquals(di.query("h*", null).size(), expectedMaxHits);
		Assert.assertEquals(di.query("h*", 50).size(), expectedMaxHits);
	}
	
	@Test
	public void testSizeLimits2() {
		
		int expectedMaxHits = 624;  //May need to change this when tweaking metadata... until we come up with a proper "testing" terminology to use for things like this.
		
		Assert.assertEquals(di.query("s*", Integer.MAX_VALUE).size(), expectedMaxHits);
		
		Assert.assertEquals(di.query("s*", 0).size(), 1);
		Assert.assertEquals(di.query("s*", -5).size(), 1);
		Assert.assertEquals(di.query("s*", null).size(), 100);
		Assert.assertEquals(di.query("s*", 375).size(), 375);
		Assert.assertEquals(di.query("s*", null, null, 1, 375, null).size(), 375);
		Assert.assertEquals(di.query("s*", null, null, 2, 375, null).size(), expectedMaxHits - 375);
	}
	
	@Test
	public void testPaging() {
		
		int expectedMaxHits = 15;  //May need to change this when tweaking metadata... until we come up with a proper "testing" terminology to use for things like this.
		
		Assert.assertEquals(di.query("h*", null, null, null, null, null).size(), expectedMaxHits);
		
		final List<SearchResult> allResults = di.query("h*", null, null, null, null, null);
		
		List<SearchResult> paged = new ArrayList<>(expectedMaxHits);
		
		//Get in two pages
		paged.addAll(di.query("h*", null, null, 1, 10, null));
		paged.addAll(di.query("h*", null, null, 2, 10, null));
		
		Assert.assertTrue(CollectionUtils.containsAll(allResults, paged));
		Assert.assertTrue(CollectionUtils.containsAll(paged, allResults));
		

		//Try with page one twice
		paged = new ArrayList<>(expectedMaxHits);
		paged.addAll(di.query("h*", null, null, 1, 10, null));
		paged.addAll(di.query("h*", null, null, 1, 10, null));
		
		Assert.assertTrue(CollectionUtils.containsAll(allResults, paged));
		Assert.assertFalse(CollectionUtils.containsAll(paged, allResults));
		
		//One page at a time...
		paged = new ArrayList<>(expectedMaxHits);
		
		paged.addAll(di.query("h*", null, null, 1, 1, null));
		paged.addAll(di.query("h*", null, null, 2, 1, null));
		paged.addAll(di.query("h*", null, null, 3, 1, null));
		paged.addAll(di.query("h*", null, null, 4, 1, null));
		paged.addAll(di.query("h*", null, null, 5, 1, null));
		paged.addAll(di.query("h*", null, null, 6, 1, null));
		paged.addAll(di.query("h*", null, null, 7, 1, null));
		paged.addAll(di.query("h*", null, null, 8, 1, null));
		paged.addAll(di.query("h*", null, null, 9, 1, null));
		paged.addAll(di.query("h*", null, null, 10, 1, null));
		paged.addAll(di.query("h*", null, null, 11, 1, null));
		paged.addAll(di.query("h*", null, null, 12, 1, null));
		paged.addAll(di.query("h*", null, null, 13, 1, null));
		paged.addAll(di.query("h*", null, null, 14, 1, null));
		paged.addAll(di.query("h*", null, null, 15, 1, null));
		paged.addAll(di.query("h*", null, null, 16, 1, null));
		paged.addAll(di.query("h*", null, null, 17, 1, null));
		
		Assert.assertTrue(CollectionUtils.containsAll(allResults, paged));
		Assert.assertTrue(CollectionUtils.containsAll(paged, allResults));
	}
	
	@Test
	public void testReversePaging() {
		
		int expectedMaxHits = 4;  //May need to change this when tweaking metadata... until we come up with a proper "testing" terminology to use for things like this.
		
		Assert.assertEquals(di.query("RF2 AND nid", null, null, null, null, null).size(), expectedMaxHits);
		
		final List<SearchResult> allResults = di.query("RF2 AND nid", null, null, null, null, null);
		
		List<SearchResult> paged = new ArrayList<>(expectedMaxHits);
		
		printResults(allResults);
		
		//One page at a time...
		paged = new ArrayList<>(expectedMaxHits);
		
		paged.addAll(di.query("RF2 AND nid", null, null, 8, 1, null));
		paged.addAll(di.query("RF2 AND nid", null, null, 7, 1, null));
		paged.addAll(di.query("RF2 AND nid", null, null, 6, 1, null));
		paged.addAll(di.query("RF2 AND nid", null, null, 5, 1, null));
		paged.addAll(di.query("RF2 AND nid", null, null, 4, 1, null));
		paged.addAll(di.query("RF2 AND nid", null, null, 3, 1, null));
		paged.addAll(di.query("RF2 AND nid", null, null, 2, 1, null));
		paged.addAll(di.query("RF2 AND nid", null, null, 1, 1, null));
				
		Assert.assertTrue(CollectionUtils.containsAll(allResults, paged));
		Assert.assertTrue(CollectionUtils.containsAll(paged, allResults));
		
		//Make sure the results came back in reverse score order
		int i = 0;
		int ii = 1;
		while (ii < expectedMaxHits)
		{
			Assert.assertTrue(paged.get(i++).getScore() <= paged.get(ii++).getScore());
		}
	}
	
	@Test
	public void testQueries() {
		Assert.assertEquals(di.query("legacy", null).size(), 2);

		Assert.assertEquals(di.query("legacy AND (SOLOR)", null).size(), 1);
		Assert.assertEquals(di.query("legacy AND SOLOR", null).size(), 1);
		Assert.assertEquals(di.query("legacy AND \\(SOLOR\\)", null).size(), 1);
		Assert.assertEquals(di.query("\"RF2 legacy\" AND (SOLOR)", null).size(), 1);
		
		
		
		Assert.assertEquals(di.query("legacy NOT \\(SOLOR\\)", null).size(), 1);
		//This query won't work as expected, because the way we are searching, the white space analyzer keeps the text as ... "(SOLOR)" so "NOT SOLOR"
		//matches on the whitespace analyzed field, as it doesn't contain the token "SOLOR".
		//Assert.assertEquals(di.query("legacy NOT SOLOR", null).size(), 1);
		Assert.assertEquals(di.query("legacy NOT (SOLOR)", null).size(), 1);

		Assert.assertEquals(di.query("legacy OR (SOLOR)", null).size(), 100);
		Assert.assertEquals(di.query("legacy SOLOR", null).size(), 100);
		Assert.assertEquals(di.query("legacy (SOLOR)", null).size(), 100);
		Assert.assertEquals(di.query("legacy solor", null).size(), 100);
	}
	
	@Test
	public void testOtherFields() {
		
		Assert.assertEquals(di.query("s*", new int[] {MetaData.ENGLISH_LANGUAGE____SOLOR.getNid()}, null, 1, 375, null).size(), 375);
		Assert.assertEquals(di.query("s*", new int[] {MetaData.SPANISH_LANGUAGE____SOLOR.getNid()}, null, 1, 375, null).size(), 0);
		
		Assert.assertEquals(di.query("s*", null, AmpRestriction.restrictPath(NidSet.of(Arrays.asList(new Integer[] {MetaData.DEVELOPMENT_PATH____SOLOR.getNid()}))), 
				null, 10, null).size(), 10);
		Assert.assertEquals(di.query("s*", null, AmpRestriction.restrictPath(NidSet.of(Arrays.asList(new Integer[] {MetaData.MASTER_PATH____SOLOR.getNid()}))), 
				null, 10, null).size(), 0);
		
		Assert.assertEquals(di.query("s*", null, AmpRestriction.restrictModule(NidSet.of(Arrays.asList(new Integer[] {MetaData.SOLOR_MODULE____SOLOR.getNid()}))), 
				null, 10, null).size(), 10);
		Assert.assertEquals(di.query("s*", null, AmpRestriction.restrictModule(NidSet.of(Arrays.asList(new Integer[] {MetaData.ICD10_MODULES____SOLOR.getNid()}))), 
				null, 10, null).size(), 0);
		
		Assert.assertEquals(di.query("s*", null, AmpRestriction.restrictAuthor(NidSet.of(Arrays.asList(new Integer[] {MetaData.USER____SOLOR.getNid()}))), 
				null, 10, null).size(), 10);
		Assert.assertEquals(di.query("s*", null, AmpRestriction.restrictAuthor(NidSet.of(Arrays.asList(new Integer[] {MetaData.KEITH_EUGENE_CAMPBELL____SOLOR.getNid()}))), 
				null, 10, null).size(), 0);
	}
	
	@Test
	public void testPredicate() {
		
		//no predicate
		Assert.assertEquals(di.query("so*", false, null, null, null, 1, Integer.MAX_VALUE, null).size(), 487);
		
		//no fail predicate
		Assert.assertEquals(di.query("so*", false, null, (nid -> true), null, 1, Integer.MAX_VALUE, null).size(), 487);
		
		//no pass predicate
		Assert.assertEquals(di.query("so*", false, null, (nid -> false), null, 1, Integer.MAX_VALUE, null).size(), 0);
		
		//predicate that only returns SOPT terms
		Assert.assertEquals(di.query("so*", false, null, nid -> {
				if (((DescriptionVersion)Get.assemblageService().getSemanticChronology(nid).getLatestVersion(StampCoordinates.getDevelopmentLatest()).get())
						.getText().contains("SOPT")) {
					return true;
				}
				return false;
			}, null, 1, Integer.MAX_VALUE, null).size(), 3);
		
		ArrayList<String> temp = new ArrayList<>();
		for (SearchResult x : di.query("so*", false, null, nid -> {
			if (((DescriptionVersion)Get.assemblageService().getSemanticChronology(nid).getLatestVersion(StampCoordinates.getDevelopmentLatest()).get())
					.getText().contains("SOPT")) {
				return true;
			}
			return false;
		}, null, 1, 5, null))
		{
			temp.add(((DescriptionVersion)Get.assemblageService().getSemanticChronology(x.getNid()).getLatestVersion(StampCoordinates.getDevelopmentLatest()).get())
						.getText());
		}
		
		Assert.assertTrue(temp.contains("SOPT"));
		Assert.assertTrue(temp.contains("SOPT modules"));
		Assert.assertTrue(temp.contains("SOPT modules (SOLOR)"));
	}
	
	private void printResults(List<SearchResult> result)
	{
		for (SearchResult sr : result)
		{
			System.out.println(Get.assemblageService().getSemanticChronology(sr.getNid()).getLatestVersion(StampCoordinates.getDevelopmentLatest()).get().toString());
		}
	}
	

}
