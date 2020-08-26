/*
 * Copyright 2020 Mind Computing Inc, Sagebits LLC
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
package sh.isaac.api.datastore.extendedStore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.datastore.ExtendedStoreData;

/**
 * Just a combination of the two extended store related interfaces
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * 
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
@Contract
public abstract class ExtendedStoreWithSerializer<K, V> implements ExtendedStoreData<K, V>
{
	private ConcurrentHashMap<K, V> backingMap = new ConcurrentHashMap<>();

	protected ExtendedStoreWithSerializer()
	{
		//for HK2 and subclasses
	}

	/**
	 * The provided serializers should map your external type into a supported {@link SimpleTypeStoreHandler} type, and back again.
	 * 
	 * @param dis
	 * @param valueSerializer - optional custom serializer
	 * @param valueDeserializer  - optional custom deserializer
	 * @throws IOException 
	 */
	public void init(DataInputStream dis, Function<Object, Object> valueSerializer, Function<Object, Object> valueDeserializer) throws IOException
	{
		int size = dis.readInt();
		for (int i = 0; i < size; i++)
		{
			Map.Entry<K, V> entry = readEntry(dis);
			backingMap.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @param dos
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void serialize(DataOutputStream dos) throws IOException
	{
		HashMap<K, V> mapToWrite = new HashMap<>(backingMap);
		
		writeMeta(dos, mapToWrite.size() > 0 ? (Map.Entry<Object, Object>)mapToWrite.entrySet().iterator().next() : null);
		dos.writeInt(mapToWrite.size());
		for (Entry<K, V> entry : mapToWrite.entrySet())
		{
			writeEntry((Entry<Object, Object>) entry, dos);
		}
	}
	
	abstract public Map.Entry<K, V> readEntry(DataInputStream dis) throws IOException;
	abstract public void writeEntry(Map.Entry<Object, Object> entry, DataOutputStream dos) throws IOException;
	abstract public void writeMeta(DataOutputStream dos, Map.Entry<Object, Object> sampleEntry) throws IOException;

	/**
	 * Must align with the name in your annotation: @Service (name="theNameHere")
	 * 
	 * @return the service name
	 */
	abstract public String getServiceName();

	@Override
	public V remove(K key)
	{
		return backingMap.remove(key);
	}

	@Override
	public V get(K key)
	{
		return backingMap.get(key);
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return backingMap.containsKey(key);
	}

	@Override
	public Set<K> keySet()
	{
		return backingMap.keySet();
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
	{
		return backingMap.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public V put(K key, V value)
	{
		return backingMap.put(key, value);
	}

	@Override
	public V accumulateAndGet(K key, V newData, BinaryOperator<V> accumulatorFunction)
	{
		return backingMap.merge(key, newData, (newValue, existingValue) ->
		{
			return accumulatorFunction.apply(newValue, existingValue);
		});
	}

	@Override
	public Set<Map.Entry<K, V>> getEntrySet()
	{
		return backingMap.entrySet();
	}

	@Override
	public void clearStore()
	{
		backingMap.clear();
	}

	@Override
	public int size()
	{
		return backingMap.size();
	}

	@Override
	public long sizeAsLong()
	{
		return backingMap.size();
	}

	@Override
	public Stream<V> getValueStream()
	{
		return backingMap.values().stream();
	}

	@Override
	public Stream<Map.Entry<K, V>> getStream()
	{
		return backingMap.entrySet().stream();
	}
}
