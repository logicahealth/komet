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



package sh.komet.progress.view;

//~--- non-JDK imports --------------------------------------------------------

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.application.Platform;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.SetChangeListener;

import javafx.concurrent.Task;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;

import javafx.util.Duration;

import sh.isaac.api.Get;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class TaskProgressNode
         implements ExplorationNode {
   final KometProgressView<Task<?>> taskProgressView   = new KometProgressView<>();
   final SimpleStringProperty       activeTasksTooltip = new SimpleStringProperty("No active tasks...");
   final SimpleStringProperty       title              = new SimpleStringProperty(TaskProgressNodeFactory.TITLE_BASE);
   final AnchorPane                 anchorPane         = new AnchorPane();
   int                              currentIcon        = 0;
   final Node[]                     progressIcons      = new Node[] {
      Iconography.SPINNER0.getIconographic(), Iconography.SPINNER1.getIconographic(),
      Iconography.SPINNER2.getIconographic(), Iconography.SPINNER3.getIconographic(),
      Iconography.SPINNER4.getIconographic(), Iconography.SPINNER5.getIconographic(),
      Iconography.SPINNER6.getIconographic(), Iconography.SPINNER7.getIconographic(),
   };
   final SimpleObjectProperty<Node> progressIcon = new SimpleObjectProperty<>(progressIcons[0]);
   final Manifold                   manifold;
   final ScrollPane                 scrollPane;

   //~--- constructors --------------------------------------------------------

   public TaskProgressNode(Manifold manifold) {
      this.manifold = manifold;

      ActiveTasks activeTasks = Get.activeTasks();

      activeTasks.get()
                 .addListener(
                     (SetChangeListener.Change<? extends Task<?>> change) -> {

                        if (change.wasAdded()) {
                           Platform.runLater(() -> taskProgressView.getTasks()
                                 .add(change.getElementAdded()));
                        } else if (change.wasRemoved()) {
                           Platform.runLater(() -> taskProgressView.getTasks()
                                 .remove(change.getElementRemoved()));
                        }

                        if (change.getSet()
                                  .isEmpty()) {
                           Platform.runLater(
                               () -> {
                                  activeTasksTooltip.set("No active tasks");
                                  title.set(TaskProgressNodeFactory.TITLE_BASE);
                                  //nextIcon();
                               });
                        } else {
                           Platform.runLater(
                               () -> {
                                  int taskCount = change.getSet()
                                                        .size();

                                  activeTasksTooltip.set(taskCount + " active tasks");
                                  title.set(taskCount + " " + TaskProgressNodeFactory.TITLE_BASE);
                                  //nextIcon();
                               });
                        }
                     });
      scrollPane = new ScrollPane(taskProgressView);
      scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      AnchorPane.setBottomAnchor(scrollPane, 1.0);
      AnchorPane.setLeftAnchor(scrollPane, 1.0);
      AnchorPane.setRightAnchor(scrollPane, 1.0);
      AnchorPane.setTopAnchor(scrollPane, 1.0);
      anchorPane.getChildren()
                .add(scrollPane);
      anchorPane.setMaxHeight(2000);
      anchorPane.layoutBoundsProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                       System.out.println("Progress anchorPane pane: " + newValue);
                       taskProgressView.setMinWidth(newValue.getWidth());
                       taskProgressView.setPrefWidth(newValue.getWidth());
                       taskProgressView.setMaxWidth(newValue.getWidth());
                       taskProgressView.setMinHeight(newValue.getHeight());
                       taskProgressView.setPrefHeight(newValue.getHeight());
                       taskProgressView.setMaxHeight(Double.MAX_VALUE);
                    });
   }

   //~--- methods -------------------------------------------------------------

   public void nextIcon() {
      currentIcon++;

      if (currentIcon == 8) {
         currentIcon = 0;
      }

      progressIcon.setValue(progressIcons[currentIcon]);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ReadOnlyProperty<Node> getIcon() {
      return progressIcon;
   }

   @Override
   public Manifold getManifold() {
      return this.manifold;
   }

   @Override
   public Node getNode() {
      return taskProgressView;
   }

   @Override
   public ReadOnlyProperty<String> getTitle() {
      return title;
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return activeTasksTooltip;
   }
}

