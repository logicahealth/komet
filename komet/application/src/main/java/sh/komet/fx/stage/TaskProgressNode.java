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

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.TaskProgressView;
import sh.isaac.api.Get;
import sh.isaac.api.progress.ActiveTasks;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class TaskProgressNode implements ExplorationNode {
   final Manifold manifold;
   final TaskProgressView<Task<?>> taskProgressView = new TaskProgressView<>();
   final SimpleStringProperty activeTasksTooltip = new SimpleStringProperty("No active tasks...");
   final ScrollPane scrollPane;
   final AnchorPane anchorPane = new AnchorPane();

   public TaskProgressNode(Manifold manifold) {
      this.manifold = manifold;
      ActiveTasks activeTasks = Get.activeTasks();
      
      activeTasks.get().addListener((SetChangeListener.Change<? extends Task<?>> change) -> {
         if (change.wasAdded()) {
            taskProgressView.getTasks().add(change.getElementAdded());
         }
         if (change.wasRemoved()) {
            taskProgressView.getTasks().remove(change.getElementRemoved());
         }
         
         switch (change.getSet().size()) {
            case 0: activeTasksTooltip.set("No active tasks");
            break;
            default: activeTasksTooltip.set(change.getSet().size() + " active tasks");
         }
      });
      scrollPane = new ScrollPane(taskProgressView);
      scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      AnchorPane.setBottomAnchor(scrollPane, 1.0);
      AnchorPane.setLeftAnchor(scrollPane, 1.0);
      AnchorPane.setRightAnchor(scrollPane, 1.0);
      AnchorPane.setTopAnchor(scrollPane, 1.0);
      anchorPane.getChildren().add(scrollPane);
      anchorPane.widthProperty().addListener((observable, oldValue, newValue) -> {
         taskProgressView.setMinWidth(newValue.doubleValue());
         taskProgressView.setPrefWidth(newValue.doubleValue());
         taskProgressView.setMaxWidth(newValue.doubleValue());
      });
      taskProgressView.setMaxHeight(2000);
   }
   
   @Override
   public Manifold getManifold() {
      return this.manifold;
   }

   @Override
   public Node getNode() {
      return anchorPane;
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return activeTasksTooltip;
   }
}
