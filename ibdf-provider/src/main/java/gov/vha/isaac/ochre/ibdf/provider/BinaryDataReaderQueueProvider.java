/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.ibdf.provider;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderQueueService;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.task.TimedTaskWithProgressTracker;

/**
 * 
 * {@link BinaryDataReaderQueueProvider}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class BinaryDataReaderQueueProvider
	extends TimedTaskWithProgressTracker<Integer>
	implements BinaryDataReaderQueueService, Spliterator<OchreExternalizableUnparsed>
{

	Path dataPath;
	DataInputStream input;
	int streamBytes;
	int objects = 0;

	int NOTSTARTED = 3;
	int RUNNING = 2;
	int DONEREADING = 1;
	int COMLETE = 0;

	CountDownLatch complete = new CountDownLatch(NOTSTARTED);

	//Only one thread doing the reading from disk, give it lots of buffer space
	private BlockingQueue<OchreExternalizableUnparsed> readData = new ArrayBlockingQueue<>(5000);
	//This buffers from between the time when we deserialize the object, and when we write it back to the DB.
	private BlockingQueue<OchreExternalizable> parsedData = new ArrayBlockingQueue<>(50);
	ExecutorService es_;

	public BinaryDataReaderQueueProvider(Path dataPath) throws FileNotFoundException
	{
		this.dataPath = dataPath;
		this.input = new DataInputStream(new FileInputStream(dataPath.toFile()));
		try
		{
			streamBytes = input.available();
			addToTotalWork(streamBytes);
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private Stream<OchreExternalizableUnparsed> getStreamInternal()
	{
		running();
		return StreamSupport.stream(this, false);
	}

	/**
	 * 
	 * @return the number of objects read.
	 */
	@Override
	protected Integer call()
	{
		try
		{
			complete.await();
		}
		catch (InterruptedException ex)
		{
			throw new RuntimeException(ex);
		}
		return objects;
	}

	@Override
	public void shutdown()
	{
		try
		{
			input.close();
			if (complete.getCount() == RUNNING)
			{
				complete.countDown();
			}
			es_.shutdown();
			while (!readData.isEmpty())
			{
				Thread.sleep(10);
			}
			es_.shutdownNow();
			es_.awaitTermination(50, TimeUnit.MINUTES);
			if (complete.getCount() == DONEREADING)
			{
				complete.countDown();
			}
			done();
		}
		catch (IOException | InterruptedException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean isFinished()
	{
		return complete.getCount() == COMLETE;
	}

	@Override
	public boolean tryAdvance(Consumer<? super OchreExternalizableUnparsed> action)
	{
		try
		{
			int startBytes = input.available();
			OchreExternalizableObjectType type = OchreExternalizableObjectType.fromDataStream(input);
			byte dataFormatVersion = input.readByte();
			int recordSize = input.readInt();
			byte[] objectData = new byte[recordSize];
			input.readFully(objectData);
			ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(objectData);
			buffer.setExternalData(true);
			buffer.setObjectDataFormatVersion(dataFormatVersion);
			action.accept(new OchreExternalizableUnparsed(type, buffer));
			objects++;
			completedUnitsOfWork(startBytes - input.available());
			return true;
		}
		catch (EOFException ex)
		{
			shutdown();
			return false;
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Spliterator<OchreExternalizableUnparsed> trySplit()
	{
		return null;
	}

	@Override
	public long estimateSize()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public int characteristics()
	{
		return IMMUTABLE | NONNULL;
	}

	/**
	 * 
	 * @see gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderQueueService#getQueue()
	 */
	@Override
	public BlockingQueue<OchreExternalizable> getQueue()
	{
		if (complete.getCount() == NOTSTARTED)
		{
			synchronized (complete)
			{
				if (complete.getCount() == NOTSTARTED)
				{
					complete.countDown();
					//These threads handle the parsing of the bytes back into ochre objects, which is kind of slow, as all of the UUIDs have
					//to be resolved back to nids and sequences.  Seems to work best to use about 2/3 of the processors here.
					int threadCount = Math.round((float) Runtime.getRuntime().availableProcessors() * (float) 0.667);
					threadCount = (threadCount < 2 ? 2 : threadCount);
					es_ = Executors.newFixedThreadPool(threadCount);
					for (int i = 0; i < threadCount; i++)
					{
						es_.execute(() -> 
						{
							while (complete.getCount() > COMLETE || !readData.isEmpty())
							{
								boolean accepted;
								try
								{
									accepted = parsedData.offer(readData.take().parse(), 5, TimeUnit.MINUTES);
								}
								catch (InterruptedException e)
								{
									break;
								}
								if (!accepted)
								{
									throw new RuntimeException("unexpeced queue issues");
								}
							}
						});
					}

					Get.workExecutors().getExecutor().execute(() -> 
					{
						try
						{
							getStreamInternal().forEach((unparsed) -> 
							{
								try
								{
									readData.offer(unparsed, 5, TimeUnit.MINUTES);
								}
								catch (Exception e)
								{
									throw new RuntimeException(e);
								}
							});
						}
						catch (Exception e)
						{
							Get.workExecutors().getExecutor().execute(() -> {
								shutdown();
							});
							throw e;
						}
					});
				}
			}
		}
		return parsedData;
	}
}