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

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public abstract class TaskProgressNode
        extends ExplorationNodeAbstract {

   final TaskProgressView<Task<?>> taskProgressView = new TaskProgressView<>();
   final SimpleStringProperty activeTasksTooltip = new SimpleStringProperty("No tasks...");
   final SimpleStringProperty title = new SimpleStringProperty(TaskProgressNodeFactory.TITLE_BASE);
   final SimpleStringProperty titledNodeTitle = new SimpleStringProperty(TaskProgressNodeFactory.TITLE_BASE);
   final AnchorPane anchorPane = new AnchorPane();
   final ScrollPane scrollPane;
   protected Label titleLabel = null;

   //~--- constructors --------------------------------------------------------
   public TaskProgressNode(ViewProperties viewProperties) {
       super(viewProperties);

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
              .addListener(this::boundsListener);
   }

   //~--- methods -------------------------------------------------------------

    private void boundsListener(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        taskProgressView.setMinWidth(newValue.getWidth());
        taskProgressView.setPrefWidth(newValue.getWidth());
        taskProgressView.setMaxWidth(newValue.getWidth());
        taskProgressView.setMinHeight(newValue.getHeight());
        taskProgressView.setPrefHeight(newValue.getHeight());
        taskProgressView.setMaxHeight(Double.MAX_VALUE);

    }


   //~--- get methods ---------------------------------------------------------
   @Override
   public Node getNode() {
      return taskProgressView;
   }


}
