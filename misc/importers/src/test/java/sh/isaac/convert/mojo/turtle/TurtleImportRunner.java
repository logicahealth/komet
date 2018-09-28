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
package sh.isaac.convert.mojo.turtle;

import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import javafx.application.Platform;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.constants.DatabaseInitialization;

/**
 * Just a main for running the Turtle import in eclipse, so it launches with a logging config
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class TurtleImportRunner
{

	public static void main(String[] args) throws MojoExecutionException, IOException
	{
		try
		{
			File file = new File("target", "isaac-turtle.data");
			// make sure this is empty
			FileUtils.deleteDirectory(file);

			Get.configurationService().setDataStoreFolderPath(file.toPath());

			Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);

			LookupService.startupIsaac();

			TurtleImportMojoDirect timd = new TurtleImportMojoDirect();
			timd.configure(null, new File("../../integration/tests/src/test/resources/turtle/bevontology-0.8.ttl").toPath(), "0.8", null);
			timd.convertContent(update -> {});
		}
		finally
		{
			LookupService.shutdownSystem();
			Platform.exit();
		}
	}

}
