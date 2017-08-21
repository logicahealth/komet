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

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

/**
 *
 * @author kec
 */
public class IconToggle extends ToggleButton {
   Node graphicIfSelected;
   Node grapicNotSelected;

   public IconToggle(Node graphicIfSelected, Node grapicNotSelected) {
      super(null, grapicNotSelected);
      this.graphicIfSelected = graphicIfSelected;
      this.grapicNotSelected = grapicNotSelected;
      this.selectedProperty().addListener(this::stateChanged);
   }
   
   private void stateChanged(ObservableValue<? extends Boolean> selected, Boolean wasSelected, Boolean isSelected) {
      if (isSelected) {
         this.setGraphic(graphicIfSelected);
      } else {
         this.setGraphic(grapicNotSelected);
      }
   }
}
