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
import java.nio.file.Files;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * Test code for json read/write of stored prefs
 */
public class StoredPrefsTest
{
	@Test
	public void testStorage() throws Exception
	{
		final StoredPrefs sp = new StoredPrefs("super secret".toCharArray());
		
		sp.setArtifactPassword(new char[] {'z', 'c'});
		sp.setArtifactURL("1");
		sp.setArtifactUsername("2");
		sp.setGitPassword(new char[] {});
		sp.setGitURL("3");
		sp.setGitUsername("fred");
		sp.setLocalM2FolderPath("jane");
		
		File tempFile = Files.createTempFile(null, null).toFile();
		
		sp.store(tempFile);
		
		StoredPrefs read = StoredPrefs.readStoredPrefs(tempFile, "super secret".toCharArray(), false);
		
		
		Assert.assertEquals(new String(sp.getArtifactPassword()), new String(read.getArtifactPassword()));
		Assert.assertEquals(sp.getArtifactURL(),  read.getArtifactURL());
		Assert.assertEquals(sp.getArtifactUsername(),  read.getArtifactUsername());
		Assert.assertEquals(new String(sp.getGitPassword()),  new String(read.getGitPassword()));
		Assert.assertEquals(sp.getGitURL(),  read.getGitURL());
		Assert.assertEquals(sp.getGitUsername(),  read.getGitUsername());
		Assert.assertEquals(sp.getLocalM2FolderPath(),  read.getLocalM2FolderPath());
		tempFile.delete();
	}
	
	@Test
	public void testStorage2() throws Exception
	{
		final StoredPrefs sp = new StoredPrefs("super secret".toCharArray());
		
		sp.setArtifactPassword(new char[] {'z', 'c'});
		sp.setArtifactURL("1");
		sp.setArtifactUsername("2");
		sp.setGitPassword(new char[] {});
		sp.setGitURL("3");
		sp.setGitUsername("fred");
		sp.setLocalM2FolderPath("jane");
		
		File tempFile = Files.createTempFile(null, null).toFile();
		
		sp.store(tempFile);
		
		try
		{
			StoredPrefs.readStoredPrefs(tempFile, "wrong password".toCharArray(), false);
			Assert.fail();
		}
		catch (Exception e)
		{
			//expected path
		}
		
		StoredPrefs read = StoredPrefs.readStoredPrefs(tempFile, "wrong password".toCharArray(), true);
		
		
		Assert.assertEquals("", new String(read.getArtifactPassword()));
		Assert.assertEquals(sp.getArtifactURL(),  read.getArtifactURL());
		Assert.assertEquals(sp.getArtifactUsername(),  read.getArtifactUsername());
		Assert.assertEquals("",  new String(read.getGitPassword()));
		Assert.assertEquals(sp.getGitURL(),  read.getGitURL());
		Assert.assertEquals(sp.getGitUsername(),  read.getGitUsername());
		Assert.assertEquals(sp.getLocalM2FolderPath(),  read.getLocalM2FolderPath());
		tempFile.delete();
	}

}
