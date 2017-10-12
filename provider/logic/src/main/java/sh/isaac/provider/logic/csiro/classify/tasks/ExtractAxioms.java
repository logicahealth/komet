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



package sh.isaac.provider.logic.csiro.classify.tasks;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.atomic.AtomicInteger;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.provider.logic.csiro.classify.ClassifierData;
import sh.isaac.api.component.semantic.SemanticSnapshotService;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ExtractAxioms.
 *
 * @author kec
 */
public class ExtractAxioms
        extends TimedTaskWithProgressTracker<Void> {
   /** The stamp coordinate. */
   StampCoordinate stampCoordinate;

   /** The logic coordinate. */
   LogicCoordinate logicCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new extract axioms.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    */
   public ExtractAxioms(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate) {
      this.stampCoordinate = stampCoordinate;
      this.logicCoordinate = logicCoordinate;
      updateTitle("Extract axioms");
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Call.
    *
    * @return the void
    * @throws Exception the exception
    */
   @Override
   protected Void call()
            throws Exception {
      final AtomicInteger  logicGraphMembers = new AtomicInteger();
      final ClassifierData cd                = ClassifierData.get(this.stampCoordinate, this.logicCoordinate);

      if (cd.isIncrementalAllowed()) {
         // axioms are already extracted.
      } else {
         cd.clearAxioms();
         processAllStatedAxioms(this.stampCoordinate, this.logicCoordinate, cd, logicGraphMembers);
      }

      return null;
   }

   /**
    * Process all stated axioms.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    * @param cd the cd
    * @param logicGraphMembers the logic graph members
    */
   protected void processAllStatedAxioms(StampCoordinate stampCoordinate,
         LogicCoordinate logicCoordinate,
         ClassifierData cd,
         AtomicInteger logicGraphMembers) {
      final SemanticSnapshotService<LogicGraphVersionImpl> sememeSnapshot = Get.assemblageService()
                                                                            .getSnapshot(LogicGraphVersionImpl.class,
                                                                                  stampCoordinate);

      sememeSnapshot.getLatestSemanticVersionsFromAssemblage(logicCoordinate.getStatedAssemblageSequence(), this)
                    .forEach((LatestVersion<LogicGraphVersionImpl> latest) -> {
                                final LogicGraphVersionImpl lgs = latest.get();
                                final int conceptSequence = Get.identifierService()
                                                               .getConceptSequence(lgs.getReferencedComponentNid());

                                if (Get.conceptService()
                                       .isConceptActive(conceptSequence, stampCoordinate)) {
                                   cd.translate(lgs);
                                   logicGraphMembers.incrementAndGet();
                                }
                             });
   }
}

