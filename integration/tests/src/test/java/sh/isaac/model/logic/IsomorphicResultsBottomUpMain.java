/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.model.logic;

import java.io.File;
import java.util.prefs.BackingStoreException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferencesService;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.komet.preferences.UserConfigurationPerOSProvider;
import static sh.isaac.model.logic.IsomorphicResultsBottomUpNGTest.makeComparisonExpression;
import static sh.isaac.model.logic.IsomorphicResultsBottomUpNGTest.makeReferenceExpression;

/**
 *
 * @author kec
 */
public class IsomorphicResultsBottomUpMain {

    private static final Logger LOG = LogManager.getLogger();

    public void configure() throws Exception {
        LOG.info("isomorphic-suite setup");
        File db = new File("target/isomorphic-suite");
        RecursiveDelete.delete(db);
        //Don't overwrite "real" config
        UserConfigurationPerOSProvider.nodeName = "userConfigForTest";
        db.mkdirs();
        System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, db.getCanonicalPath());
        LookupService.startupPreferenceProvider();
        //Make sure remnants from any previous test are gone
        IsaacPreferences mainDataStore = Get.service(PreferencesService.class).getUserPreferences();
        mainDataStore.node(UserConfigurationPerOSProvider.nodeName).removeNode();
        Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
        LookupService.startupIsaac();
    }

    public void shutdown() throws BackingStoreException {
        LOG.info("isomorphic-suite teardown");
        //cleanup
        IsaacPreferences mainDataStore = Get.service(PreferencesService.class).getUserPreferences();
        mainDataStore.node(UserConfigurationPerOSProvider.nodeName).removeNode();
        LookupService.shutdownSystem();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            IsomorphicResultsBottomUpMain main = new IsomorphicResultsBottomUpMain();
            main.configure();
            // TODO code application logic here
            LogicalExpression referenceExpression = makeReferenceExpression();
            LogicalExpression comparisonExpression = makeComparisonExpression();
            IsomorphicResultsBottomUp results = new IsomorphicResultsBottomUp(referenceExpression, comparisonExpression);
            System.out.println(results);

            IsomorphicResultsFromPathHash isomorphicResultsFromPathHash = new IsomorphicResultsFromPathHash(referenceExpression, comparisonExpression);
            IsomorphicSolution results2 = isomorphicResultsFromPathHash.call();
            System.out.println(results2);
            main.shutdown();
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        System.exit(0);
    }

}
