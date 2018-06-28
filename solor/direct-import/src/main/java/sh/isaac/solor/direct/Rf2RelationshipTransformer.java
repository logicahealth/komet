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
package sh.isaac.solor.direct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.ModelGet;

/**
 *
 * @author kec
 */
public class Rf2RelationshipTransformer extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

   private static final int WRITE_PERMITS = Runtime.getRuntime().availableProcessors() * 2;
   protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);
   final int transformSize = 10240;
   final IdentifierService identifierService = ModelGet.identifierService();
   private final ImportType importType;

   public Rf2RelationshipTransformer(ImportType importType) {
       this.importType = importType;
      Get.activeTasks().add(this);
      updateTitle("Converting RF2 to EL++ " + importType);
      
   }

   @Override
   protected Void call() throws Exception {
      try {
         setStartTime();
         updateMessage("Computing concept to stated relationship associations...");
         int conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();

         IntObjectHashMap<int[]> conceptNid_StatedRelationshipsNids_Map = setupRelSpinedMap(TermAux.RF2_STATED_RELATIONSHIP_ASSEMBLAGE.getNid(), conceptAssemblageNid);
         addToTotalWork(4);
         completedUnitOfWork();

         List<TransformationGroup> statedTransformList = new ArrayList<>();

         updateMessage("Transforming stated logical definitions...");
         conceptNid_StatedRelationshipsNids_Map.keyValuesView().forEach((IntObjectPair<int[]> pair) -> {
               statedTransformList.add(new TransformationGroup(pair.getOne(), pair.getTwo(), PremiseType.STATED));
               if (statedTransformList.size() == transformSize) {
                  List<TransformationGroup> listForTask = new ArrayList<>(statedTransformList);
                  LogicGraphTransformerAndWriter transformer = new LogicGraphTransformerAndWriter(listForTask, writeSemaphore, this.importType, getStartTime());
                  Get.executor().submit(transformer);
                  statedTransformList.clear();
               }
         });
         // pickup any items remaining in the list. 
         LogicGraphTransformerAndWriter remainingStatedtransformer = new LogicGraphTransformerAndWriter(statedTransformList, writeSemaphore, this.importType, getStartTime());
         Get.executor().submit(remainingStatedtransformer);
         
         
         completedUnitOfWork();

         updateMessage("Transforming inferred logical definitions...");
         updateMessage("Computing concept to inferred relationship associations...");
         IntObjectHashMap<int[]> conceptNid_InferredRelationshipsNids_Map = setupRelSpinedMap(TermAux.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE.getNid(), conceptAssemblageNid);
         completedUnitOfWork();
         List<TransformationGroup> inferredTransformList = new ArrayList<>();

         conceptNid_InferredRelationshipsNids_Map.keyValuesView().forEach((IntObjectPair<int[]> pair) -> {
               inferredTransformList.add(new TransformationGroup(pair.getOne(), pair.getTwo(), PremiseType.INFERRED));
               if (inferredTransformList.size() == transformSize) {
                  List<TransformationGroup> listForTask = new ArrayList<>(inferredTransformList);
                  LogicGraphTransformerAndWriter transformer = new LogicGraphTransformerAndWriter(listForTask, writeSemaphore, this.importType, getStartTime());
                  Get.executor().submit(transformer);
                  inferredTransformList.clear();
               }
         });
         // pickup any items remaining in the list. 
         LogicGraphTransformerAndWriter remainingInferredTransformer = new LogicGraphTransformerAndWriter(inferredTransformList, writeSemaphore, this.importType, getStartTime());
         Get.executor().submit(remainingInferredTransformer);
         
         writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
         completedUnitOfWork();
         updateMessage("Completed transformation");

         return null;
      } finally {
         Get.taxonomyService().notifyTaxonomyListenersToRefresh();
         Get.activeTasks().remove(this);
      }
   }

   private IntObjectHashMap<int[]> setupRelSpinedMap(int relationshipAssemblageNid, int conceptAssemblageNid) {
      IntObjectHashMap<int[]> conceptNid_RelationshipsNids_Map = new IntObjectHashMap<>();
      Get.assemblageService().getSemanticChronologyStream(relationshipAssemblageNid)
              .forEach((semanticChronology) -> {
                 int conceptNid = semanticChronology.getReferencedComponentNid();
                 int conceptAssemblageNidFound = identifierService.getAssemblageNid(conceptNid).getAsInt();
                 if (conceptAssemblageNidFound != conceptAssemblageNid) {
                    throw new IllegalStateException("conceptAssemblageNids do not match: " + conceptAssemblageNidFound + " " + conceptAssemblageNid);
                 }
                 int referencedComponentNid = semanticChronology.getReferencedComponentNid();
                 int[] relNids = conceptNid_RelationshipsNids_Map.get(referencedComponentNid);
                 if (relNids == null) {
                	 conceptNid_RelationshipsNids_Map.put(referencedComponentNid, new int[]{semanticChronology.getNid()});
                 } else {
                    relNids = Arrays.copyOf(relNids, relNids.length + 1);
                    relNids[relNids.length - 1] = semanticChronology.getNid();
                    conceptNid_RelationshipsNids_Map.put(referencedComponentNid, relNids);
                 }
              });
      return conceptNid_RelationshipsNids_Map;
   }

}
