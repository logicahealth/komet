package org.ihtsdo.otf.mojo.properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import gov.vha.isaac.lookup.constants.Constants;

/**
 * Created by kec on 9/13/14.
 */

@Mojo( name = "set-termstore-properties")
public class SetTermstoreProperties extends AbstractMojo {

    @Parameter
    String termstoreRootLocation;

    @Parameter
    String searchRootLocation;

    @Override
    public void execute()
            throws MojoExecutionException {

        if (termstoreRootLocation != null) {
            System.setProperty(Constants.CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY,
                    termstoreRootLocation);
        }
        if (searchRootLocation != null) {
            System.setProperty(Constants.SEARCH_ROOT_LOCATION_PROPERTY,
                    searchRootLocation);
        }
    }
}
