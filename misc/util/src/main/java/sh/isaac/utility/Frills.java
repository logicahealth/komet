/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

package sh.isaac.utility;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.IsaacCache;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableDescriptionVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnUtility;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.index.IndexSemanticQueryService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.util.NumericUtils;
import sh.isaac.api.util.TaskCompleteCallback;
import sh.isaac.api.util.UUIDUtil;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.model.VersionImpl;
import sh.isaac.model.concept.ConceptVersionImpl;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.coordinate.ManifoldCoordinateImpl;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.external.ConceptNodeWithUuids;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.semantic.DynamicUsageDescriptionImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.DynamicImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.model.semantic.version.LongVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;


/**
 * The Class Frills.
 */

//This is a service, simply to implement the DynamicColumnUtility interface.  Everything else is static, and may be used directly
@Service
@Singleton
public class Frills
         implements DynamicColumnUtility, IsaacCache {
   private static final Logger LOG = LogManager.getLogger(Frills.class);

   private static final Cache<Integer, Boolean> IS_ASSOCIATION_CLASS = Caffeine.newBuilder().maximumSize(50).build();
   private static final Cache<Integer, Boolean> IS_MAPPING_CLASS = Caffeine.newBuilder().maximumSize(50).build();
   private static final Cache<Integer, Integer> MODULE_TO_TERM_TYPE_CACHE = Caffeine.newBuilder().maximumSize(50).build();


   /**
    * Build, without committing, a new concept using the provided columnName and columnDescription values which is suitable 
    * for use as a column descriptor within {@link DynamicUsageDescription}.
    * 
    * see {@link DynamicUsageDescription}
    * 
    * The new concept will be created under the concept {@link DynamicConstants#DYNAMIC_COLUMNS}
    * 
    * A complete usage pattern (where both the refex assemblage concept and the column name concept needs
    * to be created) would look roughly like this:
    * 
    * Frills.createNewDynamicSemanticUsageDescriptionConcept(
    *    "The name of the Semantic", 
    *    "The description of the Semantic",
    *    new DynamicColumnInfo[]{new DynamicColumnInfo(
    *       0,
    *       Frills.createNewDynamicSemanticColumnInfoConcept(
    *          "column name",
    *          "column description"
    *          )
    *       DynamicDataType.STRING,
    *       new DynamicStringImpl("default value")
    *       )}
    *    )
    * 
    *
    * @param columnName the column name
    * @param columnDescription the column description
    * @return the concept chronology
    * @throws RuntimeException the runtime exception
    */
   public static List<Chronology> buildUncommittedNewDynamicSemanticColumnInfoConcept(String columnName,
         String columnDescription) {
      return Get.service(DynamicUtility.class).buildUncommittedNewDynamicSemanticColumnInfoConcept(columnName, columnDescription, null, null);
   }

   /**
    * This method returns a new uncommitted concept chronology which represents a DynamicSemantic definition.
    * A {@link DynamicUsageDescription} abstraction may be constructed by passing this chronology into he {@link DynamicUsageDescriptionImpl} ctor.
    * 
    * see {@link DynamicUsageDescription}
    *
    * @param semanticFQN the semantic FQN
    * @param semanticPreferredTerm the semantic preferred term
    * @param semanticDescription the semantic description
    * @param columns the data definitions of the columns for this dynamic semantic 
    * @param parentConceptNid - optional - the parent concept nid - if not specified, the concept will be created as a child 
    *    of {@link DynamicConstants#DYNAMIC_ASSEMBLAGES} 
    * @param referencedComponentRestriction - optional - if specified, this semantic may only be applied to the specified type of referenced components.
    * @param referencedComponentSubRestriction - optional - if specified, and the referencedComponentRestriction is of type semantic, then this can further restrice
    * the type of semantic this can be applied to. See {@link DynamicUtility#configureDynamicRestrictionData(IsaacObjectType, VersionType)}
    * @param editCoord the edit coord
    * @return the concept chronology that represents the new dynamic semantic type.
    */
   public static ConceptChronology buildUncommittedNewDynamicSemanticUsageDescription(String semanticFQN,
         String semanticPreferredTerm,
         String semanticDescription,
         DynamicColumnInfo[] columns,
         Integer parentConceptNid,
         IsaacObjectType referencedComponentRestriction,
         VersionType referencedComponentSubRestriction,
         EditCoordinate editCoord) {
      try {
         final EditCoordinate localEditCoord = ((editCoord == null) ? 
               Get.configurationService().getUserConfiguration(Optional.empty()).getEditCoordinate() : editCoord);
         final ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);

         conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE____SOLOR);
         conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT____SOLOR);
         conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());

         final DescriptionBuilderService descriptionBuilderService = LookupService.getService(
                                                                         DescriptionBuilderService.class);
         final LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class)
                                                                  .getLogicalExpressionBuilder();
         final ConceptChronology parentConcept = Get.conceptService()
                                                    .getConceptChronology((parentConceptNid == null)
                                                          ? DynamicConstants.get().DYNAMIC_ASSEMBLAGES
                                                                .getNid()
               : parentConceptNid);

         NecessarySet(And(ConceptAssertion(parentConcept, defBuilder)));

         final LogicalExpression parentDef = defBuilder.build();
         final ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(semanticFQN, null, parentDef, MetaData.CONCEPT_ASSEMBLAGE____SOLOR.getNid());
         DescriptionBuilder<? extends SemanticChronology, ? extends MutableDescriptionVersion> definitionBuilder =
            descriptionBuilderService.getDescriptionBuilder(
                semanticPreferredTerm,
                builder,
                MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR,
                MetaData.ENGLISH_LANGUAGE____SOLOR);

         definitionBuilder.addPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT____SOLOR);
         builder.addDescription(definitionBuilder);

         final ConceptChronology newCon = builder.build(localEditCoord, ChangeCheckerMode.ACTIVE, new ArrayList<>())
                                                 .getNoThrow();

         LookupService.getService(DynamicUtility.class).configureConceptAsDynamicSemantic(newCon.getNid(), semanticDescription, 
            columns, referencedComponentRestriction, referencedComponentSubRestriction, localEditCoord);

         return newCon;
      } catch (final IllegalStateException e) {
         throw new RuntimeException("Creation of dynamic Failed!", e);
      }
   }

   /**
    * Create a new concept using the provided columnName and columnDescription values which is suitable 
    * for use as a column descriptor within {@link DynamicUsageDescription}.
    * 
    * The new concept will be created under the concept {@link DynamicConstants#DYNAMIC_COLUMNS}
    * 
    * A complete usage pattern (where both the refex assemblage concept and the column name concept needs
    * to be created) would look roughly like this:
    * 
    * Frills.createNewDynamicSemanticUsageDescriptionConcept(
    *    "The name of the Semantic", 
    *    "The description of the Semantic",
    *    new DynamicColumnInfo[]{new DynamicColumnInfo(
    *       0,
    *       DynamicColumnInfo.createNewDynamicSemanticColumnInfoConcept(
    *          "column name",
    *          "column description"
    *          )
    *       DynamicDataType.STRING,
    *       new DynamicStringImpl("default value")
    *       )}
    *    )
    *
    * @param columnName the column name
    * @param columnDescription the column description
    * @return the concept chronology<? extends concept version<?>>
    * @throws RuntimeException the runtime exception
    */
   public static ConceptChronology createNewDynamicSemanticColumnInfoConcept(String columnName, String columnDescription)
            throws RuntimeException {
      final ConceptChronology newCon = (ConceptChronology)buildUncommittedNewDynamicSemanticColumnInfoConcept(columnName, columnDescription).get(0);

      try {  //TODO [DAN 3] figure out what edit coords we should use for this sort of work.
         Get.commitService()
            .commit(Get.configurationService().getGlobalDatastoreConfiguration().getDefaultEditCoordinate(), "creating new dynamic column: " + columnName)
            .get();
         return newCon;
      } catch (InterruptedException | ExecutionException e) {
         final String msg = "Failed committing new DynamicSemanticColumnInfo concept columnName=\"" + columnName +
                            "\", columnDescription=\"" + columnDescription + "\"";

         LOG.error(msg, e);
         throw new RuntimeException(msg, e);
      }
   }

   /**
    * See {@link DynamicUsageDescription} for the full details on what this builds.
    *
    * Does all the work to create a new concept that is suitable for use as an Assemblage Concept for a new style dynamic element.
    *
    * The concept will be created under the concept {@link DynamicConstants#DYNAMIC_ASSEMBLAGES} if a parent is not specified
    *
    * //TODO [REFEX] figure out language details (how we know what language to put on the name/description
    *
    * @param semanticFQN the semantic FQN
    * @param semanticPreferredTerm - The preferred term for this refex concept that will be created.
    * @param semanticDescription - A user friendly string the explains the overall intended purpose of this semantic (what it means, what it stores)
    * @param columns - The column information for this new refex.  May be an empty list or null.
    * @param parentConceptNid  - optional - if null, uses {@link DynamicConstants#DYNAMIC_ASSEMBLAGES}
    * @param referencedComponentRestriction - optional - may be null - if provided - this restricts the type of object referenced by the nid or
    * UUID that is set for the referenced component in an instance of this semantic.  If {@link IsaacObjectType#UNKNOWN} is passed, it is ignored, as
    * if it were null.
    * @param referencedComponentSubRestriction - optional - may be null - subtype restriction for {@link IsaacObjectType#SEMANTIC} restrictions
    * @param editCoord - optional - the coordinate to use during create of the semantic concept (and related descriptions) - if not provided, uses system default.
    * @return a reference to the newly created semantic item
    */
   public static DynamicUsageDescription createNewDynamicSemanticUsageDescriptionConcept(String semanticFQN,
         String semanticPreferredTerm,
         String semanticDescription,
         DynamicColumnInfo[] columns,
         Integer parentConceptNid,
         IsaacObjectType referencedComponentRestriction,
         VersionType referencedComponentSubRestriction,
         EditCoordinate editCoord) {
      final ConceptChronology newDynamicSemanticUsageDescriptionConcept =
         buildUncommittedNewDynamicSemanticUsageDescription(semanticFQN,
             semanticPreferredTerm,
             semanticDescription,
             columns,
             parentConceptNid,
             referencedComponentRestriction,
             referencedComponentSubRestriction,
             editCoord);

      try {
         Get.commitService()
            .commit(Get.configurationService().getGlobalDatastoreConfiguration().getDefaultEditCoordinate(), 
                  "creating new dynamic assemblage (DynamicSemanticUsageDescription): NID=" +
                newDynamicSemanticUsageDescriptionConcept.getNid() + ", FQN=" + semanticFQN + ", PT=" +
                semanticPreferredTerm + ", DESC=" + semanticDescription)
            .get();
      } catch (InterruptedException | ExecutionException e) {
         throw new RuntimeException("Commit of dynamic Failed!", e);
      }

      return new DynamicUsageDescriptionImpl(newDynamicSemanticUsageDescriptionConcept.getNid());
   }
   
   /**
    * Create a logical expression suitable for populating a concept's logic graph sequence
    * with the specified parents
    * 
    * @param parentConceptSequences
    * @return the logical expression
    */
   public static LogicalExpression createConceptParentLogicalExpression(int... parentConceptSequences) {
      // build logic graph
      LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();
      ArrayList<Assertion> assertions = new ArrayList<>();
      for (int parentConceptSequence : parentConceptSequences) {
         assertions.add(ConceptAssertion(parentConceptSequence, defBuilder));
      }

      NecessarySet(And(assertions.toArray(new Assertion[assertions.size()])));
      LogicalExpression parentDef = defBuilder.build();

      return parentDef;
   }

   /**
    * Defines association.
    *
    * @param conceptNid the concept sequence
    * @return true, if the concept is properly defined as a semantic which represents an association.  See {@link DynamicConstants#DYNAMIC_ASSOCIATION}
    */
   public static boolean definesAssociation(int conceptNid) {
      return IS_ASSOCIATION_CLASS.get(conceptNid, nid -> {
         return Get.assemblageService()
               .getSemanticChronologyStreamForComponentFromAssemblage(nid, DynamicConstants.get().DYNAMIC_ASSOCIATION.getNid())
               .anyMatch(semantic -> true);
      });
   }
   
   /**
    * Checks if the semantic chronology is a type that represents an association.  Passes the assemblageNid to {@link #definesAssociation(int)}
    *
    * @param sc the sc
    * @return true, if association
    */
   public static boolean definesAssociation(SemanticChronology sc) {
      return definesAssociation(sc.getAssemblageNid());
   }
   
   /**
    * Returns true if a concept has a {@link MetaData#IDENTIFIER_SOURCE____SOLOR} semantic attached to it (at any coordinate)
    * @param assemblageNid
    * @return true, if it is a semantic definition
    */
   public static boolean definesIdentifierSemantic(int assemblageNid) 
   {
      if (Get.identifierService().getObjectTypeForComponent(assemblageNid) == IsaacObjectType.CONCEPT) 
      {
         Optional<SemanticChronology> semantic = Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(
               assemblageNid, MetaData.IDENTIFIER_SOURCE____SOLOR.getNid()).findAny();
      if (semantic.isPresent())
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Defines dynamic element.  See {@link DynamicUsageDescriptionImpl#isDynamicSemantic(int)}
    *
    * @param conceptNid the concept nid
    * @return true, if successful
    */
   public static boolean definesDynamicSemantic(int conceptNid) {
      return DynamicUsageDescriptionImpl.isDynamicSemantic(conceptNid);
   }

   /**
    * Checks if the concept is specified in such a way that it defines a mapping assemblage.  See {@link IsaacMappingConstants#DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE}
    *
    * @param conceptNid the concept sequence
    * @return true, if successful
    */
   public static boolean definesMapping(int conceptNid) {

      return IS_MAPPING_CLASS.get(conceptNid, nid -> {
         return Get.assemblageService()
               .getSemanticChronologyStreamForComponentFromAssemblage(conceptNid, IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getNid())
               .anyMatch(semantic -> true);
      });
   }
   
   /**
    * Walk up the module tree, looking for the module concept sequence directly under {@link MetaData#MODULE____SOLOR} - return it if found, otherwise, return null.
    */
   private static Integer findTermTypeConcept(int conceptModuleNid, StampCoordinate stamp)
   {
      int[] parents = Get.taxonomyService().getSnapshot(
            new ManifoldCoordinateImpl(stamp == null ? StampCoordinates.getDevelopmentLatest(): stamp, 
                  LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate()))
            .getTaxonomyParentConceptNids(conceptModuleNid);
      for (int current : parents)
      {
         if (current == MetaData.MODULE____SOLOR.getNid())
         {
            return conceptModuleNid;
         }
         else
         {
            return findTermTypeConcept(current, stamp);
         }
      }
      return null;
   }

   /**
    * Checks if the SemanticChronology represents a mapping.  Passes the assemblageNid to {@link #definesMapping(int)}
    *
    * @param sc the sc
    * @return true, if mapping
    */
   public static boolean isMapping(SemanticChronology sc) {
      return definesMapping(sc.getAssemblageNid());
   }
   
   /**
    * Utility method to get the best text value description for a concept, according to the passed in options, 
    * or the user preferences.  Calls {@link #getDescription(int, StampCoordinate, LanguageCoordinate)} with values 
    * extracted from the taxonomyCoordinate, or null. 
    * @param conceptUUID - UUID for a concept
    * @param languageCoordinate - optional - if not provided, defaults to system preferences values
    * @param stampCoordinate - optional - if not provided, defaults to system preference values
    * @return the description
    */
   public static Optional<String> getDescription(UUID conceptUUID, StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) 
   {
      return getDescription(Get.identifierService().getNidForUuids(conceptUUID), stampCoordinate, languageCoordinate);
   }
   
   /**
    * Utility method to get the best text value description for a concept, according to the passed in options, 
    * or the user preferences. 
    * @param conceptNid - The nid of the concept
    * @param manifoldCoordinate - optional - if not provided, defaults to system preferences values
    * @return the description
    */
   public static Optional<String> getDescription(int conceptNid, ManifoldCoordinate manifoldCoordinate) {
      return getDescription(conceptNid, 
          manifoldCoordinate == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getManifoldCoordinate().getStampCoordinate() 
                : manifoldCoordinate.getStampCoordinate(), 
          manifoldCoordinate == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getManifoldCoordinate().getLanguageCoordinate() 
                : manifoldCoordinate.getLanguageCoordinate());
   }
   
   /**
    * Utility method to get the best text value description for a concept, according to the passed in options, 
    * or the user preferences. 
    * @param conceptNid - The nid of the concept
    * @param languageCoordinate - optional - if not provided, defaults to system preferences values
    * @param stampCoordinate - optional - if not provided, defaults to system preference values
    * @return the description, if available
    */
   public static Optional<String> getDescription(int conceptNid, StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
      LanguageCoordinate lc = languageCoordinate == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getLanguageCoordinate() : languageCoordinate;
      LatestVersion<DescriptionVersion> d = lc.getDescription(conceptNid, 
            stampCoordinate == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate() : stampCoordinate);

      if (d.isPresent()) {
         return Optional.of(d.get().getText());
      }
      return Optional.empty();
   }
   
   /**
    * If this description is flagged as an extended description type, return the type concept of the extension.
    * @param stampCoordinate - optional Stamp - pass null to use the default stamp.  In either case, this only looks for an active extended type - state is overridden.
    * @param descriptionId - the nid or sequence of the description semantic to check for an extended type. 
    * @param returnInactiveExtendedType - true to return an extended description type even if it is INACTVE .  
    * false to only return the extended description type if it is present and active (returns EMPTY if the extended type is missing or inactive)
    * @return the concept identifer of the extended type
    */
   public static Optional<UUID> getDescriptionExtendedTypeConcept(StampCoordinate stampCoordinate, int descriptionId, boolean returnInactiveExtendedType) 
   {
      Optional<SemanticChronology> descriptionExtendedTypeAnnotationSemantic =
            getAnnotationSemantic(descriptionId, DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getNid());
      
      if (descriptionExtendedTypeAnnotationSemantic.isPresent()) 
      {
         final StampCoordinate effectiveStampCoordinate = (stampCoordinate == null) ? 
               Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate().makeCoordinateAnalog(Status.ANY_STATUS_SET) : 
                  stampCoordinate.makeCoordinateAnalog(Status.ANY_STATUS_SET);
         
         LatestVersion<Version> lsv = descriptionExtendedTypeAnnotationSemantic.get().getLatestVersion(effectiveStampCoordinate);
         if (! lsv.isPresent()) {
            LOG.info("No latest version present for descriptionExtendedTypeAnnotationSemantic chronology " 
                  + descriptionExtendedTypeAnnotationSemantic.get().getPrimordialUuid() + " using " + (stampCoordinate != null ? "passed" : "default") 
                  + " stamp coordinate analog " + effectiveStampCoordinate);
            return Optional.empty();
         }
         if (!lsv.contradictions().isEmpty()) {
            //TODO handle contradictions
            LOG.warn("Component " + descriptionId + " " + " has DYNAMIC_SEMANTIC_EXTENDED_DESCRIPTION_TYPE annotation with " + 
            lsv.contradictions().size() + " contradictions");
         }
         if (!returnInactiveExtendedType && lsv.get().getStatus() != Status.ACTIVE) {
            LOG.info("Latest version present is NOT ACTIVE for descriptionExtendedTypeAnnotationSemantic chronology " 
                  + descriptionExtendedTypeAnnotationSemantic.get().getPrimordialUuid() + " using " + (stampCoordinate != null ? "passed" : "default") 
                  + " stamp coordinate analog " + effectiveStampCoordinate);
            return Optional.empty();   
         }
         
         DynamicData[] dataColumns = ((DynamicVersion<?>)lsv.get()).getData();
         if (dataColumns.length != 1)
         {
            throw new RuntimeException("Invalidly specified DYNAMIC_SEMANTIC_EXTENDED_DESCRIPTION_TYPE.  Should always have a column size of 1");
         }
         
         if (dataColumns[0].getDynamicDataType() == DynamicDataType.UUID) 
         {
            return Optional.of(((DynamicUUIDImpl)dataColumns[0]).getDataUUID());
         }
         // This isn't supposed to happen, but we have some bad data where it did.
         else if (dataColumns[0].getDynamicDataType() == DynamicDataType.STRING) 
         {
            LOG.warn("Extended description type data found with type string instead of type UUID!");
            return Optional.of(UUID.fromString(((DynamicStringImpl)dataColumns[0]).getDataString()));
         }
         
         throw new RuntimeException("Failed to find UUID DynamicSemanticData type in DYNAMIC_SEMANTIC_EXTENDED_DESCRIPTION_TYPE annotation dynamic semantic");
      }
      return Optional.empty();
   }
   
   /**
    * Calls {@link #getConceptForUnknownIdentifier(String)} in a background thread.  returns immediately. 
    * 
    * 
    * @param identifier - what to search for
    * @param callback - who to inform when lookup completes
    * @param callId - An arbitrary identifier that will be returned to the caller when this completes
    * @param stampCoord - optional - what stamp to use when returning the ConceptSnapshot (defaults to user prefs)
    * @param langCoord - optional - what lang coord to use when returning the ConceptSnapshot (defaults to user prefs)
    */
   public static void lookupConceptForUnknownIdentifier(
         final String identifier,
         final TaskCompleteCallback<ConceptSnapshot> callback,
         final Integer callId,
         final StampCoordinate stampCoord,
         final LanguageCoordinate langCoord)
   {
      LOG.debug("Threaded Lookup: '{}'", identifier);
      final long submitTime = System.currentTimeMillis();
      Runnable r = new Runnable()
      {
         @Override
         public void run()
         {
            ConceptSnapshot result = null;
            Optional<? extends ConceptChronology> c = getConceptForUnknownIdentifier(identifier);
            if (c.isPresent())
            {
               Optional<ConceptSnapshot> temp = getConceptSnapshot(c.get().getNid(), stampCoord, langCoord);
               if (temp.isPresent())
               {
                  result = temp.get();
               }
               callback.taskComplete(result, submitTime, callId);
            }
            else {
               callback.taskComplete(null, submitTime, callId);
            }
         }
      };
      Get.workExecutors().getExecutor().execute(r);
   }
   
   /**
    * 
    * All done in a background thread, method returns immediately
    * 
    * @param nid - The NID to search for
    * @param callback - who to inform when lookup completes
    * @param callId - An arbitrary identifier that will be returned to the caller when this completes
    * @param stampCoord - optional - what stamp to use when returning the ConceptSnapshot (defaults to user prefs)
    * @param langCoord - optional - what lang coord to use when returning the ConceptSnapshot (defaults to user prefs)
    */
   public static void lookupConceptSnapshot(final int nid, final TaskCompleteCallback<ConceptSnapshot> callback, final Integer callId, 
         final StampCoordinate stampCoord, final LanguageCoordinate langCoord) {
      LOG.debug("Threaded Lookup: '{}'", nid);
      final long submitTime = System.currentTimeMillis();
      Runnable r = new Runnable() {
         @Override
         public void run() {
            Optional<ConceptSnapshot> c = getConceptSnapshot(nid, stampCoord, langCoord);
            if (c.isPresent()) {
               callback.taskComplete(c.get(), submitTime, callId);
            } else {
               callback.taskComplete(null, submitTime, callId);
            }
         }
      };
      Get.workExecutors().getExecutor().execute(r);
   }

   /**
    * Make stamp coordinate analog varying by modules only.
    *
    * @param existingStampCoordinate the existing stamp coordinate
    * @param requiredModuleSequence the required module sequence
    * @param optionalModuleSequences the optional module sequences
    * @return the stamp coordinate
    */
   public static StampCoordinate makeStampCoordinateAnalogVaryingByModulesOnly(StampCoordinate existingStampCoordinate,
         int requiredModuleSequence,
         int... optionalModuleSequences) {
      final NidSet moduleSequenceSet = new NidSet();

      moduleSequenceSet.add(requiredModuleSequence);

      if (optionalModuleSequences != null) {
         for (final int seq: optionalModuleSequences) {
            moduleSequenceSet.add(seq);
         }
      }

      final EnumSet<Status> allowedStates = EnumSet.allOf(Status.class);

      allowedStates.addAll(existingStampCoordinate.getAllowedStates());

      final StampCoordinate newStampCoordinate = new StampCoordinateImpl(
                                                     existingStampCoordinate.getStampPrecedence(),
                                                           existingStampCoordinate.getStampPosition(),
                                                           moduleSequenceSet,
                                                           allowedStates);

      return newStampCoordinate;
   }

   /**
    * Read dynamic column name description from a concept that represents a dynamic semantic column
    *
    * @param columnDescriptionConcept the column description concept
    * @return the string[] - position 0 being the "name" of the column, and position 1 being the description.
    * Suitable for a table label and tooltip.
    */
   @Override
   public String[] readDynamicColumnNameDescription(UUID columnDescriptionConcept) {
      String columnName           = null;
      String columnDescription    = null;
      String fqn                  = null;
      String acceptableSynonym    = null;
      String acceptableDefinition = null;

      try {
         final ConceptChronology cc = Get.conceptService()
                                         .getConceptChronology(columnDescriptionConcept);

         for (final SemanticChronology dc: cc.getConceptDescriptionList()) {
            if ((columnName != null) && (columnDescription != null)) {
               break;
            }

            final LatestVersion<DescriptionVersion> descriptionVersion = ((SemanticChronology) dc).getLatestVersion(Get.configurationService()
                  .getUserConfiguration(Optional.empty()).getStampCoordinate()
                  .makeCoordinateAnalog(Status.ANY_STATUS_SET));

            if (descriptionVersion.isPresent()) {
               final DescriptionVersion d = descriptionVersion.get();

               if (d.getDescriptionTypeConceptNid() ==
                     TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
                  fqn = d.getText();
               } else if (d.getDescriptionTypeConceptNid() ==
                          TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()) {
                  if (Frills.isDescriptionPreferred(d.getNid(), null)) {
                     columnName = d.getText();
                  } else {
                     acceptableSynonym = d.getText();
                  }
               } else if (d.getDescriptionTypeConceptNid() ==
                          TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
                  if (Frills.isDescriptionPreferred(d.getNid(), null)) {
                     columnDescription = d.getText();
                  } else {
                     acceptableDefinition = d.getText();
                  }
               }
            }
         }
      } catch (final RuntimeException e) {
         LOG.warn("Failure reading DynamicSemanticColumnInfo '" + columnDescriptionConcept + "'", e);
      }

      if (columnName == null) {
         LOG.warn(
             "No preferred synonym found on '" + columnDescriptionConcept + "' to use " +
             "for the column name - using FQN");
         columnName = ((fqn == null) ? "ERROR - see log"
                                     : fqn);
      }

      if ((columnDescription == null) && (acceptableDefinition != null)) {
         columnDescription = acceptableDefinition;
      }

      if ((columnDescription == null) && (acceptableSynonym != null)) {
         columnDescription = acceptableSynonym;
      }

      if (columnDescription == null) {
         LOG.debug(
             "No preferred or acceptable definition or acceptable synonym found on '" + columnDescriptionConcept +
             "' to use for the column description- re-using the the columnName, instead.");
         columnDescription = columnName;
      }

      return new String[] { columnName, columnDescription };
   }

   /**
    * Refresh indexes.  
    */
   public static void refreshIndexes() {
      LookupService.get()
                   .getAllServiceHandles(IndexQueryService.class)
                   .forEach(
                       index -> {
         // Making a query, with long.maxValue, causes the index to refresh itself, and look at the latest updates, if there have been updates.
                          index.getService()
                               .query("hi", null, null, null, 1, Long.MAX_VALUE);
                       });
   }
   
   /**
    * calls {@link Frills#resetStatus(Status, Chronology, EditCoordinate, StampCoordinate...)} but has types specified for concepts
    */
   private static VersionUpdatePair<ConceptVersion> resetConceptState(Status status, ConceptChronology chronology, 
         EditCoordinate editCoordinate, StampCoordinate ... readCoordinates) throws Exception {   
      return resetStatus(status, chronology, editCoordinate, readCoordinates);
   }
   
   /**
    * 
    * Reset the state of the chronology IFF an existing version corresponding to passed edit and/or stamp coordinates either does not exist or differs in state.
    * 
    * @param status
    *           - state to which to set new version of chronology
    * @param chronology
    *           - the chronology of the object that we want to create a new version of with the specified state
    * @param editCoordinate
    *           - where to create the new version
    * @param readCoordinates
    *           - (optional) the read coordinates to read the current state from. Defaults to the system default if not provided. When more than one is provided,
    *           it tries each in order, until is finds the first one that is present.
    * @return - null, if no change was required, or, the mutable that will need to be committed. Also returns the latestVersion that the state was read from for
    *         convenience.
    * @throws RestException
    */
   @SuppressWarnings({"unchecked" })
   private static <T extends Version> VersionUpdatePair<T> resetStatus(Status status, Chronology chronology, EditCoordinate editCoordinate,
         StampCoordinate... readCoordinates) throws Exception {
      String detail = chronology.getIsaacObjectType() + " " + chronology.getClass().getSimpleName() + " (UUID=" + chronology.getPrimordialUuid() + ")";
      LatestVersion<Version> latestVersion = null;

      if (readCoordinates == null || readCoordinates.length == 0) {
         latestVersion = chronology.getLatestVersion(Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate());
      } else {
         for (StampCoordinate rc : readCoordinates) {
            latestVersion = chronology.getLatestVersion(rc);
            if (latestVersion.isPresent()) {
               break;
            }
         }
      }

      if (!latestVersion.isPresent()) {
         throw new Exception("Failed getting latest version of " + detail + ". May require different stamp or edit coordinate parameters.");
      }

      if (latestVersion.get().getStatus() == status) {
         LOG.debug("Not resetting state of " + detail + "from " + latestVersion.get().getStatus() + " to " + status);
         return null;
      }

      VersionUpdatePair<T> versionsHolder = new VersionUpdatePair<>();
      if (chronology instanceof SemanticChronology) {
         versionsHolder.set((T) ((SemanticChronology) chronology).<T>createMutableVersion(status, editCoordinate), (T)latestVersion.get());
      } else if (chronology instanceof ConceptChronology) {
         versionsHolder.set((T)((ConceptChronology) chronology).createMutableVersion(status, editCoordinate), (T)latestVersion.get());
      } else {
         throw new RuntimeException("Unsupported ObjectChronology type " + detail);
      }
      return versionsHolder;
   }
   
   /**
    * Reset the state of an object to the new state, copying by creating a new version of the object with the same mutable values as the existing object.
    * 
    * This returns an empty optional (and does a NOOP) if the existing state is already the same as the desired state.
    * 
    * @param status
    *           - The desired new state
    * @param componentToModify
    *           - the id of the object to change the state of
    * @param editCoordinate
    *           - where to write the new state.
    * @param readCoordinates
    *           - (optional) the read coordinates to read the current state from. Defaults to the system default if not provided. When more than one is provided,
    *           it tries each in order, until is finds the first one that is present.
    * @return - empty optional, if no change, or the uncommitted chronology of the object that was changed.
    * @throws Exception
    */
   public static Optional<Chronology> resetStatusWithNoCommit(Status status, int componentToModify, EditCoordinate editCoordinate, StampCoordinate... readCoordinates) throws Exception {

      final IsaacObjectType type = Get.identifierService().getObjectTypeForComponent(componentToModify);

      Chronology objectToCommit = null;

      Status priorState = null;
      int nid = 0;

      switch (type) {
         case CONCEPT: {
            ConceptChronology cc = Get.conceptService().getConceptChronology(componentToModify);
            nid = cc.getNid();

            VersionUpdatePair<ConceptVersion> updatePair = resetConceptState(status, cc, editCoordinate, readCoordinates);
            if (updatePair != null) {
               priorState = updatePair.latest.getStatus();
               objectToCommit = cc;
            }
            break;
         }

         case SEMANTIC: {
            SemanticChronology semantic = Get.assemblageService().getSemanticChronology(componentToModify);
            nid = semantic.getNid();
            switch (semantic.getVersionType()) {
               case DESCRIPTION: {
                  VersionUpdatePair<DescriptionVersionImpl> semanticUpdatePair = resetStatus(status, semantic, editCoordinate, readCoordinates);

                  if (semanticUpdatePair != null) {
                     priorState = semanticUpdatePair.latest.getStatus();
                     semanticUpdatePair.mutable.setCaseSignificanceConceptNid(semanticUpdatePair.latest.getCaseSignificanceConceptNid());
                     semanticUpdatePair.mutable.setDescriptionTypeConceptNid(semanticUpdatePair.latest.getDescriptionTypeConceptNid());
                     semanticUpdatePair.mutable.setLanguageConceptNid(semanticUpdatePair.latest.getLanguageConceptNid());
                     semanticUpdatePair.mutable.setText(semanticUpdatePair.latest.getText());
                     objectToCommit = semantic;
                  }
                  break;
               }
               case STRING: {
                  VersionUpdatePair<StringVersionImpl> semanticUpdatePair = resetStatus(status, semantic, editCoordinate, readCoordinates);

                  if (semanticUpdatePair != null) {
                     priorState = semanticUpdatePair.latest.getStatus();
                     semanticUpdatePair.mutable.setString(semanticUpdatePair.latest.getString());
                     objectToCommit = semantic;
                  }

                  break;
               }
               case DYNAMIC: {
                  VersionUpdatePair<DynamicImpl> semanticUpdatePair = resetStatus(status, semantic, editCoordinate, readCoordinates);

                  if (semanticUpdatePair != null) {
                     priorState = semanticUpdatePair.latest.getStatus();
                     semanticUpdatePair.mutable.setData(semanticUpdatePair.latest.getData());
                     objectToCommit = semantic;
                  }
                  break;
               }
               case COMPONENT_NID: {
                  VersionUpdatePair<ComponentNidVersionImpl> semanticUpdatePair = resetStatus(status, semantic, editCoordinate, readCoordinates);

                  if (semanticUpdatePair != null) {
                     priorState = semanticUpdatePair.latest.getStatus();
                     semanticUpdatePair.mutable.setComponentNid(semanticUpdatePair.latest.getComponentNid());
                     objectToCommit = semantic;
                  }
                  break;
               }
               case LOGIC_GRAPH: {
                  VersionUpdatePair<LogicGraphVersionImpl> semanticUpdatePair = resetStatus(status, semantic, editCoordinate, readCoordinates);

                  if (semanticUpdatePair != null) {
                     priorState = semanticUpdatePair.latest.getStatus();
                     semanticUpdatePair.mutable.setGraphData(semanticUpdatePair.latest.getGraphData());
                     objectToCommit = semantic;
                  }
                  break;
               }
               case LONG: {
                  VersionUpdatePair<LongVersionImpl> semanticUpdatePair = resetStatus(status, semantic, editCoordinate, readCoordinates);

                  if (semanticUpdatePair != null) {
                     priorState = semanticUpdatePair.latest.getStatus();
                     semanticUpdatePair.mutable.setLongValue(semanticUpdatePair.latest.getLongValue());
                     objectToCommit = semantic;
                  }
                  break;
               }
               case MEMBER:
                  VersionUpdatePair<VersionImpl> semanticUpdatePair = resetStatus(status, semantic, editCoordinate, readCoordinates);

                  if (semanticUpdatePair != null) {
                     priorState = semanticUpdatePair.latest.getStatus();
                     objectToCommit = semantic;
                  }
                  break;
               case UNKNOWN:
               default:
                  String detail = semantic.getVersionType() + " (UUID=" + semantic.getPrimordialUuid() + ", SEMANTIC NID=" + semantic.getNid() + ", REF COMP NID="
                        + semantic.getReferencedComponentNid() + ")";

                  throw new Exception("Unsupported semantic of type " + detail + "from id " + componentToModify);
            }
            break;
         }

         case UNKNOWN:
         default:
            throw new Exception("Could not locate component '" + componentToModify + "' of unexpected type " + type + " to change its state");
      }

      if (objectToCommit != null) {
         LOG.debug("Built updated version of " + type + " " + componentToModify + "<" + nid + ">" + " with state changed (from " + priorState + " to " + status + ")");
      } else {
         LOG.debug("No need to commit update of " + type + " " + componentToModify + "<" + nid + ">" + " with unchanged state (" + status + ")");
      }

      return Optional.ofNullable(objectToCommit);
   }

   /**
    * To string.
    *
    * @param version toString for StampedVersion
    * @return the string
    */
   public static String toString(StampedVersion version) {
      return version.getClass()
                    .getSimpleName() + " STAMP=" + version.getStampSequence() + "{state=" + version.getStatus() +
                                       ", time=" + version.getTime() + ", author=" + version.getAuthorNid() +
                                       ", module=" + version.getModuleNid() + ", path=" +
                                       version.getPathNid() + "}";
   }
   
   /**
    * Get isA children of a concept.  Does not return the requested concept in any circumstance.
    * @param conceptNid The concept to look at
    * @param recursive recurse down from the concept
    * @param leafOnly only return leaf nodes
    * @param stamp - optional - defaults to system default if not provided.
    * @return the set of concept sequence ids that represent the children
    */
   public static Set<Integer> getAllChildrenOfConcept(int conceptNid, boolean recursive, boolean leafOnly, StampCoordinate stamp) {
      
      TaxonomySnapshotService tss = Get.taxonomyService().getSnapshot(
            new ManifoldCoordinateImpl((stamp == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate() : stamp),
                  LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate()));
      
      Set<Integer> temp = getAllChildrenOfConcept(new HashSet<Integer>(), conceptNid, recursive, leafOnly, tss);
      if (leafOnly && temp.size() == 1) {
         temp.remove(conceptNid);
      }
      return temp;
   }
   
   /**
    * Recursively get Is a children of a concept.  May inadvertently return the requested starting sequence when leafOnly is true, and 
    * there are no children.
    */
   private static Set<Integer> getAllChildrenOfConcept(Set<Integer> handledConceptNids, int conceptNid, boolean recursive, boolean leafOnly, TaxonomySnapshotService tss) {
      Set<Integer> results = new HashSet<>();

      // This both prevents infinite recursion and avoids processing or returning of duplicates
      if (handledConceptNids.contains(conceptNid)) {
         return results;
      }

      AtomicInteger count = new AtomicInteger();
      IntStream children = Arrays.stream(tss.getTaxonomyChildConceptNids(conceptNid));

      children.forEach((conSequence) -> {
         count.getAndIncrement();
         if (leafOnly) {
            Set<Integer> temp = getAllChildrenOfConcept(handledConceptNids, conSequence, recursive, leafOnly, tss);

            if (recursive) {
               results.addAll(temp);
            } else {
               temp.remove(conSequence); // remove itself
               if (temp.size() == 0) {
                  // This is a leaf node. Add it to results.
                  results.add(conSequence);
               }
            }
         } else {
            results.add(conSequence);
            if (recursive) {
               results.addAll(getAllChildrenOfConcept(handledConceptNids, conSequence, recursive, leafOnly, tss));
            }
         }
      });

      if (leafOnly && count.get() == 0) {
         results.add(conceptNid);
      }
      handledConceptNids.add(conceptNid);
      return results;
   }


   /**
    * Convenience method to return sequences of a distinct set of modules in
    * which versions of an Chronology have been defined.
    *
    * @param chronology The Chronology
    * @return sequences of a distinct set of modules in which versions of an
    * Chronology have been defined
    */
   public static Set<Integer> getAllModuleSequences(Chronology chronology) {
      final Set<Integer> moduleSequences = new HashSet<>();

      for (final StampedVersion version: chronology.getVersionList()) {
         moduleSequences.add(version.getModuleNid());
      }

      return Collections.unmodifiableSet(moduleSequences);
   }
   
   /**
    * A convenience method to determine if a particular component has 0 or 1 annotations of a particular type.  If there is more than one 
    * annotation of a particular type, this method will throw a runtime exception.
    * @param componentNid - the component to check for the assemblage
    * @param assemblageConceptId - the assemblage type you are interested in
    * @return the annotation, if present
    */
   public static Optional<SemanticChronology> getAnnotationSemantic(int componentNid, int assemblageConceptId) 
   {
      Set<SemanticChronology> semanticSet= Get.assemblageService()
            .getSemanticChronologyStreamForComponentFromAssemblage(componentNid, assemblageConceptId).collect(Collectors.toSet());
      switch(semanticSet.size()) 
      {
         case 0:
            return Optional.empty();
         case 1:
            return Optional.of(semanticSet.iterator().next());
            default:
               LOG.fatal("Component " + componentNid + " has " + semanticSet.size() + " annotations of type " + 
                     Get.conceptDescriptionText(assemblageConceptId) + " (should only have zero or 1) - returning arbitrary result!");
               return Optional.of(semanticSet.iterator().next());
      }
   }


   /**
    * Gets the annotation string value.
    *
    * @param componentId the component id
    * @param assemblageConceptId the assemblage concept id
    * @param stamp the stamp
    * @return the annotation string value
    */
   public static Optional<String> getAnnotationStringValue(int componentId,
         int assemblageConceptId,
         StampCoordinate stamp) {
      try {
         final UUID assemblageConceptUuid = Get.identifierService()
                                                         .getUuidPrimordialForNid(assemblageConceptId);

         final int               componentNid = componentId;
         final ArrayList<String> values       = new ArrayList<>(1);
         final int assemblageConceptSequence = Get.identifierService().getNidForUuids(assemblageConceptUuid);

         Get.assemblageService()
            .getSnapshot(SemanticVersion.class,
                (stamp == null) ? Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate() : stamp)
            .getLatestSemanticVersionsForComponentFromAssemblage(componentNid, assemblageConceptSequence)
            .forEach(latestSemantic -> {
                   if (latestSemantic.get()
                                   .getChronology()
                                   .getVersionType() == VersionType.STRING) {
                      values.add(((StringVersionImpl) latestSemantic.get()).getString());
                   } else if (latestSemantic.get()
                                          .getChronology()
                                          .getVersionType() == VersionType.COMPONENT_NID) {
                      values.add(((ComponentNidVersionImpl) latestSemantic.get()).getComponentNid() + "");
                   } else if (latestSemantic.get()
                                          .getChronology()
                                          .getVersionType() == VersionType.LONG) {
                      values.add(((LongVersionImpl) latestSemantic.get()).getLongValue() + "");
                   } else if (latestSemantic.get()
                                          .getChronology()
                                          .getVersionType() == VersionType.DYNAMIC) {
                      final DynamicData[] data = ((DynamicImpl) latestSemantic.get()).getData();

                      if (data.length > 0) {
                         LOG.warn(
                             "Found multiple (" + data.length + ") dynamic data fields in semantic " +
                             latestSemantic.get().getNid() + " of assemblage type " + assemblageConceptUuid +
                             " on component " + Get.identifierService().getUuidPrimordialForNid(
                                 componentNid));
                      }

                      values.add(data[0].dataToString());
                   }
                });

         if (values.size() > 1) {
            LOG.warn(
                "Found multiple (" + values.size() + ") " + assemblageConceptUuid +
                " annotation semantics on component " + Get.identifierService().getUuidPrimordialForNid(
                    componentNid) + ". Using first value \"" + values.get(0) + "\".");
         }

         if (values.size() > 0) {
            return Optional.of(values.get(0));
         }
      } catch (final Exception e) {
         LOG.error(
             "Unexpected error trying to find " + assemblageConceptId + " annotation semantic on component " +
             componentId,
             e);
      }

      return Optional.empty();
   }

   /**
    * Checks if concept fully defined.
    *
    * @param lgs The LogicGraphVersion containing the logic graph data
    * @return true if the corresponding concept is fully defined, otherwise returns false (for primitive concepts)
    *
    * Things that are defined with at least one SUFFICIENT_SET node are defined.
    * Things that are defined without any SUFFICIENT_SET nodes are primitive.
    */
   public static boolean isConceptFullyDefined(LogicGraphVersion lgs) {
      return lgs.getLogicalExpression()
                .contains(NodeSemantic.SUFFICIENT_SET);
   }

   /**
    * Return true for fully defined, false for primitive, or empty for unknown, on the standard logic coordinates / standard development path.
    *
    * @param conceptNid the concept nid
    * @param stated the stated
    * @return the optional
    */
   public static Optional<Boolean> isConceptFullyDefined(int conceptNid, boolean stated) {
      final Optional<SemanticChronology> semantic = Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(conceptNid,
            (stated ? LogicCoordinates.getStandardElProfile().getStatedAssemblageNid() : LogicCoordinates.getStandardElProfile().getInferredAssemblageNid())
               ).findAny();

      if (semantic.isPresent()) {
         final LatestVersion<LogicGraphVersion> sv = ((SemanticChronology) semantic.get()).getLatestVersion(StampCoordinates.getDevelopmentLatest());

         if (sv.isPresent()) {
            return Optional.of(isConceptFullyDefined((LogicGraphVersion) sv.get()));
         }
      }

      return Optional.empty();
   }
   
   /**
    * Find the CODE(s) for a component (if it has one) {@link MetaData#CODE____SOLOR}
    *
    * @param componentNid
    * @param stamp - optional - if not provided uses default from config
    * service
    * @return the codes, if found, or empty (will not return null)
    */
   @SuppressWarnings("rawtypes")
   public static List<String> getCodes(int componentNid, StampCoordinate stamp) {
      try 
      {
         ArrayList<String> codes = new ArrayList<>(1);
         Get.assemblageService().getSnapshot(SemanticVersion.class, stamp == null ? 
               Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate() : stamp)
               .getLatestSemanticVersionsForComponentFromAssemblage(componentNid,
                     MetaData.CODE____SOLOR.getNid()).forEach(latestSemantic ->
                     {
                        //expected path
                        if (latestSemantic.get().getChronology().getVersionType() == VersionType.STRING)
                        {
                           codes.add(((StringVersion)latestSemantic.get()).getString());
                        }
                        //Data model bug path (can go away, after bug is fixed)
                        else if (latestSemantic.get().getChronology().getVersionType() == VersionType.DYNAMIC)
                        {
                           codes.add(((DynamicVersion)latestSemantic.get()).getData()[0].dataToString());
                        }
                     });
         return codes;
      }
      catch (Exception e) 
      {
         LOG.error("Unexpected error trying to find CODE for nid " + componentNid, e);
         throw new RuntimeException(e);
      }
   }
   
   /**
    * If the passed in value is a {@link UUID}, calls {@link ConceptService#getOptionalConcept(int)} after converting the UUID to nid. Next, if no hit, if the
    * passed in value is parseable as a int < 0 (a nid), calls {@link ConceptService#getOptionalConcept(int)} Next, if no hit, if the passed in value is parseable
    * as a long, and is a valid SCTID (checksum is valid) - treats it as a SCTID and attempts to look up the SCTID in the lucene index.
    * 
    * @param identifier - the value to search for
    * @return The concept, if found, which carries the specified identifier.
    */
   public static Optional<? extends ConceptChronology> getConceptForUnknownIdentifier(String identifier) {
      LOG.debug("Concept Chronology lookup by string '{}'", identifier);

      if (StringUtils.isBlank(identifier)) {
         return Optional.empty();
      }
      String localIdentifier = identifier.trim();

      Optional<UUID> uuid = UUIDUtil.getUUID(localIdentifier);
      if (uuid.isPresent()) {
         return Get.conceptService().getOptionalConcept(uuid.get());
      }

      // if it is a negative integer, assume nid
      Optional<Integer> nid = NumericUtils.getNID(localIdentifier);
      if (nid.isPresent()) {
         return Get.conceptService().getOptionalConcept(nid.get());
      }

      if (SctId.isValidSctId(localIdentifier)) {

         IndexSemanticQueryService si = LookupService.get().getService(IndexSemanticQueryService.class);
         if (si != null) {
            // force the prefix algorithm, and add a trailing space - quickest way to do an exact-match type of search
            List<SearchResult> result = si.query(localIdentifier + " ", true, new int[] { MetaData.SCTID____SOLOR.getNid() }, null, null, 5, Long.MIN_VALUE);
            if (result.size() > 0) {
               int componentNid = Get.assemblageService().getSemanticChronology(result.get(0).getNid()).getReferencedComponentNid();
               if (Get.identifierService().getObjectTypeForComponent(componentNid) == IsaacObjectType.CONCEPT) {
                  return Get.conceptService().getOptionalConcept(componentNid);
               } else {
                  LOG.warn("Passed in SCTID is not a Concept ID!");
                  return Optional.empty();
               }
            }
         } else {
            LOG.warn("Semantic Index not available - can't lookup SCTID");
         }
      } 
      return Optional.empty();
   }

   /**
    * Gets the concept snapshot.
    *
    * @param conceptNidOrSequence the concept nid or sequence
    * @param stampCoord - optional - what stamp to use when returning the ConceptSnapshot (defaults to user prefs)
    * @param langCoord - optional - what lang coord to use when returning the ConceptSnapshot (defaults to user prefs)
    * @return the ConceptSnapshot, or an optional that indicates empty, if the identifier was invalid, or if the concept didn't
    * have a version available on the specified manifoldCoordinate
    */
   public static Optional<ConceptSnapshot> getConceptSnapshot(int conceptNidOrSequence, StampCoordinate stampCoord, LanguageCoordinate langCoord) {
      final Optional<? extends ConceptChronology> c = Get.conceptService().getOptionalConcept(conceptNidOrSequence);

      if (c.isPresent()) {
         try {
               return Optional.of(Get.conceptService().getSnapshot(new ManifoldCoordinateImpl(
                     stampCoord == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate() : stampCoord,
                     langCoord == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getLanguageCoordinate() : langCoord))
                        .getConceptSnapshot(c.get().getNid()));
         } catch (final Exception e) {
            // TODO DAN defaultConceptSnapshotService APIs are currently broken, provide no means of detecting if a concept doesn't exist on a given coordinate
            // See slack convo https://informatics-arch.slack.com/archives/dev-isaac/p1440568057000512
            //need to retest in the new env
         
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   /**
    * Gets the concept snapshot.  Call {@link #getConceptSnapshot(int, StampCoordinate, LanguageCoordinate)}
    *
    * @param conceptUUID the concept UUID
    * @param stampCoord the stamp to utilize
    * @param langCoord  the language to utilize
    * @return the ConceptSnapshot, or an optional that indicates empty, if the identifier was invalid, or if the concept didn't
    *   have a version available on the specified stampCoord
    */
   public static Optional<ConceptSnapshot> getConceptSnapshot(UUID conceptUUID, StampCoordinate stampCoord, LanguageCoordinate langCoord) {
      return getConceptSnapshot(Get.identifierService().getNidForUuids(conceptUUID), stampCoord, langCoord);
   }


   /**
    * Determine if a particular description semantic is flagged as preferred IN
    * ANY LANGUAGE. Returns false if there is no acceptability semantic.
    *
    * @param descriptionSemanticNid the description semantic nid
    * @param stamp - optional - if not provided, uses default from config service
    * @return true, if description is preferred in some language
    * @throws RuntimeException If there is unexpected data (incorrectly) attached to the semantic
    */
   public static boolean isDescriptionPreferred(int descriptionSemanticNid,
         StampCoordinate stamp)
            throws RuntimeException {
      final AtomicReference<Boolean> answer = new AtomicReference<>();

      // Ignore the language annotation... treat preferred in any language as good enough for our purpose here...
      Get.assemblageService()
         .getSemanticChronologyStreamForComponent(descriptionSemanticNid)
         .forEach(nestedSemantic -> {
                if (nestedSemantic.getVersionType() == VersionType.COMPONENT_NID) {
                  final LatestVersion<ComponentNidVersion> latest = ((SemanticChronology) nestedSemantic)
                        .getLatestVersion(
                              (stamp == null) ? Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate(): stamp);

                   if (latest.isPresent()) {
                      if (latest.get()
                                .getComponentNid() == MetaData.PREFERRED____SOLOR.getNid()) {
                         if ((answer.get() != null) && (answer.get() != true)) {
                            throw new RuntimeException("contradictory annotations about preferred status!");
                         }

                         answer.set(true);
                      } else if (latest.get()
                                       .getComponentNid() == MetaData.ACCEPTABLE____SOLOR.getNid()) {
                         if ((answer.get() != null) && (answer.get() != false)) {
                            throw new RuntimeException("contradictory annotations about preferred status!");
                         }

                         answer.set(false);
                      } else {
                         throw new RuntimeException("Unexpected component nid!");
                      }
                   }
                }
             });

      if (answer.get() == null) {
         LOG.warn("Description nid {} does not have an acceptability semantic!", descriptionSemanticNid);
         return false;
      }

      return answer.get();
   }

   /**
    * Convenience method to extract the latest version of descriptions of the
    * requested type.
    *
    * @param conceptNid The concept to read descriptions for
    * @param descriptionType expected to be one of
    * {@link MetaData#REGULAR_NAME____SOLOR} or
    * {@link MetaData#FULLY_QUALIFIED_NAME____SOLOR} or
    * {@link MetaData#DEFINITION____SOLOR}
    * @param stamp - optional - if not provided gets the default from the config service
    * @return the descriptions - may be empty, will not be null
    */
   public static List<DescriptionVersion> getDescriptionsOfType(int conceptNid,
         ConceptSpecification descriptionType,
         StampCoordinate stamp) {
      final ArrayList<DescriptionVersion> results = new ArrayList<>();

      Get.assemblageService()
         .getSemanticChronologyStreamForComponent(conceptNid)
         .forEach(descriptionC -> {
                if (descriptionC.getVersionType() == VersionType.DESCRIPTION) {
                   final LatestVersion<DescriptionVersion> latest = ((SemanticChronology) descriptionC).getLatestVersion((stamp == null)? 
                         Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate() : stamp);

                   if (latest.isPresent()) {
                      final DescriptionVersion ds = latest.get();

                      if (ds.getDescriptionTypeConceptNid() == descriptionType.getNid()) {
                         results.add(ds);
                      }
                   }
                }
             });
      return results;
   }
   
   /**
    * Get a list of all "extended" description types - the children of {@link MetaData#DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR}
    * @param stamp - optional - defaults to system default if not provided
    * @return the list of extended description types
    * @throws IOException
    */
   public static List<SimpleDisplayConcept> getExtendedDescriptionTypes(StampCoordinate stamp) throws IOException {
      Set<Integer> extendedDescriptionTypes;
      ArrayList<SimpleDisplayConcept> temp = new ArrayList<>();
      extendedDescriptionTypes = Frills.getAllChildrenOfConcept(MetaData.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getNid(), true, true, stamp);
      for (Integer seq : extendedDescriptionTypes) {
         temp.add(new SimpleDisplayConcept(seq));
      }
      Collections.sort(temp);
      return temp;
   }

   /**
    * Gets the id info.
    *
    * @param id int identifier
    * @return a IdInfo, the toString() for which will display known identifiers and descriptions associated with the passed id
    *
    * This method should only be used for logging. The returned data structure is not meant to be parsed.
    */
   private static IdInfo getIdInfo(int id) {
      return getIdInfo(Integer.toString(id));
   }

   /**
    * @param id String identifier may parse to int NID, int sequence or UUID
    * 
    * Calls {@link #getIdInfo(String, StampCoordinate, LanguageCoordinate)} with development latest and US English FSN
    * @return a IdInfo, the toString() for which will display known identifiers and descriptions associated with the passed id
    * 
    * This method should only be used for logging. The returned data structure is not meant to be parsed.
    */
   private static IdInfo getIdInfo(String id) {
      return getIdInfo(
          id,
          StampCoordinates.getDevelopmentLatest(),
          LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate());
   }
   /**
    * @param id int identifier
    * @param sc
    * @param lc
    * calls {@link #getIdInfo(int, StampCoordinate, LanguageCoordinate)}
    * @return a IdInfo, the toString() for which will display known identifiers and descriptions associated with the passed id
    * 
    * This method should only be used for logging. The returned data structure is not meant to be parsed.
    */
   private static IdInfo getIdInfo(int id, StampCoordinate sc, LanguageCoordinate lc) {
      return getIdInfo(Integer.toString(id), sc, lc);
   }

   /**
    * 
    * @param id String identifier may parse to int NID, or UUID
    * @param sc The stamp coordinate to use, when looking up descriptions - uses dev latest if not passed
    * @param lc the language coordinate to use, when looking up descriptions.  Uses us english, if not provided.
    * @return a IdInfo, the toString() for which will display known identifiers and descriptions associated with the passed id
    * 
    * This method should only be used for logging. The returned data structure is not meant to be parsed.
    */
   private static IdInfo getIdInfo(String id, StampCoordinate sc, LanguageCoordinate lc) {
      Map<String, Object> idInfo = new HashMap<>();

      Long sctId = null;
      Integer nid = null;
      UUID[] uuids = null;
      IsaacObjectType typeOfPassedId = null;
      
      if (sc == null)
      {
         sc = StampCoordinates.getDevelopmentLatest();
      }
      
      if (lc == null)
      {
         lc = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
      }

      try {
         Optional<Integer> intId = NumericUtils.getInt(id);
         if (intId.isPresent())
         {
            if (intId.get() < 0) {
               nid = intId.get();
            }

            if (nid != null) {
               typeOfPassedId = Get.identifierService().getObjectTypeForComponent(nid);
               uuids = Get.identifierService().getUuidArrayForNid(nid);
            }
         }
         else
         {
            Optional<UUID> uuidId = UUIDUtil.getUUID(id);
            if (uuidId.isPresent())
            {
               // id interpreted as the id of either a semantic or a concept
               nid = Get.identifierService().getNidForUuids(uuidId.get());
               typeOfPassedId = Get.identifierService().getObjectTypeForComponent(nid);
            }
         }

         if (nid != null) {
            idInfo.put("DESC", Get.conceptService().getSnapshot(new ManifoldCoordinateImpl(sc, lc)).conceptDescriptionText(nid));
            if (typeOfPassedId == IsaacObjectType.CONCEPT) {
               Optional<Long> optSctId = Frills.getSctId(nid, sc);
               if (optSctId.isPresent()) {
                  sctId = optSctId.get();
                  
                  idInfo.put("SCTID", sctId);
               }
            }
         }
      } catch (Exception e) {
         LOG.warn("Problem getting idInfo for \"{}\". Caught {}", e.getClass().getName(), e.getLocalizedMessage());
      }
      idInfo.put("PASSED_ID", id);
      idInfo.put("NID", nid);
      idInfo.put("UUIDs", Arrays.toString(uuids));
      idInfo.put("TYPE", typeOfPassedId);

      return new IdInfo(idInfo);
   }

   /**
    * Gets the inferred definition chronology.
    *
    * @param conceptId either a concept nid or sequence.
    * @param logicCoordinate LogicCoordinate.
    * @return the inferred definition chronology for the specified concept
    * according to the default logic coordinate.
    */
   public static Optional<SemanticChronology> getInferredDefinitionChronology(int conceptId,
         LogicCoordinate logicCoordinate) {
      return Get.assemblageService()
                .getSemanticChronologyStreamForComponentFromAssemblage(conceptId, logicCoordinate.getInferredAssemblageNid())
                .findAny();
   }

   /**
    * Gets the logic graph chronology.
    *
    * @param id The int sequence or NID of the Concept for which the logic graph is requested
    * @param stated boolean indicating stated vs inferred definition chronology should be used
    * @return An Optional containing a LogicGraphVersion SemanticChronology
    */
   public static Optional<SemanticChronology> getLogicGraphChronology(int id,
         boolean stated) {
      LOG.debug("Getting {} logic graph chronology for {}", (stated ? "stated"
            : "inferred"), Optional.ofNullable(Frills.getIdInfo(id)));

      final Optional<SemanticChronology> defChronologyOptional =
         stated ? Get.statedDefinitionChronology(
             id)
                : Get.inferredDefinitionChronology(id);

      if (defChronologyOptional.isPresent()) {
         LOG.debug("Got {} logic graph chronology for {}", (stated ? "stated"
               : "inferred"), Optional.ofNullable(Frills.getIdInfo(id)));

         final SemanticChronology semanticChronology =
            (SemanticChronology) defChronologyOptional.get();

         return Optional.of(semanticChronology);
      } else {
         LOG.warn("NO {} logic graph chronology for {}", (stated ? "stated"
               : "inferred"), Optional.ofNullable(Frills.getIdInfo(id)));
         return Optional.empty();
      }
   }

   /**
    * Gets the logic graph chronology.
    *
    * @param id The int sequence or NID of the Concept for which the logic graph is requested
    * @param stated boolean indicating stated vs inferred definition chronology should be used
    * @param stampCoordinate The StampCoordinate for which the logic graph is requested
    * @param languageCoordinate The LanguageCoordinate for which the logic graph is requested
    * @param logicCoordinate the LogicCoordinate for which the logic graph is requested
    * @return An Optional containing a LogicGraphVersion SemanticChronology
    */
   public static Optional<SemanticChronology> getLogicGraphChronology(int id,
         boolean stated,
         StampCoordinate stampCoordinate,
         LanguageCoordinate languageCoordinate,
         LogicCoordinate logicCoordinate) {
      LOG.debug("Getting {} logic graph chronology for {}", (stated ? "stated"
            : "inferred"), Optional.ofNullable(Frills.getIdInfo(id, stampCoordinate, languageCoordinate)));

      final Optional<SemanticChronology> defChronologyOptional = stated ? getStatedDefinitionChronology(
                                                                                              id,
                                                                                                    logicCoordinate)
            : getInferredDefinitionChronology(id, logicCoordinate);

      if (defChronologyOptional.isPresent()) {
         LOG.debug("Got {} logic graph chronology for {}", (stated ? "stated"
               : "inferred"), Optional.ofNullable(Frills.getIdInfo(id, stampCoordinate, languageCoordinate)));

         final SemanticChronology semanticChronology =
            (SemanticChronology) defChronologyOptional.get();

         return Optional.of(semanticChronology);
      } else {
         LOG.warn("NO {} logic graph chronology for {}", (stated ? "stated"
               : "inferred"), Optional.ofNullable(Frills.getIdInfo(id, stampCoordinate, languageCoordinate)));
         return Optional.empty();
      }
   }

   /**
    * Gets the logic graph version.
    *
    * @param logicGraphSemanticChronology The SemanticChronology chronology for which the logic graph version is requested
    * @param stampCoordinate StampCoordinate to be used for selecting latest version
    * @return An Optional containing a LogicGraphVersion SemanticChronology
    */
   public static LatestVersion<LogicGraphVersion> getLogicGraphVersion(
         SemanticChronology logicGraphSemanticChronology,
         StampCoordinate stampCoordinate) {
      LOG.debug("Getting logic graph semantic for {}",
          Optional.ofNullable(Frills.getIdInfo(logicGraphSemanticChronology.getReferencedComponentNid())));

      final LatestVersion<LogicGraphVersion> latest = ((SemanticChronology) logicGraphSemanticChronology).getLatestVersion(
                                                                stampCoordinate);

      if (latest.isPresent()) {
         LOG.debug("Got logic graph semantic for {}",
             Optional.ofNullable(Frills.getIdInfo(logicGraphSemanticChronology.getReferencedComponentNid())));
      } else {
         LOG.warn("NO logic graph semantic for {}",
             Optional.ofNullable(Frills.getIdInfo(logicGraphSemanticChronology.getReferencedComponentNid())));
      }

      return latest;
   }

   /**
    * Determine if Chronology has nested semantics.
    *
    * @param chronology the chronology
    * @return true if there is a nested semantic, false otherwise
    */
   public static boolean hasNestedSemantic(Chronology chronology) {
      return !chronology.getSemanticChronologyList()
                        .isEmpty();
   }
   
   /**
    * Convenience method to find the nearest concept related to a semantic.  Recursively walks referenced components until it finds a concept.
    * @param nid 
    * @return the nearest concept sequence, or -1, if no concept can be found.
    */
   public static Optional<Integer> getNearestConcept(int nid)
   {
      Optional<? extends Chronology> c = Get.identifiedObjectService().getChronology(nid);
      
      if (c.isPresent())
      {
         if (c.get().getIsaacObjectType() == IsaacObjectType.SEMANTIC)
         {
            return getNearestConcept(((SemanticChronology)c.get()).getReferencedComponentNid());
         }
         else if (c.get().getIsaacObjectType() == IsaacObjectType.CONCEPT)
         {
            return Optional.of(((ConceptChronology)c.get()).getNid());
         }
         else
         {
            LOG.warn("Unexpected object type: " + c.get().getIsaacObjectType());
         }
      }
      return Optional.empty();
   }

   /**
    * Gets the nid for SCTID.
    *
    * @param sctID the sct ID
    * @return the nid for SCTID
    */
   public static Optional<Integer> getNidForSCTID(long sctID) {
      final IndexQueryService si = LookupService.get().getService(IndexSemanticQueryService.class);

      if (si != null) {
         // force the prefix algorithm, and add a trailing space - quickest way to do an exact-match type of search
         final List<SearchResult> result = si.query(sctID + " ",
                                                     true,
                                                     new int[] { MetaData.SCTID____SOLOR.getNid() },
                                                     null,
                                                     null,
                                                     5,
                                                     Long.MIN_VALUE);

         if (result.size() > 0) {
            return Optional.of(Get.assemblageService().getSemanticChronology(result.get(0).getNid()).getReferencedComponentNid());
         }
      } else {
         LOG.warn("Semantic Index not available - can't lookup SCTID");
      }

      return Optional.empty();
   }

   /**
    * Gets the nid for VUID.
    *
    * @param vuID the vu ID
    * @return the nid for VUID
    */
   public static Optional<Integer> getNidForVUID(long vuID) {
      final IndexQueryService si = LookupService.get().getService(IndexQueryService.class, "semantic indexer");

      if (si != null) {
         // force the prefix algorithm, and add a trailing space - quickest way to do an exact-match type of search
         final List<SearchResult> result = si.query(vuID + " ",
                                                     true,
                                                     new int[] { MetaData.VUID____SOLOR.getNid() },
                                                     null, 
                                                     null,
                                                     5,
                                                     Long.MIN_VALUE);

         if (result.size() > 0) {
            return Optional.of(Get.assemblageService()
                                  .getSemanticChronology(result.get(0)
                                        .getNid())
                                  .getReferencedComponentNid());
         }
      } else {
         LOG.warn("Semantic Index not available - can't lookup VUID");
      }

      return Optional.empty();
   }
   
   /**
    * Retrieve the set of integer parent concept nids stored in the logic graph necessary sets
    * 
    * @param logicGraph
    * @return the parents
    */
   public static Set<Integer> getParentConceptNidsFromLogicGraph(LogicGraphVersion logicGraph) {
      Set<Integer> parentConceptSequences = new HashSet<>();
      List<LogicNode> necessarySets = logicGraph.getLogicalExpression().getNodesOfType(NodeSemantic.NECESSARY_SET);
      for (LogicNode necessarySetNode: necessarySets) {
         for (LogicNode childOfNecessarySetNode : necessarySetNode.getChildren()) {
            if (null == childOfNecessarySetNode.getNodeSemantic()) {
                String msg = "Logic graph for concept NID=" + logicGraph.getReferencedComponentNid() + " has child of NecessarySet logic graph node of unexpected type \""
                        + childOfNecessarySetNode.getNodeSemantic() + "\". Expected AndNode or ConceptNode in " + logicGraph;
                LOG.error(msg);
                throw new RuntimeException(msg);
            } else switch (childOfNecessarySetNode.getNodeSemantic()) {
                 case AND:
                     AndNode andNode = (AndNode) childOfNecessarySetNode;
                     for (AbstractLogicNode childOfAndNode : andNode.getChildren()) {
                         if (childOfAndNode.getNodeSemantic() == NodeSemantic.CONCEPT) {
                             if (childOfAndNode instanceof ConceptNodeWithNids) {
                                 ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) childOfAndNode;
                                 parentConceptSequences.add(conceptNode.getConceptNid());
                             } else if (childOfAndNode instanceof ConceptNodeWithUuids) {
                                 ConceptNodeWithUuids conceptNode = (ConceptNodeWithUuids) childOfAndNode;
                                 parentConceptSequences.add(Get.identifierService().getNidForUuids(conceptNode.getConceptUuid()));
                             } else {
                                 // Should never happen
                                 String msg = "Logic graph for concept NID=" + logicGraph.getReferencedComponentNid() + " has child of AndNode logic graph node of unexpected type \""
                                         + childOfAndNode.getClass().getSimpleName() + "\". Expected ConceptNodeWithNids or ConceptNodeWithUuids in " + logicGraph;
                                 LOG.error(msg);
                                 throw new RuntimeException(msg);
                             }
                         }
                     }     break;
                 case CONCEPT:
                     if (childOfNecessarySetNode instanceof ConceptNodeWithNids) {
                         ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) childOfNecessarySetNode;
                         parentConceptSequences.add(conceptNode.getConceptNid());
                     } else if (childOfNecessarySetNode instanceof ConceptNodeWithUuids) {
                         ConceptNodeWithUuids conceptNode = (ConceptNodeWithUuids) childOfNecessarySetNode;
                         parentConceptSequences.add(Get.identifierService().getNidForUuids(conceptNode.getConceptUuid()));
                     } else {
                         // Should never happen
                         String msg = "Logic graph for concept NID=" + logicGraph.getReferencedComponentNid() + " has child of NecessarySet logic graph node of unexpected type \""
                                 + childOfNecessarySetNode.getClass().getSimpleName() + "\". Expected ConceptNodeWithNids or ConceptNodeWithUuids in " + logicGraph;
                         LOG.error(msg);
                         throw new RuntimeException(msg);
                     }     break;
                 default:
                     String msg = "Logic graph for concept NID=" + logicGraph.getReferencedComponentNid() + " has child of NecessarySet logic graph node of unexpected type \""
                             + childOfNecessarySetNode.getNodeSemantic() + "\". Expected AndNode or ConceptNode in " + logicGraph;
                     LOG.error(msg);
                     throw new RuntimeException(msg);
             }
         }
      }

      return parentConceptSequences;
   }

   /**
    * Find the SCTID for a component (if it has one).
    *
    * @param componentNid the component nid
    * @param stamp - optional - if not provided uses default from config
    * service
    * @return the id, if found, or empty (will not return null)
    */
   public static Optional<Long> getSctId(int componentNid, StampCoordinate stamp) {
      try {
         final List<LatestVersion<StringVersionImpl>> semantic = Get.assemblageService()
               .getSnapshot(StringVersionImpl.class, (stamp == null) ? Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate() : stamp)
               .getLatestSemanticVersionsForComponentFromAssemblage(componentNid, MetaData.SCTID____SOLOR.getNid());

         if (semantic.size() > 0 && semantic.get(0).isPresent()) {
            return Optional.of(Long.parseLong(semantic.get(0).get().getString()));
         }
      } catch (final Exception e) {
         LOG.error("Unexpected error trying to find SCTID for nid " + componentNid, e);
      }

      return Optional.empty();
   }
   

   /**
    * Get all semantics for a specified component of specified assemblages restricted by VersionType
    * 
    * @param componentNid
    *           - referenced component nid of requested semantics
    * @param allowedAssemblageNids
    *           - set of concept sequences of allowed assemblages
    * @param typesToExclude
    *           - set of VersionType restrictions
    * @return the filtered stream of semantics
    */
   public static Stream<SemanticChronology> getSemanticForComponentFromAssemblagesFilteredBySemanticType(int componentNid,
         Set<Integer> allowedAssemblageNids, Set<VersionType> typesToExclude) {
      NidSet semanticSequences = Get.assemblageService().getSemanticNidsForComponentFromAssemblages(componentNid, allowedAssemblageNids);
      if (typesToExclude == null || typesToExclude.isEmpty()) {
         return semanticSequences.stream().mapToObj((int semanticSequence) -> Get.assemblageService().getSemanticChronology(semanticSequence));
      } else {
         final ArrayList<SemanticChronology> filteredList = new ArrayList<>();
         for (PrimitiveIterator.OfInt it = semanticSequences.getIntIterator(); it.hasNext();) {
            SemanticChronology chronology = Get.assemblageService().getSemanticChronology(it.nextInt());
            boolean exclude = false;
            for (VersionType type : typesToExclude) {
               if (chronology.getVersionType() == type) {
                  exclude = true;
                  break;
               }
            }

            if (!exclude) {
               filteredList.add(chronology);
            }
         }

         return filteredList.stream();
      }
   }

   /**
    * Construct a stamp coordinate from an existing stamp coordinate, and the path from the edit coordinate, ensuring that the returned
    * stamp coordinate includes the module edit coordinate.
    *
    * @param stampCoordinate - optional - used to fill in the stamp details not available from the edit coordinate.  If not provided,
    * uses the system defaults.
    * @param editCoordinate - ensure that the returned stamp coordinate includes the module and path from this edit coordinate.
    * @return a new stamp coordinate
    */
   public static StampCoordinate getStampCoordinateFromEditCoordinate(StampCoordinate stampCoordinate,
         EditCoordinate editCoordinate) {
      if (stampCoordinate == null) {
         stampCoordinate = Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate();
      }

      final StampPosition stampPosition = new StampPositionImpl(
                                              stampCoordinate.getStampPosition().getTime(),
                                                    editCoordinate.getPathNid());
      final StampCoordinateImpl temp = new StampCoordinateImpl(
                                           stampCoordinate.getStampPrecedence(),
                                                 stampPosition,
                                                 stampCoordinate.getModuleNids(),
                                                 stampCoordinate.getAllowedStates());

      if (temp.getModuleNids()
              .size() > 0) {
         temp.getModuleNids()
             .add(editCoordinate.getModuleNid());
      }

      return temp;
   }

   /**
    * Gets the stamp coordinate from stamp.
    *
    * @param stamp Stamp from which to generate StampCoordinate
    * @return StampCoordinate corresponding to Stamp values
    *
    * StampPrecedence set to StampPrecedence.TIME
    *
    * Use StampCoordinate.makeCoordinateAnalog() to customize result
    */
   public static StampCoordinate getStampCoordinateFromStamp(Stamp stamp) {
      return getStampCoordinateFromStamp(stamp, StampPrecedence.TIME);
   }

   /**
    * Gets the stamp coordinate from stamp.
    *
    * @param stamp Stamp from which to generate StampCoordinate
    * @param precedence Precedence to assign StampCoordinate
    * @return StampCoordinate corresponding to Stamp values
    *
    * Use StampCoordinate.makeCoordinateAnalog() to customize result
    */
   public static StampCoordinate getStampCoordinateFromStamp(Stamp stamp, StampPrecedence precedence) {
      final StampPosition stampPosition = new StampPositionImpl(stamp.getTime(), stamp.getPathNid());
      final StampCoordinate stampCoordinate = new StampCoordinateImpl(
                                                  precedence,
                                                        stampPosition,
                                                        NidSet.of(stamp.getModuleNid()),
                                                        EnumSet.of(stamp.getStatus()));

      LOG.debug("Created StampCoordinate from Stamp: " + stamp + ": " + stampCoordinate);
      return stampCoordinate;
   }

   /**
    * Gets the stamp coordinate from version.
    *
    * @param version StampedVersion from which to generate StampCoordinate
    * @return StampCoordinate corresponding to StampedVersion values
    *
    * StampPrecedence set to StampPrecedence.TIME
    *
    * Use StampCoordinate.makeCoordinateAnalog() to customize result
    */
   public static StampCoordinate getStampCoordinateFromVersion(StampedVersion version) {
      return getStampCoordinateFromVersion(version, StampPrecedence.TIME);
   }

   /**
    * Gets the stamp coordinate from version.
    *
    * @param version StampedVersion from which to generate StampCoordinate
    * @param precedence the precedence
    * @return StampCoordinate corresponding to StampedVersion values
    *
    * Use StampCoordinate.makeCoordinateAnalog() to customize result
    */
   public static StampCoordinate getStampCoordinateFromVersion(StampedVersion version, StampPrecedence precedence) {
      final StampPosition stampPosition = new StampPositionImpl(version.getTime(), version.getPathNid());
      final StampCoordinate stampCoordinate = new StampCoordinateImpl(
                                                  precedence,
                                                        stampPosition,
                                                        NidSet.of(version.getModuleNid()),
                                                        EnumSet.of(version.getStatus()));

      LOG.debug("Created StampCoordinate from StampedVersion: " + toString(version) + ": " + stampCoordinate);
      return stampCoordinate;
   }

   /**
    * Gets the stated definition chronology.
    *
    * @param conceptId either a concept nid or sequence.
    * @param logicCoordinate LogicCoordinate.
    * @return the stated definition chronology for the specified concept
    * according to the default logic coordinate.
    */
   public static Optional<SemanticChronology> getStatedDefinitionChronology(int conceptId,
         LogicCoordinate logicCoordinate) {
      return Get.assemblageService()
                .getSemanticChronologyStreamForComponentFromAssemblage(conceptId, logicCoordinate.getStatedAssemblageNid())
                .findAny();
   }
   
   /**
    * Returns the set of terminology types (which are concepts directly under {@link MetaData#MODULE____SOLOR} for any concept in the system as a 
    * set of concept sequences.
    * 
    * Also, if the concept is a child of {@link MetaData#METADATA____SOLOR}, then it will also be marked with the terminology type of 
    * {@link MetaData#SOLOR_MODULE____SOLOR} -even if there is no concept version that exists using the MetaData#SOLOR_MODULE____SOLOR} module - this gives 
    * an easy way to identify "metadata" concepts.
    * 
    * @param oc
    *           - the concept to read modules for
    * @param stamp
    *           - if null, return the modules ignoring coordinates. If not null, only return modules visible on the given coordinate
    * @return the types
    */
   public static HashSet<Integer> getTerminologyTypes(ConceptChronology oc, StampCoordinate stamp) {
      HashSet<Integer> modules = new HashSet<>();
      HashSet<Integer> terminologyTypes = new HashSet<>();
      
      TaxonomySnapshotService tss = Get.taxonomyService().getStatedLatestSnapshot(
            (stamp == null ? StampCoordinates.getDevelopmentLatest().getStampPosition().getStampPathNid() : stamp.getStampPosition().getStampPathNid()),
            (stamp == null ? NidSet.EMPTY : stamp.getModuleNids()),
            (stamp == null ? Status.ACTIVE_ONLY_SET : stamp.getAllowedStates()));

      if (stamp == null) {
         for (int stampSequence : oc.getVersionStampSequences()) {
            modules.add(Get.stampService().getModuleNidForStamp(stampSequence));
         }
         if (tss.isKindOf(oc.getNid(), MetaData.METADATA____SOLOR.getNid())) {
            terminologyTypes.add(MetaData.SOLOR_MODULE____SOLOR.getNid());
         }
      } else {
         oc.getVersionList().stream().filter(version -> {
            return stamp.getAllowedStates().contains(version.getStatus())
                  && (stamp.getModuleNids().size() == 0 ? true : stamp.getModuleNids().contains(version.getModuleNid()));
         }).forEach(version -> {
            modules.add(version.getModuleNid());
         });
         
         if (tss.isKindOf(oc.getNid(), MetaData.METADATA____SOLOR.getNid()))
         {
            terminologyTypes.add(MetaData.SOLOR_MODULE____SOLOR.getNid());
         }
      }

      for (int moduleNid : modules) {
         Integer temp = (MODULE_TO_TERM_TYPE_CACHE.get(moduleNid, mNid -> {
            if (tss.isKindOf(mNid, MetaData.MODULE____SOLOR.getNid())) {
               return findTermTypeConcept(moduleNid, stamp);
            }
            return null;
         }));
         if (temp != null){
            terminologyTypes.add(temp);
         }
      }
      return terminologyTypes;
   }

   /**
    * Gets the version type.
    *
    * @param obj the obj
    * @return the version type
    */
   public static Class<? extends StampedVersion> getVersionType(Chronology obj) {
      switch (obj.getIsaacObjectType()) {
         case SEMANTIC: {
            final SemanticChronology semanticChronology = (SemanticChronology) obj;

            switch (semanticChronology.getVersionType()) {
               case COMPONENT_NID:
                  return ComponentNidVersionImpl.class;

               case DESCRIPTION:
                  return DescriptionVersionImpl.class;

               case DYNAMIC:
                  return DynamicImpl.class;

               case LOGIC_GRAPH:
                  return LogicGraphVersionImpl.class;

               case LONG:
                  return LongVersionImpl.class;

               case STRING:
                  return StringVersionImpl.class;

               case MEMBER:
                  return VersionImpl.class;
               case UNKNOWN:
               default:
                  throw new RuntimeException("Semantic with NID=" + obj.getNid() + " is of unsupported SemanticType " + semanticChronology.getVersionType());
            }
         }

         case CONCEPT:
            return ConceptVersionImpl.class;
         
         case STAMP:
         case STAMP_ALIAS:
         case STAMP_COMMENT:
         case UNKNOWN:

         default:
            throw new RuntimeException("Object with NID=" + obj.getNid() + " is unsupported by this utility: " + obj.getIsaacObjectType());
      }
   }

   /**
    * Gets the version type.
    *
    * @param nid the nid
    * @return the version type
    */
   public static Class<? extends StampedVersion> getVersionType(int nid) {
      final Optional<? extends Chronology> obj = Get.identifiedObjectService().getChronology(nid);

      if (!obj.isPresent()) {
         throw new RuntimeException("No StampedVersion object exists with NID=" + nid);
      }

      return getVersionType(obj.get());
   }
   
   /**
    * Find the VUID for a component (if it has one)
    * 
    * Calls {@link #getVuId(int, StampCoordinate)} with a null (default) stamp coordinate
    *
    * @param componentNid
    * @return the id, if found, or empty (will not return null)
    */
   public static Optional<Long> getVuId(int componentNid) {
      return getVuId(componentNid, null);
   }

   /**
    * Find the VUID for a component (if it has one).
    *
    * @param componentNid the component nid
    * @param stamp - optional - if not provided uses default from config service
    * @return the id, if found, or empty (will not return null)
    */
   public static Optional<Long> getVuId(int componentNid, StampCoordinate stamp) {
      try {
         final ArrayList<Long> vuids = new ArrayList<>(1);

         Get.assemblageService().getSnapshot(SemanticVersion.class, (stamp == null) ? 
               Get.configurationService().getUserConfiguration(Optional.empty()).getStampCoordinate() : stamp)
               .getLatestSemanticVersionsForComponentFromAssemblage(componentNid, MetaData.VUID____SOLOR.getNid()).forEach(latestSemantic -> {
                  // expected path
                  if (latestSemantic.get().getChronology().getVersionType() == VersionType.STRING) {
                     vuids.add(Long.parseLong(((StringVersion) latestSemantic.get()).getString()));
                  }

                  // Data model bug path (can go away, after bug is fixed)
                  else if (latestSemantic.get().getChronology().getVersionType() == VersionType.DYNAMIC) {
                     vuids.add(Long.parseLong(((DynamicVersion<?>) latestSemantic.get()).getData()[0].dataToString()));
                  }
               });

         if (vuids.size() > 1) {
            LOG.warn("Found multiple VUIDs on component " + Get.identifierService().getUuidPrimordialForNid(componentNid));
         }

         if (vuids.size() > 0) {
            return Optional.of(vuids.get(0));
         }
      } catch (final Exception e) {
         LOG.error("Unexpected error trying to find VUID for nid " + componentNid, e);
      }

      return Optional.empty();
   }
   
   /**
    * Returns the nids of all matching vuid semantics (if any found on view coordinate).
    * 
    * @param vuID the vuID to lookup
    * @return the nids of semantics that contain vuids
    */
   public static Set<Integer> getVuidSemanticNidsForVUID(long vuID) {
      final IndexSemanticQueryService si = LookupService.get().getService(IndexSemanticQueryService.class);
      if (si == null) {
         final String msg = "Semantic Index not available - can't lookup VUID " + vuID;
         LOG.error(msg);
         throw new RuntimeException(msg);
      }

      // StampCoordinate with LATEST ACTIVE_ONLY from all VHAT modules
      final StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE, TermAux.DEVELOPMENT_PATH.getNid());
      final Set<Integer> vhatModules = Frills.getAllChildrenOfConcept(MetaData.VHAT_MODULES____SOLOR.getNid(), true, true, null);
      final StampCoordinate stampCoordinate = new StampCoordinateImpl(StampPrecedence.PATH, stampPosition, NidSet.of(vhatModules), Status.ACTIVE_ONLY_SET);

      final Set<Integer> matchingVuidSemanticNids = new HashSet<>();

      final Predicate<Integer> filter = new Predicate<Integer>() {
         @Override
         public boolean test(Integer t) {
            final Optional<? extends SemanticChronology> SemanticChronologyToCheck = 
                  (Optional<? extends SemanticChronology>) Get.assemblageService().getOptionalSemanticChronology(t);
            // This check should be redundant
            if (SemanticChronologyToCheck.isPresent() && SemanticChronologyToCheck.get().getAssemblageNid() == MetaData.VUID____SOLOR.getNid()) {
               final SemanticChronology existingVuidSemantic = ((SemanticChronology) SemanticChronologyToCheck.get());
               LatestVersion<Version> latestVersionOptional = existingVuidSemantic.getLatestVersion(stampCoordinate);

               if (latestVersionOptional.isPresent()) {
                  // TODO do we care about contradictions?
                  StringVersion semanticVersion = ((StringVersion)latestVersionOptional.get());
                  if ((vuID + "").equals(semanticVersion.getString())) {
                     return true;
                  }
               }
            }
            return false;
         }
      };
      // force the prefix algorithm, and add a trailing space - quickest way to do an exact-match type of search
      List<SearchResult> results = si.query(vuID + " ", true, new int[] { MetaData.VUID____SOLOR.getNid() }, filter, null, null, 1000, Long.MAX_VALUE);
      if (results.size() > 0) {
         for (SearchResult result : results) {
            matchingVuidSemanticNids.add(result.getNid());
         }
      }

      return Collections.unmodifiableSet(matchingVuidSemanticNids);
   }

   /**
    * {@link IdInfo}.
    *
    * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
    *
    * Class to contain and hide map generated by getIdInfo(). Only useful method is toString(). The returned String is not meant to be parsed.
    */
   public final static class IdInfo {
      /** The map. */
      private final Map<String, Object> map;

      /**
       * Instantiates a new id info.
       *
       * @param map the map
       */
      private IdInfo(Map<String, Object> map) {
         this.map = map;
      }

      /**
       * To string.
       *
       * @return the string
       */
      @Override
      public String toString() {
         return this.map.toString();
      }
   }
   
   private static class VersionUpdatePair<T extends StampedVersion> {
      T mutable;
      T latest;

      public void set(T mutable, T latest) {
         this.mutable = mutable;
         this.latest = latest;
      }
   }
   
   public static CommitRecord commitCheck(CommitTask commitTask) throws RuntimeException {
      try {
         Optional<CommitRecord> cr = commitTask.get();
         if (!cr.isPresent())
         {
            LOG.error("Commit Failure - reasons - {}", Arrays.toString(commitTask.getAlerts().toArray()));
            throw new RuntimeException("Commit failure - " + Arrays.toString(commitTask.getAlerts().toArray()));
         }
         return cr.get();
      }
      catch (RuntimeException e) {
         throw e;
      }
      catch (Exception e) {
         LOG.error("Commit Failure", e);
         throw new RuntimeException("Commit failure", e);
      }
   }
   
   /**
    * @return a sorted list of SimpleDisplayConcept objects that represent all dynamic semantic assemblages in the system (active or inactive) 
    */
   public static List<SimpleDisplayConcept> getAllDynamicSemanticAssemblageConcepts()
   {
      List<SimpleDisplayConcept> allDynamicSemanticDefConcepts = new ArrayList<>();

      Get.assemblageService().getSemanticChronologyStream(DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION.getNid()).forEach(semanticC ->
      {
         //This will be a nid of a description - need to get the referenced component of that description
         int annotatedDescriptionNid = semanticC.getReferencedComponentNid();
         try
         {
            allDynamicSemanticDefConcepts.add(new SimpleDisplayConcept(Get.assemblageService().getSemanticChronology(annotatedDescriptionNid).getReferencedComponentNid()));
         }
         catch (Exception e)
         {
            LOG.error("Unexpected error looking up dynamic semantics! with " + semanticC.toUserString(), e);
         }
      });

      Collections.sort(allDynamicSemanticDefConcepts);
      return allDynamicSemanticDefConcepts;
   }
   
   public static List<ConceptChronology> getIdentifierAssemblages() {
      List<ConceptChronology> identifierAnnotatedConcepts = new ArrayList<>();

      Get.assemblageService().getSemanticChronologyStream(MetaData.IDENTIFIER_SOURCE____SOLOR.getNid()).sequential().forEach(
            identifierAnnotationSemanticChronology ->  {
         identifierAnnotatedConcepts.add(Get.conceptService().getConceptChronology(identifierAnnotationSemanticChronology.getReferencedComponentNid()));
      });
      return identifierAnnotatedConcepts;
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public void reset()
   {
      IS_ASSOCIATION_CLASS.invalidateAll();
      IS_MAPPING_CLASS.invalidateAll();
      MODULE_TO_TERM_TYPE_CACHE.invalidateAll();
   }
}

