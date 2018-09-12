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
package sh.isaac.api.datastore;

import java.util.List;
import sh.isaac.api.chronicle.Chronology;

/**
 * Serialization mechanisms for a Chronology
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public interface ChronologySerializeable extends Chronology
{
	/**
	 * A method to support serialization of the data.
	 * 
	 * Get data to write to datastore - chronology and version data.
	 *
	 * @return the data to write. Implementations of {@link DataStore#getChronologyVersionData(int)} need to know how to construct themselves
	 *         from this data.
	 */
	public byte[] getChronologyVersionDataToWrite();

	/**
	 * A method to support serialization of the data.
	 * 
	 * An alternative to the {@link #getChronologyVersionDataToWrite} mechanism, where the store is allowed to get the
	 * chronology info separate from the version info. See {@link #getVersionDataToWrite()}
	 * 
	 * Backing stores should only store the underlying bytes, and not attempt to serialize the ByteArrayDataBuffer.
	 * 
	 * @return The buffer that contains the chronology bytes to write. Implementations of {@link DataStore#getChronologyVersionData(int)}
	 *         should return these bytes + all of the bytes provided by {@link #getVersionDataToWrite()}
	 */
	public byte[] getChronologyDataToWrite();

	/**
	 * A method to support serialization of the data.
	 * 
	 * An alternative to the {@link #getChronologyVersionDataToWrite} mechanism, where the store is allowed to get the
	 * chronology info separate from the version info. See {@link #getChronologyDataToWrite()}
	 * 
	 * Backing stores should only store the underlying bytes, and not attempt to serialize the ByteArrayDataBuffer.
	 * 
	 * @return The versions to be written. Backing stores may ignore any versions they already have stored.
	 *         Implementations of {@link DataStore#getChronologyVersionData(int)}
	 *         should return all of the bytes provided by {@link #getChronologyDataToWrite()} + all of the bytes from each item
	 *         in the list (with no additional list / item marker bytes).
	 */
	public List<byte[]> getVersionDataToWrite();
}
