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

import java.io.IOException;
import java.util.Hashtable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * {@link LOINCReader}
 *
 * Abstract class for the required methods of a LOINC reader - we have several, as the format has changed
 * with each release, sometimes requiring a new parser.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class LOINCReader
{
	protected Logger log = LogManager.getLogger();

	protected int fieldCount_ = 0;

	protected Hashtable<String, Integer> fieldMap = new Hashtable<String, Integer>();

	protected Hashtable<Integer, String> fieldMapInverse = new Hashtable<Integer, String>();

	private int supportedVersion = -1;
	private String mapFileName = null;

	public abstract int getDataSize();

	/**
	 * Read line.
	 *
	 * @return the string[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract String[] readLine() throws IOException;

	/**
	 * Gets the field map.
	 *
	 * @return the field map
	 */
	public Hashtable<String, Integer> getFieldMap()
	{
		return this.fieldMap;
	}

	/**
	 * Gets the field map inverse.
	 *
	 * @return the field map inverse
	 */
	public Hashtable<Integer, String> getFieldMapInverse()
	{
		return this.fieldMapInverse;
	}

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	public abstract String[] getHeader();

	/**
	 * Gets the position for column.
	 *
	 * @param col the col
	 * @return the position for column
	 */
	public int getPositionForColumn(String col)
	{
		return this.fieldMap.get(col);
	}

	/**
	 * Gets the release date.
	 *
	 * @return the release date
	 */
	public abstract String getReleaseDate();

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public abstract String getVersion();

	/**
	 * @return Our internal version number that we use to map format changes
	 */
	public int getFormatVersionNumber()
	{
		if (supportedVersion > -1)
		{
			return supportedVersion;
		}
		else
		{
			String version = getVersion();
			if (version.contains("2.36"))
			{
				supportedVersion = 1;
				mapFileName = "classMappings-2.36.txt";
			}
			else if (version.contains("2.38"))
			{
				supportedVersion = 2;
				mapFileName = "classMappings-2.36.txt"; // Yes, wrong one, never made the file for 2.38
			}
			else if (version.contains("2.40"))
			{
				supportedVersion = 3;
				mapFileName = "classMappings-2.40.txt";
			}
			else if (version.contains("2.44"))
			{
				supportedVersion = 4;
				mapFileName = "classMappings-2.44.txt";
			}
			else if (version.contains("2.46"))
			{
				supportedVersion = 4;
				mapFileName = "classMappings-2.46.txt";
			}
			else if (version.contains("2.48"))
			{
				supportedVersion = 4;
				mapFileName = "classMappings-2.48.txt";
			}
			else if (version.contains("2.50"))
			{
				supportedVersion = 5;
				mapFileName = "classMappings-2.52.txt"; // never did a 2.50, skipped to 2.52
			}
			else if (version.contains("2.52"))
			{
				supportedVersion = 6;
				mapFileName = "classMappings-2.52.txt";
			}
			else if (version.contains("2.54"))
			{
				supportedVersion = 7;
				mapFileName = "classMappings-2.54.txt";
			}
			else if (version.contains("2.56"))
			{
				supportedVersion = 7;
				mapFileName = "classMappings-2.56.txt";
			}
			else if (version.contains("2.59") || version.contains("2.63"))
			{
				supportedVersion = 8;
				mapFileName = "classMappings-2.59.txt";
			}
			else if (version.contains("2.65") || version.contains("2.66") || version.contains("2.67"))
			{
				supportedVersion = 9;
				mapFileName = "classMappings-2.66.txt";
			}
			else if (version.contains("2.68"))
			{
				supportedVersion = 10;
				mapFileName = "classMappings-2.68.txt";
			}
			else
			{
				log.error("ERROR: UNTESTED VERSION - NO TESTED PROPERTY MAPPING EXISTS!");
				supportedVersion = 9;
				mapFileName = "classMappings-2.66.txt";
			}
		}
		return supportedVersion;
	}
	
	/**
	 * @return the name for the mapFile that best matches this version
	 */
	public String getMapFileName()
	{
		if (mapFileName == null)
		{
			getFormatVersionNumber();
		}
		return mapFileName;
	}

}
