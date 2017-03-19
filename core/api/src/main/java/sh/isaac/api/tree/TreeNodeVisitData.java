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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api.tree;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.SequenceSet;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class TreeNodeVisitData {
   private SequenceSet<?>       visitStarted = new SequenceSet<>();
   private SequenceSet<?>       visitEnded   = new SequenceSet<>();
   private SequenceSet<?>       leafNodes    = new SequenceSet<>();
   private int                  maxDepth     = 0;
   private int                  time         = 0;
   private int                  nodesVisited = 0;
   protected final IntArrayList distanceList;
   protected final IntArrayList discoveryTimeList;
   protected final IntArrayList finishTimeList;
   protected final IntArrayList predecessorSequenceList;
   protected final IntArrayList siblingGroupSequenceList;
   private OpenIntHashSet[]     conceptsReferencedAtNodeOrAbove;
   private final int            graphSize;

   //~--- constructors --------------------------------------------------------

   public TreeNodeVisitData(int graphSize) {
      this.graphSize                = graphSize;
      this.visitStarted             = new SequenceSet<>();
      this.visitEnded               = new SequenceSet<>();
      this.leafNodes                = new SequenceSet<>();
      this.distanceList             = new IntArrayList(new int[graphSize]);
      this.discoveryTimeList        = new IntArrayList(new int[graphSize]);
      this.finishTimeList           = new IntArrayList(new int[graphSize]);
      this.siblingGroupSequenceList = new IntArrayList(new int[graphSize]);
      this.predecessorSequenceList  = new IntArrayList(new int[graphSize]);
      this.predecessorSequenceList.fillFromToWith(0, graphSize - 1, -1);
   }

   //~--- methods -------------------------------------------------------------

   public void endNodeVisit(int nodeSequence) {
      setNodeStatus(nodeSequence, NodeStatus.FINISHED);
      setFinishTime(nodeSequence, this.time++);
   }

   public void startNodeVisit(int nodeSequence, int depth) {
      setNodeStatus(nodeSequence, NodeStatus.PROCESSING);
      setDiscoveryTime(nodeSequence, this.time++);
      setDistance(nodeSequence, depth);
      this.nodesVisited++;
   }

   //~--- get methods ---------------------------------------------------------

   public OpenIntHashSet getConceptsReferencedAtNodeOrAbove(int nodeSequence) {
      if (nodeSequence >= 0) {
         // lazy creation to save memory since not all tree traversals want to
         // use this capability.
         if (this.conceptsReferencedAtNodeOrAbove == null) {
            this.conceptsReferencedAtNodeOrAbove = new OpenIntHashSet[this.graphSize];
         }

         if (this.conceptsReferencedAtNodeOrAbove[nodeSequence] == null) {
            this.conceptsReferencedAtNodeOrAbove[nodeSequence] = new OpenIntHashSet(this.graphSize);
         }

         return this.conceptsReferencedAtNodeOrAbove[nodeSequence];
      }

      return new OpenIntHashSet(this.graphSize);
   }

   //~--- set methods ---------------------------------------------------------

   public void setConceptsReferencedAtNodeOrAbove(int nodeSequence, ConceptSequenceSet conceptSet) {
      if (nodeSequence >= 0) {
         // lazy creation to save memory since not all tree traversals want to
         // use this capability.
         if (this.conceptsReferencedAtNodeOrAbove == null) {
            this.conceptsReferencedAtNodeOrAbove = new OpenIntHashSet[this.graphSize];
         }

         this.conceptsReferencedAtNodeOrAbove[nodeSequence] = conceptSet.asOpenIntHashSet();
      }
   }

   public void setConceptsReferencedAtNodeOrAbove(int nodeSequence, OpenIntHashSet conceptSet) {
      if (nodeSequence >= 0) {
         // lazy creation to save memory since not all tree traversals want to
         // use this capability.
         if (this.conceptsReferencedAtNodeOrAbove == null) {
            this.conceptsReferencedAtNodeOrAbove = new OpenIntHashSet[this.graphSize];
         }

         this.conceptsReferencedAtNodeOrAbove[nodeSequence] = conceptSet;
      }
   }

   //~--- get methods ---------------------------------------------------------

   public int getDiscoveryTime(int sequence) {
      return this.discoveryTimeList.getQuick(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   private void setDiscoveryTime(int sequence, int discoveryTime) {
      if (sequence >= this.discoveryTimeList.size()) {
         this.discoveryTimeList.setSize(sequence + 1);
      }

      this.discoveryTimeList.set(sequence, discoveryTime);
   }

   //~--- get methods ---------------------------------------------------------

   public int getDistance(int sequence) {
      return this.distanceList.getQuick(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   public void setDistance(int sequence, int distance) {
      if (sequence >= this.distanceList.size()) {
         this.distanceList.setSize(sequence + 1);
      }

      this.distanceList.set(sequence, distance);
      this.maxDepth = Math.max(this.maxDepth, distance);
   }

   //~--- get methods ---------------------------------------------------------

   public int getFinishTime(int sequence) {
      return this.finishTimeList.getQuick(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   private void setFinishTime(int sequence, int finishTime) {
      if (sequence >= this.finishTimeList.size()) {
         this.finishTimeList.setSize(sequence + 1);
      }

      this.finishTimeList.set(sequence, finishTime);
   }

   //~--- get methods ---------------------------------------------------------

   public int getGraphSize() {
      return this.graphSize;
   }

   public SequenceSet<?> getIntermediateNodes() {
      final SequenceSet intermediateNodes = new SequenceSet<>();

      intermediateNodes.or(this.visitEnded);
      intermediateNodes.andNot(this.leafNodes);
      return intermediateNodes;
   }

   //~--- set methods ---------------------------------------------------------

   public void setLeafNode(int sequence) {
      this.leafNodes.add(sequence);
   }

   //~--- get methods ---------------------------------------------------------

   public SequenceSet<?> getLeafNodes() {
      return this.leafNodes;
   }

   public int getMaxDepth() {
      return this.maxDepth;
   }

   public SequenceSet<?> getNodeIdsForDepth(int depth) {
      final SequenceSet<?> nodeIdsForDepth = new SequenceSet<>();

      for (int i = 0; i < this.distanceList.size(); i++) {
         if (this.distanceList.get(i) == depth) {
            nodeIdsForDepth.add(i);
         }
      }

      return nodeIdsForDepth;
   }

   public NodeStatus getNodeStatus(int nodeSequence) {
      if (!this.visitStarted.contains(nodeSequence)) {
         return NodeStatus.UNDISCOVERED;
      }

      if (this.visitEnded.contains(nodeSequence)) {
         return NodeStatus.FINISHED;
      }

      return NodeStatus.PROCESSING;
   }

   //~--- set methods ---------------------------------------------------------

   public void setNodeStatus(int nodeSequence, NodeStatus nodeStatus) {
      switch (nodeStatus) {
      case FINISHED:
         this.visitEnded.add(nodeSequence);
         break;

      case PROCESSING:
         this.visitStarted.add(nodeSequence);
         break;

      case UNDISCOVERED:
         throw new UnsupportedOperationException("Can't reset to undiscovered");

      default:
         throw new UnsupportedOperationException("no support for: " + nodeStatus);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public int getNodesVisited() {
      return this.nodesVisited;
   }

   public int getPredecessorSequence(int sequence) {
      return this.predecessorSequenceList.getQuick(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   public void setPredecessorSequence(int sequence, int predecessorSequence) {
      if (sequence >= this.predecessorSequenceList.size()) {
         this.predecessorSequenceList.setSize(sequence + 1);
      }

      this.predecessorSequenceList.set(sequence, predecessorSequence);
   }

   //~--- get methods ---------------------------------------------------------

   public int getSiblingGroupForSequence(int sequence) {
      return this.siblingGroupSequenceList.get(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   public void setSiblingGroupForSequence(int sequence, int value) {
      this.siblingGroupSequenceList.set(sequence, value);
   }

   //~--- get methods ---------------------------------------------------------

   public int getTime() {
      return this.time;
   }
}

