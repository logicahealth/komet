package sh.isaac.convert.directUtils.solor;

import sh.isaac.MetaData;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.*;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.converters.sharedUtils.stats.LoadStats;

import java.util.*;

/**
 * 2019-08-02
 * aks8m - https://github.com/aks8m
 *
 * This is an abstract wrapper for the DirectWriterHelper class. The idea is to be able to port some direct writer
 * classes (e.g., ConceptWriter, DescriptionWriter) from the Solor->Direct-Import module into using the
 * Direct Write Helper framework.
 */
public abstract class DirectWriteHelperTimedTaskWrapper extends TimedTaskWithProgressTracker<Void> {

    private final DirectWriteHelper directWriteHelper;

    public DirectWriteHelperTimedTaskWrapper(int author, int module, int path, ConverterUUID converterUUID, String terminologyName, boolean delayValidations) {
        this.directWriteHelper = new DirectWriteHelper(author, module, path, converterUUID, terminologyName, delayValidations);
    }

    /**
     * Calls {@link #makeConcept(UUID, Status, long, UUID[])} with no additional IDs
     *
     * @param concept The UUID of the concept to be created
     * @param status The status of the concept to be created
     * @param time The time to use for the concept to be created
     * @return The UUId of the concept created for convenience (same as the passed in value)
     */
    public UUID makeConcept(UUID concept, Status status, long time) {
        return this.directWriteHelper.makeConcept(concept, status, time);
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
    public UUID makeConceptEnNoDialect(UUID concept, String name, UUID descriptionType, UUID[] parents, Status status, long time) {
       return this.directWriteHelper.makeConceptEnNoDialect(concept, name, descriptionType, parents, status, time);
    }

    /**
     * @param concept The UUID of the concept to be created
     * @param status The status of the concept to be created
     * @param time The time to use for the concept to be created
     * @param additionalUUIDs additional UUIDs for the concept, if any.
     * @return The UUId of the concept created for convenience (same as the passed in value)
     */
    public UUID makeConcept(UUID concept, Status status, long time, UUID[] additionalUUIDs) {
        return this.directWriteHelper.makeConcept(concept, status, time, additionalUUIDs);
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
    public UUID makeDynamicRefsetMember(UUID assemblageConcept, UUID memberUUID, long time) {
        return this.directWriteHelper.makeDynamicRefsetMember(assemblageConcept, memberUUID, time);
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
    public UUID makeDynamicRefsetMember(UUID assemblageConcept, UUID memberUUID, Status status, long time) {
        return this.directWriteHelper.makeDynamicRefsetMember(assemblageConcept, memberUUID, status, time);
    }

    /**
     * This creates a semantic type of {@link VersionType#MEMBER}
     *
     * @param assemblageConcept The type of refset member to create
     * @param memberUUID The id being added to the refset
     * @param time The time to use for the entry
     * @return The UUID of the object created
     */
    public UUID makeBrittleRefsetMember(UUID assemblageConcept, UUID memberUUID, long time) {
        return this.directWriteHelper.makeBrittleRefsetMember(assemblageConcept, memberUUID, time);
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
    public UUID makeDynamicSemantic(UUID assemblageConcept, UUID referencedComponent, DynamicData data, long time) {
        return this.directWriteHelper.makeDynamicSemantic(assemblageConcept, referencedComponent, data, time);
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
    public UUID makeDynamicSemantic(UUID assemblageConcept, UUID referencedComponent, DynamicData[] data, long time) {
        return this.directWriteHelper.makeDynamicSemantic(assemblageConcept, referencedComponent, data, time);
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
    public UUID makeDynamicSemantic(UUID assemblageConcept, UUID referencedComponent, DynamicData[] data, long time, UUID uuidForCreatedSemantic) {
        return this.directWriteHelper.makeDynamicSemantic(assemblageConcept, referencedComponent, data, time, uuidForCreatedSemantic);
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
    public UUID makeDynamicSemantic(UUID assemblageConcept, UUID referencedComponent, DynamicData[] data, Status status, long time, UUID uuidForCreatedSemantic) {
        return this.directWriteHelper.makeDynamicSemantic(assemblageConcept, referencedComponent, data, status, time, uuidForCreatedSemantic);
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
    public UUID makeAssociation(UUID assemblageConcept, UUID sourceConcept, UUID targetConcept, Status status, long time) {
        return this.directWriteHelper.makeAssociation(assemblageConcept, sourceConcept, targetConcept, status, time);
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
    public UUID makeAssociation(UUID assemblageConcept, UUID sourceConcept, UUID targetConcept, long time) {
        return this.directWriteHelper.makeAssociation(assemblageConcept, sourceConcept, targetConcept, time);
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
    public UUID makeDescriptionEn(UUID concept, String text, UUID descriptionType, UUID caseSignificance, Status status, long time, UUID dialectAcceptibility) {
        return this.directWriteHelper.makeDescriptionEn(concept, text, descriptionType, caseSignificance, status, time, dialectAcceptibility);
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
    public UUID makeDescriptionEnNoDialect(UUID concept, String text, UUID descriptionType, Status status, long time) {
        return this.directWriteHelper.makeDescriptionEnNoDialect(concept, text, descriptionType, status, time);
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
    public UUID makeComment(UUID referencedComponent, String comment, String commentContext, long time) {
        return this.directWriteHelper.makeComment(referencedComponent, comment, commentContext, time);
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
                                UUID dialectAcceptibility) {
        return this.directWriteHelper.makeDescription(concept, text, descriptionType, language, caseSignificance, status,
                time, dialect, dialectAcceptibility);
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
    public UUID makeDescription(UUID description, UUID concept, String text, UUID descriptionType, UUID language,
                                UUID caseSignificance, Status status, long time, UUID dialect,
                                UUID dialectAcceptibility) {
       return this.directWriteHelper.makeDescription(description, concept, text, descriptionType, language,
               caseSignificance, status, time, dialect, dialectAcceptibility);
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
    public UUID makeExtendedDescriptionTypeAnnotation(UUID description, UUID nativeDescriptionType, long time) {
        return this.directWriteHelper.makeExtendedDescriptionTypeAnnotation(description, nativeDescriptionType, time);
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
    public UUID makeExtendedRelationshipTypeAnnotation(UUID logicGraph, UUID nativeRelationshipType, long time) {
        return this.directWriteHelper.makeExtendedRelationshipTypeAnnotation(logicGraph, nativeRelationshipType, time);
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
    public UUID makeParentGraph(UUID concept, UUID parent, Status status, long time) {
        return this.directWriteHelper.makeParentGraph(concept, parent, status,time);
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
    public UUID makeParentGraph(UUID concept, Collection<UUID> parents, Status status, long time) {
        return this.directWriteHelper.makeParentGraph(concept, parents, status, time);
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
    public UUID makeGraph(UUID concept, UUID graphSemanticId, LogicalExpression logicalExpression, Status status, long time) {
        return this.directWriteHelper.makeGraph(concept, graphSemanticId, logicalExpression, status, time);
    }

    /**
     * @param moduleNid the new moduleNid
     */
    public void changeModule(int moduleNid) {
        this.directWriteHelper.changeModule(moduleNid);
    }

    /**
     * Index and write the Chronology to the store. This only needs to be called if you have created chronologies outside of this class.
     *
     * @param c
     */
    public void indexAndWrite(Chronology c) {
       this.directWriteHelper.indexAndWrite(c);
    }

    /**
     * Properly update the taxonomy service for graph changes made. Ensure to call this at some point after calling
     * {@link #makeParentGraph(UUID, List, Status, long)}
     */
    public void processTaxonomyUpdates() {
       this.directWriteHelper.processTaxonomyUpdates();
    }

    /**
     * Properly execute any validations that were delayed during the load process
     */
    public void processDelayedValidations() {
       this.directWriteHelper.processDelayedValidations();
    }

    /**
     * @return the load stats object that has details on what has been created by this class.
     */
    public LoadStats getLoadStats() {
        return this.directWriteHelper.getLoadStats();
    }

    /**
     * Reset the load stats tracking
     */
    public void clearLoadStats() {
        this.directWriteHelper.clearLoadStats();
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
                                      boolean makeAssociationTypes, boolean makeRefsets, boolean makeRelationTypes, long time) {
       this.directWriteHelper.makeMetadataHierarchy(makeAttributeTypes, makeDescriptionTypes, makeDescriptionTypesNative,
               makeAssociationTypes, makeRefsets, makeRelationTypes, time);
    }

    /**
     * @return The concept that was created as the root of all metadata for this terminology
     */
    public UUID getMetadataRoot() {
        return this.directWriteHelper.getMetadataRoot();
    }

    /**
     * @return The module we are currently configured to use
     */
    public int getModuleNid() {
        return this.directWriteHelper.getModuleNid();
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
                                                    IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction, long time) {
        this.directWriteHelper.configureConceptAsDynamicAssemblage(concept, dynamicUsageDescription, columns, referencedComponentTypeRestriction, referencedComponentTypeSubRestriction, time);
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
                                              IsaacObjectType associationComponentTypeRestriction, VersionType associationComponentTypeSubRestriction, long time) {
       this.directWriteHelper.configureConceptAsAssociation(concept, dynamicUsageDescription, inverseName, associationComponentTypeRestriction, associationComponentTypeSubRestriction, time);
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
    public void configureConceptAsIdentifier(UUID concept, long time) {
        this.directWriteHelper.configureConceptAsIdentifier(concept, time);
    }

    /**
     * returns true if {@link #configureConceptAsIdentifier(UUID, long)} was called for this property type
     * @param concept
     * @return
     */
    public boolean isConfiguredAsIdentifier(UUID concept) {
        return this.directWriteHelper.isConfiguredAsIdentifier(concept);
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
    public UUID makeDescriptionTypeConcept(UUID uuid, String name, String preferredName, String altName, UUID coreDescriptionType, List<UUID> additionalParents, long time) {
        return this.directWriteHelper.makeDescriptionTypeConcept(uuid, name, preferredName, altName, coreDescriptionType, additionalParents,time);
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
                                           IsaacObjectType associationComponentTypeRestriction, VersionType associationComponentTypeSubRestriction, List<UUID> additionalParents, long time) {
        return this.directWriteHelper.makeAssociationTypeConcept(uuid, name, preferredName, altName, associationUsageDescription,
                inverseName, associationComponentTypeRestriction, associationComponentTypeSubRestriction, additionalParents,
                time);
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
                                         List<UUID> additionalParents, long time) {
        return this.directWriteHelper.makeAttributeTypeConcept(uuid, name, preferredName, altName, isIdentifier,
                dataType, additionalParents, time);
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
                                         DynamicDataType dataType, List<UUID> additionalParents, long time) {
        return this.directWriteHelper.makeAttributeTypeConcept(uuid, name, preferredName, altName, description,
                isIdentifier, dataType, additionalParents, time);
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
                                         DynamicColumnInfo[] dataTypeColumns, IsaacObjectType referencedComponentTypeRestriction, List<UUID> additionalParents, long time) {
        return this.directWriteHelper.makeAttributeTypeConcept(uuid, name, preferredName, altName, description,
                dataTypeColumns, referencedComponentTypeRestriction, additionalParents, time);
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
    public UUID linkToExistingAttributeTypeConcept(ConceptSpecification existingConstant, long time, StampCoordinate stampCoordinate) {
        return this.directWriteHelper.linkToExistingAttributeTypeConcept(existingConstant, time, stampCoordinate);
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
    public UUID makeRefsetTypeConcept(UUID uuid, String name, String preferredName, String altName, long time) {
        return this.directWriteHelper.makeRefsetTypeConcept(uuid, name, preferredName, altName, time);
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
    public UUID makeStringAnnotation(UUID assemblageConcept, UUID referencedComponent, String value, long time) {
        return this.directWriteHelper.makeStringAnnotation(assemblageConcept, referencedComponent, value, time);
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
    public UUID makeStringAnnotation(UUID assemblageConcept, UUID referencedComponent, String value, Status status, long time) {
        return this.directWriteHelper.makeStringAnnotation(assemblageConcept, referencedComponent, value, status, time);
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
    public UUID makeBrittleStringAnnotation(UUID assemblageConcept, UUID referencedComponent, String annotation, long time) {
        return this.directWriteHelper.makeBrittleStringAnnotation(assemblageConcept, referencedComponent, annotation, time);
    }

    /**
     * Create the standard terminology metadata entries, that detail what was loaded.
     * @param terminologyModuleVersionConcept
     * @param converterSourceArtifactVersion
     * @param converterSourceReleaseDate
     * @param converterOutputArtifactVersion
     * @param converterOutputArtifactClassifier
     * @param fhirURI
     * @param time
     */
    public void makeTerminologyMetadataAnnotations(UUID terminologyModuleVersionConcept, String converterSourceArtifactVersion,
                                                   Optional<String> converterSourceReleaseDate, Optional<String> converterOutputArtifactVersion, Optional<String> converterOutputArtifactClassifier,
                                                   Optional<String> fhirURI, long time) {
        this.directWriteHelper.makeTerminologyMetadataAnnotations(terminologyModuleVersionConcept, converterSourceArtifactVersion,
                converterSourceReleaseDate, converterOutputArtifactVersion, converterOutputArtifactClassifier, fhirURI,
                time);
    }

    /**
     * @return The UUID of the association types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
     *         if it has been created.
     */
    public Optional<UUID> getAssociationTypesNode() {
        return this.directWriteHelper.getAssociationTypesNode();
    }

    /**
     * @return The UUID of the attribute types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
     *         if it has been created.
     */
    public Optional<UUID> getAttributeTypesNode() {
        return this.directWriteHelper.getAttributeTypesNode();
    }

    /**
     * @return The UUID of the description types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
     *         if it has been created.
     */
    public Optional<UUID> getDescriptionTypesNode() {
        return this.directWriteHelper.getDescriptionTypesNode();
    }

    /**
     * @return The UUID of the refset types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
     *         if it has been created.
     */
    public Optional<UUID> getRefsetTypesNode() {
        return this.directWriteHelper.getRefsetTypesNode();
    }

    /**
     * @return The UUID of the relation types node from {@link #makeMetadataHierarchy(String, boolean, boolean, boolean, boolean, boolean, long)},
     *         if it has been created.
     */
    public Optional<UUID> getRelationTypesNode() {
        return this.directWriteHelper.getRelationTypesNode();
    }

    /**
     * @return true, if the concept was configured {@link #configureConceptAsAssociation(UUID, String, String, IsaacObjectType, VersionType, long)}
     *         as an association
     * @param concept the concept to check
     */
    public boolean isAssociation(UUID concept) {
        return this.directWriteHelper.isAssociation(concept);
    }

    /**
     * Return the UUID of the concept that matches the description created by {@link #makeDescriptionTypeConcept(String, String, String, UUID, long)}
     *
     * @param descriptionName the name or altName of the description
     * @return the UUID of the concept that represents it
     */
    public UUID getDescriptionType(String descriptionName) {
        return this.directWriteHelper.getDescriptionType(descriptionName);
    }

    /**
     * @return all description names fed into {@link #makeDescriptionTypeConcept(String, String, String, UUID, long)}
     */
    public Set<String> getDescriptionTypes() {
        return this.directWriteHelper.getDescriptionTypes();
    }

    /**
     * Return the UUID of the concept that matches the description created by
     * {@link #makeAssociationTypeConcept(UUID, String, String, String, String, String, IsaacObjectType, VersionType, List, long)
     *
     * @param associationName the name or altName of the association
     * @return the UUID of the concept that represents it
     */
    public UUID getAssociationType(String associationName) {
        return this.directWriteHelper.getAssociationType(associationName);
    }

    /**
     * Return the UUID of the concept that matches the description created by
     * {@link #makeAssociationTypeConcept(UUID, String, String, String, String, String, IsaacObjectType, VersionType, List, long)
     *
     * where the List parameter for the additional parents contained the concept {@link #getRelationTypesNode()}
     * @param relationshipName the name relationship
     * @return the UUID of the concept that represents it
     */
    public UUID getRelationshipType(String relationshipName) {
        return this.directWriteHelper.getRelationshipType(relationshipName);
    }

    /**
     * Return the UUID of the concept that matches the description created by {@link #makeAttributeTypeConcept(String, String, boolean, DynamicDataType, List, long)}
     *
     * @param attributeName the name or altName of the attribute
     * @return the UUID of the concept that represents it
     */
    public UUID getAttributeType(String attributeName) {
        return this.directWriteHelper.getAttributeType(attributeName);
    }

    /**
     * @return all attribute names fed into {@link #makeAttributeTypeConcept(UUID, String, String, String, boolean, DynamicDataType, List, long)}
     * or ({@link #makeAttributeTypeConcept(UUID, String, String, String, String, boolean, DynamicDataType, List, long)}
     */
    public Set<String> getAttributeTypes() {
        return this.directWriteHelper.getAttributeTypes();
    }

    /**
     * Return the UUID of the concept that matches the description created by {@link #makeRefsetTypeConcept(String, String, long)}
     *
     * @param refsetName the name or altName of the description
     * @return the UUID of the concept that represents it
     */
    public UUID getRefsetType(String refsetName) {
        return this.directWriteHelper.getRefsetType(refsetName);
    }

    /**
     * @param otherMetadataName
     * @return Return the UUID of the grouping concept that was created by {@link #makeOtherMetadataRootNode(String, long)}
     */
    public UUID getOtherMetadataRootType(String otherMetadataName) {
        return this.directWriteHelper.getOtherMetadataRootType(otherMetadataName);
    }

    /**
     * @param otherMetadataGroup the group name
     * @return all other names fed into {@link #makeOtherTypeConcept(UUID, UUID, String, String, String, String, DynamicDataType, List, long)}
     */
    public Set<String> getOtherTypes(UUID otherMetadataGroup) {
        return this.directWriteHelper.getOtherTypes(otherMetadataGroup);
    }

    /**
     * @param otherMetadataGroup the grouping concept that was created by {@link #makeOtherMetadataRootNode(String, long)}
     * @param otherName the type that was created by {@link #makeOtherTypeConcept(UUID, UUID, String, String, String, String, DynamicDataType, List, long)}
     * @return the UUID assigned to the concept created for {otherName}
     */
    public UUID getOtherType(UUID otherMetadataGroup, String otherName) {
        return this.directWriteHelper.getOtherType(otherMetadataGroup, otherName);
    }

    /**
     * A convenience method that calls {@link #getOtherType(UUID, String)} with {@link #getOtherMetadataRootType(String)} as the first
     * parameter
     * @param otherMetadataGroup The string name that was passed into {@link #makeOtherMetadataRootNode(String, long);}
     * @param otherName The string type that was passed into {@link #makeOtherTypeConcept(UUID, UUID, String, String, String, String, DynamicDataType, List, long)}
     * @return The UUID of the concept that represents the type.
     */
    public UUID getOtherType(String otherMetadataGroup, String otherName) {
        return this.directWriteHelper.getOtherType(otherMetadataGroup, otherName);
    }

    /**
     * @param authorNid
     */
    public void changeAuthor(int authorNid) {
        this.directWriteHelper.changeAuthor(authorNid);
    }

    /**
     * If a terminology loader needs to create other terminology-specific metadata, outside of our standard types, they can use this method.
     * It creates a concept under {@link #getMetadataRoot()}
     * @param nodeName
     * @param time
     * @return
     */
    public UUID makeOtherMetadataRootNode(String nodeName, long time) {
        return this.directWriteHelper.makeOtherMetadataRootNode(nodeName, time);
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
                                     DynamicDataType dataType, List<UUID> additionalParents, long time) {
        return this.directWriteHelper.makeOtherTypeConcept(otherTypeGroup, uuid, name, preferredName, altName, description,
                dataType, additionalParents, time);
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
    public UUID addExistingTypeConcept(UUID parentTypesNode, UUID type, String name) {
        return this.directWriteHelper.addExistingTypeConcept(parentTypesNode, type, name);
    }
}
