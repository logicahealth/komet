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

package sh.isaac.convert.mojo.loinc.techPreview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.input.BOMInputStream;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * The Class LoincExpressionReader.
 */
public class LoincExpressionReader
{
	private int fieldCount = 0;

	private Hashtable<String, Integer> fieldMap = new Hashtable<String, Integer>();

	private Hashtable<Integer, String> fieldMapInverse = new Hashtable<Integer, String>();

	private String[] header;
	private LinkedList<String[]> data = new LinkedList<>();

	/**
	 * Instantiates a new loinc expression reader.
	 *
	 * @param zipFile the zip file
	 * @throws ZipException the zip exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public LoincExpressionReader(Path zipPath) throws ZipException, IOException
	{
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath, StandardOpenOption.READ)))
		{
			ZipEntry ze = zis.getNextEntry();
			boolean found = false;
	
			while (ze != null)
			{
				if (ze.getName().toLowerCase().contains("xder2_sscccRefset_LOINCExpressionAssociationFull".toLowerCase()))
				{
					found = true;
					readData(zis);
					break;
				}
				ze = zis.getNextEntry();
			}
	
			if (!found)
			{
				throw new IOException("Unable to find expression refset file with the pattern 'xder2_sscccRefset_LOINCExpressionAssociationFull' in the zip file "
						+ zipPath);
			}
		}
	}

	/**
	 * Instantiates a new loinc expression reader.
	 *
	 * @param is the is
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public LoincExpressionReader(InputStream is) throws IOException
	{
		readData(is);
	}

	/**
	 * Read line.
	 *
	 * @return the string[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String[] readLine() throws IOException
	{
		if (data.isEmpty())
		{
			return null;
		}
		return data.removeFirst();
	}

	/**
	 * read the data, buffer it for later.
	 *
	 * @param is the is
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void readData(InputStream is) throws IOException
	{
		CSVReader reader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(new BOMInputStream(is))))
				.withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
		String[] temp = reader.readNext();

		while (temp != null)
		{
			if (this.fieldCount == 0)
			{
				this.fieldCount = temp.length;

				int i = 0;

				for (final String s : temp)
				{
					this.fieldMapInverse.put(i, s.toLowerCase());
					this.fieldMap.put(s.toLowerCase(), i++);
				}
			}
			else if (temp.length < this.fieldCount)
			{
				temp = Arrays.copyOf(temp, this.fieldCount);
			}
			else if (temp.length > this.fieldCount)
			{
				throw new RuntimeException("Data error - to many fields found on line: " + Arrays.toString(temp));
			}
			if (header == null)
			{
				this.header = temp;
			}
			else
			{
				data.add(temp);
			}
			temp = reader.readNext();
		}
	}

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	public String[] getHeader()
	{
		return this.header;
	}

	/**
	 * Gets the position for column.
	 *
	 * @param col the col
	 * @return the position for column
	 */
	public int getPositionForColumn(String col)
	{
		return this.fieldMap.get(col.toLowerCase());
	}
}
