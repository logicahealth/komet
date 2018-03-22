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

import java.util.UUID;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;

/**
 * The core, generic get/set methods that will be required of services like {@link UserConfigurationPerDB}
 * and {@link UserConfigurationPerOSUser}
 * 
 * Also carries an enum of the more standard configuration options for convenience, but the store is not 
 * limited to these values.
 * 
 * Implementations must support the generic types as specified, at a minimum supporting types such as String, 
 * Integer, Long, and int[].
 * 
 * The currently existing implementations are built on top of what is present / working / convenient in the system.
 * Implementing another store, such as a JSON file backed store, or a GIT-backed remote sync system are left as
 * an exercise to the user...
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public interface UserConfigurationInternalImpl
{
	public enum ConfigurationOption 
	{
		EDIT_COORDINATE(ObservableEditCoordinate.class),
		LANGUAGE_COORDINATE(ObservableEditCoordinate.class),
		LOGIC_COORDINATE(ObservableEditCoordinate.class),
		MANIFOLD_COORDINATE(ObservableEditCoordinate.class),
		STAMP_COORDINATE(ObservableEditCoordinate.class),
		CLASSIFIER(Integer.class),
		DESCRIPTION_LOGIC_PROFILE(Integer.class),
		DESCRIPTION_TYPE_PREFERENCE_LIST(Integer[].class),
		DIALECT_ASSEMBLAGE_PREFERENCE_LIST(Integer[].class),
		INFERRED_ASSEMBLAGE(Integer.class),
		LANGUAGE(Integer.class),
		EDIT_MODULE(Integer.class),
		EDIT_PATH(Integer.class),
		STATED_ASSEMBLAGE(Integer.class),
		TIME(Long.class),
		PREMISE_TYPE(String.class),
		USER(String.class);  //for the global options only
		
		Class<?> dataType;
		ConfigurationOption(Class<?> dataType)
		{
			this.dataType = dataType;
		}
		
		public Class<?> getType()
		{
			return dataType;
		}
	}
	
	/**
	 * This method is not intended for external use, and will throw an exception if called.
	 * Implementations of UserConfiguration should expect this method to be called once, immediately after construction.
	 * Any subsequent calls should throw an exception.
	 * @param userConcept
	 */
	public void setUser(UUID userConcept);
	
	public <T> T putOption(ConfigurationOption option, T objectValue);
	
	public <T> T getOption(ConfigurationOption option);
	
	public boolean hasOption(ConfigurationOption option);
	
	public <T> T putOption(String custom, T objectValue);
	
	public <T> T getOption(String custom);
	
	public boolean hasOption(String custom);
	
	/**
	 * Remove all stored configuration for the current user.
	 */
	public void clearStoredConfiguration();
}
