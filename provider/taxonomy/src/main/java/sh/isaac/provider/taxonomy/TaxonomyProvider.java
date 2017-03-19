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
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class TaxonomyProvider
         implements TaxonomyService, ConceptActiveService, ChronologyChangeListener {
   private static final Logger LOG                    = LogManager.getLogger();
   private static final String TAXONOMY               = "taxonomy";
   private static final String ORIGIN_DESTINATION_MAP = "origin-destination.map";

   //~--- fields --------------------------------------------------------------

   private final ConcurrentSkipListSet<DestinationOriginRecord> destinationOriginRecordSet =
      new ConcurrentSkipListSet<>();
   private final AtomicBoolean                  loadRequired                       = new AtomicBoolean();
   private final LogicCoordinate                logicCoordinate = LogicCoordinates.getStandardElProfile();
   private final int                            isaSequence                        = TermAux.IS_A.getConceptSequence();
   private final int                            roleGroupSequence = TermAux.ROLE_GROUP.getConceptSequence();
   private final UUID                           providerUuid                       = UUID.randomUUID();
   private final ConcurrentSkipListSet<Integer> sememeSequencesForUnhandledChanges = new ConcurrentSkipListSet<>();
   private final StampedLock                    stampedLock                        = new StampedLock();
   private DatabaseValidity                     databaseValidity                   = DatabaseValidity.NOT_SET;
   private LruCache<Integer, Tree>              treeCache                          = new LruCache<>(5);

   /**
    * The {@code taxonomyMap} associates concept sequence keys with a primitive
    * taxonomy record, which represents the destination, stamp, and taxonomy
    * flags for parent and child concepts.
    */
   private final CasSequenceObjectMap<TaxonomyRecordPrimitive> originDestinationTaxonomyRecordMap;
   private final Path                                          folderPath;
   private final Path                                          taxonomyProviderFolder;
   private IdentifierService                                   identifierService;

   //~--- constant enums ------------------------------------------------------

   private enum AllowedRelTypes { HIERARCHICAL_ONLY,
                                  ALL_RELS; }

   //~--- constructors --------------------------------------------------------

   private TaxonomyProvider()
            throws IOException {
      folderPath             = LookupService.getService(ConfigurationService.class)
            .getChronicleFolderPath();
      taxonomyProviderFolder = folderPath.resolve(TAXONOMY);

      if (!Files.exists(taxonomyProviderFolder)) {
         databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
      }

      loadRequired.set(!Files.exists(taxonomyProviderFolder));
      Files.createDirectories(taxonomyProviderFolder);
      originDestinationTaxonomyRecordMap = new CasSequenceObjectMap<>(new TaxonomyRecordSerializer(),
            taxonomyProviderFolder,
            "seg.",
            ".taxonomy.map");
      LOG.info("CradleTaxonomyProvider constructed");
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void clearDatabaseValidityValue() {
      // Reset to enforce analysis
      databaseValidity = DatabaseValidity.NOT_SET;
   }

   @Override
   public void handleChange(ConceptChronology<? extends StampedVersion> cc) {
      // not interested on concept changes
   }

   @Override
   public void handleChange(SememeChronology<? extends SememeVersion<?>> sc) {
      if (sc.getSememeType() == SememeType.LOGIC_GRAPH) {
         sememeSequencesForUnhandledChanges.add(sc.getSememeSequence());
      }
   }

   @Override
   public void handleCommit(CommitRecord commitRecord) {
      // If a logic graph changed, clear our cache.
      if (sememeSequencesForUnhandledChanges.size() > 0) {
         treeCache.clear();
      }

      UpdateTaxonomyAfterCommitTask.get(this, commitRecord, sememeSequencesForUnhandledChanges, stampedLock);
   }

   @Override
   public void updateStatus(ConceptChronology<?> conceptChronology) {
      int                     conceptSequence = conceptChronology.getConceptSequence();
      TaxonomyRecordPrimitive parentTaxonomyRecord;

      if (originDestinationTaxonomyRecordMap.containsKey(conceptSequence)) {
         parentTaxonomyRecord = originDestinationTaxonomyRecordMap.get(conceptSequence)
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
      originDestinationTaxonomyRecordMap.put(conceptSequence, parentTaxonomyRecord);
   }

   @Override
   public void updateTaxonomy(SememeChronology<LogicGraphSememe<?>> logicGraphChronology) {
      int conceptSequence = identifierService.getConceptSequence(logicGraphChronology.getReferencedComponentNid());
      Optional<TaxonomyRecordPrimitive> record = originDestinationTaxonomyRecordMap.get(conceptSequence);
      TaxonomyRecordPrimitive           parentTaxonomyRecord;

      if (record.isPresent()) {
         parentTaxonomyRecord = record.get();
      } else {
         parentTaxonomyRecord = new TaxonomyRecordPrimitive();
      }

      TaxonomyFlags taxonomyFlags;

      if (logicGraphChronology.getAssemblageSequence() == logicCoordinate.getInferredAssemblageSequence()) {
         taxonomyFlags = TaxonomyFlags.INFERRED;
      } else {
         taxonomyFlags = TaxonomyFlags.STATED;
      }

      List<Graph<? extends LogicGraphSememe<?>>> versionGraphList = logicGraphChronology.getVersionGraphList();

      versionGraphList.forEach((versionGraph) -> {
                                  processVersionNode(versionGraph.getRoot(), parentTaxonomyRecord, taxonomyFlags);
                               });
      originDestinationTaxonomyRecordMap.put(conceptSequence, parentTaxonomyRecord);
   }

   @Override
   public boolean wasEverKindOf(int childId, int parentId) {
      childId  = Get.identifierService()
                    .getConceptSequence(childId);
      parentId = Get.identifierService()
                    .getConceptSequence(parentId);

      if (childId == parentId) {
         return true;
      }

      long    stamp         = stampedLock.tryOptimisticRead();
      boolean wasEverKindOf = recursiveFindAncestor(childId, parentId, new HashSet<>());

      if (stampedLock.validate(stamp)) {
         return wasEverKindOf;
      }

      stamp = stampedLock.readLock();

      try {
         return recursiveFindAncestor(childId, parentId, new HashSet<>());
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   private IntStream filterOriginSequences(IntStream origins, int parentSequence, ConceptSequenceSet typeSequenceSet) {
      return origins.filter((originSequence) -> {
                               Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                                  originDestinationTaxonomyRecordMap.get(originSequence);

                               if (taxonomyRecordOptional.isPresent()) {
                                  TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                                  return taxonomyRecord.containsSequenceViaType(parentSequence,
                                        typeSequenceSet,
                                        TaxonomyFlags.ALL_RELS);
                               }

                               return false;
                            });
   }

   private IntStream filterOriginSequences(IntStream origins,
         int parentSequence,
         ConceptSequenceSet typeSequenceSet,
         TaxonomyCoordinate tc) {
      return origins.filter((originSequence) -> {
                               Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                                  originDestinationTaxonomyRecordMap.get(originSequence);

                               if (taxonomyRecordOptional.isPresent()) {
                                  TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

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

   private IntStream filterOriginSequences(IntStream origins, int parentSequence, int typeSequence, int flags) {
      return origins.filter((originSequence) -> {
                               Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                                  originDestinationTaxonomyRecordMap.get(originSequence);

                               if (taxonomyRecordOptional.isPresent()) {
                                  TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

                                  return taxonomyRecord.containsSequenceViaTypeWithFlags(parentSequence,
                                        typeSequence,
                                        flags);
                               }

                               return false;
                            });
   }

   private IntStream filterOriginSequences(IntStream origins,
         int parentSequence,
         int typeSequence,
         TaxonomyCoordinate tc,
         AllowedRelTypes allowedRelTypes) {
      return origins.filter((originSequence) -> {
                               Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
                                  originDestinationTaxonomyRecordMap.get(originSequence);

                               if (taxonomyRecordOptional.isPresent()) {
                                  TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

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

   private void processNewLogicGraph(LogicGraphSememe firstVersion,
                                     TaxonomyRecordPrimitive parentTaxonomyRecord,
                                     TaxonomyFlags taxonomyFlags) {
      if (firstVersion.getCommitState() == CommitStates.COMMITTED) {
         LogicalExpression expression = firstVersion.getLogicalExpression();

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

   private void processVersionNode(Node<? extends LogicGraphSememe> node,
                                   TaxonomyRecordPrimitive parentTaxonomyRecord,
                                   TaxonomyFlags taxonomyFlags) {
      if (node.getParent() == null) {
         processNewLogicGraph(node.getData(), parentTaxonomyRecord, taxonomyFlags);
      } else {
         LogicalExpression comparisonExpression = node.getParent()
                                                      .getData()
                                                      .getLogicalExpression();
         LogicalExpression referenceExpression  = node.getData()
                                                      .getLogicalExpression();
         IsomorphicResultsBottomUp isomorphicResults = new IsomorphicResultsBottomUp(referenceExpression,
                                                                                     comparisonExpression);

         isomorphicResults.getAddedRelationshipRoots().forEach((logicalNode) -> {
                                      int stampSequence = node.getData()
                                                              .getStampSequence();

                                      processRelationshipRoot(logicalNode,
                                            parentTaxonomyRecord,
                                            taxonomyFlags,
                                            stampSequence,
                                            comparisonExpression);
                                   });
         isomorphicResults.getDeletedRelationshipRoots().forEach((logicalNode) -> {
                                      int activeStampSequence = node.getData()
                                                                    .getStampSequence();
                                      int stampSequence       = Get.stampService()
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

   private boolean recursiveFindAncestor(int childSequence, int parentSequence, HashSet<Integer> examined) {
      // currently unpacking from array to object.
      // TODO operate directly on array if unpacking is a performance bottleneck.
      if (examined.contains(childSequence)) {
         return false;
      }

      examined.add(childSequence);

      Optional<TaxonomyRecordPrimitive> record = originDestinationTaxonomyRecordMap.get(childSequence);

      if (record.isPresent()) {
         TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
         int[] conceptSequencesForType = childTaxonomyRecords.getConceptSequencesForType(isaSequence)
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

   private boolean recursiveFindAncestor(int childSequence, int parentSequence, TaxonomyCoordinate tc) {
      // currently unpacking from array to object.
      // TODO operate directly on array if unpacking is a performance bottleneck.
      Optional<TaxonomyRecordPrimitive> record = originDestinationTaxonomyRecordMap.get(childSequence);

      if (record.isPresent()) {
         TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
         int[] activeConceptSequences = childTaxonomyRecords.getConceptSequencesForType(isaSequence, tc)
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

   private void recursiveFindAncestors(int childSequence, ConceptSequenceSet ancestors, TaxonomyCoordinate tc) {
      // currently unpacking from array to object.
      // TODO operate directly on array if unpacking is a performance bottleneck.
      Optional<TaxonomyRecordPrimitive> record = originDestinationTaxonomyRecordMap.get(childSequence);

      if (record.isPresent()) {
         TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
         int[] activeConceptSequences = childTaxonomyRecords.getConceptSequencesForType(isaSequence, tc)
                                                            .toArray();

         Arrays.stream(activeConceptSequences).forEach((parent) -> {
                           if (!ancestors.contains(parent)) {
                              ancestors.add(parent);
                              recursiveFindAncestors(parent, ancestors, tc);
                           }
                        });
      }
   }

   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting TaxonomyService post-construct");

         if (!loadRequired.get()) {
            LOG.info("Reading taxonomy.");

            boolean isPopulated = originDestinationTaxonomyRecordMap.initialize();
            File    inputFile   = new File(taxonomyProviderFolder.toFile(), ORIGIN_DESTINATION_MAP);

            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)))) {
               int size = in.readInt();

               for (int i = 0; i < size; i++) {
                  destinationOriginRecordSet.add(new DestinationOriginRecord(in.readInt(), in.readInt()));
               }
            }

            if (isPopulated) {
               databaseValidity = DatabaseValidity.POPULATED_DIRECTORY;
            }
         }

         Get.commitService()
            .addChangeListener(this);
         identifierService = Get.identifierService();
      } catch (Exception e) {
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Cradle Taxonomy Provider", e);
         throw new RuntimeException(e);
      }
   }

   @PreDestroy
   private void stopMe() {
      LOG.info("Writing taxonomy.");
      originDestinationTaxonomyRecordMap.write();

      File outputFile = new File(taxonomyProviderFolder.toFile(), ORIGIN_DESTINATION_MAP);

      outputFile.getParentFile()
                .mkdirs();

      try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
         out.writeInt(destinationOriginRecordSet.size());
         destinationOriginRecordSet.forEach((rec) -> {
                                               try {
                                                  out.writeInt(rec.getDestinationSequence());
                                                  out.writeInt(rec.getOriginSequence());
                                               } catch (IOException ex) {
                                                  throw new RuntimeException(ex);
                                               }
                                            });
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void updateIsaRel(ConceptNodeWithSequences conceptNode,
                             TaxonomyRecordPrimitive parentTaxonomyRecord,
                             TaxonomyFlags taxonomyFlags,
                             int stampSequence,
                             int originSequence) {
      parentTaxonomyRecord.getTaxonomyRecordUnpacked()
                          .addStampRecord(conceptNode.getConceptSequence(),
                                isaSequence,
                                stampSequence,
                                taxonomyFlags.bits);
      destinationOriginRecordSet.add(new DestinationOriginRecord(conceptNode.getConceptSequence(), originSequence));
   }

   private void updateSomeRole(RoleNodeSomeWithSequences someNode,
                               TaxonomyRecordPrimitive parentTaxonomyRecord,
                               TaxonomyFlags taxonomyFlags,
                               int stampSequence,
                               int originSequence) {
      if (someNode.getTypeConceptSequence() == roleGroupSequence) {
         AndNode andNode = (AndNode) someNode.getOnlyChild();

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
            ConceptNodeWithSequences restrictionNode = (ConceptNodeWithSequences) someNode.getOnlyChild();

            parentTaxonomyRecord.getTaxonomyRecordUnpacked()
                                .addStampRecord(restrictionNode.getConceptSequence(),
                                      someNode.getTypeConceptSequence(),
                                      stampSequence,
                                      taxonomyFlags.bits);
            destinationOriginRecordSet.add(new DestinationOriginRecord(restrictionNode.getConceptSequence(),
                  originSequence));
         } else {
            // TODO dan put this here to stop a pile of errors. It was returning AndNode.  Not sure what to do with it
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public IntStream getAllCircularRelationshipOriginSequences(TaxonomyCoordinate tc) {
      ConceptService  conceptService  = Get.conceptService();
      StampCoordinate stampCoordinate = tc.getStampCoordinate();

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

   @Override
   public IntStream getAllCircularRelationshipTypeSequences(int originId, TaxonomyCoordinate tc) {
      int                originSequence = Get.identifierService()
                                             .getConceptSequence(originId);
      ConceptSequenceSet ancestors      = getAncestorOfSequenceSet(originId, tc);

      if (tc.getTaxonomyType() != PremiseType.INFERRED) {
         ancestors.or(getAncestorOfSequenceSet(originId, tc.makeAnalog(PremiseType.INFERRED)));
      }

      ConceptSequenceSet excludedTypes       = ConceptSequenceSet.of(isaSequence);
      IntStream.Builder  typeSequenceBuilder = IntStream.builder();

      getAllRelationshipDestinationSequencesNotOfType(originId,
            excludedTypes,
            tc).filter((destinationSequence) -> ancestors.contains(destinationSequence)).forEach((destinationSequence) -> {
                           getAllTypesForRelationship(originSequence, destinationSequence, tc).forEach(
                               (typeSequence) -> typeSequenceBuilder.accept(typeSequence));
                        });
      return typeSequenceBuilder.build();
   }

   @Override
   public IntStream getAllRelationshipDestinationSequences(int originId) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

      if (stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequences();
         }

         return IntStream.empty();
      }

      stamp = stampedLock.readLock();

      try {
         taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequences();
         }
      } finally {
         stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   @Override
   public IntStream getAllRelationshipDestinationSequences(int originId, TaxonomyCoordinate tc) {
      // lock handled by called method
      return getAllRelationshipDestinationSequencesOfType(originId, new ConceptSequenceSet(), tc);
   }

   @Override
   public IntStream getAllRelationshipDestinationSequencesNotOfType(int originId,
         ConceptSequenceSet typeSequenceSet,
         TaxonomyCoordinate tc) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

      if (stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesNotOfType(typeSequenceSet, tc);
         }

         return IntStream.empty();
      }

      stamp = stampedLock.readLock();

      try {
         taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesNotOfType(typeSequenceSet, tc);
         }
      } finally {
         stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   @Override
   public IntStream getAllRelationshipDestinationSequencesOfType(int originId, ConceptSequenceSet typeSequenceSet) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

      if (stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesOfType(typeSequenceSet);
         }

         return IntStream.empty();
      }

      stamp = stampedLock.readLock();

      try {
         taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesOfType(typeSequenceSet);
         }
      } finally {
         stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   @Override
   public IntStream getAllRelationshipDestinationSequencesOfType(int originId,
         ConceptSequenceSet typeSequenceSet,
         TaxonomyCoordinate tc) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

      if (stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesOfType(typeSequenceSet, tc);
         }

         return IntStream.empty();
      }

      stamp = stampedLock.readLock();

      try {
         taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getDestinationSequencesOfType(typeSequenceSet, tc);
         }
      } finally {
         stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   @Override
   public IntStream getAllRelationshipOriginSequences(int destination) {
      // lock handled by getOriginSequenceStream
      return getOriginSequenceStream(destination);
   }

   @Override
   public IntStream getAllRelationshipOriginSequences(int destination, TaxonomyCoordinate tc) {
      // Set of all concept sequences that point to the parent.
      // lock handled by getOriginSequenceStream
      IntStream origins = getOriginSequenceStream(destination);

      return filterOriginSequences(origins, destination, isaSequence, tc, AllowedRelTypes.ALL_RELS);
   }

   @Override
   public IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet) {
      destinationId = Get.identifierService()
                         .getConceptSequence(destinationId);

      long stamp = stampedLock.tryOptimisticRead();

      // Set of all concept sequences that point to the parent.
      IntStream origins = getOriginSequenceStream(destinationId);

      if (stampedLock.validate(stamp)) {
         return filterOriginSequences(origins, destinationId, typeSequenceSet);
      }

      stamp = stampedLock.readLock();

      try {
         origins = getOriginSequenceStream(destinationId);
         return filterOriginSequences(origins, destinationId, typeSequenceSet);
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   @Override
   public IntStream getAllRelationshipOriginSequencesOfType(int destinationId,
         ConceptSequenceSet typeSequenceSet,
         TaxonomyCoordinate tc) {
      destinationId = Get.identifierService()
                         .getConceptSequence(destinationId);

      long stamp = stampedLock.tryOptimisticRead();

      // Set of all concept sequences that point to the parent.
      IntStream origins = getOriginSequenceStream(destinationId);

      if (stampedLock.validate(stamp)) {
         return filterOriginSequences(origins, destinationId, typeSequenceSet, tc);
      }

      stamp = stampedLock.readLock();

      try {
         origins = getOriginSequenceStream(destinationId);
         return filterOriginSequences(origins, destinationId, typeSequenceSet, tc);
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   @Override
   public IntStream getAllTypesForRelationship(int originId, int destinationId, TaxonomyCoordinate tc) {
      originId = Get.identifierService()
                    .getConceptSequence(originId);

      long                              stamp                  = stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

      if (stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getTypesForRelationship(destinationId, tc);
         }

         return IntStream.empty();
      }

      stamp = stampedLock.readLock();

      try {
         taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(originId);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .getTypesForRelationship(destinationId, tc);
         }
      } finally {
         stampedLock.unlock(stamp);
      }

      return IntStream.empty();
   }

   @Override
   public ConceptSequenceSet getAncestorOfSequenceSet(int childId, TaxonomyCoordinate tc) {
      ConceptSequenceSet ancestors = new ConceptSequenceSet();

      recursiveFindAncestors(Get.identifierService()
                                .getConceptSequence(childId), ancestors, tc);
      return ancestors;
   }

   @Override
   public boolean isChildOf(int childId, int parentId, TaxonomyCoordinate tc) {
      childId  = Get.identifierService()
                    .getConceptSequence(childId);
      parentId = Get.identifierService()
                    .getConceptSequence(parentId);

      RelativePositionCalculator        computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
      int                               flags    = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(tc);
      long                              stamp    = stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> record   = originDestinationTaxonomyRecordMap.get(childId);

      if (stampedLock.validate(stamp)) {
         if (record.isPresent()) {
            TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
            Optional<TypeStampTaxonomyRecords> parentStampRecordsOptional =
               childTaxonomyRecords.getConceptSequenceStampRecords(parentId);

            if (parentStampRecordsOptional.isPresent()) {
               TypeStampTaxonomyRecords parentStampRecords = parentStampRecordsOptional.get();

               if (computer.isLatestActive(parentStampRecords.getStampsOfTypeWithFlags(isaSequence, flags))) {
                  if (stampedLock.validate(stamp)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }

      stamp = stampedLock.readLock();

      try {
         record = originDestinationTaxonomyRecordMap.get(childId);

         if (record.isPresent()) {
            TaxonomyRecordUnpacked childTaxonomyRecords = new TaxonomyRecordUnpacked(record.get().getArray());
            Optional<TypeStampTaxonomyRecords> parentStampRecordsOptional =
               childTaxonomyRecords.getConceptSequenceStampRecords(parentId);

            if (parentStampRecordsOptional.isPresent()) {
               TypeStampTaxonomyRecords parentStampRecords = parentStampRecordsOptional.get();

               if (computer.isLatestActive(parentStampRecords.getStampsOfTypeWithFlags(isaSequence, flags))) {
                  if (stampedLock.validate(stamp)) {
                     return true;
                  }
               }
            }
         }
      } finally {
         stampedLock.unlock(stamp);
      }

      return false;
   }

   @Override
   public ConceptSequenceSet getChildOfSequenceSet(int parentId, TaxonomyCoordinate tc) {
      // Set of all concept sequences that point to the parent.
      // lock handled by getOriginSequenceStream
      IntStream origins = getOriginSequenceStream(parentId);

      return ConceptSequenceSet.of(filterOriginSequences(origins,
            parentId,
            isaSequence,
            tc,
            AllowedRelTypes.HIERARCHICAL_ONLY));
   }

   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      long                              stamp                  = stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional =
         originDestinationTaxonomyRecordMap.get(conceptSequence);

      if (stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .isConceptActive(conceptSequence, stampCoordinate);
         }

         return false;
      }

      stamp = stampedLock.readLock();

      try {
         taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(conceptSequence);

         if (taxonomyRecordOptional.isPresent()) {
            return taxonomyRecordOptional.get()
                                         .isConceptActive(conceptSequence, stampCoordinate);
         }

         return false;
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   @Override
   public Path getDatabaseFolder() {
      return taxonomyProviderFolder;
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return databaseValidity;
   }

   public ConcurrentSkipListSet<DestinationOriginRecord> getDestinationOriginRecordSet() {
      return destinationOriginRecordSet;
   }

   @Override
   public boolean isKindOf(int childId, int parentId, TaxonomyCoordinate tc) {
      childId  = Get.identifierService()
                    .getConceptSequence(childId);
      parentId = Get.identifierService()
                    .getConceptSequence(parentId);

      if (childId == parentId) {
         return true;
      }

      long    stamp    = stampedLock.tryOptimisticRead();
      boolean isKindOf = recursiveFindAncestor(childId, parentId, tc);

      if (stampedLock.validate(stamp)) {
         return isKindOf;
      }

      stamp = stampedLock.readLock();

      try {
         return recursiveFindAncestor(childId, parentId, tc);
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   @Override
   public ConceptSequenceSet getKindOfSequenceSet(int rootId, TaxonomyCoordinate tc) {
      rootId = Get.identifierService()
                  .getConceptSequence(rootId);

      long stamp = stampedLock.tryOptimisticRead();

      // TODO Look at performance of getTaxonomyTree...
      Tree               tree      = getTaxonomyTree(tc);
      ConceptSequenceSet kindOfSet = ConceptSequenceSet.of(rootId);

      tree.depthFirstProcess(rootId,
                             (TreeNodeVisitData t,
                              int conceptSequence) -> {
                                kindOfSet.add(conceptSequence);
                             });

      if (stampedLock.validate(stamp)) {
         return kindOfSet;
      }

      stamp = stampedLock.readLock();

      try {
         tree = getTaxonomyTree(tc);

         ConceptSequenceSet kindOfSet2 = ConceptSequenceSet.of(rootId);

         tree.depthFirstProcess(rootId,
                                (TreeNodeVisitData t,
                                 int conceptSequence) -> {
                                   kindOfSet2.add(conceptSequence);
                                });
         return kindOfSet2;
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   @Override
   public UUID getListenerUuid() {
      return providerUuid;
   }

   public CasSequenceObjectMap<TaxonomyRecordPrimitive> getOriginDestinationTaxonomyRecords() {
      return originDestinationTaxonomyRecordMap;
   }

   private IntStream getOriginSequenceStream(int parentId) {
      // Set of all concept sequences that point to the parent.
      parentId = Get.identifierService()
                    .getConceptSequence(parentId);

      long stamp = stampedLock.tryOptimisticRead();
      NavigableSet<DestinationOriginRecord> subSet =
         destinationOriginRecordSet.subSet(new DestinationOriginRecord(parentId,
                                                                       Integer.MIN_VALUE),
                                           new DestinationOriginRecord(parentId,
                                                 Integer.MAX_VALUE));

      if (stampedLock.validate(stamp)) {
         return subSet.stream()
                      .mapToInt((DestinationOriginRecord record) -> record.getOriginSequence());
      }

      stamp = stampedLock.readLock();

      try {
         subSet = destinationOriginRecordSet.subSet(new DestinationOriginRecord(parentId, Integer.MIN_VALUE),
               new DestinationOriginRecord(parentId, Integer.MAX_VALUE));
         return subSet.stream()
                      .mapToInt((DestinationOriginRecord record) -> record.getOriginSequence());
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   @Override
   public IntStream getRoots(TaxonomyCoordinate tc) {
      long stamp = stampedLock.tryOptimisticRead();
      Tree tree  = getTaxonomyTree(tc);

      if (!stampedLock.validate(stamp)) {
         stamp = stampedLock.readLock();

         try {
            tree = getTaxonomyTree(tc);
         } finally {
            stampedLock.unlock(stamp);
         }
      }

      return tree.getRootSequenceStream();
   }

   @Override
   public TaxonomySnapshotService getSnapshot(TaxonomyCoordinate tc) {
      return new TaxonomySnapshotProvider(tc);
   }

   @Override
   public IntStream getTaxonomyChildSequences(int parentId) {
      // Set of all concept sequences that point to the parent.
      // lock handled by getOriginSequenceStream
      IntStream origins = getOriginSequenceStream(parentId);

      return filterOriginSequences(origins, parentId, isaSequence, TaxonomyFlags.ALL_RELS);
   }

   @Override
   public IntStream getTaxonomyChildSequences(int parentId, TaxonomyCoordinate tc) {
      // Set of all concept sequences that point to the parent.
      // lock handled by getOriginSequenceStream
      IntStream origins = getOriginSequenceStream(parentId);

      return filterOriginSequences(origins, parentId, isaSequence, tc, AllowedRelTypes.HIERARCHICAL_ONLY);
   }

   @Override
   public IntStream getTaxonomyParentSequences(int childId) {
      childId = Get.identifierService()
                   .getConceptSequence(childId);

      long                              stamp                  = stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(childId);

      if (stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

            return taxonomyRecord.getParentSequences()
                                 .distinct();
         }

         return IntStream.empty();
      }

      stamp = stampedLock.readLock();

      try {
         taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(childId);

         if (taxonomyRecordOptional.isPresent()) {
            TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

            return taxonomyRecord.getParentSequences()
                                 .distinct();
         }

         return IntStream.empty();
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   @Override
   public IntStream getTaxonomyParentSequences(int childId, TaxonomyCoordinate tc) {
      childId = Get.identifierService()
                   .getConceptSequence(childId);

      long                              stamp                  = stampedLock.tryOptimisticRead();
      Optional<TaxonomyRecordPrimitive> taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(childId);

      if (stampedLock.validate(stamp)) {
         if (taxonomyRecordOptional.isPresent()) {
            TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

            return taxonomyRecord.getParentSequences(tc);
         }

         return IntStream.empty();
      }

      stamp = stampedLock.readLock();

      try {
         taxonomyRecordOptional = originDestinationTaxonomyRecordMap.get(childId);

         if (taxonomyRecordOptional.isPresent()) {
            TaxonomyRecordPrimitive taxonomyRecord = taxonomyRecordOptional.get();

            return taxonomyRecord.getParentSequences(tc);
         }

         return IntStream.empty();
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   @Override
   public Tree getTaxonomyTree(TaxonomyCoordinate tc) {
      // TODO determine if the returned tree is thread safe for multiple accesses in parallel, if not, may need a pool of these.
      Tree temp = treeCache.get(tc.hashCode());

      {
         if (temp != null) {
            return temp;
         }
      }

      long            stamp                 = stampedLock.tryOptimisticRead();
      IntStream       conceptSequenceStream = Get.identifierService()
                                                 .getParallelConceptSequenceStream();
      GraphCollector  collector             = new GraphCollector(originDestinationTaxonomyRecordMap, tc);
      HashTreeBuilder graphBuilder          = conceptSequenceStream.collect(HashTreeBuilder::new, collector, collector);

      if (stampedLock.validate(stamp)) {
         temp = graphBuilder.getSimpleDirectedGraphGraph();
         treeCache.put(tc.hashCode(), temp);
         return temp;
      }

      stamp = stampedLock.readLock();

      try {
         conceptSequenceStream = Get.identifierService()
                                    .getParallelConceptSequenceStream();
         collector             = new GraphCollector(originDestinationTaxonomyRecordMap, tc);
         graphBuilder          = conceptSequenceStream.collect(HashTreeBuilder::new, collector, collector);
         temp                  = graphBuilder.getSimpleDirectedGraphGraph();
         treeCache.put(tc.hashCode(), temp);
         return temp;
      } finally {
         stampedLock.unlock(stamp);
      }
   }

   //~--- inner classes -------------------------------------------------------

   private class TaxonomySnapshotProvider
            implements TaxonomySnapshotService {
      TaxonomyCoordinate tc;

      //~--- constructors -----------------------------------------------------

      public TaxonomySnapshotProvider(TaxonomyCoordinate tc) {
         this.tc = tc;
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public IntStream getAllRelationshipDestinationSequences(int originId) {
         return TaxonomyProvider.this.getAllRelationshipDestinationSequences(originId, tc);
      }

      @Override
      public IntStream getAllRelationshipDestinationSequencesOfType(int originId, ConceptSequenceSet typeSequenceSet) {
         return TaxonomyProvider.this.getAllRelationshipDestinationSequencesOfType(originId, typeSequenceSet, tc);
      }

      @Override
      public IntStream getAllRelationshipOriginSequences(int destination) {
         return TaxonomyProvider.this.getAllRelationshipOriginSequences(destination, tc);
      }

      @Override
      public IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet) {
         return TaxonomyProvider.this.getAllRelationshipOriginSequencesOfType(destinationId, typeSequenceSet, tc);
      }

      @Override
      public boolean isChildOf(int childId, int parentId) {
         return TaxonomyProvider.this.isChildOf(childId, parentId, tc);
      }

      @Override
      public boolean isKindOf(int childId, int parentId) {
         return TaxonomyProvider.this.isKindOf(childId, parentId, tc);
      }

      @Override
      public ConceptSequenceSet getKindOfSequenceSet(int rootId) {
         return TaxonomyProvider.this.getKindOfSequenceSet(rootId, tc);
      }

      @Override
      public IntStream getRoots() {
         return TaxonomyProvider.this.getRoots(tc);
      }

      @Override
      public IntStream getTaxonomyChildSequences(int parentId) {
         return TaxonomyProvider.this.getTaxonomyChildSequences(parentId, tc);
      }

      @Override
      public IntStream getTaxonomyParentSequences(int childId) {
         return TaxonomyProvider.this.getTaxonomyParentSequences(childId, tc);
      }

      @Override
      public Tree getTaxonomyTree() {
         return TaxonomyProvider.this.getTaxonomyTree(tc);
      }
   }
}

