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
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.Get;
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
	private Cache<Integer, UUID[]> nidToPrimordialCache = null;
	private final String MAX_NID_STORE_LOC = "-max-nid-assigned-"; 

	public DataStoreUuidToIntMap(ExtendedStore datastore)
	{
		this.dataStore = datastore;
		data = this.dataStore.<UUID, Integer> getStore("UUIDToIntMap");
		
		OptionalLong ol = this.dataStore.getSharedStoreLong(MAX_NID_STORE_LOC);
		if (ol.isPresent())
		{
			NEXT_NID_PROVIDER.set((int)ol.getAsLong());
		}
		
		// Loader utility enables this when doing IBDF file creation to to get from nid back to UUID - this prevents it from doing table scans.
		if (Get.configurationService().isInDBBuildMode(BuildMode.IBDF))
		{
			enableInverseCache();
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
		return nidToPrimordialCache != null;
	}
	
	/**
	 * @see sh.isaac.api.collections.uuidnidmap.UuidToIntMap#enableInverseCache()
	 */
	@Override
	public void enableInverseCache()
	{
		this.nidToPrimordialCache = Caffeine.newBuilder().build();
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
		Integer nid = data.computeIfAbsent(uuidKey, (keyAgain) -> 
		{
			final int newNid = NEXT_NID_PROVIDER.incrementAndGet();
			OptionalLong ol = dataStore.putSharedStoreLong(MAX_NID_STORE_LOC, newNid);
			if (ol.isPresent() && ol.getAsLong() > newNid)
			{
				//If the old one was bigger than the new one, we have a thread race that we lost, don't want to overwrite the bigger value, so put it back.
				long inputNid = newNid;
				while (ol.getAsLong() > inputNid)
				{
					inputNid = ol.getAsLong();
					ol = dataStore.putSharedStoreLong(MAX_NID_STORE_LOC, inputNid);
					//Loop, to make sure we didn't lose another race...
				}
			}
			return newNid;
		});
		updateCache(nid, uuidKey);
		return nid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UUID[] getKeysForValue(int nid)
	{
		if (this.nidToPrimordialCache != null)
		{
			final UUID[] cacheHit = this.nidToPrimordialCache.getIfPresent(nid);

			if ((cacheHit != null) && (cacheHit.length > 0))
			{
				return cacheHit;
			}
		}

		final ArrayList<UUID> uuids = new ArrayList<>();
		data.getEntrySet().parallelStream().filter((entry) -> entry.getValue().intValue() == nid).forEach((entry -> uuids.add(entry.getKey())));

		final UUID[] temp = uuids.toArray(new UUID[uuids.size()]);

		if ((this.nidToPrimordialCache != null) && (temp.length > 0))
		{
			this.nidToPrimordialCache.put(nid, temp);
		}

		return temp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cacheContainsNid(int nid)
	{
		if (this.nidToPrimordialCache != null)
		{
			return this.nidToPrimordialCache.getIfPresent(nid) != null;
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
		if (this.nidToPrimordialCache != null)
		{
			synchronized (nidToPrimordialCache)
			{
				final UUID[] temp = this.nidToPrimordialCache.getIfPresent(nid);
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

				this.nidToPrimordialCache.put(nid, temp1);
			}
		}
	}
}
