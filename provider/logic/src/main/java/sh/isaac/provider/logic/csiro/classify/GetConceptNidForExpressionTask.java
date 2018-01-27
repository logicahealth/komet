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



package sh.isaac.provider.logic.csiro.classify;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import java.util.concurrent.ExecutionException;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import sh.isaac.MetaData;
import sh.isaac.api.DataSource;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.util.WorkExecutors;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.api.component.semantic.SemanticSnapshotService;

//~--- classes ----------------------------------------------------------------

/**
 * The Class GetConceptNidForExpressionTask.
 *
 * @author kec
 */
public class GetConceptNidForExpressionTask
        extends Task<Integer> {
   /** The expression. */
   LogicalExpression expression;

   /** The classifier provider. */
   ClassifierProvider classifierProvider;

   /** The stamp coordinate. */
   StampCoordinate stampCoordinate;

   /** The logic coordinate. */
   LogicCoordinate logicCoordinate;

   /** The stated edit coordinate. */
   EditCoordinate statedEditCoordinate;

   //~--- constructors --------------------------------------------------------

   //TODO should this take in an assemblageID?
   /**
    * Instantiates a new gets the concept nid for expression task.
    *
    * @param expression the expression
    * @param classifierProvider the classifier provider
    * @param statedEditCoordinate the stated edit coordinate
    */
   private GetConceptNidForExpressionTask(LogicalExpression expression,
         ClassifierProvider classifierProvider,
         EditCoordinate statedEditCoordinate) {
      this.expression           = expression;
      this.classifierProvider   = classifierProvider;
      this.stampCoordinate      = classifierProvider.stampCoordinate;
      this.logicCoordinate      = classifierProvider.logicCoordinate;
      this.statedEditCoordinate = statedEditCoordinate;
      updateTitle("Get ID for Expression");
      updateProgress(-1, Integer.MAX_VALUE);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Creates the.
    *
    * @param expression the expression
    * @param classifierProvider the classifier provider
    * @param statedEditCoordinate the stated edit coordinate
    * @return the gets the concept sequence for expression task
    */
   public static GetConceptNidForExpressionTask create(LogicalExpression expression,
         ClassifierProvider classifierProvider,
         EditCoordinate statedEditCoordinate) {
      final GetConceptNidForExpressionTask task = new GetConceptNidForExpressionTask(
                                                           expression,
                                                                 classifierProvider,
                                                                 statedEditCoordinate);

      LookupService.getService(ActiveTasks.class)
                   .get()
                   .add(task);
      LookupService.getService(WorkExecutors.class)
                   .getExecutor()
                   .execute(task);
      return task;
   }

   /**
    * Call.
    *
    * @return the integer
    * @throws Exception the exception
    */
   @Override
   protected Integer call()
            throws Exception {
      try {
         final SemanticSnapshotService<LogicGraphVersionImpl> sememeSnapshot = Get.assemblageService()
                                                                               .getSnapshot(LogicGraphVersionImpl.class,
                                                                                           this.stampCoordinate);

         updateMessage("Searching existing definitions...");

         final LatestVersion<LogicGraphVersionImpl> match = sememeSnapshot.getLatestSemanticVersionsFromAssemblage(
                                                               this.logicCoordinate.getStatedAssemblageNid())
                                                                         .filterVersion((LatestVersion<LogicGraphVersionImpl> t) -> {
                  final LogicGraphVersionImpl lgs = t.get();
                  final LogicalExpressionImpl existingGraph = new LogicalExpressionImpl(
                                                                  lgs.getGraphData(),
                                                                        DataSource.INTERNAL);

                  updateMessage("found existing definition");
                  return existingGraph.equals(this.expression);
               })
                                                                         .findFirstVersion();

         if (match.isPresent()) {
            return match.get().getReferencedComponentNid();
         }

         updateMessage("Building new concept...");

         final UUID                  uuidForNewConcept     = UUID.randomUUID();
         final ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);

         conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE____SOLOR);
         conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT____SOLOR);
         conceptBuilderService.setDefaultLogicCoordinate(this.logicCoordinate);

         final ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(
                                            uuidForNewConcept.toString(),
                                            "expression",
                                            this.expression,
                                            MetaData.SOLOR_CONCEPT_ASSEMBLAGE____SOLOR.getNid());
         final ConceptChronology concept = builder.build(this.statedEditCoordinate, ChangeCheckerMode.INACTIVE)
                                                  .get();

         updateMessage("Commiting new expression...");

         try {
            Get.commitService()
               .commit(Get.configurationService().getDefaultEditCoordinate(), "Expression commit.")
               .get();
            updateMessage("Classifying new concept...");
            this.classifierProvider.classify()
                                   .get();
         } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
         }

         return concept.getNid();
      } finally {
         updateProgress(-1, Integer.MAX_VALUE);
         LookupService.getService(ActiveTasks.class)
                      .get()
                      .remove(this);
      }
   }
}

