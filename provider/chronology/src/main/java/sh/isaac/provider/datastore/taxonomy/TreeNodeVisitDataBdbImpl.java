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
package sh.isaac.provider.datastore.taxonomy;

//~--- JDK imports ------------------------------------------------------------
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.api.tree.NodeStatus;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.SpinedNidIntMap;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class TreeNodeVisitDataBdbImpl
        implements TreeNodeVisitData {
   private static final Logger LOG = LogManager.getLogger();

   /**
    * The visit started.
    */
   private OpenIntHashSet visitStarted = new OpenIntHashSet();

   /**
    * The visit ended.
    */
   private OpenIntHashSet visitEnded = new OpenIntHashSet();

   /**
    * The leaf nodes.
    */
   private OpenIntHashSet leafNodes = new OpenIntHashSet();

   /**
    * The max depth.
    */
   private int maxDepth = 0;

   /**
    * The time.
    */
   private int time = 0;

   /**
    * The count of the nodes visited.
    */
   private int nodesVisited = 0;

   /**
    * The processor may use this userNodeSet for their own purposes, such as the concepts referenced at node or above.
    */
   HashMap<String, OpenIntHashSet[]> userNodeMap = new HashMap<>();

   /**
    * The startNid for this traversal.
    */
   private int startNid = -1;

   /**
    * set to hold any discovered cycles
    */
   private final Set<int[]> cycleSet = new TreeSet<>(
           (int[] o1,
                   int[] o2) -> {
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

   /**
    * The distance list. For each node, the distance from the root is tracked in this list, where the node is
    * represented by the index of the list, and the distance is represented by the value of the list at the index.
    */
   protected final IntArrayList distanceList;

   /**
    * The discovery time list. For each node, the discovery time is tracked in this list, where the node is represented
    * by the index of the list, and the discovery time is represented by the value of the list at the index.
    */
   protected final IntArrayList discoveryTimeList;

   /**
    * The finish time list. For each node, the finish time is tracked in this list, where the node is represented by the
    * index of the list, and the finish time is represented by the value of the list at the index.
    */
   protected final IntArrayList finishTimeList;

   /**
    * The predecessor nid list. For each node, the identifier of it's predecessor is provided, where the node is
    * represented by the index of the list, and the identifier of the predecessor is represented by the value of the
    * list at the index.
    */
   protected final IntArrayList predecessorNidList;

   /**
    * The sibling group sequence list. For each node, the identifier of it's sibling group is provided, where the node
    * is represented by the index of the list, and the sibling group is represented by the value of the list at the
    * index.
    */
   protected final IntArrayList siblingGroupNidList;

   /**
    * The graph size.
    */
   private final int graphSize;
   private final int conceptAssemblageNid;
   private final SpinedNidIntMap nid_sequenceInAssemblage_map;
   private final SpinedIntIntMap sequenceInAssemblage_nid_map;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new tree node visit data.
    *
    * @param graphSize the graph size
    * @param conceptAssemblageNid
    * @param nid_sequenceInAssemblage_map
    * @param sequenceInAssemblage_nid_map
    *
    *
    */
   public TreeNodeVisitDataBdbImpl(int graphSize,
           int conceptAssemblageNid,
           SpinedNidIntMap nid_sequenceInAssemblage_map,
           SpinedIntIntMap sequenceInAssemblage_nid_map) {
      this.conceptAssemblageNid = conceptAssemblageNid;
      this.nid_sequenceInAssemblage_map = nid_sequenceInAssemblage_map;
      this.sequenceInAssemblage_nid_map = sequenceInAssemblage_nid_map;
      this.graphSize = graphSize + 1;
      this.visitStarted = new OpenIntHashSet();
      this.visitEnded = new OpenIntHashSet();
      this.leafNodes = new OpenIntHashSet();
      this.distanceList = new IntArrayList(new int[this.graphSize]);
      this.distanceList.fillFromToWith(0, this.graphSize - 1, -1);
      this.discoveryTimeList = new IntArrayList(new int[this.graphSize]);
      this.discoveryTimeList.fillFromToWith(0, this.graphSize - 1, -1);
      this.finishTimeList = new IntArrayList(new int[this.graphSize]);
      this.finishTimeList.fillFromToWith(0, this.graphSize - 1, -1);
      this.siblingGroupNidList = new IntArrayList(new int[this.graphSize]);
      this.siblingGroupNidList.fillFromToWith(0, this.graphSize - 1, -1);
      this.predecessorNidList = new IntArrayList(new int[this.graphSize]);
      this.predecessorNidList.fillFromToWith(0, this.graphSize - 1, -1);
   }

   //~--- methods -------------------------------------------------------------
   /**
    * End node visit.
    *
    * @param nodeNid the node sequence
    */
   @Override
   public void endNodeVisit(int nodeNid) {
      setNodeStatus(nodeNid, NodeStatus.FINISHED);
      setFinishTime(nodeNid, this.time++);
   }

   /**
    * Start node visit.
    *
    * @param nodeNid the node sequence
    * @param depth the depth
    */
   @Override
   public void startNodeVisit(int nodeNid, int depth) {
      if (nodeNid >= 0) {
         throw new IllegalStateException("Nid is positive: " + nodeNid);
      }
      if ((depth == 0) && (startNid == -1)) {
         startNid = nodeNid;
      }

      setNodeStatus(nodeNid, NodeStatus.PROCESSING);
      setDiscoveryTime(nodeNid, this.time++);
      setDistance(nodeNid, depth);
      this.nodesVisited++;
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------
   @Override
   public Set<int[]> getCycleSet() {
      return cycleSet;
   }

   /**
    * Gets the discovery time.
    *
    * @param nodeNid the node nid
    * @return the discovery time
    */
   @Override
   public int getDiscoveryTime(int nodeNid) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }

      return this.discoveryTimeList.getQuick(nodeSequence);
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set discovery time.
    *
    * @param nodeNid the nodeNid
    * @param discoveryTime the discovery time
    */
   private void setDiscoveryTime(int nodeNid, int discoveryTime) {
      if (nodeNid >= 0) {
         throw new IllegalStateException("Nid is positive: " + nodeNid);
      }
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
      if (nodeSequence >= this.discoveryTimeList.size()) {
         this.discoveryTimeList.setSize(nodeSequence + 1);
      }

      this.discoveryTimeList.set(nodeSequence, discoveryTime);
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------
   /**
    * Gets the distance.
    *
    * @param nodeNid the nodeNid
    * @return the distance
    */
   @Override
   public int getDistance(int nodeNid) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
      return this.distanceList.getQuick(nodeSequence);
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set distance.
    *
    * @param nodeNid the node Nid
    * @param distance the distance
    */
   @Override
   public void setDistance(int nodeNid, int distance) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
      if (nodeSequence >= this.distanceList.size()) {
         this.distanceList.setSize(nodeSequence + 1);
      }

      this.distanceList.set(nodeSequence, distance);
      this.maxDepth = Math.max(this.maxDepth, distance);
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------
   /**
    * Gets the finish time.
    *
    * @param nodeNid the nodeNid
    * @return the finish time
    */
   @Override
   public int getFinishTime(int nodeNid) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
      return this.finishTimeList.getQuick(nodeSequence);
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set finish time.
    *
    * @param sequence the sequence
    * @param finishTime the finish time
    */
   private void setFinishTime(int nodeNid, int finishTime) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
      if (nodeSequence >= this.finishTimeList.size()) {
         this.finishTimeList.setSize(nodeSequence + 1);
      }

      this.finishTimeList.set(nodeSequence, finishTime);
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------
   /**
    * Gets the graph size.
    *
    * @return the graph size
    */
   @Override
   public int getGraphSize() {
      return this.graphSize;
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the leaf node.
    *
    * @param nodeNid the nid of the leaf node
    */
   @Override
   public void setLeafNode(int nodeNid) {
      this.leafNodes.add(nodeNid);
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------
   /**
    * Gets the leaf node nids.
    *
    * @return the leaf node nids
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
            nodeIdsForDepth.add(this.sequenceInAssemblage_nid_map.get(i));
         }
      }

      return nodeIdsForDepth;
   }

   /**
    * Gets the node status.
    *
    * @param nodeNid the node sequence
    * @return the node status
    */
   @Override
   public NodeStatus getNodeStatus(int nodeNid) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
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
    * @param nodeNid the node nid
    * @param nodeStatus the node status
    */
   @Override
   public void setNodeStatus(int nodeNid, NodeStatus nodeStatus) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
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

   //~--- getValueSpliterator methods ---------------------------------------------------------
   /**
    * Gets the count of the nodes visited.
    *
    * @return the count of the nodes visited
    */
   @Override
   public int getNodesVisited() {
      return this.nodesVisited;
   }

   /**
    * Gets the predecessor sequence or -1 if no predecessor.
    *
    * @param nodeNid the node nid
    * @return the predecessor nid
    */
   @Override
   public int getPredecessorNid(int nodeNid) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
      return this.predecessorNidList.getQuick(nodeSequence);
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set predecessor sequence.
    *
    * @param nodeNid the node nid
    * @param predecessorNid the predecessor sequence
    */
   @Override
   public void setPredecessorNid(int nodeNid, int predecessorNid) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
      if (nodeSequence >= this.predecessorNidList.size()) {
         this.predecessorNidList.setSize(nodeSequence + 1);
      }

      this.predecessorNidList.set(nodeSequence, predecessorNid);
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------
   /**
    * Gets the sibling group for sequence.
    *
    * @param nodeNid the node nid
    * @return the sibling group for sequence
    */
   @Override
   public int getSiblingGroupForNid(int nodeNid) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
      return this.siblingGroupNidList.get(nodeSequence);
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set sibling group for sequence.
    *
    * @param nodeNid the node nid
    * @param value the value
    */
   @Override
   public void setSiblingGroupForNid(int nodeNid, int value) {
      int nodeSequence = nid_sequenceInAssemblage_map.get(nodeNid);
      if (nodeSequence == Integer.MAX_VALUE) {
         throw new IllegalStateException("nodeSequence not initialized: " + nodeSequence);
      }
      this.siblingGroupNidList.set(nodeSequence, value);
   }

   //~--- getValueSpliterator methods ---------------------------------------------------------
   /**
    * The start sequence for this traversal. If only one root, then this is the sequence of the root node.
    *
    * @return the start sequence
    */
   @Override
   public int getStartNid() {
      return startNid;
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   @Override
   public int getTime() {
      return this.time;
   }

   /**
    * Gets the userNodeSet.
    *
    * @param nodeSetKey
    * @param nodeNid the node sequence
    * @return the concepts referenced at node or above
    */
   @Override
   public OpenIntHashSet getUserNodeSet(String nodeSetKey, int nodeNid) {
      if (nodeNid < 0) {
         int assemblageNid = ModelGet.identifierService().getAssemblageNid(nodeNid).getAsInt();
         if (assemblageNid < 0) {
            int nodeSequence = ModelGet.identifierService().getElementSequenceForNid(nodeNid);
            // lazy creation to save memory since not all tree traversals want to
            // use this capability.
            if (!this.userNodeMap.containsKey(nodeSetKey)) {
               this.userNodeMap.put(nodeSetKey, new OpenIntHashSet[this.graphSize]);
            }

            OpenIntHashSet[] userNodeSet = this.userNodeMap.get(nodeSetKey);

            if (nodeSequence < userNodeSet.length) {
               if (userNodeSet[nodeSequence] == null) {
                  userNodeSet[nodeSequence] = new OpenIntHashSet((int) Math.log(this.graphSize));
               }
            } else {
               if (userNodeSet[nodeSequence] == null) {
                  userNodeSet[nodeSequence] = new OpenIntHashSet((int) Math.log(this.graphSize));
               }
            }

            return userNodeSet[nodeSequence];
         }
         LOG.warn("Trying to retrieve sequence for unknown nid: " + nodeNid);
         return new OpenIntHashSet();
      }

      throw new UnsupportedOperationException("Node nid > 0: " + nodeNid);
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
}
