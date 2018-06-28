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
package sh.isaac.model.collections;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * A thin wrapper for the eclipse implementation of an intObject map to align the 
 * conflicting return types of put.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * @param <E> The type of the object held in the map
 */
public class EclipseIntObjectMap<E> implements IntObjectMap<E> 
{

	IntObjectHashMap<AtomicReference<E>> map_;
	
	public EclipseIntObjectMap(int initialCapacity)
	{
		map_ = new IntObjectHashMap<>(initialCapacity);
	}
	
	public EclipseIntObjectMap()
	{
		map_ = new IntObjectHashMap<>();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Optional<E> getOptional(int key)
	{
		AtomicReference<E> result = map_.get(key);
		return result == null ? Optional.empty(): Optional.ofNullable(result.get());
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void put(int key, E value)
	{
		map_.getIfAbsentPut(key, new AtomicReference<E>()).set(value);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public E getAndSet(int key, E value)
	{
		return map_.getIfAbsentPut(key, new AtomicReference<E>()).getAndSet(value);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public E get(int key)
	{
		AtomicReference<E> result = map_.get(key);
		return result == null ? null : result.get();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsKey(int key)
	{
		return map_.containsKey(key);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return map_.size();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		map_.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forEach(IntBiConsumer<E> consumer)
	{
		map_.forEachKeyValue((key, value) ->
		{
			consumer.accept(key, value.get());
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E accumulateAndGet(int key, E newValue, BinaryOperator<E> accumulatorFunction)
	{
		return map_.getIfAbsentPut(key, new AtomicReference<E>()).accumulateAndGet(newValue, accumulatorFunction);
	}
}
