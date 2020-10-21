/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;

/**
 * @author a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */

@Test
public class TSBugDemo
{
	@Test
	public void bugTestOne() throws InterruptedException, ExecutionException, IOException
	{
		try
		{
			File db = new File("target/TSBugDemoDB");
			FileUtils.deleteDirectory(db);
			db.mkdirs();
			Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
			Get.configurationService().setDataStoreFolderPath(db.toPath());
			LookupService.startupIsaac();

			Transaction transaction = Get.commitService().newTransaction(Optional.of("TSBugDemoDB bugTestOne"), ChangeCheckerMode.INACTIVE);
			// Create a concept with two parents.
			ConverterUUID converterUUID = new ConverterUUID(UUID.randomUUID(), true);
			DirectWriteHelper dwh = new DirectWriteHelper(transaction, TermAux.USER.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.DEVELOPMENT_PATH.getNid(), converterUUID, 
					"hi", false);
	
			UUID concept = dwh.makeConcept(converterUUID.createNamespaceUUIDFromString("hi"), Status.ACTIVE, System.currentTimeMillis());
			
			UUID parentGraph = dwh.makeParentGraph(concept, Arrays.asList(new UUID[] {
				MetaData.ACCEPTABLE____SOLOR.getPrimordialUuid(), 
				MetaData.ACTIVE_ONLY_DESCRIPTION_LUCENE_MATCH____QUERY_CLAUSE.getPrimordialUuid(), 
				MetaData.ACTIVE_ONLY_DESCRIPTION_REGEX_MATCH____QUERY_CLAUSE.getPrimordialUuid()}),
			 Status.ACTIVE, System.currentTimeMillis());
			
			dwh.processTaxonomyUpdates();
			
			TaxonomySnapshot tss = Get.taxonomyService().getSnapshot(Coordinates.Manifold.DevelopmentStatedRegularNameSort());
			Assert.assertEquals(tss.getTaxonomyParentConceptNids(Get.identifierService().getNidForUuids(concept)).length, 3);
			
			byte[][] data = ((LogicGraphVersion)Get.assemblageService().getSemanticChronology(Get.identifierService().getNidForUuids(parentGraph))
					.getVersionList().get(0)).getGraphData();
			
			//retire the 3-parent graph
			MutableLogicGraphVersion v = Get.assemblageService().getSemanticChronology(Get.identifierService().getNidForUuids(parentGraph))
					.createMutableVersion(Coordinates.Manifold.DevelopmentStatedRegularNameSort().getWriteCoordinate(transaction, null, Status.INACTIVE));
			v.setGraphData(data);
			transaction.commit("test commit").get();

			//TODO broken:
	//		Assert.assertEquals(tss.getTaxonomyParentConceptNids(Get.identifierService().getNidForUuids(concept)).length, 0);
			
			Get.taxonomyService().notifyTaxonomyListenersToRefresh();
			tss = Get.taxonomyService().getSnapshot(Coordinates.Manifold.DevelopmentStatedRegularNameSort());
			
			//TODO still broken after forced cache clear, and regen of TSS:
	//		Assert.assertEquals(tss.getTaxonomyParentConceptNids(Get.identifierService().getNidForUuids(concept)).length, 0);

			transaction = Get.commitService().newTransaction(Optional.of("TSBugDemoDB bugTestOne second"), ChangeCheckerMode.INACTIVE);
			//Make a new stated parent graph with only 2 parents
			dwh.makeParentGraph(concept, Arrays.asList(new UUID[] {
					MetaData.ACTIVE_ONLY_DESCRIPTION_LUCENE_MATCH____QUERY_CLAUSE.getPrimordialUuid(), 
					MetaData.ACTIVE_ONLY_DESCRIPTION_REGEX_MATCH____QUERY_CLAUSE.getPrimordialUuid()}),
				 Status.ACTIVE, System.currentTimeMillis());
				
			dwh.processTaxonomyUpdates();
	//		TODO KEC all three of these commented asserts, I would expect to pass, but they don't....
	//		Assert.assertEquals(tss.getTaxonomyParentConceptNids(Get.identifierService().getNidForUuids(concept)).length, 2);
		}
		finally
		{
			LookupService.shutdownIsaac();
		}
	}
}
