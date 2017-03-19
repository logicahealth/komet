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



package sh.isaac.provider.logic;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.MetaData;
import sh.isaac.api.DataSource;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.dag.Node;
import sh.isaac.api.logic.*;
import sh.isaac.api.relationship.RelationshipAdaptorChronicleKey;
import sh.isaac.api.relationship.RelationshipVersionAdaptor;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithSequences;
import sh.isaac.model.relationship.RelationshipAdaptorChronicleKeyImpl;
import sh.isaac.model.relationship.RelationshipAdaptorChronologyImpl;
import sh.isaac.model.relationship.RelationshipVersionAdaptorImpl;
import sh.isaac.model.sememe.version.LogicGraphSememeImpl;
import sh.isaac.provider.logic.csiro.classify.ClassifierProvider;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service(name = "logic provider")
@RunLevel(value = 2)
public class LogicProvider
         implements LogicService {
   private static final Logger                                       log                  = LogManager.getLogger();
   private static final Map<ClassifierServiceKey, ClassifierService> classifierServiceMap = new ConcurrentHashMap<>();

   //~--- constructors --------------------------------------------------------

   private LogicProvider() {
      // For HK2
      log.info("logic provider constructed");
   }

   //~--- methods -------------------------------------------------------------

   private RelationshipVersionAdaptorImpl createIsaRel(int originSequence,
         ConceptNodeWithSequences destinationNode,
         int stampSequence,
         PremiseType premiseType) {
      final int destinationSequence = Get.identifierService()
                                   .getConceptSequence(destinationNode.getConceptSequence());
      final int typeSequence = MetaData.IS_A.getConceptSequence();
      final int group        = 0;
      final RelationshipAdaptorChronicleKeyImpl key = new RelationshipAdaptorChronicleKeyImpl(originSequence,
                                                                                        destinationSequence,
                                                                                        typeSequence,
                                                                                        group,
                                                                                        premiseType,
                                                                                        destinationNode.getNodeIndex());

      return new RelationshipVersionAdaptorImpl(key, stampSequence);
   }

   private Stream<RelationshipVersionAdaptorImpl> createSomeRole(int originSequence,
         RoleNodeSomeWithSequences someNode,
         int stampSequence,
         PremiseType premiseType,
         int roleGroup) {
      final Stream.Builder<RelationshipVersionAdaptorImpl> roleStream = Stream.builder();

      if (someNode.getTypeConceptSequence() == MetaData.ROLE_GROUP.getConceptSequence()) {
         final AndNode andNode = (AndNode) someNode.getOnlyChild();

         andNode.getChildStream().forEach((roleGroupSomeNode) -> {
                            if (roleGroupSomeNode instanceof RoleNodeSomeWithSequences) {
                               createSomeRole(originSequence,
                                              (RoleNodeSomeWithSequences) roleGroupSomeNode,
                                              stampSequence,
                                              premiseType,
                                              someNode.getNodeIndex()).forEach((adaptor) -> {
                        roleStream.add(adaptor);
                     });
                            } else {
                               // TODO Keith - not sure what to do here...  getting a FeatureNodeWithSequences
                            }
                         });
      } else {
         final LogicNode restriction = someNode.getOnlyChild();
         int       destinationSequence;

         if (restriction.getNodeSemantic() == NodeSemantic.CONCEPT) {
            final ConceptNodeWithSequences restrictionNode = (ConceptNodeWithSequences) someNode.getOnlyChild();

            destinationSequence = Get.identifierService()
                                     .getConceptSequence(restrictionNode.getConceptSequence());
         } else {
            destinationSequence = MetaData.ANONYMOUS_CONCEPT.getConceptSequence();
         }

         final int typeSequence = Get.identifierService()
                               .getConceptSequence(someNode.getTypeConceptSequence());
         final RelationshipAdaptorChronicleKeyImpl key = new RelationshipAdaptorChronicleKeyImpl(originSequence,
                                                                                           destinationSequence,
                                                                                           typeSequence,
                                                                                           roleGroup,
                                                                                           premiseType,
                                                                                           someNode.getNodeIndex());

         roleStream.accept(new RelationshipVersionAdaptorImpl(key, stampSequence));
      }

      return roleStream.build();
   }

   private Stream<RelationshipVersionAdaptorImpl> extractRelationshipAdaptors(
           SememeChronology<LogicGraphSememe<?>> logicGraphChronology,
           PremiseType premiseType) {
      final Stream.Builder<RelationshipVersionAdaptorImpl> streamBuilder = Stream.builder();

      // one graph for each origin... Usually only one.
      for (final Graph<? extends LogicGraphSememe<?>> versionGraph: logicGraphChronology.getVersionGraphList()) {
         final Node<? extends LogicGraphSememe<?>> node = versionGraph.getRoot();

         processNode(node, null, streamBuilder, premiseType);
      }

      final int originConceptSequence = Get.identifierService()
                                     .getConceptSequence(logicGraphChronology.getReferencedComponentNid());

      logicGraphChronology.getVersionList().forEach((logicVersion) -> {
                                      final LogicalExpressionOchreImpl expression =
                                         new LogicalExpressionOchreImpl(logicVersion.getGraphData(),
                                                                        DataSource.INTERNAL,
                                                                        originConceptSequence);
                                   });
      return streamBuilder.build();
   }

   private void generateRelAdaptorChronicles(SememeChronology<? extends SememeVersion> logicalDef,
         HashMap<RelationshipAdaptorChronicleKey, RelationshipAdaptorChronologyImpl> conceptOriginRelationshipMap,
         PremiseType premiseType) {
      generateRelAdaptorChronicles(Integer.MAX_VALUE, logicalDef, conceptOriginRelationshipMap, premiseType);
   }

   private void generateRelAdaptorChronicles(int conceptDestinationSequence,
         SememeChronology<? extends SememeVersion> logicalDef,
         HashMap<RelationshipAdaptorChronicleKey, RelationshipAdaptorChronologyImpl> conceptOriginRelationshipMap,
         PremiseType premiseType) {
      extractRelationshipAdaptors((SememeChronology<LogicGraphSememe<?>>) logicalDef,
                                  premiseType).forEach((relAdaptor) -> {
               if ((conceptDestinationSequence == Integer.MAX_VALUE) ||
                   (conceptDestinationSequence == relAdaptor.getDestinationSequence())) {
                  RelationshipAdaptorChronologyImpl chronicle =
                     conceptOriginRelationshipMap.get(relAdaptor.getChronicleKey());

                  if (chronicle == null) {
                     // compute nid, combine the sememe sequence + the node sequence from which
                     final int topBits    = relAdaptor.getNodeSequence() << 24;
                     final int adaptorNid = logicalDef.getSememeSequence() + topBits;

                     chronicle = new RelationshipAdaptorChronologyImpl(adaptorNid, logicalDef.getNid());
                     conceptOriginRelationshipMap.put(relAdaptor.getChronicleKey(), chronicle);
                  }

                  relAdaptor.setChronology(chronicle);
                  chronicle.getVersionList()
                           .add(relAdaptor);
               }
            });
   }

   private void processNode(Node<? extends LogicGraphSememe<?>> node,
                            LogicalExpression previousExpression,
                            Stream.Builder<RelationshipVersionAdaptorImpl> streamBuilder,
                            PremiseType premiseType) {
      final LogicalExpression newExpression         = node.getData()
                                                    .getLogicalExpression();
      final int               stampSequence         = node.getData()
                                                    .getStampSequence();
      final int               inactiveStampSequence = Get.stampService()
                                                   .getRetiredStampSequence(stampSequence);

      if (previousExpression == null) {
         processRootExpression(newExpression, streamBuilder, stampSequence, premiseType);
      } else {
         final IsomorphicResults comparison = newExpression.findIsomorphisms(previousExpression);

         comparison.getAddedRelationshipRoots()
                   .forEach((addedRelRoot) -> processRelNode(addedRelRoot,
                         streamBuilder,
                         newExpression,
                         stampSequence,
                         premiseType));
         comparison.getDeletedRelationshipRoots()
                   .forEach((addedRelRoot) -> processRelNode(addedRelRoot,
                         streamBuilder,
                         newExpression,
                         inactiveStampSequence,
                         premiseType));
      }

      for (final Node<? extends LogicGraphSememe<?>> child: node.getChildren()) {
         processNode(child, newExpression, streamBuilder, premiseType);
      }
   }

   private void processRelNode(LogicNode aLogicNode,
                               Stream.Builder<RelationshipVersionAdaptorImpl> streamBuilder,
                               LogicalExpression expression,
                               int stampSequence,
                               PremiseType premiseType)
            throws UnsupportedOperationException {
      switch (aLogicNode.getNodeSemantic()) {
      case CONCEPT:
         streamBuilder.accept(createIsaRel(expression.getConceptSequence(),
                                           (ConceptNodeWithSequences) aLogicNode,
                                           stampSequence,
                                           premiseType));
         break;

      case ROLE_SOME:
         createSomeRole(expression.getConceptSequence(),
                        (RoleNodeSomeWithSequences) aLogicNode,
                        stampSequence,
                        premiseType,
                        0).forEach((someRelAdaptor) -> {
                                      streamBuilder.accept(someRelAdaptor);
                                   });
         break;

      case FEATURE:
         break;  // TODO Keith, not sure how this should be handled

      default:
         throw new UnsupportedOperationException("Can't handle: " + aLogicNode.getNodeSemantic());
      }
   }

   private void processRootExpression(LogicalExpression expression,
                                      Stream.Builder<RelationshipVersionAdaptorImpl> streamBuilder,
                                      int stampSequence,
                                      PremiseType premiseType) {
      expression.getRoot().getChildStream().forEach((necessaryOrSufficientSet) -> {
                            necessaryOrSufficientSet.getChildStream()
                                  .forEach((LogicNode andOrOrLogicNode) -> andOrOrLogicNode.getChildStream()
                                        .forEach((LogicNode aLogicNode) -> {
                  processRelNode(aLogicNode, streamBuilder, expression, stampSequence, premiseType);
               }));
                         });
   }

   @PostConstruct
   private void startMe() {
      log.info("Starting LogicProvider.");
   }

   @PreDestroy
   private void stopMe() {
      log.info("Stopping LogicProvider.");
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ClassifierService getClassifierService(StampCoordinate stampCoordinate,
         LogicCoordinate logicCoordinate,
         EditCoordinate editCoordinate) {
      final ClassifierServiceKey key = new ClassifierServiceKey(stampCoordinate, logicCoordinate, editCoordinate);

      if (!classifierServiceMap.containsKey(key)) {
         classifierServiceMap.putIfAbsent(key,
                                          new ClassifierProvider(stampCoordinate, logicCoordinate, editCoordinate));
      }

      return classifierServiceMap.get(key);
   }

   @Override
   public Optional<LatestVersion<? extends LogicalExpression>> getLogicalExpression(int conceptId,
         int logicAssemblageId,
         StampCoordinate stampCoordinate) {
      final SememeSnapshotService<LogicGraphSememeImpl> ssp = Get.sememeService()
                                                           .getSnapshot(LogicGraphSememeImpl.class, stampCoordinate);
      final List<LatestVersion<LogicalExpressionOchreImpl>> latestVersions =
         ssp.getLatestSememeVersionsForComponentFromAssemblage(conceptId,
                                                               logicAssemblageId)
            .map((LatestVersion<LogicGraphSememeImpl> lgs) -> {
                    final LogicalExpressionOchreImpl expressionValue = new LogicalExpressionOchreImpl(lgs.value().getGraphData(),
                                                                                                DataSource.INTERNAL,
                                                                                                lgs.value().getReferencedComponentNid());
                    final LatestVersion<LogicalExpressionOchreImpl> latestExpressionValue = new LatestVersion<>(expressionValue);

                    if (lgs.contradictions()
                           .isPresent()) {
                       lgs.contradictions().get().forEach((LogicGraphSememeImpl contradiction) -> {
                                      final LogicalExpressionOchreImpl contradictionValue =
                                         new LogicalExpressionOchreImpl(contradiction.getGraphData(),
                                                                        DataSource.INTERNAL,
                                                                        contradiction.getReferencedComponentNid());

                                      latestExpressionValue.addLatest(contradictionValue);
                                   });
                    }

                    return latestExpressionValue;
                 })
            .collect(Collectors.toList());

      if (latestVersions.isEmpty()) {
         return Optional.empty();
      }

      if (latestVersions.size() > 1) {
         throw new IllegalStateException("More than one LogicGraphSememeImpl for concept in assemblage: " +
                                         latestVersions);
      }

      return Optional.of(latestVersions.get(0));
   }

   @Override
   public Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipAdaptorsOriginatingWithConcept(
           ConceptChronology<?> conceptChronology) {
      return getRelationshipAdaptorsOriginatingWithConcept(conceptChronology, LogicCoordinates.getStandardElProfile());
   }

   @Override
   public Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipAdaptorsOriginatingWithConcept(
           ConceptChronology<?> conceptChronology,
           LogicCoordinate logicCoordinate) {
      final Stream.Builder<RelationshipAdaptorChronologyImpl> streamBuilder = Stream.builder();
      final HashMap<RelationshipAdaptorChronicleKey, RelationshipAdaptorChronologyImpl> conceptOriginRelationshipMap =
         new HashMap<>();
      final List<SememeChronology<? extends SememeVersion>> statedDefinitions = Get.sememeService()
                                                                             .getSememesForComponentFromAssemblage(
                                                                                conceptChronology.getNid(),
                                                                                      logicCoordinate.getStatedAssemblageSequence())
                                                                             .collect(Collectors.toList());
      final List<SememeChronology<? extends SememeVersion>> inferredDefinitions = Get.sememeService()
                                                                               .getSememesForComponentFromAssemblage(
                                                                                  conceptChronology.getNid(),
                                                                                        logicCoordinate.getInferredAssemblageSequence())
                                                                               .collect(Collectors.toList());

      statedDefinitions.forEach((statedDef) -> {
                                   generateRelAdaptorChronicles(statedDef,
                                         conceptOriginRelationshipMap,
                                         PremiseType.STATED);
                                });
      inferredDefinitions.forEach((inferredDef) -> {
                                     generateRelAdaptorChronicles(inferredDef,
                                           conceptOriginRelationshipMap,
                                           PremiseType.INFERRED);
                                  });
      conceptOriginRelationshipMap.values().stream().forEach((relAdaptor) -> {
               streamBuilder.accept(relAdaptor);
            });
      return streamBuilder.build();
   }

   @Override
   public Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipAdaptorsWithConceptAsDestination(
           ConceptChronology<?> conceptChronology) {
      return getRelationshipAdaptorsWithConceptAsDestination(conceptChronology,
            LogicCoordinates.getStandardElProfile());
   }

   @Override
   public Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipAdaptorsWithConceptAsDestination(
           ConceptChronology<?> conceptChronology,
           LogicCoordinate logicCoordinate) {
      final List<SememeChronology<? extends SememeVersion>>   statedDefinitions = new ArrayList<>();
      final List<SememeChronology<? extends SememeVersion>> inferredDefinitions = new ArrayList<>();
      final Stream.Builder<RelationshipAdaptorChronologyImpl> streamBuilder = Stream.builder();
      final HashMap<RelationshipAdaptorChronicleKey, RelationshipAdaptorChronologyImpl> conceptDestinationRelationshipMap =
         new HashMap<>();

      Get.taxonomyService()
         .getAllRelationshipOriginSequences(conceptChronology.getConceptSequence())
         .forEach((originConceptSequence) -> {
                     statedDefinitions.addAll(Get.sememeService()
                           .getSememesForComponentFromAssemblage(originConceptSequence,
                                 logicCoordinate.getStatedAssemblageSequence())
                           .collect(Collectors.toList()));
                     inferredDefinitions.addAll(Get.sememeService()
                           .getSememesForComponentFromAssemblage(originConceptSequence,
                                 logicCoordinate.getInferredAssemblageSequence())
                           .collect(Collectors.toList()));
                  });
      statedDefinitions.forEach((statedDef) -> {
                                   generateRelAdaptorChronicles(conceptChronology.getConceptSequence(),
                                         statedDef,
                                         conceptDestinationRelationshipMap,
                                         PremiseType.STATED);
                                });
      inferredDefinitions.forEach((inferredDef) -> {
                                     generateRelAdaptorChronicles(conceptChronology.getConceptSequence(),
                                           inferredDef,
                                           conceptDestinationRelationshipMap,
                                           PremiseType.INFERRED);
                                  });
      conceptDestinationRelationshipMap.values().stream().forEach((relAdaptor) -> {
               streamBuilder.accept(relAdaptor);
            });
      return streamBuilder.build();
   }

   //~--- inner classes -------------------------------------------------------

   private static class ClassifierServiceKey {
      StampCoordinate stampCoordinate;
      LogicCoordinate logicCoordinate;
      EditCoordinate  editCoordinate;

      //~--- constructors -----------------------------------------------------

      public ClassifierServiceKey(StampCoordinate stampCoordinate,
                                  LogicCoordinate logicCoordinate,
                                  EditCoordinate editCoordinate) {
         this.stampCoordinate = stampCoordinate;
         this.logicCoordinate = logicCoordinate;
         this.editCoordinate  = editCoordinate;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         }

         if (getClass() != obj.getClass()) {
            return false;
         }

         final ClassifierServiceKey other = (ClassifierServiceKey) obj;

         if (!Objects.equals(this.stampCoordinate, other.stampCoordinate)) {
            return false;
         }

         if (!Objects.equals(this.logicCoordinate, other.logicCoordinate)) {
            return false;
         }

         return Objects.equals(this.editCoordinate, other.editCoordinate);
      }

      @Override
      public int hashCode() {
         int hash = 3;

         hash = 59 * hash + Objects.hashCode(this.logicCoordinate);
         return hash;
      }
   }
}

