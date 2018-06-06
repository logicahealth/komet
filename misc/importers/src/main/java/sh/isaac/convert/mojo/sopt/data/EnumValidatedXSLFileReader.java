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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

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
	
	public static EnumValidatedXSLFileReader readZip(File inputFileOrDirectory) throws IOException
	{
		File file = null;
		if (inputFileOrDirectory.isDirectory())
		{
			ArrayList<File> files = new ArrayList<File>();
			for (File f : inputFileOrDirectory.listFiles())
			{
				if (f.isFile() && (f.getName().toLowerCase().endsWith(".zip")))
				{
					files.add(f);
				}
			}

			if (files.size() != 1)
			{
				throw new RuntimeException(files.size() + " zip files were found inside of " + inputFileOrDirectory.getAbsolutePath()
						+ " but this implementation requires 1 and only 1 zip file to be present.");
			}

			file = files.get(0);
		}
		else
		{
			file = inputFileOrDirectory;
		}

		System.out.println("Prepared to process: " + file.getCanonicalPath());

		try (ZipFile zf = new ZipFile(file);)
		{
			Enumeration<? extends ZipEntry> zipEntries = zf.entries();
			InputStream is = null;
			while (zipEntries.hasMoreElements())
			{
				ZipEntry ze = zipEntries.nextElement();
				if (ze.getName().toLowerCase().endsWith(".xls") && ze.getName().toUpperCase().contains("PHDSC"))
				{
					if (is != null)
					{
						throw new RuntimeException("Found multiple xls files inside the zip file that contain 'PHDSC' in their file name.  Expected only 1.");
					}
					else
					{
						is = zf.getInputStream(ze);
					}
				}
			}
	
			if (is == null)
			{
				throw new RuntimeException("Failed to find a xls file inside the zip file that contain 'PHDSC' in the file name.");
			}
	
			return new EnumValidatedXSLFileReader(is);
		}
	}

	public EnumValidatedXSLFileReader(InputStream excelFile) throws IOException
	{
		try
		{
			Workbook workbook = WorkbookFactory.create(excelFile);
			
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
		catch (EncryptedDocumentException | InvalidFormatException | org.apache.poi.openxml4j.exceptions.InvalidFormatException e)
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
