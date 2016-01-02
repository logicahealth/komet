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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;

/**
 * 
 * {@link Unzip}
 *
 * Trivial unzip utility using java zip support.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Unzip
{
	public final static void unzip(File zipFile, File rootDir) throws IOException
	{
		ZipFile zip = new ZipFile(zipFile);
		@SuppressWarnings("unchecked") Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry entry = entries.nextElement();
			java.io.File f = new java.io.File(rootDir, entry.getName());
			if (entry.isDirectory())
			{
				f.mkdirs();
				continue;
			}
			else
			{
				f.createNewFile();
			}
			InputStream is = null;
			OutputStream os = null;
			try
			{
				is = zip.getInputStream(entry);
				os = new FileOutputStream(f);
				IOUtils.copy(is, os);
			}
			finally
			{
				if (is != null)
				{
					try
					{
						is.close();
					}
					catch (Exception e)
					{
						// noop
					}
				}
				if (os != null)
				{
					try
					{
						os.close();
					}
					catch (Exception e)
					{
						// noop
					}
				}
			}
		}
		zip.close();
	}
}
