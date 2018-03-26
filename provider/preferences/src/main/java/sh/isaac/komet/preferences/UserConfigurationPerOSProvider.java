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
package sh.isaac.komet.preferences;

import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.UserConfigurationPerOSUser;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferencesService;

/**
 * A hatchet job to utilize the {@link IsaacPreferences} API as a backing store 
 * for the {@link UserConfigurationPerOSUser} interface.  
 * 
 * This currently supports the basic types in use, String, int, long, int[], but nothing else.
 * 
 * For the generic object get / set methods, this only supports String.
 * 
 * This store chains through to the OS provided user-preference store, and stores per user 
 * UUID identifier inside of that, to support the use case where this store was utilized while the system
 * was in multi-user mode.
 * 
 * An instance of this service is created PerLookup, which in practice, is one lookup per userId, 
 * due to caching in ConfigurationServiceProvider
 * 
 * This has a Rank of 0, to override this implementation, provide another implementation with a higher rank.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */

@Service(name = "Default UserConfig Per OS Profile")
@Rank(value = 0)
@PerLookup
public class UserConfigurationPerOSProvider implements UserConfigurationPerOSUser
{
	private IsaacPreferences dataStore = null;
	private UUID userId = null;
	
	/**
	 * This controls where it stores the data in the user profile.  Normally, we do not change this, however, certain test cases need to change it, 
	 * in order to not overwrite real stored prefs with garbage when running tests.
	 */
	public static String nodeName ="IsaacUserConfiguration";
	
	private UserConfigurationPerOSProvider()
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
				if (option.getType().isAssignableFrom(String.class))
				{
					return (T)dataStore.get(option.name()).orElse(null);
				}
				if (option.getType().isAssignableFrom(PremiseType.class))
				{
					String temp = dataStore.get(option.name()).orElse(null);
					if (temp != null)
					{
						return (T)PremiseType.valueOf(temp); 
					}
					return (T)null;
				}
				else if (option.getType().isAssignableFrom(Integer.class))
				{
					//translate UUID to nid
					String uuid = dataStore.get(option.name()).orElse(null);
					if (uuid != null)
					{
						Integer nid = Get.identifierService().getNidForUuids(UUID.fromString(uuid));
						return (T)nid;
					}
					else 
					{
						return (T)null;
					}
				}
				else if (option.getType().isAssignableFrom(Long.class))
				{
					OptionalLong temp = dataStore.getLong(option.name());
					return (T) (temp.isPresent() ? temp.getAsLong() : null);
				}
				else if (option.getType().isAssignableFrom(int[].class))
				{
					String temp = dataStore.get(option.name()).orElse(null);
					return (T) stringToIntArray(temp);
				}
				else {
					throw new RuntimeException("Unsupported data type");
				}
			default :
				throw new RuntimeException("Unsupported option type");
		}
	}
	
	private String intArrayToString(int[] ints)
	{
		if (ints == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i : ints)
		{
			sb.append(Get.identifierService().getUuidPrimordialForNid(i));
			sb.append(":");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	private int[] stringToIntArray(String string)
	{
		ArrayList<Integer> ints = new ArrayList<>();
		if (string == null)
		{
			return null;
		}
		String[] temp = string.split(":");
		for (String s : temp)
		{
			ints.add(Get.identifierService().getNidForUuids(UUID.fromString(s)));
		}
		
		int[] temp2 = new int[ints.size()];
		int j = 0;
		for (int i : ints)
		{
			temp2[j++] = i;
		}
		
		return temp2;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setUser(UUID userId)
	{
		if (dataStore != null)
		{
			throw new RuntimeException("User has already been set!");
		}
		IsaacPreferences mainDataStore = Get.service(PreferencesService.class).getUserPreferences();
		dataStore = mainDataStore.node(nodeName).node(userId.toString());
		this.userId = userId;
	}

	/** 
	 * {@inheritDoc}
	 */
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
				T toReturn = getOption(option);
				if (option.getType().isAssignableFrom(String.class))
				{
					if (objectValue == null)
					{
						dataStore.remove(option.name());
					}
					else
					{
						dataStore.put(option.name(), (String)objectValue);
					}
				}
				else if (option.getType().isAssignableFrom(PremiseType.class))
				{
					if (objectValue == null)
					{
						dataStore.remove(option.name());
					}
					else
					{
						dataStore.put(option.name(), ((PremiseType)objectValue).name());
					}					
				}
				else if (option.getType().isAssignableFrom(Integer.class))
				{
					if (objectValue == null)
					{
						dataStore.remove(option.name());
					}
					else
					{
						dataStore.put(option.name(), Get.identifierService().getUuidPrimordialForNid((Integer)objectValue).toString());
					}
					return toReturn;
				}
				else if (option.getType().isAssignableFrom(Long.class))
				{
					if (objectValue == null)
					{
						dataStore.remove(option.name());
					}
					else
					{
						dataStore.putLong(option.name(), (Long)objectValue);
					}
				}
				else if (option.getType().isAssignableFrom(int[].class))
				{
					if (objectValue == null)
					{
						dataStore.remove(option.name());
					}
					else
					{
						dataStore.put(option.name(), intArrayToString((int[])objectValue));
					}
				}
				else {
					throw new RuntimeException("Unsupported data type");
				}
				return toReturn;
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
		return getOption(option) != null;
	}

	@Override
	public <T> T putOption(String custom, T objectValue)
	{
		T toReturn = getOption(custom);
		
		if (objectValue == null)
		{
			dataStore.remove("customS:" + custom);
			dataStore.remove("customB:" + custom);
			dataStore.remove("customI:" + custom);
		}
		else
		{
			if (objectValue instanceof String)
			{
				dataStore.put("customS:" + custom, (String)objectValue);
			}
			else if (objectValue instanceof Boolean)
			{
				dataStore.putBoolean("customB:" + custom, (Boolean)objectValue);
			}
			else if (objectValue instanceof Integer)
			{
				dataStore.putInt("customI:" + custom, (Integer)objectValue);
			}
			else
			{
				throw new RuntimeException("Unsupported data type");
			}
		}
		return toReturn;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOption(String custom)
	{
		if (dataStore.hasKey("customS:" + custom))
		{
			return (T) dataStore.get("customS:" + custom).orElse(null);
		}
		else if  (dataStore.hasKey("customB:" + custom))
		{
			return (T) dataStore.getBoolean("customB:" + custom).orElse(null);
		}
		else if  (dataStore.hasKey("customI:" + custom))
		{
			OptionalInt oi = dataStore.getInt("customI:" + custom);
			if (oi.isPresent())
			{
				return (T) new Integer(oi.getAsInt());
			}
			else
			{
				return (T)null;
			}
		}
		return (T)null;
	}

	@Override
	public boolean hasOption(String custom)
	{
		return getOption(custom) != null;
	}

	@Override
	public void clearStoredConfiguration()
	{
		IsaacPreferences temp = dataStore.parent();
		try
		{
			dataStore.removeNode();
			temp.flush();
		}
		catch (BackingStoreException e)
		{
			throw new RuntimeException(e);
		}
		dataStore = temp.node(nodeName).node(userId.toString());
	}
}
