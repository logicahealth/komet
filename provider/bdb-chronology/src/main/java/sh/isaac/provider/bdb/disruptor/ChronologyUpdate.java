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
package sh.isaac.provider.bdb.disruptor;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.bootstrap.TermAux;

import sh.isaac.api.collections.SpinedIntObjectMap;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.dag.Node;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.logic.IsomorphicResultsBottomUp;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithSequences;
import sh.isaac.provider.bdb.taxonomy.TaxonomyFlag;
import sh.isaac.provider.bdb.taxonomy.TaxonomyRecord;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ChronologyUpdate {

   private static final Logger LOG = LogManager.getLogger();
   private static final int INFERRED_ASSEMBLAGE_SEQUENCE;
   private static final int ISA_SEQUENCE;
   private static final int ROLE_GROUP_SEQUENCE;
   private static final IdentifierService IDENTIFIER_SERVICE;
   static SpinedIntObjectMap<int[]> origin_DestinationTaxonomyRecord_Map;

   public static void setOrigin_DestinationTaxonomyRecord_Map(SpinedIntObjectMap<int[]> origin_DestinationTaxonomyRecord_Map) {
      ChronologyUpdate.origin_DestinationTaxonomyRecord_Map = origin_DestinationTaxonomyRecord_Map;
   }

   //~--- static initializers -------------------------------------------------
   static {
         INFERRED_ASSEMBLAGE_SEQUENCE = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getConceptSequence();
         ISA_SEQUENCE = TermAux.IS_A.getConceptSequence();
         ROLE_GROUP_SEQUENCE = TermAux.ROLE_GROUP.getConceptSequence();
         IDENTIFIER_SERVICE = Get.identifierService();
   }

   public static void handleStatusUpdate(ConceptChronology conceptChronology) {
      TaxonomyRecord taxonomyRecord = new TaxonomyRecord();
         for (int stampSequence : conceptChronology.getVersionStampSequences()) {
            taxonomyRecord
                    .addStampRecord(
                            conceptChronology.getConceptSequence(),
                            conceptChronology.getConceptSequence(),
                            stampSequence,
                            TaxonomyFlag.CONCEPT_STATUS.bits);
         }
         
         origin_DestinationTaxonomyRecord_Map.accumulateAndGet(conceptChronology.getConceptSequence(), null, (existing, u) -> {
            taxonomyRecord.merge(new TaxonomyRecord(existing));
            return taxonomyRecord.pack(); 
         });
   }

   //~--- methods -------------------------------------------------------------

   public static void handleTaxonomyUpdate(SemanticChronology logicGraphChronology) {

         TaxonomyFlag taxonomyFlags;

         if (logicGraphChronology.getAssemblageSequence() == INFERRED_ASSEMBLAGE_SEQUENCE) {
            taxonomyFlags = TaxonomyFlag.INFERRED;
         } else {
            taxonomyFlags = TaxonomyFlag.STATED;
         }

         final List<Graph<LogicGraphVersion>> versionGraphList = logicGraphChronology.getVersionGraphList();
         TaxonomyRecord taxonomyRecord = new TaxonomyRecord();
         for (Graph<LogicGraphVersion> versionGraph: versionGraphList) {
            processVersionNode(versionGraph.getRoot(), taxonomyRecord, taxonomyFlags);
         }
         int conceptSequence = IDENTIFIER_SERVICE.getConceptSequence(logicGraphChronology.getReferencedComponentNid());
         origin_DestinationTaxonomyRecord_Map.accumulateAndGet(conceptSequence, null, (existing, u) -> {
            taxonomyRecord.merge(new TaxonomyRecord(existing));
            return taxonomyRecord.pack(); 
         });
   }

   /**
    * Process version node.
    *
    * @param node the node
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    */
   private static void processVersionNode(Node<? extends LogicGraphVersion> node,
                                   TaxonomyRecord parentTaxonomyRecord,
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
    * Process relationship root.
    *
    * @param logicNode the logical logic node
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    * @param stampSequence the stamp sequence
    * @param comparisonExpression the comparison expression
    */
   private static void processRelationshipRoot(LogicNode logicNode,
         TaxonomyRecord parentTaxonomyRecord,
         TaxonomyFlag taxonomyFlags,
         int stampSequence,
         LogicalExpression comparisonExpression) {
      switch (logicNode.getNodeSemantic()) {
      case CONCEPT:
         updateIsaRel(((ConceptNodeWithSequences)logicNode).getConceptSequence(),
             parentTaxonomyRecord,
             taxonomyFlags,
             stampSequence);
         break;

      case ROLE_SOME:
         updateSomeRole((RoleNodeSomeWithSequences) logicNode,
             parentTaxonomyRecord,
             taxonomyFlags,
             stampSequence);
         break;

      case FEATURE:

         // Features do not have taxonomy implications...
         break;

      default:
         throw new UnsupportedOperationException("at Can't handle: " + logicNode.getNodeSemantic());
      }
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
    * Update isa rel.
    *
    * @param conceptNode the concept node
    * @param parentTaxonomyRecord the parent taxonomy record
    * @param taxonomyFlags the taxonomy flags
    * @param stampSequence the stamp sequence
    * @param originSequence the origin sequence
    */
   private static void updateIsaRel(int originSequence,
                             TaxonomyRecord parentTaxonomyRecord,
                             TaxonomyFlag taxonomyFlags,
                             int stampSequence) {
      parentTaxonomyRecord.addStampRecord(originSequence,
                              ISA_SEQUENCE,
                              stampSequence,
                              taxonomyFlags.bits);
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
   private static void updateSomeRole(RoleNodeSomeWithSequences someNode,
                               TaxonomyRecord parentTaxonomyRecord,
                               TaxonomyFlag taxonomyFlags,
                               int stampSequence) {
      if (someNode.getTypeConceptSequence() == ROLE_GROUP_SEQUENCE) {
         final AndNode andNode = (AndNode) someNode.getOnlyChild();

         andNode.getChildStream()
                .forEach(
                    (roleGroupSomeNode) -> {
                       if (roleGroupSomeNode instanceof RoleNodeSomeWithSequences) {
                          updateSomeRole(
                              (RoleNodeSomeWithSequences) roleGroupSomeNode,
                              parentTaxonomyRecord,
                              taxonomyFlags,
                              stampSequence);
                       } else {
                          // TODO Dan put this here to stop a pile of errors....
                          // one of the types coming back was a FeatureNodeWithSequences - not sure what to do with it.
                       }
                    });
      } else {
         if (someNode.getOnlyChild() instanceof ConceptNodeWithSequences) {
            final ConceptNodeWithSequences restrictionNode = (ConceptNodeWithSequences) someNode.getOnlyChild();

            parentTaxonomyRecord.addStampRecord(
                                    restrictionNode.getConceptSequence(),
                                    someNode.getTypeConceptSequence(),
                                    stampSequence,
                                    taxonomyFlags.bits);
         } else {
            // TODO dan put this here to stop a pile of errors. It was returning AndNode.  Not sure what to do with it
         }
      }
   }
}
