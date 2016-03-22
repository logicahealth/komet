package gov.vha.isaac.ochre.api.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javafx.concurrent.Task;

public class ChecksumGenerator
{
	/**
	 * Accepts types like "MD5 or SHA1"
	 * @param data
	 * @return
	 */
	public static String calculateChecksum(String type, byte[] data)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance(type);
			DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(data), md);
			dis.read(data);
			return getStringValue(md);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected error: " + e);
		}
	}
	
	
	public static Task<String> calculateChecksum(String type, File data)
	{
		Task<String> checkSumCalculator = new Task<String>()
		{
			@Override
			protected String call() throws Exception
			{
				long fileLength = data.length();
				updateProgress(0, fileLength);
				updateMessage("Calculating Checksum for " + data.getName() + " - 0 / " + fileLength);
				MessageDigest md = MessageDigest.getInstance(type);
				try (InputStream is = Files.newInputStream(data.toPath()))
				{
					DigestInputStream dis = new DigestInputStream(is, md);
					byte[] buffer = new byte[8192];
					
					long loopCount = 0;
					int read = 0;
					while (read != -1)
					{
						//update every 10 MB
						if (loopCount++ % 1280 == 0)
						{
							updateProgress((loopCount * 8192l), fileLength);
							updateTitle("Calculating Checksum for " + data.getName() + " - " + (loopCount * 8192l) + " / " + fileLength);
						}
						read = dis.read(buffer);
					}
					updateProgress(fileLength, fileLength);
					updateMessage("Done calculating Checksum for " + data.getName());
					return getStringValue(md);
				}
			}
		};
		
		return checkSumCalculator;
	}
	
	private static String getStringValue(MessageDigest md)
	{
		byte[] digest = md.digest();
		return new HexBinaryAdapter().marshal(digest).toLowerCase();
	}
}
