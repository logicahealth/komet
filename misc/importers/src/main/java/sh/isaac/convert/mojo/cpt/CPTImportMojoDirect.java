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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
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
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * 
 * {@link CPTImportMojoDirect}
 * 
 * Goal which converts CPT data into the workbench jbin format
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Mojo(name = "convert-CPT-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
//This implements DirectConverter, but isn't a service nor a singleton due to maven dependencies getting cranky.  See the extension of this class
//for the HK2 pattern.
public class CPTImportMojoDirect extends DirectConverterBaseMojo implements DirectConverter
{
	/**
	 * This constructor is for maven and HK2 and should not be used at runtime.
	 */
	public CPTImportMojoDirect()
	{
		
	}
	
	/**
	 * A constructor for runtime usage
	 * @param outputDirectory - optional - if provided, debug info will be written here
	 * @param inputFolder - the folder to search for the source file
	 * @param converterSourceArtifactVersion - the version number of the source file being passed in
	 * @param stampCoordinate - the coordinate to use for readback in cases where content merges into existing content
	 */
	public CPTImportMojoDirect(File outputDirectory, File inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate)
	{
		this();
		configure(outputDirectory, inputFolder, converterSourceArtifactVersion, stampCoordinate);
	}
	
	/**
	 * If this was constructed via HK2, then you must call the configure method prior to calling {@link #convertContent()}
	 * If this was constructed via the constructor that takes parameters, you do not need to call this.
	 * 
	 * @see sh.isaac.convert.directUtils.DirectConverter#configure(java.io.File, java.io.File, java.lang.String, sh.isaac.api.coordinate.StampCoordinate)
	 */
	@Override
	public void configure(File outputDirectory, File inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate)
	{
		this.outputDirectory = outputDirectory;
		this.inputFileLocation = inputFolder;
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
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent()
	 * @see DirectConverter#convertContent()
	 */
	@Override
	public void convertContent() throws IOException 
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

		File zipFile = null;
		for (File f : inputFileLocation.listFiles())
		{
			if (f.getName().toLowerCase().endsWith(".zip"))
			{
				if (zipFile != null)
				{
					throw new RuntimeException("Only expected to find one zip file in the folder " + inputFileLocation.getCanonicalPath());
				}
				zipFile = f;
			}
		}

		if (zipFile == null)
		{
			throw new RuntimeException("Did not find a zip file in " + inputFileLocation.getCanonicalPath());
		}

		ZipFile zf = new ZipFile(zipFile);

		HashMap<String, CPTData> data = new HashMap<>();

		log.info("Reading LONGULT.txt");
		int read1 = TextReader.read(zf.getInputStream(zf.getEntry("LONGULT.txt")), data, CPTFileType.LONGULT);
		log.info("Reading MEDU.txt");
		int read2 = TextReader.read(zf.getInputStream(zf.getEntry("MEDU.txt")), data, CPTFileType.MEDU);
		log.info("Reading SHORTU.txt");
		int read3 = TextReader.read(zf.getInputStream(zf.getEntry("SHORTU.txt")), data, CPTFileType.SHORTU);

		zf.close();

		if (read1 != read2 || read1 != read3)
		{
			throw new RuntimeException("Didn't find the same number of codes in all 3 files!");
		}
		
		//Right now, we are configured for the CPT grouping modules nid
		dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.CPT_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID, "CPT");
		
		setupModule("CPT", MetaData.CPT_MODULES____SOLOR.getPrimordialUuid(), contentTime);
		
		//Set up our metadata hierarchy
		dwh.makeMetadataHierarchy(true, true, true, false, true, false, contentTime);

		dwh.makeDescriptionTypeConcept("LONGULT", "Long Description Upper/Lower Case",
				MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), contentTime);
		
		dwh.makeDescriptionTypeConcept("MEDU", "Medium Description Upper Case",
				MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), contentTime);
		
		dwh.makeDescriptionTypeConcept("SHORTU", "Short Description Upper Case",
				MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), contentTime);
		
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, contentTime, readbackCoordinate);

		// Every time concept created add membership to "All CPT Concepts"
		UUID allCPTConceptsRefset = dwh.makeRefsetTypeConcept("All CPT Concepts", null, contentTime);

		// Create CPT root concept under SOLOR_CONCEPT____SOLOR
		final UUID cptRootConcept = dwh.makeConceptEnNoDialect("CPT", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
				new UUID[] {MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()}, Status.ACTIVE, contentTime);

		log.info("Metadata load stats");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}
		
		dwh.clearLoadStats();

		String firstThree = "";
		ComponentReference parent = null;
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
				parent = ComponentReference.fromConcept(dwh.makeConceptEnNoDialect(firstThree + "--", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						new UUID[] {cptRootConcept}, Status.ACTIVE, contentTime));
				dwh.makeDescriptionEnNoDialect(parent.getPrimordialUuid(), "Grouping concept for all codes that start with " + firstThree, 
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
			}
			
			UUID concept = dwh.makeConcept(converterUUID.createNamespaceUUIDFromString(d.code), Status.ACTIVE, contentTime);

			dwh.makeParentGraph(concept, parent.getPrimordialUuid(), Status.ACTIVE, contentTime);
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
		log.info("Dumping UUID Debug File");
		if (outputDirectory != null)
		{
			converterUUID.dump(outputDirectory, "cptUuid");
		}
		converterUUID.clearCache();		
	}
}