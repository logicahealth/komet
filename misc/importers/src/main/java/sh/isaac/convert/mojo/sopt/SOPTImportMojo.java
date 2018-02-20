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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import javafx.application.Platform;
import sh.isaac.MetaData;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.convert.mojo.sopt.data.EnumValidatedXSLFileReader;
import sh.isaac.convert.mojo.sopt.data.SOPTDataColumnsV1;
import sh.isaac.convert.mojo.sopt.data.SOPTValueSetColumnsV1;
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

			importUtil = new IBDFCreationUtility(Optional.of("SOPT " + converterSourceArtifactVersion), Optional.of(MetaData.SOPT_MODULES____SOLOR),
					outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, false, date.getTime());

			attributes_ = new PT_Annotations();
			descriptions_ = new PT_Descriptions();

			refsets_ = new BPT_Refsets(REFSET_NAME);
			refsets_.addProperty(REFSET_PROPERTY_NAME);

			// Every time concept created, add membership to "All SOPT Concepts"
			allSoptConceptsRefset = refsets_.getProperty(REFSET_PROPERTY_NAME).getUUID();

			// Switch on version to select proper Columns enum to use in constructing reader
			final EnumValidatedXSLFileReader data = EnumValidatedXSLFileReader.readZip(inputFileLocation);
			
			ConsoleUtil.println("Loaded Terminology containing " + data.getValueSetData().size() + " entries");

			// COLUMNS from SOPTDataColumnsV1:
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
			importUtil.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			// load metadata
			importUtil.loadMetaDataItems(Arrays.asList(attributes_, refsets_, descriptions_), soptMetadata.getPrimordialUuid());

			// Create SOPT root concept under SOLOR_CONCEPT____SOLOR
			final ComponentReference soptRootConcept = ComponentReference.fromConcept(importUtil.createConcept(REFSET_NAME, true, 
					MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()));
			ConsoleUtil.println("Created SOPT root concept " + soptRootConcept.getPrimordialUuid() + " under SOLOR_CONCEPT____SOLOR");
			
			ComponentReference valueSetConcept = null;
			for (Entry<SOPTValueSetColumnsV1, String> md : data.getValueSetMetaData().entrySet())
			{
				if (StringUtils.isNotBlank(md.getValue()))
				{
					switch (md.getKey()) {
						case ValueSetCode:
							importUtil.addStaticStringAnnotation(valueSetConcept, md.getValue(), attributes_.getProperty(md.getKey().name()).getUUID(), Status.ACTIVE);
							break;
						case ValueSetDefinition:
							importUtil.addDescription(valueSetConcept, md.getValue(), DescriptionType.DEFINITION, true, 
									descriptions_.getProperty(md.getKey().name()).getUUID(), Status.ACTIVE);
							break;
						case ValueSetName:
							//Due to the order of the data in the metadata, this case will hit first as we iterate.
							valueSetConcept = ComponentReference.fromConcept(importUtil.createConcept(
									ConverterUUID.createNamespaceUUIDFromString("ValueSet|" + md.getValue()), md.getValue(), false, soptRootConcept.getPrimordialUuid()));
							importUtil.addDescription(valueSetConcept, md.getValue(), DescriptionType.REGULAR_NAME, true, 
									descriptions_.getProperty(md.getKey().name()).getUUID(), Status.ACTIVE);
							
							importUtil.configureConceptAsDynamicRefex(valueSetConcept, "Holds the Value Set members from SOPT", null, IsaacObjectType.CONCEPT, null);
							
							break;
						case ValueSetOID:
						case ValueSetReleaseComments:
						case ValueSetStatus:
						case ValueSetUpdatedDate:
						case ValueSetVersion:
							importUtil.addStringAnnotation(valueSetConcept, md.getValue(), attributes_.getProperty(md.getKey().name()).getUUID(), Status.ACTIVE);
							break;
						default :
							throw new RuntimeException("Unmapped enum type");
					}
				}
			}

			HashMap<SOPTDataColumnsV1, Function<String[], String>> dataFetchers = new HashMap<>();
			AtomicInteger i = new AtomicInteger(0);
			for (SOPTDataColumnsV1 colType : data.getDataHeaders())
			{
				int col = i.getAndIncrement();
				dataFetchers.put(colType, (dataIn) -> dataIn[col]);
			}
			
			HashMap<String, ComponentReference> codeSystems = new HashMap<>();
			
			for (String[] row : data.getValueSetData())
			{
				String conceptCode = dataFetchers.get(SOPTDataColumnsV1.ConceptCode).apply(row); 
				String conceptName = dataFetchers.get(SOPTDataColumnsV1.ConceptName).apply(row);
				String preferredConceptName = dataFetchers.get(SOPTDataColumnsV1.PreferredConceptName).apply(row);
				String preferredAltCode = dataFetchers.get(SOPTDataColumnsV1.PreferredAlternateCode).apply(row);
				
				String codeSystemOid = dataFetchers.get(SOPTDataColumnsV1.CodeSystemOID).apply(row);
				String codeSystemName = dataFetchers.get(SOPTDataColumnsV1.CodeSystemName).apply(row);
				String codeSystemCode = dataFetchers.get(SOPTDataColumnsV1.CodeSystemCode).apply(row);
				String codeSystemVersion = dataFetchers.get(SOPTDataColumnsV1.CodeSystemVersion).apply(row);
				String hl7TableCode = dataFetchers.get(SOPTDataColumnsV1.HL7Table0396Code).apply(row);
				
				String key = codeSystemOid + codeSystemName + codeSystemCode + codeSystemVersion + hl7TableCode;
				
				ComponentReference codeSystem = codeSystems.get(key);
				if (codeSystem == null)
				{
					codeSystem = ComponentReference.fromConcept(importUtil.createConcept(
							ConverterUUID.createNamespaceUUIDFromString("CodeSystemSet|" + codeSystemName), codeSystemName,  false, soptRootConcept.getPrimordialUuid()));
					importUtil.addDescription(codeSystem, codeSystemName, DescriptionType.REGULAR_NAME, true, 
							descriptions_.getProperty(SOPTDataColumnsV1.CodeSystemName.name()).getUUID(), Status.ACTIVE);
					importUtil.addStaticStringAnnotation(codeSystem, codeSystemCode, attributes_.getProperty(SOPTDataColumnsV1.CodeSystemCode.name()).getUUID(), Status.ACTIVE);
					importUtil.addStringAnnotation(codeSystem, codeSystemOid, attributes_.getProperty(SOPTDataColumnsV1.CodeSystemOID.name()).getUUID(), Status.ACTIVE);
					importUtil.addStringAnnotation(codeSystem, codeSystemVersion, attributes_.getProperty(SOPTDataColumnsV1.CodeSystemVersion.name()).getUUID(), Status.ACTIVE);
					importUtil.addStaticStringAnnotation(codeSystem, hl7TableCode, attributes_.getProperty(SOPTDataColumnsV1.HL7Table0396Code.name()).getUUID(), Status.ACTIVE);
					codeSystems.put(key, codeSystem);
				}
				
				
				UUID rowConceptUuid = ConverterUUID.createNamespaceUUIDFromString(conceptCode + "|" + conceptName);
				UUID parentUuid = findParentUuid(conceptCode);
				parentUuid = parentUuid == null ? codeSystem.getPrimordialUuid() : parentUuid;
				parentConcepts.put(conceptCode, rowConceptUuid);
				
				final ComponentReference cr = ComponentReference.fromConcept(importUtil.createConcept(rowConceptUuid));
				importUtil.addParent(cr, parentUuid);
				
				importUtil.addDescription(cr, conceptName, DescriptionType.FULLY_QUALIFIED_NAME, true, 
						descriptions_.getProperty(SOPTDataColumnsV1.ConceptName.name()).getUUID(), Status.ACTIVE);
				
				importUtil.addDescription(cr, conceptName, DescriptionType.REGULAR_NAME, true, 
						descriptions_.getProperty(SOPTDataColumnsV1.ConceptName.name()).getUUID(), Status.ACTIVE);
				
				if (StringUtils.isNotBlank(preferredConceptName) && !preferredConceptName.equals(conceptName))
				{
					importUtil.addDescription(cr, preferredConceptName, DescriptionType.REGULAR_NAME, true, 
							descriptions_.getProperty(SOPTDataColumnsV1.PreferredConceptName.name()).getUUID(), Status.ACTIVE);
				}
				
				importUtil.addStaticStringAnnotation(cr, conceptCode, attributes_.getProperty(SOPTDataColumnsV1.ConceptCode.name()).getUUID(), Status.ACTIVE);
				if (StringUtils.isNotBlank(preferredAltCode))
				{
					importUtil.addStaticStringAnnotation(cr, conceptCode, attributes_.getProperty(SOPTDataColumnsV1.PreferredAlternateCode.name()).getUUID(), Status.ACTIVE);
				}
				
				importUtil.addAssemblageMembership(cr, valueSetConcept.getPrimordialUuid(), Status.ACTIVE, (Long) null);
				importUtil.addAssemblageMembership(cr, allSoptConceptsRefset, Status.ACTIVE, (Long) null);
				++conceptCount;
			}

			ConsoleUtil.println("Metadata load stats");
			for (String line : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}
			importUtil.clearLoadStats();

			ConsoleUtil.println("Processed " + conceptCount + " concepts");

			ConsoleUtil.println("Load Statistics");

			// this could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			ConverterUUID.dump(outputDirectory, "soptUuid");

			importUtil.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());

		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected error", e);
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

	private ConceptVersion createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptVersion concept = importUtil.createConcept(typeName, true);
		loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	public static void main(String[] args) throws MojoExecutionException
	{
		SOPTImportMojo i = new SOPTImportMojo();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocation = new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
		i.converterOutputArtifactVersion = "2016.06";
		i.converterVersion = "SNAPSHOT";
		i.converterSourceArtifactVersion = "7.0";
		i.execute();
		Platform.exit();
	}
}