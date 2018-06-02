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
package sh.isaac.pombuilder.diff;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.convert.differ.IBDFDiffTool;
import sh.isaac.dbConfigBuilder.artifacts.IBDFFile;
import sh.isaac.pombuilder.FileUtil;
import sh.isaac.pombuilder.GitPublish;
import sh.isaac.provider.sync.git.gitblit.GitBlitUtils;

/**
 * Creates a POM project, which, when executed, will calculate a Delta IBDF file between the two input files.
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DiffExecutionCreator
{
	private static final Logger LOG = LogManager.getLogger();
	public static final String IBDF_OUTPUT_GROUP = "sh.isaac.terminology.diffs";
	public static final String WORKING_SUBFOLDER = "diff-executor";
	
	/**
	 * Creates a POM project, which, when executed, will calculate a Delta IBDF file between the two input files.
	 * 
	 * See {@link IBDFDiffTool#init(File, File, File, UUID, long, boolean, boolean, boolean)} for more details on the parameters.
	 * 
	 * @param calculatorVersion - Which version of the Diff tool should be used.
	 * @param initialFile - The starting point, typically the lower version number.
	 * @param endFile - The end point, typically the higher version number
	 * @param outputVersion - The version number to assign the resulting IBDF delta file.
	 * @param author - The author to use for any component retirements
	 * @param time - The time to use for any component retirements
	 * @param ignoreTimeInCompare - in certain cases, the source content doesn't provide a time, so the IBDF converter invents a time.
	 *            In these cases, we don't want to compare on time. This is only allowed when the incoming file only has one version per chronology.
	 *            Also, metadata is usually generated with the terminology change time, which is usually different on each import.
	 * @param ignoreSiblingModules - When processing version 8 of something, against version 7, typically, the module is specified
	 *            as a version-specific module - 7 or 8. But both 7 and 8 will share an unversioned 'parent' module. If true, we will ignore
	 *            module differences, as long as the parent module of each module is the same. If false, the module must be identical.
	 * @param generateRetiresForMissingModuleMetadata - if true, we treat the version specific module concept as if it were any other concept,
	 *            and generate retirements for it, if it isn't present in the new IBDF file (which it usually wouldn't be). If false, we won't
	 *            generate retire diffs for any sibling module concepts, or their attached semantics.
	 * @param gitRepositoryURL - optional - The URL to publish this built project to. If not provided, the project is not published.
	 * @param gitUsername - The username to utilize to publish this project
	 * @param gitPassword - the password to utilize to publish this project
	 * @param workingFolder - optional - if provided, the working files are created inside this folder. If not provided, uses a system temp folder.
	 * @param deleteAfterPublish - true if the content created inside of workingFolder should be deleted after a successful publish.
	 * @return the tag created in the repository that carries the created project
	 * @throws Exception
	 */
	public static String createDiffExecutor(String calculatorVersion, IBDFFile initialFile, IBDFFile endFile, String outputVersion,
			UUID author, long time, boolean ignoreTimeInCompare,boolean ignoreSiblingModules, boolean generateRetiresForMissingModuleMetadata,
			String gitRepositoryURL, String gitUsername,char[] gitPassword, File workingFolder, boolean deleteAfterPublish) throws Exception
	{
		LOG.debug("Creating a diff execution for '{}' with converter version '{}' on the server '{}'", initialFile, calculatorVersion, gitRepositoryURL);

		File baseFolder;
		if (workingFolder != null)
		{
			baseFolder = new File(workingFolder, WORKING_SUBFOLDER);
			FileUtil.recursiveDelete(baseFolder);
		}
		else
		{
			baseFolder = Files.createTempDirectory(WORKING_SUBFOLDER).toFile();
		}
		baseFolder.mkdirs();
		boolean haveLock = false;
		try
		{
			FileUtil.writeFile("converterProjectTemplate", "src/assembly/MANIFEST.MF", baseFolder, new HashMap<>(), "");
			FileUtil.writeFile("shared", "LICENSE.txt", baseFolder, new HashMap<>(), "");
			final HashMap<String, String> pomSwaps = new HashMap<>();
			
			pomSwaps.put("#GROUP_ID#", IBDF_OUTPUT_GROUP);
			pomSwaps.put("#ARTIFACTID#", initialFile.getArtifactId() + "-" + initialFile.getVersion() + "--" +  endFile.getVersion() + "-delta");
			pomSwaps.put("#VERSION#", outputVersion);
			pomSwaps.put("#NAME#", "Delta calculator for " + initialFile.getVersion() + " and " + endFile.getVersion() + " of " + initialFile.getArtifactId());
			pomSwaps.put("#LOADER_VERSION#", calculatorVersion);
			pomSwaps.put("#SCM_URL#", StringUtils.isNotBlank(gitRepositoryURL) ? GitBlitUtils.constructChangesetRepositoryURL(gitRepositoryURL) : "");

			pomSwaps.put("#START_STATE_IBDF#", initialFile.getArtifactId());
			pomSwaps.put("#START_STATE_IBDF_VERSION#", initialFile.getVersion());
			pomSwaps.put("#START_STATE_CLASSIFIER#", StringUtils.isNotBlank(initialFile.getClassifier()) ? "<classifier>" + initialFile.getClassifier() + "</classifier>" 
					: "");
			
			pomSwaps.put("#END_STATE_IBDF#", endFile.getArtifactId());
			pomSwaps.put("#END_STATE_IBDF_VERSION#", endFile.getVersion());
			pomSwaps.put("#END_STATE_CLASSIFIER#", StringUtils.isNotBlank(endFile.getClassifier()) ? "<classifier>" + endFile.getClassifier() + "</classifier>" 
					: "");
			
			pomSwaps.put("#AUTHOR#", author.toString());
			pomSwaps.put("#TIME#", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(Instant.ofEpochMilli(time)));
			pomSwaps.put("#IGNORE_TIME#", ignoreTimeInCompare + "");
			pomSwaps.put("#IGNORE_SIBLING#", ignoreSiblingModules + "");
			pomSwaps.put("#GENERATE_RETIRES_FOR_MISSING_MODULES#", generateRetiresForMissingModuleMetadata + "");
			
			
			final String tagWithoutRevNumber = IBDF_OUTPUT_GROUP + "/" + pomSwaps.get("#ARTIFACTID#") + "/" + pomSwaps.get("#VERSION#");

			LOG.debug("Generated tag (without rev number): '{}'", tagWithoutRevNumber);

			String tag = tagWithoutRevNumber;
			if (StringUtils.isNotBlank(gitRepositoryURL))
			{
				// Lock over the duration where we are determining what tag to use.
				haveLock = true;
				GitPublish.lock(gitRepositoryURL);

				final ArrayList<String> existingTags = GitPublish.readTags(gitRepositoryURL, gitUsername, gitPassword);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Currently Existing tags in '{}': {} ", gitRepositoryURL, Arrays.toString(existingTags.toArray(new String[existingTags.size()])));
				}

				final int highestBuildRevision = GitPublish.readHighestRevisionNumber(existingTags, tagWithoutRevNumber);

				// Fix version number
				if (highestBuildRevision == -1)
				{
					// No tag at all - create without rev number, don't need to change our pomSwaps
					tag = tagWithoutRevNumber;
				}
				else
				{
					// If we are a SNAPSHOT, don't embed a build number, because nexus won't allow the upload, otherwise, embed a rev number
					if (!pomSwaps.get("#VERSION#").endsWith("SNAPSHOT"))
					{
						pomSwaps.put("#VERSION#", pomSwaps.get("#VERSION#") + "-" + (highestBuildRevision + 1));
					}

					tag = tagWithoutRevNumber + "-" + (highestBuildRevision + 1);
				}

				GitPublish.unlock(gitRepositoryURL);
				haveLock = false;

				LOG.info("Final calculated tag: '{}'", tag);
			}
			pomSwaps.put("#SCM_TAG#", tag);
			
			FileUtil.writeFile("shared", "NOTICE.txt", baseFolder, new HashMap<>(), "");
			FileUtil.writeFile("diffProjectTemplate", "pom.xml", baseFolder, pomSwaps, "");

			if (StringUtils.isNotBlank(gitRepositoryURL))
			{
				GitPublish.publish(baseFolder, gitRepositoryURL, gitUsername, gitPassword, tag);
				if (deleteAfterPublish)
				{
					FileUtil.recursiveDelete(baseFolder);
				}
			}
			return tag;
			
		}
		finally
		{
			try
			{
				if (haveLock)
				{
					GitPublish.unlock(gitRepositoryURL);
				}
			}
			catch (final Exception e)
			{
				LOG.error("Problem cleaning up temp folder " + baseFolder, e);
			}
		}
	}
}
