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



package sh.isaac.api.util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * {@link DBLocator}
 *
 * Was previously a utility to help ease the transition from the old paths used for the BDB, to the new paths.
 *
 * With the new ISAAC foundation, the role of this class is vastly simplified.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DBLocator {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   //~--- methods -------------------------------------------------------------

   /**
    * Attempts to find the database folder, using the following selection criteria:
    * 1) If the passed in folder ends with '.data' and it exists, it is used directly.
    * 2) Otherwise, we scan the children of the passed in folder, looking for a folder that ends with .data
    * 3) If still not found - it will scan the sibling folders of the passed in folder, looking for a folder that ends with .data
    * 4) Finally, if nothing matches, it just returns the input folder - however, if the folder doesn't exist, it will create it.
    *
    * @param inputFolder the input folder
    * @return the file
    */
   public static File findDBFolder(File inputFolder) {
      inputFolder = inputFolder.getAbsoluteFile();

      // If it is a folder with a '.data' at the end of the name, just use it.
      if (inputFolder.getName().endsWith(".data") && inputFolder.isDirectory()) {
         LOG.info("Data Store Location set to " + inputFolder.getAbsolutePath());
         return inputFolder;
      }

      // Otherwise see if we can find a .data folder as a direct child
      if (inputFolder.isDirectory()) {
         for (final File f: inputFolder.listFiles()) {
            if (f.getName().endsWith(".data") && f.isDirectory()) {
               LOG.info("Data Store Location set to " + f.getAbsolutePath());
               return f;
            }
         }
      }

      // Or as a sibling
      if (inputFolder.getParentFile()
                     .isDirectory()) {
         for (final File f: inputFolder.getParentFile()
                                       .listFiles()) {
            // If it is a folder with a '.bdb' at the end of the name, then berkeley-db will be in a sub-folder.
            if (f.getName().endsWith(".data") && f.isDirectory()) {
               LOG.info("Data Store Location set to " + f.getAbsolutePath());
               return f;
            }
         }
      }

      // can't match an expected pattern... just return the input.
      if (!inputFolder.exists()) {
         inputFolder.mkdirs();
      }

      LOG.info("Data Store Location set to " + inputFolder.getAbsolutePath());
      return inputFolder;
   }
}

