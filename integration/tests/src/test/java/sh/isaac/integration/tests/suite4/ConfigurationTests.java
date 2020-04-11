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
package sh.isaac.integration.tests.suite4;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sh.isaac.api.Get;
import sh.isaac.api.GlobalDatastoreConfiguration;
import sh.isaac.api.LookupService;
import sh.isaac.api.UserConfiguration;
import sh.isaac.api.UserConfiguration.ConfigurationStore;
import sh.isaac.api.UserConfigurationInternalImpl.ConfigurationOption;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.komet.preferences.UserConfigurationPerOSProvider;
import sh.isaac.model.configuration.DefaultCoordinateProvider;

@Test(suiteName = "suite4")
public class ConfigurationTests {

	private static final Logger LOG = LogManager.getLogger();

	@BeforeClass
	public void configure() throws Exception {
		LOG.info("Suite 4 setup");
		File db = new File("target/suite4");
		RecursiveDelete.delete(db);
		//Don't overwrite "real" config
		UserConfigurationPerOSProvider.nodeName = "userConfigForTest";
		db.mkdirs();
		System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, db.getCanonicalPath());
		LookupService.startupPreferenceProvider();
		//Make sure remnants from any previous test are gone
		IsaacPreferences mainDataStore = Get.preferencesService().getUserPreferences();
		mainDataStore.node(UserConfigurationPerOSProvider.nodeName).removeNode();
		Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
		LookupService.startupIsaac();
	}

	@AfterClass
	public void shutdown() throws BackingStoreException {
		LOG.info("Suite 4 teardown");
		//cleanup
		IsaacPreferences mainDataStore = Get.preferencesService().getUserPreferences();
		mainDataStore.node(UserConfigurationPerOSProvider.nodeName).removeNode();
		LookupService.shutdownSystem();
	}

	@Test(groups="beforeUser", enabled = false)
	public void testDefaultsFromGlobal() {
		
		GlobalDatastoreConfiguration c = Get.configurationService().getGlobalDatastoreConfiguration();
		DefaultCoordinateProvider dcp = new DefaultCoordinateProvider();
		
		// at startup, everything should be the same as the defaults...
		testHelper2(c, dcp);
	}
	
	private void testHelper2(GlobalDatastoreConfiguration c, DefaultCoordinateProvider dcp)
	{
		Assert.assertEquals(dcp.getDefaultEditCoordinate().getAuthorNid(), c.getDefaultEditCoordinate().getAuthorNid());
		Assert.assertEquals(dcp.getDefaultEditCoordinate().getModuleNid(), c.getDefaultEditCoordinate().getModuleNid());
		Assert.assertEquals(dcp.getDefaultEditCoordinate().getPathNid(), c.getDefaultEditCoordinate().getPathNid());
		
		Assert.assertTrue(Arrays.equals(dcp.getDefaultLanguageCoordinate().getDescriptionTypePreferenceList(), 
				c.getDefaultLanguageCoordinate().getDescriptionTypePreferenceList()));
		
		Assert.assertTrue(Arrays.equals(dcp.getDefaultLanguageCoordinate().getDialectAssemblagePreferenceList(), 
				c.getDefaultLanguageCoordinate().getDialectAssemblagePreferenceList()));

		Assert.assertEquals(dcp.getDefaultLanguageCoordinate().getLanguageConceptNid(), c.getDefaultLanguageCoordinate().getLanguageConceptNid());
		
		
		Assert.assertEquals(dcp.getDefaultLogicCoordinate().getClassifierNid(), c.getDefaultLogicCoordinate().getClassifierNid());
		Assert.assertEquals(dcp.getDefaultLogicCoordinate().getDescriptionLogicProfileNid(), c.getDefaultLogicCoordinate().getDescriptionLogicProfileNid());
		Assert.assertEquals(dcp.getDefaultLogicCoordinate().getInferredAssemblageNid(), c.getDefaultLogicCoordinate().getInferredAssemblageNid());
		Assert.assertEquals(dcp.getDefaultLogicCoordinate().getStatedAssemblageNid(), c.getDefaultLogicCoordinate().getStatedAssemblageNid());
		
		Assert.assertEquals(dcp.getDefaultManifoldCoordinate().getPremiseType(), c.getDefaultManifoldCoordinate().getPremiseType());
	}
	
	@Test(groups="beforeUser", enabled = false)
	public void testDefaultsFromUser() {
		testHelper(new DefaultCoordinateProvider(), Get.configurationService().getUserConfiguration(null));
		testHelper(new DefaultCoordinateProvider(), Get.configurationService().getUserConfiguration(Optional.empty()));
	}
	
	@Test(groups="beforeUser", enabled = false)
	public void testSetShouldFail() {
		//With no user specified, the user profile should be falling back to defaults, and user props should be unsettable
		try 
		{
			Get.configurationService().getUserConfiguration(Optional.empty()).setEditModule(ConfigurationStore.DATABASE, 5);
			Assert.fail("Didn't fail user pref set");
		}
		catch (Exception e)
		{
			//expected
		}
		
		try 
		{
			Get.configurationService().getUserConfiguration(Optional.empty()).setEditModule(ConfigurationStore.PROFILE, 5);
			Assert.fail("Didn't fail user pref set");
		}
		catch (Exception e)
		{
			//expected
		}
	}
	
	@Test(dependsOnGroups="beforeUser", groups="defaultEdit", enabled = false)
	public void testDefaultEdit() {
		//**********************
		//Please stop changing these values - the entire point of the test is to validate that the datastore configuration saves and reads things properly, 
		//not that hardcoded values elsewhere in the code magically align with these values.....
		//If other tests are having issues with these, then the tests aren't being isolated properly.
		//**********************
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultClassifier(-1);
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultDescriptionLogicProfile(-2);
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultDescriptionTypePreferenceList(new int[] { -3 });
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultDialectAssemblagePreferenceList(new int[] { -4 });
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultInferredAssemblage(-5);
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultLanguage( -6 );
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultModule(-7);
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultPath(-8);
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultPremiseType(PremiseType.STATED);
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultStatedAssemblage(-9);
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultTime(10);
		Get.configurationService().getGlobalDatastoreConfiguration().setDefaultUser(-11);
		
		LookupService.shutdownIsaac();
		
		LookupService.startupIsaac();
		
		GlobalDatastoreConfiguration c = Get.configurationService().getGlobalDatastoreConfiguration();
		
		Assert.assertEquals(-1, c.getDefaultLogicCoordinate().getClassifierNid());
		Assert.assertEquals(-2, c.getDefaultLogicCoordinate().getDescriptionLogicProfileNid());
		Assert.assertTrue(Arrays.equals(new int[] {-3}, 
				c.getDefaultLanguageCoordinate().getDescriptionTypePreferenceList()), 
				"found " + Arrays.toString(c.getDefaultLanguageCoordinate().getDescriptionTypePreferenceList()) + "while I was expecting " +
				Arrays.toString(new int[] {-3}));
		Assert.assertTrue(Arrays.equals(new int[] {-4}, 
				c.getDefaultLanguageCoordinate().getDialectAssemblagePreferenceList()),
				"found " + Arrays.toString(c.getDefaultLanguageCoordinate().getDialectAssemblagePreferenceList()));
		Assert.assertEquals(-5, c.getDefaultLogicCoordinate().getInferredAssemblageNid());
		Assert.assertEquals(-6, c.getDefaultLanguageCoordinate().getLanguageConceptNid());
		Assert.assertEquals(-7, c.getDefaultEditCoordinate().getModuleNid());
		Assert.assertEquals(-8, c.getDefaultEditCoordinate().getPathNid());
		Assert.assertEquals(PremiseType.STATED, c.getDefaultManifoldCoordinate().getPremiseType());
		Assert.assertEquals(-9, c.getDefaultLogicCoordinate().getStatedAssemblageNid());
		Assert.assertEquals(-11, c.getDefaultEditCoordinate().getAuthorNid());
		
		//User prefs should follow:
		UserConfiguration uc = Get.configurationService().getUserConfiguration(null);
		Assert.assertEquals(-1, uc.getLogicCoordinate().getClassifierNid());
		Assert.assertEquals(-2, uc.getLogicCoordinate().getDescriptionLogicProfileNid());
		Assert.assertTrue(Arrays.equals(new int[] {-3}, 
				uc.getLanguageCoordinate().getDescriptionTypePreferenceList()));
		Assert.assertTrue(Arrays.equals(new int[] {-4}, 
				uc.getLanguageCoordinate().getDialectAssemblagePreferenceList()));
		Assert.assertEquals(-5, uc.getLogicCoordinate().getInferredAssemblageNid());
		Assert.assertEquals(-6, uc.getLanguageCoordinate().getLanguageConceptNid());
		Assert.assertEquals(-7, uc.getEditCoordinate().getModuleNid());
		Assert.assertEquals(-8, uc.getEditCoordinate().getPathNid());
		Assert.assertEquals(PremiseType.STATED, uc.getManifoldCoordinate().getPremiseType());
		Assert.assertEquals(-9, uc.getLogicCoordinate().getStatedAssemblageNid());
		Assert.assertEquals(-11, uc.getEditCoordinate().getAuthorNid());
		
		Get.configurationService().getGlobalDatastoreConfiguration().clearStoredConfiguration();
		
		// should be back to defaults
		testHelper2( Get.configurationService().getGlobalDatastoreConfiguration(), new DefaultCoordinateProvider());
		LookupService.shutdownIsaac();
		LookupService.startupIsaac();
		testHelper2( Get.configurationService().getGlobalDatastoreConfiguration(), new DefaultCoordinateProvider());
		testHelper(new DefaultCoordinateProvider(), Get.configurationService().getUserConfiguration(null));
	}
	
	private void testHelper(DefaultCoordinateProvider dcp, UserConfiguration c) {
		Assert.assertEquals(dcp.getDefaultEditCoordinate().getAuthorNid(), c.getEditCoordinate().getAuthorNid());
		Assert.assertEquals(dcp.getDefaultEditCoordinate().getModuleNid(), c.getEditCoordinate().getModuleNid());
		Assert.assertEquals(dcp.getDefaultEditCoordinate().getPathNid(), c.getEditCoordinate().getPathNid());
		
		Assert.assertTrue(Arrays.equals(dcp.getDefaultLanguageCoordinate().getDescriptionTypePreferenceList(), 
				c.getLanguageCoordinate().getDescriptionTypePreferenceList()));
		
		Assert.assertTrue(Arrays.equals(dcp.getDefaultLanguageCoordinate().getDialectAssemblagePreferenceList(), 
				c.getLanguageCoordinate().getDialectAssemblagePreferenceList()));

		Assert.assertEquals(dcp.getDefaultLanguageCoordinate().getLanguageConceptNid(), c.getLanguageCoordinate().getLanguageConceptNid());
		
		
		Assert.assertEquals(dcp.getDefaultLogicCoordinate().getClassifierNid(), c.getLogicCoordinate().getClassifierNid());
		Assert.assertEquals(dcp.getDefaultLogicCoordinate().getDescriptionLogicProfileNid(), c.getLogicCoordinate().getDescriptionLogicProfileNid());
		Assert.assertEquals(dcp.getDefaultLogicCoordinate().getInferredAssemblageNid(), c.getLogicCoordinate().getInferredAssemblageNid());
		Assert.assertEquals(dcp.getDefaultLogicCoordinate().getStatedAssemblageNid(), c.getLogicCoordinate().getStatedAssemblageNid());

		Assert.assertEquals(dcp.getDefaultManifoldCoordinate().getPremiseType(), c.getManifoldCoordinate().getPremiseType());
	}
	
	@Test(dependsOnGroups= {"beforeUser", "defaultEdit"}, groups="systemEdit", enabled = false)
	public void testUserSystemEdit() {
		
		UserConfiguration uc = Get.configurationService().getUserConfiguration(Optional.empty());
		Assert.assertFalse(uc.getUserId().isPresent());
		
		Get.configurationService().setSingleUserMode(true);
		
		uc = Get.configurationService().getUserConfiguration(Optional.empty());
		Assert.assertEquals(uc.getUserId().get(), TermAux.USER.getPrimordialUuid());
		
		uc.setClassifier(ConfigurationStore.PROFILE, TermAux.ACCEPTABLE.getNid());
		uc.setDescriptionLogicProfile(ConfigurationStore.PROFILE, TermAux.ACTIVE_QUERY_CLAUSE.getNid());
		uc.setDescriptionTypePreferenceList(ConfigurationStore.PROFILE, new int[] {TermAux.AND_NOT_QUERY_CLAUSE.getNid()});
		uc.setDialectAssemblagePreferenceList(ConfigurationStore.PROFILE, new int[] {TermAux.AND_QUERY_CLAUSE.getNid()});
		uc.setEditModule(ConfigurationStore.PROFILE, TermAux.APACHE_2_LICENSE.getNid());
		uc.setEditPath(ConfigurationStore.PROFILE, TermAux.ASSEMBLAGE.getNid());
		uc.setInferredAssemblage(ConfigurationStore.PROFILE, TermAux.ASSEMBLAGE_CONTAINS_COMPONENT_QUERY_CLAUSE.getNid());
		uc.setLanguage(ConfigurationStore.PROFILE, TermAux.ASSEMBLAGE_CONTAINS_CONCEPT_QUERY_CLAUSE.getNid());
		uc.setObject(ConfigurationStore.PROFILE, "fred", -28);
		uc.setObject(ConfigurationStore.PROFILE, "jane", new Boolean(true));
		uc.setPremiseType(ConfigurationStore.PROFILE, PremiseType.STATED);
		uc.setStatedAssemblage(ConfigurationStore.PROFILE, TermAux.ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT_QUERY_CLAUSE.getNid());
		uc.setTime(ConfigurationStore.PROFILE, 30);
		
		Assert.assertEquals(TermAux.ACCEPTABLE.getNid(), uc.getLogicCoordinate().getClassifierNid());
		Assert.assertEquals(TermAux.ACTIVE_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getDescriptionLogicProfileNid());
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.AND_NOT_QUERY_CLAUSE.getNid()}, uc.getLanguageCoordinate().getDescriptionTypePreferenceList()));
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.AND_QUERY_CLAUSE.getNid()}, uc.getLanguageCoordinate().getDialectAssemblagePreferenceList()));
		Assert.assertEquals(TermAux.APACHE_2_LICENSE.getNid(), uc.getEditCoordinate().getModuleNid());
		Assert.assertEquals(TermAux.ASSEMBLAGE.getNid(), uc.getEditCoordinate().getPathNid());
		Assert.assertEquals(TermAux.ASSEMBLAGE_CONTAINS_COMPONENT_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getInferredAssemblageNid());
		Assert.assertEquals(TermAux.ASSEMBLAGE_CONTAINS_CONCEPT_QUERY_CLAUSE.getNid(), uc.getLanguageCoordinate().getLanguageConceptNid());
		Assert.assertEquals(Integer.valueOf(-28), uc.<Integer>getObject("fred"));
		Assert.assertEquals(true, uc.<Boolean>getObject("jane").booleanValue());
		Assert.assertEquals(PremiseType.STATED, uc.getManifoldCoordinate().getPremiseType());
		Assert.assertEquals(TermAux.ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getStatedAssemblageNid());
		Assert.assertEquals(TermAux.USER.getNid(), uc.getEditCoordinate().getAuthorNid());

		//defaults not impacted
		testHelper2(Get.configurationService().getGlobalDatastoreConfiguration(), new DefaultCoordinateProvider());
		
		uc.setOption(ConfigurationStore.PROFILE, ConfigurationOption.PREMISE_TYPE, PremiseType.INFERRED);
		Assert.assertEquals(PremiseType.INFERRED, uc.getManifoldCoordinate().getPremiseType());
		
		LookupService.shutdownIsaac();
		LookupService.startupIsaac();
		Get.configurationService().setSingleUserMode(true);
		
		Assert.assertEquals(TermAux.ACCEPTABLE.getNid(), uc.getLogicCoordinate().getClassifierNid());
		Assert.assertEquals(TermAux.ACTIVE_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getDescriptionLogicProfileNid());
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.AND_NOT_QUERY_CLAUSE.getNid()}, uc.getLanguageCoordinate().getDescriptionTypePreferenceList()));
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.AND_QUERY_CLAUSE.getNid()}, uc.getLanguageCoordinate().getDialectAssemblagePreferenceList()));
		Assert.assertEquals(TermAux.APACHE_2_LICENSE.getNid(), uc.getEditCoordinate().getModuleNid());
		Assert.assertEquals(TermAux.ASSEMBLAGE.getNid(), uc.getEditCoordinate().getPathNid());
		Assert.assertEquals(TermAux.ASSEMBLAGE_CONTAINS_COMPONENT_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getInferredAssemblageNid());
		Assert.assertEquals(TermAux.ASSEMBLAGE_CONTAINS_CONCEPT_QUERY_CLAUSE.getNid(), uc.getLanguageCoordinate().getLanguageConceptNid());
		Assert.assertEquals(new Integer(-28), uc.<Integer>getObject("fred"));
		Assert.assertEquals(new Boolean(true).booleanValue(), uc.<Boolean>getObject("jane").booleanValue());
		Assert.assertEquals(PremiseType.INFERRED, uc.getManifoldCoordinate().getPremiseType());
		Assert.assertEquals(TermAux.ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getStatedAssemblageNid());
		Assert.assertEquals(TermAux.USER.getNid(), uc.getEditCoordinate().getAuthorNid());
		
		//Defaults not impacted:
		testHelper2(Get.configurationService().getGlobalDatastoreConfiguration(), new DefaultCoordinateProvider());
	}
	
	@Test(dependsOnGroups= {"beforeUser", "defaultEdit", "systemEdit"}, groups="dbEdit", enabled = false)
	public void testUserDBEdit() {
		//These edits should override the system settings
	
		UserConfiguration uc = Get.configurationService().getUserConfiguration(Optional.empty());
		Assert.assertEquals(uc.getUserId().get(), TermAux.USER.getPrimordialUuid());
		
		uc.setClassifier(ConfigurationStore.DATABASE, TermAux.XOR_QUERY_CLAUSE.getNid());
		uc.setDescriptionLogicProfile(ConfigurationStore.DATABASE, TermAux.VHAT_MODULES.getNid());
		uc.setDescriptionTypePreferenceList(ConfigurationStore.DATABASE, new int[] {TermAux.SCT_CORE_MODULE.getNid()});
		uc.setDialectAssemblagePreferenceList(ConfigurationStore.DATABASE, new int[] {TermAux.US_GOVERNMENT_WORK.getNid()});
		uc.setEditModule(ConfigurationStore.DATABASE, TermAux.US_DIALECT_ASSEMBLAGE.getNid());
		uc.setEditPath(ConfigurationStore.DATABASE, TermAux.UNSPECIFIED_MODULE.getNid());
		uc.setInferredAssemblage(ConfigurationStore.DATABASE, TermAux.TEMPLATE.getNid());
		uc.setLanguage(ConfigurationStore.DATABASE, TermAux.SWEDISH_LANGUAGE.getNid());
		uc.setObject(ConfigurationStore.DATABASE, "fred", -38);
		uc.setObject(ConfigurationStore.DATABASE, "jane", new Boolean(false));
		uc.setPremiseType(ConfigurationStore.DATABASE, PremiseType.STATED);
		uc.setStatedAssemblage(ConfigurationStore.DATABASE, TermAux.SUFFICIENT_CONCEPT_DEFINITION.getNid());
		uc.setTime(ConfigurationStore.DATABASE, 40);
		
		Assert.assertEquals(TermAux.XOR_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getClassifierNid());
		Assert.assertEquals(TermAux.VHAT_MODULES.getNid(), uc.getLogicCoordinate().getDescriptionLogicProfileNid());
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.SCT_CORE_MODULE.getNid()}, uc.getLanguageCoordinate().getDescriptionTypePreferenceList()));
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.US_GOVERNMENT_WORK.getNid()}, uc.getLanguageCoordinate().getDialectAssemblagePreferenceList()));
		Assert.assertEquals(TermAux.US_DIALECT_ASSEMBLAGE.getNid(), uc.getEditCoordinate().getModuleNid());
		Assert.assertEquals(TermAux.UNSPECIFIED_MODULE.getNid(), uc.getEditCoordinate().getPathNid());
		Assert.assertEquals(TermAux.TEMPLATE.getNid(), uc.getLogicCoordinate().getInferredAssemblageNid());
		Assert.assertEquals(TermAux.SWEDISH_LANGUAGE.getNid(), uc.getLanguageCoordinate().getLanguageConceptNid());
		Assert.assertEquals(new Integer(-38), uc.<Integer>getObject("fred"));
		Assert.assertEquals(new Boolean(false).booleanValue(), uc.<Boolean>getObject("jane").booleanValue());
		Assert.assertEquals(PremiseType.STATED, uc.getManifoldCoordinate().getPremiseType());
		Assert.assertEquals(TermAux.SUFFICIENT_CONCEPT_DEFINITION.getNid(), uc.getLogicCoordinate().getStatedAssemblageNid());
		Assert.assertEquals(TermAux.USER.getNid(), uc.getEditCoordinate().getAuthorNid());
		
		uc.setOption(ConfigurationStore.DATABASE, ConfigurationOption.PREMISE_TYPE, PremiseType.INFERRED);
		Assert.assertEquals(PremiseType.INFERRED, uc.getManifoldCoordinate().getPremiseType());
		
		//Defaults not impacted:
		testHelper2(Get.configurationService().getGlobalDatastoreConfiguration(), new DefaultCoordinateProvider());
		
		LookupService.shutdownIsaac();
		LookupService.startupIsaac();
		Get.configurationService().setSingleUserMode(true);
		
		Assert.assertEquals(TermAux.XOR_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getClassifierNid());
		Assert.assertEquals(TermAux.VHAT_MODULES.getNid(), uc.getLogicCoordinate().getDescriptionLogicProfileNid());
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.SCT_CORE_MODULE.getNid()}, uc.getLanguageCoordinate().getDescriptionTypePreferenceList()));
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.US_GOVERNMENT_WORK.getNid()}, uc.getLanguageCoordinate().getDialectAssemblagePreferenceList()));
		Assert.assertEquals(TermAux.US_DIALECT_ASSEMBLAGE.getNid(), uc.getEditCoordinate().getModuleNid());
		Assert.assertEquals(TermAux.UNSPECIFIED_MODULE.getNid(), uc.getEditCoordinate().getPathNid());
		Assert.assertEquals(TermAux.TEMPLATE.getNid(), uc.getLogicCoordinate().getInferredAssemblageNid());
		Assert.assertEquals(TermAux.SWEDISH_LANGUAGE.getNid(), uc.getLanguageCoordinate().getLanguageConceptNid());
		Assert.assertEquals(new Integer(-38), uc.<Integer>getObject("fred"));
		Assert.assertEquals(new Boolean(false).booleanValue(), uc.<Boolean>getObject("jane").booleanValue());
		Assert.assertEquals(PremiseType.INFERRED, uc.getManifoldCoordinate().getPremiseType());
		Assert.assertEquals(TermAux.SUFFICIENT_CONCEPT_DEFINITION.getNid(), uc.getLogicCoordinate().getStatedAssemblageNid());
		Assert.assertEquals(40, uc.getPathCoordinate().getStampFilter().getStampPosition().getTime());
		Assert.assertEquals(TermAux.USER.getNid(), uc.getEditCoordinate().getAuthorNid());
		
		//Defaults not impacted:
		testHelper2(Get.configurationService().getGlobalDatastoreConfiguration(), new DefaultCoordinateProvider());
	}
	
	@Test(dependsOnGroups= {"beforeUser", "defaultEdit", "systemEdit", "dbEdit"}, enabled = false)
	public void testUserOrderEdit() {
		
		//A change to a profile setting should not be reflected.
		UserConfiguration uc = Get.configurationService().getUserConfiguration(Optional.empty());
		uc.setClassifier(ConfigurationStore.PROFILE, TermAux.CONCEPT_ASSEMBLAGE.getNid());
		uc.setObject(ConfigurationStore.PROFILE, "fred", 500);
		
		Assert.assertEquals(TermAux.XOR_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getClassifierNid());
		Assert.assertEquals(new Integer(-38), uc.<Integer>getObject("fred"));
		
		//But if we clear the DB setting, it should fall back to the profile setting
		uc.setOption(ConfigurationStore.DATABASE, ConfigurationOption.CLASSIFIER, null);
		uc.setObject(ConfigurationStore.DATABASE, "fred", null);
		Assert.assertEquals(TermAux.CONCEPT_ASSEMBLAGE.getNid(), uc.getLogicCoordinate().getClassifierNid());
		Assert.assertEquals(new Integer(500), uc.<Integer>getObject("fred"));
		
		//and clearing the profile setting should put it back to default
		uc.setOption(ConfigurationStore.PROFILE, ConfigurationOption.CLASSIFIER, null);
		uc.setObject(ConfigurationStore.PROFILE, "fred", null);
		Assert.assertEquals(new DefaultCoordinateProvider().getDefaultLogicCoordinate().getClassifierNid(), uc.getLogicCoordinate().getClassifierNid());
		Assert.assertNull(uc.<Integer>getObject("fred"));
		
		//Clear all DB options
		uc.clearConfiguration(ConfigurationStore.DATABASE);
		//should be back to last set profile options
		Assert.assertEquals(new DefaultCoordinateProvider().getDefaultLogicCoordinate().getClassifierNid(), uc.getLogicCoordinate().getClassifierNid());
		Assert.assertEquals(TermAux.ACTIVE_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getDescriptionLogicProfileNid());
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.AND_NOT_QUERY_CLAUSE.getNid()}, uc.getLanguageCoordinate().getDescriptionTypePreferenceList()));
		Assert.assertTrue(Arrays.equals(new int[] {TermAux.AND_QUERY_CLAUSE.getNid()}, uc.getLanguageCoordinate().getDialectAssemblagePreferenceList()));
		Assert.assertEquals(TermAux.APACHE_2_LICENSE.getNid(), uc.getEditCoordinate().getModuleNid());
		Assert.assertEquals(TermAux.ASSEMBLAGE.getNid(), uc.getEditCoordinate().getPathNid());
		Assert.assertEquals(TermAux.ASSEMBLAGE_CONTAINS_COMPONENT_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getInferredAssemblageNid());
		Assert.assertEquals(TermAux.ASSEMBLAGE_CONTAINS_CONCEPT_QUERY_CLAUSE.getNid(), uc.getLanguageCoordinate().getLanguageConceptNid());
		Assert.assertNull(uc.<Integer>getObject("fred"));
		Assert.assertEquals(new Boolean(true).booleanValue(), uc.<Boolean>getObject("jane").booleanValue());
		Assert.assertEquals(PremiseType.INFERRED, uc.getManifoldCoordinate().getPremiseType());
		Assert.assertEquals(TermAux.ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT_QUERY_CLAUSE.getNid(), uc.getLogicCoordinate().getStatedAssemblageNid());
		Assert.assertEquals(30, uc.getPathCoordinate().getStampFilter().getStampPosition().getTime());
		Assert.assertEquals(TermAux.USER.getNid(), uc.getEditCoordinate().getAuthorNid());
		
		//Clear all Profile settings
		uc.clearConfiguration(ConfigurationStore.PROFILE);
		//Should be back to globals (which were previously set back to defaults themselves)
		testHelper(new DefaultCoordinateProvider(), uc);
	}
}
