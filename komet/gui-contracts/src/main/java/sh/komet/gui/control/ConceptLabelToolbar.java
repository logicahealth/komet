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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ConceptLabelToolbar implements ChangeListener<String> {

   final MenuButton manifoldLinkMenu = new MenuButton();
   final ConceptLabel conceptLabel;
   final Manifold manifold;

   public void manifoldEventHandler(Event event) {
      MenuItem menuItem = (MenuItem) event.getSource();
      String manifoldGroup = (String) menuItem.getUserData();
      ConceptSpecification spec = manifold.getConceptForGroup(manifoldGroup);
      if (spec != null) {
         ConceptChronology focusedConcept = Get.concept(spec);
         manifold.setFocusedConceptChronology(focusedConcept);
      } else {
         manifold.setFocusedConceptChronology(null);
      }
      manifold.setGroupName(manifoldGroup);
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
      this.conceptLabel = new ConceptLabel(manifold, ConceptLabel::setFullySpecifiedText);

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
      manifold.groupNameProperty().addListener(new WeakChangeListener<>(this));
// History
// 
   }

   public static Node make(Manifold manifold) {

      ConceptLabelToolbar gctb = new ConceptLabelToolbar(manifold);
      GridPane toolBarGrid = new GridPane();
      GridPane.setConstraints(gctb.manifoldLinkMenu, 0, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
      toolBarGrid.getChildren().add(gctb.manifoldLinkMenu);
      GridPane.setConstraints(gctb.conceptLabel, 1, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
      toolBarGrid.getChildren().add(gctb.conceptLabel);
      
      toolBarGrid.getStyleClass().add("concept-label-toolbar");
      return toolBarGrid;
   }

   @Override
   public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
      manifoldLinkMenu.setGraphic(getNodeForManifold(manifold));      
   }
}
