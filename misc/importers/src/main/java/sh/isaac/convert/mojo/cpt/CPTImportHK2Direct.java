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
package sh.isaac.convert.mojo.cpt;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.mojo.cpt.TextReader.CPTFileType;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * {@link CPTImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class CPTImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	/**
	 * This constructor is for HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link #configure(File, Path, String, StampCoordinate)} method on it.
	 */
	protected CPTImportHK2Direct()
	{
		//for HK2 and the maven extension class
	}
	
	@Override
	public ConverterOptionParam[] getConverterOptions()
	{
		return new ConverterOptionParam[] {};
	}

	@Override
	public void setConverterOption(String internalName, String... values)
	{
		//noop, we don't require any.
	}

	/**
	 * If this was constructed via HK2, then you must call the configure method prior to calling {@link #convertContent()}
	 * If this was constructed via the constructor that takes parameters, you do not need to call this.
	 * 
	 * @see sh.isaac.convert.directUtils.DirectConverter#configure(java.io.File, java.io.File, java.lang.String, sh.isaac.api.coordinate.StampCoordinate)
	 */
	@Override
	public void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate)
	{
		this.outputDirectory = outputDirectory;
		this.inputFileLocationPath = inputFolder;
		this.converterSourceArtifactVersion = converterSourceArtifactVersion;
		this.converterUUID = new ConverterUUID(UuidT5Generator.PATH_ID_FROM_FS_DESC, false);
		this.readbackCoordinate = stampCoordinate == null ? StampCoordinates.getDevelopmentLatest() : stampCoordinate;
	}
	
	@Override
	public SupportedConverterTypes[] getSupportedTypes()
	{
		return new SupportedConverterTypes[] {SupportedConverterTypes.CPT};
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer, BiConsumer))
	 * @see DirectConverter#convertContent(Consumer, BiConsumer))
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdates) throws IOException 
	{
		long contentTime;
		try
		{
			Date date = new SimpleDateFormat("yyyy").parse(converterSourceArtifactVersion);
			contentTime = date.getTime();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to parse year from " + converterSourceArtifactVersion);
		}

		final AtomicReference<Path> zipFile = new AtomicReference<>();
		
		Files.walk(inputFileLocationPath, new FileVisitOption[] {}).forEach(path ->
		{
			if (path.toString().toLowerCase().endsWith(".zip"))
			{
				if (zipFile.get() != null)
				{
					throw new RuntimeException("Only expected to find one zip file in the folder " + inputFileLocationPath.normalize());
				}
				zipFile.set(path);
			}
		});

		if (zipFile.get() == null)
		{
			throw new RuntimeException("Did not find a zip file in " + inputFileLocationPath.normalize());
		}
		HashMap<String, CPTData> data = new HashMap<>();
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.get(), StandardOpenOption.READ)))
		{
			ZipEntry ze = zis.getNextEntry();
			int read1 = 0;
			int read2 = 0;
			int read3 = 0;
			while (ze != null)
			{
				if (ze.getName().equals("LONGULT.txt"))
				{
					log.info("Reading LONGULT.txt");
					statusUpdates.accept("Reading LONGULT.txt");
					read1 = TextReader.read(zis, data, CPTFileType.LONGULT);
				}
				else if (ze.getName().equals("SHORTU.txt"))
				{
					log.info("Reading SHORTU.txt");
					statusUpdates.accept("Reading SHORTU.txt");
					read2 = TextReader.read(zis, data, CPTFileType.SHORTU);
				}
				else if (ze.getName().equals("MEDU.txt"))
				{
					log.info("Reading MEDU.txt");
					statusUpdates.accept("Reading MEDU.txt");
					read3 = TextReader.read(zis, data, CPTFileType.MEDU);
				}
				if (read1 > 0 && read2 > 0 && read3 > 0)
				{
					break;
				}
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			if (read1 != read2 || read1 != read3)
			{
				throw new RuntimeException("Didn't find the same number of codes in all 3 files!");
			}
		}

		statusUpdates.accept("Setting up metadata");
		
		//Right now, we are configured for the CPT grouping modules nid
		dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.CPT_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID, 
				"CPT", false);
		
		setupModule("CPT", MetaData.CPT_MODULES____SOLOR.getPrimordialUuid(), Optional.of("http://www.ama-assn.org/go/cpt"), contentTime);
		
		//Set up our metadata hierarchy
		dwh.makeMetadataHierarchy(true, true, true, false, true, false, contentTime);

		dwh.makeDescriptionTypeConcept(null, "LONGULT", null, "Long Description Upper/Lower Case",
				MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		dwh.makeDescriptionTypeConcept(null, "MEDU", null, "Medium Description Upper Case",
				MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		dwh.makeDescriptionTypeConcept(null, "SHORTU", null, "Short Description Upper Case",
				MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, contentTime, readbackCoordinate);

		// Every time concept created add membership to "All CPT Concepts"
		UUID allCPTConceptsRefset = dwh.makeRefsetTypeConcept(null, "All CPT Concepts", null, null, contentTime);

		// Create CPT root concept under SOLOR_CONCEPT____SOLOR
		final UUID cptRootConcept = dwh.makeConceptEnNoDialect(null, "CPT", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
				new UUID[] {MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()}, Status.ACTIVE, contentTime);

		log.info("Metadata load stats");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}
		
		dwh.clearLoadStats();
		
		statusUpdates.accept("Loading content");

		String firstThree = "";
		UUID parent = null;
		int cptConCount = 0;
		int groupingConCount = 0;

		List<CPTData> sorted = new ArrayList<>(data.values());
		Collections.sort(sorted, new Comparator<CPTData>()
		{
			@Override
			public int compare(CPTData o1, CPTData o2)
			{
				int left = Integer.parseInt(o1.code.substring(0, 3));
				int right = Integer.parseInt(o2.code.substring(0, 3));
				return Integer.compare(left, right);
			}
		});

		for (CPTData d : sorted)
		{
			String temp = d.code.substring(0, 3);
			if (!temp.equals(firstThree))
			{
				// Make a new grouping concept
				firstThree = d.code.substring(0, 3);
				parent = dwh.makeConceptEnNoDialect(null, firstThree + "--", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						new UUID[] {cptRootConcept}, Status.ACTIVE, contentTime);
				dwh.makeDescriptionEnNoDialect(parent, "Grouping concept for all codes that start with " + firstThree, 
						MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, contentTime);
				groupingConCount++;
			}
			cptConCount++;
			if (cptConCount % 100 == 0)
			{
				showProgress();
			}
			if (cptConCount % 1000 == 0)
			{
				advanceProgressLine();
				log.info("Processed " + cptConCount + " concepts");
				statusUpdates.accept("Processed " + cptConCount + " concepts");
			}
			
			UUID concept = dwh.makeConcept(converterUUID.createNamespaceUUIDFromString(d.code), Status.ACTIVE, contentTime);

			dwh.makeParentGraph(concept, parent, Status.ACTIVE, contentTime);
			dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), concept, d.code, contentTime);

			dwh.makeDynamicRefsetMember(allCPTConceptsRefset, concept, contentTime);

			if (StringUtils.isNotBlank(d.shortu))
			{
				dwh.makeDescriptionEnNoDialect(concept, d.shortu, dwh.getDescriptionType("SHORTU"), Status.ACTIVE, contentTime);
			}
			if (StringUtils.isNotBlank(d.longult))
			{
				dwh.makeDescriptionEnNoDialect(concept, d.longult, dwh.getDescriptionType("LONGULT"), Status.ACTIVE, contentTime);
			}
			if (StringUtils.isNotBlank(d.medu))
			{
				dwh.makeDescriptionEnNoDialect(concept, d.medu, dwh.getDescriptionType("MEDU"), Status.ACTIVE, contentTime);
			}
		}
		
		dwh.processTaxonomyUpdates();
		Get.taxonomyService().notifyTaxonomyListenersToRefresh();

		advanceProgressLine();
		log.info("Load Statistics");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}

		log.info("Loaded " + cptConCount + " CPT Concepts");
		log.info("Created " + groupingConCount + " Grouping Concepts");

		// this could be removed from final release. Just added to help debug editor problems.
		if (outputDirectory != null)
		{
			log.info("Dumping UUID Debug File");
			converterUUID.dump(outputDirectory, "cptUuid");
		}
		converterUUID.clearCache();
	}
}