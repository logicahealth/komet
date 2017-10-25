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

package sh.isaac.provider.bdb.taxonomy;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.SpinedIntObjectMap;
import sh.isaac.api.tree.hashtree.HashTreeBuilder;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * Stream-based, parallelizable,  collector to create a graph, which represents a
 * particular point in time, and a particular semantic state (stated or inferred)
 * of a taxonomy. The HashTreeBuilder does not require concurrent access, since there is one
 * HashTreeBuilder per thread, and then when the process completes, the HashTreeBuilders are 
 * merged in a single thread. 
 * @author kec
 */
public class GraphCollector
         implements ObjIntConsumer<HashTreeBuilder>, BiConsumer<HashTreeBuilder, HashTreeBuilder> {
   /** The isa concept sequence. */
   private final int ISA_CONCEPT_SEQUENCE = TermAux.IS_A.getConceptSequence();

   /** The watch list. */
   ConceptSequenceSet watchList = new ConceptSequenceSet();

   /** The taxonomy map. */
   final SpinedIntObjectMap<int[]> taxonomyMap;

   /** The taxonomy coordinate. */
   final ManifoldCoordinate manifoldCoordinate;

   /** The taxonomy flags. */
   final int taxonomyFlags;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new graph collector.
    *
    * @param taxonomyMap the taxonomy map
    * @param manifoldCoordinate the view coordinate
    */
   public GraphCollector(SpinedIntObjectMap<int[]> taxonomyMap, ManifoldCoordinate manifoldCoordinate) {
      this.taxonomyMap        = taxonomyMap;
      this.manifoldCoordinate = manifoldCoordinate;
      this.taxonomyFlags      = TaxonomyFlag.getFlagsFromManifoldCoordinate(manifoldCoordinate);

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
      final int[] taxonomyData = this.taxonomyMap.get(originSequence);

      if (taxonomyData != null) {
         TaxonomyRecordPrimitive isaacPrimitiveTaxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);
         // For debugging.
         if (this.watchList.contains(originSequence)) {
            System.out.println("Found watch: " + isaacPrimitiveTaxonomyRecord);
         }

         final TaxonomyRecord taxonomyRecordUnpacked = isaacPrimitiveTaxonomyRecord.getTaxonomyRecordUnpacked();
         final int[] destinationStream =
            taxonomyRecordUnpacked.getConceptSequencesForType(this.ISA_CONCEPT_SEQUENCE,
                                                              this.manifoldCoordinate);

         for (int destinationSequence: destinationStream) {
            graphBuilder.add(destinationSequence, originSequence);
         }
      }
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
      buff.append(TaxonomyFlag.getTaxonomyFlags(this.taxonomyFlags));
      buff.append("}");
      return buff.toString();
   }
}

