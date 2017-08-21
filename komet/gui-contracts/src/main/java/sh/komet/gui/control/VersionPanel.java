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
package sh.komet.gui.control;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public final class VersionPanel  extends BadgedVersionPanel {
   
   CheckBox revertCheckBox = new CheckBox();
   public VersionPanel(Manifold manifold, ObservableCategorizedVersion categorizedVersion) {
      super(manifold, categorizedVersion);
      revertCheckBox.setSelected(false);
      this.getStyleClass()
              .add(StyleClasses.VERSION_PANEL.toString());
      this.expandControl.setVisible(false);
      //this.setBackground(new Background(new BackgroundFill(Color.IVORY, CornerRadii.EMPTY, Insets.EMPTY)));
   }

   @Override
   public void addExtras() {
      
      // move the badge, replace edit control with revert  checkbox.  
      gridpane.getChildren()
              .remove(editControl);
      gridpane.getChildren()
              .remove(stampControl);
      gridpane.getChildren()
              .remove(revertCheckBox);
      GridPane.setConstraints(stampControl, columns, 0, 1, 1, HPos.LEFT, VPos.BASELINE, Priority.NEVER, Priority.NEVER);
      gridpane.getChildren()
              .add(stampControl);
      GridPane.setConstraints(revertCheckBox, columns+1, 0, 1, 1, HPos.RIGHT, VPos.BASELINE, Priority.NEVER, Priority.NEVER, new Insets(0,4,1,0));
      gridpane.getChildren()
              .add(revertCheckBox);
   }

   @Override
   protected boolean isLatestPanel() {
      return false;
   }
}
