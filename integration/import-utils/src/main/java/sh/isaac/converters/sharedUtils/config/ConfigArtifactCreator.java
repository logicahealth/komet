/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.converters.sharedUtils.config;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.MojoExecutionException;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.LookupService;
import sh.isaac.mojo.external.QuasiMojo;
import sh.isaac.pombuilder.converter.ConverterOptionParam;

//~--- classes ----------------------------------------------------------------

/**
 * {@link ConfigArtifactCreator}
 * Locate the files on the classpath that document a configuration for a converter, and create the json output
 * artifacts for them.
 *
 * This will find any class on the class path that implements {@link ConfigOptionsDescriptor}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "create-config-artifact")
public class ConfigArtifactCreator
        extends QuasiMojo {
   
   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   @Override
   public void execute()
            throws MojoExecutionException {
      try {
         final List<ConfigOptionsDescriptor> configs = LookupService.get()
                                                              .getAllServices(ConfigOptionsDescriptor.class);

         for (final ConfigOptionsDescriptor c: configs) {
            ConverterOptionParam.serialize(c.getConfigOptions(),
                                           new File(this.outputDirectory,
                                                 c.getName() + "." + ConverterOptionParam.MAVEN_FILE_TYPE));
         }

         getLog().info("Output Config artifact files for " + configs.size() + " entries found on the classpath.");
      } catch (final Exception e) {
         throw new MojoExecutionException("Unexpected error validating the resources folder", e);
      }
   }
}

