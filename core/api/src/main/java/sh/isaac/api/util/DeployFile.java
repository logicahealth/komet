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
package sh.isaac.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.concurrent.Task;

/**
 * {@link DeployFile} Deploys files in a maven pattern to a nexus (and probably other) artifact servers.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DeployFile extends Task<Integer>
{
	private static Logger log = LogManager.getLogger(DeployFile.class);

	String groupId_;
	String artifactId_;
	String version_;
	String classifier_;
	String dataType_;
	File dataFile_;
	String url_;
	String username_;
	String password_;
	
	StringBuilder status_ = new StringBuilder();
	
	public DeployFile(String groupId, String artifactId, String version, String classifier, String dataType, File dataFile, String url, String username, String password) 
			throws Exception
	{
		groupId_ = groupId;
		artifactId_ = artifactId;
		version_ = version;
		classifier_ = classifier;
		dataType_ = dataType;
		dataFile_ = dataFile;
		url_ = url;
		username_ = username;
		password_ = password;
	}

	private void writeChecksumFile(File file, String type, File toFolder) throws NoSuchAlgorithmException, IOException
	{
		updateTitle("Calculating " + type + " Checksum for " + file.getName());
		MessageDigest md = MessageDigest.getInstance(type);
		try (InputStream is = Files.newInputStream(file.toPath()))
		{
			DigestInputStream dis = new DigestInputStream(is, md);
			byte[] buffer = new byte[8192];
			long fileLength = file.length();
			long loopCount = 0;
			int read = 0;
			while (read != -1)
			{
				//update every 10 MB
				if (loopCount++ % 1280 == 0)
				{
					updateProgress((loopCount * 8192l), fileLength);
					updateTitle("Calculating Checksum for " + file.getName() + " - " + (loopCount * 8192l) + " / " + fileLength);
				}
				read = dis.read(buffer);
			}
		}
		byte[] digest = md.digest();
		String checkSum = new BigInteger(1, digest).toString(16);

		Files.write(new File(toFolder, file.getName() + "." + type.toLowerCase()).toPath(), 
				checkSum.getBytes(), StandardOpenOption.WRITE,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		updateTitle("");
		updateProgress(-1, 0);
	}

	private void putFile(File file, String targetFileName) throws Exception
	{
		String groupIdTemp = groupId_;
		groupIdTemp = groupIdTemp.replaceAll("\\.", "/");
		URL url = new URL(url_ + (url_.endsWith("/") ? "" : "/") + groupIdTemp + "/" + artifactId_ + "/" + version_
				+ "/" + (targetFileName == null ? file.getName() : targetFileName));

		log.info("Uploading " + file.getAbsolutePath() + " to " + url.toString());
		
		updateTitle("Uploading " + file.getName());
		updateProgress(0, file.length());

		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		if (username_.length() > 0 || password_.length() > 0)
		{
			String encoded = Base64.getEncoder().encodeToString((username_ + ":" + password_).getBytes());
			httpCon.setRequestProperty("Authorization", "Basic " + encoded);
		}
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		httpCon.setConnectTimeout(30 * 1000);
		httpCon.setReadTimeout(60 * 60 * 1000);
		long fileLength = file.length();
		httpCon.setFixedLengthStreamingMode(fileLength);
		OutputStream out = httpCon.getOutputStream();

		byte[] buf = new byte[8192];
		long loopCount = 0;
		FileInputStream fis = new FileInputStream(file);
		int read = 0;
		while ((read = fis.read(buf, 0, buf.length)) > 0)
		{
			//update every MB
			if (loopCount++ % 128 == 0)
			{
				updateProgress((loopCount * 8192l), fileLength);
				updateTitle("Uploading " + file.getName() + " - " + (loopCount * 8192l) + " / " + fileLength);
			}
			out.write(buf, 0, read);
		}
		out.flush();
		out.close();
		fis.close();
		InputStream is = httpCon.getInputStream();
		StringBuilder sb = new StringBuilder();
		read = 0;
		byte[] buffer = new byte[1024];
		CharBuffer cBuffer = ByteBuffer.wrap(buffer).asCharBuffer();
		while (read != -1)
		{
			read = is.read(buffer);
			if (read > 0)
			{
				sb.append(cBuffer, 0, read);
			}
		}
		httpCon.disconnect();
		if (sb.toString().trim().length() > 0)
		{
			throw new Exception("The server reported an error during the publish operation:  " + sb.toString());
		}
		log.info("Upload Successful");
		updateTitle("Upload Successful");
		updateProgress(-1, 0);
	}

	/**
	 * @see javafx.concurrent.Task#call()
	 */
	@Override
	protected Integer call() throws Exception
	{
		updateProgress(0, 5);

		updateStatus("Creating Checksum Files");
		writeChecksumFile(dataFile_, "MD5", dataFile_.getParentFile());
		updateProgress(1, 5);
		writeChecksumFile(dataFile_, "SHA1", dataFile_.getParentFile());
		updateProgress(2, 5);

		updateStatus("Uploading files");
		putFile(new File(dataFile_.getParentFile(), dataFile_.getName() + ".md5"), 
				StringUtils.isBlank(dataType_) ? null : artifactId_ + "-" + version_ + (StringUtils.isNotBlank(classifier_) ? "-" + classifier_ : "") + "." + dataType_ + ".md5");
		updateProgress(3, 5);
		putFile(new File(dataFile_.getParentFile(), dataFile_.getName() + ".sha1"), 
				StringUtils.isBlank(dataType_) ? null : artifactId_ + "-" + version_ + (StringUtils.isNotBlank(classifier_) ? "-" + classifier_ : "") + "." + dataType_ + ".sha1");
		updateProgress(4, 5);
		putFile(dataFile_, 
				StringUtils.isBlank(dataType_) ? null : artifactId_ + "-" + version_ + (StringUtils.isNotBlank(classifier_) ? "-" + classifier_ : "") + "." + dataType_);
		updateProgress(5, 5);
		
		updateTitle("Deploy complete");
		return 0;
	}
	
	private void updateStatus(String message)
	{
		log.debug(message);
		status_.append(message + "\r\n");
		updateMessage(status_.toString());
	}
}
