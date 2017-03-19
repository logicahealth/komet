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



package sh.isaac.utility.export;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.LookupService;
import sh.isaac.api.util.DBLocator;

import static sh.isaac.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TestVetsExporter.
 */
public class TestVetsExporter {
   
   /** The log. */
   private static Logger log = LogManager.getLogger();

   //~--- methods -------------------------------------------------------------

   /**
    * The main method.
    *
    * @param args the arguments
    */
   public static void main(String[] args) {
      new TestVetsExporter();
      issacInit();

      final VetsExporter ve = new VetsExporter();

      ve.export(System.out, 1451628000000l, System.currentTimeMillis(), false);
      isaacStop();
      javafx.application.Platform.exit();
   }

   /**
    * Isaac stop.
    */
   private static void isaacStop() {
      log.info("Stopping ISAAC");
      LookupService.shutdownSystem();
      log.info("ISAAC stopped");
   }

   /**
    * Issac init.
    */
   private static void issacInit() {
      log.info("Isaac Init called");

      try {
         log.info("ISAAC Init thread begins");

         if (StringUtils.isBlank(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY))) {
            System.getProperty("isaacDatabaseLocation");

            // File temp = new File(sysProp);
            final File   dataStoreLocation = DBLocator.findDBFolder(new File(""));  // temp

            if (!dataStoreLocation.exists()) {
               throw new RuntimeException("Couldn't find a data store from the input of '" +
                                          dataStoreLocation.getAbsoluteFile().getAbsolutePath() + "'");
            }

            if (!dataStoreLocation.isDirectory()) {
               throw new RuntimeException("The specified data store: '" + dataStoreLocation.getAbsolutePath() +
                                          "' is not a folder");
            }

            // use the passed in JVM parameter location
            LookupService.getService(ConfigurationService.class)
                         .setDataStoreFolderPath(dataStoreLocation.toPath());
            System.out.println("  Setup AppContext, data store location = " + dataStoreLocation.getAbsolutePath());
         }

         // status_.set("Starting ISAAC");
         LookupService.startupIsaac();

         // status_.set("Ready");
         System.out.println("Done setting up ISAAC");
      } catch (final Exception e) {
         log.error("Failure starting ISAAC", e);
      }
   }
}

