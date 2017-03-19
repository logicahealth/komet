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



package sh.isaac.api.tree;

//~--- JDK imports ------------------------------------------------------------

import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.ConceptSequenceSet;

//~--- interfaces -------------------------------------------------------------

/**
 * A structure to represent the concept taxonomy at a particular point in time.
 * @author kec
 */
public interface Tree {
   /**
    * Visit the nodes of this tree that are accessible via the startSequence in
    * a breadth-first manner, and provide the sequence of the visited node, and
    * the {@code TreeNodeVisitData} to the consumer. Nodes are processed when
    * they are discovered,
    *
    * @param startSequence starting sequence for the breadth-first traversal of
    * this tree.
    * @param consumer The consumer that accepts each node and the
    * {@code TreeNodeVisitData} during the traversal.
    * @return {@code TreeNodeVisitData} for obtaining summary data.
    */
   TreeNodeVisitData breadthFirstProcess(int startSequence, ObjIntConsumer<TreeNodeVisitData> consumer);

   /**
    * An ancestor tree represents all the paths from a child to the tree root.
    * The child is the root of the ancestor tree, and the root of the original
    * source tree becomes the leaf of the ancestor tree.
    *
    * @param childSequence sequence of the node to compute the ancestor tree from.
    * @return a tree with the {@code childSequence} as the root
    */
   Tree createAncestorTree(int childSequence);

   /**
    * Visit the nodes of this tree that are accessible via the startSequence in
    * a depth-first manner, and provide the sequence of the visited node, and
    * the {@code TreeNodeVisitData} to the consumer.
    *
    * @param startSequence starting sequence for the depth-first traversal of
    * this tree.
    * @param consumer The consumer that accepts each node and the
    * {@code TreeNodeVisitData} during the traversal.
    * @return {@code TreeNodeVisitData} for obtaining summary data.
    */
   TreeNodeVisitData depthFirstProcess(int startSequence, ObjIntConsumer<TreeNodeVisitData> consumer);

   /**
    * Size.
    *
    * @return the number of nodes in the tree.
    */
   int size();

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the children sequence stream.
    *
    * @param parentSequence sequence of the concept from which to find children
    * @return an IntStream of child sequences.
    */
   IntStream getChildrenSequenceStream(int parentSequence);

   /**
    * Gets the children sequences.
    *
    * @param parentSequence sequence of the concept from which to find children
    * @return an array of child sequences.
    */
   int[] getChildrenSequences(int parentSequence);

   /**
    * Gets the descendent sequence set.
    *
    * @param parentSequence sequence of the concept from which to compute
    * descendents.
    * @return {@code BitSet} of the descendents of the {@code parentSequence}
    */
   ConceptSequenceSet getDescendentSequenceSet(int parentSequence);

   /**
    * Gets the parent sequence stream.
    *
    * @param childSequence sequence of the concept from which to find parent
    * @return an IntStream of parent sequences.
    */
   IntStream getParentSequenceStream(int childSequence);

   /**
    * Gets the parent sequences.
    *
    * @param childSequence sequence of the concept from which to find parent
    * @return an array of parent sequences.
    */
   int[] getParentSequences(int childSequence);

   /**
    * Gets the root sequence stream.
    *
    * @return IntStream of sequence identifiers for the root concept[s] of this tree.
    */
   IntStream getRootSequenceStream();

   /**
    * Gets the root sequences.
    *
    * @return sequence identifiers for the root concept[s] of this tree.
    */
   int[] getRootSequences();
}

