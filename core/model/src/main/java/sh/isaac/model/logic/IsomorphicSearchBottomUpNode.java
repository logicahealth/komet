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



package sh.isaac.model.logic;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.logic.NodeSemantic;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class IsomorphicSearchBottomUpNode
         implements Comparable<IsomorphicSearchBottomUpNode> {
   final NodeSemantic       nodeSemantic;
   final ConceptSequenceSet conceptsReferencedAtNodeOrAbove;
   int                      conceptsReferencedAtNodeOrAboveHash;
   final int                childNodeId;
   final int                nodeId;
   final int                size;

   //~--- constructors --------------------------------------------------------

   public IsomorphicSearchBottomUpNode(NodeSemantic nodeSemantic,
         OpenIntHashSet conceptsReferencedAtNodeOrAbove,
         int childNodeId,
         int nodeId) {
      this.nodeSemantic                        = nodeSemantic;
      this.conceptsReferencedAtNodeOrAbove     = ConceptSequenceSet.of(conceptsReferencedAtNodeOrAbove);
      this.size                                = conceptsReferencedAtNodeOrAbove.size();
      this.conceptsReferencedAtNodeOrAboveHash = 1;

      for (final int element: conceptsReferencedAtNodeOrAbove.keys()
            .elements()) {
         this.conceptsReferencedAtNodeOrAboveHash = 31 * this.conceptsReferencedAtNodeOrAboveHash + element;
      }

      this.childNodeId = childNodeId;
      this.nodeId      = nodeId;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public int compareTo(IsomorphicSearchBottomUpNode o) {
      int comparison = this.nodeSemantic.compareTo(o.nodeSemantic);

      if (comparison != 0) {
         return comparison;
      }

      comparison = Integer.compare(this.childNodeId, o.childNodeId);

      if (comparison != 0) {
         return comparison;
      }

      comparison = Integer.compare(this.size, o.size);

      if (comparison != 0) {
         return comparison;
      }

      comparison = Integer.compare(this.conceptsReferencedAtNodeOrAboveHash, o.conceptsReferencedAtNodeOrAboveHash);

      if (comparison != 0) {
         return comparison;
      }

      comparison = this.conceptsReferencedAtNodeOrAbove.compareTo(o.conceptsReferencedAtNodeOrAbove);

      if (comparison != 0) {
         return comparison;
      }

      return Integer.compare(this.nodeId, o.nodeId);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      return compareTo((IsomorphicSearchBottomUpNode) obj) == 0;
   }

   @Override
   public int hashCode() {
      return this.conceptsReferencedAtNodeOrAboveHash;
   }

   @Override
   public String toString() {
      return "BottomUpNode{" + this.nodeSemantic + ", conceptsAtOrAbove=" + this.conceptsReferencedAtNodeOrAbove + ", childId=" +
             this.childNodeId + ", nodeId=" + this.nodeId + '}';
   }
}

