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

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.LookupService;
import sh.isaac.api.RemoteServiceInfo;
import sh.isaac.api.constants.Constants;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableTaxonomyCoordinate;

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
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private Path              dataStoreFolderPath_       = null;
   DefaultCoordinateProvider defaultCoordinateProvider_ = new DefaultCoordinateProvider();
   private volatile boolean  initComplete_              = false;
   private boolean           bootstrapMode              = false;
   private boolean           dbBuildMode                = false;
   private RemoteServiceInfo gitConfigInfo              = null;

   //~--- constructors --------------------------------------------------------

   private DefaultConfigurationService() {
      // only for HK2
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean inBootstrapMode() {
      return bootstrapMode;
   }

   @Override
   public boolean inDBBuildMode() {
      return dbBuildMode;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setBootstrapMode() {
      bootstrapMode = true;
   }

   @Override
   public void setDBBuildMode() {
      dbBuildMode = true;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @see
    * sh.isaac.api.ConfigurationService#getDataStoreFolderPath()
    */
   @Override
   public Optional<Path> getDataStoreFolderPath() {
      if ((dataStoreFolderPath_ == null) &&!initComplete_) {
         synchronized (this) {
            if ((dataStoreFolderPath_ == null) &&!initComplete_) {
               // This hacking is to prevent fortify from flagging an external data source path
               StringBuilder dataStoreRootFolder = new StringBuilder();

               System.getProperty(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY)
                     .chars()
                     .forEach(c -> dataStoreRootFolder.append((char) c));

               if (!StringUtils.isBlank(dataStoreRootFolder.toString())) {
                  dataStoreFolderPath_ = Paths.get(dataStoreRootFolder.toString());

                  if (!Files.exists(dataStoreFolderPath_)) {
                     try {
                        Files.createDirectories(dataStoreFolderPath_);
                     } catch (IOException e) {
                        throw new RuntimeException("Failure creating dataStoreRootFolder folder: " +
                                                   dataStoreFolderPath_.toString(),
                                                   e);
                     }
                  }

                  if (!Files.isDirectory(dataStoreFolderPath_)) {
                     throw new IllegalStateException(
                         "The specified path to the db folder appears to be a file, rather than a folder, as expected.  " +
                         " Found: " + dataStoreFolderPath_.toAbsolutePath().toString());
                  }
               }

               initComplete_ = true;
            }
         }
      }

      return Optional.ofNullable(dataStoreFolderPath_);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @see
    * sh.isaac.api.ConfigurationService#setDataStoreFolderPath(java.nio.file.Path)
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
             " Found: " + dataStoreFolderPath_.toAbsolutePath().toString());
      }

      try {
         Files.createDirectories(dataStoreFolderPath);
      } catch (IOException e) {
         throw new RuntimeException("Failure creating dataStoreFolderPath folder: " + dataStoreFolderPath.toString(),
                                    e);
      }

      dataStoreFolderPath_ = dataStoreFolderPath;
   }

   @Override
   public void setDefaultClassifier(int conceptId) {
      defaultCoordinateProvider_.setDefaultClassifier(conceptId);
   }

   @Override
   public void setDefaultDescriptionLogicProfile(int conceptId) {
      defaultCoordinateProvider_.setDefaultDescriptionLogicProfile(conceptId);
   }

   @Override
   public void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
      defaultCoordinateProvider_.setDefaultDescriptionTypePreferenceList(descriptionTypePreferenceList);
   }

   @Override
   public void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
      defaultCoordinateProvider_.setDefaultDialectAssemblagePreferenceList(dialectAssemblagePreferenceList);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ObservableEditCoordinate getDefaultEditCoordinate() {
      return defaultCoordinateProvider_.getDefaultEditCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDefaultInferredAssemblage(int conceptId) {
      defaultCoordinateProvider_.setDefaultInferredAssemblage(conceptId);
   }

   @Override
   public void setDefaultLanguage(int conceptId) {
      defaultCoordinateProvider_.setDefaultLanguage(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ObservableLanguageCoordinate getDefaultLanguageCoordinate() {
      return defaultCoordinateProvider_.getDefaultLanguageCoordinate();
   }

   @Override
   public ObservableLogicCoordinate getDefaultLogicCoordinate() {
      return defaultCoordinateProvider_.getDefaultLogicCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDefaultModule(int conceptId) {
      defaultCoordinateProvider_.setDefaultModule(conceptId);
   }

   @Override
   public void setDefaultPath(int conceptId) {
      defaultCoordinateProvider_.setDefaultPath(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ObservableStampCoordinate getDefaultStampCoordinate() {
      return defaultCoordinateProvider_.getDefaultStampCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDefaultStatedAssemblage(int conceptId) {
      defaultCoordinateProvider_.setDefaultStatedAssemblage(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ObservableTaxonomyCoordinate getDefaultTaxonomyCoordinate() {
      return defaultCoordinateProvider_.getDefaultTaxonomyCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDefaultTime(long timeInMs) {
      defaultCoordinateProvider_.setDefaultTime(timeInMs);
   }

   @Override
   public void setDefaultUser(int conceptId) {
      defaultCoordinateProvider_.setDefaultUser(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Optional<RemoteServiceInfo> getGitConfiguration() {
      return Optional.ofNullable(gitConfigInfo);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setGitConfiguration(RemoteServiceInfo gitConfiguration) {
      this.gitConfigInfo = gitConfiguration;
   }
}

