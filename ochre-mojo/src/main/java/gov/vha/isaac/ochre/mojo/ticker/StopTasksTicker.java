package gov.vha.isaac.ochre.mojo.ticker;

import gov.vha.isaac.ochre.api.progress.ActiveTasksTicker;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Created by kec on 4/9/15.
 */
@Mojo(name = "stop-tasks-ticker", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)

public class StopTasksTicker extends AbstractMojo
{

	@Override
	public void execute() throws MojoExecutionException
	{
		ActiveTasksTicker.stop();
	}
}