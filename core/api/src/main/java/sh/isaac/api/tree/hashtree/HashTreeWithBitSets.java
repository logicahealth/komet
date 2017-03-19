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
package sh.isaac.api.tree.hashtree;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.SequenceSet;

//~--- classes ----------------------------------------------------------------

/**
 * A {@code Tree} implemented using a {@code OpenIntObjectHashMap<int[]>}.
 *
 * @author kec
 */
public class HashTreeWithBitSets
        extends AbstractHashTree {
   final ConceptSequenceSet conceptSequencesWithParents;
   final ConceptSequenceSet conceptSequencesWithChildren;
   final ConceptSequenceSet conceptSequences;

   //~--- constructors --------------------------------------------------------

   public HashTreeWithBitSets() {
      conceptSequencesWithParents  = new ConceptSequenceSet();
      conceptSequencesWithChildren = new ConceptSequenceSet();
      conceptSequences             = new ConceptSequenceSet();
   }

   public HashTreeWithBitSets(int initialSize) {
      conceptSequencesWithParents  = new ConceptSequenceSet();
      conceptSequencesWithChildren = new ConceptSequenceSet();
      conceptSequences             = new ConceptSequenceSet();
   }

   //~--- methods -------------------------------------------------------------

   public void addChildren(int parentSequence, int[] childSequenceArray) {
      maxSequence = Math.max(parentSequence, maxSequence);

      if (childSequenceArray.length > 0) {
         if (!parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            parentSequence_ChildSequenceArray_Map.put(parentSequence, childSequenceArray);
         } else {
            OpenIntHashSet combinedSet = new OpenIntHashSet();

            Arrays.stream(parentSequence_ChildSequenceArray_Map.get(parentSequence))
                  .forEach((sequence) -> combinedSet.add(sequence));
            Arrays.stream(childSequenceArray)
                  .forEach((sequence) -> combinedSet.add(sequence));
            parentSequence_ChildSequenceArray_Map.put(parentSequence, combinedSet.keys()
                  .elements());
         }

         IntStream.of(childSequenceArray).forEach((int sequence) -> {
                              conceptSequences.add(sequence);
                           });
         maxSequence = Math.max(IntStream.of(childSequenceArray)
                                         .max()
                                         .getAsInt(), maxSequence);
         conceptSequencesWithChildren.add(parentSequence);
      }
   }

   public void addParents(int childSequence, int[] parentSequenceArray) {
      maxSequence = Math.max(childSequence, maxSequence);

      if (parentSequenceArray.length > 0) {
         if (!childSequence_ParentSequenceArray_Map.containsKey(childSequence)) {
            childSequence_ParentSequenceArray_Map.put(childSequence, parentSequenceArray);
         } else {
            OpenIntHashSet combinedSet = new OpenIntHashSet();

            Arrays.stream(childSequence_ParentSequenceArray_Map.get(childSequence))
                  .forEach((sequence) -> combinedSet.add(sequence));
            Arrays.stream(parentSequenceArray)
                  .forEach((sequence) -> combinedSet.add(sequence));
            childSequence_ParentSequenceArray_Map.put(childSequence, combinedSet.keys()
                  .elements());
         }

         childSequence_ParentSequenceArray_Map.put(childSequence, parentSequenceArray);
         IntStream.of(parentSequenceArray).forEach((int sequence) -> {
                              conceptSequences.add(sequence);
                           });
         maxSequence = Math.max(IntStream.of(parentSequenceArray)
                                         .max()
                                         .getAsInt(), maxSequence);
         conceptSequencesWithParents.add(childSequence);
      }
   }

   public int conceptSequencesWithChildrenCount() {
      return conceptSequencesWithChildren.size();
   }

   public int conceptSequencesWithParentsCount() {
      return conceptSequencesWithParents.size();
   }

   @Override
   public int size() {
      return getNodeSequences().size() + 1;
   }

   //~--- get methods ---------------------------------------------------------

   public IntStream getLeafSequences() {
      SequenceSet leavesSet = new SequenceSet<>();

      leavesSet.or(conceptSequencesWithParents);
      leavesSet.andNot(conceptSequencesWithChildren);
      return leavesSet.stream();
   }

   public int getMaxSequence() {
      return maxSequence;
   }

   public SequenceSet<?> getNodeSequences() {
      return conceptSequences;
   }

   @Override
   public IntStream getRootSequenceStream() {
      SequenceSet rootSet = new SequenceSet<>();

      rootSet.or(conceptSequencesWithChildren);
      rootSet.andNot(conceptSequencesWithParents);
      return rootSet.stream();
   }

   @Override
   public int[] getRootSequences() {
      return getRootSequenceStream().toArray();
   }
}

