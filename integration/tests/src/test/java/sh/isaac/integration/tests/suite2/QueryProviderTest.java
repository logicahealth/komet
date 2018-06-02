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
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.index.AmpRestriction;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.convert.mojo.turtle.TurtleImportMojo;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.provider.query.lucene.indexers.DescriptionIndexer;
import sh.isaac.provider.query.lucene.indexers.SemanticIndexer;

/**
 * These tests have been rewritten to test against the beverage ontology, which is included in the resources folder.
 * The tests try to avoid hit-count checking that count on metadata, since that is constantly in flux...
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
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
		Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
		LookupService.startupIsaac();

		TurtleImportMojo tim = new TurtleImportMojo(null, QueryProviderTest.class.getResourceAsStream("/turtle/bevontology-0.8.ttl"), "0.8");
		
		tim.processTurtle();
		
		di = LookupService.get().getService(DescriptionIndexer.class);
		di.forceMerge();  //Just a way to force the query readers to refresh more quickly than they would
		si = LookupService.get().getService(SemanticIndexer.class);
		si.forceMerge();
	}

	@AfterClass
	public void shutdown() {
		LOG.info("Suite 2 teardown");
		LookupService.shutdownSystem();
	}

	@Test
	public void testSizeLimits() {
		
		int expectedMaxHits = 13;
		
		Assert.assertEquals(di.query("soju").size(), expectedMaxHits);
		
		for (int i = 1; i <=expectedMaxHits; i++)
		{
			Assert.assertEquals(di.query("soju", i).size(), i);
		}
		Assert.assertEquals(di.query("soju", 0).size(), 1);
		Assert.assertEquals(di.query("soju", null).size(), expectedMaxHits);
		Assert.assertEquals(di.query("soju", 50).size(), expectedMaxHits);
	}
	
	@Test
	public void testSizeLimits2() {
		
		int expectedMaxHits = 158;
		
		Assert.assertEquals(di.query("bevon", Integer.MAX_VALUE).size(), expectedMaxHits);
		
		Assert.assertEquals(di.query("bevon", 0).size(), 1);
		Assert.assertEquals(di.query("bevon", -5).size(), 1);
		Assert.assertEquals(di.query("bevon", null).size(), 100);
		Assert.assertEquals(di.query("bevon", 125).size(), 125);
		Assert.assertEquals(di.query("bevon", null, null, 1, 125, null).size(), 125);
		Assert.assertEquals(di.query("bevon", null, null, 2, 125, null).size(), expectedMaxHits - 125);
	}
	
	@Test
	public void testPaging() {
		
		int expectedMaxHits = 13;
		
		Assert.assertEquals(di.query("soju", null, null, null, null, null).size(), expectedMaxHits);
		
		final List<SearchResult> allResults = di.query("soju", null, null, null, null, null);
		
		List<SearchResult> paged = new ArrayList<>(expectedMaxHits);
		
		//Get in two pages
		paged.addAll(di.query("soju", null, null, 1, 10, null));
		paged.addAll(di.query("soju", null, null, 2, 10, null));
		
		Assert.assertTrue(CollectionUtils.containsAll(allResults, paged));
		Assert.assertTrue(CollectionUtils.containsAll(paged, allResults));
		

		//Try with page one twice
		paged = new ArrayList<>(expectedMaxHits);
		paged.addAll(di.query("soju", null, null, 1, 10, null));
		paged.addAll(di.query("soju", null, null, 1, 10, null));
		
		Assert.assertTrue(CollectionUtils.containsAll(allResults, paged));
		Assert.assertFalse(CollectionUtils.containsAll(paged, allResults));
		
		//One page at a time...
		paged = new ArrayList<>(expectedMaxHits);
		
		paged.addAll(di.query("soju", null, null, 1, 1, null));
		paged.addAll(di.query("soju", null, null, 2, 1, null));
		paged.addAll(di.query("soju", null, null, 3, 1, null));
		paged.addAll(di.query("soju", null, null, 4, 1, null));
		paged.addAll(di.query("soju", null, null, 5, 1, null));
		paged.addAll(di.query("soju", null, null, 6, 1, null));
		paged.addAll(di.query("soju", null, null, 7, 1, null));
		paged.addAll(di.query("soju", null, null, 8, 1, null));
		paged.addAll(di.query("soju", null, null, 9, 1, null));
		paged.addAll(di.query("soju", null, null, 10, 1, null));
		paged.addAll(di.query("soju", null, null, 11, 1, null));
		paged.addAll(di.query("soju", null, null, 12, 1, null));
		paged.addAll(di.query("soju", null, null, 13, 1, null));
		
		Assert.assertTrue(CollectionUtils.containsAll(allResults, paged));
		Assert.assertTrue(CollectionUtils.containsAll(paged, allResults));
	}
	
	@Test
	public void testReversePaging() {
		
		int expectedMaxHits = 4;
		
		Assert.assertEquals(di.query("distilled AND soju", null, null, null, null, null).size(), expectedMaxHits);
		
		final List<SearchResult> allResults = di.query("distilled AND soju", null, null, null, null, null);
		
		List<SearchResult> paged = new ArrayList<>(expectedMaxHits);
		
//		printResults(allResults);
		
		//One page at a time...
		paged = new ArrayList<>(expectedMaxHits);
		
		paged.addAll(di.query("distilled AND soju", null, null, 4, 1, null));
		paged.addAll(di.query("distilled AND soju", null, null, 3, 1, null));
		paged.addAll(di.query("distilled AND soju", null, null, 2, 1, null));
		paged.addAll(di.query("distilled AND soju", null, null, 1, 1, null));
				
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
		Assert.assertEquals(di.query("mash", null).size(), 4);

		Assert.assertEquals(di.query("mash AND cereal", null).size(), 1);
		Assert.assertEquals(di.query("bevon AND metadata AND (ISAAC)", null).size(), 1);
		Assert.assertEquals(di.query("bevon AND metadata AND \\(ISAAC\\)", null).size(), 1);
		Assert.assertEquals(di.query("metadata AND \"Beverage Ontology\" AND (ISAAC)", null).size(), 1);
		
		
		
		Assert.assertEquals(di.query("bevon AND metadata NOT \\(ISAAC\\)", null).size(), 1);
		//This query won't work as expected, because the way we are searching, the white space analyzer keeps the text as ... "(SOLOR)" so "NOT SOLOR"
		//matches on the whitespace analyzed field, as it doesn't contain the token "SOLOR".
		//Assert.assertEquals(di.query("bevon AND metadata NOT ISAAC", null).size(), 1);
		Assert.assertEquals(di.query("bevon AND metadata NOT (ISAAC)", null).size(), 1);

		Assert.assertEquals(di.query("bevon OR (ISAAC)", null).size(), 100);
		Assert.assertEquals(di.query("bevon ISAAC", null).size(), 100);
		Assert.assertEquals(di.query("bevon (ISAAC)", null).size(), 100);
		Assert.assertEquals(di.query("bevon isaac", null).size(), 100);
	}
	
	@Test
	public void testOtherFields() {
		
		Assert.assertEquals(di.query("bevon", new int[] {MetaData.ENGLISH_LANGUAGE____SOLOR.getNid()}, null, 1, 125, null).size(), 125);
		Assert.assertEquals(di.query("bevon", new int[] {MetaData.SPANISH_LANGUAGE____SOLOR.getNid()}, null, 1, 375, null).size(), 0);
		Assert.assertEquals(di.query("fu*", new int[] {}, null, 1, 375, null).size(), 6);
		Assert.assertEquals(di.query("fu*", new int[] {MetaData.IRISH_LANGUAGE____SOLOR.getNid()}, null, 1, 375, null).size(), 1);
		Assert.assertEquals(di.query("fuisce", new int[] {MetaData.IRISH_LANGUAGE____SOLOR.getNid()}, null, 1, 375, null).size(), 1);
		
		Assert.assertEquals(di.query("bevon", null, AmpRestriction.restrictPath(NidSet.of(Arrays.asList(new Integer[] {MetaData.DEVELOPMENT_PATH____SOLOR.getNid()}))), 
				null, 10, null).size(), 10);
		Assert.assertEquals(di.query("bevon", null, AmpRestriction.restrictPath(NidSet.of(Arrays.asList(new Integer[] {MetaData.MASTER_PATH____SOLOR.getNid()}))), 
				null, 10, null).size(), 0);
		
		Assert.assertEquals(di.query("bevon", null, AmpRestriction.restrictModule(NidSet.of(Arrays.asList(new Integer[] 
				{Get.identifierService().getNidForUuids(UUID.fromString("ef56f36a-9b3a-54e7-9afd-c48b36f4c5e3"))}))),  //UUID for bevon module 0.8
				null, 10, null).size(), 10);
		Assert.assertEquals(di.query("bevon", null, AmpRestriction.restrictModule(NidSet.of(Arrays.asList(new Integer[] {MetaData.ICD10_MODULES____SOLOR.getNid()}))), 
				null, 10, null).size(), 0);
		
		int userNid = Get.assemblageService().getSemanticChronology(di.query("jayg.me").get(0).getNid()).getReferencedComponentNid();
		
		Assert.assertEquals(di.query("whiskey", null, AmpRestriction.restrictAuthor(NidSet.of(Arrays.asList(new Integer[] {userNid}))), 
				null, 13, null).size(), 13);
		Assert.assertEquals(di.query("bevon", null, AmpRestriction.restrictAuthor(NidSet.of(Arrays.asList(new Integer[] {MetaData.KEITH_EUGENE_CAMPBELL____SOLOR.getNid()}))), 
				null, 13, null).size(), 0);
	}
	
	@Test
	public void testPredicate() {
		
		//no predicate
		Assert.assertEquals(di.query("rdfs.co", false, null, null, null, 1, Integer.MAX_VALUE, null).size(), 141);
		
		//no fail predicate
		Assert.assertEquals(di.query("rdfs.co", false, null, (nid -> true), null, 1, Integer.MAX_VALUE, null).size(), 141);
		
		//no pass predicate
		Assert.assertEquals(di.query("rdfs.co", false, null, (nid -> false), null, 1, Integer.MAX_VALUE, null).size(), 0);
		
		//predicate that only returns grain terms
		Assert.assertEquals(di.query("whiskey", false, null, nid -> {
				if (((DescriptionVersion)Get.assemblageService().getSemanticChronology(nid).getLatestVersion(StampCoordinates.getDevelopmentLatest()).get())
						.getText().contains("grain")) {
					return true;
				}
				return false;
			}, null, 1, Integer.MAX_VALUE, null).size(), 2);
		
		ArrayList<String> temp = new ArrayList<>();
		for (SearchResult x : di.query("whiskey", false, null, nid -> {
			if (((DescriptionVersion)Get.assemblageService().getSemanticChronology(nid).getLatestVersion(StampCoordinates.getDevelopmentLatest()).get())
					.getText().contains("grain")) {
				return true;
			}
			return false;
		}, null, 1, 20, null))
		{
			temp.add(((DescriptionVersion)Get.assemblageService().getSemanticChronology(x.getNid()).getLatestVersion(StampCoordinates.getDevelopmentLatest()).get())
						.getText());
//			printResults(Arrays.asList(new SearchResult[] {x}));
		}
		
		Assert.assertEquals(temp.size(), 2);
		
		boolean found1 = false;
		boolean found2 = false;
		for (String s : temp)
		{
			if (s.contains("fermented grain mash."))
			{
				found1 = true;
			}
			if (s.contains("a fermented mash of cereal grain"))
			{
				found2 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
	}
	
	@Test
	public void testPrefixWithMergeOnConcepts() {
		
		Assert.assertEquals(di.query("whis", true, null, null, 1, 125, null).size(), 53);
		
		Assert.assertEquals(di.mergeResultsOnConcept(di.query("whis", true, null, null, 1, 125, null)).size(), 25);
	}
	
	private void printResults(List<SearchResult> result)
	{
		for (SearchResult sr : result)
		{
			System.out.println(Get.assemblageService().getSemanticChronology(sr.getNid()).getLatestVersion(StampCoordinates.getDevelopmentLatest()).get().toString());
		}
	}
	

}
