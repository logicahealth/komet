/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.tree;

import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import java.util.BitSet;
import java.util.function.ObjIntConsumer;

/**
 * A structure to represent the concept taxonomy from a particular 
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
     * 
     * @param parentSequence sequence of the concept from which to compute 
     * descendents. 
     * @return {@code BitSet} of the descendents of the {@code parentSequence}
     */
    ConceptSequenceSet getDescendentSequenceSet(int parentSequence);
    
    /**
     * 
     * @param parentSequence sequence of the concept from which to find children
     * @return an array of child sequences. 
     */

    int[] getChildrenSequences(int parentSequence);

    /**
     *
     * @param childSequence sequence of the concept from which to find parent
     * @return an array of parent sequences. 
     */
    int[] getParentSequences(int childSequence);

    /**
     *
     * @return sequence identifiers for the root concept[s] of this tree.
     */
    int[] getRootSequences();

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
     *
     * @return the number of nodes in the tree.
     */
    int size();

}
