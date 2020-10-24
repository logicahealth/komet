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
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The interface for getting back extended stores
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
public interface ExtendedStoreData<K, V>
{
	/**
	 * Remove a value from the store.
	 * 
	 * @param key The key 
	 * @return the existing value, if any
	 */
	public V remove(K key);

	/**
	 * Get a value from the specified store name
	 * 
	 * @param key
	 * @return the value, or null if not present
	 */
	public V get(K key);
	
	/**
	 * @param key
	 * @return true, if the store contains the specified key
	 */
	public boolean containsKey(K key);
	
	/**
	 * @return All the keys
	 */
	public Set<K> keySet();
	
	/**
	 * Atomically compute a value for a given key, only if the key is absent.
	 * Otherwise, return the existing value for the key.
	 * @param key
	 * @param mappingFunction
	 * @return the found, or created value.
	 */
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

	/**
	 * Put a value into the store
	 * 
	 * @param key
	 * @param value
	 * @return the existing value, if any
	 */
	public V put(K key, V value);

	/**
	 * Atomically updates the element at index {@code key} with the results of applying the given function
	 * to the current and given values, returning the updated value. The function should be side-effect-free, since
	 * it may be re-applied when attempted updates fail due to contention among threads. The function is applied with
	 * the current value at index {@code key} as its first argument, and the given update as the second argument.
	 * 
	 * @param key
	 * @param newData
	 * @param accumulatorFunction
	 * @return the updated value
	 */
	public V accumulateAndGet(K key, V newData, BinaryOperator<V> accumulatorFunction);
	
	/**
	 * Return a view of all entries in the store.  It is left up to individual implementations, whether this is a 
	 * 'live' view, or a snapshot of the point in time when the method was called.
	 * @return A set that may be iterated to get all entries
	 */
	public Set<Map.Entry<K, V>> getEntrySet();
	
	/**
	 * Erase an entire store.
	 */
	public void clearStore();
	
	/**
	 * Return the size of a store
	 * @return the size
	 */
	public int size();
	
	/**
	 * Return the size of a store
	 * @return the size
	 */
	public long sizeAsLong();
	
	/**
	 * @param parallel true to allow parallel, false for single threaded
	 * @return the values as a stream
	 */
	public Stream<V> getValueStream(boolean parallel);
	
	/**
	 * @param parallel true to allow parallel, false for single threaded
	 * @return the key value pairs as a stream
	 */
	public Stream<Entry<K, V>> getStream(boolean parallel);
}
