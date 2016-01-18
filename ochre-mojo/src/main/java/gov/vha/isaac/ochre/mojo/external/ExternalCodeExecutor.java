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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.mojo.external;

import gov.vha.isaac.ochre.api.LookupService;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which executes mojo-like things that can't be run as mojos directly, due to class loading issues in maven.
 * 
 * Basically, the short of it, is that a DB lifecycle can't span multiple plugins in maven. because of classloader
 * isolation among the plugins.  This class allows us to run any code, even code not in this plugin module - so 
 * long as it extends the {@link QuasiMojo} class.
 * 
 * @see QuasiMojo
 */
@Mojo(defaultPhase = LifecyclePhase.PROCESS_RESOURCES, name = "quasi-mojo-executor")
public class ExternalCodeExecutor extends AbstractMojo
{
	@Parameter(required = true, defaultValue = "${project.version}") 
	protected String projectVersion;

	@Parameter(required = true, defaultValue = "${project.build.directory}") 
	protected File outputDirectory;

	@Parameter(required = true) 
	protected String quasiMojoName;
	
	@Parameter(required = false, defaultValue = "false") 
	protected boolean skipExecution = false;;

	@Parameter(required = false) 
	protected Map<String, String> parameters;

	/**
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			if (skipExecution)
			{
				getLog().info("Skipping execution of " + quasiMojoName);
				return;
			}
			else
			{
				getLog().info("Executing " + quasiMojoName);
			}
			long start = System.currentTimeMillis();
			QuasiMojo quasiMojo = LookupService.getService(QuasiMojo.class, quasiMojoName);
			
			if (quasiMojo == null)
			{
				throw new MojoExecutionException("Could not locate a QuasiMojo implementation with the name '" + quasiMojoName + "'.");
			}
			
			quasiMojo.outputDirectory = outputDirectory;
			quasiMojo.projectVersion = projectVersion;
			quasiMojo.log_ = getLog();
			
			if (parameters != null && parameters.size() > 0)
			{
				Class<?> myClass = quasiMojo.getClass();
				Iterator<String> params = parameters.keySet().iterator();
				while (params.hasNext())
				{
					String name = params.next();
					String value = parameters.get(name);
					params.remove();

					Field myField = null;
					try
					{
						myField = myClass.getDeclaredField(name);
					}
					catch (NoSuchFieldException e)
					{
						//recurse up the parent classes, looking for the field
						Class<?> parent = myClass;
						while (myField == null && parent.getSuperclass() != null)
						{
							parent = parent.getSuperclass();
							try
							{
								myField = parent.getDeclaredField(name);
							}
							catch (NoSuchFieldException e1)
							{
								// ignore
							}
						}
					}
					if (myField == null)
					{
						throw new MojoExecutionException("No field in " + quasiMojo + " to place the parameter " + name + " : " + value);
					}
					myField.setAccessible(true);

					if (myField.getType().equals(String.class))
					{
						myField.set(quasiMojo, value);
					}
					else if (myField.getType().equals(File.class))
					{
						myField.set(quasiMojo, new File(value));
					}
					else if (myField.getType().equals(Integer.class))
					{
						myField.set(quasiMojo, Integer.parseInt(value));
					}
					else if (myField.getType().equals(Long.class))
					{
						myField.set(quasiMojo, Long.parseLong(value));
					}
					else if (myField.getType().equals(Boolean.class))
					{
						myField.set(quasiMojo, Boolean.parseBoolean(value));
					}
					else
					{
						throw new MojoExecutionException("Can't handle field datatype " + myField.getType());
					}
						
				}
				
				if (parameters.size() > 0)
				{
					for (String s : parameters.keySet())
					{
						getLog().warn("Mojo specified a parameter '" + s + "' that couldn't be placed into the execution class!");
					}
				}
			}
			
			quasiMojo.execute();

			getLog().info(quasiMojoName + " execution completed in " + (System.currentTimeMillis() - start) + "ms");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("QuasiMojo Execution Failure", e);
		}
	}
}