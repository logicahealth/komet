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

import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.mojo.sopt.data.EnumValidatedXSLFileReader;
import sh.isaac.convert.mojo.sopt.data.SOPTDataColumnsV1;
import sh.isaac.convert.mojo.sopt.data.SOPTValueSetColumnsV1;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * {@link SOPTImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class SOPTImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	private Map<String, UUID> parentConcepts = new HashMap<>();

	private final static String REFSET_PROPERTY_NAME = "All SOPT Concepts";

	private UUID allSoptConceptsRefset;

	private int conceptCount = 0;

	/**
	 * This constructor is for HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link DirectConverter#configure(File, Path, String, StampFilter, Transaction)} method on it.
	 */
	protected SOPTImportHK2Direct() 
	{
		super();
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

	@Override
	public SupportedConverterTypes[] getSupportedTypes()
	{
		return new SupportedConverterTypes[] {SupportedConverterTypes.SOPT};
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer, BiConsumer)
	 * @see DirectConverter#convertContent(Consumer, BiConsumer)
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdate) throws IOException
	{
		statusUpdates.accept("Reading content");

		// Switch on version to select proper Columns enum to use in constructing reader
		final EnumValidatedXSLFileReader data = EnumValidatedXSLFileReader.readZip(inputFileLocationPath);

		String date = data.getValueSetMetaData().get(SOPTValueSetColumnsV1.ValueSetUpdatedDate);
		
		
		long contentTime;
		try
		{
			contentTime = new SimpleDateFormat("MM/dd/yy").parse(date).getTime();
		}
		catch (ParseException e)
		{
			throw new IOException("Failed to parse content time / date from '" + date + "'");
		}
		log.info("Loaded Terminology containing " + data.getValueSetData().size() + " entries");
		
		statusUpdates.accept("Setting up metadata");
		
		//Right now, we are configured for the CPT grouping modules nid
		dwh = new DirectWriteHelper(transaction, TermAux.USER.getNid(), MetaData.SOPT_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID, 
				"SOPT", false);
		
		setupModule("SOPT", MetaData.SOPT_MODULES____SOLOR.getPrimordialUuid(), Optional.empty(), contentTime);
		
		//Set up our metadata hierarchy
		dwh.makeMetadataHierarchy(true, true, true, false, true, false, contentTime);

		dwh.makeDescriptionTypeConcept(null, SOPTDataColumnsV1.ConceptName.name(), null, null,
				MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		dwh.makeDescriptionTypeConcept(null, SOPTDataColumnsV1.CodeSystemName.name(), null, null,
				MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		dwh.makeDescriptionTypeConcept(null, SOPTDataColumnsV1.PreferredConceptName.name(), null, null,
				MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		dwh.makeDescriptionTypeConcept(null, SOPTValueSetColumnsV1.ValueSetName.name(), null, null,
				MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);

		dwh.makeDescriptionTypeConcept(null, SOPTValueSetColumnsV1.ValueSetDefinition.name(), null, null,
				MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		//This appears in both the concept and valueset tabs, one is likely an error, just make it once.  It doesn't appear to be used yet anyway.
		dwh.makeDescriptionTypeConcept(null, SOPTDataColumnsV1.ValueSetConceptDefinitionText.name(), null, null,
				MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
		
		
		dwh.makeAttributeTypeConcept(null, SOPTDataColumnsV1.CodeSystemOID.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTDataColumnsV1.CodeSystemVersion.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTDataColumnsV1.CodeSystemCode.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTDataColumnsV1.ConceptCode.name(), null, null, true, null, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTDataColumnsV1.HL7Table0396Code.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTDataColumnsV1.PreferredAlternateCode.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTValueSetColumnsV1.ValueSetCode.name(), null, null, true, null, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTValueSetColumnsV1.ValueSetOID.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTValueSetColumnsV1.ValueSetReleaseComments.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTValueSetColumnsV1.ValueSetStatus.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTValueSetColumnsV1.ValueSetUpdatedDate.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, SOPTValueSetColumnsV1.ValueSetVersion.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		
		// Every time concept created add membership to "All CPT Concepts"
		allSoptConceptsRefset = dwh.makeRefsetTypeConcept(null, REFSET_PROPERTY_NAME, null, null, contentTime);

		// Create SOPT root concept under SOLOR_CONCEPT____SOLOR
		final UUID soptRootConcept = dwh.makeConceptEnNoDialect(null, "SOPT", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
				new UUID[] {MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()}, Status.ACTIVE, contentTime);

		log.info("Metadata load stats");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}
		
		dwh.clearLoadStats();
		
		statusUpdates.accept("Loading content");

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
		
		UUID valueSetConcept = null;
		for (Entry<SOPTValueSetColumnsV1, String> md : data.getValueSetMetaData().entrySet())
		{
			if (StringUtils.isNotBlank(md.getValue()))
			{
				switch (md.getKey()) {
					case ValueSetName:
						//Due to the order of the data in the metadata, this case will hit first as we iterate.
						valueSetConcept = converterUUID.createNamespaceUUIDFromString("ValueSet|" + md.getValue());
						dwh.makeConcept(valueSetConcept, Status.ACTIVE, contentTime);
						dwh.makeParentGraph(valueSetConcept, dwh.getRefsetTypesNode().get(), Status.ACTIVE, contentTime);
						dwh.makeDescriptionEn(valueSetConcept, md.getValue(), dwh.getDescriptionType(md.getKey().name()), MetaData.NOT_APPLICABLE____SOLOR.getPrimordialUuid(),
								Status.ACTIVE, contentTime, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
						
						dwh.configureConceptAsDynamicAssemblage(valueSetConcept, "Holds the Value Set members from SOPT", null, IsaacObjectType.CONCEPT,
								null, contentTime);
						break;
					case ValueSetCode:
						dwh.makeBrittleStringAnnotation(dwh.getAttributeType(md.getKey().name()), valueSetConcept, md.getValue(), contentTime);
						break;
					case ValueSetDefinition:
					case ValueSetConceptDefinitionText:
						dwh.makeDescriptionEnNoDialect(valueSetConcept, md.getValue(), dwh.getDescriptionType(md.getKey().name()), 
								Status.ACTIVE, contentTime);
						break;
					case ValueSetOID:
					case ValueSetReleaseComments:
					case ValueSetStatus:
					case ValueSetUpdatedDate:
					case ValueSetVersion:
						dwh.makeStringAnnotation(dwh.getAttributeType(md.getKey().name()), valueSetConcept, md.getValue(), contentTime);
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
		
		HashMap<String, UUID> codeSystems = new HashMap<>();
		
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
			String valueSetConceptDefinitionText = dataFetchers.get(SOPTDataColumnsV1.ValueSetConceptDefinitionText) == null ? null :
				dataFetchers.get(SOPTDataColumnsV1.ValueSetConceptDefinitionText).apply(row);
			
			String key = codeSystemOid + codeSystemName + codeSystemCode + codeSystemVersion + hl7TableCode;
			
			UUID codeSystem = codeSystems.get(key);
			if (codeSystem == null)
			{
				codeSystem = converterUUID.createNamespaceUUIDFromString("CodeSystemSet|" + codeSystemName);
				dwh.makeConcept(codeSystem, Status.ACTIVE, contentTime);
				dwh.makeParentGraph(codeSystem, soptRootConcept, Status.ACTIVE, contentTime);
				dwh.makeDescriptionEnNoDialect(codeSystem, codeSystemName, dwh.getDescriptionType(SOPTDataColumnsV1.CodeSystemName.name()), Status.ACTIVE, contentTime);
				dwh.makeStringAnnotation(dwh.getAttributeType(SOPTDataColumnsV1.CodeSystemCode.name()), codeSystem, codeSystemCode, contentTime);
				dwh.makeStringAnnotation(dwh.getAttributeType(SOPTDataColumnsV1.CodeSystemOID.name()), codeSystem, codeSystemOid, contentTime);
				dwh.makeStringAnnotation(dwh.getAttributeType(SOPTDataColumnsV1.CodeSystemVersion.name()), codeSystem, codeSystemVersion, contentTime);
				dwh.makeStringAnnotation(dwh.getAttributeType(SOPTDataColumnsV1.HL7Table0396Code.name()), codeSystem, hl7TableCode, contentTime);
				codeSystems.put(key, codeSystem);
			}
			
			UUID rowConceptUuid = converterUUID.createNamespaceUUIDFromString(conceptCode + "|" + conceptName);
			UUID parentUuid = findParentUuid(conceptCode);
			parentUuid = parentUuid == null ? codeSystem : parentUuid;
			parentConcepts.put(conceptCode, rowConceptUuid);
			
			final UUID concept = dwh.makeConcept(rowConceptUuid, Status.ACTIVE, contentTime);
			dwh.makeParentGraph(concept, parentUuid, Status.ACTIVE, contentTime);
			
			dwh.makeDescriptionEnNoDialect(concept, conceptName, dwh.getDescriptionType(SOPTDataColumnsV1.ConceptName.name()), Status.ACTIVE, contentTime);
			
			if (StringUtils.isNotBlank(preferredConceptName) && !preferredConceptName.equals(conceptName))
			{
				dwh.makeDescriptionEnNoDialect(concept, preferredConceptName, dwh.getDescriptionType(SOPTDataColumnsV1.PreferredConceptName.name()), 
						Status.ACTIVE, contentTime);
			}
			
			if (StringUtils.isNotBlank(valueSetConceptDefinitionText))
			{
				dwh.makeDescriptionEnNoDialect(concept, valueSetConceptDefinitionText, dwh.getDescriptionType(SOPTDataColumnsV1.ValueSetConceptDefinitionText.name()), 
						Status.ACTIVE, contentTime);
			}

			dwh.makeBrittleStringAnnotation(dwh.getAttributeType(SOPTDataColumnsV1.ConceptCode.name()), concept, conceptCode, contentTime);
			
			if (StringUtils.isNotBlank(preferredAltCode))
			{
				dwh.makeStringAnnotation(dwh.getAttributeType(SOPTDataColumnsV1.PreferredAlternateCode.name()), concept, preferredAltCode, contentTime);
			}
			
			dwh.makeDynamicRefsetMember(valueSetConcept, concept, contentTime);
			dwh.makeDynamicRefsetMember(allSoptConceptsRefset, concept, contentTime);
			++conceptCount;
		}

		dwh.processTaxonomyUpdates();
		dwh.clearIsaacCaches();
		
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
}