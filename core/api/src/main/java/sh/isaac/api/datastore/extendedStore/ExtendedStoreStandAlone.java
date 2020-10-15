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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.datastore.ExtendedStoreData;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * @param <K>
 * @param <VE>
 * @param <VI> 
  */

public class ExtendedStoreStandAlone<K, VI, VE> implements ExtendedStoreData<K, VE>
{
	private ExtendedStoreWithSerializer<K, VE> data;
	private File storageLocation;
	private static final Logger LOG = LogManager.getLogger();

	@SuppressWarnings("unchecked")
	/**
	 * @param storageLocation
	 * @param valueSerializer - optional - a function that takes an external type to a type that {@link SimpleTypeStoreHandler} can handle
	 * @param valueDeserializer - optional - a function that reverses the serialization
	 */
	public ExtendedStoreStandAlone(File storageLocation, Function<VE, VI> valueSerializer, Function<VI, VE> valueDeserializer)
	{
		this.storageLocation = storageLocation;
		try
		{
			if (!storageLocation.exists())
			{
				storageLocation.getParentFile().mkdirs();
				storageLocation.createNewFile();
			}
			if (storageLocation.length() > 1)
			{
				try (DataInputStream dis = new DataInputStream(new FileInputStream(storageLocation)))
				{
					String serviceName = dis.readUTF();
					ExtendedStoreWithSerializer<K, VE> store = LookupService.getService(ExtendedStoreWithSerializer.class, serviceName);
					store.init(dis, (Function<Object, Object>) valueSerializer, (Function<Object, Object>) valueDeserializer);
					data = store;
				}
			}
			else
			{
				if (valueSerializer == null)
				{
					data = (ExtendedStoreWithSerializer<K, VE>) new SimpleTypeStoreMap();
				}
				else
				{
					data = (ExtendedStoreWithSerializer<K, VE>) new ArbitraryTypeStoreMap((Function<Object, Object>) valueSerializer,
							(Function<Object, Object>) valueDeserializer);
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Failure reading {}", storageLocation, e);
			throw new RuntimeException("Problem reading extended store map file for " + storageLocation);
		}
	}

	/**
	 * Write this data to disk
	 * 
	 * @return
	 */
	public Future<?> sync()
	{
		return Get.executor().submit(() -> {
			try
			{
				LOG.info("writing " + storageLocation);
				try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(storageLocation)))
				{
					dos.writeUTF(data.getServiceName());
					data.serialize(dos);
				}
			}
			catch (IOException ex)
			{
				LOG.error("error syncing identifier provider", ex);
				throw new RuntimeException("Failure during sync");
			}
		});
	}

	@Override
	public VE remove(K key)
	{
		return data.remove(key);
	}

	@Override
	public VE get(K key)
	{
		return data.get(key);
	}

	@Override
	public boolean containsKey(Object key)
	{
		return data.containsKey(key);
	}

	@Override
	public Set<K> keySet()
	{
		return data.keySet();
	}

	@Override
	public VE computeIfAbsent(K key, Function<? super K, ? extends VE> mappingFunction)
	{
		return data.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public VE put(K key, VE value)
	{
		return data.put(key, value);
	}

	@Override
	public VE accumulateAndGet(K key, VE newData, BinaryOperator<VE> accumulatorFunction)
	{
		return data.accumulateAndGet(key, newData, (newValue, existingValue) -> {
			return accumulatorFunction.apply(newValue, existingValue);
		});
	}

	@Override
	public Set<Map.Entry<K, VE>> getEntrySet()
	{
		return data.getEntrySet();
	}

	@Override
	public void clearStore()
	{
		data.clearStore();
	}

	@Override
	public int size()
	{
		return data.size();
	}

	@Override
	public long sizeAsLong()
	{
		return data.size();
	}

	@Override
	public Stream<VE> getValueStream(boolean parallel)
	{
		return data.getValueStream(parallel);
	}

	@Override
	public Stream<Map.Entry<K, VE>> getStream(boolean parallel)
	{
		return data.getStream(parallel);
	}
}
