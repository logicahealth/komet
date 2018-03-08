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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.LookupService;
import sh.isaac.api.RemoteServiceInfo;
import sh.isaac.api.constants.Constants;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;

//~--- classes ----------------------------------------------------------------

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
@Service(name = "Cradle Default Configuration Service")
@Rank(value = 0)
@Singleton
public class DefaultConfigurationService
         implements ConfigurationService {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The data store folder path. */
   private Path dataStoreFolderPath = null;

   /** The default coordinate provider. */
   DefaultCoordinateProvider defaultCoordinateProvider = new DefaultCoordinateProvider();

   /** The init complete. */
   private volatile boolean initComplete = false;

   /** The db build mode. */
   private SimpleObjectProperty<BuildMode> dbBuildMode = new SimpleObjectProperty<>(null);

   /** The git config info. */
   private RemoteServiceInfo gitConfigInfo = null;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new default configuration service.
    */
   private DefaultConfigurationService() {
      // only for HK2
   }

   /**
    * In DB build mode.
    *
    * @return true, if successful
    */
   @Override
   public boolean inDBBuildMode() {
      return null != this.dbBuildMode.get();
   }

   @Override
   public boolean inDBBuildMode(BuildMode buildMode) {
      return buildMode == null ? inDBBuildMode() : this.dbBuildMode.get() == buildMode;
   }

   /**
    * Set DB build mode.
    */
   @Override
   public void setDBBuildMode(BuildMode buildMode) {
      if (this.dbBuildMode.get() != null && buildMode != this.dbBuildMode.get()) {
         throw new RuntimeException("Not allowed to change DBBuild Mode more than once.  Shutdown and restart to change mode");
      }
      this.dbBuildMode.set(buildMode);
   }
   
   

   //~--- get methods ---------------------------------------------------------

   @Override
   public ReadOnlyObjectProperty<BuildMode> getDBBuildMode() {
      return dbBuildMode;
   }

/**
    * Gets the data store folder path.
    *
    * @return the data store folder path
    * @see sh.isaac.api.ConfigurationService#getDataStoreFolderPath()
    */
   @Override
   public Optional<Path> getDataStoreFolderPath() {
      if ((this.dataStoreFolderPath == null) &&!this.initComplete) {
         synchronized (this) {
            if ((this.dataStoreFolderPath == null) && !this.initComplete) {
               String dataStoreRootFolder = System.getProperty(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY);

               if (!StringUtils.isBlank(dataStoreRootFolder.toString())) {
                  this.dataStoreFolderPath = Paths.get(dataStoreRootFolder.toString());
               }

               this.initComplete = true;
            }
         }
      }

      return Optional.ofNullable(this.dataStoreFolderPath);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the data store folder path.
    *
    * @param dataStoreFolderPath the new data store folder path
    * @throws IllegalStateException the illegal state exception
    * @throws IllegalArgumentException the illegal argument exception
    * @see sh.isaac.api.ConfigurationService#setDataStoreFolderPath(java.nio.file.Path)
    */
   @Override
   public void setDataStoreFolderPath(Path dataStoreFolderPath)
            throws IllegalStateException, IllegalArgumentException {
      LOG.info("setDataStoreFolderPath called with " + dataStoreFolderPath);

      if (LookupService.isIsaacStarted()) {
         throw new IllegalStateException("Can only set the dbFolderPath prior to starting Isaac. Runlevel: " +
                                         LookupService.getCurrentRunLevel());
      }

      if (Files.exists(dataStoreFolderPath) &&!Files.isDirectory(dataStoreFolderPath)) {
         throw new IllegalArgumentException(
             "The specified path to the db folder appears to be a file, rather than a folder, as expected.  " +
             " Found: " + this.dataStoreFolderPath.toAbsolutePath().toString());
      }

      try {
         Files.createDirectories(dataStoreFolderPath);
      } catch (final IOException e) {
         throw new RuntimeException("Failure creating dataStoreFolderPath folder: " + dataStoreFolderPath.toString(),
                                    e);
      }

      this.dataStoreFolderPath = dataStoreFolderPath;
   }

   /**
    * Sets the default classifier.
    *
    * @param conceptId the new default classifier
    */
   @Override
   public void setDefaultClassifier(int conceptId) {
      this.defaultCoordinateProvider.setDefaultClassifier(conceptId);
   }

   /**
    * Sets the default description logic profile.
    *
    * @param conceptId the new default description logic profile
    */
   @Override
   public void setDefaultDescriptionLogicProfile(int conceptId) {
      this.defaultCoordinateProvider.setDefaultDescriptionLogicProfile(conceptId);
   }

   /**
    * Sets the default description type preference list.
    *
    * @param descriptionTypePreferenceList the new default description type preference list
    */
   @Override
   public void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
      this.defaultCoordinateProvider.setDefaultDescriptionTypePreferenceList(descriptionTypePreferenceList);
   }

   /**
    * Sets the default dialect assemblage preference list.
    *
    * @param dialectAssemblagePreferenceList the new default dialect assemblage preference list
    */
   @Override
   public void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
      this.defaultCoordinateProvider.setDefaultDialectAssemblagePreferenceList(dialectAssemblagePreferenceList);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default edit coordinate.
    *
    * @return the default edit coordinate
    */
   @Override
   public ObservableEditCoordinate getDefaultEditCoordinate() {
      return this.defaultCoordinateProvider.getDefaultEditCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default inferred assemblage.
    *
    * @param conceptId the new default inferred assemblage
    */
   @Override
   public void setDefaultInferredAssemblage(int conceptId) {
      this.defaultCoordinateProvider.setDefaultInferredAssemblage(conceptId);
   }

   /**
    * Sets the default language.
    *
    * @param conceptId the new default language
    */
   @Override
   public void setDefaultLanguage(int conceptId) {
      this.defaultCoordinateProvider.setDefaultLanguage(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default language coordinate.
    *
    * @return the default language coordinate
    */
   @Override
   public ObservableLanguageCoordinate getDefaultLanguageCoordinate() {
      return this.defaultCoordinateProvider.getDefaultLanguageCoordinate();
   }

   /**
    * Gets the default logic coordinate.
    *
    * @return the default logic coordinate
    */
   @Override
   public ObservableLogicCoordinate getDefaultLogicCoordinate() {
      return this.defaultCoordinateProvider.getDefaultLogicCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default module.
    *
    * @param conceptId the new default module
    */
   @Override
   public void setDefaultModule(int conceptId) {
      this.defaultCoordinateProvider.setDefaultModule(conceptId);
   }

   /**
    * Sets the default path.
    *
    * @param conceptId the new default path
    */
   @Override
   public void setDefaultPath(int conceptId) {
      this.defaultCoordinateProvider.setDefaultPath(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default stamp coordinate.
    *
    * @return the default stamp coordinate
    */
   @Override
   public ObservableStampCoordinate getDefaultStampCoordinate() {
      return this.defaultCoordinateProvider.getDefaultStampCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default stated assemblage.
    *
    * @param conceptId the new default stated assemblage
    */
   @Override
   public void setDefaultStatedAssemblage(int conceptId) {
      this.defaultCoordinateProvider.setDefaultStatedAssemblage(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default taxonomy coordinate.
    *
    * @return the default taxonomy coordinate
    */
   @Override
   public ObservableManifoldCoordinate getDefaultManifoldCoordinate() {
      return this.defaultCoordinateProvider.getDefaultManifoldCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default time.
    *
    * @param timeInMs the new default time
    */
   @Override
   public void setDefaultTime(long timeInMs) {
      this.defaultCoordinateProvider.setDefaultTime(timeInMs);
   }

   /**
    * Sets the default user.
    *
    * @param conceptId the new default user
    */
   @Override
   public void setDefaultUser(int conceptId) {
      this.defaultCoordinateProvider.setDefaultUser(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the git configuration.
    *
    * @return the git configuration
    */
   @Override
   public Optional<RemoteServiceInfo> getGitConfiguration() {
      return Optional.ofNullable(this.gitConfigInfo);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the git configuration.
    *
    * @param gitConfiguration the new git configuration
    */
   @Override
   public void setGitConfiguration(RemoteServiceInfo gitConfiguration) {
      this.gitConfigInfo = gitConfiguration;
   }
}

