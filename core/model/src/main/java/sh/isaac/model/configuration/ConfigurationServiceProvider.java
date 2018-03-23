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

package sh.isaac.model.configuration;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.DatastoreServices;
import sh.isaac.api.IsaacCache;
import sh.isaac.api.LookupService;
import sh.isaac.api.UserConfiguration;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.util.UuidT5Generator;


/**
 * The default implementation of {@link ConfigurationService} which is used to
 * specify where the datastore location is, among other things.
 *
 * Note that this default implementation has a {@link Rank} of 0. To override
 * this implementation with any other, simply provide another implementation on
 * the classpath with a higher rank.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "Default Configuration Service")
@Rank(value = 0)
@Singleton
public class ConfigurationServiceProvider
         implements ConfigurationService, IsaacCache {

   private static final Logger LOG = LogManager.getLogger();
   
   //this is just used for a cache key
   private final UUID UUID_FOR_NO_USER = UuidT5Generator.get("_no_user_present_");

   /** The value specified by a call to {@link #setDataStoreFolderPath(Path)}, if any. */
   private Path userDataStoreFolderPath = null;
   
   /** The value calculated per the rules in {@link #getDataStoreFolderPath()}. */
   private Path calculatedDataStoreFolderPath = null;
   
   /** The value calculated from the system property {@link SystemPropertyConstants#DATA_STORE_ROOT_LOCATION_PROPERTY}. */
   private Path systemPropertyDataStoreFolderPath = null;
   
   /** the value specified by a call to {@link #setIBDFImportPathFolderPath(Path)} */ 
   private Path userIbdfImportPath = null;
   
   /** the value specified by the system property {@link SystemPropertyConstants#IMPORT_FOLDER_LOCATION} */
   private Path systemPropertyIbdfImportPath = null;

   private SimpleObjectProperty<BuildMode> dbBuildMode = new SimpleObjectProperty<>(null);
   
   private DatabaseInitialization dbInitMode = DatabaseInitialization.NO_DATA_LOAD;
   
   private Cache<UUID, UserConfiguration> userConfigCache;
   
   private Optional<UUID> currentUser = Optional.empty();

   /**
    * Instantiates a new default configuration service.
    */
   private ConfigurationServiceProvider() {
      // only for HK2
      userConfigCache = Caffeine.newBuilder().maximumSize(10).build();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isInDBBuildMode() {
      return null != this.dbBuildMode.get();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isInDBBuildMode(BuildMode buildMode) {
      return buildMode == null ? isInDBBuildMode() : this.dbBuildMode.get() == buildMode;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setDBBuildMode(BuildMode buildMode) {
      if (this.dbBuildMode.get() != null && buildMode != this.dbBuildMode.get()) {
         throw new RuntimeException("Not allowed to change DBBuild Mode more than once.  Shutdown and restart to change mode");
      }
      this.dbBuildMode.set(buildMode);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ReadOnlyObjectProperty<BuildMode> getDBBuildMode() {
      return dbBuildMode;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Path getDataStoreFolderPath() {
      if (this.systemPropertyDataStoreFolderPath != null) {
            return this.systemPropertyDataStoreFolderPath;
      }
      if (this.userDataStoreFolderPath == null && this.calculatedDataStoreFolderPath == null) {
         synchronized (this) {
            if (this.userDataStoreFolderPath == null && this.calculatedDataStoreFolderPath == null) {
                  //Docs from interface for quick ref:
//               * 1) a path constructed from the value of {@link SystemPropertyConstants#DATA_STORE_ROOT_LOCATION_PROPERTY}, if that system property has been set 
//               * 
//               * 2) The value specified by a call to {@link #setDataStoreFolderPath(Path)}, if that method has been called.
//               * 
//               * 3) if the 'target' folder exists in the JVM launch location, check for the pattern target/data/*.data with a sub-file named dataStoreId.txt.
//               *    If found, returns the found 'target/data/*.data' path.  Otherwise, returns 'target/data/isaac.data'.
//               *   
//               * 4) check for the pattern 'data/*.data' with a sub-file named dataStoreId.txt.relative to the JVM launch location.  If found, returns the 'data/*.data'
//               *    path.  Otherwise, returns 'data/isaac.data'. relative to the JVM launch location. 

               String dataStoreRootFolder = System.getProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY);

               if (StringUtils.isNotBlank(dataStoreRootFolder)) {
                  this.systemPropertyDataStoreFolderPath = Paths.get(dataStoreRootFolder);
                  return this.systemPropertyDataStoreFolderPath;
               }
               else if (new File("target").isDirectory()){
                  this.calculatedDataStoreFolderPath = getDatabaseFolder(Paths.get("target", "data"));
               }
               else {
                  this.calculatedDataStoreFolderPath = getDatabaseFolder(Paths.get("data"));
               }
               LOG.info("Calculated data store folder path: {}", this.calculatedDataStoreFolderPath.toFile().getAbsolutePath());
            }
         }
      }

      if (this.userDataStoreFolderPath != null) {
         return this.userDataStoreFolderPath;
      }
      return this.calculatedDataStoreFolderPath;
   }
   
   private Path getDatabaseFolder(Path basePath) {
      if (basePath.toFile().isDirectory()) {
         for (File f : basePath.toFile().listFiles()) {
            if (f.getName().endsWith(".data") && f.isDirectory() && new File(f, DatastoreServices.DATASTORE_ID_FILE).isFile()) {
               // Found an existing database that ends with .data
               return f.toPath();
            }
         }
      }
      return basePath.resolve("isaac.data");
   }


   /**
    * {@inheritDoc}
    */
   @Override
   public void setDataStoreFolderPath(Path dataStoreFolderPath)
            throws IllegalStateException, IllegalArgumentException {
      LOG.info("setDataStoreFolderPath called with " + dataStoreFolderPath);

      if (LookupService.isIsaacStarted()) {
         throw new IllegalStateException("Can only set the dbFolderPath prior to starting Isaac. Runlevel: " + LookupService.getCurrentRunLevel());
      }

      if (Files.exists(dataStoreFolderPath) && !Files.isDirectory(dataStoreFolderPath)) {
         throw new IllegalArgumentException(
             "The specified path to the db folder appears to be a file, rather than a folder, as expected.  " +
             " Found: " + dataStoreFolderPath.toAbsolutePath().toString());
      }

      try {
         Files.createDirectories(dataStoreFolderPath);
      } catch (final IOException e) {
         throw new RuntimeException("Failure creating userDataStoreFolderPath folder: " + dataStoreFolderPath.toString(),
                                    e);
      }

      this.userDataStoreFolderPath = dataStoreFolderPath;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Path getIBDFImportPath()
   {
   //       * 1) a path constructed from the value of {@link SystemPropertyConstants#IMPORT_FOLDER_LOCATION}, if that system property has been set 
   //       * 
   //       * 2) The value specified by a call to {@link #setIBDFImportPathFolderPath(Path)}, if that method has been called.
   //       * 
   //       * 3) returns 'data/to-import' relative to the JVM launch location.
      if (this.systemPropertyIbdfImportPath != null) {
         return this.systemPropertyIbdfImportPath;
      }
      if (this.userIbdfImportPath == null) {
         String importFolder = System.getProperty(SystemPropertyConstants.IMPORT_FOLDER_LOCATION);

         if (StringUtils.isNotBlank(importFolder)) {
            this.systemPropertyIbdfImportPath = Paths.get(importFolder);
            return this.systemPropertyIbdfImportPath;
         }
         this.userIbdfImportPath = Paths.get("data", "to-import");
      }
      return this.userIbdfImportPath;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setIBDFImportPathFolderPath(Path ibdfImportFolder) throws IllegalStateException, IllegalArgumentException
   {
      LOG.info("setIBDFImportPathFolderPath called with " + ibdfImportFolder);

         if (LookupService.isIsaacStarted()) {
            throw new IllegalStateException("Can only set the ibdf import prior to starting Isaac. Runlevel: " + LookupService.getCurrentRunLevel());
         }

         if (Files.exists(ibdfImportFolder) && !Files.isDirectory(ibdfImportFolder)) {
            throw new IllegalArgumentException(
                "The specified path to the ibdf import folder appears to be a file, rather than a folder, as expected.  " +
                " Found: " + ibdfImportFolder.toAbsolutePath().toString());
         }

         try {
            Files.createDirectories(ibdfImportFolder);
         } catch (final IOException e) {
            throw new RuntimeException("Failure creating ibdf import folder: " + ibdfImportFolder.toString(),
                                       e);
         }

         this.userIbdfImportPath = ibdfImportFolder;
      
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public UserConfiguration getUserConfiguration(Optional<UUID> userId)
   {
      UserConfiguration uc = userConfigCache.getIfPresent((userId == null || !userId.isPresent()) ? UUID_FOR_NO_USER : userId.get());
      if (uc == null)
      {
         uc = ConfigurationService.super.getUserConfiguration(userId);
         userConfigCache.put(uc.getUserId().orElse(UUID_FOR_NO_USER), uc);
      }
      return uc;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Optional<UUID> getCurrentUserId()
   {
      return currentUser;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setSingleUserMode(boolean singleUserMode)
   {
      if (singleUserMode)
      {
         // TODO lookup the OS user name, create an appropriate concept, return the nid for that.
         currentUser = Optional.of(TermAux.USER.getPrimordialUuid());
      }
      else {
         currentUser = Optional.empty();
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public DatabaseInitialization getDatabaseInitializationMode() {
      String temp = System.getProperty(SystemPropertyConstants.DATA_STORE_INIT);
      DatabaseInitialization diFromSystem = null;
      if (StringUtils.isNotBlank(temp)) {
         try {
            diFromSystem = DatabaseInitialization.valueOf(temp);
            if (diFromSystem == null)
            {
               LogManager.getLogger().warn("Ignoring invalid value '{}' for system property '{}'", temp, SystemPropertyConstants.DATA_STORE_INIT);
            }
         }
         catch (Exception e) {
            LogManager.getLogger().warn("Ignoring invalid value '{}' for system property '{}'", temp, SystemPropertyConstants.DATA_STORE_INIT);
         }
      }
      
      if (diFromSystem != null) {
         LOG.info("Overriding the DatabaseInitialization configuration of {} with the value {} from a system property", dbInitMode, diFromSystem);
         return diFromSystem;
      }
      
      return dbInitMode == null ? DatabaseInitialization.NO_DATA_LOAD : dbInitMode;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setDatabaseInitializationMode(DatabaseInitialization initMode) {
      dbInitMode = initMode;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void reset() {
      if (LookupService.getCurrentRunLevel() <= LookupService.SL_L0_METADATA_STORE_STARTED_RUNLEVEL) {
         //If we have dropped below all isaac running runlevels, then clear these refs.
         this.calculatedDataStoreFolderPath = null;
         this.userDataStoreFolderPath = null;
         this.dbBuildMode.set(null);
         this.dbInitMode = DatabaseInitialization.NO_DATA_LOAD;
         this.userConfigCache.invalidateAll();
         this.currentUser = Optional.empty();
         this.systemPropertyDataStoreFolderPath = null;
         this.systemPropertyIbdfImportPath = null;
         this.userDataStoreFolderPath = null;
         this.userIbdfImportPath = null;
      }
   }
}
