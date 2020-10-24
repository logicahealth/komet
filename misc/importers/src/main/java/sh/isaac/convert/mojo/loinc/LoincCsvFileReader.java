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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import sh.isaac.api.util.NaturalOrder;

/**
 *
 * {@link LoincCsvFileReader}
 *
 * Reads the CSV formatted release files of LOINC, and the custom release notes file
 * to extract the date and time information.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LoincCsvFileReader extends LOINCReader
{
	private Logger log = LogManager.getLogger();
	private String version = null;
	private String release = null;
	private final TreeMap<String, Long> versionTimeMap = new TreeMap<>(new NaturalOrder());
	private String[] header;
	private LinkedList<String[]> data = new LinkedList<>();
	private int dataSize;

	/**
	 * Instantiates a new loinc csv file reader.
	 *
	 * @param is the is
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws CsvValidationException 
	 */
	public LoincCsvFileReader(InputStream is) throws IOException, CsvValidationException
	{
		// Their new format includes the (optional) UTF-8 BOM, which chokes java for stupid legacy reasons.
		readAllData(new CSVReader(new BufferedReader(new InputStreamReader(new BOMInputStream(is)))), false);
		this.header = readLine();
		//DO not close the reader, because that would close the underlying inputstream which may be coming from a zip file.
	}

	/**
	 * Instantiates a new loinc csv file reader.
	 *
	 * @param path the path to read
	 * @param populateVersionTimeMap the populate version time map
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws CsvValidationException 
	 */
	public LoincCsvFileReader(Path path, boolean populateVersionTimeMap) throws IOException, CsvValidationException
	{
		log.info("Using the data file " + path);

		// Their new format includes the (optional) UTF-8 BOM, which chokes java for stupid legacy reasons.
		readAllData(new CSVReader(new BufferedReader(new InputStreamReader(new BOMInputStream(Files.newInputStream(path, StandardOpenOption.READ))))), true);
		this.header = readLine();
		readReleaseNotes(path.getParent(), populateVersionTimeMap);
	}
	
	private void readAllData(CSVReader reader, boolean closeAtEnd) throws IOException, CsvValidationException
	{
		String[] temp = reader.readNext();

		while (temp != null)
		{
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
				throw new RuntimeException("Data error - to many fields found on line: " + Arrays.toString(temp));
			}
			data.add(temp);
			temp = reader.readNext();
		}
		if (closeAtEnd)
		{
			reader.close();
		}
		dataSize = data.size();
	}

	@Override
	public int getDataSize()
	{
		return dataSize;
	}

	/**
	 * {@inheritDoc}
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
	 * Read release notes.
	 *
	 * @param dataFolderPath the data folder
	 * @param populateVersionTimeMap the populate version time map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("deprecation")
	public void readReleaseNotes(Path dataFolderPath, boolean populateVersionTimeMap) throws IOException
	{
		AtomicReference<Path> relNotes = new AtomicReference<>();

		Files.walk(dataFolderPath, new FileVisitOption[] {}).forEach(path -> {
			if (path.toString().toLowerCase().contains("releasenotes.txt"))
			{
				relNotes.set(path);
			}
		});

		if (relNotes.get() != null)
		{
			final SimpleDateFormat[] sdf = new SimpleDateFormat[] { new SimpleDateFormat("MMM dd, yyyy"), new SimpleDateFormat("MMM yyyy"),
					new SimpleDateFormat("MMMyyyy"), new SimpleDateFormat("MM/dd/yy") };
			try (final BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(relNotes.get(), StandardOpenOption.READ))))
			{
				int lineNo = 1;
				String line = br.readLine();
				boolean first = true;
				String versionCache = null;
				boolean betaVersion = false;

				while (line != null)
				{
					if (line.matches("\\s*\\|\\s*Version [\\w\\.\\-\\(\\)]*\\s*\\|\\s*"))
					{
						final String temp = line.substring(line.indexOf("Version") + "Version ".length());

						versionCache = temp.replace('|', ' ').trim();
						//Our regex doesn't match beta lines
						betaVersion = false;

						if (first)
						{
							this.version = versionCache;
						}
					}
					else if (line.contains("Beta"))
					{
						betaVersion = true;
					}

					if (line.matches("\\s*\\|\\s*Released [\\w\\s/,]*\\|"))
					{
						String temp = line.substring(line.indexOf("Released") + "Released ".length());

						temp = temp.replace('|', ' ').trim();

						if (first)
						{
							this.release = temp;
							first = false;

							if (!populateVersionTimeMap)
							{
								break;
							}
						}

						Long time = -1l;

						for (final SimpleDateFormat f : sdf)
						{
							try
							{
								time = f.parse(temp).getTime();
								break;
							}
							catch (final ParseException e)
							{
								// noop
							}
						}

						if (time < 0)
						{
							throw new IOException("Failed to parse " + temp);
						}

						if (versionCache == null)
						{
							if (betaVersion)
							{
								log.info("Ignoring Beta release for line " + lineNo + ": " + line);
							}
							else
							{
								log.error("No version for line " + lineNo + ": " + line);
							}
						}
						else
						{
							this.versionTimeMap.put(versionCache, time);
						}

						versionCache = null;
					}

					line = br.readLine();
					lineNo++;
				}

				br.close();

				if (populateVersionTimeMap)
				{
					// release notes is missing this one...set it to a time before 2.03.
					if (!this.versionTimeMap.containsKey("2.02"))
					{
						final Date temp = new Date(this.versionTimeMap.get("2.03"));

						temp.setMonth(temp.getMonth() - 1);
						this.versionTimeMap.put("2.02", temp.getTime());
					}

					// Debug codel
					//          ConsoleUtil.println("Release / Time map read from readme file:");
					//          for (Entry<String, Long> x : versionTimeMap.entrySet())
					//          {
					//                  ConsoleUtil.println(x.getKey() + " " + new Date(x.getValue()).toString());
					//          }
				}
			}
		}
		else
		{
			log.error("Couldn't find release notes file - can't read version or release date!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getHeader()
	{
		return this.header;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReleaseDate()
	{
		return this.release;
	}

	/**
	 * Gets the time version map.
	 *
	 * @return the time version map
	 */
	public TreeMap<String, Long> getTimeVersionMap()
	{
		return this.versionTimeMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVersion()
	{
		return this.version;
	}
}