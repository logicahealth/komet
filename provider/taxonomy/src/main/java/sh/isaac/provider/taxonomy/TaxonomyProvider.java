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



package sh.isaac.provider.taxonomy;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

//~--- JDK imports ------------------------------------------------------------

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.ConceptActiveService;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.dag.Node;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.task.TaskWrapper;
import sh.isaac.api.tree.Tree;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.logic.IsomorphicResultsBottomUp;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithSequences;
import sh.isaac.model.waitfree.CasSequenceObjectMap;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TaxonomyProvider.
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class TaxonomyProvider
         implements TaxonomyService, ConceptActiveService, ChronologyChangeListener {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   /**
    * The Constant TAXONOMY.
    */
   private static final String TAXONOMY = "taxonomy";

   /**
    * The Constant ORIGIN_DESTINATION_MAP.
    */
   private static final String ORIGIN_DESTINATION_MAP = "origin-destination.map";
   // TODO persist dataStoreId.
   private final UUID dataStoreId = UUID.randomUUID();

   @Override
   public UUID getDataStoreId() {
      return dataStoreId;
   }


   //~--- fields --------------------------------------------------------------

   /**
    * The destination origin record set.
    */
   private final ConcurrentSkipListSet<DestinationOriginRecord> destinationOriginRecordSet =
      new ConcurrentSkipListSet<>();

   /**
    * The load required.
    */
   private final AtomicBoolean loadRequired = new AtomicBoolean();

   /**
    * The logic coordinate.
    */
   private final LogicCoordinate logicCoordinate = LogicCoordinates.getStandardElProfile();

   /**
    * The isa sequence.
    */
   private final int isaSequence = TermAux.IS_A.getConceptSequence();

   /**
    * The role group sequence.
    */
   private final int roleGroupSequence = TermAux.ROLE_GROUP.getConceptSequence();

   /**
    * The provider uuid.
    */
   private final UUID providerUuid = UUID.randomUUID();

   /**
    * The semantic sequences for unhandled changes.
    */
   private final ConcurrentSkipListSet<Integer> semanticSequencesForUnhandledChanges = new ConcurrentSkipListSet<>();

   /**
    * The stamped lock.
    */
   private final StampedLock stampedLock = new StampedLock();

   /**
    * The database validity.
    */
   private DatabaseValidity databaseValidity = DatabaseValidity.NOT_SET;

   /**
    * The tree cache.
    */
   private final ConcurrentHashMap<Integer, Task<Tree>> snapshotCache = new ConcurrentHashMap<>(5);

   /**
    * The {@code taxonomyMap} associates concept sequence keys with a primitive taxonomy record, which represents the
    * destination, stamp, and taxonomy flags for parent and child concepts.
    */
   private final CasSequenceObjectMap<TaxonomyRecordPrimitive> originDestinationTaxonomyRecordMap;

   /**
    * The folder path.
    */
   private final Path folderPath;

   /**
    * The taxonomy provider folder.
    */
   private final Path taxonomyProviderFolder;

   /**
    * The identifier service.
    */
   private IdentifierService identifierService;

   //~--- constant enums ------------------------------------------------------

   /**
    * The Enum AllowedRelTypes.
    */
   private enum AllowedRelTypes {
      /**
       * Only hierarchical relationships.
       */
      HIERARCHICAL_ONLY,

      /**
       * All relationships.
       */
      ALL_RELS;
   }

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new taxonomy provider.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private TaxonomyProvider()
            throws IOException {
      this.folderPath             = LookupService.getService(ConfigurationService.class)
            .getChronicleFolderPath();
      this.taxonomyProviderFolder = this.folderPath.resolve(TAXONOMY);

      if (!Files.exists(this.taxonomyProviderFolder)) {
         this.databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
      }

      this.loadRequired.set(!Files.exists(this.taxonomyProviderFolder));
      Files.createDirectories(this.taxonomyProviderFolder);
      this.originDestinationTaxonomyRecordMap = new CasSequenceObjectMap<>(
          new TaxonomyRecordSerializer(),
          this.taxonomyProviderFolder,
          "seg.",
          ".taxonomy.map");
      LOG.info("CradleTaxonomyProvider constructed");
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Clear database validity value.
    */
   @Override
   public void clearDatabaseValidityValue() {
      // Reset to enforce analysis
      this.databaseValidity = DatabaseValidity.NOT_SET;
   }

   /**
    * Handle change.
    *
    * @param cc the cc
    */
   @Override
   public void handleChange(ConceptChronology cc) {
      // not interested on concept changes
   }

   /**
    * Handle change.
    *
    * @param sc the sc
    */
   @Override
   public void handleChange(SemanticChronology sc) {
      if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
         this.semanticSequencesForUnhandledChanges.add(sc.getSemanticSequence());
      }
   }

   /**
    * Handle commit.
    *
    * @param commitRecord the commit record
    */
   @Override
   public void handleCommit(CommitRecord commitRecord) {
      // If a logic graph changed, clear our cache.
      if (this.semanticSequencesForUnhandledChanges.size() > 0) {
         this.snapshotCache.clear();
      }

      UpdateTaxonomyAfterCommitTask.get(this, commitRecord, this.semanticSequencesForUnhandledChanges, this.stampedLock);
   }

   /**
    * Update status.
    *
    * @param conceptChronology the concept chronology
    */
   @Override
   public void updateStatus(ConceptChronology conceptChronology) {
      final int               conceptSequence = conceptChronology.getConceptSequence();
      TaxonomyRecordPrimitive parentTaxonomyRecord;

      if (this.originDestinationTaxonomyRecordMap.containsKey(conceptSequence)) {
         parentTaxonomyRecord = this.originDestinationTaxonomyRecordMap.get(conceptSequence)
               .get();
      } else {
         parentTaxonomyRecord = new TaxonomyRecordPrimitive();
      }
       for (int stampSequence: conceptChronology.getVersionStampSequences()) {
                             parentTaxonomyRecord.getTaxonomyRecordUnpacked()
                                    .addStampRecord(
                                        conceptSequence,
                                        conceptSequence,
                                        stampSequence,
                                        TaxonomyFlag.CONCEPT_STATUS.bits);
       }
       
      this.originDestinationTaxonomyRecordMap.put(conceptSequence, parentTaxonomyRecord);
   }

   /**
    * Update taxonomy.
    *
    * @param logicGraphChronology the logic graph chronology
    */
   @Override
   public void updateTaxonomy(SemanticChronology logicGraphChronology) {
      final int conceptSequence = this.identifierService.getConceptSequence(
                                      logicGraphChronology.getReferencedComponentNid());
      final Optional<TaxonomyRecordPrimitive> record = this.originDestinationTaxonomyRecordMap.get(conceptSequence);
      TaxonomyRecordPrimitive                 parentTaxonomyRecord;

      if (record.isPresent()) {
         parentTaxonomyRecord = record.get();
      } else {
         parentTaxonomyRecord = new TaxonomyRecordPrimitive();
      }

      TaxonomyFlag taxonomyFlags;

      if (logicGraphChronology.getAssemblageSequence() == this.logicCoordinate.getInferredAssemblageSequence()) {
         taxonomyFlags = TaxonomyFlag.INFERRED;
      } else {
         taxonomyFlags = TaxonomyFlag.STATED;
      }

      final List<Graph<LogicGraphVersion>> versionGraphList = logicGraphChronology.getVersionGraphList();

      versionGraphList.forEach(
          (versionGraph) -> {
             processVersionNode(versionGraph.getRoot(), parentTaxonomyRecord, taxonomyFlags);
          });
      this.originDestinationTaxonomyRecordMap.put(conceptSequence, parentTaxonomyRecord);
   }

   /**
    * Was ever kind of.
    *
    * @param childId the child id
    * @param parentId the parent id
    * @return true, if successful
    */
   @Override
   public boolean wasEverKindOf(int childId, int parentId) {
      childId  = Get.identifierService()
                    .getConceptSequence(childId);
      parentId = Get.identifierService()
                    .getConceptSequence(parentId);

      if (childId == parentId) {
         return true;
      }

      long          stamp         = this.stampedLock.tryOptimisticRead();
      final boolean wasEverKindOf = recursiveFindAncestor(childId, parentId, new NidSet());

      if (this.stampedLock.validate(stamp)) {
         return wasEverKindOf;
      }

      stamp = this.stampedLock.readLock();

      try {
         return recursiveFindAncestor(childId, parentId, new NidSet());
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   /**
    * Filter origin sequences.
    *
    * @param origins the origins
    * @param parentSequence the parent sequence
    * @param typeSequenceSet the type sequence set
    * @return the int stream
    */
   private IntStream filterOriginSequences(IntStream origins, int parentSequence, ConceptSequenceSet typeSequenceSet) {
      return origins.filter(
          (originSequence) -> {
             final Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                this.originDestinationTaxonomyRecordMap.get(
                    originSequence);

             if (taxonomyRecordOptional.isPresent()) {
                final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                return taxonomyRecord.containsSequenceViaType(parentSequence, typeSequenceSet, TaxonomyFlag.ALL_RELS);
             }

             return false;
          });
   }

   /**
    * Filter origin sequences.
    *
    * @param origins the origins
    * @param parentSequence the parent sequence
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @return the int stream
    */
   private IntStream filterOriginSequences(IntStream origins,
         int parentSequence,
         ConceptSequenceSet typeSequenceSet,
         ManifoldCoordinate tc) {
      return origins.filter(
          (originSequence) -> {
             final Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                this.originDestinationTaxonomyRecordMap.get(
                    originSequence);

             if (taxonomyRecordOptional.isPresent()) {
                final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                if (taxonomyRecord.conceptSatisfiesStamp(originSequence, tc.getStampCoordinate())) {
                   return taxonomyRecord.containsSequenceViaType(
                       parentSequence,
                       typeSequenceSet,
                       tc,
                       TaxonomyFlag.ALL_RELS);
                }
             }

             return false;
          });
   }

   /**
    * Filter origin sequences.
    *
    * @param origins the origins
    * @param parentSequence the parent sequence
    * @param typeSequence the type sequence
    * @param flags the flags
    * @return the int stream
    */
   private IntStream filterOriginSequences(IntStream origins, int parentSequence, int typeSequence, int flags) {
      return origins.filter(
          (originSequence) -> {
             final Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                this.originDestinationTaxonomyRecordMap.get(
                    originSequence);

             if (taxonomyRecordOptional.isPresent()) {
                final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                return taxonomyRecord.containsSequenceViaTypeWithFlags(parentSequence, typeSequence, flags);
             }

             return false;
          });
   }

   /**
    * Filter origin sequences.
    *
    * @param origins the origins
    * @param parentSequence the parent sequence
    * @param typeSequence the type sequence
    * @param tc the tc
    * @param allowedRelTypes the allowed rel types
    * @return the int stream
    */
   private IntStream filterOriginSequences(IntStream origins,
         int parentSequence,
         int typeSequence,
         ManifoldCoordinate tc,
         AllowedRelTypes allowedRelTypes) {
      return origins.filter(
          (originSequence) -> {
             final Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                this.originDestinationTaxonomyRecordMap.get(
                    originSequence);

             if (taxonomyRecordOptional.isPresent()) {
                final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                if (taxonomyRecord.conceptSatisfiesStamp(originSequence, tc.getStampCoordinate())) {
                   if (allowedRelTypes == AllowedRelTypes.ALL_RELS) {
                      return taxonomyRecord.containsSequenceViaType(
                          parentSequence,
                          typeSequence,
                          tc,
                          TaxonomyFlag.ALL_RELS);
                   }

                   return taxonomyRecord.containsSequenceViaType(parentSequence, typeSequence, tc);
                }
             }

             return false;
          });
   }

   /**
    * Process new logic graph.
    *
    * @param firstVersion the first version
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    */
   private void processNewLogicGraph(LogicGraphVersion firstVersion,
                                     TaxonomyRecordPrimitive parentTaxonomyRecord,
                                     TaxonomyFlag taxonomyFlags) {
      if (firstVersion.getCommitState() == CommitStates.COMMITTED) {
         final LogicalExpression expression = firstVersion.getLogicalExpression();

         expression.getRoot()
                   .getChildStream()
                   .forEach(
                       (necessaryOrSufficientSet) -> {
                          necessaryOrSufficientSet.getChildStream()
                                .forEach(
                                    (LogicNode andOrOrLogicNode) -> andOrOrLogicNode.getChildStream()
                                          .forEach(
                                              (LogicNode aLogicNode) -> {
                                                 processRelationshipRoot(
                                                       aLogicNode,
                                                             parentTaxonomyRecord,
                                                             taxonomyFlags,
                                                             firstVersion.getStampSequence(),
                                                             expression);
                                              }));
                       });
      }
   }

   /**
    * Process relationship root.
    *
    * @param logicalLogicNode the logical logic node
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    * @param stampSequence the stamp sequence
    * @param comparisonExpression the comparison expression
    */
   private void processRelationshipRoot(LogicNode logicalLogicNode,
         TaxonomyRecordPrimitive parentTaxonomyRecord,
         TaxonomyFlag taxonomyFlags,
         int stampSequence,
         LogicalExpression comparisonExpression) {
      switch (logicalLogicNode.getNodeSemantic()) {
      case CONCEPT:
         updateIsaRel(
             (ConceptNodeWithSequences) logicalLogicNode,
             parentTaxonomyRecord,
             taxonomyFlags,
             stampSequence,
             comparisonExpression.getConceptSequence());
         break;

      case ROLE_SOME:
         updateSomeRole(
             (RoleNodeSomeWithSequences) logicalLogicNode,
             parentTaxonomyRecord,
             taxonomyFlags,
             stampSequence,
             comparisonExpression.getConceptSequence());
         break;

      case FEATURE:

         // Features do not have taxonomy implications...
         break;

      default:
         throw new UnsupportedOperationException("at Can't handle: " + logicalLogicNode.getNodeSemantic());
      }
   }

   /**
    * Process version node.
    *
    * @param node the node
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    */
   private void processVersionNode(Node<? extends LogicGraphVersion> node,
                                   TaxonomyRecordPrimitive parentTaxonomyRecord,
                                   TaxonomyFlag taxonomyFlags) {
      if (node.getParent() == null) {
         processNewLogicGraph(node.getData(), parentTaxonomyRecord, taxonomyFlags);
      } else {
         final LogicalExpression comparisonExpression = node.getParent()
                                                            .getData()
                                                            .getLogicalExpression();
         final LogicalExpression referenceExpression  = node.getData()
                                                            .getLogicalExpression();
         final IsomorphicResultsBottomUp isomorphicResults = new IsomorphicResultsBottomUp(
                                                                 referenceExpression,
                                                                       comparisonExpression);

         isomorphicResults.getAddedRelationshipRoots()
                          .forEach(
                              (logicalNode) -> {
                                 final int stampSequence = node.getData()
                                                               .getStampSequence();

                                 processRelationshipRoot(
                                     logicalNode,
                                     parentTaxonomyRecord,
                                     taxonomyFlags,
                                     stampSequence,
                                     comparisonExpression);
                              });
         isomorphicResults.getDeletedRelationshipRoots()
                          .forEach(
                              (logicalNode) -> {
                                 final int activeStampSequence = node.getData()
                                                                     .getStampSequence();
                                 final int stampSequence       = Get.stampService()
                                                                    .getRetiredStampSequence(activeStampSequence);

                                 processRelationshipRoot(
                                     logicalNode,
                                     parentTaxonomyRecord,
                                     taxonomyFlags,
                                     stampSequence,
                                     comparisonExpression);
                              });
      }

      node.getChildren()
          .forEach(
              (childNode) -> {
                 processVersionNode(childNode, parentTaxonomyRecord, taxonomyFlags);
              });
   }

   /**
    * Recursive find ancestor.
    *
    * @param childSequence the child sequence
    * @param parentSequence the parent sequence
    * @param tc the tc
    * @return true, if successful
    */
   private boolean recursiveFindAncestor(int childSequence, int parentSequence, ManifoldCoordinate tc) {
      // currently unpacking from array to object.
      // TODO operate directly on array if unpacking is a performance bottleneck.
      final Optional<TaxonomyRecordPrimitive> record = this.originDestinationTaxonomyRecordMap.get(childSequence);

      if (record.isPresent()) {
         final TaxonomyRecord childTaxonomyRecords = new TaxonomyRecord(record.get().getArray());
         final int[] activeConceptSequences = childTaxonomyRecords.getConceptSequencesForType(this.isaSequence, tc);

         for (int parentSequenceOfType: activeConceptSequences) {
            if (parentSequenceOfType == parentSequence) {
               return true;
            }
         }

         for (int intermediateChild: activeConceptSequences) {
            if (recursiveFindAncestor(intermediateChild, parentSequence, tc)) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Recursive find ancestor.
    *
    * @param childSequence the child sequence
    * @param parentSequence the parent sequence
    * @param examined the examined
    * @return true, if successful
    */
   private boolean recursiveFindAncestor(int childSequence, int parentSequence, NidSet examined) {
      // currently unpacking from array to object.
      // TODO operate directly on array if unpacking is a performance bottleneck.
      if (examined.contains(childSequence)) {
         return false;
      }

      examined.add(childSequence);

      final Optional<TaxonomyRecordPrimitive> record = this.originDestinationTaxonomyRecordMap.get(childSequence);

      if (record.isPresent()) {
         final TaxonomyRecord childTaxonomyRecords = new TaxonomyRecord(record.get().getArray());
         final int[] conceptSequencesForType = childTaxonomyRecords.getConceptSequencesForType(this.isaSequence);

         for (int parentSequenceOfType: conceptSequencesForType) {
            if (parentSequenceOfType == parentSequence) {
               return true;
            }
         }

         for (int intermediateChild: conceptSequencesForType) {
            if (recursiveFindAncestor(intermediateChild, parentSequence, examined)) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Recursive find ancestors.
    *
    * @param childSequence the child sequence
    * @param ancestors the ancestors
    * @param tc the tc
    */
   private void recursiveFindAncestors(int childSequence, ConceptSequenceSet ancestors, ManifoldCoordinate tc) {
      // currently unpacking from array to object.
      // TODO operate directly on array if unpacking is a performance bottleneck.
      final Optional<TaxonomyRecordPrimitive> record = this.originDestinationTaxonomyRecordMap.get(childSequence);

      if (record.isPresent()) {
         final TaxonomyRecord childTaxonomyRecords = new TaxonomyRecord(record.get().getArray());
         final int[] activeConceptSequences = childTaxonomyRecords.getConceptSequencesForType(this.isaSequence, tc);

         Arrays.stream(activeConceptSequences)
               .forEach(
                   (parent) -> {
                      if (!ancestors.contains(parent)) {
                         ancestors.add(parent);
                         recursiveFindAncestors(parent, ancestors, tc);
                      }
                   });
      }
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting TaxonomyService post-construct");

         if (!this.loadRequired.get()) {
            LOG.info("Reading taxonomy.");

            final boolean isPopulated = this.originDestinationTaxonomyRecordMap.initialize();
            final File    inputFile   = new File(this.taxonomyProviderFolder.toFile(), ORIGIN_DESTINATION_MAP);

            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)))) {
               final int size = in.readInt();

               for (int i = 0; i < size; i++) {
                  this.destinationOriginRecordSet.add(new DestinationOriginRecord(in.readInt(), in.readInt()));
               }
            }

            if (isPopulated) {
               this.databaseValidity = DatabaseValidity.POPULATED_DIRECTORY;
            }
         }

         Get.commitService()
            .addChangeListener(this);
         this.identifierService = Get.identifierService();
      } catch (final Exception e) {
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Cradle Taxonomy Provider", e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Writing taxonomy.");
      this.originDestinationTaxonomyRecordMap.write();

      final File outputFile = new File(this.taxonomyProviderFolder.toFile(), ORIGIN_DESTINATION_MAP);

      outputFile.getParentFile()
                .mkdirs();

      try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
         out.writeInt(this.destinationOriginRecordSet.size());
         this.destinationOriginRecordSet.forEach(
             (rec) -> {
                try {
                   out.writeInt(rec.getDestinationSequence());
                   out.writeInt(rec.getOriginSequence());
                } catch (final IOException ex) {
                   throw new RuntimeException(ex);
                }
             });
      } catch (final IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Update isa rel.
    *
    * @param conceptNode the concept node
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    * @param stampSequence the stamp sequence
    * @param originSequence the origin sequence
    */
   private void updateIsaRel(ConceptNodeWithSequences conceptNode,
                             TaxonomyRecordPrimitive parentTaxonomyRecord,
                             TaxonomyFlag taxonomyFlags,
                             int stampSequence,
                             int originSequence) {
      parentTaxonomyRecord.getTaxonomyRecordUnpacked()
                          .addStampRecord(
                              conceptNode.getConceptSequence(),
                              this.isaSequence,
                              stampSequence,
                              taxonomyFlags.bits);
      this.destinationOriginRecordSet.add(
          new DestinationOriginRecord(conceptNode.getConceptSequence(), originSequence));
   }

   /**
    * Update some role.
    *
    * @param someNode the some node
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    * @param stampSequence the stamp sequence
    * @param originSequence the origin sequence
    */
   private void updateSomeRole(RoleNodeSomeWithSequences someNode,
                               TaxonomyRecordPrimitive parentTaxonomyRecord,
                               TaxonomyFlag taxonomyFlags,
                               int stampSequence,
                               int originSequence) {
      if (someNode.getTypeConceptSequence() == this.roleGroupSequence) {
         final AndNode andNode = (AndNode) someNode.getOnlyChild();

         andNode.getChildStream()
                .forEach(
                    (roleGroupSomeNode) -> {
                       if (roleGroupSomeNode instanceof RoleNodeSomeWithSequences) {
                          updateSomeRole(
                              (RoleNodeSomeWithSequences) roleGroupSomeNode,
                              parentTaxonomyRecord,
                              taxonomyFlags,
                              stampSequence,
                              originSequence);
                       } else {
                          // TODO Dan put this here to stop a pile of errors....
                          // one of the types coming back was a FeatureNodeWithSequences - not sure what to do with it.
                       }
                    });
      } else {
         if (someNode.getOnlyChild() instanceof ConceptNodeWithSequences) {
            final ConceptNodeWithSequences restrictionNode = (ConceptNodeWithSequences) someNode.getOnlyChild();

            parentTaxonomyRecord.getTaxonomyRecordUnpacked()
                                .addStampRecord(
                                    restrictionNode.getConceptSequence(),
                                    someNode.getTypeConceptSequence(),
                                    stampSequence,
                                    taxonomyFlags.bits);
            this.destinationOriginRecordSet.add(
                new DestinationOriginRecord(restrictionNode.getConceptSequence(), originSequence));
         } else {
            // TODO dan put this here to stop a pile of errors. It was returning AndNode.  Not sure what to do with it
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the all relationship origin sequences of type.
    *
    * @param destinationId the destination id
    * @param typeSequenceSet the type sequence set
    * @return the all relationship origin sequences of type
    */
   @Override
   public IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet) {
      destinationId = Get.identifierService()
                         .getConceptSequence(destinationId);

      long stamp = this.stampedLock.tryOptimisticRead();

      // Set of all concept sequences that point to the parent.
      IntStream origins = getOriginSequenceStream(destinationId);

      if (this.stampedLock.validate(stamp)) {
         return filterOriginSequences(origins, destinationId, typeSequenceSet);
      }

      stamp = this.stampedLock.readLock();

      try {
         origins = getOriginSequenceStream(destinationId);
         return filterOriginSequences(origins, destinationId, typeSequenceSet);
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   /**
    * Checks if concept active.
    *
    * @param conceptSequence the concept sequence
    * @param stampCoordinate the stamp coordinate
    * @return true, if concept active
    */
   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      long stamp = this.stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(
                                                                     conceptSequence);

      if (this.stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .isConceptActive(conceptSequence, stampCoordinate);
         }

         return false;
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(conceptSequence);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .isConceptActive(conceptSequence, stampCoordinate);
         }

         return false;
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   /**
    * Gets the database folder.
    *
    * @return the database folder
    */
   @Override
   public Path getDatabaseFolder() {
      return this.taxonomyProviderFolder;
   }

   /**
    * Gets the database validity status.
    *
    * @return the database validity status
    */
   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return this.databaseValidity;
   }

   /**
    * Gets the destination origin record set.
    *
    * @return the destination origin record set
    */
   public ConcurrentSkipListSet<DestinationOriginRecord> getDestinationOriginRecordSet() {
      return this.destinationOriginRecordSet;
   }

   /**
    * Gets the listener uuid.
    *
    * @return the listener uuid
    */
   @Override
   public UUID getListenerUuid() {
      return this.providerUuid;
   }

   /**
    * Gets the origin destination taxonomy records.
    *
    * @return the origin destination taxonomy records
    */
   public CasSequenceObjectMap<TaxonomyRecordPrimitive> getOriginDestinationTaxonomyRecords() {
      return this.originDestinationTaxonomyRecordMap;
   }

   /**
    * Gets the origin sequence stream.
    *
    * @param parentId the parent id
    * @return the origin sequence stream
    */
   private IntStream getOriginSequenceStream(int parentId) {
      // Set of all concept sequences that point to the parent.
      parentId = Get.identifierService()
                    .getConceptSequence(parentId);

      long stamp = this.stampedLock.tryOptimisticRead();
      NavigableSet<DestinationOriginRecord> subSet = this.destinationOriginRecordSet.subSet(
                                                         new DestinationOriginRecord(parentId, Integer.MIN_VALUE),
                                                               new DestinationOriginRecord(
                                                                     parentId,
                                                                           Integer.MAX_VALUE));

      if (this.stampedLock.validate(stamp)) {
         return subSet.stream()
                      .mapToInt((DestinationOriginRecord record) -> record.getOriginSequence());
      }

      stamp = this.stampedLock.readLock();

      try {
         subSet = this.destinationOriginRecordSet.subSet(
             new DestinationOriginRecord(parentId, Integer.MIN_VALUE),
             new DestinationOriginRecord(parentId, Integer.MAX_VALUE));
         return subSet.stream()
                      .mapToInt((DestinationOriginRecord record) -> record.getOriginSequence());
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   /**
    * Gets the snapshot.
    *
    * @param tc the tc
    * @return the snapshot
    */
   @Override
   public Task<TaxonomySnapshotService> getSnapshot(ManifoldCoordinate tc) {
      
      Task<Tree> treeTask = getTaxonomyTree(tc);
      Task<TaxonomySnapshotService> getSnapshotTask = 
              new TaskWrapper<>(treeTask, (t) -> {
               return new TaxonomySnapshotProvider(tc, t);
            }, "Generating taxonomy snapshot");

      Get.executor()
         .execute(getSnapshotTask);
      return getSnapshotTask;
   }

   /**
    * Gets the taxonomy child sequences.
    *
    * @param parentId the parent id
    * @return the taxonomy child sequences
    */
   @Override
   public IntStream getTaxonomyChildSequences(int parentId) {
      // Set of all concept sequences that point to the parent.
      // lock handled by getOriginSequenceStream
      final IntStream origins = getOriginSequenceStream(parentId);

      return filterOriginSequences(origins, parentId, this.isaSequence, TaxonomyFlag.ALL_RELS);
   }

   /**
    * Gets the taxonomy parent sequences.
    *
    * @param childId the child id
    * @return the taxonomy parent sequences
    */
   @Override
   public IntStream getTaxonomyParentSequences(int childId) {
      childId = Get.identifierService()
                   .getConceptSequence(childId);

      long                              stamp                  = this.stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(childId);

      if (this.stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

            return taxonomyRecord.getParentSequences()
                                 .distinct();
         }

         return IntStream.empty();
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(childId);

         if (taxonomyRecordOptional.isPresent()) {
            final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

            return taxonomyRecord.getParentSequences()
                                 .distinct();
         }

         return IntStream.empty();
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   /**
    * Gets the taxonomy tree.
    *
    * @param tc the tc
    * @return the taxonomy tree
    */
   public Task<Tree> getTaxonomyTree(ManifoldCoordinate tc) {
      // TODO determine if the returned tree is thread safe for multiple accesses in parallel, if not, may need a pool of these.
      final Task<Tree> treeTask = this.snapshotCache.get(tc.hashCode());

      if (treeTask != null) {
         return treeTask;
      }

      TreeBuilderTask treeBuilderTask = new TreeBuilderTask(originDestinationTaxonomyRecordMap, tc, stampedLock);
//      Task<Tree>      previousTask    = this.snapshotCache.putIfAbsent(tc.hashCode(), treeBuilderTask);
//
//      if (previousTask != null) {
//         return previousTask;
//      }

      Get.executor()
         .execute(treeBuilderTask);
      return treeBuilderTask;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class TaxonomySnapshotProvider.
    */
   private class TaxonomySnapshotProvider
            implements TaxonomySnapshotService {
      /**
       * The tc.
       */
      final ManifoldCoordinate tc;
      final Tree               treeSnapshot;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new taxonomy snapshot provider.
       *
       * @param tc the tc
       */
      public TaxonomySnapshotProvider(ManifoldCoordinate tc, Tree treeSnapshot) {
         this.tc           = tc;
         this.treeSnapshot = treeSnapshot;
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Checks if child of.
       *
       * @param childId the child id
       * @param parentId the parent id
       * @return true, if child of
       */
      @Override
      public boolean isChildOf(int childId, int parentId) {
         return this.treeSnapshot.isChildOf(childId, parentId);
      }

      /**
       * Checks if kind of.
       *
       * @param childId the child id
       * @param parentId the parent id
       * @return true, if kind of
       */
      @Override
      public boolean isKindOf(int childId, int parentId) {
         return this.treeSnapshot.isDescendentOf(childId, parentId);
      }

      /**
       * Gets the kind of sequence set.
       *
       * @param rootId the root id
       * @return the kind of sequence set
       */
      @Override
      public ConceptSequenceSet getKindOfSequenceSet(int rootId) {
         ConceptSequenceSet kindOfSet = this.treeSnapshot.getDescendentSequenceSet(rootId);
         kindOfSet.add(rootId);
         return kindOfSet;
      }

      @Override
      public ManifoldCoordinate getManifoldCoordinate() {
         return this.tc;
      }

      /**
       * Gets the roots.
       *
       * @return the roots
       */
      @Override
      public int[] getRoots() {
         return treeSnapshot.getRootSequences();
      }

      /**
       * Gets the taxonomy child sequences.
       *
       * @param parentId the parent id
       * @return the taxonomy child sequences
       */
      @Override
      public int[] getTaxonomyChildSequences(int parentId) {
         return this.treeSnapshot.getChildrenSequenceStream(parentId).toArray();
      }

      /**
       * Gets the taxonomy parent sequences.
       *
       * @param childId the child id
       * @return the taxonomy parent sequences
       */
      @Override
      public int[] getTaxonomyParentSequences(int childId) {
         return this.treeSnapshot.getParentSequences(childId);
      }

      /**
       * Gets the taxonomy tree.
       *
       * @return the taxonomy tree
       */
      @Override
      public Tree getTaxonomyTree() {
         return this.treeSnapshot;
      }
   }
}

