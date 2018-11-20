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
import javafx.application.Platform;

/**
 * Just a main for running the Turtle import in eclipse, so it launches with a logging config
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class TurtleMojoRunner extends TurtleImportMojoDirect
{

	/**
	 * @param args
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MojoExecutionException, IOException
	{
		TurtleMojoRunner i = new TurtleMojoRunner();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocationPath = new File("../../integration/tests/src/test/resources/turtle/").toPath();
		i.converterOutputArtifactVersion = "turtle-0.8";
		i.converterVersion = "SNAPSHOT";
		i.converterSourceArtifactVersion = "0.8";
		i.execute();
		Platform.exit();
	}

}
