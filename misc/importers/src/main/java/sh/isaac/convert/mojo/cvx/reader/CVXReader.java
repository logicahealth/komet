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

package sh.isaac.convert.mojo.cvx.reader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import sh.isaac.convert.mojo.cvx.data.CVXCodes;
import sh.isaac.convert.mojo.cvx.data.ObjectFactory;
import sh.isaac.converters.sharedUtils.ConsoleUtil;

/**
 * 
 * {@link CVXReader}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class CVXReader
{
	private final File file_;

	public CVXReader(File inputFileOrDirectory) throws IOException
	{
		if (inputFileOrDirectory.isDirectory())
		{
			ArrayList<File> files = new ArrayList<File>();
			for (File f : inputFileOrDirectory.listFiles())
			{
				if (f.isFile() && (f.getName().toLowerCase().endsWith(".xml")))
				{
					files.add(f);
				}
			}

			if (files.size() != 1)
			{
				throw new RuntimeException(files.size() + " xml files were found inside of " + inputFileOrDirectory.getAbsolutePath()
						+ " but this implementation requires 1 and only 1 xml file to be present.");
			}

			file_ = files.get(0);
		}
		else
		{
			file_ = inputFileOrDirectory;
		}

		ConsoleUtil.println("Prepared to process: " + file_.getCanonicalPath());
	}

	public CVXCodes process() throws IOException, JAXBException
	{
		byte[] data = Files.readAllBytes(file_.toPath());
		ByteArrayInputStream xmlContentBytes = new ByteArrayInputStream(data);
		// XMLStreamReader xmlStreamReader = new DomStreamReader(file_);
		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		// note: setting schema to null will turn validator off
		unmarshaller.setSchema(null);
		Object xmlObject = CVXCodes.class.cast(unmarshaller.unmarshal(xmlContentBytes));
		return (CVXCodes) xmlObject;
	}
}
