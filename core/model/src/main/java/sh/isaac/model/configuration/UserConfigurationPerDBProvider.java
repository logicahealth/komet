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
package sh.isaac.model.configuration;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.UserConfigurationPerDB;
import sh.isaac.api.metacontent.MetaContentService;

/**
 * An implementation of {@link UserConfigurationPerDB} based on the {@link MetaContentService}
 * 
 * This implementation supports any type of data storage, however, for any type beyond simple 
 * java types, it falls back to java serialization, which may cause various issues.
 * 
 * If utilizing the generic store APIs, recommend passing the data type as a byte[] for better performance
 * and control over the serialization.
 * 
 * An instance of this service is created PerLookup, which in practice, is one lookup per userId, 
 * due to caching in ConfigurationServiceProvider

 * This has a Rank of 0, to override this implementation, provide another implementation with a higher rank.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */

@Service(name = "Default UserConfig Per DB")
@Rank(value = 0)
@PerLookup
public class UserConfigurationPerDBProvider implements UserConfigurationPerDB
{
	private ConcurrentMap<String, Object> dataStore = null;
	
	private UserConfigurationPerDBProvider()
	{
		// for HK2
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOption(ConfigurationOption option)
	{
		switch(option) {
			case EDIT_COORDINATE:
			case LANGUAGE_COORDINATE:
			case LOGIC_COORDINATE:
			case MANIFOLD_COORDINATE:
			case STAMP_COORDINATE:
				throw new RuntimeException("Read of full not supported, please read individual parts");
			case CLASSIFIER:
			case USER:
			case DESCRIPTION_LOGIC_PROFILE:
			case DESCRIPTION_TYPE_PREFERENCE_LIST:
			case DIALECT_ASSEMBLAGE_PREFERENCE_LIST:
			case EDIT_MODULE:
			case EDIT_PATH:
			case INFERRED_ASSEMBLAGE:
			case LANGUAGE:
			case STATED_ASSEMBLAGE:
			case PREMISE_TYPE:
			case TIME:
				Object o = dataStore.get(option.name());
				if (o == null)
				{
					return null;
				}
				return (T) o;
			default :
				throw new RuntimeException("Unsupported option type");
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setUser(UUID userConcept)
	{
		if (dataStore != null)
		{
			throw new RuntimeException("User has already been set!");
		}
		dataStore = Get.service(MetaContentService.class).<String,Object>openStore("UserConfigurationPerDBProvider-" + userConcept);
	}

	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T putOption(ConfigurationOption option, T objectValue)
	{
		switch(option) {
			case EDIT_COORDINATE:
			case LANGUAGE_COORDINATE:
			case LOGIC_COORDINATE:
			case MANIFOLD_COORDINATE:
			case STAMP_COORDINATE:
				throw new RuntimeException("Write of full object not supported, please write individual parts");
			case CLASSIFIER:
			case USER:
			case DESCRIPTION_LOGIC_PROFILE:
			case DESCRIPTION_TYPE_PREFERENCE_LIST:
			case DIALECT_ASSEMBLAGE_PREFERENCE_LIST:
			case EDIT_MODULE:
			case EDIT_PATH:
			case INFERRED_ASSEMBLAGE:
			case LANGUAGE:
			case STATED_ASSEMBLAGE:
			case PREMISE_TYPE:
			case TIME:
				Object o;
				if (objectValue == null)
				{
					o = dataStore.remove(option.name());
				}
				else
				{
					o = dataStore.put(option.name(), objectValue);
				}
				if (o == null)
				{
					return null;
				}
				return (T)o;
			default :
				throw new RuntimeException("Option not yet supported by provider");
			
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasOption(ConfigurationOption option)
	{
		return dataStore.containsKey(option.name());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T putOption(String custom, T objectValue)
	{
		if (objectValue == null)
		{
			return (T) dataStore.remove("custom:" + custom);
		}
		else
		{
			return (T) dataStore.put("custom:" + custom, objectValue);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOption(String custom)
	{
		return (T) dataStore.get("custom:" + custom);
	}

	@Override
	public boolean hasOption(String custom)
	{
		return dataStore.containsKey("custom:" + custom);
	}

	@Override
	public void clearStoredConfiguration()
	{
		dataStore.clear();
	}
}
