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
package sh.isaac.convert.directUtils;

import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Utility methods relates to logging when running in direct mode inside a mojo...
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class LoggingConfig
{
	/**
	 * Configure Log4j on the fly to write to the desired output file.
	 * @param outputDirectory
	 * @param converterOutputArtifactClassifier
	 */
	public static void configureLogging(File outputDirectory, String converterOutputArtifactClassifier)
	{
		LoggerContext lc = (LoggerContext) LogManager.getContext(false);
		Appender a = lc.getConfiguration().getAppender("mylogger");
		((org.apache.logging.log4j.core.config.AbstractConfiguration)lc.getConfiguration()).removeAppender("mylogger");  //Clean up from a previous run
		lc.updateLoggers();
		if (a != null) 
		{
			((org.apache.logging.log4j.core.Logger)lc.getRootLogger()).removeAppender(a);
		}
		
		FileAppender fa = FileAppender.newBuilder().withName("mylogger").withAppend(false)
				.withFileName(new File(outputDirectory, 
						(StringUtils.isBlank(converterOutputArtifactClassifier) ? "" : converterOutputArtifactClassifier + "-") + "ConsoleOutput.txt").toString())
				.withLayout(PatternLayout.newBuilder().withPattern("%-5p %d  [%t] %C{2} (%F:%L) - %m%n").build())
				.setConfiguration(lc.getConfiguration()).build();
		fa.start();
		lc.getConfiguration().addAppender(fa);
		lc.getRootLogger().addAppender(lc.getConfiguration().getAppender(fa.getName()));
		lc.updateLoggers();
	}
}
