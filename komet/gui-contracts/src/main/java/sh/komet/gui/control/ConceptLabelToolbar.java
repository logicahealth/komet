/*
 * Copyright 2017 Your Organisation.
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

import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.control.ConceptLabel;

/**
 *
 * @author kec
 */
public class ConceptLabelToolbar implements ChangeListener<String> {

   final MenuButton manifoldLinkMenu = new MenuButton();
   final ArrayList<Control> barControls = new ArrayList();
   final Manifold manifold;

   public void manifoldEventHandler(Event event) {
      MenuItem menuItem = (MenuItem) event.getSource();
      String manifoldGroup = (String) menuItem.getUserData();
      
      ConceptChronology focusedConcept = manifold.getConceptForGroup(manifoldGroup);
      manifold.setGroupName(manifoldGroup);
      manifold.setFocusedConceptChronology(focusedConcept);
      manifoldLinkMenu.setGraphic(getNodeForManifold(manifoldGroup));
   }

   public final Node getNodeForManifold(Manifold manifold) {
      return getNodeForManifold(manifold.getGroupName());
   }
   // TODO make the manifold menu it's own object to be used in many places
   public final Node getNodeForManifold(String manifoldGroup) {
      HBox combinedGraphic = new HBox(1);
      combinedGraphic.setMinWidth(45);
      combinedGraphic.setPrefWidth(45);
      combinedGraphic.setMaxWidth(45);
      if (manifoldGroup.equals(Manifold.UNLINKED_GROUP_NAME)) {
         Node linkBroken = Iconography.LINK_BROKEN.getIconographic();
         Rectangle rect = new Rectangle(16, 16, Color.TRANSPARENT);
         combinedGraphic.getChildren().addAll(linkBroken,rect);
      } else {
         combinedGraphic.getChildren().addAll(
              Iconography.LINK.getIconographic(), 
         Manifold.getIconographic(manifoldGroup));
      }
      return combinedGraphic;
   }

   private ConceptLabelToolbar(Manifold manifold) {
      this.manifold = manifold;

      // Manifold
      Manifold.getGroupNames().stream().map((m) -> {
         MenuItem manifoldItem = new MenuItem(m, getNodeForManifold(m));
         manifoldItem.setUserData(m);
         manifoldItem.addEventHandler(ActionEvent.ACTION, this::manifoldEventHandler);
         return manifoldItem;
      }).forEachOrdered((manifoldItem) -> {
         manifoldLinkMenu.getItems().add(manifoldItem);
      });

      manifoldLinkMenu.setGraphic(getNodeForManifold(manifold));

      barControls.add(manifoldLinkMenu);
      barControls.add(new ConceptLabel(manifold, ConceptLabel::setFullySpecifiedText));
      manifold.groupNameProperty().addListener(new WeakChangeListener<>(this));
// History
// 
   }

   public static ToolBar make(Manifold manifold) {

      ConceptLabelToolbar gctb = new ConceptLabelToolbar(manifold);
      ToolBar toolbar = new ToolBar(gctb.barControls.toArray(new Control[gctb.barControls.size()]));
      toolbar.getStyleClass().add("concept-label-toolbar");
      toolbar.setUserData(gctb);
      return toolbar;

   }

   @Override
   public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
      manifoldLinkMenu.setGraphic(getNodeForManifold(manifold));      
   }
}
