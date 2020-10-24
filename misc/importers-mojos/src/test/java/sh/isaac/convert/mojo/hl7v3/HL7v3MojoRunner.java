package sh.isaac.convert.mojo.hl7v3;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import javafx.application.Platform;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         Just a runner class in the test package, so that eclipse launches with a classpath that includes test libraries, which makes
 *         logging work to the console.
 */
public class HL7v3MojoRunner extends HL7v3ImportMojoDirect
{
	/**
	 * @param args
	 * @throws MojoExecutionException
	 */
	public static void main(String[] args) throws MojoExecutionException
	{
		HL7v3MojoRunner i = new HL7v3MojoRunner();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/contentManager/converter-executor/target/");
		i.inputFileLocationPath = new File("../../integration/db-config-builder-ui/target/contentManager/converter-executor/target/generated-resources/src").toPath();
		i.converterOutputArtifactVersion = "2.47.1-1.0-SNAPSHOT";
		i.converterVersion = "1.0-SNAPSHOT";
		i.converterSourceArtifactVersion = "2.47.1";
		i.execute();
		Platform.exit();
	}
}
