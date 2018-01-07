/*
 * Copyright 2017 ISAAC's KOMET Collaborators.
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
package sh.komet.fx.stage;

import java.util.concurrent.Future;
import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.direct.ImportType;
import sh.isaac.solor.rf2.direct.Rf2DirectImporter;
import sh.isaac.solor.rf2.direct.Rf2RelationshipTransformer;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ImportAndTransformTask extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {
   
   final Manifold manifold;
   final ImportType importType;
   
   public ImportAndTransformTask(Manifold manifold, ImportType importType) {
      this.manifold = manifold;
      this.importType = importType;
      updateTitle("Import and transform " + importType.toString());
      
      addToTotalWork(3);
      Get.activeTasks().add(this);
   }
   
   @Override
   protected Void call() throws Exception {
      try {
         completedUnitOfWork();
         updateMessage("Importing new content...");
         Rf2DirectImporter importer = new Rf2DirectImporter(importType);
         Future<?> importTask = Get.executor().submit(importer);
         importTask.get();
         completedUnitOfWork();
         
         updateMessage("Transforming to SOLOR...");
         Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer();
         Future<?> transformTask = Get.executor().submit(transformer);
         transformTask.get();
         completedUnitOfWork();
         
//         updateMessage("Classifying new content...");
//         ClassifierService classifierService = Get.logicService().getClassifierService(manifold, manifold.getEditCoordinate());
//         Future<?> classifyTask = classifierService.classify();
//         classifyTask.get();
//         completedUnitOfWork();
         
         return null;
      } finally {
         this.done();
         Get.taxonomyService().notifyTaxonomyListenersToRefresh();
         Get.activeTasks().remove(this);
      }
   }
   
}
