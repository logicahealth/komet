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

package sh.isaac.convert.mojo.fhir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.CloseIgnoringInputStream;
import fhir.Bundle;
import fhir.CodeSystem;
import fhir.ObjectFactory;
import fhir.ValueSet;


/**
 * 
 * @author darmbrust
 *
 */
public class FHIRReader
{
	private Logger log = LogManager.getLogger();
	protected ArrayList<Bundle> bundles = new ArrayList<>();;
	protected ArrayList<CodeSystem> codeSystems = new ArrayList<>();;
	protected ArrayList<ValueSet> valueSets = new ArrayList<>();;
	

	public FHIRReader(Path inputFileOrDirectory) throws IOException
	{
		Files.walk(inputFileOrDirectory, new FileVisitOption[] {}).forEach(path ->
		{
			try
			{
				if (!Files.isDirectory(path)) {
					if (path.toString().toLowerCase().endsWith(".zip"))
					{
						InputStream is = Files.newInputStream(path); 
						processZip(is);
						is.close();
					}
					else if (path.toString().toLowerCase().endsWith(".xml"))
					{
						InputStream is = Files.newInputStream(path);
						log.debug("Deserializing {}", path);
						deserialize(is);
						is.close();
					}
					else
					{
						log.debug("Ignoring {}", path.toString());
					}
				}
			}
			catch (Exception e)
			{
				log.error("Error processing {}", path,  e);
			}
		});

		log.info("Read {} bundles, {} CodeSystems, and {} ValueSets" , bundles.size(), codeSystems.size(), valueSets.size());
	}
	
	private void processZip(InputStream zipStream) throws IOException, JAXBException
	{
		try (ZipInputStream zis = new ZipInputStream(new CloseIgnoringInputStream(zipStream)))
		{
			ZipEntry ze = zis.getNextEntry();
			while (ze != null)
			{
				if (!ze.isDirectory())
				{
					if (ze.getName().toLowerCase().endsWith(".xml"))
					{
						log.debug("Deserializing {}", ze.getName());
						deserialize(zis);
					}
					else if (ze.getName().toLowerCase().endsWith(".zip"))
					{
						//recurse
						processZip(zis);
					}
					else
					{
						log.debug("Ignoring {} in zip", ze.getName());
					}
				}
				ze = zis.getNextEntry();
			}
		}
	}

	private void deserialize(InputStream is) throws IOException, JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		// note: setting schema to null will turn validator off
		unmarshaller.setSchema(null);
		Object xmlObject = unmarshaller.unmarshal(new CloseIgnoringInputStream(is));
		if (xmlObject instanceof JAXBElement<?>)
		{
			if (((JAXBElement<?>)xmlObject).getDeclaredType().equals(Bundle.class))
			{
				bundles.add((Bundle)((JAXBElement<?>)xmlObject).getValue());
			}
			else if(((JAXBElement<?>)xmlObject).getDeclaredType().equals(CodeSystem.class))
			{
				codeSystems.add((CodeSystem)((JAXBElement<?>)xmlObject).getValue());
			}
			else if (((JAXBElement<?>)xmlObject).getDeclaredType().equals(ValueSet.class))
			{
				valueSets.add((ValueSet)((JAXBElement<?>)xmlObject).getValue());
			}
			else 
			{
				log.warn("Unsupported content type: {}.  Ignored", ((JAXBElement<?>)xmlObject).getDeclaredType());
			}
		}
		else 
		{
			log.warn("Unsupported content type: {}.  Ignored", xmlObject.getClass());
		}
	}
}
