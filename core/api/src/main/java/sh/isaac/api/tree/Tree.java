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
import java.util.function.Supplier;
import sh.isaac.api.collections.NidSet;

//~--- non-JDK imports --------------------------------------------------------


//~--- interfaces -------------------------------------------------------------

/**
 * A structure to represent the concept taxonomy at a particular point in time.
 * @author kec
 */
public interface Tree {
   /**
    * 
    * @return the assemblage nid which specifies the assemblage where the concepts in this tree
    * where created within. 
    */
   int getAssemblageNid();
   /**
    * Visit the nodes of this tree that are accessible via the startNid in
    * a breadth-first manner, and provide the nid of the visited node, and
    * the {@code TreeNodeVisitDataImpl} to the consumer. Nodes are processed when
    * they are discovered,
    *
    * @param rootNid starting nid for the breadth-first traversal of
    * this tree.
    * @param consumer The consumer that accepts each node and the
    * {@code TreeNodeVisitDataImpl} during the traversal.
    * @param emptyDataSupplier provides a new, unprocessed, TreeNodeVisitData object 
    * @return {@code TreeNodeVisitDataImpl} for obtaining summary data.
    */
   TreeNodeVisitData breadthFirstProcess(int rootNid, ObjIntConsumer<TreeNodeVisitData> consumer, 
           Supplier<TreeNodeVisitData> emptyDataSupplier);

   /**
    * An ancestor tree represents all the paths from a child to the tree root.
    * The child is the root of the ancestor tree, and the root of the original
    * source tree becomes the leaf of the ancestor tree.
    *
    * @param childNid nid of the node to compute the ancestor tree from.
    * @return a tree with the {@code childNid} as the root
    */
   Tree createAncestorTree(int childNid);

   /**
    * Visit the nodes of this tree that are accessible via the startNid in
    * a depth-first manner, and provide the nid of the visited node, and
    * the {@code TreeNodeVisitDataImpl} to the consumer.
    *
    * @param rootNid starting nid for the depth-first traversal of
    * this tree.
    * @param consumer The consumer that accepts each node and the
    * {@code TreeNodeVisitDataImpl} during the traversal.
    * @param emptyDataSupplier provides a new, unprocessed, TreeNodeVisitData object 
    * @return {@code TreeNodeVisitDataImpl} for obtaining summary data.
    */
   TreeNodeVisitData depthFirstProcess(int rootNid, ObjIntConsumer<TreeNodeVisitData> consumer, 
           Supplier<TreeNodeVisitData> emptyDataSupplier);

   /**
    * Size.
    *
    * @return the number of nodes in the tree.
    */
   int size();

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the children nids.
    *
    * @param parentNid nid of the concept from which to find children
    * @return an array of child nids.
    */
   int[] getChildNids(int parentNid);

   /**
    * Gets the descendent nid set.
    *
    * @param parentNid nid of the concept from which to compute
    * descendents.
    * @return {@code BitSet} of the descendents of the {@code parentNid}
    */
   NidSet getDescendentNidSet(int parentNid);

   /**
    * Gets the parent identifiers.
    *
    * @param childNid nid of the concept from which to find parent
    * @return an array of parent nids.
    */
   int[] getParentNids(int childNid);
   
   /**
    * Remove a parent from the tree... May be necessary after detecting a cycle in a 
    * tree to make it processible. 
    * @param childNid
    * @param parentNid 
    */
   void removeParent(int childNid, int parentNid);
   /**
    * Gets the root nids.
    *
    * @return nid identifiers for the root concept[s] of this tree.
    */
   int[] getRootNids();
   
   /**
    * Determine if the childNid is a taxonomic descendent of the parentNid. 
    * @param childNid
    * @param parentNid
    * @return true if the childNid is a descendent of the parentNid by any route. 
    */
   boolean isDescendentOf(int childNid, int parentNid);
   
   /**
    * Determine if the childNid is a taxonomic child of the parentNid. 
    * @param childNid
    * @param parentNid
    * @return true if the childNid is a descendent of the parentNid by any route. 
    */
   boolean isChildOf(int childNid, int parentNid);
}

