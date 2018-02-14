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

package sh.isaac.convert.mojo.sopt.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import sh.isaac.converters.sharedUtils.ConsoleUtil;

/**
 * {@link EnumValidatedXSLFileReader}
 *
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 * @param <COLUMNS>
 */
public class EnumValidatedXSLFileReader<COLUMNS extends Enum<COLUMNS>>
{
	int dataLinesRead = 0;

	final Class<COLUMNS> columnsEnumClass;
	private Workbook workbook;
	private Sheet sheet;
	private String[][] data;

	public EnumValidatedXSLFileReader(InputStream excelFile, Class<COLUMNS> columnsEnumClass) throws IOException
	{
		this(excelFile, columnsEnumClass, true, true);
	}

	public EnumValidatedXSLFileReader(InputStream excelFile, Class<COLUMNS> columnsEnumClass, boolean headerExists, boolean validateHeaderAgainstColumnEnum)
			throws IOException
	{
		this.columnsEnumClass = columnsEnumClass;

		try
		{

			this.workbook = WorkbookFactory.create(excelFile);
			// data is located in the second sheet named
			// PHVS_SourceOfPaymentTypology
			this.sheet = workbook.getSheetAt(1);
			// logSheet();
			int rows = this.sheet.getLastRowNum();
			int cells = sheet.getRow(0).getPhysicalNumberOfCells();

			data = new String[rows][cells];
			Row row;
			Cell cell;

			for (int r = 0; r < rows; r++)
			{
				row = sheet.getRow(r);
				if (row != null)
				{
					for (int c = 0; c < cells; c++)
					{
						cell = row.getCell(c);
						if (cell != null)
						{
							data[r][c] = cell.getStringCellValue();
						}
					}
				}
			}

			// sort data by first column which contains hierarchy ids of
			// records
			Arrays.sort(data, (a, b) -> (a[0].compareTo(b[0])));
			// logArray();

		}
		catch (EncryptedDocumentException e)
		{
			throw new IOException(e);
		}
		catch (InvalidFormatException e)
		{
			throw new IOException(e);
		}
		catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (excelFile != null)
			{
				excelFile.close();
			}
		}

		if (headerExists)
		{
			dataLinesRead = 0;
		}
	}

	int getDataLinesRead()
	{
		return dataLinesRead <= 0 ? 0 : dataLinesRead;
	}

	/**
	 * Reads and returns a row of data from the source XSL file, validating
	 * number of columns against columnsEnumClass.getEnumConstants().length,
	 * trimming results and incrementing dataLinesRead
	 * 
	 * @return the row data
	 * @throws IOException
	 */
	public String[] readRow() throws IOException
	{

		ArrayList<String> untrimmed = new ArrayList<>();
		String[] row;

		if (dataLinesRead < data.length)
		{
			row = data[dataLinesRead];
		}
		else
		{
			return null;
		}

		if (row != null)
		{
			for (String s : row)
			{
				untrimmed.add(s);
			}
		}

		String[] trimmed = null;
		if (untrimmed != null)
		{
			++dataLinesRead;

			// If row length is 0, there were no values in the row. We reached
			// the end of the file.
			if (untrimmed.size() == 0)
			{
				return null;
			}
			else if (untrimmed.size() < columnsEnumClass.getEnumConstants().length)
			{
				throw new RuntimeException("Data error - not enough fields (" + untrimmed.size() + " of " + columnsEnumClass.getEnumConstants().length
						+ ") found on line " + dataLinesRead + ": " + String.join(", ", untrimmed));

			}
			else if (untrimmed.size() > columnsEnumClass.getEnumConstants().length)
			{
				throw new RuntimeException("Data error - too many fields (" + untrimmed.size() + " of " + columnsEnumClass.getEnumConstants().length
						+ ") found on line " + dataLinesRead + ": " + String.join(", ", untrimmed));
			}

			trimmed = new String[untrimmed.size()];
			for (int i = 0; i < untrimmed.size(); ++i)
			{
				trimmed[i] = untrimmed.get(i) != null ? untrimmed.get(i).trim() : null;
			}
		}

		return trimmed;
	}

	/**
	 * Reads and returns a row of data from the source XLS file as a Map of
	 * value by header, validating number of columns against
	 * columnsEnumClass.getEnumConstants().length, trimming results and
	 * incrementing dataLinesRead
	 * 
	 * @return the data
	 * @throws IOException
	 */
	public Map<COLUMNS, String> readRowAsMap() throws IOException
	{
		String[] rowAsArray = readRow();
		if (rowAsArray != null)
		{
			Map<COLUMNS, String> rowAsMap = new HashMap<>();
			for (int i = 0; i < columnsEnumClass.getEnumConstants().length; i++)
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
	 * close() the Workbook resource
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		if (workbook != null)
		{
			workbook.close();
		}
	}

	// write the workbook sheet to console for debug if needed.
	@SuppressWarnings("unused")
	private void logSheet()
	{
		int rowIdx = 0;
		ArrayList<String> colValues;
		for (Row row : this.sheet)
		{
			colValues = new ArrayList<String>();
			for (Cell cell : row)
			{
				colValues.add(cell.getStringCellValue());
			}
			ConsoleUtil.println("R:" + (++rowIdx) + "\t" + String.join("|", colValues));
		}
	}

	// write the data to console for debug if needed.
	@SuppressWarnings("unused")
	private void logArray()
	{
		int rowIdx = 0;
		for (String[] row : data)
		{
			ConsoleUtil.println("R:" + (++rowIdx) + "\t" + String.join("|", row));
		}
	}

}
