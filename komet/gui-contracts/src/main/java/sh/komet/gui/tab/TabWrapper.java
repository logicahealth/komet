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
package sh.komet.gui.tab;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import sh.isaac.komet.iconography.Iconography;
import static sh.komet.gui.style.StyleClasses.ADD_TAB_MENU_BUTTON;

/**
 * Wrap a tab in a StackPane so that we can add a "add" button for adding new
 * tabs. The .css file will need to add space in the upper left corner for the
 * control. 
 * @author kec
 */
public class TabWrapper extends StackPane {
   final MenuButton addTabMenuButton;
   private TabWrapper(MenuButton addTabMenuButton) {
      super();
      this.addTabMenuButton = addTabMenuButton;
   }

   public static TabWrapper wrap(TabPane dragAndDropTabPane, MenuItem... items) {
      MenuButton addTabMenuButton = new MenuButton("",
              Iconography.PLUS.getIconographic(), items);
      TabWrapper tabWrapper = new TabWrapper(addTabMenuButton);
      addTabMenuButton.getStyleClass().add(ADD_TAB_MENU_BUTTON.toString());
      StackPane.setAlignment(addTabMenuButton, Pos.TOP_LEFT);
      StackPane.setAlignment(dragAndDropTabPane, Pos.TOP_LEFT);
      tabWrapper.getChildren().addAll(dragAndDropTabPane, addTabMenuButton);
      
      return tabWrapper;
   }

   public MenuButton getAddTabMenuButton() {
      return addTabMenuButton;
   }
}
