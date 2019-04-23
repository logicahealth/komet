package sh.isaac.convert.mojo.vhat;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import javafx.application.Platform;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         Just a runner class in the test package, so that eclipse launches with a classpath that includes test libraries, which makes
 *         logging work to the console.
 */
public class VHATMojoRunner extends VHATImportMojoDirect
{
	public static void main(String[] args) throws MojoExecutionException
	{
		VHATMojoRunner i = new VHATMojoRunner();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocationPath = new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src").toPath();
		i.converterVersion = "SNAPSHOT";
		i.converterOutputArtifactVersion = "2016.01.07.foo";
		i.converterSourceArtifactVersion = "2016.01.07.bar";
		i.execute();
		Platform.exit();
	}
}
