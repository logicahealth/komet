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
package sh.isaac.convert.directUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.provider.ibdf.BinaryDataWriterProvider;

/**
 * Used to intercept the data being written to the datastore, so we can also write out an IBDF file at the same time.
 * Current part of the rf2Direct import package, but will probably migrate to a more shared location in the future...
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DataWriteListenerImpl implements DataWriteListener
{
	private Logger log = LogManager.getLogger();
	
	private BinaryDataWriterProvider writer;
	private Set<Integer> assemblageTypesToIgnore;
	private ConcurrentHashMap<Integer, Boolean> chainedIgnores = new ConcurrentHashMap<>(); 
	int ignored = 0;
	
	public DataWriteListenerImpl(Path ibdfFileToWrite, Set<Integer> assemblageTypesToIgnore) throws IOException 
	{
		log.info("Writing IBDF to " + ibdfFileToWrite.toFile().getCanonicalPath());
		this.writer = new BinaryDataWriterProvider(ibdfFileToWrite, true);
		this.assemblageTypesToIgnore = assemblageTypesToIgnore;
	}
	
	public void close() throws IOException
	{
		writer.close();
		writer = null;
		log.info("IBDF writer closed");
		if (ignored > 0)
		{
			log.info("Skipped {} chronologies", ignored);
		}
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(Chronology data)
	{
		if (assemblageTypesToIgnore != null && 
				(assemblageTypesToIgnore.contains(data.getAssemblageNid()) ||
						(data.getIsaacObjectType() == IsaacObjectType.SEMANTIC && chainedIgnores.containsKey(((SemanticChronology)data).getReferencedComponentNid()))))
		{
			//ignore - these are just intermediate things Keith creates, that we don't actually want / need in the DB
			ignored++;
			chainedIgnores.put(data.getNid(), Boolean.FALSE);
		}
		else
		{
			writer.put(data);
		}
	}
	
	/**
	 * Careful using this - likely not thread safe
	 * @return The undelying writer
	 */
	public DataWriterService getWriterHandle()
	{
		return writer;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void sync()
	{
		try 
		{
			writer.flush();
		}
		catch (IOException e) 
		{
			log.error("Flush error", e);
		}
	}
}
