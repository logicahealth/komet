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

import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BinaryOperator;

/**
 * If a backend data store wishes to provide all necessary storage for all of the
 * ISAAC datastore, they should also implement this interface.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public interface ExtendedStore
{
	/**
	 * Get an int value from the specified store name
	 * 
	 * @param store - the store to read the value from.
	 * @param key
	 * @return the value, or an empty optional if not present
	 */
	public OptionalInt getInt(String store, int key);

	/**
	 * Put an int value into the specified store name
	 * 
	 * @param store - the store to write the value to
	 * @param key
	 * @param value
	 * @return the existing value, if any
	 */
	public OptionalInt putInt(String store, int key, int value);

	/**
	 * Get a byte[] value from the specified store name
	 * 
	 * @param store - the store to read the value from.
	 * @param key
	 * @return the value, or null if not present
	 */
	public byte[] getByteArray(String store, int key);

	/**
	 * Put an byte[] into the specified store name
	 * 
	 * @param store - the store to write the value to
	 * @param key
	 * @param value
	 * @return the existing value, if any
	 */
	public byte[] putByteArray(String store, int key, byte[] value);

	/**
	 * Atomically updates the element at index {@code key} with the results of applying the given function
	 * to the current and given values, returning the updated value. The function should be side-effect-free, since
	 * it may be re-applied when attempted updates fail due to contention among threads. The function is applied with
	 * the current value at index {@code key} as its first argument, and the given update as the second argument.
	 * 
	 * @param store
	 * @param key
	 * @param newData
	 * @param accumulatorFunction
	 * @return the updated value
	 */
	public byte[] accumulateAndGetByteArray(String store, int key, byte[] newData, BinaryOperator<byte[]> accumulatorFunction);
	
	/**
	 * Return a view of all entries in the bytearray store.  It is left up to individual implemenatations, whether this is a 
	 * 'live' view, or a snapshot of the point in time when the method was called.
	 * @param store
	 * @return A set that may be iterated to get all entries
	 */
	public Set<Map.Entry<Integer,byte[]>> byteArrayEntrySet(String store);
}
