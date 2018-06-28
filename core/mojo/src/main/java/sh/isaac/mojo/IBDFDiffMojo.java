/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.mojo;

import java.io.File;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.convert.differ.IBDFDiffTool;

/**
 * A mojo execution wrapper for the IBDF Diff computer
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Mojo(name = "diff-ibdf", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class IBDFDiffMojo extends AbstractMojo
{
	/**
	 * Location to write the output file.
	 */
	@Parameter(required = true, defaultValue = "${project.build.directory}")
	private File outputDirectory;

	/**
	 * See {@link IBDFDiffTool#init(File, File, File, UUID, long, boolean, boolean, boolean)}
	 */
	@Parameter(required = true)
	private File initialStateIBDFFolder;

	/**
	 * See {@link IBDFDiffTool#init(File, File, File, UUID, long, boolean, boolean, boolean)}
	 */
	@Parameter(required = true)
	private File endStateIBDFFolder;

	/**
	 * See {@link IBDFDiffTool#init(File, File, File, UUID, long, boolean, boolean, boolean)}
	 */
	@Parameter(required = true)
	private String author;

	/**
	 * See {@link IBDFDiffTool#init(File, File, File, UUID, long, boolean, boolean, boolean)}
	 */
	@Parameter(required = true)
	private String time;

	/**
	 * See {@link IBDFDiffTool#init(File, File, File, UUID, long, boolean, boolean, boolean)}
	 */
	@Parameter(required = true)
	private boolean ignoreTimeInCompare;

	/**
	 * See {@link IBDFDiffTool#init(File, File, File, UUID, long, boolean, boolean, boolean)}
	 */
	@Parameter(required = true)
	private boolean ignoreSiblingModules;

	/**
	 * See {@link IBDFDiffTool#init(File, File, File, UUID, long, boolean, boolean, boolean)}
	 */
	@Parameter(required = true)
	private boolean generateRetiresForMissingModuleMetadata;
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		try
		{
			UUID authorUUID = UUID.fromString(author);
			long timeLong = Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).parse(time)).toEpochMilli();
			IBDFDiffTool idt = IBDFDiffTool.getInstance(outputDirectory, findIbdf(initialStateIBDFFolder), findIbdf(endStateIBDFFolder), authorUUID, timeLong, 
					ignoreTimeInCompare, ignoreSiblingModules, generateRetiresForMissingModuleMetadata);
			
			LookupService.startupWorkExecutors();
			
			Get.workExecutors().getExecutor().submit(idt).get();
			LookupService.shutdownIsaac();
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Failure", e);
		}
	}
	
	private File findIbdf(File inputFolder) throws Exception
	{
		if (!inputFolder.isDirectory())
		{
			throw new Exception("The path " + inputFolder.getAbsolutePath() + " is not a folder");
		}
		for (File f : inputFolder.listFiles())
		{
			if (f.isFile() && f.getName().toLowerCase().endsWith(".ibdf"))
			{
				return f;
			}
		}
		throw new Exception("IBDF file not located in " + inputFolder.getAbsolutePath());
	}
}
