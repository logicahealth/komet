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

package sh.isaac.misc.exporters.rf2.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;

/**
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public abstract class RF2File
{
	private final File location;
	private final int colCount;
	private final BufferedWriter writer;
	private final Semaphore rowLock = new Semaphore(1);
	private final AtomicInteger writtenData = new AtomicInteger(0);
	
	/**
	 * 
	 * @param rootFolder
	 * @param relativePath
	 * @param fileType
	 * @param contentType
	 * @param contentSubType
	 * @param languageCode 
	 * @param releaseType
	 * @param namespace
	 * @param versionDate YYYYMMDD
	 * @param colNames 
	 * @throws IOException 
	 */
	public RF2File(File rootFolder, String relativePath, String fileType, String contentType, Optional<String> contentSubType, Optional<String> languageCode, 
			RF2ReleaseType releaseType, String namespace, String versionDate, String ... colNames) throws IOException
	{
		Path temp = rootFolder.toPath().resolve(releaseType.name() + "/" + relativePath);
		
		location = temp.resolve(fileType + "_" + contentType + "_" + contentSubType.orElse("") + releaseType.name() 
			+ (languageCode.isEmpty() ? "" : "-" + languageCode.get()) + "_" + namespace + "_" + versionDate + ".txt").toFile();
		location.getParentFile().mkdirs();
		
		colCount = colNames.length;
		writer = new BufferedWriter(new FileWriter(location, StandardCharsets.UTF_8));
		writeRow(colNames);
		//don't count the header
		writtenData.decrementAndGet();
	}
	
	/**
	 * For ad-hoc files like my error log file
	 * @param rootFolder
	 * @param releaseType - may be null, to place a file in the root folder
	 * @param fileName
	 * @throws IOException 
	 */
	protected RF2File(File rootFolder, RF2ReleaseType releaseType, String fileName) throws IOException
	{
		
		Path temp = rootFolder.toPath();
		if (releaseType != null)
		{
			temp = temp.resolve(releaseType.name());
		}
		location = temp.resolve(fileName + ".log").toFile();
		location.getParentFile().mkdirs();
		colCount = 0;
		writer = new BufferedWriter(new FileWriter(location, StandardCharsets.UTF_8));
	}
	
	/**
	 * This is thread safe
	 * @param columns - must match spec for file in length
	 * @throws IOException
	 */
	public void writeRow(String ... columns) throws IOException
	{
		if (columns.length != colCount)
		{
			throw new IllegalArgumentException("Wrong number of columns - should be " + colCount + " in " + location.toString());
		}
		rowLock.acquireUninterruptibly();
		for (int i = 0; i < columns.length; i++)
		{
			writer.append(columns[i]);
			if (i < (columns.length - 1))
			{
				writer.append("\t");
			}
		}
		writer.append("\r\n");
		rowLock.release();
		writtenData.incrementAndGet();
	}
	
	/**
	 * Bypass all column checking, and just write the string as a full line.
	 * @throws IOException 
	 */
	public void writeLine(String line) throws IOException
	{
		rowLock.acquireUninterruptibly();
		writer.append(line);
		writer.append("\r\n");
		rowLock.release();
		writtenData.incrementAndGet();
	}
	
	public void close() throws IOException
	{
		LogManager.getLogger().debug("Wrote {} rows of data to file {}", getWrittenRowCount(), location.getName());
		writer.close();
	}
	
	public int getWrittenRowCount()
	{
		return writtenData.get();
	}
	

}
