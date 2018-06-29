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

//~--- non-JDK imports --------------------------------------------------------

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;
import sh.isaac.api.tree.NodeStatus;
import sh.isaac.api.tree.TreeNodeVisitData;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TreeNodeVisitDataImpl. 
 *
 * @author kec
 */
public class TreeNodeVisitDataImpl implements TreeNodeVisitData {
   /** The visit started. */
   private OpenIntHashSet visitStarted = new OpenIntHashSet();

   /** The visit ended. */
   private OpenIntHashSet visitEnded = new OpenIntHashSet();

   /** The leaf nodes. */
   private OpenIntHashSet leafNodes = new OpenIntHashSet();

   /** The max depth. */
   private int maxDepth = 0;

   /** The time. */
   private int time = 0;

   /** The nodes visited. */
   private int nodesVisited = 0;

   /** The distance list. For each node, the distance from the root is tracked in this list, where the node is 
    represented by the index of the list, and the distance is represented by the value of the list at the index. */
   protected final IntArrayList distanceList;

   /** The discovery time list. For each node, the discovery time is tracked in this list, where the node is 
    represented by the index of the list, and the discovery time is represented by the value of the list at the index.*/
   protected final IntArrayList discoveryTimeList;

   /** The finish time list. For each node, the finish time is tracked in this list, where the node is 
    represented by the index of the list, and the finish time is represented by the value of the list at the index.*/
   protected final IntArrayList finishTimeList;

   /** The predecessor sequence list. For each node, the identifier of it's predecessor is provided, where the node
    is represented by the index of the list, and the identifier of the predecessor is represented by the value of the 
    list at the index. */
   protected final IntArrayList predecessorSequenceList;

   /** The sibling group sequence list. For each node, the identifier of it's sibling group is provided, where the node
    is represented by the index of the list, and the sibling group is represented by the value of the 
    list at the index. */
   protected final IntArrayList siblingGroupSequenceList;

   /** The processor may use this userNodeSet for their own purposes, such as the concepts referenced at node or above. 
    */
   HashMap<String, OpenIntHashSet[]> userNodeMap = new HashMap<>();

   /** The graph size. */
   private final int graphSize;
   
   /** The startSequence for this traversal. */
   private int startSequence = -1;
   
   /** set to hold any discovered cycles */
   private final Set<int[]> cycleSet = new TreeSet<>((int[] o1, int[] o2) -> {
      if (o1.length != o2.length) {
         return o1.length - o2.length;
      }
      // See if sets have same membership, just different order
      Arrays.sort(o1);
      Arrays.sort(o2);
      
      if (Arrays.equals(o1, o2)) {
         return 0;
      }
      for (int i = 0; i < o1.length; i++) {
         if (o1[i] != o2[i]) {
            return o1[i] - o2[i];
         }
      }
      return 0;
   });

   @Override
   public Set<int[]> getCycleSet() {
      return cycleSet;
   }


   /**
    * The start sequence for this traversal. If only one root, then this is the 
    * sequence of the root node. 
    * @return the start sequence 
    */
   @Override
   public int getStartNid() {
      return startSequence;
   }

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tree node visit data.
    *
    * @param graphSize the graph size
    */
   public TreeNodeVisitDataImpl(int graphSize) {
      this.graphSize                = graphSize;
      this.visitStarted             = new OpenIntHashSet();
      this.visitEnded               = new OpenIntHashSet();
      this.leafNodes                = new OpenIntHashSet();
      this.distanceList             = new IntArrayList(new int[graphSize]);
      this.distanceList.fillFromToWith(0, graphSize - 1, -1);
      this.discoveryTimeList        = new IntArrayList(new int[graphSize]);
      this.discoveryTimeList.fillFromToWith(0, graphSize - 1, -1);
      this.finishTimeList           = new IntArrayList(new int[graphSize]);
      this.finishTimeList.fillFromToWith(0, graphSize - 1, -1);
      this.siblingGroupSequenceList = new IntArrayList(new int[graphSize]);
      this.siblingGroupSequenceList.fillFromToWith(0, graphSize - 1, -1);
      this.predecessorSequenceList  = new IntArrayList(new int[graphSize]);
      this.predecessorSequenceList.fillFromToWith(0, graphSize - 1, -1);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * End node visit.
    *
    * @param nodeSequence the node sequence
    */
   @Override
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
   @Override
   public void startNodeVisit(int nodeSequence, int depth) {
      if (depth == 0 && startSequence == -1) {
         startSequence = nodeSequence;
      }
      setNodeStatus(nodeSequence, NodeStatus.PROCESSING);
      setDiscoveryTime(nodeSequence, this.time++);
      setDistance(nodeSequence, depth);
      this.nodesVisited++;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the userNodeSet.
    *
    * @param nodeSetKey
    * @param nodeSequence the node sequence
    * @return the concepts referenced at node or above
    */
   @Override
   public OpenIntHashSet getUserNodeSet(String nodeSetKey, int nodeSequence) {
      if (nodeSequence >= 0) {
         // lazy creation to save memory since not all tree traversals want to
         // use this capability.
         if (!this.userNodeMap.containsKey(nodeSetKey)) {
            this.userNodeMap.put(nodeSetKey, new OpenIntHashSet[this.graphSize]);
         }
         
         OpenIntHashSet[] userNodeSet = this.userNodeMap.get(nodeSetKey);
         if (userNodeSet[nodeSequence] == null) {
            userNodeSet[nodeSequence] = new OpenIntHashSet((int) Math.log(this.graphSize));
         }

         return userNodeSet[nodeSequence];
      }

      throw new UnsupportedOperationException("Node sequence < 0: " +  nodeSequence);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set the user node set.
    *
    * @param nodeSetKey
    * @param nodeSequence the node sequence
    * @param conceptSet the concept set
    */
   @Override
   public void setUserNodeSet(String nodeSetKey, int nodeSequence, OpenIntHashSet conceptSet) {
      if (nodeSequence >= 0) {
         // lazy creation to save memory since not all tree traversals want to
         // use this capability.
         if (!this.userNodeMap.containsKey(nodeSetKey)) {
            this.userNodeMap.put(nodeSetKey, new OpenIntHashSet[this.graphSize]);
         }

         this.userNodeMap.get(nodeSetKey)[nodeSequence] = conceptSet;
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the discovery time.
    *
    * @param sequence the sequence
    * @return the discovery time
    */
   @Override
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
   @Override
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
   @Override
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
   @Override
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

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the leaf node.
    *
    * @param sequence the new leaf node
    */
   @Override
   public void setLeafNode(int sequence) {
      this.leafNodes.add(sequence);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the leaf nodes.
    *
    * @return the leaf nodes
    */
   @Override
   public OpenIntHashSet getLeafNodes() {
      return this.leafNodes;
   }

   /**
    * Gets the max depth.
    *
    * @return the max depth
    */
   @Override
   public int getMaxDepth() {
      return this.maxDepth;
   }

   /**
    * Gets the node ids for depth.
    *
    * @param depth the depth
    * @return the node ids for depth
    */
   @Override
   public OpenIntHashSet getNodeIdsForDepth(int depth) {
      final OpenIntHashSet nodeIdsForDepth = new OpenIntHashSet();

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
   @Override
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
   @Override
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
   @Override
   public int getNodesVisited() {
      return this.nodesVisited;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public OptionalInt getPredecessorNid(int sequence) {
      int temp = this.predecessorSequenceList.getQuick(sequence);
      if (temp == -1) {
         return OptionalInt.empty();
      }
      return OptionalInt.of(temp);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set predecessor sequence.
    *
    * @param sequence the sequence
    * @param predecessorSequence the predecessor sequence
    */
   @Override
   public void setPredecessorNid(int sequence, int predecessorSequence) {
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
   @Override
   public int getSiblingGroupForNid(int sequence) {
      return this.siblingGroupSequenceList.get(sequence);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set sibling group for sequence.
    *
    * @param sequence the sequence
    * @param value the value
    */
   @Override
   public void setSiblingGroupForNid(int sequence, int value) {
      this.siblingGroupSequenceList.set(sequence, value);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the time.
    *
    * @return the time
    */
   @Override
   public int getTime() {
      return this.time;
   }
}

