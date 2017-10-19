/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.provider.bdb;

import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import sh.isaac.api.LookupService;
import static sh.isaac.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;
import sh.isaac.api.memory.HeapUseTicker;
import sh.isaac.api.progress.ActiveTasksTicker;

/**
 *
 * @author kec
 */

//https://www.jfokus.se/jfokus08/pres/jf08-HundredKilobytesKernelHK2.pdf
//https://github.com/saden1/hk2-testng
@HK2("integration")
public class BdbTestSuiteManager {
      /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /**
    * Tear down suite.
    *
    * @throws Exception the exception
    */
   @AfterGroups(groups = { "db" })
   public void tearDownSuite()
            throws Exception {
      LOG.info("Bdb test suite teardown");
      LookupService.shutdownSystem();
      ActiveTasksTicker.stop();
      HeapUseTicker.stop();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set up suite.
    *
    * @throws Exception the exception
    */
   @BeforeGroups(groups = { "db" })
   public void setUpSuite()
            throws Exception {
      LOG.info("Bdb test suite setup");
      System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "target/testdb/");

      final java.nio.file.Path dbFolderPath = Paths.get(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY));

      LOG.info("termstore folder path exists: " + dbFolderPath.toFile().exists());
      LookupService.startupIsaac();
      ActiveTasksTicker.start(10);
      HeapUseTicker.start(10);
   }
}
