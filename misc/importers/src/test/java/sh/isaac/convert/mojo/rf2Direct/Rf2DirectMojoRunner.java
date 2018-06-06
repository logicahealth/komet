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
package sh.isaac.convert.mojo.rf2Direct;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import javafx.application.Platform;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         Just a runner class in the test package, so that eclipse launches with a classpath that includes test libraries, which makes
 *         logging work to the console.
 */
public class Rf2DirectMojoRunner extends Rf2DirectImportMojo
{
	public static void main(String[] args) throws MojoExecutionException
	{
		Rf2DirectMojoRunner i = new Rf2DirectMojoRunner();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocation= new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
		i.converterOutputArtifactVersion = "20170731T150000Z-loader-4.48-SNAPSHOT";
		i.converterVersion = "4.48-SNAPSHOT";
		i.converterSourceArtifactVersion = "20170731T150000Z";
		i.converterOutputArtifactClassifier = "SNAPSHOT";
		i.execute();
		Platform.exit();
	}

}
