/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.ibdf.diff;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.provider.ibdf.diff.BinaryDataDifferProvider;

//~--- classes ----------------------------------------------------------------

/**
 * Unit test for BinaryDataDifferProvider. Uses database defined in pom
 *
 * {@link BinaryDataDifferProvider}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class BinaryDataDifferTest {
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The terminology input file name. */
   private final String TERMINOLOGY_INPUT_FILE_NAME = "vhat-ibdf";

   /** The old version. */
   private final String OLD_VERSION = "4.3-SNAPSHOT";

   /** The new version. */
   private final String NEW_VERSION = "4.31-SNAPSHOT";

   /** The datastore path. */
   private final File DATASTORE_PATH = new File("target/db");

   /** The differ provider. */
   private final BinaryDataDifferProvider differProvider = new BinaryDataDifferProvider();

   //~--- methods -------------------------------------------------------------

   /**
    * Setup DB.
    *
    * @throws Exception the exception
    */
   @Before
   public void setupDB()
            throws Exception {
      Get.configurationService().setDataStoreFolderPath(DATASTORE_PATH.toPath());
      log.info("  Setup AppContext, data store location = " + Get.configurationService().getDataStoreFolderPath().toFile().getAbsolutePath());
      LookupService.startupIsaac();
      log.info("Done setting up ISAAC");
   }

   /**
    * Shutdown DB.
    *
    * @throws Exception the exception
    */
   @After
   public void shutdownDB()
            throws Exception {
      LookupService.shutdownIsaac();
      log.info("ISAAC shut down");
   }

   /**
    * Mimick the IbdfDiffMojo.
    */
   @Test
   public void testDiff() {
      // Parameters used by Mojo
      // Input Files
      final File oldVersionFile = new File("src/test/resources/data/old/" + this.TERMINOLOGY_INPUT_FILE_NAME + ".ibdf");
      final File newVersionFile = new File("src/test/resources/data/new/" + this.TERMINOLOGY_INPUT_FILE_NAME + ".ibdf");

      // Output Files
      final String ibdfFileOutputDir      = "target/unitTestOutput/ibdfFileOutputDir/";
      final String analysisFilesOutputDir = "target/unitTestOutput/analysisFilesOutputDir/";
      final String ouptutIbdfFileName = this.TERMINOLOGY_INPUT_FILE_NAME + "-Diff-" + this.OLD_VERSION + "-to-" +
                                        this.NEW_VERSION + ".ibdf";

      // Others
      final String  importDate          = "2016-09-30";
      final boolean diffOnStatus        = true;
      final boolean diffOnAuthor        = true;
      final boolean diffOnModule        = true;
      final boolean diffOnPath          = true;
      final boolean diffOnTimestamp     = true;
      final boolean createAnalysisFiles = true;
//TODO figure out where this working code is
//      this.differProvider.initialize(analysisFilesOutputDir,
//                                     ibdfFileOutputDir,
//                                     ouptutIbdfFileName,
//                                     createAnalysisFiles,
//                                     diffOnStatus,
//                                     diffOnTimestamp,
//                                     diffOnAuthor,
//                                     diffOnModule,
//                                     diffOnPath,
//                                     importDate);
//
//      try {
//         final Map<IsaacObjectType, Set<IsaacExternalizable>> oldContentMap =
//            this.differProvider.processVersion(oldVersionFile);
//         final Map<IsaacObjectType, Set<IsaacExternalizable>> newContentMap =
//            this.differProvider.processVersion(newVersionFile);
//         final Map<ChangeType, List<IsaacExternalizable>> changedComponents =
//            this.differProvider.identifyVersionChanges(oldContentMap,
//                                                       newContentMap);
//
//         this.differProvider.generateDiffedIbdfFile(changedComponents);
//
//         if (createAnalysisFiles) {
//            this.differProvider.writeFilesForAnalysis(oldContentMap,
//                  newContentMap,
//                  changedComponents,
//                  ibdfFileOutputDir,
//                  analysisFilesOutputDir);
//         }
//      } catch (final Exception e) {
//         assertTrue(false);
//      }
//
//      assertTrue(true);
   }
}

