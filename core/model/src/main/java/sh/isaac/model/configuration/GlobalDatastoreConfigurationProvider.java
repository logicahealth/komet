/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.GlobalDatastoreConfiguration;
import sh.isaac.api.LookupService;
import sh.isaac.api.RemoteServiceInfo;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.util.PasswordHasher;

/**
 * The default implementation of {@link GlobalDatastoreConfiguration} which stores
 * config options for the database, and persists them, utilizing the MetaContentService.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "Default Configuration Service")
@Rank(value = 0)
@RunLevel(value=LookupService.SL_L0_METADATA_STORE_STARTED_RUNLEVEL)
public class GlobalDatastoreConfigurationProvider implements GlobalDatastoreConfiguration
{
	DefaultCoordinateProvider defaultCoordinateProvider = null;
	ConcurrentMap<String, Object> dataStore;
	Logger LOG = LogManager.getLogger();
	
	/**
	 * Instantiates a new default configuration service.
	 */
	private GlobalDatastoreConfigurationProvider()
	{
		// only for HK2
		LOG.info("Setting up Configuration Service");
		dataStore = Get.service(MetaContentService.class).<String,Object>openStore("GlobalDatastoreConfig");
		//need to delay the init of the defaultCoordianteProvider till the identifier service is up (level 2) but
		//want this service to be available for other config options before starting the DB....
	}
	
	private void initCheckCoords()
	{
		if (defaultCoordinateProvider == null)
		{
			defaultCoordinateProvider = new DefaultCoordinateProvider();
			for (ConfigurationOption dt : ConfigurationOption.values())
			{
				init(dt);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservableEditCoordinate getDefaultEditCoordinate()
	{
		initCheckCoords();
		return this.defaultCoordinateProvider.getDefaultEditCoordinate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservableLanguageCoordinate getDefaultLanguageCoordinate()
	{
		initCheckCoords();
		return this.defaultCoordinateProvider.getDefaultLanguageCoordinate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservableLogicCoordinate getDefaultLogicCoordinate()
	{
		initCheckCoords();
		return this.defaultCoordinateProvider.getDefaultLogicCoordinate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservableManifoldCoordinate getDefaultManifoldCoordinate()
	{
		initCheckCoords();
		return this.defaultCoordinateProvider.getDefaultManifoldCoordinate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservableStampCoordinate getDefaultStampCoordinate()
	{
		initCheckCoords();
		return this.defaultCoordinateProvider.getDefaultStampCoordinate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<RemoteServiceInfo> getGitConfiguration()
	{
		if (this.dataStore.containsKey("gitConfigUrl"))
		{
			return Optional.of(new RemoteServiceInfo()
			{
				
				@Override
				public String getUsername()
				{
					return (String)GlobalDatastoreConfigurationProvider.this.dataStore.get("gitConfigUsername");
				}
				
				@Override
				public String getURL()
				{
					return (String)GlobalDatastoreConfigurationProvider.this.dataStore.get("gitConfigUrl");
				}
				
				@Override
				public char[] getPassword()
				{
					try
					{
						return PasswordHasher.decryptToChars("obfuscate".toCharArray(), 
								(String)GlobalDatastoreConfigurationProvider.this.dataStore.get("gitConfigPassword"));
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				}
			});
		}
		else
		{
			return Optional.empty();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Note that passwords stored by this method are only obfuscated, and not securely encrypted.  The data store must remain
	 * protected on disk, to maintain the security of the git password, if any.
	 */
	@Override
	public void setGitConfiguration(RemoteServiceInfo gitConfiguration)
	{
		if (gitConfiguration != null)
		{
			this.dataStore.put("gitConfigUrl", gitConfiguration.getURL());
			this.dataStore.put("gitConfigUsername", gitConfiguration.getUsername());
			try
			{
				this.dataStore.put("gitConfigPassword", PasswordHasher.encrypt("obfuscate".toCharArray(), gitConfiguration.getPassword()));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			this.dataStore.remove("gitConfigUrl");
			this.dataStore.remove("gitConfigUsername");
			this.dataStore.remove("gitConfigPassword");
		}
	}

	@Override
	public void setDefaultClassifier(int conceptId)
	{
		write(ConfigurationOption.CLASSIFIER, conceptId);
	}

	@Override
	public void setDefaultDescriptionLogicProfile(int conceptId)
	{
		write(ConfigurationOption.DESCRIPTION_LOGIC_PROFILE, conceptId);
	}

	@Override
	public void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList)
	{
		write(ConfigurationOption.DESCRIPTION_TYPE_PREFERENCE_LIST, descriptionTypePreferenceList);
	}

	@Override
	public void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList)
	{
		write(ConfigurationOption.DIALECT_ASSEMBLAGE_PREFERENCE_LIST, dialectAssemblagePreferenceList);
	}

	@Override
	public void setDefaultInferredAssemblage(int conceptId)
	{
		write(ConfigurationOption.INFERRED_ASSEMBLAGE, conceptId);
	}

	@Override
	public void setDefaultLanguage(int conceptId)
	{
		write(ConfigurationOption.LANGUAGE, conceptId);
	}

	@Override
	public void setDefaultModule(int conceptId)
	{
		write(ConfigurationOption.EDIT_MODULE, conceptId);
	}

	@Override
	public void setDefaultPath(int conceptId)
	{
		write(ConfigurationOption.EDIT_PATH, conceptId);
	}

	@Override
	public void setDefaultStatedAssemblage(int conceptId)
	{
		write(ConfigurationOption.STATED_ASSEMBLAGE, conceptId);
	}

	@Override
	public void setDefaultTime(long timeInMs)
	{
		write(ConfigurationOption.TIME, timeInMs);
	}
	
	@Override
	public void setDefaultPremiseType(PremiseType premiseType)
	{
		write(ConfigurationOption.PREMISE_TYPE, premiseType.name());
	}

	@Override
	public void setDefaultUser(int conceptId)
	{
		write(ConfigurationOption.USER, conceptId);
	}
	

	private <T> Object write(ConfigurationOption option, Object data)
	{
		initCheckCoords();
		if (data == null)
		{
			throw new RuntimeException("Cannot clear default options");
		}
		switch(option) {
			case EDIT_COORDINATE:
			case LANGUAGE_COORDINATE:
			case LOGIC_COORDINATE:
			case MANIFOLD_COORDINATE:
			case STAMP_COORDINATE:
				throw new RuntimeException("Write of full object supported by global, please write individual parts");
			case CLASSIFIER:
				defaultCoordinateProvider.setDefaultClassifier((int)data);
				break;
			case DESCRIPTION_LOGIC_PROFILE:
				defaultCoordinateProvider.setDefaultDescriptionLogicProfile((int)data);
				break;
			case DESCRIPTION_TYPE_PREFERENCE_LIST:
				defaultCoordinateProvider.setDefaultDescriptionTypePreferenceList((int[])data);
				break;
			case DIALECT_ASSEMBLAGE_PREFERENCE_LIST:
				defaultCoordinateProvider.setDefaultDialectAssemblagePreferenceList((int[])data);
				break;
			case INFERRED_ASSEMBLAGE:
				defaultCoordinateProvider.setDefaultInferredAssemblage((int)data);
				break;
			case LANGUAGE:
				defaultCoordinateProvider.setDefaultLanguage((int)data);
				break;
			case EDIT_MODULE:
				defaultCoordinateProvider.setDefaultModule((int)data);
				break;
			case EDIT_PATH:
				defaultCoordinateProvider.setDefaultPath((int)data);
				break;
			case STATED_ASSEMBLAGE:
				defaultCoordinateProvider.setDefaultStatedAssemblage((int)data);
				break;
			case TIME:
				defaultCoordinateProvider.setDefaultTime((long)data);
				break;
			case PREMISE_TYPE:
				defaultCoordinateProvider.setDefaultPremiseType(PremiseType.valueOf((String)data));
				break;
			case USER:
				defaultCoordinateProvider.setDefaultUser((int)data);
				break;

			default :
				throw new RuntimeException("Oops");
		}
		return dataStore.put(option.name(), data);
	}
		
	private void init(ConfigurationOption option)
	{
		if (dataStore.containsKey(option.name()))
		{
			switch(option) {
				case EDIT_COORDINATE:
				case LANGUAGE_COORDINATE:
				case LOGIC_COORDINATE:
				case MANIFOLD_COORDINATE:
				case STAMP_COORDINATE:
					//Noops in this provider, they are configured via the options below.
					return;
				case CLASSIFIER:
					defaultCoordinateProvider.setDefaultClassifier((int)dataStore.get(option.name()));
					break;
				case DESCRIPTION_LOGIC_PROFILE:
					defaultCoordinateProvider.setDefaultDescriptionLogicProfile((int)dataStore.get(option.name()));
					break;
				case DESCRIPTION_TYPE_PREFERENCE_LIST:
					defaultCoordinateProvider.setDefaultDescriptionTypePreferenceList((int[])dataStore.get(option.name()));
					break;
				case DIALECT_ASSEMBLAGE_PREFERENCE_LIST:
					defaultCoordinateProvider.setDefaultDialectAssemblagePreferenceList((int[])dataStore.get(option.name()));
					break;
				case INFERRED_ASSEMBLAGE:
					defaultCoordinateProvider.setDefaultInferredAssemblage((int)dataStore.get(option.name()));
					break;
				case LANGUAGE:
					defaultCoordinateProvider.setDefaultLanguage((int)dataStore.get(option.name()));
					break;
				case EDIT_MODULE:
					defaultCoordinateProvider.setDefaultModule((int)dataStore.get(option.name()));
					break;
				case EDIT_PATH:
					defaultCoordinateProvider.setDefaultPath((int)dataStore.get(option.name()));
					break;
				case STATED_ASSEMBLAGE:
					defaultCoordinateProvider.setDefaultStatedAssemblage((int)dataStore.get(option.name()));
					break;
				case TIME:
					defaultCoordinateProvider.setDefaultTime((long)dataStore.get(option.name()));
					break;
				case PREMISE_TYPE:
					defaultCoordinateProvider.setDefaultPremiseType(PremiseType.valueOf((String)dataStore.get(option.name())));
					break;
				case USER:
					defaultCoordinateProvider.setDefaultUser((int)dataStore.get(option.name()));
					break;
				default :
					throw new RuntimeException("Oops");
			}
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setUser(UUID userNid)
	{
		// noop - this service doesn't care about the user.
	}

	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T putOption(ConfigurationOption option, T objectValue)
	{
		return (T)write(option, objectValue);
	}

	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOption(ConfigurationOption option)
	{
		initCheckCoords();
		switch(option) {
			case EDIT_COORDINATE:
				return (T)defaultCoordinateProvider.getDefaultEditCoordinate();
			case LANGUAGE_COORDINATE:
				return (T)defaultCoordinateProvider.getDefaultLanguageCoordinate();
			case LOGIC_COORDINATE:
				return (T)defaultCoordinateProvider.getDefaultLogicCoordinate();
			case MANIFOLD_COORDINATE:
				return (T)defaultCoordinateProvider.getDefaultManifoldCoordinate();
			case STAMP_COORDINATE:
				return (T)defaultCoordinateProvider.getDefaultStampCoordinate();
			case CLASSIFIER:
				return (T)new Integer(defaultCoordinateProvider.getDefaultLogicCoordinate().getClassifierNid());
			case DESCRIPTION_LOGIC_PROFILE:
				return (T)new Integer(defaultCoordinateProvider.getDefaultLogicCoordinate().getDescriptionLogicProfileNid());
			case DESCRIPTION_TYPE_PREFERENCE_LIST:
				return (T)defaultCoordinateProvider.getDefaultLanguageCoordinate().getDescriptionTypePreferenceList();
			case DIALECT_ASSEMBLAGE_PREFERENCE_LIST:
				return (T)defaultCoordinateProvider.getDefaultLanguageCoordinate().getDialectAssemblagePreferenceList();
			case INFERRED_ASSEMBLAGE:
				return (T)new Integer(defaultCoordinateProvider.getDefaultLogicCoordinate().getInferredAssemblageNid());
			case LANGUAGE:
				return (T)new Integer(defaultCoordinateProvider.getDefaultLanguageCoordinate().getLanguageConceptNid());
			case EDIT_MODULE:
				return(T)new Integer(defaultCoordinateProvider.getDefaultEditCoordinate().getModuleNid());
			case EDIT_PATH:
				return (T)new Integer(defaultCoordinateProvider.getDefaultEditCoordinate().getPathNid());
			case STATED_ASSEMBLAGE:
				return (T)new Integer(defaultCoordinateProvider.getDefaultLogicCoordinate().getStatedAssemblageNid());
			case TIME:
				return (T)new Long(defaultCoordinateProvider.getDefaultStampCoordinate().getStampPosition().getTime());
			case PREMISE_TYPE:
				return (T)defaultCoordinateProvider.getDefaultManifoldCoordinate().getTaxonomyPremiseType();
			case USER:
				return (T)new Integer(defaultCoordinateProvider.getDefaultEditCoordinate().getAuthorNid());
			default :
				throw new RuntimeException("Oops");
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasOption(ConfigurationOption option)
	{
		switch(option) {
			case EDIT_COORDINATE:
			case LANGUAGE_COORDINATE:
			case LOGIC_COORDINATE:
			case MANIFOLD_COORDINATE:
			case STAMP_COORDINATE:
			case CLASSIFIER:
			case USER:
			case DESCRIPTION_LOGIC_PROFILE:
			case DESCRIPTION_TYPE_PREFERENCE_LIST:
			case DIALECT_ASSEMBLAGE_PREFERENCE_LIST:
			case INFERRED_ASSEMBLAGE:
			case LANGUAGE:
			case EDIT_MODULE:
			case EDIT_PATH:
			case STATED_ASSEMBLAGE:
			case PREMISE_TYPE:
			case TIME:
				return true;
			default :
				throw new RuntimeException("oops");
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T putOption(String custom, T objectValue)
	{
		return (T)dataStore.put("custom" + custom, objectValue);
	}

	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOption(String custom)
	{
		return (T) dataStore.get("custom" + custom);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasOption(String custom)
	{
		return dataStore.containsKey("custom" + custom);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<MemoryConfiguration> getMemoryConfiguration()
	{
		String temp = System.getProperty(SystemPropertyConstants.DATA_STORE_MEMORY_CONFIG_PROPERTY);
		MemoryConfiguration mcFromSystem = null;
		
		if (StringUtils.isNotBlank(temp))
		{
			try
			{
				mcFromSystem = MemoryConfiguration.valueOf(temp);
				if (mcFromSystem == null)
				{
					LOG.warn("Ignoring invalid value '{}' for system property '{}'", temp, SystemPropertyConstants.DATA_STORE_MEMORY_CONFIG_PROPERTY);
				}
			}
			catch (Exception e)
			{
				LOG.warn("Ignoring invalid value '{}' for system property '{}'", temp, SystemPropertyConstants.DATA_STORE_MEMORY_CONFIG_PROPERTY);
			}
		}
		String enumName = getOption(MemoryConfiguration.class.getName());
		MemoryConfiguration mcFromProps = null;
		if (StringUtils.isNotBlank(enumName))
		{
			mcFromProps = MemoryConfiguration.valueOf(enumName);
		}
		
		if (mcFromSystem != null && mcFromProps != null)
		{
			LOG.info("Overriding the Memory configuration of {} with the value {} from a system property", mcFromProps, mcFromSystem);
			return Optional.of(mcFromSystem);
		}
		
		return Optional.ofNullable(mcFromSystem == null ? mcFromProps : mcFromSystem);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMemoryConfiguration(MemoryConfiguration memoryConfiguration)
	{
		putOption(MemoryConfiguration.class.getName(), memoryConfiguration.name());
	}

	@Override
	public void clearStoredConfiguration()
	{
		dataStore.clear();
		defaultCoordinateProvider = new DefaultCoordinateProvider();
	}
}
