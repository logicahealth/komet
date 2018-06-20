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
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import sh.isaac.api.tree.NodeStatus;
import sh.isaac.api.tree.TreeNodeVisitData;

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
   private HashMap<String, IntObjectHashMap<OpenIntHashSet>> userNodeMap = new HashMap<>();

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

   //TODO [refactor test] test these changes
   /**
    * The distance list. For each node, the distance from the root is tracked in this list, where the node is
    * represented by the index of the list, and the distance is represented by the value of the list at the index.
    */
   protected final IntIntHashMap distanceMap;

   /**
    * The discovery time list. For each node, the discovery time is tracked in this list, where the node is represented
    * by the key, and the discovery time is represented by the value.
    */
   protected final IntIntHashMap discoveryTimeMap;

   /**
    * The finish time list. For each node, the finish time is tracked in this list, where the node is represented by the
    * key, and the finish time is represented by the value.
    */
   protected final IntIntHashMap finishTimeMap;

   /**
    * The predecessor nid set. For each node, the identifier of it's predecessor is provided, where the node is
    * represented by the key, and the identifier of the predecessor is represented by the value of the
    * list at the key.
    */
   protected final IntIntHashMap predecessorNidMap;

   /**
    * The sibling group sequence map. For each node, the identifier of it's sibling group is provided, where the node
    * is represented by the key, and the sibling group is represented by the value.
    */
   protected final IntIntHashMap siblingGroupNidMap;

   /**
    * Instantiates a new tree node visit data.
    */
   public TreeNodeVisitDataBdbImpl() {
      this.visitStarted = new OpenIntHashSet();
      this.visitEnded = new OpenIntHashSet();
      this.leafNodes = new OpenIntHashSet();
      this.distanceMap = new IntIntHashMap();
      this.discoveryTimeMap = new IntIntHashMap();
      this.finishTimeMap = new IntIntHashMap();
      this.siblingGroupNidMap = new IntIntHashMap();
      this.predecessorNidMap = new IntIntHashMap();
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
      return this.discoveryTimeMap.get(nodeNid);
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

      this.discoveryTimeMap.put(nodeNid, discoveryTime);
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
      return this.distanceMap.get(nodeNid);
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
      this.distanceMap.put(nodeNid, distance);
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
      return this.finishTimeMap.get(nodeNid);
   }

   /**
    * Set finish time.
    *
    * @param sequence the sequence
    * @param finishTime the finish time
    */
   private void setFinishTime(int nodeNid, int finishTime) {
      this.finishTimeMap.put(nodeNid, finishTime);
   }

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

      for (IntIntPair iterateDepth : distanceMap.keyValuesView()) {
         if (iterateDepth.getTwo() == depth) {
            nodeIdsForDepth.add(iterateDepth.getOne());
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
      if (!this.visitStarted.contains(nodeNid)) {
         return NodeStatus.UNDISCOVERED;
      }

      if (this.visitEnded.contains(nodeNid)) {
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
      switch (nodeStatus) {
         case FINISHED:
            this.visitEnded.add(nodeNid);
            break;

         case PROCESSING:
            this.visitStarted.add(nodeNid);
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
    * {@inheritDoc}
    */
   @Override
   public OptionalInt getPredecessorNid(int nodeNid) {
      if (this.predecessorNidMap.contains(nodeNid)) {
         return OptionalInt.of(this.predecessorNidMap.get(nodeNid));
      }
      return OptionalInt.empty();
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set predecessor nid.
    *
    * @param nodeNid the node nid
    * @param predecessorNid the predecessor sequence
    */
   @Override
   public void setPredecessorNid(int nodeNid, int predecessorNid) {
      this.predecessorNidMap.put(nodeNid, predecessorNid);
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
      return this.siblingGroupNidMap.get(nodeNid);
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
      this.siblingGroupNidMap.put(nodeNid, value);
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
    * @param nodeId the node id
    * @return the concepts referenced at node or above
    */
   @Override
   public OpenIntHashSet getUserNodeSet(String nodeSetKey, int nodeId) {
      if (!this.userNodeMap.containsKey(nodeSetKey)) {
           this.userNodeMap.put(nodeSetKey, new IntObjectHashMap<OpenIntHashSet>());
      }
      
      IntObjectHashMap<OpenIntHashSet> userNodeSet = this.userNodeMap.get(nodeSetKey);

      if (!userNodeSet.containsKey(nodeId)) {
        userNodeSet.put(nodeId, new OpenIntHashSet());
      }
      return userNodeSet.get(nodeId);
   }

   /**
    * Set the user node set.
    *
    * @param nodeSetKey
    * @param nodeId the node sequence
    * @param conceptSet the concept set
    */
   @Override
   public void setUserNodeSet(String nodeSetKey, int nodeId, OpenIntHashSet conceptSet) {
      if (!this.userNodeMap.containsKey(nodeSetKey)) {
            this.userNodeMap.put(nodeSetKey, new IntObjectHashMap<OpenIntHashSet>());
      }

      this.userNodeMap.get(nodeSetKey).put(nodeId, conceptSet);
   }
}
