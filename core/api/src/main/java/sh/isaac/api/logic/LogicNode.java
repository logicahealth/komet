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
import java.util.Optional;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.component.sememe.version.DescriptionVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * Created by kec on 12/9/14.
 */
public interface LogicNode
        extends Comparable<LogicNode> {
   /**
    * Adds the children.
    *
    * @param children the children
    */
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
    * Fragment to string.
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
    * Sort the children of this node.
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

   /**
    * Gets the bytes.
    *
    * @param dataTarget the data target
    * @return the bytes
    */
   byte[] getBytes(DataTarget dataTarget);

   /**
    * Gets the child stream.
    *
    * @return the child stream
    */
   default Stream<LogicNode> getChildStream() {
      return Arrays.stream(getChildren());
   }

   /**
    * Gets the children.
    *
    * @return the children
    */
   LogicNode[] getChildren();

   /**
    * Gets the descendents.
    *
    * @return the descendents
    */
   LogicNode[] getDescendents();

   /**
    * Gets the node index.
    *
    * @return the node index
    */
   short getNodeIndex();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the node index.
    *
    * @param nodeIndex the new node index
    */
   void setNodeIndex(short nodeIndex);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the node semantic.
    *
    * @return the node semantic
    */
   NodeSemantic getNodeSemantic();
   
   /**
    * Gets the preferred description for this node.
    * @param stampCoordinate to determine the current description.
    * @param languageCoordinate to determine the language and dialect.
    * @return the preferred description. 
    */
   LatestVersion<DescriptionVersion> getPreferredDescription(StampCoordinate stampCoordinate, 
           LanguageCoordinate languageCoordinate);
   
   /**
    * Get the concept sequence for the concept being defined by the logical expression for which this node is a part. 
    * @return the concept sequence. 
    */
   int getSequenceForConceptBeingDefined();
}

