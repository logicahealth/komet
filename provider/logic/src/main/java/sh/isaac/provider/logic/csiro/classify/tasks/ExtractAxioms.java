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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.provider.logic.csiro.classify.ClassifierData;



/**
 * The Class ExtractAxioms.
 *
 * @author kec
 */
public class ExtractAxioms
        extends TimedTaskWithProgressTracker<ClassifierData> {

   private static ConceptProxy problemConcept = new ConceptProxy("[#] Abortions",
           UUID.fromString("f3de7f8d-53c4-58a7-8013-febda45ae73d"));

   StampCoordinate stampCoordinate;

   LogicCoordinate logicCoordinate;

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

   @Override
   protected ClassifierData call()
            throws Exception {
      Get.activeTasks().add(this);
       try {
           final AtomicInteger logicGraphMembers = new AtomicInteger();
           final ClassifierData cd = ClassifierData.get(this.stampCoordinate, this.logicCoordinate);
           
           if (cd.isIncrementalAllowed()) {
               // axioms are already extracted.
           } else {
               cd.clearAxioms();
               processAllStatedAxioms(this.stampCoordinate, this.logicCoordinate, cd, logicGraphMembers);
           }
           
           return cd;
       } finally {
           Get.activeTasks().remove(this);
       }
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
      final SemanticSnapshotService<LogicGraphVersionImpl> semanticSnapshot = Get.assemblageService()
                                                                            .getSnapshot(LogicGraphVersionImpl.class,
                                                                                  stampCoordinate);

      AtomicInteger inactiveConcepts = new AtomicInteger(0);

      semanticSnapshot.getLatestSemanticVersionsFromAssemblage(logicCoordinate.getStatedAssemblageNid(), this)
                    .forEach((LatestVersion<LogicGraphVersionImpl> latest) -> {
                                final LogicGraphVersionImpl lgs = latest.get();
                                final int conceptNid = lgs.getReferencedComponentNid();

                                if (conceptNid == problemConcept.getNid()) {
                                    boolean active = Get.conceptService()
                                            .isConceptActive(conceptNid, stampCoordinate);
                                    if (active) {
                                        LOG.info("Found ACTIVE problem concept: " + problemConcept);
                                    } else {
                                        LOG.info("Found INACTIVE problem concept: " + problemConcept);
                                    }

                                }

                                if (Get.conceptService()
                                       .isConceptActive(conceptNid, stampCoordinate)) {
                                   cd.translate(lgs);
                                   logicGraphMembers.incrementAndGet();
                                } else {
                                    inactiveConcepts.incrementAndGet();
                                }
                             });

       StringBuilder sb = new StringBuilder();
       sb.append("Extracted ");
       sb.append(logicGraphMembers);
       sb.append(" active (");
       sb.append(inactiveConcepts.get());
       sb.append(" inactive) logical definitions from: ");
       sb.append(Get.conceptDescriptionText(logicCoordinate.getStatedAssemblageNid()));

       LOG.info(sb);
   }
}