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

import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.GlobalDatastoreConfiguration;
import sh.isaac.api.UserConfiguration;
import sh.isaac.api.UserConfigurationInternalImpl;
import sh.isaac.api.UserConfigurationInternalImpl.ConfigurationOption;
import sh.isaac.api.UserConfigurationPerDB;
import sh.isaac.api.UserConfigurationPerOSUser;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.model.coordinate.ManifoldCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;

/**
 * Get or set various options that a user might want to set in their environment.
 * 
 * See {@link UserConfiguration}
 * 
 * Note, you should NOT directly request or use an instance of {@link UserConfigurationImpl} or  {@link UserConfiguration}- 
 * instead, you should always request the instance via {@link ConfigurationService#getUserConfiguration(Optional)}.  This will
 * ensure that the user configuration is initialized properly.
 * 
 * The services that provide storage, and the defaults are all looked up via HK2, so these may be swapped out at runtime by 
 * providing higher ranking implementations of {@link GlobalDatastoreConfiguration}, {@link UserConfigurationPerDB} 
 * and {@link UserConfigurationPerOSUser}
 * 
 * An instance of this service is created per-user.  There is per-user caching of this service in 
 * {@link ConfigurationServiceProvider#getUserConfiguration(Optional)}.
 * 
 * If the service is requested without providing a userId:
 *  - If we are in {@link ConfigurationService#setSingleUserMode(boolean)}, the service provider will utilize 
 *    {@link ConfigurationService#getCurrentUserId()}
 *  - If we are NOT in single user mode, and no nid was specified during the creation of the service, the service will
 *    only return default values, and calls to any setters will fail with a runtime exception.
 * 
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */

@Service(name = "Default Configuration Service")
@Rank(value = 0)
@PerLookup
public class UserConfigurationImpl implements UserConfiguration
{
	Logger LOG = LogManager.getLogger();
	
	private UserConfigurationPerDB dbConfig = null;
	private UserConfigurationPerOSUser osConfig = null;
	private GlobalDatastoreConfiguration globalConfig = null;
	
	private ObservableEditCoordinate editCoordinate;
	private ObservableLanguageCoordinate languageCoordinate;
	private ObservableLogicCoordinate logicCoordinate;
	private ObservableStampCoordinate stampCoordinate;
	private ObservableManifoldCoordinate manifoldCoordinate;
	
	private Optional<UUID> userConcept;
	
	private UserConfigurationImpl()
	{
		//For HK2
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void finishInit(Optional<UUID> userConcept)
	{
		LOG.debug("Init UserConfig for {}", userConcept);
		if (globalConfig != null)
		{
			throw new RuntimeException("finishInit should not be called more than once");
		}
		this.userConcept = userConcept;
		if (userConcept.isPresent())
		{
			dbConfig = Get.service(UserConfigurationPerDB.class);
			if (dbConfig != null)
			{
				dbConfig.setUser(userConcept.get());
			}
			osConfig = Get.service(UserConfigurationPerOSUser.class);
			if (osConfig != null)
			{
				osConfig.setUser(userConcept.get());
			}
		}
		//If they didn't pass a user, we can't have any user specific prefs. will let all calls fall through to the globalConfig
		
		globalConfig = Get.service(GlobalDatastoreConfiguration.class);
		if (globalConfig == null) {
			LOG.warn("No GlobalDatastoreConfiguration available on the classpath!");
		}
		
		//Configure our cached objects
		editCoordinate = globalConfig.getDefaultEditCoordinate().deepClone();
		editCoordinate.authorNidProperty().set(userConcept.isPresent() ? Get.identifierService().getNidForUuids(userConcept.get()) 
				: globalConfig.getDefaultEditCoordinate().getAuthorNid());
		editCoordinate.moduleNidProperty().set(getOption(ConfigurationOption.EDIT_MODULE));
		editCoordinate.pathNidProperty().set(getOption(ConfigurationOption.EDIT_PATH));
		
		//TODO add setters / options for things below that aren't yet being set?
		languageCoordinate = globalConfig.getDefaultLanguageCoordinate().deepClone();
		languageCoordinate.descriptionTypePreferenceListProperty().get().setAll((int[])getOption(ConfigurationOption.DESCRIPTION_TYPE_PREFERENCE_LIST));
		languageCoordinate.dialectAssemblagePreferenceListProperty().get().setAll((int[])getOption(ConfigurationOption.DIALECT_ASSEMBLAGE_PREFERENCE_LIST));
		languageCoordinate.languageConceptNidProperty().set(getOption(ConfigurationOption.LANGUAGE));
		//languageCoordinate.nextProrityLanguageCoordinateProperty();
		
		logicCoordinate = globalConfig.getDefaultLogicCoordinate().deepClone();
		logicCoordinate.classifierNidProperty().set(getOption(ConfigurationOption.CLASSIFIER));
		//logicCoordinate.conceptAssemblageNidProperty()
		logicCoordinate.descriptionLogicProfileNidProperty().set(getOption(ConfigurationOption.DESCRIPTION_LOGIC_PROFILE));
		logicCoordinate.inferredAssemblageNidProperty().set(getOption(ConfigurationOption.INFERRED_ASSEMBLAGE));
		logicCoordinate.statedAssemblageNidProperty().set(getOption(ConfigurationOption.STATED_ASSEMBLAGE));
		
		stampCoordinate = globalConfig.getDefaultStampCoordinate().deepClone();
		//stampCoordinate.allowedStatesProperty();
		//stampCoordinate.moduleNidsProperty();
		//stampCoordinate.stampPositionProperty().get().stampPathNidProperty();
		stampCoordinate.stampPositionProperty().get().timeProperty().set(getOption(ConfigurationOption.TIME));
		//stampCoordinate.stampPrecedenceProperty()
		
		manifoldCoordinate = new ObservableManifoldCoordinateImpl(new ManifoldCoordinateImpl(PremiseType.STATED, stampCoordinate, languageCoordinate, logicCoordinate));
		manifoldCoordinate.taxonomyPremiseTypeProperty().set(getOption(ConfigurationOption.PREMISE_TYPE));
		//manifoldCoordinate.uuidProperty();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public ObservableEditCoordinate getEditCoordinate()
	{
		//TODO currently, if the user edits individual parts of any of the Observables we return from this class, they won't get stored back.
		//Perhaps we should listen / store - but - how would we know which service to write it back to?  
		//TODO we should really be returning these as ReadOnly observables
		return editCoordinate;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public ObservableLanguageCoordinate getLanguageCoordinate()
	{
		return languageCoordinate;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public ObservableLogicCoordinate getLogicCoordinate()
	{
		return logicCoordinate;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public ObservableManifoldCoordinate getManifoldCoordinate()
	{
		return manifoldCoordinate;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public ObservableStampCoordinate getStampCoordinate()
	{
		return stampCoordinate;
	}

	private UserConfigurationInternalImpl findServiceWithValue(ConfigurationOption option)
	{
		if (dbConfig != null && dbConfig.hasOption(option))
		{
			return dbConfig;
		}
		else if (osConfig != null && osConfig.hasOption(option))
		{
			return osConfig;
		}
		else if (globalConfig.hasOption(option))
		{
			return globalConfig;
		}
		return null;
	}
	
	private UserConfigurationInternalImpl findServiceWithValue(String custom)
	{
		if (dbConfig != null && dbConfig.hasOption(custom))
		{
			return dbConfig;
		}
		else if (osConfig != null && osConfig.hasOption(custom))
		{
			return osConfig;
		}
		else if (globalConfig.hasOption(custom))
		{
			return globalConfig;
		}
		return null;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public <T> T getObject(String objectKey)
	{
		UserConfigurationInternalImpl uc = findServiceWithValue(objectKey);
		if (uc != null)
		{
			return uc.getOption(objectKey);
		}
		return null;
	}


	/** 
	 * {@inheritDoc}
	 */
	@Override
	public <T> T setObject(ConfigurationStore store, String objectKey, T objectValue)
	{
		switch(store)
		{
			case DATABASE:
				if (dbConfig == null)
				{
					throw new RuntimeException("No database configuration store is available");
				}
				return dbConfig.putOption(objectKey, objectValue);
			case PROFILE:
				if (osConfig == null)
				{
					throw new RuntimeException("No profile configuration store is available");
				}
				return osConfig.putOption(objectKey, objectValue);
			default :
				throw new RuntimeException("oops");
			
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getOption(ConfigurationOption option)
	{
		switch(option) {
			case EDIT_COORDINATE:
				return (T)getEditCoordinate();
			case LANGUAGE_COORDINATE:
				return (T)getLanguageCoordinate();
			case LOGIC_COORDINATE:
				return (T)getLogicCoordinate();
			case MANIFOLD_COORDINATE:
				return (T)getManifoldCoordinate();
			case STAMP_COORDINATE:
				return (T)getStampCoordinate();
			default :
				UserConfigurationInternalImpl uc = findServiceWithValue(option);
				if (uc != null)
				{
					return (T)uc.getOption(option);
				}
				return null;
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public <T> Object setOption(ConfigurationStore store, ConfigurationOption option, T objectValue)
	{
		if (objectValue != null)
		{
			if (!(objectValue.getClass().isAssignableFrom(option.getType())))
			{
				throw new RuntimeException("Invalid data type for " + option.name()); 
			}
		}
		T toReturn;
		boolean shouldUpdateObject = false;
		switch(store)
		{
			case DATABASE:
				if (dbConfig == null)
				{
					throw new RuntimeException("No database configuration store is available");
				}
				toReturn =  dbConfig.putOption(option, objectValue);
				shouldUpdateObject = true;  //the database pref overrides the user pref, so always update the object
				break;
			case PROFILE:
				if (osConfig == null)
				{
					throw new RuntimeException("No profile configuration store is available");
				}
				toReturn = osConfig.putOption(option, objectValue);
				if (dbConfig == null || !dbConfig.hasOption(option))
				{
					shouldUpdateObject = true;  //only set the object if the database config isn't overriding it
				}
				break;
			default :
				throw new RuntimeException("oops");
		}
		
		if (shouldUpdateObject)
		{
			if (objectValue == null)
			{
				switch(option)
				{
					case CLASSIFIER:
						logicCoordinate.classifierNidProperty().set(getOption(ConfigurationOption.CLASSIFIER));
						break;
					case DESCRIPTION_LOGIC_PROFILE:
						logicCoordinate.descriptionLogicProfileNidProperty().set(getOption(ConfigurationOption.DESCRIPTION_LOGIC_PROFILE));
						break;
					case DESCRIPTION_TYPE_PREFERENCE_LIST:
						languageCoordinate.descriptionTypePreferenceListProperty().get().setAll((int[])getOption(ConfigurationOption.DESCRIPTION_TYPE_PREFERENCE_LIST));
						break;
					case DIALECT_ASSEMBLAGE_PREFERENCE_LIST:
						languageCoordinate.dialectAssemblagePreferenceListProperty().get().setAll((int[])getOption(ConfigurationOption.DIALECT_ASSEMBLAGE_PREFERENCE_LIST));
						break;
					case EDIT_MODULE:
						editCoordinate.moduleNidProperty().set(getOption(ConfigurationOption.EDIT_MODULE));
						break;
					case EDIT_PATH:
						editCoordinate.pathNidProperty().set(getOption(ConfigurationOption.EDIT_PATH));
						break;
					case INFERRED_ASSEMBLAGE:
						logicCoordinate.inferredAssemblageNidProperty().set(getOption(ConfigurationOption.INFERRED_ASSEMBLAGE));
						break;
					case LANGUAGE:
						languageCoordinate.languageConceptNidProperty().set(getOption(ConfigurationOption.LANGUAGE));
						break;
					case PREMISE_TYPE:
						manifoldCoordinate.taxonomyPremiseTypeProperty().set(getOption(ConfigurationOption.PREMISE_TYPE));
						break;
					case STATED_ASSEMBLAGE:
						logicCoordinate.statedAssemblageNidProperty().set(getOption(ConfigurationOption.STATED_ASSEMBLAGE));
						break;
					case TIME:
						stampCoordinate.stampPositionProperty().get().timeProperty().set(getOption(ConfigurationOption.TIME));
						break;
					case STAMP_COORDINATE:
					case EDIT_COORDINATE:
					case LANGUAGE_COORDINATE:
					case LOGIC_COORDINATE:
					case MANIFOLD_COORDINATE:
						throw new RuntimeException("sets of coordinate objects not supported, please call individual setters");
					case USER:
						throw new RuntimeException("User is for internal use only");
					default :
						throw new RuntimeException("oops");
				}
			}
			else
			{
				switch(option)
				{
					case CLASSIFIER:
						logicCoordinate.classifierNidProperty().set((Integer)objectValue);
						break;
					case DESCRIPTION_LOGIC_PROFILE:
						logicCoordinate.descriptionLogicProfileNidProperty().set((Integer)objectValue);
						break;
					case DESCRIPTION_TYPE_PREFERENCE_LIST:
						languageCoordinate.descriptionTypePreferenceListProperty().get().setAll((int[])objectValue);
						break;
					case DIALECT_ASSEMBLAGE_PREFERENCE_LIST:
						languageCoordinate.dialectAssemblagePreferenceListProperty().get().setAll((int[])objectValue);
						break;
					case EDIT_MODULE:
						editCoordinate.moduleNidProperty().set((Integer)objectValue);
						break;
					case EDIT_PATH:
						editCoordinate.pathNidProperty().set((Integer)objectValue);
						break;
					case INFERRED_ASSEMBLAGE:
						logicCoordinate.inferredAssemblageNidProperty().set((Integer)objectValue);
						break;
					case LANGUAGE:
						languageCoordinate.languageConceptNidProperty().set((Integer)objectValue);
						break;
					case PREMISE_TYPE:
						manifoldCoordinate.taxonomyPremiseTypeProperty().set((PremiseType)objectValue);
						break;
					case STATED_ASSEMBLAGE:
						logicCoordinate.statedAssemblageNidProperty().set((Integer)objectValue);
						break;
					case TIME:
						stampCoordinate.stampPositionProperty().get().timeProperty().set((Long)objectValue);
						break;
					case STAMP_COORDINATE:
					case EDIT_COORDINATE:
					case LANGUAGE_COORDINATE:
					case LOGIC_COORDINATE:
					case MANIFOLD_COORDINATE:
						throw new RuntimeException("sets of coordinate objects not supported, please call individual setters");
					case USER:
						throw new RuntimeException("User is for internal use only");
					default :
						throw new RuntimeException("oops");
				}
			}
		}
		return toReturn;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Optional<UUID> getUserId()
	{
		return userConcept;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setClassifier(ConfigurationStore store, int conceptId)
	{
		setOption(store, ConfigurationOption.CLASSIFIER, conceptId);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setDescriptionLogicProfile(ConfigurationStore store, int conceptId)
	{
		setOption(store, ConfigurationOption.DESCRIPTION_LOGIC_PROFILE, conceptId);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setDescriptionTypePreferenceList(ConfigurationStore store, int[] descriptionTypePreferenceList)
	{
		setOption(store, ConfigurationOption.DESCRIPTION_TYPE_PREFERENCE_LIST, descriptionTypePreferenceList);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setDialectAssemblagePreferenceList(ConfigurationStore store, int[] dialectAssemblagePreferenceList)
	{
		setOption(store, ConfigurationOption.DIALECT_ASSEMBLAGE_PREFERENCE_LIST, dialectAssemblagePreferenceList);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setInferredAssemblage(ConfigurationStore store, int conceptId)
	{
		setOption(store, ConfigurationOption.INFERRED_ASSEMBLAGE, conceptId);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setLanguage(ConfigurationStore store, int conceptId)
	{
		setOption(store, ConfigurationOption.LANGUAGE, conceptId);
	}
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setEditModule(ConfigurationStore store, int conceptId)
	{
		setOption(store, ConfigurationOption.EDIT_MODULE, conceptId);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setEditPath(ConfigurationStore store, int conceptId)
	{
		setOption(store, ConfigurationOption.EDIT_PATH, conceptId);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setStatedAssemblage(ConfigurationStore store, int conceptId)
	{
		setOption(store, ConfigurationOption.STATED_ASSEMBLAGE, conceptId);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setPremiseType(ConfigurationStore store, PremiseType premiseType)
	{
		setOption(store, ConfigurationOption.PREMISE_TYPE, premiseType);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setTime(ConfigurationStore store, long timeInMs)
	{
		setOption(store, ConfigurationOption.TIME, timeInMs);
	}

	@Override
	public void clearConfiguration(ConfigurationStore store)
	{
		switch(store)
		{
			case DATABASE:
				if (dbConfig == null)
				{
					throw new RuntimeException("User not configured, can't clear");
				}
				dbConfig.clearStoredConfiguration();
				break;
			case PROFILE:
				if (osConfig == null)
				{
					throw new RuntimeException("User not configured, can't clear");
				}
				osConfig.clearStoredConfiguration();
				break;
			default :
				throw new RuntimeException("oops");			
		}
		for (ConfigurationOption co : ConfigurationOption.values())
		{
			if (co == ConfigurationOption.EDIT_COORDINATE || co == ConfigurationOption.LANGUAGE_COORDINATE || co == ConfigurationOption.LOGIC_COORDINATE ||
					co == ConfigurationOption.MANIFOLD_COORDINATE || co == ConfigurationOption.STAMP_COORDINATE || co == ConfigurationOption.USER)
			{
				continue;
			}
			setOption(store, co, null);
		}
	}
}
