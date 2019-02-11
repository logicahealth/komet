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

package sh.isaac.convert.mojo.loinc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * {@link TxtFileReader}
 *
 * A reader for various txt file formats used by LOINC.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TxtFileReader extends LOINCReader
{
	protected Logger log = LogManager.getLogger();
	private String version;
	private String releaseDate;
	private String headerLine;
	private LinkedList<String[]> data = new LinkedList<>();
	private int dataSize;

	/**
	 * Instantiates a new txt file reader.
	 *
	 * @param path the path to read from
	 * @throws Exception the exception
	 */
	public TxtFileReader(Path path) throws Exception
	{
		log.info("Using the data file " + path);
		try(BufferedReader dataReader = new BufferedReader(new InputStreamReader(Files.newInputStream(path, StandardOpenOption.READ))))
		{

			// Line 1 of the file is version, line 2 is date. Hope they are consistent.....
			this.version = dataReader.readLine();
			this.releaseDate = dataReader.readLine();
	
			// Scan forward in the data file for the "cutoff" point
			int i = 0;
	
			while (true)
			{
				i++;
	
				final String temp = dataReader.readLine();
	
				if (temp.equals("<----Clip Here for Data----->"))
				{
					break;
				}
	
				if (i > 500)
				{
					throw new Exception("Couldn't find '<----Clip Here for Data----->' constant.  Format must have changed.  Failing");
				}
			}
	
			this.headerLine = dataReader.readLine();
			
			String line = dataReader.readLine();
			while (line != null)
			{
				if (line.length() > 0)
				{
					data.add(getFields(line));
				}
				line = dataReader.readLine();
			}
		}
		dataSize = data.size();
	}

	@Override
	public int getDataSize()
	{
		return dataSize;
	}

	/**
	 * @see sh.isaac.convert.mojo.loinc.LOINCReader#readLine()
	 */
	@Override
	public String[] readLine() throws IOException
	{
		if (data.isEmpty())
		{
			return null;
		}
		return data.removeFirst();
	}

	/**
	 * Gets the fields.
	 *
	 * @param line the line
	 * @return the fields
	 */
	private String[] getFields(String line)
	{
		String[] temp = line.split("\\t");

		for (int i = 0; i < temp.length; i++)
		{
			if (temp[i].length() == 0)
			{
				temp[i] = null;
			}
			else if (temp[i].startsWith("\"") && temp[i].endsWith("\""))
			{
				temp[i] = temp[i].substring(1, temp[i].length() - 1);
			}
		}

		if (this.fieldCount_ == 0)
		{
			this.fieldCount_ = temp.length;

			int i = 0;

			for (final String s : temp)
			{
				this.fieldMapInverse.put(i, s);
				this.fieldMap.put(s, i++);
			}
		}
		else if (temp.length < this.fieldCount_)
		{
			temp = Arrays.copyOf(temp, this.fieldCount_);
		}
		else if (temp.length > this.fieldCount_)
		{
			throw new RuntimeException("Data error - to many fields found on line: " + line);
		}

		return temp;
	}

	/**
	 * @see sh.isaac.convert.mojo.loinc.LOINCReader#getHeader()
	 */
	@Override
	public String[] getHeader()
	{
		return getFields(this.headerLine);
	}

	/**
	 * @see sh.isaac.convert.mojo.loinc.LOINCReader#getReleaseDate()
	 */
	@Override
	public String getReleaseDate()
	{
		return this.releaseDate;
	}

	/**
	 * @see sh.isaac.convert.mojo.loinc.LOINCReader#getVersion()
	 */
	@Override
	public String getVersion()
	{
		return this.version;
	}
}
