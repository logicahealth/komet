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



package sh.isaac.komet.gui.treeview;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.task.TaskWrapper;

//~--- classes ----------------------------------------------------------------

/**
 * TODO left in as and example, but taxonomy snapshot generates in the background without the caller having to
 * be aware of the background activity, and results can be obtained via the taxonomy snapshot via alternative
 * (but slower) means while the background task completes. After crating other examples, this example to handle things like classification,
 * this class can be removed, and the classes that refer to it can be updated.
 * @author kec
 */
public class CreateSnapshotService
        extends Service<TaxonomySnapshotService> {
   ManifoldCoordinate manifoldCoordinate;

   //~--- constructors --------------------------------------------------------

   public CreateSnapshotService(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected Task<TaxonomySnapshotService> createTask() {
      return new TaskWrapper(
          new Task<TaxonomySnapshotService>() {
             @Override
             protected TaxonomySnapshotService call()
                      throws Exception {
                return Get.taxonomyService()
                          .getSnapshot(manifoldCoordinate);
             }
          },
              (t) -> {
                 return t;
              },
          "Snapshot service generating taxonomy");
   }

   //~--- get methods ---------------------------------------------------------

   public ManifoldCoordinate getManifoldCoordinate() {
      return manifoldCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   public void setManifoldCoordinate(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
   }
}

