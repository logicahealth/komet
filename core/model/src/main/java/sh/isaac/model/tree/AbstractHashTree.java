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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.tree.NodeStatus;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.SpinedIntObjectMap;

//~--- classes ----------------------------------------------------------------

/**
 * The Class AbstractHashTree.
 *
 * @author kec
 */
public abstract class AbstractHashTree
         implements Tree {
   protected static final String MULTI_PARENT_SETS = "MultiParentSets";

   /**
    * The Constant EMPTY_INT_ARRAY.
    */
   static final int[] EMPTY_INT_ARRAY = new int[0];

   //~--- fields --------------------------------------------------------------

   /**
    * map from a childIndex sequence key to an array of parentIndex sequences.
    */
   protected final SpinedIntObjectMap<int[]> childSequence_ParentNidArray_Map;

   /**
    * Map from a parentIndex sequence key to an array of childIndex sequences.
    */
   protected final SpinedIntObjectMap<int[]> parentSequence_ChildNidArray_Map;
   protected final ManifoldCoordinate          manifoldCoordinate;
   protected final int                         assemblageNid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new abstract hash tree.
    *
    * @param manifoldCoordinate
    * @param assemblageNid the assemblage nid which specifies the assemblage where the concepts in this tree
    * where created within.
    */
   public AbstractHashTree(ManifoldCoordinate manifoldCoordinate, int assemblageNid) {
      this.manifoldCoordinate                    = manifoldCoordinate;
      this.assemblageNid                         = assemblageNid;
      this.childSequence_ParentNidArray_Map = new SpinedIntObjectMap<>();
      this.parentSequence_ChildNidArray_Map = new SpinedIntObjectMap<>();
   }

   /**
    * Instantiates a new abstract hash tree.
    *
    * @param manifoldCoordinate
    * @param assemblageNid the assemblage nid which specifies the assemblage where the concepts in this tree
    * where created within.
    * @param initialSize the initial size
    */
   public AbstractHashTree(ManifoldCoordinate manifoldCoordinate, int assemblageNid, int initialSize) {
      this.manifoldCoordinate                    = manifoldCoordinate;
      this.assemblageNid                         = assemblageNid;
      this.childSequence_ParentNidArray_Map = new SpinedIntObjectMap<>();
      this.parentSequence_ChildNidArray_Map = new SpinedIntObjectMap<>();
   }

   //~--- methods -------------------------------------------------------------

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
         final int   currentNid = bfsQueue.remove();
         final int   currentDistance = nodeVisitData.getDistance(currentNid);
         final int[] childNids  = getChildNids(currentNid);

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
    * Creates the ancestor tree.
    *
    * @param childNid the childIndex sequence
    * @return the tree
    */
   @Override
   public final Tree createAncestorTree(int childNid) {
      final SimpleHashTree tree = new SimpleHashTree(manifoldCoordinate, assemblageNid);

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
   public void removeParent(int childNid, int parentNid) {
      int childSequence = ModelGet.identifierService().getElementSequenceForNid(childNid, assemblageNid);
      int[] parents    = childSequence_ParentNidArray_Map.get(childSequence);
      int[] newParents = new int[parents.length - 1];
      int   j          = 0;

      for (int i = 0; i < parents.length; i++) {
         if (parents[i] != parentNid) {
            newParents[j++] = parents[i];
         }
      }

      childSequence_ParentNidArray_Map.put(childSequence, newParents);
   }

   /**
    * Dfs visit.
    *
    * @param nid the sequence
    * @param consumer the consumer
    * @param nodeVisitData the node visit data
    * @param depth the depth
    */
   protected void dfsVisit(int nid,
                           ObjIntConsumer<TreeNodeVisitData> consumer,
                           TreeNodeVisitData nodeVisitData,
                           int depth) {
      // Change to NodeStatus.PROCESSING
      nodeVisitData.startNodeVisit(nid, depth);

      final int[] childNids = getChildNids(nid);

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
   private void addParentsAsChildren(SimpleHashTree tree, int childSequence, int[] parentSequences) {
      IntStream.of(parentSequences)
               .forEach(
                   (parentSequence) -> {
                      tree.addChild(childSequence, parentSequence);
                      addParentsAsChildren(tree, parentSequence, getParentNids(parentSequence));
                   });
   }

   private int[] findCycle(int childSequence, int[] encounterOrder) {
      int[] parentSequences = getParentNids(childSequence);

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
   public int getAssemblageNid() {
      return assemblageNid;
   }

   @Override
   public boolean isChildOf(int childSequence, int parentSequence) {
      int[] parentSequences = getParentNids(childSequence);

      if (Arrays.binarySearch(parentSequences, parentSequence) >= 0) {
         return true;
      }

      return false;
   }

   /**
    * Gets the children sequence stream.
    *
    * @param parentSequence the parentIndex sequence
    * @return the children sequence stream
    */
   @Override
   public IntStream getChildNidStream(int parentSequence) {
      return IntStream.of(getChildNids(parentSequence));
   }

   /**
    * Gets the children sequences.
    *
    * @param parentNid the parentIndex sequence
    * @return the children sequences
    */
   @Override
   public final int[] getChildNids(int parentNid) {
      int parentSequence = ModelGet.identifierService().getElementSequenceForNid(parentNid, assemblageNid);
      if (this.parentSequence_ChildNidArray_Map.containsKey(parentSequence)) {
         return this.parentSequence_ChildNidArray_Map.get(parentSequence);
      }

      return new int[0];
   }

   @Override
   public boolean isDescendentOf(int childNid, int parentNid) {
      int[] parentNids = getParentNids(childNid);

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
      int[] parentSequences = getParentNids(childNid);

      if (Arrays.binarySearch(parentSequences, parentNidToFind) >= 0) {
         return true;
      }

      for (int sequenceToTest: parentSequences) {
         if (depth < 100) {
            if (sequenceToTest != childNid) {
               if (isDescendentOfWithDepth(sequenceToTest,
                     parentNidToFind,
                     depth + 1,
                     originalChildNid,
                     nodeVisitData)) {
                  return true;
               }
            } else {
               System.out.println("Self reference for: " + childNid + " " + Get.conceptDescriptionText(childNid));
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
               System.out.println(builder.toString());
            }
         }
      }

      return false;
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
      int parentSequence = ModelGet.identifierService().getElementSequenceForNid(parentNid, assemblageNid);
      if (this.parentSequence_ChildNidArray_Map.containsKey(parentSequence)) {
         getDescendentsRecursive(parentNid, descendentNids);
         return descendentNids;
      }

      return descendentNids;
   }

   /**
    * Gets the descendents recursive.
    *
    * @param parentNid the parentIndex sequence
    * @param descendentNids the descendent sequences
    * @return the descendents recursive
    */
   private void getDescendentsRecursive(int parentNid, NidSet descendentNids) {
      int parentSequence = ModelGet.identifierService().getElementSequenceForNid(parentNid, assemblageNid);
      if (this.parentSequence_ChildNidArray_Map.containsKey(parentSequence)) {
         for (final int childNid: this.parentSequence_ChildNidArray_Map.get(parentSequence)) {
            descendentNids.add(childNid);
            getDescendentsRecursive(childNid, descendentNids);
         }
      }
   }

   /**
    * Gets the parentIndex sequence stream.
    *
    * @param childNid the childIndex sequence
    * @return the parentIndex sequence stream
    */
   @Override
   public IntStream getParentNidStream(int childNid) {
      return IntStream.of(getParentNids(childNid));
   }

   /**
    * Gets the parentIndex sequences.
    *
    * @param childNid the childIndex sequence
    * @return the parentIndex sequences
    */
   @Override
   public final int[] getParentNids(int childNid) {
      int childSequence = ModelGet.identifierService().getElementSequenceForNid(childNid, assemblageNid);
      if (this.childSequence_ParentNidArray_Map.containsKey(childSequence)) {
         return this.childSequence_ParentNidArray_Map.get(childSequence);
      }

      return new int[0];
   }
}

