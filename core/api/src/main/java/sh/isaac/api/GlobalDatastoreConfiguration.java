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
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;

/**
 * This interface defines all attributes of the system that can be get or set on a global basis, and are persisted as part of the 
 * isaac datastore (but not directly inside the chronicle datastore)
 * 
 * Using the setters on any value here will impact every user of the system.
 * 
 * User specific properties and settings should not be put here.
 * 
 * The default implementation of all of the setter methods here, is to throw an unsupported operation exception, 
 * implementations of this class must override the setters, if they wish to provide the ability to set and persist
 * any of these values.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Contract
public interface GlobalDatastoreConfiguration extends UserConfigurationInternalImpl
{
	/**
	 * Gets the default edit coordinate.
	 *
	 * @return an {@code ObservableEditCoordinate} based on the configuration defaults.
	 */
	public ObservableEditCoordinate getDefaultEditCoordinate();

	/**
	 * Gets the default language coordinate.
	 *
	 * @return an {@code ObservableLanguageCoordinate} based on the configuration defaults.
	 */
	public ObservableLanguageCoordinate getDefaultLanguageCoordinate();

	/**
	 * Gets the default logic coordinate.
	 *
	 * @return an {@code ObservableLogicCoordinate} based on the configuration defaults.
	 */
	public ObservableLogicCoordinate getDefaultLogicCoordinate();

	/**
	 * Gets the default taxonomy coordinate.
	 *
	 * @return an {@code ObservableManifoldCoordinate} based on the configuration defaults.
	 */
	public ObservableManifoldCoordinate getDefaultManifoldCoordinate();

	/**
	 * Gets the default stamp coordinate.
	 *
	 * @return an {@code ObservableStampCoordinate} based on the configuration defaults.
	 */
	public ObservableStampCoordinate getDefaultStampCoordinate();

	/**
	 * Return the known (if any) details to utilize to make a GIT server connection.
	 * The returned URL should point to the root of the git server - not to a particular repository.
	 *
	 * @return the git configuration
	 */
	public default Optional<RemoteServiceInfo> getGitConfiguration()
	{
		return Optional.empty();
	}
	
	/**
	 * @return The current specified memory configuration.  If the returned
	 * optional is empty, then the data store service implementations are 
	 * free to choose a default that suits the implementation
	 * 
	 * Note that this value can be overridden by specifying a system property of 
	 * {@link SystemPropertyConstants#DATA_STORE_MEMORY_CONFIG_PROPERTY} 
	 * with a value from {@link MemoryConfiguration}
	 * 
	 * If the system property is specified, it takes priority over any set value.
	 */
	public Optional<MemoryConfiguration> getMemoryConfiguration();
	
	/**
	 * Change the memory configuration (though, this will only take effect after a restart)
	 * @param memoryConfiguration
	 */
	public void setMemoryConfiguration(MemoryConfiguration memoryConfiguration);
	
	/**
	 * Sets the default classifier. When changed, other default objects that
	 * reference this object will be updated accordingly. Default: The value to
	 * use if another value is not provided.
	 *
	 * @param conceptId the new default classifier
	 */
	public default void setDefaultClassifier(int conceptId)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the default description-logic profile. When changed, other default
	 * objects that reference this object will be updated accordingly. Default:
	 * The value to use if another value is not provided.
	 *
	 * @param conceptId a conceptNid
	 */
	public default void setDefaultDescriptionLogicProfile(int conceptId)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the default description type preference list for description
	 * retrieval. When changed, other default objects that reference this object
	 * will be updated accordingly. Default: The value to use if another value
	 * is not provided.
	 *
	 * @param descriptionTypePreferenceList prioritized preference list of description type sequences
	 */
	public default void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the default dialect preference list for description retrieval. When
	 * changed, other default objects that reference this object will be updated
	 * accordingly. Default: The value to use if another value is not provided.
	 *
	 * @param dialectAssemblagePreferenceList prioritized preference list of dialect assemblage sequences
	 */
	public default void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the default inferred definition assemblage. When changed, other
	 * default objects that reference this object will be updated accordingly.
	 * Default: The value to use if another value is not provided.
	 *
	 * @param conceptId the nid of a concept
	 */
	public default void setDefaultInferredAssemblage(int conceptId)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the default language for description retrieval. When changed, other
	 * default objects that reference this object will be updated accordingly.
	 * Default: The value to use if another value is not provided.
	 *
	 * @param conceptId the nid of a concept
	 */
	public default void setDefaultLanguage(int conceptId)
	{
		throw new UnsupportedOperationException();
	}
	/**
	 * Sets the default module for editing operations. When changed, other
	 * default objects that reference this object will be updated accordingly.
	 * Default: The value to use if another value is not provided.
	 *
	 * @param conceptId the nid of a concept
	 */
	public default void setDefaultModule(int conceptId)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the default path for editing operations. When changed, other default
	 * objects that reference this object will be updated accordingly. Default:
	 * The value to use if another value is not provided.
	 *
	 * @param conceptId the nid of a concept
	 */
	public default void setDefaultPath(int conceptId)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the default stated definition assemblage. When changed, other
	 * default objects that reference this object will be updated accordingly.
	 * Default: The value to use if another value is not provided.
	 *
	 * @param conceptId the nid of a concept
	 */
	public default void setDefaultStatedAssemblage(int conceptId)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the default time for viewing versions of components When changed,
	 * other default objects that reference this object will be updated
	 * accordingly. Default: The value to use if another value is not provided.
	 *
	 * @param timeInMs Time in milliseconds since unix epoch. Long.MAX_VALUE is
	 *            used to represent the latest versions.
	 */
	public default void setDefaultTime(long timeInMs)
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Sets the default premise type for taxonomy.  When changed,
	 * other default objects that reference this object will be updated
	 * accordingly. Default: The value to use if another value is not provided.
	 * @param premiseType the desired premise type
	 */
	public default void setDefaultPremiseType(PremiseType premiseType)
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Sets the default user for editing and role-based access control. When
	 * changed, other default objects that reference this object will be updated
	 * accordingly. Default: The value to use if another value is not provided.
	 *
	 * @param conceptId the nid of a concept
	 */
	public default void setDefaultUser(int conceptId)
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Specify the details to be returned by {@link #getGitConfiguration()}. This method is optional, and may not be supported
	 * (in which case, it throws an {@link UnsupportedOperationException})
	 *
	 * @param rsi the new git configuration
	 */
	public default void setGitConfiguration(RemoteServiceInfo rsi)
	{
		throw new UnsupportedOperationException();
	}
}
