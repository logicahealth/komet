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



package sh.isaac.mojo.external;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import org.jvnet.hk2.annotations.Contract;

//~--- classes ----------------------------------------------------------------

/**
 * {@link QuasiMojo}
 *
 * Implement this class, and add a '@Service(name = "your-execution-name")' annotation to the code.
 *
 * Your code can then be executed with a configuration like this:
 *
 *      <plugin>
 *              <groupId>sh.isaac.modules</groupId>
 *              <artifactId>mojo</artifactId>
 *              <dependencies>
 *                      <dependency>
 *                              <groupId>group.that.contains.your.code</groupId>
 *                              <artifactId>artifact.that.contains.your.code</artifactId>
 *                              <version>${your.code.version}</version>
 *                      </dependency>
 *              </dependencies>
 *              <executions>
 *                      <execution>
 *                      <id>your-id</id>
 *                      <goals>
 *                              <goal>quasi-mojo-executor</goal>
 *                      </goals>
 *                      <configuration>
 *                              <quasiMojoName>your-execution-name</quasiMojoName>  <!--must match @Service annotation name -->
 *                              <parameters>
 *                                      <loincFileLocation>${project.build.directory}/generated-resources/loinc</loincFileLocation>
 *                                      <loincTPFileLocation>${project.build.directory}/generated-resources/loincTP</loincTPFileLocation>
 *                                      <loaderVersion>${loader.version}</loaderVersion>
 *                              </parameters>
 *                      </configuration>
 *                      </execution>
 *              </executions>
 *      </plugin>
 *
 * Configuration to your class is passed via the <parameters> section.  Each item within the parameters _must_ have a matching
 * parameter name in your implementation class.  Basic types (String, File, Integer, Boolean, etc) are handled automatically.
 *
 * This allows your code to be executed within the same classloader which does DB / ISAAC things - (other executions
 * within the isaac-mojo package) but lets us not worry about dependency chain issues, as your class is found at runtime
 * via HK2 injection.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public abstract class QuasiMojo {
   /** The log. */
   protected Log log_ = null;

   /** The project version. */
   protected String projectVersion;

   /** The output directory. */
   protected File outputDirectory;

   //~--- methods -------------------------------------------------------------

   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   public abstract void execute()
            throws MojoExecutionException;

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the log.
    *
    * @return the log
    */
   public Log getLog() {
      return this.log_;
   }
}

