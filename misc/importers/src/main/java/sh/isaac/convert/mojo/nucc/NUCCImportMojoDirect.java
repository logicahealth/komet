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

package sh.isaac.convert.mojo.nucc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.mojo.nucc.data.EnumValidatedTableData;
import sh.isaac.convert.mojo.nucc.data.EnumValidatedTableDataReader;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * 
 * {@link NUCCImportMojoDirect}
 *
 * Goal which converts NUCC data ISAAC
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Mojo(name = "convert-NUCC-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class NUCCImportMojoDirect extends DirectConverterBaseMojo implements DirectConverter
{
	/**
	 * This constructor is for maven and HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link #configure(File, Path, String, StampCoordinate)} method on it.
	 */
	public NUCCImportMojoDirect()
	{
		
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
		return new SupportedConverterTypes[] {SupportedConverterTypes.NUCC};
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Transaction, Consumer, BiConsumer))
	 * @see DirectConverter#convertContent(Transaction, Consumer, BiConsumer))
	 */
	@Override
	public void convertContent(Transaction transaction, Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdate) throws IOException
	{
		final Map<String, UUID> groupingValueConceptByValueMap = new HashMap<>();
		final Map<String, UUID> classificationValueConceptByValueMap = new HashMap<>();
		final Map<String, UUID> specializationValueConceptByValueMap = new HashMap<>();

		String temp = "Bogus date"; // TODO Find date from source
		Date date = null;
		try
		{
			date = new SimpleDateFormat("yyyy.MM.dd").parse(temp);
		}
		catch (Exception e)
		{
			date = new Date(); // TODO remove this when getting valid data from source
		}
		
		//Right now, we are configured for the NUCC grouping modules nid
		dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.NUCC_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID, 
				"NUCC", false);
		
		setupModule(transaction, "NUCC", MetaData.NUCC_MODULES____SOLOR.getPrimordialUuid(), date.getTime());
		
		//Set up our metadata hierarchy
		dwh.makeMetadataHierarchy(transaction, true, false, true, false, true, false, date.getTime());
		
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, date.getTime(), readbackCoordinate);
		
		dwh.makeAttributeTypeConcept(transaction, null, NUCCColumnsV1.Grouping.name(), null, null, false, DynamicDataType.NID, null, date.getTime());
		dwh.makeAttributeTypeConcept(transaction, null, NUCCColumnsV1.Classification.name(), null, null, false, DynamicDataType.NID, null, date.getTime());
		dwh.makeAttributeTypeConcept(transaction, null, NUCCColumnsV1.Specialization.name(), null, null, false, DynamicDataType.NID, null, date.getTime());

		dwh.makeRefsetTypeConcept(transaction, null, "All NUCC Concepts", null, null, date.getTime());

		// Switch on version to select proper Columns enum to use in constructing reader
		final EnumValidatedTableDataReader<NUCCColumnsV1> importer = new EnumValidatedTableDataReader<>(inputFileLocationPath, NUCCColumnsV1.class);
		final EnumValidatedTableData<NUCCColumnsV1> terminology = importer.process();

		log.info("Read " + terminology.rows().size() + " entries");
		statusUpdates.accept("Read " + terminology.rows().size() + " entries");

		/*
		 * COLUMNS from NUCCColumnsV1:
		 * Code, // Required FSN
		 * Grouping, // Create attribute and concepts representing each unique required value
		 * Classification, // Create attribute and concepts representing each optional unique value
		 * Specialization, // Create attribute and concepts representing each optional unique value
		 * Definition, // Optional DEFINITION
		 * Notes // Optional comment
		 */
		// Parent nuccMetadata ComponentReference

		// Create NUCC root concept under SOLOR_CONCEPT____SOLOR
		final UUID nuccRootConcept = dwh.makeConceptEnNoDialect(transaction, null, "NUCC", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
				new UUID[] {MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()}, Status.ACTIVE, date.getTime());
		
		log.info("Metadata load stats");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}
		
		dwh.clearLoadStats();
		
		statusUpdates.accept("Loading content");

		// Create concepts for each unique value in each of three Grouping, Classification and Specialization columns
		// Each concept is created as a child of its respective column type concept
		// A map of String value to ConceptChronology is maintained for later use
		for (String value : terminology.getDistinctValues(NUCCColumnsV1.Grouping))
		{
			if (StringUtils.isBlank(value))
			{
				throw new RuntimeException("Cannot load NUCC data with blank Grouping");
			}
			// Create the Grouping value concept as a child of both NUCC root and the Grouping property metadata concept
			// and store in map for later retrieval
			UUID conceptToMake = converterUUID.createNamespaceUUIDFromString(NUCCColumnsV1.Grouping.name() + "|" + value);
			UUID valueConcept = dwh.makeConceptEnNoDialect(transaction, conceptToMake, value, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					new UUID[] {dwh.getAttributeType(NUCCColumnsV1.Grouping.name()), nuccRootConcept}, 
					Status.ACTIVE, date.getTime());

			// Store Grouping value concept in map by value
			groupingValueConceptByValueMap.put(value, valueConcept);
		}

		// Create a concept for each distinct non blank Classification value and store in map for later retrieval
		for (String value : terminology.getDistinctValues(NUCCColumnsV1.Classification))
		{
			if (StringUtils.isNotBlank(value))
			{
				UUID conceptToMake = converterUUID.createNamespaceUUIDFromString(NUCCColumnsV1.Classification.name() + "|" + value);
				UUID valueConcept = dwh.makeConceptEnNoDialect(transaction, conceptToMake, value, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						new UUID[] {dwh.getAttributeType(NUCCColumnsV1.Classification.name())}, Status.ACTIVE, date.getTime());
				classificationValueConceptByValueMap.put(value, valueConcept);
			}
		}

		// Create a concept for each distinct Specialization value and store in map for later retrieval
		for (String value : terminology.getDistinctValues(NUCCColumnsV1.Specialization))
		{
			if (StringUtils.isNotBlank(value))
			{
				UUID conceptToMake = converterUUID.createNamespaceUUIDFromString(NUCCColumnsV1.Specialization.name() + "|" + value);
				UUID valueConcept = dwh.makeConceptEnNoDialect(transaction, conceptToMake, value, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						new UUID[] {dwh.getAttributeType(NUCCColumnsV1.Specialization.name())}, Status.ACTIVE, date.getTime());
				specializationValueConceptByValueMap.put(value, valueConcept);
			}
		}

		int dataRows = 0;
		// Populate hierarchy, one row at a time, creating concepts as children of their respective Grouping concepts
		for (Map<NUCCColumnsV1, String> row : terminology.rows())
		{
			final UUID groupingValueConcept = row.get(NUCCColumnsV1.Grouping) != null
					? groupingValueConceptByValueMap.get(row.get(NUCCColumnsV1.Grouping))
					: null;
			final UUID classificationValueConcept = row.get(NUCCColumnsV1.Classification) != null
					? classificationValueConceptByValueMap.get(row.get(NUCCColumnsV1.Classification))
					: null;
			final UUID specializationValueConcept = row.get(NUCCColumnsV1.Specialization) != null
					? specializationValueConceptByValueMap.get(row.get(NUCCColumnsV1.Specialization))
					: null;

			if (groupingValueConcept == null)
			{
				throw new RuntimeException("Cannot create NUCC concept without Grouping: " + row.toString());
			}

			try
			{
				// Create row concept
				UUID concept = dwh.makeConceptEnNoDialect(transaction, null, row.get(NUCCColumnsV1.Code), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						new UUID[] {groupingValueConcept}, Status.ACTIVE, date.getTime());
				
				// Add required NUCC Code annotation
				dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), concept, row.get(NUCCColumnsV1.Code), date.getTime());

				// Add required Grouping NID annotation
				dwh.makeDynamicSemantic(dwh.getAttributeType(NUCCColumnsV1.Grouping.name()), concept, 
						new DynamicData[] {new DynamicNidImpl(groupingValueConcept)}, date.getTime());

				// Add optional Classification NID annotation
				if (classificationValueConcept != null)
				{
					dwh.makeDynamicSemantic(dwh.getAttributeType(NUCCColumnsV1.Classification.name()), concept, 
							new DynamicData[] {new DynamicNidImpl(classificationValueConcept)}, date.getTime());
				}
				// Add optional Specialization NID annotation
				if (specializationValueConcept != null)
				{
					dwh.makeDynamicSemantic(dwh.getAttributeType(NUCCColumnsV1.Specialization.name()), concept, 
							new DynamicData[] {new DynamicNidImpl(specializationValueConcept)}, date.getTime());
				}

				// Add optional Notes comment annotation
				if (StringUtils.isNotBlank(row.get(NUCCColumnsV1.Notes)))
				{
					dwh.makeComment(concept, row.get(NUCCColumnsV1.Notes), null, date.getTime());
				}

				// Add optional Definition description
				if (StringUtils.isNotBlank(row.get(NUCCColumnsV1.Definition)))
				{
					dwh.makeDescriptionEnNoDialect(concept, row.get(NUCCColumnsV1.Definition), MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
							Status.ACTIVE, date.getTime());
				}

				// Add to refset allNuccConceptsRefset
				dwh.makeDynamicRefsetMember(dwh.getRefsetType("All NUCC Concepts"), concept, date.getTime());
				++dataRows;
			}
			catch (Exception e)
			{
				throw new RuntimeException("Failed processing row with " + e.getClass().getSimpleName() + " " + e.getLocalizedMessage() + ": " + row, e);
			}
		}

		dwh.processTaxonomyUpdates();
		Get.taxonomyService().notifyTaxonomyListenersToRefresh();
		
		log.info("Processed " + dataRows + " rows");
		statusUpdates.accept("Processed " + dataRows + " rows");
		
		log.info("Load Statistics");

		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}

		log.info("Processed " + groupingValueConceptByValueMap.size() + " distinct NUCC " + NUCCColumnsV1.Grouping + " concepts");
		log.info("Processed " + classificationValueConceptByValueMap.size() + " distinct NUCC " + NUCCColumnsV1.Classification + " concepts");
		log.info("Processed " + specializationValueConceptByValueMap.size() + " distinct NUCC " + NUCCColumnsV1.Specialization + " concepts");

		// this could be removed from final release. Just added to help debug editor problems.
		if (outputDirectory != null)
		{
			log.info("Dumping UUID Debug File");
			converterUUID.dump(outputDirectory, "nuccUuid");
		}
		converterUUID.clearCache();
	}
}