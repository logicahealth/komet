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
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.provider.taxonomy.TaxonomyFlag;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TaxonomyWalkCollector.
 *
 * @author kec
 */
public class TaxonomyWalkCollector
         implements ObjIntConsumer<TaxonomyWalkAccumulator>,
                    BiConsumer<TaxonomyWalkAccumulator, TaxonomyWalkAccumulator> {
   /** The Constant MAX_PRINT_COUNT. */
   private static final int MAX_PRINT_COUNT = 10;

   //~--- fields --------------------------------------------------------------

   /** The watch sequences. */
   final OpenIntHashSet watchSequences = new OpenIntHashSet();

   /** The error count. */
   int errorCount = 0;

   /** The print count. */
   int printCount = 0;

   /** The manifold coordinate. */
   final ManifoldCoordinate manifoldCoordinate;

   /** The taxonomy flags. */
   final int taxonomyFlags;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new taxonomy walk collector.
    *
    * @param manifoldCoordinate the taxonomy coordinate
    */
   public TaxonomyWalkCollector(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
      this.taxonomyFlags      = TaxonomyFlag.getFlagsFromManifoldCoordinate(manifoldCoordinate);

      final int watchNid = Get.identifierService()
                              .getNidForUuids(UUID.fromString("df79ab93-4436-35b8-be3f-2a8e5849d732"));

      this.watchSequences.add(Get.identifierService()
                                 .getConceptSequence(watchNid));
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Accept.
    *
    * @param accumulator the accumulator
    * @param conceptSequence the concept sequence
    */
   @Override
   public void accept(TaxonomyWalkAccumulator accumulator, int conceptSequence) {
      if (this.watchSequences.contains(conceptSequence)) {
         accumulator.watchConcept = Get.conceptService()
                                       .getConceptChronology(conceptSequence);
      } else {
         accumulator.watchConcept = null;
      }

      if (Get.conceptService()
             .isConceptActive(conceptSequence, this.manifoldCoordinate.getStampCoordinate())) {
         try {
            final int[] parentSequences = Get.taxonomyService().getSnapshot(manifoldCoordinate).get()
                    
                    .getTaxonomyParentSequences(conceptSequence);
            final int parentCount = parentSequences.length;
            
            if (parentCount == 0) {
               final ConceptChronology c = Get.conceptService()
                       .getConceptChronology(conceptSequence);
               
               if (this.printCount < MAX_PRINT_COUNT) {
                  this.printCount++;
                  LogManager.getLogger()
                          .warn("No parents for: " + c.toUserString());
               }
            }
            
            accumulator.parentConnections += parentCount;
         } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException();
         }
      }
   }

   /**
    * Accept.
    *
    * @param t the t
    * @param u the u
    */
   @Override
   public void accept(TaxonomyWalkAccumulator t, TaxonomyWalkAccumulator u) {
      t.combine(u);
   }
}

