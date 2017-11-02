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
 /*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.model.tree;

//~--- JDK imports ------------------------------------------------------------
import java.util.Arrays;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- classes ----------------------------------------------------------------
/**
 * A {@code Tree} implemented using a {@code OpenIntObjectHashMap<int[]>}.
 *
 * @author kec
 */
public class HashTreeWithBitSets
        extends AbstractHashTree {

   /**
    * The concept sequences with parents.
    */
   final OpenIntHashSet conceptSequencesWithParents;

   /**
    * The concept sequences with children.
    */
   final OpenIntHashSet conceptSequencesWithChildren;

   /**
    * The concept sequences.
    */
   final OpenIntHashSet conceptSequences;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new hash tree with bit sets.
    *
    * @param manifoldCoordinate
    * @param assemblageNid the assemblage nid which specifies the assemblage where the concepts in this tree
    * where created within.
    */
   public HashTreeWithBitSets(ManifoldCoordinate manifoldCoordinate, int assemblageNid) {
      super(manifoldCoordinate, assemblageNid);
      this.conceptSequencesWithParents = new OpenIntHashSet();
      this.conceptSequencesWithChildren = new OpenIntHashSet();
      this.conceptSequences = new OpenIntHashSet();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the children.
    *
    * @param parentSequence the parent sequence
    * @param childSequenceArray the child sequence array
    */
   public void addChildren(int parentSequence, int[] childSequenceArray) {
      this.conceptSequences.add(parentSequence);
      if (childSequenceArray == null) {
         return;
      }
      for (int childSequence: childSequenceArray) {
         this.conceptSequences.add(childSequence);
      }

      if (childSequenceArray.length > 0) {
         if (!this.parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            this.parentSequence_ChildSequenceArray_Map.put(parentSequence, childSequenceArray);
         } else {
            final OpenIntHashSet combinedSet = new OpenIntHashSet();

            Arrays.stream(this.parentSequence_ChildSequenceArray_Map.get(parentSequence))
                    .forEach((sequence) -> combinedSet.add(sequence));
            Arrays.stream(childSequenceArray)
                    .forEach((sequence) -> combinedSet.add(sequence));
            this.parentSequence_ChildSequenceArray_Map.put(parentSequence, combinedSet.keys()
                    .elements());
         }
         for (int childSequence: childSequenceArray) {
            this.conceptSequences.add(childSequence);
         }
         this.conceptSequencesWithChildren.add(parentSequence);
      }
   }

   /**
    * Adds the parents.
    *
    * @param childSequence the child sequence
    * @param parentSequenceArray the parent sequence array
    */
   public void addParents(int childSequence, int[] parentSequenceArray) {
      this.conceptSequences.add(childSequence);
      if (parentSequenceArray.length > 0) {
         if (!this.childSequence_ParentSequenceArray_Map.containsKey(childSequence)) {
            this.childSequence_ParentSequenceArray_Map.put(childSequence, parentSequenceArray);
         } else {
            final OpenIntHashSet combinedSet = new OpenIntHashSet();

            Arrays.stream(this.childSequence_ParentSequenceArray_Map.get(childSequence))
                    .forEach((sequence) -> combinedSet.add(sequence));
            Arrays.stream(parentSequenceArray)
                    .forEach((sequence) -> combinedSet.add(sequence));
            this.childSequence_ParentSequenceArray_Map.put(childSequence, combinedSet.keys()
                    .elements());
         }

         this.childSequence_ParentSequenceArray_Map.put(childSequence, parentSequenceArray);
         for (int parentSequence: parentSequenceArray) {
            this.conceptSequences.add(parentSequence);
         }
         this.conceptSequencesWithParents.add(childSequence);
      }
   }

   /**
    * Concept sequences with children count.
    *
    * @return the int
    */
   public int conceptSequencesWithChildrenCount() {
      return this.conceptSequencesWithChildren.size();
   }

   /**
    * Concept sequences with parents count.
    *
    * @return the int
    */
   public int conceptSequencesWithParentsCount() {
      return this.conceptSequencesWithParents.size();
   }

   /**
    * Size.
    *
    * @return the int
    */
   @Override
   public int size() {
      return getNodeSequences().size() + 1;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the node sequences.
    *
    * @return the node sequences
    */
   public OpenIntHashSet getNodeSequences() {
      return this.conceptSequences;
   }

   /**
    * Gets the root sequences.
    *
    * @return the root sequences
    */
   @Override
   public int[] getRootNids() {
      OpenIntHashSet rootSet = (OpenIntHashSet) this.conceptSequencesWithChildren.clone();
      this.conceptSequencesWithParents.forEachKey((sequence) ->{
         rootSet.remove(sequence);
         return true;
      });
      
      return rootSet.keys().elements();
   }
}
