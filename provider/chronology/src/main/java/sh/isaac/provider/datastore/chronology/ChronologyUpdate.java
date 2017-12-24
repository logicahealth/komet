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



package sh.isaac.provider.datastore.chronology;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.dag.Node;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.logic.IsomorphicResultsBottomUp;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.provider.datastore.identifier.IdentifierProvider;
import sh.isaac.provider.datastore.taxonomy.TaxonomyProvider;
import sh.isaac.model.taxonomy.TaxonomyFlag;
import sh.isaac.model.taxonomy.TaxonomyRecord;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ChronologyUpdate {
   private static final Logger                LOG                 = LogManager.getLogger();
   private static final int                   INFERRED_ASSEMBLAGE_NID;
   private static final int                   ISA_NID;
   private static final int                   CHILD_OF_NID;
   private static final int                   ROLE_GROUP_NID;
   private static final IdentifierProvider IDENTIFIER_SERVICE;
   private static final TaxonomyProvider   TAXONOMY_SERVICE;

   //~--- static initializers -------------------------------------------------

   static {
      INFERRED_ASSEMBLAGE_NID = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid();
      CHILD_OF_NID            = TermAux.CHILD_OF.getNid();
      ISA_NID                 = TermAux.IS_A.getNid();
      ROLE_GROUP_NID          = TermAux.ROLE_GROUP.getNid();
      IDENTIFIER_SERVICE      = Get.service(IdentifierProvider.class);
      TAXONOMY_SERVICE        = Get.service(TaxonomyProvider.class);
   }

   //~--- methods -------------------------------------------------------------

   public static void handleStatusUpdate(ConceptChronology conceptChronology) {
      TaxonomyRecord taxonomyRecord = new TaxonomyRecord();

      for (int stampSequence: conceptChronology.getVersionStampSequences()) {
         taxonomyRecord.addStampRecord(
             conceptChronology.getNid(),
             conceptChronology.getNid(),
             stampSequence,
             TaxonomyFlag.CONCEPT_STATUS.bits);
      }

      SpinedIntIntArrayMap map = TAXONOMY_SERVICE.getTaxonomyRecordMap(conceptChronology.getAssemblageNid());
      map.accumulateAndGet(
                         conceptChronology.getNid(),
                         taxonomyRecord.pack(),
                             ChronologyUpdate::merge);
   }

   public static void handleTaxonomyUpdate(SemanticChronology logicGraphChronology) {
      int referencedComponentNid = logicGraphChronology.getReferencedComponentNid();
      int conceptAssemblageNid   = IDENTIFIER_SERVICE.getAssemblageNidForNid(referencedComponentNid);
      

//    System.out.println("Taxonomy update " + taxonomyUpdateCount.getAndIncrement() + " for: " + 
//            referencedComponentNid + " index: " + 
//            ModelGet.identifierService().getElementSequenceForNid(referencedComponentNid));
      TaxonomyFlag taxonomyFlags;

      if (logicGraphChronology.getAssemblageNid() == INFERRED_ASSEMBLAGE_NID) {
         taxonomyFlags = TaxonomyFlag.INFERRED;
      } else {
         taxonomyFlags = TaxonomyFlag.STATED;
      }

      final List<Graph<LogicGraphVersion>> versionGraphList = logicGraphChronology.getVersionGraphList();
      TaxonomyRecord                       taxonomyRecordForConcept   = new TaxonomyRecord();

      for (Graph<LogicGraphVersion> versionGraph: versionGraphList) {
         // this is the CPU hog...
         processVersionNode(referencedComponentNid, versionGraph.getRoot(), taxonomyRecordForConcept, taxonomyFlags);
      }

      SpinedIntIntArrayMap origin_DestinationTaxonomyRecord_Map = TAXONOMY_SERVICE.getTaxonomyRecordMap(
                                                                           conceptAssemblageNid);
      int[] start = taxonomyRecordForConcept.pack();
      int[] result = origin_DestinationTaxonomyRecord_Map.accumulateAndGet(
                         logicGraphChronology.getReferencedComponentNid(),
                         start, ChronologyUpdate::merge);

      if (start.length > result.length) {
         LOG.error("Accumulate shrank");
      } else if (result.length == start.length) {
         LOG.error("Did not grow");
      }
   }
   
   private static int[] merge(int[] existing, int[] update) {
      TaxonomyRecord existingRecord = new TaxonomyRecord(existing);

      existingRecord.merge(new TaxonomyRecord(update));
      return existingRecord.pack();
   }

   /**
    * Process new logic graph.
    *
    * @param firstVersion the first version
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    */
   private static void processNewLogicGraph(LogicGraphVersion firstVersion,
         TaxonomyRecord parentTaxonomyRecord,
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
                                                 processRelationshipRoot(firstVersion.getReferencedComponentNid(),
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
    * @param logicNode the logical logic node
    * @param taxonomyRecordForConcept the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    * @param stampSequence the stamp sequence
    * @param comparisonExpression the comparison expression
    */
   private static void processRelationshipRoot(int conceptNid, LogicNode logicNode,
         TaxonomyRecord taxonomyRecordForConcept,
         TaxonomyFlag taxonomyFlags,
         int stampSequence,
         LogicalExpression comparisonExpression) {
      switch (logicNode.getNodeSemantic()) {
      case CONCEPT:
         updateIsaRel(conceptNid,
             ((ConceptNodeWithNids) logicNode).getConceptNid(),
             taxonomyRecordForConcept,
             taxonomyFlags,
             stampSequence);
         break;

      case ROLE_SOME:
         updateSomeRole((RoleNodeSomeWithNids) logicNode, taxonomyRecordForConcept, taxonomyFlags, stampSequence);
         break;

      case FEATURE:

         // Features do not have taxonomy implications...
         break;

      default:
         throw new UnsupportedOperationException("at Can't handle: " + logicNode.getNodeSemantic());
      }
   }

   /**
    * Process version node.
    *
    * @param node the node
    * @param taxonomyRecordForConcept the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    */
   private static void processVersionNode(int conceptNid, Node<? extends LogicGraphVersion> node,
         TaxonomyRecord taxonomyRecordForConcept,
         TaxonomyFlag taxonomyFlags) {
      if (node.getParent() == null) {
         processNewLogicGraph(node.getData(), taxonomyRecordForConcept, taxonomyFlags);
      } else {
         final LogicalExpression comparisonExpression = node.getParent()
                                                            .getData()
                                                            .getLogicalExpression();
         final LogicalExpression referenceExpression  = node.getData()
                                                            .getLogicalExpression();
         final IsomorphicResultsBottomUp isomorphicResults = new IsomorphicResultsBottomUp(
                                                                 referenceExpression,
                                                                       comparisonExpression);

         for (LogicNode relationshipRoot: isomorphicResults.getAddedRelationshipRoots()) {
            final int stampSequence = node.getData()
                                          .getStampSequence();

            processRelationshipRoot(conceptNid,
                relationshipRoot,
                taxonomyRecordForConcept,
                taxonomyFlags,
                stampSequence,
                comparisonExpression);
         }

         for (LogicNode relationshipRoot: isomorphicResults.getDeletedRelationshipRoots()) {
            final int activeStampSequence = node.getData()
                                                .getStampSequence();
            final int stampSequence       = Get.stampService()
                                               .getRetiredStampSequence(activeStampSequence);

            processRelationshipRoot(conceptNid,
                relationshipRoot,
                taxonomyRecordForConcept,
                taxonomyFlags,
                stampSequence,
                comparisonExpression);
         }
      }

      for (Node<? extends LogicGraphVersion> childNode: node.getChildren()) {
         processVersionNode(conceptNid, childNode, taxonomyRecordForConcept, taxonomyFlags);
      }
   }

   /**
    * Update isa rel.
    *
    * @param conceptNode the concept node
    * @param taxonomyRecordForConcept the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    * @param stampSequence the stamp sequence
    * @param destinationNid the destination nid
    */
   private static void updateIsaRel(int originNid,
                                    int destinationNid,
                                    TaxonomyRecord taxonomyRecordForConcept,
                                    TaxonomyFlag taxonomyFlags,
                                    int stampSequence) {
      taxonomyRecordForConcept.addStampRecord(destinationNid, ISA_NID, stampSequence, taxonomyFlags.bits);
      
      TaxonomyRecord destinationTaxonomyRecord = new TaxonomyRecord();
      destinationTaxonomyRecord.addStampRecord(originNid, CHILD_OF_NID, stampSequence, taxonomyFlags.bits);

      int conceptAssemblageNid   = IDENTIFIER_SERVICE.getAssemblageNidForNid(originNid);
      SpinedIntIntArrayMap map = TAXONOMY_SERVICE.getOrigin_DestinationTaxonomyRecord_Map(conceptAssemblageNid);
      map.accumulateAndGet(
                         destinationNid,
                         destinationTaxonomyRecord.pack(),
                             ChronologyUpdate::merge);
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
   private static void updateSomeRole(RoleNodeSomeWithNids someNode,
                                      TaxonomyRecord parentTaxonomyRecord,
                                      TaxonomyFlag taxonomyFlags,
                                      int stampSequence) {
      if (someNode.getTypeConceptNid() == ROLE_GROUP_NID) {
         final AndNode andNode = (AndNode) someNode.getOnlyChild();

         andNode.getChildStream()
                .forEach(
                    (roleGroupSomeNode) -> {
                       if (roleGroupSomeNode instanceof RoleNodeSomeWithNids) {
                          updateSomeRole(
                              (RoleNodeSomeWithNids) roleGroupSomeNode,
                              parentTaxonomyRecord,
                              taxonomyFlags,
                              stampSequence);
                       } else {
                          // TODO Dan put this here to stop a pile of errors....
                          // one of the types coming back was a FeatureNodeWithSequences - not sure what to do with it.
                       }
                    });
      } else {
         if (someNode.getOnlyChild() instanceof ConceptNodeWithNids) {
            final ConceptNodeWithNids restrictionNode = (ConceptNodeWithNids) someNode.getOnlyChild();

            parentTaxonomyRecord.addStampRecord(
                restrictionNode.getConceptNid(),
                someNode.getTypeConceptNid(),
                stampSequence,
                taxonomyFlags.bits);
         } else {
            // TODO dan put this here to stop a pile of errors. It was returning AndNode.  Not sure what to do with it
         }
      }
   }
}

