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
package sh.isaac.provider.mvStore;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.datastore.ChronologySerializeable;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.datastore.ExtendedStoreData;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NumericUtils;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.DataStoreSubService;

/**
 * A backend for Isaac written on top of the H2 MV store
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
/** Align this with {@link DatabaseImplementation#MV} */
@Service (name="MV")
@Singleton
@Rank(value=-9)
public class MVDataStoreProvider implements DataStoreSubService, ExtendedStore
{
	private static final Logger LOG = LogManager.getLogger();
	private static final String MV_STORE = "mv-store";
	private static final String SHARED_STORE = "shared-store";
	private File mvFolder;

	private MVStore store;
	
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
	
	MVMap<Integer, int[]> componentToSemanticNidsMap;
	MVMap<Integer, Integer> nidToAssemblageNidMap;
	MVMap<Integer, Integer> assemblageToIsaacObjectTypeMap;
	MVMap<Integer, Integer> assemblageToVersionTypeMap;
	
	ConcurrentHashMap<String, MVMap<Integer, byte[]>> chronicleMaps = new ConcurrentHashMap<>(100);
	ConcurrentHashMap<String, MVMap<Integer, byte[][]>> versionMaps = new ConcurrentHashMap<>(100);
	ConcurrentHashMap<String, MVMap<Integer, int[]>> taxonomyMaps = new ConcurrentHashMap<>(2);
	
	ConcurrentHashMap<String, MVExtendedStore<?, ?, ?>> extendedStores = new ConcurrentHashMap<>(5);
	MVMap<String, Long> sharedStore;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startup()
	{
		LOG.info("Starting MV DataStore provider post-construct");

		try
		{
			ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
			Path folderPath = configurationService.getDataStoreFolderPath();

			mvFolder = new File(folderPath.toFile(), MV_STORE);
			mvFolder.mkdirs();

			if (new File(mvFolder, DATASTORE_ID_FILE).isFile())
			{
				this.datastoreStartState = DataStoreStartState.EXISTING_DATASTORE;
			}
			else
			{
				this.datastoreStartState = DataStoreStartState.NO_DATASTORE;
				Files.write(mvFolder.toPath().resolve(DATASTORE_ID_FILE), UUID.randomUUID().toString().getBytes());
			}
			// Read ID
			dataStoreId = Optional.of(UUID.fromString(new String(Files.readAllBytes(mvFolder.toPath().resolve(DATASTORE_ID_FILE)))));
			
			//Note, if problems start happening with MVStore with getting java.nio.channels.ClosedChannelException 
			//one can work around the problem by adding `"retry:" +`
			//to the front of the fileName, just below.  For this to work, you also need to switch the dependency stack 
			//from h2-mvstore to the full 'h2' jar file, as the hs-mvstore jar file doesn't contain the classes necessary
			//to perform the interrupt handling.
			//Its best, however, to prevent the exception by not using Thread.interrupt on threads that interact with the DB.
			//This also means no calling Task.cancel(), as that fires interrupts.
			//TODO play with memory / cache size
			this.store = new MVStore.Builder().cacheSize(2000).fileName(new File(mvFolder, MV_STORE + ".mv").getAbsolutePath()).open();
			this.store.setVersionsToKeep(0);
			
			componentToSemanticNidsMap = this.store.<Integer, int[]>openMap(COMPONENT_TO_SEMANTIC_NIDS_MAP);
			nidToAssemblageNidMap = this.store.<Integer, Integer>openMap(NID_TO_ASSEMBLAGE_NID_MAP);
			assemblageToIsaacObjectTypeMap = this.store.<Integer, Integer>openMap(ASSEMBLAGE_TO_ISAAC_OBJECT_TYPE_MAP);
			assemblageToVersionTypeMap = this.store.<Integer, Integer>openMap(ASSEMBLAGE_TO_VERSION_TYPE_MAP);
			sharedStore = this.store.<String, Long>openMap(SHARED_STORE);
			
			LOG.info("MV DataStore started");
		}
		catch (Exception e)
		{
			LOG.error("Error starting MV Store", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		try
		{
			LOG.info("Stopping MV Data Store.");
			try
			{
				sync().get();
			}
			catch (Exception e1)
			{
				LOG.error("Error syncing mv store prior to shutdown!", e1);
			}
			
			writeListeners.clear();
			
			componentToSemanticNidsMap = null;
			nidToAssemblageNidMap = null;
			assemblageToIsaacObjectTypeMap = null;
			assemblageToVersionTypeMap = null;
			sharedStore = null;
			extendedStores.clear();
			taxonomyMaps.clear();
			chronicleMaps.clear();
			versionMaps.clear();

			//If we are in IBDF build mode, we don't care about DB size
			if (Get.configurationService().getDBBuildMode().get() != BuildMode.IBDF)
			{
				LOG.info("Running MVStore compact");
				TimedTask<Void> tt = new TimedTask<Void>()
				{
					@Override
					protected Void call() throws Exception
					{
						try
						{
							updateMessage("Compacting Data Store");
							store.compactMoveChunks();
						}
						finally
						{
							Get.activeTasks().remove(this);
						}
						return null;
					}};
				Get.activeTasks().add(tt);
				Get.workExecutors().getIOExecutor().submit(tt).get();
			}
			
			this.store.close();
			this.store = null;
			
			datastoreStartState = DataStoreStartState.NOT_YET_CHECKED;
			dataStoreId = Optional.empty();

			LOG.info("MV Data Store stopped.");
		}
		catch (Exception e)
		{
			LOG.error("Unexpected error shutting down mvStore", e);
		}
	}
	
	private MVMap<Integer, int[]> getTaxonomyMap(int assemblageNid)
	{
		return taxonomyMaps.computeIfAbsent((TAXONOMY + Integer.toString(assemblageNid)), 
				mapNameKey -> this.store.<Integer, int[]>openMap(mapNameKey));
	}
	
	private MVMap<Integer, byte[]> getChronicleMap(int assemblageNid)
	{
		return chronicleMaps.computeIfAbsent((CHRONICLE + Integer.toString(assemblageNid)),
				mapNameKey -> store.<Integer, byte[]>openMap(mapNameKey));
	}
	
	private MVMap<Integer, byte[][]> getVersionMap(int assemblageNid)
	{
		return versionMaps.computeIfAbsent((VERSION + Integer.toString(assemblageNid)), 
				mapNameKey -> this.store.<Integer, byte[][]>openMap(mapNameKey));
	}
	
	@Override
	public Path getDataStorePath()
	{
		return mvFolder.toPath();
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
					
					addToTotalWork(writeListeners.size() + 2);
					updateMessage("Notifying all write listeners to sync...");
					for (DataWriteListener dwl : writeListeners)
					{
						dwl.sync();
						completedUnitOfWork();
					}
					
					updateMessage("Commit Store...");
					store.commit();
					completedUnitOfWork();
					
					updateMessage("Sync file store...");
					store.sync();
					completedUnitOfWork();
					
					updateMessage("sync complete");
					MVDataStoreProvider.LOG.info("MV Datastore sync complete.");
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

			if (chronology instanceof SemanticChronology)
			{
				final SemanticChronology semanticChronology = (SemanticChronology) chronology;
				final int referencedComponentNid = semanticChronology.getReferencedComponentNid();

				componentToSemanticNidsMap.compute(referencedComponentNid, (key, oldValue) ->
				{
					if (oldValue == null)
					{
						return new int[] {semanticChronology.getNid()};
					}
					else
					{
						if (Arrays.binarySearch(oldValue, semanticChronology.getNid()) < 0)
						{
							//Not in the array
							int[] newValue = new int[oldValue.length + 1];
							System.arraycopy(oldValue, 0, newValue, 0, oldValue.length);
							newValue[oldValue.length] = semanticChronology.getNid();
							Arrays.sort(newValue);
							return newValue;
						}
						else
						{
							//already there
							return oldValue;
						}
					}
				});
			}

			MVMap<Integer, byte[]> chronicleData = getChronicleMap(assemblageNid);
			chronicleData.compute(chronology.getNid(), (key, oldData) ->
			{
				if (oldData == null)
				{
					return chronology.getChronologyDataToWrite();
				}
				else
				{
					return ChronologyImpl.mergeChronologyData(oldData, chronology.getChronologyDataToWrite());
				}
			});
			
			MVMap<Integer, byte[][]> versionData = getVersionMap(assemblageNid);
			versionData.compute(chronology.getNid(), (key, oldValue) ->
			{
				HashSet<ByteBuffer> existingVersions = new HashSet<>(oldValue == null ? 0 : oldValue.length);
				if (oldValue != null)
				{
					for (byte[] byteArray : oldValue)
					{
						existingVersions.add(ByteBuffer.wrap(byteArray));
					}
				}
				
				ArrayList<ByteBuffer> versionsToAdd = new ArrayList<>(2);
				for (byte[] v : chronology.getVersionDataToWrite())
				{
					ByteBuffer bb = ByteBuffer.wrap(v);
					if (!existingVersions.contains(bb))
					{
						versionsToAdd.add(bb);
					}
				}
				
				byte[][] newValue = new byte[existingVersions.size() + versionsToAdd.size()][];
				
				int i = 0;
				for (ByteBuffer bb : existingVersions)
				{
					newValue[i++] = bb.array();
				}
				for (ByteBuffer bb : versionsToAdd)
				{
					newValue[i++] = bb.array();
				}
				
				return newValue;
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

	@Override
	public int[] getAssemblageConceptNids()
	{
		HashSet<Integer> results = new HashSet<>();
		for (String s : store.getMapNames())  // The map names are assemblage Nids, except for the special ones, that are strings...
		{
			if (s.startsWith(CHRONICLE))
			{
				if (s.contains("-"))
				{
					//looks like chronicle-nid-bucket
					String[] temp = s.split("-");
					if (temp.length > 1)
					{
						OptionalInt oi = NumericUtils.getInt(temp[1]);
						if (oi.isPresent())
						{
							results.add(oi.getAsInt() * -1);
						}
					}
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
		OptionalInt assemblageNid = getAssemblageOfNid(nid);
		if (!assemblageNid.isPresent())
		{
			return Optional.empty();
		}
		
		ByteArrayDataBuffer badb = new ByteArrayDataBuffer();
		
		byte[] chronicle = getChronicleMap(assemblageNid.getAsInt()).get(nid);
		if (chronicle == null)
		{
			return Optional.empty();
		}
		else
		{
			badb.put(chronicle);
		}
		
		byte[][] version = getVersionMap(assemblageNid.getAsInt()).get(nid);
		if (version == null)
		{
			throw new RuntimeException("Should be impossible to have chronicle data with no version data");
		}
		else
		{
			for (byte[] ba : version)
			{
				badb.put(ba);
			}
		}
		badb.trimToSize();
		badb.flip();
		return Optional.of(badb);
	}

	@Override
	public int[] getSemanticNidsForComponent(final int componentNid)
	{
		int[] temp = componentToSemanticNidsMap.get(componentNid);
		return temp == null ? new int[0] : temp;
	}

	@Override
	public int getAssemblageMemoryInUse(int assemblageNid)
	{
		LOG.warn("Assemblage Memory in Use is not supported by mvStore");
		return -1;
	}

	@Override
	public int getAssemblageSizeOnDisk(int assemblageNid)
	{
		AtomicInteger total = new AtomicInteger();
		
		getChronicleMap(assemblageNid).forEach((key, value) -> 
		{
			total.getAndAdd(4);  //for the key
			total.getAndAdd(value.length);
		});
		
		getVersionMap(assemblageNid).forEach((key, value) -> 
		{
			total.getAndAdd(4);  //for the key
			for (byte[] b : value)
			{
				total.getAndAdd(b.length);
			}
		});
		return total.get();
	}

	@Override
	public boolean hasChronologyData(int nid, IsaacObjectType ofType)
	{
		OptionalInt assemblageNid = getAssemblageOfNid(nid);
		if (!assemblageNid.isPresent())
		{
			return false;
		}
		if (getChronicleMap(assemblageNid.getAsInt()).containsKey(nid))
		{
			if (getIsaacObjectTypeForAssemblageNid(assemblageNid.getAsInt()) != ofType)
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
		MVMap<Integer, byte[]> data = getChronicleMap(assemblageNid);
		
		AtomicReference<Iterator<Integer>> it = new AtomicReference<>(data.keyIterator(data.firstKey()));
		final Supplier<? extends Spliterator.OfInt> streamSupplier = new Supplier<OfInt>()
		{
			@Override
			public OfInt get()
			{
				return new OfInt()
				{
					@Override
					public long estimateSize()
					{
						return data.sizeAsLong();
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
						if (it.get().hasNext())
						{
							action.accept(it.get().next());
							return true;
						}
						else
						{
							return false;
						}
					}
				};
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
		Integer value = nidToAssemblageNidMap.get(nid);
		return value == null ? OptionalInt.empty() : OptionalInt.of(value);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setAssemblageForNid(int nid, int assemblage) throws IllegalArgumentException
	{
		Integer oldValue = nidToAssemblageNidMap.putIfAbsent(nid, assemblage);
		if (oldValue != null && oldValue != assemblage)
		{
			throw new IllegalArgumentException("Not allowed to change the assemblage type of a nid");
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int[] getTaxonomyData(int assemblageNid, int conceptNid)
	{
		return getTaxonomyMap(assemblageNid).get(conceptNid);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int[] accumulateAndGetTaxonomyData(int assemblageNid, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction)
	{
		return getTaxonomyMap(assemblageNid).compute(conceptNid, (key, oldValue) ->
		{
			if (oldValue == null)
			{
				return newData;
			}
			else
			{
				return accumulatorFunction.apply(oldValue, newData);
			}
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IsaacObjectType getIsaacObjectTypeForAssemblageNid(int assemblageNid)
	{
		Integer current = assemblageToIsaacObjectTypeMap.get(assemblageNid);
		if (current == null)
		{
			return IsaacObjectType.UNKNOWN;
		}
		else
		{
			return IsaacObjectType.values()[current.intValue()];
		}
	}

	@Override
	public NidSet getAssemblageNidsForType(IsaacObjectType type)
	{
		NidSet results = new NidSet();
		for (Entry<Integer, Integer> entry : assemblageToIsaacObjectTypeMap.entrySet())
		{
			if (type.ordinal() == entry.getValue().intValue())
			{
				results.add(entry.getKey());
			}
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAssemblageIsaacObjectType(int assemblageNid, IsaacObjectType type) throws IllegalStateException
	{
		Integer existingValue = assemblageToIsaacObjectTypeMap.putIfAbsent(assemblageNid, type.ordinal());
		if (existingValue != null && existingValue.intValue() != type.ordinal())
		{
			throw new IllegalArgumentException("Not allowed to change the object type an assemblage");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VersionType getVersionTypeForAssemblageNid(int assemblageNid)
	{
		Integer current = assemblageToVersionTypeMap.get(assemblageNid);
		if (current == null)
		{
			return VersionType.UNKNOWN;
		}
		else
		{
			return VersionType.values()[current.intValue()];
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAssemblageVersionType(int assemblageNid, VersionType type) throws IllegalStateException
	{
		Integer existingValue = assemblageToVersionTypeMap.putIfAbsent(assemblageNid, type.ordinal());
		if (existingValue != null && existingValue.intValue() != type.ordinal())
		{
			throw new IllegalArgumentException("Not allowed to change the version type an assemblage");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean implementsSequenceStore()
	{
		return false;
	}
	
	@Override
	public void compact()
	{
		LOG.info("Performing full compact on MV Store");
		store.compactRewriteFully();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean implementsExtendedStoreAPI()
	{
		return true;
	}

	//Extended Store API implementation below this
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public OptionalLong getSharedStoreLong(String key)
	{
		Long i = sharedStore.get(key);
		return i == null ? OptionalLong.empty() : OptionalLong.of(i.longValue());
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public OptionalLong putSharedStoreLong(String key, long value)
	{
		Long i = sharedStore.put(key, value);
		return i == null ? OptionalLong.empty() : OptionalLong.of(i.longValue());
	}

	@Override
	public OptionalLong removeSharedStoreLong(String key)
	{
		Long i = sharedStore.remove(key);
		return i == null ? OptionalLong.empty() : OptionalLong.of(i.longValue());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> ExtendedStoreData<K, V> getStore(String storeName)
	{
		return getStore(storeName, null, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V, VT> ExtendedStoreData<K, VT> getStore(String storeName, Function<VT, V> valueSerializer, Function<V, VT> valueDeserializer)
	{
		 return (ExtendedStoreData<K, VT>) extendedStores.computeIfAbsent(storeName, mapNameKey -> 
		 	new MVExtendedStore<K, V, VT>(this.store.<K, V>openMap(mapNameKey), valueSerializer, valueDeserializer));
	}
}