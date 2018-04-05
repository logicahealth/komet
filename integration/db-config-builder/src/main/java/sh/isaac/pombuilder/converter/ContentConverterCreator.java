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

package sh.isaac.pombuilder.converter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//~--- non-JDK imports --------------------------------------------------------
import javafx.util.Pair;
import sh.isaac.api.util.UUIDUtil;
import sh.isaac.dbConfigBuilder.artifacts.Converter;
import sh.isaac.dbConfigBuilder.artifacts.IBDFFile;
import sh.isaac.dbConfigBuilder.artifacts.SDOSourceContent;
import sh.isaac.pombuilder.FileUtil;
import sh.isaac.pombuilder.GitPublish;
import sh.isaac.provider.sync.git.gitblit.GitBlitUtils;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link ContentConverterCreator}
 *
 * A class that has the convenience methods that will construct and publish a pom project - which when executed, will
 * convert SDO source content into IBDF content. The convenience methods in this class carry all of the documentation
 * and information necessary to create various conversion types.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ContentConverterCreator
{
	/** The Constant LOG. */
	private static final Logger LOG = LogManager.getLogger();
	public static final String IBDF_OUTPUT_GROUP = "sh.isaac.terminology.converted";
	public static final String WORKING_SUBFOLDER = "converter-executor";
	public static final String CLASSIFIERS_OPTION = "classifiers";

	// ~--- methods -------------------------------------------------------------

	// TODO jruby note - added the workingFolder and deleteAfterPublish params
	/**
	 * Create a source conversion project which is executable via maven.
	 *
	 * @param sourceContent - The artifact information for the content to be converted. The artifact information must follow known naming conventions
	 *            - group id should be sh.isaac.terminology.source. Currently supported artifactIds are 'loinc-src-data',
	 *            'loinc-src-data-tech-preview', 'rf2-src-data-*', 'vhat'
	 * @param converterVersion - The version number of the content converter code to utilize. The jar file for this converter must be available to the
	 *            maven execution environment at the time when the conversion is run.
	 * @param additionalSourceDependencies - Some converters require additional data files to satisfy dependencies. See
	 *            {@link #getSupportedConversions()} for accurate dependencies for any given conversion type.
	 * @param additionalIBDFDependencies - Some converters require additional data files to satisfy dependencies. See
	 *            {@link #getSupportedConversions()}
	 *            for accurate dependencies for any given conversion type.
	 * @param converterOptionValues a map of converter options (fetched via {@link #getConverterOptions(SupportedConverterTypes, String, String, String, String)} to a set
	 *            of values. The values are the items that the user selected / entered. This may be blank, depending on the converter and/or the user
	 *            choices.
	 * @param gitRepositoryURL - optional - The URL to publish this built project to. If not provided, the project is not published.
	 * @param gitUsername - The username to utilize to publish this project
	 * @param gitPassword - the password to utilize to publish this project
	 * @param workingFolder - optional - if provided, the working files are created inside this folder. If not provided, uses a system temp folder.
	 * @param deleteAfterPublish - true if the content created inside of workingFolder should be deleted after a successful publish.
	 * @return the tag created in the repository that carries the created project
	 * @throws Exception the exception
	 */
	public static String createContentConverter(SDOSourceContent sourceContent, String converterVersion, SDOSourceContent[] additionalSourceDependencies,
			IBDFFile[] additionalIBDFDependencies, Map<ConverterOptionParam, Set<String>> converterOptionValues, String gitRepositoryURL, String gitUsername,
			char[] gitPassword, File workingFolder, boolean deleteAfterPublish) throws Exception
	{
		LOG.debug("Creating a content converter for '{}' with converter version '{}' on the server '{}'", sourceContent, converterVersion, gitRepositoryURL);

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
		
		if (sourceContent.getArtifactId().contains("*"))
		{
			throw new Exception("The source content artifact id contains a wildcard that must be replaced before calling this method");
		}

		try
		{
			final Pair<SupportedConverterTypes, String> artifactInfo = SupportedConverterTypes.findConverterTypeAndExtensionBySrcArtifactId(sourceContent.getArtifactId());
			final SupportedConverterTypes conversionType = artifactInfo.getKey();
			final String extensionSuffix = artifactInfo.getValue();
			final StringBuilder extraProperties = new StringBuilder();

			FileUtil.writeFile("converterProjectTemplate", "src/assembly/MANIFEST.MF", baseFolder, new HashMap<>(), "");
			FileUtil.writeFile("shared", "LICENSE.txt", baseFolder, new HashMap<>(), "");

			final StringBuffer noticeAppend = new StringBuffer();
			final HashMap<String, String> pomSwaps = new HashMap<>();

			pomSwaps.put("#VERSION#", sourceContent.getVersion() + "-loader-" + converterVersion);
			pomSwaps.put("#NAME#", conversionType.getNiceName() + " Artifact Converter");
			pomSwaps.put("#SOURCE_DATA_VERSION#", sourceContent.getVersion());
			pomSwaps.put("#LOADER_VERSION#", converterVersion);
			pomSwaps.put("#SCM_URL#", StringUtils.isNotBlank(gitRepositoryURL) ? GitBlitUtils.constructChangesetRepositoryURL(gitRepositoryURL) : "");

			String temp = FileUtil.readFile("converterProjectTemplate/pomSnippits/fetchExecution.xml");

			temp = temp.replace("#GROUPID#", sourceContent.getGroupId());
			temp = temp.replace("#ARTIFACTID#", sourceContent.getArtifactId());
			temp = temp.replace("#VERSION#", sourceContent.getVersion());

			final StringBuilder fetches = new StringBuilder(temp);

			for (final SDOSourceContent ac : additionalSourceDependencies)
			{
				temp = FileUtil.readFile("converterProjectTemplate/pomSnippits/fetchExecution.xml");
				temp = temp.replace("#GROUPID#", ac.getGroupId());
				temp = temp.replace("#ARTIFACTID#", ac.getArtifactId());
				temp = temp.replace("#VERSION#", ac.getVersion());
				fetches.append(temp);
				extraProperties.append("<" + ac.getArtifactId() + ".version>" + ac.getVersion() + "</" + ac.getArtifactId() + ".version>\n");
			}

			pomSwaps.put("#FETCH_EXECUTION#", fetches.toString());

			final StringBuilder dependencies = new StringBuilder();
			final StringBuilder unpackArtifacts = new StringBuilder();
			String unpackDependencies = "";

			if (additionalIBDFDependencies.length > 0)
			{
				unpackDependencies = FileUtil.readFile("converterProjectTemplate/pomSnippits/unpackDependency.xml");

				for (final IBDFFile ibdf : additionalIBDFDependencies)
				{
					temp = FileUtil.readFile("converterProjectTemplate/pomSnippits/ibdfDependency.xml");
					temp = temp.replace("#GROUPID#", ibdf.getGroupId());
					temp = temp.replace("#ARTIFACTID#", ibdf.getArtifactId());
					temp = temp.replace("#CLASSIFIER#", (ibdf.hasClassifier() ? ibdf.getClassifier() : ""));
					temp = temp.replace("#VERSION#", ibdf.getVersion());
					dependencies.append(temp);
					unpackArtifacts.append(ibdf.getArtifactId());
					unpackArtifacts.append(",");
				}

				temp = FileUtil.readFile("converterProjectTemplate/pomSnippits/ibdfDependency.xml");
				temp = temp.replace("#GROUPID#", "sh.isaac.core");
				temp = temp.replace("#ARTIFACTID#", "metadata");
				temp = temp.replace("#CLASSIFIER#", "all");
				temp = temp.replace("#VERSION#", converterVersion);
				dependencies.append(temp);
				unpackArtifacts.append("metadata");
				unpackDependencies = unpackDependencies.replace("#UNPACK_ARTIFACTS#", unpackArtifacts.toString());
			}

			pomSwaps.put("#IBDF_DEPENDENCY#", dependencies.toString());
			pomSwaps.put("#UNPACK_DEPENDENCIES#", unpackDependencies);

			final String goal = conversionType.getConverterMojoName();

			pomSwaps.put("#CONVERTER_GROUP_ID#", conversionType.getConverterGroupId());
			pomSwaps.put("#CONVERTER_ARTIFACT_ID#", conversionType.getConverterArtifactId());
			pomSwaps.put("#ARTIFACTID#", conversionType.getConverterOutputArtifactId() + extensionSuffix);

			final StringBuffer licenseInfo = new StringBuffer();

			for (final String s : conversionType.getLicenseInformation())
			{
				licenseInfo.append(s);
			}

			pomSwaps.put("#LICENSE#", licenseInfo.toString());

			for (final String s : conversionType.getNoticeInformation())
			{
				noticeAppend.append(s);
			}

			final StringBuilder userOptions = new StringBuilder();
			String[] classifiers = null;

			if (converterOptionValues != null)
			{
				final String optionIndent = "\t\t\t\t\t\t\t\t\t";

				for (final Entry<ConverterOptionParam, Set<String>> option : converterOptionValues.entrySet())
				{
					if (option.getKey().getInternalName().equals(CLASSIFIERS_OPTION))
					{
						//handled below
						classifiers = option.getValue().toArray(new String[option.getValue().size()]);
						continue;
					}
					if (option.getValue() != null)
					{
						if (!option.getKey().isAllowMultiSelect() && (option.getValue().size() > 1))
						{
							LOG.info("Throwing exception back because the option " + option.getKey().getDisplayName() + " allows at most, one value");
							throw new Exception("The option " + option.getKey().getDisplayName() + " allows at most, one value");
						}

						if (!option.getKey().isAllowNoSelection() && (option.getValue().size() == 0))
						{
							LOG.info("Throwing exception back because This option " + option.getKey().getDisplayName() + " requires a value");
							throw new Exception("This option " + option.getKey().getDisplayName() + " requires a value");
						}

						if (option.getValue().size() > 0)
						{
							if (option.getKey().isAllowMultiSelect())
							{
								userOptions.append(optionIndent + "<" + option.getKey().getInternalName() + "s>\n");

								for (final String value : option.getValue())
								{
									userOptions.append(optionIndent + "\t<" + option.getKey().getInternalName() + ">");

									if (UUIDUtil.isUUID(value))
									{
										userOptions.append("\n");
										userOptions.append(optionIndent + "\t\t<description></description>\n");  // Its ok not to populate this
										userOptions.append(optionIndent + "\t\t<uuid>" + value + "</uuid>\n");
										userOptions.append(optionIndent + "\t");
									}
									else
									{
										userOptions.append(value);
									}

									userOptions.append("</" + option.getKey().getInternalName() + ">\n");
								}

								userOptions.append(optionIndent + "</" + option.getKey().getInternalName() + "s>");
							}
							else
							{
								final String value = option.getValue().iterator().next();

								userOptions.append(optionIndent + "<" + option.getKey().getInternalName() + ">");

								if (UUIDUtil.isUUID(value))
								{
									userOptions.append("\n");
									userOptions.append(optionIndent + "\t<description></description>\n");  // Its ok not to populate this
									userOptions.append(optionIndent + "\t<uuid>" + value + "</uuid>\n");
									userOptions.append(optionIndent);
								}
								else
								{
									userOptions.append(value);
								}

								userOptions.append("</" + option.getKey().getInternalName() + ">");
							}
						}
					}
					else if (!option.getKey().isAllowNoSelection())
					{
						LOG.info("Throwing exception back because this option " + option.getKey().getDisplayName() + " requires a value");
						throw new Exception("This option " + option.getKey().getDisplayName() + " requires a value");
					}
				}
			}

			final StringBuilder profiles = new StringBuilder();

			if (classifiers == null)
			{
				//no classifier option found above
				switch (conversionType)
				{
					case SCT:
					case SCT_EXTENSION:
						classifiers = new String[] { "Snapshot", "Delta", "Full" };
						break;

					default :
						classifiers = new String[] { "" };
						break;
				}
			}

			for (final String classifier : classifiers)
			{
				temp = FileUtil.readFile("converterProjectTemplate/pomSnippits/profile.xml");
				temp = temp.replaceAll("#CLASSIFIER#", classifier);
				temp = temp.replaceAll("#CONVERTER_GROUP_ID#", conversionType.getConverterGroupId());
				temp = temp.replaceAll("#CONVERTER_ARTIFACT_ID#", conversionType.getConverterArtifactId());
				temp = temp.replaceAll("#CONVERTER_VERSION#", converterVersion);
				temp = temp.replaceAll("#GOAL#", goal);
				temp = temp.replaceAll("#USER_CONFIGURATION_OPTIONS#", userOptions.toString());
				profiles.append(temp);

				String assemblyInfo = FileUtil.readFile("converterProjectTemplate/src/assembly/assembly.xml");
				final StringBuilder assemblySnippits = new StringBuilder();

				for (final String classifier2 : classifiers)
				{
					String assemblyRef = FileUtil.readFile("converterProjectTemplate/src/assembly/assemblySnippits/assemblyRef.xml");

					assemblyRef = assemblyRef.replace("#ASSEMBLY#", "assembly-" + classifier2 + ".xml");
					assemblySnippits.append(assemblyRef);
				}

				assemblyInfo = assemblyInfo.replace("#ASSEMBLY_FILES#", assemblySnippits.toString());
				assemblyInfo = assemblyInfo.replaceAll("#CLASSIFIER#", classifier);

				if (classifier.length() == 0)
				{
					assemblyInfo = assemblyInfo.replaceAll("#CLASSIFIER_WILD#", classifier);
				}
				else
				{
					assemblyInfo = assemblyInfo.replaceAll("#CLASSIFIER_WILD#", classifier + "*");
				}

				final File assemblyFile = new File(baseFolder, "src/assembly/assembly-" + classifier + ".xml");

				assemblyFile.getParentFile().mkdirs();
				Files.write(assemblyFile.toPath(), assemblyInfo.getBytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
			}

			pomSwaps.put("#PROFILE#", profiles.toString());

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

			if (extraProperties.length() > 0)
			{
				extraProperties.setLength(extraProperties.length() - 1);
			}

			pomSwaps.put("#EXTRA_PROPERTIES#", extraProperties.toString());
			FileUtil.writeFile("shared", "NOTICE.txt", baseFolder, new HashMap<>(), noticeAppend.toString());
			FileUtil.writeFile("converterProjectTemplate", "pom.xml", baseFolder, pomSwaps, "");

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

	// ~--- get methods ---------------------------------------------------------

	/**
	 * Will return an Artifact with only the groupID and artifactID populated - this represents the
	 * artifact that is capable of handling the conversion of the specified source content.
	 * 
	 * @param artifactId - the artifactid of the source content that the user desires to converter.
	 * @return - the group and artifact id of the converter tool that is capable of handling that content.
	 */
	public static Converter getConverterForSourceArtifact(String artifactId)
	{
		final SupportedConverterTypes supportedConverterType = SupportedConverterTypes.findBySrcArtifactId(artifactId);

		return new Converter(supportedConverterType.getConverterGroupId(), supportedConverterType.getConverterArtifactId(), "");
	}

	/**
	 * Gets the converter options. Look at {@link ConverterOptionParam#fromArtifact(File, SupportedConverterTypes, String, String, String, char[])}
	 *
	 * @param converter The converter you want the options for
	 * @param converterVersion the version of the converter you want the options for
	 * @param repositoryBaseURL the repository base URL
	 * @param repositoryUsername the repository username
	 * @param repositoryPassword the repository password
	 * @return the converter options
	 * @throws Exception the exception
	 */
	public static ConverterOptionParam[] getConverterOptions(SupportedConverterTypes converter, String converterVersion, String repositoryBaseURL, String repositoryUsername,
			String repositoryPassword) throws Exception
	{
		return ConverterOptionParam.fromArtifact(null, converter, converterVersion, repositoryBaseURL, repositoryUsername, repositoryPassword.toCharArray());
	}

	/**
	 * Return information about all of the supported conversion types, including all of the information types
	 * that must be supplied with each converter.
	 *
	 * @return the supported conversions
	 */
	public static SupportedConverterTypes[] getSupportedConversions()
	{
		return SupportedConverterTypes.values();
	}
}
