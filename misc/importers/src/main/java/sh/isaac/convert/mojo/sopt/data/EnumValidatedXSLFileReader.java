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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.CloseIgnoringInputStream;

/**
 * {@link EnumValidatedXSLFileReader}
 *
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 */
public class EnumValidatedXSLFileReader
{
	LinkedHashMap<SOPTValueSetColumnsV1, String> valueSetMetaData = new LinkedHashMap<>();
	SOPTDataColumnsV1[] dataHeaders;
	List<String[]> valueSetData = new ArrayList<>();
	
	public static EnumValidatedXSLFileReader readZip(Path inputFileOrDirectory) throws IOException
	{
		final AtomicReference<Path> zipFile = new AtomicReference<>();
		
		Files.walk(inputFileOrDirectory, new FileVisitOption[] {}).forEach(path ->
		{
			if (path.toString().toLowerCase().endsWith(".zip"))
			{
				if (zipFile.get() != null)
				{
					throw new RuntimeException("Only expected to find one zip file in the folder " + inputFileOrDirectory.normalize());
				}
				zipFile.set(path);
			}
		});

		if (zipFile.get() == null)
		{
			throw new RuntimeException("Did not find a zip file in " + inputFileOrDirectory.normalize());
		}

		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.get(), StandardOpenOption.READ)))
		{
			ZipEntry ze = zis.getNextEntry();
			EnumValidatedXSLFileReader result = null;
			while (ze != null)
			{
				if (ze.getName().toLowerCase().endsWith(".xls") && ze.getName().toUpperCase().contains("PHDSC"))
				{
					if (result != null)
					{
						throw new RuntimeException("Found multiple xls files inside the zip file that contain 'PHDSC' in their file name.  Expected only 1.");
					}
					else
					{
						result = new EnumValidatedXSLFileReader(zis);
					}
				}
				ze = zis.getNextEntry();
			}
	
			if (result == null)
			{
				throw new RuntimeException("Failed to find a xls file inside the zip file that contain 'PHDSC' in the file name.");
			}
	
			return result;
		}
	}

	public EnumValidatedXSLFileReader(InputStream excelFile) throws IOException
	{
		try
		{
			//TODO get the date
			Workbook workbook = WorkbookFactory.create(new CloseIgnoringInputStream(excelFile));
			
			readInfoSheet(workbook.getSheetAt(0));
			
			// data is located in the second dataSheet named
			// PHVS_SourceOfPaymentTypology
			Sheet dataSheet = workbook.getSheetAt(1);

			int cells = dataSheet.getRow(0).getPhysicalNumberOfCells();
			dataHeaders = new SOPTDataColumnsV1[cells];
			
			for (int col = 0; col < dataHeaders.length; col++)
			{
				dataHeaders[col] = SOPTDataColumnsV1.parse(dataSheet.getRow(0).getCell(col).getStringCellValue());
			}
			
			int rows = dataSheet.getLastRowNum() - 1;

			Row row;
			Cell cell;

			for (int r = 1; r < rows; r++)
			{
				row = dataSheet.getRow(r);
				if (row != null)
				{
					String[] columns = new String[dataHeaders.length];
					for (int c = 0; c < cells; c++)
					{
						cell = row.getCell(c);
						if (cell != null)
						{
							columns[c] = cell.getStringCellValue().trim();
						}
					}
					valueSetData.add(columns);
				}
			}

			// sort data by first column which contains hierarchy ids of records
			Collections.sort(valueSetData, (a, b) -> (a[0].compareTo(b[0])));
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * @param sheet
	 */
	private void readInfoSheet(Sheet sheet)
	{
		Row headerRow = sheet.getRow(0);
		
		for (int col = 0; col < headerRow.getPhysicalNumberOfCells(); col++)
		{
			SOPTValueSetColumnsV1 column = SOPTValueSetColumnsV1.parse(headerRow.getCell(col).getStringCellValue());
			valueSetMetaData.put(column, sheet.getRow(1).getCell(col).getStringCellValue());
		}
	}

	/**
	 * @return The metadata from the value sets header tab of the spreadsheet.
	 */
	public LinkedHashMap<SOPTValueSetColumnsV1, String> getValueSetMetaData()
	{
		return valueSetMetaData;
	}

	/**
	 * @return the columns headers of data present in this spreadsheet
	 */
	public SOPTDataColumnsV1[] getDataHeaders()
	{
		return dataHeaders;
	}

	/**
	 * All of the row of the spreadsheet, properly sorted via ascending concept code, with column order matching the spreadsheet 
	 * and the values of {@link #getDataHeaders()}
	 * @return the spreadsheet data
	 */
	public List<String[]> getValueSetData()
	{
		return valueSetData;
	}
}
