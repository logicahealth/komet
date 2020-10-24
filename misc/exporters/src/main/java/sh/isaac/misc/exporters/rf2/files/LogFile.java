/*
 * Copyright 2020 Mind Computing Inc, Sagebits LLC
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

package sh.isaac.misc.exporters.rf2.files;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class LogFile extends RF2File
{
	/**
	 * @param rootFolder
	 * @param releaseType - may be null, to place a file in the root folder
	 * @param fileName
	 * @throws IOException
	 */
	public LogFile(File rootFolder, RF2ReleaseType releaseType, String fileName) throws IOException
	{
		super(rootFolder, releaseType, fileName);
	}
}
