/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
 * US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.provider.xodux;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ArrayByteIterable.Iterator;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.IntegerBinding;
import jetbrains.exodus.core.execution.ThreadJobProcessorPool;
import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.EnvironmentConfig;
import jetbrains.exodus.env.EnvironmentStatistics;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.io.SharedOpenFilesCache;
import jetbrains.exodus.log.Log;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.datastore.ChronologySerializeable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NumericUtils;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.DataStoreSubService;
import sh.isaac.model.semantic.SemanticChronologyImpl;

/**
 * A backend for Isaac written on top of xodus
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
/** Align this with {@link DatabaseImplementation#XODUS} */
@Service (name="XODUS")
@Singleton
@Rank(value=-9)
public class XodusDataStoreProvider implements DataStoreSubService
{
	private static final Logger LOG = LogManager.getLogger();
	private static final String XODUS_STORE = "xodus-store";
	private File xodusFolder;

	private final int splitNidsIntoBuckets = 15;
	
	private volatile ConcurrentHashMap<String, Store> xodusStores;  //enviornmentID + storeId -> Store
	private ConcurrentHashMap<String, Environment> xodusEnvironments; //enviornmentId -> Enviornment

	private DataStoreStartState datastoreStartState = DataStoreStartState.NOT_YET_CHECKED;
	private Optional<UUID> dataStoreId = Optional.empty();

	private ArrayList<DataWriteListener> writeListeners = new ArrayList<>();

	private final String COMPONENT_TO_SEMANTIC_NIDS_MAP = "componentToSemanticNidsMap";
	private final String NID_TO_ASSEMBLAGE_NID_MAP = "nidToAssemblageNidMap";
	private final String ASSEMBLAGE_TO_ISAAC_OBJECT_TYPE_MAP = "assemblageToIsaacObjectTypeMap";
	private final String ASSEMBLAGE_TO_VERSION_TYPE_MAP = "assemblageToVersionTypeMap";
	private final String TAXONOMY = "taxonomy";
	private final String CHRONICLE = "chronicle";
	private final String VERSION = "version";
	
	Cache<Integer, IsaacObjectType> assemblageToObjectTypeCache = Caffeine.newBuilder().maximumSize(100).build();
	Cache<Integer, VersionType> assemblageToVersionTypeCache = Caffeine.newBuilder().maximumSize(100).build();

	//TODO still need to fix the APIs to route the StampProvider, CommitProvider and UUIDIntMapMap into this API
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startup()
	{
		LOG.info("Starting Xodus DataStore provider post-construct");

		try
		{
			ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
			Path folderPath = configurationService.getDataStoreFolderPath();

			xodusFolder = new File(folderPath.toFile(), XODUS_STORE);
			xodusFolder.mkdirs();
			xodusStores = new ConcurrentHashMap<>();
			xodusEnvironments = new ConcurrentHashMap<>();

			if (new File(xodusFolder, DATASTORE_ID_FILE).isFile())
			{
				this.datastoreStartState = DataStoreStartState.EXISTING_DATASTORE;
			}
			else
			{
				this.datastoreStartState = DataStoreStartState.NO_DATASTORE;
				Files.write(xodusFolder.toPath().resolve(DATASTORE_ID_FILE), UUID.randomUUID().toString().getBytes());
			}
			// Read ID
			dataStoreId = Optional.of(UUID.fromString(new String(Files.readAllBytes(xodusFolder.toPath().resolve(DATASTORE_ID_FILE)))));
		}
		catch (Exception e)
		{
			LOG.error("Error starting Xodus Store", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		LOG.info("Stopping Xodus Data Store.");
		synchronized (xodusStores)  // Prevent inadvertent creation during iteration
		{
			try
			{
				sync().get();
			}
			catch (Exception e1)
			{
				LOG.error("Error syncing xodus prior to shutdown!", e1);
			}
			for (Environment env : xodusEnvironments.values())
			{
				env.close();
			}
			xodusStores.clear();
			xodusEnvironments.clear();

			xodusStores = null;  // Force a null pointer, if someone tries to get a store when we are shut down.
			xodusEnvironments = null;
			
			datastoreStartState = DataStoreStartState.NOT_YET_CHECKED;
			dataStoreId = Optional.empty();
			writeListeners.clear();
			
			assemblageToObjectTypeCache.invalidateAll();
			assemblageToVersionTypeCache.invalidateAll();
			
			//Best way to shutdown the rest of xodus, at the moment:
			//Clear xodus caches
			try
			{
				Field tailPages = Class.forName("jetbrains.exodus.log.SharedLogCache").getSuperclass().getDeclaredField("TAIL_PAGES_CACHE");
				tailPages.setAccessible(true);
				Object concurrentLongObjectCache = tailPages.get(null);  //static field
				Method clear = Class.forName("jetbrains.exodus.core.dataStructures.ConcurrentLongObjectCache").getMethod("clear");
				clear.invoke(concurrentLongObjectCache);
				
				Log.invalidateSharedCache();
				SharedOpenFilesCache.invalidate();
				
				//Kill off xodus threads....
				ThreadJobProcessorPool.getProcessors().forEach(jobProcessor -> jobProcessor.finish());
				
				//If we tell the spawner thread to stop, it doesn't restart properly.  So leave it be...		
//				Field spawner = ThreadJobProcessorPool.class.getDeclaredField("SPAWNER");
//				spawner.setAccessible(true);
//				Object threadJobSpawner = spawner.get(null); //static field
//				threadJobSpawner.getClass().getMethod("finish").invoke(threadJobSpawner);
			}
			catch (Exception e)
			{
				LOG.error("Unexpected error shutting down xodus", e);
			}
		}
		LOG.info("Stopped Xodus Data Store.");
	}
	
	
	/**
	 * Creates positive, compressed values for nids.
	 * @param nid
	 * @return
	 */
	private ArrayByteIterable nidToIterable(int nid)
	{
		return IntegerBinding.intToCompressedEntry(Integer.MAX_VALUE + nid);
	}
	
	private int compressedByteIterableToNid(ByteIterable nidBytes)
	{
		return IntegerBinding.compressedEntryToInt(nidBytes) - Integer.MAX_VALUE;
	}
	
	/**
	 * For cases where this is only one store in an environment
	 */
	private Store getStore(final String storeName, boolean withDupes)
	{
		return getStore(storeName, storeName, withDupes);
	}
	
	private Store getStore(final String envId, final String storeName, boolean withDupes)
	{
		Store store = xodusStores.computeIfAbsent((envId + storeName), key -> 
		{
			Environment env = xodusEnvironments.computeIfAbsent(envId, envKey -> 
			{
				File file = new File(xodusFolder, envId);
				file.mkdirs();
				EnvironmentConfig config = new EnvironmentConfig();
				config.setLogFileSize(8192l * 4l);
				return Environments.newInstance(file, config);
			});

			return env.computeInExclusiveTransaction(new TransactionalComputable<Store>()
			{
				@Override
				public Store compute(final Transaction txn)
				{
					return txn.getEnvironment().openStore(storeName, withDupes ? StoreConfig.WITH_DUPLICATES_WITH_PREFIXING
							: StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING, txn);
				}
			});
		});
		return store;
	}

	/**
	 * For cases where this is only one store in an environment, and it does not allow dupes
	 */
	private Store getStore(final String storeName)
	{
		return getStore(storeName, false);
	}
	
	private String getEnvIdForItem(int assemblage, int nid)
	{
		return new String(assemblage + "-" + ((nid * -1) % splitNidsIntoBuckets));
	}

	@Override
	public Path getDataStorePath()
	{
		return xodusFolder.toPath();
	}

	@Override
	public DataStoreStartState getDataStoreStartState()
	{
		return datastoreStartState;
	}

	@Override
	public Optional<UUID> getDataStoreId()
	{
		return dataStoreId;
	}
	@Override
	public Future<?> sync()
	{
		TimedTaskWithProgressTracker<Void> tt = new TimedTaskWithProgressTracker<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				try
				{
					Get.activeTasks().add(this);
					addToTotalWork(writeListeners.size());
					updateMessage("Notifying all write listeners to sync...");
					for (DataWriteListener dwl : writeListeners)
					{
						dwl.sync();
						completedUnitOfWork();
					}
					updateMessage("sync complete");
					XodusDataStoreProvider.LOG.info("Xodus Datastore sync complete.");
					return null;
				}
				finally
				{
					Get.activeTasks().remove(this);
				}
			}
		};
		return Get.executor().submit(tt);
	}

	@Override
	public void putChronologyData(ChronologySerializeable chronology)
	{
		try
		{
			final int assemblageNid = chronology.getAssemblageNid();

			if (chronology instanceof SemanticChronologyImpl)
			{
				final SemanticChronologyImpl semanticChronology = (SemanticChronologyImpl) chronology;
				Store componentToSemanticNidsMap = getStore(COMPONENT_TO_SEMANTIC_NIDS_MAP, true);
				final int referencedComponentNid = semanticChronology.getReferencedComponentNid();
				
				//Need to find out if we already have a mapping from referencedComponentNid -> semanticChronology.getNid((
				ArrayByteIterable computedKey = nidToIterable(referencedComponentNid);
				AtomicBoolean have = new AtomicBoolean(false);
				
				Transaction readOnlyTxn = componentToSemanticNidsMap.getEnvironment().beginReadonlyTransaction();
				try (Cursor cursor = componentToSemanticNidsMap.openCursor(readOnlyTxn))
				{
					ByteIterable v = cursor.getSearchKey(computedKey);
					if (v != null && compressedByteIterableToNid(v) == semanticChronology.getNid())
					{
						have.set(true);
					}
					// there is a value for specified key, the variable v contains the leftmost value
					while (v != null && !have.get() && cursor.getNextDup())
					{
						// this loop traverses all pairs with the same key, values differ on each iteration
						v = cursor.getValue();
						if (v != null && compressedByteIterableToNid(v) == semanticChronology.getNid())
						{
							have.set(true);
						}
					}
				}
				readOnlyTxn.abort();
				
				if (!have.get())
				{
					componentToSemanticNidsMap.getEnvironment().executeInTransaction((Transaction txn) -> 
					{
						componentToSemanticNidsMap.put(txn, computedKey, nidToIterable(semanticChronology.getNid()));
					});
				}
			}

			Store chronologyStore = getStore(getEnvIdForItem(assemblageNid, chronology.getNid()), CHRONICLE, false);
			Store versionStore = getStore(getEnvIdForItem(assemblageNid, chronology.getNid()), VERSION, true);
			ArrayByteIterable key = nidToIterable(chronology.getNid());
			chronologyStore.getEnvironment().executeInTransaction((Transaction txn) -> {
				ByteIterable chronologyOldValue = chronologyStore.get(txn, key);
				if (chronologyOldValue != null)
				{
					byte[] mergedData = ChronologyImpl.mergeChronologyData(getBytes(new ArrayByteIterable(chronologyOldValue)), 
							chronology.getChronologyDataToWrite());
					chronologyStore.put(txn, key, new ArrayByteIterable(mergedData));
				}
				else
				{
					chronologyStore.put(txn, key, new ArrayByteIterable(chronology.getChronologyDataToWrite()));
				}
				//Now, read the existing versions, and see which one(s) we need to add to the store
				HashSet<ByteBuffer> existingVersions = new HashSet<>();
				try (Cursor cursor = versionStore.openCursor(txn))
				{
					final ByteIterable v = cursor.getSearchKey(key);
					if (v != null)
					{
						existingVersions.add(ByteBuffer.wrap(getBytes(new ArrayByteIterable(v))));
						// there is a value for specified key, the variable v contains the leftmost value
						while (cursor.getNextDup())
						{
							// this loop traverses all pairs with the same key, values differ on each iteration
							existingVersions.add(ByteBuffer.wrap(getBytes(new ArrayByteIterable(cursor.getValue()))));
						}
					}
				}
				
				for (byte[] v : chronology.getVersionDataToWrite())
				{
					ByteBuffer bb = ByteBuffer.wrap(v);
					if (!existingVersions.contains(bb))
					{
						versionStore.put(txn, key, new ArrayByteIterable(bb.array()));
					}
				}
			});
			

			for (DataWriteListener dwl : writeListeners)
			{
				dwl.writeData(chronology);
			}

		}
		catch (Throwable e)
		{
			LOG.error("Unexpected error putting chronology data!", e);
			throw e;
		}
	}
	
	private byte[] getBytes(ArrayByteIterable arrayByteIterable)
	{
		byte[] result = new byte[arrayByteIterable.getLength()];
		int i = 0;
		Iterator it = arrayByteIterable.iterator();
		while (it.hasNext())
		{
			result[i++] = it.next();
		}
		if (i != result.length)
		{
			throw new RuntimeException("Xodus has a strange API");
		}
		return result;
	}

	private boolean storeHasKey(int nidKey, Store store)
	{
		AtomicBoolean answer = new AtomicBoolean();
		store.getEnvironment().executeInReadonlyTransaction((Transaction txn) -> {
			ArrayByteIterable computedKey = nidToIterable(nidKey);
			answer.set(store.get(txn, computedKey) != null);
		});
		return answer.get();
	}

	@Override
	public int[] getAssemblageConceptNids()
	{
		HashSet<Integer> results = new HashSet<>(xodusEnvironments.size());
		for (String s : xodusEnvironments.keySet())  // The keys are assemblage Nids, except for the special ones, that are strings...
		{
			//the nid has a trailing splitter, so it looks like "-123435-4" = we want the middle nid part, and need to make it negative.
			String[] chunks = s.split("-");
			if (chunks.length == 3)
			{
				OptionalInt oi = NumericUtils.getInt(chunks[1]);
				if (oi.isPresent())
				{
					results.add(oi.getAsInt() * -1);
				}
			}
		}
		int[] finalResult = new int[results.size()];
		int i = 0;
		for (int nid : results)
		{
			finalResult[i++] = nid;
		}
		return finalResult;
	}


	@Override
	public Optional<ByteArrayDataBuffer> getChronologyVersionData(int nid)
	{
		OptionalInt assemblageId = getAssemblageOfNid(nid);
		if (!assemblageId.isPresent())
		{
			return Optional.empty();
		}
		Store assemblageToChronicle = getStore(getEnvIdForItem(assemblageId.getAsInt(), nid), CHRONICLE, false);
		Store assemblageToVersion = getStore(getEnvIdForItem(assemblageId.getAsInt(), nid), VERSION, true);
		ArrayByteIterable computedKey = nidToIterable(nid);
		
		ByteArrayDataBuffer badb = new ByteArrayDataBuffer();
		
		Transaction txn = assemblageToChronicle.getEnvironment().beginReadonlyTransaction();
		try
		{
			ByteIterable bi = assemblageToChronicle.get(txn, computedKey);
			if (bi != null)
			{
				
				Iterator byteIterator = new ArrayByteIterable(bi).iterator();
				while (byteIterator.hasNext())
				{
					badb.putByte(byteIterator.next());
				}
			}
			else
			{
				return Optional.empty();
			}
		}
		finally
		{
			txn.abort();
		}
		
		txn = assemblageToVersion.getEnvironment().beginReadonlyTransaction();
		
		try (Cursor cursor = assemblageToVersion.openCursor(txn))
		{
			final ByteIterable bi = cursor.getSearchKey(computedKey);
			if (bi != null)
			{
				Iterator byteIterator = new ArrayByteIterable(bi).iterator();
				while (byteIterator.hasNext())
				{
					badb.putByte(byteIterator.next());
				}
				// there is a value for specified key, the variable v contains the leftmost value
				while (cursor.getNextDup())
				{
					// this loop traverses all pairs with the same key, values differ on each iteration
					byteIterator = new ArrayByteIterable(cursor.getValue()).iterator();
					while (byteIterator.hasNext())
					{
						badb.putByte(byteIterator.next());
					}
				}
			}
			else
			{
				throw new RuntimeException("Should be impossible to have chronicle data with no version data");
			}
		}
		finally
		{
			txn.abort();
		}
		
		badb.trimToSize();
		badb.flip();
		return Optional.of(badb);
	}

	@Override
	public int[] getSemanticNidsForComponent(final int componentNid)
	{
		Store componentToSemanticNidsMap = getStore(COMPONENT_TO_SEMANTIC_NIDS_MAP, true);
		ArrayByteIterable computedKey = nidToIterable(componentNid);
		
		NidSet results = new NidSet();

		Transaction txn = componentToSemanticNidsMap.getEnvironment().beginReadonlyTransaction();
		try (Cursor cursor = componentToSemanticNidsMap.openCursor(txn))
		{
			final ByteIterable v = cursor.getSearchKey(computedKey);
			if (v != null)
			{
				results.add(compressedByteIterableToNid(v));
				// there is a value for specified key, the variable v contains the leftmost value
				while (cursor.getNextDup())
				{
					// this loop traverses all pairs with the same key, values differ on each iteration
					results.add(compressedByteIterableToNid(cursor.getValue()));
				}
			}
		}
		txn.abort();
		return results.asArray();
	}

	@Override
	public int getAssemblageMemoryInUse(int assemblageNid)
	{
		LOG.warn("Assemblage Memory in Use is not supported by xodus");
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getAssemblageSizeOnDisk(int assemblageNid)
	{
		//The same environment carries both the CHRONICLE and VERSION stores, so don't need to read both
		int result = 0;
		for (int i = 0; i < splitNidsIntoBuckets; i++)
		{
			result += (int) getStore((assemblageNid + "-" + i), CHRONICLE, false).getEnvironment().getStatistics()
					.getStatisticsItem(EnvironmentStatistics.Type.DISK_USAGE).getTotal();
		}
		return result;
	}

	@Override
	public boolean hasChronologyData(int nid, IsaacObjectType ofType)
	{
		OptionalInt assemblageId = getAssemblageOfNid(nid);
		if (!assemblageId.isPresent())
		{
			return false;
		}
		Store assemblageToData = getStore(getEnvIdForItem(assemblageId.getAsInt(), nid), CHRONICLE, false);
		if (storeHasKey(nid, assemblageToData))
		{
			if (getIsaacObjectTypeForAssemblageNid(assemblageId.getAsInt()) != ofType)
			{
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerDataWriteListener(DataWriteListener dataWriteListener)
	{
		writeListeners.add(dataWriteListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterDataWriteListener(DataWriteListener dataWriteListener)
	{
		writeListeners.remove(dataWriteListener);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public IntStream getNidsForAssemblage(final int assemblageNid)
	{
		//TODO this is going to cause a leak, if they don't iterate the entire stream for whatever reason.
		//Need to handle.  Hard to fix though, because I can't have a cleanup thread close the transaction, due to the same-thread rules in xodus.
		//Likely need to iterate the data in a new thread here, and have that thread handle self closing, and just hand data back to the stream
		AtomicInteger storeBucket = new AtomicInteger(0);
		AtomicReference<Store> store = new AtomicReference<Store>();
		AtomicLong lastRead = new AtomicLong(System.currentTimeMillis());
		
		final Supplier<? extends Spliterator.OfInt> streamSupplier = new Supplier<OfInt>()
		{
			Transaction txn;
			Cursor cursor;
			{
				nextStore();
			}
			
			/**
			 * {@inheritDoc}
			 */
			@Override
			public OfInt get()
			{
				return new OfInt()
				{
					@Override
					public long estimateSize()
					{
						try
						{
							return store.get().count(txn) * splitNidsIntoBuckets;
						}
						catch (Exception e)
						{
							if (!txn.isFinished()) 
							{
								cursor.close();
								txn.abort();
							}
							throw e;
						}
					}
					
					@Override
					public int characteristics()
					{
						return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.SIZED;
					}
					
					@Override
					public OfInt trySplit()
					{
						return null;
					}
					
					@Override
					public boolean tryAdvance(IntConsumer action)
					{
						try
						{
							boolean cursorHasNext = cursor.getNext();
							if (!cursorHasNext)
							{
								while (!cursorHasNext && nextStore())
								{
									//If there are no items in this store, go to the next store
									cursorHasNext = cursor.getNext();
									if (!cursorHasNext)
									{
										//Nothing in this store.
										if (!txn.isFinished()) 
										{
											cursor.close();
											txn.abort();
										}
									}
								}
								
								//If we got here, and we still have no items, we are done.
								if (!cursorHasNext)
								{
									if (!txn.isFinished()) 
									{
										cursor.close();
										txn.abort();
									}
									return false;
								}
							}

							action.accept(compressedByteIterableToNid(cursor.getKey()));
							lastRead.set(System.currentTimeMillis());
							return true;
						}
						catch (Exception e)
						{
							if (!txn.isFinished()) 
							{
								cursor.close();
								txn.abort();
							}
							throw e;
						}
					}
				};
			}
			
			private boolean nextStore()
			{
				if (txn != null && !txn.isFinished()) 
				{
					cursor.close();
					txn.abort();
				}
				if (storeBucket.get() < splitNidsIntoBuckets)
				{
					store.set(getStore((assemblageNid + "-" + storeBucket.getAndIncrement()), CHRONICLE, false));
					txn = store.get().getEnvironment().beginReadonlyTransaction();
					
					try
					{
						cursor = store.get().openCursor(txn);
					}
					catch (Exception e)
					{
						txn.abort();
						throw e;
					}
					return true;
				}
				else
				{
					store.set(null);
					return false;
				}
			}
		};
		
		IntStream results = StreamSupport.intStream(streamSupplier, Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.SIZED, false);
		return results;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public OptionalInt getAssemblageOfNid(int nid)
	{
		Store nidToAssemblageNidMap = getStore(NID_TO_ASSEMBLAGE_NID_MAP +  "-" + Integer.toString((nid * -1) % splitNidsIntoBuckets));
		ArrayByteIterable computedKey = nidToIterable(nid);
		
		Transaction txn = nidToAssemblageNidMap.getEnvironment().beginReadonlyTransaction();
		try
		{
			ByteIterable bi = nidToAssemblageNidMap.get(txn, computedKey);
			if (bi != null)
			{
				return OptionalInt.of(compressedByteIterableToNid(bi));
			}
			else
			{
				return OptionalInt.empty();
			}
		}
		finally
		{
			txn.abort();
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setAssemblageForNid(int nid, int assemblage) throws IllegalArgumentException
	{
		Store nidToAssemblageNidMap = getStore(NID_TO_ASSEMBLAGE_NID_MAP +  "-" + Integer.toString((nid * -1) % splitNidsIntoBuckets));
		ArrayByteIterable key = nidToIterable(nid);
		
		//read only, as this will be faster / not lock the way a write transaction will.
		Transaction txn = nidToAssemblageNidMap.getEnvironment().beginReadonlyTransaction();
		try
		{
			ByteIterable bi = nidToAssemblageNidMap.get(txn, key);
			if (bi != null)
			{
				int storedAssemblageId = compressedByteIterableToNid(bi);
				if (storedAssemblageId != assemblage)
				{
					throw new IllegalArgumentException("Not allowed to change the assemblage type of a nid");
				}
				return;
			}
		}
		finally
		{
			txn.abort();
		}
		
		//If we get here, not yet stored.
		nidToAssemblageNidMap.getEnvironment().executeInTransaction((Transaction writeTxn) -> {
			
			//reread in transaction, in case it changed...
			ByteIterable oldValue = nidToAssemblageNidMap.get(writeTxn, key);
			if (oldValue != null)
			{
				if (compressedByteIterableToNid(oldValue) != assemblage)
				{
					throw new IllegalArgumentException("Not allowed to change the assemblage type of a nid");
				}
			}
			else
			{
				nidToAssemblageNidMap.put(writeTxn, key, nidToIterable(assemblage));
			}
		});
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int[] getTaxonomyData(int assemblageNid, int conceptNid)
	{
		Store taxonomyDataMap = getStore(TAXONOMY + assemblageNid +  "-" + Integer.toString((conceptNid * -1) % splitNidsIntoBuckets));
		ArrayByteIterable computedKey = nidToIterable(conceptNid);
		
		Transaction txn = taxonomyDataMap.getEnvironment().beginReadonlyTransaction();
		try
		{
			ByteIterable bi = taxonomyDataMap.get(txn, computedKey);
			if (bi != null)
			{
				return IntArrayBinding.entryToIntArray(bi);
			}
			else
			{
				return null;
			}
		}
		finally
		{
			txn.abort();
		}
		
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int[] accumulateAndGetTaxonomyData(int assemblageNid, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction)
	{
		AtomicReference<int[]> result = new AtomicReference<int[]>(newData);
		Store taxonomyDataMap = getStore(TAXONOMY + assemblageNid +  "-" + Integer.toString((conceptNid * -1) % splitNidsIntoBuckets));
		taxonomyDataMap.getEnvironment().executeInTransaction((Transaction txn) -> {
			ArrayByteIterable key = nidToIterable(conceptNid);
			ByteIterable oldValue = taxonomyDataMap.get(txn, key);
			if (oldValue != null)
			{
				int[] oldIntArray = IntArrayBinding.entryToIntArray(oldValue);
				int[] mergedIntArray = accumulatorFunction.apply(oldIntArray, newData);
				taxonomyDataMap.put(txn, key, IntArrayBinding.intArrayToEntry(mergedIntArray));
				result.set(mergedIntArray);
			}
			else
			{
				taxonomyDataMap.put(txn, key, IntArrayBinding.intArrayToEntry(newData));
			}
		});
		return result.get();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IsaacObjectType getIsaacObjectTypeForAssemblageNid(int assemblageNid)
	{
		IsaacObjectType iot = assemblageToObjectTypeCache.getIfPresent(assemblageNid);
		if (iot != null)
		{
			return iot;
		}
		else
		{
			Store assemblageToIsaacObjectTypeMap = getStore(ASSEMBLAGE_TO_ISAAC_OBJECT_TYPE_MAP);
			ArrayByteIterable computedKey = nidToIterable(assemblageNid);
			
			Transaction txn = assemblageToIsaacObjectTypeMap.getEnvironment().beginReadonlyTransaction();
			try
			{
				ByteIterable bi = assemblageToIsaacObjectTypeMap.get(txn, computedKey);
				if (bi != null)
				{
					iot = IsaacObjectType.values()[IntegerBinding.compressedEntryToInt(bi)];
					assemblageToObjectTypeCache.put(assemblageNid, iot);
					return iot;
				}
				else
				{
					return IsaacObjectType.UNKNOWN;
				}
			}
			finally
			{
				txn.abort();
			}
		}
	}

	@Override
	public NidSet getAssemblageNidsForType(IsaacObjectType type)
	{
		NidSet results = new NidSet();
		Store store = getStore(ASSEMBLAGE_TO_ISAAC_OBJECT_TYPE_MAP);
		Transaction txn = store.getEnvironment().beginReadonlyTransaction();
		try (Cursor cursor = store.openCursor(txn))
		{
			while (cursor.getNext())
			{
				int foundTypeOrdinal = IntegerBinding.compressedEntryToInt(cursor.getValue());
				if (type.ordinal() == foundTypeOrdinal)
				{
					results.add(compressedByteIterableToNid(cursor.getKey()));
				}
			}
		}
		finally
		{
			txn.abort();
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAssemblageIsaacObjectType(int assemblageNid, IsaacObjectType type) throws IllegalStateException
	{
		Store assemblageToIsaacObjectTypeMap = getStore(ASSEMBLAGE_TO_ISAAC_OBJECT_TYPE_MAP);
		assemblageToIsaacObjectTypeMap.getEnvironment().executeInTransaction((Transaction txn) -> {
			ArrayByteIterable key = nidToIterable(assemblageNid);
			ByteIterable oldValue = assemblageToIsaacObjectTypeMap.get(txn, key);
			if (oldValue != null)
			{
				if (IntegerBinding.compressedEntryToInt(oldValue) != type.ordinal())
				{
					throw new IllegalArgumentException("Not allowed to change the object type an assemblage");
				}
			}
			else
			{
				assemblageToIsaacObjectTypeMap.put(txn, key, IntegerBinding.intToCompressedEntry(type.ordinal()));
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VersionType getVersionTypeForAssemblageNid(int assemblageNid)
	{
		VersionType vt = assemblageToVersionTypeCache.getIfPresent(assemblageNid);
		if (vt != null)
		{
			return vt;
		}
		else
		{
			Store assemblageToVersionTypeMap = getStore(ASSEMBLAGE_TO_VERSION_TYPE_MAP);
			ArrayByteIterable computedKey = nidToIterable(assemblageNid);
			
			Transaction txn = assemblageToVersionTypeMap.getEnvironment().beginReadonlyTransaction();
			try
			{
				ByteIterable bi = assemblageToVersionTypeMap.get(txn, computedKey);
				if (bi != null)
				{
					vt = VersionType.values()[IntegerBinding.compressedEntryToInt(bi)];
					assemblageToVersionTypeCache.put(assemblageNid, vt);
					return vt;
				}
				else
				{
					return VersionType.UNKNOWN;
				}
			}
			finally
			{
				txn.abort();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAssemblageVersionType(int assemblageNid, VersionType type) throws IllegalStateException
	{
		Store assemblageToVersionTypeMap = getStore(ASSEMBLAGE_TO_VERSION_TYPE_MAP);
		assemblageToVersionTypeMap.getEnvironment().executeInTransaction((Transaction txn) -> {
			ArrayByteIterable key = nidToIterable(assemblageNid);
			ByteIterable oldValue = assemblageToVersionTypeMap.get(txn, key);
			if (oldValue != null)
			{
				if (IntegerBinding.compressedEntryToInt(oldValue) != type.ordinal())
				{
					throw new IllegalArgumentException("Not allowed to change the version type an assemblage");
				}
			}
			else
			{
				assemblageToVersionTypeMap.put(txn, key, IntegerBinding.intToCompressedEntry(type.ordinal()));
			}
		});
	}
	
	@Override
	public boolean implementsSequenceStore()
	{
		return false;
	}
}