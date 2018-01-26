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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.IsaacCache;
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
import sh.isaac.model.taxonomy.TypeStampTaxonomyRecords;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
@Service
@Singleton
public class ChronologyUpdate implements IsaacCache{

   private static final Logger LOG = LogManager.getLogger();
   private static int INFERRED_ASSEMBLAGE_NID;
   private static int ISA_NID;
   private static int CHILD_OF_NID;
   private static int ROLE_GROUP_NID;
   private static IdentifierProvider IDENTIFIER_SERVICE;
   private static TaxonomyProvider TAXONOMY_SERVICE;

   private ChronologyUpdate() {
      //For HK2 only
   }
   
   private static void initCheck() {
      if (TAXONOMY_SERVICE == null) {
         INFERRED_ASSEMBLAGE_NID = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid();
         CHILD_OF_NID = TermAux.CHILD_OF.getNid();
         ISA_NID = TermAux.IS_A.getNid();
         ROLE_GROUP_NID = TermAux.ROLE_GROUP.getNid();
         IDENTIFIER_SERVICE = Get.service(IdentifierProvider.class);
         TAXONOMY_SERVICE = Get.service(TaxonomyProvider.class);
      }
   }

   //~--- methods -------------------------------------------------------------
   public static void handleStatusUpdate(ConceptChronology conceptChronology) {
      initCheck();
      TaxonomyRecord taxonomyRecord = new TaxonomyRecord();

      for (int stampSequence : conceptChronology.getVersionStampSequences()) {
         taxonomyRecord.addStampRecord(
               conceptChronology.getNid(),
               conceptChronology.getNid(),
               stampSequence,
               TaxonomyFlag.CONCEPT_STATUS.bits);
      }

      SpinedIntIntArrayMap map = TAXONOMY_SERVICE.getTaxonomyRecordMap(conceptChronology.getAssemblageNid());
      int[] record = taxonomyRecord.pack();
      //TaxonomyRecord.validate(record);
      map.accumulateAndGet(
            conceptChronology.getNid(),
            record,
            ChronologyUpdate::merge);
   }

   public static void handleTaxonomyUpdate(SemanticChronology logicGraphChronology) {
      initCheck();
      int referencedComponentNid = logicGraphChronology.getReferencedComponentNid();
      int conceptAssemblageNid = IDENTIFIER_SERVICE.getAssemblageNid(referencedComponentNid).getAsInt();

//   System.out.println("Taxonomy update " + taxonomyUpdateCount.getAndIncrement() + " for: " + 
//         referencedComponentNid + " index: " + 
//         ModelGet.identifierService().getElementSequenceForNid(referencedComponentNid));
      TaxonomyFlag taxonomyFlags;

      if (logicGraphChronology.getAssemblageNid() == INFERRED_ASSEMBLAGE_NID) {
         taxonomyFlags = TaxonomyFlag.INFERRED;
      } else {
         taxonomyFlags = TaxonomyFlag.STATED;
      }

      final List<Graph<LogicGraphVersion>> versionGraphList = logicGraphChronology.getVersionGraphList();
      TaxonomyRecord taxonomyRecordForConcept = new TaxonomyRecord();

      for (Graph<LogicGraphVersion> versionGraph : versionGraphList) {
         // this is the CPU hog...
         processVersionNode(referencedComponentNid, versionGraph.getRoot(), taxonomyRecordForConcept, taxonomyFlags);
      }

      SpinedIntIntArrayMap origin_DestinationTaxonomyRecord_Map = TAXONOMY_SERVICE.getTaxonomyRecordMap(
            conceptAssemblageNid);
      int[] start = taxonomyRecordForConcept.pack();
      //start = start.clone();
      //TaxonomyRecord.validate(start);
      //int[] begin = origin_DestinationTaxonomyRecord_Map.get(logicGraphChronology.getReferencedComponentNid());
      //begin = begin.clone();
      int[] result = origin_DestinationTaxonomyRecord_Map.accumulateAndGet(
            logicGraphChronology.getReferencedComponentNid(),
            start, ChronologyUpdate::merge);
      //result = result.clone();
      if (start.length > result.length) {
         TaxonomyRecord taxonomyRecordResult = new TaxonomyRecord(result);
         LOG.error("Accumulate shrank");
//         origin_DestinationTaxonomyRecord_Map.put(logicGraphChronology.getReferencedComponentNid(), begin);
//         int[] result2 = origin_DestinationTaxonomyRecord_Map.accumulateAndGet(
//               logicGraphChronology.getReferencedComponentNid(),
//               start, ChronologyUpdate::merge);
//         if (Arrays.equals(result, result2)) {
//            LOG.error("Results are equal. ");
//         } else {
//            LOG.error("Results are not equal. ");
//         }

      } else if (result.length == start.length) {
         LOG.error("Did not grow");
      }
   }

   private static int[] merge(int[] existing, int[] update) {
      if (existing == null) {
         existing = new int[0];
      }
      //TaxonomyRecord.validate(existing);
      //TaxonomyRecord.validate(update);
      OpenIntIntHashMap updateConceptsRecordStartMap = new OpenIntIntHashMap();
      updateConceptsRecordStartMap.put(update[0], 0);
      int recordEnd = update[1] + 1;
      while (recordEnd < update.length) {
         if (recordEnd < 0) {
            throw new IllegalStateException("Record end cannot be negative. ");
         }
         int updateConcept = update[recordEnd];
         if (updateConceptsRecordStartMap.containsKey(updateConcept)) {
            throw new IllegalStateException("Concept in update record twice");
         }
         updateConceptsRecordStartMap.put(updateConcept, recordEnd);
         recordEnd = recordEnd + update[recordEnd + 1] + 1;
      }

      // if length changed, then use this lengthChangedList for the merged result
      AtomicReference<IntArrayList> lengthChangedListReference = new AtomicReference<>();

      for (int i = 0; i < existing.length;) {
         int currentConceptNid = existing[i];
         int length = existing[i + 1] + 1;
         if (updateConceptsRecordStartMap.containsKey(currentConceptNid)) {
            int updateRecordStartIndex = updateConceptsRecordStartMap.get(currentConceptNid);
            updateConceptsRecordStartMap.removeKey(currentConceptNid);

            TypeStampTaxonomyRecords existingRecords = new TypeStampTaxonomyRecords(existing, i + 1);
            int[] existingRecArray = existingRecords.toArray();
            TypeStampTaxonomyRecords updateRecords = new TypeStampTaxonomyRecords(update, updateRecordStartIndex + 1);
            existingRecords.merge(updateRecords);
            int[] updatedExistingTypeStampRecords = existingRecords.toArray();

            if (!Arrays.equals(updatedExistingTypeStampRecords, existingRecArray)) {
               if (existingRecArray.length == updatedExistingTypeStampRecords.length) {
                  IntArrayList lengthChangedList = lengthChangedListReference.get();

                  // case 1, same size, updated flag...
                  if (lengthChangedList == null) {
                     System.arraycopy(updatedExistingTypeStampRecords, 0, existing, i + 2, updatedExistingTypeStampRecords.length);
                  } else {
                     // add concept
                     lengthChangedList.add(currentConceptNid);
                     // add length
                     lengthChangedList.add(updatedExistingTypeStampRecords.length + 1);
                     for (int mergedArrayItem : updatedExistingTypeStampRecords) {
                        // then add type stamp flag records. 
                        lengthChangedList.add(mergedArrayItem);
                     }
                  }

               } else {
                  // case 2, different size. 
                  IntArrayList lengthChangedList = lengthChangedListReference.get();
                  if (lengthChangedList == null) {
                     lengthChangedList = new IntArrayList();
                     for (int existingIndex = 0; existingIndex < i; existingIndex++) {
                        lengthChangedList.add(existing[existingIndex]);
                     }
                     lengthChangedListReference.set(lengthChangedList);
                  }
                  // add concept
                  lengthChangedList.add(currentConceptNid);
                  // add length
                  lengthChangedList.add(updatedExistingTypeStampRecords.length + 1);
                  for (int mergedArrayItem : updatedExistingTypeStampRecords) {
                     // then add type stamp flag records. 
                     lengthChangedList.add(mergedArrayItem);
                  }
               }
            } else if (lengthChangedListReference.get() != null) {
               IntArrayList lengthChangedList = lengthChangedListReference.get();
               // just copy to result, as it will not change. 
               int copyEnd = i + length;
               for (int copyIndex = i; copyIndex < copyEnd; copyIndex++) {
                  lengthChangedList.add(existing[copyIndex]);
               }
            }

         } else if (lengthChangedListReference.get() != null) {
            IntArrayList lengthChangedList = lengthChangedListReference.get();
            // just copy to result, as it will not change. 
            int copyEnd = i + length;
            for (int copyIndex = i; copyIndex < copyEnd; copyIndex++) {
               lengthChangedList.add(existing[copyIndex]);
            }
         }
         i = i + length;
      }
      if (!updateConceptsRecordStartMap.isEmpty()) {
         if (lengthChangedListReference.get() == null) {
            lengthChangedListReference.set(new IntArrayList(existing));
         }
         // Find the update concepts
         updateConceptsRecordStartMap.forEachPair((conceptId, recordStart) -> {
            // copy in the entire record
            int copyEnd = recordStart + update[recordStart + 1] + 1;
            IntArrayList intList = lengthChangedListReference.get();
            for (int copyIndex = recordStart; copyIndex < copyEnd; copyIndex++) {
               intList.add(update[copyIndex]);
            }
            return true;
         });
      }
      IntArrayList lengthChangedList = lengthChangedListReference.get();
      if (lengthChangedList != null) {
         lengthChangedList.trimToSize();
         int[] result = lengthChangedList.elements();
         //TaxonomyRecord.validate(result);
         return result;
      }
      return existing;
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

         for (LogicNode necessaryOrSufficientSet : expression.getRoot().getChildren()) {
            for (LogicNode andOrOrLogicNode : necessaryOrSufficientSet.getChildren()) {
               for (LogicNode aLogicNode : andOrOrLogicNode.getChildren()) {
                  processRelationshipRoot(firstVersion.getReferencedComponentNid(),
                        aLogicNode,
                        parentTaxonomyRecord,
                        taxonomyFlags,
                        firstVersion.getStampSequence(),
                        expression);
               }
            }
         }
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
         final LogicalExpression referenceExpression = node.getData()
               .getLogicalExpression();
         final IsomorphicResultsBottomUp isomorphicResults = new IsomorphicResultsBottomUp(
               referenceExpression,
               comparisonExpression);

         for (LogicNode relationshipRoot : isomorphicResults.getAddedRelationshipRoots()) {
            final int stampSequence = node.getData()
                  .getStampSequence();

            processRelationshipRoot(conceptNid,
                  relationshipRoot,
                  taxonomyRecordForConcept,
                  taxonomyFlags,
                  stampSequence,
                  comparisonExpression);
         }

         for (LogicNode relationshipRoot : isomorphicResults.getDeletedRelationshipRoots()) {
            final int activeStampSequence = node.getData()
                  .getStampSequence();
            final int stampSequence = Get.stampService()
                  .getRetiredStampSequence(activeStampSequence);

            processRelationshipRoot(conceptNid,
                  relationshipRoot,
                  taxonomyRecordForConcept,
                  taxonomyFlags,
                  stampSequence,
                  comparisonExpression);
         }
      }

      for (Node<? extends LogicGraphVersion> childNode : node.getChildren()) {
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

      int conceptAssemblageNid = IDENTIFIER_SERVICE.getAssemblageNid(originNid).getAsInt();
      SpinedIntIntArrayMap map = TAXONOMY_SERVICE.getOrigin_DestinationTaxonomyRecord_Map(conceptAssemblageNid);
      int[] record = destinationTaxonomyRecord.pack();
      //TaxonomyRecord.validate(record);
      map.accumulateAndGet(
            destinationNid,
            record,
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
                           // TODO [KEC] Dan put this here to stop a pile of errors....
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
            // TODO [KEC] dan put this here to stop a pile of errors. It was returning AndNode.  Not sure what to do with it
         }
      }
   }

   @Override
   public void reset() {
      LOG.info("Clearing ChronologyUpdate cache");
      INFERRED_ASSEMBLAGE_NID = 0;
      CHILD_OF_NID = 0;
      ISA_NID = 0;
      ROLE_GROUP_NID = 0;
      IDENTIFIER_SERVICE = null;
      TAXONOMY_SERVICE = null;
   }
}
