/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.solor.rf2.direct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.ContainerSequenceService;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.SpinedIntIntArrayMap;

/**
 *
 * @author kec
 */
public class Rf2RelationshipTransformer extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

   protected static final Logger LOG = LogManager.getLogger();
   private static final int WRITE_PERMITS = Runtime.getRuntime().availableProcessors() * 2;
   protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);
   final int transformSize = 10240;
   final ContainerSequenceService containerService = ModelGet.identifierService();

   public Rf2RelationshipTransformer() {
      Get.activeTasks().add(this);
      updateTitle("Converting RF2 to EL++");
   }

   @Override
   protected Void call() throws Exception {
      try {
         updateMessage("Computing concept to stated relationship associations...");
         int conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();

         SpinedIntIntArrayMap conceptElementSequence_StatedRelationshipNids_Map = setupRelSpinedMap(TermAux.RF2_STATED_RELATIONSHIP_ASSEMBLAGE.getNid(), conceptAssemblageNid);
         addToTotalWork(4);
         completedUnitOfWork();

         List<TransformationGroup> statedTransformList = new ArrayList<>();

         updateMessage("Transforming stated logical definitions...");
         conceptElementSequence_StatedRelationshipNids_Map.forEach((int conceptElementSequence, int[] value) -> {
            int conceptNid = containerService.getNidForElementSequence(conceptElementSequence, conceptAssemblageNid);
            if (conceptNid < 0) {
               statedTransformList.add(new TransformationGroup(conceptNid, value, PremiseType.STATED));
               if (statedTransformList.size() == transformSize) {
                  List<TransformationGroup> listForTask = new ArrayList<>(statedTransformList);
                  LogicGraphTransformerAndWriter transformer = new LogicGraphTransformerAndWriter(listForTask, writeSemaphore);
                  Get.executor().submit(transformer);
                  statedTransformList.clear();
               }
            } else {
               throw new IllegalStateException("Concept nid >= 0: " + conceptNid);
            }
         });
         // pickup any items remaining in the list. 
         LogicGraphTransformerAndWriter remainingStatedtransformer = new LogicGraphTransformerAndWriter(statedTransformList, writeSemaphore);
         Get.executor().submit(remainingStatedtransformer);
         
         
         completedUnitOfWork();

         updateMessage("Transforming inferred logical definitions...");
         updateMessage("Computing concept to inferred relationship associations...");
         SpinedIntIntArrayMap conceptElementSequence_InferredRelationshipNids_Map = setupRelSpinedMap(TermAux.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE.getNid(), conceptAssemblageNid);
         completedUnitOfWork();
         List<TransformationGroup> inferredTransformList = new ArrayList<>();

         conceptElementSequence_InferredRelationshipNids_Map.forEach((int conceptElementSequence, int[] value) -> {
            int conceptNid = containerService.getNidForElementSequence(conceptElementSequence, conceptAssemblageNid);
            if (conceptNid < 0) {
               inferredTransformList.add(new TransformationGroup(conceptNid, value, PremiseType.INFERRED));
               if (inferredTransformList.size() == transformSize) {
                  List<TransformationGroup> listForTask = new ArrayList<>(inferredTransformList);
                  LogicGraphTransformerAndWriter transformer = new LogicGraphTransformerAndWriter(listForTask, writeSemaphore);
                  Get.executor().submit(transformer);
                  inferredTransformList.clear();
               }
            } else {
               throw new IllegalStateException("Concept nid >= 0: " + conceptNid);
            }
         });
         // pickup any items remaining in the list. 
         LogicGraphTransformerAndWriter remainingInferredTransformer = new LogicGraphTransformerAndWriter(inferredTransformList, writeSemaphore);
         Get.executor().submit(remainingInferredTransformer);
         
         writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
         completedUnitOfWork();
         updateMessage("Completed transformation");

         return null;
      } finally {
         Get.activeTasks().remove(this);
      }
   }

   private SpinedIntIntArrayMap setupRelSpinedMap(int relationshipAssemblageNid, int conceptAssemblageNid) {
      SpinedIntIntArrayMap conceptElementSequence_RelationshipNids_Map = new SpinedIntIntArrayMap();
      Get.assemblageService().getSemanticChronologyStream(relationshipAssemblageNid)
              .forEach((semanticChronology) -> {
                 int conceptNid = semanticChronology.getReferencedComponentNid();
                 int conceptAssemblageNidFound = containerService.getAssemblageNidForNid(conceptNid);
                 if (conceptAssemblageNidFound != conceptAssemblageNid) {
                    throw new IllegalStateException("conceptAssemblageNids do not match: " + conceptAssemblageNidFound + " " + conceptAssemblageNid);
                 }
                 int referencedComponentNid = semanticChronology.getReferencedComponentNid();
                 int[] relNids = conceptElementSequence_RelationshipNids_Map.get(referencedComponentNid);
                 if (relNids == null) {
                    conceptElementSequence_RelationshipNids_Map.put(referencedComponentNid, new int[]{semanticChronology.getNid()});
                 } else {
                    relNids = Arrays.copyOf(relNids, relNids.length + 1);
                    relNids[relNids.length - 1] = semanticChronology.getNid();
                    conceptElementSequence_RelationshipNids_Map.put(referencedComponentNid, relNids);
                 }
              });
      return conceptElementSequence_RelationshipNids_Map;
   }

}
