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
package sh.isaac.convert.mojo.icd10;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
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
import sh.isaac.convert.mojo.icd10.data.ICD10;
import sh.isaac.convert.mojo.icd10.reader.ICD10Reader;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * {@link ICD10ImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class ICD10ImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	private UUID icdRootConcept, HIPPA_Valid, allICDConceptsRefset;
	private long contentTime;
	private Consumer<String> statusUpdates;

	private int conceptCount = 0;
	Map<String, UUID> codeToUuid = new HashMap<>();

	/**
	 * Converter source type - should be an ICD 10 variation such as 'PCS' or 'CM'
	 */
	@Parameter(required = true)
	protected String sourceType;
	
	/**
	 * This constructor is for maven and HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link #configure(File, Path, String, StampCoordinate)} method on it.
	 */
	protected ICD10ImportHK2Direct()
	{
		//For HK2 / maven
	}
	
	@Override
	public ConverterOptionParam[] getConverterOptions()
	{
		return new ICD10ConfigOptions().getConfigOptions();
	}

	@Override
	public void setConverterOption(String internalName, String... values)
	{
		if (internalName.equals(getConverterOptions()[0].getInternalName()))
		{
			this.sourceType = values[0];
		}
		else
		{
			throw new RuntimeException("Unsupported converter option: " + internalName);
		}
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
		return new SupportedConverterTypes[] {SupportedConverterTypes.ICD10_CM, SupportedConverterTypes.ICD10_PCS};
	}
	
	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer, BiConsumer))
	 * @see DirectConverter#convertContent(Consumer, BiConsumer))
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdate) throws IOException 
	{
		this.statusUpdates = statusUpdates;
		
		try
		{
			contentTime = new SimpleDateFormat("yyyy").parse(converterSourceArtifactVersion).getTime();
		}
		catch (Exception e)
		{
			throw new IOException("Could not determine date from source Artifact Version - expected the version to be a 4 digit year.");
		}
		log.info("Codeset date: [" + new Date(contentTime) + "]");

		if (StringUtils.isEmpty(sourceType))
		{
			throw new IOException("sourceType must be set to a value such as 'CM' or 'PCS'");
		}
		
		String termName = "ICD-10-" + sourceType.toUpperCase();
		
		statusUpdates.accept("Setting up metadata for " + termName);
		log.info("Setting up metadata for " + termName);
		
		//Right now, we are configured for the CPT grouping modules nid
		dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.ICD10_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID, 
				termName, false);

		setupModule(termName, MetaData.ICD10_MODULES____SOLOR.getPrimordialUuid(), Optional.of("http://hl7.org/fhir/sid/icd-10-" + sourceType), contentTime);

		// Normally, the importer configures this to the parent ICD10 modules UUID - but then we get duplicates generated between CM and PCS.
		// Need to use a different namespace for each.
		converterUUID.configureNamespace(Get.identifierService().getUuidPrimordialForNid(dwh.getModuleNid()));
		
		//Set up our metadata hierarchy
		dwh.makeMetadataHierarchy(true, true, true, false, true, false, contentTime);

		dwh.makeDescriptionTypeConcept(null, "Short Description", null, null,
				MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		dwh.makeDescriptionTypeConcept(null, "Long Description", null, null,
				MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		dwh.makeAttributeTypeConcept(null, "ICD-10 Order Number", null, null, true, null, null, contentTime);
		
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, contentTime, readbackCoordinate);

		// Every time concept created add membership to "All CPT Concepts"
		allICDConceptsRefset = dwh.makeRefsetTypeConcept(null, "All " + termName + " Concepts", null, null, contentTime);
		HIPPA_Valid = dwh.makeRefsetTypeConcept(null, "HIPAA Valid", null, null, contentTime);

		// Create CPT root concept under SOLOR_CONCEPT____SOLOR
		icdRootConcept = dwh.makeConceptEnNoDialect(null, termName, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
				new UUID[] {MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()}, Status.ACTIVE, contentTime);

		log.info("Metadata load stats");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}
		
		dwh.clearLoadStats();
		
		statusUpdates.accept("Loading content");
		log.info("Begin code parsing.");

		// Also reads in codes from file
		final ICD10Reader importer = new ICD10Reader(inputFileLocationPath, converterSourceArtifactVersion);

		log.info("Read terminology containing " + importer.getAllCodesCount() + " entries");

		/*
		 * 1: Order number
		 * 2: Code (alpha-numeric, 3-7 characters)
		 * 3: Header (non-HIPAA value) => Subset
		 * 4: Short description (FSN?)
		 * 5: Long description (preferred term?)
		 */

		// Process headers first to get parent values for concepts
		importer.getIntermediateHeaderConcepts().forEach(icd10Item -> process(icd10Item));

		// Process leaf concepts
		importer.getLeafConcepts().forEach(icd10Item -> process(icd10Item));

		if (importer.getAllCodesCount() != conceptCount)
		{
			throw new IOException("Size mismatch!");
		}
		
		dwh.processTaxonomyUpdates();
		Get.taxonomyService().notifyTaxonomyListenersToRefresh();

		advanceProgressLine();
		statusUpdates.accept("Processed " + conceptCount + " concepts");
		log.info("Processed " + conceptCount + " concepts");

		log.info("Load Statistics");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}
		
		// this could be removed from final release. Just added to help debug editor problems.
		if (outputDirectory != null)
		{
			log.info("Dumping UUID Debug File");
			converterUUID.dump(outputDirectory, "cptUuid");
		}
		converterUUID.clearCache();
	}

	private void process(ICD10 row)
	{
		try
		{
			final String code = row.getCode();
			final String order = row.getOrderNumber();
			final String shortDesc = row.getShortDescription();
			final String longDesc = row.getLongDescription();
			final Status status = Status.ACTIVE;

			// Create row concept
			final UUID rowConceptUuid = converterUUID.createNamespaceUUIDFromString(code);
			final UUID concept = dwh.makeConcept(rowConceptUuid, status, contentTime);

			dwh.makeDescriptionEnNoDialect(concept, shortDesc, dwh.getDescriptionType("Short Description"), status, contentTime);
			dwh.makeDescriptionEnNoDialect(concept, longDesc, dwh.getDescriptionType("Long Description"), status, contentTime);
			
			dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), concept, code, contentTime);

			dwh.makeBrittleStringAnnotation(dwh.getAttributeType("ICD-10 Order Number"), concept, order, contentTime);
			
			if (!row.isHeader())
			{
				dwh.makeDynamicRefsetMember(HIPPA_Valid, concept, contentTime);
			}

			// Figure out the correct parent code.
			if (code.length() <= 3)
			{
				// Hang it on root
				dwh.makeParentGraph(concept, icdRootConcept, status, contentTime);
			}
			else
			{
				String parentCode = code.substring(0, code.length() - 1);
				while (true)
				{
					if (parentCode.length() < 3)
					{
						throw new MojoExecutionException("Failed to find parent for " + code);
					}
					UUID temp = codeToUuid.get(parentCode);
					if (temp == null)
					{
						parentCode = code.substring(0, parentCode.length() - 1);
						continue;
					}
					else
					{
						dwh.makeParentGraph(concept, temp, status, contentTime);
						break;
					}
				}
			}

			// Add to refset allIcdConceptsRefset
			dwh.makeDynamicRefsetMember(allICDConceptsRefset, concept, contentTime);
			codeToUuid.put(code, concept);

			conceptCount++;
			if (conceptCount % 500 == 0)
			{
				showProgress();
			}
			if (conceptCount % 10000 == 0)
			{
				advanceProgressLine();
				log.info("Processed " + conceptCount + " concepts");
				statusUpdates.accept("Processed " + conceptCount + " concepts");
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Exception", e);
		}
	}
}
