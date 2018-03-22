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

import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jvnet.hk2.annotations.Contract;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.constants.SystemPropertyConstants;


/**
 * An interface used for system configuration.
 * 
 * The core interface here, only contains getters and setters for boot strapping the system - 
 * in general, things that must be set prior to launching the datastore (like the path to the datastore)
 * 
 * Items in this core API are not persisted across runs.  
 * 
 * More specific configuration, which is persisted through the lifecycle, can be found in {@link #getGlobalDatastoreConfiguration()}
 *  
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface ConfigurationService {

   public enum BuildMode{DB, IBDF}
   
   /**
    * @return the nid of the current user of the system.  This is ONLY returned if the system is in 
    * single user mode.
    * See {@link #setSingleUserMode(boolean)}
    */
   public Optional<Integer> getCurrentUserNid();

   /**
    * @param singleUserMode - if true, put the system in single user mode - read a user name from the hosting OS, find (or create) a concept as necessary 
    * to represent that user in the DB, and from this point forward, return the nid of this concept for {@link #getCurrentUserNid()} 
    * if false, remove the system from single user mode, and from this point forward, return an {@link Optional#empty()} for {@link #getCurrentUserNid()} 
    */
   public void setSingleUserMode(boolean singleUserMode);
   
   /**
    * Enable verbose debug.
    *
    * @return true if verbose debug has been enabled.  This default implementation allows the
    * feature to be enabled by setting the system property {@link SystemPropertyConstants#ISAAC_DEBUG} to 'true'
    */
   public default boolean isVerboseDebugEnabled() {
      final String value = System.getProperty(SystemPropertyConstants.ISAAC_DEBUG);

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
    * Note that this mode can be enabled-only only.  If you enable dbBuildMode, the mode cannot be turned off 
    * without a complete system shutdown / restart.
    * 
    * 
    * The default implementation of this returns false.
    *
    * @return true, if in ANY db build mode.  See also {@link #isInDBBuildMode(BuildMode)}
    */
   public default boolean isInDBBuildMode() {
      return false;
   }
   
   /**
    * When building a DB, we don't want to index per commit, or write changeset files, among other things.
    *
    * Note that this mode can be enabled-only only.  If you enable dbBuildMode, the mode cannot be turned off 
    * without a complete system shutdown / restart.
    * 
    * 
    * The default implementation of this returns false.
    * @param buildMode the build mode to query about.  If null is passed, behaves the same as {@link #isInDBBuildMode()}  
    *
    * @return true, if in the specified db build mode.  See also {@link #isInDBBuildMode()}
    */
   public default boolean isInDBBuildMode(BuildMode buildMode) {
      return false;
   }
   
   /**
    * An observable wrapper for dbBuildMode.  See also {@link #isInDBBuildMode(BuildMode)}
    * @return the object property that will notify if build mode is started
    */
   public default ReadOnlyObjectProperty<BuildMode> getDBBuildMode() {
      return new SimpleObjectProperty<BuildMode>(null);
   }

   /**
    * See {@link #isInDBBuildMode()}.
    * @param buildMode the build mode to enable.
    */
   public default void setDBBuildMode(BuildMode buildMode) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the data store folder path.
    *
    * @return The root folder of the datastore.
    *
    * This method will return (in the following order):
    *
    * 1) a path constructed from the value of {@link SystemPropertyConstants#DATA_STORE_ROOT_LOCATION_PROPERTY}, if that system property has been set 
    * 
    * 2) The value specified by a call to {@link #setDataStoreFolderPath(Path)}, if that method has been called.
    * 
    * 3) if the 'target' folder exists in the JVM launch location, check for the pattern target/data/*.data with a sub-file named dataStoreId.txt.
    *    If found, returns the found 'target/data/*.data' path.  Otherwise, returns 'target/data/isaac.data'.
    *   
    * 4) check for the pattern 'data/*.data' with a sub-file named dataStoreId.txt.relative to the JVM launch location.  If found, returns the 'data/*.data'
    *    path.  Otherwise, returns 'data/isaac.data'. relative to the JVM launch location. 
    * 
    * The returned path may or may NOT Exist on disk at the time that this method returns. The caller is responsible to create the path if necessary. 
    */
   public Path getDataStoreFolderPath();
   
   /**
    * Specify the root folder of the database. 
    *
    * This method can only be utilized prior to the first call to
    * {@link LookupService#startupIsaac()}  Calling this method specifies the value that will be returned by {@link #getDataStoreFolderPath()}
   *
    * @param dataStoreFolderPath the new data store folder path
    * @throws IllegalStateException if this is called after the system has already started.
    * @throws IllegalArgumentException if the provided dbFolderPath is an existing file, rather than a folder.
    */
   public void setDataStoreFolderPath(Path dataStoreFolderPath)
            throws IllegalStateException, IllegalArgumentException;

   /**
    * @return The folder that imports should execute from.
    * This method will return (in the following order):
    * 1) a path constructed from the value of {@link SystemPropertyConstants#IMPORT_FOLDER_LOCATION}, if that system property has been set 
    * 
    * 2) The value specified by a call to {@link #setIBDFImportPathFolderPath(Path)}, if that method has been called.
    * 
    * 3) returns 'data/to-import' relative to the JVM launch location.
    */
   public Path getIBDFImportPath();
   
   
   /**
    * Specify the location of the folder containing ibdf files to import.  This method can only be utilized prior to the first call to 
    * {@link #getIBDFImportPath()}.  
    * @param ibdfImportFolder
    * @throws IllegalStateException if this is called after the system has already started.
    * @throws IllegalArgumentException if the provided dbFolderPath is an existing file, rather than a folder.
    */
   public void setIBDFImportPathFolderPath(Path ibdfImportFolder) throws IllegalStateException, IllegalArgumentException;
   
   /**
    * @return The configuration properties that accompany the datastore, and are persisted along with the datastore, 
    * and impact all users
    */
   public default GlobalDatastoreConfiguration getGlobalDatastoreConfiguration() {
      return Get.service(GlobalDatastoreConfiguration.class);
   }
   
   /**
    * Fetch the configuration specific to a particular user.  
    * 
    * Note that this default implementation doesn't cache the UserConfiguration objects, overriding implementations should 
    * 
    * @param userNid - the nid of the concept that represents a user to fetch the configuration options for.
    * If no nid is passed, the nid from {@link #getCurrentUserNid()} will be used (which only works, if we are in single user mode)
    * If we are not in single user mode, then this will return all of the same options as if you had called 
    * {@link #getGlobalDatastoreConfiguration()}
    * @return the user-specific configuration options, if a userNid is available, otherwise, the global default options.
    */
   public default UserConfiguration getUserConfiguration(Optional<Integer> userNid) {
      UserConfiguration ucp = Get.service(UserConfiguration.class);
      ucp.finishInit((userNid == null || !userNid.isPresent()) ? getCurrentUserNid() : userNid);
      return ucp;
   }
}
