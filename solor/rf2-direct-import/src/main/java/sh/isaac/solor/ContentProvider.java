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
package sh.isaac.solor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * A way to pass around closeable input streams for doing imports without passing around all the zip file mess.
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class ContentProvider implements Supplier<ContentStreamProvider>
{
	String streamSourceName;
	Supplier<ContentStreamProvider> contentStream;
	private byte[] itemBytes;
	
	public ContentProvider(File zipFile, ZipEntry entry)
	{
		this(zipFile, null, entry, null);
	}

	public ContentProvider(File zipFile, ZipEntry nestedZipFile, ZipEntry entry, byte[] unzippedOuterBytes)
	{
		this.itemBytes = unzippedOuterBytes;
		streamSourceName = (nestedZipFile == null ? (zipFile.getName() + ":" +  entry.getName()) : 
			(zipFile.getName() + ":" + nestedZipFile.getName() + ":" +  entry.getName()));
			
		contentStream = new Supplier<ContentStreamProvider>()
		{
			@Override
			public ContentStreamProvider get()
			{
				return new ContentStreamProvider()
				{
					private ZipFile zipFileHandle;
					
					@Override
					public void close() throws Exception
					{
						if (zipFileHandle != null)
						{
							zipFileHandle.close();
						}
						ContentProvider.this.itemBytes = null;
					}
					
					@Override
					public BufferedReader get()
					{
						try
						{
							if (nestedZipFile == null)
							{
								zipFileHandle = new ZipFile(zipFile, Charset.forName("UTF-8"));
								return new BufferedReader(new InputStreamReader(zipFileHandle.getInputStream(entry), Charset.forName("UTF-8")));
							}
							else
							{
								
								if (ContentProvider.this.itemBytes == null)
								{
									// This is returning a stream of another zip file.
									zipFileHandle = new ZipFile(zipFile, Charset.forName("UTF-8"));
									ZipInputStream innerZipStream = new ZipInputStream(zipFileHandle.getInputStream(nestedZipFile), Charset.forName("UTF-8"));
									ZipEntry innerZipEntry = innerZipStream.getNextEntry();
									while (innerZipEntry != null)
									{
										if (innerZipEntry.getName().equals(entry.getName()))
										{
											return new BufferedReader(new InputStreamReader(innerZipStream, Charset.forName("UTF-8")));
										}
										innerZipEntry = innerZipStream.getNextEntry();
									}
									throw new RuntimeException("Wrong file names passed in");
								}
								else
								{
									return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ContentProvider.this.itemBytes), Charset.forName("UTF-8")));
								}
							}
						}
						catch (IOException e)
						{
							throw new RuntimeException(e);
						}
					}
				};
			}
		};
	}
	
	public String getStreamSourceName()
	{
		return streamSourceName;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public ContentStreamProvider get()
	{
		return contentStream.get();
	}
}
