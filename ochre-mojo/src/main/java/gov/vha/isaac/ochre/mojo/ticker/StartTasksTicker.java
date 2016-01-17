package gov.vha.isaac.ochre.mojo.ticker;

import gov.vha.isaac.ochre.api.progress.ActiveTasksTicker;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Created by kec on 4/9/15.
 */
@Mojo(name = "start-tasks-ticker", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class StartTasksTicker extends AbstractMojo
{

	@Parameter String intervalInSeconds = "10";

	@Override
	public void execute() throws MojoExecutionException
	{
		ActiveTasksTicker.start(Integer.parseInt(intervalInSeconds));
	}

}