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

import java.util.OptionalLong;
import java.util.function.Function;

/**
 * If a backend data store wishes to provide all necessary storage for all of the
 * ISAAC datastore, they should also implement this interface.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public interface ExtendedStore extends DataStore
{
	/**
	 * Get a long value from the shared store.  Make sure you use service-specific keys
	 * if you make use of the shared store.  This is useful for services that want to store one
	 * or two data points.
	 * 
	 * @param key The service-unique key - recommend prefixing your key with your service name.
	 * @return the value, or empty optional if not present
	 */
	public OptionalLong getSharedStoreLong(String key);

	/**
	 * Put a long value into the shared store.  Make sure you use service-specific keys
	 * if you make use of the shared store.  This is useful for services that want to store one
	 * or two data points.
	 * 
	 * @param key The service-unique key - recommend prefixing your key with your service name.
	 * @param value the value to store.
	 * @return the existing value, if any
	 */
	public OptionalLong putSharedStoreLong(String key, long value);
	
	/**
	 * Remove a long value from the shared store.  Make sure you use service-specific keys
	 * if you make use of the shared store.  This is useful for services that want to store one
	 * or two data points.
	 * 
	 * @param key The service-unique key - recommend prefixing your key with your service name.
	 * @return the existing value, if any
	 */
	public OptionalLong removeSharedStoreLong(String key);
	
	/**
	 * @param storeName
	 * @return A store that handles the specified types.  This constructor should only be used with simple types, 
	 * which can be handled by the implementations default serialization mechanism.  
	 */
	public <K, V> ExtendedStoreData<K, V> getStore(String storeName);
	
	/**
	 * @param <K> The type of the key.  Should be simple type, like String, UUID, byte[], and the class versions of the primitive types.
	 * @param <V> The type of the value to write to the datastore.  Should be a simple type, String, UUID, byte[], and the class versions of the primitive types.
	 * @param <VT> The type of the value that you actually want to store.  You must provide the serializer / deserializer function.
	 * @param storeName The name of the store
	 * @param valueSerializer The function that will turn a type <V> into <VT>.  Function must be null safe.
	 * @param valueDeserializer The function that will turn a type <VT> into <V>.  Function must be null safe.
	 * @return A store that handles the specified types.
	 */
	public <K, V, VT> ExtendedStoreData<K, VT> getStore(String storeName, Function<VT, V> valueSerializer, Function<V, VT> valueDeserializer);
}
