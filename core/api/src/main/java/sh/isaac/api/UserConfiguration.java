/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api;

import java.util.Optional;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;
import javafx.beans.property.ReadOnlyObjectProperty;
import sh.isaac.api.UserConfigurationInternalImpl.ConfigurationOption;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampPath;

/**
 * This class provides the ability for the users to customize and persist changes to various default values.
 * 
 * When options are persisted, the caller must specify whether the change is only for the current database 
 * {@link ConfigurationStore#DATABASE} or if they should be stored in the user profile folder on the computer, 
 * and apply to any other database that is opened.  {@link ConfigurationStore#PROFILE}
 * 
 * On reads, the implementation will return the value from the most-specific services that is applicable.
 * userConfigPerDB {@link ConfigurationStore#DATABASE} -> user Config per OS {@link ConfigurationStore#PROFILE} -> Global config per DB
 * 
 * An option set in the database store has the highest priority, followed by an option set on the profile store, 
 * followed by the system default value.  Default values come from an implementation of the {@link GlobalDatastoreConfiguration}
 *
 * For all but the generic object getter / setter pattern, there will always be a 
 * value returned, as the gets fall through to the default system values which are always present.
 * 
 * Proper implementations of  {@link UserConfiguration} should fail, if the internal method to specify the userID has not yet 
 * been called, or if it is called twice - which will prevent API misuse if a caller inadvertently tries to get an instance 
 * of a {@link UserConfiguration} from HK2.
 * 
 * Do not request instances of {@link UserConfiguration} directly from HK2 - please utilize 
 * {@link ConfigurationService#getUserConfiguration(Optional)} to ensure getting a properly initialized service.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Contract
public interface UserConfiguration
{
	public enum ConfigurationStore {DATABASE, PROFILE};
	
	/**
	 * This method is for internal use, and will fail if called.
	 * @param userConcept
	 */
	public void finishInit(Optional<UUID> userConcept);

	/**
	 * @return The edit coordinate as configured by the current user
	 */
	public ReadOnlyObjectProperty<WriteCoordinate> getWriteCoordinate();

	/**
	 * @return the language coordinate as configured by the current user
	 * Any changes made within the returned value will not be automatically persisted.
	 */
	public ObservableLanguageCoordinate getLanguageCoordinate();

	/**
	 * @return The logic coordinate as configured by the current user
	 * Any changed made within the returned value will not be automatically persisted.
	 */
	public ObservableLogicCoordinate getLogicCoordinate();

	/**
	 * @return The manifold coordinate as configured by the current user, which is 
	 * mostly made up of the {@link #getEditCoordinate()}, {@link #getLanguageCoordinate()}, and 
	 * {@link #getPathCoordinate()}
	 * Any changed made within the returned value will not be automatically persisted.
	 */
	public ObservableManifoldCoordinate getManifoldCoordinate();

	/**
	 * @return the stamp coordinate as configured by the current user
	 * Any changes made within the returned value will not be automatically persisted.
	 */
	public ObservableStampPath getPathCoordinate();

	/**
	 * Enable a key/value store mechanism to store arbitrary objects.  This enables add-on modules
	 * to use this store for arbitrary user-preference data without having to extend these interfaces.
	 * 
	 * For core functionality, extending these interfaces with properly documented getters and setters
	 * is the preferred approach, however.
	 * @param objectKey the key for the requested object
	 * @return The object corresponding to the specified key, or null if no object is present.
	 */
	public <T> T getObject(String objectKey);

	/**
	 * Enable a key/value store mechanism to store arbitrary objects.  This enables add-on modules
	 * to use this store for arbitrary user-preference data without having to extend these interfaces.
	 * 
	 * For core functionality, extending these interfaces with properly documented getters and setters
	 * is the preferred approach, however.
	 * @param store 
	 * @param objectKey the key for the object to store
	 * @param objectValue the value to store, or null, to "unset" the option.
	 * @return The previously stored object corresponding to the specified key, or null if no object was present.
	 */
	public <T> T setObject(ConfigurationStore store, String objectKey, T objectValue);

	/**
	 * A generic way to get configuration options, without having to create individual getters for each one.
	 * @param option the option to get the value for
	 * @return the value
	 */
	public <T> T getOption(ConfigurationOption option);
	
	/**
	 * @return The UUID of the concept representing the user these options are for, or empty, if their is no user, 
	 * and this is passing directly through to the default config options
	 */
	public Optional<UUID> getUserId();

	/**
	 * A generic way to set configuration options, without having to create individual setters for each one.
	 * @param store Which store to store the value in
	 * @param option the option being stored, or null, to "unset" the option.
	 * @param objectValue the value of the option to store 
	 * @return the existing value, if any.
	 */
	public <T> Object setOption(ConfigurationStore store, ConfigurationOption option, T objectValue);

	/**
	 * Sets the classifier.
	 * @param store the data store this option should be saved in
	 * @param conceptId the new classifier
	 */
	public void setClassifier(ConfigurationStore store, int conceptId);

	/**
	 * Sets the description-logic profile. 
	 * @param store the data store this option should be saved in
	 * @param conceptId a conceptNid
	 */
	public void setDescriptionLogicProfile(ConfigurationStore store, int conceptId);

	/**
	 * Sets the description type preference list for description retrieval. 
	 * 
	 * @param store the data store this option should be saved id
	 * @param descriptionTypePreferenceList prioritized preference list of description type nids
	 */
	public void setDescriptionTypePreferenceList(ConfigurationStore store, int[] descriptionTypePreferenceList);

	/**
	 * Sets the dialect preference list for description retrieval. 
	 *
	 * @param store the data store this option should be saved in
	 * @param dialectAssemblagePreferenceList prioritized preference list of dialect assemblage nids
	 */
	public void setDialectAssemblagePreferenceList(ConfigurationStore store, int[] dialectAssemblagePreferenceList);

	/**
	 * Sets the inferred definition assemblage. 
	 *
	 * @param store the data store this option should be saved in
	 * @param conceptId the nid of a concept
	 */
	public void setInferredAssemblage(ConfigurationStore store, int conceptId);

	/**
	 * Sets the language for description retrieval. 
	 *
	 * @param store the data store this option should be saved in
	 * @param conceptId the nid of a concept
	 */
	public void setLanguage(ConfigurationStore store, int conceptId);

	/**
	 * Sets the module for editing operations. 
	 * 
	 * @param store the data store this option should be saved in
	 * @param conceptId the nid of a concept
	 */
	public void setEditModule(ConfigurationStore store, int conceptId);

	/**
	 * Sets the path for editing operations. 
	 *
	 * @param store the data store this option should be saved in
	 * @param conceptId the nid of a concept
	 */
	public void setEditPath(ConfigurationStore store, int conceptId);

	/**
	 * Sets the stated definition assemblage. 
	 * 
	 * @param store the data store this option should be saved in
	 * @param conceptId the nid of a concept
	 */
	public void setStatedAssemblage(ConfigurationStore store, int conceptId);
	
	/**
	 * Sets the premise type
	 * @param store the data store this option should be saved in
	 * @param premiseType the premise type selection of the user
	 */
	public void setPremiseType(ConfigurationStore store, PremiseType premiseType);

	/**
	 * Sets the time for viewing versions of components 
	 *
	 * @param store the data store this option should be saved in
	 * @param timeInMs Time in milliseconds since unix epoch. Long.MAX_VALUE is used to represent the latest versions.
	 */
	public void setTime(ConfigurationStore store, long timeInMs);
	
	/**
	 * clear any stored prefs for the current user 
	 *
	 * @param store the data store to clear
	 */
	public void clearConfiguration(ConfigurationStore store);
}