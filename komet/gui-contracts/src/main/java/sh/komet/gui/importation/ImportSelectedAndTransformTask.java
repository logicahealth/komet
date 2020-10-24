/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.importation;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.solor.ContentProvider;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.LoincExpressionToConcept;
import sh.isaac.solor.direct.LoincExpressionToNavConcepts;
import sh.isaac.solor.direct.Rf2RelationshipTransformer;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public class ImportSelectedAndTransformTask extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {
   
   final ViewProperties viewProperties;
   final ImportType importType;
   final List<ContentProvider> entriesToImport;
   private final Transaction transaction;

   public ImportSelectedAndTransformTask(Transaction transaction, ViewProperties viewProperties, ImportType importType,
                                         List<ContentProvider> entriesToImport) {
      this.transaction = transaction;
      this.entriesToImport = entriesToImport;
      this.viewProperties = viewProperties;
      this.importType = importType;
      updateTitle("Import and transform " + importType.toString());
      
      addToTotalWork(6);
      Get.activeTasks().add(this);
   }
   
   @Override
   protected Void call() throws Exception {
      try {
         Transaction transaction = Get.commitService().newTransaction(Optional.of("ImportSelectedAndTransformTask"), ChangeCheckerMode.INACTIVE, false);
         completedUnitOfWork();
         updateMessage("Importing new content...");
         DirectImporter importer = new DirectImporter(transaction, importType, entriesToImport);
         Future<?> importTask = Get.executor().submit(importer);
         importTask.get();
         completedUnitOfWork();

         updateMessage("Transforming to SOLOR...");
         Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(transaction, importType);
         Future<?> transformTask = Get.executor().submit(transformer);
         transformTask.get();
         completedUnitOfWork();

         updateMessage("Convert LOINC expressions...");
         LoincExpressionToConcept convertLoinc = new LoincExpressionToConcept(transaction);
         Future<?> convertLoincTask = Get.executor().submit(convertLoinc);
         convertLoincTask.get();
         completedUnitOfWork();

         updateMessage("Adding navigation concepts...");
         LoincExpressionToNavConcepts addNavigationConcepts = new LoincExpressionToNavConcepts(transaction, viewProperties.getManifoldCoordinate());
         Future<?> addNavigationConceptsTask = Get.executor().submit(addNavigationConcepts);
         addNavigationConceptsTask.get();
         completedUnitOfWork();

         updateMessage("Classifying new content...");
         ClassifierService classifierService = Get.logicService().getClassifierService(viewProperties.getManifoldCoordinate().toManifoldCoordinateImmutable());
         Future<?> classifyTask = classifierService.classify();
         classifyTask.get();
         completedUnitOfWork();
                  
         return null;
      } finally {
         this.done();
         Get.taxonomyService().notifyTaxonomyListenersToRefresh();
         Get.activeTasks().remove(this);
      }
   }
   
}
