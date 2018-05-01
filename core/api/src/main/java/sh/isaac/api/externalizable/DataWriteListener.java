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
package sh.isaac.api.externalizable;

import sh.isaac.api.chronicle.Chronology;

/**
 * An interface to allow listening to writes to the datastore implementations.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public interface DataWriteListener
{
	/**
	 * Called whenever an item is written to the isaac datastores.
	 * @param data
	 */
	void writeData(Chronology data);
	
	/**
	 * Flush any buffers to disk
	 */
	void sync();
}
