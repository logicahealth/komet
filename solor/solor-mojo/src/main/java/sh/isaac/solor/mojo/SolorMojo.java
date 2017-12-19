/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.solor.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import sh.isaac.api.LookupService;
import static sh.isaac.api.constants.Constants.IMPORT_FOLDER_LOCATION;
import sh.isaac.solor.rf2.direct.Rf2DirectImporter;

/**
 *
 * @author kec
 */
@Mojo(
   name         = "solor-import",
   defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)
public class SolorMojo extends AbstractMojo {

      /** Location of the folder that contains the files to import. */
   @Parameter(required = false, defaultValue = "${project.build.directory}/data")
   private String importFolderLocation;


   public SolorMojo() {
   }
   
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
      System.setProperty(IMPORT_FOLDER_LOCATION, importFolderLocation);
      LookupService.startupIsaac();
      Rf2DirectImporter importer = new Rf2DirectImporter();
      importer.run();
      LookupService.shutdownIsaac();
   }
}
