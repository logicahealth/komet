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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.*;
import sh.isaac.api.DatabaseServices.DatabaseValidity;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.LruCache;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.dag.Node;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.api.tree.hashtree.HashTreeBuilder;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.logic.IsomorphicResultsBottomUp;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithSequences;
import sh.isaac.model.waitfree.CasSequenceObjectMap;
import sh.isaac.provider.taxonomy.graph.GraphCollector;

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
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /** The Constant TAXONOMY. */
   private static final String TAXONOMY = "taxonomy";

   /** The Constant ORIGIN_DESTINATION_MAP. */
   private static final String ORIGIN_DESTINATION_MAP = "origin-destination.map";

   //~--- fields --------------------------------------------------------------

   /** The destination origin record set. */
   private final ConcurrentSkipListSet<DestinationOriginRecord> destinationOriginRecordSet =
      new ConcurrentSkipListSet<>();

   /** The load required. */
   private final AtomicBoolean loadRequired = new AtomicBoolean();

   /** The logic coordinate. */
   private final LogicCoordinate logicCoordinate = LogicCoordinates.getStandardElProfile();

   /** The isa sequence. */
   private final int isaSequence = TermAux.IS_A.getConceptSequence();

   /** The role group sequence. */
   private final int roleGroupSequence = TermAux.ROLE_GROUP.getConceptSequence();

   /** The provider uuid. */
   private final UUID providerUuid = UUID.randomUUID();

   /** The sememe sequences for unhandled changes. */
   private final ConcurrentSkipListSet<Integer> sememeSequencesForUnhandledChanges = new ConcurrentSkipListSet<>();

   /** The stamped lock. */
   private final StampedLock stampedLock = new StampedLock();

   /** The database validity. */
   private DatabaseValidity databaseValidity = DatabaseValidity.NOT_SET;

   /** The tree cache. */
   private final LruCache<Integer, Tree> treeCache = new LruCache<>(5);

   /**
    * The {@code taxonomyMap} associates concept sequence keys with a primitive
    * taxonomy record, which represents the destination, stamp, and taxonomy
    * flags for parent and child concepts.
    */
   private final CasSequenceObjectMap<TaxonomyRecordPrimitive> originDestinationTaxonomyRecordMap;

   /** The folder path. */
   private final Path folderPath;

   /** The taxonomy provider folder. */
   private final Path taxonomyProviderFolder;

   /** The identifier service. */
   private IdentifierService identifierService;

   //~--- constant enums ------------------------------------------------------

   /**
    * The Enum AllowedRelTypes.
    */
   private enum AllowedRelTypes {
      /** The hierarchical only. */
      HIERARCHICAL_ONLY,

      /** The all rels. */
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
      this.originDestinationTaxonomyRecordMap = new CasSequenceObjectMap<>(new TaxonomyRecordSerializer(),
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
   public void handleChange(ConceptChronology<? extends StampedVersion> cc) {
      // not interested on concept changes
   }

   /**
    * Handle change.
    *
    * @param sc the sc
    */
   @Override
   public void handleChange(SememeChronology<? extends SememeVersion<?>> sc) {
      if (sc.getSememeType() == SememeType.LOGIC_GRAPH) {
         this.sememeSequencesForUnhandledChanges.add(sc.getSememeSequence());
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
      if (this.sememeSequencesForUnhandledChanges.size() > 0) {
         this.treeCache.clear();
      }

      UpdateTaxonomyAfterCommitTask.get(this, commitRecord, this.sememeSequencesForUnhandledChanges, this.stampedLock);
   }

   /**
    * Update status.
    *
    * @param conceptChronology the concept chronology
    */
   @Override
   public void updateStatus(ConceptChronology<?> conceptChronology) {
      final int               conceptSequence = conceptChronology.getConceptSequence();
      TaxonomyRecordPrimitive parentTaxonomyRecord;

      if (this.originDestinationTaxonomyRecordMap.containsKey(conceptSequence)) {
         parentTaxonomyRecord = this.originDestinationTaxonomyRecordMap.get(conceptSequence)
               .get();
      } else {
         parentTaxonomyRecord = new TaxonomyRecordPrimitive();
      }

      conceptChronology.getVersionStampSequences().forEach((stampSequence) -> {
                                   parentTaxonomyRecord.getTaxonomyRecordUnpacked()
                                         .addStampRecord(conceptSequence,
                                               conceptSequence,
                                               stampSequence,
                                               TaxonomyFlags.CONCEPT_STATUS.bits);
                                });
      this.originDestinationTaxonomyRecordMap.put(conceptSequence, parentTaxonomyRecord);
   }

   /**
    * Update taxonomy.
    *
    * @param logicGraphChronology the logic graph chronology
    */
   @Override
   public void updateTaxonomy(SememeChronology<LogicGraphSememe<?>> logicGraphChronology) {
      final int conceptSequence =
         this.identifierService.getConceptSequence(logicGraphChronology.getReferencedComponentNid());
      final Optional<TaxonomyRecordPrimitive> record = this.originDestinationTaxonomyRecordMap.get(conceptSequence);
      TaxonomyRecordPrimitive                 parentTaxonomyRecord;

      if (record.isPresent()) {
         parentTaxonomyRecord = record.get();
      } else {
         parentTaxonomyRecord = new TaxonomyRecordPrimitive();
      }

      TaxonomyFlags taxonomyFlags;

      if (logicGraphChronology.getAssemblageSequence() == this.logicCoordinate.getInferredAssemblageSequence()) {
         taxonomyFlags = TaxonomyFlags.INFERRED;
      } else {
         taxonomyFlags = TaxonomyFlags.STATED;
      }

      final List<Graph<? extends LogicGraphSememe<?>>> versionGraphList = logicGraphChronology.getVersionGraphList();

      versionGraphList.forEach((versionGraph) -> {
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
      final boolean wasEverKindOf = recursiveFindAncestor(childId, parentId, new HashSet<>());

      if (this.stampedLock.validate(stamp)) {
         return wasEverKindOf;
      }

      stamp = this.stampedLock.readLock();

      try {
         return recursiveFindAncestor(childId, parentId, new HashSet<>());
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
      return origins.filter((originSequence) -> {
                               final Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                                  this.originDestinationTaxonomyRecordMap.get(originSequence);

                               if (taxonomyRecordOptional.isPresent()) {
                                  final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                                  return taxonomyRecord.containsSequenceViaType(parentSequence,
                                        typeSequenceSet,
                                        TaxonomyFlags.ALL_RELS);
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
         TaxonomyCoordinate tc) {
      return origins.filter((originSequence) -> {
                               final Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                                  this.originDestinationTaxonomyRecordMap.get(originSequence);

                               if (taxonomyRecordOptional.isPresent()) {
                                  final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                                  if (taxonomyRecord.conceptSatisfiesStamp(originSequence, tc.getStampCoordinate())) {
                                     return taxonomyRecord.containsSequenceViaType(parentSequence,
                                           typeSequenceSet,
                                           tc,
                                           TaxonomyFlags.ALL_RELS);
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
      return origins.filter((originSequence) -> {
                               final Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                                  this.originDestinationTaxonomyRecordMap.get(originSequence);

                               if (taxonomyRecordOptional.isPresent()) {
                                  final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                                  return taxonomyRecord.containsSequenceViaTypeWithFlags(parentSequence,
                                        typeSequence,
                                        flags);
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
         TaxonomyCoordinate tc,
         AllowedRelTypes allowedRelTypes) {
      return origins.filter((originSequence) -> {
                               final Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                                  this.originDestinationTaxonomyRecordMap.get(originSequence);

                               if (taxonomyRecordOptional.isPresent()) {
                                  final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                                  if (taxonomyRecord.conceptSatisfiesStamp(originSequence, tc.getStampCoordinate())) {
                                     if (allowedRelTypes == AllowedRelTypes.ALL_RELS) {
                                        return taxonomyRecord.containsSequenceViaType(parentSequence,
                                              typeSequence,
                                              tc,
                                              TaxonomyFlags.ALL_RELS);
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
   private void processNewLogicGraph(LogicGraphSememe firstVersion,
                                     TaxonomyRecordPrimitive parentTaxonomyRecord,
                                     TaxonomyFlags taxonomyFlags) {
      if (firstVersion.getCommitState() == CommitStates.COMMITTED) {
         final LogicalExpression expression = firstVersion.getLogicalExpression();

         expression.getRoot().getChildStream().forEach((necessaryOrSufficientSet) -> {
                               necessaryOrSufficientSet.getChildStream()
                                     .forEach((LogicNode andOrOrLogicNode) -> andOrOrLogicNode.getChildStream()
                                           .forEach((LogicNode aLogicNode) -> {
                     processRelationshipRoot(
                         aLogicNode, parentTaxonomyRecord, taxonomyFlags, firstVersion.getStampSequence(), expression);
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
         TaxonomyFlags taxonomyFlags,
         int stampSequence,
         LogicalExpression comparisonExpression) {
      switch (logicalLogicNode.getNodeSemantic()) {
      case CONCEPT:
         updateIsaRel((ConceptNodeWithSequences) logicalLogicNode,
                      parentTaxonomyRecord,
                      taxonomyFlags,
                      stampSequence,
                      comparisonExpression.getConceptSequence());
         break;

      case ROLE_SOME:
         updateSomeRole((RoleNodeSomeWithSequences) logicalLogicNode,
                        parentTaxonomyRecord,
                        taxonomyFlags,
                        stampSequence,
                        comparisonExpression.getConceptSequence());
         break;

      case FEATURE:

         // Features do not have taxonomy implications...
         break;

      default:
         throw new UnsupportedOperationException("Can't handle: " + logicalLogicNode.getNodeSemantic());
      }
   }

   /**
    * Process version node.
    *
    * @param node the node
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    */
   private void processVersionNode(Node<? extends LogicGraphSememe> node,
                                   TaxonomyRecordPrimitive parentTaxonomyRecord,
                                   TaxonomyFlags taxonomyFlags) {
      if (node.getParent() == null) {
         processNewLogicGraph(node.getData(), parentTaxonomyRecord, taxonomyFlags);
      } else {
         final LogicalExpression comparisonExpression = node.getParent()
                                                            .getData()
                                                            .getLogicalExpression();
         final LogicalExpression referenceExpression  = node.getData()
                                                            .getLogicalExpression();
         final IsomorphicResultsBottomUp isomorphicResults = new IsomorphicResultsBottomUp(referenceExpression,
                                                                                           comparisonExpression);

         isomorphicResults.getAddedRelationshipRoots().forEach((logicalNode) -> {
                                      final int stampSequence = node.getData()
                                                                    .getStampSequence();

                                      processRelationshipRoot(logicalNode,
                                            parentTaxonomyRecord,
                                            taxonomyFlags,
                                            stampSequence,
                                            comparisonExpression);
                                   });
         isomorphicResults.getDeletedRelationshipRoots().forEach((logicalNode) -> {
                                      final int activeStampSequence = node.getData()
                                                                          .getStampSequence();
                                      final int stampSequence       = Get.stampService()
                                                                         .getRetiredStampSequence(activeStampSequence);

                                      processRelationshipRoot(logicalNode,
                                            parentTaxonomyRecord,
                                            taxonomyFlags,
                                            stampSequence,
                                            comparisonExpression);
                                   });
      }

      node.getChildren().forEach((childNode) -> {
                      processVersionNode(childNode, parentTaxonomyRecord, taxonomyFlags);
                   });
   }

   /**
    * Recursive find ancestor.
    *
    * @param childSequence the child sequence
    * @param parentSequence the parent sequence
    * @param examined the examined
    * @return true, if successful
    */
   private boolean recursiveFindAncestor(int childSequence, int parentSequence, HashSet<Integer> examined) {
      // currently unpacking from array to object.
      // TODO operate directly on array if unpacking is a performance bottleneck.
      if (examined.contains(childSequence)) {
         return false;
      }

      examined.add(childSequence);

      final Optional<TaxonomyRecordPrimitive> record = this.originDestinationTaxonomyRecordMap.get(childSequence);

      if (record.isPresent()) {
         final TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
         final int[] conceptSequencesForType = childTaxonomyRecords.getConceptSequencesForType(this.isaSequence)
                                                                   .toArray();

         if (Arrays.stream(conceptSequencesForType)
                   .anyMatch((int parentSequenceOfType) -> parentSequenceOfType == parentSequence)) {
            return true;
         }

         return Arrays.stream(conceptSequencesForType)
                      .anyMatch((int intermediateChild) -> recursiveFindAncestor(intermediateChild,
                            parentSequence,
                            examined));
      }

      return false;
   }

   /**
    * Recursive find ancestor.
    *
    * @param childSequence the child sequence
    * @param parentSequence the parent sequence
    * @param tc the tc
    * @return true, if successful
    */
   private boolean recursiveFindAncestor(int childSequence, int parentSequence, TaxonomyCoordinate tc) {
      // currently unpacking from array to object.
      // TODO operate directly on array if unpacking is a performance bottleneck.
      final Optional<TaxonomyRecordPrimitive> record = this.originDestinationTaxonomyRecordMap.get(childSequence);

      if (record.isPresent()) {
         final TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
         final int[] activeConceptSequences = childTaxonomyRecords.getConceptSequencesForType(this.isaSequence, tc)
                                                                  .toArray();

         if (Arrays.stream(activeConceptSequences)
                   .anyMatch((int activeParentSequence) -> activeParentSequence == parentSequence)) {
            return true;
         }

         return Arrays.stream(activeConceptSequences)
                      .anyMatch((int intermediateChild) -> recursiveFindAncestor(intermediateChild,
                            parentSequence,
                            tc));
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
   private void recursiveFindAncestors(int childSequence, ConceptSequenceSet ancestors, TaxonomyCoordinate tc) {
      // currently unpacking from array to object.
      // TODO operate directly on array if unpacking is a performance bottleneck.
      final Optional<TaxonomyRecordPrimitive> record = this.originDestinationTaxonomyRecordMap.get(childSequence);

      if (record.isPresent()) {
         final TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
         final int[] activeConceptSequences = childTaxonomyRecords.getConceptSequencesForType(this.isaSequence, tc)
                                                                  .toArray();

         Arrays.stream(activeConceptSequences).forEach((parent) -> {
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
         this.destinationOriginRecordSet.forEach((rec) -> {
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
                             TaxonomyFlags taxonomyFlags,
                             int stampSequence,
                             int originSequence) {
      parentTaxonomyRecord.getTaxonomyRecordUnpacked()
                          .addStampRecord(conceptNode.getConceptSequence(),
                                this.isaSequence,
                                stampSequence,
                                taxonomyFlags.bits);
      this.destinationOriginRecordSet.add(new DestinationOriginRecord(conceptNode.getConceptSequence(),
            originSequence));
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
                               TaxonomyFlags taxonomyFlags,
                               int stampSequence,
                               int originSequence) {
      if (someNode.getTypeConceptSequence() == this.roleGroupSequence) {
         final AndNode andNode = (AndNode) someNode.getOnlyChild();

         andNode.getChildStream().forEach((roleGroupSomeNode) -> {
                            if (roleGroupSomeNode instanceof RoleNodeSomeWithSequences) {
                               updateSomeRole((RoleNodeSomeWithSequences) roleGroupSomeNode,
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
                                .addStampRecord(restrictionNode.getConceptSequence(),
                                      someNode.getTypeConceptSequence(),
                                      stampSequence,
                                      taxonomyFlags.bits);
            this.destinationOriginRecordSet.add(new DestinationOriginRecord(restrictionNode.getConceptSequence(),
                  originSequence));
         } else {
            // TODO dan put this here to stop a pile of errors. It was returning AndNode.  Not sure what to do with it
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the all circular relationship origin sequences.
    *
    * @param tc the tc
    * @return the all circular relationship origin sequences
    */
   @Override
   public IntStream getAllCircularRelationshipOriginSequences(TaxonomyCoordinate tc) {
      final ConceptService  conceptService  = Get.conceptService();
      final StampCoordinate stampCoordinate = tc.getStampCoordinate();

      return Get.identifierService().getParallelConceptSequenceStream().filter((conceptSequence) -> {
                           if (conceptService.isConceptActive(conceptSequence, stampCoordinate)) {
                              if (getAllCircularRelationshipTypeSequences(conceptSequence, tc).anyMatch(
                                  ((typeSequence) -> true))) {
                                 return true;
                              }
                           }

                           return false;
                        });
   }

   /**
    * Gets the all circular relationship type sequences.
    *
    * @param originId the origin id
    * @param tc the tc
    * @return the all circular relationship type sequences
    */
   @Override
   public IntStream getAllCircularRelationshipTypeSequences(int originId, TaxonomyCoordinate tc) {
      final int                originSequence = Get.identifierService()
                                                   .getConceptSequence(originId);
      final ConceptSequenceSet ancestors      = getAncestorOfSequenceSet(originId, tc);

      if (tc.getTaxonomyType() != PremiseType.INFERRED) {
         ancestors.or(getAncestorOfSequenceSet(originId, tc.makeAnalog(PremiseType.INFERRED)));
      }

      final ConceptSequenceSet excludedTypes       = ConceptSequenceSet.of(this.isaSequence);
      final IntStream.Builder  typeSequenceBuilder = IntStream.builder();

      getAllRelationshipDestinationSequencesNotOfType(originId,
            excludedTypes,
            tc).filter((destinationSequence) -> ancestors.contains(destinationSequence)).forEach((destinationSequence) -> {
                           getAllTypesForRelationship(originSequence, destinationSequence, tc).forEach(
                               (typeSequence) -> typeSequenceBuilder.accept(typeSequence));
                        });
      return typeSequenceBuilder.build();
   }

   /**
    * Gets the all relationship destination sequences.
    *
    * @param originId the origin id
    * @return the all relationship destination sequences
    */
   @Override
   public IntStream getAllRelationshipDestinationSequences(int originId) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = this.stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

      if (this.stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequences();
         }

         return IntStream.empty();
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequences();
         }
      } finally {
         this.stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   /**
    * Gets the all relationship destination sequences.
    *
    * @param originId the origin id
    * @param tc the tc
    * @return the all relationship destination sequences
    */
   @Override
   public IntStream getAllRelationshipDestinationSequences(int originId, TaxonomyCoordinate tc) {
      // lock handled by called method
      return getAllRelationshipDestinationSequencesOfType(originId, new ConceptSequenceSet(), tc);
   }

   /**
    * Gets the all relationship destination sequences not of type.
    *
    * @param originId the origin id
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @return the all relationship destination sequences not of type
    */
   @Override
   public IntStream getAllRelationshipDestinationSequencesNotOfType(int originId,
         ConceptSequenceSet typeSequenceSet,
         TaxonomyCoordinate tc) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = this.stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

      if (this.stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesNotOfType(typeSequenceSet, tc);
         }

         return IntStream.empty();
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesNotOfType(typeSequenceSet, tc);
         }
      } finally {
         this.stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   /**
    * Gets the all relationship destination sequences of type.
    *
    * @param originId the origin id
    * @param typeSequenceSet the type sequence set
    * @return the all relationship destination sequences of type
    */
   @Override
   public IntStream getAllRelationshipDestinationSequencesOfType(int originId, ConceptSequenceSet typeSequenceSet) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = this.stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

      if (this.stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesOfType(typeSequenceSet);
         }

         return IntStream.empty();
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesOfType(typeSequenceSet);
         }
      } finally {
         this.stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   /**
    * Gets the all relationship destination sequences of type.
    *
    * @param originId the origin id
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @return the all relationship destination sequences of type
    */
   @Override
   public IntStream getAllRelationshipDestinationSequencesOfType(int originId,
         ConceptSequenceSet typeSequenceSet,
         TaxonomyCoordinate tc) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = this.stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

      if (this.stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesOfType(typeSequenceSet, tc);
         }

         return IntStream.empty();
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesOfType(typeSequenceSet, tc);
         }
      } finally {
         this.stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   /**
    * Gets the all relationship origin sequences.
    *
    * @param destination the destination
    * @return the all relationship origin sequences
    */
   @Override
   public IntStream getAllRelationshipOriginSequences(int destination) {
      // lock handled by getOriginSequenceStream
      return getOriginSequenceStream(destination);
   }

   /**
    * Gets the all relationship origin sequences.
    *
    * @param destination the destination
    * @param tc the tc
    * @return the all relationship origin sequences
    */
   @Override
   public IntStream getAllRelationshipOriginSequences(int destination, TaxonomyCoordinate tc) {
      // Set of all concept sequences that point to the parent.
      // lock handled by getOriginSequenceStream
      final IntStream origins = getOriginSequenceStream(destination);

      return filterOriginSequences(origins, destination, this.isaSequence, tc, AllowedRelTypes.ALL_RELS);
   }

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
    * Gets the all relationship origin sequences of type.
    *
    * @param destinationId the destination id
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @return the all relationship origin sequences of type
    */
   @Override
   public IntStream getAllRelationshipOriginSequencesOfType(int destinationId,
         ConceptSequenceSet typeSequenceSet,
         TaxonomyCoordinate tc) {
      destinationId = Get.identifierService()
                         .getConceptSequence(destinationId);

      long stamp = this.stampedLock.tryOptimisticRead();

      // Set of all concept sequences that point to the parent.
      IntStream origins = getOriginSequenceStream(destinationId);

      if (this.stampedLock.validate(stamp)) {
         return filterOriginSequences(origins, destinationId, typeSequenceSet, tc);
      }

      stamp = this.stampedLock.readLock();

      try {
         origins = getOriginSequenceStream(destinationId);
         return filterOriginSequences(origins, destinationId, typeSequenceSet, tc);
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   /**
    * Gets the all types for relationship.
    *
    * @param originId the origin id
    * @param destinationId the destination id
    * @param tc the tc
    * @return the all types for relationship
    */
   @Override
   public IntStream getAllTypesForRelationship(int originId, int destinationId, TaxonomyCoordinate tc) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = this.stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

      if (this.stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getTypesForRelationship(destinationId, tc);
         }

         return IntStream.empty();
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getTypesForRelationship(destinationId, tc);
         }
      } finally {
         this.stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   /**
    * Gets the ancestor of sequence set.
    *
    * @param childId the child id
    * @param tc the tc
    * @return the ancestor of sequence set
    */
   @Override
   public ConceptSequenceSet getAncestorOfSequenceSet(int childId, TaxonomyCoordinate tc) {
      final ConceptSequenceSet ancestors = new ConceptSequenceSet();

      recursiveFindAncestors(Get.identifierService()
                                .getConceptSequence(childId), ancestors, tc);
      return ancestors;
   }

   /**
    * Checks if child of.
    *
    * @param childId the child id
    * @param parentId the parent id
    * @param tc the tc
    * @return true, if child of
    */
   @Override
   public boolean isChildOf(int childId, int parentId, TaxonomyCoordinate tc) {
      childId  = Get.identifierService()
                    .getConceptSequence(childId);
      parentId = Get.identifierService()
                    .getConceptSequence(parentId);

      final RelativePositionCalculator  computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
      final int                         flags    = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(tc);
      long                              stamp    = this.stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> record   = this.originDestinationTaxonomyRecordMap.get(childId);

      if (this.stampedLock.validate(stamp)) {
         if (record.isPresent()) {
            final TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
            final Optional<TypeStampTaxonomyRecords> parentStampRecordsOptional =
               childTaxonomyRecords.getConceptSequenceStampRecords(parentId);

            if (parentStampRecordsOptional.isPresent()) {
               final TypeStampTaxonomyRecords parentStampRecords = parentStampRecordsOptional.get();

               if (computer.isLatestActive(parentStampRecords.getStampsOfTypeWithFlags(this.isaSequence, flags))) {
                  if (this.stampedLock.validate(stamp)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }

      stamp = this.stampedLock.readLock();

      try {
         record = this.originDestinationTaxonomyRecordMap.get(childId);

         if (record.isPresent()) {
            final TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
            final Optional<TypeStampTaxonomyRecords> parentStampRecordsOptional =
               childTaxonomyRecords.getConceptSequenceStampRecords(parentId);

            if (parentStampRecordsOptional.isPresent()) {
               final TypeStampTaxonomyRecords parentStampRecords = parentStampRecordsOptional.get();

               if (computer.isLatestActive(parentStampRecords.getStampsOfTypeWithFlags(this.isaSequence, flags))) {
                  if (this.stampedLock.validate(stamp)) {
                     return true;
                  }
               }
            }
         }
      } finally {
         this.stampedLock.unlock(stamp);
      }

      return false;
   }

   /**
    * Gets the child of sequence set.
    *
    * @param parentId the parent id
    * @param tc the tc
    * @return the child of sequence set
    */
   @Override
   public ConceptSequenceSet getChildOfSequenceSet(int parentId, TaxonomyCoordinate tc) {
      // Set of all concept sequences that point to the parent.
      // lock handled by getOriginSequenceStream
      final IntStream origins = getOriginSequenceStream(parentId);

      return ConceptSequenceSet.of(filterOriginSequences(origins,
            parentId,
            this.isaSequence,
            tc,
            AllowedRelTypes.HIERARCHICAL_ONLY));
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
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
         this.originDestinationTaxonomyRecordMap.get(conceptSequence);

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
    * Checks if kind of.
    *
    * @param childId the child id
    * @param parentId the parent id
    * @param tc the tc
    * @return true, if kind of
    */
   @Override
   public boolean isKindOf(int childId, int parentId, TaxonomyCoordinate tc) {
      childId  = Get.identifierService()
                    .getConceptSequence(childId);
      parentId = Get.identifierService()
                    .getConceptSequence(parentId);

      if (childId == parentId) {
         return true;
      }

      long          stamp    = this.stampedLock.tryOptimisticRead();
      final boolean isKindOf = recursiveFindAncestor(childId, parentId, tc);

      if (this.stampedLock.validate(stamp)) {
         return isKindOf;
      }

      stamp = this.stampedLock.readLock();

      try {
         return recursiveFindAncestor(childId, parentId, tc);
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   /**
    * Gets the kind of sequence set.
    *
    * @param rootId the root id
    * @param tc the tc
    * @return the kind of sequence set
    */
   @Override
   public ConceptSequenceSet getKindOfSequenceSet(int rootId, TaxonomyCoordinate tc) {
      rootId = Get.identifierService()
                  .getConceptSequence(rootId);

      long stamp = this.stampedLock.tryOptimisticRead();

      // TODO Look at performance of getTaxonomyTree...
      Tree                     tree      = getTaxonomyTree(tc);
      final ConceptSequenceSet kindOfSet = ConceptSequenceSet.of(rootId);

      tree.depthFirstProcess(rootId,
                             (TreeNodeVisitData t,
                              int conceptSequence) -> {
                                kindOfSet.add(conceptSequence);
                             });

      if (this.stampedLock.validate(stamp)) {
         return kindOfSet;
      }

      stamp = this.stampedLock.readLock();

      try {
         tree = getTaxonomyTree(tc);

         final ConceptSequenceSet kindOfSet2 = ConceptSequenceSet.of(rootId);

         tree.depthFirstProcess(rootId,
                                (TreeNodeVisitData t,
                                 int conceptSequence) -> {
                                   kindOfSet2.add(conceptSequence);
                                });
         return kindOfSet2;
      } finally {
         this.stampedLock.unlock(stamp);
      }
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
      NavigableSet<DestinationOriginRecord> subSet =
         this.destinationOriginRecordSet.subSet(new DestinationOriginRecord(parentId,
                                                                            Integer.MIN_VALUE),
                                                new DestinationOriginRecord(parentId,
                                                      Integer.MAX_VALUE));

      if (this.stampedLock.validate(stamp)) {
         return subSet.stream()
                      .mapToInt((DestinationOriginRecord record) -> record.getOriginSequence());
      }

      stamp = this.stampedLock.readLock();

      try {
         subSet = this.destinationOriginRecordSet.subSet(new DestinationOriginRecord(parentId, Integer.MIN_VALUE),
               new DestinationOriginRecord(parentId, Integer.MAX_VALUE));
         return subSet.stream()
                      .mapToInt((DestinationOriginRecord record) -> record.getOriginSequence());
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   /**
    * Gets the roots.
    *
    * @param tc the tc
    * @return the roots
    */
   @Override
   public IntStream getRoots(TaxonomyCoordinate tc) {
      long stamp = this.stampedLock.tryOptimisticRead();
      Tree tree  = getTaxonomyTree(tc);

      if (!this.stampedLock.validate(stamp)) {
         stamp = this.stampedLock.readLock();

         try {
            tree = getTaxonomyTree(tc);
         } finally {
            this.stampedLock.unlock(stamp);
         }
      }

      return tree.getRootSequenceStream();
   }

   /**
    * Gets the snapshot.
    *
    * @param tc the tc
    * @return the snapshot
    */
   @Override
   public TaxonomySnapshotService getSnapshot(TaxonomyCoordinate tc) {
      return new TaxonomySnapshotProvider(tc);
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

      return filterOriginSequences(origins, parentId, this.isaSequence, TaxonomyFlags.ALL_RELS);
   }

   /**
    * Gets the taxonomy child sequences.
    *
    * @param parentId the parent id
    * @param tc the tc
    * @return the taxonomy child sequences
    */
   @Override
   public IntStream getTaxonomyChildSequences(int parentId, TaxonomyCoordinate tc) {
      // Set of all concept sequences that point to the parent.
      // lock handled by getOriginSequenceStream
      final IntStream origins = getOriginSequenceStream(parentId);

      return filterOriginSequences(origins, parentId, this.isaSequence, tc, AllowedRelTypes.HIERARCHICAL_ONLY);
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
    * Gets the taxonomy parent sequences.
    *
    * @param childId the child id
    * @param tc the tc
    * @return the taxonomy parent sequences
    */
   @Override
   public IntStream getTaxonomyParentSequences(int childId, TaxonomyCoordinate tc) {
      childId = Get.identifierService()
                   .getConceptSequence(childId);

      long                              stamp                  = this.stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(childId);

      if (this.stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

            return taxonomyRecord.getParentSequences(tc);
         }

         return IntStream.empty();
      }

      stamp = this.stampedLock.readLock();

      try {
         taxonomyRecordOptional = this.originDestinationTaxonomyRecordMap.get(childId);

         if (taxonomyRecordOptional.isPresent()) {
            final TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

            return taxonomyRecord.getParentSequences(tc);
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
   @Override
   public Tree getTaxonomyTree(TaxonomyCoordinate tc) {
      // TODO determine if the returned tree is thread safe for multiple accesses in parallel, if not, may need a pool of these.
      Tree temp = this.treeCache.get(tc.hashCode());

      {
         if (temp != null) {
            return temp;
         }
      }

      long            stamp                 = this.stampedLock.tryOptimisticRead();
      IntStream       conceptSequenceStream = Get.identifierService()
                                                 .getParallelConceptSequenceStream();
      GraphCollector  collector             = new GraphCollector(this.originDestinationTaxonomyRecordMap, tc);
      HashTreeBuilder graphBuilder          = conceptSequenceStream.collect(HashTreeBuilder::new, collector, collector);

      if (this.stampedLock.validate(stamp)) {
         temp = graphBuilder.getSimpleDirectedGraphGraph();
         this.treeCache.put(tc.hashCode(), temp);
         return temp;
      }

      stamp = this.stampedLock.readLock();

      try {
         conceptSequenceStream = Get.identifierService()
                                    .getParallelConceptSequenceStream();
         collector             = new GraphCollector(this.originDestinationTaxonomyRecordMap, tc);
         graphBuilder          = conceptSequenceStream.collect(HashTreeBuilder::new, collector, collector);
         temp                  = graphBuilder.getSimpleDirectedGraphGraph();
         this.treeCache.put(tc.hashCode(), temp);
         return temp;
      } finally {
         this.stampedLock.unlock(stamp);
      }
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class TaxonomySnapshotProvider.
    */
   private class TaxonomySnapshotProvider
            implements TaxonomySnapshotService {
      /** The tc. */
      TaxonomyCoordinate tc;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new taxonomy snapshot provider.
       *
       * @param tc the tc
       */
      public TaxonomySnapshotProvider(TaxonomyCoordinate tc) {
         this.tc = tc;
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Gets the all relationship destination sequences.
       *
       * @param originId the origin id
       * @return the all relationship destination sequences
       */
      @Override
      public IntStream getAllRelationshipDestinationSequences(int originId) {
         return TaxonomyProvider.this.getAllRelationshipDestinationSequences(originId, this.tc);
      }

      /**
       * Gets the all relationship destination sequences of type.
       *
       * @param originId the origin id
       * @param typeSequenceSet the type sequence set
       * @return the all relationship destination sequences of type
       */
      @Override
      public IntStream getAllRelationshipDestinationSequencesOfType(int originId, ConceptSequenceSet typeSequenceSet) {
         return TaxonomyProvider.this.getAllRelationshipDestinationSequencesOfType(originId, typeSequenceSet, this.tc);
      }

      /**
       * Gets the all relationship origin sequences.
       *
       * @param destination the destination
       * @return the all relationship origin sequences
       */
      @Override
      public IntStream getAllRelationshipOriginSequences(int destination) {
         return TaxonomyProvider.this.getAllRelationshipOriginSequences(destination, this.tc);
      }

      /**
       * Gets the all relationship origin sequences of type.
       *
       * @param destinationId the destination id
       * @param typeSequenceSet the type sequence set
       * @return the all relationship origin sequences of type
       */
      @Override
      public IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet) {
         return TaxonomyProvider.this.getAllRelationshipOriginSequencesOfType(destinationId, typeSequenceSet, this.tc);
      }

      /**
       * Checks if child of.
       *
       * @param childId the child id
       * @param parentId the parent id
       * @return true, if child of
       */
      @Override
      public boolean isChildOf(int childId, int parentId) {
         return TaxonomyProvider.this.isChildOf(childId, parentId, this.tc);
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
         return TaxonomyProvider.this.isKindOf(childId, parentId, this.tc);
      }

      /**
       * Gets the kind of sequence set.
       *
       * @param rootId the root id
       * @return the kind of sequence set
       */
      @Override
      public ConceptSequenceSet getKindOfSequenceSet(int rootId) {
         return TaxonomyProvider.this.getKindOfSequenceSet(rootId, this.tc);
      }

      /**
       * Gets the roots.
       *
       * @return the roots
       */
      @Override
      public IntStream getRoots() {
         return TaxonomyProvider.this.getRoots(this.tc);
      }

      /**
       * Gets the taxonomy child sequences.
       *
       * @param parentId the parent id
       * @return the taxonomy child sequences
       */
      @Override
      public IntStream getTaxonomyChildSequences(int parentId) {
         return TaxonomyProvider.this.getTaxonomyChildSequences(parentId, this.tc);
      }

      /**
       * Gets the taxonomy parent sequences.
       *
       * @param childId the child id
       * @return the taxonomy parent sequences
       */
      @Override
      public IntStream getTaxonomyParentSequences(int childId) {
         return TaxonomyProvider.this.getTaxonomyParentSequences(childId, this.tc);
      }

      /**
       * Gets the taxonomy tree.
       *
       * @return the taxonomy tree
       */
      @Override
      public Tree getTaxonomyTree() {
         return TaxonomyProvider.this.getTaxonomyTree(this.tc);
      }
   }
}

