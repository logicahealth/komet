/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api.tree;

import java.util.OptionalInt;
import java.util.Set;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 */
public interface TreeNodeVisitData {

   //~--- methods -------------------------------------------------------------
   /**
    * End node visit.
    *
    * @param nodeId the node id
    */
   void endNodeVisit(int nodeId);

   Set<int[]> getCycleSet();

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the discovery time.
    *
    * @param nodeId the node id
    * @return the discovery time
    */
   int getDiscoveryTime(int nodeId);

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the distance.
    *
   * @param nodeId the node id
     * @return the distance
    */
   int getDistance(int nodeId);

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the finish time.
    *
   * @param nodeId the node id
   * @return the finish time
   */
   int getFinishTime(int nodeId);

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the graph size.
    *
    * @return the graph size
    */
   int getGraphSize();

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the leaf nodes.
    *
    * @return the leaf nodes
    */
   OpenIntHashSet getLeafNodes();

   /**
    * Gets the max depth.
    *
    * @return the max depth
    */
   int getMaxDepth();

   /**
    * Gets the node ids for depth.
    *
    * @param depth the depth
    * @return the node ids for depth
    */
   OpenIntHashSet getNodeIdsForDepth(int depth);

   /**
    * Gets the node status.
    *
    * @param nodeId the node id
    * @return the node status
    */
   NodeStatus getNodeStatus(int nodeId);

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the nodes visited.
    *
    * @return the nodes visited
    */
   int getNodesVisited();

   /**
    * Gets the predecessor nid or empty if no predecessor.
    *
    * @param nodeId the node id
    * @return the predecessor nid
    */
   OptionalInt getPredecessorNid(int nodeId);

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the sibling group for nid.
    *
    * @param nodeId the node id
    * @return the sibling group for nid
    */
   int getSiblingGroupForNid(int nodeId);

   /**
    * The start nid for this traversal. If only one root, then this is the
    * nid of the root node.
    * @return the start nid
    */
   int getStartNid();

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the time.
    *
    * @return the time
    */
   int getTime();

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the userNodeSet.
    *
    * @param nodeSetKey
    * @param nodeId the node id
    * @return the concepts referenced at node or above
    */
   OpenIntHashSet getUserNodeSet(String nodeSetKey, int nodeId);

   //~--- set methods ---------------------------------------------------------
   /**
    * Set distance.
    *
    * @param nodeId the node id
    * @param distance the distance
    */
   void setDistance(int nodeId, int distance);

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the leaf node.
    *
    * @param nodeId the node id
    */
   void setLeafNode(int nodeId);

   //~--- set methods ---------------------------------------------------------
   /**
    * Set node status.
    *
    * @param nodeId the node id
    * @param nodeStatus the node status
    */
   void setNodeStatus(int nodeId, NodeStatus nodeStatus);

   //~--- set methods ---------------------------------------------------------
   /**
    * Set predecessor nid.
    *
    * @param nodeId the node id
    * @param predecessorNodeId the predecessor node id
    */
   void setPredecessorNid(int nodeId, int predecessorNodeId);

   //~--- set methods ---------------------------------------------------------
   /**
    * Set sibling group for nid.
    *
    * @param nodeId the node id
    * @param value the value
    */
   void setSiblingGroupForNid(int nodeId, int value);

   //~--- set methods ---------------------------------------------------------
   /**
    * Set the user node set.
    *
    * @param nodeSetKey
    * @param nodeId the node id
    * @param conceptSet the concept set
    */
   void setUserNodeSet(String nodeSetKey, int nodeId, OpenIntHashSet conceptSet);

   /**
    * Start node visit.
    *
    * @param nodeId the node id
    * @param depth the depth
    */
   void startNodeVisit(int nodeId, int depth);
   
}
