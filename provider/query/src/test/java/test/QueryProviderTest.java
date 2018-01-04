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
package test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.constants.Constants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.mojo.IndexTermstore;
import sh.isaac.mojo.LoadTermstore;
import sh.isaac.provider.query.lucene.LuceneIndexer;
import sh.isaac.provider.query.lucene.indexers.DescriptionIndexer;


public class QueryProviderTest
{
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
	
	LuceneIndexer li_ = null;
	
	@BeforeClass
	public void configure() throws Exception
	{
		try 
		{
			File db = new File("target/db");
			RecursiveDelete.delete(db);
			db.mkdirs();
			System.setProperty(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY, db.getCanonicalPath());
			LookupService.startupIsaac();
			LoadTermstore lt = new LoadTermstore();
			lt.setLog(new SystemStreamLog());
			lt.setibdfFilesFolder(new File("target/data/"));
			lt.execute();
			new IndexTermstore().execute();
			
			li_ = LookupService.get().getService(DescriptionIndexer.class);
			//TODO [DAN]implement some reasonable tests here on paging, etc
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public void shutdown()
	{
		LookupService.shutdownSystem();
	}
}
