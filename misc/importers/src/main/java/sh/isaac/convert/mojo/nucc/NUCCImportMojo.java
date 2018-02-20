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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import javafx.application.Platform;
import sh.isaac.MetaData;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.convert.mojo.nucc.data.EnumValidatedTableData;
import sh.isaac.convert.mojo.nucc.data.EnumValidatedTableDataReader;
import sh.isaac.convert.mojo.nucc.propertyTypes.PT_Annotations;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Refsets;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;

/**
 * 
 * {@link NUCCImportMojo}
 *
 * Goal which converts NUCC data into the workbench jbin format
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Mojo(name = "convert-NUCC-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class NUCCImportMojo extends ConverterBaseMojo
{
	private HashMap<UUID, String> loadedConcepts = new HashMap<>();

	private PropertyType attributes_;
	private BPT_Refsets refsets_;

	private UUID allNuccConceptsRefset;

	@Override
	public void execute() throws MojoExecutionException
	{
		final Map<String, ConceptVersion> groupingValueConceptByValueMap = new HashMap<>();
		final Map<String, ConceptVersion> classificationValueConceptByValueMap = new HashMap<>();
		final Map<String, ConceptVersion> specializationValueConceptByValueMap = new HashMap<>();

		try
		{
			super.execute();

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

			importUtil = new IBDFCreationUtility(Optional.of("NUCC" + " " + converterSourceArtifactVersion), Optional.of(MetaData.NUCC_MODULES____SOLOR),
					outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, false, date.getTime());

			attributes_ = new PT_Annotations();

			refsets_ = new BPT_Refsets("NUCC");
			refsets_.addProperty("All NUCC Concepts");

			// Every time concept created add membership to "All NUCC Concepts"
			allNuccConceptsRefset = refsets_.getProperty("All NUCC Concepts").getUUID();

			// Switch on version to select proper Columns enum to use in constructing reader
			final EnumValidatedTableDataReader<NUCCColumnsV1> importer = new EnumValidatedTableDataReader<>(inputFileLocation, NUCCColumnsV1.class);
			final EnumValidatedTableData<NUCCColumnsV1> terminology = importer.process();

			ConsoleUtil.println("Loaded Terminology containing " + terminology.rows().size() + " entries");

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
			final ComponentReference nuccMetadata = ComponentReference.fromConcept(
					createType(MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(), "NUCC Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG));

			// loadTerminologyMetadataAttributes onto nuccMetadata
			importUtil.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			// load metadata
			importUtil.loadMetaDataItems(Arrays.asList(attributes_, refsets_), nuccMetadata.getPrimordialUuid());

			// Create NUCC root concept under SOLOR_CONCEPT____SOLOR
			final ConceptVersion nuccRootConcept = importUtil.createConcept("NUCC", true, MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid());
			ConsoleUtil.println("Created NUCC root concept " + nuccRootConcept.getPrimordialUuid() + " under SOLOR_CONCEPT____SOLOR");

			ConsoleUtil.println("Metadata load stats");
			for (String line : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}

			ConsoleUtil.println("Processed " + importUtil.getLoadStats().getConceptCount() + " metadata concepts");

			importUtil.clearLoadStats();

			// Create concepts for each unique value in each of three Grouping, Classification and Specialization columns
			// Each concept is created as a child of its respective column type concept
			// A map of String value to ConceptChronology is maintained for later use
			final UUID groupingPropertyUuid = attributes_.getProperty(NUCCColumnsV1.Grouping.toString()).getUUID();
			for (String value : terminology.getDistinctValues(NUCCColumnsV1.Grouping))
			{
				if (StringUtils.isBlank(value))
				{
					throw new RuntimeException("Cannot load NUCC data with blank Grouping");
				}

				// Create the Grouping value concept as a child of both NUCC root and the Grouping property metadata concept
				// and store in map for later retrieval
				final UUID valueConceptUuid = ConverterUUID.createNamespaceUUIDFromString(groupingPropertyUuid.toString() + "|" + value, true);
				final ConceptVersion valueConcept = importUtil.createConcept(valueConceptUuid, value, null, null, null, groupingPropertyUuid,
						nuccRootConcept.getPrimordialUuid());
				// ConsoleUtil.println("Created NUCC Grouping value concept " + valueConcept.getPrimordialUuid() + " for \"" + value + "\" under
				// parents Grouping property " + groupingPropertyUuid + " and NUCC root concept " + nuccRootConcept);

				// Store Grouping value concept in map by value
				groupingValueConceptByValueMap.put(value, valueConcept);
			}

			// Create a concept for each distinct non blank Classification value and store in map for later retrieval
			final UUID classificationPropertyUuid = attributes_.getProperty(NUCCColumnsV1.Classification.toString()).getUUID();
			for (String value : terminology.getDistinctValues(NUCCColumnsV1.Classification))
			{
				if (StringUtils.isNotBlank(value))
				{
					final UUID valueConceptUuid = ConverterUUID.createNamespaceUUIDFromString(classificationPropertyUuid.toString() + "|" + value, true);
					final ConceptVersion valueConcept = importUtil.createConcept(valueConceptUuid, value, true, classificationPropertyUuid);
					// ConsoleUtil.println("Created NUCC Classification value concept " + valueConcept.getPrimordialUuid() + " for \"" + value + "\"
					// under parent Classification property concept " + classificationPropertyUuid);
					// Store Classification value concept in map by value
					classificationValueConceptByValueMap.put(value, valueConcept);
				}
			}

			// Create a concept for each distinct Specialization value and store in map for later retrieval
			final UUID specializationPropertyUuid = attributes_.getProperty(NUCCColumnsV1.Specialization.toString()).getUUID();
			for (String value : terminology.getDistinctValues(NUCCColumnsV1.Specialization))
			{
				if (StringUtils.isNotBlank(value))
				{
					final UUID valueConceptUuid = ConverterUUID.createNamespaceUUIDFromString(specializationPropertyUuid.toString() + "|" + value, true);
					final ConceptVersion valueConcept = importUtil.createConcept(valueConceptUuid, value, true, specializationPropertyUuid);
					// ConsoleUtil.println("Created NUCC Specialization value concept " + valueConcept.getPrimordialUuid() + " for \"" + value + "\"
					// under parent Specialization property concept " + classificationPropertyUuid);
					// Store Specialization value concept in map by value
					specializationValueConceptByValueMap.put(value, valueConcept);
				}
			}

			final UUID codePropertyUuid = attributes_.getProperty(NUCCColumnsV1.Code.toString()).getUUID();

			int dataRows = 0;
			// Populate hierarchy, one row at a time, creating concepts as children of their respective Grouping concepts
			for (Map<NUCCColumnsV1, String> row : terminology.rows())
			{
				final ConceptVersion groupingValueConcept = row.get(NUCCColumnsV1.Grouping) != null
						? groupingValueConceptByValueMap.get(row.get(NUCCColumnsV1.Grouping))
						: null;
				final ConceptVersion classificationValueConcept = row.get(NUCCColumnsV1.Classification) != null
						? classificationValueConceptByValueMap.get(row.get(NUCCColumnsV1.Classification))
						: null;
				final ConceptVersion specializationValueConcept = row.get(NUCCColumnsV1.Specialization) != null
						? specializationValueConceptByValueMap.get(row.get(NUCCColumnsV1.Specialization))
						: null;

				if (groupingValueConcept == null)
				{
					throw new RuntimeException("Cannot create NUCC concept without Grouping: " + row.toString());
				}

				try
				{
					// Create row concept
					UUID rowConceptUuid = ConverterUUID
							.createNamespaceUUIDFromString(groupingValueConcept.getPrimordialUuid().toString() + "|" + row.get(NUCCColumnsV1.Code), true);
					final ConceptVersion rowConcept = importUtil.createConcept(rowConceptUuid, row.get(NUCCColumnsV1.Code), true,
							groupingValueConcept.getPrimordialUuid());
					final ComponentReference rowComponentReference = ComponentReference.fromConcept(rowConcept);

					// Add required NUCC Code annotation
					importUtil.addStaticStringAnnotation(rowComponentReference, row.get(NUCCColumnsV1.Code), codePropertyUuid, Status.ACTIVE);

					// Add required Grouping NID annotation
					importUtil.addAnnotation(rowComponentReference, null, new DynamicNidImpl(groupingValueConcept.getNid()), groupingPropertyUuid,
							Status.ACTIVE, (Long) null);

					// Add optional Classification NID annotation
					if (classificationValueConcept != null)
					{
						importUtil.addAnnotation(rowComponentReference, null, new DynamicNidImpl(classificationValueConcept.getNid()),
								classificationPropertyUuid, Status.ACTIVE, (Long) null);
					}
					// Add optional Specialization NID annotation
					if (specializationValueConcept != null)
					{
						importUtil.addAnnotation(rowComponentReference, null, new DynamicNidImpl(specializationValueConcept.getNid()),
								specializationPropertyUuid, Status.ACTIVE, (Long) null);
					}

					// Add optional Notes comment annotation
					if (StringUtils.isNotBlank(row.get(NUCCColumnsV1.Notes)))
					{
						importUtil.addAnnotation(rowComponentReference, null, new DynamicStringImpl(row.get(NUCCColumnsV1.Notes)),
								DynamicConstants.get().DYNAMIC_COMMENT_ATTRIBUTE.getPrimordialUuid(), Status.ACTIVE, (Long) null);
					}

					// Add optional Definition description
					if (StringUtils.isNotBlank(row.get(NUCCColumnsV1.Definition)))
					{
						importUtil.addDescription(rowComponentReference, row.get(NUCCColumnsV1.Definition), DescriptionType.DEFINITION, false, (UUID) null,
								Status.ACTIVE);
					}

					// Add to refset allNuccConceptsRefset
					importUtil.addAssemblageMembership(rowComponentReference, allNuccConceptsRefset, Status.ACTIVE, (Long) null);

					++dataRows;
				}
				catch (Exception e)
				{
					final String msg = "Failed processing row with " + e.getClass().getSimpleName() + " " + e.getLocalizedMessage() + ": " + row;
					ConsoleUtil.println(msg);
					throw new RuntimeException(msg, e);
				}
			}

			ConsoleUtil.println("Load stats");
			for (String line : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}

			ConsoleUtil.println("Processed " + dataRows + " data rows");
			ConsoleUtil.println("Processed " + importUtil.getLoadStats().getConceptCount() + " total concepts");

			ConsoleUtil.println("Processed " + groupingValueConceptByValueMap.size() + " distinct NUCC " + NUCCColumnsV1.Grouping + " concepts");
			ConsoleUtil.println("Processed " + classificationValueConceptByValueMap.size() + " distinct NUCC " + NUCCColumnsV1.Classification + " concepts");
			ConsoleUtil.println("Processed " + specializationValueConceptByValueMap.size() + " distinct NUCC " + NUCCColumnsV1.Specialization + " concepts");

			ConsoleUtil.println("Load Statistics");

			// this could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			ConverterUUID.dump(outputDirectory, "nuccUuid");

			importUtil.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}

	private ConceptVersion createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptVersion concept = importUtil.createConcept(typeName, true);
		loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	public static void main(String[] args) throws MojoExecutionException
	{
		NUCCImportMojo i = new NUCCImportMojo();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocation= new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
		i.converterOutputArtifactVersion = "2016.01.07.foo";
		i.converterVersion = "SNAPSHOT";
		i.converterSourceArtifactVersion = "17.0";
		i.execute();
		Platform.exit();
	}
}