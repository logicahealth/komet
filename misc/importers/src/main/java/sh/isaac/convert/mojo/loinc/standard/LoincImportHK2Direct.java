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
package sh.isaac.convert.mojo.loinc.standard;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.mojo.loinc.LOINCReader;
import sh.isaac.convert.mojo.loinc.LoincCsvFileReader;
import sh.isaac.convert.mojo.loinc.NameMap;
import sh.isaac.convert.mojo.loinc.TxtFileReader;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * {@link LoincImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class LoincImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	private final HashMap<String, HashMap<String, String>> mapToData = new HashMap<>();

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	private HashSet<UUID> concepts = new HashSet<>();

	private int skippedDeletedItems = 0;

	private final HashMap<UUID, HashSet<UUID>> multiaxialPathsToRoot = new HashMap<>();

	protected Hashtable<String, Integer> fieldMap;

	protected Hashtable<Integer, String> fieldMapInverse;

	private NameMap classMapping;

	private TreeMap<String, Long> versionTimeMap;
	
	HashSet<String> skippable = new HashSet<>();

	/**
	 * This constructor is for maven and HK2 and should not be used at runtime. You should
	 * get your reference of this class from HK2, and then call the {@link #configure(File, Path, String, StampCoordinate)} method on it.
	 */
	protected LoincImportHK2Direct()
	{
		//For maven and hk2
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
	 * If this was constructed via HK2, then you must call the configure method prior to calling {@link #convertContent(Consumer, BiConsumer)}
	 * If this was constructed via the constructor that takes parameters, you do not need to call this.
	 * 
	 * @see sh.isaac.convert.directUtils.DirectConverter#configure(File, Path, String, StampCoordinate)
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
		return new SupportedConverterTypes[] { SupportedConverterTypes.LOINC };
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer, BiConsumer) 
	 * @see DirectConverter#convertContent(Consumer, BiConsumer)
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdate) throws IOException
	{
		log.info("LOINC Processing Begins " + new Date().toString());

		AtomicReference<LOINCReader> loincData = new AtomicReference<>();
		AtomicReference<LOINCReader> mapTo = new AtomicReference<>();
		AtomicReference<LOINCReader> sourceOrg = new AtomicReference<>();
		AtomicReference<LOINCReader> loincMultiData = new AtomicReference<>();

		try
		{
			Files.walk(inputFileLocationPath, new FileVisitOption[] {}).forEach(path -> 
			{
				try
				{
					if (path.toString().toLowerCase().endsWith("loincdb.txt"))
					{
						log.info("Reading loincdb.txt");
						loincData.set(new TxtFileReader(path));
					}
					else if (path.toString().toLowerCase().endsWith("loinc.csv"))
					{
						log.info("Reading loinc.csv");
						loincData.set(new LoincCsvFileReader(path, true));
						this.versionTimeMap = ((LoincCsvFileReader) loincData.get()).getTimeVersionMap();
					}
					else if (path.toString().toLowerCase().endsWith("map_to.csv") ||
							path.toString().toLowerCase().endsWith("mapto.csv"))
					{
						log.info("Reading map_to.csv");
						mapTo.set(new LoincCsvFileReader(path, false));
					}
					else if (path.toString().toLowerCase().endsWith("source_organization.csv") ||
							path.toString().toLowerCase().endsWith("sourceorganization.csv"))
					{
						log.info("Reading source_organization.csv");
						sourceOrg.set(new LoincCsvFileReader(path, false));
					}
					else if (path.toString().toLowerCase().endsWith("multiaxialhierarchy.csv") 
							|| path.toString().toLowerCase().endsWith("multi-axial_hierarchy.csv"))
					{
						log.info("Reading multi-axial_hierarchy.csv");
						loincMultiData.set(new LoincCsvFileReader(path, false));
					}
					else if (path.toString().toLowerCase().endsWith(".zip"))
					{
						// New zip file set
						try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path, StandardOpenOption.READ)))
						{
							ZipEntry ze = zis.getNextEntry();
	
							while (ze != null)
							{
								// see {@link SupportedConverterTypes}
								if (path.toString().toLowerCase().contains("text"))
								{
									if (ze.getName().toLowerCase().endsWith("loinc.csv"))
									{
										log.info("Using the data file " + path + " - " + ze.getName());
										loincData.set(new LoincCsvFileReader(zis));
										((LoincCsvFileReader) loincData.get()).readReleaseNotes(path.getParent(), true);
										this.versionTimeMap = ((LoincCsvFileReader) loincData.get()).getTimeVersionMap();
									}
									else if (ze.getName().toLowerCase().endsWith("map_to.csv") || ze.getName().toLowerCase().endsWith("mapto.csv"))
									{
										log.info("Using the data file " + path + " - " + ze.getName());
										mapTo.set(new LoincCsvFileReader(zis));
									}
									else if (ze.getName().toLowerCase().endsWith("source_organization.csv") || 
											ze.getName().toLowerCase().endsWith("sourceorganization.csv"))
									{
										log.info("Using the data file " + path + " - " + ze.getName());
										sourceOrg.set(new LoincCsvFileReader(zis));
									}
								}
								else if (path.toString().toLowerCase().contains("multi-axial_hierarchy") ||
										path.toString().toLowerCase().contains("multiaxialhierarchy"))
								{
									if ((ze.getName().toLowerCase().contains("multiaxial") || ze.getName().toLowerCase().contains("multi-axial")) 
											&& ze.getName().toLowerCase().endsWith(".csv"))
									{
										log.info("Using the data file " + path + " - " + ze.getName());
										loincMultiData.set(new LoincCsvFileReader(zis));
									}
								}
								ze = zis.getNextEntry();
							}
						}
					}
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			});

			if (loincData.get() == null)
			{
				throw new MojoExecutionException("Could not find the loinc data file in " + this.inputFileLocationPath);
			}

			if (loincMultiData.get() == null)
			{
				throw new MojoExecutionException("Could not find the multi-axial file in " + this.inputFileLocationPath);
			}

			final SimpleDateFormat dateReader = new SimpleDateFormat("MMMMMMMMMMMMM yyyy"); // Parse things like "June 2014"
			final Date releaseDate = dateReader.parse(loincData.get().getReleaseDate());

			log.info("Setting up metadata");
			//Right now, we are configured for the LOINC grouping modules nid
			dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.LOINC_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(),
					converterUUID, "LOINC", false);

			setupModule("LOINC", MetaData.LOINC_MODULES____SOLOR.getPrimordialUuid(), Optional.of("http://loinc.org"), releaseDate.getTime());

			//Set up our metadata hierarchy
			dwh.makeMetadataHierarchy(true, true, true, true, true, true, releaseDate.getTime());

			this.fieldMap = loincData.get().getFieldMap();
			this.fieldMapInverse = loincData.get().getFieldMapInverse();

			int sourceVersion = loincData.get().getFormatVersionNumber();
			
			this.classMapping = new NameMap(loincData.get().getMapFileName());

			if (mapTo != null)
			{
				String[] line = mapTo.get().readLine();

				while (line != null)
				{
					if (line.length > 0)
					{
						HashMap<String, String> nestedData = this.mapToData.get(line[0]);

						if (nestedData == null)
						{
							nestedData = new HashMap<>();
							this.mapToData.put(line[0], nestedData);
						}

						if (nestedData.put(line[1], line[2]) != null)
						{
							throw new Exception("Oops - " + line[0] + " " + line[1] + " " + line[2]);
						}
					}

					line = mapTo.get().readLine();
				}
			}
			
			skippable.add("SOURCE");
			skippable.add("FINAL");  //deleted in 2.38

			initDescriptionTypes(sourceVersion, releaseDate.getTime());
			dwh.makeAssociationTypeConcept(null, "MAP_TO", "Map To", null, "Associations from the LOINC 'Map To' table", null, IsaacObjectType.CONCEPT, null,
					null, releaseDate.getTime());

			initAttributeTypes(sourceVersion, releaseDate.getTime());

			initClassStructure(sourceVersion, releaseDate.getTime());
			initAxisStructure(sourceVersion, releaseDate.getTime());

			// Every time concept created add membership to "All CPT Concepts"
			UUID allLoincConceptsRefset = dwh.makeRefsetTypeConcept(null, "All LOINC Concepts", null, null, releaseDate.getTime());

			if (sourceOrg != null)
			{
				UUID sourceOrgUuid = dwh.makeConceptEnNoDialect(null, "Source Organization",
						MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), new UUID[] { dwh.getMetadataRoot() }, Status.ACTIVE,
						releaseDate.getTime());

				String[] line = sourceOrg.get().readLine();

				while (line != null)
				{
					// ﻿"COPYRIGHT_ID","NAME","COPYRIGHT","TERMS_OF_USE","URL"
					if (line.length > 0)
					{
						UUID lineConcept = dwh.makeConceptEnNoDialect(null, line[1], dwh.getDescriptionType("NAME"),
								new UUID[] { sourceOrgUuid }, Status.ACTIVE, releaseDate.getTime());
						dwh.makeStringAnnotation(dwh.getAttributeType("COPYRIGHT_ID"), lineConcept, line[0], releaseDate.getTime());
						dwh.makeStringAnnotation(dwh.getAttributeType("COPYRIGHT"), lineConcept, line[2], releaseDate.getTime());
						dwh.makeStringAnnotation(dwh.getAttributeType("TERMS_OF_USE"), lineConcept, line[3], releaseDate.getTime());
						dwh.makeStringAnnotation(dwh.getAttributeType("URL"), lineConcept, line[4], releaseDate.getTime());
					}

					line = sourceOrg.get().readLine();
				}
			}

			log.info("Metadata load stats");
			for (String line : dwh.getLoadStats().getSummary())
			{
				log.info(line);
			}

			dwh.clearLoadStats();

			statusUpdates.accept("Loading content");
			
			progressUpdate.accept(0d, (double)loincData.get().getDataSize());

			// The next line of the file is the header.
			final String[] headerFields = loincData.get().getHeader();

			// validate that we are configured to map all properties properly
			checkForLeftoverPropertyTypes(headerFields);

			// Root
			final UUID rootConcept = dwh.makeConceptEnNoDialect(null, "LOINC", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					new UUID[] { MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid() }, Status.ACTIVE, releaseDate.getTime());

			dwh.makeDescriptionEnNoDialect(rootConcept, "Logical Observation Identifiers Names and Codes",
					MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, releaseDate.getTime());

			log.info("Root concept FQN is 'LOINC' and the UUID is " + rootConcept);

			this.concepts.add(rootConcept);

			// load the data
			log.info("Processing file....");

			int dataRows = 0;

			{
				String[] line = loincData.get().readLine();

				dataRows++;

				while (line != null)
				{
					if (line.length > 0)
					{
						processDataLine(line);
					}

					line = loincData.get().readLine();
					dataRows++;

					if (dataRows % 1000 == 0)
					{
						showProgress();
						progressUpdate.accept((double)dataRows, (double)loincData.get().getDataSize());
					}

					if (dataRows % 10000 == 0)
					{
						statusUpdates.accept("Processed " + dataRows + " lines");
						advanceProgressLine();
						log.info("Processed " + dataRows + " lines");
					}
				}
			}

			advanceProgressLine();
			progressUpdate.accept(0d, (double)loincMultiData.get().getDataSize());
			statusUpdates.accept("Reading multiaxial data");
			log.info("Read " + dataRows + " data lines from file");
			log.info("Processing multi-axial file");

			{

				// header - PATH_TO_ROOT,SEQUENCE,IMMEDIATE_PARENT,CODE,CODE_TEXT
				int lineCount = 0;
				String[] line = loincMultiData.get().readLine();

				while (line != null)
				{
					lineCount++;

					if (line.length == 5)
					{
						processMultiAxialData(rootConcept, line, releaseDate.getTime());
					}

					else
					{
						log.error("Skipping multiaxial line because its the wrong length: " + Arrays.toString(line));
					}

					line = loincMultiData.get().readLine();

					if (lineCount % 1000 == 0)
					{
						showProgress();
						progressUpdate.accept((double)lineCount, (double)loincMultiData.get().getDataSize());
					}
				}

				advanceProgressLine();
				log.info("Read " + lineCount + " data lines from file.  Creating graphs and hierarcy concepts");
				statusUpdates.accept("Creating Logic Graphs");
				progressUpdate.accept(0d, (double)this.multiaxialPathsToRoot.size());

				int pos = 0;
				for (final Entry<UUID, HashSet<UUID>> items : this.multiaxialPathsToRoot.entrySet())
				{
					dwh.makeParentGraph(items.getKey(), items.getValue(), Status.ACTIVE, releaseDate.getTime());
					pos++;
					if (pos % 1000 == 0)
					{
						progressUpdate.accept((double)pos, (double)this.multiaxialPathsToRoot.size());
					}
				}
			}
			
			//Load allConcepts refset
			for (UUID uuid : concepts)
			{
				dwh.makeDynamicRefsetMember(allLoincConceptsRefset, uuid, releaseDate.getTime());
			}

			statusUpdates.accept("Processing Taxonomy Updates");
			progressUpdate.accept(0d, -1d);
			dwh.processTaxonomyUpdates();
			Get.taxonomyService().notifyTaxonomyListenersToRefresh();
			
			progressUpdate.accept(1d, 1d);
			
			log.info("Processed " + this.concepts.size() + " concepts total");
			log.info("Data Load Summary:");

			log.info("Load Statistics");
			for (String line : dwh.getLoadStats().getSummary())
			{
				log.info(line);
			}

			log.info("Skipped " + this.skippedDeletedItems + " Loinc codes because they were flagged as DELETED and they had no desriptions.");

			// this could be removed from final release. Just added to help debug editor problems.
			if (outputDirectory != null)
			{
				log.info("Dumping UUID Debug File");
				converterUUID.dump(outputDirectory, "loincUuid");
			}
			converterUUID.clearCache();
			log.info("LOINC Processing Completes " + new Date().toString());
		}
		catch (final Exception ex)
		{
			try
			{
				// make sure this is dumped
				if (outputDirectory != null)
				{
					converterUUID.dump(this.outputDirectory, "loincUuid");
				}
			}
			catch (final IOException e)
			{
				// noop
			}

			throw new IOException(ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * @param sourceVersion
	 * @param time
	 */
	private void initAxisStructure(int sourceVersion, long time)
	{
		UUID axisNode = dwh.makeOtherMetadataRootNode("Axis", time);
//		this.concepts.put(classConcept.getPrimordialUuid(), classConcept);

		UUID otherType = dwh.makeOtherTypeConcept(axisNode, null, "COMPONENT", "Component", null, null, null, null, time);
//		this.concepts.put(temp.getPrimordialUuid(), temp);
		dwh.configureConceptAsAssociation(otherType, "The Axis Type Association", null, IsaacObjectType.CONCEPT, null, time);

		otherType = dwh.makeOtherTypeConcept(axisNode, null, "PROPERTY", "Property", null, null, null, null, time);
//	this.concepts.put(temp.getPrimordialUuid(), temp);
		dwh.configureConceptAsAssociation(otherType, "The Axis Type Association", null, IsaacObjectType.CONCEPT, null, time);

		otherType = dwh.makeOtherTypeConcept(axisNode, null, "TIME_ASPCT", "Time Aspect", null, null, null, null, time);
//	this.concepts.put(temp.getPrimordialUuid(), temp);
		dwh.configureConceptAsAssociation(otherType, "The Axis Type Association", null, IsaacObjectType.CONCEPT, null, time);

		otherType = dwh.makeOtherTypeConcept(axisNode, null, "SYSTEM", "System", null, null, null, null, time);
//	this.concepts.put(temp.getPrimordialUuid(), temp);
		dwh.configureConceptAsAssociation(otherType, "The Axis Type Association", null, IsaacObjectType.CONCEPT, null, time);
		
		otherType = dwh.makeOtherTypeConcept(axisNode, null, "SCALE_TYP", "Scale Type", null, null, null, null, time);
//		this.concepts.put(temp.getPrimordialUuid(), temp);
		dwh.configureConceptAsAssociation(otherType, "The Axis Type Association", null, IsaacObjectType.CONCEPT, null, time);

		otherType = dwh.makeOtherTypeConcept(axisNode, null, "METHOD_TYP", "Method Type", null, null, null, null, time);
//	this.concepts.put(temp.getPrimordialUuid(), temp);
		dwh.configureConceptAsAssociation(otherType, "The Axis Type Association", null, IsaacObjectType.CONCEPT, null, time);
	}

	/**
	 * @param sourceVersion
	 * @param time
	 */
	private void initClassStructure(int sourceVersion, long time)
	{
		UUID classNode = dwh.makeOtherMetadataRootNode("Class", time);
//		this.concepts.put(classConcept.getPrimordialUuid(), classConcept);

		UUID otherType = dwh.makeOtherTypeConcept(classNode, null, "CLASS", "Class", null, null, null, null, time);
//		this.concepts.put(temp.getPrimordialUuid(), temp);
		dwh.configureConceptAsAssociation(otherType, "The Class Association", null, IsaacObjectType.CONCEPT, null, time);
	}

	/**
	 * @param sourceVersion
	 * @param time
	 */
	private void initDescriptionTypes(int sourceVersion, long time)
	{
		dwh.makeDescriptionTypeConcept(null, "CONSUMER_NAME", "Consumer Name", null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null,
				time);
		if (sourceVersion <= 1)
		{
			// deleted in 2.38
			dwh.makeDescriptionTypeConcept(null, "EXACT_CMP_SY", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, time);
		}
		if (sourceVersion <= 5)
		{	// deleted in 2.52
			dwh.makeDescriptionTypeConcept(null, "ACSSYM", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, time);
			dwh.makeDescriptionTypeConcept(null, "BASE_NAME", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, time);
		}

		if (sourceVersion >= 6)
		{
			//Added in 2.52
			dwh.makeDescriptionTypeConcept(null, "DefinitionDescription", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null,
					time);
		}

		dwh.makeDescriptionTypeConcept(null, "SHORTNAME", "Short Name", null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, time);

		dwh.makeDescriptionTypeConcept(null, "LONG_COMMON_NAME", "Long Common Name", null,
				MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, time);

		//from multiaxial
		dwh.makeDescriptionTypeConcept(null, "CODE_TEXT", "Code Test", null, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null,
				time);

		//from source_organization
		dwh.makeDescriptionTypeConcept(null, "NAME", "Name", null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, time);
	}

	/**
	 * @param sourceVersion
	 * @param time
	 */
	private void initAttributeTypes(int sourceVersion, long time)
	{
		if (sourceVersion <= 1)
		{
			// replaced with DATE_LAST_CHANGED in 2.38
			dwh.makeAttributeTypeConcept(null, "DT_LAST_CH", "Date Last Changed", null, false, DynamicDataType.STRING, null, time);
			// deleted in 2.38
			dwh.makeAttributeTypeConcept(null, "ANSWERLIST", "Answer List", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "SCOPE", "Scope", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "IPCC_UNITS", "IPCC Units", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "REFERENCE", "Reference", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "SETROOT", "", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "PANELELEMENTS", "Panel Elements", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "INPC_PERCENTAGE", "INPC Percentage", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "DEFINITION_DESCRIPTION_HELP", "Definition Description Help", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "RELAT_NMS", "Related Names", null, false, DynamicDataType.STRING, null, time);
		}
		if (sourceVersion >= 2 && sourceVersion <= 6)
		{
			// replaced with VersionLastChanged in 2.54
			dwh.makeAttributeTypeConcept(null, "DATE_LAST_CHANGED", "Date Last Changed", null, false, DynamicDataType.STRING, null, time);
		}
		if (sourceVersion <= 4)
		{
			// Moved from ID - turned out it wasn't unique (see loinc_num 42040-6 and 39807-3)  //deleted in 2.52
			dwh.makeAttributeTypeConcept(null, "NAACCR_ID", "NAACCR ID", null, false, DynamicDataType.STRING, null, time);
		}

		if (sourceVersion <= 5)
		{
			// deleted in 2.52
			dwh.makeAttributeTypeConcept(null, "CHNG_TYPE", "Change Type", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "COMMENTS", "Comments", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "MOLAR_MASS", "Molar Mass", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "CODE_TABLE", "Code Table", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "HL7_V2_DATATYPE", "", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "HL7_V3_DATATYPE", "", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "CURATED_RANGE_AND_UNITS", "Curated Range and Units", null, false, DynamicDataType.STRING, null, time);
		}

		if (sourceVersion >= 2)
		{
			// added in 2.38
			dwh.makeAttributeTypeConcept(null, "COMMON_ORDER_RANK", "Common Order Rank", null, false, DynamicDataType.STRING, null, time);
		}

		if (sourceVersion >= 3)
		{
			// added in 2.40 (or maybe 2.39, 2.39 is untested - they failed to document it)
			dwh.makeAttributeTypeConcept(null, "COMMON_SI_TEST_RANK", "Common SI Test Rank", null, false, DynamicDataType.STRING, null, time);
		}
		if (sourceVersion >= 4)
		{
			dwh.makeAttributeTypeConcept(null, "HL7_ATTACHMENT_STRUCTURE", "HL7 Attachement Structure", null, false, DynamicDataType.STRING, null, time);
		}
		if (sourceVersion >= 5)
		{
			// added in 2.50
			dwh.makeAttributeTypeConcept(null, "EXTERNAL_COPYRIGHT_LINK", "External Copyright Link", null, false, DynamicDataType.STRING, null, time);
		}
		if (sourceVersion >= 6)
		{
			// added in 2.52
			dwh.makeAttributeTypeConcept(null, "UnitsAndRange", "Units and Range", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "PanelType", "Panel Type", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "AskAtOrderEntry", "Ask at Order Entry", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "AssociatedObservations", "Associated Observations", null, false, DynamicDataType.STRING, null, time);
		}
		if (sourceVersion >= 7)
		{
			// added in 2.54
			dwh.makeAttributeTypeConcept(null, "VersionLastChanged", "Version Last Changed", null, false, DynamicDataType.STRING, null, time);
		}
		if (sourceVersion >= 8)
		{
			//added in 2.59
			dwh.makeAttributeTypeConcept(null, "VersionFirstReleased", "Version First Released", null, false, DynamicDataType.STRING, null, time);
			dwh.makeAttributeTypeConcept(null, "ValidHL7AttachmentRequest", "Valid HL7 Attachment Request", null, false, DynamicDataType.STRING, null, time);
		}
		
		if (sourceVersion <= 8)
		{
			//the release notes claim this was deleted in 2.63, but doesn't seem to have been - looks like, now, it was removed in 2.65
			dwh.makeAttributeTypeConcept(null, "DOCUMENT_SECTION", "Document Section", null, false, DynamicDataType.STRING, null, time);
		}
		
		dwh.makeAttributeTypeConcept(null, "CHNG_TYPE", "Change Type", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "CLASSTYPE", "Class Type", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "FORMULA", "Formula", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "SPECIES", "Species", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "EXMPL_ANSWERS", "Example Answers", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "SURVEY_QUEST_TEXT", "Survey Question Text", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "SURVEY_QUEST_SRC", "Survey Question Source", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "UNITSREQUIRED", "Units Required", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "SUBMITTED_UNITS", "Submitted Units", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "ORDER_OBS", "Order OBS", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "CDISC_COMMON_TESTS", "CDISC Common Tests", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "HL7_FIELD_SUBFIELD_ID", "HL7 Field Subfield Id", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "EXTERNAL_COPYRIGHT_NOTICE", "External Copyright Notice", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "EXAMPLE_UNITS", "Example Units", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "EXAMPLE_UCUM_UNITS", "Example UCUM Units", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "EXAMPLE_SI_UCUM_UNITS", "Example SI UCUM Units", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "STATUS_REASON", "Status Reason", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "STATUS_TEXT", "Status Text", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "CHANGE_REASON_PUBLIC", "Change Reason Public", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "COMMON_TEST_RANK", "Common Test Rank", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "STATUS", "Status", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "RELATEDNAMES2", "Related Names 2", null, false, DynamicDataType.STRING, null, time);

		//from multiaxial
		dwh.makeAttributeTypeConcept(null, "SEQUENCE", "Sequence", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "IMMEDIATE_PARENT", "Immediate Parent", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "PATH_TO_ROOT", "Path to Root", null, false, DynamicDataType.STRING, null, time);
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, time, readbackCoordinate);

		// From Source_Organization
		dwh.makeAttributeTypeConcept(null, "COPYRIGHT_ID", "Copyright ID", null, true, null, null, time);
		dwh.makeAttributeTypeConcept(null, "COPYRIGHT", "Copyright", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "TERMS_OF_USE", "Terms of Use", null, false, DynamicDataType.STRING, null, time);
		dwh.makeAttributeTypeConcept(null, "URL", "Url", null, false, DynamicDataType.STRING, null, time);

		// From MAP_TO
		dwh.makeAttributeTypeConcept(null, "COMMENT", "Comment", null, false, DynamicDataType.STRING, null, time);

		dwh.makeAttributeTypeConcept(null, "LOINC_NUM", "LOINC Identifier", null, "Carries the LOINC_NUM native identifier", false, DynamicDataType.STRING,
				null, time);

		dwh.makeAttributeTypeConcept(null, "ABBREVIATION", "Abbreviation", null, false, DynamicDataType.STRING, null, time);
	}

	/**
	 * Utility to help build UUIDs in a consistent manner.
	 *
	 * @param uniqueIdentifier the unique identifier
	 * @return the uuid
	 */
	private UUID buildUUID(String uniqueIdentifier)
	{
		return converterUUID.createNamespaceUUIDFromString(uniqueIdentifier, true);
	}

	/**
	 * Check for leftover property types.
	 *
	 * @param fileColumnNames the file column names
	 * @throws Exception the exception
	 */
	private void checkForLeftoverPropertyTypes(String[] fileColumnNames) throws Exception
	{
		for (final String name : fileColumnNames)
		{
			UUID uuid = dwh.getAttributeType(name);
			if (uuid == null)
			{
				uuid = dwh.getDescriptionType(name);
			}
			if (uuid == null)
			{
				uuid = dwh.getAssociationType(name);
			}
			if (uuid == null)
			{
				uuid = dwh.getRefsetType(name);
			}
			if (uuid == null)
			{
				uuid = dwh.getOtherType(dwh.getOtherMetadataRootType("Axis"), name);
			}
			if (uuid == null)
			{
				uuid = dwh.getOtherType(dwh.getOtherMetadataRootType("Class"), name);
			}
			if (uuid == null && !skippable.contains(name))
			{
				log.error("ERROR:  No mapping for property type: " + name);
			}
		}
	}

	/**
	 * Check path.
	 *
	 * @param concept the concept
	 * @param pathToRoot the path to root
	 */
	private void checkPath(UUID concept, String[] pathToRoot)
	{
		// The passed in concept should have a relation to the item at the end of the root list.
		UUID conceptToLookAt = concept;
		for (int i = (pathToRoot.length - 1); i >= 0; i--)
		{
			final UUID target = buildUUID(pathToRoot[i]);
			HashSet<UUID> parents = this.multiaxialPathsToRoot.get(conceptToLookAt);

			if (parents == null)
			{
				parents = new HashSet<>();
				this.multiaxialPathsToRoot.put(conceptToLookAt, parents);
			}

			parents.add(target);

			if (!concepts.contains(target))
			{
				log.error("Missing concept! " + pathToRoot[i]);
				break;
			}
			conceptToLookAt = target;
		}
	}

	/**
	 * Map status.
	 *
	 * @param status the status
	 * @return the state
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Status mapStatus(String status) throws IOException
	{
		switch (status)
		{
			case "ACTIVE":
			case "TRIAL":
			case "DISCOURAGED":
				return Status.ACTIVE;

			case "DEPRECATED":
				return Status.INACTIVE;

			default :
				log.error("No mapping for status: " + status);
				return Status.ACTIVE;
		}
	}

	/**
	 * Process data line.
	 *
	 * @param fields the fields
	 * @throws ParseException the parse exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void processDataLine(String[] fields) throws ParseException, IOException
	{
		Integer index = this.fieldMap.get("VersionLastChanged"); // They changed this in 2.54 release
		Long time;

		if (index != null)
		{
			time = this.versionTimeMap.get(fields[index]);

			if (time == null)
			{
				throw new IOException("Couldn't find time for version " + fields[index]);
			}
		}
		else
		{
			index = this.fieldMap.get("DATE_LAST_CHANGED"); // They changed this in 2.38 release

			if (index == null)
			{
				index = this.fieldMap.get("DT_LAST_CH");
			}

			final String lastChanged = fields[index];

			time = (StringUtils.isBlank(lastChanged) ? null : this.sdf.parse(lastChanged).getTime());
		}

		final Status status = mapStatus(fields[this.fieldMap.get("STATUS")]);
		final String code = fields[this.fieldMap.get("LOINC_NUM")];
		final UUID concept = dwh.makeConcept(buildUUID(code), status, time);

		for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++)
		{
			if ((fields[fieldIndex] != null) && (fields[fieldIndex].length() > 0))
			{
				final String propertyName = this.fieldMapInverse.get(fieldIndex);


				if (dwh.getAttributeType(propertyName) != null)
				{
					UUID attributeType = dwh.getAttributeType(propertyName);
					if ((propertyName.equals("COMMON_TEST_RANK") || propertyName.equals("COMMON_ORDER_RANK")
							|| propertyName.equals("COMMON_SI_TEST_RANK")) && fields[fieldIndex].equals("0"))
					{
						continue; // Skip attributes of these types when the value is 0
					}
					

					if (dwh.isConfiguredAsIdentifier(attributeType))
					{
						dwh.makeBrittleStringAnnotation(attributeType, concept, fields[fieldIndex], time);
					}
					else
					{
						dwh.makeStringAnnotation(attributeType, concept, fields[fieldIndex], time);
					}
				}
				else if (dwh.getDescriptionType(propertyName) != null)
				{
					UUID descriptionType = dwh.getDescriptionType(propertyName);
					dwh.makeDescriptionEnNoDialect(concept, fields[fieldIndex], descriptionType, status, time);
				}
				else if (dwh.getOtherType(dwh.getOtherMetadataRootType("Axis"), propertyName) != null)
				{
					UUID type = dwh.getOtherType(dwh.getOtherMetadataRootType("Axis"), propertyName);
					
					// See if this class object exists yet.
					final UUID axisConcept = converterUUID.createNamespaceUUIDFromString("Axis:" + this.fieldMapInverse.get(fieldIndex) 
						+ ":" + fields[fieldIndex], true);

					if (!concepts.contains(axisConcept))
					{
						dwh.makeConceptEnNoDialect(axisConcept, fields[fieldIndex], MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
								new UUID[] {type}, status, time);
						concepts.add(axisConcept);
					}
					dwh.makeAssociation(type, concept, axisConcept, time);
				}
				else if (dwh.getOtherType(dwh.getOtherMetadataRootType("Class"), propertyName) != null)
				{
					UUID type = dwh.getOtherType(dwh.getOtherMetadataRootType("Class"), propertyName);
					
					// See if this class object exists yet.
					final UUID classConcept = converterUUID.createNamespaceUUIDFromString("Class:" + this.fieldMapInverse.get(fieldIndex) 
						+ ":" + fields[fieldIndex], true);

					if (!concepts.contains(classConcept))
					{
						dwh.makeConceptEnNoDialect(classConcept, fields[fieldIndex], MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
								new UUID[] {type}, status, time);
						concepts.add(classConcept);
						if (this.classMapping.hasMatch(fields[fieldIndex]))
						{
							dwh.makeStringAnnotation(dwh.getAttributeType("ABBREVIATION"), classConcept, fields[fieldIndex], time);
						}
					}
					dwh.makeAssociation(type, concept, classConcept, time);
				}
				else if (dwh.getAssociationType(propertyName) != null)
				{
					UUID associationType = dwh.getAssociationType(propertyName);
					dwh.makeAssociation(associationType, concept, buildUUID(fields[fieldIndex]), time);
				}
				else if (skippable.contains(propertyName))
				{
					dwh.getLoadStats().addSkippedProperty();
				}
				else
				{
					log.error("ERROR: No property type mapping for the property " + this.fieldMapInverse.get(fieldIndex) + ":" + fields[fieldIndex]);
					continue;
				}
			}
		}

		// MAP_TO moved to a different file in 2.42.
		final HashMap<String, String> mappings = this.mapToData.get(code);

		if (mappings != null)
		{
			mappings.entrySet().forEach((mapping) -> {
				final String target = mapping.getKey();
				final UUID targetUUID = buildUUID(target);
				final String comment = mapping.getValue();
				
				UUID association = dwh.makeAssociation(dwh.getAssociationType("MAP_TO"), concept, targetUUID, time);

				if ((comment != null) && (comment.length() > 0))
				{
					dwh.makeStringAnnotation(dwh.getAttributeType("COMMENT"), association, comment, time);
				}
			});
		}

//		// Now add all the descriptions
//		if (descriptions.isEmpty())
//		{
//			if ("DEL".equals(fields[this.fieldMap.get("CHNG_TYPE")]))
//			{
//				// They put a bunch of these in 2.44... leaving out most of the important info... just makes a mess. Don't load them.
//				this.skippedDeletedItems++;
//				return;
//			}

		if (!concepts.add(concept))
		{
			log.error("Duplicate LOINC code (LOINC_NUM):" + code);
		}
	}

	/**
	 * Process multi axial data.
	 *
	 * @param rootConcept the root concept
	 * @param line the line
	 */
	private void processMultiAxialData(UUID rootConcept, String[] line, long time)
	{
		// PATH_TO_ROOT,SEQUENCE,IMMEDIATE_PARENT,CODE,CODE_TEXT
		// This file format used to be a disaster... but it looks like since 2.40, they encode proper CSV, so I've thrown out the custom parsing.
		// If you need the old custom parser that reads the crap they used to produce as 'CSV', look at the SVN history for this method.
		final String pathString = line[0];
		final String[] pathToRoot = ((pathString.length() > 0) ? pathString.split("\\.") : new String[] {});
		final String sequence = line[1];
		final String immediateParentString = line[2];
		final UUID immediateParent = (((immediateParentString == null) || (immediateParentString.length() == 0)) ? rootConcept
				: buildUUID(immediateParentString));
		final String code = line[3];
		final String codeText = line[4];

		if ((code.length() == 0) || (codeText.length() == 0))
		{
			log.error("missing code or text!");
		}

		final UUID concept = buildUUID(code);

		if (!concepts.contains(concept))
		{
			dwh.makeConcept(concept, Status.ACTIVE, time);

			if (StringUtils.isNotBlank(sequence))
			{
				dwh.makeStringAnnotation(dwh.getAttributeType("SEQUENCE"), concept, sequence, time);
			}

			if (StringUtils.isNotBlank(immediateParentString))
			{
				dwh.makeStringAnnotation(dwh.getAttributeType("IMMEDIATE_PARENT"), concept, immediateParentString, time);
			}
			
			dwh.makeDescriptionEnNoDialect(concept, codeText, dwh.getDescriptionType("CODE_TEXT"), Status.ACTIVE, time);

			HashSet<UUID> parents = this.multiaxialPathsToRoot.get(concept);

			if (parents == null)
			{
				parents = new HashSet<>();
				this.multiaxialPathsToRoot.put(concept, parents);
			}

			parents.add(immediateParent);

			if (!pathString.isEmpty())
			{
				dwh.makeStringAnnotation(dwh.getAttributeType("PATH_TO_ROOT"), concept, pathString, time);
			}

			dwh.makeStringAnnotation(dwh.getAttributeType("LOINC_NUM"), concept, code, time);
			dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), concept, code, time);
			this.concepts.add(concept);
		}

		// Make sure everything in pathToRoot is linked.
		checkPath(concept, pathToRoot);
	}
}

