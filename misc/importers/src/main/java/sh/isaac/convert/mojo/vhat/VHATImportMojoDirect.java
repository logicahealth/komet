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

package sh.isaac.convert.mojo.vhat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicValidatorType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.mojo.vhat.data.TerminologyDataReader;
import sh.isaac.convert.mojo.vhat.data.dto.ConceptImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.DesignationExtendedImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.DesignationImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.MapEntryImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.MapSetImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.NamedPropertiedItemImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.PropertyImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.RelationshipImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.SubsetImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.SubsetMembershipImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.TerminologyDTO;
import sh.isaac.convert.mojo.vhat.data.dto.TypeImportDTO;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.misc.constants.VHATConstants;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicLongImpl;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * @author  a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * Goal which converts VHAT XML into the isaac format
 */
@Mojo(name = "convert-VHAT-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class VHATImportMojoDirect extends DirectConverterBaseMojo implements DirectConverter
{
	private HashMap<UUID, String> referencedConcepts = new HashMap<>();
	private HashMap<UUID, String> loadedConcepts = new HashMap<>();
	private UUID rootConceptUUID;

	private UUID allVhatConceptsRefset;

	private HashSet<String> conceptsWithNoDesignations = new HashSet<>();
	private int mapEntryCount = 0;
	private int mapSetCount = 0;
	private int conceptCount = 0;

	private HashMap<UUID, String> associationOrphanConcepts = new HashMap<>();// ID to designation
	
	HashMap<Long, UUID> subsetVuidToConceptIdMap = new HashMap<>();

	/**
	 * This constructor is for maven and HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link #configure(File, Path, String, StampCoordinate)} method on it.
	 */
	public VHATImportMojoDirect()
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
		return new SupportedConverterTypes[] {SupportedConverterTypes.VHAT};
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer, BiConsumer))
	 * @see DirectConverter#convertContent(Consumer, BiConsumer))
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdate) throws IOException 
	{
		statusUpdates.accept("Setting up metadata");
		
		try
		{
			String temp = converterSourceArtifactVersion.substring(0, 10);
			long time = new SimpleDateFormat("yyyy.MM.dd").parse(temp).getTime();
			

			dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.VHAT_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID, 
					"VHAT", true);
			
			setupModule("VHAT", MetaData.VHAT_MODULES____SOLOR.getPrimordialUuid(), Optional.empty(), time);
			
			//Set up our metadata hierarchy
			dwh.makeMetadataHierarchy(true, true, true, true, true, true, time);
			
			statusUpdates.accept("Reading content");
			TerminologyDataReader importer = new TerminologyDataReader(inputFileLocationPath);
			TerminologyDTO terminology = importer.process();

			List<TypeImportDTO> dto = terminology.getTypes();

			dwh.makeAttributeTypeConcept(null, "Version Effective Date", null, null, false, DynamicDataType.STRING, null, time);
			dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, time, readbackCoordinate);
			dwh.linkToExistingAttributeTypeConcept(MetaData.VUID____SOLOR, time, readbackCoordinate);
			
			
			// read in the dynamic types
			for (TypeImportDTO typeImportDTO : dto)
			{
				if (typeImportDTO.getKind().equals("DesignationType"))
				{
					// Add some rankings for FSN / synonym handling
					if (typeImportDTO.getName().equals("Fully Specified Name"))
					{
						dwh.makeDescriptionTypeConcept(null, typeImportDTO.getName(), null, null, 
								MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, time);
					}
					else
					{
						dwh.makeDescriptionTypeConcept(null, typeImportDTO.getName(), null, null, 
								MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, time);
					}
				}
				else if (typeImportDTO.getKind().equals("RelationshipType"))
				{
					ArrayList<UUID> additionalParents = new ArrayList<>();
					
					if (typeImportDTO.getName().equals("has_parent"))
					{
						additionalParents.add(dwh.getRelationTypesNode().get());
					}
					
					dwh.makeAssociationTypeConcept(null, typeImportDTO.getName(), null, null, null, null, IsaacObjectType.CONCEPT, null, additionalParents, time);
				}
				else if (typeImportDTO.getKind().equals("PropertyType"))
				{
					if (typeImportDTO.getName().equals("GEM_Flags"))
					{
						// skip - GEM_Flags are loaded as a column on a mapset instead.
					}
					else
					{
						dwh.makeAttributeTypeConcept(null, typeImportDTO.getName(), null, null, false, DynamicDataType.STRING, null, time);
					}
				}
				else
				{
					log.error("Unexpected Type!");
				}
			}

			// get the refset names
			for (SubsetImportDTO subset : terminology.getSubsets())
			{
				dwh.makeRefsetTypeConcept(null, subset.getSubsetName(), null, null, time);
			}

			// Every time concept created add membership to "All VHAT Concepts"
			allVhatConceptsRefset = dwh.makeRefsetTypeConcept(VHATConstants.VHAT_ALL_CONCEPTS.getPrimordialUuid(), 
					VHATConstants.VHAT_ALL_CONCEPTS.getRegularName().get(), null, null, time);

			log.info("Metadata load stats");
			for (String line : dwh.getLoadStats().getSummary())
			{
				log.info(line);
			}
			
			dwh.clearLoadStats();
			
			//Update these after the metadata is loaded, so other validations work during load
			dwh.processTaxonomyUpdates();
			Get.taxonomyService().notifyTaxonomyListenersToRefresh();
			dwh.processDelayedValidations();

			loadedConcepts.put(allVhatConceptsRefset, VHATConstants.VHAT_ALL_CONCEPTS.getRegularName().get());

			//Put the vuids onto the subsets
			for (SubsetImportDTO subset : terminology.getSubsets())
			{
				UUID concept = dwh.getRefsetType(subset.getSubsetName());
				loadedConcepts.put(concept, subset.getSubsetName());
				dwh.makeBrittleStringAnnotation(MetaData.VUID____SOLOR.getPrimordialUuid(), concept, subset.getVuid().toString(), time);
				subsetVuidToConceptIdMap.put(subset.getVuid(), concept);
			}

			// TODO use the codesystem version info?
			// TODO use the Version info?
			statusUpdates.accept("Processing concepts...");

			progressUpdate.accept(0d, (double)terminology.getCodeSystem().getVersion().getConcepts().size());
			for (ConceptImportDTO item : terminology.getCodeSystem().getVersion().getConcepts())
			{
				conceptCount++;
				writeEConcept(item, time, statusUpdates);
				if (conceptCount % 500 == 0)
				{
					showProgress();
					progressUpdate.accept((double)conceptCount, (double)terminology.getCodeSystem().getVersion().getConcepts().size());
				}
				if (conceptCount % 10000 == 0)
				{
					advanceProgressLine();
					statusUpdates.accept("Processed " + conceptCount + " concepts");
					log.info("Processed " + conceptCount + " concepts");
				}
			}

			advanceProgressLine();
			log.info("Processed " + conceptCount + " concepts");
			log.info("Starting mapsets");
			statusUpdates.accept("Processing mapsets...");

			progressUpdate.accept(0d, (double)terminology.getCodeSystem().getVersion().getMapsets().size());
			for (MapSetImportDTO item : terminology.getCodeSystem().getVersion().getMapsets())
			{
				mapSetCount++;
				progressUpdate.accept((double)mapSetCount, (double)terminology.getCodeSystem().getVersion().getMapsets().size());
				writeEConcept(item, time, statusUpdates);
			}

			advanceProgressLine();
			log.info("Processed " + mapSetCount + " mapsets with " + mapEntryCount + " members");
			statusUpdates.accept("Finishing...");
			progressUpdate.accept(0d, -1d);

			ArrayList<UUID> missingConcepts = new ArrayList<>();

			for (UUID refUUID : referencedConcepts.keySet())
			{
				if (loadedConcepts.get(refUUID) == null)
				{
					missingConcepts.add(refUUID);
					log.error("Data error - The concept " + refUUID + " - " + referencedConcepts.get(refUUID)
							+ " was referenced, but not loaded - will be created as '-MISSING-'");
				}
			}

			if (missingConcepts.size() > 0)
			{
				UUID missingParent = dwh.makeConceptEnNoDialect(null, "Missing Concepts", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
						new UUID[] {rootConceptUUID}, Status.ACTIVE, time);
				for (UUID refUUID : missingConcepts)
				{
					dwh.makeConceptEnNoDialect(refUUID, "-MISSING-", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
							new UUID[] {missingParent}, Status.ACTIVE, time);
				}
			}
			// Handle missing association sources and targets
			log.info("Creating placeholder concepts for " + associationOrphanConcepts.size() + " association orphans");
			// We currently don't have these association targets, so need to invent placeholder concepts.
			
			UUID missingSDORefset = dwh.makeRefsetTypeConcept(VHATConstants.VHAT_MISSING_SDO_CODE_SYSTEM_CONCEPTS.getPrimordialUuid(), 
					VHATConstants.VHAT_MISSING_SDO_CODE_SYSTEM_CONCEPTS.getRegularName().get(), null, 
					"A simple refset to store the missing concepts we have to create during import because"
							+ " we don't yet have the SDO code systems in place", time);
			
			for (Entry<UUID, String> item : associationOrphanConcepts.entrySet())
			{
				if (loadedConcepts.get(item.getKey()) == null)
				{
					dwh.makeDynamicRefsetMember(VHATConstants.VHAT_MISSING_SDO_CODE_SYSTEM_CONCEPTS.getPrimordialUuid(), 
							dwh.makeConceptEnNoDialect(item.getKey(), item.getValue(), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, 
									Status.ACTIVE, time), 
							time);
				}
			}

			// Put in names instead of IDs so the load stats print nicer:
			Hashtable<String, String> stringsToSwap = new Hashtable<String, String>();
			for (SubsetImportDTO subset : terminology.getSubsets())
			{
				stringsToSwap.put(subset.getVuid() + "", subset.getSubsetName());
			}

			for (MapSetImportDTO mapSet : terminology.getCodeSystem().getVersion().getMapsets())
			{
				stringsToSwap.put(mapSet.getVuid() + "", mapSet.getName());
			}

			log.info("Load Statistics");
			// swap out vuids with names to make it more readable...
			for (String line : dwh.getLoadStats().getSummary())
			{
				Enumeration<String> e = stringsToSwap.keys();
				while (e.hasMoreElements())
				{
					String current = e.nextElement();
					line = line.replaceAll(current, stringsToSwap.get(current));
				}
				log.info(line);
			}

			statusUpdates.accept("Processing deferred validations and updates...");
			dwh.processTaxonomyUpdates();
			Get.taxonomyService().notifyTaxonomyListenersToRefresh();
			dwh.processDelayedValidations();
			
			// this could be removed from final release. Just added to help debug editor problems.
			if (outputDirectory != null)
			{
				log.info("Dumping UUID Debug File");
				converterUUID.dump(outputDirectory, "vhatUuid");
			}
			converterUUID.clearCache();

			if (conceptsWithNoDesignations.size() > 0)
			{
				log.error(conceptsWithNoDesignations.size() + " concepts were found with no descriptions at all.  These were assigned '-MISSING-'");
				if (outputDirectory != null)
				{
					FileWriter fw = new FileWriter(new File(outputDirectory, "NoDesignations.txt"));
					for (String s : conceptsWithNoDesignations)
					{
						fw.write(s);
						fw.write(System.lineSeparator());
					}
					fw.close();
				}
			}
			
			VhatUtil.check(dwh, rootConceptUUID, missingSDORefset);

			log.info("Sanity checked static UUIDs - all ok");
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private void writeEConcept(NamedPropertiedItemImportDTO conceptOrMapSet, long time, Consumer<String> statusUpdates) throws Exception
	{
		boolean isMapSet = false;
		if (conceptOrMapSet instanceof MapSetImportDTO)
		{
			isMapSet = true;
			mapSetCount++;
		}

		final UUID concept = dwh.makeConcept(getConceptUuid(conceptOrMapSet.getCode()), conceptOrMapSet.isActive() ? Status.ACTIVE : Status.INACTIVE, time);
		loadedConcepts.put(concept, conceptOrMapSet.getCode());
		dwh.makeBrittleStringAnnotation(MetaData.VUID____SOLOR.getPrimordialUuid(), concept, conceptOrMapSet.getVuid().toString(), time);
		dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), concept, conceptOrMapSet.getCode().toString(), time);

		Function<DesignationImportDTO, UUID> descriptionProcessor = (didto -> {
			UUID description = dwh.makeDescription(getDescriptionUuid(didto.getCode().toString()), concept, didto.getValueNew(), 
					dwh.getDescriptionType(didto.getTypeName()), MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(), null, 
					(didto.isActive() ? Status.ACTIVE : Status.INACTIVE), time, null, null);
			
			dwh.makeBrittleStringAnnotation(MetaData.VUID____SOLOR.getPrimordialUuid(), description, didto.getVuid().toString(), time);
			dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), description, didto.getCode().toString(), time);
			
			if (didto.getValueNew().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get()))
			{
				// On the root node, we need to add some extra attributes
				dwh.makeDescriptionEnNoDialect(concept, "VHA Terminology", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time);
				dwh.makeDescriptionEnNoDialect(concept, "VHAT", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time);
				log.info("Root concept FSN is 'VHAT' and the UUID is " + concept);
				dwh.makeParentGraph(concept, MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid(), Status.ACTIVE, time);
				rootConceptUUID = concept;
			}
			return description;
		});

		if (isMapSet)
		{
			for (DesignationImportDTO didto : ((MapSetImportDTO) conceptOrMapSet).getDesignations())
			{
				descriptionProcessor.apply(didto);
			}
		}
		else
		{
			for (DesignationExtendedImportDTO didto : ((ConceptImportDTO) conceptOrMapSet).getDesignations())
			{
				UUID description = descriptionProcessor.apply(didto);
				
				// VHAT is kind of odd, in that the attributes are attached to the description, rather than the concept.
				for (PropertyImportDTO property : didto.getProperties())
				{
					dwh.makeStringAnnotation(dwh.getAttributeType(property.getTypeName()), description, property.getValueNew(), 
							property.isActive() ? Status.ACTIVE : Status.INACTIVE, time);
				}
				
				for (SubsetMembershipImportDTO subsetMembership : didto.getSubsets())
				{
					if (subsetMembership.getVuid() != null)
					{
						UUID subsetConcept = subsetVuidToConceptIdMap.get(subsetMembership.getVuid());
						dwh.makeDynamicRefsetMember(subsetConcept, description, time);
					}
				}
			}
		}

		for (PropertyImportDTO property : conceptOrMapSet.getProperties())
		{
			dwh.makeDynamicSemantic(dwh.getAttributeType(property.getTypeName()), concept, new DynamicData[] {new DynamicStringImpl(property.getValueNew())}, 
					property.isActive() ? Status.ACTIVE : Status.INACTIVE, time, null);
		}

		List<RelationshipImportDTO> relationshipImports = conceptOrMapSet.getRelationships();
		
		if (relationshipImports != null)
		{
			UUID isANativeType = null;
			Status graphStatus = Status.ACTIVE;
			ArrayList<UUID> parents = new ArrayList<>();
			
			for (RelationshipImportDTO relationshipImportDTO : relationshipImports)
			{
				UUID sourceUuid = getConceptUuid(conceptOrMapSet.getCode());
				UUID targetUuid = getConceptUuid(relationshipImportDTO.getNewTargetCode());

				referencedConcepts.put(targetUuid, relationshipImportDTO.getNewTargetCode());

				if (!sourceUuid.equals(concept))
				{
					throw new RuntimeException("Design failure!");
				}
				
				dwh.makeAssociation(dwh.getAssociationType(relationshipImportDTO.getTypeName()), sourceUuid, 
						targetUuid, relationshipImportDTO.isActive() ? Status.ACTIVE : Status.INACTIVE, time);
				
				// If it is an isA rel, also create it as a rel.
				if (dwh.getRelationshipType(relationshipImportDTO.getTypeName()) != null)
				{
					parents.add(targetUuid);
					isANativeType = dwh.getAssociationType(relationshipImportDTO.getTypeName());
					graphStatus = relationshipImportDTO.isActive() ? Status.ACTIVE : Status.INACTIVE;
				}
			}
			if (parents.size() > 0)
			{
				UUID graph = dwh.makeParentGraph(concept, parents, graphStatus, time);
				dwh.makeExtendedRelationshipTypeAnnotation(graph, isANativeType, time);
			}
		}

		dwh.makeDynamicRefsetMember(allVhatConceptsRefset, concept, time);

		if (isMapSet)
		{
			// Add a relationship to the subsets metadata concept.
			if (relationshipImports != null && relationshipImports.size() > 0)
			{
				throw new RuntimeException("Didn't expect mapsets to have their own relationships!");
			}

			// add it as an association too
			dwh.makeAssociation(dwh.getAssociationType("has_parent"), concept, dwh.getRefsetTypesNode().get(), time);
			dwh.makeAssociation(dwh.getAssociationType("has_parent"), concept, 
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid(), time);
			

			// place it in two places - refsets under VHAT Metadata, and the dynamic semantic mapping semantic type.
			dwh.makeParentGraph(concept, Arrays.asList(new UUID[] {dwh.getRefsetTypesNode().get(), 
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid()}), Status.ACTIVE, time);
			
			MapSetImportDTO mapSet = ((MapSetImportDTO) conceptOrMapSet);

			// before defining the columns, we need to determine if this mapset makes use of gem flags
			boolean mapSetDefinitionHasGemFlag = false;
			for (MapEntryImportDTO mapItem : mapSet.getMapEntries())
			{
				if (mapSetDefinitionHasGemFlag)
				{
					break;
				}
				for (PropertyImportDTO mapItemProperty : mapItem.getProperties())
				{
					if (mapItemProperty.getTypeName().equals("GEM_Flags"))
					{
						mapSetDefinitionHasGemFlag = true;
						break;
					}
				}
			}

			DynamicColumnInfo[] columns = new DynamicColumnInfo[mapSetDefinitionHasGemFlag ? 6 : 5];
			int col = 0;
			columns[col] = new DynamicColumnInfo(col++, DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid(),
					DynamicDataType.UUID, null, false, DynamicValidatorType.COMPONENT_TYPE,
					new DynamicArrayImpl<>(new DynamicString[] { new DynamicStringImpl(IsaacObjectType.CONCEPT.name()) }), true);
			columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_EQUIVALENCE_TYPE.getPrimordialUuid(),
					DynamicDataType.UUID, null, false, DynamicValidatorType.IS_KIND_OF,
					new DynamicUUIDImpl(IsaacMappingConstants.get().MAPPING_EQUIVALENCE_TYPES.getPrimordialUuid()), true);
			columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_SEQUENCE.getPrimordialUuid(),
					DynamicDataType.INTEGER, null, false, true);
			columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_GROUPING.getPrimordialUuid(), DynamicDataType.LONG,
					null, false, true);
			columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_EFFECTIVE_DATE.getPrimordialUuid(),
					DynamicDataType.LONG, null, false, true);
			// moved to end - make it more convenient for GUI where target and qualifier are extracted, and used elsewhere - its convenient not to
			// have the order change.
			if (mapSetDefinitionHasGemFlag)
			{
				columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_GEM_FLAGS.getPrimordialUuid(),
						DynamicDataType.STRING, null, false, true);
			}

			dwh.configureConceptAsDynamicAssemblage(concept, mapSet.getName(), columns, IsaacObjectType.CONCEPT, null, time);

			// Annotate this concept as a mapset definition concept.
			dwh.makeDynamicRefsetMember(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid(), concept, time);

			// Now that we have defined the map semantic, add the other annotations onto the map set definition.
			if (StringUtils.isNotBlank(mapSet.getSourceCodeSystemName()))
			{
				dwh.makeDynamicSemantic(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), 
						concept, new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM.getNid()),
								new DynamicStringImpl(mapSet.getSourceCodeSystemName()) }, time);
			}

			if (StringUtils.isNotBlank(mapSet.getSourceVersionName()))
			{
				dwh.makeDynamicSemantic(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), 
						concept, new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM_VERSION.getNid()),
								new DynamicStringImpl(mapSet.getSourceVersionName()) }, time);
			}

			if (StringUtils.isNotBlank(mapSet.getTargetCodeSystemName()))
			{
				dwh.makeDynamicSemantic(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), 
						concept, new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM.getNid()),
								new DynamicStringImpl(mapSet.getTargetCodeSystemName()) }, time);
			}

			if (StringUtils.isNotBlank(mapSet.getTargetVersionName()))
			{
				dwh.makeDynamicSemantic(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), 
						concept, new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM_VERSION.getNid()),
								new DynamicStringImpl(mapSet.getTargetVersionName()) }, time);
			}

			for (MapEntryImportDTO mapItem : mapSet.getMapEntries())
			{
				UUID sourceConcept = mapSet.getSourceCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get())
								? getConceptUuid(mapItem.getSourceCode())
								: getAssociationOrphanUuid(mapItem.getSourceCode());

				if (!loadedConcepts.containsKey(sourceConcept))
				{
					if (mapSet.getSourceCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get()))
					{
						log.error("Missing VHAT association source concept! " + mapItem.getSourceCode());
					}
					associationOrphanConcepts.put(sourceConcept, mapItem.getSourceCode());
				}

				UUID targetConcept = mapSet.getTargetCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get())
						? getConceptUuid(mapItem.getTargetCode())
						: getAssociationOrphanUuid(mapItem.getTargetCode());

				if (!loadedConcepts.containsKey(targetConcept))
				{
					if (mapSet.getTargetCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get()))
					{
						log.error("Missing VHAT association target concept! " + mapItem.getTargetCode());
					}
					associationOrphanConcepts.put(targetConcept, mapItem.getTargetCode());
				}

				String gemFlag = null;
				if (mapItem.getProperties() != null)
				{
					for (PropertyImportDTO property : mapItem.getProperties())
					{
						if (property.getTypeName().equals("GEM_Flags"))
						{
							if (gemFlag != null)
							{
								throw new RuntimeException("Didn't expect multiple gem flags on a single mapItem!");
							}
							gemFlag = property.getValueNew();
						}
					}
				}

				DynamicData[] columnData = new DynamicData[mapSetDefinitionHasGemFlag ? 6 : 5];
				col = 0;
				columnData[col++] = new DynamicUUIDImpl(targetConcept);
				columnData[col++] = null;  // qualifier column
				columnData[col++] = new DynamicIntegerImpl(mapItem.getSequence()); // sequence column
				columnData[col++] = mapItem.getGrouping() != null ? new DynamicLongImpl(mapItem.getGrouping()) : null; // grouping column
				columnData[col++] = mapItem.getEffectiveDate() != null ? new DynamicLongImpl(mapItem.getEffectiveDate().getTime()) : null; // effectiveDate
				if (mapSetDefinitionHasGemFlag)
				{
					columnData[col++] = gemFlag == null ? null : new DynamicStringImpl(gemFlag);
				}

				UUID association = dwh.makeDynamicSemantic(concept, sourceConcept, columnData, mapItem.isActive() ? Status.ACTIVE : Status.INACTIVE, time, 
						getMapItemUUID(concept, mapItem.getVuid().toString()));
				
				dwh.makeBrittleStringAnnotation(MetaData.VUID____SOLOR.getPrimordialUuid(), association, mapItem.getVuid().toString(), time);

				if (mapItem.getProperties() != null)
				{
					for (PropertyImportDTO property : mapItem.getProperties())
					{
						if (property.getTypeName().equals("GEM_Flags"))
						{
							// already handled above
						}
						else
						{
							throw new RuntimeException("Properties on map set items not yet handled");
							// This code is correct, but our gui doesn't expect this, so throw an error for now, so we know if any show up.
						}
					}
				}
				if (mapItem.getDesignations() != null && mapItem.getDesignations().size() > 0)
				{
					throw new RuntimeException("Designations on map set items not yet handled");
				}
				if (mapItem.getRelationships() != null && mapItem.getRelationships().size() > 0)
				{
					throw new RuntimeException("Relationships on map set items not yet handled");
				}
				mapEntryCount++;
				if (mapEntryCount % 1000 == 0)
				{
					showProgress();
				}
				if (mapEntryCount % 10000 == 0)
				{
					advanceProgressLine();
					statusUpdates.accept("Processed " + mapSetCount + " mapsets with " + mapEntryCount + " members");
					log.info("Processed " + mapSetCount + " mapsets with " + mapEntryCount + " members");
				}
			}
		}
	}

	private UUID getAssociationOrphanUuid(String code)
	{
		UUID temp = converterUUID.createNamespaceUUIDFromString("associationOrphan:" + code, true);
		Get.identifierService().assignNid(temp);
		return temp;
	}

	private UUID getMapItemUUID(UUID mapSetUUID, String mapItemVuid)
	{
		return converterUUID.createNamespaceUUIDFromString("mapSetUuid:" + mapSetUUID + "mapItemVuid:" + mapItemVuid, false);
	}

	private UUID getConceptUuid(String codeId)
	{
		UUID temp = converterUUID.createNamespaceUUIDFromString("code:" + codeId, true);
		Get.identifierService().assignNid(temp);
		return temp;
	}

	private UUID getDescriptionUuid(String descriptionId)
	{
		return converterUUID.createNamespaceUUIDFromString("description:" + descriptionId, false);
	}
}