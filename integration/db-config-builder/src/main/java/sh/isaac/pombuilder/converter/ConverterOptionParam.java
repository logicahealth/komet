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


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import sh.isaac.api.util.DownloadUnzipTask;
import sh.isaac.api.util.WorkExecutors;
import sh.isaac.dbConfigBuilder.artifacts.MavenArtifactUtils;
import sh.isaac.pombuilder.FileUtil;

/**
 *
 * {@link ConverterOptionParam}
 *
 * The set of options that apply to a particular converter. Converters build this object, and serialize it to json, and publish it to maven.
 * Consumers (the GUI) read the json file, and pass it here, or ask us to read the json file and parse it.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConverterOptionParam
{
	/** The Constant MAVEN_FILE_TYPE. */
	public static final String MAVEN_FILE_TYPE = "options.json";

	/** The Constant LOG. */
	private static final Logger LOG = LogManager.getLogger();

	private String displayName;
	private String internalName;
	private String description;
	private boolean allowNoSelection;
	private boolean allowMultiSelectInPomMode;
	private boolean allowMultiSelectInDirectMode = false;
	private String[] defaultsForDirectMode = new String[0];

	/** The suggested pick list values. */
	private ConverterOptionParamSuggestedValue[] suggestedPickListValues;

	/**
	 * Instantiates a new converter option param.
	 */
	@SuppressWarnings("unused")
	private ConverterOptionParam()
	{
		// for jackson
	}

	/**
	 * Instantiates a new converter option param.
	 *
	 * @param displayName The name of this option
	 * @param internalName The name to use when writing the option to a pom file
	 * @param description A description suitable for display to end users of the system (in the GUI)
	 * @param allowNoSelection true if it is valid for the user to select 0 entries from the pick list, false if they must select 1 or more.
	 * @param allowMultiSelectInPomMode true if it is valid for the user to select more than 1 entry from the pick list, false if they may select at most 1
	 *            when the conversion is being done by a generated pom file.
	 * @param allowMultiSelectInDirectMode true if it is valid for the user to select more than 1 entry from the pick list when importing directly into a live 
	 *            DB.  False if they may select at most 1.
	 * @param defaultsForDirectMode - the default values (if any) that can be used for direct import
	 * @param suggestedPickListValues the values to provide the user to select from. This may not be an all-inclusive list of values - the
	 *            user should still have the option to provide their own value.
	 */
	@SafeVarargs
	public ConverterOptionParam(String displayName, String internalName, String description, boolean allowNoSelection, boolean allowMultiSelectInPomMode,
			boolean allowMultiSelectInDirectMode, String[] defaultsForDirectMode, ConverterOptionParamSuggestedValue... suggestedPickListValues)
	{
		this.displayName = displayName;
		this.internalName = internalName;
		this.description = description;
		this.allowNoSelection = allowNoSelection;
		this.allowMultiSelectInPomMode = allowMultiSelectInPomMode;
		this.allowMultiSelectInDirectMode = allowMultiSelectInDirectMode;
		this.defaultsForDirectMode = defaultsForDirectMode == null ? new String[] {} : defaultsForDirectMode;
		this.suggestedPickListValues = suggestedPickListValues;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null)
		{
			return false;
		}

		if (getClass() != obj.getClass())
		{
			return false;
		}

		final ConverterOptionParam other = (ConverterOptionParam) obj;

		if (this.allowNoSelection != other.allowNoSelection)
		{
			return false;
		}

		if (this.allowMultiSelectInPomMode != other.allowMultiSelectInPomMode)
		{
			return false;
		}
		
		if (this.allowMultiSelectInDirectMode != other.allowMultiSelectInDirectMode)
		{
			return false;
		}

		if (this.description == null)
		{
			if (other.description != null)
			{
				return false;
			}
		}
		else if (!this.description.equals(other.description))
		{
			return false;
		}

		if (this.displayName == null)
		{
			if (other.displayName != null)
			{
				return false;
			}
		}
		else if (!this.displayName.equals(other.displayName))
		{
			return false;
		}

		if (this.internalName == null)
		{
			if (other.internalName != null)
			{
				return false;
			}
		}
		else if (!this.internalName.equals(other.internalName))
		{
			return false;
		}

		if (!Arrays.equals(this.suggestedPickListValues, other.suggestedPickListValues))
		{
			return false;
		}
		
		if (!Arrays.equals(this.defaultsForDirectMode, other.defaultsForDirectMode))
		{
			return false;
		}

		return true;
	}

	/**
	 * Read the options specification from a json file found on the provided maven artifact server, with the provided artifact type.
	 * May return an empty array, will not return null.
	 * 
	 * @param localMavenPath - optional - if provided, will look in the local maven store first. If not found, then looks in the artifact store at
	 *            baseMavenUrl
	 * @param converter The converter you want the options for
	 * @param converterVersion the version of the converter you want the options for
	 * @param baseMavenUrl the base maven url
	 * @param mavenUsername the maven username
	 * @param mavenPassword the maven password
	 * @return the converter option param[]
	 * @throws Exception the exception
	 */
	public static ConverterOptionParam[] fromArtifact(File localMavenPath, SupportedConverterTypes converter, String converterVersion, String baseMavenUrl,
			String mavenUsername, char[] mavenPassword) throws Exception
	{
		File tempFolder = null;

		try
		{
			LOG.debug("Trying to read 'options.json' for {} from '{}' '{}'", converter.getNiceName(), converterVersion, baseMavenUrl);
			tempFolder = File.createTempFile("jsonDownload", "");
			tempFolder.delete();
			tempFolder.mkdir();

			if (localMavenPath != null && localMavenPath.isDirectory())
			{
				String temp = converter.converterGroupId.replaceAll("\\.", "/");
				File browseFolder = new File(localMavenPath, temp + "/" + converter.converterArtifactId);
				for (File versionFolder : browseFolder.listFiles())
				{
					if (versionFolder.isDirectory() && versionFolder.getName().equals(converterVersion))
					{
						File zippedJsonFile = new File(versionFolder, converter.converterArtifactId + "-" + converterVersion + "." + MAVEN_FILE_TYPE + ".zip");
						if (zippedJsonFile.exists())
						{
							try
							{
								ZipFile zipFile = new ZipFile(zippedJsonFile);
								zipFile.extractAll(tempFolder.getCanonicalPath());
								LOG.info("Read json options from local maven folder");
								return processFolderWithJson(converter, tempFolder);
							}
							catch (ZipException e)
							{
								LOG.error("error extracting local options zip file", e);
							}
						}
						else
						{
							LOG.warn("Found version folder, but no metadata?");
						}
					}
				}
			}

			// First, try to get the pom file to validate the params they sent us. If this fails, they sent bad info, and we fail.
			final URL pomURL = MavenArtifactUtils.makeFullURL(baseMavenUrl, mavenUsername, mavenPassword, converter.converterGroupId,
					converter.converterArtifactId, converterVersion, "", "pom");
			DownloadUnzipTask dut = new DownloadUnzipTask(mavenUsername, mavenPassword, pomURL, false, true, tempFolder);

			WorkExecutors.get().getExecutor().execute(dut);

			final File pomFile = dut.get();

			if (!pomFile.exists())
			{
				LOG.debug("Throwing back an exception, as no pom was readable for the specified artifact");
				throw new Exception("Failed to find the pom file for the specified project");
			}
			else
			{
				pomFile.delete();
			}

			// Now that we know that the credentials / artifact / version are good - see if there is a config file (there may not be)
			try
			{
				final URL config = MavenArtifactUtils.makeFullURL(baseMavenUrl, mavenUsername, mavenPassword, converter.converterGroupId,
						converter.converterArtifactId, converterVersion, "", MAVEN_FILE_TYPE + ".zip");

				dut = new DownloadUnzipTask(mavenUsername, mavenPassword, config, true, true, tempFolder);
				WorkExecutors.get().getExecutor().execute(dut);

				final File unzippedFolder = dut.get();

				return processFolderWithJson(converter, unzippedFolder);
			}
			catch (final Exception e)
			{
				// If we successfully downloaded the pom file, but failed here, just assume this file doesn't exist / isn't applicable to this
				// converter.
				LOG.info("No config file found for converter " + converter);
				return new ConverterOptionParam[] {};
			}
		}
		finally
		{
			try
			{
				FileUtil.recursiveDelete(tempFolder);
			}
			catch (final Exception e)
			{
				LOG.error("Problem cleaning up temp folder {}", tempFolder, e);
			}
		}
	}

	/**
	 * @param unzippedFolder
	 * @return
	 * @throws IOException 
	 */
	private static ConverterOptionParam[] processFolderWithJson(SupportedConverterTypes converter, File unzippedFolder) throws IOException
	{
		// This will have returned a folder that contains multiple files:
		// convert-ICD10-to-ibdf.options.json
		// convert-RF2-to-ibdf.options.json
		// convert-rxnorm-to-ibdf.options.json

		ConverterOptionParam[] temp = new ConverterOptionParam[] {};
		for (File jsonFile : unzippedFolder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				if (name.toLowerCase().endsWith(".options.json"))
				{
					return true;
				}
				return false;
			}
		}))
		{
			if (jsonFile.getName().startsWith(converter.getConverterMojoName()))
			{
				temp = fromFile(jsonFile);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("From file {} Found options: {}", jsonFile.getName(), Arrays.toString(temp));
				}
				else
				{
					LOG.info("From file {} Read {} options", jsonFile.getName(), temp.length);
				}
				break;
			}

			jsonFile.delete();
		}
		return temp;
	}

	/**
	 * Read the options specification from a json file.
	 *
	 * @param jsonConverterOptionFile the json converter option file
	 * @return the converter option param[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ConverterOptionParam[] fromFile(File jsonConverterOptionFile) throws IOException
	{
		final ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(jsonConverterOptionFile, ConverterOptionParam[].class);
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;

		result = prime * result + (this.allowNoSelection ? 1231 : 1237);
		result = prime * result + (this.allowMultiSelectInPomMode ? 1231 : 1237);
		result = prime * result + (this.allowMultiSelectInDirectMode ? 1231 : 1237);
		result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());
		result = prime * result + ((this.displayName == null) ? 0 : this.displayName.hashCode());
		result = prime * result + ((this.internalName == null) ? 0 : this.internalName.hashCode());
		result = prime * result + Arrays.hashCode(this.suggestedPickListValues);
		result = prime * result + Arrays.hashCode(this.defaultsForDirectMode);
		return result;
	}

	/**
	 * Serialize to json.
	 *
	 * @param options the options
	 * @param outputFile the output file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void serialize(ConverterOptionParam[] options, File outputFile) throws IOException
	{
		final ObjectMapper mapper = new ObjectMapper();

		try
		{
			mapper.writeValue(outputFile, options);
		}
		catch (final JsonProcessingException e)
		{
			throw new RuntimeException("Unexpected error", e);
		}
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString()
	{
		return "ConverterOptionParam [displayName=" + this.displayName + ", internalName=" + this.internalName + ", description=" + this.description
				+ ", allowNoSelection=" + this.allowNoSelection + ", allowMultiSelectInPomMode=" + this.allowMultiSelectInPomMode
				+ ", allowMultiSelectInDirectMode=" + this.allowMultiSelectInDirectMode + ", defaultsForDirectMode=["
						+ Arrays.toString(this.defaultsForDirectMode) + "] " + ", suggestedPickListValues=["
				+ Arrays.toString(this.suggestedPickListValues) + "]";
	}
	
	/**
	 * true if it is valie for the user to select more than 1 entry from the pick list, false if they may select at most 1 when running the 
	 * conversion as a maven / pom conversion.
	 *
	 * @return true, if allow multi select
	 */
	public boolean isAllowMultiSelectInPomMode()
	{
		return this.allowMultiSelectInPomMode;
	}
	
	/**
	 * true if it is valie for the user to select more than 1 entry from the pick list, false if they may select at most 1 when running the 
	 * conversion as a direct conversion.
	 *
	 * @return true, if allow multi select
	 */
	public boolean isAllowMultiSelectInDirectMode()
	{
		return this.allowMultiSelectInDirectMode;
	}

	/**
	 * true if it is valid for the user to select 0 entries from the pick list, false if they must select 1 or more.
	 *
	 * @return true, if allow no selection
	 */
	public boolean isAllowNoSelection()
	{
		return this.allowNoSelection;
	}

	/**
	 * The description of this option suitable to display to the end user, in a GUI.
	 *
	 * @return the description
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * The displayName of this option - suitable for GUI use to the end user.
	 *
	 * @return the display name
	 */
	public String getDisplayName()
	{
		return this.displayName;
	}

	/**
	 * The internalName of this option - use when creating the pom file.
	 *
	 * @return the internal name
	 */
	public String getInternalName()
	{
		return this.internalName;
	}

	/**
	 * Gets the suggested pick list values.
	 *
	 * @return the suggested pick list values
	 */
	public ConverterOptionParamSuggestedValue[] getSuggestedPickListValues()
	{
		return this.suggestedPickListValues;
	}
	
	/**
	 * Get the default values for direct mode
	 * @return
	 */
	public String[] getDefaultsForDirectMode()
	{
		return defaultsForDirectMode;
	}
}
