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
import java.io.FileWriter;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import javafx.application.Platform;
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
 * @author  a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * Goal which converts VHAT XML into the isaac format
 */
@Mojo(name = "convert-VHAT-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class VHATImportMojo extends ConverterBaseMojo
{
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

	private HashMap<UUID, String> associationOrphanConcepts = new HashMap<>();// ID to designation
	
	HashMap<Long, UUID> subsetVuidToConceptIdMap = new HashMap<>();

	@Override
	public void execute() throws MojoExecutionException
	{

		try
		{
			super.execute();

			String temp = converterOutputArtifactVersion.substring(0, 10);
			importUtil = new IBDFCreationUtility(Optional.of("VHAT " + converterSourceArtifactVersion), Optional.of(MetaData.VHAT_MODULES____SOLOR),
					outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, false,
					new SimpleDateFormat("yyyy.MM.dd").parse(temp).getTime());

			attributes_ = new PT_Annotations();
			descriptions_ = new BPT_Descriptions(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get());
			associations_ = new BPT_Associations(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get());
			relationships_ = new BPT_Relations(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get());
			refsets_ = new BPT_Refsets(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get());
			refsets_.addProperty(VHATConstants.VHAT_ALL_CONCEPTS.getRegularName().get());

			TerminologyDataReader importer = new TerminologyDataReader(inputFileLocation);
			TerminologyDTO terminology = importer.process();

			List<TypeImportDTO> dto = terminology.getTypes();

			ComponentReference vhatMetadata = ComponentReference.fromConcept(
					createType(MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(), "VHAT Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG));

			importUtil.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			// TODO would be nice to automate this
			importUtil.registerDynamicColumnInfo(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_NID_EXTENSION.getPrimordialUuid(),
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_NID_EXTENSION.getDynamicColumns());
			importUtil.registerDynamicColumnInfo(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid(),
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getDynamicColumns());
			importUtil.registerDynamicColumnInfo(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(),
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getDynamicColumns());

			// read in the dynamic types
			for (TypeImportDTO typeImportDTO : dto)
			{
				if (typeImportDTO.getKind().equals("DesignationType"))
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
				else if (typeImportDTO.getKind().equals("RelationshipType"))
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
				else if (typeImportDTO.getKind().equals("PropertyType"))
				{
					if (typeImportDTO.getName().equals("GEM_Flags"))
					{
						// skip - GEM_Flags are loaded as a column on a mapset instead.
					}
					else
					{
						attributes_.addProperty(typeImportDTO.getName());
					}
				}
				else
				{
					System.err.println("Unexpected Type!");
				}
			}

			// get the refset names
			for (SubsetImportDTO subset : terminology.getSubsets())
			{
				refsets_.addProperty(subset.getSubsetName());
			}

			importUtil.loadMetaDataItems(Arrays.asList(descriptions_, attributes_, associations_, relationships_, refsets_), vhatMetadata.getPrimordialUuid());

			ConsoleUtil.println("Metadata load stats");
			for (String line : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}

			importUtil.clearLoadStats();

			allVhatConceptsRefset = refsets_.getProperty(VHATConstants.VHAT_ALL_CONCEPTS.getRegularName().get()).getUUID();
			loadedConcepts.put(allVhatConceptsRefset, VHATConstants.VHAT_ALL_CONCEPTS.getRegularName().get());

			//Put the vuids onto the subsets
			for (SubsetImportDTO subset : terminology.getSubsets())
			{
				UUID concept = refsets_.getProperty(subset.getSubsetName()).getUUID();
				loadedConcepts.put(concept, subset.getSubsetName());
				importUtil.addStaticStringAnnotation(ComponentReference.fromConcept(concept), subset.getVuid().toString(), attributes_.getProperty("VUID").getUUID(), 
						Status.ACTIVE);
				subsetVuidToConceptIdMap.put(subset.getVuid(), concept);
			}

			// TODO use the codesystem version info?
			// TODO use the Version info?

			for (ConceptImportDTO item : terminology.getCodeSystem().getVersion().getConcepts())
			{
				conceptCount++;
				writeEConcept(item);
				if (conceptCount % 500 == 0)
				{
					ConsoleUtil.showProgress();
				}
				if (conceptCount % 10000 == 0)
				{
					ConsoleUtil.println("Processed " + conceptCount + " concepts");
				}
			}

			ConsoleUtil.println("Processed " + conceptCount + " concepts");
			ConsoleUtil.println("Starting mapsets");

			for (MapSetImportDTO item : terminology.getCodeSystem().getVersion().getMapsets())
			{
				mapSetCount++;
				writeEConcept(item);
				if (mapEntryCount % 100 == 0)
				{
					ConsoleUtil.showProgress();
				}
				if (mapEntryCount % 500 == 0)
				{
					ConsoleUtil.println("Processed " + mapSetCount + " mapsets with " + mapEntryCount + " members");
				}
			}

			ConsoleUtil.println("Processed " + mapSetCount + " mapsets with " + mapEntryCount + " members");

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
				ConceptVersion missingParent = importUtil.createConcept("Missing Concepts", true);
				importUtil.addParent(ComponentReference.fromConcept(missingParent), rootConceptUUID);
				for (UUID refUUID : missingConcepts)
				{
					ComponentReference c = ComponentReference.fromConcept(importUtil.createConcept(refUUID, "-MISSING-", true));
					importUtil.addParent(c, missingParent.getPrimordialUuid());
				}
			}
			// Handle missing association sources and targets
			ConsoleUtil.println("Creating placeholder concepts for " + associationOrphanConcepts.size() + " association orphans");
			// We currently don't have these association targets, so need to invent placeholder concepts.
			ComponentReference missingSDORefset = ComponentReference.fromConcept(importUtil.createConcept(null,
					VHATConstants.VHAT_MISSING_SDO_CODE_SYSTEM_CONCEPTS.getRegularName().get(), null, null, null, refsets_.getPropertyTypeUUID(), (UUID) null));
			importUtil.configureConceptAsDynamicRefex(missingSDORefset, "A simple refset to store the missing concepts we have to create during import because"
					+ " we don't yet have the SDO code systems in place", null, IsaacObjectType.CONCEPT, null);
			for (Entry<UUID, String> item : associationOrphanConcepts.entrySet())
			{
				if (loadedConcepts.get(item.getKey()) == null)
				{
					importUtil.addAssemblageMembership(ComponentReference.fromConcept(importUtil.createConcept(item.getKey(), item.getValue(), true)),
							missingSDORefset.getPrimordialUuid(), Status.ACTIVE, null);
				}
			}

			VhatUtil.check(associations_, attributes_, descriptions_, refsets_, rootConceptUUID, missingSDORefset.getPrimordialUuid());

			ConsoleUtil.println("Sanity checked static UUIDs - all ok");

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

			ConsoleUtil.println("Load Statistics");
			// swap out vuids with names to make it more readable...
			for (String line : importUtil.getLoadStats().getSummary())
			{
				Enumeration<String> e = stringsToSwap.keys();
				while (e.hasMoreElements())
				{
					String current = e.nextElement();
					line = line.replaceAll(current, stringsToSwap.get(current));
				}
				ConsoleUtil.println(line);
			}

			// this could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			ConverterUUID.dump(outputDirectory, "vhatUuid");

			if (conceptsWithNoDesignations.size() > 0)
			{
				ConsoleUtil
						.printErrorln(conceptsWithNoDesignations.size() + " concepts were found with no descriptions at all.  These were assigned '-MISSING-'");
				FileWriter fw = new FileWriter(new File(outputDirectory, "NoDesignations.txt"));
				for (String s : conceptsWithNoDesignations)
				{
					fw.write(s);
					fw.write(System.getProperty("line.separator"));
				}
				fw.close();
			}
			importUtil.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}

	}

	private void writeEConcept(NamedPropertiedItemImportDTO conceptOrMapSet) throws Exception
	{
		boolean isMapSet = false;
		if (conceptOrMapSet instanceof MapSetImportDTO)
		{
			isMapSet = true;
			mapSetCount++;
		}

		ComponentReference concept = ComponentReference.fromConcept(
				importUtil.createConcept(getConceptUuid(conceptOrMapSet.getCode()), null, conceptOrMapSet.isActive() ? Status.ACTIVE : Status.INACTIVE, null));
		loadedConcepts.put(concept.getPrimordialUuid(), conceptOrMapSet.getCode());
		importUtil.addStaticStringAnnotation(concept, conceptOrMapSet.getVuid().toString(), attributes_.getProperty("VUID").getUUID(), Status.ACTIVE);
		importUtil.addStaticStringAnnotation(concept, conceptOrMapSet.getCode(), attributes_.getProperty("Code").getUUID(), Status.ACTIVE);

		ArrayList<ValuePropertyPairExtended> descriptionHolder = new ArrayList<>();
		if (isMapSet)
		{
			for (DesignationImportDTO didto : ((MapSetImportDTO) conceptOrMapSet).getDesignations())
			{
				descriptionHolder.add(new ValuePropertyPairExtended(didto.getValueNew(), getDescriptionUuid(didto.getCode().toString()),
						descriptions_.getProperty(didto.getTypeName()), didto, !didto.isActive()));
			}
		}
		else
		{
			for (DesignationExtendedImportDTO didto : ((ConceptImportDTO) conceptOrMapSet).getDesignations())
			{
				descriptionHolder.add(new ValuePropertyPairExtended(didto.getValueNew(), getDescriptionUuid(didto.getCode().toString()),
						descriptions_.getProperty(didto.getTypeName()), didto, !didto.isActive()));
			}
		}

		for (PropertyImportDTO property : conceptOrMapSet.getProperties())
		{
			importUtil.addStringAnnotation(concept, property.getValueNew(), attributes_.getProperty(property.getTypeName()).getUUID(),
					property.isActive() ? Status.ACTIVE : Status.INACTIVE);
		}

		List<SemanticChronology> wbDescriptions = importUtil.addDescriptions(concept, descriptionHolder);

		// Descriptions have now all been added to the concepts - now we need to process the rest of the ugly bits of vhat
		// and place them on the descriptions.
		for (int i = 0; i < descriptionHolder.size(); i++)
		{
			ValuePropertyPairExtended vpp = descriptionHolder.get(i);
			SemanticChronology desc = wbDescriptions.get(i);

			if (vpp.getValue().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get()))
			{
				// On the root node, we need to add some extra attributes
				importUtil.addDescription(concept, VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get(), DescriptionType.REGULAR_NAME, true,
						descriptions_.getProperty("Synonym").getUUID(), Status.ACTIVE);
				importUtil.addDescription(concept, "VHA Terminology", DescriptionType.REGULAR_NAME, false, descriptions_.getProperty("Synonym").getUUID(),
						Status.ACTIVE);
				ConsoleUtil.println("Root concept FSN is 'VHAT' and the UUID is " + concept.getPrimordialUuid());
				importUtil.addParent(concept, MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid());
				rootConceptUUID = concept.getPrimordialUuid();
			}
			importUtil.addStaticStringAnnotation(ComponentReference.fromChronology(desc, () -> "Description"), vpp.getDesignationImportDTO().getVuid() + "",
					attributes_.getProperty("VUID").getUUID(), Status.ACTIVE);
			importUtil.addStaticStringAnnotation(ComponentReference.fromChronology(desc, () -> "Description"), vpp.getDesignationImportDTO().getCode(),
					attributes_.getProperty("Code").getUUID(), Status.ACTIVE);

			// VHAT is kind of odd, in that the attributes are attached to the description, rather than the concept.
			if (!isMapSet)
			{
				for (PropertyImportDTO property : ((DesignationExtendedImportDTO) vpp.getDesignationImportDTO()).getProperties())
				{
					importUtil.addStringAnnotation(ComponentReference.fromChronology(desc, () -> "Description"), property.getValueNew(),
							attributes_.getProperty(property.getTypeName()).getUUID(), property.isActive() ? Status.ACTIVE : Status.INACTIVE);
				}
				
				for (SubsetMembershipImportDTO subsetMembership : ((DesignationExtendedImportDTO) vpp.getDesignationImportDTO()).getSubsets())
				{
					if (subsetMembership.getVuid() != null)
					{
						UUID subsetConcept = subsetVuidToConceptIdMap.get(subsetMembership.getVuid());
						importUtil.addAssemblageMembership(ComponentReference.fromChronology(desc, () -> "Description"), subsetConcept, Status.ACTIVE, null);
					}
				}
			}
		}

		if (descriptionHolder.size() == 0)
		{
			// Seems like a data error - but it is happening... no descriptions at all.....
			conceptsWithNoDesignations.add(conceptOrMapSet.getCode());
			// The workbench implodes if you don't have a fully specified name....
			importUtil.addDescription(concept, "-MISSING-", DescriptionType.FULLY_QUALIFIED_NAME, true, descriptions_.getProperty("Synonym").getUUID(),
					Status.ACTIVE);
		}

		List<RelationshipImportDTO> relationshipImports = conceptOrMapSet.getRelationships();
		LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
		ArrayList<ConceptAssertion> assertions = new ArrayList<>();
		if (relationshipImports != null)
		{
			UUID isANativeType = null;
			for (RelationshipImportDTO relationshipImportDTO : relationshipImports)
			{
				UUID sourceUuid = getConceptUuid(conceptOrMapSet.getCode());
				UUID targetUuid = getConceptUuid(relationshipImportDTO.getNewTargetCode());

				referencedConcepts.put(targetUuid, relationshipImportDTO.getNewTargetCode());

				if (!sourceUuid.equals(concept.getPrimordialUuid()))
				{
					throw new MojoExecutionException("Design failure!");
				}

				importUtil.addAssociation(concept, null, targetUuid, associations_.getProperty(relationshipImportDTO.getTypeName()).getUUID(),
						relationshipImportDTO.isActive() ? Status.ACTIVE : Status.INACTIVE, null, null);

				// If it is an isA rel, also create it as a rel.
				if (associations_.getProperty(relationshipImportDTO.getTypeName()) != null
						&& MetaData.IS_A____SOLOR.getPrimordialUuid().equals(associations_.getProperty(relationshipImportDTO.getTypeName()).getWBTypeUUID()))
				{
					assertions.add(ConceptAssertion(Get.identifierService().getNidForUuids(targetUuid), leb));
					isANativeType = associations_.getProperty(relationshipImportDTO.getTypeName()).getUUID();
				}
			}
			if (assertions.size() > 0)
			{
				NecessarySet(And(assertions.toArray(new Assertion[assertions.size()])));
				importUtil.addRelationshipGraph(concept, null, leb.build(), true, null, null, isANativeType);  // TODO handle inactive
			}
		}

		importUtil.addAssemblageMembership(concept, allVhatConceptsRefset, Status.ACTIVE, null);

		if (isMapSet)
		{
			// Add a relationship to the subsets metadata concept.
			if (relationshipImports != null && relationshipImports.size() > 0)
			{
				throw new RuntimeException("Didn't expect mapsets to have their own relationships!");
			}

			// add it as an association too
			importUtil.addAssociation(concept, null, refsets_.getPropertyTypeUUID(), associations_.getProperty("has_parent").getUUID(), Status.ACTIVE, null,
					null);

			importUtil.addAssociation(concept, null, refsets_.getAltMetaDataParentUUID(), associations_.getProperty("has_parent").getUUID(), Status.ACTIVE,
					null, null);

			importUtil.addAssociation(concept, null, IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid(),
					associations_.getProperty("has_parent").getUUID(), Status.ACTIVE, null, null);

			// place it in three places - refsets under VHAT Metadata, vhat refsets under SOLOR Refsets, and the dynamic semantic mapping semantic type.
			NecessarySet(And(new Assertion[] { ConceptAssertion(Get.identifierService().getNidForUuids(refsets_.getAltMetaDataParentUUID()), leb),
					ConceptAssertion(Get.identifierService().getNidForUuids(refsets_.getPropertyTypeUUID()), leb),
					ConceptAssertion(
							Get.identifierService().getNidForUuids(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid()),
							leb) }));
			importUtil.addRelationshipGraph(concept, null, leb.build(), true, null, null);

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

			importUtil.configureConceptAsDynamicRefex(concept, mapSet.getName(), columns, IsaacObjectType.CONCEPT, null);

			// Annotate this concept as a mapset definition concept.
			importUtil.addAnnotation(concept, null, null, IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid(),
					Status.ACTIVE, null);

			// Now that we have defined the map semantic, add the other annotations onto the map set definition.
			if (StringUtils.isNotBlank(mapSet.getSourceCodeSystemName()))
			{
				importUtil.addAnnotation(concept, null,
						new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM.getNid()),
								new DynamicStringImpl(mapSet.getSourceCodeSystemName()) },
						IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
			}

			if (StringUtils.isNotBlank(mapSet.getSourceVersionName()))
			{
				importUtil.addAnnotation(concept, null,
						new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM_VERSION.getNid()),
								new DynamicStringImpl(mapSet.getSourceVersionName()) },
						IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
			}

			if (StringUtils.isNotBlank(mapSet.getTargetCodeSystemName()))
			{
				importUtil.addAnnotation(concept, null,
						new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM.getNid()),
								new DynamicStringImpl(mapSet.getTargetCodeSystemName()) },
						IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
			}

			if (StringUtils.isNotBlank(mapSet.getTargetVersionName()))
			{
				importUtil.addAnnotation(concept, null,
						new DynamicData[] { new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM_VERSION.getNid()),
								new DynamicStringImpl(mapSet.getTargetVersionName()) },
						IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
			}

			for (MapEntryImportDTO mapItem : mapSet.getMapEntries())
			{
				ComponentReference sourceConcept = ComponentReference
						.fromConcept(mapSet.getSourceCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get())
								? getConceptUuid(mapItem.getSourceCode())
								: getAssociationOrphanUuid(mapItem.getSourceCode()));

				if (!loadedConcepts.containsKey(sourceConcept.getPrimordialUuid()))
				{
					if (mapSet.getSourceCodeSystemName().equals(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get()))
					{
						ConsoleUtil.printErrorln("Missing VHAT association source concept! " + mapItem.getSourceCode());
					}
					associationOrphanConcepts.put(sourceConcept.getPrimordialUuid(), mapItem.getSourceCode());
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

				SemanticChronology association = importUtil.addAnnotation(sourceConcept,
						getMapItemUUID(concept.getPrimordialUuid(), mapItem.getVuid().toString()), columnData, concept.getPrimordialUuid(),
						mapItem.isActive() ? Status.ACTIVE : Status.INACTIVE, null, null);

				importUtil.addStaticStringAnnotation(ComponentReference.fromChronology(association, () -> "Association"), mapItem.getVuid().toString(),
						attributes_.getProperty("VUID").getUUID(), Status.ACTIVE);

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
			}

		}
	}

	private ConceptVersion createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptVersion concept = importUtil.createConcept(typeName, true);
		loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	private UUID getAssociationOrphanUuid(String code)
	{
		UUID temp = ConverterUUID.createNamespaceUUIDFromString("associationOrphan:" + code, true);
		Get.identifierService().assignNid(temp);
		return temp;
	}

	private UUID getMapItemUUID(UUID mapSetUUID, String mapItemVuid)
	{
		return ConverterUUID.createNamespaceUUIDFromString("mapSetUuid:" + mapSetUUID + "mapItemVuid:" + mapItemVuid, false);
	}

	private UUID getConceptUuid(String codeId)
	{
		UUID temp = ConverterUUID.createNamespaceUUIDFromString("code:" + codeId, true);
		Get.identifierService().assignNid(temp);
		return temp;
	}

	private UUID getDescriptionUuid(String descriptionId)
	{
		return ConverterUUID.createNamespaceUUIDFromString("description:" + descriptionId, false);
	}

	public static void main(String[] args) throws MojoExecutionException
	{
		VHATImportMojo i = new VHATImportMojo();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocation = new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
		i.converterOutputArtifactVersion = "2016.01.07.foo";
		i.converterVersion = "SNAPSHOT";
		i.converterSourceArtifactVersion = "fre";
		i.execute();
		Platform.exit();
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