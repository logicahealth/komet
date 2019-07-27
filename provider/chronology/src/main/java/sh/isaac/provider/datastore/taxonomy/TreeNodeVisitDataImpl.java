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
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.roaringbitmap.RoaringBitmap;
import sh.isaac.api.datastore.SequenceStore;
import sh.isaac.api.tree.NodeStatus;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.ModelGet;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class TreeNodeVisitDataImpl
        implements TreeNodeVisitData {
   private static final Logger LOG = LogManager.getLogger();

   /**
    * The visit started.
    */
   private RoaringBitmap visitStarted = new RoaringBitmap();

   /**
    * The visit ended.
    */
   private RoaringBitmap visitEnded = new RoaringBitmap();

   /**
    * The leaf nodes.
    */
   private RoaringBitmap leafNodes = new RoaringBitmap();

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
   HashMap<String, RoaringBitmap[]> userNodeMap = new HashMap<>();

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
   
   private final SequenceStore ss;
   private final int conceptAssemblageNid;

   /**
    * Instantiates a new tree node visit data.
    * @param conceptAssemblageNid The assemblage Nid which specifies the assemblage where the concepts in this tree
    * where created within.  
    */
   public TreeNodeVisitDataImpl(int conceptAssemblageNid) {
      this.visitStarted = new RoaringBitmap();
      this.visitEnded = new RoaringBitmap();
      this.leafNodes = new RoaringBitmap();
      this.distanceList = new IntArrayList();
      this.discoveryTimeList = new IntArrayList();
      this.finishTimeList = new IntArrayList();
      this.siblingGroupNidList = new IntArrayList();
      this.predecessorNidList = new IntArrayList();
      this.predecessorNidList.fillFromToWith(0, this.predecessorNidList.size() - 1, -1);
      this.conceptAssemblageNid = conceptAssemblageNid;
      ss = ModelGet.sequenceStore();
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
      return this.discoveryTimeList.getQuick(nidToSequence(nodeNid));
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
      int sequence = nidToSequence(nodeNid);
      if (sequence >= this.discoveryTimeList.size()) {
         this.discoveryTimeList.setSize(sequence + 1);
      }
      this.discoveryTimeList.set(sequence, discoveryTime);
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
      return this.distanceList.getQuick(nidToSequence(nodeNid));
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
      int sequence = nidToSequence(nodeNid);
      if (sequence >= this.distanceList.size()) {
         this.distanceList.setSize(sequence + 1);
      }
      this.distanceList.set(sequence, distance);
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
      return this.finishTimeList.getQuick(nidToSequence(nodeNid));
   }

   /**
    * Set finish time.
    *
    * @param sequence the sequence
    * @param finishTime the finish time
    */
   private void setFinishTime(int nodeNid, int finishTime) {
      int sequence = nidToSequence(nodeNid);
      if (sequence >= this.finishTimeList.size()) {
         this.finishTimeList.setSize(sequence + 1);
      }

      this.finishTimeList.set(sequence, finishTime);
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
   public RoaringBitmap getLeafNodes() {
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
   public RoaringBitmap getNodeIdsForDepth(int depth) {
      final RoaringBitmap nodeIdsForDepth = new RoaringBitmap();

      for (int i = 0; i < this.distanceList.size(); i++) {
         if (this.distanceList.get(i) == depth) {
            nodeIdsForDepth.add(sequenceToNid(i));
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
      int nodeSequence = nidToSequence(nodeNid);
      int toReturn = this.predecessorNidList.getQuick(nodeSequence);
      if (toReturn == -1 ) {
         return OptionalInt.empty();
      }
      else
      {
         return OptionalInt.of(toReturn);
      }
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
      int nodeSequence = nidToSequence(nodeNid);
      int oldSize = this.predecessorNidList.size();
      if (nodeSequence >= oldSize) {
          this.predecessorNidList.setSize(nodeSequence + 1);
          this.predecessorNidList.fillFromToWith(oldSize, nodeSequence - 1, -1);  //In case there are gaps
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
      return this.siblingGroupNidList.getQuick(nidToSequence(nodeNid));
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
      int sequence = nidToSequence(nodeNid);
      if (sequence >= this.siblingGroupNidList.size()) {
         this.siblingGroupNidList.setSize(sequence + 1);
      }
      this.siblingGroupNidList.set(sequence, value);
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
   public RoaringBitmap getUserNodeSet(String nodeSetKey, int nodeNid) {
        int nodeSequence = nidToSequence(nodeNid);
        // lazy creation to save memory since not all tree traversals want to
        // use this capability.
        if (!this.userNodeMap.containsKey(nodeSetKey)) {
           this.userNodeMap.put(nodeSetKey, new RoaringBitmap[nodeSequence + 1]);
        }

      RoaringBitmap[] userNodeSet = this.userNodeMap.get(nodeSetKey);

        if (nodeSequence >= userNodeSet.length) {
           RoaringBitmap[] replacement = new RoaringBitmap[nodeSequence + 1];
           this.userNodeMap.put(nodeSetKey, replacement);
           for (int i = 0; i < userNodeSet.length; i++) {
              replacement[i] = userNodeSet[i];
           }
           userNodeSet = replacement;
        }
        
       if (userNodeSet[nodeSequence] == null) {
          userNodeSet[nodeSequence] = new RoaringBitmap();
       }
       return userNodeSet[nodeSequence];
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set the user node set.
    *
    * @param nodeSetKey
    * @param nodeNid the node sequence
    * @param conceptSet the concept set
    */
   @Override
   public void setUserNodeSet(String nodeSetKey, int nodeNid, RoaringBitmap conceptSet) {
      int nodeSequence = nidToSequence(nodeNid);
     // lazy creation to save memory since not all tree traversals want to use this capability.
     if (!this.userNodeMap.containsKey(nodeSetKey)) {
        this.userNodeMap.put(nodeSetKey, new RoaringBitmap[nodeSequence + 1]);
     }

     this.userNodeMap.get(nodeSetKey)[nodeSequence] = conceptSet;
   }
   
   private int nidToSequence(int nid) {
      if (ss == null) {
         return Integer.MAX_VALUE + nid; 
      }
      else {
         return ss.getElementSequenceForNid(nid, conceptAssemblageNid);
      }
   }
   
   private int sequenceToNid(int sequence) {
   if (ss == null) {
         return Integer.MAX_VALUE + sequence; 
      }
      else {
         return ss.getNidForElementSequence(conceptAssemblageNid, sequence);
      }
   }
}
