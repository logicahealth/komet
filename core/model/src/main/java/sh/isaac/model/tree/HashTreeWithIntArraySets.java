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



package sh.isaac.model.tree;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.alert.Alert;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.tree.NodeStatus;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.MergeIntArray;
import sh.isaac.model.collections.SpinedIntIntArrayMap;

//~--- classes ----------------------------------------------------------------

/**
 * A {@code Tree} implemented using a {@code OpenIntObjectHashMap<int[]>}.
 *
 * @author kec
 */
public class HashTreeWithIntArraySets
         implements Tree {
   private static final Logger LOG = LogManager.getLogger();
   protected static final String MULTI_PARENT_SETS = "MultiParentSets";

   /**
    * The Constant EMPTY_INT_ARRAY.
    */
   static final int[] EMPTY_INT_ARRAY = new int[0];

   //~--- fields --------------------------------------------------------------

   /**
    * The concept sequences with parents.
    */
   final OpenIntHashSet conceptNidsWithParents;

   /**
    * The concept sequences with children.
    */
   final OpenIntHashSet conceptNidsWithChildren;

   /**
    * The concept sequences.
    */
   final OpenIntHashSet conceptNids;

   /**
    * map from a nid key to an array of parent nids.
    */
   protected final SpinedIntIntArrayMap childNid_ParentNidSetArray_Map;

   /**
    * Map from a nid key to an array of child nids.
    */
   protected final SpinedIntIntArrayMap parentNid_ChildNidSetArray_Map;
   protected final ManifoldCoordinate        manifoldCoordinate;
   protected final int                       assemblageNid;
   protected final OpenIntHashSet roots = new OpenIntHashSet();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new hash tree with bit sets.
    *
    * @param manifoldCoordinate
    * @param assemblageNid the assemblage nid which specifies the assemblage where the concepts in this tree
    * where created within.
    */
   public HashTreeWithIntArraySets(ManifoldCoordinate manifoldCoordinate, int assemblageNid) {
      this.manifoldCoordinate             = manifoldCoordinate;
      this.assemblageNid                  = assemblageNid;
      this.conceptNidsWithParents    = new OpenIntHashSet();
      this.conceptNidsWithChildren   = new OpenIntHashSet();
      this.conceptNids               = new OpenIntHashSet();
      this.childNid_ParentNidSetArray_Map = new SpinedIntIntArrayMap();
      this.parentNid_ChildNidSetArray_Map = new SpinedIntIntArrayMap();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the child.
    *
    * @param parentSequence the parent sequence
    * @param childSequence the child sequence
    */
   public final void addChild(int parentSequence, int childSequence) {
      if (this.parentNid_ChildNidSetArray_Map.containsKey(parentSequence)) {
         this.parentNid_ChildNidSetArray_Map.put(
             parentSequence,
             addToArray(this.parentNid_ChildNidSetArray_Map.get(parentSequence), childSequence));
      } else {
         this.parentNid_ChildNidSetArray_Map.put(parentSequence, new int[] { childSequence });
      }

      if (this.childNid_ParentNidSetArray_Map.containsKey(childSequence)) {
         this.childNid_ParentNidSetArray_Map.put(
             childSequence,
             addToArray(this.childNid_ParentNidSetArray_Map.get(childSequence), parentSequence));
      } else {
         this.childNid_ParentNidSetArray_Map.put(childSequence, new int[] { parentSequence });
      }
   }

   /**
    * Adds the children.
    *
    * @param parentSequence the parent sequence
    * @param childSequenceArray the child sequence array
    */
   public final void addChildren(int parentSequence, int[] childSequenceArray) {
      this.conceptNids.add(parentSequence);

      if (childSequenceArray == null) {
         return;
      }

      for (int childSequence: childSequenceArray) {
         this.conceptNids.add(childSequence);
      }

      if (childSequenceArray.length > 0) {
         if (!this.parentNid_ChildNidSetArray_Map.containsKey(parentSequence)) {
            this.parentNid_ChildNidSetArray_Map.put(parentSequence, childSequenceArray);
         } else {
            final OpenIntHashSet combinedSet = new OpenIntHashSet();

            Arrays.stream(this.parentNid_ChildNidSetArray_Map.get(parentSequence))
                  .forEach((sequence) -> combinedSet.add(sequence));
            Arrays.stream(childSequenceArray)
                  .forEach((sequence) -> combinedSet.add(sequence));
            this.parentNid_ChildNidSetArray_Map.put(parentSequence, combinedSet.keys()
                  .elements());
         }

         for (int childSequence: childSequenceArray) {
            this.conceptNids.add(childSequence);
         }

         this.conceptNidsWithChildren.add(parentSequence);
      }
   }

   /**
    * Adds the parents.
    *
    * @param childSequence the child sequence
    * @param parentSequenceArray the parent sequence array
    */
   public final void addParents(int childSequence, int[] parentSequenceArray) {
      this.conceptNids.add(childSequence);

      if (parentSequenceArray.length > 0) {
         if (!this.childNid_ParentNidSetArray_Map.containsKey(childSequence)) {
            this.childNid_ParentNidSetArray_Map.put(childSequence, parentSequenceArray);
         } else {
            final OpenIntHashSet combinedSet = new OpenIntHashSet();

            Arrays.stream(this.childNid_ParentNidSetArray_Map.get(childSequence))
                  .forEach((sequence) -> combinedSet.add(sequence));
            Arrays.stream(parentSequenceArray)
                  .forEach((sequence) -> combinedSet.add(sequence));
            this.childNid_ParentNidSetArray_Map.put(childSequence, combinedSet.keys()
                  .elements());
         }

         this.childNid_ParentNidSetArray_Map.put(childSequence, parentSequenceArray);

         for (int parentSequence: parentSequenceArray) {
            this.conceptNids.add(parentSequence);
         }

         this.conceptNidsWithParents.add(childSequence);
      }
   }

   /**
    * Breadth first process.
    *
    * @param startNid the start nid
    * @param consumer the consumer
    * @return the tree node visit data
    */
   @Override
   public final TreeNodeVisitData breadthFirstProcess(int startNid,
         ObjIntConsumer<TreeNodeVisitData> consumer,
         Supplier<TreeNodeVisitData> emptyDataSupplier) {
      final TreeNodeVisitData nodeVisitData = emptyDataSupplier.get();
      final Queue<Integer>    bfsQueue      = new LinkedList<>();

      nodeVisitData.startNodeVisit(startNid, 0);
      bfsQueue.add(startNid);

      while (!bfsQueue.isEmpty()) {
         final int   currentNid      = bfsQueue.remove();
         final int   currentDistance = nodeVisitData.getDistance(currentNid);
         final int[] childNids       = getChildNoFilter(currentNid);

         if (childNids.length == 0) {
            nodeVisitData.setLeafNode(currentNid);
         }

         consumer.accept(nodeVisitData, currentNid);

         for (final int childNid: childNids) {
            consumer.accept(nodeVisitData, childNid);

            if (nodeVisitData.getNodeStatus(childNid) == NodeStatus.UNDISCOVERED) {
               nodeVisitData.startNodeVisit(childNid, currentDistance + 1);
               nodeVisitData.setPredecessorNid(childNid, currentNid);
               bfsQueue.add(childNid);
            }
         }

         nodeVisitData.endNodeVisit(currentNid);
      }

      return nodeVisitData;
   }

   /**
    * Concept sequences with children count.
    *
    * @return the int
    */
   public final int conceptSequencesWithChildrenCount() {
      return this.conceptNidsWithChildren.size();
   }

   /**
    * Concept sequences with parents count.
    *
    * @return the int
    */
   public final int conceptSequencesWithParentsCount() {
      return this.conceptNidsWithParents.size();
   }

   /**
    * Creates the ancestor tree.
    *
    * @param childNid the childIndex sequence
    * @return the tree
    */
   @Override
   public final Tree createAncestorTree(int childNid) {
      final HashTreeWithIntArraySets tree = new HashTreeWithIntArraySets(manifoldCoordinate, assemblageNid);

      addParentsAsChildren(tree, childNid, getParentNids(childNid));
      return tree;
   }

   /**
    * Depth first process.
    *
    * @param startNid the start nid (or root of the tree)
    * @param consumer the consumer
    * @return the tree node visit data
    */
   @Override
   public final TreeNodeVisitData depthFirstProcess(int startNid,
         ObjIntConsumer<TreeNodeVisitData> consumer,
         Supplier<TreeNodeVisitData> emptyDataSupplier) {
      final TreeNodeVisitData graphVisitData = emptyDataSupplier.get();

      dfsVisit(startNid, consumer, graphVisitData, 0);
      return graphVisitData;
   }

   @Override
   public final void removeParent(int childNid, int parentNid) {
      int[] parents       = childNid_ParentNidSetArray_Map.get(childNid);
      int[] newParents    = new int[parents.length - 1];
      int   j             = 0;

      for (int i = 0; i < parents.length; i++) {
         if (parents[i] != parentNid) {
            newParents[j++] = parents[i];
         }
      }

      childNid_ParentNidSetArray_Map.put(childNid, newParents);
   }

   /**
    * Size.
    *
    * @return the int
    */
   @Override
   public final int size() {
      return getNodeSequences().size() + 1;
   }

   /**
    * Adds the to array.
    *
    * @param array the array
    * @param toAdd the to add
    * @return the int[]
    */
   protected final static int[] addToArray(int[] array, int toAdd) {
      int searchResult = Arrays.binarySearch(array, toAdd);

      if (searchResult >= 0) {
         return array;
      }

      int[] array2      = new int[array.length + 1];
      int   insertIndex = -searchResult - 1;

      System.arraycopy(array, 0, array2, 0, insertIndex);
      System.arraycopy(array, insertIndex, array2, insertIndex + 1, array.length - insertIndex);
      array2[insertIndex] = toAdd;
      return array2;
   }

   /**
    * Dfs visit.
    *
    * @param nid the sequence
    * @param consumer the consumer
    * @param nodeVisitData the node visit data
    * @param depth the depth
    */
   protected final void dfsVisit(int nid,
                           ObjIntConsumer<TreeNodeVisitData> consumer,
                           TreeNodeVisitData nodeVisitData,
                           int depth) {
      // Change to NodeStatus.PROCESSING
      nodeVisitData.startNodeVisit(nid, depth);

      final int[] childNids = getChildNoFilter(nid);

      if (childNids.length == 0) {
         nodeVisitData.setLeafNode(nid);
      }

      consumer.accept(nodeVisitData, nid);

      for (final int childNid: childNids) {
         if (nodeVisitData.getNodeStatus(childNid) == NodeStatus.UNDISCOVERED) {
            nodeVisitData.setPredecessorNid(childNid, nid);
            dfsVisit(childNid, consumer, nodeVisitData, depth + 1);
         } else {
            // second path to node. Could be multi-parent or cycle...
            OpenIntHashSet userNodeSet = nodeVisitData.getUserNodeSet(MULTI_PARENT_SETS, nid);

            // add previous predecessor to node.
            userNodeSet.add(nodeVisitData.getPredecessorNid(nid));

            // add to extra parent set of the child...
            boolean addParent = true;

            for (int otherParentNid: userNodeSet.keys()
                  .elements()) {
               if (isDescendentOf(otherParentNid, nid, nodeVisitData)) {
                  addParent = false;
               }

               if (isDescendentOf(nid, otherParentNid, nodeVisitData)) {
                  userNodeSet.remove(otherParentNid);
               }
            }

            if (addParent) {
               userNodeSet.add(nid);
            }
         }
      }

      // Change to NodeStatus.FINISHED
      nodeVisitData.endNodeVisit(nid);
   }

   /**
    * Adds the parents as children.
    *
    * @param tree the tree
    * @param childSequence the childIndex sequence
    * @param parentSequences the parentIndex sequences
    */
   private void addParentsAsChildren(HashTreeWithIntArraySets tree, int childSequence, int[] parentSequences) {
      IntStream.of(parentSequences)
               .forEach(
                   (parentSequence) -> {
                      tree.addChild(childSequence, parentSequence);
                      addParentsAsChildren(tree, parentSequence, getParentNids(parentSequence));
                   });
   }

   private int[] findCycle(int childSequence, int[] encounterOrder) {
      int[] parentSequences = getParentNidsNoFilter(childSequence);

      for (int sequenceToTest: parentSequences) {
         int[] encounterWithNewSequence = Arrays.copyOf(encounterOrder, encounterOrder.length + 1);

         encounterWithNewSequence[encounterOrder.length] = sequenceToTest;

         if (Arrays.stream(encounterOrder)
                   .anyMatch((pastSequence) -> pastSequence == sequenceToTest)) {
            return encounterWithNewSequence;
         }
      }

      for (int sequenceToTest: parentSequences) {
         int[] encounterWithNewSequence = Arrays.copyOf(encounterOrder, encounterOrder.length + 1);

         encounterWithNewSequence[encounterOrder.length] = sequenceToTest;

         int[] cycleArray = findCycle(sequenceToTest, encounterWithNewSequence);

         if (cycleArray.length > 0) {
            return cycleArray;
         }
      }

      return new int[0];
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public final int getAssemblageNid() {
      return assemblageNid;
   }

   /**
    * Gets the children sequences.
    *
    * @param parentNid the parentIndex sequence
    * @return the children sequences
    */
   public final int[] getChildNoFilter(int parentNid) {
      int[] returnValue = this.parentNid_ChildNidSetArray_Map.get(parentNid);
      if (returnValue != null) {
         return returnValue;
      }
      return new int[0];
   }
   @Override
   public final int[] getChildNids(int parentNid) {
      int[] returnValue = this.parentNid_ChildNidSetArray_Map.get(parentNid);
      if (returnValue != null) {
          OpenIntHashSet childrenSet = new OpenIntHashSet();
          for (int childNid: returnValue) {
              if (Arrays.binarySearch(getParentNids(childNid), parentNid) >= 0) {
                  childrenSet.add(childNid);
              }
          }
          IntArrayList childrenList = childrenSet.keys();
          childrenList.sort();
         return childrenList.elements();
      }
      return new int[0];
   }

   @Override
   public final boolean isChildOf(int childConceptNid, int parentConceptNid) {
      int[] parentConceptNids = getParentNids(childConceptNid);

      return Arrays.binarySearch(parentConceptNids, parentConceptNid) >= 0;
   }

   /**
    * Gets the descendent sequence set.
    *
    * @param parentNid the parentNid
    * @return the descendent sequence set
    */
   @Override
   public final NidSet getDescendentNidSet(int parentNid) {
      final NidSet descendentNids = new NidSet();
      if (this.parentNid_ChildNidSetArray_Map.containsKey(parentNid)) {
         getDescendentsRecursive(parentNid, descendentNids);
         return descendentNids;
      }

      return descendentNids;
   }

   @Override
   public final boolean isDescendentOf(int childNid, int parentNid) {
      int[] parentNids = getParentNidsNoFilter(childNid);

      if (Arrays.binarySearch(parentNids, parentNid) >= 0) {
         return true;
      }

      for (int nidToTest: parentNids) {
         if (isDescendentOf(nidToTest, parentNid)) {
            return true;
         }
      }

      return false;
   }

   private boolean isDescendentOf(int childNid, int parentNidToFind, TreeNodeVisitData nodeVisitData) {
      return isDescendentOfWithDepth(childNid, parentNidToFind, 0, childNid, nodeVisitData);
   }

   private boolean isDescendentOfWithDepth(int childNid,
         int parentNidToFind,
         int depth,
         int originalChildNid,
         TreeNodeVisitData nodeVisitData) {
      int[] parentSequences = getParentNidsNoFilter(childNid);

      if (Arrays.binarySearch(parentSequences, parentNidToFind) >= 0) {
         return true;
      }

      for (int sequenceToTest: parentSequences) {
         if (depth < 100) {
            if (sequenceToTest != childNid) {
               if (isDescendentOfWithDepth(
                     sequenceToTest,
                     parentNidToFind,
                     depth + 1,
                     originalChildNid,
                     nodeVisitData)) {
                  return true;
               }
            } else {
               LOG.warn("Self reference for: " + childNid + " " + Get.conceptDescriptionText(childNid));
            }
         } else {
            int[] cycleArray = findCycle(originalChildNid, new int[] { originalChildNid });

            if (cycleArray.length > 0) {
               int lastSequenceInCycle = cycleArray[cycleArray.length - 1];
               int cycleStart          = 0;

               while (cycleArray[cycleStart] != lastSequenceInCycle) {
                  cycleStart++;
               }

               cycleArray = Arrays.copyOfRange(cycleArray, cycleStart, cycleArray.length - 1);
            }

            if (cycleArray.length == 0) {
               cycleArray = findCycle(originalChildNid, new int[] { originalChildNid });
            }

            if (!nodeVisitData.getCycleSet()
                              .contains(cycleArray)) {
               nodeVisitData.getCycleSet()
                            .add(cycleArray);

               StringBuilder builder = new StringBuilder();

               if (cycleArray.length == 0) {
                  builder.append("Depth exceeded, but  cycle not found between: \n     ");
                  builder.append(originalChildNid)
                         .append(" ")
                         .append(Get.conceptDescriptionText(originalChildNid));
                  builder.append("\n     ")
                         .append(sequenceToTest)
                         .append(" ")
                         .append(Get.conceptDescriptionText(sequenceToTest));
               } else {
                  builder.append("Cycle found: \n");

                  if (cycleArray.length == 1) {
                     builder.append("\n   SELF REFERENCE");
                  }

                  for (int sequence: cycleArray) {
                     builder.append("\n   ")
                            .append(sequence)
                            .append(" ")
                            .append(Get.conceptDescriptionText(sequence));
                  }
               }

               builder.append("\n");
               LOG.debug(builder.toString());
            }
         }
      }

      return false;
   }

   /**
    * Gets the descendents recursive.
    *
    * @param parentNid the parentIndex sequence
    * @param descendentNids the descendent sequences
    * @return the descendents recursive
    */
   private void getDescendentsRecursive(int parentNid, NidSet descendentNids) {
      if (this.parentNid_ChildNidSetArray_Map.containsKey(parentNid)) {
         for (final int childNid: this.parentNid_ChildNidSetArray_Map.get(parentNid)) {
            descendentNids.add(childNid);
            getDescendentsRecursive(childNid, descendentNids);
         }
      }
   }

   /**
    * Gets the node sequences.
    *
    * @return the node sequences
    */
   public final OpenIntHashSet getNodeSequences() {
      return this.conceptNids;
   }

   /**
    * Gets the parentIndex sequences.
    *
    * @param childNid the childIndex sequence
    * @return the parentIndex sequences
    */
   public final int[] getParentNidsNoFilter(int childNid) {
      if (this.childNid_ParentNidSetArray_Map.containsKey(childNid)) {
         return this.childNid_ParentNidSetArray_Map.get(childNid);
      }
      return new int[0];
   }

   @Override
   public final int[] getParentNids(int childNid) {
      if (this.childNid_ParentNidSetArray_Map.containsKey(childNid)) {
         int[] parents = this.childNid_ParentNidSetArray_Map.get(childNid);
         if (parents.length > 1) {
             OpenIntHashSet redundantParents = new OpenIntHashSet();
             for (int i = 0; i < parents.length -1; i++) {
                 for (int j = 1; j < parents.length; j++) {
                     if (isDescendentOf(parents[i], parents[j])) {
                         // redundant parent j
                         redundantParents.add(parents[j]);
                     } else if (isDescendentOf(parents[j], parents[i])) {
                         // redundant parent i
                         redundantParents.add(parents[i]);
                     }
                 }
             }
             OpenIntHashSet closestParentSet = new OpenIntHashSet();
             for (int parent: parents) {
                 if (!redundantParents.contains(parent)) {
                     closestParentSet.add(parent);
                 }
             }
             IntArrayList closestParentList = closestParentSet.keys();
             closestParentList.sort();
             return closestParentList.elements();
         }
         return parents;
      }

      return new int[0];
   }

   /**
    * Gets the root sequences.
    *
    * @return the root sequences
    */
   @Override
   public final int[] getRootNids() {
      OpenIntHashSet rootSet = (OpenIntHashSet) this.conceptNidsWithChildren.clone();

      this.conceptNidsWithParents.forEachKey(
          (sequence) -> {
             rootSet.remove(sequence);
             return true;
          });

      int[] rootSequences = rootSet.keys()
                                   .elements();

      for (int i = 0; i < rootSet.size(); i++) {
         rootSequences[i] = ModelGet.identifierService()
                                    .getNidForElementSequence(rootSequences[i], assemblageNid);
      }

      return rootSequences;
   }

   //~--- methods -------------------------------------------------------------
   /**
    * Adds the.
    *
    * @param parent the parent
    * @param child the child
    */
   public void add(int parent, int child) {
      if (Get.configurationService().isVerboseDebugEnabled() && (parent == TermAux.SOLOR_ROOT.getNid())) {
         LOG.debug("SOLOR root nid added to tree: " + TermAux.SOLOR_ROOT.getNid());
      }
      conceptNids.add(parent);
      conceptNids.add(child);
      conceptNidsWithParents.add(child);
      conceptNidsWithChildren.add(parent);
      this.childNid_ParentNidSetArray_Map.accumulateAndGet(child, new int[]{parent}, MergeIntArray::merge);
      this.parentNid_ChildNidSetArray_Map.accumulateAndGet(parent, new int[]{child}, MergeIntArray::merge);
   }


   protected void computeRoots() {
      roots.clear();
      conceptNids.forEachKey((conceptNid) -> {
         if (!conceptNidsWithParents.contains(conceptNid)) {
            roots.add(conceptNid);
         }
         return true;
      });
      if (roots.size() != 1) {
         StringBuilder builder1 = new StringBuilder();
         builder1.append("Root count != 1: ");
         builder1.append(roots.size());
         LOG.warn(builder1.toString());
         final StringBuilder builder = new StringBuilder("Roots: \n");
         int count = 0;
         for (int sequence : roots.keys().elements()) {
            count++;
            if (count > 4) {
               break;
            }
            builder.append(sequence).append(": ").append(Get.conceptDescriptionText(sequence)).append("\n");
            printWatch(sequence, "root: ");
         }
         String title = roots.size() + " " +
                 manifoldCoordinate.getTaxonomyPremiseType().toString() + 
                 " roots";
         if (roots.isEmpty()) {
            title = "No taxonomy roots";
         }
         AlertObject alert = new AlertObject(title, builder.toString(), AlertType.ERROR, AlertCategory.TAXONOMY);
         Alert.publishAddition(alert);
      }
   }

   protected void printWatch(int conceptNid, String prefix) {
      if (!Get.configurationService().isVerboseDebugEnabled()) {
         return;
      }
      int nid = conceptNid;

      if (nid >= 0) {
         nid = ModelGet.identifierService()
                       .getNidForElementSequence(conceptNid, assemblageNid);
      }

      SpinedIntIntArrayMap taxonomyMap = ModelGet.taxonomyDebugService()
                                                 .getTaxonomyRecordMap(
                                                       ModelGet.identifierService()
                                                             .getAssemblageNid(nid)
                                                             .getAsInt());
      int[] record   = taxonomyMap.get(nid);
      int   finalNid = nid;

      LOG.debug("\n" + prefix + " watch: " + Get.conceptDescriptionText(nid));
      LOG.debug("\nTaxonomy record: " + Arrays.toString(record));
      LOG.debug("\n" + ModelGet.taxonomyDebugService().describeTaxonomyRecord(nid));
      childNid_ParentNidSetArray_Map.forEach(
          (int sequence,
           int[] parentArray) -> {
             if (Arrays.stream(parentArray)
                       .anyMatch((value) -> value == finalNid)) {
                int sequenceNid = sequence;

                if (sequenceNid >= 0) {
                   sequenceNid = ModelGet.identifierService()
                                         .getNidForElementSequence(sequence, assemblageNid);
                }

                System.out.println(
                    prefix + Get.conceptDescriptionText(
                        finalNid) + " found in parent set of: " + sequence + " " + Get.conceptDescriptionText(
                            sequenceNid));
             }
          });
      parentNid_ChildNidSetArray_Map.forEach(
          (int sequence,
           int[] childArray) -> {
             if (Arrays.stream(childArray)
                       .anyMatch((value) -> value == finalNid)) {
                int sequenceNid = sequence;

                if (sequenceNid >= 0) {
                   sequenceNid = ModelGet.identifierService()
                                         .getNidForElementSequence(sequence, assemblageNid);
                }

                System.out.println(
                    prefix + Get.conceptDescriptionText(
                        finalNid) + " found in child set of: " + sequence + " " + Get.conceptDescriptionText(
                            sequenceNid));
             }
          });
      LOG.debug(Get.concept(nid)
                            .toString());
      LOG.debug("");
   }
}

