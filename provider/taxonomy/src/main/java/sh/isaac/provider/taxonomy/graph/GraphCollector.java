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
   private final int                                   ISA_CONCEPT_SEQUENCE         = TermAux.IS_A.getConceptSequence();
   int                                                 originSequenceBeingProcessed = -1;
   ConceptSequenceSet                                  watchList                    = new ConceptSequenceSet();
   final CasSequenceObjectMap<TaxonomyRecordPrimitive> taxonomyMap;
   final TaxonomyCoordinate                            taxonomyCoordinate;
   final int                                           taxonomyFlags;

   //~--- constructors --------------------------------------------------------

   public GraphCollector(CasSequenceObjectMap<TaxonomyRecordPrimitive> taxonomyMap, TaxonomyCoordinate viewCoordinate) {
      this.taxonomyMap        = taxonomyMap;
      this.taxonomyCoordinate = viewCoordinate;
      taxonomyFlags           = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(viewCoordinate);

//    addToWatchList("779ece66-7e95-323e-a261-214caf48c408");
//    addToWatchList("778a75c9-8264-36aa-9ad6-b9c6e5ee9187");
//    addToWatchList("c377a425-6ac0-3574-9110-b17deb9d49ff");
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void accept(HashTreeBuilder t, HashTreeBuilder u) {
      t.combine(u);
   }

   @Override
   public void accept(HashTreeBuilder graphBuilder, int originSequence) {
      originSequenceBeingProcessed = originSequence;

      Optional<TaxonomyRecordPrimitive> isaacPrimitiveTaxonomyRecord = taxonomyMap.get(originSequence);

      if (isaacPrimitiveTaxonomyRecord.isPresent()) {
         // For debugging.
         if (watchList.contains(originSequence)) {
            System.out.println("Found watch: " + isaacPrimitiveTaxonomyRecord);
         }

         TaxonomyRecordUnpacked taxonomyRecordUnpacked = isaacPrimitiveTaxonomyRecord.get()
                                                                                     .getTaxonomyRecordUnpacked();
         IntStream destinationStream = taxonomyRecordUnpacked.getConceptSequencesForType(ISA_CONCEPT_SEQUENCE,
                                                                                         taxonomyCoordinate);

         destinationStream.forEach((int destinationSequence) -> graphBuilder.add(destinationSequence, originSequence));
      }

      originSequenceBeingProcessed = -1;
   }

   public final void addToWatchList(String uuid)
            throws RuntimeException {
      watchList.add(Get.identifierService()
                       .getConceptSequenceForUuids(UUID.fromString(uuid)));
   }

   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append("GraphCollector{");
      buff.append(TaxonomyFlags.getTaxonomyFlags(taxonomyFlags));

      if (originSequenceBeingProcessed != -1) {
         buff.append("} processing: ");
         buff.append(Get.conceptDescriptionText(originSequenceBeingProcessed));
         buff.append(" <");
         buff.append(originSequenceBeingProcessed);
         buff.append(">");
      } else {
         buff.append("}");
      }

      return buff.toString();
   }
}

