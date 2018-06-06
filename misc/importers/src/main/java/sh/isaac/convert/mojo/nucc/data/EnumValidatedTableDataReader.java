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

package sh.isaac.convert.mojo.nucc.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * 
 * {@link EnumValidatedTableDataReader}
 *
 * Uses a CsvFileReader to process() a passed File,
 * validating contents with respect to a passed COLUMNS Enum,
 * and returning a Terminology object
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * @param <COLUMNS>
 */
public class EnumValidatedTableDataReader<COLUMNS extends Enum<COLUMNS>>
{
	private final EnumValidatedCsvFileReader<COLUMNS> fileReader;

	private EnumValidatedTableData<COLUMNS> terminology = new EnumValidatedTableData<>();

	public EnumValidatedTableDataReader(File inputFileOrDirectory, Class<COLUMNS> columnsEnumClass) throws IOException
	{
		this(inputFileOrDirectory, columnsEnumClass, true, true);
	}

	public EnumValidatedTableDataReader(File inputFileOrDirectory, Class<COLUMNS> columnsEnumClass, boolean headerExists,
			boolean validateHeaderAgainstColumnsEnum) throws IOException
	{
		File file = null;
		if (inputFileOrDirectory.isDirectory())
		{
			ArrayList<File> files = new ArrayList<File>();
			for (File f : inputFileOrDirectory.listFiles())
			{
				if (f.isFile() && (f.getName().toLowerCase().endsWith(".csv")))
				{
					files.add(f);
				}
			}

			if (files.size() != 1)
			{
				throw new RuntimeException(files.size() + " csv files were found inside of " + inputFileOrDirectory.getAbsolutePath()
						+ " but this implementation requires 1 and only 1 csv file to be present.");
			}

			file = files.get(0);
		}
		else
		{
			file = inputFileOrDirectory;
		}

		System.out.println("Prepared to process: " + file.getAbsolutePath());

		this.fileReader = new EnumValidatedCsvFileReader<>(file, columnsEnumClass, headerExists, validateHeaderAgainstColumnsEnum);
	}

	public EnumValidatedTableData<COLUMNS> process() throws IOException
	{
		try
		{
			for (Map<COLUMNS, String> row = fileReader.readLineAsMap(); row != null; row = fileReader.readLineAsMap())
			{
				terminology.rows().add(row);
			}
			return terminology;
		}
		finally
		{
			close();
		}
	}

	public void close() throws IOException
	{
		fileReader.close();
	}
}