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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevelException;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.MutableDynamicVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableStringVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.util.SemanticTags;
import sh.isaac.api.util.UuidFactory;
import sh.isaac.api.util.metainf.VersionFinder;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.converters.sharedUtils.stats.LoadStats;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.configuration.ManifoldCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.utility.Frills;

/**
 * A class to help structure external terminologies into the system in a consistent way, especially with respect to metadata.
 * 
 * Also, many helper method for common operations.
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
	private StampService stampService;
	private AssemblageService assemblageService;
	private SemanticBuilderService<?> semanticBuilderService;
	private TaxonomyService taxonomyService;
	private String terminologyName;

	private HashSet<Integer> deferredTaxonomyUpdates = new HashSet<Integer>();
	private LoadStats loadStats;
	
	private UUID metadataRoot;

	private Optional<UUID> associationTypesNode = Optional.empty();
	private Optional<UUID> attributeTypesNode = Optional.empty();
	private Optional<UUID> descriptionTypesNode = Optional.empty();
	private Optional<UUID> refsetTypesNode = Optional.empty();
	private Optional<UUID> relationTypesNode = Optional.empty();
	private HashMap<String, UUID> otherTypesNode = new HashMap<>();

	private HashSet<UUID> associations = new HashSet<>();
	private HashSet<UUID> refsets = new HashSet<>();

	//maintain a mapping of _all_ applied description labels to the UUID created for it.
	private HashMap<String, UUID> createdDescriptionTypes = new HashMap<>();
	private HashMap<String, UUID> createdAssociationTypes = new HashMap<>();
	private HashMap<String, UUID> createdRelationshipTypes = new HashMap<>();
	private HashMap<String, UUID> createdAttributeTypes = new HashMap<>();
	private HashMap<String, UUID> createdRefsetTypes = new HashMap<>();
	private HashMap<UUID, HashMap<String, UUID>> otherTypes = new HashMap<>();
	private HashSet<UUID> identifierTypes = new HashSet<>();
	
	private List<BooleanSupplier> delayedValidations = new ArrayList<>();
	private boolean delayValidations = false;

	/**
	 * If you create ANY logic graphs, ensure that you call {@link #processTaxonomyUpdates()} before allowing this helper to be
	 * garbage collected.
	 * 
	 * Note, this method also reconfigured the passed in converterUUID to use the namespace of the  passed in module, as a convenience, 
	 * as that is the typical use pattern.
	 * 
	 * @param author The default author to use for creates
	 * @param module The default module to use for creates
	 * @param path The default path to use for creates
	 * @param converterUUID The converterUUID debug tool to use to generate and store UUID mappings
	 * @param terminologyName - the name of this terminology, used for metadata and semantic types in metadata
	 * @param delayValidations - some loader patterns load content out of order, which requires delayed validations.
	 *        If you delay validations, you must call {@link #processDelayedValidations()} when the load is complete
	 */
	public DirectWriteHelper(int author, int module, int path, ConverterUUID converterUUID, String terminologyName, boolean delayValidations)
	{
		this.indexers = Get.services(IndexBuilderService.class);
		this.authorNid = author;
		this.moduleNid = module;
		this.pathNid = path;
		this.converterUUID = converterUUID;
		this.conceptService = Get.conceptService();
		this.identifierService = Get.identifierService();
		this.stampService = Get.stampService();
		this.assemblageService = Get.assemblageService();
		this.semanticBuilderService = Get.semanticBuilderService();
		this.taxonomyService = Get.taxonomyService();
		this.loadStats = new LoadStats();
		this.terminologyName = terminologyName;
		this.delayValidations = delayValidations;
		
		converterUUID.configureNamespace(Get.identifierService().getUuidPrimordialForNid(module));
		
	}

	/**
	 * Calls {@link #makeConcept(UUID, Status, long, UUID[])} with no additional IDs
	 * 
	 * @param concept The UUID of the concept to be created
	 * @param status The status of the concept to be created
	 * @param time The time to use for the concept to be created
	 * @return The UUId of the concept created for convenience (same as the passed in value)
	 */
	public UUID makeConcept(UUID concept, Status status, long time)
	{
		return makeConcept(concept, status, time, null);
	}

	/**
	 * Calls {@link #makeConcept(UUID, Status, long)}, followed by {@link #makeDescriptionEnNoDialect(UUID, String, UUID, Status, long)
	 * 
	 * @param concept - optional - the UUID to use for the created concept.  If not provided, created from the name
	 * @param name The text to use for the FQN and name
	 * @param descriptionType The type of the description - {@link TermAux#DEFINITION_DESCRIPTION_TYPE} or
	 *            {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
	 *            or {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, or a properly specified third-party description type
	 * @param parents - optional list of parent(s) to add
	 * @param status the status for the content
	 * @param time the time for the content
	 * @return the UUID of the created concept
	 */
	public UUID makeConceptEnNoDialect(UUID concept, String name, UUID descriptionType, UUID[] parents, Status status, long time)
	{
		concept = makeConcept(concept == null ? converterUUID.createNamespaceUUIDFromString(name) : concept, status, time);
		makeDescriptionEnNoDialect(concept, name, descriptionType, status, time);
		if (parents != null && parents.length > 0)
		{
			makeParentGraph(concept, Arrays.asList(parents), status, time);
		}
		return concept;
	}

	/**
	 * @param concept The UUID of the concept to be created
	 * @param status The status of the concept to be created
	 * @param time The time to use for the concept to be created
	 * @param additionalUUIDs additional UUIDs for the concept, if any.
	 * @return The UUId of the concept created for convenience (same as the passed in value)
	 */
	public UUID makeConcept(UUID concept, Status status, long time, UUID[] additionalUUIDs)
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
		return concept;
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
		return makeDynamicRefsetMember(assemblageConcept, memberUUID, null, time);
	}

	/**
	 * This creates a semantic type of {@link VersionType#DYNAMIC} with no data columns
	 * 
	 * This serves the same purpose as {@link #makeBrittleRefsetMember(UUID, UUID, long)} but allows for the refset to be described in the
	 * dynamic definition.
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param memberUUID The id being added to the refset
	 * @param status - the status to use, active if null.
	 * @param time The time to use for the entry
	 * @return The UUID of the object created
	 */
	public UUID makeDynamicRefsetMember(UUID assemblageConcept, UUID memberUUID, Status status, long time)
	{
		UUID uuidForCreatedMember = UuidFactory.getUuidForDynamic(converterUUID.getNamespace(), assemblageConcept, memberUUID, null,
				((input, uuid) -> converterUUID.addMapping(input, uuid)));
		SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(VersionType.DYNAMIC, uuidForCreatedMember,
				identifierService.getNidForUuids(assemblageConcept), identifierService.getNidForUuids(memberUUID));
		refsetMemberToWrite.createMutableVersion(stampService.getStampSequence(status == null ? Status.ACTIVE : status, time, authorNid, moduleNid, pathNid));
		indexAndWrite(refsetMemberToWrite);
		loadStats.addRefsetMember(getOriginStringForUuid(assemblageConcept));
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
		UUID uuidForCreatedMember = UuidFactory.getUuidForMemberSemantic(converterUUID.getNamespace(), assemblageConcept, memberUUID,
				((input, uuid) -> converterUUID.addMapping(input, uuid)));
		SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(VersionType.MEMBER, uuidForCreatedMember,
				identifierService.getNidForUuids(assemblageConcept), identifierService.getNidForUuids(memberUUID));
		refsetMemberToWrite.createMutableVersion(stampService.getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, pathNid));
		indexAndWrite(refsetMemberToWrite);
		loadStats.addRefsetMember(getOriginStringForUuid(assemblageConcept));
		return refsetMemberToWrite.getPrimordialUuid();
	}
	
	/**
	 * This creates a semantic type of {@link VersionType#DYNAMIC} with the specified data column.
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param referencedComponent the referenced component this dynamic semantic entry is being added to
	 * @param data optional - The data column for this dynamic semantic entry
	 * @param time The time to use for the entry
	 * @return The UUID of the object created
	 */
	public UUID makeDynamicSemantic(UUID assemblageConcept, UUID referencedComponent, DynamicData data, long time)
	{
		return makeDynamicSemantic(assemblageConcept, referencedComponent, data == null ? null : new DynamicData[] {data}, time, null);
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
		return makeDynamicSemantic(assemblageConcept, referencedComponent, data, time, null);
	}
	
	/**
	 * This creates a semantic type of {@link VersionType#DYNAMIC} with the specified data columns.
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param referencedComponent the referenced component this dynamic semantic entry is being added to
	 * @param data optional - The data columns for this dynamic semantic entry
	 * @param time The time to use for the entry
	 * @param uuidForCreatedSemantic - optional - if provided, used this UUID, instead of calculating one from the data.
	 * @return The UUID of the object created
	 */
	public UUID makeDynamicSemantic(UUID assemblageConcept, UUID referencedComponent, DynamicData[] data, long time, UUID uuidForCreatedSemantic)
	{
		return makeDynamicSemantic(assemblageConcept, referencedComponent, data, null, time, uuidForCreatedSemantic);
	}

	/**
	 * This creates a semantic type of {@link VersionType#DYNAMIC} with the specified data columns.
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param referencedComponent the referenced component this dynamic semantic entry is being added to
	 * @param data optional - The data columns for this dynamic semantic entry
	 * @param status optional - the status to use - active if not provided
	 * @param time The time to use for the entry
	 * @param uuidForCreatedSemantic - optional - if provided, used this UUID, instead of calculating one from the data.
	 * @return The UUID of the object created
	 */
	public UUID makeDynamicSemantic(UUID assemblageConcept, UUID referencedComponent, DynamicData[] data, Status status, long time, UUID uuidForCreatedSemantic)
	{
		UUID uuidForCreatedMember = uuidForCreatedSemantic == null ? UuidFactory.getUuidForDynamic(converterUUID.getNamespace(), assemblageConcept,
				referencedComponent, data, ((input, uuid) -> converterUUID.addMapping(input, uuid))) : uuidForCreatedSemantic;
		int referencedComponentNid = identifierService.getNidForUuids(referencedComponent);
		SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(VersionType.DYNAMIC, uuidForCreatedMember,
				identifierService.getNidForUuids(assemblageConcept), referencedComponentNid);
		MutableDynamicVersion<?> dv = refsetMemberToWrite
				.createMutableVersion(stampService.getStampSequence(status == null ? Status.ACTIVE : status, time, authorNid, moduleNid, pathNid));
		delayedValidations.addAll(dv.setData(data, delayValidations));
		indexAndWrite(refsetMemberToWrite);
		
		IsaacObjectType objectType = identifierService.getObjectTypeForComponent(referencedComponentNid);
		String objectTypeAnnotated;
		if (objectType == IsaacObjectType.SEMANTIC)
		{
			objectTypeAnnotated = assemblageService.getSemanticChronology(referencedComponentNid).getVersionType().name();
		}
		else
		{
			objectTypeAnnotated = objectType.name();
		}
		
		if (associations.contains(assemblageConcept))
		{
			loadStats.addAssociation(getOriginStringForUuid(assemblageConcept));
		}
		else if (refsets.contains(assemblageConcept))
		{
			loadStats.addRefsetMember(getOriginStringForUuid(assemblageConcept));
		}
		else
		{
			loadStats.addAnnotation(objectTypeAnnotated, getOriginStringForUuid(assemblageConcept));
		}
		return refsetMemberToWrite.getPrimordialUuid();
	}

	/**
	 * create a dynamic semantic entry in the pattern that matches an assemblage dynamic semantic.
	 * 
	 * @param assemblageConcept the type of association to create
	 * @param sourceConcept the source of the association
	 * @param targetConcept the optional target of the association
	 * @param status the status to use
	 * @param time the time to make the changes at
	 * @return the UUID of the object created
	 */
	public UUID makeAssociation(UUID assemblageConcept, UUID sourceConcept, UUID targetConcept, Status status, long time)
	{
		return makeDynamicSemantic(assemblageConcept, sourceConcept, targetConcept == null ? null : new DynamicData[] { new DynamicUUIDImpl(targetConcept) },
				status, time, null);
	}
	
	/**
	 * create a dynamic semantic entry in the pattern that matches an assemblage dynamic semantic.
	 * 
	 * @param assemblageConcept the type of association to create
	 * @param sourceConcept the source of the association
	 * @param targetConcept the optional target of the association
	 * @param time the time to make the changes at
	 * @return the UUID of the object created
	 */
	public UUID makeAssociation(UUID assemblageConcept, UUID sourceConcept, UUID targetConcept, long time)
	{
		return makeDynamicSemantic(assemblageConcept, sourceConcept, targetConcept == null ? null : new DynamicData[] { new DynamicUUIDImpl(targetConcept) },
				time);
	}

	/**
	 * Calls {@link #makeDescription(UUID, String, UUID, UUID, UUID, Status, long, UUID, UUID)} with
	 * {@link MetaData#ENGLISH_DIALECT_ASSEMBLAGE____SOLOR}
	 * and {@link MetaData#US_ENGLISH_DIALECT____SOLOR}
	 * 
	 * @param concept The concept to attach the description onto
	 * @param text The text of the description
	 * @param descriptionType The type of the description - {@link TermAux#DEFINITION_DESCRIPTION_TYPE} or
	 *            {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
	 *            or {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}
	 * @param caseSignificance {@link TermAux#DESCRIPTION_NOT_CASE_SENSITIVE} or {@link TermAux#DESCRIPTION_INITIAL_CHARACTER_SENSITIVE}
	 * @param status the status for the description
	 * @param time the time for the description
	 * @param dialectAcceptibility The dialect acceptability of the description. Ignored if dialect is null. required if dialect is not null.
	 *            either {@link TermAux#ACCEPTABLE} or {@link TermAux#PREFERRED}
	 * @return the UUID of the created description
	 */
	public UUID makeDescriptionEn(UUID concept, String text, UUID descriptionType, UUID caseSignificance, Status status, long time, UUID dialectAcceptibility)
	{
		return makeDescription(concept, text, descriptionType, MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(), caseSignificance, status, time,
				MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid(), dialectAcceptibility);
	}

	/**
	 * Calls {@link #makeDescription(UUID, String, UUID, UUID, UUID, Status, long, UUID, UUID)} with no dialect, no dialect acceptibility,
	 * and case sentivity of {@link MetaData#NOT_APPLICABLE____SOLOR}
	 * 
	 * @param concept The concept to attach the description onto
	 * @param text The text of the description
	 * @param descriptionType The type of the description - {@link TermAux#DEFINITION_DESCRIPTION_TYPE} or
	 *            {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
	 *            or {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, or a properly specified third-party description type
	 * @param status the status for the description
	 * @param time the time for the description
	 * @return the UUID of the created description
	 */
	public UUID makeDescriptionEnNoDialect(UUID concept, String text, UUID descriptionType, Status status, long time)
	{
		return makeDescription(concept, text, descriptionType, MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(),
				MetaData.NOT_APPLICABLE____SOLOR.getPrimordialUuid(), status, time, null, null);
	}

	/**
	 * Create a new dynamic semantic of type {@link DynamicConstants#DYNAMIC_COMMENT_ATTRIBUTE} in the corret format.
	 * 
	 * @param referencedComponent the item to place the comment on
	 * @param comment the comment
	 * @param commentContext the optional comment context
	 * @param time the time to make the edit it
	 * @return the created object
	 */
	public UUID makeComment(UUID referencedComponent, String comment, String commentContext, long time)
	{
		return makeDynamicSemantic(DynamicConstants.get().DYNAMIC_COMMENT_ATTRIBUTE.getPrimordialUuid(), referencedComponent,
				new DynamicData[] { new DynamicStringImpl(comment), (StringUtils.isBlank(commentContext) ? null : new DynamicStringImpl(commentContext)) },
				time);
	}

	
	/**
	 * @param concept The concept to attach the description onto
	 * @param text The text of the description
	 * @param descriptionType The type of the description - {@link TermAux#DEFINITION_DESCRIPTION_TYPE} or
	 *            {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
	 *            or {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}
	 * @param language The language concept of the description
	 * @param caseSignificance {@link TermAux#DESCRIPTION_NOT_CASE_SENSITIVE} or {@link TermAux#DESCRIPTION_INITIAL_CHARACTER_SENSITIVE} or
	 *            or {@link TermAux#DESCRIPTION_CASE_SENSITIVE}.  If not provided, is set to {@link MetaData#NOT_APPLICABLE____SOLOR}
	 * @param status the status for the description
	 * @param time the time for the description
	 * @param dialect The dialect of the description. Null to skip making a dialect.
	 * @param dialectAcceptibility The dialect acceptability of the description. Ignored if dialect is null. required if dialect is not null.
	 *            either {@link TermAux#ACCEPTABLE} or {@link TermAux#PREFERRED}
	 * @return the UUID of the created description
	 */
	public UUID makeDescription(UUID concept, String text, UUID descriptionType, UUID language, UUID caseSignificance, Status status, long time, UUID dialect,
			UUID dialectAcceptibility)
	{
		return makeDescription(null, concept, text, descriptionType, language, caseSignificance, status, time, dialect, dialectAcceptibility);
	}
	
	/**
	 * @param description - optional - the UUID to use for the created description
	 * @param concept The concept to attach the description onto
	 * @param text The text of the description
	 * @param descriptionType The type of the description - {@link TermAux#DEFINITION_DESCRIPTION_TYPE} or
	 *            {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
	 *            or {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}
	 * @param language The language concept of the description
	 * @param caseSignificance {@link TermAux#DESCRIPTION_NOT_CASE_SENSITIVE} or {@link TermAux#DESCRIPTION_INITIAL_CHARACTER_SENSITIVE} or
	 *            or {@link TermAux#DESCRIPTION_CASE_SENSITIVE}.
	 *            If not provided, is set to {@link MetaData#NOT_APPLICABLE____SOLOR}
	 * @param status the status for the description
	 * @param time the time for the description
	 * @param dialect The dialect of the description. Null to skip making a dialect.
	 * @param dialectAcceptibility The dialect acceptability of the description. Ignored if dialect is null. required if dialect is not null.
	 *            either {@link TermAux#ACCEPTABLE} or {@link TermAux#PREFERRED}
	 * @return the UUID of the created description
	 */
	public UUID makeDescription(UUID description, UUID concept, String text, UUID descriptionType, UUID language, UUID caseSignificance, Status status, long time, UUID dialect,
			UUID dialectAcceptibility)
	{
		if (StringUtils.isBlank(text))
		{
			throw new RuntimeException("Blank text!");
		}

		if (descriptionType == null)
		{
			throw new RunLevelException("Missing description type for description '" + text + "'");
		}

		UUID descriptionUuid = description == null ? UuidFactory.getUuidForDescriptionSemantic(converterUUID.getNamespace(), concept, caseSignificance, descriptionType, language,
				text, ((input, uuid) -> converterUUID.addMapping(input, uuid))) : description;

		SemanticChronologyImpl descriptionToWrite = new SemanticChronologyImpl(VersionType.DESCRIPTION, descriptionUuid,
				identifierService.getNidForUuids(language), identifierService.getNidForUuids(concept));
		int stamp = stampService.getStampSequence(status, time, authorNid, moduleNid, pathNid);
		DescriptionVersionImpl descriptionVersion = descriptionToWrite.createMutableVersion(stamp);
		descriptionVersion.setCaseSignificanceConceptNid(caseSignificance == null ? MetaData.NOT_APPLICABLE____SOLOR.getNid() 
				: identifierService.getNidForUuids(caseSignificance));
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
		loadStats.addDescription(getOriginStringForUuid(descriptionType));
		return descriptionUuid;
	}

	/**
	 * Annotate a description with the native description type it came from
	 * 
	 * @param description the ISAAC description semantic to be annotated
	 * @param nativeDescriptionType - the UUID of the concept that represents the native description type.
	 *            Must be a child of {@link TermAux#DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY}
	 * @param time The time to use for this creation
	 * @return the identifier of the created object
	 */
	public UUID makeExtendedDescriptionTypeAnnotation(UUID description, UUID nativeDescriptionType, long time)
	{
		return makeDynamicSemantic(DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getPrimordialUuid(), description,
				new DynamicData[] { new DynamicUUIDImpl(nativeDescriptionType) }, time);
	}

	/**
	 * Annotate a relationship with the native description type it came from
	 * 
	 * @param logicGraph the ISAAC logic graph that the relationship was put into
	 * @param nativeRelationshipType - the UUID of the concept that represents the native relationship type.
	 *            Must be a child of {@link TermAux#RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY}
	 * @param time The time to use for this creation
	 * @return the identifier of the created object
	 */
	public UUID makeExtendedRelationshipTypeAnnotation(UUID logicGraph, UUID nativeRelationshipType, long time)
	{
		return makeDynamicSemantic(DynamicConstants.get().DYNAMIC_EXTENDED_RELATIONSHIP_TYPE.getPrimordialUuid(), logicGraph,
				new DynamicData[] { new DynamicUUIDImpl(nativeRelationshipType) }, time);
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
		return makeParentGraph(concept, Arrays.asList(new UUID[] { parent }), status, time);
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
	public UUID makeParentGraph(UUID concept, Collection<UUID> parents, Status status, long time)
	{
		// Eliminate duplicates
		final Set<UUID> uuids = new HashSet<>();
		for (UUID parentUuid : parents)
		{
			uuids.add(parentUuid);
		}

		LogicalExpressionImpl lei = new LogicalExpressionImpl();
		ArrayList<ConceptNodeWithNids> parentConceptNodes = new ArrayList<>(uuids.size());

		for (UUID parentUuid : uuids)
		{
			parentConceptNodes.add(lei.Concept(identifierService.getNidForUuids(parentUuid)));
		}
		NecessarySetNode nsn = lei.NecessarySet(lei.And(parentConceptNodes.toArray(new ConceptNodeWithNids[parentConceptNodes.size()])));
		lei.getRoot().addChildren(nsn);
		return makeGraph(concept, null, lei, status, time);
	}
	
	/**
	 * Ensure that you call {@link #processTaxonomyUpdates()} at some point after using this method.
	 * 
	 * @param concept The concept we are placing the graph on
	 * @param graphSemanticId - optional - the UUID to use for this graph, or null, to have it calculated
	 * @param logicalExpression  - the (Stated) logical expression to add
	 * @param status The status of the graph
	 * @param time The time for the graph
	 * @return The identifier of the created graph
	 */
	public UUID makeGraph(UUID concept, UUID graphSemanticId, LogicalExpression logicalExpression, Status status, long time)
	{
		SemanticBuilder<?> sb = semanticBuilderService.getLogicalExpressionBuilder(logicalExpression, identifierService.getNidForUuids(concept),
				MetaData.EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR.getNid());
		sb.setStatus(status);
		if (graphSemanticId != null)
		{
			sb.setPrimordialUuid(graphSemanticId);
		}
		else
		{
			sb.setT5Uuid(converterUUID.getNamespace(), ((input, uuid) -> converterUUID.addMapping(input, uuid)));
		}
		//TODO add code to handle an existing graph?
		int graphStamp = stampService.getStampSequence(status, time, authorNid, moduleNid, pathNid);
		SemanticChronology sc = (SemanticChronology) sb.build(graphStamp, new ArrayList<>());
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
	 * Index and write the Chronology to the store. This only needs to be called if you have created chronologies outside of this class.
	 * 
	 * @param c
	 */
	public void indexAndWrite(Chronology c)
	{
		if (c instanceof ConceptChronology)
		{
			conceptService.writeConcept((ConceptChronology) c);
		}
		else if (c instanceof SemanticChronology)
		{
			assemblageService.writeSemanticChronology((SemanticChronology) c);
		}
		else
		{
			throw new RuntimeException("unsupported type " + c);
		}
		for (IndexBuilderService indexer : indexers)
		{
			indexer.indexNow(c);
		}
	}

	/**
	 * Properly update the taxonomy service for graph changes made. Ensure to call this at some point after calling
	 * {@link #makeParentGraph(UUID, List, Status, long)}
	 */
	public void processTaxonomyUpdates()
	{
		HashSet<Integer> temp = deferredTaxonomyUpdates;
		deferredTaxonomyUpdates = new HashSet<>();
		log.debug("Processing deferred taxonomy updates for {} graphs", temp.size());
		for (int nid : temp)
		{
			taxonomyService.updateTaxonomy(assemblageService.getSemanticChronology(nid));
		}
	}
	
	/**
	 * Properly execute any validations that were delayed during the load process
	 */
	public void processDelayedValidations()
	{
		List<BooleanSupplier> temp = delayedValidations;
		delayedValidations = new ArrayList<>();
		log.debug("Processing delayed validations for {} items", temp.size());
		for (BooleanSupplier bs : temp) {
			bs.getAsBoolean();
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
	 * Reset the load stats tracking
	 */
	public void clearLoadStats()
	{
		this.loadStats = new LoadStats();
	}

	/**
	 * Set up a hierarchy under {@link MetaData#SOLOR_CONTENT_METADATA____SOLOR} for various assemblage types for a specific terminology import.
	 * 
	 * @param makeAttributeTypes - create the {terminologyName} Attribute Types node. This is used for arbitrary property types.
	 * @param makeDescriptionTypes - create the {terminologyName} Description Types node. The is used for extended (native) description types.
	 * @param makeDescriptionTypesNative - if {makeDescriptionTypes} is set to true -then when false - configure this as an extended type,
	 *            by giving the description types node a parent of {@link MetaData#DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR}. When true, does
	 *            NOT make this description type node a child of {@link MetaData#DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR}, rather, just
	 *            placing it in the terminology metadata hierarchy. In this case, individual terminology types should be created with two parents - one being
	 *            {@link MetaData#DESCRIPTION_TYPE____SOLOR}, and the other being the concept created here. The description should also be annotated
	 *            with {@link MetaData#DYNAMIC_DESCRIPTION_CORE_TYPE}
	 * @param makeAssociationTypes - create the {terminologyName} Association Types node. This is used for associations/relationships that don't go
	 *            into the logic graph.
	 * @param makeRefsets - create the {terminologyName} Refsets node. This is for all member refset types
	 * @param makeRelationTypes - create the {terminologyName} Relation Types node. This is used for extended (native) relationship types that were
	 *            mapped to isA
	 *            and placed in the logic graph.
	 * @param time - the time to use for the creation
	 */
	public void makeMetadataHierarchy(boolean makeAttributeTypes, boolean makeDescriptionTypes, boolean makeDescriptionTypesNative,
			boolean makeAssociationTypes, boolean makeRefsets, boolean makeRelationTypes, long time)
	{
		String rootFsn = terminologyName + " Metadata (" + terminologyName + ")";
		metadataRoot = converterUUID.createNamespaceUUIDFromString(rootFsn, true);
		if (!conceptService.hasConcept(identifierService.assignNid(metadataRoot)))
		{
			log.info("Building metadata root '" + rootFsn + "'");
			makeConcept(metadataRoot, Status.ACTIVE, time);
			makeDescriptionEn(metadataRoot, rootFsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
			makeDescriptionEn(metadataRoot, SemanticTags.stripSemanticTagIfPresent(rootFsn),
					MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(),
					Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
			makeParentGraph(metadataRoot, MetaData.CONTENT_METADATA____SOLOR.getPrimordialUuid(), Status.ACTIVE, time);
		}

		if (makeAttributeTypes)
		{
			String fsn = terminologyName + " Attribute Types (" + terminologyName + ")";
			log.info("Building attribute types '" + fsn + "'");
			attributeTypesNode = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(attributeTypesNode.get())))
			{
				makeConcept(attributeTypesNode.get(), Status.ACTIVE, time);
				makeDescriptionEn(attributeTypesNode.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(attributeTypesNode.get(), SemanticTags.stripSemanticTagIfPresent(fsn),
						MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeParentGraph(attributeTypesNode.get(), metadataRoot, Status.ACTIVE, time);
			}
		}

		if (makeDescriptionTypes)
		{
			String fsn = terminologyName + " Description Types (" + terminologyName + ")";
			log.info("Building description types '" + fsn + "'");
			descriptionTypesNode = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(descriptionTypesNode.get())))
			{
				makeConcept(descriptionTypesNode.get(), Status.ACTIVE, time);
				makeDescriptionEn(descriptionTypesNode.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(descriptionTypesNode.get(), SemanticTags.stripSemanticTagIfPresent(fsn),
						MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				UUID[] parents = makeDescriptionTypesNative ? new UUID[] { metadataRoot} 
					: new UUID[] { metadataRoot, MetaData.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getPrimordialUuid()};
				makeParentGraph(descriptionTypesNode.get(), Arrays.asList(parents), Status.ACTIVE, time);
			}
		}

		if (makeAssociationTypes)
		{
			String fsn = terminologyName + " Association Types (" + terminologyName + ")";
			log.info("Building association types '" + fsn + "'");
			associationTypesNode = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(associationTypesNode.get())))
			{
				makeConcept(associationTypesNode.get(), Status.ACTIVE, time);
				makeDescriptionEn(associationTypesNode.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(associationTypesNode.get(), SemanticTags.stripSemanticTagIfPresent(fsn),
						MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeParentGraph(associationTypesNode.get(), metadataRoot, Status.ACTIVE, time);
			}
		}

		if (makeRelationTypes)
		{
			String fsn = terminologyName + " Relation Types (" + terminologyName+ ")";
			log.info("Building relation types '" + fsn + "'");
			relationTypesNode = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(relationTypesNode.get())))
			{
				makeConcept(relationTypesNode.get(), Status.ACTIVE, time);
				makeDescriptionEn(relationTypesNode.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(relationTypesNode.get(), SemanticTags.stripSemanticTagIfPresent(fsn),
						MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeParentGraph(relationTypesNode.get(),
						Arrays.asList(new UUID[] { metadataRoot, MetaData.RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getPrimordialUuid() }),
						Status.ACTIVE, time);
			}
		}

		if (makeRefsets)
		{
			String fsn = terminologyName + " Refsets (" + terminologyName + ")";
			log.info("Building refsets '" + fsn + "'");
			refsetTypesNode = Optional.of(converterUUID.createNamespaceUUIDFromString(fsn, true));
			if (!conceptService.hasConcept(identifierService.assignNid(refsetTypesNode.get())))
			{
				makeConcept(refsetTypesNode.get(), Status.ACTIVE, time);
				makeDescriptionEn(refsetTypesNode.get(), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeDescriptionEn(refsetTypesNode.get(), SemanticTags.stripSemanticTagIfPresent(fsn),
						MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
						MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
						MetaData.PREFERRED____SOLOR.getPrimordialUuid());
				makeParentGraph(refsetTypesNode.get(), Arrays.asList(new UUID[] { metadataRoot}),
						Status.ACTIVE, time);
			}
		}
	}
	
	/**
	 * @return The concept that was created as the root of all metadata for this terminology
	 */
	public UUID getMetadataRoot()
	{
		return metadataRoot;
	}
	
	/**
	 * @return The module we are currently configured to use
	 */
	public int getModuleNid()
	{
		return moduleNid;
	}

	/**
	 * Add all of the necessary metadata semantics onto the specified concept to make it a concept that defines a dynamic semantic assemblage
	 * See {@link DynamicUsageDescription} class for more details on this format.
	 * implemented by See
	 * {@link DynamicUtility#configureConceptAsDynamicSemantic(int, String, DynamicColumnInfo[], IsaacObjectType, VersionType, int)}
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
	 * @param inverseName - optional - the name to use when reading this association in reverse. Do not create this name if it is identical to the
	 *            forward name
	 * @param associationComponentTypeRestriction optional restriction on which types of components can be associated
	 * @param associationComponentTypeSubRestriction optional sub-restriction on which types of semantics can be associated.
	 * @param time - The time to use for this modification
	 */
	public void configureConceptAsAssociation(UUID concept, String dynamicUsageDescription, String inverseName,
			IsaacObjectType associationComponentTypeRestriction, VersionType associationComponentTypeSubRestriction, long time)
	{
		//Make this a dynamic refex - with the association column info
		DynamicColumnInfo[] columns = new DynamicColumnInfo[] { new DynamicColumnInfo(0,
				DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid(), DynamicDataType.UUID, null, false, true) };
		configureConceptAsDynamicAssemblage(concept, dynamicUsageDescription, columns, associationComponentTypeRestriction,
				associationComponentTypeSubRestriction, time);

		//Add this concept to the association refset
		makeDynamicRefsetMember(DynamicConstants.get().DYNAMIC_ASSOCIATION.getPrimordialUuid(), concept, time);

		//Add inverse name, if provided
		if (StringUtils.isNotBlank(inverseName))
		{
			//make UUID manually for inverse, to it doesn't blow up when the inverse name is the same as the forward name
			UUID descriptionUUID = UuidFactory.getUuidForDescriptionSemantic(converterUUID.getNamespace(), concept, 
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(),
					MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(),
					"inverse:" + inverseName, ((input, uuid) -> converterUUID.addMapping(input, uuid)));
					
			makeDescription(descriptionUUID, concept, inverseName, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(),
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
					MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid(),
					MetaData.ACCEPTABLE____SOLOR.getPrimordialUuid());
			makeDynamicRefsetMember(DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME.getPrimordialUuid(), descriptionUUID, time);
		}
		associations.add(concept);
	}

	/**
	 * The current usage of the {@link MetaData#IDENTIFIER_SOURCE____SOLOR} refset is for {@link StringVersion} semantics
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
		identifierTypes.add(makeBrittleRefsetMember(MetaData.IDENTIFIER_SOURCE____SOLOR.getPrimordialUuid(), concept, time));
	}
	
	/**
	 * returns true if {@link #configureConceptAsIdentifier(UUID, long)} was called for this property type
	 * @param concept
	 * @return
	 */
	public boolean isConfiguredAsIdentifier(UUID concept)
	{
		return identifierTypes.contains(concept);
	}

	/**
	 * Creates a new concept to represent a core description type or an extended description type.
	 * Creates a FQN and regular name, adds parents of
	 * {@link #getDescriptionTypesNode()} and when a core description type adds a second parent of - {@link MetaData#DESCRIPTION_TYPE____SOLOR}
	 * and annotates the description type with the {@link DynamicConstants#DYNAMIC_DESCRIPTION_CORE_TYPE} annotation if coreDescriptionType is provided.
	 * 
	 * @param uuid optional - the UUID to use for the concept.  Created from the FQN, if not provided
	 * @param name The name to use for the FQN and Regular Name
	 * @param preferredName - optional - if provided, this is the preferred regular name, and the {name} value will be an acceptable regular name.
	 * If not provided, the {name} will also be used as the preferred regular name.
	 * @param altName - optional - additional regular name to add
	 * @param coreDescriptionType - optional - the core type this new type should be treated as for presentation. Should be one of
	 *            {@link MetaData#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR}, {@link MetaData#REGULAR_NAME_DESCRIPTION_TYPE____SOLOR},
	 *            or {@link MetaData#DEFINITION_DESCRIPTION_TYPE____SOLOR}.
	 *            If not provided, then this will be created as an extended description type, where it isn't a child of {@link MetaData#DESCRIPTION_TYPE____SOLOR}
	 * @param additionalParents - optional - other concepts to link as parent concepts
	 * @param time - the commit time
	 * @return the UUID of the created concept.
	 */
	public UUID makeDescriptionTypeConcept(UUID uuid, String name, String preferredName, String altName, UUID coreDescriptionType, List<UUID> additionalParents, long time)
	{
		UUID concept = makeTypeConcept(uuid, name, preferredName, altName, (fName, fConcept) -> createdDescriptionTypes.put(fName, fConcept), time);

		ArrayList<UUID> parents = new ArrayList<>();
		parents.add(getDescriptionTypesNode().get());
		if (coreDescriptionType != null)
		{
			parents.add(MetaData.DESCRIPTION_TYPE____SOLOR.getPrimordialUuid());
		}
		if (additionalParents != null)
		{
			parents.addAll(additionalParents);
		}

		makeParentGraph(concept, parents, Status.ACTIVE, time);
		if (coreDescriptionType != null)
		{
			makeDynamicSemantic(DynamicConstants.get().DYNAMIC_DESCRIPTION_CORE_TYPE.getPrimordialUuid(), concept,
				new DynamicData[] { new DynamicUUIDImpl(coreDescriptionType) }, time);
		}
		return concept;
	}
	
	/**
	 * Creates a new concept to represent an association type, and the calls 
	 * {@link #configureConceptAsAssociation(UUID, String, String, IsaacObjectType, VersionType, long)} to set the 
	 * concept up as an association.  
	 * Adds a parent of {@link #getAssociationTypesNode()}
	 * @param uuid optional - the UUID to use for the concept.  Created from the FQN, if not provided
	 * @param name The name to use for the FQN and Regular Name
	 * @param preferredName - optional - if provided, this is the preferred regular name, and the {name} value will be an acceptable regular name.
	 * If not provided, the {name} will also be used as the preferred regular name.
	 * @param altName - optional - additional regular name to add
	 * @param associationUsageDescription - if provided, used as a description on this association type
	 * @param inverseName - optional - if provided, used as the inverse name on the association
	 * @param associationComponentTypeRestriction - optional restriction for the association
	 * @param associationComponentTypeSubRestriction - optional restriction for the association
	 * @param additionalParents - optional additional parents
	 * @param time - the commit time
	 * @return the UUID of the created concept.
	 */
	public UUID makeAssociationTypeConcept(UUID uuid, String name, String preferredName, String altName, String associationUsageDescription, String inverseName, 
			IsaacObjectType associationComponentTypeRestriction, VersionType associationComponentTypeSubRestriction, List<UUID> additionalParents, long time)
	{
		ArrayList<UUID> parents = new ArrayList<>();
		parents.add(getAssociationTypesNode().get());
		AtomicBoolean markAsRel = new AtomicBoolean(false);
		if (additionalParents != null)
		{
			for (UUID parent : additionalParents)
			{
				parents.add(parent);
				if (getRelationTypesNode().isPresent() && parent.equals(getRelationTypesNode().get()))
				{
					markAsRel.set(true);
				}
			}
			parents.addAll(additionalParents);
		}
		
		UUID concept = makeTypeConcept(uuid, name, preferredName, altName, (fName, fConcept) -> 
		{
			if (markAsRel.get())
			{
				createdRelationshipTypes.put(fName, fConcept);
			}
			return createdAssociationTypes.put(fName, fConcept);
		}, time);
		
		configureConceptAsAssociation(concept, StringUtils.isBlank(associationUsageDescription) ? (StringUtils.isBlank(altName) ? name : altName) 
				: associationUsageDescription, 
				inverseName, associationComponentTypeRestriction, associationComponentTypeSubRestriction, time);
		makeParentGraph(concept, parents, Status.ACTIVE, time);
		return concept;
	}

	/**
	 * Creates a new concept to represent an attribute type, which can store one value of data. Adds parents of
	 * {@link #getAttributeTypesNode()} and {@link MetaData#IDENTIFIER_SOURCE____SOLOR} (if {isIdentifier} is true)
	 * 
	 * @param uuid optional - the UUID to use for the concept.  Created from the FQN, if not provided
	 * @param name The name to use for the FQN and Regular Name
	 * @param preferredName - optional - if provided, this is the preferred regular name, and the {name} value will be an acceptable regular name.
	 * If not provided, the {name} will also be used as the preferred regular name.
	 * @param altName - optional - additional regular name to add
	 * @param isIdentifier - if true, mark this attribute as an identifier, (which uses brittle refsets) otherwise,
	 *            configures it as a dynamic refset that holds one value, of the specified type.
	 * @param dataType - optional the data type to store in the annotation - ignored if {isIdentifier} is true.  If 
	 *            isIdentifier is false, and this is null, it is the callers responsibility to call 
	 *            {@link #configureConceptAsDynamicAssemblage(UUID, String, DynamicColumnInfo[], IsaacObjectType, VersionType, long)}
	 *            to finish configuring this node.
	 * @param additionalParents - optional - if this concept should have more parents, supply them here
	 * @param time - the commit time
	 * @return the UUID of the created concept.
	 */
	public UUID makeAttributeTypeConcept(UUID uuid, String name, String preferredName, String altName, boolean isIdentifier, DynamicDataType dataType, 
			List<UUID> additionalParents, long time)
	{
		return makeAttributeTypeConcept(uuid, name, preferredName, altName, null, isIdentifier, dataType, additionalParents, time);
	}

	/**
	 * Creates a new concept to represent an attribute type, which can store one value of data. Adds parents of
	 * {@link #getAttributeTypesNode()} and {@link MetaData#IDENTIFIER_SOURCE____SOLOR} (if {isIdentifier} is true)
	 * 
	 * @param uuid optional - the UUID to use for the concept.  Created from the FQN, if not provided
	 * @param name The name to use for the FQN and Regular Name
	 * @param preferredName - optional - if provided, this is the preferred regular name, and the {name} value will be an acceptable regular name.
	 * If not provided, the {name} will also be used as the preferred regular name.
	 * @param altName - optional - additional regular name to add
	 * @param description - optional - used as the dynamic assemblage configuration description if provided, otherwise, the altName or name is used.
	 * @param isIdentifier - if true, mark this attribute as an identifier, (which uses brittle refsets) otherwise,
	 *            configures it as a dynamic refset that holds one value, of the specified type.
	 * @param dataType - optional the data type to store in the annotation - ignored if {isIdentifier} is true.  If 
	 *            isIdentifier is false, and this is null, it is the callers responsibility to call 
	 *            {@link #configureConceptAsDynamicAssemblage(UUID, String, DynamicColumnInfo[], IsaacObjectType, VersionType, long)}
	 *            to finish configuring this node.
	 * @param additionalParents - optional - if this concept should have more parents, supply them here
	 * @param time - the commit time
	 * @return the UUID of the created concept.
	 */
	public UUID makeAttributeTypeConcept(UUID uuid, String name, String preferredName, String altName, String description, boolean isIdentifier, 
			DynamicDataType dataType, List<UUID> additionalParents, long time)
	{
		UUID concept = makeTypeConcept(uuid, name, preferredName, altName, (fName, fConcept) -> createdAttributeTypes.put(fName, fConcept), time);
		ArrayList<UUID> parents = new ArrayList<>();
		parents.add(getAttributeTypesNode().get());
		if (isIdentifier)
		{
			parents.add(MetaData.IDENTIFIER_SOURCE____SOLOR.getPrimordialUuid());
			makeBrittleRefsetMember(MetaData.IDENTIFIER_SOURCE____SOLOR.getPrimordialUuid(), concept, time);
		}
		else if (dataType != null)
		{
			configureConceptAsDynamicAssemblage(concept, StringUtils.isBlank(description) ? (StringUtils.isBlank(altName) ? name : altName) : description,
					new DynamicColumnInfo[] { new DynamicColumnInfo(0, concept, dataType, null, true, true) }, null, null, time);
		}
		
		if (additionalParents != null)
		{
			parents.addAll(additionalParents);
		}

		makeParentGraph(concept, parents, Status.ACTIVE, time);
		return concept;
	}
	
	/**
	 * Creates a new concept to represent an attribute type, which can store one value of data. Adds parents of
	 * {@link #getAttributeTypesNode()} and {@link MetaData#IDENTIFIER_SOURCE____SOLOR} (if {isIdentifier} is true)
	 * 
	 * @param uuid optional - the UUID to use for the concept.  Created from the FQN, if not provided
	 * @param name The name to use for the FQN and Regular Name
	 * @param preferredName - optional - if provided, this is the preferred regular name, and the {name} value will be an acceptable regular name.
	 * If not provided, the {name} will also be used as the preferred regular name.
	 * @param altName - optional - additional regular name to add
	 * @param description - optional - used as the dynamic assemblage configuration description if provided, otherwise, the altName or name is used.
	 * @param dataTypeColumns - optional the column descriptor for each column to to store in the annotation. This gets passed along to
	 *     {@link #configureConceptAsDynamicAssemblage(UUID, String, DynamicColumnInfo[], IsaacObjectType, VersionType, long)}
	 * @param referencedComponentTypeRestriction - optional - 
	 *     see {@link #configureConceptAsDynamicAssemblage(UUID, String, DynamicColumnInfo[], IsaacObjectType, VersionType, long)}
	 * @param additionalParents - optional - if this concept should have more parents, supply them here
	 * @param time - the commit time
	 * @return the UUID of the created concept.
	 */
	public UUID makeAttributeTypeConcept(UUID uuid, String name, String preferredName, String altName, String description, 
			DynamicColumnInfo[] dataTypeColumns, IsaacObjectType referencedComponentTypeRestriction, List<UUID> additionalParents, long time)
	{
		UUID concept = makeTypeConcept(uuid, name, preferredName, altName, (fName, fConcept) -> createdAttributeTypes.put(fName, fConcept), time);
		ArrayList<UUID> parents = new ArrayList<>();
		parents.add(getAttributeTypesNode().get());
		if (dataTypeColumns != null)
		{
			configureConceptAsDynamicAssemblage(concept, StringUtils.isBlank(description) ? (StringUtils.isBlank(altName) ? name : altName) : description,
					dataTypeColumns, referencedComponentTypeRestriction, null, time);
		}
		
		if (additionalParents != null)
		{
			parents.addAll(additionalParents);
		}

		makeParentGraph(concept, parents, Status.ACTIVE, time);
		return concept;
	}

	/**
	 * Update the logic graph of an already existing concept, to add another parent to it, and add another FQN to it, that includes a semantic
	 * tag for the module re-using the constant.
	 * 
	 * @param existingConstant the existing concept
	 * @param time the commit time - will be overridden, if we are merging with something newer, such that the resulting time is newer than the 
	 *        newest time in the merge source
	 * @param stampCoordinate the stamp coordinate to use when reading the current graph
	 * @return the UUID of the updated graph
	 */
	public UUID linkToExistingAttributeTypeConcept(ConceptSpecification existingConstant, long time, StampCoordinate stampCoordinate)
	{
		List<LatestVersion<LogicGraphVersionImpl>> lgs = assemblageService.getSnapshot(LogicGraphVersionImpl.class, stampCoordinate)
				.getLatestSemanticVersionsForComponentFromAssemblage(existingConstant.getNid(), MetaData.EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR.getNid());

		if (lgs.size() == 0)
		{
			throw new RuntimeException("No existing parent?");
		}
		else if (lgs.size() > 1)
		{
			throw new RuntimeException("Unexpected number of stated logic graphs");
		}

		Set<Integer> existingParents = Frills.getParentConceptNidsFromLogicGraph(lgs.get(0).get());

		LogicalExpressionImpl lei = new LogicalExpressionImpl();
		ArrayList<ConceptNodeWithNids> parentConceptNodes = new ArrayList<>(existingParents.size() + 1);

		for (Integer parent : existingParents)
		{
			parentConceptNodes.add(lei.Concept(parent));
		}
		parentConceptNodes.add(lei.Concept(identifierService.getNidForUuids(getAttributeTypesNode().get())));
		NecessarySetNode nsn = lei.NecessarySet(lei.And(parentConceptNodes.toArray(new ConceptNodeWithNids[parentConceptNodes.size()])));
		lei.getRoot().addChildren(nsn);
		
		long timeToUse = time;
		//Make sure our merge is newer than the existing logic graph.  If we load old content, which comes in with an old commit time, but
		//merge onto a concept from metadata that is newer, the merge gets lost in the history.
		if (timeToUse < lgs.get(0).get().getTime())
		{
			timeToUse = lgs.get(0).get().getTime() + 1;  
		}

		int stamp = stampService.getStampSequence(Status.ACTIVE, timeToUse, authorNid, moduleNid, pathNid);
		MutableLogicGraphVersion mlgv = lgs.get(0).get().getChronology().createMutableVersion(stamp);
		mlgv.setGraphData(lei.getData(DataTarget.INTERNAL));
		indexAndWrite(mlgv.getChronology());
		deferredTaxonomyUpdates.add(mlgv.getNid());
		loadStats.addGraph();

		String currentName = SemanticTags.stripSemanticTagIfPresent(existingConstant.getFullyQualifiedName());

		makeDescriptionEn(existingConstant.getPrimordialUuid(), currentName + " (" + terminologyName + ")",
				MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
				MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.ACCEPTABLE____SOLOR.getPrimordialUuid());

		createdAttributeTypes.put(currentName, existingConstant.getPrimordialUuid());

		return mlgv.getPrimordialUuid();
	}

	/**
	 * Creates a new concept to represent a dynamic refset type, (essentially the same as an attribute type, except it stores no data).
	 * Adds a parent of {@link #getRefsetTypesNode()}
	 * @param uuid optional - the UUID to use for the concept.  Created from the FQN, if not provided
	 * @param name The name to use for the FQN and Regular Name
	 * @param preferredName - optional - if provided, this is the preferred regular name, and the {name} value will be an acceptable regular name.
	 * If not provided, the {name} will also be used as the preferred regular name.
	 * @param altName - optional - additional regular name to add
	 * @param time - the commit time
	 * @return the UUID of the created concept.
	 */
	public UUID makeRefsetTypeConcept(UUID uuid, String name, String preferredName, String altName, long time)
	{
		UUID concept = makeTypeConcept(uuid, name, preferredName, altName, (fName, fConcept) -> createdRefsetTypes.put(fName, fConcept), time);
		ArrayList<UUID> parents = new ArrayList<>();
		parents.add(getRefsetTypesNode().get());
		configureConceptAsDynamicAssemblage(concept, StringUtils.isBlank(altName) ? name : altName, new DynamicColumnInfo[] {}, null, null, time);

		makeParentGraph(concept, parents, Status.ACTIVE, time);
		refsets.add(concept);
		return concept;
	}

	/**
	 * Internal helper method for making the various type concepts
	 * 
	 * @param uuid - optional - the UUID to use for the concept.  Created from the FQN, if not provided
	 * @param name - used in construction of the FQN, and a regular name.
	 * @param preferredName - optional - if provided, this is the preferred regular name, and the {name} value will be an acceptable regular name.
	 * If not provided, the {name} will also be used as the preferred regular name.
	 * @param altName - optional - an additional acceptable regular name to add.
	 * @param typeListUpdate - which list of created items should be updated.
	 * @param time
	 * @return
	 */
	private UUID makeTypeConcept(UUID uuid, String name, String preferredName, String altName, BiFunction<String, UUID, UUID> typeListUpdate, long time)
	{
		String fqn = name + " (" + terminologyName + ")";
		UUID concept = uuid == null ?  converterUUID.createNamespaceUUIDFromString(fqn) : uuid;
		makeConcept(concept, Status.ACTIVE, time);

		makeDescriptionEn(concept, fqn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
				MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, MetaData.PREFERRED____SOLOR.getPrimordialUuid());
		makeDescriptionEn(concept, name, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
				MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, StringUtils.isBlank(preferredName) ?
						MetaData.PREFERRED____SOLOR.getPrimordialUuid() : MetaData.ACCEPTABLE____SOLOR.getPrimordialUuid());

		if (typeListUpdate.apply(name, concept) != null)
		{
			throw new RuntimeException("The type '" + name + "' is already mapped.  Type names must be unique!");
		}
		
		if (StringUtils.isNotBlank(preferredName) && !name.equals(preferredName))
		{
			makeDescriptionEn(concept, preferredName, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
					MetaData.PREFERRED____SOLOR.getPrimordialUuid());
			if (typeListUpdate.apply(preferredName, concept) != null)
			{
				throw new RuntimeException("The type '" + preferredName + "' is already mapped.  Type names must be unique!");
			}
		}
		
		if (StringUtils.isNotBlank(altName) && !name.equals(altName))
		{
			makeDescriptionEn(concept, altName, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
					MetaData.ACCEPTABLE____SOLOR.getPrimordialUuid());
			if (typeListUpdate.apply(altName, concept) != null)
			{
				throw new RuntimeException("The type '" + altName + "' is already mapped.  Type names must be unique!");
			}
		}
		return concept;
	}
	
	/**
	 * This creates a semantic type of {@link VersionType#DYNAMIC} with a single String column
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param referencedComponent the referenced component this dynamic semantic entry is being added to
	 * @param value The string value to attach
	 * @param time The time to use for the entry
	 * @return The UUID of the object created
	 */
	public UUID makeStringAnnotation(UUID assemblageConcept, UUID referencedComponent, String value, long time)
	{
		return makeDynamicSemantic(assemblageConcept, referencedComponent, new DynamicData[] {new DynamicStringImpl(value)}, time);
	}
	
	/**
	 * This creates a semantic type of {@link VersionType#DYNAMIC} with a single String column
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param referencedComponent the referenced component this dynamic semantic entry is being added to
	 * @param value The string value to attach
	 * @param status The status to use
	 * @param time The time to use for the entry
	 * @return The UUID of the object created
	 */
	public UUID makeStringAnnotation(UUID assemblageConcept, UUID referencedComponent, String value, Status status, long time)
	{
		return makeDynamicSemantic(assemblageConcept, referencedComponent, new DynamicData[] {new DynamicStringImpl(value)}, status, time, null);
	}

	/**
	 * This creates a semantic type of {@link VersionType#STRING}
	 * 
	 * @param assemblageConcept The type of refset member to create
	 * @param referencedComponent the referenced component this dynamic semantic entry is being added to
	 * @param annotation the string value to attach
	 * @param time the time to use for the entry
	 * @return
	 */
	public UUID makeBrittleStringAnnotation(UUID assemblageConcept, UUID referencedComponent, String annotation, long time)
	{
		UUID uuidForCreatedMember = UuidFactory.getUuidForStringSemantic(converterUUID.getNamespace(), assemblageConcept, referencedComponent, annotation,
				((input, uuid) -> converterUUID.addMapping(input, uuid)));
		final int referencedComponentNid = identifierService.getNidForUuids(referencedComponent);
		SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(VersionType.STRING, uuidForCreatedMember,
				identifierService.getNidForUuids(assemblageConcept), referencedComponentNid);
		MutableStringVersion version = refsetMemberToWrite
				.createMutableVersion(stampService.getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, pathNid));
		version.setString(annotation);
		indexAndWrite(refsetMemberToWrite);
		IsaacObjectType objectType = identifierService.getObjectTypeForComponent(referencedComponentNid);
		String objectTypeAnnotated;
		if (objectType == IsaacObjectType.SEMANTIC)
		{
			objectTypeAnnotated = assemblageService.getSemanticChronology(referencedComponentNid).getVersionType().name();
		}
		else
		{
			objectTypeAnnotated = objectType.name();
		}
		loadStats.addAnnotation(objectTypeAnnotated, getOriginStringForUuid(assemblageConcept));
		return refsetMemberToWrite.getPrimordialUuid();
	}

	/**
	 * Create the standard terminology metadata entries, that detail what was loaded.
	 * @param terminologyModuleVersionConcept
	 * @param converterSourceArtifactVersion
	 * @param converterSourceReleaseDate
	 * @param converterOutputArtifactVersion
	 * @param converterOutputArtifactClassifier
	 * @param time
	 */
	public void makeTerminologyMetadataAnnotations(UUID terminologyModuleVersionConcept, String converterSourceArtifactVersion,
			Optional<String> converterSourceReleaseDate, Optional<String> converterOutputArtifactVersion, Optional<String> converterOutputArtifactClassifier,
			long time)
	{
		makeBrittleStringAnnotation(MetaData.SOURCE_ARTIFACT_VERSION____SOLOR.getPrimordialUuid(), terminologyModuleVersionConcept,
				converterSourceArtifactVersion, time);
		if (converterOutputArtifactVersion.isPresent() && StringUtils.isNotBlank(converterOutputArtifactVersion.get()))
		{
			makeBrittleStringAnnotation(MetaData.CONVERTED_IBDF_ARTIFACT_VERSION____SOLOR.getPrimordialUuid(), terminologyModuleVersionConcept,
					converterOutputArtifactVersion.get(), time);
		}
		makeBrittleStringAnnotation(MetaData.CONVERTER_VERSION____SOLOR.getPrimordialUuid(), terminologyModuleVersionConcept,
				VersionFinder.findProjectVersion(true), time);

		if (converterOutputArtifactClassifier.isPresent() && StringUtils.isNotBlank(converterOutputArtifactClassifier.get()))
		{
			makeBrittleStringAnnotation(MetaData.CONVERTED_IBDF_ARTIFACT_CLASSIFIER____SOLOR.getPrimordialUuid(), terminologyModuleVersionConcept,
					converterOutputArtifactClassifier.get(), time);
		}
		if (converterSourceReleaseDate.isPresent() && StringUtils.isNotBlank(converterSourceReleaseDate.get()))
		{
			makeBrittleStringAnnotation(MetaData.SOURCE_RELEASE_DATE____SOLOR.getPrimordialUuid(), terminologyModuleVersionConcept,
					converterSourceReleaseDate.get(), time);
		}
	}

	/**
	 * @return The UUID of the association types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
	 *         if it has been created.
	 */
	public Optional<UUID> getAssociationTypesNode()
	{
		return associationTypesNode;
	}

	/**
	 * @return The UUID of the attribute types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
	 *         if it has been created.
	 */
	public Optional<UUID> getAttributeTypesNode()
	{
		return attributeTypesNode;
	}

	/**
	 * @return The UUID of the description types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
	 *         if it has been created.
	 */
	public Optional<UUID> getDescriptionTypesNode()
	{
		return descriptionTypesNode;
	}

	/**
	 * @return The UUID of the refset types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
	 *         if it has been created.
	 */
	public Optional<UUID> getRefsetTypesNode()
	{
		return refsetTypesNode;
	}

	/**
	 * @return The UUID of the relation types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
	 *         if it has been created.
	 */
	public Optional<UUID> getRelationTypesNode()
	{
		return relationTypesNode;
	}

	/**
	 * @return true, if the concept was configured {@link #configureConceptAsAssociation(UUID, String, String, IsaacObjectType, VersionType, long)}
	 *         as an association
	 * @param concept the concept to check
	 */
	public boolean isAssociation(UUID concept)
	{
		return associations.contains(concept);
	}

	/**
	 * Return the UUID of the concept that matches the description created by {@link #makeDescriptionTypeConcept(String, String, String, UUID, long)}
	 * 
	 * @param descriptionName the name or altName of the description
	 * @return the UUID of the concept that represents it
	 */
	public UUID getDescriptionType(String descriptionName)
	{
		return createdDescriptionTypes.get(descriptionName);
	}
	
	/**
	 * @return all description names fed into {@link #makeDescriptionTypeConcept(String, String, String, UUID, long)}
	 */
	public Set<String> getDescriptionTypes()
	{
		return createdDescriptionTypes.keySet();
	}
	
	/**
	 * Return the UUID of the concept that matches the description created by 
	 * {@link #makeAssociationTypeConcept(UUID, String, String, String, String, String, IsaacObjectType, VersionType, List, long)
	 * 
	 * @param associationName the name or altName of the association
	 * @return the UUID of the concept that represents it
	 */
	public UUID getAssociationType(String associationName)
	{
		return createdAssociationTypes.get(associationName);
	}
	
	/**
	 * Return the UUID of the concept that matches the description created by 
	 * {@link #makeAssociationTypeConcept(UUID, String, String, String, String, String, IsaacObjectType, VersionType, List, long)
	 * 
	 * where the List parameter for the additional parents contained the concept {@link #getRelationTypesNode()}
	 * @param relationshipName the name relationship
	 * @return the UUID of the concept that represents it
	 */
	public UUID getRelationshipType(String relationshipName)
	{
		return createdRelationshipTypes.get(relationshipName);
	}
	
	/**
	 * Return the UUID of the concept that matches the description created by {@link #makeAttributeTypeConcept(String, String, boolean, DynamicDataType, List, long)}
	 * 
	 * @param attributeName the name or altName of the attribute
	 * @return the UUID of the concept that represents it
	 */
	public UUID getAttributeType(String attributeName)
	{
		return createdAttributeTypes.get(attributeName);
	}
	
	/**
	 * @return all attribute names fed into {@link #makeAttributeTypeConcept(UUID, String, String, String, boolean, DynamicDataType, List, long)}
	 * or ({@link #makeAttributeTypeConcept(UUID, String, String, String, String, boolean, DynamicDataType, List, long)}
	 */
	public Set<String> getAttributeTypes()
	{
		return createdAttributeTypes.keySet();
	}
	
	/**
	 * Return the UUID of the concept that matches the description created by {@link #makeRefsetTypeConcept(String, String, long)}
	 * 
	 * @param refsetName the name or altName of the description
	 * @return the UUID of the concept that represents it
	 */
	public UUID getRefsetType(String refsetName)
	{
		return createdRefsetTypes.get(refsetName);
	}
	
	/**
	 * @param otherMetadataName
	 * @return Return the UUID of the grouping concept that was created by {@link #makeOtherMetadataRootNode(String, long)}
	 */
	public UUID getOtherMetadataRootType(String otherMetadataName)
	{
		return otherTypesNode.get(otherMetadataName);
	}
	
	/**
	 * @param otherMetadataGroup the group name
	 * @return all other names fed into {@link #makeOtherTypeConcept(UUID, UUID, String, String, String, String, DynamicDataType, List, long)}
	 */
	public Set<String> getOtherTypes(UUID otherMetadataGroup)
	{
		return otherTypes.get(otherMetadataGroup).keySet();
	}
	
	/**
	 * @param otherMetadataGroup the grouping concept that was created by {@link #makeOtherMetadataRootNode(String, long)}
	 * @param otherName the type that was created by {@link #makeOtherTypeConcept(UUID, UUID, String, String, String, String, DynamicDataType, List, long)}
	 * @return the UUID assigned to the concept created for {otherName}
	 */
	public UUID getOtherType(UUID otherMetadataGroup, String otherName)
	{
		HashMap<String, UUID> map = otherTypes.get(otherMetadataGroup);
		return map == null ? null : map.get(otherName);
	}
	
	/**
	 * A convenience method that calls {@link #getOtherType(UUID, String)} with {@link #getOtherMetadataRootType(String)} as the first 
	 * parameter
	 * @param otherMetadataGroup The string name that was passed into {@link #makeOtherMetadataRootNode(String, long);}
	 * @param otherName The string type that was passed into {@link #makeOtherTypeConcept(UUID, UUID, String, String, String, String, DynamicDataType, List, long)}
	 * @return The UUID of the concept that represents the type.
	 */
	public UUID getOtherType(String otherMetadataGroup, String otherName)
	{
		return getOtherType(getOtherMetadataRootType(otherMetadataGroup), otherName);
	}

	/**
	 * @param authorNid
	 */
	public void changeAuthor(int authorNid)
	{
		log.debug("Changing author nid from {} to {}", this.authorNid, authorNid);
		this.authorNid = authorNid;
	}

	private String getOriginStringForUuid(UUID uuid)
	{
		String temp = converterUUID.getUUIDCreationString(uuid);
		if (temp != null)
		{
			String[] parts = temp.split(":");
			if (parts != null && parts.length > 1)
			{
				return parts[parts.length - 1];
			}
			return temp;
		}
		return Get.conceptDescriptionText(identifierService.getNidForUuids(uuid));
	}
	
	/**
	 * If a terminology loader needs to create other terminology-specific metadata, outside of our standard types, they can use this method.
	 * It creates a concept under {@link #getMetadataRoot()}
	 * @param nodeName
	 * @param time
	 * @return
	 */
	public UUID makeOtherMetadataRootNode(String nodeName, long time)
	{
		String fsn = terminologyName + " " + nodeName + " (" + terminologyName + ")";
		log.info("Building extra metadata node '" + fsn + "'");
		otherTypesNode.put(nodeName, converterUUID.createNamespaceUUIDFromString(fsn, true));
		if (!conceptService.hasConcept(identifierService.assignNid(otherTypesNode.get(nodeName))))
		{
			makeConcept(otherTypesNode.get(nodeName), Status.ACTIVE, time);
			makeDescriptionEn(otherTypesNode.get(nodeName), fsn, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
					MetaData.PREFERRED____SOLOR.getPrimordialUuid());
			makeDescriptionEn(otherTypesNode.get(nodeName), SemanticTags.stripSemanticTagIfPresent(fsn),
					MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
					MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time,
					MetaData.PREFERRED____SOLOR.getPrimordialUuid());
			makeParentGraph(otherTypesNode.get(nodeName), metadataRoot, Status.ACTIVE, time);
		}
		return otherTypesNode.get(nodeName);
	}
	
	/**
	 * Creates a new concept to represent an attribute type, which can store one value of data. Adds parents of
	 * {@link #getAttributeTypesNode()} and {@link MetaData#IDENTIFIER_SOURCE____SOLOR} (if {isIdentifier} is true)
	 * 
	 * @param otherTypeGroup - the grouper concept to create this type concept under
	 * @param uuid optional - the UUID to use for the concept.  Created from the FQN, if not provided
	 * @param name The name to use for the FQN and Regular Name
	 * @param preferredName - optional - if provided, this is the preferred regular name, and the {name} value will be an acceptable regular name.
	 * If not provided, the {name} will also be used as the preferred regular name.
	 * @param altName - optional - additional regular name to add
	 * @param description - optional - used as the dynamic assemblage configuration description if provided, otherwise, the altName or name is used.
	 * @param dataType - optional the data type to store in the annotation - if this is null, it is the callers responsibility to call 
	 *            {@link #configureConceptAsDynamicAssemblage(UUID, String, DynamicColumnInfo[], IsaacObjectType, VersionType, long)}
	 *            to finish configuring this node, if they want to to be a dynamic type.
	 *            
	 *            Alternatively, you can pass a type of {@link DynamicDataType#UNKNOWN}, to specify that this should be setup as an identifier
	 *            (which is a brittle-string annotation type)
	 * @param additionalParents - optional - if this concept should have more parents, supply them here
	 * @param time - the commit time
	 * @return the UUID of the created concept.
	 */
	public UUID makeOtherTypeConcept(UUID otherTypeGroup, UUID uuid, String name, String preferredName, String altName, String description, 
			DynamicDataType dataType, List<UUID> additionalParents, long time)
	{
		UUID concept = makeTypeConcept(uuid, name, preferredName, altName, (fName, fConcept) -> {
			HashMap<String, UUID> map = otherTypes.get(otherTypeGroup);
			if (map == null)
			{
				map = new HashMap<>();
				otherTypes.put(otherTypeGroup, map);
			}
			return map.put(fName, fConcept);
		}, time);
		ArrayList<UUID> parents = new ArrayList<>();
		parents.add(otherTypeGroup);
		if (dataType != null)
		{
			if (dataType == DynamicDataType.UNKNOWN)
			{
				parents.add(MetaData.IDENTIFIER_SOURCE____SOLOR.getPrimordialUuid());
				makeBrittleRefsetMember(MetaData.IDENTIFIER_SOURCE____SOLOR.getPrimordialUuid(), concept, time);
			}
			else
			{
				configureConceptAsDynamicAssemblage(concept, StringUtils.isBlank(description) ? (StringUtils.isBlank(altName) ? name : altName) : description,
					new DynamicColumnInfo[] { new DynamicColumnInfo(0, concept, dataType, null, true, true) }, null, null, time);
			}
		}
		
		if (additionalParents != null)
		{
			parents.addAll(additionalParents);
		}

		makeParentGraph(concept, parents, Status.ACTIVE, time);
		return concept;
	}

	/**
	 * A mechanism for runtime (mostly delta) loaders to add pre-existing items into the mappings of name to types, for 
	 * simplified lookups and handling.
	 * @param parentTypesNode - A uuid that matches one of {@link #getAttributeTypesNode()}, {@link #getAssociationTypesNode()},
	 * {@link #getDescriptionTypesNode()}, {@link #getRefsetTypesNode()} or {@link #getRelationTypesNode()}
	 * @param type - optional - if not provided, calcualted from name in the same way as the other makeTypeConcept methods.
	 *     the type you are adding.  Should be a child of the {parentTypesNode}
	 * @param name - the name to store as the lookup to the {type}
	 * @return the passed in {type} uuid, or the calculated UUID.
	 */
	public UUID addExistingTypeConcept(UUID parentTypesNode, UUID type, String name)
	{
		String fqn = name + " (" + terminologyName + ")";
		final UUID concept = type == null ?  converterUUID.createNamespaceUUIDFromString(fqn) : type;
		
		final int typeNid = Get.nidForUuids(concept);
		if (!Get.conceptService().hasConcept(typeNid))
		{
			throw new RuntimeException("The concept " + concept + " " + name + " does not exist!");
		}
		
		if (!Get.taxonomyService().getSnapshotNoTree(ManifoldCoordinates.getStatedManifoldCoordinate(StampCoordinates.getDevelopmentLatest(), 
				LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate())).isChildOf(typeNid, Get.nidForUuids(parentTypesNode)))
		{
			throw new RuntimeException("The existing concept " + concept + " " + name + " must be a child of the parentTypesNode " + parentTypesNode);
		}	
		
		if (parentTypesNode.equals(getAttributeTypesNode().orElse(null)))
		{
			createdAttributeTypes.put(name, concept);
		}
		else if (parentTypesNode.equals(getAssociationTypesNode().orElse(null)))
		{
			createdAssociationTypes.put(name, concept);
		}
		else if (parentTypesNode.equals(getDescriptionTypesNode().orElse(null)))
		{
			createdDescriptionTypes.put(name, concept);
		}
		else if (parentTypesNode.equals(getRefsetTypesNode().orElse(null)))
		{
			createdRefsetTypes.put(name, concept);
		}
		else if (parentTypesNode.equals(getRelationTypesNode().orElse(null)))
		{
			createdRelationshipTypes.put(name, concept);
		}
		else
		{
			throw new RuntimeException("Unknown parent types node " + parentTypesNode);
		}
		return concept;
	}
}
