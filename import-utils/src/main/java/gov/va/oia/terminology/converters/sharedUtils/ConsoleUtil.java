/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.oia.terminology.converters.sharedUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 
 * {@link ConsoleUtil}
 * 
 * Utility code for writing to the console in a more intelligent way, including detecting running without a real console, 
 * and changing the behavior as appropriate 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConsoleUtil
{
	private static int lastStatus;
	private static boolean progressLine = false;
	private static int printsSinceReturn = 0;
	private static boolean progressLineUsed = false;
	private static StringBuilder consoleOutputCache = new StringBuilder();
	private static String eol = System.getProperty("line.separator");

	public static boolean disableFancy = (System.console() == null);

	public static void showProgress()
	{
		char c;
		switch (lastStatus)
		{
			case 0:
				c = '/';
				break;
			case 1:
				c = '-';
				break;
			case 2:
				c = '\\';
				break;
			case 3:
				c = '|';
				break;

			default:  // shouldn't be used
				c = '-';
				break;
		}
		lastStatus++;
		if (lastStatus > 3)
		{
			lastStatus = 0;
		}

		if (!progressLine)
		{
			System.out.println();
			printsSinceReturn = 0;
		}
		if (disableFancy)
		{
			System.out.print(".");
			printsSinceReturn++;
			if (printsSinceReturn >= 75)
			{
				System.out.println();
				printsSinceReturn = 0;
			}
		}
		else
		{
			System.out.print("\r" + c);
		}
		progressLine = true;
		progressLineUsed = true;
	}

	public static void print(String string)
	{
		if (progressLine)
		{
			if (disableFancy)
			{
				if (progressLineUsed)
				{
					System.out.println();
					printsSinceReturn = 0;
				}
			}
			else
			{
				System.out.print("\r \r");
			}
			progressLine = false;
		}
		System.out.print(string);
		consoleOutputCache.append(string);
	}

	public static void println(String string)
	{
		if (progressLine)
		{
			if (disableFancy)
			{
				if (progressLineUsed)
				{
					System.out.println();
					printsSinceReturn = 0;
				}
			}
			else
			{
				System.out.print("\r \r");
			}
		}
		System.out.println(string);
		consoleOutputCache.append(string);
		consoleOutputCache.append(eol);
		progressLine = true;
		progressLineUsed = false;
	}

	public static void printErrorln(String string)
	{
		if (progressLine)
		{
			if (disableFancy)
			{
				if (progressLineUsed)
				{
					System.out.println();
					printsSinceReturn = 0;
				}
			}
			else
			{
				System.out.print("\r \r");
			}
			progressLine = false;
		}
		System.err.println(string);
		consoleOutputCache.append("ERROR->");
		consoleOutputCache.append(string);
		consoleOutputCache.append(eol);
		printsSinceReturn = 0;
		progressLine = true;
		progressLineUsed = false;
	}

	public static void writeOutputToFile(Path path) throws IOException
	{
		BufferedWriter bw = Files.newBufferedWriter(path, Charset.forName("UTF-8"), new OpenOption[] {StandardOpenOption.CREATE});
		bw.append(consoleOutputCache.toString());
		bw.close();
	}
}