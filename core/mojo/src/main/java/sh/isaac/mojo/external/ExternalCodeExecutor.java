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

import java.lang.reflect.Field;

import java.util.Iterator;
import java.util.Map;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import sh.isaac.api.LookupService;
import sh.isaac.api.util.FortifyFun;

//~--- classes ----------------------------------------------------------------

/**
 * Goal which executes mojo-like things that can't be run as mojos directly, due to class loading issues in maven.
 *
 * Basically, the short of it, is that a DB lifecycle can't span multiple plugins in maven. because of classloader
 * isolation among the plugins.  This class allows us to run any code, even code not in this plugin module - so
 * long as it extends the {@link QuasiMojo} class.
 *
 * @see QuasiMojo
 */
@Mojo(
   defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
   name         = "quasi-mojo-executor"
)
public class ExternalCodeExecutor
        extends AbstractMojo {
   /** The skip execution. */
   @Parameter(
      required                     = false,
      defaultValue                 = "false"
   )
   protected boolean skipExecution = false;;

   /** The project version. */
   @Parameter(
      required     = true,
      defaultValue = "${project.version}"
   )
   protected String projectVersion;

   /** The output directory. */
   @Parameter(
      required     = true,
      defaultValue = "${project.build.directory}"
   )
   protected File outputDirectory;

   /** The quasi mojo name. */
   @Parameter(required = true)
   protected String quasiMojoName;

   /** The parameters. */
   @Parameter(required = false)
   protected Map<String, String> parameters;

   //~--- methods -------------------------------------------------------------

   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   @Override
   public void execute()
            throws MojoExecutionException {
      try {
         if (this.skipExecution) {
            getLog().info("Skipping execution of " + this.quasiMojoName);
            return;
         } else {
            getLog().info("Executing " + this.quasiMojoName);
         }

         final long      start     = System.currentTimeMillis();
         final QuasiMojo quasiMojo = LookupService.getService(QuasiMojo.class, this.quasiMojoName);

         if (quasiMojo == null) {
            throw new MojoExecutionException("Could not locate a QuasiMojo implementation with the name '" +
                                             this.quasiMojoName + "'.");
         }

         quasiMojo.outputDirectory = this.outputDirectory;
         quasiMojo.projectVersion  = this.projectVersion;
         quasiMojo.log_            = getLog();

         if ((this.parameters != null) && (this.parameters.size() > 0)) {
            final Class<?>         myClass = quasiMojo.getClass();
            final Iterator<String> params  = this.parameters.keySet()
                                                            .iterator();

            while (params.hasNext()) {
               final String name  = params.next();
               final String value = this.parameters.get(name);

               params.remove();

               Field myField = null;

               try {
                  myField = myClass.getDeclaredField(name);
               } catch (final NoSuchFieldException e) {
                  // recurse up the parent classes, looking for the field
                  Class<?> parent = myClass;

                  while ((myField == null) && (parent.getSuperclass() != null)) {
                     parent = parent.getSuperclass();

                     try {
                        myField = parent.getDeclaredField(name);
                     } catch (final NoSuchFieldException e1) {
                        // ignore
                     }
                  }
               }

               if (myField == null) {
                  throw new MojoExecutionException("No field in " + quasiMojo + " to place the parameter " + name +
                                                   " : " + value);
               }

               FortifyFun.fixAccessible(myField);  // myField.setAccessible(true);

               if (myField.getType()
                          .equals(String.class)) {
                  myField.set(quasiMojo, value);
               } else if (myField.getType()
                                 .equals(File.class)) {
                  myField.set(quasiMojo, new File(value));
               } else if (myField.getType()
                                 .equals(Integer.class)) {
                  myField.set(quasiMojo, Integer.parseInt(value));
               } else if (myField.getType()
                                 .equals(Long.class)) {
                  myField.set(quasiMojo, Long.parseLong(value));
               } else if (myField.getType()
                                 .equals(Boolean.class)) {
                  myField.set(quasiMojo, Boolean.parseBoolean(value));
               } else {
                  throw new MojoExecutionException("Can't handle field datatype " + myField.getType());
               }
            }

            if (this.parameters.size() > 0) {
               for (final String s: this.parameters.keySet()) {
                  getLog().warn("Mojo specified a parameter '" + s +
                                "' that couldn't be placed into the execution class!");
               }
            }
         }

         quasiMojo.execute();
         getLog().info(this.quasiMojoName + " execution completed in " + (System.currentTimeMillis() - start) + "ms");
      } catch (final Exception e) {
         throw new MojoExecutionException("QuasiMojo Execution Failure", e);
      }
   }
}

