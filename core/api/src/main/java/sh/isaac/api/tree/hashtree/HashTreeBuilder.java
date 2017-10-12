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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mahout.math.list.IntArrayList;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.set.OpenIntHashSet;
import sh.isaac.api.Get;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.tree.TreeNodeVisitData;
import static sh.isaac.api.tree.hashtree.AbstractHashTree.MULTI_PARENT_SETS;

//~--- classes ----------------------------------------------------------------
/**
 * The Class HashTreeBuilder.
 *
 * @author kec
 */
public class HashTreeBuilder {

   
   /**
    * The Constant builderCount.
    */
   private static final AtomicInteger builderCount = new AtomicInteger();

   //~--- fields --------------------------------------------------------------
   /**
    * The child sequence parent sequence stream map.
    */
   final OpenIntObjectHashMap<OpenIntHashSet> childSequence_ParentSequenceSet_Map = new OpenIntObjectHashMap<>();

   /**
    * The parent sequence child sequence stream map.
    */
   final OpenIntObjectHashMap<OpenIntHashSet> parentSequence_ChildSequenceSet_Map = new OpenIntObjectHashMap<>();

   protected final ManifoldCoordinate manifoldCoordinate;

   /**
    * The concept sequences with parents.
    */
   final OpenIntHashSet conceptSequencesWithParents = new OpenIntHashSet();

   /**
    * The concept sequences with children.
    */
   final OpenIntHashSet conceptSequencesWithChildren = new OpenIntHashSet();

   /**
    * The concept sequences.
    */
   final OpenIntHashSet conceptSequences = new OpenIntHashSet();

   /**
    * The builder id.
    */
   final int builderId;

   String[] watchUuids = new String[]{"598edc74-ae81-3f3e-bc26-69a1d48d8203",
      "65dabbf3-3f57-3989-ab0c-f8824e0e0aa2", "fcea27cf-fb3c-3d49-8b68-6a416b3d237d"};
   IntArrayList watchSequences = new IntArrayList();

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new hash tree builder.
    *
    * @param manifoldCoordinate
    */
   public HashTreeBuilder(ManifoldCoordinate manifoldCoordinate) {
      this.builderId = builderCount.getAndIncrement();
      this.manifoldCoordinate = manifoldCoordinate;
      for (String uuidStr : watchUuids) {
         watchSequences.add(Get.identifierService().getConceptSequenceForUuids(UUID.fromString(uuidStr)));
      }
    }

   //~--- methods -------------------------------------------------------------
   /**
    * Adds the.
    *
    * @param parent the parent
    * @param child the child
    */
   public void add(int parent, int child) {
      conceptSequences.add(parent);
      conceptSequences.add(child);
      conceptSequencesWithParents.add(child);
      conceptSequencesWithChildren.add(parent);

      if (!this.childSequence_ParentSequenceSet_Map.containsKey(child)) {
         OpenIntHashSet intSet = new OpenIntHashSet();
         intSet.add(parent);
         this.childSequence_ParentSequenceSet_Map.put(child, intSet);
      } else {
         this.childSequence_ParentSequenceSet_Map.get(child)
                 .add(parent);
      }

      if (!this.parentSequence_ChildSequenceSet_Map.containsKey(parent)) {
         OpenIntHashSet intSet = new OpenIntHashSet();
         intSet.add(child);
         this.parentSequence_ChildSequenceSet_Map.put(parent, intSet);
      } else {
         this.parentSequence_ChildSequenceSet_Map.get(parent)
                 .add(child);
      }
   }

   /**
    * Combine.
    *
    * @param another the another
    */
   public void combine(HashTreeBuilder another) {
      addToOne(this.conceptSequences, another.conceptSequences);
      addToOne(this.conceptSequencesWithChildren, another.conceptSequencesWithChildren);
      addToOne(this.conceptSequencesWithParents, another.conceptSequencesWithParents);
      another.childSequence_ParentSequenceSet_Map.forEachPair((int childSequence,
              OpenIntHashSet parentsFromAnother) -> {
         if (this.childSequence_ParentSequenceSet_Map.containsKey(childSequence)) {
            addToOne(this.childSequence_ParentSequenceSet_Map.get(childSequence), parentsFromAnother);
         } else {
            this.childSequence_ParentSequenceSet_Map.put(childSequence, parentsFromAnother);
         }
         return true;
      });
      another.parentSequence_ChildSequenceSet_Map.forEachPair((int parentSequence,
              OpenIntHashSet childrenFromAnother) -> {
         if (this.parentSequence_ChildSequenceSet_Map.containsKey(parentSequence)) {
            addToOne(this.parentSequence_ChildSequenceSet_Map.get(parentSequence), childrenFromAnother);
         } else {
            this.parentSequence_ChildSequenceSet_Map.put(parentSequence, childrenFromAnother);
         }
         return true;
      });
   }
   
   private void addToOne(OpenIntHashSet one, OpenIntHashSet another) {
      another.forEachKey((sequence) -> {
         one.add(sequence);
         return true;
      });
   }
   private void removeFromOne(OpenIntHashSet one, OpenIntHashSet another) {
      another.forEachKey((sequence) -> {
         one.remove(sequence);
         return true;
      });
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the simple directed graph graph.
    *
    * @return the simple directed graph graph
    */
   public HashTreeWithBitSets getSimpleDirectedGraphGraph() {
      OpenIntHashSet roots = (OpenIntHashSet) conceptSequences.clone();
      removeFromOne(roots, conceptSequencesWithParents);
      if (roots.size() == 1) {
         int rootSequence = roots.keys().get(0);
 
         final HashTreeWithBitSets graph = new HashTreeWithBitSets(this.childSequence_ParentSequenceSet_Map.size(),
                 manifoldCoordinate);

         this.childSequence_ParentSequenceSet_Map.forEachPair((int childSequence,
                 OpenIntHashSet parentSequenceSet) -> {
            if (!parentSequenceSet.isEmpty()) {
               graph.addParents(childSequence, setToSortedArray(parentSequenceSet));
            }
            return true;
         });
         this.parentSequence_ChildSequenceSet_Map.forEachPair((int parentSequence,
                 OpenIntHashSet childSequenceSet) -> {
            if (!childSequenceSet.isEmpty()) {
               graph.addChildren(parentSequence, setToSortedArray(childSequenceSet));
            }
            return true;
         });

         TreeNodeVisitData visitData = graph.depthFirstProcess(rootSequence, (TreeNodeVisitData t, int thisSequence) -> {
            if (watchSequences.contains(thisSequence)) {
               printWatch(thisSequence, "dfs: ");
            }
         });

         System.out.println("Nodes visited: " + visitData.getNodesVisited());
         for (int sequence: watchSequences.toList()) {
            OpenIntHashSet multiParents = visitData.getUserNodeSet(MULTI_PARENT_SETS, sequence);
            System.out.println(Get.conceptDescriptionText(sequence) + " multiParentSet: " + multiParents);
         }
         
         return graph;
      } else {
         final StringBuilder builder = new StringBuilder("Too many roots: \n");
         for (int sequence: roots.keys().elements()) {
            builder.append(sequence).append(": ").append(Get.conceptDescriptionText(sequence)).append("\n");
            printWatch(sequence, "root: ");
         }
         // try again
         
      OpenIntHashSet roots2 = (OpenIntHashSet) conceptSequences.clone();
      removeFromOne(roots2, conceptSequencesWithParents);
          builder.append(" second try: ").append(roots2);
         throw new UnsupportedOperationException(builder.toString());
      }

   }
   
   private int[] setToSortedArray(OpenIntHashSet set) {
      IntArrayList list = set.keys();
      list.sort();
      return list.elements();
   }


   private void printWatch(int thisSequence, String prefix) {
      System.out.println("\n" + prefix + " watch: " + Get.conceptDescriptionText(thisSequence));
      childSequence_ParentSequenceSet_Map.forEachPair((sequence, bitMap) -> {
         if (bitMap.contains(thisSequence)) {
            System.out.println(prefix + Get.conceptDescriptionText(thisSequence) + " found in parent set of: " + sequence + " " + Get.conceptDescriptionText(sequence));
         }
         return true; //To change body of generated lambdas, choose Tools | Templates.
      });
      parentSequence_ChildSequenceSet_Map.forEachPair((sequence, bitMap) -> {
         if (bitMap.contains(thisSequence)) {
            System.out.println(prefix + Get.conceptDescriptionText(thisSequence) + " found in child set of: " + sequence + " " + Get.conceptDescriptionText(sequence));
         }
         return true; //To change body of generated lambdas, choose Tools | Templates.
      });
      System.out.println();
   }
}
