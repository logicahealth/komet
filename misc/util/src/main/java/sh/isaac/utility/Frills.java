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

//~--- JDK imports ------------------------------------------------------------


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.LruCache;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.ComponentNidSememe;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.component.sememe.version.MutableDescriptionSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.StringSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnUtility;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.externalizable.OchreExternalizableObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.NumericUtils;
import sh.isaac.api.util.TaskCompleteCallback;
import sh.isaac.api.util.UUIDUtil;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.model.concept.ConceptVersionImpl;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.relationship.RelationshipVersionAdaptorImpl;
import sh.isaac.model.sememe.DynamicSememeUsageDescriptionImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeStringImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import sh.isaac.model.sememe.version.ComponentNidSememeImpl;
import sh.isaac.model.sememe.version.DescriptionSememeImpl;
import sh.isaac.model.sememe.version.DynamicSememeImpl;
import sh.isaac.model.sememe.version.LogicGraphSememeImpl;
import sh.isaac.model.sememe.version.LongSememeImpl;
import sh.isaac.model.sememe.version.StringSememeImpl;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import sh.isaac.api.index.IndexService;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class Frills.
 */

//This is a service, simply to implement the DynamicSememeColumnUtility interface.  Everythign else is static, and may be used directly
@Service
@Singleton
public class Frills
         implements DynamicSememeColumnUtility {
   /** The LOG. */
   private static final Logger LOG = LogManager.getLogger(Frills.class);

   /** The is association cache. */
   private static final LruCache<Integer, Boolean> IS_ASSOCIATION_CLASS = new LruCache<>(50);

   /** The is mapping cache. */
   private static final LruCache<Integer, Boolean> IS_MAPPING_CLASS = new LruCache<>(50);

   //~--- methods -------------------------------------------------------------

   /**
    * Build, without committing, a new concept using the provided columnName and columnDescription values which is suitable
    * for use as a column descriptor within {@link DynamicSememeUsageDescription}.
    *
    * The new concept will be created under the concept {@link DynamicSememeConstants#DYNAMIC_SEMEME_COLUMNS}
    *
    * A complete usage pattern (where both the refex assemblage concept and the column name concept needs
    * to be created) would look roughly like this:
    *
    * DynamicSememeUtility.createNewDynamicSememeUsageDescriptionConcept(
    *       "The name of the Sememe",
    *       "The description of the Sememe",
    *       new DynamicSememeColumnInfo[]{new DynamicSememeColumnInfo(
    *               0,
    *               DynamicSememeColumnInfo.createNewDynamicSememeColumnInfoConcept(
    *                       "column name",
    *                       "column description"
    *                       )
    *               DynamicSememeDataType.STRING,
    *               new DynamicSememeStringImpl("default value")
    *               )}
    *       )
    *
    * //TODO [REFEX] figure out language details (how we know what language to put on the name/description
    *
    * @param columnName the column name
    * @param columnDescription the column description
    * @return the concept chronology<? extends concept version<?>>
    * @throws RuntimeException the runtime exception
    */
   @SuppressWarnings("deprecation")
   public static ConceptChronology<? extends ConceptVersion<?>> buildNewDynamicSememeColumnInfoConcept(
           String columnName,
           String columnDescription)
            throws RuntimeException {
      if ((columnName == null) ||
            (columnName.length() == 0) ||
            (columnDescription == null) ||
            (columnDescription.length() == 0)) {
         throw new RuntimeException("Both the column name and column description are required");
      }

      final ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);

      conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE____ISAAC);
      conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT____ISAAC);
      conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());

      final DescriptionBuilderService descriptionBuilderService =
         LookupService.getService(DescriptionBuilderService.class);
      final LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class)
                                                               .getLogicalExpressionBuilder();

      NecessarySet(And(ConceptAssertion(Get.conceptService()
            .getConcept(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMNS
                  .getNid()),
                                        defBuilder)));

      final LogicalExpression parentDef = defBuilder.build();
      final ConceptBuilder    builder = conceptBuilderService.getDefaultConceptBuilder(columnName, null, parentDef);
      DescriptionBuilder<?, ?> definitionBuilder = descriptionBuilderService.getDescriptionBuilder(columnName,
                                                                                                   builder,
                                                                                                   MetaData.SYNONYM____ISAAC,
                                                                                                   MetaData.ENGLISH_LANGUAGE____ISAAC);

      definitionBuilder.addPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT____ISAAC);
      builder.addDescription(definitionBuilder);
      definitionBuilder = descriptionBuilderService.getDescriptionBuilder(columnDescription,
            builder,
            MetaData.DEFINITION_DESCRIPTION_TYPE____ISAAC,
            MetaData.ENGLISH_LANGUAGE____ISAAC);
      definitionBuilder.addPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT____ISAAC);
      builder.addDescription(definitionBuilder);

      ConceptChronology<? extends ConceptVersion<?>> newCon;

      try {
         newCon = builder.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE, new ArrayList<>())
                         .get();
      } catch (InterruptedException | ExecutionException e) {
         final String msg = "Failed building new DynamicSememeColumnInfo concept columnName=\"" + columnName +
                            "\", columnDescription=\"" + columnDescription + "\"";

         LOG.error(msg, e);
         throw new RuntimeException(msg, e);
      }

      return newCon;
   }

   /**
    * This method returns an uncommitted refexUsageDescriptor concept chronology.
    * A DynamicSememeUsageDescription may be constructed by passing it to the DynamicSememeUsageDescriptionImpl ctor.
    *
    * @param sememeFSN the sememe FSN
    * @param sememePreferredTerm the sememe preferred term
    * @param sememeDescription the sememe description
    * @param columns the columns
    * @param parentConceptNidOrSequence the parent concept nid or sequence
    * @param referencedComponentRestriction the referenced component restriction
    * @param referencedComponentSubRestriction the referenced component sub restriction
    * @param editCoord the edit coord
    * @return the concept chronology<? extends concept version<?>>
    */
   @SuppressWarnings("deprecation")
   public static ConceptChronology<? extends ConceptVersion<?>> buildUncommittedNewDynamicSememeUsageDescription(
           String sememeFSN,
           String sememePreferredTerm,
           String sememeDescription,
           DynamicSememeColumnInfo[] columns,
           Integer parentConceptNidOrSequence,
           ObjectChronologyType referencedComponentRestriction,
           SememeType referencedComponentSubRestriction,
           EditCoordinate editCoord) {
      try {
         final EditCoordinate localEditCoord = ((editCoord == null) ? Get.configurationService()
                                                                         .getDefaultEditCoordinate()
               : editCoord);
         final ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);

         conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE____ISAAC);
         conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT____ISAAC);
         conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());

         final DescriptionBuilderService descriptionBuilderService =
            LookupService.getService(DescriptionBuilderService.class);
         final LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class)
                                                                  .getLogicalExpressionBuilder();
         final ConceptChronology<?> parentConcept = Get.conceptService()
                                                       .getConcept((parentConceptNidOrSequence == null)
                                                          ? DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES
                                                                .getNid()
               : parentConceptNidOrSequence);

         NecessarySet(And(ConceptAssertion(parentConcept, defBuilder)));

         final LogicalExpression parentDef = defBuilder.build();
         final ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(sememeFSN, null, parentDef);
         DescriptionBuilder<? extends SememeChronology<?>, ? extends MutableDescriptionSememe<?>> definitionBuilder =
            descriptionBuilderService.getDescriptionBuilder(sememePreferredTerm,
                                                            builder,
                                                            MetaData.SYNONYM____ISAAC,
                                                            MetaData.ENGLISH_LANGUAGE____ISAAC);

         definitionBuilder.addPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT____ISAAC);
         builder.addDescription(definitionBuilder);

         final ConceptChronology<? extends ConceptVersion<?>> newCon = builder.build(localEditCoord,
                                                                                     ChangeCheckerMode.ACTIVE,
                                                                                     new ArrayList<>())
                                                                              .getNoThrow();

         {

            // Set up the dynamic sememe 'special' definition
            definitionBuilder = descriptionBuilderService.getDescriptionBuilder(sememeDescription,
                  builder,
                  MetaData.DEFINITION_DESCRIPTION_TYPE____ISAAC,
                  MetaData.ENGLISH_LANGUAGE____ISAAC);
            definitionBuilder.addPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT____ISAAC);

            final SememeChronology<?> definitionSememe = definitionBuilder.build(localEditCoord,
                                                                                 ChangeCheckerMode.ACTIVE)
                                                                          .getNoThrow();

            Get.sememeBuilderService()
               .getDynamicSememeBuilder(definitionSememe.getNid(),
                                        DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION
                                              .getSequence(),
                                        null)
               .build(localEditCoord, ChangeCheckerMode.ACTIVE)
               .getNoThrow();
         }

         if (columns != null) {
            // Ensure that we process in column order - we don't always keep track of that later - we depend on the data being stored in the right order.
            final TreeSet<DynamicSememeColumnInfo> sortedColumns = new TreeSet<>(Arrays.asList(columns));

            for (final DynamicSememeColumnInfo ci: sortedColumns) {
               final DynamicSememeData[] data = LookupService.getService(DynamicSememeUtility.class)
                                                             .configureDynamicSememeDefinitionDataForColumn(ci);

               Get.sememeBuilderService()
                  .getDynamicSememeBuilder(newCon.getNid(),
                                           DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION
                                                 .getSequence(),
                                           data)
                  .build(localEditCoord, ChangeCheckerMode.ACTIVE)
                  .getNoThrow();
            }
         }

         final DynamicSememeData[] data = LookupService.getService(DynamicSememeUtility.class)
                                                       .configureDynamicSememeRestrictionData(
                                                          referencedComponentRestriction,
                                                                referencedComponentSubRestriction);

         if (data != null) {
            Get.sememeBuilderService()
               .getDynamicSememeBuilder(newCon.getNid(),
                                        DynamicSememeConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION
                                              .getSequence(),
                                        data)
               .build(localEditCoord, ChangeCheckerMode.ACTIVE)
               .getNoThrow();
         }

         return newCon;
      } catch (final IllegalStateException e) {
         throw new RuntimeException("Creation of Dynamic Sememe Failed!", e);
      }
   }

   /**
    * Create a new concept using the provided columnName and columnDescription values which is suitable
    * for use as a column descriptor within {@link DynamicSememeUsageDescription}.
    *
    * The new concept will be created under the concept {@link DynamicSememeConstants#DYNAMIC_SEMEME_COLUMNS}
    *
    * A complete usage pattern (where both the refex assemblage concept and the column name concept needs
    * to be created) would look roughly like this:
    *
    * DynamicSememeUtility.createNewDynamicSememeUsageDescriptionConcept(
    *       "The name of the Sememe",
    *       "The description of the Sememe",
    *       new DynamicSememeColumnInfo[]{new DynamicSememeColumnInfo(
    *               0,
    *               DynamicSememeColumnInfo.createNewDynamicSememeColumnInfoConcept(
    *                       "column name",
    *                       "column description"
    *                       )
    *               DynamicSememeDataType.STRING,
    *               new DynamicSememeStringImpl("default value")
    *               )}
    *       )
    *
    * //TODO [REFEX] figure out language details (how we know what language to put on the name/description
    *
    * @param columnName the column name
    * @param columnDescription the column description
    * @return the concept chronology<? extends concept version<?>>
    * @throws RuntimeException the runtime exception
    */
   @SuppressWarnings("deprecation")
   public static ConceptChronology<? extends ConceptVersion<?>> createNewDynamicSememeColumnInfoConcept(
           String columnName,
           String columnDescription)
            throws RuntimeException {
      final ConceptChronology<? extends ConceptVersion<?>> newCon = buildNewDynamicSememeColumnInfoConcept(columnName,
                                                                                                           columnDescription);

      try {
         Get.commitService()
            .commit("creating new dynamic sememe column: " + columnName)
            .get();
         return newCon;
      } catch (InterruptedException | ExecutionException e) {
         final String msg = "Failed committing new DynamicSememeColumnInfo concept columnName=\"" + columnName +
                            "\", columnDescription=\"" + columnDescription + "\"";

         LOG.error(msg, e);
         throw new RuntimeException(msg, e);
      }
   }

   /**
    * See {@link DynamicSememeUsageDescription} for the full details on what this builds.
    *
    * Does all the work to create a new concept that is suitable for use as an Assemblage Concept for a new style Dynamic Sememe.
    *
    * The concept will be created under the concept {@link DynamicSememeConstants#DYNAMIC_SEMEME_ASSEMBLAGES} if a parent is not specified
    *
    * //TODO [REFEX] figure out language details (how we know what language to put on the name/description
    *
    * @param sememeFSN the sememe FSN
    * @param sememePreferredTerm - The preferred term for this refex concept that will be created.
    * @param sememeDescription - A user friendly string the explains the overall intended purpose of this sememe (what it means, what it stores)
    * @param columns - The column information for this new refex.  May be an empty list or null.
    * @param parentConceptNidOrSequence  - optional - if null, uses {@link DynamicSememeConstants#DYNAMIC_SEMEME_ASSEMBLAGES}
    * @param referencedComponentRestriction - optional - may be null - if provided - this restricts the type of object referenced by the nid or
    * UUID that is set for the referenced component in an instance of this sememe.  If {@link ObjectChronologyType#UNKNOWN_NID} is passed, it is ignored, as
    * if it were null.
    * @param referencedComponentSubRestriction - optional - may be null - subtype restriction for {@link ObjectChronologyType#SEMEME} restrictions
    * @param editCoord - optional - the coordinate to use during create of the sememe concept (and related descriptions) - if not provided, uses system default.
    * @return a reference to the newly created sememe item
    */
   @SuppressWarnings("deprecation")
   public static DynamicSememeUsageDescription createNewDynamicSememeUsageDescriptionConcept(String sememeFSN,
         String sememePreferredTerm,
         String sememeDescription,
         DynamicSememeColumnInfo[] columns,
         Integer parentConceptNidOrSequence,
         ObjectChronologyType referencedComponentRestriction,
         SememeType referencedComponentSubRestriction,
         EditCoordinate editCoord) {
      final ConceptChronology<? extends ConceptVersion<?>> newDynamicSememeUsageDescriptionConcept =
         buildUncommittedNewDynamicSememeUsageDescription(sememeFSN,
                                                          sememePreferredTerm,
                                                          sememeDescription,
                                                          columns,
                                                          parentConceptNidOrSequence,
                                                          referencedComponentRestriction,
                                                          referencedComponentSubRestriction,
                                                          editCoord);

      try {
         Get.commitService()
            .commit("creating new dynamic sememe assemblage (DynamicSememeUsageDescription): NID=" +
                    newDynamicSememeUsageDescriptionConcept.getNid() + ", FSN=" + sememeFSN + ", PT=" +
                    sememePreferredTerm + ", DESC=" + sememeDescription)
            .get();
      } catch (InterruptedException | ExecutionException e) {
         throw new RuntimeException("Commit of Dynamic Sememe Failed!", e);
      }

      return new DynamicSememeUsageDescriptionImpl(newDynamicSememeUsageDescriptionConcept.getNid());
   }

   /**
    * Defines association.
    *
    * @param conceptSequence the concept sequence
    * @return true, if successful
    */
   public static boolean definesAssociation(int conceptSequence) {
      if (IS_ASSOCIATION_CLASS.containsKey(conceptSequence)) {
         return IS_ASSOCIATION_CLASS.get(conceptSequence);
      }

      final boolean temp = Get.sememeService()
                              .getSememesForComponentFromAssemblage(Get.identifierService()
                                    .getConceptNid(conceptSequence),
                                    DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME
                                          .getConceptSequence())
                              .anyMatch(sememe -> true);

      IS_ASSOCIATION_CLASS.put(conceptSequence, temp);
      return temp;
   }

   /**
    * Defines dynamic sememe.
    *
    * @param conceptSequence the concept sequence
    * @return true, if successful
    */
   public static boolean definesDynamicSememe(int conceptSequence) {
      return DynamicSememeUsageDescriptionImpl.isDynamicSememe(conceptSequence);
   }

   /**
    * Defines mapping.
    *
    * @param conceptSequence the concept sequence
    * @return true, if successful
    */
   public static boolean definesMapping(int conceptSequence) {
      if (IS_MAPPING_CLASS.containsKey(conceptSequence)) {
         return IS_MAPPING_CLASS.get(conceptSequence);
      }

      final boolean temp = Get.sememeService()
                              .getSememesForComponentFromAssemblage(Get.identifierService()
                                    .getConceptNid(conceptSequence),
                                    IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE
                                          .getConceptSequence())
                              .anyMatch(sememe -> true);

      IS_MAPPING_CLASS.put(conceptSequence, temp);
      return temp;
   }

   /**
    * Convenience method to find the nearest concept related to a sememe.  Recursively walks referenced components until it finds a concept.
    *
    * @param nid the nid
    * @return the nearest concept sequence, or -1, if no concept can be found.
    */
   public static int findConcept(int nid) {
      final Optional<? extends ObjectChronology<? extends StampedVersion>> c = Get.identifiedObjectService()
                                                                                  .getIdentifiedObjectChronology(nid);

      if (c.isPresent()) {
         if (null == c.get()
                 .getOchreObjectType()) {
            LOG.warn("Unexpected object type: " + c.get().getOchreObjectType());
         } else {
            switch (c.get()
                    .getOchreObjectType()) {
               case SEMEME:
                  return findConcept(((SememeChronology<?>) c.get()).getReferencedComponentNid());
               case CONCEPT:
                  return ((ConceptChronology<?>) c.get()).getConceptSequence();
               default:
                  LOG.warn("Unexpected object type: " + c.get().getOchreObjectType());
                  break;
            }
         }
      }

      return -1;
   }


   /**
    * All done in a background thread, method returns immediately.
    *
    * @param nid the nid
    * @param callback - who to inform when lookup completes
    * @param callId - An arbitrary identifier that will be returned to the caller when this completes
    * @param manifoldCoordinate
    */
   public static void lookupConceptSnapshot(final int nid,
         final TaskCompleteCallback<ConceptSnapshot> callback,
         final Integer callId,
         final ManifoldCoordinate manifoldCoordinate) {
      LOG.debug("Threaded Lookup: '{}'", nid);

      final long     submitTime = System.currentTimeMillis();
      final Runnable r          = () -> {
                                     final Optional<ConceptSnapshot> c = getConceptSnapshot(nid, manifoldCoordinate);

                                     callback.taskComplete(c.isPresent() ? c.get()
               : null, submitTime, callId);
                                  };

      Get.workExecutors()
         .getExecutor()
         .execute(r);
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
      final ConceptSequenceSet moduleSequenceSet = new ConceptSequenceSet();

      moduleSequenceSet.add(requiredModuleSequence);

      if (optionalModuleSequences != null) {
         for (final int seq: optionalModuleSequences) {
            moduleSequenceSet.add(seq);
         }
      }

      final EnumSet<State> allowedStates = EnumSet.allOf(State.class);

      allowedStates.addAll(existingStampCoordinate.getAllowedStates());

      final StampCoordinate newStampCoordinate = new StampCoordinateImpl(existingStampCoordinate.getStampPrecedence(),
                                                                         existingStampCoordinate.getStampPosition(),
                                                                         moduleSequenceSet,
                                                                         allowedStates);

      return newStampCoordinate;
   }

   /**
    * Read dynamic sememe column name description.
    *
    * @param columnDescriptionConcept the column description concept
    * @return the string[]
    */
   @SuppressWarnings("unchecked")
   @Override
   public String[] readDynamicSememeColumnNameDescription(UUID columnDescriptionConcept) {
      String columnName           = null;
      String columnDescription    = null;
      String fsn                  = null;
      String acceptableSynonym    = null;
      String acceptableDefinition = null;

      try {
         final ConceptChronology<? extends ConceptVersion<?>> cc = Get.conceptService()
                                                                      .getConcept(columnDescriptionConcept);

         for (final SememeChronology<? extends DescriptionSememe<?>> dc: cc.getConceptDescriptionList()) {
            if ((columnName != null) && (columnDescription != null)) {
               break;
            }

            @SuppressWarnings("rawtypes")
            final Optional<LatestVersion<DescriptionSememe<?>>> descriptionVersion =
               ((SememeChronology) dc).getLatestVersion(DescriptionSememe.class,
                                                        Get.configurationService()
                                                              .getDefaultStampCoordinate()
                                                              .makeCoordinateAnalog(State.ACTIVE,
                                                                    State.INACTIVE,
                                                                    State.CANCELED,
                                                                    State.PRIMORDIAL));

            if (descriptionVersion.isPresent() && descriptionVersion.get().value().isPresent()) {
               final DescriptionSememe<?> d = descriptionVersion.get()
                                                                .value().get();

               if (d.getDescriptionTypeConceptSequence() ==
                     TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence()) {
                  fsn = d.getText();
               } else if (d.getDescriptionTypeConceptSequence() ==
                          TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence()) {
                  if (Frills.isDescriptionPreferred(d.getNid(), null)) {
                     columnName = d.getText();
                  } else {
                     acceptableSynonym = d.getText();
                  }
               } else if (d.getDescriptionTypeConceptSequence() ==
                          TermAux.DEFINITION_DESCRIPTION_TYPE.getConceptSequence()) {
                  if (Frills.isDescriptionPreferred(d.getNid(), null)) {
                     columnDescription = d.getText();
                  } else {
                     acceptableDefinition = d.getText();
                  }
               }
            }
         }
      } catch (final Exception e) {
         LOG.warn("Failure reading DynamicSememeColumnInfo '" + columnDescriptionConcept + "'", e);
      }

      if (columnName == null) {
         LOG.warn("No preferred synonym found on '" + columnDescriptionConcept + "' to use " +
                  "for the column name - using FSN");
         columnName = ((fsn == null) ? "ERROR - see log"
                                     : fsn);
      }

      if ((columnDescription == null) && (acceptableDefinition != null)) {
         columnDescription = acceptableDefinition;
      }

      if ((columnDescription == null) && (acceptableSynonym != null)) {
         columnDescription = acceptableSynonym;
      }

      if (columnDescription == null) {
         LOG.info("No preferred or acceptable definition or acceptable synonym found on '" + columnDescriptionConcept +
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
                   .getAllServiceHandles(IndexService.class)
                   .forEach(index -> {
         // Making a query, with long.maxValue, causes the index to refresh itself, and look at the latest updates, if there have been updates.
                               index.getService()
                                    .query("hi", null, 1, Long.MAX_VALUE);
                            });
   }

   /**
    * To string.
    *
    * @param version toString for StampedVersion
    * @return the string
    */
   public static String toString(StampedVersion version) {
      return version.getClass()
                    .getSimpleName() + " STAMP=" + version.getStampSequence() + "{state=" + version.getState() +
                                       ", time=" + version.getTime() + ", author=" + version.getAuthorSequence() +
                                       ", module=" + version.getModuleSequence() + ", path=" +
                                       version.getPathSequence() + "}";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Returns a Map correlating each dialect sequence for a passed
    * descriptionSememeId with its respective acceptability nid (preferred vs
    * acceptable).
    *
    * @param descriptionSememeNid the description sememe nid
    * @param stamp - optional - if not provided, uses default from config
    * service
    * @return the acceptabilities
    * @throws RuntimeException If there is inconsistent data (incorrectly)
    * attached to the sememe
    */
   public static Map<Integer, Integer> getAcceptabilities(int descriptionSememeNid,
         StampCoordinate stamp)
            throws RuntimeException {
      final Map<Integer, Integer> dialectSequenceToAcceptabilityNidMap = new ConcurrentHashMap<>();

      Get.sememeService()
         .getSememesForComponent(descriptionSememeNid)
         .forEach(nestedSememe -> {
                     if (nestedSememe.getSememeType() == SememeType.COMPONENT_NID) {
                        final int dialectSequence = nestedSememe.getAssemblageSequence();
                        @SuppressWarnings({ "rawtypes", "unchecked" })
                        final Optional<LatestVersion<ComponentNidSememe>> latest =
                           ((SememeChronology) nestedSememe).getLatestVersion(ComponentNidSememe.class,
                                                                              (stamp == null)
                                                                              ? Get.configurationService()
                                                                                    .getDefaultStampCoordinate()
                  : stamp);

                        if (latest.isPresent() && latest.get().value().isPresent()) {
                           if ((latest.get().value().get().getComponentNid() == MetaData.PREFERRED____ISAAC.getNid()) ||
                               (latest.get().value().get().getComponentNid() == MetaData.ACCEPTABLE____ISAAC.getNid())) {
                              if ((dialectSequenceToAcceptabilityNidMap.get(dialectSequence) != null) &&
                                  (dialectSequenceToAcceptabilityNidMap.get(dialectSequence) !=
                                   latest.get().value().get().getComponentNid())) {
                                 throw new RuntimeException("contradictory annotations about acceptability!");
                              } else {
                                 dialectSequenceToAcceptabilityNidMap.put(dialectSequence,
                                       latest.get()
                                             .value().get()
                                             .getComponentNid());
                              }
                           } else {
                              UUID   uuid          = null;
                              String componentDesc = null;
                                 throw new UnsupportedOperationException("Need to refactor to use ManifoldCoordinate. ");
                                 /*
 
                              try {
                                 final Optional<UUID> uuidOptional = Get.identifierService()
                                                                        .getUuidPrimordialForNid(latest.get()
                                                                              .value()
                                                                              .getComponentNid());

                                 if (uuidOptional.isPresent()) {
                                    uuid = uuidOptional.get();
                                 }
                                final Optional<LatestVersion<DescriptionSememe<?>>> desc = Get.conceptService()
                                                                                               .getSnapshot(StampCoordinates.getDevelopmentLatest())
                                                                                               .getDescriptionOptional(
                                                                                                  latest.get()
                                                                                                        .value()
                                                                                                        .getComponentNid());

                                 componentDesc = desc.isPresent() ? desc.get()
                                       .value()
                                       .getText()
                           : null;
                              } catch (final Exception e) {
                                 // NOOP
                              }
                                 
                              LOG.warn("Unexpected component " + componentDesc + " (uuid=" + uuid + ", nid=" +
                                       latest.get().value().getComponentNid() + ")");

                              // throw new RuntimeException("Unexpected component " + componentDesc + " (uuid=" + uuid + ", nid=" + latest.get().value().getComponentNid() + ")");
                              // dialectSequenceToAcceptabilityNidMap.put(dialectSequence, latest.get().value().getComponentNid());
                           */
                                 }
                        }
                                 
                     }
                  });
      return dialectSequenceToAcceptabilityNidMap;
   }

   /**
    * Get isA children of a concept.  Does not return the requested concept in any circumstance.
    * @param conceptSequence The concept to look at
    * @param recursive recurse down from the concept
    * @param leafOnly only return leaf nodes
    * @return the set of concept sequence ids that represent the children
    */
   public static Set<Integer> getAllChildrenOfConcept(int conceptSequence, boolean recursive, boolean leafOnly) {
      final Set<Integer> temp = getAllChildrenOfConcept(new HashSet<>(), conceptSequence, recursive, leafOnly);

      if (leafOnly && (temp.size() == 1)) {
         temp.remove(conceptSequence);
      }

      return temp;
   }

   /**
    * Recursively get Is a children of a concept.  May inadvertenly return the requested starting sequence when leafOnly is true, and
    * there are no children.
    *
    * @param handledConceptSequenceIds the handled concept sequence ids
    * @param conceptSequence the concept sequence
    * @param recursive the recursive
    * @param leafOnly the leaf only
    * @return the all children of concept
    */
   private static Set<Integer> getAllChildrenOfConcept(Set<Integer> handledConceptSequenceIds,
         int conceptSequence,
         boolean recursive,
         boolean leafOnly) {
      final Set<Integer> results = new HashSet<>();

      // This both prevents infinite recursion and avoids processing or returning of duplicates
      if (handledConceptSequenceIds.contains(conceptSequence)) {
         return results;
      }

      final AtomicInteger count    = new AtomicInteger();
      final IntStream     children = Get.taxonomyService()
                                        .getTaxonomyChildSequences(conceptSequence);

      children.forEach((conSequence) -> {
                          count.getAndIncrement();

                          if (!leafOnly) {
                             results.add(conSequence);
                          }

                          if (recursive) {
                             results.addAll(getAllChildrenOfConcept(handledConceptSequenceIds,
                                   conSequence,
                                   recursive,
                                   leafOnly));
                          }
                       });

      if (leafOnly && (count.get() == 0)) {
         results.add(conceptSequence);
      }

      handledConceptSequenceIds.add(conceptSequence);
      return results;
   }

   /**
    * Convenience method to return sequences of a distinct set of modules in
    * which versions of an ObjectChronology have been defined.
    *
    * @param chronology The ObjectChronology
    * @return sequences of a distinct set of modules in which versions of an
    * ObjectChronology have been defined
    */
   public static Set<Integer> getAllModuleSequences(ObjectChronology<? extends StampedVersion> chronology) {
      final Set<Integer> moduleSequences = new HashSet<>();

      for (final StampedVersion version: chronology.getVersionList()) {
         moduleSequences.add(version.getModuleSequence());
      }

      return Collections.unmodifiableSet(moduleSequences);
   }

   /**
    * A convenience method to determine if a particular component has 0 or 1 annotations of a particular type.  If there is more than one
    * annotation of a particular type, this method will throw a runtime exception.
    *
    * @param componentNid - the component to check for the assemblage
    * @param assemblageConceptId - the assemblage type you are interested in (nid or concept sequence)
    * @return the annotation sememe
    */
   public static Optional<SememeChronology<? extends SememeVersion<?>>> getAnnotationSememe(int componentNid,
         int assemblageConceptId) {
      final Set<Integer> allowedAssemblages = new HashSet<>();

      allowedAssemblages.add(assemblageConceptId);

      final Set<SememeChronology<? extends SememeVersion<?>>> sememeSet = Get.sememeService()
                                                                             .getSememesForComponentFromAssemblages(
                                                                                componentNid,
                                                                                      allowedAssemblages)
                                                                             .collect(Collectors.toSet());

      switch (sememeSet.size()) {
      case 0:
         return Optional.empty();

      case 1:
         return Optional.of(sememeSet.iterator()
                                     .next());

      default:
         throw new RuntimeException("Component " + componentNid + " has " + sememeSet.size() +
                                    " annotations of type " + Get.conceptDescriptionText(assemblageConceptId) +
                                    " (should only have zero or 1)");
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
         final Optional<UUID> assemblageConceptUuid = Get.identifierService()
                                                         .getUuidPrimordialFromConceptId(assemblageConceptId);

         if (!assemblageConceptUuid.isPresent()) {
            throw new RuntimeException("getUuidPrimordialFromConceptId() return empty UUID for assemblageConceptId " +
                                       assemblageConceptId);
         }

         final int               componentNid = Get.identifierService()
                                                   .getConceptNid(componentId);
         final ArrayList<String> values       = new ArrayList<>(1);
         final int assemblageConceptSequence = Get.identifierService()
                                                  .getConceptSequenceForUuids(assemblageConceptUuid.get());

         Get.sememeService()
            .getSnapshot(SememeVersion.class,
                         (stamp == null) ? Get.configurationService()
                               .getDefaultStampCoordinate()
                                         : stamp)
            .getLatestSememeVersionsForComponentFromAssemblage(componentNid, assemblageConceptSequence)
            .forEach(latestSememe -> {
                        if (latestSememe.value().get()
                                        .getChronology()
                                        .getSememeType() == SememeType.STRING) {
                           values.add(((StringSememeImpl) latestSememe.value().get()).getString());
                        } else if (latestSememe.value().get()
                                               .getChronology()
                                               .getSememeType() == SememeType.COMPONENT_NID) {
                           values.add(((ComponentNidSememeImpl) latestSememe.value().get()).getComponentNid() + "");
                        } else if (latestSememe.value().get()
                                               .getChronology()
                                               .getSememeType() == SememeType.LONG) {
                           values.add(((LongSememeImpl) latestSememe.value().get()).getLongValue() + "");
                        } else if (latestSememe.value().get()
                                               .getChronology()
                                               .getSememeType() == SememeType.DYNAMIC) {
                           final DynamicSememeData[] data = ((DynamicSememeImpl) latestSememe.value().get()).getData();

                           if (data.length > 0) {
                              LOG.warn("Found multiple (" + data.length + ") dynamic sememe data fields in sememe " +
                                       latestSememe.value().get().getNid() + " of assemblage type " + assemblageConceptUuid +
                                       " on component " +
                                       Get.identifierService().getUuidPrimordialForNid(componentNid));
                           }

                           values.add(data[0].dataToString());
                        }
                     });

         if (values.size() > 1) {
            LOG.warn("Found multiple (" + values.size() + ") " + assemblageConceptUuid +
                     " annotation sememes on component " +
                     Get.identifierService().getUuidPrimordialForNid(componentNid) + ". Using first value \"" +
                     values.get(0) + "\".");
         }

         if (values.size() > 0) {
            return Optional.of(values.get(0));
         }
      } catch (final Exception e) {
         LOG.error("Unexpected error trying to find " + assemblageConceptId + " annotation sememe on component " +
                   componentId,
                   e);
      }

      return Optional.empty();
   }

   /**
    * Checks if association.
    *
    * @param sc the sc
    * @return true, if association
    */
   public static boolean isAssociation(SememeChronology<? extends SememeVersion<?>> sc) {
      return definesAssociation(sc.getAssemblageSequence());
   }


   /**
    * Checks if concept fully defined.
    *
    * @param <T> the generic type
    * @param lgs The LogicGraphSememe containing the logic graph data
    * @return true if the corresponding concept is fully defined, otherwise returns false (for primitive concepts)
    *
    * Things that are defined with at least one SUFFICIENT_SET node are defined.
    * Things that are defined without any SUFFICIENT_SET nodes are primitive.
    */
   public static <T extends LogicGraphSememe<T>> boolean isConceptFullyDefined(LogicGraphSememe<T> lgs) {
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
      final Optional<SememeChronology<? extends SememeVersion<?>>> sememe = Get.sememeService()
                                                                               .getSememesForComponentFromAssemblage(
                                                                                  conceptNid,
                                                                                        (stated
                                                                                         ? LogicCoordinates.getStandardElProfile()
                                                                                               .getStatedAssemblageSequence()
            : LogicCoordinates.getStandardElProfile()
                              .getInferredAssemblageSequence()))
                                                                               .findAny();

      if (sememe.isPresent()) {
         @SuppressWarnings({ "unchecked", "rawtypes" })
         final Optional<LatestVersion<LogicGraphSememe>> sv =
            ((SememeChronology) sememe.get()).getLatestVersion(LogicGraphSememe.class,
                                                               StampCoordinates.getDevelopmentLatest());

         if (sv.isPresent() && sv.get().value().isPresent()) {
            return Optional.of(isConceptFullyDefined((LogicGraphSememe<?>) sv.get()
                  .value().get()));
         }
      }

      return Optional.empty();
   }

   /**
    * Gets the concept snapshot.
    *
    * @param conceptNidOrSequence the concept nid or sequence
    * @param manifoldCoordinate - optional - what stamp to use when returning the ConceptSnapshot (defaults to user prefs)
    * @return the ConceptSnapshot, or an optional that indicates empty, if the identifier was invalid, or if the concept didn't
 have a version available on the specified manifoldCoordinate
    */
   public static Optional<ConceptSnapshot> getConceptSnapshot(int conceptNidOrSequence,
         ManifoldCoordinate manifoldCoordinate) {
      final Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> c = Get.conceptService()
                                                                                      .getOptionalConcept(
                                                                                         conceptNidOrSequence);

      if (c.isPresent()) {
         try {
            return Optional.of(Get.conceptService()
                                  .getSnapshot((manifoldCoordinate == null) ? Get.configurationService()
                                        .getDefaultManifoldCoordinate()
                  : manifoldCoordinate)
                                  .getConceptSnapshot(c.get()
                                        .getConceptSequence()));
         } catch (final Exception e) {
            // TODO defaultConceptSnapshotService APIs are currently broken, provide no means of detecting if a concept doesn't exist on a given coordinate
            // See slack convo https://informatics-arch.slack.com/archives/dev-isaac/p1440568057000512
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   /**
    * Gets the concept snapshot.
    *
    * @param conceptUUID the concept UUID
    * @param manifoldCoordinate
    * @return the ConceptSnapshot, or an optional that indicates empty, if the identifier was invalid, or if the concept didn't
    *   have a version available on the specified stampCoord
    */
   public static Optional<ConceptSnapshot> getConceptSnapshot(UUID conceptUUID,
         ManifoldCoordinate manifoldCoordinate) {
      return getConceptSnapshot(Get.identifierService()
                                   .getNidForUuids(conceptUUID), manifoldCoordinate);
   }

   /**
    * If this description is flagged as an extended description type, return the type concept of the extension.
    *
    * @param stampCoordinate the stamp coordinate
    * @param descriptionId - the nid or sequence of the description sememe to check for an extended type.
    * @return the description extended type concept
    */
   public static Optional<UUID> getDescriptionExtendedTypeConcept(StampCoordinate stampCoordinate, int descriptionId) {
      final Optional<SememeChronology<? extends SememeVersion<?>>> descriptionExtendedTypeAnnotationSememe =
         getAnnotationSememe(Get.identifierService()
                                .getSememeNid(descriptionId),
                             DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE
                                   .getConceptSequence());

      if (descriptionExtendedTypeAnnotationSememe.isPresent()) {
         @SuppressWarnings({ "rawtypes", "unchecked" })
         final Optional<LatestVersion<DynamicSememeImpl>> optionalLatestSememeVersion =
            ((SememeChronology) (descriptionExtendedTypeAnnotationSememe.get())).getLatestVersion(
                DynamicSememeImpl.class,
                (stampCoordinate == null) ? Get.configurationService()
                      .getDefaultStampCoordinate()
                                          : stampCoordinate);

         if (!optionalLatestSememeVersion.get().contradictions().isEmpty()) {
            // TODO handle contradictions
            LOG.warn("Component " + descriptionId + " " +
                     " has DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE annotation with " +
                     optionalLatestSememeVersion.get().contradictions().size() + " contradictions");
         }

         final DynamicSememeData[] dataColumns = optionalLatestSememeVersion.get()
                                                                            .value().get()
                                                                            .getData();

         if (dataColumns.length != 1) {
            throw new RuntimeException(
                "Invalidly specified DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.  Should always have a column size of 1");
         }

         if (dataColumns[0].getDynamicSememeDataType() == DynamicSememeDataType.UUID) {
            return Optional.of(((DynamicSememeUUIDImpl) dataColumns[0]).getDataUUID());
         }

         // This isn't supposed to happen, but we have some bad data where it did.
         else if (dataColumns[0].getDynamicSememeDataType() == DynamicSememeDataType.STRING) {
            LOG.warn("Extended description type data found with type string instead of type UUID!");
            return Optional.of(UUID.fromString(((DynamicSememeStringImpl) dataColumns[0]).getDataString()));
         }

         throw new RuntimeException(
             "Failed to find UUID DynamicSememeData type in DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE annotation dynamic sememe");
      }

      return Optional.empty();
   }

   /**
    * Determine if a particular description sememe is flagged as preferred IN
    * ANY LANGUAGE. Returns false if there is no acceptability sememe.
    *
    * @param descriptionSememeNid the description sememe nid
    * @param stamp - optional - if not provided, uses default from config
    * service
    * @return true, if description preferred
    * @throws RuntimeException If there is unexpected data (incorrectly)
    * attached to the sememe
    */
   public static boolean isDescriptionPreferred(int descriptionSememeNid,
         StampCoordinate stamp)
            throws RuntimeException {
      final AtomicReference<Boolean> answer = new AtomicReference<>();

      // Ignore the language annotation... treat preferred in any language as good enough for our purpose here...
      Get.sememeService()
         .getSememesForComponent(descriptionSememeNid)
         .forEach(nestedSememe -> {
                     if (nestedSememe.getSememeType() == SememeType.COMPONENT_NID) {
                        @SuppressWarnings({ "rawtypes", "unchecked" })
                        final Optional<LatestVersion<ComponentNidSememe>> latest =
                           ((SememeChronology) nestedSememe).getLatestVersion(ComponentNidSememe.class,
                                                                              (stamp == null)
                                                                              ? Get.configurationService()
                                                                                    .getDefaultStampCoordinate()
                  : stamp);

                        if (latest.isPresent() && latest.get().value().isPresent()) {
                           if (latest.get()
                                     .value().get()
                                     .getComponentNid() == MetaData.PREFERRED____ISAAC.getNid()) {
                              if ((answer.get() != null) && (answer.get() != true)) {
                                 throw new RuntimeException("contradictory annotations about preferred status!");
                              }

                              answer.set(true);
                           } else if (latest.get()
                                            .value().get()
                                            .getComponentNid() == MetaData.ACCEPTABLE____ISAAC.getNid()) {
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
         LOG.warn("Description nid {} does not have an acceptability sememe!", descriptionSememeNid);
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
    * {@link MetaData#SYNONYM} or
    * {@link MetaData#FULLY_SPECIFIED_NAME} or
    * {@link MetaData#DEFINITION_DESCRIPTION_TYPE}
    * @param stamp - optional - if not provided gets the default from the
    * config service
    * @return the descriptions - may be empty, will not be null
    */
   public static List<DescriptionSememe<?>> getDescriptionsOfType(int conceptNid,
         ConceptSpecification descriptionType,
         StampCoordinate stamp) {
      final ArrayList<DescriptionSememe<?>> results = new ArrayList<>();

      Get.sememeService()
         .getSememesForComponent(conceptNid)
         .forEach(descriptionC -> {
                     if (descriptionC.getSememeType() == SememeType.DESCRIPTION) {
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        final Optional<LatestVersion<DescriptionSememe<?>>> latest =
                           ((SememeChronology) descriptionC).getLatestVersion(DescriptionSememe.class,
                                                                              (stamp == null)
                                                                              ? Get.configurationService()
                                                                                    .getDefaultStampCoordinate()
                  : stamp);

                        if (latest.isPresent()) {
                           final DescriptionSememe<?> ds = latest.get()
                                                                 .value().get();

                           if (ds.getDescriptionTypeConceptSequence() == descriptionType.getConceptSequence()) {
                              results.add(ds);
                           }
                        }
                     }
                  });
      return results;
   }


   /**
    * Gets the id info.
    *
    * @param id int identifier
    * @return a IdInfo, the toString() for which will display known identifiers and descriptions associated with the passed id
    *
    * This method should only be used for logging. The returned data structure is not meant to be parsed.
    */
   public static IdInfo getIdInfo(int id) {
      return getIdInfo(Integer.toString(id));
   }

   /**
    * Gets the id info.
    *
    * @param id String identifier may parse to int NID, int sequence or UUID
    * @return a IdInfo, the toString() for which will display known identifiers and descriptions associated with the passed id
    *
    * This method should only be used for logging. The returned data structure is not meant to be parsed.
    */
   public static IdInfo getIdInfo(String id) {
      return getIdInfo(id,
                       StampCoordinates.getDevelopmentLatest(),
                       LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate());
   }

   /**
    * Gets the id info.
    *
    * @param id UUID identifier
    * @return a IdInfo, the toString() for which will display known identifiers and descriptions associated with the passed id
    *
    * This method should only be used for logging. The returned data structure is not meant to be parsed.
    */
   public static IdInfo getIdInfo(UUID id) {
      return getIdInfo(id.toString());
   }

   /**
    * Gets the id info.
    *
    * @param id the id
    * @param sc the sc
    * @param lc the lc
    * @return the id info
    */
   public static IdInfo getIdInfo(int id, StampCoordinate sc, LanguageCoordinate lc) {
      return getIdInfo(Integer.toString(id), sc, lc);
   }

   /**
    * Gets the id info.
    *
    * @param id the id
    * @param sc the sc
    * @param lc the lc
    * @return the id info
    */
   public static IdInfo getIdInfo(String id, StampCoordinate sc, LanguageCoordinate lc) {
      final Map<String, Object> idInfo         = new HashMap<>();
      Long                      sctId          = null;
      Integer                   seq            = null;
      Integer                   nid            = null;
      UUID[]                    uuids          = null;
      ObjectChronologyType      typeOfPassedId = null;

      try {
         final Optional<Integer> intId = NumericUtils.getInt(id);

         if (intId.isPresent()) {
            // id interpreted as the id of the referenced component
            if (intId.get() > 0) {
               seq = intId.get();
               nid = Get.identifierService()
                        .getConceptNid(seq);
            } else if (intId.get() < 0) {
               nid = intId.get();
               seq = Get.identifierService()
                        .getConceptSequence(intId.get());
            }

            if (nid != null) {
               typeOfPassedId = Get.identifierService()
                                   .getChronologyTypeForNid(nid);
               uuids          = Get.identifierService()
                                   .getUuidArrayForNid(nid);
            }
         } else {
            final Optional<UUID> uuidId = UUIDUtil.getUUID(id);

            if (uuidId.isPresent()) {
               // id interpreted as the id of either a sememe or a concept
               nid            = Get.identifierService()
                                   .getNidForUuids(uuidId.get());
               typeOfPassedId = Get.identifierService()
                                   .getChronologyTypeForNid(nid);

               switch (typeOfPassedId) {
               case CONCEPT: {
                  seq = Get.identifierService()
                           .getConceptSequenceForUuids(uuidId.get());
                  break;
               }

               case SEMEME: {
                  seq = Get.identifierService()
                           .getSememeSequenceForUuids(uuidId.get());
                  break;
               }

               case UNKNOWN_NID:
               default:
               }
            }
         }

         if (nid != null) {
            throw new UnsupportedOperationException("Need to refactor to use ManifoldCoordinate");
            /*idInfo.put("DESC", Get.conceptService()
                                  .getSnapshot(sc)
                                  .conceptDescriptionText(nid));

            if (typeOfPassedId == ObjectChronologyType.CONCEPT) {
               final Optional<Long> optSctId = Frills.getSctId(nid, sc);

               if (optSctId.isPresent()) {
                  sctId = optSctId.get();
                  idInfo.put("SCTID", sctId);
               }
            }
            */
         }
      } catch (final Exception e) {
         LOG.warn("Problem getting idInfo for \"{}\". Caught {}", e.getClass()
               .getName(), e.getLocalizedMessage());
      }

      idInfo.put("PASSED_ID", id);
      idInfo.put("SEQ", seq);
      idInfo.put("NID", nid);
      idInfo.put("UUIDs", Arrays.toString(uuids));
      idInfo.put("TYPE", typeOfPassedId);
      return new IdInfo(idInfo);
   }

   /**
    * Gets the id info.
    *
    * @param id the id
    * @param sc the sc
    * @param lc the lc
    * @return the id info
    */
   public static IdInfo getIdInfo(UUID id, StampCoordinate sc, LanguageCoordinate lc) {
      return getIdInfo(id.toString(), sc, lc);
   }

   /**
    * Gets the inferred definition chronology.
    *
    * @param conceptId either a concept nid or sequence.
    * @param logicCoordinate LogicCoordinate.
    * @return the inferred definition chronology for the specified concept
    * according to the default logic coordinate.
    */
   public static Optional<SememeChronology<? extends SememeVersion<?>>> getInferredDefinitionChronology(int conceptId,
         LogicCoordinate logicCoordinate) {
      conceptId = Get.identifierService()
                     .getConceptNid(conceptId);
      return Get.sememeService()
                .getSememesForComponentFromAssemblage(conceptId, logicCoordinate.getInferredAssemblageSequence())
                .findAny();
   }

   /**
    * Gets the logic graph chronology.
    *
    * @param id The int sequence or NID of the Concept for which the logic graph is requested
    * @param stated boolean indicating stated vs inferred definition chronology should be used
    * @return An Optional containing a LogicGraphSememe SememeChronology
    */
   public static Optional<SememeChronology<? extends LogicGraphSememe<?>>> getLogicGraphChronology(int id,
         boolean stated) {
      LOG.debug("Getting {} logic graph chronology for {}", (stated ? "stated"
            : "inferred"), Optional.ofNullable(Frills.getIdInfo(id)));

      final Optional<SememeChronology<? extends SememeVersion<?>>> defChronologyOptional =
         stated ? Get.statedDefinitionChronology(id)
                : Get.inferredDefinitionChronology(id);

      if (defChronologyOptional.isPresent()) {
         LOG.debug("Got {} logic graph chronology for {}", (stated ? "stated"
               : "inferred"), Optional.ofNullable(Frills.getIdInfo(id)));

         @SuppressWarnings("unchecked")
         final SememeChronology<? extends LogicGraphSememe<?>> sememeChronology =
            (SememeChronology<? extends LogicGraphSememe<?>>) defChronologyOptional.get();

         return Optional.of(sememeChronology);
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
    * @return An Optional containing a LogicGraphSememe SememeChronology
    */
   public static Optional<SememeChronology<? extends LogicGraphSememe<?>>> getLogicGraphChronology(int id,
         boolean stated,
         StampCoordinate stampCoordinate,
         LanguageCoordinate languageCoordinate,
         LogicCoordinate logicCoordinate) {
      LOG.debug("Getting {} logic graph chronology for {}", (stated ? "stated"
            : "inferred"), Optional.ofNullable(Frills.getIdInfo(id, stampCoordinate, languageCoordinate)));

      final Optional<SememeChronology<? extends SememeVersion<?>>> defChronologyOptional =
         stated ? getStatedDefinitionChronology(id,
                                                logicCoordinate)
                : getInferredDefinitionChronology(id, logicCoordinate);

      if (defChronologyOptional.isPresent()) {
         LOG.debug("Got {} logic graph chronology for {}", (stated ? "stated"
               : "inferred"), Optional.ofNullable(Frills.getIdInfo(id, stampCoordinate, languageCoordinate)));

         @SuppressWarnings("unchecked")
         final SememeChronology<? extends LogicGraphSememe<?>> sememeChronology =
            (SememeChronology<? extends LogicGraphSememe<?>>) defChronologyOptional.get();

         return Optional.of(sememeChronology);
      } else {
         LOG.warn("NO {} logic graph chronology for {}", (stated ? "stated"
               : "inferred"), Optional.ofNullable(Frills.getIdInfo(id, stampCoordinate, languageCoordinate)));
         return Optional.empty();
      }
   }

   /**
    * Gets the logic graph version.
    *
    * @param logicGraphSememeChronology The SememeChronology<? extends LogicGraphSememe<?>> chronology for which the logic graph version is requested
    * @param stampCoordinate StampCoordinate to be used for selecting latest version
    * @return An Optional containing a LogicGraphSememe SememeChronology
    */
   public static Optional<LatestVersion<LogicGraphSememe<?>>> getLogicGraphVersion(
           SememeChronology<? extends LogicGraphSememe<?>> logicGraphSememeChronology,
           StampCoordinate stampCoordinate) {
      LOG.debug("Getting logic graph sememe for {}",
                Optional.ofNullable(Frills.getIdInfo(logicGraphSememeChronology.getReferencedComponentNid())));

      @SuppressWarnings({ "unchecked", "rawtypes" })
      final Optional<LatestVersion<LogicGraphSememe<?>>> latest =
         ((SememeChronology) logicGraphSememeChronology).getLatestVersion(LogicGraphSememe.class,
                                                                          stampCoordinate);

      if (latest.isPresent()) {
         LOG.debug("Got logic graph sememe for {}",
                   Optional.ofNullable(Frills.getIdInfo(logicGraphSememeChronology.getReferencedComponentNid())));
      } else {
         LOG.warn("NO logic graph sememe for {}",
                  Optional.ofNullable(Frills.getIdInfo(logicGraphSememeChronology.getReferencedComponentNid())));
      }

      return latest;
   }

   /**
    * Checks if mapping.
    *
    * @param sc the sc
    * @return true, if mapping
    */
   public static boolean isMapping(SememeChronology<? extends SememeVersion<?>> sc) {
      return definesMapping(sc.getAssemblageSequence());
   }

   /**
    * Determine if Chronology has nested sememes.
    *
    * @param chronology the chronology
    * @return true if there is a nested sememe, false otherwise
    */
   public static boolean hasNestedSememe(ObjectChronology<?> chronology) {
      return !chronology.getSememeList()
                        .isEmpty();
   }

   /**
    * Gets the nid for SCTID.
    *
    * @param sctID the sct ID
    * @return the nid for SCTID
    */
   public static Optional<Integer> getNidForSCTID(long sctID) {
      final IndexService si = LookupService.get()
                                             .getService(IndexService.class, "sememe indexer");

      if (si != null) {
         // force the prefix algorithm, and add a trailing space - quickest way to do an exact-match type of search
         final List<SearchResult> result = si.query(sctID + " ",
                                                    true,
                                                    new Integer[] { MetaData.SCTID____ISAAC.getConceptSequence() },
                                                    5,
                                                    Long.MIN_VALUE);

         if (result.size() > 0) {
            return Optional.of(Get.sememeService()
                                  .getSememe(result.get(0)
                                        .getNid())
                                  .getReferencedComponentNid());
         }
      } else {
         LOG.warn("Sememe Index not available - can't lookup SCTID");
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
      final IndexService si = LookupService.get()
                                             .getService(IndexService.class, "sememe indexer");

      if (si != null) {
         // force the prefix algorithm, and add a trailing space - quickest way to do an exact-match type of search
         final List<SearchResult> result = si.query(vuID + " ",
                                                    true,
                                                    new Integer[] { MetaData.VUID____ISAAC.getConceptSequence() },
                                                    5,
                                                    Long.MIN_VALUE);

         if (result.size() > 0) {
            return Optional.of(Get.sememeService()
                                  .getSememe(result.get(0)
                                        .getNid())
                                  .getReferencedComponentNid());
         }
      } else {
         LOG.warn("Sememe Index not available - can't lookup VUID");
      }

      return Optional.empty();
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
         final Optional<LatestVersion<StringSememeImpl>> sememe = Get.sememeService()
                                                                     .getSnapshot(StringSememeImpl.class,
                                                                           (stamp == null) ? Get.configurationService()
                                                                                 .getDefaultStampCoordinate()
               : stamp)
                                                                     .getLatestSememeVersionsForComponentFromAssemblage(
                                                                        componentNid,
                                                                              MetaData.SCTID____ISAAC.getConceptSequence())
                                                                     .findFirst();

         if (sememe.isPresent() && sememe.get().value().isPresent()) {
            return Optional.of(Long.parseLong(sememe.get()
                  .value().get()
                  .getString()));
         }
      } catch (final Exception e) {
         LOG.error("Unexpected error trying to find SCTID for nid " + componentNid, e);
      }

      return Optional.empty();
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
         stampCoordinate = Get.configurationService()
                              .getDefaultStampCoordinate();
      }

      final StampPosition stampPosition = new StampPositionImpl(stampCoordinate.getStampPosition().getTime(),
                                                                editCoordinate.getPathSequence());
      final StampCoordinateImpl temp = new StampCoordinateImpl(stampCoordinate.getStampPrecedence(),
                                                               stampPosition,
                                                               stampCoordinate.getModuleSequences(),
                                                               stampCoordinate.getAllowedStates());

      if (temp.getModuleSequences()
              .size() > 0) {
         temp.getModuleSequences()
             .add(editCoordinate.getModuleSequence());
      }

      return temp;
   }

   /**
    * Gets the stamp coordinate from stamp.
    *
    * @param stamp Stamp from which to generate StampCoordinate
    * @return StampCoordinate corresponding to Stamp values

 StampPrecedence set to StampPrecedence.TIME

 Use StampCoordinate.makeCoordinateAnalog() to customize result
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

 Use StampCoordinate.makeCoordinateAnalog() to customize result
    */
   public static StampCoordinate getStampCoordinateFromStamp(Stamp stamp, StampPrecedence precedence) {
      final StampPosition stampPosition = new StampPositionImpl(stamp.getTime(), stamp.getPathSequence());
      final StampCoordinate stampCoordinate = new StampCoordinateImpl(precedence,
                                                                      stampPosition,
                                                                      ConceptSequenceSet.of(stamp.getModuleSequence()),
                                                                      EnumSet.of(stamp.getStatus()));

      LOG.debug("Created StampCoordinate from Stamp: " + stamp + ": " + stampCoordinate);
      return stampCoordinate;
   }

   /**
    * Gets the stamp coordinate from version.
    *
    * @param version StampedVersion from which to generate StampCoordinate
    * @return StampCoordinate corresponding to StampedVersion values

 StampPrecedence set to StampPrecedence.TIME

 Use StampCoordinate.makeCoordinateAnalog() to customize result
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

 StampPrecedence set to StampPrecedence.TIME

 Use StampCoordinate.makeCoordinateAnalog() to customize result
    */
   public static StampCoordinate getStampCoordinateFromVersion(StampedVersion version, StampPrecedence precedence) {
      final StampPosition stampPosition = new StampPositionImpl(version.getTime(), version.getPathSequence());
      final StampCoordinate stampCoordinate = new StampCoordinateImpl(precedence,
                                                                      stampPosition,
                                                                      ConceptSequenceSet.of(
                                                                         version.getModuleSequence()),
                                                                      EnumSet.of(version.getState()));

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
   public static Optional<SememeChronology<? extends SememeVersion<?>>> getStatedDefinitionChronology(int conceptId,
         LogicCoordinate logicCoordinate) {
      conceptId = Get.identifierService()
                     .getConceptNid(conceptId);
      return Get.sememeService()
                .getSememesForComponentFromAssemblage(conceptId, logicCoordinate.getStatedAssemblageSequence())
                .findAny();
   }

   /**
    * Gets the version type.
    *
    * @param nid the nid
    * @return the version type
    */
   public static Class<? extends StampedVersion> getVersionType(int nid) {
      final Optional<? extends ObjectChronology<? extends StampedVersion>> obj = Get.identifiedObjectService()
                                                                                    .getIdentifiedObjectChronology(nid);

      if (!obj.isPresent()) {
         throw new RuntimeException("No StampedVersion object exists with NID=" + nid);
      }

      return getVersionType(obj.get());
   }

   /**
    * Gets the version type.
    *
    * @param obj the obj
    * @return the version type
    */
   public static Class<? extends StampedVersion> getVersionType(ObjectChronology<? extends StampedVersion> obj) {
      switch (obj.getOchreObjectType()) {
      case SEMEME: {
         @SuppressWarnings({ "rawtypes", "unchecked" })
         final SememeChronology<? extends SememeVersion> sememeChronology =
            (SememeChronology<? extends SememeVersion>) obj;

         switch (sememeChronology.getSememeType()) {
         case COMPONENT_NID:
            return ComponentNidSememeImpl.class;

         case DESCRIPTION:
            return DescriptionSememeImpl.class;

         case DYNAMIC:
            return DynamicSememeImpl.class;

         case LOGIC_GRAPH:
            return LogicGraphSememeImpl.class;

         case LONG:
            return LongSememeImpl.class;

         case STRING:
            return StringSememeImpl.class;

         case RELATIONSHIP_ADAPTOR:
            return RelationshipVersionAdaptorImpl.class;

         case UNKNOWN:
         case MEMBER:
         default:
            throw new RuntimeException("Sememe with NID=" + obj.getNid() + " is of unsupported SememeType " +
                                       sememeChronology.getSememeType());
         }
      }

      case CONCEPT:
         return ConceptVersionImpl.class;

      default:
         throw new RuntimeException("Object with NID=" + obj.getNid() +
                                    " is of unsupported OchreExternalizableObjectType " + obj.getOchreObjectType());
      }
   }

   /**
    * Find the VUID for a component (if it has one).
    *
    * @param componentNid the component nid
    * @param stamp - optional - if not provided uses default from config
    * service
    * @return the id, if found, or empty (will not return null)
    */
   public static Optional<Long> getVuId(int componentNid, StampCoordinate stamp) {
      try {
         final ArrayList<Long> vuids = new ArrayList<>(1);

         Get.sememeService()
            .getSnapshot(SememeVersion.class,
                         (stamp == null) ? Get.configurationService()
                               .getDefaultStampCoordinate()
                                         : stamp)
            .getLatestSememeVersionsForComponentFromAssemblage(componentNid, MetaData.VUID____ISAAC.getConceptSequence())
            .forEach(latestSememe -> {
            // expected path
                        if (latestSememe.value().get()
                                        .getChronology()
                                        .getSememeType() == SememeType.STRING) {
                           vuids.add(Long.parseLong(((StringSememe) latestSememe.value().get()).getString()));
                        }

                        // Data model bug path (can go away, after bug is fixed)
                        else if (latestSememe.value().get()
                                             .getChronology()
                                             .getSememeType() == SememeType.DYNAMIC) {
                           vuids.add(Long.parseLong(((DynamicSememe) latestSememe.value().get()).getData()[0]
                                 .dataToString()));
                        }
                     });

         if (vuids.size() > 1) {
            LOG.warn("Found multiple VUIDs on component " +
                     Get.identifierService().getUuidPrimordialForNid(componentNid));
         }

         if (vuids.size() > 0) {
            return Optional.of(vuids.get(0));
         }
      } catch (final Exception e) {
         LOG.error("Unexpected error trying to find VUID for nid " + componentNid, e);
      }

      return Optional.empty();
   }

   //~--- inner classes -------------------------------------------------------

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

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new id info.
       *
       * @param map the map
       */
      private IdInfo(Map<String, Object> map) {
         this.map = map;
      }

      //~--- methods ----------------------------------------------------------

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


   ;
}
//~--- JDK imports ------------------------------------------------------------
