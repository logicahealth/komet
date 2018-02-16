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
import java.util.regex.Pattern;
import org.codehaus.plexus.util.StringUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import sh.isaac.api.LookupService;
import sh.isaac.api.util.WorkExecutors;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.pombuilder.converter.UploadFileInfo;
import sh.isaac.pombuilder.upload.SrcUploadCreator;

/**
 * @author a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * A simple command-line tool to collect the necessary parameters to create a maven project which will 
 * upload source content of various types in a standard form / format.
 */
public class CreateUploadProject
{

	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable
	{
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in)))
		{
			System.out.println("This application builds Source Upload configurations");
			System.out.println("Supported content types:");
			System.out.println("");
			{
				int i = 1;
				for (SupportedConverterTypes x : SupportedConverterTypes.values())
				{
					System.out.println("  " + i++ +") " + x.getArtifactId() + " - " + x.getNiceName());
				}
			}
			
			System.out.println();
			
			int selection = 0;
			while (selection == 0)
			{
				System.out.println("Please enter the number of the desired content or 'q' to quit");
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
					//go round again
				}
			}
			System.out.println("Creating a source upload config for " + SupportedConverterTypes.values()[selection - 1].getNiceName());
			System.out.println(SupportedConverterTypes.values()[selection - 1].getSourceVersionDescription());
			System.out.println();
			
			String version;
			while (true)
			{
				System.out.println("Please enter the source version: ");
				version = bufferedReader.readLine();
				if (version.trim().length() > 0)
				{
					if (Pattern.matches(SupportedConverterTypes.values()[selection - 1].getSourceVersionRegExpValidator(), version.trim()))
					{
						break;
					}
					else
					{
						System.out.println("The version must match the regular expression " 
								+ SupportedConverterTypes.values()[selection - 1].getSourceVersionRegExpValidator());
					}
				}
			}
			
			String extensionName = "";
			
			if (SupportedConverterTypes.values()[selection - 1].getArtifactId().contains("*"))
			{
				System.out.println("This source upload type (" + SupportedConverterTypes.values()[selection - 1].getArtifactId() + ") contains a wild card");
				System.out.println("Please provide the value - for snomed extensions, this is typically a language such as 'en'");
				extensionName = bufferedReader.readLine();
			}
			
			System.out.println("Creating a configuration for " + SupportedConverterTypes.values()[selection - 1].getNiceName() 
					+ " for version '" + version + "'" + (extensionName.length() > 0 ? " and the extension name '" + extensionName + "'" : ""));
			
			LookupService.startupWorkExecutors();
			
			Task<String> builder = SrcUploadCreator.createSrcUploadConfiguration(SupportedConverterTypes.values()[selection - 1], 
					version, 
					extensionName, 
					null, 
					null, 
					null, 
					null, 
					null, 
					null, 
					null, 
					new File("target"), 
					false, 
					false);
			
			WorkExecutors.get().getExecutor().submit(builder);
			builder.get();
			
			System.out.println("Configuration created in " + new File("target/src-upload").getAbsolutePath());
			System.out.println();
			System.out.println("You may now populate 'target/src-upload/native-source' with the source content for " 
					+ SupportedConverterTypes.values()[selection - 1].getNiceName() + ", which expects:");
			
			{
				int i = 1;
				for (UploadFileInfo fi :SupportedConverterTypes.values()[selection - 1].getUploadFileInfo())
				{
					System.out.println(i++ + ")");
					System.out.println("  File Description: " + fi.getExpectedNamingPatternDescription());
					System.out.println("  File Pattern: " + fi.getExpectedNamingPatternRegExpPattern());
					System.out.println("  File Sample Name: " + fi.getSampleName());
					if (StringUtils.isNotBlank(fi.getSuggestedSourceLocation()))
					{
						System.out.println("  File Suggested Source: " + fi.getSuggestedSourceLocation());
					}
					if (StringUtils.isNotBlank(fi.getSuggestedSourceURL()))
					{
						System.out.println("  File Suggested Source URL: " + fi.getSuggestedSourceURL());
					}
					System.out.println();
				}
			}
			
			LookupService.shutdownSystem();
			Platform.exit();
		}
		
		

	}

}
