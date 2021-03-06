package sh.isaac.convert.mojo.icd10;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import javafx.application.Platform;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         Just a runner class in the test package, so that eclipse launches with a classpath that includes test libraries, which makes
 *         logging work to the console.
 */
public class ICD10MojoRunner extends ICD10ImportMojoDirect
{
	public static void main(String[] args) throws MojoExecutionException
	{
		ICD10MojoRunner i = new ICD10MojoRunner();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocationPath = new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src").toPath();
		i.sourceType = "CM";
		i.converterVersion = "SNAPSHOT";
		i.converterOutputArtifactVersion = "0.2";
		i.converterOutputArtifactClassifier = "foo";
		i.converterSourceArtifactVersion = "2018";
		i.execute();
		Platform.exit();
	}
}
