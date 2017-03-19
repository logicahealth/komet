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
package sh.isaac.provider.taxonomy.graph;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.api.tree.hashtree.HashTreeBuilder;
import sh.isaac.model.waitfree.CasSequenceObjectMap;
import sh.isaac.provider.taxonomy.TaxonomyFlags;
import sh.isaac.provider.taxonomy.TaxonomyRecordPrimitive;
import sh.isaac.provider.taxonomy.TaxonomyRecordUnpacked;

//~--- classes ----------------------------------------------------------------

/**
 * Stream-based, parallelizable,  collector to create a graph, which represents a
 * particular point in time, and a particular semantic state (stated or inferred)
 * of a taxonomy.
 * @author kec
 */
public class GraphCollector
         implements ObjIntConsumer<HashTreeBuilder>, BiConsumer<HashTreeBuilder, HashTreeBuilder> {
   /** The isa concept sequence. */
   private final int ISA_CONCEPT_SEQUENCE = TermAux.IS_A.getConceptSequence();

   /** The origin sequence being processed. */
   int originSequenceBeingProcessed = -1;

   /** The watch list. */
   ConceptSequenceSet watchList = new ConceptSequenceSet();

   /** The taxonomy map. */
   final CasSequenceObjectMap<TaxonomyRecordPrimitive> taxonomyMap;

   /** The taxonomy coordinate. */
   final TaxonomyCoordinate taxonomyCoordinate;

   /** The taxonomy flags. */
   final int taxonomyFlags;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new graph collector.
    *
    * @param taxonomyMap the taxonomy map
    * @param viewCoordinate the view coordinate
    */
   public GraphCollector(CasSequenceObjectMap<TaxonomyRecordPrimitive> taxonomyMap, TaxonomyCoordinate viewCoordinate) {
      this.taxonomyMap        = taxonomyMap;
      this.taxonomyCoordinate = viewCoordinate;
      this.taxonomyFlags      = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(viewCoordinate);

//    addToWatchList("779ece66-7e95-323e-a261-214caf48c408");
//    addToWatchList("778a75c9-8264-36aa-9ad6-b9c6e5ee9187");
//    addToWatchList("c377a425-6ac0-3574-9110-b17deb9d49ff");
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Accept.
    *
    * @param t the t
    * @param u the u
    */
   @Override
   public void accept(HashTreeBuilder t, HashTreeBuilder u) {
      t.combine(u);
   }

   /**
    * Accept.
    *
    * @param graphBuilder the graph builder
    * @param originSequence the origin sequence
    */
   @Override
   public void accept(HashTreeBuilder graphBuilder, int originSequence) {
      this.originSequenceBeingProcessed = originSequence;

      final Optional<TaxonomyRecordPrimitive> isaacPrimitiveTaxonomyRecord = this.taxonomyMap.get(originSequence);

      if (isaacPrimitiveTaxonomyRecord.isPresent()) {
         // For debugging.
         if (this.watchList.contains(originSequence)) {
            System.out.println("Found watch: " + isaacPrimitiveTaxonomyRecord);
         }

         final TaxonomyRecordUnpacked taxonomyRecordUnpacked = isaacPrimitiveTaxonomyRecord.get()
                                                                                           .getTaxonomyRecordUnpacked();
         final IntStream destinationStream =
            taxonomyRecordUnpacked.getConceptSequencesForType(this.ISA_CONCEPT_SEQUENCE,
                                                              this.taxonomyCoordinate);

         destinationStream.forEach((int destinationSequence) -> graphBuilder.add(destinationSequence, originSequence));
      }

      this.originSequenceBeingProcessed = -1;
   }

   /**
    * Adds the to watch list.
    *
    * @param uuid the uuid
    * @throws RuntimeException the runtime exception
    */
   public final void addToWatchList(String uuid)
            throws RuntimeException {
      this.watchList.add(Get.identifierService()
                            .getConceptSequenceForUuids(UUID.fromString(uuid)));
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder buff = new StringBuilder();

      buff.append("GraphCollector{");
      buff.append(TaxonomyFlags.getTaxonomyFlags(this.taxonomyFlags));

      if (this.originSequenceBeingProcessed != -1) {
         buff.append("} processing: ");
         buff.append(Get.conceptDescriptionText(this.originSequenceBeingProcessed));
         buff.append(" <");
         buff.append(this.originSequenceBeingProcessed);
         buff.append(">");
      } else {
         buff.append("}");
      }

      return buff.toString();
   }
}

