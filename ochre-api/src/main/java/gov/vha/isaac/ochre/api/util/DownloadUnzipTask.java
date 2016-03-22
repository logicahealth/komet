/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
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
package gov.vha.isaac.ochre.api.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.concurrent.Task;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

/**
 * {@link DownloadUnzipTask}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("restriction")
public class DownloadUnzipTask extends Task<Integer>
{
	private static Logger log = LoggerFactory.getLogger(DownloadUnzipTask.class);
	String username_, password_;
	URL url_;
	private boolean cancel = false;

	public DownloadUnzipTask(String username, String password, URL url)
	{
		this.username_ = username;
		this.password_ = password;
		this.url_ = url;
	}

	/**
	 * @see javafx.concurrent.Task#call()
	 */
	@Override
	protected Integer call() throws Exception
	{
		log.debug("Beginning download from " + url_);
		HttpURLConnection httpCon = (HttpURLConnection) url_.openConnection();
		if (username_.length() > 0 || password_.length() > 0)
		{
			String encoded = Base64.getEncoder().encodeToString((username_ + ":" + password_).getBytes());
			httpCon.setRequestProperty("Authorization", "Basic " + encoded);
		}
		httpCon.setDoInput(true);
		httpCon.setRequestMethod("GET");
		httpCon.setConnectTimeout(30 * 1000);
		httpCon.setReadTimeout(60 * 60 * 1000);
		long fileLength = httpCon.getContentLengthLong();
		InputStream in = httpCon.getInputStream();
		
		File file = File.createTempFile("DBDownload", "");

		byte[] buf = new byte[1048576];
		FileOutputStream fos= new FileOutputStream(file);
		int read = 0;
		long totalRead = 0;
		while (!cancel && (read = in.read(buf, 0, buf.length)) > 0)
		{
			totalRead += read;
			//update every 1 MB
			updateProgress(totalRead, fileLength);
			float percentDone = (float)((int)(((float)totalRead / (float)fileLength) * 1000)) / 10f;
			updateTitle("Downloading - " + percentDone + " % - out of " + fileLength + " bytes");
			fos.write(buf, 0, read);
		}
		fos.flush();
		fos.close();
		in.close();
		
		if (cancel)
		{
			log.debug("Download cancelled");
			throw new Exception("Cancelled!");
		}
		log.debug("Download complete - start unzip");
		
		updateTitle("Unzipping");
		try
		{
			ZipFile zipFile = new ZipFile(file);
			zipFile.setRunInThread(true);
			zipFile.extractAll(new File("").getAbsolutePath());
			while (zipFile.getProgressMonitor().getState() == ProgressMonitor.STATE_BUSY)
			{
				if (cancel)
				{
					zipFile.getProgressMonitor().cancelAllTasks();
					break;
				}
				updateProgress(zipFile.getProgressMonitor().getPercentDone(), 100);
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					// noop
				}
			}
			log.debug("Unzip complete");
		}
		catch (Exception e)
		{
			log.error("error unzipping", e);
			throw new Exception("The downloaded file doesn't appear to be a zip file");
		}
		finally
		{
			file.delete();
		}
		
		return 0;
	}

	/**
	 * @see javafx.concurrent.Task#cancel(boolean)
	 */
	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		super.cancel(mayInterruptIfRunning);
		cancel = true;
		return true;
	}

}
