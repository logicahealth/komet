/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
package sh.isaac.convert.mojo.cpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.apache.commons.io.input.BOMInputStream;
import com.opencsv.CSVReader;
import sh.isaac.converters.sharedUtils.ConsoleUtil;

/**
 * {@link TextReader}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TextReader
{
	public enum CPTFileType
	{
		LONGULT, MEDU, SHORTU
	}

	public static int read(InputStream input, HashMap<String, CPTData> dataHolder, CPTFileType type) throws IOException
	{
		AtomicBoolean dataStarted = new AtomicBoolean(false);
		AtomicInteger lineCount = new AtomicInteger(0);
		AtomicInteger dataLineCount = new AtomicInteger(0);
		try
		{
			Consumer<String[]> processor = lineData -> {
				lineCount.getAndIncrement();
				if (!dataStarted.get())
				{
					if (lineCount.get() > 50)
					{
						throw new RuntimeException("Not finding data in file");
					}
					else if (lineData.length == 2)
					{
						// found the first line of data
						dataStarted.set(true);
					}
				}

				if (dataStarted.get())
				{
					dataLineCount.getAndIncrement();
					if (lineData.length != 2 && lineData.length != 0)
					{
						throw new RuntimeException("Unexpected line length: " + Arrays.toString(lineData) + " on line " + lineCount.get());
					}
					CPTData cpt = dataHolder.get(lineData[0].trim());
					if (cpt == null)
					{
						cpt = new CPTData(lineData[0].trim());
						dataHolder.put(cpt.code, cpt);
					}

					switch (type)
					{
						case LONGULT:
							cpt.longult = lineData[1].trim();
							break;
						case MEDU:
							cpt.medu = lineData[1].trim();
							break;
						case SHORTU:
							cpt.shortu = lineData[1].trim();
							break;
						default :
							throw new RuntimeException("missed case");
					}
				}
			};

			switch (type)
			{
				case LONGULT:
					try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new BOMInputStream(input))), '\t'))
					{
						reader.forEach(processor);
					}
					break;
				case MEDU:
				case SHORTU:
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(input), StandardCharsets.UTF_8)))
					{
						reader.lines().forEach(line -> {
							if (line.matches("\\d{3}\\w{2} .*"))
							{
								processor.accept(new String[] { line.substring(0, 5), line.substring(6, line.length()) });
							}
							else
							{
								processor.accept(new String[] { line });  // header info, pass anyway, so counts are right
							}
						});
					}
					break;
				default :
					throw new RuntimeException("Missing case!");
			}
		}
		finally
		{
			input.close();
		}
		ConsoleUtil.println("Read " + lineCount.get() + " lines with " + dataLineCount.get() + " cpt codes");
		return dataLineCount.get();
	}
}
