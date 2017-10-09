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
package sh.isaac.api.tree.hashtree;

//~--- JDK imports ------------------------------------------------------------
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.set.OpenIntHashSet;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshotService;

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.tree.NodeStatus;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;

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
   protected final OpenIntObjectHashMap<int[]> childSequence_ParentSequenceArray_Map;

   /**
    * Map from a parentIndex sequence key to an array of childIndex sequences.
    */
   protected final OpenIntObjectHashMap<int[]> parentSequence_ChildSequenceArray_Map;
   
   protected final ManifoldCoordinate manifoldCoordinate;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new abstract hash tree.
    *
    * @param manifoldCoordinate
    */
   public AbstractHashTree(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
      this.childSequence_ParentSequenceArray_Map = new OpenIntObjectHashMap<>();
      this.parentSequence_ChildSequenceArray_Map = new OpenIntObjectHashMap<>();
   }

   /**
    * Instantiates a new abstract hash tree.
    *
    * @param manifoldCoordinate
    * @param initialSize the initial size
    */
   public AbstractHashTree(ManifoldCoordinate manifoldCoordinate, int initialSize) {
      this.manifoldCoordinate = manifoldCoordinate;
      this.childSequence_ParentSequenceArray_Map = new OpenIntObjectHashMap<>(initialSize);
      this.parentSequence_ChildSequenceArray_Map = new OpenIntObjectHashMap<>(initialSize);
   }

   //~--- methods -------------------------------------------------------------
   /**
    * Breadth first process.
    *
    * @param startSequence the start sequence
    * @param consumer the consumer
    * @return the tree node visit data
    */
   @Override
   public final TreeNodeVisitData breadthFirstProcess(int startSequence, ObjIntConsumer<TreeNodeVisitData> consumer) {
      final TreeNodeVisitData nodeVisitData = new TreeNodeVisitData(Get.identifierService().getMaxConceptSequence());
      final Queue<Integer> bfsQueue = new LinkedList<>();

      nodeVisitData.startNodeVisit(startSequence, 0);
      bfsQueue.add(startSequence);

      while (!bfsQueue.isEmpty()) {
         final int currentSequence = bfsQueue.remove();
         final int currentDistance = nodeVisitData.getDistance(currentSequence);
         final int[] childSequences = getChildrenSequences(currentSequence);

         if (childSequences.length == 0) {
            nodeVisitData.setLeafNode(currentSequence);
         }

         consumer.accept(nodeVisitData, currentSequence);

         for (final int childSequence : childSequences) {
            nodeVisitData.setLeafNode(currentSequence);
            consumer.accept(nodeVisitData, currentSequence);

            if (nodeVisitData.getNodeStatus(childSequence) == NodeStatus.UNDISCOVERED) {
               nodeVisitData.startNodeVisit(childSequence, currentDistance + 1);
               nodeVisitData.setPredecessorSequence(childSequence, currentSequence);
               bfsQueue.add(childSequence);
            }
         }

         nodeVisitData.endNodeVisit(currentSequence);
      }

      return nodeVisitData;
   }

   /**
    * Creates the ancestor tree.
    *
    * @param childSequence the childIndex sequence
    * @return the tree
    */
   @Override
   public final Tree createAncestorTree(int childSequence) {
      final SimpleHashTree tree = new SimpleHashTree(manifoldCoordinate);

      addParentsAsChildren(tree, childSequence, getParentSequences(childSequence));
      return tree;
   }

   /**
    * Depth first process.
    *
    * @param startSequence the start sequence
    * @param consumer the consumer
    * @return the tree node visit data
    */
   @Override
   public final TreeNodeVisitData depthFirstProcess(int startSequence, ObjIntConsumer<TreeNodeVisitData> consumer) {
      final TreeNodeVisitData graphVisitData = new TreeNodeVisitData(Get.identifierService().getMaxConceptSequence());

      dfsVisit(startSequence, consumer, graphVisitData, 0);
      return graphVisitData;
   }

   /**
    * Dfs visit.
    *
    * @param sequence the sequence
    * @param consumer the consumer
    * @param nodeVisitData the node visit data
    * @param depth the depth
    */
   protected void dfsVisit(int sequence,
           ObjIntConsumer<TreeNodeVisitData> consumer,
           TreeNodeVisitData nodeVisitData,
           int depth) {
      // Change to NodeStatus.PROCESSING
      nodeVisitData.startNodeVisit(sequence, depth);

      final int[] childSequences = getChildrenSequences(sequence);

      if (childSequences.length == 0) {
         nodeVisitData.setLeafNode(sequence);
      }

      consumer.accept(nodeVisitData, sequence);

      for (final int childSequence : childSequences) {
         if (nodeVisitData.getNodeStatus(childSequence) == NodeStatus.UNDISCOVERED) {
            nodeVisitData.setPredecessorSequence(childSequence, sequence);
            dfsVisit(childSequence, consumer, nodeVisitData, depth + 1);
         } else {
            // second path to node. Could be multi-parent or cycle...
            OpenIntHashSet userNodeSet = nodeVisitData.getUserNodeSet(MULTI_PARENT_SETS, childSequence);
            // add previous predecessor to node. 
            userNodeSet.add(nodeVisitData.getPredecessorSequence(childSequence));
            // add to extra parent set of the child...
            boolean addParent = true;
            for (int otherParentSequence : userNodeSet.keys().elements()) {
               if (isDescendentOf(otherParentSequence, sequence, nodeVisitData)) {
                  addParent = false;
               }
               if (isDescendentOf(sequence, otherParentSequence, nodeVisitData)) {
                  userNodeSet.remove(otherParentSequence);
               }
            }
            if (addParent) {
               userNodeSet.add(sequence);
            }

         }
      }

      // Change to NodeStatus.FINISHED
      nodeVisitData.endNodeVisit(sequence);
   }

   @Override
   public boolean isDescendentOf(int childSequence, int parentSequence) {
      int[] parentSequences = getParentSequences(childSequence);
      if (Arrays.binarySearch(parentSequences, parentSequence) >= 0) {
         return true;
      }
      for (int sequenceToTest : parentSequences) {
         if (isDescendentOf(sequenceToTest, parentSequence)) {
            return true;
         }
      }
      return false;
   }

   private boolean isDescendentOf(int childSequence, int parentSequenceToFind, TreeNodeVisitData nodeVisitData) {
      return isDescendentOfWithDepth(childSequence, parentSequenceToFind, 0, childSequence, nodeVisitData);
   }

   private boolean isDescendentOfWithDepth(int childSequence, int parentSequenceToFind, 
           int depth, int originalChildSequence, TreeNodeVisitData nodeVisitData) {
      int[] parentSequences = getParentSequences(childSequence);
      if (Arrays.binarySearch(parentSequences, parentSequenceToFind) >= 0) {
         return true;
      }
      for (int sequenceToTest : parentSequences) {
         if (depth < 100) {
            if (sequenceToTest != childSequence) {
               if (isDescendentOfWithDepth(sequenceToTest, parentSequenceToFind, depth + 1, originalChildSequence, nodeVisitData)) {
                  return true;
               }
            } else {
               System.out.println("Self reference for: " + childSequence + " " + Get.conceptDescriptionText(childSequence));
            }

         } else {
            int[] cycleArray = findCycle(originalChildSequence, new int[]{originalChildSequence});
            if (cycleArray.length > 0) {
               int lastSequenceInCycle = cycleArray[cycleArray.length - 1];
               int cycleStart = 0;
               while (cycleArray[cycleStart] != lastSequenceInCycle) {
                  cycleStart++;
               }
               cycleArray = Arrays.copyOfRange(cycleArray, cycleStart, cycleArray.length);
            }
            if (cycleArray.length == 0) {
               cycleArray = findCycle(originalChildSequence, new int[]{originalChildSequence});
            }
            if (!nodeVisitData.getCycleSet().contains(cycleArray)) {
               nodeVisitData.getCycleSet().add(cycleArray);
               StringBuilder builder = new StringBuilder();
               if (cycleArray.length == 0) {
                  builder.append("Depth exceeded, but  cycle not found between: \n     ");
                  builder.append(originalChildSequence).append(" ")
                          .append(Get.conceptDescriptionText(originalChildSequence));
                  builder.append("\n     ")
                          .append(sequenceToTest).append(" ")
                          .append(Get.conceptDescriptionText(sequenceToTest));
               } else {
                  builder.append("Cycle found: \n");
                  if (cycleArray.length == 2) {
                     builder.append("\n   SELF REFERENCE");
                  }
                  for (int sequence : cycleArray) {
                     builder.append("\n   ").append(sequence).append(" ").append(Get.conceptDescriptionText(sequence));
                  }
               }
               builder.append("\n");
               System.out.println(builder.toString());
            }
         }
      }
      return false;
   }

   private int[] findCycle(int childSequence, int[] encounterOrder) {
      int[] parentSequences = getParentSequences(childSequence);
      for (int sequenceToTest : parentSequences) {
         int[] encounterWithNewSequence = Arrays.copyOf(encounterOrder, encounterOrder.length + 1);
         encounterWithNewSequence[encounterOrder.length] = sequenceToTest;
         if (Arrays.stream(encounterOrder).anyMatch((pastSequence) -> pastSequence == sequenceToTest)) {
            return encounterWithNewSequence;
         } 
      }
      for (int sequenceToTest : parentSequences) {
         int[] encounterWithNewSequence = Arrays.copyOf(encounterOrder, encounterOrder.length + 1);
         encounterWithNewSequence[encounterOrder.length] = sequenceToTest;
         int[] cycleArray = findCycle(sequenceToTest, encounterWithNewSequence);
         if (cycleArray.length > 0) {
            return cycleArray;
         }
      }
      
      return new int[0];
   }

   /**
    * Adds the parents as children.
    *
    * @param tree the tree
    * @param childSequence the childIndex sequence
    * @param parentSequences the parentIndex sequences
    */
   private void addParentsAsChildren(SimpleHashTree tree, int childSequence, int[] parentSequences) {
      IntStream.of(parentSequences).forEach((parentSequence) -> {
         tree.addChild(childSequence, parentSequence);
         addParentsAsChildren(tree, parentSequence, getParentSequences(parentSequence));
      });
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the children sequence stream.
    *
    * @param parentSequence the parentIndex sequence
    * @return the children sequence stream
    */
   @Override
   public IntStream getChildrenSequenceStream(int parentSequence) {
      return IntStream.of(getChildrenSequences(parentSequence));
   }

   /**
    * Gets the children sequences.
    *
    * @param parentSequence the parentIndex sequence
    * @return the children sequences
    */
   @Override
   public final int[] getChildrenSequences(int parentSequence) {
      if (this.parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {

         return this.parentSequence_ChildSequenceArray_Map.get(parentSequence);
      }

      return new int[0];
   }

   /**
    * Gets the descendent sequence set.
    *
    * @param parentSequence the parentIndex sequence
    * @return the descendent sequence set
    */
   @Override
   public final ConceptSequenceSet getDescendentSequenceSet(int parentSequence) {
      final ConceptSequenceSet descendentSequences = new ConceptSequenceSet();

      if (this.parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
         getDescendentsRecursive(parentSequence, descendentSequences);
         return descendentSequences;
      }

      return descendentSequences;
   }

   /**
    * Gets the descendents recursive.
    *
    * @param parentSequence the parentIndex sequence
    * @param descendentSequences the descendent sequences
    * @return the descendents recursive
    */
   private void getDescendentsRecursive(int parentSequence, ConceptSequenceSet descendentSequences) {
      if (this.parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
         for (final int childSequence : this.parentSequence_ChildSequenceArray_Map.get(parentSequence)) {
            descendentSequences.add(childSequence);
            getDescendentsRecursive(childSequence, descendentSequences);
         }
      }
   }

   /**
    * Gets the parentIndex sequence stream.
    *
    * @param childSequence the childIndex sequence
    * @return the parentIndex sequence stream
    */
   @Override
   public IntStream getParentSequenceStream(int childSequence) {
      return IntStream.of(getParentSequences(childSequence));
   }

   /**
    * Gets the parentIndex sequences.
    *
    * @param childSequence the childIndex sequence
    * @return the parentIndex sequences
    */
   @Override
   public final int[] getParentSequences(int childSequence) {
      if (this.childSequence_ParentSequenceArray_Map.containsKey(childSequence)) {
         return this.childSequence_ParentSequenceArray_Map.get(childSequence);
      }

      return new int[0];
   }
}
