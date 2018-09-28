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

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicValidatorType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.mojo.vhat.data.SqlDataReader;
import sh.isaac.convert.mojo.vhat.data.dto.ConceptImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.DesignationExtendedImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.DesignationImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.MapEntryImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.MapSetImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.NamedPropertiedItemImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.PropertyImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.RelationshipImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.SubsetImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.TypeImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.Version;
import sh.isaac.convert.mojo.vhat.propertyTypes.PT_Annotations;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Associations;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Descriptions;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Refsets;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Relations;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyAssociation;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import sh.isaac.converters.sharedUtils.sql.H2DatabaseHandle;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.misc.constants.VHATConstants;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicLongImpl;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;

/**
 * Goal which converts VHAT data into the workbench jbin format
 * 
 * Open the H2 Server and set these indexes _before_ running.
 * 
 * CREATE INDEX IDX_CON_ID ON CONCEPT(ID);
 * CREATE INDEX IDX_CON_CODE ON CONCEPT(CODE);
 * CREATE INDEX IDX_CON_EID ON CONCEPT(ENTITY_ID);
 * CREATE INDEX IDX_MEX_MEID ON MAPENTRYEXTENSION (MAPENTRYID);
 * CREATE INDEX IDX_REL_EID ON RELATIONSHIP (ENTITY_ID);
 * CREATE INDEX IDX_REL_SEID ON RELATIONSHIP (SOURCE_ENTITY_ID);
 * CREATE INDEX IDX_REL_TEID ON RELATIONSHIP (TARGET_ENTITY_ID);
 * CREATE INDEX IDX_REL_KIND ON RELATIONSHIP (KIND);
 * CREATE INDEX IDX_REL_VID ON RELATIONSHIP (VERSION_ID);
 * CREATE INDEX IDX_CON_TID ON CONCEPT(TYPE_ID);
 * CREATE INDEX IDX_VER_ID ON VERSION(ID);
 * CREATE INDEX IDX_TYPE_ID ON TYPE(ID);
 * CREATE INDEX IDX_PROPERTY_ENTITY_ID ON PROPERTY(ENTITY_ID);
 * CREATE INDEX IDX_PROPERTY_CONCEPTENTITY_ID ON PROPERTY(CONCEPTENTITY_ID);
 * @deprecated this was never completed, and the VA work it requires doesn't exist right now.
 */
@Mojo(name = "convert-VHAT-SQL-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class VHATSQLImportMojo extends ConverterBaseMojo
{
	private IBDFCreationUtility importUtil_;

	private HashMap<UUID, String> referencedConcepts = new HashMap<>();
	private HashMap<UUID, String> loadedConcepts = new HashMap<>();
	private UUID rootConceptUUID;

	private PropertyType attributes_, descriptions_, associations_, relationships_;
	private BPT_Refsets refsets_;

	private UUID allVhatConceptsRefset;

	private HashSet<String> conceptsWithNoDesignations = new HashSet<>();
	private int mapEntryCount = 0;
	private int mapSetCount = 0;
	private int conceptCount = 0;

	// The boolean flag in left is set to true if the source is the orphan, is set to false if the target is the orphan
	private HashMap<UUID, Supplier<Long>> associationOrphanConceptsMapEntries = new HashMap<>();
	
	HashMap<Long, UUID> subsetVuidToConceptIdMap = new HashMap<>();

	private SqlDataReader importer = new SqlDataReader();

	private HashMap<String, UUID> codeToUUIDs = new HashMap<>();
	private HashSet<Long> vuidToUUIDs = new HashSet<>();
	private ArrayList<UUID> loadedRefsets = new ArrayList<>();
	private ArrayList<UUID> loadedGraphs = new ArrayList<>();
	private ArrayList<String> descriptionsCodes = new ArrayList<>();
	private ArrayList<Long> descriptionsVUIDs = new ArrayList<>();
	private ArrayList<UUID> loadedMapEntries = new ArrayList<>();
	private Map<Long, UUID> vhatVersionUUIDs_ = new HashMap<>();
	private Map<Long, Long> vhatVersionTimes_ = new HashMap<>();

	/**
	 * Location of the input properties file with the database connection info. Not required, by default, reads a file included on the classpath
	 * with the standard locations..
	 */
	@Parameter(required = false)
	protected File propertiesFile;

	/**
	 * The prefix to use when reading the properties file. Typically something like 'dev' or 'test' (without the quotes)
	 */
	@Parameter(required = true)
	protected String propertiesFilePrefix;

	@Override
	public void execute() throws MojoExecutionException
	{

		try
		{
			super.execute();

			String temp = converterOutputArtifactVersion.substring(0, 11);

			importUtil_ = new IBDFCreationUtility(Optional.of("VHAT " + converterSourceArtifactVersion), Optional.of(MetaData.VHAT_MODULES____SOLOR),
					outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, true,
					new SimpleDateFormat("yyyy.MM.dd").parse(temp).getTime());

			attributes_ = new PT_Annotations();
			descriptions_ = new BPT_Descriptions(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get());
			associations_ = new BPT_Associations(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get());
			relationships_ = new BPT_Relations(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get());
			refsets_ = new BPT_Refsets(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get());
			refsets_.addProperty(VHATConstants.VHAT_ALL_CONCEPTS.getRegularName().get());

			ComponentReference vhatMetadata = ComponentReference.fromConcept(
					createType(MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(), "VHAT Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG));

			importUtil_.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			// TODO: would be nice to automate this
			importUtil_.registerDynamicColumnInfo(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_NID_EXTENSION.getPrimordialUuid(),
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_NID_EXTENSION.getDynamicColumns());
			importUtil_.registerDynamicColumnInfo(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid(),
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getDynamicColumns());
			importUtil_.registerDynamicColumnInfo(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(),
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getDynamicColumns());

			Properties props = new Properties();
			if (propertiesFile != null)
			{
				props.load(new FileReader(propertiesFile));
			}
			else
			{
				props.load(SqlDataReader.class.getClassLoader().getResourceAsStream("config.properties"));
			}
			importer.process(props, propertiesFilePrefix);

			Map<Long, Version> vhats_ = importer.getVhatVersions().orElse(new HashMap<>());

			for (Map.Entry<Long, Version> entry : vhats_.entrySet())
			{
				Long key = entry.getKey();
				Version value = entry.getValue();
				String moduleToCreate = "VHAT " + value.getName();
				UUID moduleUUID = UuidT5Generator.get(UuidT5Generator.PATH_ID_FROM_FS_DESC, moduleToCreate);
				// Created with FSN 'VHAT #'
				ComponentReference moduleConcept = ComponentReference
						.fromConcept(importUtil_.createConcept(moduleUUID, moduleToCreate, false, MetaData.VHAT_MODULES____SOLOR.getPrimordialUuid()));
				// Adding Synonym of just '#'
				importUtil_.addDescription(moduleConcept, value.getName(), DescriptionType.REGULAR_NAME, false, null, Status.ACTIVE);
				importUtil_.addStaticStringAnnotation(moduleConcept, value.getEffectiveDate().getTime() + "",
						attributes_.getProperty(PT_Annotations.Attribute.VER_EFF_DATE.get()).getUUID(), Status.ACTIVE);
				ConsoleUtil.println("Creating module " + moduleUUID + " with description '" + moduleToCreate + "'");
				vhatVersionUUIDs_.put(key, moduleUUID);
				vhatVersionTimes_.put(key, value.getEffectiveDate().getTime());
			}

			// Default if there is no version, which should "never" happen
			vhatVersionUUIDs_.put(-1L, importUtil_.getModule().getPrimordialUuid());

			List<TypeImportDTO> dto = new ArrayList<>();
			if (importer.getTypes().isPresent())
			{
				dto = importer.getTypes().get();
			}

			// Read in the dynamic types
			for (TypeImportDTO typeImportDTO : dto)
			{
				if (typeImportDTO.getKind().equals("D")) // DesignationType
				{
					Property p = descriptions_.addProperty(typeImportDTO.getName());
					// Add some rankings for FSN / synonym handling
					if (p.getSourcePropertyNameFQN().equals("Fully Specified Name"))
					{
						p.setPropertySubType(BPT_Descriptions.FULLY_QUALIFIED_NAME);
					}
					else if (p.getSourcePropertyNameFQN().equals("Preferred Name"))
					{
						p.setPropertySubType(BPT_Descriptions.SYNONYM);
					}
					else if (p.getSourcePropertyNameFQN().equals("Synonym"))
					{
						p.setPropertySubType(BPT_Descriptions.SYNONYM + 1);
					}
				}
				else if (typeImportDTO.getKind().equals("R")) // RelationshipType
				{
					PropertyAssociation pa = new PropertyAssociation(associations_, typeImportDTO.getName(), typeImportDTO.getName(), null,
							typeImportDTO.getName(), false);
					// currently loading isA as a graph rel, and an assoiation
					if (typeImportDTO.getName().equals("has_parent"))
					{
						// Hang the has_parent under the relationships node too.
						pa.setSecondParent(relationships_.getPropertyTypeUUID());
						pa.setWBPropertyType(MetaData.IS_A____SOLOR.getPrimordialUuid());
					}
					associations_.addProperty(pa);
				}
				else if (typeImportDTO.getKind().equals("P")) // PropertyType
				{
					// GEM_Flags are loaded here so they are available as a column on a mapset
					attributes_.addProperty(typeImportDTO.getName());
				}
				else
				{
					System.err.println("Unexpected Type!");
				}
			}

			// Get the refset names
			if (importer.getFullListOfSubsets().isPresent())
			{
				for (SubsetImportDTO subset : importer.getFullListOfSubsets().get())
				{
					refsets_.addProperty(subset.getSubsetName());
				}
			}

			importUtil_.loadMetaDataItems(Arrays.asList(descriptions_, attributes_, associations_, relationships_, refsets_), vhatMetadata.getPrimordialUuid());

			ConsoleUtil.println("Metadata load stats");
			for (String line : importUtil_.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}

			importUtil_.clearLoadStats();

			allVhatConceptsRefset = refsets_.getProperty(VHATConstants.VHAT_ALL_CONCEPTS.getRegularName().get()).getUUID();
			loadedConcepts.put(allVhatConceptsRefset, VHATConstants.VHAT_ALL_CONCEPTS.getRegularName().get());

			//Put the vuids onto the subsets
			if (importer.getSubsetsByVuid().isPresent())
			{
				for (Map.Entry<Long, String> entry : importer.getSubsetsByVuid().get().entrySet())
				{
					Long subsetVuid = entry.getKey();
					String subsetName = entry.getValue();
					
					UUID concept = refsets_.getProperty(subsetName).getUUID();
					loadedConcepts.put(concept, subsetName);
					importUtil.addStaticStringAnnotation(ComponentReference.fromConcept(concept), subsetVuid.toString(), attributes_.getProperty("VUID").getUUID(), 
							Status.ACTIVE);
					subsetVuidToConceptIdMap.put(subsetVuid, concept);
				}
			}

			// TODO use the codesystem version info?
			// TODO use the Version info?

			if (importer.getConcepts().isPresent())
			{
				for (Map.Entry<Long, ArrayList<ConceptImportDTO>> entry : importer.getConcepts().get().entrySet())
				{
					long conceptEntityId = Long.valueOf(entry.getKey()).longValue();
					for (ConceptImportDTO item : entry.getValue())
					{
						conceptCount++;
						writeEConcept(null, item, conceptEntityId);
						if (conceptCount % 100 == 0)
						{
							ConsoleUtil.showProgress();
						}
						if (conceptCount % 3000 == 0)
						{
							ConsoleUtil.println("Processed " + conceptCount + " concepts");
						}
					}
				}
			}

			// Not needed any more, let the GC reclaim memory to speed up processing
			importer.clearRelationships();
			ConsoleUtil.println("Processed " + conceptCount + " concepts");
			ConsoleUtil.println("Starting mapsets");

			if (importer.getMapSets().isPresent())
			{
				for (Map.Entry<Long, ArrayList<MapSetImportDTO>> entry : importer.getMapSets().get().entrySet())
				{
					long conceptEntityId = Long.valueOf(entry.getKey().longValue());
					for (MapSetImportDTO item : entry.getValue())
					{
						conceptCount++;
						writeEConcept(null, item, conceptEntityId);
						ConsoleUtil.println("Processed " + mapSetCount + " mapsets with " + mapEntryCount + " members");
					}
				}
			}
			ConsoleUtil.println("Finished mapsets");

			// Put in names instead of IDs so the load stats print nicer:
			Hashtable<String, String> stringsToSwap = new Hashtable<String, String>();
			if (importer.getFullListOfSubsets().isPresent())
			{
				for (SubsetImportDTO subset : importer.getFullListOfSubsets().get())
				{
					stringsToSwap.put(subset.getVuid() + "", subset.getSubsetName());
				}
			}

			if (importer.getFullListOfMapSets().isPresent())
			{
				for (MapSetImportDTO mapSet : importer.getFullListOfMapSets().get())
				{
					stringsToSwap.put(mapSet.getVuid() + "", mapSet.getName());
				}
			}

			ArrayList<UUID> missingConcepts = new ArrayList<>();

			for (UUID refUUID : referencedConcepts.keySet())
			{
				if (loadedConcepts.get(refUUID) == null)
				{
					missingConcepts.add(refUUID);
					ConsoleUtil.printErrorln("Data error - The concept " + refUUID + " - " + referencedConcepts.get(refUUID)
							+ " was referenced, but not loaded - will be created as '-MISSING-'");
				}
			}

			if (missingConcepts.size() > 0)
			{
				ConceptVersion missingParent = importUtil_.createConcept("Missing Concepts", true);
				importUtil_.addParent(ComponentReference.fromConcept(missingParent), rootConceptUUID);
				for (UUID refUUID : missingConcepts)
				{
					ComponentReference c = ComponentReference.fromConcept(importUtil_.createConcept(refUUID, "-MISSING-", true));
					importUtil_.addParent(c, missingParent.getPrimordialUuid());
				}
			}

			// Handle missing association sources and targets
			ConsoleUtil.println("Creating placeholder concepts for " + associationOrphanConceptsMapEntries.size() + " association orphans");
			// We currently don't have these association targets, so need to invent placeholder concepts.
			ComponentReference missingSDORefset = ComponentReference.fromConcept(importUtil_.createConcept(null,
					VHATConstants.VHAT_MISSING_SDO_CODE_SYSTEM_CONCEPTS.getRegularName().get(), null, null, null, refsets_.getPropertyTypeUUID(), (UUID) null));
			importUtil_.configureConceptAsDynamicRefex(missingSDORefset, "A simple refset to store the missing concepts we have to create during import because"
					+ " we don't yet have the SDO code systems in place", null, IsaacObjectType.CONCEPT, null);

			int orphanCount = 0;
			for (UUID missingConceptUUID : associationOrphanConceptsMapEntries.keySet())
			{
				if (loadedConcepts.get(missingConceptUUID) == null)
				{
					Long conceptEntityId = associationOrphanConceptsMapEntries.get(missingConceptUUID).get();
					if (conceptEntityId == null)
					{
						getLog().error("Couldn't get concept entityId for UUID " + missingConceptUUID);
					}
					else
					{
						List<ConceptImportDTO> conVersions = importer.readConcept(conceptEntityId);

						if (conVersions.size() == 0)
						{
							getLog().error("Couldn't read orphan concept entity " + conceptEntityId);
						}
						else
						{
							ComponentReference concept = null;
							for (ConceptImportDTO con : conVersions)
							{
								concept = writeEConcept(missingConceptUUID, con, conceptEntityId);
							}
							// Just do this once, the UUID is the same on all versions
							importUtil_.addAssemblageMembership(concept, missingSDORefset.getPrimordialUuid(), Status.ACTIVE, null);
						}
					}
				}

				orphanCount++;
				if (orphanCount % 5000 == 0)
				{
					ConsoleUtil.println("Processed " + orphanCount + " association orphans");
				}
			}

			VhatUtil.check(associations_, attributes_, descriptions_, refsets_, rootConceptUUID, missingSDORefset.getPrimordialUuid());
			ConsoleUtil.println("Sanity checked static UUIDs - all ok");

			ConsoleUtil.println("Processed " + associationOrphanConceptsMapEntries.size() + " association orphans");

			ConsoleUtil.println("Load Statistics");
			// Swap out vuids with names to make it more readable...
			for (String line : importUtil_.getLoadStats().getSummary())
			{
				Enumeration<String> e = stringsToSwap.keys();
				while (e.hasMoreElements())
				{
					String current = e.nextElement();
					line = line.replaceAll(current, stringsToSwap.get(current));
				}
				ConsoleUtil.println(line);
			}

			// This could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			Get.service(ConverterUUID.class).dump(outputDirectory, "vhatUuid");

			if (conceptsWithNoDesignations.size() > 0)
			{
				ConsoleUtil
						.printErrorln(conceptsWithNoDesignations.size() + " concepts were found with no descriptions at all.  These were assigned '-MISSING-'");
				FileWriter fw = new FileWriter(new File(outputDirectory, "NoDesignations.txt"));
				for (String s : conceptsWithNoDesignations)
				{
					fw.write(s);
					fw.write(System.lineSeparator());
				}
				fw.close();
			}
			importer.shutdown();
			importUtil_.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}

	}

	private ComponentReference writeEConcept(UUID uuidToUse, NamedPropertiedItemImportDTO conceptOrMapSet, Long conceptEntityId) throws Exception
	{
		boolean isMapSet = false;
		if (conceptOrMapSet instanceof MapSetImportDTO)
		{
			isMapSet = true;
			mapSetCount++;
		}

		if (uuidToUse == null)
		{
			uuidToUse = getConceptUuid(conceptOrMapSet.getCode());
		}

		UUID module = vhatVersionUUIDs_.containsKey(conceptOrMapSet.getVersion()) ? vhatVersionUUIDs_.get(conceptOrMapSet.getVersion())
				: vhatVersionUUIDs_.get(-1L);
		Long time = vhatVersionTimes_.get(conceptOrMapSet.getVersion());
		// Switch to the appropriate module for this concept of mapset item
		importUtil_.setModule(module, time);

		ComponentReference concept = ComponentReference.fromConcept(
				importUtil_.createConcept(uuidToUse, conceptOrMapSet.getTime(), conceptOrMapSet.isActive() ? Status.ACTIVE : Status.INACTIVE, null));

		// Only add Code to the chronology if it changed
		if (!codeToUUIDs.containsKey(conceptOrMapSet.getCode()))
		{
			// For use with Relationships later
			codeToUUIDs.put(conceptOrMapSet.getCode(), uuidToUse);
			importUtil_.addStaticStringAnnotation(concept, conceptOrMapSet.getCode(), attributes_.getProperty("Code").getUUID(), Status.ACTIVE);
			loadedConcepts.put(concept.getPrimordialUuid(), conceptOrMapSet.getCode());
		}

		// Only add VUID to the chronology if it changed
		if (conceptOrMapSet.getVuid() != null && !vuidToUUIDs.contains(conceptOrMapSet.getVuid()))
		{
			vuidToUUIDs.add(conceptOrMapSet.getVuid());
			importUtil_.addStaticStringAnnotation(concept, conceptOrMapSet.getVuid().toString(), attributes_.getProperty("VUID").getUUID(), Status.ACTIVE);
		}

		ArrayList<ValuePropertyPairExtended> descriptionHolder = new ArrayList<>();

		// All designations using DesignationExtendedImportDTO class
		if (importer.getDesignationsForEntity(conceptEntityId).isPresent())
		{
			for (DesignationExtendedImportDTO didto : importer.getDesignationsForEntity(conceptEntityId).get())
			{
				descriptionHolder.add(new ValuePropertyPairExtended(didto.getValueNew(),
						getDescriptionUuid(didto.getCode() == null ? conceptOrMapSet.getCode() + didto.getValueNew() : didto.getCode().toString()),
						descriptions_.getProperty(didto.getTypeName()), didto, !didto.isActive()));
			}
		}

		if (importer.getPropertiesForEntity(conceptEntityId).isPresent())
		{
			for (PropertyImportDTO property : importer.getPropertiesForEntity(conceptEntityId).get())
			{
				Get.service(ConverterUUID.class).setUUIDMapState(false);
				importUtil_.addStringAnnotation(concept, property.getValueNew(), attributes_.getProperty(property.getTypeName()).getUUID(),
						property.isActive() ? Status.ACTIVE : Status.INACTIVE);
				Get.service(ConverterUUID.class).setUUIDMapState(true);
			}
		}

		// Per Dan, this is a valid reason to create duplicate UUIDs, as
		// only the active/inactive values have changed
		Get.service(ConverterUUID.class).setUUIDMapState(false);
		List<SemanticChronology> wbDescriptions = importUtil_.addDescriptions(concept, descriptionHolder);
		Get.service(ConverterUUID.class).setUUIDMapState(true);

		// Descriptions have now all been added to the concepts - now we need to process the rest of the ugly bits of vhat
		// and place them on the descriptions.
		for (int i = 0; i < descriptionHolder.size(); i++)
		{
			ValuePropertyPairExtended vpp = descriptionHolder.get(i);
			SemanticChronology desc = wbDescriptions.get(i);

			if (vpp.getValue().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get()))
			{
				// On the root node, we need to add some extra attributes
				importUtil_.addDescription(concept, VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get(), DescriptionType.REGULAR_NAME, true,
						descriptions_.getProperty("Synonym").getUUID(), Status.ACTIVE);
				importUtil_.addDescription(concept, "VHA Terminology", DescriptionType.REGULAR_NAME, false, descriptions_.getProperty("Synonym").getUUID(),
						Status.ACTIVE);
				ConsoleUtil.println("Root concept FSN is 'VHAT' and the UUID is " + concept.getPrimordialUuid());
				importUtil_.addParent(concept, MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid());
				rootConceptUUID = concept.getPrimordialUuid();
			}

			if (vpp.getDesignationImportDTO().getVuid() != null && !descriptionsVUIDs.contains(vpp.getDesignationImportDTO().getVuid()))
			{
				importUtil_.addStaticStringAnnotation(ComponentReference.fromChronology(desc, () -> "Description"),
						vpp.getDesignationImportDTO().getVuid() + "", attributes_.getProperty("VUID").getUUID(), Status.ACTIVE);

				descriptionsVUIDs.add(vpp.getDesignationImportDTO().getVuid());
			}
			if (vpp.getDesignationImportDTO().getCode() != null && !descriptionsCodes.contains(vpp.getDesignationImportDTO().getCode()))
			{
				importUtil_.addStaticStringAnnotation(ComponentReference.fromChronology(desc, () -> "Description"), vpp.getDesignationImportDTO().getCode(),
						attributes_.getProperty("Code").getUUID(), Status.ACTIVE);

				descriptionsCodes.add(vpp.getDesignationImportDTO().getCode());
			}

			// VHAT is kind of odd, in that the attributes are attached to the description,
			// rather than the concept.
			// TODO this won't load any properties for concepts being loaded from external code sets, because they don't have vuids....
			if (vpp.getDesignationImportDTO().getVuid() != null && !isMapSet)
			{
				ArrayList<Long> subsetVuids = importer.getSubsetMembershipsCodesByVuid().get(vpp.getDesignationImportDTO().getVuid());
				if (subsetVuids != null)
				{
					for (Long subsetVuid : subsetVuids)
					{
						UUID subsetConcept = subsetVuidToConceptIdMap.get(subsetVuid);
						importUtil.addAssemblageMembership(ComponentReference.fromChronology(desc, () -> "Description"), subsetConcept, Status.ACTIVE, null);
					}
				}
				if (importer.getPropertiesForVuid(vpp.getDesignationImportDTO().getVuid()).isPresent())
				{
					for (PropertyImportDTO property : importer.getPropertiesForVuid(vpp.getDesignationImportDTO().getVuid()).get())
					{
						Get.service(ConverterUUID.class).setUUIDMapState(false);
						importUtil_.addStringAnnotation(ComponentReference.fromChronology(desc, () -> "Description"), property.getValueNew(),
								attributes_.getProperty(property.getTypeName()).getUUID(), property.isActive() ? Status.ACTIVE : Status.INACTIVE);
						Get.service(ConverterUUID.class).setUUIDMapState(true);
					}
				}
			}
		}

		if (descriptionHolder.size() == 0)
		{
			// Seems like a data error - but it is happening... no descriptions at all.....
			if (!conceptsWithNoDesignations.contains(conceptOrMapSet.getCode()))
			{
				conceptsWithNoDesignations.add(conceptOrMapSet.getCode());
				// The workbench implodes if you don't have a fully specified name....
				importUtil_.addDescription(concept, null,
						org.apache.commons.lang3.StringUtils.isBlank(conceptOrMapSet.getName()) ? "-MISSING-" : conceptOrMapSet.getName(),
						DescriptionType.FULLY_QUALIFIED_NAME, true, null, null, null, null, descriptions_.getProperty("Synonym").getUUID(), Status.ACTIVE,
						conceptOrMapSet.getTime());
			}
		}

		LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
		ArrayList<ConceptAssertion> assertions = new ArrayList<>();

		if (importer.getRelationshipsForEntity(conceptEntityId).isPresent())
		{
			UUID isANativeType = null;
			for (RelationshipImportDTO relationshipImportDTO : importer.getRelationshipsForEntity(conceptEntityId).get())
			{
				UUID sourceUuid = getConceptUuid(conceptOrMapSet.getCode());
				UUID targetUuid = getConceptUuid(relationshipImportDTO.getNewTargetCode());

				referencedConcepts.put(targetUuid, relationshipImportDTO.getNewTargetCode());

				if (!sourceUuid.equals(concept.getPrimordialUuid()))
				{
					// throw new MojoExecutionException("Design failure!");
				}

				// TODO: I'm assuming this is also a valid area to create duplicate UUIDs,
				// only the active/inactive values have changed
				Get.service(ConverterUUID.class).setUUIDMapState(false);
				importUtil_.addAssociation(concept, null, targetUuid, associations_.getProperty(relationshipImportDTO.getTypeName()).getUUID(),
						relationshipImportDTO.isActive() ? Status.ACTIVE : Status.INACTIVE, relationshipImportDTO.getTime(), null);
				Get.service(ConverterUUID.class).setUUIDMapState(true);

				if (associations_.getProperty(relationshipImportDTO.getTypeName()) != null
						&& MetaData.IS_A____SOLOR.getPrimordialUuid().equals(associations_.getProperty(relationshipImportDTO.getTypeName()).getWBTypeUUID()))
				{
					assertions.add(ConceptAssertion(Get.identifierService().getNidForUuids(targetUuid), leb));
					isANativeType = associations_.getProperty(relationshipImportDTO.getTypeName()).getUUID();
				}
			}
			if (assertions.size() > 0)
			{
				// TODO: A gragh can only be loaded once, correct?
				if (!loadedGraphs.contains(concept.getPrimordialUuid()))
				{
					NecessarySet(And(assertions.toArray(new Assertion[assertions.size()])));
					importUtil_.addRelationshipGraph(concept, null, leb.build(), true, conceptOrMapSet.getTime(), null, isANativeType);  // TODO handle inactive
					loadedGraphs.add(concept.getPrimordialUuid());
				}
			}
		}

		if (!loadedRefsets.contains(concept.getPrimordialUuid()))
		{
			importUtil_.addAssemblageMembership(concept, allVhatConceptsRefset, Status.ACTIVE, conceptOrMapSet.getTime());
			loadedRefsets.add(concept.getPrimordialUuid());
		}

		if (isMapSet)
		{
			// We are only fetching relationships of concepts, so ignoring this
			// Add a relationship to the subsets metadata concept.
			/*
			 * if (relationshipImports != null && relationshipImports.size() > 0)
			 * {
			 * throw new RuntimeException("Didn't expect mapsets to have their own relationships!");
			 * }
			 */

			// Add it as an association too
			// TODO: Validate this is ok for duplicates
			Get.service(ConverterUUID.class).setUUIDMapState(false);
			importUtil_.addAssociation(concept, null, refsets_.getPropertyTypeUUID(), associations_.getProperty("has_parent").getUUID(), Status.ACTIVE,
					concept.getTime(), null);

			importUtil_.addAssociation(concept, null, refsets_.getAltMetaDataParentUUID(), associations_.getProperty("has_parent").getUUID(), Status.ACTIVE,
					null, null);

			importUtil_.addAssociation(concept, null, IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid(),
					associations_.getProperty("has_parent").getUUID(), Status.ACTIVE, null, null);

			Get.service(ConverterUUID.class).setUUIDMapState(true);

			// Place it in three places - refsets under VHAT Metadata, vhat refsets under SOLOR Refsets, and the dynamic semantic mapping semantic type.
			NecessarySet(And(new Assertion[] { ConceptAssertion(Get.identifierService().getNidForUuids(refsets_.getAltMetaDataParentUUID()), leb),
					ConceptAssertion(Get.identifierService().getNidForUuids(refsets_.getPropertyTypeUUID()), leb),
					ConceptAssertion(
							Get.identifierService().getNidForUuids(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid()),
							leb) }));

			// TODO: Validate
			if (!loadedGraphs.contains(concept.getPrimordialUuid()))
			{
				importUtil_.addRelationshipGraph(concept, null, leb.build(), true, concept.getTime(), null);
				loadedGraphs.add(concept.getPrimordialUuid());
			}

			MapSetImportDTO mapSet = ((MapSetImportDTO) conceptOrMapSet);

			// Before defining the columns, we need to determine if this mapset makes use of gem flags
			boolean mapSetDefinitionHasGemFlag = false;
			if (importer.getMapEntriesForMapSet(conceptOrMapSet.getVuid()).isPresent())
			{
				for (MapEntryImportDTO mapItem : importer.getMapEntriesForMapSet(conceptOrMapSet.getVuid()).get())
				{
					if (mapSetDefinitionHasGemFlag)
					{
						break;
					}

					if (importer.getPropertiesForVuid(mapItem.getVuid()).isPresent())
					{
						for (PropertyImportDTO mapItemProperty : importer.getPropertiesForVuid(mapItem.getVuid()).get())
						{
							if (mapItemProperty.getTypeName().equals("GEM_Flags"))
							{
								mapSetDefinitionHasGemFlag = true;
								break;
							}
						}
					}
				}
			}

			DynamicColumnInfo[] columns = new DynamicColumnInfo[mapSetDefinitionHasGemFlag ? 6 : 5];
			int col = 0;
			columns[col] = new DynamicColumnInfo(col++, DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid(),
					DynamicDataType.UUID, null, false, DynamicValidatorType.COMPONENT_TYPE,
					new DynamicArrayImpl<>(new DynamicString[] { new DynamicStringImpl(IsaacObjectType.CONCEPT.name()) }), true);
			if (mapSetDefinitionHasGemFlag)
			{
				columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_GEM_FLAGS.getPrimordialUuid(),
						DynamicDataType.STRING, null, false, true);
			}
			columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_EQUIVALENCE_TYPE.getPrimordialUuid(),
					DynamicDataType.UUID, null, false, DynamicValidatorType.IS_KIND_OF,
					new DynamicUUIDImpl(IsaacMappingConstants.get().MAPPING_EQUIVALENCE_TYPES.getPrimordialUuid()), true);
			columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_SEQUENCE.getPrimordialUuid(),
					DynamicDataType.INTEGER, null, false, true);
			columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_GROUPING.getPrimordialUuid(), DynamicDataType.LONG,
					null, false, true);
			columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_EFFECTIVE_DATE.getPrimordialUuid(),
					DynamicDataType.LONG, null, false, true);

			// TODO: Not sure this is right
			Get.service(ConverterUUID.class).setUUIDMapState(false);
			importUtil_.configureConceptAsDynamicRefex(concept, mapSet.getName(), columns, IsaacObjectType.CONCEPT, null);

			// Annotate this concept as a mapset definition concept.
			importUtil_.addAnnotation(concept, null, null, IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid(),
					Status.ACTIVE, conceptOrMapSet.getTime());

			// Now that we have defined the map semantic, add the other annotations onto the map set definition.
			if (StringUtils.isNotBlank(mapSet.getSourceCodeSystemName()))
			{
				importUtil_.addAnnotation(concept, null,
						new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM.getNid()),
								new DynamicStringImpl(mapSet.getSourceCodeSystemName()) },
						IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
			}

			if (StringUtils.isNotBlank(mapSet.getSourceVersionName()))
			{
				importUtil_.addAnnotation(concept, null,
						new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM_VERSION.getNid()),
								new DynamicStringImpl(mapSet.getSourceVersionName()) },
						IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
			}

			if (StringUtils.isNotBlank(mapSet.getTargetCodeSystemName()))
			{
				importUtil_.addAnnotation(concept, null,
						new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM.getNid()),
								new DynamicStringImpl(mapSet.getTargetCodeSystemName()) },
						IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
			}

			if (StringUtils.isNotBlank(mapSet.getTargetVersionName()))
			{
				importUtil_.addAnnotation(concept, null,
						new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM_VERSION.getNid()),
								new DynamicStringImpl(mapSet.getTargetVersionName()) },
						IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
			}
			Get.service(ConverterUUID.class).setUUIDMapState(true);

			if (importer.getMapEntriesForMapSet(conceptOrMapSet.getVuid()).isPresent())
			{
				for (MapEntryImportDTO mapItem : importer.getMapEntriesForMapSet(conceptOrMapSet.getVuid()).get())
				{
					ComponentReference sourceConcept = ComponentReference
							.fromConcept(mapSet.getSourceCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get())
									? getConceptUuid(mapItem.getSourceCode())
									: getAssociationOrphanUuid(mapItem.getSourceCode()));

					if (!loadedConcepts.containsKey(sourceConcept.getPrimordialUuid()))
					{
						if (mapSet.getSourceCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get())) // TODO: check this
						{
							ConsoleUtil.printErrorln("Missing VHAT association source concept! " + mapItem.getSourceCode());
						}
						associationOrphanConceptsMapEntries.put(sourceConcept.getPrimordialUuid(),
								() -> importer.getConceptEntityId(mapItem.getSourceCode(), mapSet.getSourceVersionId()));
					}

					UUID targetConcept = mapSet.getTargetCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get())
							? getConceptUuid(mapItem.getTargetCode())
							: getAssociationOrphanUuid(mapItem.getTargetCode());

					if (!loadedConcepts.containsKey(targetConcept))
					{
						if (mapSet.getTargetCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get()))
						{
							ConsoleUtil.printErrorln("Missing VHAT association target concept! " + mapItem.getTargetCode());
						}
						associationOrphanConceptsMapEntries.put(targetConcept,
								() -> importer.getConceptEntityId(mapItem.getTargetCode(), mapSet.getTargetVersionId()));
					}

					ArrayList<PropertyImportDTO> mapItemProperties = importer.getPropertiesForVuid(mapItem.getVuid())
							.orElse(new ArrayList<PropertyImportDTO>());
					String gemFlag = null;
					for (PropertyImportDTO property : mapItemProperties)
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

					DynamicData[] columnData = new DynamicData[mapSetDefinitionHasGemFlag ? 6 : 5];
					col = 0;
					columnData[col++] = new DynamicUUIDImpl(targetConcept);
					if (mapSetDefinitionHasGemFlag)
					{
						columnData[col++] = gemFlag == null ? null : new DynamicStringImpl(gemFlag);
					}
					columnData[col++] = null;  // qualifier column
					columnData[col++] = new DynamicIntegerImpl(mapItem.getSequence()); // sequence column
					columnData[col++] = mapItem.getGrouping() != null ? new DynamicLongImpl(mapItem.getGrouping()) : null; // grouping column
					columnData[col++] = mapItem.getEffectiveDate() != null ? new DynamicLongImpl(mapItem.getEffectiveDate().getTime()) : null; // effectiveDate

					// TODO: Assuming these can have duplicates?
					Get.service(ConverterUUID.class).setUUIDMapState(false);
					UUID mapItemUUID = getMapItemUUID(concept.getPrimordialUuid(), mapItem.getVuid().toString());
					SemanticChronology association = importUtil_.addAnnotation(sourceConcept, mapItemUUID, columnData, concept.getPrimordialUuid(),
							mapItem.isActive() ? Status.ACTIVE : Status.INACTIVE, mapSet.getTime(), null);
					Get.service(ConverterUUID.class).setUUIDMapState(true);

					if (!loadedMapEntries.contains(mapItemUUID))
					{
						loadedMapEntries.add(mapItemUUID);
						importUtil_.addStaticStringAnnotation(ComponentReference.fromChronology(association, () -> "Association"), mapItem.getVuid().toString(),
								attributes_.getProperty("VUID").getUUID(), Status.ACTIVE);
					}

					for (PropertyImportDTO property : mapItemProperties)
					{
						if (property.getTypeName().equals("GEM_Flags"))
						{
							// Already handled above
						}
						else
						{
							throw new RuntimeException("Properties on map set items not yet handled");
							// This code is correct, but our gui doesn't expect this, so throw an error for now, so we know if any show up.
							// importUtil_.addStringAnnotation(ComponentReference.fromChronology(association), property.getValueNew(),
							// attributes_.getProperty(property.getTypeName()).getUUID(), property.isActive() ? Status.ACTIVE : Status.INACTIVE);
						}
					}
					/*
					 * We just won't add these, as we control the data queried (if they were present)
					 * if (mapItem.getDesignations() != null && mapItem.getDesignations().size() > 0)
					 * {
					 * throw new RuntimeException("Designations on map set items not yet handled");
					 * }
					 * if (mapItem.getRelationships() != null && mapItem.getRelationships().size() > 0)
					 * {
					 * throw new RuntimeException("Relationships on map set items not yet handled");
					 * }
					 */
					mapEntryCount++;
				}
			}
		}
		return concept;
	}

	private ConceptVersion createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptVersion concept = importUtil_.createConcept(typeName, true);
		loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil_.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	private UUID getAssociationOrphanUuid(String code)
	{
		return Get.service(ConverterUUID.class).createNamespaceUUIDFromString("associationOrphan:" + code, true);
	}

	private UUID getMapItemUUID(UUID mapSetUUID, String mapItemVuid)
	{
		return Get.service(ConverterUUID.class).createNamespaceUUIDFromString("mapSetUuid:" + mapSetUUID + "mapItemVuid:" + mapItemVuid, false);
	}

	private UUID getConceptUuid(String codeId)
	{
		return Get.service(ConverterUUID.class).createNamespaceUUIDFromString("code:" + codeId, true);
	}

	private UUID getDescriptionUuid(String descriptionId)
	{
		return Get.service(ConverterUUID.class).createNamespaceUUIDFromString("description:" + descriptionId, true);
	}

	public static void main(String[] args) throws MojoExecutionException
	{
		VHATSQLImportMojo i = new VHATSQLImportMojo();
		// Change here if not using the default in vhat-mojo/src/main/resources/config.properties
		i.setupH2(new File("c:\\va\\tmp\\vts-import"));
		i.outputDirectory = new File("../vhat-ibdf-sql/target");
		i.inputFileLocation = new File("../vhat-ibdf-sql/target/generated-resources/src/");
		i.converterOutputArtifactVersion = "2017.01.24.foo";
		i.converterOutputArtifactId = "vhat-ibdf-sql";
		i.converterOutputArtifactClassifier = "";
		i.converterVersion = "SNAPSHOT";
		i.converterSourceArtifactVersion = "fre";
		i.execute();
		javafx.application.Platform.exit();
	}

	private void setupH2(File h2File) throws MojoExecutionException
	{
		try
		{
			H2DatabaseHandle _h2 = new H2DatabaseHandle();
			_h2.createOrOpenDatabase(h2File);
			importer.setDatabaseConnection(_h2.getConnection());
		}
		catch (Exception e)
		{
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	private class ValuePropertyPairExtended extends ValuePropertyPair
	{
		private DesignationImportDTO didto_;

		public ValuePropertyPairExtended(String value, UUID descriptionUUID, Property property, DesignationImportDTO didto, boolean disabled)
		{
			super(value, descriptionUUID, property);
			didto_ = didto;
			setDisabled(disabled);
		}

		public DesignationImportDTO getDesignationImportDTO()
		{
			return didto_;
		}
	}
}