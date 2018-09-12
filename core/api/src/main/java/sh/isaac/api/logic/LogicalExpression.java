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



package sh.isaac.api.logic;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.function.BiConsumer;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.commit.CommittableObject;
import sh.isaac.api.tree.TreeNodeVisitData;

//~--- interfaces -------------------------------------------------------------

/**
 * A tree representation of a logical expression, able to represent
 * all EL++ as well as full first order logic for future compatibility.
 * @author kec
 */
public interface LogicalExpression extends CommittableObject {
   /**
    * Contains.
    *
    * @param semantic the type of nodes to match
    * @return true if the expression contains at least 1 node that matches
    * the semantic
    */
   boolean contains(NodeSemantic semantic);

   /**
    * Find isomorphic aspects of this {@code LogicalExpression} (the reference expression) with respect
    * to another (the comparison expression). The {@code IsomorphicResults} will include the maximal
    * common rooted isomorphic solution, as well as identify additions and deletions.
    * This method can be computationally intensive
    * @param another the other {@code LogicalExpression} to compare with
    * @return The results of the comparison.
    */
   IsomorphicResults findIsomorphisms(LogicalExpression another);

   /**
    * Present the consumer the nodes of this expression in a depth-first manner,
    * starting with the root node.
    * @param consumer the consumer of the nodes.
    */
   void processDepthFirst(BiConsumer<LogicNode, TreeNodeVisitData> consumer);

   /**
    * Present the consumer the nodes of this expression in a depth-first manner,
    * starting with the root node.
    * @param consumer the consumer of the nodes.
    * @param treeNodeVisitData the visitData object to be used in the depth first processing. 
    */
   void processDepthFirst(BiConsumer<LogicNode, TreeNodeVisitData> consumer, TreeNodeVisitData treeNodeVisitData);

   /**
    * Process the fragment starting at fragmentRoot in a depth first manner.
    *
    * @param fragmentRoot the fragment root
    * @param consumer the consumer
    */
   void processDepthFirst(LogicNode fragmentRoot, BiConsumer<LogicNode, TreeNodeVisitData> consumer);

   /**
    * Use to when printing out multiple expressions, and you want to differentiate the
    * identifiers so that they are unique across all the expressions.
    * @param nodeIdSuffix the identifier suffix for this expression.
    * @return a text representation of this expression.
    */
   String toString(String nodeIdSuffix);

   /**
    * Use when the root node should not be displayed, and many of the features of 
    * use to the isaac developer (node id, etc) are removed, and the display is optimized
    * for .
    * @return a simple text representation of this expression.
    */
   String toSimpleString();
   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept nid.
    *
    * @return the concept nid this expression is associated with
    */
   int getConceptBeingDefinedNid();

   /**
    * Gets the data.
    *
    * @param dataTarget if the serialization should be targeted for internal or universal use.
    * @return a array of byte arrays that represent this logical expression
    * for serialization
    */
   byte[][] getData(DataTarget dataTarget);

   /**
    * Checks if meaningful.
    *
    * @return true if the expression is sufficiently complete to be meaningful.
    */
   boolean isMeaningful();

   /**
    * Gets the node.
    *
    * @param nodeIndex the node index
    * @return the node corresponding to the node index
    */
   LogicNode getNode(int nodeIndex);
   
   /**
    * Remove the node, and all references to the node,
    * and all the descendents of the node
    * @param node the node to remove
     * @return a new logical expression minus the node and all it's descendents. 
    */
   default LogicalExpression removeNode(LogicNode node) {
       return removeNode(node.getNodeIndex());
   }

   
   /**
    * Remove the node, and all references to the node,
    * and all the descendents of the node
    * @param nodeIndex the index of the node to remove
     * @return a new logical expression minus the node and all it's descendents. 
    */
   LogicalExpression removeNode(int nodeIndex);
   /**
    * Gets the node count.
    *
    * @return the number of nodes in this expression
    */
   int getNodeCount();

   /**
    * Gets the nodes of type.
    *
    * @param semantic the type of nodes to match
    * @return the nodes in the expression that match the NodeSemantic
    */
   List<LogicNode> getNodesOfType(NodeSemantic semantic);

   /**
    * Gets the root.
    *
    * @return the root node if this expression
    */
   LogicNode getRoot();
   
   /**
    * 
    * @return a string representation of this expression that conforms
    * to legal java for the logical expression builder service. 
    */
   String toBuilder();

   /**
    * specify the identifier of the concept that this logical expression represents. 
    * @param conceptNid 
    */
   void setConceptBeingDefinedNid(int conceptNid);
}

