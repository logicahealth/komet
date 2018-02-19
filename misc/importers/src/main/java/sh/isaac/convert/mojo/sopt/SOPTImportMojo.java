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

package sh.isaac.convert.mojo.sopt;

import java.io.File;
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
import sh.isaac.MetaData;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.convert.mojo.sopt.data.EnumValidatedTableData;
import sh.isaac.convert.mojo.sopt.data.EnumValidatedTableDataReader;
import sh.isaac.convert.mojo.sopt.propertyTypes.PT_Annotations;
import sh.isaac.convert.mojo.sopt.propertyTypes.PT_Descriptions;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Refsets;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;

/**
 * {@link SOPTImportMojo}
 * 
 * Goal which converts SOPT data into the workbench jbin format
 * 
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 */
@Mojo(name = "convert-SOPT-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SOPTImportMojo extends ConverterBaseMojo
{
	private IBDFCreationUtility importUtil_;

	private HashMap<UUID, String> loadedConcepts = new HashMap<>();

	private PropertyType attributes_;
	private PT_Descriptions descriptions_;
	private BPT_Refsets refsets_;

	private Map<String, UUID> parentConcepts = new HashMap<>();

	private final static String REFSET_NAME = "SOPT";
	private final static String REFSET_PROPERTY_NAME = "All SOPT Concepts";
	private final static String COMPONENT_REFERENCE_METADATA = "SOPT Metadata";

	private UUID allSoptConceptsRefset;

	private int conceptCount = 0;

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			super.execute();

			Date date = new Date();

			importUtil_ = new IBDFCreationUtility(Optional.of("SOPT " + converterSourceArtifactVersion), Optional.of(MetaData.SOPT_MODULES____SOLOR),
					outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, false, date.getTime());

			attributes_ = new PT_Annotations();
			descriptions_ = new PT_Descriptions();

			refsets_ = new BPT_Refsets(REFSET_NAME);
			refsets_.addProperty(REFSET_PROPERTY_NAME);

			// Every time concept created, add membership to "All SOPT Concepts"
			allSoptConceptsRefset = refsets_.getProperty(REFSET_PROPERTY_NAME).getUUID();

			// Switch on version to select proper Columns enum to use in constructing reader
			final EnumValidatedTableDataReader<SOPTColumnsV1> importer = new EnumValidatedTableDataReader<>(inputFileLocation, SOPTColumnsV1.class);
			final EnumValidatedTableData<SOPTColumnsV1> terminology = importer.process();

			ConsoleUtil.println("Loaded Terminology containing " + terminology.rows().size() + " entries");

			// COLUMNS from NUCCColumnsV1:
			// Concept Code - Hierarchical numbering eg. 1, 11, 111, 112, 113, 12, 121, 122, 129, 13, 14, 2, 21
			// Concept Name - name of concept
			// Preferred Concept Name - all rows the same as Concept Name except 1.
			// Preferred Alternate Code - all are null
			// Code System OID - 2.16.840.1.113883.3.221.5
			// Code System Name - Source of Payment Typology (PHDSC)
			// Code System Code - PH_SourceOfPaymentTypology_PHDSC
			// Code System Version - 7.0
			// HL7 Table 0396 Code - PHDSCPT

			// Parent soptMetadata ComponentReference
			final ComponentReference soptMetadata = ComponentReference.fromConcept(createType(MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(),
					COMPONENT_REFERENCE_METADATA + IBDFCreationUtility.METADATA_SEMANTIC_TAG));

			// loadTerminologyMetadataAttributes onto soptMetadata
			importUtil_.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			// load metadata
			importUtil_.loadMetaDataItems(Arrays.asList(attributes_, refsets_, descriptions_), soptMetadata.getPrimordialUuid());

			// Create SOPT root concept under SOLOR_CONCEPT____SOLOR
			final ConceptVersion soptRootConcept = importUtil_.createConcept(REFSET_NAME, true, MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid());
			ConsoleUtil.println("Created SOPT root concept " + soptRootConcept.getPrimordialUuid() + " under SOLOR_CONCEPT____SOLOR");

//			// Code System OID
//			final Map<String, ConceptChronology> codeSystemOIDValueConceptByValueMap = new HashMap<>();
//			ConsoleUtil.println("attributes_ is null = " + (attributes_ == null));
//			final UUID codeSystemOIDUuid = attributes_.getProperty(NUCCColumnsV1.CodeSystemOID.toString()).getUUID();
//
//			for (String value : terminology.getDistinctValues(NUCCColumnsV1.CodeSystemOID)) {
//				if (StringUtils.isBlank(value)) {
//					throw new RuntimeException("Cannot load SOPT data with blank Code System OID");
//				}
//
//				// Create the Concept System OID concept as a child of both SOPT root and the Concept Name property
//				// metadata concept and store in map for later retrieval
//				final UUID valueConceptUuid = ConverterUUID
//						.createNamespaceUUIDFromString(codeSystemOIDUuid.toString() + "|" + value, true);
//				final ConceptChronology valueConcept = importUtil_.createConcept(
//						valueConceptUuid, value, null, null, null, codeSystemOIDUuid,
//						soptRootConcept.getPrimordialUuid());
//
//				ConsoleUtil.println("Created SOPT Code value concept " + valueConcept.getPrimordialUuid() + " for \""
//						+ value + "\" under parents Code System OID property " + codeSystemOIDUuid
//						+ " and SOPT root concept " + soptRootConcept);
//
//				// Store Code System OID value concept in map by value
//				codeSystemOIDValueConceptByValueMap.put(value, valueConcept);
//			}
//
//			// Code System Name
//			final Map<String, ConceptChronology> codeSystemNameValueConceptByValueMap = new HashMap<>();
//			final UUID codeSystemNameUuid = attributes_.getProperty(NUCCColumnsV1.CodeSystemName.toString()).getUUID();
//
//			for (String value : terminology.getDistinctValues(NUCCColumnsV1.CodeSystemName)) {
//				if (StringUtils.isBlank(value)) {
//					throw new RuntimeException("Cannot load SOPT data with blank Code System Name");
//				}
//
//				// Create the Concept System Name concept as a child of both SOPT root and the Concept Name property
//				// metadata concept and store in map for later retrieval
//				final UUID valueConceptUuid = ConverterUUID
//						.createNamespaceUUIDFromString(codeSystemNameUuid.toString() + "|" + value, true);
//				final ConceptChronology valueConcept = importUtil_.createConcept(
//						valueConceptUuid, value, null, null, null, codeSystemNameUuid,
//						soptRootConcept.getPrimordialUuid());
//
//				ConsoleUtil.println("Created SOPT Code  value concept " + valueConcept.getPrimordialUuid() + " for \""
//						+ value + "\" under parents Code System Name property " + codeSystemNameUuid
//						+ " and SOPT root concept " + soptRootConcept);
//
//				// Store Code System Name value concept in map by value
//				codeSystemNameValueConceptByValueMap.put(value, valueConcept);
//			}
//
//			// Code System Code
//			final Map<String, ConceptChronology> codeSystemCodeValueConceptByValueMap = new HashMap<>();
//			final UUID codeSystemCodeUuid = attributes_.getProperty(NUCCColumnsV1.CodeSystemCode.toString()).getUUID();
//
//			for (String value : terminology.getDistinctValues(NUCCColumnsV1.CodeSystemCode)) {
//				if (StringUtils.isBlank(value)) {
//					throw new RuntimeException("Cannot load SOPT data with blank Code System Code");
//				}
//
//				// Create the Concept System Code concept as a child of both SOPT root and the Concept Name property
//				// metadata concept and store in map for later retrieval
//				final UUID valueConceptUuid = ConverterUUID
//						.createNamespaceUUIDFromString(codeSystemCodeUuid.toString() + "|" + value, true);
//				final ConceptChronology valueConcept = importUtil_.createConcept(
//						valueConceptUuid, value, null, null, null, codeSystemCodeUuid,
//						soptRootConcept.getPrimordialUuid());
//
//				ConsoleUtil.println("Created SOPT Code  value concept " + valueConcept.getPrimordialUuid() + " for \""
//						+ value + "\" under parents Code System Code property " + codeSystemCodeUuid
//						+ " and SOPT root concept " + soptRootConcept);
//
//				// Store Code System Code value concept in map by value
//				codeSystemCodeValueConceptByValueMap.put(value, valueConcept);
//			}
//
//			// Code System Version
//			final Map<String, ConceptChronology> codeSystemVersionValueConceptByValueMap = new HashMap<>();
//			final UUID codeSystemVersionUuid = attributes_.getProperty(NUCCColumnsV1.CodeSystemVersion.toString())
//					.getUUID();
//
//			for (String value : terminology.getDistinctValues(NUCCColumnsV1.CodeSystemVersion)) {
//				if (StringUtils.isBlank(value)) {
//					throw new RuntimeException("Cannot load SOPT data with blank Code Concept Version");
//				}
//
//				// Create the Concept System Code concept as a child of both SOPT root and the Concept Name property
//				// metadata concept and store in map for later retrieval
//				final UUID valueConceptUuid = ConverterUUID
//						.createNamespaceUUIDFromString(codeSystemCodeUuid.toString() + "|" + value, true);
//				final ConceptChronology valueConcept = importUtil_.createConcept(
//						valueConceptUuid, value, null, null, null, codeSystemVersionUuid,
//						soptRootConcept.getPrimordialUuid());
//
//				ConsoleUtil.println("Created SOPT Code  value concept " + valueConcept.getPrimordialUuid() + " for \""
//						+ value + "\" under parents Code System Version property " + codeSystemVersionUuid
//						+ " and SOPT root concept " + soptRootConcept);
//
//				// Store Code System Version value concept in map by value
//				codeSystemVersionValueConceptByValueMap.put(value, valueConcept);
//			}
//
//			// HL7 Table 0396 Code
//			final Map<String, ConceptChronology> hl7Table3096CodeValueConceptByValueMap = new HashMap<>();
//			final UUID hl7Table3096CodeUuid = attributes_.getProperty(NUCCColumnsV1.HL7Table0396Code.toString()).getUUID();
//
//			for (String value : terminology.getDistinctValues(NUCCColumnsV1.HL7Table0396Code)) {
//				if (StringUtils.isBlank(value)) {
//					throw new RuntimeException("Cannot load SOPT data with blank HL7 Table 0396 Code");
//				}
//
//				// Create the Concept System Code concept as a child of both SOPT root and the Concept Name property
//				// metadata concept and store in map for later retrieval
//				final UUID valueConceptUuid = ConverterUUID
//						.createNamespaceUUIDFromString(codeSystemCodeUuid.toString() + "|" + value, true);
//				final ConceptChronology valueConcept = importUtil_.createConcept(
//						valueConceptUuid, value, null, null, null, codeSystemVersionUuid,
//						soptRootConcept.getPrimordialUuid());
//
//				ConsoleUtil.println("Created SOPT Code  value concept " + valueConcept.getPrimordialUuid() + " for \""
//						+ value + "\" under parents HL7 Table 3096 property " + hl7Table3096CodeUuid
//						+ " and SOPT root concept " + soptRootConcept);
//
//				// Store HL7 Table 0396 Code value concept in map by value
//				hl7Table3096CodeValueConceptByValueMap.put(value, valueConcept);
//			}

			// Populate hierarchy, one row at a time
			for (Map<SOPTColumnsV1, String> row : terminology.rows())
			{

				// if code is not a number, it is the header row, skip the row
				if (!StringUtils.isNumeric(row.get(SOPTColumnsV1.ConceptCode)))
				{

//					final ConceptChronology codeSystemOIDConcept = row
//							.get(NUCCColumnsV1.CodeSystemName) != null
//									? codeSystemOIDValueConceptByValueMap.get(row.get(NUCCColumnsV1.CodeSystemOID)) : null;
//
//					final ConceptChronology codeSystemNameConcept = row
//							.get(NUCCColumnsV1.CodeSystemName) != null
//									? codeSystemNameValueConceptByValueMap.get(row.get(NUCCColumnsV1.CodeSystemName))
//									: null;
//
//					final ConceptChronology codeSystemCodeConcept = row
//							.get(NUCCColumnsV1.CodeSystemVersion) != null
//									? codeSystemCodeValueConceptByValueMap.get(row.get(NUCCColumnsV1.CodeSystemCode))
//									: null;
//
//					final ConceptChronology codeSystemVersionConcept = row
//							.get(NUCCColumnsV1.CodeSystemVersion) != null
//									? codeSystemVersionValueConceptByValueMap.get(row.get(NUCCColumnsV1.CodeSystemVersion))
//									: null;
//
//					final ConceptChronology hl7Table0396CodeConcept = row
//							.get(NUCCColumnsV1.HL7Table0396Code) != null
//									? hl7Table3096CodeValueConceptByValueMap.get(row.get(NUCCColumnsV1.HL7Table0396Code))
//									: null;
//					
//					//(ComponentReference referencedComponent, UUID uuidForCreatedAnnotation, 
//					//DynamicData value, UUID refexDynamicTypeUuid, Status state, Long time)
//									
//					// add annotations Code System OID NID
//					importUtil_.addAnnotation(ComponentReference.fromChronology(codeSystemOIDConcept), soptMetadata.getPrimordialUuid(),
//							new DynamicNidImpl(codeSystemOIDConcept.getNid()), codeSystemOIDUuid, Status.ACTIVE,
//							(Long) null);
//
//					// add annotations Code System Name NID
//					importUtil_.addAnnotation(ComponentReference.fromChronology(codeSystemNameConcept), soptMetadata.getPrimordialUuid(),
//							new DynamicNidImpl(codeSystemNameConcept.getNid()), codeSystemNameUuid, Status.ACTIVE,
//							(Long) null);
//
//					// add annotations Code System Code NID
//					importUtil_.addAnnotation(ComponentReference.fromChronology(codeSystemVersionConcept), soptMetadata.getPrimordialUuid(),
//							new DynamicNidImpl(codeSystemOIDConcept.getNid()), codeSystemCodeUuid, Status.ACTIVE,
//							(Long) null);
//
//					// add annotations Code System Version NID
//					importUtil_.addAnnotation(ComponentReference.fromChronology(codeSystemVersionConcept), soptMetadata.getPrimordialUuid(),
//							new DynamicNidImpl(codeSystemVersionConcept.getNid()), codeSystemVersionUuid,
//							Status.ACTIVE, (Long) null);
//
//					// add annotations Code System HL7 Table 0396 code NID
//					importUtil_.addAnnotation(ComponentReference.fromChronology(hl7Table0396CodeConcept), soptMetadata.getPrimordialUuid(),
//							new DynamicNidImpl(hl7Table0396CodeConcept.getNid()), hl7Table3096CodeUuid,
//							Status.ACTIVE, (Long) null);
					continue;
				}

				// 1, 11, 111, 112, 113, 12, 121, 122, 123, 129, 13, 14, 2, 21
				// ...
				String conceptCode;
				String conceptName;
				String preferredConceptCode;
				String preferredConceptName;

				try
				{

					conceptCode = row.get(SOPTColumnsV1.ConceptCode);
					conceptName = row.get(SOPTColumnsV1.ConceptName);
					preferredConceptCode = row.get(SOPTColumnsV1.PreferredAlternateCode);
					preferredConceptName = row.get(SOPTColumnsV1.PreferredConceptName);

					UUID parentUuid = findParentUuid(conceptCode);

					ConsoleUtil.println("Creating " + conceptCode + " - " + conceptName);

					// String name, boolean skipDupeCheck
					UUID rowConceptUuid = ConverterUUID.createNamespaceUUIDFromString(conceptCode + "|" + conceptName, true);

					// add to map
					parentConcepts.put(conceptCode, rowConceptUuid);
					final ConceptVersion rowConcept;

					rowConcept = importUtil_.createConcept(rowConceptUuid, // UUID conceptPrimordialUuid,
							conceptName, // String fsn,
							true, // boolean createSynonymFromFSN,
							(parentUuid == null) ? soptRootConcept.getPrimordialUuid() : parentUuid); // relParentPrimordial);

					final ComponentReference rowComponentReference = ComponentReference.fromConcept(rowConcept);

					importUtil_.addDescription(rowComponentReference, conceptCode, DescriptionType.FULLY_QUALIFIED_NAME, true, null, Status.ACTIVE);

					if (!conceptName.equals(preferredConceptName))
					{
						importUtil_.addDescription(rowComponentReference, preferredConceptName, DescriptionType.REGULAR_NAME, true, null, Status.ACTIVE);
					}

					if (preferredConceptCode != null && StringUtils.isNotEmpty(preferredConceptCode))
					{
						addDescription(rowComponentReference, preferredConceptCode, DescriptionType.REGULAR_NAME,
								descriptions_.getProperty("PreferredConceptCode").getUUID(), false);
					}

					importUtil_.addStaticStringAnnotation(rowComponentReference, conceptCode, MetaData.CODE____SOLOR.getPrimordialUuid(), Status.ACTIVE);

					// add refset
					importUtil_.addAssemblageMembership(rowComponentReference, allSoptConceptsRefset, Status.ACTIVE, (Long) null);

					++conceptCount;

				}
				catch (Exception e)
				{
					final String msg = "Failed processing row with " + e.getClass().getSimpleName() + " " + e.getLocalizedMessage() + ": " + row;
					ConsoleUtil.println(msg);
					throw new RuntimeException(msg, e);
				}
			}

			ConsoleUtil.println("Metadata load stats");
			for (String line : importUtil_.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}
			importUtil_.clearLoadStats();

			ConsoleUtil.println("Processed " + conceptCount + " concepts");

			ConsoleUtil.println("Load Statistics");

			// this could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			ConverterUUID.dump(outputDirectory, "soptUuid");

			importUtil_.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}

	// find UUID of parent record. eg. the parent of record 111 is 11.
	// there may be an instance where the parent of a record is not the
	// same key less 1 character. eg. 9999's parent record is 99
	private UUID findParentUuid(String conceptCode)
	{

		UUID parentUUID = null;
		if (conceptCode != null && conceptCode.length() > 1)
		{
			for (int i = conceptCode.length() - 1; i > 0; i--)
			{
				parentUUID = parentConcepts.get(conceptCode.substring(0, i));
				if (parentUUID != null)
				{
					break;
				}
			}
		}
		return parentUUID;
	}

	private void addDescription(ComponentReference concept, String text, DescriptionType descriptionType, UUID extendedType, boolean preferred)
	{
		UUID descriptionPrimordialUUID = ConverterUUID.createNamespaceUUIDFromStrings(concept.getPrimordialUuid().toString(), text, extendedType.toString(),
				descriptionType.name(), new Boolean(preferred).toString());
		importUtil_.addDescription(concept, descriptionPrimordialUUID, text, descriptionType, preferred, extendedType, Status.ACTIVE);
	}

	private ConceptVersion createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptVersion concept = importUtil_.createConcept(typeName, true);
		loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil_.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	public static void main(String[] args) throws MojoExecutionException
	{
		SOPTImportMojo i = new SOPTImportMojo();
		i.outputDirectory = new File("../sopt-ibdf/target");
		i.inputFileLocation = new File("../sopt-ibdf/target/generated-resources/src/");
		i.converterOutputArtifactVersion = "2016.06";
		i.converterVersion = "SNAPSHOT";
		i.converterSourceArtifactVersion = "7.0";
		i.execute();
	}
}