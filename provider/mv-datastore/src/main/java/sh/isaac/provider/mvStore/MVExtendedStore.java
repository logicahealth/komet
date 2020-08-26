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
package sh.isaac.provider.mvStore;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;
import org.h2.mvstore.MVMap;
import sh.isaac.api.datastore.ExtendedStoreData;

/**
 * An implementation of an Extended Store type.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * @param <K> The type of the key. Should be simple type, like String, Integer, Long, etc.
 * @param <V> The type of the value to write to the datastore. Should be a simple type, like String, Integer, Long, byte[], etc.
 * @param <VT> The type of the value that you actually want to store. You must provide the serializer / deserializer function if V does not equal VT.
 */
public class MVExtendedStore<K, V, VT> implements ExtendedStoreData<K, VT>
{
	MVMap<K, V> backingStore;
	Function<VT, V> valueSerializer;
	Function<V, VT> valueDeserializer;

	/**
	 * Construct a new MVExtended Store.
	 * 
	 * @param backingStore The backing store to use to store the data
	 * @param valueSerializer optional - The type of V must be VT if not provided. Otherwise, a function that turns VT into V.  Function must be null safe.
	 * @param valueDeserializer optional - The type of V must be VT if not provided. Otherwise, a function that turns V into VT.   Function must be null safe.
	 */
	@SuppressWarnings("unchecked")
	public MVExtendedStore(MVMap<K, V> backingStore, Function<VT, V> valueSerializer, Function<V, VT> valueDeserializer)
	{
		this.backingStore = backingStore;
		this.valueSerializer = valueSerializer == null ? (((value) -> (V) value)) : valueSerializer;
		this.valueDeserializer = valueDeserializer == null ? (((value) -> (VT) value)) : valueDeserializer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VT remove(K key)
	{
		return valueDeserializer.apply(backingStore.remove(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VT get(K key)
	{
		return valueDeserializer.apply(backingStore.get(key));
	}
	
	@Override
	public boolean containsKey(K key)
	{
		return backingStore.containsKey(key);
	}
	
	@Override
	public Set<K> keySet()
	{
		return backingStore.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VT computeIfAbsent(K key, Function<? super K, ? extends VT> mappingFunction)
	{
		return valueDeserializer.apply(backingStore.computeIfAbsent(key, (keyAgain) -> valueSerializer.apply(mappingFunction.apply(keyAgain))));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VT put(K key, VT value)
	{
		return valueDeserializer.apply(backingStore.put(key, valueSerializer.apply(value)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VT accumulateAndGet(K key, VT newData, BinaryOperator<VT> accumulatorFunction)
	{
		//final V transformedNewData = valueSerializer.apply(newData); 
		return valueDeserializer.apply(backingStore.compute(key, (keyAgain, oldValue) ->
		{
			if (oldValue == null)
			{
				return valueSerializer.apply(newData);
			}
			else
			{
				return valueSerializer.apply(accumulatorFunction.apply(valueDeserializer.apply(oldValue), newData));
			}
		}));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Entry<K, VT>> getEntrySet()
	{
		return new Set<Entry<K, VT>>()
		{
			Set<Entry<K, V>> realData = MVExtendedStore.this.backingStore.entrySet();
			
			@Override
			public int size()
			{
				return realData.size();
			}

			@Override
			public boolean isEmpty()
			{
				return realData.isEmpty();
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean contains(Object o)
			{
				return realData.contains(MVExtendedStore.this.valueSerializer.apply((VT)o));
			}

			@Override
			public Iterator<Entry<K, VT>> iterator()
			{
				Iterator<Entry<K, V>> backing = realData.iterator();
				
				return new Iterator<Entry<K, VT>>()
				{
					@Override
					public boolean hasNext()
					{
						return backing.hasNext();
					}

					@Override
					public Entry<K, VT> next()
					{
						Entry<K, V> backingEntry = backing.next();
						
						return new Entry<K, VT>()
						{
							@Override
							public K getKey()
							{
								return backingEntry.getKey(); 
							}

							@Override
							public VT getValue()
							{
								return MVExtendedStore.this.valueDeserializer.apply(backingEntry.getValue());
							}

							@Override
							public VT setValue(VT value)
							{
								return MVExtendedStore.this.valueDeserializer.apply(backingEntry.setValue(MVExtendedStore.this.valueSerializer.apply(value)));
							}};
					}};
				
			}

			@Override
			public Object[] toArray()
			{
				throw new UnsupportedOperationException("toArray not supported in iterator");
			}

			@Override
			public <T> T[] toArray(T[] a)
			{
				throw new UnsupportedOperationException("toArray not supported in iterator");
			}

			@Override
			public boolean add(Entry<K, VT> e)
			{
				throw new UnsupportedOperationException("add not supported in iterator");
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean remove(Object o)
			{
				return realData.remove(MVExtendedStore.this.valueSerializer.apply((VT)o));
			}

			@Override
			public boolean containsAll(Collection<?> c)
			{
				throw new UnsupportedOperationException("containsAll not supported in iterator");
			}

			@Override
			public boolean addAll(Collection<? extends Entry<K, VT>> c)
			{
				throw new UnsupportedOperationException("addAll not supported in iterator");
			}

			@Override
			public boolean retainAll(Collection<?> c)
			{
				throw new UnsupportedOperationException("retainAll not supported in iterator");
			}

			@Override
			public boolean removeAll(Collection<?> c)
			{
				throw new UnsupportedOperationException("removeAll not supported in iterator");
			}

			@Override
			public void clear()
			{
				realData.clear();
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearStore()
	{
		backingStore.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return backingStore.size();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long sizeAsLong()
	{
		return backingStore.sizeAsLong();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Stream<VT> getValueStream()
	{
		return backingStore.values().stream().map((in) -> valueDeserializer.apply(in));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Stream<Entry<K, VT>> getStream()
	{
		return backingStore.entrySet().stream().map((in) -> 
		{
			return new Entry<K, VT>()
			{
				@Override
				public K getKey()
				{
					return in.getKey();
				}

				@Override
				public VT getValue()
				{
					return valueDeserializer.apply(in.getValue());
				}

				@Override
				public VT setValue(VT value)
				{
					throw new UnsupportedOperationException("SetValue not supported in Stream");
				}};
		});
	}
}
