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
package sh.isaac.dbConfigBuilder.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import javafx.application.Platform;
import sh.isaac.api.LookupService;
import sh.isaac.api.util.metainf.VersionFinder;
import sh.isaac.converters.sharedUtils.config.ConfigOptionsDescriptor;
import sh.isaac.dbConfigBuilder.artifacts.IBDFFile;
import sh.isaac.dbConfigBuilder.artifacts.SDOSourceContent;
import sh.isaac.pombuilder.converter.ContentConverterCreator;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.ConverterOptionParamSuggestedValue;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.pombuilder.upload.SrcUploadCreator;

/**
 * @author a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * A simple command-line tool to collect the necessary parameters to create a maven project which will 
 * execute one of the mojo-based converters for content.
 */
public class CreateMojoExecutionProject
{

	/**
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable
	{
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in)))
		{
			System.out.println("This application builds Content Converter configurations");
			System.out.println("Supported content artifact types");
			System.out.println("");
			{
				int i = 1;
				for (SupportedConverterTypes x : ContentConverterCreator.getSupportedConversions())
				{
					System.out.println("  " + i++ + ") " + x.getArtifactId() + " - " + x.getNiceName());
				}
			}

			System.out.println();

			int selection = 0;
			while (selection == 0)
			{
				System.out.println("Please enter the number of the desired converter or 'q' to quit");
				String line = bufferedReader.readLine();
				if (line.trim().equalsIgnoreCase("q"))
				{
					System.exit(0);
				}
				try
				{
					selection = Integer.parseInt(line.trim());
					if (selection <= 0 || selection > SupportedConverterTypes.values().length)
					{
						selection = 0;
					}
				}
				catch (NumberFormatException e)
				{
					// go round again
				}
			}

			SupportedConverterTypes selectedConverter = ContentConverterCreator.getSupportedConversions()[selection - 1];

			String artifactId = selectedConverter.getArtifactId();
			
			if (selectedConverter.getArtifactId().contains("*"))
			{
				System.out.println("This selected converter type (" + selectedConverter.getArtifactId() + ") contains a wild card");
				System.out.println("Please provide the value to replace the '*'.  For snomed extensions, this is typically a language such as 'en' or a country code such as 'us'");
				String wildCard = bufferedReader.readLine();
				artifactId = selectedConverter.getArtifactId().replaceAll("\\*", wildCard);
			}

			System.out.println();
			System.out.println(selectedConverter.getSourceVersionDescription());
			System.out.println("What version of the source content will be converted?");
			String sourceVersion = bufferedReader.readLine();

			SDOSourceContent ssc = new SDOSourceContent(SrcUploadCreator.SRC_UPLOAD_GROUP, artifactId, sourceVersion);

			System.out.println("Creating a content converter config for " + ssc);
			System.out.println();

			String converterVersion = VersionFinder.findProjectVersion(true);

			System.out.println("What version of the converter software should be utilized?  Press enter for the default of " + converterVersion);

			String readVersion = bufferedReader.readLine();
			if (readVersion.trim().length() > 0)
			{
				converterVersion = readVersion;
			}

			System.out.println("Using converter version " + converterVersion);
			System.out.println();

			SDOSourceContent[] additionalSourceDependencies = new SDOSourceContent[selectedConverter.getArtifactDependencies().size()];
			if (additionalSourceDependencies.length > 0)
			{
				System.out.println("The selected converter has additional source dependencies.");
				for (int i = 0; i < additionalSourceDependencies.length; i++)
				{
					SupportedConverterTypes dependency = SupportedConverterTypes.findBySrcArtifactId(selectedConverter.getArtifactDependencies().get(i));
					System.out.println("What version of the dependency " + dependency.getNiceName() + " should be used?");
					System.out.println(dependency.getSourceVersionDescription());
					String dependencyVersion = bufferedReader.readLine();
					
					System.out.println("Please specify the classifier of the additional source dependency (Delta, Snapshot, Full, etc) if any - just push enter for none.");
					String classifier = bufferedReader.readLine();
					
					additionalSourceDependencies[i] = new SDOSourceContent(SrcUploadCreator.SRC_UPLOAD_GROUP, dependency.getArtifactId(), dependencyVersion, 
							StringUtils.isNotBlank(classifier.trim()) ? classifier.trim() : null);
					System.out.println("Added the dependency " + additionalSourceDependencies[i]);
					System.out.println();
				}
			}

			IBDFFile[] additionalIBDFDependencies = new IBDFFile[selectedConverter.getIBDFDependencies().size()];
			if (additionalIBDFDependencies.length > 0)
			{
				System.out.println("The selected converter has additional ibdf dependencies.");
				for (int i = 0; i < additionalIBDFDependencies.length; i++)
				{
					System.out.println("What version of the dependency " + selectedConverter.getIBDFDependencies().get(i) + " should be used?");
					System.out.println("The version pattern for ibdf files is {sourceVersion}-loader-{converterVersion}");
					System.out.println("The newest converterVersion would be " + VersionFinder.findProjectVersion(true));
					System.out.println("For the sourceVersion parameter, " + SupportedConverterTypes.findByIBDFArtifactId(selectedConverter.getIBDFDependencies().get(i))
							.getSourceVersionDescription());
					String dependencyVersion = bufferedReader.readLine();
					
					System.out.println("Please specify the classifier of the additional ibdf dependency (Delta, Snapshot, Full, etc) if any - just push enter for none.");
					String classifier = bufferedReader.readLine();
					
					additionalIBDFDependencies[i] = new IBDFFile(ContentConverterCreator.IBDF_OUTPUT_GROUP, selectedConverter.getIBDFDependencies().get(i),
							dependencyVersion, StringUtils.isNotBlank(classifier.trim()) ? classifier.trim() : null);
					System.out.println("Added the dependency " + additionalIBDFDependencies[i]);
					System.out.println();
				}
			}

			Map<ConverterOptionParam, Set<String>> converterOptionValues = new HashMap<>();
			for (ConfigOptionsDescriptor cod : LookupService.getServices(ConfigOptionsDescriptor.class))
			{
				if (cod.getName().equals(selectedConverter.getConverterMojoName()))
				{
					System.out.println("The selected requires configuration parameters.");
					for (ConverterOptionParam cop : cod.getConfigOptions())
					{
						System.out.println(cop.getDisplayName() + ": " + cop.getDescription());
						System.out.println("The suggested values are:");
						for (ConverterOptionParamSuggestedValue sv : cop.getSuggestedPickListValues())
						{
							System.out.println("  " + sv.getValue() + (sv.getValue().equals(sv.getDescription()) ? "" : " - " + sv.getDescription()));
						}
						System.out.println("Please enter your values, separated by commas:");
						System.out.println();
						String values = bufferedReader.readLine();
						if (values.length() > 0)
						{
							HashSet<String> uniqueValues = new HashSet<>();
							for (String s : values.split(","))
							{
								uniqueValues.add(s);
							}
							converterOptionValues.put(cop, uniqueValues);
						}
					}
				}
			}

			ContentConverterCreator.createContentConverter(ssc, converterVersion, additionalSourceDependencies, additionalIBDFDependencies,
					converterOptionValues, null, null, null, new File("target"), false);

			System.out.println("Configuration created in " + new File("target/converter-executor").getAbsolutePath());
			System.out.println("Convert and publish your content with a command like ");
			System.out.println("mvn clean deploy -DaltDeploymentRepository=mynexus::default::https://sagebits.net/nexus/repository/tmp-snapshots/");
			LookupService.shutdownSystem();
			Platform.exit();
		}
	}

}
