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

import java.util.Arrays;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.collections.ConceptSequenceSet;

//~--- interfaces -------------------------------------------------------------

/**
 * Created by kec on 12/9/14.
 */
public interface LogicNode
        extends Comparable<LogicNode> {
   void addChildren(LogicNode... children);

   /**
    * Adds the sequences of the concepts referenced by this node, including the
    * node semantic. Used by isomorphic algorithms to score potential matches.
    * Concepts reference by children of connector nodes should not be included, just
    * concepts associated with the node itself (node semantic concept + type concept, etc).
    * @param conceptSequenceSet The set to add the concept sequences to.
    */
   void addConceptsReferencedByNode(ConceptSequenceSet conceptSequenceSet);

   /**
    *
    * @return A string representing the fragment of the expression
    * rooted in this node.
    */
   String fragmentToString();

   /**
    * Use to when printing out multiple expressions, and you want to differentiate the
    * identifiers so that they are unique across all the expressions.
    * @param nodeIdSuffix the identifier suffix for this expression.
    * @return A string representing the fragment of the expression
    * rooted in this node.
    */
   String fragmentToString(String nodeIdSuffix);

   /**
    * Sort the children of this node
    */
   void sort();

   /**
    * Use to when printing out multiple expressions, and you want to differentiate the
    * identifiers so that they are unique across all the expressions.
    * @param nodeIdSuffix the identifier suffix for this expression.
    * @return a text representation of this expression.
    */
   String toString(String nodeIdSuffix);

   //~--- get methods ---------------------------------------------------------

   byte[] getBytes(DataTarget dataTarget);

   default Stream<LogicNode> getChildStream() {
      return Arrays.stream(getChildren());
   }

   LogicNode[] getChildren();

   LogicNode[] getDescendents();

   short getNodeIndex();

   //~--- set methods ---------------------------------------------------------

   void setNodeIndex(short nodeIndex);

   //~--- get methods ---------------------------------------------------------

   NodeSemantic getNodeSemantic();

   ;
}

