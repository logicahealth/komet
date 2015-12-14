/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.mojo;


import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.LookupService;
import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which opens (and creates if necessary) a Data Store.
 *
 * Note that this duplicates functionality found in
 * {@link SetTermstoreProperties} and {@link StartupIsaac}, but we can't use
 * that in combination with other things in the package, due to limitations of
 * maven when it comes to interweaving operations from multiple mojo plugins.
 */
@Mojo(defaultPhase = LifecyclePhase.PROCESS_SOURCES, name = "setup-isaac")
public class Setup extends AbstractMojo {

    /**
     * See {@link ConfigurationService#setDataStoreFolderPath(java.nio.file.Path) for details on what should
     * be in the passed in folder location.
     *
     * @parameter
     * @required
     */
    @Parameter(required = true)
    private File dataStoreLocation;

    /**
     * Location of the folder that contains the user profiles
     */
    @Parameter(required = false)
    private File userProfileFolderLocation;

    /**
     * @throws org.apache.maven.plugin.MojoExecutionException
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Setup terminology store");
        try {

            LookupService.getService(ConfigurationService.class).setDataStoreFolderPath(dataStoreLocation.toPath());
            getLog().info("  Setup AppContext, data store location = " + dataStoreLocation.getCanonicalPath());

            LookupService.startupIsaac();

            getLog().info("Done setting up ISAAC");
        } catch (IllegalStateException | IllegalArgumentException | IOException e) {
            throw new MojoExecutionException("Database build failure", e);
        }
    }

    public void setDataStoreLocation(File inputBdbFolderlocation) {
        dataStoreLocation = inputBdbFolderlocation;
    }

    public void setUserProfileFolderLocation(File inputUserProfileLocation) {
        userProfileFolderLocation = inputUserProfileLocation;
    }

}
