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
package sh.isaac.api.collections.uuidnidmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sh.isaac.api.Get;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.datastore.ExtendedStoreData;

/**
 * A {@link UuidToIntMap} implementation that uses the underlying extended data store.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DataStoreUuidToIntMap implements UuidToIntMap
{
	private static final Logger LOG = LogManager.getLogger();

	private ExtendedStore dataStore;
	private ExtendedStoreData<UUID, Integer> data;
	private final AtomicInteger NEXT_NID_PROVIDER = new AtomicInteger(Integer.MIN_VALUE);

	// inverse, in memory cache only used for certain loader patterns.
	private Cache<Integer, UUID[]> nidToPrimoridialCache = null;

	public DataStoreUuidToIntMap(ExtendedStore datastore)
	{
		this.dataStore = datastore;
		data = this.dataStore.<UUID, Integer> getStore("UUIDToIntMap");
		
		// Loader utility enables this when doing IBDF file creation to to get from nid back to UUID - this prevents it from doing table scans.
		if (Get.configurationService().isInDBBuildMode(BuildMode.IBDF))
		{
			this.nidToPrimoridialCache = Caffeine.newBuilder().build();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsKey(UUID key)
	{
		return data.get(key) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsValue(int value)
	{
		return data.getValueStream().anyMatch((incoming) -> incoming.intValue() == value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean inverseCacheEnabled()
	{
		return nidToPrimoridialCache != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean put(UUID key, int value)
	{
		boolean newEntry = data.put(key, value) == null;
		if (newEntry)
		{
			updateCache(value, key);
		}
		return newEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OptionalInt get(UUID key)
	{
		Integer i = data.get(key);
		return i == null ? OptionalInt.empty() : OptionalInt.of(i);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxNid()
	{
		return NEXT_NID_PROVIDER.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWithGeneration(UUID uuidKey)
	{
		Integer nid = data.computeIfAbsent(uuidKey, (keyAgain) -> NEXT_NID_PROVIDER.incrementAndGet());
		updateCache(nid, uuidKey);
		return nid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UUID[] getKeysForValue(int nid)
	{
		if (this.nidToPrimoridialCache != null)
		{
			final UUID[] cacheHit = this.nidToPrimoridialCache.getIfPresent(nid);

			if ((cacheHit != null) && (cacheHit.length > 0))
			{
				return cacheHit;
			}
		}

		final ArrayList<UUID> uuids = new ArrayList<>();
		data.getEntrySet().parallelStream().filter((entry) -> entry.getValue().intValue() == nid).forEach((entry -> uuids.add(entry.getKey())));

		final UUID[] temp = uuids.toArray(new UUID[uuids.size()]);

		if ((this.nidToPrimoridialCache != null) && (temp.length > 0))
		{
			this.nidToPrimoridialCache.put(nid, temp);
		}

		return temp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cacheContainsNid(int nid)
	{
		if (this.nidToPrimoridialCache != null)
		{
			return this.nidToPrimoridialCache.getIfPresent(nid) != null;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDiskSpaceUsed()
	{
		// rough estimate...
		return (data.size() * (8 * 2)) + (data.size() * 4);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMemoryInUse()
	{
		LOG.warn("getMemoryInUse unsupported for DataStoreUUIDToIntMap");
		return 0;
	}

	/**
	 * Update cache.
	 *
	 * @param nid the nid
	 * @param uuidKey the uuid key
	 */
	private void updateCache(int nid, UUID uuidKey)
	{
		if (this.nidToPrimoridialCache != null)
		{
			synchronized (nidToPrimoridialCache)
			{
				final UUID[] temp = this.nidToPrimoridialCache.getIfPresent(nid);
				UUID[] temp1;

				if (temp == null)
				{
					temp1 = new UUID[] { uuidKey };
				}
				else
				{
					temp1 = Arrays.copyOf(temp, temp.length + 1);
					temp1[temp.length] = uuidKey;
				}

				this.nidToPrimoridialCache.put(nid, temp1);
			}
		}
	}
}
