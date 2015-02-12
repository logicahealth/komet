package org.ihtsdo.otf.mojo.hk2;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * Created by kec on 9/6/14.
 */
@Mojo( name = "set-run-level")
public class SetRunLevel extends AbstractMojo {

    @Parameter(required = true)
    String runLevel;

    @Override
    public void execute()
            throws MojoExecutionException {
        Hk2Looker.getRunLevelController().proceedTo(Integer.valueOf(runLevel));
    }
}
