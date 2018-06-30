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
package sh.isaac.provider.datastore;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.DataStore;
import sh.isaac.model.DataStoreSubService;
import sh.isaac.model.SequenceStore;

/**
 * This class should be the only implementation of a DataStore in the system which carries a RunLevel.
 * 
 * When ISAAC starts, it will start this DataStore.  This implementation simply passes through to a real 
 * DataStore implementation, which this code allows to be dynamically selected.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Service
@RunLevel(value = LookupService.SL_L1)
@Rank(value=500)
public class DatastoreLocator implements DataStore, SequenceStore
{
	private static final Logger LOG = LogManager.getLogger();
	private static final String dbType = "dbType.txt";
	
	private DataStoreSubService dataStore;
	
	/**
	 * Start me.
	 */
	@PostConstruct
	private void startMe()
	{
		LOG.info("Starting DataStoreLocator provider post-construct");

		try
		{
			ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
			Path folderPath = configurationService.getDataStoreFolderPath();
			folderPath.toFile().mkdirs();
			File dbTypeFile = new File(folderPath.toFile(), dbType);
			DatabaseImplementation di;
			if (dbTypeFile.isFile())
			{
				di = DatabaseImplementation.parse(new String(Files.readAllBytes(folderPath.resolve(dbType))));
				LOG.info("Existing database type is " + di);
			}
			else
			{
				di = configurationService.getDatabaseImplementation();
				LOG.info("Configured database type is " + di);
			}
			
			switch (di)
			{
				case BDB:
				case XODUS:
				case MV:
				case FILESYSTEM:
					dataStore = LookupService.get().getService(DataStoreSubService.class, di.name());
					break;
				case DEFAULT:
					di = DatabaseImplementation.FILESYSTEM;
					dataStore = LookupService.get().getService(DataStoreSubService.class, di.name());
					break;
				default :
					throw new RuntimeException("Oops");
			}
			if (dataStore == null)
			{
				throw new RuntimeException("No implementation of a DataStoreSubService is available on the classpath with the name of " + di.name());
			}
			if (!dbTypeFile.isFile())
			{
				Files.write(folderPath.resolve(dbType), di.name().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			}
			dataStore.startup();
		}
		catch (Exception e)
		{
			LOG.error("Error starting delgated store", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stop me.
	 */
	@PreDestroy
	private void stopMe()
	{
		LOG.info("Stopping DataStoreLocator");
		dataStore.shutdown();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Path getDataStorePath()
	{
		return dataStore.getDataStorePath();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public DataStoreStartState getDataStoreStartState()
	{
		return dataStore.getDataStoreStartState();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Optional<UUID> getDataStoreId()
	{
		return dataStore.getDataStoreId();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Future<?> sync()
	{
		return dataStore.sync();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void putChronologyData(ChronologyImpl chronology)
	{
		dataStore.putChronologyData(chronology);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int[] getAssemblageConceptNids()
	{
		return dataStore.getAssemblageConceptNids();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public IsaacObjectType getIsaacObjectTypeForAssemblageNid(int assemblageNid)
	{
		return dataStore.getIsaacObjectTypeForAssemblageNid(assemblageNid);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public NidSet getAssemblageNidsForType(IsaacObjectType type)
	{
		return dataStore.getAssemblageNidsForType(type);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void putAssemblageIsaacObjectType(int assemblageNid, IsaacObjectType type) throws IllegalStateException
	{
		dataStore.putAssemblageIsaacObjectType(assemblageNid, type);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Optional<ByteArrayDataBuffer> getChronologyData(int nid)
	{
		return dataStore.getChronologyData(nid);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int[] getSemanticNidsForComponent(int componentNid)
	{
		return dataStore.getSemanticNidsForComponent(componentNid);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public OptionalInt getAssemblageOfNid(int nid)
	{
		return dataStore.getAssemblageOfNid(nid);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setAssemblageForNid(int nid, int assemblage) throws IllegalArgumentException
	{
		dataStore.setAssemblageForNid(nid, assemblage);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int[] getTaxonomyData(int assemblageNid, int conceptNid)
	{
		return dataStore.getTaxonomyData(assemblageNid, conceptNid);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int[] accumulateAndGetTaxonomyData(int assemblageNid, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction)
	{
		return dataStore.accumulateAndGetTaxonomyData(assemblageNid, conceptNid, newData, accumulatorFunction);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public VersionType getVersionTypeForAssemblageNid(int assemblageNid)
	{
		return dataStore.getVersionTypeForAssemblageNid(assemblageNid);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void putAssemblageVersionType(int assemblageNid, VersionType type) throws IllegalStateException
	{
		dataStore.putAssemblageVersionType(assemblageNid, type);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int getAssemblageMemoryInUse(int assemblageNid)
	{
		return dataStore.getAssemblageMemoryInUse(assemblageNid);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int getAssemblageSizeOnDisk(int assemblageNid)
	{
		return dataStore.getAssemblageSizeOnDisk(assemblageNid);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChronologyData(int nid, IsaacObjectType ofType)
	{
		return dataStore.hasChronologyData(nid, ofType);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void registerDataWriteListener(DataWriteListener dataWriteListener)
	{
		dataStore.registerDataWriteListener(dataWriteListener);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterDataWriteListener(DataWriteListener dataWriteListener)
	{
		dataStore.unregisterDataWriteListener(dataWriteListener);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public IntStream getNidsForAssemblage(int assemblageNid)
	{
		return dataStore.getNidsForAssemblage(assemblageNid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean implementsSequenceStore()
	{
		return dataStore.implementsSequenceStore();
	}

	@Override
	public int getElementSequenceForNid(int nid)
	{
		if (implementsSequenceStore())
		{
			return ((SequenceStore)dataStore).getElementSequenceForNid(nid);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public int getElementSequenceForNid(int nid, int assemblageNid)
	{
		if (implementsSequenceStore())
		{
			return ((SequenceStore)dataStore).getElementSequenceForNid(nid, assemblageNid);
		}
		throw new UnsupportedOperationException();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int getNidForElementSequence(int assemblageNid, int sequence)
	{
		if (implementsSequenceStore())
		{
			return ((SequenceStore)dataStore).getNidForElementSequence(assemblageNid, sequence);
		}
		throw new UnsupportedOperationException();
	}
}
