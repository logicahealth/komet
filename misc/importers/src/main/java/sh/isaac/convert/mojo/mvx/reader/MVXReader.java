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

package sh.isaac.convert.mojo.mvx.reader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import sh.isaac.convert.mojo.mvx.data.MVXCodes;
import sh.isaac.convert.mojo.mvx.data.ObjectFactory;

/**
 * 
 * {@link MVXReader}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class MVXReader
{
	private final AtomicReference<Path> file_ = new AtomicReference<>();

	public MVXReader(Path inputFileOrDirectory) throws IOException
	{
		Files.walk(inputFileOrDirectory, new FileVisitOption[] {}).forEach(path ->
		{
			if (path.toString().toLowerCase().endsWith(".xml"))
			{
				if (file_.get() != null)
				{
					throw new RuntimeException("Only expected to find one xml file in the folder " + inputFileOrDirectory.normalize());
				}
				file_.set(path);
			}
		});

		if (file_.get() == null)
		{
			throw new IOException("Failed to locate the xml file in " + inputFileOrDirectory);
		}
		LogManager.getLogger().info("Prepared to process: " + file_.get());
	}

	public MVXCodes process() throws IOException, JAXBException
	{
		byte[] data = Files.readAllBytes(file_.get());
		ByteArrayInputStream xmlContentBytes = new ByteArrayInputStream(data);
		// XMLStreamReader xmlStreamReader = new DomStreamReader(file_);
		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		// note: setting schema to null will turn validator off
		unmarshaller.setSchema(null);
		Object xmlObject = MVXCodes.class.cast(unmarshaller.unmarshal(xmlContentBytes));
		return (MVXCodes) xmlObject;
	}
}