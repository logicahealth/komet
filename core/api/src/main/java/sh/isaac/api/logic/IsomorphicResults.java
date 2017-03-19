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

import java.util.stream.Stream;

//~--- interfaces -------------------------------------------------------------

/**
 * Computed results of an isomorphic comparison of two expressions: the
 * reference expression and the comparison expression.
 * @author kec
 */
public interface IsomorphicResults {
   
   /**
    * Gets the added relationship roots.
    *
    * @return roots for connected nodes that comprise is-a, typed relationships, or relationship groups that are
    *  in the referenceExpression, but not in the comparisonExpression.
    */
   Stream<LogicNode> getAddedRelationshipRoots();

   /**
    * Gets the additional node roots.
    *
    * @return roots for connected nodes that are in the reference expression, but not in the
    * common expression.
    */
   Stream<LogicNode> getAdditionalNodeRoots();

   /**
    * Gets the comparison expression.
    *
    * @return the expression that is compared to the reference expression to compute
    * isomorphic results.
    */
   LogicalExpression getComparisonExpression();

   /**
    * Gets the deleted node roots.
    *
    * @return roots for connected nodes that are in the comparison expression, but are not in
    * the common expression.
    */
   Stream<LogicNode> getDeletedNodeRoots();

   /**
    * Gets the deleted relationship roots.
    *
    * @return roots for connected nodes that comprise is-a, typed relationships, or relationship groups that are
    * in the comparisonExpression, but not in the referenceExpression.
    */
   Stream<LogicNode> getDeletedRelationshipRoots();

   /**
    * Gets the isomorphic expression.
    *
    * @return an expression containing only the connected set of nodes representing
    *  the maximal common isomorphism between the two expressions that are connected
    *  to their respective roots.
    */
   LogicalExpression getIsomorphicExpression();

   /**
    *  
    *   @return an expression containing a merger of all the nodes in the reference and comparison expression.
    */
   LogicalExpression getMergedExpression();

   /**
    * Gets the reference expression.
    *
    * @return the expression that isomorphic results are computed with respect to.
    */
   LogicalExpression getReferenceExpression();

   /**
    * Gets the shared relationship roots.
    *
    * @return roots for connected nodes that comprise is-a, typed relationships, or relationship groups that are
    *  in both the referenceExpression and in the comparisonExpression.
    */
   Stream<LogicNode> getSharedRelationshipRoots();
}

