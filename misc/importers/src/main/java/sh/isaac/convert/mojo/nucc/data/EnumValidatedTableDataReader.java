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

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import com.opencsv.exceptions.CsvValidationException;

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

	public EnumValidatedTableDataReader(Path inputFileOrDirectory, Class<COLUMNS> columnsEnumClass) throws IOException
	{
		this(inputFileOrDirectory, columnsEnumClass, true, true);
	}

	public EnumValidatedTableDataReader(Path inputFileOrDirectory, Class<COLUMNS> columnsEnumClass, boolean headerExists,
			boolean validateHeaderAgainstColumnsEnum) throws IOException
	{
		final AtomicReference<Path> file = new AtomicReference<>();
		Files.walk(inputFileOrDirectory, new FileVisitOption[] {}).forEach(path ->
		{
			if (path.toString().toLowerCase().endsWith(".csv"))
			{
				if (file.get() != null)
				{
					throw new RuntimeException("Only expected to find one csv file in the folder " + inputFileOrDirectory.normalize());
				}
				file.set(path);
			}
		});

		if (file.get() == null)
		{
			throw new IOException("Failed to locate the csv file in " + inputFileOrDirectory);
		}

		LogManager.getLogger().info("Prepared to process: " + file.get());

		try
		{
			this.fileReader = new EnumValidatedCsvFileReader<>(file.get(), columnsEnumClass, headerExists, validateHeaderAgainstColumnsEnum);
		}
		catch (CsvValidationException e)
		{
			throw new IOException(e);
		}
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
		catch (CsvValidationException e)
		{
			throw new IOException(e);
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