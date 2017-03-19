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

import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.tree.hashtree.HashTreeBuilder;
import sh.isaac.api.tree.hashtree.HashTreeWithBitSets;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.configuration.TaxonomyCoordinates;
import sh.isaac.provider.logic.csiro.classify.tasks.AggregateClassifyTask;
import sh.isaac.provider.taxonomy.TaxonomyProvider;
import sh.isaac.provider.taxonomy.graph.GraphCollector;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ClassifierProvider.
 *
 * @author kec
 */
public class ClassifierProvider
         implements ClassifierService {
   
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The stamp coordinate. */
   StampCoordinate stampCoordinate;
   
   /** The logic coordinate. */
   LogicCoordinate logicCoordinate;
   
   /** The edit coordinate. */
   EditCoordinate  editCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new classifier provider.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    * @param editCoordinate the edit coordinate
    */
   public ClassifierProvider(StampCoordinate stampCoordinate,
                             LogicCoordinate logicCoordinate,
                             EditCoordinate editCoordinate) {
      this.stampCoordinate = stampCoordinate;
      this.logicCoordinate = logicCoordinate;
      this.editCoordinate  = editCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Classify.
    *
    * @return the task
    */
   @Override
   public Task<ClassifierResults> classify() {
      return AggregateClassifyTask.get(this.stampCoordinate, this.logicCoordinate);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept sequence for expression.
    *
    * @param expression the expression
    * @param editCoordinate the edit coordinate
    * @return the concept sequence for expression
    */
   @Override
   public Task<Integer> getConceptSequenceForExpression(LogicalExpression expression, EditCoordinate editCoordinate) {
      return GetConceptSequenceForExpressionTask.create(expression, this, editCoordinate);
   }

   /**
    * Gets the inferred taxonomy graph.
    *
    * @return the inferred taxonomy graph
    */
   protected HashTreeWithBitSets getInferredTaxonomyGraph() {
      final IntStream conceptSequenceStream = Get.identifierService()
                                           .getParallelConceptSequenceStream();
      final GraphCollector collector =
         new GraphCollector(((TaxonomyProvider) Get.taxonomyService()).getOriginDestinationTaxonomyRecords(),
                            TaxonomyCoordinates.getInferredTaxonomyCoordinate(
                                StampCoordinates.getDevelopmentLatestActiveOnly(),
                                Get.configurationService().getDefaultLanguageCoordinate()));
      final HashTreeBuilder     graphBuilder = conceptSequenceStream.collect(HashTreeBuilder::new, collector, collector);
      final HashTreeWithBitSets resultGraph  = graphBuilder.getSimpleDirectedGraphGraph();

      return resultGraph;
   }

   /**
    * Gets the stated taxonomy graph.
    *
    * @return the stated taxonomy graph
    */
   protected HashTreeWithBitSets getStatedTaxonomyGraph() {
      final IntStream conceptSequenceStream = Get.identifierService()
                                           .getParallelConceptSequenceStream();
      final GraphCollector collector =
         new GraphCollector(((TaxonomyProvider) Get.taxonomyService()).getOriginDestinationTaxonomyRecords(),
                            TaxonomyCoordinates.getStatedTaxonomyCoordinate(
                                StampCoordinates.getDevelopmentLatestActiveOnly(),
                                Get.configurationService().getDefaultLanguageCoordinate()));
      final HashTreeBuilder     graphBuilder = conceptSequenceStream.collect(HashTreeBuilder::new, collector, collector);
      final HashTreeWithBitSets resultGraph  = graphBuilder.getSimpleDirectedGraphGraph();

      return resultGraph;
   }
}

