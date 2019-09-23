/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.mojo;

import java.lang.reflect.Field;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * Nasy hack code to deal with complications of logging when running in maven...
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class SLF4jUtils
{
	private static org.apache.logging.log4j.Logger log = LogManager.getLogger();
	private static void futzSLF4JLogging(String loggerName)
	{
		// xodus uses slf4j API for logging, as does maven. Maven uses the a hacked version of SimpleLogger
		// from the slf4j implementation by default. Our plugins mostly use log4j, which
		// allows us to configure them. But the xodus logging seems to ignore the re-route to log4j
		// library, and just logs directly to the MavenSimpleLogger, which we can't configure.
		// And its really noisy. So, this hack is to quiet it down....
		try
		{
			Logger l = LoggerFactory.getLogger(loggerName);  // This is actually a MavenSimpleLogger, but due to various classloader issues, can't work with the directly.
			Field f = l.getClass().getSuperclass().getDeclaredField("currentLogLevel");
			f.setAccessible(true);
			f.set(l, LocationAwareLogger.WARN_INT);
		}
		catch (Exception e)
		{
			log.trace("Failed to reset the log level of " + loggerName + ", it will continue being noisy.");
		}
	}
	
	public static void quietDownXodus()
	{
		futzSLF4JLogging("jetbrains.exodus.gc.GarbageCollector");
		futzSLF4JLogging("jetbrains.exodus.io.FileDataReader");
		futzSLF4JLogging("jetbrains.exodus.env");
	}
}
