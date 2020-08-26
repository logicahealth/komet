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
package sh.isaac.model;

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.datastore.DataStore;

/**
 * Methods required to make the selectable-backends work for the DataStore.
 * 
 * Implementations of this class should have the annotations:
 * Service (name="theName")  
 * Singleton
 * 
 * And the name value should be identical to a value in {@link DatabaseImplementation}
 * They should NOT have a runlevel.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Contract
public interface DataStoreSubService extends DataStore
{
	/**
	 * This method should not be called by end users.  It only exists for internal usage.
	 * Start or stop the system via RunLevel changes.
	 */
	public void startup();

	/**
	 * This method should not be called by end users.  It only exists for internal usage.
	 * Start or stop the system via RunLevel changes.
	 */
	public void shutdown();
	
	/**
	 * @return The implementation type of the data store in use
	 */
	public DatabaseImplementation getDataStoreType();
}
