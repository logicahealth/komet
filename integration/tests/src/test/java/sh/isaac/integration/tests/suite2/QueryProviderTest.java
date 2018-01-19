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
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.Query;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import sh.isaac.api.LookupService;
import sh.isaac.api.constants.Constants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.komet.preferences.PreferencesProvider;
import sh.isaac.mojo.IndexTermstore;
import sh.isaac.mojo.LoadTermstore;
import sh.isaac.provider.query.lucene.indexers.DescriptionIndexer;
import sh.isaac.provider.query.lucene.indexers.SemanticIndexer;

@Test(suiteName = "suite2")
public class QueryProviderTest {
	String query_ = "dynamic*";
	String field_ = "_string_content_";
	boolean prefixSearch_ = true;
	boolean metadataOnly_ = false;

	StampCoordinate stamp1_;
	StampCoordinate stamp2_;
	StampCoordinate stamp3_;
	StampCoordinate stamp4_;

	Query q_base_ = null;
	Query q_stamp0_ = null;
	Query q_stamp1_ = null;
	Query q_stamp2_ = null;
	Query q_stamp3_ = null;
	Query q_stamp4_ = null;

	DescriptionIndexer di = null;
	SemanticIndexer si = null;

	private static final Logger LOG = LogManager.getLogger();

	@BeforeClass
	public void configure() throws Exception {
		LOG.info("Suite 2 setup");
		File db = new File("target/suite2");
		RecursiveDelete.delete(db);
		db.mkdirs();
		PreferencesProvider.clearSetProperties();
		System.setProperty(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY, db.getCanonicalPath());
		LookupService.startupIsaac();
		LoadTermstore lt = new LoadTermstore();
		lt.setLog(new SystemStreamLog());
		lt.setibdfFilesFolder(new File("target/data/"));
		lt.execute();
		new IndexTermstore().execute();

		di = LookupService.get().getService(DescriptionIndexer.class);
		si = LookupService.get().getService(SemanticIndexer.class);
		// TODO [DAN 1] implement some reasonable tests here on paging, etc

	}

	@AfterClass
	public void shutdown() {
		LOG.info("Suite 2 teardown");
		LookupService.shutdownSystem();
	}

	@Test
	public void testSizeLimits() {
		
		int expectedMaxHits = 10;  //May need to change this when tweaking metadata... until we come up with a proper "testing" terminology to use for things like this.
		
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
	public void testPaging() {
		
		int expectedMaxHits = 10;  //May need to change this when tweaking metadata... until we come up with a proper "testing" terminology to use for things like this.
		
		Assert.assertEquals(di.query("h*", null, null, null, null, null).size(), expectedMaxHits);
		
		List<SearchResult> allResults = di.query("h*", null, null, null, null, null);
		
		List<SearchResult> paged = new ArrayList<>(expectedMaxHits);
		
		//Get in two pages
		paged.addAll(di.query("h*", null, null, 1, 5, null));
		paged.addAll(di.query("h*", null, null, 2, 5, null));
		
		Assert.assertTrue(CollectionUtils.containsAll(allResults, paged));
		Assert.assertTrue(CollectionUtils.containsAll(paged, allResults));
		

		//Try with page one twice
		paged = new ArrayList<>(expectedMaxHits);
		paged.addAll(di.query("h*", null, null, 1, 5, null));
		paged.addAll(di.query("h*", null, null, 1, 5, null));
		
		Assert.assertTrue(CollectionUtils.containsAll(allResults, paged));
		Assert.assertFalse(CollectionUtils.containsAll(paged, allResults));
	}
}
