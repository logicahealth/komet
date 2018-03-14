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
package test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.util.DownloadUnzipTask;
import sh.isaac.dbConfigBuilder.artifacts.MavenArtifactUtils;

/**
 * adhoc tests
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class MavenArtifactUtilsTest
{
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws InterruptedException the interrupted exception
	 * @throws ExecutionException the execution exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException
	{
		try
		{
			final String username = "foo";
			final char[] userpd = "foo".toCharArray();

			LookupService.startupWorkExecutors();

			{
				//test a non-snapshot
				final URL release = new URL(
						"http://artifactory.isaac.sh/artifactory/libs-release-local" + MavenArtifactUtils.makeMavenRelativePath("aopalliance", "aopalliance", "1.0", null, "jar"));
				Task<File> task = new DownloadUnzipTask(null, null, release, false, true, null);
	
				Get.workExecutors().getExecutor().submit(task);
	
				File foo = task.get();
	
				System.out.println(foo.getCanonicalPath());
				
				foo.delete();
				foo.getParentFile().delete();
			}

			//test a snapshot
			
			{
				final File where = new File("").getAbsoluteFile();
				URL snapshot = new URL(
						"https://sagebits.net/nexus/repository/tmp-content/" + MavenArtifactUtils.makeMavenRelativePath("https://sagebits.net/nexus/repository/tmp-content/",
								username, userpd, "sh.isaac.misc", "importers", "4.48-SNAPSHOT", "", "jar"));
	
				Task<File> task = new DownloadUnzipTask(username, userpd, snapshot, true, true, where);
				Get.workExecutors().getExecutor().submit(task);
				File foo = task.get();
				System.out.println(foo.getCanonicalPath());
			}
			
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		Platform.exit();
	}

}
