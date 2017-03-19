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
 * The Class TreeNodeVisitData.
 *
 * @author kec
 */
public class TreeNodeVisitData {
   
   /** The visit started. */
   private SequenceSet<?>       visitStarted = new SequenceSet<>();
   
   /** The visit ended. */
   private SequenceSet<?>       visitEnded   = new SequenceSet<>();
   
   /** The leaf nodes. */
   private SequenceSet<?>       leafNodes    = new SequenceSet<>();
   
   /** The max depth. */
   private int                  maxDepth     = 0;
   
   /** The time. */
   private int                  time         = 0;
   
   /** The nodes visited. */
   private int                  nodesVisited = 0;
   
   /** The distance list. */
   protected final IntArrayList distanceList;
   
   /** The discovery time list. */
   protected final IntArrayList discoveryTimeList;
   
   /** The finish time list. */
   protected final IntArrayList finishTimeList;
   
   /** The predecessor sequence list. */
   protected final IntArrayList predecessorSequenceList;
   
   /** The sibling group sequence list. */
   protected final IntArrayList siblingGroupSequenceList;
   
   /** The concepts referenced at node or above. */
   private OpenIntHashSet[]     conceptsReferencedAtNodeOrAbove;
   
   /** The graph size. */
   private final int            graphSize;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tree node visit data.
    *
    * @param graphSize the graph size
    */
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

   /**
    * End node visit.
    *
    * @param nodeSequence the node sequence
    */
   public void endNodeVisit(int nodeSequence) {
      setNodeStatus(nodeSequence, NodeStatus.FINISHED);
      setFinishTime(nodeSequence, this.time++);
   }

   /**
    * Start node visit.
    *
    * @param nodeSequence the node sequence
    * @param depth the depth
    */
   public void startNodeVisit(int nodeSequence, int depth) {
      setNodeStatus(nodeSequence, NodeStatus.PROCESSING);
      setDiscoveryTime(nodeSequence, this.time++);
      setDistance(nodeSequence, depth);
      this.nodesVisited++;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concepts referenced at node or above.
    *
    * @param nodeSequence the node sequence
    * @return the concepts referenced at node or above
    */
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

   /**
    * Set concepts referenced at node or above.
    *
    * @param nodeSequence the node sequence
    * @param conceptSet the concept set
    */
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

   /**
    * Set concepts referenced at node or above.
    *
    * @param nodeSequence the node sequence
    * @param conceptSet the concept set
    */
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

   /**
    * Gets the discovery time.
    *
    * @param sequence the sequence
    * @return the discovery time
    */
   public int getDiscoveryTime(int sequence) {
      return this.discoveryTimeList.getQuick(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set discovery time.
    *
    * @param sequence the sequence
    * @param discoveryTime the discovery time
    */
   private void setDiscoveryTime(int sequence, int discoveryTime) {
      if (sequence >= this.discoveryTimeList.size()) {
         this.discoveryTimeList.setSize(sequence + 1);
      }

      this.discoveryTimeList.set(sequence, discoveryTime);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the distance.
    *
    * @param sequence the sequence
    * @return the distance
    */
   public int getDistance(int sequence) {
      return this.distanceList.getQuick(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set distance.
    *
    * @param sequence the sequence
    * @param distance the distance
    */
   public void setDistance(int sequence, int distance) {
      if (sequence >= this.distanceList.size()) {
         this.distanceList.setSize(sequence + 1);
      }

      this.distanceList.set(sequence, distance);
      this.maxDepth = Math.max(this.maxDepth, distance);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the finish time.
    *
    * @param sequence the sequence
    * @return the finish time
    */
   public int getFinishTime(int sequence) {
      return this.finishTimeList.getQuick(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set finish time.
    *
    * @param sequence the sequence
    * @param finishTime the finish time
    */
   private void setFinishTime(int sequence, int finishTime) {
      if (sequence >= this.finishTimeList.size()) {
         this.finishTimeList.setSize(sequence + 1);
      }

      this.finishTimeList.set(sequence, finishTime);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the graph size.
    *
    * @return the graph size
    */
   public int getGraphSize() {
      return this.graphSize;
   }

   /**
    * Gets the intermediate nodes.
    *
    * @return the intermediate nodes
    */
   public SequenceSet<?> getIntermediateNodes() {
      final SequenceSet intermediateNodes = new SequenceSet<>();

      intermediateNodes.or(this.visitEnded);
      intermediateNodes.andNot(this.leafNodes);
      return intermediateNodes;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the leaf node.
    *
    * @param sequence the new leaf node
    */
   public void setLeafNode(int sequence) {
      this.leafNodes.add(sequence);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the leaf nodes.
    *
    * @return the leaf nodes
    */
   public SequenceSet<?> getLeafNodes() {
      return this.leafNodes;
   }

   /**
    * Gets the max depth.
    *
    * @return the max depth
    */
   public int getMaxDepth() {
      return this.maxDepth;
   }

   /**
    * Gets the node ids for depth.
    *
    * @param depth the depth
    * @return the node ids for depth
    */
   public SequenceSet<?> getNodeIdsForDepth(int depth) {
      final SequenceSet<?> nodeIdsForDepth = new SequenceSet<>();

      for (int i = 0; i < this.distanceList.size(); i++) {
         if (this.distanceList.get(i) == depth) {
            nodeIdsForDepth.add(i);
         }
      }

      return nodeIdsForDepth;
   }

   /**
    * Gets the node status.
    *
    * @param nodeSequence the node sequence
    * @return the node status
    */
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

   /**
    * Set node status.
    *
    * @param nodeSequence the node sequence
    * @param nodeStatus the node status
    */
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

   /**
    * Gets the nodes visited.
    *
    * @return the nodes visited
    */
   public int getNodesVisited() {
      return this.nodesVisited;
   }

   /**
    * Gets the predecessor sequence.
    *
    * @param sequence the sequence
    * @return the predecessor sequence
    */
   public int getPredecessorSequence(int sequence) {
      return this.predecessorSequenceList.getQuick(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set predecessor sequence.
    *
    * @param sequence the sequence
    * @param predecessorSequence the predecessor sequence
    */
   public void setPredecessorSequence(int sequence, int predecessorSequence) {
      if (sequence >= this.predecessorSequenceList.size()) {
         this.predecessorSequenceList.setSize(sequence + 1);
      }

      this.predecessorSequenceList.set(sequence, predecessorSequence);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sibling group for sequence.
    *
    * @param sequence the sequence
    * @return the sibling group for sequence
    */
   public int getSiblingGroupForSequence(int sequence) {
      return this.siblingGroupSequenceList.get(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set sibling group for sequence.
    *
    * @param sequence the sequence
    * @param value the value
    */
   public void setSiblingGroupForSequence(int sequence, int value) {
      this.siblingGroupSequenceList.set(sequence, value);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the time.
    *
    * @return the time
    */
   public int getTime() {
      return this.time;
   }
}

