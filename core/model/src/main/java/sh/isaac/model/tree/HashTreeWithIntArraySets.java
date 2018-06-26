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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;
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
import sh.isaac.model.collections.EclipseIntObjectMap;
import sh.isaac.model.collections.IntObjectMap;
import sh.isaac.model.collections.MergeIntArray;
import sh.isaac.model.collections.SpinedIntIntArrayMap;

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

   /**
    * The concept nids with parents.
    */
   final OpenIntHashSet conceptNidsWithParents;

   /**
    * The concept nids with children.
    */
   final OpenIntHashSet conceptNidsWithChildren;

   /**
    * The concept nids.
    */
   final OpenIntHashSet conceptNids;

   /**
    * map from a nid key to an array of parent nids.
    */
   protected final IntObjectMap<int[]> childNid_ParentNidSetArray_Map;

   /**
    * Map from a nid key to an array of child nids.
    */
   protected final IntObjectMap<int[]> parentNid_ChildNidSetArray_Map;
   protected final ManifoldCoordinate        manifoldCoordinate;
   protected final int                       assemblageNid;
   protected final OpenIntHashSet roots = new OpenIntHashSet();

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
      this.childNid_ParentNidSetArray_Map = ModelGet.dataStore().implementsSequenceStore() ? new SpinedIntIntArrayMap(): new EclipseIntObjectMap<int[]>();
      this.parentNid_ChildNidSetArray_Map = ModelGet.dataStore().implementsSequenceStore() ? new SpinedIntIntArrayMap() : new EclipseIntObjectMap<int[]>();
   }

   /**
    * Adds the child.
    *
    * @param parentId the parent id
    * @param childId the child id
    */
   private final void addChild(int parentId, int childId) {
      if (this.parentNid_ChildNidSetArray_Map.containsKey(parentId)) {
         this.parentNid_ChildNidSetArray_Map.put(
             parentId,
             addToArray(this.parentNid_ChildNidSetArray_Map.get(parentId), childId));
      } else {
         this.parentNid_ChildNidSetArray_Map.put(parentId, new int[] { childId });
      }

      if (this.childNid_ParentNidSetArray_Map.containsKey(childId)) {
         this.childNid_ParentNidSetArray_Map.put(
             childId,
             addToArray(this.childNid_ParentNidSetArray_Map.get(childId), parentId));
      } else {
         this.childNid_ParentNidSetArray_Map.put(childId, new int[] { parentId });
      }
   }

//   /**
//    * Adds the children.
//    *
//    * @param parentNid the parent id
//    * @param childNidArray the child id array
//    */
//   private final void addChildren(int parentNid, int[] childNidArray) {
//      this.conceptNids.add(parentNid);
//
//      if (childNidArray == null) {
//         return;
//      }
//
//      for (int childId: childNidArray) {
//         this.conceptNids.add(childId);
//      }
//
//      if (childNidArray.length > 0) {
//         if (!this.parentNid_ChildNidSetArray_Map.containsKey(parentNid)) {
//            this.parentNid_ChildNidSetArray_Map.put(parentNid, childNidArray);
//         } else {
//            final OpenIntHashSet combinedSet = new OpenIntHashSet();
//
//            Arrays.stream(this.parentNid_ChildNidSetArray_Map.get(parentNid))
//                  .forEach((nid) -> combinedSet.add(nid));
//            Arrays.stream(childNidArray)
//                  .forEach((nid) -> combinedSet.add(nid));
//            this.parentNid_ChildNidSetArray_Map.put(parentNid, combinedSet.keys()
//                  .elements());
//         }
//
//         for (int childNid: childNidArray) {
//            this.conceptNids.add(childNid);
//         }
//
//         this.conceptNidsWithChildren.add(parentNid);
//      }
//   }
//
//   /**
//    * Adds the parents.
//    *
//    * @param childNid the child nid
//    * @param parentNidArray the parent nid array
//    */
//   private final void addParents(int childNid, int[] parentNidArray) {
//      this.conceptNids.add(childNid);
//
//      if (parentNidArray.length > 0) {
//         if (!this.childNid_ParentNidSetArray_Map.containsKey(childNid)) {
//            this.childNid_ParentNidSetArray_Map.put(childNid, parentNidArray);
//         } else {
//            final OpenIntHashSet combinedSet = new OpenIntHashSet();
//
//            Arrays.stream(this.childNid_ParentNidSetArray_Map.get(childNid))
//                  .forEach((nid) -> combinedSet.add(nid));
//            Arrays.stream(parentNidArray)
//                  .forEach((nid) -> combinedSet.add(nid));
//            this.childNid_ParentNidSetArray_Map.put(childNid, combinedSet.keys()
//                  .elements());
//         }
//
//         this.childNid_ParentNidSetArray_Map.put(childNid, parentNidArray);
//
//         for (int parentNid: parentNidArray) {
//            this.conceptNids.add(parentNid);
//         }
//
//         this.conceptNidsWithParents.add(childNid);
//      }
//   }

   /**
    * {@inheritDoc}
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

//   /**
//    * Concept nids with children count.
//    *
//    * @return the int
//    */
//   private final int conceptNidsWithChildrenCount() {
//      return this.conceptNidsWithChildren.size();
//   }
//
//   /**
//    * Concept nids with parents count.
//    *
//    * @return the int
//    */
//   private final int conceptNidsWithParentsCount() {
//      return this.conceptNidsWithParents.size();
//   }

   /**
    * {@inheritDoc}
    */
   @Override
   public final Tree createAncestorTree(int childNid) {
      final HashTreeWithIntArraySets tree = new HashTreeWithIntArraySets(manifoldCoordinate, assemblageNid);

      addParentsAsChildren(tree, childNid, getParentNids(childNid));
      return tree;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public final TreeNodeVisitData depthFirstProcess(int startNid,
         ObjIntConsumer<TreeNodeVisitData> consumer,
         Supplier<TreeNodeVisitData> emptyDataSupplier) {
      final TreeNodeVisitData graphVisitData = emptyDataSupplier.get();

      dfsVisit(startNid, consumer, graphVisitData, 0);
      return graphVisitData;
   }

   /**
    * {@inheritDoc}
    */
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
    * {@inheritDoc}
    */
   @Override
   public final int size() {
      return getNodeIds().size() + 1;
   }

   /**
    * Adds the to array.
    *
    * @param array the array
    * @param toAdd the to add
    * @return the int[]
    */
   private final static int[] addToArray(int[] array, int toAdd) {
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
    * @param nid the nid
    * @param consumer the consumer
    * @param nodeVisitData the node visit data
    * @param depth the depth
    */
   private final void dfsVisit(int nid,
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
            OptionalInt previousPred = nodeVisitData.getPredecessorNid(nid); 
            if (previousPred.isPresent()) {
                userNodeSet.add(previousPred.getAsInt());
            }

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
    * @param childNid the child identifier
    * @param parentNids the parent identifiers
    */
   private void addParentsAsChildren(HashTreeWithIntArraySets tree, int childNid, int[] parentNids) {
      IntStream.of(parentNids)
               .forEach(
                   (parentNid) -> {
                      tree.addChild(childNid, parentNid);
                      addParentsAsChildren(tree, parentNid, getParentNids(parentNid));
                   });
   }

   /**
    * @param childNid
    * @param encounterOrder
    * @return
    */
   private int[] findCycle(int childNid, int[] encounterOrder) {
      int[] parentNids = getParentNidsNoFilter(childNid);

      for (int nidToTest: parentNids) {
         int[] encounterWithNewNid = Arrays.copyOf(encounterOrder, encounterOrder.length + 1);

         encounterWithNewNid[encounterOrder.length] = nidToTest;

         if (Arrays.stream(encounterOrder)
                   .anyMatch((pastNid) -> pastNid == nidToTest)) {
            return encounterWithNewNid;
         }
      }

      for (int nidToTest: parentNids) {
         int[] encounterWithNewNid = Arrays.copyOf(encounterOrder, encounterOrder.length + 1);

         encounterWithNewNid[encounterOrder.length] = nidToTest;

         int[] cycleArray = findCycle(nidToTest, encounterWithNewNid);

         if (cycleArray.length > 0) {
            return cycleArray;
         }
      }

      return new int[0];
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public final int getAssemblageNid() {
      return assemblageNid;
   }

   /**
    * Gets the children nids.
    *
    * @param parentNid the parentIndex nid
    * @return the children nids
    */
   private final int[] getChildNoFilter(int parentNid) {
      int[] returnValue = this.parentNid_ChildNidSetArray_Map.get(parentNid);
      if (returnValue != null) {
         return returnValue;
      }
      return new int[0];
   }
   
   /**
    * {@inheritDoc}
    */
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

   /**
    * {@inheritDoc}
    */
   @Override
   public final boolean isChildOf(int childConceptNid, int parentConceptNid) {
      int[] parentConceptNids = getParentNids(childConceptNid);

      return Arrays.binarySearch(parentConceptNids, parentConceptNid) >= 0;
   }

   /**
    * {@inheritDoc}
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

   /**
    * {@inheritDoc}
    */
   @Override
   public final boolean isDescendentOf(int childNid, int parentNid) {
      int[] parentNids = getParentNidsNoFilter(childNid);

      if (Arrays.binarySearch(parentNids, parentNid) >= 0) {
         return true;
      }
      NidSet recursionFix = new NidSet();
      for (int nidToTest: parentNids) {
         if (isDescendentOf(nidToTest, parentNid, recursionFix)) {
            return true;
         }
      }

      return false;
   }
   
   /**
    * @param childNid
    * @param parentNid
    * @param visitedNids
    * @return
    */
   private boolean isDescendentOf(int childNid, int parentNid, NidSet visitedNids) {
      if (visitedNids.contains(childNid)) {
         return false;
      }
      else {
        visitedNids.add(childNid);
      }

      int[] parentNids = getParentNidsNoFilter(childNid);

      if (Arrays.binarySearch(parentNids, parentNid) >= 0) {
         return true;
      }
      
      for (int nidToTest: parentNids) {
         if (isDescendentOf(nidToTest, parentNid, visitedNids)) {
            return true;
         }
      }

      return false;
   }

   /**
    * @param childNid
    * @param parentNidToFind
    * @param nodeVisitData
    * @return
    */
   private boolean isDescendentOf(int childNid, int parentNidToFind, TreeNodeVisitData nodeVisitData) {
      return isDescendentOfWithDepth(childNid, parentNidToFind, 0, childNid, nodeVisitData);
   }

   /**
    * @param childNid
    * @param parentNidToFind
    * @param depth
    * @param originalChildNid
    * @param nodeVisitData
    * @return
    */
   private boolean isDescendentOfWithDepth(int childNid,
         int parentNidToFind,
         int depth,
         int originalChildNid,
         TreeNodeVisitData nodeVisitData) {
      int[] parentNids = getParentNidsNoFilter(childNid);

      if (Arrays.binarySearch(parentNids, parentNidToFind) >= 0) {
         return true;
      }

      for (int nidToTest: parentNids) {
         if (nodeVisitData.nidInCycle(nidToTest)) {
           return false;
         }
         else if (depth < 100) {
            if (nidToTest != childNid) {
               if (isDescendentOfWithDepth(
                     nidToTest,
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
               int lastNidInCycle = cycleArray[cycleArray.length - 1];
               int cycleStart          = 0;

               while (cycleArray[cycleStart] != lastNidInCycle) {
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
                         .append(nidToTest)
                         .append(" ")
                         .append(Get.conceptDescriptionText(nidToTest));
               } else {
                  builder.append("Cycle found: \n");

                  if (cycleArray.length == 1) {
                     builder.append("\n   SELF REFERENCE");
                  }

                  for (int nid: cycleArray) {
                     builder.append("\n   ")
                            .append(nid)
                            .append(" ")
                            .append(Get.conceptDescriptionText(nid));
                  }
               }

               builder.append("\n");
               LOG.info(builder.toString());
            }
         }
      }

      return false;
   }

   /**
    * Gets the descendents recursive.
    *
    * @param parentNid the parentIndex nid
    * @param descendentNids the descendent nids
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
    * Gets the node identifiers.
    *
    * @return the node identifiers
    */
   private final OpenIntHashSet getNodeIds() {
      return this.conceptNids;
   }

   /**
    * Gets the parentIndex nids.
    *
    * @param childNid the childIndex nid
    * @return the parentIndex nids
    */
   private final int[] getParentNidsNoFilter(int childNid) {
      if (this.childNid_ParentNidSetArray_Map.containsKey(childNid)) {
         return this.childNid_ParentNidSetArray_Map.get(childNid);
      }
      return new int[0];
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public final int[] getParentNids(int childNid) {
      return getParentNidsNoFilter(childNid);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public final int[] getRootNids() {
      OpenIntHashSet rootSet = (OpenIntHashSet) this.conceptNidsWithChildren.clone();

      this.conceptNidsWithParents.forEachKey(
          (nid) -> {
             rootSet.remove(nid);
             return true;
          });

      return rootSet.keys().elements();
   }

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
         for (int nid : roots.keys().elements()) {
            count++;
            if (count > 4) {
               break;
            }
            builder.append(nid).append(": ").append(Get.conceptDescriptionText(nid)).append("\n");
            printWatch(nid, "root: ");
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

   protected void printWatch(final int conceptNid, String prefix) {
      if (!Get.configurationService().isVerboseDebugEnabled()) {
         return;
      }

      int[] record   = ModelGet.taxonomyDebugService().getTaxonomyData(ModelGet.identifierService().getAssemblageNid(conceptNid).getAsInt(), conceptNid);

      LOG.debug("\n" + prefix + " watch: " + Get.conceptDescriptionText(conceptNid));
      LOG.debug("\nTaxonomy record: " + Arrays.toString(record));
      LOG.debug("\n" + ModelGet.taxonomyDebugService().describeTaxonomyRecord(conceptNid));
      childNid_ParentNidSetArray_Map.forEach(
          (int inNid,
           int[] parentArray) -> {
             if (Arrays.stream(parentArray)
                       .anyMatch((value) -> value == conceptNid)) {

                System.out.println(
                    prefix + Get.conceptDescriptionText(
                          conceptNid) + " found in parent set of: " + inNid + " " + Get.conceptDescriptionText(
                            inNid));
             }
          });
      parentNid_ChildNidSetArray_Map.forEach(
          (int inNid,
           int[] childArray) -> {
             if (Arrays.stream(childArray)
                       .anyMatch((value) -> value == conceptNid)) {

                System.out.println(
                    prefix + Get.conceptDescriptionText(
                          conceptNid) + " found in child set of: " + inNid + " " + Get.conceptDescriptionText(
                            inNid));
             }
          });
      LOG.debug(Get.concept(conceptNid)
                            .toString());
      LOG.debug("");
   }
}

