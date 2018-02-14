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
package sh.isaac.integration.tests.suite1;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.Assert;
import org.testng.annotations.Test;
import sh.isaac.MetaData;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.utility.Frills;

/**
 * @author a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@HK2("integration")
@Test(suiteName = "suite1")
public class BugDemo
{
	private static final Logger LOG = LogManager.getLogger();

	@Test(groups = { "bugs" }, dependsOnGroups = { "load" })
	public void bugTestOne() throws InterruptedException, ExecutionException
	{
		// Read descriptions on a concept:
		// Attempt to read back the description.
		Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblages(MetaData.ACTION_PURPOSE____SOLOR.getNid(),
				new HashSet<>(Arrays.asList(new Integer[] { MetaData.ENGLISH_LANGUAGE____SOLOR.getNid() }))).forEach(descriptionChronology -> {
					// read back nested semantics on each one.
					NidSet semanticNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblages(descriptionChronology.getNid(), null);

					// Try to read each semantic...
					for (int nestedNid : semanticNids.asArray())
					{
						// We fail here, trying to read the nested semantic that describes the extended description type.
						Assert.assertNotNull(Get.assemblageService().getSemanticChronology(nestedNid));
					}
				});

		// Add a description to an arbitrary concept (MetaData.ACTION_STATEMENT____SOLOR.getNid()) with an INVALID extended desription type.
		SemanticBuilderService<? extends SemanticChronology> semanticBuilderService = Get.semanticBuilderService();
		SemanticBuilder<? extends SemanticChronology> descriptionSemanticBuilder = semanticBuilderService.getDescriptionBuilder(
				MetaData.DESCRIPTION_CASE_SENSITIVE____SOLOR.getNid(), MetaData.ENGLISH_LANGUAGE____SOLOR.getNid(), MetaData.REGULAR_NAME____SOLOR.getNid(),
				"foo", MetaData.ACTION_PURPOSE____SOLOR.getNid());

		// add an extended type (which is a nested semantic on the description which references the concept created above) - this is added in the same
		// builder. Note, the extended type here is invalid, and will fail a validator.
		descriptionSemanticBuilder.addSemantic(
				Get.semanticBuilderService().getDynamicBuilder(descriptionSemanticBuilder, DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getNid(),
						new DynamicData[] { new DynamicUUIDImpl(Get.identifierService().getUuidPrimordialForNid(MetaData.AND____SOLOR.getAssemblageNid())) }));

		// build the description and the extended type
		try
		{
			descriptionSemanticBuilder.build(Get.configurationService().getDefaultEditCoordinate(), ChangeCheckerMode.ACTIVE).get();
			Assert.fail("build worked when it shouldn't have");
		}
		catch (Exception e)
		{
			// expected
		}

		// This will now fail, due to traces left behind by the attempted new description create.
		Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblages(MetaData.ACTION_PURPOSE____SOLOR.getNid(),
				new HashSet<>(Arrays.asList(new Integer[] { MetaData.ENGLISH_LANGUAGE____SOLOR.getNid() }))).forEach(descriptionChronology -> {
					// read back nested semantics on each one.
					NidSet semanticNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblages(descriptionChronology.getNid(), null);

					// Try to read each semantic...
					for (int nestedNid : semanticNids.asArray())
					{
						// We fail here, trying to read the nested semantic that describes the extended description type.
						Assert.assertNotNull(Get.assemblageService().getSemanticChronology(nestedNid));
					}
				});

	}

	@Test(groups = { "bugs" }, dependsOnGroups = { "load" })
	public void exerciseStuff() throws InterruptedException, ExecutionException
	{
		// Create a simple concept to act as an "extended description type"
		ConceptBuilder cb = Get.conceptBuilderService().getDefaultConceptBuilder("nothing", null, null, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
		LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();
		NecessarySet(And(ConceptAssertion(MetaData.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getNid(), defBuilder)));
		LogicalExpression parentDef = defBuilder.build();
		cb.setLogicalExpression(parentDef);

		cb.build(Get.configurationService().getDefaultEditCoordinate(), ChangeCheckerMode.ACTIVE);

		Optional<CommitRecord> cr = Get.commitService().commit(Get.configurationService().getDefaultEditCoordinate(), "created extended type concept").get();

		if (!cr.isPresent())
		{
			Assert.fail("commit failed");
		}

		// Add a description to an arbitrary concept (MetaData.ACTION_STATEMENT____SOLOR.getNid())
		SemanticBuilderService<? extends SemanticChronology> semanticBuilderService = Get.semanticBuilderService();
		SemanticBuilder<? extends SemanticChronology> descriptionSemanticBuilder = semanticBuilderService.getDescriptionBuilder(
				MetaData.DESCRIPTION_CASE_SENSITIVE____SOLOR.getNid(), MetaData.ENGLISH_LANGUAGE____SOLOR.getNid(), MetaData.REGULAR_NAME____SOLOR.getNid(),
				"foo", MetaData.ACTION_PURPOSE____SOLOR.getNid());

		// add an extended type (which is a nested semantic on the description which references the concept created above) - this is added in the same
		// builder.
		descriptionSemanticBuilder.addSemantic(
				Get.semanticBuilderService().getDynamicBuilder(descriptionSemanticBuilder, DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getNid(),
						new DynamicData[] { new DynamicUUIDImpl(Get.identifierService().getUuidPrimordialForNid(cb.getNid())) }));

		// build the description and the extended type
		SemanticChronology newDescription = descriptionSemanticBuilder.build(Get.configurationService().getDefaultEditCoordinate(), ChangeCheckerMode.ACTIVE)
				.get();

		// commit them.
		cr = Get.commitService().commit(Get.configurationService().getDefaultEditCoordinate(),
				"creating new description semantic: NID=" + newDescription.getNid() + ", text=foo").get();

		if (!cr.isPresent())
		{
			Assert.fail("commit failed");
		}

		LOG.debug("commit {}", cr.get());

		// Attempt to read back the description.
		Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblages(MetaData.ACTION_PURPOSE____SOLOR.getNid(),
				new HashSet<>(Arrays.asList(new Integer[] { MetaData.ENGLISH_LANGUAGE____SOLOR.getNid() }))).forEach(descriptionChronology -> {
					// read back nested semantics on each one.
					NidSet semanticNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblages(descriptionChronology.getNid(), null);

					// Try to read each semantic...
					for (int nestedNid : semanticNids.asArray())
					{
						// We fail here, trying to read the nested semantic that describes the extended description type.
						Assert.assertNotNull(Get.assemblageService().getSemanticChronology(nestedNid));
					}
				});
	}

	@Test(groups = { "bugs" }, dependsOnGroups = { "load" })
	public void logicGraphMergeBug() throws InterruptedException, ExecutionException
	{
		SemanticChronology lg = Frills.getLogicGraphChronology(MetaData.ACTION_PURPOSE____SOLOR.getNid(), true).get();

		MutableLogicGraphVersion mlg = lg.createMutableVersion(Status.ACTIVE, Get.configurationService().getDefaultEditCoordinate());

		LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();
		NecessarySet(And(new Assertion[] { ConceptAssertion(MetaData.ACTION_PURPOSE____SOLOR.getNid(), defBuilder),
				ConceptAssertion(MetaData.ACTIVE_ONLY_DESCRIPTION_LUCENE_MATCH____QUERY_CLAUSE.getNid(), defBuilder) }));
		LogicalExpression parentDef = defBuilder.build();

		mlg.setGraphData(parentDef.getData(DataTarget.INTERNAL));

		Get.commitService().addUncommittedNoChecks(lg).get();
		
		Get.commitService().addUncommitted(lg).get();
		
		Frills.commitCheck(Get.commitService().commit(Get.configurationService().getDefaultEditCoordinate(), "test"));
	}
}
