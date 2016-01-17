package gov.vha.isaac.ochre.mojo.ticker;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import gov.vha.isaac.ochre.api.memory.HeapUseTicker;

/**
 * Created by kec on 4/9/15.
 */
@Mojo(name = "stop-heap-ticker", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)

public class StopHeapTicker extends AbstractMojo
{

	@Override
	public void execute() throws MojoExecutionException
	{
		HeapUseTicker.stop();
	}
}
