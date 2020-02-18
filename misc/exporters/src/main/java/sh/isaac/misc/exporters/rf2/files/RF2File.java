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

public abstract class RF2File
{
	private final File location;
	private final int colCount;
	private final BufferedWriter writer;
	private final Semaphore rowLock = new Semaphore(1);
	//start negative, so we don't count the header row
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
