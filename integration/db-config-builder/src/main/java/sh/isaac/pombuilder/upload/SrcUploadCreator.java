/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package sh.isaac.pombuilder.upload;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import sh.isaac.api.util.DeployFile;
import sh.isaac.api.util.WorkExecutors;
import sh.isaac.api.util.Zip;
import sh.isaac.pombuilder.FileUtil;
import sh.isaac.pombuilder.GitPublish;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.provider.sync.git.gitblit.GitBlitUtils;

//~--- classes ----------------------------------------------------------------

/**
 * {@link SrcUploadCreator}
 * Create a new maven pom project which when executed, will upload a set of SDO input files.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SrcUploadCreator
{
	/** The Constant LOG. */
	private static final Logger LOG = LogManager.getLogger();
	
	public static final String SRC_UPLOAD_GROUP = "sh.isaac.terminology.source";
	public static final String WORKING_SUB_FOLDER_NAME = "src-upload";

	// ~--- methods -------------------------------------------------------------

	// TODO notes for jruby
	// Added the following parameters, workingFolder, moveSourceFiles, deleteAfterPublish

	/**
	 * Creates the src upload configuration.  Operates in 4 stages, depending on the amount of parameters provided.
	 * Stage one just creates a maven project.
	 * Stage two uploads the maven project to git as a tag.
	 * Stage three zips the provided content
	 * Stage four uploads the artifacts to the provided server
	 *
	 * @param uploadType - What type of content is being uploaded.
	 * @param version - What version number does the passed in content represent
	 * @param extensionName - optional - If the upload type is a type such as {@link SupportedConverterTypes#SCT_EXTENSION} which contains a
	 *            wildcard '*' in its {@link SupportedConverterTypes#getArtifactId()} value, this parameter must be provided, and is the string to use
	 *            to  replace the wildcard. This would typically be a value such as "en" or "fr", when used for snomed extension content.
	 * @param filesToUpload - optional the files to upload. Required for publish, optional if you only want the maven skeleton project.
	 * @param gitRepositoryURL - - optional - The URL to publish this built project to. If the URL is not provided, no git publishing /tagging will be
	 *            attempted.
	 * @param gitUsername - - optional - The username to utilize to publish this project
	 * @param gitPassword - optional - the git password
	 * @param artifactRepositoryURL - optional - The artifact server path where the created artifact should be transferred. This path should go all
	 *            the way down to a specific repository, such as http://artifactory.isaac.sh/artifactory/libs-release-local or
	 *            http://artifactory.isaac.sh/artifactory/termdata-release-local
	 *            This should not point to a URL that represents a 'group' repository view. If not provided, no artifact publishing will be attempted.
	 * @param repositoryUsername - optional -The username to utilize to upload the artifact to the artifact server
	 * @param repositoryPassword - optional - The password to utilize to upload the artifact to the artifact server
	 * @param workingFolder - optional - if provided, the working files are created inside this folder. If not provided, uses a system temp folder.
	 * @param moveSourceFiles - default is true meaning the files from filesToUpload will be moved into the baseFolder.
	 *            specify false to copy instead of move the files from filesToUpload.
	 * @param deleteAfterPublish - true if any content created inside of workingFolder should be deleted after a successful publish.
	 * @return - the task handle - which will return the tag that was created in the git repository upon completion. Note that the task is NOT yet
	 *         started, when it is returned.
	 * @throws Throwable the throwable
	 */
	public static Task<String> createSrcUploadConfiguration(SupportedConverterTypes uploadType, String version, String extensionName, List<File> filesToUpload,
			String gitRepositoryURL, String gitUsername, char[] gitPassword, String artifactRepositoryURL, String repositoryUsername, String repositoryPassword,
			File workingFolder, boolean moveSourceFiles, boolean deleteAfterPublish) throws Throwable
	{
		LOG.info(
				"Building the task to create a source upload configuration for {}, version: {}, extensionName: {}, to git: {} and artifact server: {} with"
						+ " a base folder of {}, moveSourceFiles: {}, deleteAfterPublish {}",
				uploadType, version, extensionName, gitRepositoryURL, artifactRepositoryURL, workingFolder, moveSourceFiles, deleteAfterPublish);

		if (LOG.isDebugEnabled() && (filesToUpload != null))
		{
			LOG.debug("Provided files []", Arrays.toString(filesToUpload.toArray(new File[filesToUpload.size()])));
		}

		if (StringUtils.isNotBlank(artifactRepositoryURL) && ((filesToUpload == null) || (filesToUpload.size() == 0)))
		{
			LOG.info("Throwing an exception because No content was found to upload");
			throw new Exception("No content was found to upload!");
		}

		final Task<String> uploader = new Task<String>()
		{
			@Override
			protected String call() throws Exception
			{
				updateMessage("Preparing");

				File baseFolder = null;
				boolean gitLockHeld = false;

				try
				{
					if (workingFolder != null)
					{
						baseFolder = new File(workingFolder, WORKING_SUB_FOLDER_NAME);
						FileUtil.recursiveDelete(baseFolder);
					}
					else
					{
						baseFolder = Files.createTempDirectory(WORKING_SUB_FOLDER_NAME).toFile();
					}
					baseFolder.mkdirs();

					final File nativeSource = new File(baseFolder, "native-source");

					if (nativeSource.exists())
					{
						LOG.info("Task failing due to unexpected file in upload content '{}'", nativeSource);
						throw new RuntimeException("Unexpected file found in upload content!");
					}

					nativeSource.mkdir();

					if (filesToUpload != null)
					{
						for (final File f : filesToUpload)
						{
							// validate it is a file, move it into native-source
							if (f.isFile())
							{
								if (moveSourceFiles)
								{
									Files.move(f.toPath(), nativeSource.toPath().resolve(f.toPath().getFileName()));
								}
								else
								{
									Files.copy(f.toPath(), nativeSource.toPath().resolve(f.toPath().getFileName()));
								}
	
							}
							else
							{
								LOG.info("Task failing due to unexpected directory in upload content: '{}'", f.getAbsolutePath());
								throw new Exception("Unexpected directory found in upload content!  " + f.getAbsolutePath());
							}
						}
					}

					final StringBuffer noticeAppend = new StringBuffer();
					final HashMap<String, String> pomSwaps = new HashMap<>();

					pomSwaps.put("#VERSION#", version);
					pomSwaps.put("#SCM_URL#", StringUtils.isNotBlank(gitRepositoryURL) ? GitBlitUtils.constructChangesetRepositoryURL(gitRepositoryURL) : "");

					if (uploadType.getArtifactId().contains("*") && StringUtils.isBlank(extensionName))
					{
						throw new Exception("ExtensionName is required when the upload type artifact id contains a wildcard");
					}

					pomSwaps.put("#GROUPID#", SRC_UPLOAD_GROUP);

					String temp = uploadType.getArtifactId();

					if (temp.contains("*"))
					{
						temp = temp.replace("*", extensionName.toLowerCase());
					}

					pomSwaps.put("#ARTIFACTID#", temp);
					pomSwaps.put("#NAME#", uploadType.getNiceName() + " Source Upload");
					pomSwaps.put("#LICENSE#", uploadType.getLicenseInformation()[0]);  // we only use the first license for source upload
					noticeAppend.append(uploadType.getNoticeInformation()[0]);  // only use the first notice info

					final String tagWithoutRevNumber = pomSwaps.get("#GROUPID#") + "/" + pomSwaps.get("#ARTIFACTID#") + "/" + pomSwaps.get("#VERSION#");

					LOG.debug("Desired tag (withoutRevNumber): {}", tagWithoutRevNumber);
					String tag = "";

					if (StringUtils.isNotBlank(gitRepositoryURL))
					{
						// Lock over the time period where we are calculating the new tag
						gitLockHeld = true;
						GitPublish.lock(gitRepositoryURL);

						final ArrayList<String> existingTags = GitPublish.readTags(gitRepositoryURL, gitUsername, gitPassword);

						if (LOG.isDebugEnabled())
						{
							LOG.debug("Currently Existing tags in '{}': {} ", gitRepositoryURL,
									Arrays.toString(existingTags.toArray(new String[existingTags.size()])));
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

						LOG.info("Final calculated tag: '{}'", tag);
					}

					pomSwaps.put("#SCM_TAG#", tag);
					FileUtil.writeFile("shared", "LICENSE.txt", baseFolder);
					FileUtil.writeFile("shared", "NOTICE.txt", baseFolder, null, noticeAppend.toString());
					FileUtil.writeFile("srcUploadProjectTemplate", "native-source/DOTgitignore", baseFolder);
					FileUtil.writeFile("srcUploadProjectTemplate", "assembly.xml", baseFolder);
					FileUtil.writeFile("srcUploadProjectTemplate", "pom.xml", baseFolder, pomSwaps, "");

					if (StringUtils.isNotBlank(gitRepositoryURL))
					{
						updateTitle("Publishing configuration to Git");
						GitPublish.publish(baseFolder, gitRepositoryURL, gitUsername, gitPassword, tag);
						GitPublish.unlock(gitRepositoryURL);
						gitLockHeld = false;
					}

					if (filesToUpload != null && filesToUpload.size() > 0)
					{
						updateTitle("Zipping content");
						LOG.debug("Zipping content");

						final Zip z = new Zip(pomSwaps.get("#ARTIFACTID#"), pomSwaps.get("#VERSION#"), null, null, new File(baseFolder, "target"), nativeSource,
								false);
						final ArrayList<File> toZip = new ArrayList<>();

						for (final File f : nativeSource.listFiles())
						{
							if (f.getName().equals(".gitignore"))
							{
								// noop
							}
							else
							{
								toZip.add(f);
							}
						}

						z.getStatus().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> updateMessage(newValue));
						z.getTotalWork().add(z.getWorkComplete()).addListener(
								(ChangeListener<Number>) (observable, oldValue, newValue) -> updateProgress(z.getWorkComplete().get(), z.getTotalWork().get()));

						// This blocks till complete
						final File zipFile = z.addFiles(toZip);

						if (StringUtils.isNotBlank(artifactRepositoryURL))
						{
							LOG.info("Zip complete, publishing to artifact repo {}", artifactRepositoryURL);
							updateTitle("Publishing files to the Artifact Repository");

							final DeployFile pom = new DeployFile(pomSwaps.get("#GROUPID#"), pomSwaps.get("#ARTIFACTID#"), pomSwaps.get("#VERSION#"), null, "pom",
									new File(baseFolder, "pom.xml"), artifactRepositoryURL, repositoryUsername, repositoryPassword);

							pom.messageProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> updateMessage(newValue));
							WorkExecutors.get().getExecutor().execute(pom);

							final DeployFile data = new DeployFile(pomSwaps.get("#GROUPID#"), pomSwaps.get("#ARTIFACTID#"), pomSwaps.get("#VERSION#"), null, "zip",
									zipFile, artifactRepositoryURL, repositoryUsername, repositoryPassword);

							pom.progressProperty().addListener(
									(ChangeListener<Number>) (observable, oldValue, newValue) -> updateProgress(pom.getWorkDone(), pom.getTotalWork()));
							pom.messageProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> updateMessage(newValue));
							WorkExecutors.get().getExecutor().execute(data);

							// block till uploads complete
							pom.get();
							data.get();
						}
					}

					if (deleteAfterPublish)
					{
						updateTitle("Cleaning Up");

						try
						{
							FileUtil.recursiveDelete(baseFolder);
						}
						catch (final Exception e)
						{
							LOG.error("Problem cleaning up temp folder " + baseFolder, e);
						}
					}

					updateTitle("Complete");
					return tag;
				}
				catch (final Throwable e)
				{
					LOG.error("Unexpected error", e);
					throw new RuntimeException(e);
				}
				finally
				{
					try
					{
						if (gitLockHeld)
						{
							GitPublish.unlock(gitRepositoryURL);
						}
					}
					catch (final Exception e)
					{}
				}
			}
		};

		return uploader;
	}
}
