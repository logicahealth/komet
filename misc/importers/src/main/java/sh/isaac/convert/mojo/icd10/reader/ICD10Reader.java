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

package sh.isaac.convert.mojo.icd10.reader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoExecutionException;
import sh.isaac.convert.mojo.icd10.data.ICD10;
import sh.isaac.converters.sharedUtils.ConsoleUtil;

/**
 * 
 * {@link ICD10Reader}
 *
 * @author <a href="mailto:"></a>
 *
 */
public class ICD10Reader
{
	// 2017 CM/PCS order files have 76,000 - 94,000 total rows, so good starting point
	private List<ICD10> icd10Codes = new ArrayList<ICD10>(100000);

	private final File file_;

	public ICD10Reader(File inputFileOrDirectory, String converterSourceArtifactVersion) throws MojoExecutionException
	{
		// Need this for finding the right file and for concept date/time
		if (converterSourceArtifactVersion == null || converterSourceArtifactVersion.length() != 4)
		{
			throw new RuntimeException("The 'converterSourceArtifactVersion' is not " + "set correctly, which should be the year of the codeset.");
		}

		File[] files_;

		if (inputFileOrDirectory.isDirectory())
		{
			files_ = inputFileOrDirectory.listFiles();
		}
		else
		{
			files_ = new File[] { inputFileOrDirectory };
		}

		ArrayList<File> files = new ArrayList<File>();
		for (File f : files_)
		{
			if (f.isFile() && f.getName().toLowerCase().endsWith(".zip") && f.getName().contains(converterSourceArtifactVersion))
			{
				files.add(f);
			}
		}

		if (files.size() != 1)
		{
			throw new RuntimeException("Was expecting to find a single zip file which contained the source artifact version of '"
					+ converterSourceArtifactVersion + "', but instead, we found " + files.size() + " files in " + inputFileOrDirectory.getAbsolutePath());
		}

		file_ = files.get(0);
		boolean foundData = false;
		try
		{
			ZipFile zf = new ZipFile(file_);
			Enumeration<? extends ZipEntry> zipEntries = zf.entries();
			while (zipEntries.hasMoreElements())
			{
				ZipEntry ze = zipEntries.nextElement();
				if (ze.getName().toLowerCase().endsWith(".txt") && ze.getName().toLowerCase().contains("order_" + converterSourceArtifactVersion.trim()))
				{
					// Just processing the first file/zip entry found that matches
					ConsoleUtil.println("Prepared to process: " + ze.getName());
					this.readCodes(zf.getInputStream(ze));
					foundData = true;
				}
				if (foundData)
				{
					break;
				}
			}
			zf.close();
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Exception", e);
		}
		if (!foundData)
		{
			throw new RuntimeException(
					"Was looking inside the zip file " + file_.getAbsolutePath() + " for a file name that ends in '.txt' and contains 'order_"
							+ converterSourceArtifactVersion.trim() + "' but was unable to find a matching file");
		}

	}

	private void readCodes(InputStream is) throws MojoExecutionException
	{
		/*
		 * Per ICD-10 PCS and CM Order File documentation
		 * Pos Len Contents
		 * 1 5 Order number, right justified, zero filled.
		 * 6 1 Blank
		 * 7 7 ICD-10-CM or ICD-10-PCS code. Dots are not included.
		 * 14 1 Blank
		 * 15 1 0 if the code is a "header" - not valid for HIPAA-covered transactions. 1 if the code is valid for submission for HIPAA-covered
		 * transactions.
		 * 16 1 Blank
		 * 17 60 Short description
		 * 77 1 Blank
		 * 78 EOL Long description
		 */
		try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8")))
		{
			for (String line; (line = br.readLine()) != null;)
			{
				icd10Codes.add(new ICD10(line.substring(0, 0 + 5).trim(), line.substring(6, 6 + 7).trim(), line.substring(14, 14 + 1).trim(),
						line.substring(16, 16 + 60).trim(), line.substring(77).trim()));
			}
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Exception", e);
		}
	}

	public List<ICD10> getAllCodes()
	{
		return icd10Codes;
	}

	/**
	 * Returns 1 digit codes before 2 digit codes, etc.
	 * 
	 * @return the data
	 */
	public Stream<ICD10> getIntermediateHeaderConcepts()
	{
		return icd10Codes.stream().filter(i -> i.isHeader()).sorted(new Comparator<ICD10>()
		{
			public int compare(ICD10 o1, ICD10 o2)
			{
				return Integer.compare(o1.getCode().length(), o2.getCode().length());
			}
		});
	}

	public Stream<ICD10> getLeafConcepts()
	{
		return icd10Codes.stream().filter(i -> !i.isHeader());
	}

	public int getAllCodesCount()
	{
		return icd10Codes.size();
	}
}
