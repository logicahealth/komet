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



package sh.isaac.mojo;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.util.DBLocator;

//~--- classes ----------------------------------------------------------------

/**
 * Goal which opens (and creates if necessary) a Data Store.
 *
 */
@Mojo(
   defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
   name         = "setup-isaac"
)
public class Setup
        extends AbstractMojo {
   /**
    * See {@link ConfigurationService#setDBBuildMode()} for details on this option.
    */
   @Parameter(required = false)
   private final boolean dbBuildMode = false;

   /**
    * See {@link ConfigurationService#setBootstrapMode()} for details on this option.
    */
   @Parameter(required = false)
   private final boolean bootstrapMode = false;

   /**
    * See {@link ConfigurationService#setDataStoreFolderPath(java.nio.file.Path) for details on what should
    * be in the passed in folder location.
    *
    * Note that the value passed in here is also passed through {@link DBLocator#findDBFolder(File)}
    *
    * @parameter
    * @required
    */
   @Parameter(required = true)
   private File dataStoreLocation;

   /**
    * Location of the folder that contains the user profiles
    */
   @Parameter(required = false)
   private File userProfileFolderLocation;

   //~--- methods -------------------------------------------------------------

   /**
    * @throws org.apache.maven.plugin.MojoExecutionException
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   @Override
   public void execute()
            throws MojoExecutionException {
      getLog().info("Setup terminology store");

      try {
         // Make sure the service Locator comes up ok
         LookupService.get();

         if (this.dbBuildMode) {
            Get.configurationService()
               .setDBBuildMode();
         }

         if (this.bootstrapMode) {
            Get.configurationService()
               .setBootstrapMode();
         }

         this.dataStoreLocation = DBLocator.findDBFolder(this.dataStoreLocation);

         if (!this.dataStoreLocation.exists()) {
            throw new MojoExecutionException("Couldn't find a data store from the input of '" +
                                             this.dataStoreLocation.getAbsoluteFile().getAbsolutePath() + "'");
         }

         if (!this.dataStoreLocation.isDirectory()) {
            throw new IOException("The specified data store: '" + this.dataStoreLocation.getAbsolutePath() +
                                  "' is not a folder");
         }

         LookupService.getService(ConfigurationService.class)
                      .setDataStoreFolderPath(this.dataStoreLocation.toPath());
         getLog().info("  Setup AppContext, data store location = " + this.dataStoreLocation.getCanonicalPath());
         LookupService.startupIsaac();
         getLog().info("Done setting up ISAAC");
      } catch (IllegalStateException | IllegalArgumentException | IOException e) {
         throw new MojoExecutionException("Database build failure", e);
      }
   }

   //~--- set methods ---------------------------------------------------------

   public void setDataStoreLocation(File inputBdbFolderlocation) {
      this.dataStoreLocation = inputBdbFolderlocation;
   }

   public void setUserProfileFolderLocation(File inputUserProfileLocation) {
      this.userProfileFolderLocation = inputUserProfileLocation;
   }
}

