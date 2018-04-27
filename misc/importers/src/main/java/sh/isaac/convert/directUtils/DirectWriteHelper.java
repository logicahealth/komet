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
package sh.isaac.convert.directUtils;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedObjectService;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.MutableDynamicVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.util.SemanticTags;
import sh.isaac.api.util.UuidFactory;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.converters.sharedUtils.stats.LoadStats;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;

/**
 * A class similar to {@link IBDFCreationUtility}, but intended for direct writes, rather than the previous traditional way
 * to write IBDF files
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DirectWriteHelper
{
	private Logger log = LogManager.getLogger();
	
	private List<IndexBuilderService> indexers;
	private int authorNid;
	private int moduleNid;
	private int pathNid;
	private ConverterUUID converterUUID;
	private ConceptService conceptService;
	private IdentifierService identifierService;
	private IdentifiedObjectService identifiedObjectService;
	private StampService stampService;
	private AssemblageService assemblageService;
	private SemanticBuilderService<?> semanticBuilderService;
	private LogicalExpressionBuilderService logicalExpressionBuilderService;
	private TaxonomyService taxonomyService;
	
	private HashSet<Integer> deferredTaxonomyUpdates = new HashSet<Integer>();
	private LoadStats loadStats;
	
	private Optional<UUID> associationTypes = Optional.empty();
	private Optional<UUID> attributeTypes = Optional.empty();
	private Optional<UUID> descriptionTypes = Optional.empty();
	private Optional<UUID> refsetTypes = Optional.empty();
	private Optional<UUID> relationTypes = Optional.empty();

	private HashSet<UUID> associations = new HashSet<>();
	
	/**
	 * If you create ANY logic graphs, ensure that you call {@link #processTaxonomyUpdates()} before allowing this helper to be 
	 * garbage collected.
	 * 
	 * @param author The default author to use for creates
	 * @param module The default module to use for creates
	 * @param path The default path to use for creates
	 * @param converterUUID The converterUUID debug tool to use to generate and store UUID mappings
	 */
	public DirectWriteHelper(int author, int module, int path, ConverterUUID converterUUID)
	{
		this.indexers = Get.services(IndexBuilderService.class);
		this.authorNid = author;
		this.moduleNid = module;
		this.pathNid = path;
		this.converterUUID = converterUUID;
		this.conceptService = Get.conceptService();
		this.identifierService = Get.identifierService();
		this.identifiedObjectService = Get.identifiedObjectService();
		this.stampService = Get.stampService();
		this.assemblageService = Get.assemblageService();
		this.semanticBuilderService = Get.semanticBuilderService();
		this.logicalExpressionBuilderService = Get.logicalExpressionBuilderService();
		this.taxonomyService = Get.taxonomyService();
		this.loadStats = new LoadStats();
	}

	/**
	 * Calls {@link #makeConcept(UUID, Status, long, UUID[])} with no additional IDs
	 * 
	 * @param concept The UUID of the concept to be created
	 * @param status The status of the concept to be created
	 * @param time The time to use for the concept to be created
	 */
	public void makeConcept(UUID concept, Status status, long time)
	{
		makeConcept(concept, status, time, null);
	}

	/**
	 * @param concept The UUID of the concept to be created
	 * @param status The status of the concept to be created
	 * @param time The time to use for the concept to be created
	 * @param additionalUUIDs additional UUIDs for the concept, if any.
	 */
	public void makeConcept(UUID concept, Status status, long time, UUID[] additionalUUIDs)
	{
		int nid = identifierService.assignNid(concept);
		if (conceptService.hasConcept(nid))
		{
			throw new RuntimeException("Tried to create " + concept + " twice! " + converterUUID.getUUIDCreationString(concept));
		}

		ConceptChronologyImpl conceptToWrite = new ConceptChronologyImpl(concept, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
		conceptToWrite.addAdditionalUuids(additionalUUIDs);
		int conceptStamp = stampService.getStampSequence(status, time, authorNid, moduleNid, pathNid);
		conceptToWrite.createMutableVersion(conceptStamp);
		
		indexAndWrite(conceptToWrite);
		loadStats.addConcept();
	}

	/**
	 * This creates a semantic type of {@link VersionType#DYNAMIC} with no data columns
	 * 
	 * This serves the same purpose as {@link #makeBrittleRefsetMember(UUID, UUID, long)} but allows for the refset to be described in the 
	 * dynamic definition.
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param memberUUID The id being added to the refset
	 * @param time The time to use for the entry
	 * @return The UUID of the object created
	 */
	public UUID makeDynamicRefsetMember(UUID assemblageConcept, UUID memberUUID, long time)
	{
		UUID uuidForCreatedMember = UuidFactory.getUuidForDynamic(converterUUID.getNamespace(), assemblageConcept, memberUUID, null,
				((input, uuid) -> converterUUID.addMapping(input, uuid)));
		SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(VersionType.DYNAMIC,
				uuidForCreatedMember, identifierService.getNidForUuids(assemblageConcept), identifierService.getNidForUuids(memberUUID));
		refsetMemberToWrite.createMutableVersion(stampService.getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, pathNid));
		indexAndWrite(refsetMemberToWrite);
		loadStats.addRefsetMember(Get.conceptDescriptionText(identifierService.getNidForUuids(assemblageConcept)));
		return refsetMemberToWrite.getPrimordialUuid();
	}
	
	/**
	 * This creates a semantic type of {@link VersionType#MEMBER}
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param memberUUID The id being added to the refset
	 * @param time The time to use for the entry
	 * @return The UUID of the object created
	 */
	public UUID makeBrittleRefsetMember(UUID assemblageConcept, UUID memberUUID, long time)
	{
		UUID uuidForCreatedMember = UuidFactory.getUuidForDynamic(converterUUID.getNamespace(), assemblageConcept, memberUUID, null,
				((input, uuid) -> converterUUID.addMapping(input, uuid)));
		SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(VersionType.DYNAMIC,
				uuidForCreatedMember, identifierService.getNidForUuids(assemblageConcept), identifierService.getNidForUuids(memberUUID));
		refsetMemberToWrite.createMutableVersion(stampService.getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, pathNid));
		indexAndWrite(refsetMemberToWrite);
		loadStats.addRefsetMember(Get.conceptDescriptionText(identifierService.getNidForUuids(assemblageConcept)));
		return refsetMemberToWrite.getPrimordialUuid();
	}
	
	/**
	 * This creates a semantic type of {@link VersionType#DYNAMIC} with the specified data columns.
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param referencedComponent the referenced component this dynamic semantic entry is being added to
	 * @param data optional - The data columns for this dynamic semantic entry
	 * @param time The time to use for the entry
	 * @return The UUID of the object created
	 */
	public UUID makeDynamicSemantic(UUID assemblageConcept, UUID referencedComponent, DynamicData[] data, long time)
	{
		UUID uuidForCreatedMember = UuidFactory.getUuidForDynamic(converterUUID.getNamespace(), assemblageConcept, referencedComponent, data,
				((input, uuid) -> converterUUID.addMapping(input, uuid)));
		SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(VersionType.DYNAMIC,
				uuidForCreatedMember, identifierService.getNidForUuids(assemblageConcept), identifierService.getNidForUuids(referencedComponent));
		MutableDynamicVersion<?> dv = refsetMemberToWrite.createMutableVersion(stampService.getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, pathNid));
		dv.setData(data);
		indexAndWrite(refsetMemberToWrite);
		loadStats.addAnnotation(identifiedObjectService.getChronology(identifierService.getNidForUuids(referencedComponent)).get().getIsaacObjectType().toString(),
				converterUUID.getUUIDCreationString(assemblageConcept));
		return refsetMemberToWrite.getPrimordialUuid();
	}
	
	/**
	 * create a dynamic semantic entry in the pattern that matches an assemblage dynamic semantic.
	 * @param assemblageConcept the type of association to create
	 * @param sourceConcept the source of the association
	 * @param targetConcept the optional target of the association
	 * @param time the time to make the changes at
	 * @return the UUID of the object created
	 */
	public UUID makeAssociation(UUID assemblageConcept, UUID sourceConcept, UUID targetConcept, long time)
	{
		return makeDynamicSemantic(assemblageConcept, sourceConcept, targetConcept == null ? null : new DynamicData[] {new DynamicUUIDImpl(targetConcept)}, time);
	}
	
	/**
	 * Calls {@link #makeDescription(UUID, String, UUID, UUID, UUID, Status, long, UUID, UUID)} with {@link MetaData#ENGLISH_DIALECT_ASSEMBLAGE____SOLOR} 
	 *   and {@link MetaData#US_ENGLISH_DIALECT____SOLOR}
	 * @param concept The concept to attach the description onto
	 * @param text The text of the description
	 * @param descriptionType The type of the description - {@link TermAux#DEFINITION_DESCRIPTION_TYPE} or {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
	 *   or {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE} 
	 * @param caseSignificance {@link TermAux#DESCRIPTION_NOT_CASE_SENSITIVE} or {@link TermAux#DESCRIPTION_INITIAL_CHARACTER_SENSITIVE}
	 * @param status the status for the description
	 * @param time the time for the description
	 * @param dialectAcceptibility The dialect acceptability of the description.  Ignored if dialect is null.  required if dialect is not null.
	 *   either {@link TermAux#ACCEPTABLE} or {@link TermAux#PREFERRED}
	 * @return the UUID of the created description
	 */
	public UUID makeDescriptionEn(UUID concept, String text, UUID descriptionType, UUID caseSignificance, Status status, long time, UUID dialectAcceptibility)
	{
		return makeDescription(concept, text, descriptionType, MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(), caseSignificance, status, time, 
				MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid(), dialectAcceptibility);
	}
	
	/**
	 * Create a new dynamic semantic of type {@link DynamicConstants#DYNAMIC_COMMENT_ATTRIBUTE} in the corret format.
	 * @param referencedComponent the item to place the comment on
	 * @param comment the comment
	 * @param commentContext the optional comment context
	 * @param time the time to make the edit it
	 * @return the created object
	 */
	public UUID makeComment(UUID referencedComponent, String comment, String commentContext, long time)
	{
		return makeDynamicSemantic(DynamicConstants.get().DYNAMIC_COMMENT_ATTRIBUTE.getPrimordialUuid(), referencedComponent, 
				new DynamicData[] {new DynamicStringImpl(comment),
						(StringUtils.isBlank(commentContext) ? null : new DynamicStringImpl(commentContext))}, time);
	}
	
	/**
	 * @param concept The concept to attach the description onto
	 * @param text The text of the description
	 * @param descriptionType The type of the description - {@link TermAux#DEFINITION_DESCRIPTION_TYPE} or {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
	 *   or {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE} 
	 * @param language The language concept of the description
	 * @param caseSignificance {@link TermAux#DESCRIPTION_NOT_CASE_SENSITIVE} or {@link TermAux#DESCRIPTION_INITIAL_CHARACTER_SENSITIVE}
	 * @param status the status for the description
	 * @param time the time for the description
	 * @param dialect The dialect of the description.  Null to skip making a dialect.
	 * @param dialectAcceptibility The dialect acceptability of the description.  Ignored if dialect is null.  required if dialect is not null.
	 *   either {@link TermAux#ACCEPTABLE} or {@link TermAux#PREFERRED}
	 * @return the UUID of the created description
	 */
	public UUID makeDescription(UUID concept, String text, UUID descriptionType, UUID language, UUID caseSignificance, Status status, long time, UUID dialect,
			UUID dialectAcceptibility)
	{
		UUID descriptionUuid = UuidFactory.getUuidForDescriptionSemantic(converterUUID.getNamespace(), concept, caseSignificance, descriptionType, language,
				text, ((input, uuid) -> converterUUID.addMapping(input, uuid)));

		SemanticChronologyImpl descriptionToWrite = new SemanticChronologyImpl(VersionType.DESCRIPTION, descriptionUuid,
				identifierService.getNidForUuids(language), identifierService.getNidForUuids(concept));
		int stamp = stampService.getStampSequence(status, time, authorNid, moduleNid, pathNid);
		DescriptionVersionImpl descriptionVersion = descriptionToWrite.createMutableVersion(stamp);
		descriptionVersion.setCaseSignificanceConceptNid(identifierService.getNidForUuids(caseSignificance));
		descriptionVersion.setDescriptionTypeConceptNid(identifierService.getNidForUuids(descriptionType));
		descriptionVersion.setLanguageConceptNid(identifierService.getNidForUuids(language));
		descriptionVersion.setText(text);

		indexAndWrite(descriptionToWrite);

		if (dialect != null)
		{
			UUID dialectUUID = UuidFactory.getUuidForComponentNidSemantic(converterUUID.getNamespace(), dialect, descriptionUuid, dialectAcceptibility,
					((input, uuid) -> converterUUID.addMapping(input, uuid)));
			
			SemanticChronologyImpl dialectToWrite = new SemanticChronologyImpl(VersionType.COMPONENT_NID, dialectUUID,
					identifierService.getNidForUuids(dialect), identifierService.getNidForUuids(descriptionUuid));
			int dialectStamp = stampService.getStampSequence(status, time, authorNid, moduleNid, pathNid);
			ComponentNidVersionImpl dialectVersion = dialectToWrite.createMutableVersion(dialectStamp);
	
			dialectVersion.setComponentNid(identifierService.getNidForUuids(dialectAcceptibility));
			indexAndWrite(dialectToWrite);
		}
		loadStats.addDescription(Get.conceptDescriptionText(identifierService.getNidForUuids(descriptionType)));
		return descriptionUuid;
	}
	
	/**
	 * Ensure that you call {@link #processTaxonomyUpdates()} at some point after using this method.
	 * 
	 * @param concept The concept we are placing the graph on
	 * @param parent The parent of this concept
	 * @param status The status of the graph
	 * @param time The time for the graph
	 * @return The identifier of the created graph
	 */
	public UUID makeParentGraph(UUID concept, UUID parent, Status status, long time)
	{
		return makeParentGraph(concept, Arrays.asList(new UUID[] {parent}), status, time);
	}
	
	/**
	 * Ensure that you call {@link #processTaxonomyUpdates()} at some point after using this method.
	 * 
	 * @param concept The concept we are placing the graph on
	 * @param parents The parent(s) of this concept
	 * @param status The status of the graph
	 * @param time The time for the graph
	 * @return The identifier of the created graph
	 */
	public UUID makeParentGraph(UUID concept, List<UUID> parents, Status status, long time)
	{
		final LogicalExpressionBuilder leb = logicalExpressionBuilderService.getLogicalExpressionBuilder();

		// Eliminate duplicates
		final Set<UUID> uuids = new HashSet<>();
		for (UUID parentUuid : parents)
		{
			uuids.add(parentUuid);
		}

		final Assertion[] assertions = new Assertion[uuids.size()];

		int i = 0;
		for (UUID parentUuid : uuids)
		{
			assertions[i++] = ConceptAssertion(Get.identifierService().getNidForUuids(parentUuid), leb);
		}
		NecessarySet(And(assertions));
		
		LogicalExpression le = leb.build();

		SemanticBuilder<?> sb = semanticBuilderService.getLogicalExpressionBuilder(le, Get.identifierService().getNidForUuids(concept), 
				MetaData.EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR.getNid());
		sb.setStatus(status);
		sb.setT5Uuid(converterUUID.getNamespace(), ((input, uuid) -> converterUUID.addMapping(input, uuid)));
		int graphStamp = Get.stampService().getStampSequence(status, time, authorNid, moduleNid, pathNid);
		SemanticChronology sc = (SemanticChronology)sb.build(graphStamp, new ArrayList<>());
		indexAndWrite(sc);
		deferredTaxonomyUpdates.add(sc.getNid());
		loadStats.addGraph();
		return sc.getPrimordialUuid();
	}

	/**
	 * @param moduleNid the new moduleNid
	 */
	public void changeModule(int moduleNid)
	{
		log.debug("Changing module nid from {} to {}", this.moduleNid, moduleNid);
		this.moduleNid = moduleNid;
	}
	
	/**
	 * Index and write the Chronology to the store.  This only needs to be called if you have created chronologies outside of this class.
	 * @param c
	 */
	public void indexAndWrite(Chronology c)
	{
		for (IndexBuilderService indexer : indexers)
		{
			indexer.indexNow(c);
		}
		if (c instanceof ConceptChronology)
		{
			conceptService.writeConcept((ConceptChronology)c);
		}
		else if (c instanceof SemanticChronology)
		{
			assemblageService.writeSemanticChronology((SemanticChronology)c);
		}
		else
		{
			throw new RuntimeException("unsupported type " + c);
		}
	}
	
	/**
	 * Properly update the taxonomy service for graph changes made.  Ensure to call this at some point after calling {@link #makeParentGraph(UUID, List, Status, long)}
	 */
	public void processTaxonomyUpdates()
	{
		log.debug("Processing deferred taxonomy updates");
		for (int nid : deferredTaxonomyUpdates)
		{
			taxonomyService.updateTaxonomy(Get.assemblageService().getSemanticChronology(nid));
		}
	}
	
	/**
	 * @return the load stats object that has details on what has been created by this class.
	 */
	public LoadStats getLoadStats()
	{
		return loadStats;
	}
	
	/**
	 * Set up a hierarchy under {@link MetaData#SOLOR_CONTENT_METADATA____SOLOR} for various assemblage types for a specific terminology import.
	 * @param terminologyName - The name to use as a prefix for all created metadata concepts
	 * @param makeAttributeTypes - create the {name} Attribute Types node.  This is used for arbitrary property types.
	 * @param makeDescriptionTypes - create the {name} Description Types node.  The is used for extended (native) description types.
	 * @param makeAssociationTypes - create the {name} Association Types node.  This is used for associations/relationships that don't go into the logic graph. 
	 * @param makeRefsets - create the {name} Refsets node.  This is for all member refset types
	 * @param makeRelationTypes - create the {name} Relation Types node.  This is used for extended (native) relationship types that were mapped to isA
	 *   and placed in the logic graph.
	 * @param time - the time to use for the creation
	 */
	public void makeMetadataHierarchy(String terminologyName, boolean makeAttributeTypes, boolean makeDescriptionTypes, boolean makeAssociationTypes, 
			boolean makeRefsets, boolean makeRelationTypes, long time)
	{
		String rootFsn = terminologyName + " Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG;
		UUID hierarchyRoot = converterUUID.createNamespaceUUIDFromString(rootFsn, true);
		if (!conceptService.hasConcept(identifierService.assignNid(hierarchyRoot)))
		{
			log.info("Building metadata root '" + rootFsn + "'");
			makeConcept(hierarchyRoot, Status.ACTIVE, time);
			makeDescriptionEn(hierarchyRoot, rootFsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
			makeDescriptionEn(hierarchyRoot, SemanticTags.stripSemanticTagIfPresent(rootFsn), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
			makeParentGraph(hierarchyRoot, MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(), Status.ACTIVE, time);
		}
		
		if (makeAttributeTypes)
		{
			String fsn = terminologyName + " Attribute Types" + IBDFCreationUtility.METADATA_SEMANTIC_TAG;
			log.info("Building attribute types '" + fsn + "'");
			attributeTypes = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(attributeTypes.get())))
			{
				makeConcept(attributeTypes.get(), Status.ACTIVE, time);
				makeDescriptionEn(attributeTypes.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(attributeTypes.get(), SemanticTags.stripSemanticTagIfPresent(fsn), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeParentGraph(attributeTypes.get(), hierarchyRoot, Status.ACTIVE, time);
			}
		}
		
		if (makeDescriptionTypes)
		{
			String fsn = terminologyName + " Description Types" + IBDFCreationUtility.METADATA_SEMANTIC_TAG;
			log.info("Building description types '" + fsn + "'");
			descriptionTypes = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(descriptionTypes.get())))
			{
				makeConcept(descriptionTypes.get(), Status.ACTIVE, time);
				makeDescriptionEn(descriptionTypes.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(descriptionTypes.get(), SemanticTags.stripSemanticTagIfPresent(fsn), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeParentGraph(descriptionTypes.get(), 
						Arrays.asList(new UUID[] {hierarchyRoot, MetaData.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getPrimordialUuid()}), Status.ACTIVE, time);
			}
		}
		
		if (makeAssociationTypes)
		{
			String fsn = terminologyName + " Association Types" + IBDFCreationUtility.METADATA_SEMANTIC_TAG;
			log.info("Building association types '" + fsn + "'");
			associationTypes = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(associationTypes.get())))
			{
				makeConcept(associationTypes.get(), Status.ACTIVE, time);
				makeDescriptionEn(associationTypes.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(associationTypes.get(), SemanticTags.stripSemanticTagIfPresent(fsn), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeParentGraph(associationTypes.get(), hierarchyRoot, Status.ACTIVE, time);
			}
		}
		
		if (makeRelationTypes)
		{
			String fsn = terminologyName + " Relation Types" + IBDFCreationUtility.METADATA_SEMANTIC_TAG;
			log.info("Building relation types '" + fsn + "'");
			relationTypes = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(relationTypes.get())))
			{
				makeConcept(relationTypes.get(), Status.ACTIVE, time);
				makeDescriptionEn(relationTypes.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(relationTypes.get(), SemanticTags.stripSemanticTagIfPresent(fsn), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeParentGraph(relationTypes.get(), 
						Arrays.asList(new UUID[] {hierarchyRoot, MetaData.RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getPrimordialUuid()}), Status.ACTIVE, time);
			}
		}
		
		if (makeRefsets)
		{
			String fsn = terminologyName + " Refsets" + IBDFCreationUtility.METADATA_SEMANTIC_TAG;
			log.info("Building refsets '" + fsn + "'");
			refsetTypes = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(refsetTypes.get())))
			{
				makeConcept(refsetTypes.get(), Status.ACTIVE, time);
				makeDescriptionEn(refsetTypes.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(refsetTypes.get(), SemanticTags.stripSemanticTagIfPresent(fsn), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeParentGraph(refsetTypes.get(), 
						Arrays.asList(new UUID[] {hierarchyRoot, MetaData.SOLOR_ASSEMBLAGE____SOLOR.getPrimordialUuid()}), Status.ACTIVE, time);
			}
		}
	}
	
	/**
	* Add all of the necessary metadata semantics onto the specified concept to make it a concept that defines a dynamic semantic assemblage
	* See {@link DynamicUsageDescription} class for more details on this format.
	* implemented by See {@link DynamicUtility#configureConceptAsDynamicSemantic(int, String, DynamicColumnInfo[], IsaacObjectType, VersionType, int)}
	* 
	* @param concept - The concept that will define a dynamic semantic
	* @param dynamicUsageDescription - The description that describes the purpose of this dynamic semantic
	* @param columns - optional - the columns of data that this dynamic semantic needs to be able to store.
	* @param referencedComponentTypeRestriction - optional - any component type restriction info for the columns
	* @param referencedComponentTypeSubRestriction - optional - any compont sub-type restrictions for the columns
	* @param time - The time to use for this modification
	*/
	public void configureConceptAsDynamicAssemblage(UUID concept, String dynamicUsageDescription, DynamicColumnInfo[] columns,
			IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction, long time)
	{
		int stampSequence = stampService.getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, pathNid);
		List<Chronology> items = Get.service(DynamicUtility.class).configureConceptAsDynamicSemantic(identifierService.getNidForUuids(concept), 
				dynamicUsageDescription, columns, referencedComponentTypeRestriction, referencedComponentTypeSubRestriction, stampSequence);
		
		for (Chronology c : items)
		{
			indexAndWrite(c);
		}
	}
	
	/**
	* Add all of the necessary metadata semantics onto the specified concept to make it an association.
	* 
	* This configures the concept as a dynamicSemantic, and then further adds association metadata.
	* 
	* @param concept - The concept that will define a dynamic semantic
	* @param dynamicUsageDescription - The description that describes the purpose of this dynamic semantic
	* @param inverseName - optional - the name to use when reading this association in reverse.  Do not create this name if it is identical to the forward name 
	* @param associationComponentTypeRestriction optional restriction on which types of components can be associated
	* @param associationComponentTypeSubRestriction  optional sub-restriction on which types of semantics can be associated.
	* @param time - The time to use for this modification
	*/
	public void configureConceptAsAssociation(UUID concept, String dynamicUsageDescription, String inverseName, IsaacObjectType associationComponentTypeRestriction,
			VersionType associationComponentTypeSubRestriction, long time)
	{
		//Make this a dynamic refex - with the association column info
		DynamicColumnInfo[] columns = new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid(), DynamicDataType.UUID, null, false, true)};
		configureConceptAsDynamicAssemblage(concept, dynamicUsageDescription, columns, associationComponentTypeRestriction, associationComponentTypeSubRestriction, time);

		//Add this concept to the association refset
		makeDynamicRefsetMember(DynamicConstants.get().DYNAMIC_ASSOCIATION.getPrimordialUuid(), concept, time);
		
		//Add inverse name, if provided
		if (StringUtils.isNotBlank(inverseName))
		{
			//If inverse name equals forward (regular name) this is going to blow up...
			UUID descriptionUUID = makeDescriptionEn(concept, inverseName, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.ACCEPTABLE____SOLOR.getPrimordialUuid());
			makeDynamicRefsetMember(DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME.getPrimordialUuid(), descriptionUUID, time);
		}
		associations.add(concept);
	}
	
	/**
	 * The current usage of the {@link MetaData#IDENTIFIER_ASSEMBALGE____SOLOR} refset is for {@link StringVersion} semantics 
	 * which happen to contain a value that is an identifier.
	 * 
	 * Assemblage concepts marked with this identifier assemblage get special treatment in search patterns.
	 * 
	 * You should only call this method if the passed in concept is being used as the assemblage concept for {@link StringVersion} semantics. 
	 * 
	 * @param concept The concept defining a {@link StringVersion} semantic assemblage, which is also carrying identifier data.
	 * @param time the time to use for making the modifications.
	 */
	public void configureConceptAsIdentifier(UUID concept, long time)
	{
		makeBrittleRefsetMember(MetaData.IDENTIFIER_ASSEMBALGE____SOLOR.getPrimordialUuid(), concept, time);
	}

	/**
	 * @return The UUID of the association types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)}, 
	 * if it has been created.
	 */
	public Optional<UUID> getAssociationTypes()
	{
		return associationTypes;
	}

	/**
	 * @return The UUID of the attribute types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)}, 
	 * if it has been created.
	 */
	public Optional<UUID> getAttributeTypes()
	{
		return attributeTypes;
	}

	/**
	 * @return The UUID of the description types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)}, 
	 * if it has been created.
	 */
	public Optional<UUID> getDescriptionTypes()
	{
		return descriptionTypes;
	}

	/**
	 * @return The UUID of the refset types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)}, 
	 * if it has been created.
	 */
	public Optional<UUID> getRefsetTypes()
	{
		return refsetTypes;
	}

	/**
	 * @return The UUID of the relation types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)}, 
	 * if it has been created.
	 */
	public Optional<UUID> getRelationTypes()
	{
		return relationTypes;
	}
	
	/**
	 * @return true, if the concept was configured {@link #configureConceptAsAssociation(UUID, String, String, IsaacObjectType, VersionType, long)} 
	 * as an association
	 * @param concept the concept to check
	 */
	public boolean isAssociation(UUID concept)
	{
		return associations.contains(concept);
	}
}
