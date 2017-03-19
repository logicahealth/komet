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

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.OpenIntObjectHashMap;

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.tree.NodeStatus;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public abstract class AbstractHashTree
         implements Tree {
   static final int[] EMPTY_INT_ARRAY = new int[0];

   //~--- fields --------------------------------------------------------------

   /**
    * Maximum sequence number in this tree.
    */
   protected int maxSequence = -1;

   /**
    * map from a child sequence key to an array of parent sequences.
    */
   protected final OpenIntObjectHashMap<int[]> childSequence_ParentSequenceArray_Map;

   /**
    * Map from a parent sequence key to an array of child sequences.
    */
   protected final OpenIntObjectHashMap<int[]> parentSequence_ChildSequenceArray_Map;

   //~--- constructors --------------------------------------------------------

   public AbstractHashTree() {
      this.childSequence_ParentSequenceArray_Map = new OpenIntObjectHashMap<>();
      this.parentSequence_ChildSequenceArray_Map = new OpenIntObjectHashMap<>();
   }

   public AbstractHashTree(int initialSize) {
      this.childSequence_ParentSequenceArray_Map = new OpenIntObjectHashMap<>(initialSize);
      this.parentSequence_ChildSequenceArray_Map = new OpenIntObjectHashMap<>(initialSize);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public final TreeNodeVisitData breadthFirstProcess(int startSequence, ObjIntConsumer<TreeNodeVisitData> consumer) {
      final TreeNodeVisitData nodeVisitData = new TreeNodeVisitData(this.maxSequence);
      final Queue<Integer>    bfsQueue      = new LinkedList<>();

      nodeVisitData.startNodeVisit(startSequence, 0);
      bfsQueue.add(startSequence);

      while (!bfsQueue.isEmpty()) {
         final int   currentSequence = bfsQueue.remove();
         final int   currentDistance = nodeVisitData.getDistance(currentSequence);
         final int[] childSequences  = getChildrenSequences(currentSequence);

         if (childSequences.length == 0) {
            nodeVisitData.setLeafNode(currentSequence);
         }

         consumer.accept(nodeVisitData, currentSequence);

         for (final int childSequence: childSequences) {
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

   @Override
   public final Tree createAncestorTree(int childSequence) {
      final SimpleHashTree tree = new SimpleHashTree();

      addParentsAsChildren(tree, childSequence, getParentSequences(childSequence));
      return tree;
   }

   @Override
   public final TreeNodeVisitData depthFirstProcess(int startSequence, ObjIntConsumer<TreeNodeVisitData> consumer) {
      final TreeNodeVisitData graphVisitData = new TreeNodeVisitData(this.maxSequence);

      dfsVisit(startSequence, consumer, graphVisitData, 0);
      return graphVisitData;
   }

   protected void dfsVisit(int sequence,
                           ObjIntConsumer<TreeNodeVisitData> consumer,
                           TreeNodeVisitData nodeVisitData,
                           int depth) {
      nodeVisitData.startNodeVisit(sequence, depth);

      final int[] childSequences = getChildrenSequences(sequence);

      if (childSequences.length == 0) {
         nodeVisitData.setLeafNode(sequence);
      }

      consumer.accept(nodeVisitData, sequence);

      for (final int childSequence: childSequences) {
         if (nodeVisitData.getNodeStatus(childSequence) == NodeStatus.UNDISCOVERED) {
            dfsVisit(childSequence, consumer, nodeVisitData, depth + 1);
         }
      }

      nodeVisitData.endNodeVisit(sequence);
   }

   private void addParentsAsChildren(SimpleHashTree tree, int childSequence, int[] parentSequences) {
      IntStream.of(parentSequences).forEach((parentSequence) -> {
                           tree.addChild(childSequence, parentSequence);
                           addParentsAsChildren(tree, parentSequence, getParentSequences(parentSequence));
                        });
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public IntStream getChildrenSequenceStream(int parentSequence) {
      if (this.parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
         return IntStream.of(this.parentSequence_ChildSequenceArray_Map.get(parentSequence));
      }

      return IntStream.empty();
   }

   @Override
   public final int[] getChildrenSequences(int parentSequence) {
      if (this.parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
         return this.parentSequence_ChildSequenceArray_Map.get(parentSequence);
      }

      return new int[0];
   }

   @Override
   public final ConceptSequenceSet getDescendentSequenceSet(int parentSequence) {
      final ConceptSequenceSet descendentSequences = new ConceptSequenceSet();

      if (this.parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
         getDescendentsRecursive(parentSequence, descendentSequences);
         return descendentSequences;
      }

      return descendentSequences;
   }

   private void getDescendentsRecursive(int parentSequence, ConceptSequenceSet descendentSequences) {
      if (this.parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
         for (final int childSequence: this.parentSequence_ChildSequenceArray_Map.get(parentSequence)) {
            descendentSequences.add(childSequence);
            getDescendentsRecursive(childSequence, descendentSequences);
         }
      }
   }

   @Override
   public IntStream getParentSequenceStream(int childSequence) {
      if (this.childSequence_ParentSequenceArray_Map.containsKey(childSequence)) {
         return IntStream.of(this.childSequence_ParentSequenceArray_Map.get(childSequence));
      }

      return IntStream.empty();
   }

   @Override
   public final int[] getParentSequences(int childSequence) {
      if (this.childSequence_ParentSequenceArray_Map.containsKey(childSequence)) {
         return this.childSequence_ParentSequenceArray_Map.get(childSequence);
      }

      return new int[0];
   }
}

