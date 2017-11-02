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
import sh.isaac.api.logic.LogicNode;

//~--- classes ----------------------------------------------------------------

/**
 * The Class RelationshipKey.
 *
 * @author kec
 */
public class RelationshipKey
         implements Comparable<RelationshipKey> {
   /** The concepts referenced at node or below. */
   OpenIntHashSet conceptsReferencedAtNodeOrBelow = new OpenIntHashSet();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new relationship key.
    *
    * @param nodeId the node id
    * @param expression the expression
    */
   public RelationshipKey(int nodeId, LogicalExpressionImpl expression) {
      addNodes(nodeId, expression);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   @Override
   public int compareTo(RelationshipKey o) {
      int comparison = Integer.compare(this.conceptsReferencedAtNodeOrBelow.size(), o.conceptsReferencedAtNodeOrBelow.size());

      if (comparison != 0) {
         return comparison;
      }

      final int[] thisKeys  = this.conceptsReferencedAtNodeOrBelow.keys().elements();
      final int[] otherKeys = o.conceptsReferencedAtNodeOrBelow.keys().elements();

      for (int i = 0; i < thisKeys.length; i++) {
         if (thisKeys[i] != otherKeys[i]) {
            return Integer.compare(thisKeys[i], otherKeys[i]);
         }
      }

      return 0;

   }

   /**
    * Adds the nodes.
    *
    * @param nodeId the node id
    * @param expression the expression
    */
   private void addNodes(int nodeId, LogicalExpressionImpl expression) {
      final LogicNode logicNode = expression.getNode(nodeId);

      expression.getNode(nodeId)
                .addConceptsReferencedByNode(this.conceptsReferencedAtNodeOrBelow);
      logicNode.getChildStream()
               .forEach((childNode) -> addNodes(childNode.getNodeIndex(), expression));
   }
}

