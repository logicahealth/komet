package gov.vha.isaac.ochre.api.util;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;

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
			byte[] digest = md.digest();
			return new BigInteger(1, digest).toString(16);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected error: " + e);
		}
	}
}
