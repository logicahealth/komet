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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.input.BOMInputStream;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

/**
 * 
 * {@link EnumValidatedCsvFileReader}
 *
 * Reads CSV formatted file
 * Should work on any COLUMNS enum version based on columnsEnumClass passed to constructor
 * The only requirement of a passed COLUMNS Enum is that toString() returns the column header
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 * @param <COLUMNS>
 */
public class EnumValidatedCsvFileReader<COLUMNS extends Enum<COLUMNS>>
{
	int dataLinesRead = 0;

	final Class<COLUMNS> columnsEnumClass;
	final CSVReader reader;

	/**
	 * @param f - target CSV file
	 * @param columnsEnumClass - COLUMNS Enum class against which to validate number of columns and, optionally, headers
	 * @throws IOException
	 * @throws CsvValidationException 
	 */
	public EnumValidatedCsvFileReader(Path f, Class<COLUMNS> columnsEnumClass) throws IOException, CsvValidationException
	{
		this(f, columnsEnumClass, true, true);
	}

	/**
	 * @param f - target CSV file
	 * @param columnsEnumClass - COLUMNS Enum class against which to validate number of columns and, optionally, headers
	 * @param headerExists - boolean indicating whether a header row exists and should be consumed and optionally validated against the COLUMNS Enum
	 * @param validateHeaderAgainstColumnsEnum - boolean indicating whether an existing header should be validated as matching the toString() values
	 *            of the passed COLUMNS Enum
	 * 
	 *            This constructor throws IllegalArgumentException if validateHeaderAgainstColumnsEnum == true && headerExists == false
	 * 
	 * @throws IOException, IllegalArgumentException
	 * @throws CsvValidationException 
	 */
	public EnumValidatedCsvFileReader(Path f, Class<COLUMNS> columnsEnumClass, boolean headerExists, boolean validateHeaderAgainstColumnsEnum)
			throws IOException, CsvValidationException
	{
		this.columnsEnumClass = columnsEnumClass;

		this.reader = new CSVReader(new BufferedReader(new InputStreamReader(new BOMInputStream(Files.newInputStream(f, StandardOpenOption.READ)))));

		if (headerExists)
		{
			dataLinesRead = -1; // Start at -1 so after initial header read dataLinesRead == 0

			// Read header
			String[] headerRow = readLine();

			// If validateHeaderAgainstColumnsEnum then validate that headerRow matches COLUMNS Enum.getEnumConstants()
			if (validateHeaderAgainstColumnsEnum)
			{
				if (headerRow == null)
				{
					throw new RuntimeException("Invalid (null) header row in file " + f);
				}
				for (int i = 0; i < headerRow.length; ++i)
				{
					if (!headerRow[i].equals(columnsEnumClass.getEnumConstants()[i].toString()))
					{
						throw new RuntimeException("Unexpected header \"" + headerRow[i] + "\" in column " + i + ".  Expected \""
								+ columnsEnumClass.getEnumConstants()[i].toString());
					}
				}
			}
		}
		else
		{
			if (validateHeaderAgainstColumnsEnum)
			{
				throw new IllegalArgumentException("Cannot validate headers against COLUMNS Enum because passed headerExists parameter is set to false");
			}
			dataLinesRead = 0; // Start at 0
		}
	}

	Class<COLUMNS> getColumnsEnumClass()
	{
		return columnsEnumClass;
	}

	int getDataLinesRead()
	{
		return dataLinesRead <= 0 ? 0 : dataLinesRead;
	}

	/**
	 * Reads and returns a line of data from the source CSV file,
	 * validating number of columns against columnsEnumClass.getEnumConstants().length,
	 * trimming results and incrementing dataLinesRead
	 * 
	 * @return all the lines
	 * @throws IOException
	 * @throws CsvValidationException 
	 */
	public String[] readLine() throws IOException, CsvValidationException
	{
		String[] untrimmed = reader.readNext();
		String[] trimmed = null;
		if (untrimmed != null)
		{
			++dataLinesRead;

			if (untrimmed.length < columnsEnumClass.getEnumConstants().length)
			{
				throw new RuntimeException("Data error - not enough fields (" + untrimmed.length + " of " + columnsEnumClass.getEnumConstants().length
						+ ") found on line " + dataLinesRead + ": " + Arrays.toString(untrimmed));
			}
			else if (untrimmed.length > columnsEnumClass.getEnumConstants().length)
			{
				throw new RuntimeException("Data error - too many fields (" + untrimmed.length + " of " + columnsEnumClass.getEnumConstants().length
						+ ") found on line " + dataLinesRead + ": " + Arrays.toString(untrimmed));
			}

			trimmed = new String[untrimmed.length];
			for (int i = 0; i < untrimmed.length; ++i)
			{
				trimmed[i] = untrimmed[i] != null ? untrimmed[i].trim() : null;
			}
		}

		return trimmed;
	}

	/**
	 * Reads and returns a line of data from the source CSV file as a Map of value by header,
	 * validating number of columns against columnsEnumClass.getEnumConstants().length,
	 * trimming results and incrementing dataLinesRead
	 * 
	 * @return the column map
	 * @throws IOException
	 * @throws CsvValidationException 
	 */
	public Map<COLUMNS, String> readLineAsMap() throws IOException, CsvValidationException
	{
		String[] rowAsArray = readLine();
		if (rowAsArray != null)
		{
			Map<COLUMNS, String> rowAsMap = new HashMap<>();
			for (int i = 0; i < columnsEnumClass.getEnumConstants().length; ++i)
			{
				rowAsMap.put(columnsEnumClass.getEnumConstants()[i], rowAsArray[i]);
			}

			return rowAsMap;
		}
		else
		{
			return null;
		}
	}

	/**
	 * close() the CSVReader reader resource
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		reader.close();
	}
}
