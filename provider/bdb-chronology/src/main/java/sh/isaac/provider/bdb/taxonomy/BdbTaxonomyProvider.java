/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.provider.bdb.taxonomy;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import javafx.concurrent.Task;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.ConceptActiveService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.SpinedIntObjectMap;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.dag.Node;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.tree.Tree;
import sh.isaac.model.logic.IsomorphicResultsBottomUp;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithSequences;

/**
 *
 * @author kec
 */
public class BdbTaxonomyProvider 
         implements TaxonomyService, ConceptActiveService, ChronologyChangeListener {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();
   
      /**
    * The identifier service.
    */
   private IdentifierService identifierService;

      /**
    * The {@code taxonomyMap} associates concept sequence keys with a primitive taxonomy record, which represents the
    * destination, stamp, and taxonomy flags for parent and child concepts.
    */

   private final SpinedIntObjectMap<int[]> originDestinationTaxonomyRecordMap = new SpinedIntObjectMap<>();
   /**
    * The destination origin record set.
    */
      //TODO
//   private final SpinedIntObjectMap<DestinationOriginRecord> destinationOriginRecordSet =
//      new SpinedIntObjectMap<>();

   private final int inferredAssemblageSequence; 
   private final int isaSequence; 
   private final int roleGroupSequence; 

   public BdbTaxonomyProvider(int inferredAssemblageSequence, int isaSequence, int roleGroupSequence) {
      this.inferredAssemblageSequence = inferredAssemblageSequence;
      this.isaSequence = isaSequence;
      this.roleGroupSequence = roleGroupSequence;
   }
   
   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting TaxonomyService post-construct");

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
      //this.originDestinationTaxonomyRecordMap.write();
   }

   @Override
   public void updateTaxonomy(SemanticChronology logicGraphChronology) {
       final int conceptSequence = this.identifierService.getConceptSequence(
                                      logicGraphChronology.getReferencedComponentNid());
      final int[] taxonomyData = this.originDestinationTaxonomyRecordMap.get(conceptSequence);
      TaxonomyRecordPrimitive                 parentTaxonomyRecord;

      if (taxonomyData != null) {
         parentTaxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);
      } else {
         parentTaxonomyRecord = new TaxonomyRecordPrimitive();
      }

      TaxonomyFlag taxonomyFlags;

      if (logicGraphChronology.getAssemblageSequence() == inferredAssemblageSequence) {
         taxonomyFlags = TaxonomyFlag.INFERRED;
      } else {
         taxonomyFlags = TaxonomyFlag.STATED;
      }

      final List<Graph<LogicGraphVersion>> versionGraphList = logicGraphChronology.getVersionGraphList();

      versionGraphList.forEach(
          (versionGraph) -> {
             processVersionNode(versionGraph.getRoot(), parentTaxonomyRecord, taxonomyFlags);
          });
      this.originDestinationTaxonomyRecordMap.put(conceptSequence, parentTaxonomyRecord.taxonomyData);
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
      //TODO
//      this.destinationOriginRecordSet.add(
//          new DestinationOriginRecord(conceptNode.getConceptSequence(), originSequence));
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
      //TODO
//            this.destinationOriginRecordSet.add(
//                new DestinationOriginRecord(restrictionNode.getConceptSequence(), originSequence));
         } else {
            // TODO dan put this here to stop a pile of errors. It was returning AndNode.  Not sure what to do with it
         }
      }
   }

   @Override
   public boolean wasEverKindOf(int childId, int parentId) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Task<TaxonomySnapshotService> getSnapshot(ManifoldCoordinate tc) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getTaxonomyChildSequences(int parentId) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getTaxonomyParentSequences(int childId) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   public Task<Tree> getTaxonomyTree(ManifoldCoordinate tc) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void clearDatabaseValidityValue() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Path getDatabaseFolder() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public UUID getDataStoreId() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void updateStatus(ConceptChronology conceptChronology) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void handleChange(ConceptChronology cc) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void handleChange(SemanticChronology sc) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void handleCommit(CommitRecord commitRecord) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public UUID getListenerUuid() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   
}
