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
package sh.isaac.provider.taxonomy.walk;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.provider.taxonomy.TaxonomyFlags;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class TaxonomyWalkCollector
         implements ObjIntConsumer<TaxonomyWalkAccumulator>,
                    BiConsumer<TaxonomyWalkAccumulator, TaxonomyWalkAccumulator> {
   private static final int MAX_PRINT_COUNT = 10;

   //~--- fields --------------------------------------------------------------

   final OpenIntHashSet     watchSequences = new OpenIntHashSet();
   int                      errorCount     = 0;
   int                      printCount     = 0;
   final TaxonomyCoordinate taxonomyCoordinate;
   final int                taxonomyFlags;

   //~--- constructors --------------------------------------------------------

   public TaxonomyWalkCollector(TaxonomyCoordinate taxonomyCoordinate) {
      this.taxonomyCoordinate = taxonomyCoordinate;
      taxonomyFlags           = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(taxonomyCoordinate);

      int watchNid = Get.identifierService()
                        .getNidForUuids(UUID.fromString("df79ab93-4436-35b8-be3f-2a8e5849d732"));

      watchSequences.add(Get.identifierService()
                            .getConceptSequence(watchNid));
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void accept(TaxonomyWalkAccumulator accumulator, int conceptSequence) {
      if (watchSequences.contains(conceptSequence)) {
         accumulator.watchConcept = Get.conceptService()
                                       .getConcept(conceptSequence);
      } else {
         accumulator.watchConcept = null;
      }

      if (Get.conceptService()
             .isConceptActive(conceptSequence, taxonomyCoordinate.getStampCoordinate())) {
         IntStream parentSequences = Get.taxonomyService()
                                        .getTaxonomyParentSequences(conceptSequence, taxonomyCoordinate);
         int parentCount = (int) parentSequences.count();

         if (parentCount == 0) {
            ConceptChronology<?> c = Get.conceptService()
                                        .getConcept(conceptSequence);

            if (printCount < MAX_PRINT_COUNT) {
               printCount++;
               LogManager.getLogger()
                         .warn("No parents for: " + c.toUserString());
            }
         }

         accumulator.parentConnections += parentCount;
      }
   }

   @Override
   public void accept(TaxonomyWalkAccumulator t, TaxonomyWalkAccumulator u) {
      t.combine(u);
   }
}

