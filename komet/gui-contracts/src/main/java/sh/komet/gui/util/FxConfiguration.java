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
package sh.komet.gui.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.UserConfiguration;
import sh.isaac.api.UserConfiguration.ConfigurationStore;

/**
 * Komet FX Application specific options.  
 * This class to be extended as necessary, as we add GUI-specific configuration options to the UI.
 * 
 * This is for things like, use this font size, etc - things that don't belong in the Isaac core 
 * {@link ConfigurationService}.
 * 
 * This does however, build on top of the {@link UserConfiguration} for storage of set options.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class FxConfiguration
{
	private static final String USER_CSS_LOCATION = "USER_CSS_LOCATION";
	private static final String SHOW_BETA_PROPERTY = "SHOW_BETA_FEATURES";
	
	private Logger LOG = LogManager.getLogger();
	
	UserConfiguration ucStore;
	
	protected FxConfiguration()
	{
		//Accessed via FxGet
		//TODO handle passing in a proper user id in the future
		ucStore = Get.configurationService().getUserConfiguration(Optional.empty());
	}
	
	/**
	 * @return true, if beta features have been requested, false otherwise.
	 * 
	 * This defaults to false.  It may be changed via {@link #setBetaFeaturesEnabled(ConfigurationStore, boolean)}
	 * 
	 *  If the System property {@link #SHOW_BETA_PROPERTY} is specified, this overrides the stored
	 *  value.  If set to true, it enables the property, otherwise, it disables the property.
	 */
	public boolean isShowBetaFeaturesEnabled()
	{
		String temp = System.getProperty(SHOW_BETA_PROPERTY);
		if (StringUtils.isNotBlank(temp))
		{
			return Boolean.parseBoolean(temp);
		}
		Boolean enabled = ucStore.getObject(SHOW_BETA_PROPERTY);
		
		return enabled == null ? false : enabled.booleanValue();
	}
	
	/**
	 * Toggle the beta features on/off
	 * @param store Store this setting with the DB, or in the user profile
	 * @param enabled
	 */
	public void setBetaFeaturesEnabled(ConfigurationStore store, boolean enabled)
	{
		ucStore.setObject(store, SHOW_BETA_PROPERTY, enabled);
	}
	
	/**
	 * Returns the URL for the user.css file by reading options in this order:
	 * 
	 * 1) the value of the system property {@link #USER_CSS_LOCATION} if it is set, and it points to an existing file.
	 * 2) the value, if any, stored in the UserConfiguration store
	 * 3) If we detect we are running in a development environment, return the JVM launch point relative path to 
	 *    css/src/main/resources/user.css (if that file exists)
	 * 4) the proper class-path URL to the default packaging location.
	 * 
	 * @return The typical operation is to return 3 or 4.
	 */
	public URL getUserCSSURL()
	{
		try
		{
			String temp = System.getProperty(USER_CSS_LOCATION);
			if (StringUtils.isNotBlank(temp) && new File(temp).isFile())
			{
				return new File(temp).toURI().toURL();
			}
			else
			{
				LOG.warn("Ignoring {} system property because it doesn't point to an existing file: {}", USER_CSS_LOCATION, temp);
			}
			
			String s = ucStore.getObject(USER_CSS_LOCATION);
			if (StringUtils.isNotBlank(s))
			{
				return new URL(s);
			}
			else
			{
				//This should be the path, if we launched the JVM from the 'application' module
				File f = Paths.get("..", "css", "src", "main", "resources", "user.css").toFile();
				if (f.isFile())
				{
					return f.toURI().toURL();
				}
				//There are also (apparently) parts of the maven build that try to find this file prior to the package being built, for 
				//reasons I don't understand, so try this path:
				f = Paths.get("komet", "css", "src", "main", "resources", "user.css").toFile();
				if (f.isFile())
				{
					return f.toURI().toURL();
				}
			}
			
			return FxConfiguration.class.getResource("/user.css");
		}
		catch (MalformedURLException e)
		{
			//should be impossible
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Store a path to the user.css file in the user prefs.
	 * @param store store per DB or in the user profile folder
	 * @param url the url to store
	 */
	public void setUserCSSURL(ConfigurationStore store, URL url) 
	{
		ucStore.setObject(store, USER_CSS_LOCATION, url.toString());
	}
}
