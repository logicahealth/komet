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



package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------


//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.tree.Tree;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface TaxonomySnapshotService.
 *
 * @author kec
 */
public interface TaxonomySnapshotService {

   /**
    * 
    * @param conceptNid concept to test if it is a leaf node
    * @return true if the node is a leaf (it has no children)
    */
   boolean isLeaf(int conceptNid);
   
   /**
    * Checks if child of.
    *
    * @param childConceptNid the child id
    * @param parentConceptNid the parent id
    * @return true, if child of
    */
   boolean isChildOf(int childConceptNid, int parentConceptNid);

   /**
    * Checks if kind of.
    *
    * @param childConceptNid the child id
    * @param parentConceptNid the parent id
    * @return true, if kind of
    */
   boolean isKindOf(int childConceptNid, int parentConceptNid);

   /**
    * Gets the kind of sequence set.
    *
    * @param rootConceptNid the root id
    * @return the kind of sequence set
    */
   NidSet getKindOfConceptNidSet(int rootConceptNid);

   /**
    * Gets the roots.
    *
    * @return the roo concept nids
    */
   int[] getRoots();

   /**
    * Gets the taxonomy child sequences.
    *
    * @param parentConceptNid the parent id
    * @return the taxonomy child concept nids
    */
   int[] getTaxonomyChildConceptNids(int parentConceptNid);

   /**
    * Gets the taxonomy parent sequences.
    *
    * @param childConceptNids the child id
    * @return the taxonomy parent sequences
    */
   int[] getTaxonomyParentConceptNids(int childConceptNids);

   /**
    * Gets the taxonomy tree.
    *
    * @return the taxonomy tree
    */
   Tree getTaxonomyTree();
 
   /**
    * Get the ManifoldCoordinate which defines the parent/child relationships of this tree.
    * @return ManifoldCoordinate
    */
   ManifoldCoordinate getManifoldCoordinate();
   
}

