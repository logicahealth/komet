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
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;

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
   private String dbBuildMode = "";

   /**
    * This value, if present, is passed in to {@link ConfigurationService#setDataStoreFolderPath(Path)}
    *
    * @parameter
    * @optional
    */
   @Parameter(required = false)
   private File dataStoreLocation;

   /** Location of the folder that contains the user profiles. */
   @Parameter(required = false)
   private File userProfileFolderLocation;
   
   
   /**
    * The (optional) DB type to build.  Should be a constant from {@link DatabaseImplementation}
    */
   @Parameter(required = false)
   private String dbImplementation;
   
   /**
    * Specify which type of database we should build.
    * @param databaseImplementation the dbtype to use 
    */
   public void setDBType(DatabaseImplementation databaseImplementation) {
      this.dbImplementation = databaseImplementation.name();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   @Override
   public void execute()
            throws MojoExecutionException {
      getLog().info("Setup terminology store");

      try {
         // Make sure the service Locator comes up ok
         LookupService.get();

         if (StringUtils.isNotBlank(this.dbBuildMode)) {
            boolean set = false;
            for (BuildMode bm : BuildMode.values()) {
               if (bm.name().toLowerCase().equals(this.dbBuildMode.toLowerCase())) {
                  Get.configurationService().setDBBuildMode(bm);
                  set = true;
                  getLog().info("DB Build Mode set to " + bm);
                  break;
               }
            }
            if (!set) {
                throw new MojoExecutionException("dbBuildMode must be set to a value the enum types in BuildMode - couldn't match '" + this.dbBuildMode + "'");
            }
         }

         if (this.dataStoreLocation != null)
         {
            Get.configurationService().setDataStoreFolderPath(dataStoreLocation.toPath());
         }
         
         
         if (StringUtils.isNotBlank(this.dbImplementation)) {
            Get.configurationService().setDatabaseImplementation(DatabaseImplementation.parse(this.dbImplementation));
         }

         getLog().info("  Setup AppContext, data store location = " + Get.configurationService().getDataStoreFolderPath().toFile().getCanonicalPath() + 
            " DataStore Type " + Get.configurationService().getDatabaseImplementation().name());
         LookupService.startupIsaac();
         getLog().info("Done setting up ISAAC");
      } catch (IllegalStateException | IllegalArgumentException | IOException e) {
         throw new MojoExecutionException("Database build failure", e);
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param dataStoreLocation This value, if present, is passed in to {@link ConfigurationService#setDataStoreFolderPath(Path)}
    */
   public void setDataStoreLocation(File dataStoreLocation) {
      this.dataStoreLocation = dataStoreLocation;
   }

   /**
    * Set location of the folder that contains the user profiles.
    *
    * @param inputUserProfileLocation the new location of the folder that contains the user profiles
    */
   public void setUserProfileFolderLocation(File inputUserProfileLocation) {
      this.userProfileFolderLocation = inputUserProfileLocation;
   }
}

