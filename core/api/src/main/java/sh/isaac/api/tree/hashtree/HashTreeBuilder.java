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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.OpenIntObjectHashMap;

//~--- classes ----------------------------------------------------------------

/**
 * The Class HashTreeBuilder.
 *
 * @author kec
 */
public class HashTreeBuilder {
   
   /** The Constant builderCount. */
   private static final AtomicInteger builderCount = new AtomicInteger();

   //~--- fields --------------------------------------------------------------

   /** The child sequence parent sequence stream map. */
   final OpenIntObjectHashMap<IntStream.Builder> childSequence_ParentSequenceStream_Map = new OpenIntObjectHashMap<>();
   
   /** The parent sequence child sequence stream map. */
   final OpenIntObjectHashMap<IntStream.Builder> parentSequence_ChildSequenceStream_Map = new OpenIntObjectHashMap<>();
   
   /** The builder id. */
   final int                                     builderId;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new hash tree builder.
    */
   public HashTreeBuilder() {
      this.builderId = builderCount.getAndIncrement();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the.
    *
    * @param parent the parent
    * @param child the child
    */
   public void add(int parent, int child) {
      if (!this.childSequence_ParentSequenceStream_Map.containsKey(child)) {
         this.childSequence_ParentSequenceStream_Map.put(child, IntStream.builder());
      }

      this.childSequence_ParentSequenceStream_Map.get(child)
            .add(parent);

      if (!this.parentSequence_ChildSequenceStream_Map.containsKey(parent)) {
         this.parentSequence_ChildSequenceStream_Map.put(parent, IntStream.builder());
      }

      this.parentSequence_ChildSequenceStream_Map.get(parent)
            .add(child);
   }

   /**
    * Combine.
    *
    * @param another the another
    */
   public void combine(HashTreeBuilder another) {
      another.childSequence_ParentSequenceStream_Map.forEachPair((int childSequence,
            IntStream.Builder parentsFromAnother) -> {
               if (this.childSequence_ParentSequenceStream_Map.containsKey(childSequence)) {
                  final IntStream.Builder parentsStream = this.childSequence_ParentSequenceStream_Map.get(childSequence);

                  parentsFromAnother.build()
                                    .forEach((int c) -> parentsStream.add(c));
               } else {
                  this.childSequence_ParentSequenceStream_Map.put(childSequence, parentsFromAnother);
               }

               return true;
            });
      another.parentSequence_ChildSequenceStream_Map.forEachPair((int parentSequence,
            IntStream.Builder childrenFromAnother) -> {
               if (this.parentSequence_ChildSequenceStream_Map.containsKey(parentSequence)) {
                  final IntStream.Builder childrenStream = this.parentSequence_ChildSequenceStream_Map.get(parentSequence);

                  childrenFromAnother.build()
                                     .forEach((int p) -> childrenStream.add(p));
               } else {
                  this.parentSequence_ChildSequenceStream_Map.put(parentSequence, childrenFromAnother);
               }

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
      final HashTreeWithBitSets graph = new HashTreeWithBitSets(this.childSequence_ParentSequenceStream_Map.size());

      this.childSequence_ParentSequenceStream_Map.forEachPair((int childSequence,
            IntStream.Builder parentSequenceStreamBuilder) -> {
               final int[] parentSequenceArray = parentSequenceStreamBuilder.build()
                                                                      .distinct()
                                                                      .toArray();

               if (parentSequenceArray.length > 0) {
                  graph.addParents(childSequence, parentSequenceArray);
               }

               return true;
            });
      this.parentSequence_ChildSequenceStream_Map.forEachPair((int parentSequence,
            IntStream.Builder childSequenceStreamBuilder) -> {
               final int[] childSequenceArray = childSequenceStreamBuilder.build()
                                                                    .distinct()
                                                                    .toArray();

               if (childSequenceArray.length > 0) {
                  graph.addChildren(parentSequence, childSequenceArray);
               }

               return true;
            });
      return graph;
   }
}

