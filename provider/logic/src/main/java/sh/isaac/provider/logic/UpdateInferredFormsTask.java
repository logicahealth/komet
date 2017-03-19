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



package sh.isaac.provider.logic;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.atomic.AtomicInteger;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import au.csiro.ontology.Ontology;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.progress.ActiveTasks;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class UpdateInferredFormsTask
        extends Task<Integer> {
   AtomicInteger     processedCount = new AtomicInteger();
   ClassifierResults classifierResults;
   Ontology          classifiedModel;
   LogicCoordinate   logicCoordinate;
   StampCoordinate   stampCoordinate;
   int               conceptsToProcess;

   //~--- constructors --------------------------------------------------------

   private UpdateInferredFormsTask(ClassifierResults classifierResults,
                                   Ontology classifiedModel,
                                   LogicCoordinate logicCoordinate,
                                   StampCoordinate stampCoordinate) {
      this.classifierResults = classifierResults;
      this.classifiedModel   = classifiedModel;
      this.logicCoordinate   = logicCoordinate;
      this.stampCoordinate   = stampCoordinate;
      conceptsToProcess      = classifierResults.getAffectedConcepts()
            .size();
      updateProgress(-1, conceptsToProcess);  // Indeterminate progress
      updateValue(0);                         // no concepts processed
      updateTitle("Updating inferred taxonomy and forms ");
   }

   //~--- methods -------------------------------------------------------------

   public static UpdateInferredFormsTask create(ClassifierResults classifierResults,
         Ontology classifiedModel,
         LogicCoordinate logicCoordinate,
         StampCoordinate stampCoordinate) {
      UpdateInferredFormsTask task = new UpdateInferredFormsTask(classifierResults,
                                                                 classifiedModel,
                                                                 logicCoordinate,
                                                                 stampCoordinate);

      LookupService.getService(ActiveTasks.class)
                   .get()
                   .add(task);
      return task;
   }

   @Override
   protected Integer call()
            throws Exception {
      try {
         SememeSnapshotService<LogicGraphSememe> sememeSnapshot = Get.sememeService()
                                                                     .getSnapshot(LogicGraphSememe.class,
                                                                           stampCoordinate);

         classifierResults.getAffectedConcepts().stream().parallel().forEach((conceptSequence) -> {
                                      if (processedCount.incrementAndGet() % 10 == 0) {
                                         updateProgress(processedCount.get(), conceptsToProcess);

                                         ConceptChronology concept = Get.conceptService()
                                                                        .getConcept(conceptSequence);

                                         updateMessage("Updating concept: " + concept.toUserString());
                                         updateValue(processedCount.get());
                                         sememeSnapshot.getLatestSememeVersionsForComponentFromAssemblage(
                                             conceptSequence,
                                             logicCoordinate.getInferredAssemblageSequence()).forEach((LatestVersion<LogicGraphSememe> latestLogicGraph) -> {
                        processLogicGraphSememe(latestLogicGraph);
                     });
                                      }
                                   });
      } finally {
         LookupService.getService(ActiveTasks.class)
                      .get()
                      .remove(this);
      }

      throw new UnsupportedOperationException("Not supported yet.");
   }

   private void processLogicGraphSememe(LatestVersion<LogicGraphSememe> latest) {}
}

