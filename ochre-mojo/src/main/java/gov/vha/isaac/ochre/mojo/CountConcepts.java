package gov.vha.isaac.ochre.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import gov.vha.isaac.ochre.api.Get;

/**
 * Created by kec on 9/6/14.
 */
@Mojo(name = "count-concepts", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class CountConcepts extends AbstractMojo
{

	public void execute() throws MojoExecutionException
	{
		getLog().info("Concept count: " + Get.conceptService().getConceptCount());
	}
}
