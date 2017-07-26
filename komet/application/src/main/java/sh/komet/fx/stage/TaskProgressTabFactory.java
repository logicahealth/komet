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



package sh.komet.fx.stage;

//~--- non-JDK imports --------------------------------------------------------

import java.util.function.Consumer;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;


import org.controlsfx.control.TaskProgressView;
import sh.isaac.api.Get;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.ticker.Ticker;
import sh.isaac.komet.iconography.Iconography;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class TaskProgressTabFactory {
   public static Tab create() {
      final TaskProgressView<Task<?>> view = new TaskProgressView<>();
      final Tab tab = new Tab("Activity");
      ActiveTasks activeTasks = Get.activeTasks();
      final Tooltip activeTasksTooltip = new Tooltip("No active tasks...");
      
      activeTasks.get().addListener((SetChangeListener.Change<? extends Task<?>> change) -> {
         if (change.wasAdded()) {
            view.getTasks().add(change.getElementAdded());
         }
         if (change.wasRemoved()) {
            view.getTasks().remove(change.getElementRemoved());
         }
         
         switch (change.getSet().size()) {
            case 0: activeTasksTooltip.setText("No active tasks");
            break;
            default: activeTasksTooltip.setText(change.getSet().size() + " active tasks");
         }
      });

      tab.setTooltip(activeTasksTooltip);

      Ticker sortTicker = new Ticker();
      sortTicker.start(2, new Consumer() {
         @Override
         public void accept(Object t) {
            if (view.getTasks().isEmpty()) {
               if (tab.getGraphic() != null) {
                  tab.setGraphic(null);
               }
            } else {
               if (tab.getGraphic() == null) {
                  tab.setGraphic(new ProgressIndicator());
               }
            }
         }
      });

      tab.setContent(new ScrollPane(view));
      view.setGraphicFactory((Task<?> param) -> Iconography.RUN.getIconographic());
      return tab;
   }
}

