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

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;

import javafx.concurrent.Task;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import javafx.util.Callback;

import sh.isaac.api.task.TimedTask;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.interfaces.IconProvider;
 /* Modified from org.controlsfx.control.TaskProgressViewSkin per the below:
 *
 * Copyright (c) 2014, 2015 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * Copyright (c) 2014, 2015 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//~--- classes ----------------------------------------------------------------
*/

/**

 *
 * @author kec
 * @param <T>
 */
public class KometProgressSkin<T extends Task<?>>
        extends SkinBase<KometProgressView<T>> {
   ListView<T> listView;

   //~--- constructors --------------------------------------------------------

   public KometProgressSkin(KometProgressView<T> monitor) {
      super(monitor);
      monitor.boundsInLocalProperty()
             .addListener(this::updateBounds);

      BorderPane borderPane = new BorderPane();

      borderPane.getStyleClass()
                .add("box");

      // list view
      listView = new ListView<>();
      listView.setPlaceholder(new Label("No tasks running"));
      listView.setCellFactory(param -> new TaskCell());
      listView.setFocusTraversable(false);
      updateBounds(monitor.layoutBoundsProperty(), null, monitor.layoutBoundsProperty()
            .get());
      Bindings.bindContent(listView.getItems(), monitor.getTasks());
      borderPane.setCenter(listView);
      getChildren().add(listView);
   }

   //~--- methods -------------------------------------------------------------

   private void updateBounds(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
      listView.setMinWidth(newValue.getWidth());
      listView.setPrefWidth(newValue.getWidth());
      listView.setMaxWidth(newValue.getWidth());
      listView.setMinHeight(newValue.getHeight());
      listView.setPrefHeight(newValue.getHeight());
      listView.setMaxHeight(4000);
   }

   //~--- inner classes -------------------------------------------------------

   class TaskCell
           extends ListCell<T> {
      private final GridPane    cellGrid = new GridPane();
      private final ProgressBar progressBar;
      private final Label       titleText;
      private final Label       messageText;
      private final Button      cancelButton;
      private T                 task;

      //~--- constructors -----------------------------------------------------

      public TaskCell() {
         double maxWidth = listView.getMaxWidth() - 20;

         this.setMaxWidth(maxWidth);
         this.setPrefWidth(maxWidth);
         titleText = new Label();
         titleText.getStyleClass()
                  .add("task-title");
         titleText.setPrefWidth(maxWidth - 20);
         titleText.setMaxWidth(maxWidth - 20);
         titleText.setWrapText(true);
         messageText = new Label();
         messageText.getStyleClass()
                    .add("task-message");
         messageText.setPrefWidth(maxWidth - 20);
         messageText.setMaxWidth(maxWidth - 20);
         messageText.setWrapText(true);
         progressBar = new ProgressBar();
         progressBar.setMaxWidth(maxWidth - 50);
         progressBar.setMaxHeight(8);
         progressBar.getStyleClass()
                    .add("task-progress-bar");
         cancelButton = new Button("", Iconography.STOP_CIRCLE.getIconographic());
         cancelButton.getStyleClass()
                     .add("task-cancel-button");
         cancelButton.setTooltip(new Tooltip("Cancel Task"));
         cancelButton.setOnAction(
             evt -> {
                if (task != null) {
                   task.cancel(false);
                }
             });
         setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      }

      //~--- methods ----------------------------------------------------------

      public void layoutNodes(Node graphic) {
         cellGrid.getChildren()
                 .clear();

         Insets insets = new Insets(1, 4, 1, 4);

         /*
          * Node child,
          * int columnIndex,
          * int rowIndex,
          * int columnspan,
          * int rowspan,
          * HPos halignment,
          * VPos valignment,
          * Priority hgrow,
          * Priority vgrow
          */
         GridPane.setConstraints(
             titleText,
             0,
             0,
             3,
             1,
             HPos.LEFT,
             VPos.BASELINE,
             Priority.ALWAYS,
             Priority.NEVER,
             insets);
         cellGrid.getChildren()
                 .add(titleText);

         if (graphic != null) {
            GridPane.setConstraints(
                graphic,
                0,
                1,
                1,
                1,
                HPos.LEFT,
                VPos.BASELINE,
                Priority.NEVER,
                Priority.NEVER,
                insets);
            cellGrid.getChildren()
                    .add(graphic);
         }

         GridPane.setConstraints(
             progressBar,
             1,
             1,
             1,
             1,
             HPos.LEFT,
             VPos.CENTER,
             Priority.ALWAYS,
             Priority.NEVER,
             insets);
         cellGrid.getChildren()
                 .add(progressBar);
         GridPane.setConstraints(
             cancelButton,
             2,
             1,
             1,
             1,
             HPos.LEFT,
             VPos.CENTER,
             Priority.NEVER,
             Priority.NEVER,
             insets);
         cellGrid.getChildren()
                 .add(cancelButton);
         GridPane.setConstraints(
             messageText,
             0,
             2,
             3,
             1,
             HPos.LEFT,
             VPos.CENTER,
             Priority.ALWAYS,
             Priority.NEVER,
             insets);
         cellGrid.getChildren()
                 .add(messageText);
      }

      @Override
      public void updateIndex(int index) {
         super.updateIndex(index);

         /*
          * I have no idea why this is necessary but it won't work without
          * it. Shouldn't the updateItem method be enough?
          */
         if (index == -1) {
            setGraphic(null);
            getStyleClass().setAll("task-list-cell-empty");
         }
      }

      @Override
      protected void updateItem(T task, boolean empty) {
         if (task != this.task) {
            super.updateItem(task, empty);
            this.task = task;
            if (this.task != null && this.task instanceof TimedTask &!
                    ((TimedTask) this.task).canCancel()) {
               cancelButton.setVisible(false);
            }

            if (empty || (task == null)) {
               getStyleClass().setAll("task-list-cell-empty");
               setGraphic(null);
            } else {
               getStyleClass().setAll("task-list-cell");
               progressBar.progressProperty()
                          .bind(task.progressProperty());
               titleText.textProperty()
                        .bind(task.titleProperty());
               messageText.textProperty()
                          .bind(task.messageProperty());
               cancelButton.disableProperty()
                           .bind(Bindings.not(task.runningProperty()));
           
    
               Callback<T, Node> factory = getSkinnable().getGraphicFactory();

               if (factory != null) {
                  Node graphic = factory.call(task);

                  layoutNodes(graphic);
               } else {
                  if (task instanceof IconProvider) {
                     layoutNodes(((IconProvider) task).getIcon());
                  } else {
                     layoutNodes(null);
                  }
               }

               setGraphic(cellGrid);
            }
         }
      }
   }
}

