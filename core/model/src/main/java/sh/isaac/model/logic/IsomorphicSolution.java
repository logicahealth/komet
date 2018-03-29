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

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.tree.TreeNodeVisitData;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 7/19/15.
 */
public class IsomorphicSolution
         implements Comparable<IsomorphicSolution> {
   /** The score. */
   int score = -1;

   /** The legal. */
   boolean legal = true;

   /** The hashcode. */
   final int hashcode;

   /** The solution. */
   final int[] solution;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new isomorphic solution.
    *
    * @param solution the solution
    * @param referenceTreeVisitData the reference tree visit data
    * @param comparisonTreeVisitData the comparison tree visit data
    */
   public IsomorphicSolution(int[] solution,
                             TreeNodeVisitData referenceTreeVisitData,
                             TreeNodeVisitData comparisonTreeVisitData) {
      this.solution = solution;
      this.hashcode = Arrays.hashCode(solution);
      score(referenceTreeVisitData, comparisonTreeVisitData);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   @Override
   public int compareTo(IsomorphicSolution o) {
      int comparison = Integer.compare(this.score, o.score);

      if (comparison != 0) {
         return comparison;
      }

      comparison = Integer.compare(this.hashcode, o.hashcode);

      if (comparison != 0) {
         return comparison;
      }

      return compare(this.solution, o.solution);
   }

   /**
    * Equals.
    *
    * @param o the o
    * @return true, if successful
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      final IsomorphicSolution that = (IsomorphicSolution) o;

      if (this.hashcode != that.hashcode) {
         return false;
      }

      if (this.score != that.score) {
         return false;
      }

      return Arrays.equals(this.solution, that.solution);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return this.hashcode;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "solution{" + this.legal + "  s:" + this.score + ", " + Arrays.toString(this.solution) + '}';
   }

   /**
    * Compare.
    *
    * @param o1 the o 1
    * @param o2 the o 2
    * @return the int
    */
   int compare(int[] o1, int[] o2) {
      for (int i = 0; i < o1.length; i++) {
         if (o1[i] != o2[i]) {
            return (o1[i] < o2[i]) ? -1
                                   : ((o1[i] == o2[i]) ? 0
                  : 1);
         }
      }

      return 0;
   }

   /**
    * Score.
    *
    * @param referenceTreeVisitData the reference tree visit data
    * @param comparisonTreeVisitData the comparison tree visit data
    */
   final void score(TreeNodeVisitData referenceTreeVisitData, TreeNodeVisitData comparisonTreeVisitData) {
      final OpenIntHashSet                       parentNodeIds = new OpenIntHashSet(this.solution.length);
      final OpenIntHashSet                       usedNodeIds = new OpenIntHashSet(this.solution.length);
      final OpenIntObjectHashMap<OpenIntHashSet> siblingGroupToNodeSequenceMap = new OpenIntObjectHashMap<>();
      int                                        sum                           = 0;

      // give a bonus point ever time a common parent is used in the solution.
      int bonus = 0;

      for (int i = 0; i < this.solution.length; i++) {
         if (this.solution[i] >= 0) {
            sum++;

            if (usedNodeIds.contains(this.solution[i])) {
               this.legal = false;
               this.score = -1;
               return;
            } else {
               usedNodeIds.add(this.solution[i]);
            }

            final Optional<Integer>      referenceParentNodeId = referenceTreeVisitData.getPredecessorNid(i);
            final int      siblingGroup          = referenceTreeVisitData.getSiblingGroupForNid(i);
            OpenIntHashSet nodesInSiblingGroup   = siblingGroupToNodeSequenceMap.get(siblingGroup);

            if (nodesInSiblingGroup == null) {
               nodesInSiblingGroup = new OpenIntHashSet();
               siblingGroupToNodeSequenceMap.put(siblingGroup, nodesInSiblingGroup);
            }

            nodesInSiblingGroup.add(i);

            if (referenceParentNodeId.isPresent()) {
               if (parentNodeIds.contains(referenceParentNodeId.get())) {
                  bonus++;
               } else {
                  parentNodeIds.add(referenceParentNodeId.get());
               }
            }
         }
      }

      // For all logicNodes corresponding to a sibling group in the reference expression, the logicNodes in the
      // comparison expression must all be in the same sibling group in the comparison expression
      for (final int siblingGroup: siblingGroupToNodeSequenceMap.keys()
            .elements()) {
         final OpenIntHashSet groupMembers           = siblingGroupToNodeSequenceMap.get(siblingGroup);
         int                  comparisonSiblingGroup = -1;

         for (final int groupMember: groupMembers.keys()
               .elements()) {
            if (comparisonSiblingGroup == -1) {
               comparisonSiblingGroup = comparisonTreeVisitData.getSiblingGroupForNid(this.solution[groupMember]);
            } else {
               if (comparisonSiblingGroup !=
                     comparisonTreeVisitData.getSiblingGroupForNid(this.solution[groupMember])) {
                  this.legal = false;
                  this.score = -2;
                  return;
               }
            }
         }
      }

      this.score = sum + bonus;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if legal.
    *
    * @return true, if legal
    */
   public boolean isLegal() {
      return this.legal;
   }

   /**
    * Gets the score.
    *
    * @return the score
    */
   public int getScore() {
      return this.score;
   }

   /**
    * Gets the solution.
    *
    * @return the solution
    */
   public int[] getSolution() {
      return this.solution;
   }
}

