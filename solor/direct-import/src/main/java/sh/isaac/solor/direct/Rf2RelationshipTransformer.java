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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.ModelGet;

/**
 *
 * @author kec
 */
public class Rf2RelationshipTransformer extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

   private static final int WRITE_PERMITS = Runtime.getRuntime().availableProcessors() * 2;
   // TODO consider replacing readSemaphore with TaskCountManager
   protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);
   final int transformSize = 10240;
   final IdentifierService identifierService = ModelGet.identifierService();
   private final ImportType importType;
   private Transaction transaction;

   /**
    * @param transaction - if provided, does NOT commit the transaction.  If not provided, creates and commits its own transaction.
    * @param importType
    */
   public Rf2RelationshipTransformer(Transaction transaction, ImportType importType) {
      this.importType = importType;
      this.transaction = transaction;
      Get.activeTasks().add(this);
      updateTitle("Converting RF2 to EL++ " + importType);
      
   }

   @Override
   protected Void call() throws Exception {
      try {
         setStartTime();
         updateMessage("Computing concept to stated relationship associations...");
         int conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();
         int statedRelationshipAssemblageNid = TermAux.RF2_STATED_RELATIONSHIP_ASSEMBLAGE.getNid();

         addToTotalWork(4);
         completedUnitOfWork();

         List<TransformationGroup> statedTransformList = new ArrayList<>();
         
         boolean commit = (transaction == null);
         if (commit) {
             transaction = Get.commitService().newTransaction(Optional.of("stated definitions transform"), ChangeCheckerMode.INACTIVE, false);
         }
         
         updateMessage("Transforming stated rf2 relationships...");
         Get.conceptService().getConceptNidStream(conceptAssemblageNid, false).forEach((conceptNid) -> {
             ImmutableIntSet relNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(conceptNid, statedRelationshipAssemblageNid);
               statedTransformList.add(new TransformationGroup(conceptNid, relNids.toArray(), PremiseType.STATED));
               if (statedTransformList.size() == transformSize) {
                  List<TransformationGroup> listForTask = new ArrayList<>(statedTransformList);
                  LogicGraphTransformerAndWriter transformer = new LogicGraphTransformerAndWriter(transaction, listForTask, writeSemaphore, this.importType, getStartTime());
                  Get.executor().submit(transformer);
                  statedTransformList.clear();
               }
         });
         // pickup any items remaining in the list. 
         LogicGraphTransformerAndWriter remainingStatedtransformer = new LogicGraphTransformerAndWriter(transaction, statedTransformList, writeSemaphore, 
                this.importType, getStartTime());
         Get.executor().submit(remainingStatedtransformer);
         
         completedUnitOfWork();

         updateMessage("Transforming inferred logical definitions...");
         updateMessage("Computing concept to inferred relationship associations...");
         int inferredRelationshipAssemblageNid = TermAux.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE.getNid();
         completedUnitOfWork();
         List<TransformationGroup> inferredTransformList = new ArrayList<>();
         
         Get.conceptService().getConceptNidStream(conceptAssemblageNid, false).forEach((conceptNid) -> {
             ImmutableIntSet relNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(conceptNid, inferredRelationshipAssemblageNid);
               inferredTransformList.add(new TransformationGroup(conceptNid, relNids.toArray(), PremiseType.INFERRED));
               if (inferredTransformList.size() == transformSize) {
                  List<TransformationGroup> listForTask = new ArrayList<>(inferredTransformList);
                  LogicGraphTransformerAndWriter transformer = new LogicGraphTransformerAndWriter(transaction, listForTask, writeSemaphore, this.importType, getStartTime());
                  Get.executor().submit(transformer);
                  inferredTransformList.clear();
               }
         });
         // pickup any items remaining in the list. 
         LogicGraphTransformerAndWriter remainingInferredTransformer = new LogicGraphTransformerAndWriter(transaction, inferredTransformList, writeSemaphore, 
                 this.importType, getStartTime());
         Get.executor().submit(remainingInferredTransformer);
         
         writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
         if (commit) {
             transaction.commit().get();
         }
         completedUnitOfWork();
         updateMessage("Completed transformation");

         return null;
      } finally {
         Get.taxonomyService().notifyTaxonomyListenersToRefresh();
         Get.activeTasks().remove(this);
      }
   }
}
