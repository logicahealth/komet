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
package sh.isaac.dbConfigBuilder.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonReader.ClassFactory;
import com.cedarsoftware.util.io.JsonWriter;
import sh.isaac.api.util.PasswordHasher;

/**
 * Prefs store for the ContentManager
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class StoredPrefs
{
	private transient static Logger log = LogManager.getLogger();
	private String gitURL;
	private String gitUsername;
	private String gitPasswordEncrypted;

	private String artifactReadURL;
	private String artifactSnapshotDeployURL;
	private String artifactReleaseDeployURL;
	private String artifactUsername;
	private String artifactPasswordEncrypted;

	private String localM2FolderPath;
	private String mavenSettingsFile;
	
	private String pwCheck;
	
	private transient char[] passwordEncryptionPassword;
	
	private StoredPrefs()
	{
		//For json deserialize
	}


	public StoredPrefs(char[] passwordEncryptionPassword)
	{
		this.passwordEncryptionPassword = passwordEncryptionPassword;
		try
		{
			pwCheck = PasswordHasher.encrypt(passwordEncryptionPassword, "doesn't matter".toCharArray());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param jsonFileToRead
	 * @param decryptionPassword the password to use to decrypt the encrypted values
	 * @param ignoreBadPassword - if true, and the decryptionPassword is wrong, the returned StoredPrefs object will have the encrypted fields blanked out.
	 * if false, and the password is wrong, throws an error.
	 * @return The prefs read from disk.
	 * @throws FileNotFoundException If the jsonFileToRead isn't present
	 * @throws IllegalArgumentException If the password is wrong, and ignoreBadPassword is false.
	 */
	public static StoredPrefs readStoredPrefs(File jsonFileToRead, char[] decryptionPassword, boolean ignoreBadPassword) throws FileNotFoundException, IllegalArgumentException
	{
		JsonReader.assignInstantiator(StoredPrefs.class, new ClassFactory()
		{
			@SuppressWarnings("rawtypes")
			@Override
			public Object newInstance(Class c)
			{
				return new StoredPrefs();
			}
		});
		
		try (JsonReader jr = new JsonReader(new FileInputStream(jsonFileToRead)))
		{
			StoredPrefs sp = (StoredPrefs) jr.readObject();
			sp.passwordEncryptionPassword = decryptionPassword;
			try
			{
				PasswordHasher.decryptToString(decryptionPassword, sp.pwCheck);
			}
			catch (Exception e)
			{
				if (ignoreBadPassword)
				{
					sp.setGitPassword(new char[] {});
					sp.setArtifactPassword(new char[] {});
					sp.passwordEncryptionPassword = decryptionPassword;
					try
					{
						sp.pwCheck = PasswordHasher.encrypt(decryptionPassword, "doesn't matter".toCharArray());
					}
					catch (Exception e1)
					{
						throw new RuntimeException("This should be impossible");
					}
					return sp;
				}
				else
				{
					throw new IllegalArgumentException("Incorrect password");
				}
			}
			return sp;
		}
	}

	public void store(File jsonFileToWrite) throws FileNotFoundException
	{
		final Map<String, Object> args = new HashMap<>();

		args.put(JsonWriter.PRETTY_PRINT, true);
		JsonWriter jw = new JsonWriter(new FileOutputStream(jsonFileToWrite), args);
		jw.write(this);
		jw.close();
	}

	public String getGitURL()
	{
		return gitURL == null ? "" : gitURL;
	}

	public void setGitURL(String gitURL)
	{
		this.gitURL = gitURL;
	}

	public String getGitUsername()
	{
		return gitUsername == null ? "" : gitUsername;

	}

	public void setGitUsername(String gitUsername)
	{
		this.gitUsername = gitUsername;
	}

	public char[] getGitPassword()
	{
		try
		{
			return (gitPasswordEncrypted != null && gitPasswordEncrypted.length() > 0) ? 
					PasswordHasher.decryptToChars(passwordEncryptionPassword, gitPasswordEncrypted) : new char[] {};
		}
		catch (Exception e)
		{
			log.error("Error decrypting git password");
			return new char[] {};
		}
	}

	public void setGitPassword(char[] gitPassword)
	{
		try
		{
			this.gitPasswordEncrypted = gitPassword.length > 0 ? PasswordHasher.encrypt(passwordEncryptionPassword, gitPassword) : "";
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected error encrypting git password");
		};
	}

	public String getArtifactReadURL()
	{
		return artifactReadURL == null ? "" : artifactReadURL;
	}

	public void setArtifactReadURL(String artifactReadURL)
	{
		this.artifactReadURL = artifactReadURL;
	}
	
	public String getArtifactSnapshotDeployURL()
	{
		return artifactSnapshotDeployURL == null ? "" : artifactSnapshotDeployURL;
	}

	public void setArtifactReleaseDeployURL(String artifactReleaseDeployURL)
	{
		this.artifactReleaseDeployURL = artifactReleaseDeployURL;
	}
	
	public String getArtifactReleaseDeployURL()
	{
		return artifactReleaseDeployURL == null ? "" : artifactReleaseDeployURL;
	}
	
	public void setArtifactSnapshotDeployURL(String artifactSnapshotDeployURL)
	{
		this.artifactSnapshotDeployURL = artifactSnapshotDeployURL;
	}

	public String getArtifactUsername()
	{
		return artifactUsername == null ? "" : artifactUsername;
	}

	public void setArtifactUsername(String artifactUsername)
	{
		this.artifactUsername = artifactUsername;
	}

	public char[] getArtifactPassword()
	{
		try
		{
			return (artifactPasswordEncrypted != null && artifactPasswordEncrypted.length() > 0) 
					? PasswordHasher.decryptToChars(passwordEncryptionPassword, artifactPasswordEncrypted) : new char[] {};
		}
		catch (Exception e)
		{
			log.error("Error decrypting artifact password", e);
			return new char[] {};
		}
	}

	public void setArtifactPassword(char[] artifactPassword)
	{
		try
		{
			this.artifactPasswordEncrypted = artifactPassword.length > 0 ? PasswordHasher.encrypt(passwordEncryptionPassword, artifactPassword) : "";
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected error encrypting artifact password");
		};
	}

	public String getLocalM2FolderPath()
	{
		return StringUtils.isBlank(localM2FolderPath) ? new File(System.getProperty("user.home") + "/.m2/").getAbsolutePath() : localM2FolderPath;
	}

	public void setLocalM2FolderPath(String localM2FolderPath)
	{
		this.localM2FolderPath = localM2FolderPath;
	}
	
	public String getMavenSettingsFile()
	{
		return StringUtils.isBlank(mavenSettingsFile) ? new File(System.getProperty("user.home") + "/.m2/settings.xml").getAbsolutePath() : mavenSettingsFile;
	}

	public void setMavenSettingsFile(String mavenSettingsFile)
	{
		this.mavenSettingsFile = mavenSettingsFile;
	}

	public void setPasswordEncryptionPassword(char[] passwordEncryptionPassword)
	{
		char[] artTemp = getArtifactPassword();
		char[] gitTemp = getGitPassword();
		this.passwordEncryptionPassword = passwordEncryptionPassword;
		setArtifactPassword(artTemp);
		setGitPassword(gitTemp);
	}
}
