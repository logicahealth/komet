/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api.constants;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

/**
 * The database implementations that may be selected via configuration.
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public enum DatabaseImplementation {
	FILESYSTEM, XODUS, BDB, MV, POSTGRESQL, EXTERNAL, DEFAULT;
	
	private String serviceName = null;
	
	private void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}
	
	/**
	 * Return the name that should be used for a serviceLookup to HK2 for this implementation.
	 * @return
	 */
	public String getServiceName()
	{
		return StringUtils.isBlank(serviceName) ? this.name() : serviceName;
	}

	/**
	 * @param string parse the value from a string that equals the enum name value.
	 * @return the implementation, or, if it doesn't match one of the options here, 
	 * 	it will return 'EXTERNAL', with the serviceName set to the passed in string. 
	 */
	public static DatabaseImplementation parse(String string)
	{
		for (DatabaseImplementation di : DatabaseImplementation.values())
		{
			if (di.name().equals(string))
			{
				return di;
			}
		}
		DatabaseImplementation toReturn = DatabaseImplementation.EXTERNAL;
		toReturn.setServiceName(string);
		LogManager.getLogger().info("External datastore found with a service name of '{}'", string);
		return toReturn;
	}
}
