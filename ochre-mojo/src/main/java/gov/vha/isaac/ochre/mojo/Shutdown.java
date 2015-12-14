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

import gov.vha.isaac.ochre.api.LookupService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which shuts down an open data store. Note that this duplicates
 * functionality found in {@link ShutdownIsaac} but we can't use that in
 * combination with other things in this mojo package, due to limitations of
 * maven.
 */
@Mojo(defaultPhase = LifecyclePhase.PROCESS_SOURCES, name = "shutdown-isaac")
public class Shutdown extends AbstractMojo {

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            getLog().info("Shutdown terminology store");
            // ASSUMES setup has run already

            getLog().info("  Shutting Down");

            LookupService.shutdownIsaac();

            getLog().info("Done shutting down terminology store");
        } catch (Exception e) {
            throw new MojoExecutionException("Database build failure", e);
        }
    }
}
