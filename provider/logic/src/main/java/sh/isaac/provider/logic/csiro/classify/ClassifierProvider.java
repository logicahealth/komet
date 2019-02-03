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

import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.task.TimedTask;
import sh.isaac.model.configuration.ManifoldCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.taxonomy.GraphCollector;
import sh.isaac.model.tree.HashTreeBuilder;
import sh.isaac.model.tree.HashTreeWithIntArraySets;
import sh.isaac.provider.logic.csiro.classify.tasks.AggregateClassifyTask;

/**
 * The Class ClassifierProvider.
 *
 * @author kec
 */
public class ClassifierProvider
        implements ClassifierService {

   /**
    * The stamp coordinate.
    */
   StampCoordinate stampCoordinate;

   /**
    * The logic coordinate.
    */
   LogicCoordinate logicCoordinate;

   /**
    * The edit coordinate.
    */
   EditCoordinate editCoordinate;

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
      this.editCoordinate = editCoordinate;
   }

   @Override
   public TimedTask<ClassifierResults> classify() {
      return AggregateClassifyTask.get(this.stampCoordinate, this.logicCoordinate, false);
   }
   
   @Override
   public TimedTask<ClassifierResults> classify(boolean cycleCheck) {
      return AggregateClassifyTask.get(this.stampCoordinate, this.logicCoordinate, cycleCheck);
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the concept nid for expression.
    *
    * @param expression the expression
    * @param editCoordinate the edit coordinate
    * @return the concept nid for expression
    */
   @Override
   public TimedTask<Integer> getConceptNidForExpression(LogicalExpression expression, EditCoordinate editCoordinate) {
      return GetConceptNidForExpressionTask.create(expression, this, editCoordinate);
   }

   /**
    * Gets the inferred taxonomy graph.
    *
    * @return the inferred taxonomy graph
    */
   protected HashTreeWithIntArraySets getInferredTaxonomyGraph() {
      final IntStream conceptSequenceStream = Get.conceptService().getConceptNidStream(TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid()).parallel();
      final ManifoldCoordinate manifoldCoordinate = ManifoldCoordinates.getInferredManifoldCoordinate(
              StampCoordinates.getDevelopmentLatestActiveOnly(),
              Get.configurationService().getUserConfiguration(Optional.empty()).getLanguageCoordinate());
      IntFunction<int[]> taxonomyDataProvider = new IntFunction<int[]>() {
          final int assemblageNid = manifoldCoordinate.getConceptAssemblageNid();
          @Override
          public int[] apply(int conceptNid) {
              return Get.taxonomyService().getTaxonomyData(assemblageNid, conceptNid);
          }
      };
      final GraphCollector collector
              = new GraphCollector(taxonomyDataProvider, manifoldCoordinate);
      final HashTreeBuilder graphBuilder = conceptSequenceStream.collect(()
              -> new HashTreeBuilder(manifoldCoordinate, this.logicCoordinate.getConceptAssemblageNid()),
              collector,
              collector);
      final HashTreeWithIntArraySets resultGraph = graphBuilder.getSimpleDirectedGraph();

      return resultGraph;
   }

   /**
    * Gets the stated taxonomy graph.
    *
    * @return the stated taxonomy graph
    */
   protected HashTreeWithIntArraySets getStatedTaxonomyGraph() {
      final IntStream conceptSequenceStream = Get.conceptService().getConceptNidStream(TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid()).parallel();
      final ManifoldCoordinate manifoldCoordinate = ManifoldCoordinates.getStatedManifoldCoordinate(
              StampCoordinates.getDevelopmentLatestActiveOnly(),
              Get.configurationService().getUserConfiguration(Optional.empty()).getLanguageCoordinate());
      IntFunction<int[]> taxonomyDataProvider = new IntFunction<int[]>() {
          final int assemblageNid = manifoldCoordinate.getConceptAssemblageNid();
          @Override
          public int[] apply(int conceptNid) {
              return Get.taxonomyService().getTaxonomyData(assemblageNid, conceptNid);
          }
      };
      final GraphCollector collector
              = new GraphCollector(taxonomyDataProvider, manifoldCoordinate);
      final HashTreeBuilder graphBuilder = conceptSequenceStream.collect(() 
              -> new HashTreeBuilder(manifoldCoordinate, this.logicCoordinate.getConceptAssemblageNid()),
              collector,
              collector);
      final HashTreeWithIntArraySets resultGraph = graphBuilder.getSimpleDirectedGraph();

      return resultGraph;
   }

   @Override
   public String toString() {
      return "ClassifierProvider stamp: {" + stampCoordinate.toString() + "} logicCoord: {" + logicCoordinate.toString() + "} editCoord: {" 
            + editCoordinate.toString() + "}";
   }
}
