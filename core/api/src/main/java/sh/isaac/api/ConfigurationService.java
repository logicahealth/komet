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



package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.constants.Constants;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;

//~--- interfaces -------------------------------------------------------------

/**
 * An interface used for system configuration. Services started by the
 * {@link LookupService} will utilize an implementation of this service in order
 * to configure themselves.
 * TODO [KEC] consider how to manage a separation with the preferences service.   And the 3rd pref / metadata store....
 * Dan doesn't want to lose a config / preference store that actually tells you what it does... the generic preference 
 * store that was added - while generic in nature, is impossible for someone to use, because you don't even know what prefs 
 * may be set, or what they do.  Prefs without clear documentation / usage are close to useless... 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface ConfigurationService {
   /**
    * Enable verbose debug.
    *
    * @return true if verbose debug has been enabled.  This default implementation allows the
    * feature to be enabled by setting the system property {@link Constants#ISAAC_DEBUG} to 'true'
    */
   public default boolean enableVerboseDebug() {
      final String value = System.getProperty(Constants.ISAAC_DEBUG);

      if (StringUtils.isNotBlank(value)) {
         return value.trim()
                     .equalsIgnoreCase("true");
      } else {
         return false;
      }
   }

   /**
    * When building a DB, we don't want to index per commit, or write changeset files, among other things.
    *
    * Note that this mode can be enabled-only only.  If you enable dbBuildMode, the mode cannot be turned off later.
    * 
    * There are some cases where validators and such cannot be properly executed if we are building a DB 
    * 
    * The default implementation of this returns false.
    *
    * @return true, if successful
    */
   public default boolean inDBBuildMode() {
      return false;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the chronicle folder path.
    *
    * @return The root folder of the database - one would expect to find a
    * data-store specific folder such as "cradle" inside this folder. The
    * default implementation returns the result of
    * {@link #getDataStoreFolderPath()} + {@link Constants#DEFAULT_CHRONICLE_FOLDER}
    *
    * The returned path MAY NOT exists on disk at the time that this method returns.
    * It is the caller's responsibility to create the path if necessary. 
    */
   public default Path getChronicleFolderPath() {
      Path                 result;
      final Optional<Path> rootPath = getDataStoreFolderPath();

      if (!rootPath.isPresent()) {
         throw new IllegalStateException(
             "The ConfigurationService implementation has not been configured by a call to setDataStoreFolderPath()," +
             " and the system property " + Constants.DATA_STORE_ROOT_LOCATION_PROPERTY +
             " has not been set.  Cannot construct the chronicle folder path.");
      } else {
         result = rootPath.get()
                          .resolve(Constants.DEFAULT_CHRONICLE_FOLDER);
      }
      return result;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * See {@link #inDBBuildMode()}.
    */
   public default void setDBBuildMode() {
      throw new UnsupportedOperationException();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data store folder path.
    *
    * @return The root folder of the database - the returned path should contain
    * subfolders of
    * {@link Constants#DEFAULT_CHRONICLE_FOLDER} and
    * {@link Constants#DEFAULT_SEARCH_FOLDER}.
    *
    *
    * This method will return (in the following order):
    *
    * - The value specified by a call to {@link #setDataStoreFolderPath(Path)}
    * - a path constructed from the value of
    * {@link Constants#DATA_STORE_ROOT_LOCATION_PROPERTY} if
    * {@link #setDataStoreFolderPath(Path)} was never called
    * - Nothing if
    * {@link Constants#DATA_STORE_ROOT_LOCATION_PROPERTY} has not been set.
    *
    * If a value is returned, the returned path still MAY NOT Exist on disk at the time
    * that this method returns. The caller is responsible to create the path if necessary. 
    */
   public Optional<Path> getDataStoreFolderPath();

   //~--- set methods ---------------------------------------------------------

   /**
    * Specify the root folder of the database. The specified folder should
    * contain subfolders of {@link Constants#DEFAULT_CHRONICLE_FOLDER} and
    * {@link Constants#DEFAULT_SEARCH_FOLDER}.
    *
    * This method can only be utilized prior to the first call to
    * {@link LookupService#startupIsaac()}
    *
    * @param dataStoreFolderPath the new data store folder path
    * @throws IllegalStateException if this is called after the system has
    * already started.
    * @throws IllegalArgumentException if the provided dbFolderPath is an
    * existing file, rather than a folder.
    */
   public void setDataStoreFolderPath(Path dataStoreFolderPath)
            throws IllegalStateException, IllegalArgumentException;

   /**
    * Sets the default classifier. When changed, other default objects that
    * reference this object will be updated accordingly. Default: The value to
    * use if another value is not provided.
    *
    * @param conceptId the new default classifier
    */
   void setDefaultClassifier(int conceptId);

   /**
    * Sets the default description-logic profile. When changed, other default
    * objects that reference this object will be updated accordingly. Default:
    * The value to use if another value is not provided.
    *
    * @param conceptId either a nid or conceptNid
    */
   void setDefaultDescriptionLogicProfile(int conceptId);

   /**
    * Sets the default description type preference list for description
    * retrieval. When changed, other default objects that reference this object
    * will be updated accordingly. Default: The value to use if another value
    * is not provided.
    *
    * @param descriptionTypePreferenceList prioritized preference list of
    * description type sequences
    */
   void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList);

   /**
    * Sets the default dialect preference list for description retrieval. When
    * changed, other default objects that reference this object will be updated
    * accordingly. Default: The value to use if another value is not provided.
    *
    * @param dialectAssemblagePreferenceList prioritized preference list of
    * dialect assemblage sequences
    */
   void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default edit coordinate.
    *
    * @return an {@code ObservableEditCoordinate} based on the configuration
    * defaults.
    */
   ObservableEditCoordinate getDefaultEditCoordinate();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default inferred definition assemblage. When changed, other
    * default objects that reference this object will be updated accordingly.
    * Default: The value to use if another value is not provided.
    *
    * @param conceptId either a nid or conceptNid
    */
   void setDefaultInferredAssemblage(int conceptId);

   /**
    * Sets the default language for description retrieval. When changed, other
    * default objects that reference this object will be updated accordingly.
    * Default: The value to use if another value is not provided.
    *
    * @param conceptId either a nid or conceptNid
    */
   void setDefaultLanguage(int conceptId);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default language coordinate.
    *
    * @return an {@code ObservableLanguageCoordinate} based on the
    * configuration defaults.
    */
   ObservableLanguageCoordinate getDefaultLanguageCoordinate();

   /**
    * Gets the default logic coordinate.
    *
    * @return an {@code ObservableLogicCoordinate} based on the configuration
    * defaults.
    */
   ObservableLogicCoordinate getDefaultLogicCoordinate();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default module for editing operations. When changed, other
    * default objects that reference this object will be updated accordingly.
    * Default: The value to use if another value is not provided.
    *
    * @param conceptId either a nid or conceptNid
    */
   void setDefaultModule(int conceptId);

   /**
    * Sets the default path for editing operations. When changed, other default
    * objects that reference this object will be updated accordingly. Default:
    * The value to use if another value is not provided.
    *
    * @param conceptId either a nid or conceptNid
    */
   void setDefaultPath(int conceptId);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default stamp coordinate.
    *
    * @return an {@code ObservableStampCoordinate} based on the configuration
    * defaults.
    */
   ObservableStampCoordinate getDefaultStampCoordinate();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default stated definition assemblage. When changed, other
    * default objects that reference this object will be updated accordingly.
    * Default: The value to use if another value is not provided.
    *
    * @param conceptId either a nid or conceptNid
    */
   void setDefaultStatedAssemblage(int conceptId);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default taxonomy coordinate.
    *
    * @return an {@code ObservableManifoldCoordinate} based on the
    * configuration defaults.
    */
   ObservableManifoldCoordinate getDefaultManifoldCoordinate();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default time for viewing versions of components When changed,
    * other default objects that reference this object will be updated
    * accordingly. Default: The value to use if another value is not provided.
    *
    * @param timeInMs Time in milliseconds since unix epoch. Long.MAX_VALUE is
    * used to represent the latest versions.
    */
   void setDefaultTime(long timeInMs);

   /**
    * Sets the default user for editing and role-based access control. When
    * changed, other default objects that reference this object will be updated
    * accordingly. Default: The value to use if another value is not provided.
    *
    * @param conceptId either a nid or conceptNid
    */
   void setDefaultUser(int conceptId);

   //~--- get methods ---------------------------------------------------------

   /**
    * Return the known (if any) details to utilize to make a GIT server connection.
    * The returned URL should point to the root of the git server - not to a particular repository.
    *
    * @return the git configuration
    */
   public default Optional<RemoteServiceInfo> getGitConfiguration() {
      return Optional.empty();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Specify the details to be returned by {@link #getGitConfiguration()}.  This method is optional, and may not be supported
    * (in which case, it throws an {@link UnsupportedOperationException})
    *
    * @param rsi the new git configuration
    */
   public default void setGitConfiguration(RemoteServiceInfo rsi) {
      throw new UnsupportedOperationException();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the search folder path.
    *
    * @return The root folder of the search data store - one would expect to
    * find a data-store specific folder such as "lucene" inside this folder.
    * The default implementation returns either:
    *
    * A path as specified exactly via
    * {@link Constants#SEARCH_ROOT_LOCATION_PROPERTY} (if the property is set)
    * or the result of
    * {@link #getDataStoreFolderPath()} + {@link Constants#DEFAULT_SEARCH_FOLDER}
    *
    * The returned path exists on disk at the time that this method returns.
    */
   public default Path getSearchFolderPath() {
      Path                 result;
      final Optional<Path> rootPath = getDataStoreFolderPath();

      if (!rootPath.isPresent()) {
         throw new IllegalStateException(
             "The ConfigurationService implementation has not been configured by a call to setDataStoreFolderPath()," +
             " and the system property " + Constants.DATA_STORE_ROOT_LOCATION_PROPERTY +
             " has not been set.  Cannot construct the search folder path.");
      } else {
         result = rootPath.get()
                          .resolve(Constants.DEFAULT_SEARCH_FOLDER);
      }

      try {
         Files.createDirectories(result);
      } catch (final IOException e) {
         throw new RuntimeException(e);
      }

      return result;
   }
}

